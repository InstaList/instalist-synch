/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalistsynch.controller.local.dba;

import android.content.Context;
import android.test.AndroidTestCase;

import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupMemberDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.GroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.GroupMemberDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.impl.SqliteGroupAuthAccessDbController;

/**
 * Created by Desnoo on 05.02.2016.
 */
public class LocalSqliteDbControllerFactoryTest extends AndroidTestCase {

    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mContext = null;
    }

    public void testGetAuthDbController() throws Exception {
        IGroupAuthDbController authDbController = LocalSqliteDbControllerFactory.getGroupAuthDbController(mContext);
        assertNotNull(authDbController);
        assertTrue(authDbController instanceof GroupAuthDbController);

    }

    public void testGetAuthAccessDbController() throws Exception {
        IGroupAuthAccessDbController groupAuthAccessDbController = LocalSqliteDbControllerFactory.getAuthAccessDbController(mContext);
        assertNotNull(groupAuthAccessDbController);
        assertTrue(groupAuthAccessDbController instanceof SqliteGroupAuthAccessDbController);
    }

    public void testGetGroupMemberDbController() throws Exception {
        IGroupMemberDbController groupMemberDbController = LocalSqliteDbControllerFactory.getGroupMemberDbController(mContext);
        assertNotNull(groupMemberDbController);
        assertTrue(groupMemberDbController instanceof GroupMemberDbController);
    }
}