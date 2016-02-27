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
