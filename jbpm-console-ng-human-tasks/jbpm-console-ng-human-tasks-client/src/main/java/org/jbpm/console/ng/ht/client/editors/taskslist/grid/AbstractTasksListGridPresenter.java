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

package org.jbpm.console.ng.ht.client.editors.taskslist.grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetOp;
import org.dashbuilder.dataset.DataSetOpType;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.filter.FilterFactory;
import org.dashbuilder.dataset.sort.SortOrder;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.shared.api.Group;
import org.jbpm.console.ng.df.client.filter.FilterSettings;
import org.jbpm.console.ng.df.client.list.base.DataSetQueryHelper;
import org.jbpm.console.ng.gc.client.dataset.AbstractDataSetReadyCallback;
import org.jbpm.console.ng.gc.client.experimental.grid.base.ExtendedPagedTable;
import org.jbpm.console.ng.gc.client.list.base.AbstractListView;
import org.jbpm.console.ng.gc.client.list.base.AbstractScreenListPresenter;
import org.jbpm.console.ng.gc.client.list.base.events.SearchEvent;
import org.jbpm.console.ng.gc.client.menu.ServerTemplateSelected;
import org.jbpm.console.ng.gc.client.menu.ServerTemplateSelectorMenuBuilder;
import org.jbpm.console.ng.ht.client.editors.taskslist.grid.dash.DataSetTasksListGridPresenter;
import org.jbpm.console.ng.ht.client.i18n.Constants;
import org.jbpm.console.ng.ht.model.TaskSummary;
import org.jbpm.console.ng.ht.model.events.NewTaskEvent;
import org.jbpm.console.ng.ht.model.events.TaskRefreshedEvent;
import org.jbpm.console.ng.ht.model.events.TaskSelectionEvent;
import org.jbpm.console.ng.ht.service.integration.RemoteTaskService;
import org.kie.workbench.common.screens.server.management.service.SpecManagementService;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.mvp.PlaceStatus;
import org.uberfire.client.mvp.UberView;
import org.uberfire.client.workbench.widgets.common.ErrorPopupPresenter;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;
import org.uberfire.ext.widgets.common.client.menu.RefreshSelectorMenuBuilder;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.paging.PageResponse;
import org.uberfire.workbench.model.menu.Menus;

import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.jbpm.console.ng.ht.model.TaskDataSetConstants.*;

public abstract class AbstractTasksListGridPresenter extends AbstractScreenListPresenter<TaskSummary> {

    public interface DataSetTaskListView extends AbstractListView.ListView<TaskSummary, AbstractTasksListGridPresenter> {

        int getRefreshValue();

        void saveRefreshValue(int newValue);

        void applyFilterOnPresenter(String key);

        void addDomainSpecifColumns(ExtendedPagedTable<TaskSummary> extendedPagedTable, Set<String> columns);

        FilterSettings getVariablesTableSettings(String processName);

        void setSelectedTask(TaskSummary selectedTask);

    }

    private Constants constants = Constants.INSTANCE;

    private DataSetTasksListGridPresenter.DataSetTaskListView view;

    private Caller<RemoteTaskService> remoteTaskService;

    protected DataSetQueryHelper dataSetQueryHelper;

    private DataSetQueryHelper dataSetQueryHelperDomainSpecific;

    @Inject
    private ErrorPopupPresenter errorPopup;

    @Inject
    private Caller<SpecManagementService> specManagementService;

    protected ServerTemplateSelectorMenuBuilder serverTemplateSelectorMenuBuilder;

    private String selectedServerTemplate = "";

    @Inject
    private Event<TaskSelectionEvent> taskSelected;

    protected RefreshSelectorMenuBuilder refreshSelectorMenuBuilder = new RefreshSelectorMenuBuilder(this);

    public AbstractTasksListGridPresenter() {
        dataProvider = new AsyncDataProvider<TaskSummary>() {

            @Override
            protected void onRangeChanged(HasData<TaskSummary> display) {
                view.showBusyIndicator(constants.Loading());
                final Range visibleRange = view.getListGrid().getVisibleRange();
                getData(visibleRange);

            }
        };
    }

    @Override
    protected AbstractListView.ListView getListView() {
        return view;
    }

    @Override
    public void getData(final Range visibleRange) {
        try {
            if (!isAddingDefaultFilters()) {
                FilterSettings currentTableSettings = dataSetQueryHelper.getCurrentTableSettings();

                if (currentTableSettings != null ) {
                    currentTableSettings.setServerTemplateId(selectedServerTemplate);
                    currentTableSettings.setTablePageSize(view.getListGrid().getPageSize());
                    ColumnSortList columnSortList = view.getListGrid().getColumnSortList();
                    if (columnSortList != null && columnSortList.size() > 0) {
                        dataSetQueryHelper.setLastOrderedColumn((columnSortList.size() > 0) ? columnSortList.get(0).getColumn().getDataStoreName() : "");
                        dataSetQueryHelper.setLastSortOrder((columnSortList.size() > 0) && columnSortList.get(0).isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING);
                    } else {
                        dataSetQueryHelper.setLastOrderedColumn(COLUMN_CREATED_ON);
                        dataSetQueryHelper.setLastSortOrder(SortOrder.ASCENDING);
                    }

                    final List<ColumnFilter> filters = getColumnFilters(textSearchStr);
                    if (filters.isEmpty() == false) {
                        if (currentTableSettings.getDataSetLookup().getFirstFilterOp() != null) {
                            currentTableSettings.getDataSetLookup().getFirstFilterOp().addFilterColumn(OR(filters));
                        } else {
                            final DataSetFilter filter = new DataSetFilter();
                            filter.addFilterColumn(OR(filters));
                            currentTableSettings.getDataSetLookup().addOperation(filter);
                        }
                    }
                    boolean isAdminDataset = currentTableSettings.getDataSetLookup().getDataSetUUID().equals(HUMAN_TASKS_WITH_ADMIN_DATASET);

                    if (isAdminDataset ||
                            currentTableSettings.getDataSetLookup().getDataSetUUID().equals(HUMAN_TASKS_WITH_USER_DATASET)) {
                        if (currentTableSettings.getDataSetLookup().getFirstFilterOp() != null) {
                            currentTableSettings.getDataSetLookup().getFirstFilterOp().addFilterColumn(getUserGroupFilters(isAdminDataset));
                        } else {
                            final DataSetFilter filter = new DataSetFilter();
                            filter.addFilterColumn(getUserGroupFilters(isAdminDataset));
                            currentTableSettings.getDataSetLookup().addOperation(filter);
                        }

                    }
                    dataSetQueryHelper.setDataSetHandler(currentTableSettings);
                    dataSetQueryHelper.lookupDataSet(visibleRange.getStart(), createDataSetTaskCallback(visibleRange.getStart(), currentTableSettings));
                } else {
                    remoteTaskService.call(new RemoteCallback<List<TaskSummary>>() {
                        @Override
                        public void callback(List<TaskSummary> taskSummaries) {
                            boolean lastPage=false;
                            if ( taskSummaries.size() < visibleRange.getLength() ) {
                                lastPage = true;
                            }
                            updateDataOnCallback(taskSummaries,visibleRange.getStart(),lastPage);

                        }
                    }).getActiveTasks(selectedServerTemplate, visibleRange.getStart()/visibleRange.getLength(), visibleRange.getLength());
                }
            }
        } catch (Exception e) {
            errorPopup.showMessage(constants.UnexpectedError(e.getMessage()));
        }

    }

    protected List<ColumnFilter> getColumnFilters(final String searchString) {
        final List<ColumnFilter> filters = new ArrayList<ColumnFilter>();
        if (searchString != null && searchString.trim().length() > 0) {
            try {
                final Long taskId = Long.valueOf(searchString.trim());
                filters.add(equalsTo(COLUMN_TASK_ID, taskId));
            } catch (NumberFormatException ex) {
                filters.add(likeTo(COLUMN_NAME, "%" + searchString.toLowerCase() + "%", false));
                filters.add(likeTo(COLUMN_DESCRIPTION, "%" + searchString.toLowerCase() + "%", false));
                filters.add(likeTo(COLUMN_PROCESS_ID, "%" + searchString.toLowerCase() + "%", false));
            }
        }
        return filters;
    }

    /**
     * Generates a dataset filter depending of the user roles and the kind of dataset.
     * <br>In case of the adminDataset (isAdminDateset=true), retrieve the tasks that are accessible for the user logged
     * roles, without restriction over the task owner.
     * <br>In other cases, retrieve the tasks available for the user logged roles AND without owner(claimed by the groups
     * members) OR the user logged owned tasks
     * @param isAdminDataset true if the filter to create is an adminDataSet
     * @return the dynamic filter to add, depeding on the user logged roles and the kind of dataset
     */
    protected ColumnFilter getUserGroupFilters(boolean isAdminDataset) {
        Set<Group> groups = identity.getGroups();
        List<ColumnFilter> condList = new ArrayList<ColumnFilter>();
        for (Group g : groups) {
            condList.add(FilterFactory.equalsTo(COLUMN_ORGANIZATIONAL_ENTITY, g.getName()));

        }
        condList.add(FilterFactory.equalsTo(COLUMN_ORGANIZATIONAL_ENTITY, identity.getIdentifier()));
        ColumnFilter myGroupFilter;
        if (isAdminDataset) {
            return FilterFactory.OR(COLUMN_ORGANIZATIONAL_ENTITY, condList);
        } else {
            myGroupFilter = FilterFactory.AND(FilterFactory.OR(condList),
                    FilterFactory.OR(FilterFactory.equalsTo(COLUMN_ACTUAL_OWNER, ""), FilterFactory.isNull(COLUMN_ACTUAL_OWNER)));
            return FilterFactory.OR(myGroupFilter, FilterFactory.equalsTo(COLUMN_ACTUAL_OWNER, identity.getIdentifier()));
        }
    }

    protected DataSetReadyCallback createDataSetTaskCallback(final int startRange, final FilterSettings tableSettings) {
        return new AbstractDataSetReadyCallback(errorPopup, view, tableSettings.getDataSet()) {

            @Override
            public void callback(DataSet dataSet) {
                if (dataSet != null) {
                    final List<TaskSummary> myTasksFromDataSet = new ArrayList<TaskSummary>();

                    for (int i = 0; i < dataSet.getRowCount(); i++) {
                        myTasksFromDataSet.add(createTaskSummaryFromDataSet(dataSet, i));

                    }
                    List<DataSetOp> ops = tableSettings.getDataSetLookup().getOperationList();
                    String filterValue = isFilteredByTaskName(ops); //Add here the check to add the domain data columns taskName?

                    boolean lastPage=false;
                    if( dataSet.getRowCount() < view.getListGrid().getPageSize()) {
                        lastPage=true;
                    }

                    if (filterValue != null) {
                        getDomainSpecifDataForTasks(startRange, filterValue, myTasksFromDataSet, lastPage);
                    } else {
                        updateDataOnCallback(myTasksFromDataSet,startRange,lastPage);
                    }

                }
                view.hideBusyIndicator();
            }

        };
    }

    protected String isFilteredByTaskName(List<DataSetOp> ops) {
        for (DataSetOp dataSetOp : ops) {
            if (dataSetOp.getType().equals(DataSetOpType.FILTER)) {
                List<ColumnFilter> filters = ((DataSetFilter) dataSetOp).getColumnFilterList();

                for (ColumnFilter filter : filters) {

                    if (filter instanceof CoreFunctionFilter) {
                        CoreFunctionFilter coreFilter = ((CoreFunctionFilter) filter);
                        if (filter.getColumnId().toUpperCase().equals(COLUMN_NAME.toUpperCase()) &&
                                ((CoreFunctionFilter) filter).getType() == CoreFunctionType.EQUALS_TO) {

                            List parameters = coreFilter.getParameters();
                            if (parameters.size() > 0) {
                                return parameters.get(0).toString();
                            }
                        }
                    }
                }
            }
        }

        return null;

    }

    public void getDomainSpecifDataForTasks(final int startRange, String filterValue, final List<TaskSummary> myTasksFromDataSet, boolean lastPage) {

        FilterSettings variablesTableSettings = view.getVariablesTableSettings(filterValue);
        variablesTableSettings.setTablePageSize(-1);
        variablesTableSettings.setServerTemplateId(selectedServerTemplate);

        dataSetQueryHelperDomainSpecific.setDataSetHandler(variablesTableSettings);
        dataSetQueryHelperDomainSpecific.setCurrentTableSettings(variablesTableSettings);
        dataSetQueryHelperDomainSpecific.setLastOrderedColumn(COLUMN_TASK_ID);
        dataSetQueryHelperDomainSpecific.setLastSortOrder(SortOrder.ASCENDING);

        List<Comparable> tasksIds = new ArrayList<Comparable>();
        for (TaskSummary task : myTasksFromDataSet) {
            tasksIds.add(task.getTaskId());
        }
        DataSetFilter filter = new DataSetFilter();
        ColumnFilter filter1 = FilterFactory.equalsTo(COLUMN_TASK_VARIABLE_TASK_ID, tasksIds);
        filter.addFilterColumn(filter1);
        variablesTableSettings.getDataSetLookup().addOperation(filter);

        dataSetQueryHelperDomainSpecific.lookupDataSet(0, createDataSetDomainSpecificCallback(startRange, myTasksFromDataSet, variablesTableSettings.getDataSet(),lastPage));

    }

    protected DataSetReadyCallback createDataSetDomainSpecificCallback(final int startRange, final List<TaskSummary> instances, final DataSet dataset, boolean lastPage) {
        return new AbstractDataSetReadyCallback(errorPopup, view, dataset) {
            @Override
            public void callback(DataSet dataSet) {
                if (dataSet.getRowCount() > 0) {
                    Set<String> columns = new HashSet<String>();
                    for (int i = 0; i < dataSet.getRowCount(); i++) {
                        Long taskId = dataSetQueryHelperDomainSpecific.getColumnLongValue(dataSet, COLUMN_TASK_ID, i);
                        String variableName = dataSetQueryHelperDomainSpecific.getColumnStringValue(dataSet, COLUMN_TASK_VARIABLE_NAME, i);
                        String variableValue = dataSetQueryHelperDomainSpecific.getColumnStringValue(dataSet, COLUMN_TASK_VARIABLE_VALUE, i);

                        for (TaskSummary task : instances) {
                            if (task.getTaskId().equals(taskId)) {
                                task.addDomainData(variableName, variableValue);
                                columns.add(variableName);
                            }
                        }
                    }
                    view.addDomainSpecifColumns(view.getListGrid(), columns);
                }
                updateDataOnCallback(instances,startRange,lastPage);
            }

        };
    }

    protected TaskSummary createTaskSummaryFromDataSet(final DataSet dataSet, int i) {
        return new TaskSummary(
                dataSetQueryHelper.getColumnLongValue(dataSet, COLUMN_TASK_ID, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_NAME, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_DESCRIPTION, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_STATUS, i),
                dataSetQueryHelper.getColumnIntValue(dataSet, COLUMN_PRIORITY, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_ACTUAL_OWNER, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_CREATED_BY, i),
                dataSetQueryHelper.getColumnDateValue(dataSet, COLUMN_CREATED_ON, i),
                dataSetQueryHelper.getColumnDateValue(dataSet, COLUMN_ACTIVATION_TIME, i),
                dataSetQueryHelper.getColumnDateValue(dataSet, COLUMN_DUE_DATE, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_PROCESS_ID, i),
                dataSetQueryHelper.getColumnLongValue(dataSet, COLUMN_PROCESS_SESSION_ID, i),
                dataSetQueryHelper.getColumnLongValue(dataSet, COLUMN_PROCESS_INSTANCE_ID, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_DEPLOYMENT_ID, i),
                dataSetQueryHelper.getColumnLongValue(dataSet, COLUMN_PARENT_ID, i),
                HUMAN_TASKS_WITH_ADMIN_DATASET.equals(dataSet.getUUID()));
    }

    public void filterGrid(FilterSettings tableSettings) {
        dataSetQueryHelper.setCurrentTableSettings(tableSettings);
        refreshGrid();
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return constants.Tasks_List();
    }

    @WorkbenchPartView
    public UberView<AbstractTasksListGridPresenter> getView() {
        return view;
    }

    public void releaseTask(final TaskSummary task) {
        remoteTaskService.call(
                new RemoteCallback<Void>() {
                    @Override
                    public void callback(Void nothing) {
                        view.displayNotification(constants.TaskReleased(String.valueOf(task.getTaskId())));
                        refreshGrid();
                    }
                },
                new DefaultErrorCallback()
        ).releaseTask(selectedServerTemplate, task.getDeploymentId(), task.getTaskId());
        taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, task.getDeploymentId(),task.getTaskId(), task.getTaskName() ) );
    }

    public void claimTask(final TaskSummary task) {
        remoteTaskService.call(
                new RemoteCallback<Void>() {
                    @Override
                    public void callback(Void nothing) {
                        view.displayNotification(constants.TaskClaimed(String.valueOf(task.getTaskId())));
                        refreshGrid();
                    }
                },
                new DefaultErrorCallback()
        ).claimTask(selectedServerTemplate, task.getDeploymentId(), task.getTaskId());
        taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, task.getDeploymentId(),task.getTaskId(), task.getTaskName() ) );
    }

    @Override
    public void onGridPreferencesStoreLoaded() {
        refreshSelectorMenuBuilder.loadOptions(view.getRefreshValue());
    }

    @Override
    public void onUpdateRefreshInterval(boolean enableAutoRefresh, int newInterval) {
        super.onUpdateRefreshInterval(enableAutoRefresh, newInterval);
        view.saveRefreshValue(newInterval);
    }

    @Override
    protected void onSearchEvent(@Observes SearchEvent searchEvent) {
        textSearchStr = searchEvent.getFilter();
        view.applyFilterOnPresenter(dataSetQueryHelper.getCurrentTableSettings().getKey());
    }

    public abstract Menus getMenus();

    public void onServerTemplateSelected(@Observes final ServerTemplateSelected serverTemplateSelected ) {
        selectedServerTemplate = serverTemplateSelected.getServerTemplateId();
        refreshGrid();
    }

    public void selectTask(final TaskSummary summary, final Boolean close) {
        final DefaultPlaceRequest defaultPlaceRequest = new DefaultPlaceRequest( "Task Details Multi" );
        final PlaceStatus status = placeManager.getStatus( defaultPlaceRequest );
        boolean logOnly = false;
        if ( summary.getStatus().equals( "Completed" ) && summary.isLogOnly() ) {
            logOnly = true;
        }
        if ( status == PlaceStatus.CLOSE ) {
            placeManager.goTo( defaultPlaceRequest );
            taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, summary.getDeploymentId(), summary.getTaskId(), summary.getTaskName(), summary.isForAdmin(), logOnly ) );
        } else if ( status == PlaceStatus.OPEN && !close ) {
            taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, summary.getDeploymentId(),summary.getTaskId(), summary.getTaskName(), summary.isForAdmin(), logOnly ) );
        } else if ( status == PlaceStatus.OPEN && close ) {
            placeManager.closePlace( "Task Details Multi" );
        }
    }

    public void refreshNewTask( @Observes NewTaskEvent newTask ) {
        refreshGrid();
        PlaceStatus status = placeManager.getStatus( new DefaultPlaceRequest( "Task Details Multi" ) );
        if ( status == PlaceStatus.OPEN ) {
            taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, null, newTask.getNewTaskId(), newTask.getNewTaskName() ) );
        } else {
            placeManager.goTo( "Task Details Multi" );
            taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, null, newTask.getNewTaskId(), newTask.getNewTaskName() ) );
        }

        view.setSelectedTask(new TaskSummary( newTask.getNewTaskId(), newTask.getNewTaskName() ));
    }

    public void onTaskRefreshedEvent( @Observes TaskRefreshedEvent event ) {
        refreshGrid();
    }

    @Inject
    public void setView(final DataSetTaskListView view) {
        this.view = view;
    }

    @Inject
    public void setDataSetQueryHelper(final DataSetQueryHelper dataSetQueryHelper) {
        this.dataSetQueryHelper = dataSetQueryHelper;
    }

    @Inject
    public void setDataSetQueryHelperDomainSpecific(final DataSetQueryHelper dataSetQueryHelperDomainSpecific) {
        this.dataSetQueryHelperDomainSpecific = dataSetQueryHelperDomainSpecific;
    }

    @Inject
    public void setRemoteTaskService(final Caller<RemoteTaskService> remoteTaskService) {
        this.remoteTaskService = remoteTaskService;
    }

    @Inject
    public void setServerTemplateSelectorMenuBuilder(final ServerTemplateSelectorMenuBuilder serverTemplateSelectorMenuBuilder) {
        this.serverTemplateSelectorMenuBuilder = serverTemplateSelectorMenuBuilder;
    }
}