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

package org.jbpm.console.ng.bd.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface KieSessionEntryPoint {

    long startProcess(String domainId, String processId);

    long startProcess(String domainId, String processId, Map<String, Object> params);

    void abortProcessInstance(long processInstanceId);
    
    void abortProcessInstances(List<Long> processInstanceIds);

    void suspendProcessInstance(long processInstanceId);

    void signalProcessInstance(long processInstanceId, String signalName, Object event);
    
    void signalProcessInstances(List<Long> processInstanceIds, String signalName, Object event);

    void setProcessVariable(long processInstanceId, String variableId, Object value);

    Collection<String> getAvailableSignals(long processInstanceId);

}
