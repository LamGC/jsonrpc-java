package net.lamgc.jsonrpc.serializer;

import com.google.gson.JsonElement;
import net.lamgc.jsonrpc.JsonRpcProxyGenerator;

import java.lang.reflect.Method;

/**
 * 参数序列化接口.
 */
public interface ParameterSerializer {

    /**
     * 将传给方法的参数列表序列化成 {@linkplain  com.google.gson.JsonObject JsonObject} 或
     * {@linkplain com.google.gson.JsonArray JsonArray}.
     * <p>
     * 实现可自由选择是否转换成命名参数.(序列化结果为 {@linkplain com.google.gson.JsonObject JsonObject})
     *
     * @param method     与序列化有关的方法对象, 如果调用方不通过 {@link JsonRpcProxyGenerator} 生成的代理对象来调用 PRC，
     *                   则 method 参数可能为 null.
     * @param methodName 与请求相关的方法名称, 如果 method 存在, 则 methodName 等于 "{@code method.getName()}".
     * @param parameters 需要序列化的参数列表.
     * @return 返回参数列表的序列化 JSON 对象.
     * @throws Exception 当序列化出现异常时可直接抛出, 调用方将负责处理此异常.
     */
    JsonElement serializer(Method method, String methodName, Object[] parameters) throws Exception;

}
