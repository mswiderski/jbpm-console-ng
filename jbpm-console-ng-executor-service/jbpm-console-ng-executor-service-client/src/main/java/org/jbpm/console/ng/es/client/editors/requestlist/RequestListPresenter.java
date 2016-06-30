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

package org.jbpm.console.ng.es.client.editors.requestlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.Range;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.sort.SortOrder;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jbpm.console.ng.df.client.filter.FilterSettings;
import org.jbpm.console.ng.df.client.list.base.DataSetQueryHelper;
import org.jbpm.console.ng.es.client.editors.jobdetails.JobDetailsPopup;
import org.jbpm.console.ng.es.client.editors.quicknewjob.QuickNewJobPopup;
import org.jbpm.console.ng.es.client.i18n.Constants;
import org.jbpm.console.ng.es.model.RequestSummary;
import org.jbpm.console.ng.es.model.events.RequestChangedEvent;
import org.jbpm.console.ng.es.service.ExecutorService;
import org.jbpm.console.ng.gc.client.dataset.AbstractDataSetReadyCallback;
import org.jbpm.console.ng.gc.client.list.base.AbstractListView.ListView;
import org.jbpm.console.ng.gc.client.list.base.AbstractScreenListPresenter;
import org.jbpm.console.ng.gc.client.list.base.events.SearchEvent;
import org.uberfire.ext.widgets.common.client.menu.RefreshMenuBuilder;
import org.uberfire.ext.widgets.common.client.menu.RefreshSelectorMenuBuilder;
import org.jbpm.console.ng.gc.client.menu.RestoreDefaultFiltersMenuBuilder;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import org.uberfire.client.workbench.widgets.common.ErrorPopupPresenter;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.jbpm.console.ng.es.model.RequestDataSetConstants.*;

@Dependent
@WorkbenchScreen(identifier = "Requests List")
public class RequestListPresenter extends AbstractScreenListPresenter<RequestSummary> implements RefreshSelectorMenuBuilder.SupportsRefreshInterval {

    public interface RequestListView extends ListView<RequestSummary, RequestListPresenter> {

        int getRefreshValue();

        void saveRefreshValue( int newValue );

        void applyFilterOnPresenter( String key );

    }

    private Constants constants = Constants.INSTANCE;

    @Inject
    private RequestListView view;

    @Inject
    private Caller<ExecutorService> executorServices;

    @Inject
    private Event<RequestChangedEvent> requestChangedEvent;

    @Inject
    DataSetQueryHelper dataSetQueryHelper;

    @Inject
    private QuickNewJobPopup quickNewJobPopup;

    private RefreshSelectorMenuBuilder refreshSelectorMenuBuilder = new RefreshSelectorMenuBuilder(this);

    @Inject
    private ErrorPopupPresenter errorPopup;

    @Inject
    private JobDetailsPopup jobDetailsPopup;

    public RequestListPresenter() {
        super();
    }

    public RequestListPresenter(RequestListViewImpl view,
            Caller<ExecutorService> executorServices,
            DataSetQueryHelper dataSetQueryHelper,Event<RequestChangedEvent> requestChangedEvent
    ) {
        this.view = view;
        this.executorServices = executorServices;
        this.dataSetQueryHelper = dataSetQueryHelper;
        this.requestChangedEvent = requestChangedEvent;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return constants.RequestsListTitle();
    }

    @WorkbenchPartView
    public UberView<RequestListPresenter> getView() {
        return view;
    }

    public void filterGrid( FilterSettings tableSettings ) {
        dataSetQueryHelper.setCurrentTableSettings( tableSettings );
        refreshGrid();
    }

    public void createRequest() {
        Map<String, String> ctx = new HashMap<String, String>();
        ctx.put( "businessKey", "1234" );
        executorServices.call( new RemoteCallback<Long>() {
            @Override
            public void callback( Long requestId ) {
                view.displayNotification( constants.RequestScheduled(requestId) );

            }
        } ).scheduleRequest( selectedServerTemplate, "PrintOutCmd", ctx );
    }

    @Override
    protected ListView getListView() {
        return view;
    }

    @Override
    public void getData( final Range visibleRange ) {
        try {
            if(!isAddingDefaultFilters()) {
                FilterSettings currentTableSettings = dataSetQueryHelper.getCurrentTableSettings();

                if ( currentTableSettings != null ) {
                    currentTableSettings.setServerTemplateId(selectedServerTemplate);
                    currentTableSettings.setTablePageSize( view.getListGrid().getPageSize() );
                    ColumnSortList columnSortList = view.getListGrid().getColumnSortList();
                    if ( columnSortList != null && columnSortList.size() > 0 ) {
                        dataSetQueryHelper.setLastOrderedColumn( ( columnSortList.size() > 0 ) ? columnSortList.get( 0 ).getColumn().getDataStoreName() : "" );
                        dataSetQueryHelper.setLastSortOrder( ( columnSortList.size() > 0 ) && columnSortList.get( 0 ).isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING );
                    } else {
                        dataSetQueryHelper.setLastOrderedColumn( COLUMN_TIMESTAMP );
                        dataSetQueryHelper.setLastSortOrder( SortOrder.ASCENDING );
                    }

                    if ( textSearchStr != null && textSearchStr.trim().length() > 0 ) {

                        DataSetFilter filter = new DataSetFilter();
                        List<ColumnFilter> filters = new ArrayList<ColumnFilter>();
                        filters.add( likeTo( COLUMN_COMMANDNAME, "%" + textSearchStr.toLowerCase() + "%", false ) );
                        filters.add( likeTo( COLUMN_MESSAGE, "%" + textSearchStr.toLowerCase() + "%", false ) );
                        filters.add( likeTo( COLUMN_BUSINESSKEY, "%" + textSearchStr.toLowerCase() + "%", false ) );
                        filter.addFilterColumn( OR( filters ) );

                        if ( currentTableSettings.getDataSetLookup().getFirstFilterOp() != null ) {
                            currentTableSettings.getDataSetLookup().getFirstFilterOp().addFilterColumn( OR( filters ) );
                        } else {
                            currentTableSettings.getDataSetLookup().addOperation( filter );
                        }
                        textSearchStr = "";
                    }
                    dataSetQueryHelper.setDataSetHandler(currentTableSettings);
                    dataSetQueryHelper.lookupDataSet(visibleRange.getStart(), new AbstractDataSetReadyCallback( errorPopup, view, currentTableSettings.getDataSet() ) {
                        @Override
                        public void callback(DataSet dataSet) {
                            if (dataSet != null) {
                                List<RequestSummary> myRequestSumaryFromDataSet = new ArrayList<RequestSummary>();

                                for (int i = 0; i < dataSet.getRowCount(); i++) {

                                    myRequestSumaryFromDataSet.add(new RequestSummary(
                                            dataSetQueryHelper.getColumnLongValue(dataSet, COLUMN_ID, i),
                                            dataSetQueryHelper.getColumnDateValue(dataSet, COLUMN_TIMESTAMP, i),
                                            dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_STATUS, i),
                                            dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_COMMANDNAME, i),
                                            dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_MESSAGE, i),
                                            dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_BUSINESSKEY, i)));

                                }
                                boolean lastPageExactCount=false;
                                if( dataSet.getRowCount() < view.getListGrid().getPageSize()) {
                                    lastPageExactCount=true;
                                }
                                updateDataOnCallback(myRequestSumaryFromDataSet,visibleRange.getStart(),visibleRange.getStart()+ myRequestSumaryFromDataSet.size(), lastPageExactCount);

                            }
                        }
                    });
                    view.hideBusyIndicator();
                }
            }
        } catch ( Exception e ) {
            view.displayNotification(constants.ErrorRetrievingJobs(e.getMessage()));
            GWT.log( "Error looking up dataset with UUID [ " + REQUEST_LIST_DATASET + " ]" );
        }

    }

    public AsyncDataProvider<RequestSummary> getDataProvider() {
        return dataProvider;
    }

    public void cancelRequest( final Long requestId ) {
        executorServices.call( new RemoteCallback<Void>() {
            @Override
            public void callback( Void nothing ) {
                view.displayNotification( constants.RequestCancelled(requestId) );
                requestChangedEvent.fire( new RequestChangedEvent( requestId ) );
            }
        } ).cancelRequest( selectedServerTemplate, requestId );
    }

    public void requeueRequest( final Long requestId ) {
        executorServices.call( new RemoteCallback<Void>() {
            @Override
            public void callback( Void nothing ) {
                view.displayNotification( constants.RequestCancelled(requestId) );
                requestChangedEvent.fire( new RequestChangedEvent( requestId ) );
            }
        } ).requeueRequest( selectedServerTemplate, requestId );
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return MenuFactory
                .newTopLevelMenu(constants.New_Job())
                .respondsWith(new Command() {
                    @Override
                    public void execute() {
                        if (selectedServerTemplate == null || selectedServerTemplate.trim().isEmpty()) {
                            view.displayNotification(constants.SelectServerTemplate());
                        } else {
                            quickNewJobPopup.show(selectedServerTemplate);
                        }

                    }
                })
                .endMenu()
                .newTopLevelCustomMenu(serverTemplateSelectorMenuBuilder)
                .endMenu()
                .newTopLevelCustomMenu(new RefreshMenuBuilder(this)).endMenu()
                .newTopLevelCustomMenu(refreshSelectorMenuBuilder).endMenu()
                .newTopLevelCustomMenu(new RestoreDefaultFiltersMenuBuilder(this)).endMenu()
                .build();
    }

    @Override
    public void onGridPreferencesStoreLoaded() {
        refreshSelectorMenuBuilder.loadOptions( view.getRefreshValue() );
    }


    @Override
    protected void onSearchEvent( @Observes SearchEvent searchEvent ) {
        textSearchStr = searchEvent.getFilter();
        view.applyFilterOnPresenter( dataSetQueryHelper.getCurrentTableSettings().getKey() );
    }

    @Override
    public void onUpdateRefreshInterval(boolean enableAutoRefresh, int newInterval) {
        super.onUpdateRefreshInterval(enableAutoRefresh, newInterval);
        view.saveRefreshValue(newInterval);
    }

    public void showJobDetails(final RequestSummary job){
        jobDetailsPopup.show( selectedServerTemplate, String.valueOf( job.getJobId() ) );
    }

}