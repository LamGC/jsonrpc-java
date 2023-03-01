package net.lamgc.jsonrpc.serializer;

import net.lamgc.jsonrpc.JsonRpcRequest;

import java.lang.reflect.Method;

/**
 * 参数反序列化器.
 * <p>
 * 本接口主要为 {@link net.lamgc.jsonrpc.JsonRpcExecutor} 提供统一的参数反序列化接口.
 * 如果需要手动处理 Json RPC 请求, 则本接口及实现可能不适用该使用场景.
 * <p>
 * Tips: 某种意义上来讲, 如果手动实现该接口, 则可以实现一些有趣的参数形式,
 * 例如利用 params 传递一个 JsonObject 对象, 看似使用了命名参数列表, 但实际上反序列化后传递了一个对象参数.
 */
public interface ParameterDeserializer {

    /**
     * 将 JsonRpcRequest 中的参数反序列化为方法参数列表.
     *
     * @param method  与反序列化有关的方法对象.
     * @param request Json RPC 请求对象.
     * @return 返回可用于 method 的参数列表.
     * @throws Exception 当反序列化出现异常时可直接抛出, 调用方将负责处理此异常.
     */
    Object[] deserializer(Method method, JsonRpcRequest request) throws Exception;

}
