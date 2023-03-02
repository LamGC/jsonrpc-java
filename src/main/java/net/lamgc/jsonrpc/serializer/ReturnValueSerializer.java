package net.lamgc.jsonrpc.serializer;

import com.google.gson.JsonElement;

import java.lang.reflect.Method;

/**
 * 对方法返回值进行序列化.
 * <p>
 * 主要用于支持各类序列化库, 或者用于对返回值进行特殊处理.
 * <p>
 * 如果需要手动实现该接口, 请务必注意在 RPC 服务端侧也需要使用对应的 {@link ReturnValueDeserializer} 实现.
 */
public interface ReturnValueSerializer {

    /**
     * 序列化方法返回值.
     *
     * @param method      对应的方法对象.
     * @param returnValue 方法所返回的值, 返回值有可能是 null.
     * @return 返回序列化后的方法对象.
     * @throws Exception 当无法完成序列化时可抛出异常.
     */
    JsonElement serializer(Method method, Object returnValue) throws Exception;

}
