/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.console.ng.wi.backend.server.dd;

import org.guvnor.common.services.shared.metadata.model.Overview;
import org.jbpm.console.ng.wi.dd.model.DeploymentDescriptorModel;
import org.jbpm.console.ng.wi.dd.model.ItemObjectModel;
import org.jbpm.console.ng.wi.dd.service.DDEditorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.services.shared.project.KieProject;
import org.kie.workbench.common.services.shared.project.KieProjectService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.io.IOService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.rpc.SessionInfo;
import org.uberfire.workbench.events.ResourceAddedEvent;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DDConfigUpdaterTest {

    private static final String JPA_MARSHALLING_STRATEGY = "org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy";


    @Mock
    private IOService ioService;

    @Mock
    private KieProjectService projectService;

    @Mock
    private DDEditorServiceImpl ddEditorService;

    @Mock
    private DDConfigUpdaterHelper configUpdaterHelper;

    @Test
    public void testProcessResourceAdd() {
        // setup things that are going to be tested
        DeploymentDescriptorModel model = new DeploymentDescriptorModel();
        model.setOverview(new Overview());
        DDConfigUpdater ddConfigUpdater = new DDConfigUpdater(ddEditorService, projectService, ioService, configUpdaterHelper);

        /// setup mocks
        Path rootPath = Mockito.mock(Path.class);
        when(rootPath.toURI()).thenReturn("default://project");
        KieProject project = Mockito.mock(KieProject.class);
        when(project.getRootPath()).thenReturn(rootPath);
        // setup mocks behavior
        when(ioService.exists(any(org.uberfire.java.nio.file.Path.class))).thenReturn(true);
        when(configUpdaterHelper.isPersistenceFile(any(Path.class))).thenReturn(true);
        when(configUpdaterHelper.buildJPAMarshallingStrategyValue(any(KieProject.class))).thenReturn(JPA_MARSHALLING_STRATEGY);
        when(projectService.resolveProject(any(Path.class))).thenReturn(project);
        when(ddEditorService.load(any(Path.class))).thenReturn(model);

        // test the method
        ddConfigUpdater.processResourceAdd(new ResourceAddedEvent(Mockito.mock(Path.class), "test resource", Mockito.mock(SessionInfo.class)));
        // check results
        assertNotNull(model.getMarshallingStrategies());
        assertEquals(1, model.getMarshallingStrategies().size());

        ItemObjectModel objectModel = model.getMarshallingStrategies().get(0);
        assertNotNull(objectModel);
        assertEquals(JPA_MARSHALLING_STRATEGY, objectModel.getValue());
        assertEquals("mvel", objectModel.getResolver());
    }
}
