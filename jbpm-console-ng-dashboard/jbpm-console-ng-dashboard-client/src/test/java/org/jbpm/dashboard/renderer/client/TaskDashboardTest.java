/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.dashboard.renderer.client;

import javax.enterprise.event.Event;

import org.dashbuilder.common.client.error.ClientRuntimeError;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.displayer.client.Displayer;
import org.dashbuilder.renderer.client.metric.MetricDisplayer;
import org.dashbuilder.renderer.client.table.TableDisplayer;
import org.jboss.errai.common.client.api.Caller;
import org.jbpm.console.ng.bd.model.ProcessInstanceKey;
import org.jbpm.console.ng.bd.model.ProcessInstanceSummary;
import org.jbpm.console.ng.ht.model.events.TaskSelectionEvent;
import org.jbpm.console.ng.pr.service.ProcessRuntimeDataService;
import org.jbpm.dashboard.renderer.client.panel.AbstractDashboard;
import org.jbpm.dashboard.renderer.client.panel.TaskDashboard;
import org.jbpm.dashboard.renderer.client.panel.events.TaskDashboardFocusEvent;
import org.jbpm.dashboard.renderer.client.panel.widgets.ProcessBreadCrumb;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.client.mvp.PlaceStatus;
import org.uberfire.mocks.CallerMock;
import org.uberfire.workbench.events.NotificationEvent;

import static org.dashbuilder.dataset.Assertions.*;
import static org.jbpm.dashboard.renderer.model.DashboardData.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskDashboardTest extends AbstractDashboardTest {

    @Mock
    TaskDashboard.View view;

    @Mock
    ProcessBreadCrumb processBreadCrumb;

    @Mock
    Event<TaskSelectionEvent> taskSelectionEvent;

    @Mock
    Event<TaskDashboardFocusEvent> taskDashboardFocusEvent;

    @Mock
    ProcessRuntimeDataService processRuntimeDataService;

    Caller<ProcessRuntimeDataService> processRuntimeDataServiceCaller;

    @Mock
    Event<NotificationEvent> notificationEvent;

    TaskDashboard presenter;
    DataSet dataSet;

    @Override
    public void registerDataset() throws Exception {
        dataSet = TaskDashboardData.INSTANCE.toDataSet();
        dataSet.setUUID(DATASET_HUMAN_TASKS);
        clientDataSetManager.registerDataSet(dataSet);
    }

    @Override
    protected AbstractDashboard.View getView() {
        return view;
    }

    @Override
    protected AbstractDashboard getPresenter() {
        return presenter;
    }

    @Before
    public void init() throws Exception {
        super.init();

        processRuntimeDataServiceCaller = new CallerMock<ProcessRuntimeDataService>(processRuntimeDataService);

        presenter = new TaskDashboard(view,
                processBreadCrumb,
                dashboardFactory,
                clientServices,
                displayerLocator,
                displayerCoordinator,
                placeManager,
                taskSelectionEvent,
                taskDashboardFocusEvent,
                serverTemplateSelectorMenuBuilder,
                processRuntimeDataServiceCaller,
                notificationEvent);
    }

    @Test
    public void testDrawAll() {

        verify(view).init(presenter,
                presenter.getTotalMetric(),
                presenter.getCreatedMetric(),
                presenter.getReadyMetric(),
                presenter.getReservedMetric(),
                presenter.getInProgressMetric(),
                presenter.getSuspendedMetric(),
                presenter.getCompletedMetric(),
                presenter.getFailedMetric(),
                presenter.getErrorMetric(),
                presenter.getExitedMetric(),
                presenter.getObsoleteMetric(),
                presenter.getTasksByProcess(),
                presenter.getTasksByOwner(),
                presenter.getTasksByCreationDate(),
                presenter.getTasksByEndDate(),
                presenter.getTasksByRunningTime(),
                presenter.getTasksByStatus(),
                presenter.getTasksTable());

        verify(view).showLoading();

        verify(displayerListener).onDraw(presenter.getTotalMetric());
        verify(displayerListener).onDraw(presenter.getCreatedMetric());
        verify(displayerListener).onDraw(presenter.getReadyMetric());
        verify(displayerListener).onDraw(presenter.getReservedMetric());
        verify(displayerListener).onDraw(presenter.getInProgressMetric());
        verify(displayerListener).onDraw(presenter.getSuspendedMetric());
        verify(displayerListener).onDraw(presenter.getCompletedMetric());
        verify(displayerListener).onDraw(presenter.getFailedMetric());
        verify(displayerListener).onDraw(presenter.getErrorMetric());
        verify(displayerListener).onDraw(presenter.getExitedMetric());
        verify(displayerListener).onDraw(presenter.getObsoleteMetric());
        verify(displayerListener).onDraw(presenter.getTasksByProcess());
        verify(displayerListener).onDraw(presenter.getTasksByOwner());
        verify(displayerListener).onDraw(presenter.getTasksByCreationDate());
        verify(displayerListener).onDraw(presenter.getTasksByEndDate());
        verify(displayerListener).onDraw(presenter.getTasksByRunningTime());
        verify(displayerListener).onDraw(presenter.getTasksByStatus());
        verify(displayerListener).onDraw(presenter.getTasksTable());

        verify(view).hideLoading();
    }

    @Test
    public void test_JBPM_4851_Fix() {
        verify(presenter.getTotalMetric().getView()).setFilterActive(true);
        assertEquals(presenter.getTotalMetric().isFilterOn(), true);
    }

    @Test
    public void testShowInstances() {
        reset(displayerListener);
        presenter.showTasksTable();
        verify(view).showInstances();
        verify(taskDashboardFocusEvent).fire(any(TaskDashboardFocusEvent.class));
        verify(displayerListener).onRedraw(presenter.getTasksTable());
    }

    @Test
    public void testShowDashboard() {
        reset(displayerListener);
        presenter.showDashboard();
        verify(view).showDashboard();
        verify(taskDashboardFocusEvent).fire(any(TaskDashboardFocusEvent.class));
        verify(displayerListener, never()).onRedraw(presenter.getTasksTable());
    }

    @Test
    public void testTotalMetric() {
        Displayer displayer = presenter.getTotalMetric();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();
        assertEquals(dataSet.getValueAt(0, 0), 9d);
    }

    @Test
    public void testInProgressMetric() {
        Displayer displayer = presenter.getInProgressMetric();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();
        assertEquals(dataSet.getValueAt(0, 0), 3d);
    }

    @Test
    public void testCompletedMetric() {
        Displayer displayer = presenter.getCompletedMetric();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();
        assertEquals(dataSet.getValueAt(0, 0), 2d);
    }

    @Test
    public void testReservedMetric() {
        Displayer displayer = presenter.getReservedMetric();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();
        assertEquals(dataSet.getValueAt(0, 0), 1d);
    }

    @Test
    public void testSuspendedMetric() {
        Displayer displayer = presenter.getSuspendedMetric();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();
        assertEquals(dataSet.getValueAt(0, 0), 1d);
    }

    @Test
    public void testTasksByEndDate() {
        Displayer displayer = presenter.getTasksByEndDate();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();

        assertDataSetValues(dataSet, new String[][]{
                {"2019-01", "1.00"},
                {"2019-02", "0.00"},
                {"2019-03", "0.00"},
                {"2019-04", "0.00"},
                {"2019-05", "0.00"},
                {"2019-06", "0.00"},
                {"2019-07", "0.00"},
                {"2019-08", "0.00"},
                {"2019-09", "0.00"},
                {"2019-10", "0.00"},
                {"2019-11", "0.00"},
                {"2019-12", "1.00"}
        }, 0);
    }

    @Test
    public void testTasksByCreationDate() {
        Displayer displayer = presenter.getTasksByCreationDate();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();

        assertDataSetValues(dataSet, new String[][]{
                {"2019-01-01", "9.00"}
        }, 0);
    }

    @Test
    public void testTasksByRunningTime() {
        Displayer displayer = presenter.getTasksByRunningTime();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();

        assertDataSetValues(dataSet, new String[][]{
                {"Process A", "1.00", "9,000.00", "Process A", "1.00"},
                {"Process B", "1.00", "10,000.00", "Process B", "1.00"}
        }, 0);
    }

    @Test
    public void testTasksByStatus() {
        Displayer displayer = presenter.getTasksByStatus();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();

        assertDataSetValues(dataSet, new String[][]{
                {"InProgress", "3.00"},
                {"Completed", "2.00"},
                {"Suspended", "1.00"},
                {"Error", "1.00"},
                {"Reserved", "1.00"},
                {"Exited", "1.00"}
        }, 0);
    }

    @Test
    public void testTasksByOwner() {
        Displayer displayer = presenter.getTasksByOwner();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();

        assertDataSetValues(dataSet, new String[][]{
                {"user1", "3.00"},
                {"user2", "2.00"},
                {"user3", "1.00"},
                {"user4", "3.00"}
        }, 0);
    }

    @Test
    public void testTasksByProcess() {
        Displayer displayer = presenter.getTasksByProcess();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();

        assertDataSetValues(dataSet, new String[][]{
                {"Process A", "4.00"},
                {"Process B", "5.00"}
        }, 0);
    }

    @Test
    public void testTasksTable() {
        Displayer displayer = presenter.getTasksTable();
        DataSet dataSet = displayer.getDataSetHandler().getLastDataSet();

        assertDataSetValues(dataSet, new String[][]{
                {"1.00", "Process A", "1.00", "Task 1", "user1", "InProgress", "01/01/19 10:00", "", ""},
                {"4.00", "Process A", "1.00", "Task 4", "user2", "InProgress", "01/01/19 10:00", "", ""},
                {"8.00", "Process B", "2.00", "Task 4", "user4", "Completed", "01/01/19 10:00", "12/02/19 16:00", "10,000.00"},
                {"9.00", "Process B", "2.00", "Task 4", "user4", "Exited", "01/01/19 10:00", "", ""},
                {"2.00", "Process A", "1.00", "Task 2", "user1", "Completed", "01/01/19 09:00", "01/01/19 13:00", "9,000.00"},
                {"3.00", "Process A", "1.00", "Task 3", "user2", "Suspended", "01/01/19 08:00", "", ""},
                {"7.00", "Process B", "2.00", "Task 3", "user4", "Reserved", "01/01/19 08:00", "", ""},
                {"6.00", "Process B", "2.00", "Task 2", "user3", "Error", "01/01/19 07:00", "", ""},
                {"5.00", "Process B", "2.00", "Task 2", "user1", "InProgress", "01/01/19 06:00", "", ""}
        }, 0);
    }

    @Test
    public void testSelectProcess() {
        reset(view);
        reset(displayerListener);

        presenter.getTasksByProcess().filterUpdate(COLUMN_PROCESS_NAME, 1);
        final String process = "Process B";
        assertEquals(presenter.getSelectedProcess(), process);

        verify(view).showBreadCrumb(process);
        verify(view).setHeaderText(i18n.selectedTaskStatusHeader("", process));
        verify(displayerListener, times(17)).onRedraw(any(Displayer.class));
        verify(displayerListener, never()).onError(any(Displayer.class), any(ClientRuntimeError.class));
    }

    @Test
    public void testResetProcess() {
        reset(view);
        presenter.resetCurrentProcess();
        assertNull(presenter.getSelectedProcess());
        verify(view).hideBreadCrumb();
        verify(view).setHeaderText(i18n.allTasks());
    }

    @Test
    public void testSelectMetric() {
        presenter.resetCurrentMetric();
        reset(view);
        reset(displayerListener);

        MetricDisplayer inProgressMetric = presenter.getInProgressMetric();
        inProgressMetric.filterApply();

        assertEquals(presenter.getSelectedMetric(), inProgressMetric);
        verify(view).setHeaderText(i18n.tasksInProgress());
        verify(displayerListener).onFilterEnabled(eq(inProgressMetric), any(DataSetFilter.class));
        verify(displayerListener, times(1)).onFilterEnabled(any(Displayer.class), any(DataSetFilter.class));
        verify(displayerListener, never()).onFilterReset(any(Displayer.class), any(DataSetFilter.class));

        // Check that only processes with status=active are shown
        DataSet dataSet = presenter.getTasksTable().getDataSetHandler().getLastDataSet();
        assertDataSetValues(dataSet, new String[][]{
                {"1.00", "Process A", "1.00", "Task 1", "user1", "InProgress", "01/01/19 10:00", "", ""},
                {"4.00", "Process A", "1.00", "Task 4", "user2", "InProgress", "01/01/19 10:00", "", ""},
                {"5.00", "Process B", "2.00", "Task 2", "user1", "InProgress", "01/01/19 06:00", "", ""}
        }, 0);
    }

    @Test
    public void testResetMetric() {
        MetricDisplayer inProgressMetric = presenter.getInProgressMetric();
        inProgressMetric.filterApply();

        reset(displayerListener, view);
        inProgressMetric.filterReset();

        assertNull(presenter.getSelectedMetric());
        verify(view).setHeaderText(i18n.allTasks());
        verify(displayerListener).onFilterReset(eq(inProgressMetric), any(DataSetFilter.class));
        verify(displayerListener, times(1)).onFilterReset(any(Displayer.class), any(DataSetFilter.class));

        // Check that only tasks with status=InProgress are shown
        DataSet dataSet = presenter.getTasksTable().getDataSetHandler().getLastDataSet();
        assertDataSetValues(dataSet, new String[][]{
                {"1.00", "Process A", "1.00", "Task 1", "user1", "InProgress", "01/01/19 10:00", "", ""},
                {"4.00", "Process A", "1.00", "Task 4", "user2", "InProgress", "01/01/19 10:00", "", ""},
                {"8.00", "Process B", "2.00", "Task 4", "user4", "Completed", "01/01/19 10:00", "12/02/19 16:00", "10,000.00"},
                {"9.00", "Process B", "2.00", "Task 4", "user4", "Exited", "01/01/19 10:00", "", ""},
                {"2.00", "Process A", "1.00", "Task 2", "user1", "Completed", "01/01/19 09:00", "01/01/19 13:00", "9,000.00"},
                {"3.00", "Process A", "1.00", "Task 3", "user2", "Suspended", "01/01/19 08:00", "", ""},
                {"7.00", "Process B", "2.00", "Task 3", "user4", "Reserved", "01/01/19 08:00", "", ""},
                {"6.00", "Process B", "2.00", "Task 2", "user3", "Error", "01/01/19 07:00", "", ""},
                {"5.00", "Process B", "2.00", "Task 2", "user1", "InProgress", "01/01/19 06:00", "", ""}
        }, 0);
    }

    @Test
    public void testSwitchMetric() {
        MetricDisplayer inProgressMetric = presenter.getInProgressMetric();
        MetricDisplayer completedMetric = presenter.getCompletedMetric();
        inProgressMetric.filterApply();

        reset(displayerListener, view);
        completedMetric.filterApply();

        assertEquals(presenter.getSelectedMetric(), completedMetric);
        verify(displayerListener).onFilterReset(eq(inProgressMetric), any(DataSetFilter.class));
        verify(displayerListener).onFilterEnabled(eq(completedMetric), any(DataSetFilter.class));

        // Check that only tasks with status=Completed are shown
        DataSet dataSet = presenter.getTasksTable().getDataSetHandler().getLastDataSet();
        assertDataSetValues(dataSet, new String[][]{
                {"8.00", "Process B", "2.00", "Task 4", "user4", "Completed", "01/01/19 10:00", "12/02/19 16:00", "10,000.00"},
                {"2.00", "Process A", "1.00", "Task 2", "user1", "Completed", "01/01/19 09:00", "01/01/19 13:00", "9,000.00"}
        }, 0);
    }

    @Test
    public void testTaskInstanceNoDetails() {
        when(processRuntimeDataService.getProcessInstance(anyString(), any(ProcessInstanceKey.class))).thenReturn(mock(ProcessInstanceSummary.class));
        when(placeManager.getStatus(TaskDashboard.TASK_DETAILS_SCREEN_ID)).thenReturn(PlaceStatus.CLOSE);
        TableDisplayer tableDisplayer = presenter.getTasksTable();
        tableDisplayer.selectCell(COLUMN_TASK_ID, 3);

        verify(notificationEvent).fire(any(NotificationEvent.class));
        verify(taskSelectionEvent, never()).fire(any(TaskSelectionEvent.class));
        verify(taskDashboardFocusEvent, never()).fire(any(TaskDashboardFocusEvent.class));
        verify(placeManager, never()).goTo(TaskDashboard.TASK_DETAILS_SCREEN_ID);
    }

    @Test
    public void testOpenInstanceDetails() {
        when(processRuntimeDataService.getProcessInstance(anyString(), any(ProcessInstanceKey.class))).thenReturn(mock(ProcessInstanceSummary.class));
        when(placeManager.getStatus(TaskDashboard.TASK_DETAILS_SCREEN_ID)).thenReturn(PlaceStatus.CLOSE);
        TableDisplayer tableDisplayer = presenter.getTasksTable();
        tableDisplayer.selectCell(COLUMN_TASK_ID, 0);

        verify(taskSelectionEvent).fire(any(TaskSelectionEvent.class));
        verify(taskDashboardFocusEvent).fire(any(TaskDashboardFocusEvent.class));
        verify(placeManager).goTo(TaskDashboard.TASK_DETAILS_SCREEN_ID);
    }

    @Test
    public void testHeaderText(){
        verify(view).setHeaderText(i18n.allTasks());

        final String task = "Task Test";

        verifyMetricHeaderText(task, presenter.getTotalMetric(), i18n.selectedTaskStatusHeader("", task));
        verifyMetricHeaderText(task, presenter.getReadyMetric(), i18n.selectedTaskStatusHeader(i18n.taskStatusReady(), task));
        verifyMetricHeaderText(task, presenter.getReservedMetric(), i18n.selectedTaskStatusHeader(i18n.taskStatusReserved(), task));
        verifyMetricHeaderText(task, presenter.getInProgressMetric(), i18n.selectedTaskStatusHeader(i18n.taskStatusInProgress(), task));
        verifyMetricHeaderText(task, presenter.getSuspendedMetric(), i18n.selectedTaskStatusHeader(i18n.taskStatusSuspended(), task));
        verifyMetricHeaderText(task, presenter.getCompletedMetric(), i18n.selectedTaskStatusHeader(i18n.taskStatusCompleted(), task));

        reset(view);
        presenter.resetCurrentProcess();
        presenter.resetCurrentMetric();
        verify(view).setHeaderText(i18n.allTasks());
    }
}