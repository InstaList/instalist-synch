package org.noorganization.instalistsynch.model.network.response;

import android.util.Log;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.model.Tag;

import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

/**
 * An TagResponse object with timestamp.
 * Created by tinos_000 on 29.01.2016.
 */
@JsonDeserialize(using = TagResponseDeserializer.class)
public class TagResponse extends Tag {
    /**
     * Time this object was last changed.
     */
    public Date mLastChanged;
}

/**
 * Parse TagResponse from JSON.
 */
class TagResponseDeserializer extends JsonDeserializer<TagResponse> {
    private static final String LOG_TAG = CategoryResponseDeserializer.class.getSimpleName();

    @Override
    public TagResponse deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        TagResponse tagResponse = new TagResponse();
        JsonNode rootNode = jp.readValueAsTree();
        tagResponse.mUUID = rootNode.get("id").asText();
        tagResponse.mName = rootNode.get("name").asText();
        try {
            tagResponse.mLastChanged = ISO8601Utils.parse(rootNode.get("lastChanged").asText(), new ParsePosition(0));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Date string has no ISO8601 date format.");
            return null;
        }
        return tagResponse;
    }
}