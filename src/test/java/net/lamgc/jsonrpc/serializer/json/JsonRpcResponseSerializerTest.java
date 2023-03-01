package net.lamgc.jsonrpc.serializer.json;

import com.google.gson.*;
import net.lamgc.jsonrpc.JsonRpcError;
import net.lamgc.jsonrpc.JsonRpcResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static net.lamgc.jsonrpc.JsonRpcConst.*;
import static org.junit.jupiter.api.Assertions.*;


class JsonRpcResponseSerializerTest {

    @Test
    void serialize() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JsonRpcResponse.class, new JsonRpcResponseSerializer())
                .serializeNulls()
                .create();

        JsonPrimitive jsonRpcId = new JsonPrimitive(1);

        JsonRpcResponse normalResp = new JsonRpcResponse(new JsonPrimitive("Success."), jsonRpcId);
        JsonElement normalJson = gson.toJsonTree(normalResp);

        assertTrue(normalJson instanceof JsonObject, "The serialized result is not a JsonObject.");
        JsonObject normalJsonObj = normalJson.getAsJsonObject();

        assertEquals(JSON_RPC_VERSION_VALUE,
                normalJsonObj.get(JSON_RPC_VERSION_FIELD).getAsString(), "JsonRpc specification version error.");
        assertTrue(normalJsonObj.has(JSON_RPC_RESPONSE_RESULT_FIELD),
                "Result is not a JsonRpcError, but the result field does not exist in the serialized result");
        assertFalse(normalJsonObj.has(JSON_RPC_RESPONSE_ERROR_FIELD),
                "The fields result and error appear at the same time.");
        assertEquals(normalResp.getResult(),
                normalJsonObj.get(JSON_RPC_RESPONSE_RESULT_FIELD),
                "The value of the Result field does not match the expected value.");
        assertEquals(jsonRpcId, normalJsonObj.get(JSON_RPC_ID_FIELD),
                "The value of id field does not match the expected value.");


        JsonRpcError error = new JsonRpcError(-1, "error.", null);
        JsonRpcResponse errorRespWithId = new JsonRpcResponse(error, jsonRpcId);
        JsonElement errorRespWithIdJson = gson.toJsonTree(errorRespWithId);

        assertTrue(errorRespWithIdJson instanceof JsonObject, "The serialized result is not a JsonObject.");
        JsonObject errorJsonObj = errorRespWithIdJson.getAsJsonObject();

        assertEquals(JSON_RPC_VERSION_VALUE, errorJsonObj.get(JSON_RPC_VERSION_FIELD).getAsString(),
                "JsonRpc specification version error.");
        assertTrue(errorJsonObj.has(JSON_RPC_RESPONSE_ERROR_FIELD),
                "The response result is a JsonRpcError, but the serialized result has no error field.");
        assertFalse(errorJsonObj.has(JSON_RPC_RESPONSE_RESULT_FIELD),
                "The fields result and error appear at the same time.");
        assertTrue(errorJsonObj.get(JSON_RPC_RESPONSE_ERROR_FIELD).isJsonObject(),
                "The error field is not a JsonObject.");

        JsonObject errorObj = errorJsonObj.get(JSON_RPC_RESPONSE_ERROR_FIELD).getAsJsonObject();
        assertTrue(errorObj.has(JSON_RPC_ERROR_CODE_FIELD),
                "The error object does not have a code field, which is required.");
        assertTrue(errorObj.has(JSON_RPC_ERROR_MESSAGE_FIELD),
                "The error object does not have a message field, which is required.");
        assertEquals(error.getCode(), errorObj.get(JSON_RPC_ERROR_CODE_FIELD).getAsInt(),
                "The code field does not match the expected.");
        assertEquals(error.getMessage(), errorObj.get(JSON_RPC_ERROR_MESSAGE_FIELD).getAsString(),
                "The message field does not match the expectation.");
        assertEquals(jsonRpcId, errorJsonObj.get(JSON_RPC_ID_FIELD),
                "The value of id field does not match the expected value.");

        JsonElement jsonElement = gson.toJsonTree(new JsonRpcResponse(null, JsonNull.INSTANCE));
        assertTrue(jsonElement instanceof JsonObject, "The serialized result is not a JsonObject.");
        JsonObject errorJson = jsonElement.getAsJsonObject();
        assertEquals(JsonNull.INSTANCE, errorJson.get(JSON_RPC_ID_FIELD));

        assertThrows(IllegalArgumentException.class, () -> {
                    Field field = JsonRpcResponse.class.getDeclaredField("id");
                    JsonRpcResponse response = new JsonRpcResponse(null, null);
                    field.setAccessible(true);
                    field.set(response, new JsonObject());
                    field.setAccessible(false);
                    gson.toJsonTree(response);
                },
                "The id field does not accept JsonObject, but does not throw an exception.");
        assertThrows(IllegalArgumentException.class, () -> {
                    Field field = JsonRpcResponse.class.getDeclaredField("id");
                    JsonRpcResponse response = new JsonRpcResponse(null, null);
                    field.setAccessible(true);
                    field.set(response, new JsonArray());
                    field.setAccessible(false);
                    gson.toJsonTree(response);
                },
                "The id field does not accept JsonArray, but does not throw an exception.");
    }

    @Test
    void deserialize() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JsonRpcResponse.class, new JsonRpcResponseSerializer())
                .serializeNulls()
                .create();

        JsonRpcResponse response = gson.fromJson("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}", JsonRpcResponse.class);
        assertEquals(new JsonPrimitive(19), response.getResult());
        assertEquals(new JsonPrimitive(1), response.getId());

        response = gson.fromJson("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": null}", JsonRpcResponse.class);
        assertEquals(new JsonPrimitive(19), response.getResult());
        assertEquals(JsonNull.INSTANCE, response.getId());

        JsonRpcResponse errorResp = gson.fromJson(
                "{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -32601, \"message\": \"Method not found\"}, \"id\": \"1\"}",
                JsonRpcResponse.class);
        assertEquals(new JsonPrimitive("1"), errorResp.getId());
        assertTrue(errorResp.isError());
        assertEquals(new JsonRpcError(-32601, "Method not found", null), errorResp.getResult());

        // 尝试反序列化非 Json Object 类型的 JSON.
        assertThrows(JsonParseException.class, () -> gson.fromJson("[]", JsonRpcResponse.class));

        // 尝试对各种非法 jsonrpc 字段进行解析.
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": \"1.1\", \"result\": 19, \"id\": 1}", JsonRpcResponse.class),
                "The json rpc version number is incorrect, but serialization succeeded.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": 2, \"result\": 19, \"id\": 1}", JsonRpcResponse.class),
                "Json rpc version number type error, but serialization succeeded.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": {}, \"result\": 19, \"id\": 1}", JsonRpcResponse.class),
                "Json rpc version number type error, but serialization succeeded.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": [], \"result\": 19, \"id\": 1}", JsonRpcResponse.class),
                "Json rpc version number type error, but serialization succeeded.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": null, \"result\": 19, \"id\": 1}", JsonRpcResponse.class),
                "Json rpc version number type error, but serialization succeeded.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"result\": 19, \"id\": 1}", JsonRpcResponse.class),
                "The json rpc version number field does not exist, but serialization succeeded.");


        // 尝试对各种非法 id 字段进行解析.
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": \"2.0\", \"result\": 19}", JsonRpcResponse.class),
                "JSON does not contain id field.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": {}}", JsonRpcResponse.class),
                "The type of id field value is incorrect, but serialization succeeded.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": []}", JsonRpcResponse.class),
                "The type of id field value is incorrect, but serialization succeeded.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": true}", JsonRpcResponse.class),
                "The type of id field value is incorrect, but serialization succeeded.");


        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": \"2.0\", \"result\": 19, \"error\": {\"code\": -32601, \"message\": \"Method not found\"}, \"id\": 1}", JsonRpcResponse.class),
                "JSON contains result fields and error fields.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": \"2.0\", \"id\": 1}", JsonRpcResponse.class),
                "JSON does not contain the result field and error field.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": \"2.0\", \"error\": [1, 2, 3, 4, 5], \"id\": 1}", JsonRpcResponse.class),
                "The error field is not a JsonObject.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": \"2.0\", \"error\": true, \"id\": 1}", JsonRpcResponse.class),
                "The error field is not a JsonObject.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": \"2.0\", \"error\": 20, \"id\": 1}", JsonRpcResponse.class),
                "The error field is not a JsonObject.");
        assertThrows(JsonParseException.class, () ->
                        gson.fromJson("{\"jsonrpc\": \"2.0\", \"error\": \"test\", \"id\": 1}", JsonRpcResponse.class),
                "The error field is not a JsonObject.");
    }
}