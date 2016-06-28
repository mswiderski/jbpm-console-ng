/*
 * Copyright 2016 JBoss by Red Hat.
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

package org.jbpm.console.ng.pr.backend.server.integration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.console.ng.pr.service.integration.RemoteProcessImageService;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.UIServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ApplicationScoped
public class RemoteProcessImageServiceImpl implements RemoteProcessImageService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteProcessImageServiceImpl.class);

    @Inject
    private KieServerIntegration kieServerIntegration;

    protected UIServicesClient getClient(String serverTemplateId, String containerId) {
        KieServicesClient client = kieServerIntegration.getServerClient(serverTemplateId, containerId);
        if (client == null) {
            throw new RuntimeException("No client to interact with server " + serverTemplateId);
        }

        return client.getServicesClient(UIServicesClient.class);
    }

    @Override
    public String getProcessInstanceDiagram(String serverTemplateId, String containerId, Long processInstanceId) {
        UIServicesClient uiServicesClient = getClient(serverTemplateId, containerId);

        return uiServicesClient.getProcessInstanceImage(containerId, processInstanceId);
    }

    @Override
    public String getProcessDiagram(String serverTemplateId, String containerId, String processId) {
        UIServicesClient uiServicesClient = getClient(serverTemplateId, containerId);

        return uiServicesClient.getProcessImage(containerId, processId);
    }

}