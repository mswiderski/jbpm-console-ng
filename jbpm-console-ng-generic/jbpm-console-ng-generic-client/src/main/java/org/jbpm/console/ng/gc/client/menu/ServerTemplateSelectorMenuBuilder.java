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

package org.jbpm.console.ng.gc.client.menu;

import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.server.controller.api.model.events.ServerTemplateDeleted;
import org.kie.server.controller.api.model.events.ServerTemplateUpdated;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.workbench.common.screens.server.management.service.SpecManagementService;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.impl.BaseMenuCustom;

@Dependent
public class ServerTemplateSelectorMenuBuilder implements MenuFactory.CustomMenuBuilder {

    @Inject
    private ServerTemplateSelectorView view;

    @Inject
    private Caller<SpecManagementService> specManagementService;

    @Inject
    private Event<ServerTemplateSelected> serverTemplateSelectedEvent;

    @PostConstruct
    public void init() {
        specManagementService.call(new RemoteCallback<Collection<ServerTemplate>>() {
            @Override
            public void callback(final Collection<ServerTemplate> serverTemplates) {
                String serverTemplateId = null;
                for (ServerTemplate serverTemplate : serverTemplates) {
                    if (serverTemplate.getServerInstanceKeys() != null && !serverTemplate.getServerInstanceKeys().isEmpty()) {
                        view.addServerTemplate(serverTemplate.getId());
                        serverTemplateId=serverTemplate.getId();
                    }
                }

                if( serverTemplates!=null && serverTemplates.size()==1 && serverTemplateId!=null ){
                    view.selectServerTemplate( serverTemplateId );
                }

            }
        }).listServerTemplates();
        view.setServerTemplateChangeHandler(e -> serverTemplateSelectedEvent.fire(new ServerTemplateSelected(e)));
    }

    @Override
    public void push(MenuFactory.CustomMenuBuilder element) {
    }

    @Override
    public MenuItem build() {
        return new BaseMenuCustom<IsWidget>() {
            @Override
            public IsWidget build() {
                return view;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public void setEnabled(boolean enabled) {

            }

            @Override
            public String getSignatureId() {
                return "org.jbpm.console.ng.gc.client.list.base.ServerTemplateSelectorMenuBuilder#menuServerTemplate";
            }

        };
    }

    public void onServerTemplateDeleted(@Observes ServerTemplateDeleted serverTemplateDeleted) {
        view.removeServerTemplate(serverTemplateDeleted.getServerTemplateId());
    }

    public void onServerTemplateUpdated(@Observes ServerTemplateUpdated serverTemplateUpdated) {
        final ServerTemplate serverTemplate = serverTemplateUpdated.getServerTemplate();
        if (serverTemplate.getServerInstanceKeys() == null || serverTemplate.getServerInstanceKeys().isEmpty()) {
            view.removeServerTemplate(serverTemplate.getId());
        }
    }

    public interface ServerTemplateSelectorView extends IsWidget {

        void selectServerTemplate(String serverTemplateId);

        void addServerTemplate(String serverTemplateId);

        void removeServerTemplate(String serverTemplateId);

        void setServerTemplateChangeHandler(ParameterizedCommand<String> command);

    }

    @Inject
    public void setSpectManagementService(final Caller<SpecManagementService> specManagementService) {
        this.specManagementService = specManagementService;
    }

}