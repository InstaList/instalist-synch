package org.noorganization.instalistsynch.controller.synch;

import org.noorganization.instalistsynch.controller.synch.impl.LocalListSynch;

/**
 * Manager that handles all the synchronization work.
 * Created by tinos_000 on 19.02.2016.
 */
public class SynchManager {


    public void synchronize(int group){
        ILocalListSynch listSynch = new LocalListSynch();

        listSynch.refreshLocalMapping();
    }
}
