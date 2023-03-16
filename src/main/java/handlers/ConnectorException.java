package handlers;

public class ConnectorException extends Exception {
    public String msg;

    public ConnectorException(String exception, Throwable e){
        super(exception,e);
        this.msg = exception;
    }
}
