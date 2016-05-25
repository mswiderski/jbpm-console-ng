/*
 * Copyright 2015 JBoss by Red Hat.
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
package org.jbpm.console.ng.ht.client.editors.taskprocesscontext;

import java.util.Collections;
import javax.enterprise.event.Event;

import com.google.common.collect.Sets;
import org.jbpm.console.ng.bd.model.ProcessInstanceKey;
import org.jbpm.console.ng.bd.model.ProcessInstanceSummary;
import org.jbpm.console.ng.ht.client.editors.taskprocesscontext.TaskProcessContextPresenter.TaskProcessContextView;
import org.jbpm.console.ng.ht.model.TaskSummary;
import org.jbpm.console.ng.ht.model.events.TaskSelectionEvent;
import org.jbpm.console.ng.ht.service.integration.RemoteTaskService;
import org.jbpm.console.ng.pr.model.events.ProcessInstancesWithDetailsRequestEvent;
import org.jbpm.console.ng.pr.service.integration.RemoteRuntimeDataService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.client.mvp.Activity;
import org.uberfire.client.mvp.ActivityManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mvp.PlaceRequest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskProcessContextPresenterTest {

    private static final Long TASK_ID_NO_PROCESS = 1L;
    private static final Long TASK_ID_WITH_PROC = 2L;
    private static final Long TASK_ID_NULL_DETAILS = 3L;

    TaskSummary taskNoProcess = new TaskSummary(TASK_ID_NO_PROCESS, "task without process", null, null, 0, null, null, null, null, null, null/*ProcessID*/, -1, -1 /*Proc instId*/, null, -1);
    TaskSummary taskWithProcess = new TaskSummary(TASK_ID_WITH_PROC, "task with process", null, null, 0, null, null, null, null, null, "TEST_PROCESS_ID"/*ProcessID*/, -1, 123L /*Proc inst Id*/, null, -1);

    @Mock
    RemoteRuntimeDataService dataServiceEntryPoint;

    @Mock
    Event<ProcessInstancesWithDetailsRequestEvent> procNavigationMock;

    @Mock
    private TaskProcessContextView viewMock;
    @Mock
    private PlaceManager placeManager;
    @Mock
    private ActivityManager activityManager;

    private TaskProcessContextPresenter presenter;

    @Before
    public void before() {
        //Task query service mock
        RemoteTaskService tqs = mock(RemoteTaskService.class);
        when(tqs.getTask(null, null, TASK_ID_NO_PROCESS)).thenReturn(taskNoProcess);
        when(tqs.getTask(null, null, TASK_ID_WITH_PROC)).thenReturn(taskWithProcess);
        when(tqs.getTask(null, null, TASK_ID_NULL_DETAILS)).thenReturn(null);
        CallerMock<RemoteTaskService> taskQueryServiceMock
                = new CallerMock<RemoteTaskService>(tqs);

        // DataService caller mock
        CallerMock<RemoteRuntimeDataService> dataServiceCallerMock = new CallerMock<RemoteRuntimeDataService>(dataServiceEntryPoint);

        presenter = new TaskProcessContextPresenter(
                viewMock,
                placeManager,
                taskQueryServiceMock,
                dataServiceCallerMock,
                procNavigationMock,
                activityManager);
    }

    @Test
    public void processContextEmpty_whenTaskDetailsNull() {
        presenter.onTaskSelectionEvent(new TaskSelectionEvent(TASK_ID_NULL_DETAILS));

        verify(viewMock).setProcessId("None");
        verify(viewMock).setProcessInstanceId("None");
        verify(viewMock).enablePIDetailsButton(false);
    }

    @Test
    public void processContextEmtpy_whenTaskNotAssociatedWithProcess() {
        presenter.onTaskSelectionEvent(new TaskSelectionEvent(TASK_ID_NO_PROCESS));

        verify(viewMock).setProcessId("None");
        verify(viewMock).setProcessInstanceId("None");
        verify(viewMock).enablePIDetailsButton(false);
    }

    @Test
    public void processContextShowsProcessInfo_whenTaskDetailsHasProcess() {
        presenter.onTaskSelectionEvent(new TaskSelectionEvent(TASK_ID_WITH_PROC));

        verify(viewMock).setProcessId("TEST_PROCESS_ID");
        verify(viewMock).setProcessInstanceId("123");
        verify(viewMock, times(2)).enablePIDetailsButton(true);
    }

    @Test
    public void testGoToProcessInstanceDetails() {
        final ProcessInstanceSummary summary = new ProcessInstanceSummary();
        summary.setDeploymentId("deploymentId");
        summary.setProcessInstanceId(-1l);
        summary.setProcessId("processId");
        summary.setProcessName("processName");
        summary.setState(1);
        when(dataServiceEntryPoint.getProcessInstance(anyString(), any(ProcessInstanceKey.class))).thenReturn(summary);

        presenter.goToProcessInstanceDetails();

        verify(placeManager).goTo("DataSet Process Instances With Variables");
        final ArgumentCaptor<ProcessInstancesWithDetailsRequestEvent> eventCaptor = ArgumentCaptor.forClass(ProcessInstancesWithDetailsRequestEvent.class);
        verify(procNavigationMock).fire(eventCaptor.capture());
        final ProcessInstancesWithDetailsRequestEvent event = eventCaptor.getValue();
        assertEquals(summary.getDeploymentId(), event.getDeploymentId());
        assertEquals(summary.getProcessInstanceId(), event.getProcessInstanceId());
        assertEquals(summary.getProcessId(), event.getProcessDefId());
        assertEquals(summary.getProcessName(), event.getProcessDefName());
        assertEquals(Integer.valueOf(summary.getState()), event.getProcessInstanceStatus());
    }

    @Test
    public void testProcessContextEnabled() {
        when(activityManager.getActivities(any(PlaceRequest.class))).thenReturn(Sets.newHashSet(mock(Activity.class)));

        presenter.init();

        verify(viewMock).enablePIDetailsButton(true);
    }

    @Test
    public void testProcessContextDisabled() {
        when(activityManager.getActivities(any(PlaceRequest.class))).thenReturn(Collections.<Activity>emptySet());

        presenter.init();

        verify(viewMock).enablePIDetailsButton(false);
    }

}