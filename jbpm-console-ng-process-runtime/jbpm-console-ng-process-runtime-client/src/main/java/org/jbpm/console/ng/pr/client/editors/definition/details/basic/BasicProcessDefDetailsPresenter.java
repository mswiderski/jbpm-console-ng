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

package org.jbpm.console.ng.pr.client.editors.definition.details.basic;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jbpm.console.ng.bd.model.ProcessDefinitionKey;
import org.jbpm.console.ng.bd.model.ProcessSummary;
import org.jbpm.console.ng.pr.client.editors.definition.details.BaseProcessDefDetailsPresenter;
import org.jbpm.console.ng.pr.client.i18n.Constants;
import org.jbpm.console.ng.pr.service.ProcessRuntimeDataService;
import org.uberfire.backend.vfs.VFSService;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;

@Dependent
public class BasicProcessDefDetailsPresenter extends BaseProcessDefDetailsPresenter {

    public interface BasicProcessDefDetailsView extends
            BaseProcessDefDetailsPresenter.BaseProcessDefDetailsView {

    }

    private Constants constants = Constants.INSTANCE;

    @Inject
    private BasicProcessDefDetailsView view;

    @Inject
    private Caller<ProcessRuntimeDataService> processRuntimeDataService;

    @Inject
    private Caller<VFSService> fileServices;

    @Override
    public IsWidget getWidget() {
        return view;
    }

    @Override
    protected void refreshView(  final String serverTemplateId, String currentProcessDefId, String currentDeploymentId ) {
        view.getProcessIdText().setText( currentProcessDefId );
        view.getDeploymentIdText().setText( currentDeploymentId );
    }

    private void refreshProcessItems( final String serverTemplateId, final String deploymentId, final String processId ) {

        processRuntimeDataService.call(new RemoteCallback<ProcessSummary>() {

            @Override
            public void callback( ProcessSummary process ) {
                if (process != null) {
                    view.setEncodedProcessSource( process.getEncodedProcessSource() );
                    view.getProcessNameText().setText( process.getName() );

                    changeStyleRow( process.getName(), process.getVersion() );
                } else {
                    // set to null to ensure it's clear state
                    view.setEncodedProcessSource( null );
                    view.setProcessAssetPath( null );
                }
            }
        }, new DefaultErrorCallback() ).getProcess(serverTemplateId, new ProcessDefinitionKey(serverTemplateId, deploymentId, processId));

    }

    @Override
    protected void refreshProcessDef(  final String serverTemplateId, String deploymentId, String processId ) {
        refreshProcessItems( serverTemplateId, deploymentId, processId );
    }
}
