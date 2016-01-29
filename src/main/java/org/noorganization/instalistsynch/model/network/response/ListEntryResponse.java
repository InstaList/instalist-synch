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
 * An Ingredient object with timestamp.
 * Created by tinos_000 on 29.01.2016.
 */
@JsonDeserialize(using = ListEntryResponseDeserializer.class)
public class ListEntryResponse {
    /**
     * The unique id.
     */
    public String mUUID;
    /**
     * The shoppinglist id where it is placed.
     */
    public String mListUUID;
    /**
     * The referenced product id.
     */
    public String mProductUUID;
    /**
     * The amount of product that's listed
     */
    public float mAmount;
    /**
     * Whether a product is struck through, i.e. because it's already bought.
     */
    public boolean mStruck;
    /**
     * Priority of this listentry.
     */
    public int mPriority;
    /**
     * Date when last changed.
     */
    public Date mLastChanged;
}

/**
 * Parse ListEntryResponse from JSON.
 */
class ListEntryResponseDeserializer extends JsonDeserializer<ListEntryResponse> {
    private static final String LOG_TAG = CategoryResponseDeserializer.class.getSimpleName();

    @Override
    public ListEntryResponse deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ListEntryResponse listEntryResponse = new ListEntryResponse();
        JsonNode rootNode = jp.readValueAsTree();
        listEntryResponse.mUUID = rootNode.get("id").asText();
        listEntryResponse.mAmount = (float) rootNode.get("amount").asDouble();
        listEntryResponse.mListUUID = rootNode.get("shoppingListId").asText();
        listEntryResponse.mProductUUID = rootNode.get("productId").asText();
        listEntryResponse.mStruck = rootNode.get("struck").asBoolean();
        listEntryResponse.mPriority = rootNode.get("priority").asInt();
        try {
            listEntryResponse.mLastChanged = ISO8601Utils.parse(rootNode.get("lastChanged").asText(), new ParsePosition(0));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Date string has no ISO8601 date format.");
            return null;
        }
        return listEntryResponse;
    }
}