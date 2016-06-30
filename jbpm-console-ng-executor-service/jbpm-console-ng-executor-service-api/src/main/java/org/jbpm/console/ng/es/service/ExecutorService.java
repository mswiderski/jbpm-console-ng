/*
 * Copyright 2013 JBoss by Red Hat.
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

package org.jbpm.console.ng.es.service;

import java.util.Date;
import java.util.Map;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jbpm.console.ng.es.model.RequestDetails;

@Remote
public interface ExecutorService {

    RequestDetails getRequestDetails(String serverTemplateId, Long requestId);

    Long scheduleRequest(String serverTemplateId, String commandName, Map<String, String> ctx);

    Long scheduleRequest(String serverTemplateId, String commandId, Date date, Map<String, String> ctx);

    void cancelRequest(String serverTemplateId, Long requestId);

    void requeueRequest(String serverTemplateId, Long requestId);

}