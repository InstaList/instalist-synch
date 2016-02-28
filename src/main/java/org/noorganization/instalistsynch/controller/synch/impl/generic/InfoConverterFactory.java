package org.noorganization.instalistsynch.controller.synch.impl.generic;

import org.noorganization.instalist.model.Unit;

/**
 * Created by damihe on 27.02.16.
 */
public class InfoConverterFactory {

    private static UnitInfoConverter sUnitInfoConverter;

    public static IInfoConverter<?, ?> getConverter(Class _class) {
        if (_class == Unit.class) {
            if (sUnitInfoConverter == null) {
                sUnitInfoConverter = new UnitInfoConverter();
            }
            return sUnitInfoConverter;
        }
        return null;
    }
}
