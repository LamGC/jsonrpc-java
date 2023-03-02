package net.lamgc.jsonrpc;

import com.google.gson.*;
import net.lamgc.jsonrpc.serializer.GsonParameterDeserializer;
import net.lamgc.jsonrpc.serializer.GsonReturnValueSerializer;
import net.lamgc.jsonrpc.serializer.ParameterDeserializer;
import net.lamgc.jsonrpc.serializer.ReturnValueSerializer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonRpcExecutorTest {

    @Test
    void executeTest() {
        Gson gson = JsonRpcUtils.createGsonForJsonRpc();
        TestingMapExecutor normalExecutor = new TestingMapExecutor(gson);
        assertNotNull(normalExecutor.getHandler());

        JsonArray plusParams = new JsonArray();
        plusParams.add(42);
        plusParams.add(23);
        JsonRpcRequest request = new JsonRpcRequest("plus", plusParams, new JsonPrimitive("req1"));
        JsonRpcResponse response = normalExecutor.execute(request);
        assertTrue(response.getResult() instanceof JsonElement);
        assertEquals(65, ((JsonElement) response.getResult()).getAsInt());


        JsonArray concatParams = new JsonArray();
        concatParams.add("Test");
        concatParams.add(123);
        request = new JsonRpcRequest("concat", concatParams, new JsonPrimitive("req1"));
        response = normalExecutor.execute(request);
        assertTrue(response.getResult() instanceof JsonElement);
        assertEquals("Test123", ((JsonElement) response.getResult()).getAsString());


        JsonArray doNothingParams = new JsonArray();
        doNothingParams.add("a string");
        request = new JsonRpcRequest("doNothing", doNothingParams, new JsonPrimitive("req2"));
        response = normalExecutor.execute(request);
        assertTrue(response.getResult() instanceof JsonElement);
        assertEquals(JsonNull.INSTANCE, response.getResult());


        request = new JsonRpcRequest("realDoNothing", new JsonArray(), new JsonPrimitive("req2"));
        response = normalExecutor.execute(request);
        assertTrue(response.getResult() instanceof JsonElement);
        assertEquals(JsonNull.INSTANCE, response.getResult());


        request = new JsonRpcRequest("throwJsonRpcError", new JsonArray(), new JsonPrimitive("req2"));
        response = normalExecutor.execute(request);
        assertTrue(response.getResult() instanceof JsonRpcError);
        assertEquals(1, ((JsonRpcError) response.getResult()).getCode());
        assertEquals("Test error.", ((JsonRpcError) response.getResult()).getMessage());
        assertNull(((JsonRpcError) response.getResult()).getData());


        request = new JsonRpcRequest("", new JsonArray(), new JsonPrimitive("req2"));
        response = normalExecutor.execute(request);
        assertTrue(response.getResult() instanceof JsonRpcError);
        assertTrue(response.isError());
        assertEquals(JsonRpcErrors.INVALID_REQUEST.code, ((JsonRpcError) response.getResult()).getCode());
        assertEquals(JsonRpcErrors.INVALID_REQUEST.message, ((JsonRpcError) response.getResult()).getMessage());


        // 准确来讲这个应该算 Method not found, 因为 JsonRpcExecutor 的最终实现没有排除无法调用的方法.
        // 但是这里为了测试, 所以直接不检查, 然后按 Internal Error 处理.
        request = new JsonRpcRequest("privateMethod", new JsonArray(), new JsonPrimitive("req2"));
        response = normalExecutor.execute(request);
        assertTrue(response.getResult() instanceof JsonRpcError);
        assertTrue(response.isError());
        assertEquals(JsonRpcErrors.INTERNAL_ERROR.code, ((JsonRpcError) response.getResult()).getCode());
        assertEquals(JsonRpcErrors.INTERNAL_ERROR.message, ((JsonRpcError) response.getResult()).getMessage());


        JsonRpcResponse errResp = new TestingMapExecutor(gson) {
            @Override
            protected Method findMethod(JsonRpcRequest request) {
                return null;
            }
        }.execute(new JsonRpcRequest("realDoNothing", new JsonArray(), new JsonPrimitive("req2")));
        assertTrue(errResp.isError());
        assertEquals(new JsonPrimitive("req2"), errResp.getId());
        assertTrue(errResp.getResult() instanceof JsonRpcError);
        assertEquals(JsonRpcErrors.METHOD_NOT_FOUND.code, ((JsonRpcError) errResp.getResult()).getCode());
        assertEquals(JsonRpcErrors.METHOD_NOT_FOUND.message, ((JsonRpcError) errResp.getResult()).getMessage());


        errResp = new TestingMapExecutor(gson) {
            @Override
            protected Method findMethod(JsonRpcRequest request) {
                throw new RuntimeException();
            }
        }.execute(new JsonRpcRequest("realDoNothing", new JsonArray(), new JsonPrimitive("req2")));
        assertTrue(errResp.isError());
        assertEquals(new JsonPrimitive("req2"), errResp.getId());
        assertTrue(errResp.getResult() instanceof JsonRpcError);
        assertEquals(JsonRpcErrors.INTERNAL_ERROR.code, ((JsonRpcError) errResp.getResult()).getCode());
        assertEquals(JsonRpcErrors.INTERNAL_ERROR.message, ((JsonRpcError) errResp.getResult()).getMessage());


        errResp = new TestingMapExecutor((method, request1) -> {
            throw new RuntimeException();
        }, new GsonReturnValueSerializer(gson))
                .execute(new JsonRpcRequest("plus", plusParams, new JsonPrimitive("req2")));
        assertTrue(errResp.isError());
        assertEquals(new JsonPrimitive("req2"), errResp.getId());
        assertTrue(errResp.getResult() instanceof JsonRpcError);
        assertEquals(JsonRpcErrors.CONVERT_PARAMS_FAILURE.code, ((JsonRpcError) errResp.getResult()).getCode());
        assertEquals(JsonRpcErrors.CONVERT_PARAMS_FAILURE.message, ((JsonRpcError) errResp.getResult()).getMessage());


        errResp = new TestingMapExecutor((method, request1) -> new Object[3], new GsonReturnValueSerializer(gson))
                .execute(new JsonRpcRequest("plus", plusParams, new JsonPrimitive("req2")));
        assertTrue(errResp.isError());
        assertEquals(new JsonPrimitive("req2"), errResp.getId());
        assertTrue(errResp.getResult() instanceof JsonRpcError);
        assertEquals(JsonRpcErrors.INVALID_PARAMS.code, ((JsonRpcError) errResp.getResult()).getCode());
        assertEquals(JsonRpcErrors.INVALID_PARAMS.message, ((JsonRpcError) errResp.getResult()).getMessage());


        errResp = normalExecutor.execute(new JsonRpcRequest("realDoNothing", plusParams, new JsonPrimitive("req2")));
        assertTrue(errResp.isError());
        assertEquals(new JsonPrimitive("req2"), errResp.getId());
        assertTrue(errResp.getResult() instanceof JsonRpcError);
        assertEquals(JsonRpcErrors.INVALID_PARAMS.code, ((JsonRpcError) errResp.getResult()).getCode());
        assertEquals(JsonRpcErrors.INVALID_PARAMS.message, ((JsonRpcError) errResp.getResult()).getMessage());


        JsonObject multiParams = new JsonObject();
        multiParams.addProperty("arg1", "a");
        multiParams.addProperty("arg2", 1);
        errResp = normalExecutor.execute(new JsonRpcRequest("realDoNothing", multiParams, new JsonPrimitive("req2")));
        assertTrue(errResp.isError());
        assertEquals(new JsonPrimitive("req2"), errResp.getId());
        assertTrue(errResp.getResult() instanceof JsonRpcError);
        assertEquals(JsonRpcErrors.INVALID_PARAMS.code, ((JsonRpcError) errResp.getResult()).getCode());
        assertEquals(JsonRpcErrors.INVALID_PARAMS.message, ((JsonRpcError) errResp.getResult()).getMessage());


        errResp = normalExecutor.execute(new JsonRpcRequest("callToFail", new JsonArray(), new JsonPrimitive("req2")));
        assertTrue(errResp.isError());
        assertEquals(new JsonPrimitive("req2"), errResp.getId());
        assertTrue(errResp.getResult() instanceof JsonRpcError);
        assertEquals(JsonRpcErrors.INTERNAL_ERROR.code, ((JsonRpcError) errResp.getResult()).getCode());
        assertEquals(JsonRpcErrors.INTERNAL_ERROR.message, ((JsonRpcError) errResp.getResult()).getMessage());


        errResp = new TestingMapExecutor(new GsonParameterDeserializer(gson), (method, returnValue) -> {
            throw new RuntimeException();
        }).execute(new JsonRpcRequest("realDoNothing", new JsonArray(), new JsonPrimitive("req2")));
        assertTrue(errResp.isError());
        assertEquals(new JsonPrimitive("req2"), errResp.getId());
        assertTrue(errResp.getResult() instanceof JsonRpcError);
        assertEquals(JsonRpcErrors.CONVERT_RETURN_VALUE_FAILURE.code, ((JsonRpcError) errResp.getResult()).getCode());
        assertEquals(JsonRpcErrors.CONVERT_RETURN_VALUE_FAILURE.message, ((JsonRpcError) errResp.getResult()).getMessage());
    }

    private static class TestingMapExecutor extends JsonRpcExecutor {

        private final Map<String, Method> methodMap;

        public TestingMapExecutor(Gson gson) {
            super(new TestingSimpleHandler(), new GsonParameterDeserializer(gson), new GsonReturnValueSerializer(gson));
            methodMap = new HashMap<>();
            for (Method method : TestingSimpleHandler.class.getDeclaredMethods()) {
                methodMap.put(method.getName(), method);
            }
        }

        public TestingMapExecutor(ParameterDeserializer parameterDeserializer, ReturnValueSerializer returnValueSerializer) {
            super(new TestingSimpleHandler(), parameterDeserializer, returnValueSerializer);
            methodMap = new HashMap<>();
            for (Method method : TestingSimpleHandler.class.getDeclaredMethods()) {
                methodMap.put(method.getName(), method);
            }
        }

        @Override
        protected Method findMethod(JsonRpcRequest request) {
            return methodMap.get(request.getMethod());
        }
    }

    @SuppressWarnings("unused")
    private static class TestingSimpleHandler {

        public int plus(int n1, int n2) {
            return n1 + n2;
        }

        public String concat(String prefix, int number) {
            return prefix + number;
        }

        public void doNothing(String ignored) {
            // Do nothing.
        }

        public void realDoNothing() {
        }

        public void callToFail() {
            throw new RuntimeException();
        }

        private void privateMethod() {
        }

        public void throwJsonRpcError() {
            throw new JsonRpcExecuteException(new JsonRpcError(1, "Test error.", null));
        }

    }


}