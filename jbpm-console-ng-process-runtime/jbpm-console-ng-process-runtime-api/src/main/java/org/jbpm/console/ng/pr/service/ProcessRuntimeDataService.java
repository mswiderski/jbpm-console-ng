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

package org.jbpm.console.ng.pr.service;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jbpm.console.ng.bd.model.NodeInstanceSummary;
import org.jbpm.console.ng.bd.model.ProcessDefinitionKey;
import org.jbpm.console.ng.bd.model.ProcessInstanceKey;
import org.jbpm.console.ng.bd.model.ProcessInstanceSummary;
import org.jbpm.console.ng.bd.model.ProcessSummary;
import org.jbpm.console.ng.bd.model.RuntimeLogSummary;
import org.jbpm.console.ng.bd.model.TaskDefSummary;

@Remote
public interface ProcessRuntimeDataService {

    List<ProcessInstanceSummary> getProcessInstances(String serverTemplateId, List<Integer> statuses, Integer page, Integer pageSize);

    ProcessInstanceSummary getProcessInstance(String serverTemplateId, ProcessInstanceKey processInstanceKey);

    List<NodeInstanceSummary> getProcessInstanceActiveNodes(String serverTemplateId, Long processInstanceId);

    List<RuntimeLogSummary> getRuntimeLogs(String serverTemplateId, Long processInstanceId);

    List<RuntimeLogSummary> getBusinessLogs(String serverTemplateId, String processName, Long processInstanceId);

    List<ProcessSummary> getProcesses(String serverTemplateId, Integer page, Integer pageSize, String sort, boolean sortOrder);

    ProcessSummary getProcess(String serverTemplateId, ProcessDefinitionKey processDefinitionKey);

    List<ProcessSummary> getProcessesByFilter(String serverTemplateId, String textSearch, Integer page, Integer pageSize, String sort, boolean sortOrder);

    ProcessSummary getProcessesByContainerIdProcessId(String serverTemplateId, String containerId, String processId);

    List<TaskDefSummary> getProcessUserTasks(String serverTemplateId, String containerId, String processId);

}