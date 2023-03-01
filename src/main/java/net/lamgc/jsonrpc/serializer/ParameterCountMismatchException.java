package net.lamgc.jsonrpc.serializer;

import net.lamgc.jsonrpc.JsonRpcException;

public class ParameterCountMismatchException extends JsonRpcException {
    public ParameterCountMismatchException(int expect, int actual) {
        super("Parameter count mismatch, expect " + expect + ", but actual " + actual + ".");
    }
}
