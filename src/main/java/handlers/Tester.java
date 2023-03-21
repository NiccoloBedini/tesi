package handlers;
import static handlers.Parametri.*;

import test.TCPTest;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;



public class Tester {

    private int port;
    private Socket __test_socket;

    public Tester(int port) throws TesterException{
        this.port = port;

        try{
            __test_socket = new Socket();
            __test_socket.setReuseAddress(true);

        }
        catch(SocketException se){
            throw new TesterException(TESTER_INIT_CLIENT_ERROR,"Unable to create socket for test",se.getCause());
        }

        // dovrebbe esserci codice relativo all' ICMP socket
    }
     

    public void connect(String address) throws TesterException, TesterTimeoutException{
        try{
            __test_socket.connect(new InetSocketAddress(address,port));
        }

        catch(SocketTimeoutException ste){
            throw new TesterTimeoutException(TESTER_CONNECT_TIMEOUT_ERROR,"Connection timeout for port " + port, ste.getCause());
        }
        catch (IOException ie) {
            if (ie.getMessage().contains("Connection refused") || ie.getMessage().contains("Connection reset") || ie.getMessage().contains("Connection aborted")) {
                throw new TesterException(TESTER_CONNECT_REFUSED_ERROR, "Connection refused on port " + port, ie.getCause());
            } 
            else {
                throw new TesterException(TESTER_CONNECT_GENERIC_ERROR,"Unable to connect to server on port " + port, ie.getCause());
            }
        }

    }

    //overloading
    public void do_test(TCPTest test, int phase, int test_type, HashMap<Double, Integer> result) throws TesterTimeoutException, TesterException{
        do_test(test,phase,test_type,result,0);
    }

    public void do_test(TCPTest test, int phase, int test_type, HashMap<Double, Integer> result, int duration ) throws TesterTimeoutException,TesterException{
        try{
            if(phase == TEST_UPLINK_PHASE){
                if(test_type == TEST_SPEEDTEST_TYPE){
                    logger.info("<INIZIO TEST UPLINK_SPEED>");
                    if(duration == 0)
                        test.uplink_test(this.__test_socket);
                    else
                        test.uplink_test(this.__test_socket, duration);
                }
                else if(test_type == TEST_TRACEROUTE_TYPE){
                    //test.uplink_traceroute();
                    logger.severe("not implemented test");
                }
            }

            else if(phase == TEST_DOWNLINK_PHASE){
                if(test_type == TEST_SPEEDTEST_TYPE){
                    logger.info("<INIZIO TEST DOWNLINK_SPEED>");
                    test.downlink_test(this.__test_socket,result);
                }
                else if(test_type == TEST_TRACEROUTE_TYPE){
                    //test.downlink_traceroute();
                    logger.severe("not implemented test");
                }
            }
        }
        catch (SocketTimeoutException ste){
            throw new TesterTimeoutException(TESTER_TEST_TIMEOUT_ERROR,"Connection timeout when receiving on port "+port , ste.getCause());
        }
        catch (IOException se){
            throw new TesterException(TESTER_TEST_RESET_ERROR, "Test failed due to connection reset or abort",se.getCause());
        }
    }

    public void close_test_connection(){
        try{
            if( this.__test_socket != null){
                this.__test_socket.shutdownOutput();
                this.__test_socket.shutdownInput();
                this.__test_socket.close();
            }
        }
        catch(IOException e){
            logger.warning("Error on shutdown: "+ e.getMessage());
        }
    }

    public void finish_test(){
        try{
            if( this.__test_socket != null){
                this.__test_socket.close();
            }
        }
        catch(IOException e){
            logger.warning("Error on shutdown: "+ e.getMessage());
        }
    }
}
