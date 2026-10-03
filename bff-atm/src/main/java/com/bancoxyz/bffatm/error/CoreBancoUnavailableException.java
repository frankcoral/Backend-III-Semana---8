package com.bancoxyz.bffatm.error;

public class CoreBancoUnavailableException extends RuntimeException {
    public CoreBancoUnavailableException(String message) {
        super(message);
    }
}
