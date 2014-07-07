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

package org.jbpm.console.ng.pr.backend.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jbpm.console.ng.pr.model.VariableSummary;
import org.jbpm.services.api.model.VariableDesc;

public class VariableHelper {

    private static final List<String> excludedVariables = Arrays.asList(new String[] { "processId" });

    public static Collection<VariableSummary> adaptCollection(Collection<VariableDesc> variables) {
        List<VariableSummary> variablesSummary = new ArrayList<VariableSummary>();
        for (VariableDesc v : variables) {

            variablesSummary.add(new VariableSummary(v.getVariableId(), v.getVariableInstanceId(), v.getProcessInstanceId(), v
                    .getOldValue(), v.getNewValue(), v.getDataTimeStamp().getTime(), ""));

        }

        return variablesSummary;
    }

    public static Collection<VariableSummary> adaptCollection(Collection<VariableDesc> variables,
            Map<String, String> properties, long processInstanceId) {
        List<VariableSummary> variablesSummary = new ArrayList<VariableSummary>();
        for (VariableDesc v : variables) {
            if (excludedVariables.contains(v.getVariableId())) {
                continue;
            }
            String type = properties.remove(v.getVariableId());
            variablesSummary.add(new VariableSummary(v.getVariableId(), v.getVariableInstanceId(), v.getProcessInstanceId(), v
                    .getOldValue(), v.getNewValue(), v.getDataTimeStamp().getTime(), type));

        }
        if (!properties.isEmpty()) {
            for (Entry<String, String> entry : properties.entrySet()) {
                variablesSummary.add(new VariableSummary(entry.getKey(), "", processInstanceId, "", "", new Date().getTime(), entry.getValue()));
            }
        }

        return variablesSummary;
    }

    public static VariableSummary adapt(VariableDesc v) {
        return new VariableSummary(v.getVariableId(), v.getVariableInstanceId(), v.getProcessInstanceId(), v.getOldValue(),
                v.getNewValue(), v.getDataTimeStamp().getTime(), "");
    }

}
