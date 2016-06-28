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

package org.jbpm.console.ng.ht.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jbpm.console.ng.ht.model.CommentSummary;
import org.jbpm.console.ng.ht.model.TaskAssignmentSummary;
import org.jbpm.console.ng.ht.model.TaskEventSummary;
import org.jbpm.console.ng.ht.model.TaskSummary;

@Remote
public interface TaskService {

    List<TaskSummary> getActiveTasks(String serverTemplateId, Integer page, Integer pageSize);

    TaskSummary getTask(String serverTemplateId, String containerId, Long taskId);

    void updateTask(String serverTemplateId, String containerId, Long taskId, Integer priority, String description, Date dueDate);

    void claimTask(String serverTemplateId, String containerId, Long taskId);

    void releaseTask(String serverTemplateId, String containerId, Long taskId);

    void startTask(String serverTemplateId, String containerId, Long taskId);

    void completeTask(String serverTemplateId, String containerId, Long taskId, Map<String, Object> output);

    void saveTaskContent(String serverTemplateId, String containerId, Long taskId, Map<String, Object> output);

    void addTaskComment(String serverTemplateId, String containerId, Long taskId, String text, Date addedOn);

    void deleteTaskComment(String serverTemplateId, String containerId, Long taskId, Long commentId);

    List<CommentSummary> getTaskComments(String serverTemplateId, String containerId, Long taskId);

    List<TaskEventSummary> getTaskEvents(String serverTemplateId, String containerId, Long taskId);

    void delegate(String serverTemplateId, String containerId, long taskId, String entity);

    TaskAssignmentSummary getTaskAssignmentDetails(String serverTemplateId, String containerId, long taskId);

    void executeReminderForTask(long taskId,String fromUser);

}