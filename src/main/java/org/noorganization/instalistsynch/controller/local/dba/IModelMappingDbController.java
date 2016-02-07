package org.noorganization.instalistsynch.controller.local.dba;

import org.noorganization.instalistsynch.model.network.ModelMapping;

import java.util.List;

/**
 * Interface for db access of Model mapping.
 * Created by tinos_000 on 30.01.2016.
 */
public interface IModelMappingDbController {

    /**
     * Insert the model element into the database.
     *
     * @param _element the element to insert.
     * @return true if insertion was successful, false if not.
     */
    boolean insert(ModelMapping _element);

    /**
     * Updates the element at the id given by the element.
     *
     * @param _element the element to be updated.
     * @return true if at least one element was updated, false if zero elements were updated.
     */
    boolean update(ModelMapping _element);

    /**
     * Deletes the given element from the database.
     *
     * @param _element the element to delete.
     * @return true if deleted, false if not.
     */
    boolean delete(ModelMapping _element);

    /**
     * Get a list of objects by the given where clause.
     *
     * @param _whereClause the where query string.
     * @param _whereParams the params in this query. Just like with sqlite.
     * @return a list of objects matching the criteria, null is nerver be returned only empty Lists if no element was found.
     */
    List<ModelMapping> get(String _whereClause, String[] _whereParams);
}
