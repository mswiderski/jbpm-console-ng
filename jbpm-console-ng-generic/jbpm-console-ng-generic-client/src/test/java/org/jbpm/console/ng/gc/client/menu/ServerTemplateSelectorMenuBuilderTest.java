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

import java.util.ArrayList;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ServerTemplate;

import org.kie.workbench.common.screens.server.management.service.SpecManagementService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mvp.ParameterizedCommand;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class ServerTemplateSelectorMenuBuilderTest {

    @InjectMocks
    ServerTemplateSelectorMenuBuilder serverTemplateSelectorMenuBuilder;


    private CallerMock<SpecManagementService> specManagementServiceCaller;

    @Mock
    private SpecManagementService specManagementService;


    @Mock
    ServerTemplateSelectorMenuBuilder.ServerTemplateSelectorView view;


    @Before
    public void setup() {
        specManagementServiceCaller = new CallerMock<SpecManagementService>(specManagementService);
        serverTemplateSelectorMenuBuilder.setSpectManagementService(specManagementServiceCaller);
    }

    @Test
    public void testAddServerTemplatesToSelector() {

        ArrayList<ServerTemplate> serverTemplatesList = new ArrayList();

        ServerTemplate st1 = new ServerTemplate("id1", "kie-server-template1");
        st1.addServerInstance(new ServerInstanceKey());

        ServerTemplate st2 = new ServerTemplate("id2", "kie-server-template2");
        st2.addServerInstance(new ServerInstanceKey());

        serverTemplatesList.add(st1);
        serverTemplatesList.add(st2);

        when(specManagementService.listServerTemplates()).thenReturn(serverTemplatesList);
        serverTemplateSelectorMenuBuilder.init();

        verify(specManagementService).listServerTemplates();
        verify(view).addServerTemplate("id1");
        verify(view).addServerTemplate("id2");
        verify(view).setServerTemplateChangeHandler(any(ParameterizedCommand.class));
        verifyNoMoreInteractions(view);

    }

    @Test
    public void testOneServerTemplateAtSelector() {

        ArrayList<ServerTemplate> serverTemplatesList = new ArrayList();

        ServerTemplate st1 = new ServerTemplate("id1", "kie-server-template1");
        st1.addServerInstance(new ServerInstanceKey());

        serverTemplatesList.add(st1);

        when(specManagementService.listServerTemplates()).thenReturn(serverTemplatesList);
        serverTemplateSelectorMenuBuilder.init();

        verify(specManagementService).listServerTemplates();
        verify(view).addServerTemplate("id1");
        verify(view).selectServerTemplate("id1");

        verify(view).setServerTemplateChangeHandler(any(ParameterizedCommand.class));
        verifyNoMoreInteractions(view);

    }

}