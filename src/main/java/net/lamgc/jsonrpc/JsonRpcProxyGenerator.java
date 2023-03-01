package net.lamgc.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import net.lamgc.jsonrpc.serializer.GsonParameterSerializer;
import net.lamgc.jsonrpc.serializer.GsonReturnValueDeserializer;
import net.lamgc.jsonrpc.serializer.ParameterSerializer;
import net.lamgc.jsonrpc.serializer.ReturnValueDeserializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ThreadLocalRandom;

/**
 * JsonRpc 代理对象生成器.
 * <p>
 * 通过使用代理对象, 可以简化使用 JsonRpc 的复杂度.
 */
public final class JsonRpcProxyGenerator {

    /**
     * 创建一个 JsonRpc 代理对象.
     *
     * @param interfaceClass     代理对象的接口类型.
     * @param requestTransporter Json RPC 请求传输器.
     * @param gson               用于序列化参数和反序列化返回值的 Gson 对象.
     * @param <T>                代理对象的类型.
     * @return 返回一个代理对象.
     */
    public static <T> T createProxy(Class<T> interfaceClass, JsonRpcRequestTransporter requestTransporter, Gson gson) {
        return createProxy(interfaceClass, requestTransporter, gson, true);
    }

    /**
     * 创建一个 JsonRpc 代理对象.
     *
     * @param interfaceClass       代理对象的接口类型.
     * @param requestTransporter   Json RPC 请求传输器.
     * @param gson                 用于序列化参数和反序列化返回值的 Gson 对象.
     * @param enableNamedParameter 是否启用命名参数.
     * @param <T>                  代理对象的类型.
     * @return 返回一个代理对象.
     */
    public static <T> T createProxy(
            Class<T> interfaceClass,
            JsonRpcRequestTransporter requestTransporter,
            Gson gson,
            boolean enableNamedParameter
    ) {
        return createProxy(interfaceClass, requestTransporter,
                new GsonParameterSerializer(gson, enableNamedParameter), new GsonReturnValueDeserializer(gson));
    }

    /**
     * 创建一个 JsonRpc 代理对象.
     *
     * @param interfaceClass          代理对象的接口类型.
     * @param requestTransporter      Json RPC 请求传输器.
     * @param parameterSerializer     参数序列化器.
     * @param returnValueDeserializer 返回值反序列化器.
     * @param <T>                     代理对象的类型.
     * @return 返回一个代理对象.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(
            Class<T> interfaceClass,
            JsonRpcRequestTransporter requestTransporter,
            ParameterSerializer parameterSerializer,
            ReturnValueDeserializer returnValueDeserializer
    ) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                new JsonRpcProxyInvocationHandler(requestTransporter, parameterSerializer, returnValueDeserializer)
        );
    }

    private static class JsonRpcProxyInvocationHandler implements InvocationHandler {

        private final JsonRpcRequestTransporter transporter;
        private final JsonRpcRequestBuilder requestBuilder;
        private final ReturnValueDeserializer valueDeserializer;

        public JsonRpcProxyInvocationHandler(
                JsonRpcRequestTransporter transporter,
                ParameterSerializer parameterSerializer,
                ReturnValueDeserializer valueDeserializer
        ) {
            this.transporter = transporter;
            this.requestBuilder = new JsonRpcRequestBuilder(parameterSerializer);
            this.valueDeserializer = valueDeserializer;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            JsonPrimitive requestId = new JsonPrimitive(ThreadLocalRandom.current().nextLong());
            JsonRpcRequest request = requestBuilder.buildRequest(method, requestId, args);

            JsonRpcResponse response;
            try {
                response = transporter.transportRequest(request);
            } catch (Exception e) {
                throw new JsonRpcRequestException(JsonRpcErrors.REQUEST_FAILURE.toRpcError(), e);
            }
            if (!response.isError()) {
                return valueDeserializer.deserializer(method, response);
            } else {
                throw new JsonRpcRequestException((JsonRpcError) response.getResult());
            }
        }
    }

}
