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

package org.jbpm.console.ng.ht.backend.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jbpm.console.ng.ht.model.TaskDefSummary;
import org.jbpm.services.api.model.UserTaskDefinition;
import org.kie.internal.task.api.model.TaskDef;

public class TaskDefHelper {
    public static Collection<TaskDefSummary> adaptCollection(Collection<? extends UserTaskDefinition> tasks) {
        List<TaskDefSummary> tasksdefSummary = new ArrayList<TaskDefSummary>();
        int counter = 0;
        for (UserTaskDefinition t : tasks) {
            tasksdefSummary.add(new TaskDefSummary(counter, t.getName()));
            counter++;
        }

        return tasksdefSummary;
    }

    public static TaskDefSummary adapt(UserTaskDefinition t) {
        return new TaskDefSummary(0, t.getName());
    }

}
