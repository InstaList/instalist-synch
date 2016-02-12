package org.noorganization.instalistsynch.controller.local.dba.exception;

/**
 * Exception that is thrown when an error with the sqliteMappingDbController happened.
 * Created by Desnoo on 12.02.2016.
 */
public class SqliteMappingDbControllerException extends Exception{

    public SqliteMappingDbControllerException(String detailMessage) {
        super(detailMessage);
    }

}
