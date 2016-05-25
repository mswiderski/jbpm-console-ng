/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.console.ng.ht.client.editors.taskassignments;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.common.collect.FluentIterable;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.shared.api.identity.User;
import org.jbpm.console.ng.ht.client.i18n.Constants;
import org.jbpm.console.ng.ht.model.TaskAssignmentSummary;
import org.jbpm.console.ng.ht.model.events.TaskRefreshedEvent;
import org.jbpm.console.ng.ht.model.events.TaskSelectionEvent;
import org.jbpm.console.ng.ht.service.integration.RemoteTaskService;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;

@Dependent
public class TaskAssignmentsPresenter {

    public interface TaskAssignmentsView extends IsWidget {

        void init(final TaskAssignmentsPresenter presenter);

        void displayNotification(String text);

        void setPotentialOwnersInfo(String text);

        void enableDelegateButton(boolean enable);

        void setDelegateButtonActive(boolean enable);

        void clearUserOrGroupInput();

        void enableUserOrGroupInput(boolean enable);

        void setHelpText(String text);
    }

    private Constants constants = Constants.INSTANCE;
    private TaskAssignmentsView view;
    private User identity;
    private Caller<RemoteTaskService> remoteTaskService;
    private Event<TaskRefreshedEvent> taskRefreshed;
    private long currentTaskId = 0;
    private String serverTemplateId;
    private String containerId;

    @Inject
    public TaskAssignmentsPresenter(
            TaskAssignmentsView view,
            User identity,
            Caller<RemoteTaskService> remoteTaskService,
            Event<TaskRefreshedEvent> taskRefreshed
    ) {
        this.view = view;
        this.identity = identity;
        this.remoteTaskService = remoteTaskService;
        this.taskRefreshed = taskRefreshed;
    }

    @PostConstruct
    public void init() {
        view.init(this);
    }

    public IsWidget getView() {
        return view;
    }

    public void delegateTask(String entity) {
        if (entity == null || "".equals(entity.trim())) {
            view.setHelpText(constants.DelegationUserInputRequired());
            return;
        }
        remoteTaskService.call(
                new RemoteCallback<Void>() {
                    @Override
                    public void callback(Void nothing) {
                        view.displayNotification(constants.TaskSuccessfullyDelegated());
                        view.setDelegateButtonActive(false);
                        view.setHelpText(constants.DelegationSuccessfully());
                        taskRefreshed.fire(new TaskRefreshedEvent(currentTaskId));
                        refreshTaskPotentialOwners();
                    }
                },
                new DefaultErrorCallback() {

                    @Override
                    public boolean error(Message message, Throwable throwable) {
                        view.setDelegateButtonActive(true);
                        view.setHelpText(constants.DelegationUnable());
                        return super.error(message, throwable);
                    }
                }
        ).delegate(serverTemplateId, containerId, currentTaskId, entity);
    }

    public void refreshTaskPotentialOwners() {
        if (currentTaskId != 0) {
            view.enableDelegateButton(false);
            view.enableUserOrGroupInput(false);
            view.setPotentialOwnersInfo("");

            remoteTaskService.call(new RemoteCallback<TaskAssignmentSummary>() {
                @Override
                public void callback(final TaskAssignmentSummary response) {
                    if (response.getPotOwnersString() == null || response.getPotOwnersString().isEmpty()) {
                        view.setPotentialOwnersInfo(constants.No_Potential_Owners());
                    } else {
                        view.setPotentialOwnersInfo(response.getPotOwnersString().toString());

                        boolean allowDelegate = isDelegateAllowed(response);
                        view.enableDelegateButton(allowDelegate);
                        view.enableUserOrGroupInput(allowDelegate);
                    }
                }
            }, new DefaultErrorCallback()).getTaskAssignmentDetails(serverTemplateId, containerId, currentTaskId);

        }
    }

    protected Boolean isDelegateAllowed(final TaskAssignmentSummary task) {
        if (task == null) {
            return false;
        }

        if ("Completed".equals(task.getStatus())) {
            return false;
        }

        final String actualOwner = task.getActualOwner();
        if (actualOwner != null && actualOwner.equals(identity.getIdentifier())) {
            return true;
        }

        final String initiator = task.getCreatedBy();
        if (initiator != null && initiator.equals(identity.getIdentifier())) {
            return true;
        }

        final Set<String> roles = FluentIterable.from(identity.getGroups()).transform(g -> g.getName()).append(identity.getIdentifier()).toSet();

        //TODO Needs to check if po or ba string is a group or a user
        final List<String> potentialOwners = task.getPotOwnersString();
        if (potentialOwners != null && Collections.disjoint(potentialOwners, roles) == false) {
            return true;
        }

        final List<String> businessAdministrators = task.getBusinessAdmins();
        if (businessAdministrators != null && Collections.disjoint(businessAdministrators, roles) == false) {
            return true;
        }

        return false;
    }

    public void onTaskSelectionEvent(@Observes final TaskSelectionEvent event) {
        this.currentTaskId = event.getTaskId();
        serverTemplateId = event.getServerTemplateId();
        containerId = event.getContainerId();
        view.setHelpText("");
        view.clearUserOrGroupInput();
        refreshTaskPotentialOwners();
    }

    public void onTaskRefreshedEvent(@Observes TaskRefreshedEvent event) {
        if (currentTaskId == event.getTaskId()) {
            refreshTaskPotentialOwners();
        }
    }

}