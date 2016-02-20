package org.noorganization.instalistsynch.model;

/**
 * The mode to sort the entries.
 */
public enum eSortMode {
    ASC("ASC"),
    DESC("DESC");

    private final String mMode;

    /**
     * Constructor
     *
     * @param _string the string by sort mode
     */
    eSortMode(String _string) {
        mMode = _string;
    }

    @Override
    public String toString() {
        return this.mMode;
    }
}