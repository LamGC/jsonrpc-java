package net.lamgc.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.lamgc.jsonrpc.serializer.GsonParameterSerializer;
import net.lamgc.jsonrpc.serializer.ParameterSerializationException;
import org.example.not_named_parameters.SimpleInterface;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonRpcRequestBuilderTest {

    @Test
    void buildRequestTest() throws NoSuchMethodException {
        Gson gsonForJsonRpc = JsonRpcUtils.createGsonForJsonRpc();
        JsonRpcRequestBuilder requestBuilder = new JsonRpcRequestBuilder(new GsonParameterSerializer(gsonForJsonRpc));
        JsonRpcRequest subtractCallRequest = requestBuilder.buildRequest("subtract", new JsonPrimitive(1), 42, 23);
        JsonArray paramsArray = new JsonArray();
        paramsArray.add(42);
        paramsArray.add(23);

        assertEquals("subtract", subtractCallRequest.getMethod());
        assertEquals(new JsonPrimitive(1), subtractCallRequest.getId());
        assertEquals(paramsArray, subtractCallRequest.getParams());

        subtractCallRequest = requestBuilder.buildRequest("subtract2", new JsonPrimitive(5), 42, 23);

        assertEquals("subtract2", subtractCallRequest.getMethod());
        assertEquals(new JsonPrimitive(5), subtractCallRequest.getId());
        assertEquals(paramsArray, subtractCallRequest.getParams());


        subtractCallRequest = requestBuilder.buildRequest(SimpleInterface.class.getMethod("getMagicNumber", int.class, int.class),
                new JsonPrimitive(5), 42, 23);
        assertEquals("getMagicNumber", subtractCallRequest.getMethod());
        assertEquals(new JsonPrimitive(5), subtractCallRequest.getId());
        assertEquals(paramsArray, subtractCallRequest.getParams());

        requestBuilder = new JsonRpcRequestBuilder((method, methodName, parameters) -> {
            throw new RuntimeException("expect exception.");
        });

        JsonRpcRequestBuilder finalRequestBuilder = requestBuilder;
        assertThrows(ParameterSerializationException.class, () ->
                finalRequestBuilder.buildRequest(SimpleInterface.class.getMethod("getMagicNumber", int.class, int.class),
                        new JsonPrimitive(5), 42, 23));

        assertThrows(ParameterSerializationException.class, () ->
                finalRequestBuilder.buildRequest("subtract", new JsonPrimitive(1), 42, 23));
    }

}