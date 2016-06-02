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

import java.util.Iterator;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.jbpm.console.ng.gc.client.i18n.Constants;
import org.uberfire.mvp.ParameterizedCommand;

@Dependent
public class ServerTemplateSelectorViewImpl extends Composite implements ServerTemplateSelectorMenuBuilder.ServerTemplateSelectorView {

    private Constants constants = Constants.INSTANCE;

    private DropDownMenu dropDownServerTemplates;

    private Button serverTemplateButton;

    private ButtonGroup serverTemplates;

    private ParameterizedCommand<String> changeCommand;

    @PostConstruct
    public void init() {
        serverTemplateButton = GWT.create(Button.class);
        serverTemplateButton.setText(constants.ServerTemplates());
        serverTemplateButton.setDataToggle(Toggle.DROPDOWN);
        serverTemplateButton.setSize(ButtonSize.SMALL);

        dropDownServerTemplates = GWT.create(DropDownMenu.class);
        dropDownServerTemplates.addStyleName(Styles.DROPDOWN_MENU + "-right");

        serverTemplates = GWT.create(ButtonGroup.class);
        serverTemplates.add(serverTemplateButton);
        serverTemplates.add(dropDownServerTemplates);

        initWidget(serverTemplates);
    }

    @Override
    public void addServerTemplate(final String serverTemplateId) {
        final AnchorListItem serverTemplateNavLink = GWT.create(AnchorListItem.class);
        serverTemplateNavLink.setText(serverTemplateId);
        serverTemplateNavLink.setIcon(IconType.SERVER);
        serverTemplateNavLink.setIconFixedWidth(true);
        serverTemplateNavLink.addClickHandler(e -> {
            if (changeCommand != null) {
                unselectAllServerTeplateNavLinks();
                serverTemplateButton.setText(serverTemplateId);
                serverTemplateNavLink.setIcon(IconType.CHECK);
                changeCommand.execute(serverTemplateId);
            }
        });
        dropDownServerTemplates.add(serverTemplateNavLink);
    }

    @Override
    public void removeServerTemplate(final String serverTemplateId) {
        Iterator<Widget> it = dropDownServerTemplates.iterator();

        while (it.hasNext()) {
            AnchorListItem item = (AnchorListItem) it.next();
            if (item.getText().equals(serverTemplateId)) {
                it.remove();
            }
        }
    }

    @Override
    public void setServerTemplateChangeHandler(final ParameterizedCommand<String> command) {
        changeCommand = command;
    }

    private void unselectAllServerTeplateNavLinks(){
        for(int i =0; i < dropDownServerTemplates.getWidgetCount(); i++){
            ((AnchorListItem)dropDownServerTemplates.getWidget(i)).setIcon(IconType.SERVER);
        }
    }
}