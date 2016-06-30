/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.console.ng.es.client.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;
import org.uberfire.workbench.model.menu.MenuItem;

/**
 * This uses GWT to provide client side compile time resolving of locales. See:
 * http://code.google.com/docreader/#p=google-web-toolkit-doc-1-5&s=google-web- toolkit-doc-1-5&t=DevGuideInternationalization
 * (for more information).
 * <p/>
 * Each method name matches up with a key in Constants.properties (the properties file can still be used on the server). To use
 * this, use <code>GWT.create(Constants.class)</code>.
 */
public interface Constants extends Messages {

    Constants INSTANCE = GWT.create( Constants.class );

    String Queued();

    String FilterQueued();

    String All();

    String FilterAll();

    String Running();

    String FilterRunning();

    String Retrying();

    String FilterRetrying();

    String Error();

    String FilterError();

    String Completed();

    String FilterCompleted();

    String Showing();

    String Cancelled();

    String FilterCancelled();

    String Settings();

    String New_Job();

    String No_Pending_Jobs();

    String Id();

    String Time();

    String CommandName();

    String Message();

    String Key();

    String Value();

    String Due_On();

    String Actions();

    String No_Parameters_added_yet();

    String Add_Parameter();

    String Create();

    String BusinessKey();

    String Type();

    String Retries();

    String Status();

    String RequestsListTitle();

    String No_Jobs_Found();

    String The_Job_Must_Have_A_BusinessKey();

    String The_Job_Must_Have_A_Due_Date_In_The_Future();

    String The_Job_Must_Have_A_Type();

    String The_Job_Must_Have_A_Positive_Number_Of_Reties();

    String Loading();

    String Advanced();

    String Basic();

    String The_Job_Must_Have_A_Valid_Type();

    String Number_Of_Attempted_Retries();

    String Execution_Parameters();

    String Exceptions_Occurred();

    String Ok();

    String Job_Request_Details();

    String Remove();

    String Details();

    String Cancel();

    String Requeue();

    String New_JobList();

    String CancelJob();

    String RequeueJob();

    String RequestScheduled(Long requestId);

    String RequestCancelled(Long requestId);

    String ErrorRetrievingJobs(String message);

    String ClickToEdit();

    String SelectServerTemplate();

}