package dukku.common.global.auth.crypto.exception;

public class EncryptionException extends RuntimeException {
    public EncryptionException(Throwable cause) { super("암호화 실패", cause); }
}