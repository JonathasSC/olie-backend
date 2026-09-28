package com.olie.api.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Erro de negócio com corpo JSON ({@code code}, {@code message}, {@code details}) — usado quando o frontend
 * precisa distinguir o motivo, o que o status HTTP sozinho não permite.
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final transient Object details;

    public ApiException(HttpStatus status, String code, String message) {
        this(status, code, message, null);
    }

    public ApiException(HttpStatus status, String code, String message, Object details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }
}
