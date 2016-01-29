package org.noorganization.instalistsynch.model.network.response;

import android.util.Log;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Unit;

import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

/**
 * An Product object with timestamp.
 * Created by tinos_000 on 29.01.2016.
 */
@JsonDeserialize(using = ProductResponseDeserializer.class)
public class ProductResponse {
    public String mUUID;
    public String mName;
    /**
     * The unit of the product. Can also be null if the products has no unit.
     */
    public String mUnitId;
    /**
     * The default amount is usually 1.0f
     */
    public float mDefaultAmount;
    /**
     * The amount to increase or decrease over quick buttons. Usually 1.0f.
     */
    public float mStepAmount;

    public Date mLastChanged;
}

/**
 * Parse ProductResponse from JSON.
 */
class ProductResponseDeserializer extends JsonDeserializer<ProductResponse> {
    private static final String LOG_TAG = CategoryResponseDeserializer.class.getSimpleName();

    @Override
    public ProductResponse deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ProductResponse productResponse = new ProductResponse();
        JsonNode rootNode = jp.readValueAsTree();
        productResponse.mUUID = rootNode.get("id").asText();
        productResponse.mName =  rootNode.get("name").asText();
        productResponse.mUnitId = rootNode.get("unitId").asText();
        productResponse.mUnitId = productResponse.mUnitId.length() == 0 ? null : productResponse.mUnitId;
        productResponse.mDefaultAmount = (float) rootNode.get("defaultAmount").asDouble();
        productResponse.mStepAmount = (float) rootNode.get("stepAmount").asDouble();
        try {
            productResponse.mLastChanged = ISO8601Utils.parse(rootNode.get("lastChanged").asText(), new ParsePosition(0));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Date string has no ISO8601 date format.");
            return null;
        }
        return productResponse;
    }
}