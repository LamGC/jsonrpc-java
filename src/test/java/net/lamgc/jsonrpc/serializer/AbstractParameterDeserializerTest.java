package net.lamgc.jsonrpc.serializer;

import com.google.gson.*;
import net.lamgc.jsonrpc.JsonRpcRequest;
import net.lamgc.jsonrpc.JsonRpcUtils;
import org.example.not_named_parameters.SimpleInterface;
import org.example.testing.named_parameters.RemoteInterface;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbstractParameterDeserializerTest {

    @Test
    void deserializer() throws Exception {
        ParameterDeserializer deserializer = new SimpleParameterDeserializer();
        Object[] expectParams = new Object[]{42, 23};
        JsonArray paramsArray = new JsonArray();
        paramsArray.add(42);
        paramsArray.add(23);
        JsonObject paramsObject = new JsonObject();
        paramsObject.addProperty("n1", 42);
        paramsObject.addProperty("n2", 23);

        Object[] params = deserializer.deserializer(
                SimpleInterface.class.getDeclaredMethod("getMagicNumber", int.class, int.class),
                new JsonRpcRequest("getMagicNumber", paramsArray, new JsonPrimitive(1))
        );
        assertArrayEquals(expectParams, params);

        params = deserializer.deserializer(
                RemoteInterface.class.getDeclaredMethod("getMagicNumber", int.class, int.class),
                new JsonRpcRequest("getMagicNumber", paramsArray, new JsonPrimitive(1))
        );
        assertArrayEquals(expectParams, params);

        params = deserializer.deserializer(
                RemoteInterface.class.getDeclaredMethod("getMagicNumber", int.class, int.class),
                new JsonRpcRequest("getMagicNumber", paramsObject, new JsonPrimitive(1))
        );
        assertArrayEquals(expectParams, params);

        assertThrows(IllegalArgumentException.class, () -> deserializer.deserializer(
                SimpleInterface.class.getDeclaredMethod("getMagicNumber", int.class, int.class),
                new JsonRpcRequest("getMagicNumber", paramsObject, new JsonPrimitive(1))
        ));

        JsonArray badParamsArray = new JsonArray();
        badParamsArray.add(42);
        badParamsArray.add(23);
        badParamsArray.add(12);
        JsonObject badParamsObject = new JsonObject();
        badParamsObject.addProperty("n1", 42);
        badParamsObject.addProperty("n2", 23);
        badParamsObject.addProperty("n3", 12);

        assertThrows(ParameterCountMismatchException.class, () -> deserializer.deserializer(
                SimpleInterface.class.getDeclaredMethod("getMagicNumber", int.class, int.class),
                new JsonRpcRequest("getMagicNumber", badParamsArray, new JsonPrimitive(1))
        ));

        assertThrows(ParameterCountMismatchException.class, () -> deserializer.deserializer(
                RemoteInterface.class.getDeclaredMethod("getMagicNumber", int.class, int.class),
                new JsonRpcRequest("getMagicNumber", badParamsObject, new JsonPrimitive(1))
        ));

    }

    private static class SimpleParameterDeserializer extends AbstractParameterDeserializer {

        private final Gson gson = JsonRpcUtils.createGsonForJsonRpc();

        @Override
        protected Object toParameterObject(Method method, Parameter parameter, JsonElement paramJson) {
            return gson.fromJson(paramJson, parameter.getType());
        }
    }

}