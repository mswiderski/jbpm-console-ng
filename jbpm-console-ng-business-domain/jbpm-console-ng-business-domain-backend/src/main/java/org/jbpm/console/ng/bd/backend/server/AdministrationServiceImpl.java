/*
 * Copyright 2013 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.console.ng.bd.backend.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Named;

import org.guvnor.common.services.project.events.NewProjectEvent;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.ProjectService;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.server.config.ConfigGroup;
import org.guvnor.structure.server.config.ConfigType;
import org.guvnor.structure.server.config.ConfigurationFactory;
import org.guvnor.structure.server.config.ConfigurationService;
import org.jbpm.console.ng.bd.api.Vfs;
import org.jbpm.console.ng.bd.service.AdministrationService;
import org.jbpm.console.ng.bd.service.DeploymentManagerEntryPoint;
import org.jbpm.console.ng.bd.service.DeploymentUnitProvider;
import org.jbpm.console.ng.bd.service.Initializable;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorManager;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.cdi.Kjar;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.Path;

@ApplicationScoped
public class AdministrationServiceImpl implements AdministrationService {

    private static final String DEPLOYMENT_SERVICE_TYPE_CONFIG = "deployment.service";
    private static final Logger logger = LoggerFactory.getLogger(AdministrationServiceImpl.class);
    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private RepositoryService repositoryService;

    @Inject
    private OrganizationalUnitService organizationalUnitService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ConfigurationFactory configurationFactory;

    @Inject
    private DeploymentManagerEntryPoint deploymentManager;

    @Inject
    private ProjectService projectService;

    @Inject
    @Any
    private Instance<DeploymentService> deploymentService;

    @Inject
    @Any
    private Instance<DeploymentUnitProvider<DeploymentUnit>> deploymentUnitProviders;

    private String deploymentServiceType;
    
    /**
     * This flag is necessary to let dependent services know when the deployments have been bootstrapped. 
     * Because retrieving the status of the deployments will be frequently called, it's more efficient
     *  to use a boolean to save this status than to have the dependent services call produceDeploymentUnits().
     */
    private volatile boolean bootstrapDeploymentsDone = false;

    /* (non-Javadoc)
     * @see org.jbpm.console.ng.bd.backend.server.AdministrationService#bootstrapRepository(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void bootstrapRepository( String ou,
                                     String repoAlias,
                                     String repoUrl,
                                     String userName,
                                     String password ) {

        Repository repository = null;
        try {
            repository = repositoryService.getRepository( repoAlias );
            if ( repository == null ) {

                final Map<String, Object> env = new HashMap<String, Object>( 3 );
                if (repoUrl != null) {
                    env.put( "origin", repoUrl );
                }
                env.put( "username", userName );
                env.put( "crypt:password", password );

                repository = repositoryService.createRepository( "git", repoAlias, env );
            }
        } catch (Exception e) {
            // don't fail on creation of repository, just log the cause
            logger.warn("Unable to create repository with alias {} due to {}", repoAlias, e.getMessage());
        }

        OrganizationalUnit demoOrganizationalUnit = organizationalUnitService.getOrganizationalUnit( ou );
        if ( demoOrganizationalUnit == null ) {
            List<Repository> repositories = new ArrayList<Repository>();
            if (repository != null) {
                repositories.add(repository);
            }
            organizationalUnitService.createOrganizationalUnit( ou, ou+"@jbpm.org", repositories );

        } else {
            Collection<Repository> repositories = demoOrganizationalUnit.getRepositories();
            if (repositories != null) {
                boolean found = false;
                for (Repository repo : repositories) {
                    if (repo.getAlias().equals(repository.getAlias())) {
                        found = true;
                    }
                }
                if (!found) {
                    organizationalUnitService.addRepository(demoOrganizationalUnit, repository);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jbpm.console.ng.bd.backend.server.AdministrationService#bootstrapConfig()
     */
    public void bootstrapConfig() {
        ConfigGroup deploymentServiceTypeConfig = null;
        List<ConfigGroup> configGroups = configurationService.getConfiguration( ConfigType.GLOBAL );
        if ( configGroups != null ) {
            for ( ConfigGroup configGroup : configGroups ) {
                if ( DEPLOYMENT_SERVICE_TYPE_CONFIG.equals( configGroup.getName() ) ) {
                    deploymentServiceTypeConfig = configGroup;
                    break;
                }
            }
        }
        if ( deploymentServiceTypeConfig == null ) {
            deploymentServiceTypeConfig = configurationFactory.newConfigGroup( ConfigType.GLOBAL,
                    DEPLOYMENT_SERVICE_TYPE_CONFIG, "" );
            deploymentServiceTypeConfig.addConfigItem( configurationFactory.newConfigItem( "type", "kjar" ) );

            configurationService.addConfiguration( deploymentServiceTypeConfig );
        }

        deploymentServiceType = deploymentServiceTypeConfig.getConfigItemValue( "type" );
    }

    /* (non-Javadoc)
     * @see org.jbpm.console.ng.bd.backend.server.AdministrationService#bootstrapDeployments()
     */
    public void bootstrapDeployments() {
        Set<DeploymentUnit> deploymentUnits = produceDeploymentUnits();
        ( (Initializable) deploymentManager ).initDeployments( deploymentUnits );
        bootstrapDeploymentsDone = true;
    }

    /* (non-Javadoc)
     * @see org.jbpm.console.ng.bd.backend.server.AdministrationService#areDeploymentsBootstrapped()
     */
    public boolean getBootstrapDeploymentsDone() { 
        return bootstrapDeploymentsDone;
    }
    
    /* (non-Javadoc)
     * @see org.jbpm.console.ng.bd.backend.server.AdministrationService#produceDeploymentUnits()
     */
    @Produces
    @RequestScoped
    @Named("DeployList")
    public Set<DeploymentUnit> produceDeploymentUnits() {
        Set<DeploymentUnit> deploymentUnits = new HashSet<DeploymentUnit>();

        Instance<DeploymentUnitProvider<DeploymentUnit>> suitableProviders = this.deploymentUnitProviders.select( getDeploymentType() );

        for ( DeploymentUnitProvider provider : suitableProviders ) {
            deploymentUnits.addAll( provider.getDeploymentUnits() );
        }

        return deploymentUnits;
    }

    /* (non-Javadoc)
     * @see org.jbpm.console.ng.bd.backend.server.AdministrationService#getDeploymentService()
     */
    @Produces
    public DeploymentService getDeploymentService() {
        return this.deploymentService.select( getDeploymentType() ).get();
    }

    @Override
    public void bootstrapProject(String repoAlias, String group, String artifact, String version) {
        GAV gav = new GAV(group, artifact, version);
        try {
            Repository repository = repositoryService.getRepository(repoAlias);
            if (repository != null) {
                String projectLocation = repository.getUri() + ioService.getFileSystem(URI.create(repository.getUri())).getSeparator() + artifact;
                if (!ioService.exists(ioService.get(URI.create(projectLocation)))) {
                    projectService.newProject(repository, artifact, new POM(gav), "/");
                }
            } else {
                logger.error("Repository " + repoAlias + " was not found, cannot add project");
            }
        } catch (Exception e) {
            logger.error("Unable to bootstrap project {} in repository {}", gav, repoAlias, e);
        }
    }

    protected AnnotationLiteral getDeploymentType() {
        if ( deploymentServiceType.equals( "kjar" ) ) {
            return new AnnotationLiteral<Kjar>() {
            };
        } else if ( deploymentServiceType.equals( "vfs" ) ) {
            return new AnnotationLiteral<Vfs>() {
            };
        } else {
            throw new IllegalStateException( "Unknown type of deployment service " + deploymentServiceType );
        }
    }

    public void createDeploymentDescriptor(@Observes NewProjectEvent newProjectEvent) {
        Project project = newProjectEvent.getProject();
        URI projectRootURI =  URI.create(project.getRootPath().toURI());
        String repositoryAlias = projectRootURI.getHost();
        String metaInfPath = Paths.convert(project.getKModuleXMLPath()).getParent().toUri().toString();
        String separator = Paths.convert(project.getRootPath()).getFileSystem().getSeparator();
        String deploymentDescriptorPath = metaInfPath + separator + "kie-deployment-descriptor.xml";
        Path ddVFSPath = ioService.get(URI.create(deploymentDescriptorPath));
        if (!ioService.exists(ddVFSPath)) {
            DeploymentDescriptor dd = new DeploymentDescriptorManager("org.jbpm.domain").getDefaultDescriptor();
            Set<String> roles = new HashSet<String>(project.getRoles());

            Repository repo = repositoryService.getRepository(repositoryAlias);
            if (repo != null) {
                roles.addAll(repo.getRoles());
            }
            dd.getBuilder().setRequiredRoles(new ArrayList<String>(roles));

            String xmlDescriptor = dd.toXml();
            ioService.write(ddVFSPath, xmlDescriptor);
        }
    }



}
