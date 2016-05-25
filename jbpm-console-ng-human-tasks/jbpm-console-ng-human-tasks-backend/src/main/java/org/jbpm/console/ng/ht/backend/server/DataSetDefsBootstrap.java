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
package org.jbpm.console.ng.ht.backend.server;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.dashbuilder.dataprovider.DataSetProviderType;
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
import org.uberfire.commons.async.DisposableExecutor;
import org.uberfire.commons.async.SimpleAsyncExecutorService;
import org.uberfire.commons.services.cdi.Startup;

import static org.jbpm.console.ng.ht.model.TaskDataSetConstants.*;

@Startup
@ApplicationScoped
public class DataSetDefsBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(DataSetDefsBootstrap.class);

    @Inject
    protected DataSetDefRegistry dataSetDefRegistry;

    @Inject
    private KieServerIntegration kieServerIntegration;

    private DataSetDef humanTasksDef;
    private DataSetDef humanTasksWithUserDef;
    private DataSetDef humanTaskWithAdminDef;
    private DataSetDef humanTasksWithUserDomainDef;

    @PostConstruct
    protected void registerDataSetDefinitions() {
        final String jbpmDataSource = "${"+ KieServerConstants.CFG_PERSISTANCE_DS + "}";

        humanTasksDef = DataSetDefFactory.newSQLDataSetDef()
                .uuid(HUMAN_TASKS_DATASET)
                .name("Human tasks")
                .dataSource(jbpmDataSource)
                .dbSQL("select " +
                            "t.activationTime, " +
                            "t.actualOwner, " +
                            "t.createdBy, " +
                            "t.createdOn, " +
                            "t.deploymentId, " +
                            "t.description, " +
                            "t.dueDate, " +
                            "t.name, " +
                            "t.parentId, " +
                            "t.priority, " +
                            "t.processId, " +
                            "t.processInstanceId, " +
                            "t.processSessionId, " +
                            "t.status, " +
                            "t.taskId, " +
                            "t.workItemId " +
                        "from " +
                            "AuditTaskImpl t", false)
                .date(COLUMN_ACTIVATION_TIME)
                .label(COLUMN_ACTUAL_OWNER)
                .label(COLUMN_CREATED_BY)
                .date(COLUMN_CREATED_ON)
                .label(COLUMN_DEPLOYMENT_ID)
                .text(COLUMN_DESCRIPTION)
                .date(COLUMN_DUE_DATE)
                .label(COLUMN_NAME)
                .number(COLUMN_PARENT_ID)
                .number(COLUMN_PRIORITY)
                .label(COLUMN_PROCESS_ID)
                .number(COLUMN_PROCESS_INSTANCE_ID)
                .number(COLUMN_PROCESS_SESSION_ID)
                .label(COLUMN_STATUS)
                .number(COLUMN_TASK_ID)
                .number(COLUMN_WORK_ITEM_ID)
                .buildDef();

        humanTasksWithUserDef = DataSetDefFactory.newSQLDataSetDef()
                .uuid(HUMAN_TASKS_WITH_USER_DATASET)
                .name("Human tasks and users")
                .dataSource(jbpmDataSource)
                .dbSQL( "select " +
                            "t.activationTime, " +
                            "t.actualOwner, " +
                            "t.createdBy, " +
                            "t.createdOn, " +
                            "t.deploymentId, " +
                            "t.description, " +
                            "t.dueDate, " +
                            "t.name, " +
                            "t.parentId, " +
                            "t.priority, " +
                            "t.processId, " +
                            "t.processInstanceId, " +
                            "t.processSessionId, " +
                            "t.status, " +
                            "t.taskId, " +
                            "t.workItemId, " +
                            "oe.id " +
                        "from " +
                            "AuditTaskImpl t, " +
                            "PeopleAssignments_PotOwners po, " +
                            "OrganizationalEntity oe " +
                        "where " +
                            "t.taskId = po.task_id and " +
                            "po.entity_id = oe.id", false )
                .date(COLUMN_ACTIVATION_TIME)
                .label(COLUMN_ACTUAL_OWNER)
                .label(COLUMN_CREATED_BY)
                .date(COLUMN_CREATED_ON)
                .label(COLUMN_DEPLOYMENT_ID)
                .text(COLUMN_DESCRIPTION)
                .date(COLUMN_DUE_DATE)
                .label(COLUMN_NAME)
                .number(COLUMN_PARENT_ID)
                .number(COLUMN_PRIORITY)
                .label(COLUMN_PROCESS_ID)
                .number(COLUMN_PROCESS_INSTANCE_ID)
                .number(COLUMN_PROCESS_SESSION_ID)
                .label(COLUMN_STATUS)
                .label(COLUMN_TASK_ID)   //declaring as label(even though it's numeric) because needs apply groupby and  Group by number not supported
                .number(COLUMN_WORK_ITEM_ID)
                .label(COLUMN_ORGANIZATIONAL_ENTITY)
                .buildDef();

        humanTaskWithAdminDef = DataSetDefFactory.newSQLDataSetDef()
                .uuid(HUMAN_TASKS_WITH_ADMIN_DATASET)
                .name("Human tasks and admins")
                .dataSource(jbpmDataSource)
                .dbSQL("select " +
                            "t.activationTime, " +
                            "t.actualOwner, " +
                            "t.createdBy, " +
                            "t.createdOn, " +
                            "t.deploymentId, " +
                            "t.description, " +
                            "t.dueDate, " +
                            "t.name, " +
                            "t.parentId, " +
                            "t.priority, " +
                            "t.processId, " +
                            "t.processInstanceId, " +
                            "t.processSessionId, " +
                            "t.status, " +
                            "t.taskId, " +
                            "t.workItemId, " +
                            "oe.id " +
                        "from " +
                            "AuditTaskImpl t, " +
                            "PeopleAssignments_BAs bas, " +
                            "OrganizationalEntity oe " +
                        "where " +
                            "t.taskId = bas.task_id and " +
                            "bas.entity_id = oe.id", false)
                .date(COLUMN_ACTIVATION_TIME)
                .label(COLUMN_ACTUAL_OWNER)
                .label(COLUMN_CREATED_BY)
                .date(COLUMN_CREATED_ON)
                .label(COLUMN_DEPLOYMENT_ID)
                .text(COLUMN_DESCRIPTION)
                .date(COLUMN_DUE_DATE)
                .label(COLUMN_NAME)
                .number(COLUMN_PARENT_ID)
                .number(COLUMN_PRIORITY)
                .label(COLUMN_PROCESS_ID)
                .number(COLUMN_PROCESS_INSTANCE_ID)
                .number(COLUMN_PROCESS_SESSION_ID)
                .label(COLUMN_STATUS)
                .label(COLUMN_TASK_ID)     //declaring as label(even though it's numeric) because needs apply groupby and  Group by number not supported
                .number(COLUMN_WORK_ITEM_ID)
                .label(COLUMN_ORGANIZATIONAL_ENTITY)
                .buildDef();

       humanTasksWithUserDomainDef = DataSetDefFactory.newSQLDataSetDef()       //Add to this dataset TaskName? to apply with the specified filter
                .uuid(HUMAN_TASKS_WITH_VARIABLES_DATASET)
                .name("Domain Specific Task")
                .dataSource(jbpmDataSource)
                .dbSQL("select " +
                            "tvi.taskId, " +
                            "(select ati.name from AuditTaskImpl ati where ati.taskId = tvi.taskId) as \"" + COLUMN_TASK_VARIABLE_TASK_NAME + "\", " +
                            "tvi.name, " +
                            "tvi.value " +
                        "from " +
                            "TaskVariableImpl tvi", false)
               .number(COLUMN_TASK_VARIABLE_TASK_ID)
               .label(COLUMN_TASK_VARIABLE_TASK_NAME)
               .label(COLUMN_TASK_VARIABLE_NAME)
               .label(COLUMN_TASK_VARIABLE_VALUE)
               .buildDef();


        // Hide all these internal data set from end user view
        humanTasksDef.setPublic(false);
        humanTasksDef.setProvider(KieServerDataSetProvider.TYPE);
        humanTasksWithUserDef.setPublic(false);
        humanTasksWithUserDef.setProvider(KieServerDataSetProvider.TYPE);
        humanTaskWithAdminDef.setPublic(false);
        humanTaskWithAdminDef.setProvider(KieServerDataSetProvider.TYPE);
        humanTasksWithUserDomainDef.setPublic(false);
        humanTasksWithUserDomainDef.setProvider(KieServerDataSetProvider.TYPE);

        // Register the data set definitions
        dataSetDefRegistry.registerDataSetDef(humanTasksDef);
        dataSetDefRegistry.registerDataSetDef(humanTasksWithUserDef);
        dataSetDefRegistry.registerDataSetDef(humanTaskWithAdminDef);
        dataSetDefRegistry.registerDataSetDef(humanTasksWithUserDomainDef);
        logger.info("Human task datasets registered");

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

                    logger.info("Registering human task data set definitions on connected server instance '{}'", serverInstanceId);
                    QueryServicesClient queryClient = kieServerIntegration.getAdminServerClient(serverTemplateId).getServicesClient(QueryServicesClient.class);
                    QueryDefinition humanTasksDefinition = QueryDefinition.builder()
                            .name(humanTasksDef.getUUID())
                            .expression(((SQLDataSetDef) humanTasksDef).getDbSQL())
                            .source(((SQLDataSetDef) humanTasksDef).getDataSource())
                            .target("CUSTOM")
                            .build();

                    QueryDefinition humanTasksWithUserDefinition = QueryDefinition.builder()
                            .name(humanTasksWithUserDef.getUUID())
                            .expression(((SQLDataSetDef) humanTasksWithUserDef).getDbSQL())
                            .source(((SQLDataSetDef) humanTasksWithUserDef).getDataSource())
                            .target("PO_TASK")
                            .build();

                    QueryDefinition humanTaskWithAdminDefinition = QueryDefinition.builder()
                            .name(humanTaskWithAdminDef.getUUID())
                            .expression(((SQLDataSetDef) humanTaskWithAdminDef).getDbSQL())
                            .source(((SQLDataSetDef) humanTaskWithAdminDef).getDataSource())
                            .target("BA_TASK")
                            .build();

                    QueryDefinition humanTasksWithUserDomainDefinition = QueryDefinition.builder()
                            .name(humanTasksWithUserDomainDef.getUUID())
                            .expression(((SQLDataSetDef) humanTasksWithUserDomainDef).getDbSQL())
                            .source(((SQLDataSetDef) humanTasksWithUserDomainDef).getDataSource())
                            .target("CUSTOM")
                            .build();
                    while (elapsed < waitLimit) {
                        try {

                            queryClient.replaceQuery(humanTasksDefinition);
                            logger.info("Query {} definition successfully registered on kie server '{}'", HUMAN_TASKS_DATASET, serverInstanceId);

                            queryClient.replaceQuery(humanTasksWithUserDefinition);
                            logger.info("Query {} definition successfully registered on kie server '{}'", HUMAN_TASKS_WITH_USER_DATASET, serverInstanceId);

                            queryClient.replaceQuery(humanTaskWithAdminDefinition);
                            logger.info("Query {} definition successfully registered on kie server '{}'", HUMAN_TASKS_WITH_ADMIN_DATASET, serverInstanceId);

                            queryClient.replaceQuery(humanTasksWithUserDomainDefinition);
                            logger.info("Query {} definition successfully registered on kie server '{}'", HUMAN_TASKS_WITH_VARIABLES_DATASET, serverInstanceId);
                            return;
                        } catch (KieServicesException e) {
                            // unable to register, might still be booting
                            Thread.sleep(500);
                            elapsed += 500;
                            logger.debug("Cannot reach KIE Server, elapsed time while waiting '{}', max time '{}'", elapsed, waitLimit);

                        }
                    }
                    logger.warn("Timeout while trying to register task query definition on '{}'", serverInstanceId);
                } catch (Exception e) {
                    logger.warn("Unable to register task queries on '{}' due to {}", serverInstanceId, e.getMessage(), e);

                }
            }
        });

    }

}