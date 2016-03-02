/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalistsynch.controller.local.dba;

import org.noorganization.instalistsynch.model.ModelMapping;

import java.util.List;

/**
 * Interface for db access of Model mapping.
 * Created by tinos_000 on 30.01.2016.
 */
public interface IModelMappingDbController {

    /**
     * Generates a uuid that is unique.
     * @return the generated uuid.
     */
    String generateUuid();

    /**
     * Insert the model element into the database.
     *
     * @param _element the element to insert. Only the uuid should not be set.
     * @return the element with a uuid or null if a failure happened.
     */
    ModelMapping insert(ModelMapping _element);

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
