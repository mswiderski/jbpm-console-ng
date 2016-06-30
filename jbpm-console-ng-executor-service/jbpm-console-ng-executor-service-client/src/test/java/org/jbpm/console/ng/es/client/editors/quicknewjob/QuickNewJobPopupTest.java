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
package org.jbpm.console.ng.es.client.editors.quicknewjob;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.gwtmockito.WithClassesToStub;
import org.gwtbootstrap3.client.ui.html.Text;
import org.jbpm.console.ng.es.model.RequestDataSetConstants;
import org.jbpm.console.ng.es.model.RequestParameterSummary;
import org.jbpm.console.ng.es.service.ExecutorService;
import org.jbpm.console.ng.gc.client.util.UTCDateBox;
import org.jbpm.console.ng.gc.client.util.UTCTimeBox;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.mocks.CallerMock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
@WithClassesToStub({Text.class})
public class QuickNewJobPopupTest {

    private String JOB_NAME = "JOB_NAME_1";
    private String JOB_TYPE = "JOB_TYPE_1";
    private int JOB_RETRIES = 5;

    @Mock
    private CallerMock<ExecutorService> executorServices;

    @Mock
    private ExecutorService executorServicesMock;

    @Mock
    public UTCTimeBox jobDueDateTime;

    @InjectMocks
    private QuickNewJobPopup quickNewJobPopup;

    @Before
    public void setupMocks() {
        executorServices = new CallerMock<ExecutorService>(executorServicesMock);
        quickNewJobPopup.setExecutorService(executorServices);
    }

    @Test
    public void jobNameIsPassedAsBusinessKeyTest() {
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                final HashMap<String, String> ctxValues = (HashMap) invocationOnMock.getArguments()[2];
                assertTrue(ctxValues.get(RequestDataSetConstants.COLUMN_BUSINESSKEY).equals(JOB_NAME));
                assertTrue(ctxValues.get(RequestDataSetConstants.COLUMN_RETRIES).equals(String.valueOf(JOB_RETRIES)));
                assertTrue(invocationOnMock.getArguments()[2].equals(JOB_TYPE));
                return null;
            }
        }).when(executorServicesMock).scheduleRequest(anyString(), anyString(), any(Date.class), any(Map.class));

        quickNewJobPopup.createJob(JOB_NAME, new Date(), JOB_TYPE, JOB_RETRIES, new ArrayList<RequestParameterSummary>());

        verify(executorServicesMock).scheduleRequest(anyString(),anyString(), any(Date.class), any(HashMap.class));
   }

    @Test
    public void dueTimeSetToFutureTimeTest() {

        final Long nextHalfHour =  UTCDateBox.date2utc(new Date(System.currentTimeMillis() + 1800 * 1000));
        final Long nextHour =  UTCDateBox.date2utc(new Date(System.currentTimeMillis() + 3600 * 1000));
        // now is current time + 30'
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                final Long setTime = (Long) invocationOnMock.getArguments()[0];
                //assert that the jobDueDateTime is set later than 30' in the future and less than 1h
                assertTrue(setTime > nextHalfHour);
                assertTrue(setTime < nextHour);
                return null;
            }
        }).when(jobDueDateTime).setValue(anyLong());


        quickNewJobPopup.cleanForm();

        verify(jobDueDateTime).setValue(anyLong());
    }

}
