package org.noorganization.instalistsynch.utils;

/**
 * The mode to sort the entries.
 */
public enum eSORT_MODE {
    ASC("ASC"),
    DESC("DESC");

    private final String mMode;

    /**
     * Constructor
     *
     * @param _string the string by sort mode
     */
    eSORT_MODE(String _string) {
        mMode = _string;
    }

    @Override
    public String toString() {
        return this.mMode;
    }
}