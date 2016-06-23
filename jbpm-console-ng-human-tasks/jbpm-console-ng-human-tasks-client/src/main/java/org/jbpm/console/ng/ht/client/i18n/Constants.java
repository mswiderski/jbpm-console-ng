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

package org.jbpm.console.ng.ht.client.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
 * This uses GWT to provide client side compile time resolving of locales. See:
 * http://code.google.com/docreader/#p=google-web-toolkit-doc-1-5&s=google-web- toolkit-doc-1-5&t=DevGuideInternationalization
 * (for more information).
 * <p/>
 * Each method name matches up with a key in Constants.properties (the properties file can still be used on the server). To use
 * this, use <code>GWT.create(Constants.class)</code>.
 */
public interface Constants extends Messages {

    Constants INSTANCE = GWT.create(Constants.class);

    String Tasks_List();

    String Grid();

    String New_Task();

    String Personal();

    String FilterPersonal();

    String Group();

    String FilterGroup();

    String Active();

    String FilterActive();

    String All();

    String FilterAll();

    String No_Tasks_Found();

    String Priority();

    String Task();

    String Id();

    String Status();

    String Due_On();

    String Parent();

    String Complete();

    String Open();

    String Release();

    String Claim();

    String Work();

    String Start();

    String Details();

    String Actions();

    String User();

    String Process_Id();

    String Process_Instance_Id();

    String ProcessSessionId();

    String WorkItemId();

    String Process_Name();

    String Process_Definition_Id();

    String Process_Instance_Details();

    String No_Comments_For_This_Task();

    String Comment();

    String At();

    String Added_By();

    String Add_Comment();

    String Task_Must_Have_A_Name();

    String Create();

    String Task_Name();

    String Quick_Task();

    String Description();

    String Comments();

    String Filters();

    String Process_Context();

    String Update();

    String Form();

    String Advanced();

    String Basic();

    String Refresh();

    String Add_User();

    String Add_Group();

    String Remove_User();

    String Assignments();

    String Created_On();

    String DeploymentId();

    String DueDate();

    String ParentId();

    String Forward();

    String Delegate();

    String Potential_Owners();

    String No_Potential_Owners();

    String Save();

    String Delete();

    String Calendar();

    String Logs();

    String Task_Log();

    String Provide_User_Or_Group();

    String Task_Admin();

    String FilterTaskAdmin();

    String Delegate_User();

    String Reminder();

    String Actual_Owner();

    String CreatedBy();

    String ActivationTime();

    String Reminder_Details();

    String No_Actual_Owner();

    String Loading();

    String DelegationUserInputRequired();

    String DelegationUnable();

    String DelegationSuccessfully();

    String TaskSuccessfullyDelegated();

    String TaskCreatedWithId(String id);

    String High();

    String Medium();

    String Low();

    String Close();

    String Created();

    String Ready();

    String Reserved();

    String InProgress();

    String Failed();

    String Error();

    String Obsolete();

    String Completed();

    String Administrator();

    String Task_Form();

    String Task_Form_Name();

    String Task_Form_DeploymentId();

    String New_FilteredList();

    String Administrators();

    String UnexpectedError(String errorMessage);

    String TaskReleased(String taskId);

    String TaskClaimed(String taskId);

    String TaskSuccessfullyForwarded();

    String ReminderSentTo(String userName);

    String PleaseEnterUserOrAGroupToDelegate();

    String CommentCannotBeEmpty();

    String CommentDeleted();

    String TaskDetailsUpdatedForTaskId(Long taskId);
}
