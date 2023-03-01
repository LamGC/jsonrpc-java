package net.lamgc.jsonrpc.serializer;

import net.lamgc.jsonrpc.JsonRpcErrors;
import net.lamgc.jsonrpc.JsonRpcRequestException;
import net.lamgc.jsonrpc.JsonRpcUtils;

public class ParameterSerializationException extends JsonRpcRequestException {
    public ParameterSerializationException(Throwable cause) {
        super(JsonRpcErrors.CONVERT_PARAMS_FAILURE.toRpcError(
                        JsonRpcUtils.exceptionToJsonObject(cause, false, true)),
                cause);
    }
}
