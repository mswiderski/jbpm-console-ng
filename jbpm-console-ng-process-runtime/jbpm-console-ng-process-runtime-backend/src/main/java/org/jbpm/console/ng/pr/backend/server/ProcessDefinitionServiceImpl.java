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
import org.jbpm.console.ng.bd.model.ProcessSummary;
import org.jbpm.console.ng.ga.model.QueryFilter;
import org.jbpm.console.ng.pr.service.ProcessDefinitionService;
import org.jbpm.console.ng.pr.service.integration.RemoteRuntimeDataService;
import org.uberfire.paging.PageResponse;

@Service
@ApplicationScoped
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    @Inject
    private RemoteRuntimeDataService dataService;

    @Override
    public PageResponse<ProcessSummary> getData(final QueryFilter filter) {
        PageResponse<ProcessSummary> response = new PageResponse<ProcessSummary>();
        List<ProcessSummary> processDefsSums = getProcessDefinitions(filter);
        
        response.setStartRowIndex(filter.getOffset());
        response.setTotalRowSize(processDefsSums.size()-1);
        if(processDefsSums.size() > filter.getCount()){
            response.setTotalRowSizeExact(false);
        } else{
            response.setTotalRowSizeExact(true);
        }
        response.setPageRowList(processDefsSums);

        if (!processDefsSums.isEmpty() && processDefsSums.size() > (filter.getCount() + filter.getOffset())) {
            response.setPageRowList(new ArrayList<ProcessSummary>(processDefsSums.subList(filter.getOffset(), filter.getOffset() + filter.getCount())));
            response.setLastPage(false);

        } else {
            response.setPageRowList(new ArrayList<ProcessSummary>(processDefsSums));
            response.setLastPage(true);

        }
        return response;

    }

    private List<ProcessSummary> getProcessDefinitions(final QueryFilter filter) {
        String serverTemplateId = (String) filter.getParams().get("serverTemplateId");
        // append 1 to the count to check if there are further pages
        org.kie.internal.query.QueryFilter qf = new org.kie.internal.query.QueryFilter(filter.getOffset(), filter.getCount()+1,
                filter.getOrderBy(), filter.isAscending());
        List<ProcessSummary> processDefsSums = null;
        if((String)filter.getParams().get("textSearch") != null && !((String)filter.getParams().get("textSearch")).equals("")){
            processDefsSums = dataService.getProcessesByFilter(serverTemplateId, ((String)filter.getParams().get("textSearch")), qf.getOffset(), qf.getCount(), qf.getOrderBy(), qf.isAscending());
        }else{
            processDefsSums = dataService.getProcesses(serverTemplateId, qf.getOffset(), qf.getCount(), qf.getOrderBy(), qf.isAscending());
        }

        return processDefsSums;
    }

}