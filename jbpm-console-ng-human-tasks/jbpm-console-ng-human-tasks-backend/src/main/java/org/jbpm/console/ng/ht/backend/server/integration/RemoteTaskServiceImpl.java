package org.jbpm.console.ng.ht.backend.server.integration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.console.ng.ht.model.CommentSummary;
import org.jbpm.console.ng.ht.model.TaskAssignmentSummary;
import org.jbpm.console.ng.ht.model.TaskEventSummary;
import org.jbpm.console.ng.ht.model.TaskSummary;
import org.jbpm.console.ng.ht.service.integration.RemoteTaskService;
import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.model.instance.TaskComment;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ApplicationScoped
public class RemoteTaskServiceImpl implements RemoteTaskService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteTaskServiceImpl.class);

    @Inject
    private KieServerIntegration kieServerIntegration;

    @Inject
    private IdentityProvider identityProvider;


    @Override
    public List<TaskSummary> getActiveTasks(String serverTemplateId, Integer page, Integer pageSize) {
        List<TaskSummary> taskSummaries = new ArrayList<TaskSummary>();

        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return taskSummaries;
        }

        UserTaskServicesClient client = getClient(serverTemplateId);

        List<org.kie.server.api.model.instance.TaskSummary> tasks = client.findTasksAssignedAsPotentialOwner(identityProvider.getName(), page, pageSize);

        for (org.kie.server.api.model.instance.TaskSummary task : tasks) {
            TaskSummary taskSummary = build(task);

            taskSummaries.add(taskSummary);
        }

        return taskSummaries;
    }

    @Override
    public TaskSummary getTask(String serverTemplateId, String containerId, Long taskId) {
        UserTaskServicesClient client = getClient(serverTemplateId);

        TaskInstance task = client.getTaskInstance(containerId, taskId);

        return build(task);
    }

    @Override
    public void updateTask(String serverTemplateId, String containerId, Long taskId, Integer priority, String description, Date dueDate) {

        UserTaskServicesClient client = getClient(serverTemplateId);
        // TODO update only when it actually changed
        client.setTaskDescription(containerId, taskId, description);
        client.setTaskPriority(containerId, taskId, priority);
        client.setTaskExpirationDate(containerId, taskId, dueDate);
    }

    @Override
    public void claimTask(String serverTemplateId, String containerId, Long taskId) {
        UserTaskServicesClient client = getClient(serverTemplateId);

        client.claimTask(containerId, taskId, identityProvider.getName());
    }

    @Override
    public void releaseTask(String serverTemplateId, String containerId, Long taskId) {
        UserTaskServicesClient client = getClient(serverTemplateId);

        client.releaseTask(containerId, taskId, identityProvider.getName());
    }

    @Override
    public void startTask(String serverTemplateId, String containerId, Long taskId) {
        UserTaskServicesClient client = getClient(serverTemplateId);

        client.startTask(containerId, taskId, identityProvider.getName());
    }

    @Override
    public void completeTask(String serverTemplateId, String containerId, Long taskId, Map<String, Object> output) {
        UserTaskServicesClient client = getClient(serverTemplateId);

        client.completeTask(containerId, taskId, identityProvider.getName(), output);
    }

    @Override
    public void saveTaskContent(String serverTemplateId, String containerId, Long taskId, Map<String, Object> output) {
        UserTaskServicesClient client = getClient(serverTemplateId);

        client.saveTaskContent(containerId, taskId, output);
    }

    @Override
    public void addTaskComment(String serverTemplateId, String containerId, Long taskId, String text, Date addedOn) {
        UserTaskServicesClient client = getClient(serverTemplateId);

        client.addTaskComment(containerId, taskId, text, identityProvider.getName(), addedOn);
    }

    @Override
    public void deleteTaskComment(String serverTemplateId, String containerId, Long taskId, Long commentId) {
        UserTaskServicesClient client = getClient(serverTemplateId);

        client.deleteTaskComment(containerId, taskId, commentId);
    }

    @Override
    public List<CommentSummary> getTaskComments(String serverTemplateId, String containerId, Long taskId) {
        List<CommentSummary> commentSummaries = new ArrayList<CommentSummary>();
        UserTaskServicesClient client = getClient(serverTemplateId);

        List<TaskComment> comments = client.getTaskCommentsByTaskId(containerId, taskId);

        for (TaskComment comment : comments) {
            CommentSummary summary = build(comment);

            commentSummaries.add(summary);
        }

        return commentSummaries;
    }

    @Override
    public List<TaskEventSummary> getTaskEvents(String serverTemplateId, String containerId, Long taskId) {
        List<TaskEventSummary> eventSummaries = new ArrayList<TaskEventSummary>();
        UserTaskServicesClient client = getClient(serverTemplateId);

        List<TaskEventInstance> events = client.findTaskEvents(taskId, 0, 1000);

        for (TaskEventInstance event : events) {
            TaskEventSummary summary = build(event);

            eventSummaries.add(summary);
        }

        return eventSummaries;
    }

    @Override
    public void delegate(String serverTemplateId, String containerId, long taskId, String entity) {
        UserTaskServicesClient client = getClient(serverTemplateId);

        client.delegateTask(containerId, taskId, identityProvider.getName(), entity);
    }

    @Override
    public TaskAssignmentSummary getTaskAssignmentDetails(String serverTemplateId, String containerId, long taskId) {
        UserTaskServicesClient client = getClient(serverTemplateId);

        TaskInstance task = client.getTaskInstance(containerId, taskId, false, false, true);
        TaskAssignmentSummary summary = new TaskAssignmentSummary();
        summary.setTaskId(task.getId());
        summary.setActualOwner(task.getActualOwner());
        summary.setTaskName(task.getName());
        summary.setPotOwnersString(task.getPotentialOwners());
        summary.setCreatedBy(task.getCreatedBy());
        summary.setBusinessAdmins(task.getBusinessAdmins());
        summary.setStatus(task.getStatus());
        return summary;
    }

    protected UserTaskServicesClient getClient(String serverTemplateId) {
        KieServicesClient client = kieServerIntegration.getServerClient(serverTemplateId);
        if (client == null) {
            throw new RuntimeException("No client to interact with server " + serverTemplateId);
        }

        return client.getServicesClient(UserTaskServicesClient.class);
    }

    protected TaskSummary build(org.kie.server.api.model.instance.TaskSummary task) {
        TaskSummary taskSummary = new TaskSummary(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getActualOwner(),
                task.getCreatedBy(),
                task.getCreatedOn(),
                task.getActivationTime(),
                task.getExpirationTime(),
                task.getProcessId(),
                -1,
                task.getProcessInstanceId(),
                task.getContainerId(),
                task.getParentId(),
                false
        );

        return taskSummary;
    }

    protected TaskSummary build(TaskInstance task) {
        if (task == null) {
            return null;
        }

        TaskSummary taskSummary = new TaskSummary(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getActualOwner(),
                task.getCreatedBy(),
                task.getCreatedOn(),
                task.getActivationTime(),
                task.getExpirationDate(),
                task.getProcessId(),
                -1,
                task.getProcessInstanceId(),
                task.getContainerId(),
                task.getParentId(),
                false
        );

        return taskSummary;
    }

    protected CommentSummary build(TaskComment comment) {
        CommentSummary summary = new CommentSummary(
                comment.getId(),
                comment.getText(),
                comment.getAddedBy(),
                comment.getAddedAt()
        );

        return summary;
    }

    protected TaskEventSummary build(TaskEventInstance event) {

        TaskEventSummary summary = new TaskEventSummary(
                event.getId(),
                event.getTaskId(),
                event.getType(),
                event.getUserId(),
                event.getWorkItemId(),
                event.getLogTime(),
                ""
        );

        return summary;
    }
}
