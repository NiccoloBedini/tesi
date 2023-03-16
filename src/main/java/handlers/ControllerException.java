package handlers;

public class ControllerException extends Exception {

    public String msg;

    public ControllerException(String exception){
        super(exception);
        msg = exception;
    }
}
