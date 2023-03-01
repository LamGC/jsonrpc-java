package net.lamgc.jsonrpc.serializer.json;

import com.google.gson.*;
import net.lamgc.jsonrpc.JsonRpcRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static net.lamgc.jsonrpc.JsonRpcConst.*;
import static org.junit.jupiter.api.Assertions.*;

class JsonRpcRequestSerializerTest {

    @Test
    void deserialize() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JsonRpcRequest.class, new JsonRpcRequestSerializer())
                .create();

        JsonArray paramList = new JsonArray();
        paramList.add(42);
        paramList.add(23);
        JsonRpcRequest request = gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class);
        assertEquals("subtract", request.getMethod());
        assertEquals(paramList, request.getParams());
        assertEquals(new JsonPrimitive(1), request.getId());

        request = gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": \"req1\"}", JsonRpcRequest.class);
        assertEquals("subtract", request.getMethod());
        assertEquals(paramList, request.getParams());
        assertEquals(new JsonPrimitive("req1"), request.getId());

        request = gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"id\": \"req1\"}", JsonRpcRequest.class);
        assertEquals("subtract", request.getMethod());
        assertNull(request.getParams());
        assertEquals(new JsonPrimitive("req1"), request.getId());

        request = gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": [42, 23]}", JsonRpcRequest.class);
        assertEquals("subtract", request.getMethod());
        assertEquals(paramList, request.getParams());
        assertNull(request.getId());

        request = gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\"}", JsonRpcRequest.class);
        assertEquals("subtract", request.getMethod());
        assertNull(request.getParams());
        assertNull(request.getId());

        JsonObject namedParams = new JsonObject();
        namedParams.addProperty("subtrahend", 23);
        namedParams.addProperty("minuend", 42);
        request = gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": {\"subtrahend\": 23, \"minuend\": 42}, \"id\": 3}", JsonRpcRequest.class);
        assertEquals("subtract", request.getMethod());
        assertEquals(namedParams, request.getParams());
        assertEquals(new JsonPrimitive(3), request.getId());

        request = gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": {}}", JsonRpcRequest.class);
        assertEquals("subtract", request.getMethod());
        assertEquals(new JsonObject(), request.getParams());
        assertNull(request.getId());

        request = gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": []}", JsonRpcRequest.class);
        assertEquals("subtract", request.getMethod());
        assertEquals(new JsonArray(), request.getParams());
        assertNull(request.getId());


        // Bad version
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"1.0\", \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": 1, \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": true, \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": {}, \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": [], \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": null, \"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"method\": \"subtract\", \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));


        // Bad method name
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": [], \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": {}, \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": null, \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": true, \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": 123, \"params\": [42, 23], \"id\": 1}", JsonRpcRequest.class));


        // Bad params
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": 123, \"id\": \"req1\"}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": true, \"id\": \"req1\"}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": \"test\", \"id\": \"req1\"}", JsonRpcRequest.class));


        // Bad id
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": null, \"id\": {\"field\": 123}}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": null, \"id\": [42, 23]}", JsonRpcRequest.class));
        assertThrows(JsonParseException.class, () ->
                gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"subtract\", \"params\": null, \"id\": true}", JsonRpcRequest.class));
    }

    @Test
    void serialize() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JsonRpcRequest.class, new JsonRpcRequestSerializer())
                .create();

        // Normal request
        JsonElement json = gson.toJsonTree(new JsonRpcRequest("testMethod", new JsonArray(), new JsonPrimitive(1)));
        assertTrue(json instanceof JsonObject, "");
        JsonObject jsonObj = json.getAsJsonObject();
        assertTrue(jsonObj.has(JSON_RPC_VERSION_FIELD));
        assertTrue(jsonObj.get(JSON_RPC_VERSION_FIELD).isJsonPrimitive());
        assertEquals(JSON_RPC_VERSION_VALUE, jsonObj.get(JSON_RPC_VERSION_FIELD).getAsString());
        assertTrue(jsonObj.has(JSON_RPC_REQUEST_METHOD_FIELD));
        assertTrue(jsonObj.get(JSON_RPC_REQUEST_METHOD_FIELD).isJsonPrimitive());
        assertEquals("testMethod", jsonObj.get(JSON_RPC_REQUEST_METHOD_FIELD).getAsString());
        assertTrue(jsonObj.has(JSON_RPC_REQUEST_PARAMS_FIELD));
        assertTrue(jsonObj.get(JSON_RPC_REQUEST_PARAMS_FIELD).isJsonArray());
        assertTrue(jsonObj.has(JSON_RPC_ID_FIELD));
        assertEquals(new JsonPrimitive(1), jsonObj.get(JSON_RPC_ID_FIELD));


        json = gson.toJsonTree(new JsonRpcRequest("testMethod", new JsonObject(), new JsonPrimitive(1)));
        assertTrue(json instanceof JsonObject, "");
        jsonObj = json.getAsJsonObject();
        assertTrue(jsonObj.has(JSON_RPC_VERSION_FIELD));
        assertTrue(jsonObj.get(JSON_RPC_VERSION_FIELD).isJsonPrimitive());
        assertEquals(JSON_RPC_VERSION_VALUE, jsonObj.get(JSON_RPC_VERSION_FIELD).getAsString());
        assertTrue(jsonObj.has(JSON_RPC_REQUEST_METHOD_FIELD));
        assertTrue(jsonObj.get(JSON_RPC_REQUEST_METHOD_FIELD).isJsonPrimitive());
        assertEquals("testMethod", jsonObj.get(JSON_RPC_REQUEST_METHOD_FIELD).getAsString());
        assertTrue(jsonObj.has(JSON_RPC_REQUEST_PARAMS_FIELD));
        assertTrue(jsonObj.get(JSON_RPC_REQUEST_PARAMS_FIELD).isJsonObject());
        assertTrue(jsonObj.has(JSON_RPC_ID_FIELD));
        assertEquals(new JsonPrimitive(1), jsonObj.get(JSON_RPC_ID_FIELD));


        json = gson.toJsonTree(new JsonRpcRequest("testMethod", new JsonObject(), null));
        assertTrue(json instanceof JsonObject, "");
        jsonObj = json.getAsJsonObject();
        assertTrue(jsonObj.has(JSON_RPC_VERSION_FIELD));
        assertTrue(jsonObj.get(JSON_RPC_VERSION_FIELD).isJsonPrimitive());
        assertEquals(JSON_RPC_VERSION_VALUE, jsonObj.get(JSON_RPC_VERSION_FIELD).getAsString());
        assertTrue(jsonObj.has(JSON_RPC_REQUEST_METHOD_FIELD));
        assertTrue(jsonObj.get(JSON_RPC_REQUEST_METHOD_FIELD).isJsonPrimitive());
        assertEquals("testMethod", jsonObj.get(JSON_RPC_REQUEST_METHOD_FIELD).getAsString());
        assertTrue(jsonObj.has(JSON_RPC_REQUEST_PARAMS_FIELD));
        assertTrue(jsonObj.get(JSON_RPC_REQUEST_PARAMS_FIELD).isJsonObject());
        assertFalse(jsonObj.has(JSON_RPC_ID_FIELD));


        json = gson.toJsonTree(new JsonRpcRequest("testMethod", null, null));
        assertTrue(json instanceof JsonObject, "");
        jsonObj = json.getAsJsonObject();
        assertTrue(jsonObj.has(JSON_RPC_VERSION_FIELD));
        assertTrue(jsonObj.get(JSON_RPC_VERSION_FIELD).isJsonPrimitive());
        assertEquals(JSON_RPC_VERSION_VALUE, jsonObj.get(JSON_RPC_VERSION_FIELD).getAsString());
        assertTrue(jsonObj.has(JSON_RPC_REQUEST_METHOD_FIELD));
        assertTrue(jsonObj.get(JSON_RPC_REQUEST_METHOD_FIELD).isJsonPrimitive());
        assertEquals("testMethod", jsonObj.get(JSON_RPC_REQUEST_METHOD_FIELD).getAsString());
        assertFalse(jsonObj.has(JSON_RPC_REQUEST_PARAMS_FIELD));


        assertThrows(IllegalArgumentException.class, () -> {
            Field field = JsonRpcRequest.class.getDeclaredField("method");
            field.setAccessible(true);
            JsonRpcRequest request = new JsonRpcRequest("", new JsonObject(), new JsonPrimitive(1));
            field.set(request, null);
            field.setAccessible(true);
            gson.toJsonTree(request);
        });
    }
}