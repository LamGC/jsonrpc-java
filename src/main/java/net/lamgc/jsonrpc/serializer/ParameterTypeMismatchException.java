package net.lamgc.jsonrpc.serializer;

import net.lamgc.jsonrpc.JsonRpcException;

/**
 * 参数类型不匹配异常.
 * <p>
 * 当序列化实现尝试将 JsonRpcRequest 中的参数反序列化为方法参数时, 如果参数类型不匹配, 则抛出此异常.
 */
public class ParameterTypeMismatchException extends JsonRpcException {

    public ParameterTypeMismatchException(String methodName, String paramName, Class<?> expectType, Throwable cause) {
        super(methodName + "[" + paramName + "] " + expectType.getName(), cause);
    }

}
