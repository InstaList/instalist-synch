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
