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

package org.noorganization.instalistsynch.model;

/**
 * Enum that represents the names of each mapping table name.
 * Do not insert any enum without a string value.
 * Created by tinos_000 on 20.02.2016.
 */
public enum eModelMappingTableNames {

    LIST("list_mapping_table"),
    CATEGORY("category_mapping_table"),
    LIST_ENTRY("list_entry_mapping_table"),
    INGREDIENT("ingredient_mapping_table"),
    PRODUCT("product_mapping_table"),
    RECIPE("recipe_mapping_table"),
    TAG("tag_mapping_table"),
    TAGGED_PRODUCT("tagged_product_mapping_table"),
    UNIT("unit_mapping_table");

    /**
     * Value of the enum.
     */
    private String mText;

    /**
     * Constructor.
     *
     * @param _text the text of the corresponding enum.
     */
    eModelMappingTableNames(String _text) {
        mText = _text;
    }

    @Override
    public String toString() {
        return mText;
    }
}
