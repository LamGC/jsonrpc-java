package net.lamgc.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.example.not_named_parameters.SimpleInterface;
import org.example.testing.named_parameters.RemoteInterface;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonRpcProxyGeneratorTest {

    // 测试代理对象是否能够正常调用方法, 如果正常生成 JsonRpcRequest 对象, 则返回预设的 JsonRpcResponse 对象以确定请求成功
    @Test
    void normalCallTest() {
        Gson gson = JsonRpcUtils.createGsonForJsonRpc();
        SimpleInterface simpleImpl = JsonRpcProxyGenerator.createProxy(SimpleInterface.class, request -> {
            assertEquals("getMagicNumber", request.getMethod());
            assertNotNull(request.getParams());
            assertTrue(request.getParams() instanceof JsonArray);
            assertEquals(2, ((JsonArray) request.getParams()).size());
            assertEquals(new JsonPrimitive(12), ((JsonArray) request.getParams()).get(0));
            assertEquals(new JsonPrimitive(67), ((JsonArray) request.getParams()).get(1));
            assertNotNull(request.getId());
            return new JsonRpcResponse(new JsonPrimitive(79), request.getId());
        }, gson);
        assertEquals(79, simpleImpl.getMagicNumber(12, 67));

        RemoteInterface remoteImpl = JsonRpcProxyGenerator.createProxy(RemoteInterface.class, request -> {
            assertEquals("getMagicNumber", request.getMethod());
            assertNotNull(request.getParams());
            assertTrue(request.getParams() instanceof JsonObject);
            assertEquals(new JsonPrimitive(12), ((JsonObject) request.getParams()).get("n1"));
            assertEquals(new JsonPrimitive(67), ((JsonObject) request.getParams()).get("n2"));
            assertNotNull(request.getId());
            return new JsonRpcResponse(new JsonPrimitive(79), request.getId());
        }, gson);
        assertEquals(79, remoteImpl.getMagicNumber(12, 67));
    }

    @Test
    void callObjectMethod() {
        Gson gson = JsonRpcUtils.createGsonForJsonRpc();
        SimpleInterface impl = JsonRpcProxyGenerator.createProxy(SimpleInterface.class, request -> {
            fail("Calling object built-in methods through RPC is not allowed.");
            return null;
        }, gson);

        assertNotNull(impl.toString());
        assertNotEquals(0, impl.hashCode());
        assertNotEquals(new Object(), impl);
    }

    @Test
    void errorResponseTest() {
        // 故意在 Transporter 中返回一个 JsonRpcError 错误，来检查是否会抛出异常
        Gson gson = JsonRpcUtils.createGsonForJsonRpc();
        SimpleInterface impl = JsonRpcProxyGenerator.createProxy(SimpleInterface.class, request ->
                new JsonRpcResponse(new JsonRpcError(1, "Test Error", null), request.getId()), gson);
        assertThrows(JsonRpcRequestException.class, () -> impl.getMagicNumber(12, 34));
    }

    // 通过在 Transporter 中抛出异常, 检查代理对象是否会抛出异常.
    @Test
    void callMethodThrowException() {
        Gson gson = JsonRpcUtils.createGsonForJsonRpc();
        SimpleInterface impl = JsonRpcProxyGenerator.createProxy(SimpleInterface.class, request -> {
            throw new RuntimeException("Test Exception");
        }, gson);

        assertThrows(JsonRpcRequestException.class, () -> impl.getMagicNumber(1, 2));
    }

    @Test
    void callNoParameterMethodTest() {
        Gson gson = JsonRpcUtils.createGsonForJsonRpc();
        NoParameterInterface impl = JsonRpcProxyGenerator.createProxy(NoParameterInterface.class, request -> new JsonRpcResponse(null, request.getId()), gson);
        impl.tick();
    }

    private interface NoParameterInterface {

        void tick();

    }

}