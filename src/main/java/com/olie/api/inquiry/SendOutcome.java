package com.olie.api.inquiry;

public record SendOutcome(Result result, int attempts, String failureReason) {

    public enum Result {
        SENT,
        FAILED,
        /** A sessão caiu no meio: o contato volta para a fila e a consulta é pausada. */
        CONNECTION_LOST
    }

    public static SendOutcome sent(int attempts) {
        return new SendOutcome(Result.SENT, attempts, null);
    }

    public static SendOutcome failed(int attempts, String reason) {
        return new SendOutcome(Result.FAILED, attempts, reason);
    }

    public static SendOutcome connectionLost(int attempts) {
        return new SendOutcome(Result.CONNECTION_LOST, attempts, null);
    }
}
