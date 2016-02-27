package org.noorganization.instalistsynch.controller.synch.impl.generic;

import org.noorganization.instalist.comm.message.EntityObject;

import java.util.Date;

/**
 * Created by damihe on 27.02.16.
 */
public interface IInfoConverter<T, U extends EntityObject> {

    T toModel(U _infoObject);

    U toInfo(T _modelObject, Date _changeDate);
}
