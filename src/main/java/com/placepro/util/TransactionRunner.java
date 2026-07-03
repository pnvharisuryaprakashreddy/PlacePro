package com.placepro.util;

public interface TransactionRunner {

    <T> T execute(TransactionCallback<T> callback);
}
