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
@JsonDeserialize(using = IngredientResponseDeserializer.class)
public class IngredientResponse {
    public Date mLastChanged;
    public String mUUID;
    public String mProductUUID;
    public String mRecipeUUID;
    public float mAmount;
}

/**
 * Parse CategoryResponse from JSON.
 */
class IngredientResponseDeserializer extends JsonDeserializer<IngredientResponse> {
    private static final String LOG_TAG = CategoryResponseDeserializer.class.getSimpleName();

    @Override
    public IngredientResponse deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        IngredientResponse ingredientResponse = new IngredientResponse();
        JsonNode rootNode = jp.readValueAsTree();
        ingredientResponse.mUUID = rootNode.get("id").asText();
        ingredientResponse.mAmount = (float) rootNode.get("amount").asDouble();
        ingredientResponse.mProductUUID = rootNode.get("productId").asText();
        ingredientResponse.mRecipeUUID = rootNode.get("recipeId").asText();
        try {
            ingredientResponse.mLastChanged = ISO8601Utils.parse(rootNode.get("lastChanged").asText(), new ParsePosition(0));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Date string has no ISO8601 date format.");
            return null;
        }
        return ingredientResponse;
    }
}