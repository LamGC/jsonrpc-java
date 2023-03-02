package net.lamgc.jsonrpc;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JsonRpcTest {

    @Test
    public void basicExecuteTest() {
        Gson gson = JsonRpcUtils.createGsonForJsonRpc();
        JsonRpcExecutor executor = new SimpleJsonRpcExecutor(new SimpleTestImpl(), gson);
        SimpleTestInterface testInterface = JsonRpcProxyGenerator.createProxy(SimpleTestInterface.class, executor::execute, gson);
        SimpleObject test = new SimpleObject(1, "test", true);
        assertEquals("test123true" + test, testInterface.testMethod("test", 123, true, test));

        assertThrows(JsonRpcRequestException.class, testInterface::throwExceptionMethod);

        try {
            testInterface.returnJsonError();
        } catch (Exception e) {
            assertTrue(e instanceof JsonRpcRequestException);
            assertEquals(1, ((JsonRpcRequestException) e).getError().getCode());
            assertEquals("test error", ((JsonRpcRequestException) e).getError().getMessage());
        }

        try {
            testInterface.throwExceptionMethod();
        } catch (Exception e) {
            assertTrue(e instanceof JsonRpcRequestException);
            assertEquals(JsonRpcErrors.INTERNAL_ERROR.code, ((JsonRpcRequestException) e).getError().getCode());
            assertEquals(JsonRpcErrors.INTERNAL_ERROR.message, ((JsonRpcRequestException) e).getError().getMessage());
        }

    }

    private interface SimpleTestInterface {
        String testMethod(String param1, int param2, boolean param3, SimpleObject obj);

        void throwExceptionMethod();

        void returnJsonError();
    }

    @SuppressWarnings("unused")
    private static class SimpleObject {

        private int id;
        private String name;
        private boolean disabled;

        private SimpleObject(int id, String name, boolean disabled) {
            this.id = id;
            this.name = name;
            this.disabled = disabled;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        @Override
        public String toString() {
            return "SimplePOJO{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", disabled=" + disabled +
                    '}';
        }
    }

    private static class SimpleTestImpl implements SimpleTestInterface {

        @Override
        public String testMethod(String param1, int param2, boolean param3, SimpleObject obj) {
            return param1 + param2 + param3 + obj;
        }

        @Override
        public void throwExceptionMethod() {
            throw new RuntimeException("Test Exception");
        }

        @Override
        public void returnJsonError() {
            throw new JsonRpcExecuteException(new JsonRpcError(1, "test error", null));
        }

    }


}
