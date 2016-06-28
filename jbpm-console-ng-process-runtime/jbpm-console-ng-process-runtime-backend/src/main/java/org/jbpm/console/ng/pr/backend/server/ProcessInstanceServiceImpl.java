/*
 * Copyright 2014 JBoss by Red Hat.
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
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.bd.model.ProcessInstanceSummary;
import org.jbpm.console.ng.ga.model.QueryFilter;
import org.jbpm.console.ng.pr.service.ProcessInstanceService;
import org.jbpm.console.ng.pr.service.integration.RemoteRuntimeDataService;
import org.uberfire.paging.PageResponse;

import static java.util.stream.Collectors.*;

@Service
@ApplicationScoped
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    @Inject
    private RemoteRuntimeDataService dataService;

    @Override
    public PageResponse<ProcessInstanceSummary> getData(QueryFilter filter) {
        PageResponse<ProcessInstanceSummary> response = new PageResponse<ProcessInstanceSummary>();
        List<ProcessInstanceSummary> processInstancesSums = getProcessInstances(filter);

        response.setStartRowIndex(filter.getOffset());
        response.setTotalRowSize(processInstancesSums.size()-1);
        if(processInstancesSums.size() > filter.getCount()){
            response.setTotalRowSizeExact(false);
        } else{
            response.setTotalRowSizeExact(true);
        }

        if (!processInstancesSums.isEmpty() && processInstancesSums.size() > (filter.getCount() + filter.getOffset())) {
            response.setPageRowList(new ArrayList<ProcessInstanceSummary>(processInstancesSums.subList(filter.getOffset(), filter.getOffset() + filter.getCount())));
            response.setLastPage(false);

        } else {
            response.setPageRowList(new ArrayList<ProcessInstanceSummary>(processInstancesSums));
            response.setLastPage(true);

        }
        return response;

    }

    private List<ProcessInstanceSummary> getProcessInstances(QueryFilter filter) {
        List<Integer> states = null;
        String initiator = "";
        String serverTemplateId = "";
        if (filter.getParams() != null) {
            states = (List<Integer>) filter.getParams().get("states");
            initiator = (String) filter.getParams().get("initiator");
            serverTemplateId = (String) filter.getParams().get("serverTemplateId");
        }
        // append 1 to the count to check if there are further pages
        org.kie.internal.query.QueryFilter qf = new org.kie.internal.query.QueryFilter(filter.getOffset(), filter.getCount() + 1,
                filter.getOrderBy(), filter.isAscending());
        List<ProcessInstanceSummary> processInstancesSums = dataService.getProcessInstances(serverTemplateId, states, qf.getOffset(), qf.getCount());

        if (filter.getParams().get("textSearch") == null || ((String) filter.getParams().get("textSearch")).isEmpty()) {
            return processInstancesSums;
        }

        String textSearch = (String) filter.getParams().get("textSearch");
        processInstancesSums = processInstancesSums
                .stream()
                .filter(pi -> pi.getProcessName().toLowerCase().contains(textSearch))
                .collect(toList());

        return processInstancesSums;
    }

}