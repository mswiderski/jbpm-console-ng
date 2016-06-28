/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.console.ng.ht.client.editors.taskprocesscontext;

import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jbpm.console.ng.bd.model.ProcessInstanceKey;
import org.jbpm.console.ng.bd.model.ProcessInstanceSummary;
import org.jbpm.console.ng.ht.model.TaskSummary;
import org.jbpm.console.ng.ht.model.events.TaskRefreshedEvent;
import org.jbpm.console.ng.ht.model.events.TaskSelectionEvent;
import org.jbpm.console.ng.ht.service.TaskService;
import org.jbpm.console.ng.pr.model.events.ProcessInstancesWithDetailsRequestEvent;
import org.jbpm.console.ng.pr.service.ProcessRuntimeDataService;
import org.uberfire.client.mvp.Activity;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

@Dependent
public class TaskProcessContextPresenter {

    public static final String PROCESS_INSTANCE_DETAILS = "DataSet Process Instances With Variables";

    public interface TaskProcessContextView extends UberView<TaskProcessContextPresenter> {

        void displayNotification(String text);

        void setProcessInstanceId(String none);

        void setProcessId(String none);

        void enablePIDetailsButton(boolean enable);
    }

    private PlaceManager placeManager;

    private ActivityManager activityManager;

    private TaskProcessContextView view;

    private Event<ProcessInstancesWithDetailsRequestEvent> processInstanceSelected;

    private Caller<TaskService> taskService;

    private Caller<ProcessRuntimeDataService> processRuntimeDataService;

    private long currentTaskId = 0;
    private long currentProcessInstanceId = -1L;
    private boolean enableProcessInstanceDetails = true;
    private String serverTemplateId;
    private String containerId;

    @Inject
    public TaskProcessContextPresenter(TaskProcessContextView view,
                                       PlaceManager placeManager,
                                       Caller<TaskService> taskService,
                                       Caller<ProcessRuntimeDataService> processRuntimeDataService,
                                       Event<ProcessInstancesWithDetailsRequestEvent> processInstanceSelected,
                                       ActivityManager activityManager) {
        this.view = view;
        this.taskService = taskService;
        this.processRuntimeDataService = processRuntimeDataService;
        this.placeManager = placeManager;
        this.processInstanceSelected = processInstanceSelected;
        this.activityManager = activityManager;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        final Set<Activity> activity = activityManager.getActivities(new DefaultPlaceRequest(PROCESS_INSTANCE_DETAILS));
        enableProcessInstanceDetails = activity.isEmpty() == false;
        view.enablePIDetailsButton(enableProcessInstanceDetails);
    }

    public IsWidget getView() {
        return view;
    }

    public void goToProcessInstanceDetails() {
        processRuntimeDataService.call(new RemoteCallback<ProcessInstanceSummary>() {
                              @Override
                              public void callback(ProcessInstanceSummary summary) {
                                  placeManager.goTo(PROCESS_INSTANCE_DETAILS);
                                  processInstanceSelected.fire(new ProcessInstancesWithDetailsRequestEvent(
                                          serverTemplateId,
                                          summary.getDeploymentId(),
                                          summary.getProcessInstanceId(),
                                          summary.getProcessId(),
                                          summary.getProcessName(),
                                          summary.getState())
                                  );
                              }
                          },
                new DefaultErrorCallback()
        ).getProcessInstance(serverTemplateId, new ProcessInstanceKey(serverTemplateId, currentProcessInstanceId));
    }

    public void refreshProcessContextOfTask() {
        taskService.call(new RemoteCallback<TaskSummary>() {
                                  @Override
                                  public void callback(TaskSummary details) {
                                      if (details == null || details.getProcessInstanceId() == -1) {
                                          view.setProcessInstanceId("None");
                                          view.setProcessId("None");
                                          view.enablePIDetailsButton(false);
                                          return;
                                      }

                                      currentProcessInstanceId = details.getProcessInstanceId();
                                      view.setProcessInstanceId(String.valueOf(currentProcessInstanceId));
                                      view.setProcessId(details.getProcessId());
                                      view.enablePIDetailsButton(true);
                                      view.enablePIDetailsButton(enableProcessInstanceDetails);
                                  }
                              },
                new DefaultErrorCallback()
        ).getTask(serverTemplateId, containerId, currentTaskId);
    }

    public void onTaskSelectionEvent(@Observes final TaskSelectionEvent event) {
        this.currentTaskId = event.getTaskId();
        this.serverTemplateId = event.getServerTemplateId();
        this.containerId = event.getContainerId();
        refreshProcessContextOfTask();
    }

    public void onTaskRefreshedEvent(@Observes final TaskRefreshedEvent event) {
        if (currentTaskId == event.getTaskId()) {
            refreshProcessContextOfTask();
        }
    }
}
