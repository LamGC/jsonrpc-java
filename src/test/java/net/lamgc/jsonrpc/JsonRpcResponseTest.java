package net.lamgc.jsonrpc;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonRpcResponseTest {

    @Test
    void getValue() {
        JsonPrimitive idStr = new JsonPrimitive("test.01");
        JsonPrimitive result = new JsonPrimitive("testSuccess");
        JsonRpcResponse successResponse = new JsonRpcResponse(result, idStr);
        assertEquals(idStr, successResponse.getId());
        assertEquals(result, successResponse.getResult());

        assertEquals(JsonNull.INSTANCE, new JsonRpcResponse(result, JsonNull.INSTANCE).getId());
        assertNull(new JsonRpcResponse(result, null).getId());

        assertEquals(new JsonRpcResponse(new JsonPrimitive(123), new JsonPrimitive(2)),
                new JsonRpcResponse(new JsonPrimitive(123), new JsonPrimitive(2)));
        assertEquals(new JsonRpcResponse(new JsonPrimitive(123), new JsonPrimitive(2)).hashCode(),
                new JsonRpcResponse(new JsonPrimitive(123), new JsonPrimitive(2)).hashCode());
        assertEquals(new JsonRpcResponse(new JsonPrimitive(123), new JsonPrimitive(2)).toString(),
                new JsonRpcResponse(new JsonPrimitive(123), new JsonPrimitive(2)).toString());

        assertNotEquals(new JsonRpcResponse(new JsonPrimitive("Test"), new JsonPrimitive(2)),
                new JsonRpcResponse(new JsonPrimitive(123), new JsonPrimitive(2)));
        assertNotEquals(new JsonRpcResponse(new JsonPrimitive("Test"), new JsonPrimitive(2)).hashCode(),
                new JsonRpcResponse(new JsonPrimitive(123), new JsonPrimitive(2)).hashCode());
        assertNotEquals(new JsonRpcResponse(new JsonPrimitive("Test"), new JsonPrimitive(2)).toString(),
                new JsonRpcResponse(new JsonPrimitive(123), new JsonPrimitive(2)).toString());
    }

    @Test
    void constructionTest() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("field1", true);
        jsonObject.addProperty("field2", 123);
        jsonObject.addProperty("field3", "test");
        JsonRpcResponse response = new JsonRpcResponse(jsonObject, new JsonPrimitive("test"));
        assertFalse(response.isError());
        assertEquals(new JsonPrimitive("test"), response.getId());
        assertEquals(jsonObject, response.getResult());


        assertThrows(IllegalArgumentException.class, () ->
                        new JsonRpcResponse(new Object(), new JsonPrimitive(1)),
                "The field result type is incorrect, but the object was constructed successfully");
        assertThrows(IllegalArgumentException.class, () ->
                        new JsonRpcResponse(jsonObject, new JsonObject()),
                "The field id type is incorrect, but the object was constructed successfully");
    }

}