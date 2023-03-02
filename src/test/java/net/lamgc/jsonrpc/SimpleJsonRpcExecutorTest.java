package net.lamgc.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import org.example.not_named_parameters.SimpleInterfaceImpl;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimpleJsonRpcExecutorTest {

    @Test
    void methodNotFoundTest() {
        Gson gson = JsonRpcUtils.createGsonForJsonRpc();
        SimpleJsonRpcExecutor executor = new SimpleJsonRpcExecutor(new SimpleInterfaceImpl(), gson);

        JsonRpcRequest methodNotFoundRequest = new JsonRpcRequest("notFoundMethod", new JsonArray(), new JsonPrimitive(1));
        assertThrows(NoSuchElementException.class, () -> executor.findMethod(methodNotFoundRequest));

        assertNotNull(executor.findMethod(new JsonRpcRequest("getMagicNumber", new JsonArray(), new JsonPrimitive(1))));
    }

    @Test
    void methodAccessFilterTest() {
        Gson gson = JsonRpcUtils.createGsonForJsonRpc();
        SimpleJsonRpcExecutor executor = new SimpleJsonRpcExecutor(new TestHandler(), gson);

        assertThrows(NoSuchElementException.class, () ->
                executor.findMethod(new JsonRpcRequest("privateMethod", new JsonArray(), new JsonPrimitive(1))));
        assertThrows(NoSuchElementException.class, () ->
                executor.findMethod(new JsonRpcRequest("staticMethod", new JsonArray(), new JsonPrimitive(1))));

        assertNotNull(executor.findMethod(new JsonRpcRequest("publicMethod", new JsonArray(), new JsonPrimitive(1))));
    }

    @Test
    void overloadHandlerTest() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleJsonRpcExecutor(new OverloadHandler(), new Gson()));
    }

    @SuppressWarnings("unused")
    private static class TestHandler {

        private static void staticMethod() {

        }

        public void publicMethod() {

        }

        private void privateMethod() {

        }

    }

    private static class OverloadHandler {

        public int plus(int n1, int n2) {
            return n1 + n2;
        }

        public int plus(int n1, int n2, int n3) {
            return n1 + n2 + n3;
        }

    }


}