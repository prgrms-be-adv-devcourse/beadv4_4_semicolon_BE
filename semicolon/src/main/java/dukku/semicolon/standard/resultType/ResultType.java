package dukku.semicolon.standard.resultType;


public interface ResultType {
    String resultCode();
    String msg();

    default <T> T data() {
        return null;
    }
}