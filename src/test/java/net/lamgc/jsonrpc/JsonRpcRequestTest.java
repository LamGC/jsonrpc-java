package net.lamgc.jsonrpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JsonRpcRequestTest {

    @Test
    void testEquals() {
        assertEquals(new JsonRpcRequest("test", null, null), new JsonRpcRequest("test", null, null));
        assertEquals(new JsonRpcRequest("test", new JsonObject(), null), new JsonRpcRequest("test", new JsonObject(), null));
        assertEquals(new JsonRpcRequest("test", new JsonArray(), null), new JsonRpcRequest("test", new JsonArray(), null));
        assertEquals(new JsonRpcRequest("test", new JsonArray(), new JsonPrimitive(1)), new JsonRpcRequest("test", new JsonArray(), new JsonPrimitive(1)));

        assertNotEquals(new JsonRpcRequest("test", null, null), new JsonRpcRequest("print", null, null));
        assertNotEquals(new JsonRpcRequest("test", new JsonArray(), null), new JsonRpcRequest("test", new JsonObject(), null));
        assertNotEquals(new JsonRpcRequest("test", new JsonArray(), new JsonPrimitive(1)), new JsonRpcRequest("test", new JsonArray(), new JsonPrimitive(2)));
    }

    @Test
    void testHashCode() {
        assertEquals(new JsonRpcRequest("test", null, null).hashCode(), new JsonRpcRequest("test", null, null).hashCode());
        assertEquals(new JsonRpcRequest("test", new JsonObject(), null).hashCode(), new JsonRpcRequest("test", new JsonObject(), null).hashCode());
        assertEquals(new JsonRpcRequest("test", new JsonArray(), null).hashCode(), new JsonRpcRequest("test", new JsonArray(), null).hashCode());
        assertEquals(new JsonRpcRequest("test", new JsonArray(), new JsonPrimitive(1)).hashCode(), new JsonRpcRequest("test", new JsonArray(), new JsonPrimitive(1)).hashCode());

        assertNotEquals(new JsonRpcRequest("test", null, null).hashCode(), new JsonRpcRequest("print", null, null).hashCode());
        assertNotEquals(new JsonRpcRequest("test", new JsonArray(), null).hashCode(), new JsonRpcRequest("test", new JsonObject(), null).hashCode());
        assertNotEquals(new JsonRpcRequest("test", new JsonArray(), new JsonPrimitive(1)).hashCode(), new JsonRpcRequest("test", new JsonArray(), new JsonPrimitive(2)).hashCode());
    }

    @Test
    void testToString() {
        assertEquals(new JsonRpcRequest("test", null, null).toString(), new JsonRpcRequest("test", null, null).toString());
        assertNotEquals(new JsonRpcRequest("test", null, null).toString(), new JsonRpcRequest("print", null, null).toString());
    }
}