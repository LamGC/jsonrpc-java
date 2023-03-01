package net.lamgc.jsonrpc.serializer;

import net.lamgc.jsonrpc.JsonRpcResponse;

import java.lang.reflect.Method;

/**
 * Rpc 响应返回值转换器.
 * <p>
 * 主要用于支持各类序列化库, 或者用于对返回值进行特殊处理.
 * <p>
 * 如果需要手动实现该接口, 请务必注意在 RPC 服务端侧也需要使用对应的 {@link ReturnValueSerializer} 实现.
 */
public interface ReturnValueDeserializer {

    /**
     * 将 Rpc Response 中的 ReturnValue 转换为对应的返回类型对象.
     *
     * @param method   Json RPC 所调用的方法.
     * @param response Json RPC 响应.
     * @return 返回与被调用方法返回类型相同的对象.
     * @throws Exception 当转换发生错误时可抛出异常.
     */
    Object deserializer(Method method, JsonRpcResponse response) throws Exception;

}
