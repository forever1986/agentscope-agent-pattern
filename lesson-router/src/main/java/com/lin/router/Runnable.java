package com.lin.router;

public interface Runnable<I, O> {
    O invoke(I input);
}
