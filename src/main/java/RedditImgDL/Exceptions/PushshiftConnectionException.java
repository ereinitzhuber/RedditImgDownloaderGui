package RedditImgDL.Exceptions;


public class PushshiftConnectionException extends Exception {
    String message;
    public PushshiftConnectionException() {
    }
    public PushshiftConnectionException(String message) {
        super(message);
    }
    public PushshiftConnectionException(Throwable cause) {
        super(cause);
    }
    public PushshiftConnectionException(String message, Throwable throwable) {
        super(message, throwable);
    }
    @Override
    public String toString() {
        //TODO ?
        return "PushshiftConnectionException: Pushshift connection failed." + message;
    }
}