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
package org.jbpm.dashboard.renderer.backend;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dashbuilder.dataset.def.DataSetDef;
import org.dashbuilder.dataset.def.DataSetDefFactory;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.console.ng.bd.model.ProcessInstanceDataSetConstants;
import org.jbpm.dashboard.dataset.integration.KieServerDataSetProvider;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.controller.api.model.events.ServerInstanceConnected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.commons.async.SimpleAsyncExecutorService;
import org.uberfire.commons.services.cdi.Startup;

import static org.jbpm.dashboard.renderer.model.DashboardData.*;

@Startup
@ApplicationScoped
public class DataSetDefsBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(DataSetDefsBootstrap.class);

    public static final String TASKS_MONITORING_DATASET = "tasksMonitoring";
    public static final String PROCESSES_MONITORING_DATASET = "processesMonitoring";

    @Inject
    DataSetDefRegistry dataSetDefRegistry;

    @Inject
    private KieServerIntegration kieServerIntegration;

    private DataSetDef processMonitoringDef;
    private DataSetDef taskMonitoringDef;

    @PostConstruct
    protected void registerDataSetDefinitions() {
        final String jbpmDataSource = "${"+ KieServerConstants.CFG_PERSISTANCE_DS + "}";

        processMonitoringDef = DataSetDefFactory.newSQLDataSetDef()
                .uuid(PROCESSES_MONITORING_DATASET)
                .name("Processes monitoring")
                .dataSource(jbpmDataSource)
                .dbSQL("select " +
                        "log.processInstanceId, " +
                        "log.processId, " +
                        "log.start_date, " +
                        "log.end_date, " +
                        "log.status, " +
                        "log.duration, " +
                        "log.user_identity, " +
                        "log.processVersion, " +
                        "log.processName, " +
                        "log.externalId " +
                        "from " +
                        "ProcessInstanceLog log", false)
                .number(ProcessInstanceDataSetConstants.COLUMN_PROCESS_INSTANCE_ID)
                .label(ProcessInstanceDataSetConstants.COLUMN_PROCESS_ID)
                .date(COLUMN_PROCESS_START_DATE)
                .date(COLUMN_PROCESS_END_DATE)
                .number(COLUMN_PROCESS_STATUS)
                .number(COLUMN_PROCESS_DURATION)
                .label(COLUMN_PROCESS_USER_ID)
                .label(COLUMN_PROCESS_VERSION)
                .label(COLUMN_PROCESS_NAME)
                .label(COLUMN_PROCESS_EXTERNAL_ID)
                .buildDef();

        taskMonitoringDef = DataSetDefFactory.newSQLDataSetDef()
                .uuid(TASKS_MONITORING_DATASET)
                .name("Tasks monitoring")
                .dataSource(jbpmDataSource)
                .dbSQL("select " +
                                "p.processName, " +
                                "p.externalId, " +
                                "t.taskId, " +
                                "t.taskName, " +
                                "t.status, " +
                                "t.createdDate, " +
                                "t.startDate, " +
                                "t.endDate, " +
                                "t.processInstanceId, " +
                                "t.userId, " +
                                "t.duration " +
                                "from ProcessInstanceLog p " +
                                "inner join BAMTaskSummary t on (t.processInstanceId = p.processInstanceId) " +
                                "inner join (select min(pk) as pk from BAMTaskSummary group by taskId) d on t.pk = d.pk",
                        true)
                .label(COLUMN_PROCESS_NAME)
                .label(COLUMN_PROCESS_EXTERNAL_ID)
                .label(COLUMN_TASK_ID)
                .label(COLUMN_TASK_NAME)
                .label(COLUMN_TASK_STATUS)
                .date(COLUMN_TASK_CREATED_DATE)
                .date(COLUMN_TASK_START_DATE)
                .date(COLUMN_TASK_END_DATE)
                .number(COLUMN_PROCESS_INSTANCE_ID)
                .label(COLUMN_TASK_OWNER_ID)
                .number(COLUMN_TASK_DURATION)
                .buildDef();

        // Hide all these internal data set from end user view
        processMonitoringDef.setPublic(false);
        processMonitoringDef.setProvider(KieServerDataSetProvider.TYPE);
        taskMonitoringDef.setPublic(false);
        taskMonitoringDef.setProvider(KieServerDataSetProvider.TYPE);

        // Register the data set definitions
        dataSetDefRegistry.registerDataSetDef(processMonitoringDef);
        dataSetDefRegistry.registerDataSetDef(taskMonitoringDef);
        logger.info("Process dashboard datasets registered");

    }

    public void registerInKieServer(@Observes final ServerInstanceConnected serverInstanceConnected) {
        SimpleAsyncExecutorService.getDefaultInstance().execute(new Runnable() {
            @Override
            public void run() {

                String serverTemplateId = serverInstanceConnected.getServerInstance().getServerTemplateId();
                String serverInstanceId = serverInstanceConnected.getServerInstance().getServerInstanceId();
                try {
                    long waitLimit = 5 * 60 * 1000;   // default 5 min
                    long elapsed = 0;

                    logger.info("Registering process instance data set definitions on connected server instance '{}'", serverInstanceId);
                    QueryServicesClient queryClient = kieServerIntegration.getAdminServerClient(serverTemplateId).getServicesClient(QueryServicesClient.class);
                    QueryDefinition processMonitoringDefDefinition = QueryDefinition.builder()
                            .name(processMonitoringDef.getUUID())
                            .expression(((SQLDataSetDef) processMonitoringDef).getDbSQL())
                            .source(((SQLDataSetDef) processMonitoringDef).getDataSource())
                            .target("CUSTOM")
                            .build();

                    QueryDefinition taskMonitoringDefDefinition = QueryDefinition.builder()
                            .name(taskMonitoringDef.getUUID())
                            .expression(((SQLDataSetDef) taskMonitoringDef).getDbSQL())
                            .source(((SQLDataSetDef) taskMonitoringDef).getDataSource())
                            .target("CUSTOM")
                            .build();

                    while (elapsed < waitLimit) {
                        try {

                            queryClient.replaceQuery(processMonitoringDefDefinition);
                            logger.info("Query {} definition successfully registered on kie server '{}'", PROCESSES_MONITORING_DATASET, serverInstanceId);

                            queryClient.replaceQuery(taskMonitoringDefDefinition);
                            logger.info("Query {} definition successfully registered on kie server '{}'", TASKS_MONITORING_DATASET, serverInstanceId);
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
