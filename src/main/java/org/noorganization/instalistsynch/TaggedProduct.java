package org.noorganization.instalistsynch;

import java.util.List;

/**
 * Created by tinos_000 on 15.12.2015.
 */
public class TaggedProduct {

    public int mCode;
    public List<String> mString;


    public int getmCode() {
        return mCode;
    }

    public void setmCode(int mCode) {
        this.mCode = mCode;
    }

    public List<String> getmString() {
        return mString;
    }

    public void setmString(List<String> mString) {
        this.mString = mString;
    }


    @Override
    public String toString() {
        return "TaggedProduct{" +
                "mCode=" + mCode +
                ", mString=" + mString +
                '}';
    }
}

