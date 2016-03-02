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

package org.noorganization.instalistsynch.controller.synch.impl.generic;

import org.noorganization.instalist.comm.message.UnitInfo;
import org.noorganization.instalist.model.Unit;

import java.util.Date;

/**
 * Created by damihe on 27.02.16.
 */
class UnitInfoConverter implements IInfoConverter<Unit, UnitInfo> {

    @Override
    public Unit toModel(UnitInfo _infoObject) {
        Unit rtn = new Unit();
        if (_infoObject.getName() != null) {
            rtn.mName = _infoObject.getName();
        }
        if (_infoObject.getUUID() != null) {
            rtn.mUUID = _infoObject.getUUID();
        }
        return rtn;
    }

    @Override
    public UnitInfo toInfo(Unit _modelObject, Date _changeDate) {
        UnitInfo rtn = new UnitInfo().withDeleted(false);
        rtn.setName(_modelObject.mName);
        rtn.setUUID(_modelObject.mUUID);
        rtn.setLastChanged(_changeDate);
        return rtn;
    }
}
