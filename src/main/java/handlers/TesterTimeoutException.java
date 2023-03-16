package handlers;

public class TesterTimeoutException extends TesterException {
    public TesterTimeoutException(int err, String msg, Throwable cause){
        super(err,msg,cause);
    }
}
