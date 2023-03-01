package net.lamgc.jsonrpc.serializer.json;

import com.google.gson.*;
import net.lamgc.jsonrpc.JsonRpcRequest;
import net.lamgc.jsonrpc.JsonRpcUtils;

import java.lang.reflect.Type;

import static net.lamgc.jsonrpc.JsonRpcConst.*;

/**
 *
 */
public class JsonRpcRequestSerializer implements JsonSerializer<JsonRpcRequest>, JsonDeserializer<JsonRpcRequest> {

    @Override
    public JsonRpcRequest deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObj = json.getAsJsonObject();
        if (!jsonObj.has(JSON_RPC_VERSION_FIELD)) {
            throw new JsonParseException("The specification version number field is missing.");
        } else {
            JsonElement version = jsonObj.get(JSON_RPC_VERSION_FIELD);
            if (!version.isJsonPrimitive()) {
                throw new JsonParseException("The specification version number field must be a JsonPrimitive.");
            }
            String versionValue = version.getAsJsonPrimitive().getAsString();
            if (!JSON_RPC_VERSION_VALUE.equals(versionValue)) {
                throw new JsonParseException("Unsupported JsonRpc specification version: " + versionValue);
            }
        }

        if (!jsonObj.has(JSON_RPC_REQUEST_METHOD_FIELD)) {
            throw new JsonParseException("The method field is missing");
        } else if (!jsonObj.get(JSON_RPC_REQUEST_METHOD_FIELD).isJsonPrimitive()) {
            throw new JsonParseException("The value of method field is not of JsonPrimitive type.");
        } else if (!jsonObj.get(JSON_RPC_REQUEST_METHOD_FIELD).getAsJsonPrimitive().isString()) {
            throw new JsonParseException("The method name field must be a string.");
        }
        String method = jsonObj.get(JSON_RPC_REQUEST_METHOD_FIELD).getAsString();

        JsonElement params = null;
        if (jsonObj.has(JSON_RPC_REQUEST_PARAMS_FIELD) && !jsonObj.get(JSON_RPC_REQUEST_PARAMS_FIELD).isJsonNull()) {
            JsonElement paramsJson = jsonObj.get(JSON_RPC_REQUEST_PARAMS_FIELD);
            if (paramsJson.isJsonArray() || paramsJson.isJsonObject()) {
                params = paramsJson;
            } else {
                throw new JsonParseException("The JSON has an params field, " +
                        "but the value of the field is not the correct type: " + paramsJson.getClass().getName());
            }
        }

        JsonPrimitive id = null;
        if (jsonObj.has(JSON_RPC_ID_FIELD)) {
            JsonElement idJson = jsonObj.get(JSON_RPC_ID_FIELD);
            if (idJson.isJsonPrimitive()
                    && (idJson.getAsJsonPrimitive().isString() || idJson.getAsJsonPrimitive().isNumber())) {
                id = idJson.getAsJsonPrimitive();
            } else {
                throw new JsonParseException("The JSON has an id field, " +
                        "but the value of the field is not the correct type: " + idJson.getClass().getName());
            }
        }

        return new JsonRpcRequest(method, params, id);
    }

    @Override
    public JsonElement serialize(JsonRpcRequest src, Type typeOfSrc, JsonSerializationContext context) {
        JsonRpcUtils.validateRequest(src);
        JsonObject result = new JsonObject();
        result.addProperty(JSON_RPC_VERSION_FIELD, JSON_RPC_VERSION_VALUE);
        result.addProperty(JSON_RPC_REQUEST_METHOD_FIELD, src.getMethod());
        if (src.getParams() != null) {
            result.add(JSON_RPC_REQUEST_PARAMS_FIELD, src.getParams());
        }
        if (src.getId() != null) {
            result.add(JSON_RPC_ID_FIELD, src.getId());
        }

        return result;
    }
}
