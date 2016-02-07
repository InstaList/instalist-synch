package org.noorganization.instalistsynch.model;

/**
 * Holds the indicator if a group member has read or write or both rights.
 * !! --> set both rights to true to indicate authorization else both to false <-- !!
 * !! Currently a placeholder for later integration!!
 *
 * Created by tinos_000 on 07.02.2016.
 */
public class AccessRight {

    private boolean mReadRight;
    private boolean mWriteRight;

    /**
     * Constructor of an AccessRight object.
     * @param readRight true if the read right should be set.
     * @param writeRight  true if the write right should be set.
     */
    public AccessRight(boolean readRight, boolean writeRight) {
        mReadRight = readRight;
        mWriteRight = writeRight;
    }

    public boolean hasReadRight() {
        return mReadRight;
    }

    public void setReadRight(boolean readRight) {
        mReadRight = readRight;
    }

    public boolean hasWriteRight() {
        return mWriteRight;
    }

    public void setWriteRight(boolean writeRight) {
        mWriteRight = writeRight;
    }


}
