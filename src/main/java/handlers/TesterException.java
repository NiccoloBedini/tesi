package handlers;

public class TesterException extends Throwable {

    public int error;
    public String msg;
    public Throwable errno;

    public TesterException(int err, String msg, Throwable causa){
        super(msg,causa);
        this.error = err;
        this.msg = msg;
        this.errno = causa;
    }
}
