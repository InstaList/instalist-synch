package org.noorganization.instalistsynch.model.network.response;

import android.util.Log;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

/**
 * An ShoppingListResponse object with timestamp.
 * Created by tinos_000 on 29.01.2016.
 */
@JsonDeserialize(using = ShoppingListResponseDeserializer.class)
public class ShoppingListResponse {
    public String mUUID;
    public String mName;
    /**
     * The unit of the product. Can also be null if the products has no unit.
     */
    public String mCategoryId;

    /**
     * Time this object was last changed.
     */
    public Date mLastChanged;
}

/**
 * Parse ShoppingListResponse from JSON.
 */
class ShoppingListResponseDeserializer extends JsonDeserializer<ShoppingListResponse> {
    private static final String LOG_TAG = CategoryResponseDeserializer.class.getSimpleName();

    @Override
    public ShoppingListResponse deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ShoppingListResponse shoppingListResponse = new ShoppingListResponse();
        JsonNode rootNode = jp.readValueAsTree();
        shoppingListResponse.mUUID = rootNode.get("id").asText();
        shoppingListResponse.mName = rootNode.get("name").asText();
        shoppingListResponse.mCategoryId = rootNode.get("categoryId").asText();
        shoppingListResponse.mCategoryId = shoppingListResponse.mCategoryId.length() == 0 ? null : shoppingListResponse.mCategoryId;
        try {
            shoppingListResponse.mLastChanged = ISO8601Utils.parse(rootNode.get("lastChanged").asText(), new ParsePosition(0));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Date string has no ISO8601 date format.");
            return null;
        }
        return shoppingListResponse;
    }
}