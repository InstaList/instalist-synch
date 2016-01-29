package org.noorganization.instalistsynch.utils;

/**
 * Utils to manipulate {@link String}'s.
 * Created by tinos_000 on 29.01.2016.
 */
public class StringUtils {

    /**
     * Fetch the first value from a given json string.
     *
     * @param _jsonString the json string to extract the value from.
     * @return the extracted value.
     */
    public static String getFirstValueFromJSON(String _jsonString) {
        String[] splitString = _jsonString.split("\"");
        String value = null;
        if (splitString.length >= 4) {
            value = splitString[3];
        }
        return value;
    }
}
