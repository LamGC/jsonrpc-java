package net.lamgc.jsonrpc;

import com.google.gson.*;
import org.example.not_named_parameters.SimpleInterface;
import org.example.testing.named_parameters.RemoteInterface;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static net.lamgc.jsonrpc.JsonRpcConst.*;
import static org.junit.jupiter.api.Assertions.*;

class JsonRpcUtilsTest {

    @Test
    void instantiationTest() {
        assertThrows(RuntimeException.class, () -> {
            Constructor<JsonRpcUtils> constructor = JsonRpcUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            try {
                constructor.newInstance();
            } catch (InvocationTargetException e) {
                throw e.getCause();
            } finally {
                constructor.setAccessible(false);
            }
        });
    }

    @Test
    void validateRequest() {
        JsonArray paramArray = new JsonArray();
        paramArray.add("test");
        paramArray.add(123);
        paramArray.add(false);
        JsonObject paramObject = new JsonObject();
        paramObject.addProperty("p1", "test");
        paramObject.addProperty("p2", 123);
        paramObject.addProperty("p3", false);
        JsonRpcRequest request = new JsonRpcRequest("test", paramArray, new JsonPrimitive(1));
        assertDoesNotThrow(() -> JsonRpcUtils.validateRequest(request));
        assertDoesNotThrow(() -> JsonRpcUtils.validateRequest(request));
        assertDoesNotThrow(() -> JsonRpcUtils.validateRequest(new JsonRpcRequest("test", paramArray, null)));

        assertThrows(IllegalArgumentException.class, () ->
                JsonRpcUtils.validateRequest(new JsonRpcRequest("", paramArray, new JsonPrimitive("req1"))));
        assertThrows(IllegalArgumentException.class, () -> {
            Field field = JsonRpcRequest.class.getDeclaredField("method");
            field.setAccessible(true);
            JsonRpcRequest req = new JsonRpcRequest("", paramArray, new JsonPrimitive("req1"));
            field.set(req, null);
            field.setAccessible(true);
            JsonRpcUtils.validateRequest(req);
        });

        assertThrows(IllegalArgumentException.class, () ->
                JsonRpcUtils.validateRequest(new JsonRpcRequest("test", JsonNull.INSTANCE, new JsonPrimitive("req1"))));
        assertThrows(IllegalArgumentException.class, () ->
                JsonRpcUtils.validateRequest(new JsonRpcRequest("test", new JsonPrimitive(123), new JsonPrimitive("req1"))));
        assertThrows(IllegalArgumentException.class, () ->
                JsonRpcUtils.validateRequest(new JsonRpcRequest("test", new JsonPrimitive(true), new JsonPrimitive("req1"))));
        assertThrows(IllegalArgumentException.class, () ->
                JsonRpcUtils.validateRequest(new JsonRpcRequest("test", new JsonPrimitive("a string."), new JsonPrimitive("req1"))));

        assertThrows(IllegalArgumentException.class, () ->
                JsonRpcUtils.validateRequest(new JsonRpcRequest("test", paramArray, new JsonPrimitive(true))));
    }

    @Test
    void exceptionToJsonObject() {
        RuntimeException exception = new RuntimeException("Just a test.", new IllegalStateException());
        JsonObject jsonObject = JsonRpcUtils.exceptionToJsonObject(exception, true, true);
        assertTrue(jsonObject.has("message"));
        assertFalse(jsonObject.has("localizedMessage"));
        assertEquals(exception.getMessage(), jsonObject.get("message").getAsString());
        assertTrue(jsonObject.has("exception"));
        assertEquals(exception.getClass().getName(), jsonObject.get("exception").getAsString());
        assertTrue(jsonObject.has("stackTrace"));
        for (int i = 0; i < exception.getStackTrace().length; i++) {
            assertEquals(exception.getStackTrace()[i].toString(),
                    jsonObject.getAsJsonArray("stackTrace").get(i).getAsString());
        }
        assertTrue(jsonObject.has("cause"));
        assertEquals(IllegalStateException.class.getName(),
                jsonObject.getAsJsonObject("cause").get("exception").getAsString());
        assertEquals("",
                jsonObject.getAsJsonObject("cause").get("message").getAsString());
        assertFalse(jsonObject.getAsJsonObject("cause").has("localizedMessage"));
        for (int i = 0; i < exception.getCause().getStackTrace().length; i++) {
            assertEquals(exception.getCause().getStackTrace()[i].toString(),
                    jsonObject.getAsJsonObject("cause").getAsJsonArray("stackTrace").get(i).getAsString());
        }


        jsonObject = JsonRpcUtils.exceptionToJsonObject(exception, false, true);
        assertTrue(jsonObject.has("message"));
        assertFalse(jsonObject.has("localizedMessage"));
        assertEquals(exception.getMessage(), jsonObject.get("message").getAsString());
        assertTrue(jsonObject.has("exception"));
        assertEquals(exception.getClass().getName(), jsonObject.get("exception").getAsString());
        assertFalse(jsonObject.has("stackTrace"));
        assertTrue(jsonObject.has("cause"));
        assertEquals(IllegalStateException.class.getName(),
                jsonObject.getAsJsonObject("cause").get("exception").getAsString());
        assertEquals("",
                jsonObject.getAsJsonObject("cause").get("message").getAsString());
        assertFalse(jsonObject.getAsJsonObject("cause").has("localizedMessage"));
        assertFalse(jsonObject.getAsJsonObject("cause").has("stackTrace"));


        jsonObject = JsonRpcUtils.exceptionToJsonObject(exception, false, false);
        assertTrue(jsonObject.has("message"));
        assertFalse(jsonObject.has("localizedMessage"));
        assertEquals(exception.getMessage(), jsonObject.get("message").getAsString());
        assertTrue(jsonObject.has("exception"));
        assertEquals(exception.getClass().getName(), jsonObject.get("exception").getAsString());
        assertFalse(jsonObject.has("stackTrace"));
        assertFalse(jsonObject.has("cause"));


        jsonObject = JsonRpcUtils.exceptionToJsonObject(exception, true, false);
        assertTrue(jsonObject.has("message"));
        assertFalse(jsonObject.has("localizedMessage"));
        assertEquals(exception.getMessage(), jsonObject.get("message").getAsString());
        assertTrue(jsonObject.has("exception"));
        assertEquals(exception.getClass().getName(), jsonObject.get("exception").getAsString());
        assertTrue(jsonObject.has("stackTrace"));
        for (int i = 0; i < exception.getStackTrace().length; i++) {
            assertEquals(exception.getStackTrace()[i].toString(),
                    jsonObject.getAsJsonArray("stackTrace").get(i).getAsString());
        }
        assertFalse(jsonObject.has("cause"));


        Exception localizedException = new Exception("Just a test.") {
            @Override
            public String getLocalizedMessage() {
                return "只是个测试。";
            }
        };
        jsonObject = JsonRpcUtils.exceptionToJsonObject(localizedException, false, false);
        assertEquals(localizedException.getMessage(), jsonObject.get("message").getAsString());
        assertTrue(jsonObject.has("localizedMessage"));
        assertEquals(localizedException.getLocalizedMessage(), jsonObject.get("localizedMessage").getAsString());


        localizedException = new Exception("Just a test.") {
            @Override
            public String getLocalizedMessage() {
                return null;
            }
        };
        jsonObject = JsonRpcUtils.exceptionToJsonObject(localizedException, false, false);
        assertEquals(localizedException.getMessage(), jsonObject.get("message").getAsString());
        assertFalse(jsonObject.has("localizedMessage"));


        localizedException = new Exception("Just a test.") {
            @Override
            public String getLocalizedMessage() {
                return "";
            }
        };
        jsonObject = JsonRpcUtils.exceptionToJsonObject(localizedException, false, false);
        assertEquals(localizedException.getMessage(), jsonObject.get("message").getAsString());
        assertFalse(jsonObject.has("localizedMessage"));
    }

    @Test
    void canUseNamedParameter() throws NoSuchMethodException {
        assertTrue(JsonRpcUtils.canUseNamedParameter(RemoteInterface.class.getDeclaredMethod("getMagicNumber", int.class, int.class)));
        assertFalse(JsonRpcUtils.canUseNamedParameter(SimpleInterface.class.getDeclaredMethod("getMagicNumber", int.class, int.class)));
    }

    @Test
    void createGsonForJsonRpc() {
        Gson gson = JsonRpcUtils.createGsonForJsonRpc();

        // Serializer Request
        JsonRpcRequest request = new JsonRpcRequest("test", new JsonArray(), new JsonPrimitive("req1"));
        JsonElement reqJson = gson.toJsonTree(request);
        assertTrue(reqJson instanceof JsonObject);
        assertEquals(JSON_RPC_VERSION_VALUE, ((JsonObject) reqJson).get("jsonrpc").getAsString());
        assertEquals("test", ((JsonObject) reqJson).get(JSON_RPC_REQUEST_METHOD_FIELD).getAsString());
        assertEquals(new JsonArray(), ((JsonObject) reqJson).getAsJsonArray(JSON_RPC_REQUEST_PARAMS_FIELD));

        // Deserializer Request
        request = gson.fromJson("{\"jsonrpc\": \"2.0\", \"method\": \"foobar\", \"id\": \"1\"}", JsonRpcRequest.class);
        assertEquals("foobar", request.getMethod());
        assertEquals(new JsonPrimitive("1"), request.getId());
        assertNull(request.getParams());

        JsonRpcResponse response = new JsonRpcResponse(new JsonPrimitive(32), new JsonPrimitive("req1"));
        JsonElement responseJson = gson.toJsonTree(response);
        assertTrue(responseJson instanceof JsonObject);
        assertEquals(JSON_RPC_VERSION_VALUE, ((JsonObject) responseJson).get("jsonrpc").getAsString());
        assertEquals(32, ((JsonObject) responseJson).get("result").getAsInt());
        assertEquals("req1", ((JsonObject) responseJson).get("id").getAsString());


        // Deserializer Normal Response
        response = gson.fromJson("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}", JsonRpcResponse.class);
        assertEquals(new JsonPrimitive(19), response.getResult());
        assertEquals(new JsonPrimitive(1), response.getId());

        // Deserializer Error Response
        JsonRpcResponse errResponse = gson.fromJson("{\"jsonrpc\": \"2.0\", \"error\": {\"code\": -32600, \"message\": \"Invalid Request\"}, \"id\": null}", JsonRpcResponse.class);
        assertTrue(errResponse.getResult() instanceof JsonRpcError);
        assertEquals(-32600, ((JsonRpcError) errResponse.getResult()).getCode());
        assertEquals("Invalid Request", ((JsonRpcError) errResponse.getResult()).getMessage());
        assertEquals(JsonNull.INSTANCE, errResponse.getId());
    }
}