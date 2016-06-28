/*
 * Copyright 2012 JBoss by Red Hat.
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
package org.jbpm.console.ng.pr.admin.backend.server;

import java.util.Map;
import java.util.stream.IntStream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.pr.admin.service.ProcessAdminService;
import org.jbpm.console.ng.pr.service.ProcessService;

@Service
@ApplicationScoped
public class ProcessAdminServiceImpl implements ProcessAdminService {

    @Inject
    private ProcessService processService;

    @Override
    public void generateMockInstances(final String serverTemplateId,
                                      final String containerId,
                                      final String processId,
                                      final String correlationKey,
                                      final Map<String, Object> params,
                                      final Integer amountOfInstances) {
        IntStream.range(0, amountOfInstances)
                .forEach(e -> processService.startProcess(
                        serverTemplateId, containerId, processId, correlationKey, params));
    }

}