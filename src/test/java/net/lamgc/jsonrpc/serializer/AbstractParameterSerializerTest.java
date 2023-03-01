package net.lamgc.jsonrpc.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lamgc.jsonrpc.JsonRpcUtils;
import org.example.not_named_parameters.SimpleInterface;
import org.example.testing.named_parameters.RemoteInterface;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractParameterSerializerTest {

    @Test
    void serializeTest() throws Exception {
        ParameterSerializer serializer = new SimpleParameterSerializer(true);
        JsonArray paramsArray = new JsonArray();
        paramsArray.add(42);
        paramsArray.add(23);

        JsonElement result = serializer.serializer(null, "test", new Object[]{42, 23});
        assertTrue(result.isJsonArray(), "JsonObject is not allowed to be returned when only the method name is provided.");
        assertEquals(paramsArray, result);

        result = serializer.serializer(
                SimpleInterface.class.getMethod("getMagicNumber", int.class, int.class),
                "getMagicNumber", new Object[]{42, 23});
        assertTrue(result.isJsonArray(), "JsonObject is not allowed to be returned when the target method does not support named parameters.");
        assertEquals(paramsArray, result);

        JsonObject paramsObject = new JsonObject();
        paramsObject.addProperty("n1", 42);
        paramsObject.addProperty("n2", 23);

        result = serializer.serializer(
                RemoteInterface.class.getMethod("getMagicNumber", int.class, int.class),
                "getMagicNumber", new Object[]{42, 23});
        assertTrue(result.isJsonObject(), "Method does not return JsonObject as expected." +
                " (named parameters are enabled, and the passed Method object has used the named parameter list)");
        assertEquals(paramsObject, result);


        serializer = new SimpleParameterSerializer(false);
        result = serializer.serializer(
                RemoteInterface.class.getMethod("getMagicNumber", int.class, int.class),
                "getMagicNumber", new Object[]{42, 23});
        assertTrue(result.isJsonArray(), "When the named parameter is disabled, JsonObject is not allowed to be returned.");
        assertEquals(paramsArray, result);
    }

    private static class SimpleParameterSerializer extends AbstractParameterSerializer {

        private final Gson gson = JsonRpcUtils.createGsonForJsonRpc();

        public SimpleParameterSerializer(boolean enableNamedParameter) {
            super(enableNamedParameter);
        }

        @Override
        protected JsonElement serializerParameter(Type expectType, Object value) {
            return gson.toJsonTree(value, expectType);
        }
    }

}