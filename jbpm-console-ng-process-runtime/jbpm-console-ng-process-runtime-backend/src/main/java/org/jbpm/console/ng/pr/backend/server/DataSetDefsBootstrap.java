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
package org.jbpm.console.ng.pr.backend.server;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefFactory;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.dashboard.dataset.integration.KieServerDataSetProvider;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.controller.api.model.events.ServerInstanceUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.async.SimpleAsyncExecutorService;
import org.uberfire.commons.services.cdi.Startup;

import static org.jbpm.console.ng.bd.model.ProcessInstanceDataSetConstants.*;

@Startup
@ApplicationScoped
public class DataSetDefsBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(DataSetDefsBootstrap.class);

    @Inject
    DataSetDefRegistry dataSetDefRegistry;

    @Inject
    private KieServerIntegration kieServerIntegration;

    private DataSetDef processInstancesDef;
    private DataSetDef processWithVariablesDef;

    @PostConstruct
    protected void registerDataSetDefinitions() {
        final String jbpmDataSource = "${"+ KieServerConstants.CFG_PERSISTANCE_DS + "}";

        processInstancesDef = DataSetDefFactory.newSQLDataSetDef()
                .uuid(PROCESS_INSTANCE_DATASET)
                .name("Process Instances")
                .dataSource(jbpmDataSource)
                .dbSQL("select " +
                            "log.processInstanceId, " +
                            "log.processId, " +
                            "log.start_date, " +
                            "log.end_date, " +
                            "log.status, " +
                            "log.parentProcessInstanceId, " +
                            "log.outcome, " +
                            "log.duration, " +
                            "log.user_identity, " +
                            "log.processVersion, " +
                            "log.processName, " +
                            "log.correlationKey, " +
                            "log.externalId, " +
                            "log.processInstanceDescription " +
                        "from " +
                            "ProcessInstanceLog log", false)
                .number(COLUMN_PROCESS_INSTANCE_ID)
                .label(COLUMN_PROCESS_ID)
                .date(COLUMN_START)
                .date(COLUMN_END)
                .number(COLUMN_STATUS)
                .number(COLUMN_PARENT_PROCESS_INSTANCE_ID)
                .label(COLUMN_OUTCOME)
                .number(COLUMN_DURATION)
                .label(COLUMN_IDENTITY)
                .label(COLUMN_PROCESS_VERSION)
                .label(COLUMN_PROCESS_NAME)
                .label(COLUMN_CORRELATION_KEY)
                .label(COLUMN_EXTERNAL_ID)
                .label(COLUMN_PROCESS_INSTANCE_DESCRIPTION)
                .buildDef();


        processWithVariablesDef = DataSetDefFactory.newSQLDataSetDef()
                .uuid(PROCESS_INSTANCE_WITH_VARIABLES_DATASET)
                .name("Domain Specific Process Instances")
                .dataSource(jbpmDataSource)
                .dbSQL("select " +
                            "vil.processInstanceId, " +
                            "vil.processId, " +
                            "vil.id, " +
                            "vil.variableId, " +
                            "vil.value " +
                        "from VariableInstanceLog vil " +
                        "where " +
                            "vil.id = " +
                                "(select MAX(v.id) " +
                                "from VariableInstanceLog v " +
                                "where " +
                                "v.variableId = vil.variableId and " +
                                "v.processInstanceId = vil.processInstanceId)" , false )
                .number(PROCESS_INSTANCE_ID)
                .label(PROCESS_NAME)
                .number(VARIABLE_ID)
                .label(VARIABLE_NAME)
                .label(VARIABLE_VALUE)
                .buildDef();

        // Hide all these internal data set from end user view
        processInstancesDef.setPublic(false);
        processInstancesDef.setProvider(KieServerDataSetProvider.TYPE);
        processWithVariablesDef.setPublic(false);
        processWithVariablesDef.setProvider(KieServerDataSetProvider.TYPE);

        // Register the data set definitions
        dataSetDefRegistry.registerDataSetDef(processInstancesDef);
        dataSetDefRegistry.registerDataSetDef(processWithVariablesDef);
        logger.info("Process instance datasets registered");
    }

    public void registerInKieServer(@Observes final ServerInstanceUpdated serverInstanceUpdated) {
        SimpleAsyncExecutorService.getDefaultInstance().execute(new Runnable() {
            @Override
            public void run() {

                String serverTemplateId = serverInstanceUpdated.getServerInstance().getServerTemplateId();
                String serverInstanceId = serverInstanceUpdated.getServerInstance().getServerInstanceId();
                try {
                    long waitLimit = 5 * 60 * 1000;   // default 5 min
                    long elapsed = 0;

                    logger.info("Registering process instance data set definitions on connected server instance '{}'", serverInstanceId);
                    QueryServicesClient queryClient = kieServerIntegration.getAdminServerClient(serverTemplateId).getServicesClient(QueryServicesClient.class);
                    QueryDefinition processInstancesDefinition = QueryDefinition.builder()
                            .name(processInstancesDef.getUUID())
                            .expression(((SQLDataSetDef) processInstancesDef).getDbSQL())
                            .source(((SQLDataSetDef) processInstancesDef).getDataSource())
                            .target("CUSTOM")
                            .build();

                    QueryDefinition processWithVariablesDefinition = QueryDefinition.builder()
                            .name(processWithVariablesDef.getUUID())
                            .expression(((SQLDataSetDef) processWithVariablesDef).getDbSQL())
                            .source(((SQLDataSetDef) processWithVariablesDef).getDataSource())
                            .target("CUSTOM")
                            .build();

                    while (elapsed < waitLimit) {
                        try {

                            queryClient.replaceQuery(processInstancesDefinition);
                            logger.info("Query {} definition successfully registered on kie server '{}'", PROCESS_INSTANCE_DATASET, serverInstanceId);

                            queryClient.replaceQuery(processWithVariablesDefinition);
                            logger.info("Query {} definition successfully registered on kie server '{}'", PROCESS_INSTANCE_WITH_VARIABLES_DATASET, serverInstanceId);
                            return;
                        } catch (KieServicesException e) {
                            // unable to register, might still be booting
                            Thread.sleep(500);
                            elapsed += 500;
                            logger.debug("Cannot reach KIE Server, elapsed time while waiting '{}', max time '{}'", elapsed, waitLimit);

                        }
                    }
                    logger.warn("Timeout while trying to register process instance query definition on '{}'", serverInstanceId);
                } catch (Exception e) {
                    logger.warn("Unable to register process instance queries on '{}' due to {}", serverInstanceId, e.getMessage(), e);

                }
            }
        });
    }
}
