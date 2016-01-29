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
 * An TaggedProductResponse object with timestamp.
 * Created by tinos_000 on 29.01.2016.
 */
@JsonDeserialize(using = TaggedProductResponseDeserializer.class)
public class TaggedProductResponse {
    /**
     * Unique taggedproduct id.
     */
    public String mUUID;
    /**
     * The associated tag of this element.
     */
    public String mTagUUID;
    /**
     * The associated product of this element.
     */
    public String mProductUUID;
    /**
     * Time this object was last changed.
     */
    public Date mLastChanged;
}

/**
 * Parse TaggedProductResponse from JSON.
 */
class TaggedProductResponseDeserializer extends JsonDeserializer<TaggedProductResponse> {
    private static final String LOG_TAG = CategoryResponseDeserializer.class.getSimpleName();

    @Override
    public TaggedProductResponse deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        TaggedProductResponse TaggedProductResponse = new TaggedProductResponse();
        JsonNode rootNode = jp.readValueAsTree();
        TaggedProductResponse.mUUID = rootNode.get("id").asText();
        TaggedProductResponse.mProductUUID = rootNode.get("productId").asText();
        TaggedProductResponse.mTagUUID = rootNode.get("tagId").asText();
        try {
            TaggedProductResponse.mLastChanged = ISO8601Utils.parse(rootNode.get("lastChanged").asText(), new ParsePosition(0));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Date string has no ISO8601 date format.");
            return null;
        }
        return TaggedProductResponse;
    }
}