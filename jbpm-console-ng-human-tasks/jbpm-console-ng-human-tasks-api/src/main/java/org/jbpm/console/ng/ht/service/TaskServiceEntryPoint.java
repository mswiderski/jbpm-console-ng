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

package org.jbpm.console.ng.ht.service;

import java.util.Date;

import java.util.List;
import java.util.Map;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jbpm.console.ng.ht.model.AuditTaskSummary;
import org.jbpm.console.ng.ht.model.CommentSummary;
import org.jbpm.console.ng.ht.model.Day;
import org.jbpm.console.ng.ht.model.TaskEventSummary;
import org.jbpm.console.ng.ht.model.TaskSummary;


@Remote
public interface TaskServiceEntryPoint {

    List<TaskSummary> getTasksAssignedAsPotentialOwnerByExpirationDateOptional(String userId, List<String> status, Date from, int offset, int count);
    
    Map<Day, List<TaskSummary>> getTasksAssignedAsPotentialOwnerFromDateToDateByDays(String userId, List<String> strStatuses,
                                                        Date from, int nrOfDaysTotal);


    /**
     * Gets the mapping '{@link Day} -> list of owned tasks' starting from specified dayand for specified number of days. Only
     * tasks with specified statuses are considered.
     * 
     * @param userId id of the task owner
     * @param from start day
     * @param nrOfDaysTotal how many days to return including start date
     * 
     * @return list of tasks per day for specified days (dates)
     */
    Map<Day, List<TaskSummary>> getTasksOwnedFromDateToDateByDays(String userId, List<String> strStatuses, Date from,
            int nrOfDaysTotal);

    List<TaskSummary> getTasksOwnedByExpirationDateOptional(String userId, List<String> strStatuses, Date from, int offset, int count);

    long addTask(String taskString, Map<String, Object> inputs, Map<String, Object> templateInputs);
    
    long addQuickTask(String taskName, int priority,
                      Date dueDate, 
                      List<String> users, List<String> groups, String identity, 
                      boolean start, boolean claim);

    long addTaskAndStart(String taskString, Map<String, Object> inputs, String userId, Map<String, Object> templateInputs);
    
    long addTaskAndClaimAndStart(String taskString, Map<String, Object> inputs, String userId,
            Map<String, Object> templateVars);

    void start(long taskId, String user);

    void startBatch(List<Long> taskIds, String user);

    void claim(long taskId, String user);

    void claimBatch(List<Long> taskIds, String user);

    void complete(long taskId, String user, Map<String, Object> params);

    void completeBatch(List<Long> taskIds, String user, Map<String, Object> params);

    void release(long taskId, String user);

    void releaseBatch(List<Long> taskIds, String user);

    void forward(long taskId, String userId, String targetEntityId);
    
    void delegate(long taskId, String userId, String targetEntityId);

    void setPriority(long taskId, int priority);

    void setExpirationDate(long taskId, Date date);

    void setDescriptions(long taskId, String description);

    TaskSummary getTaskDetails(long taskId);

    long saveContent(long taskId, Map<String, Object> values);

    Map<String, Object> getTaskOutputContentByTaskId(long taskId);

    Map<String, Object> getContentListByTaskId(long taskId);

    long addComment(long taskId, String text, String addedBy, Date addedOn);

    void deleteComment(long taskId, long commentId);

    List<CommentSummary> getAllCommentsByTaskId(long taskId);

    CommentSummary getCommentById(long taskId, long commentId);

    void updateSimpleTaskDetails(long taskId, String taskName, int priority, String taskDescription,
    // String subTaskStrategy,
            Date dueDate);
    
    
    Map<Long, List<String>> getPotentialOwnersForTaskIds(List<Long> taskIds);
    
    
    //Audit Methods
    /**
     * false: when the task was deleted in data base (it happens when the process is finished)
     * 
     * @param taskId
     * @return
     */
    Boolean existInDatabase(long taskId);
    
    List<TaskEventSummary> getAllTaskEvents(long taskId, String filter);

    List<TaskEventSummary> getAllTaskEventsByProcessInstanceId(long processInstanceId, String filter);

    List<AuditTaskSummary> getAllAuditTasks(String filter) ;

    List<AuditTaskSummary> getAllAuditTasksByUser(String userId, String filter);

}
