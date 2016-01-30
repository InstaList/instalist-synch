package org.noorganization.instalistsynch.controller.local;

import java.util.List;

/**
 * Interface for dbn access of Model synch.
 * Created by tinos_000 on 30.01.2016.
 */
public interface IModelSynchDbController<T> {

    /**
     * Insert the model element into the database.
     *
     * @param _element the element to insert.
     * @return true if insertion was successful, false if not.
     */
    boolean insert(T _element);

    /**
     * Updates the element at the id given by the element.
     *
     * @param _element the element to be updated.
     * @return true if at least one element was updated, false if zero elements were updated.
     */
    boolean update(T _element);

    /**
     * Deletes the given element from the database.
     *
     * @param _element the element to delete.
     * @return true if deleted, false if not.
     */
    boolean delete(T _element);

    /**
     * Get a list of objects by the given where clause.
     *
     * @param _whereClause the where query string.
     * @param _whereParams the params in this query. Just like with sqlite.
     * @return a list of objects matching the criteria, null is nerver be returned only empty Lists if no element was found.
     */
    List<T> get(String _whereClause, String[] _whereParams);
}
