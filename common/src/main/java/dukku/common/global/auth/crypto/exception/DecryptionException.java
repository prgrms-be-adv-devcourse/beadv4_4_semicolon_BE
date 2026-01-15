package dukku.common.global.auth.crypto.exception;

public class DecryptionException extends RuntimeException {
    public DecryptionException(Throwable cause) { super("복호화 실패", cause); }
}