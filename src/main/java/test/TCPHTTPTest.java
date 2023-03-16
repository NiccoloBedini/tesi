package test;

import static test.Parametri.*;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class TCPHTTPTest extends TCPTest {
    private String host;
    private String http_file;
    
    public TCPHTTPTest(String host, String http_file, int transfer_dimension){
        super(transfer_dimension);
        this.host = host;
        this.http_file = http_file;
    }
    //overloading per gestire transfer_dimension non DEFAULT anche se non è possibile
    public TCPHTTPTest(String host, String http_file){  
        super(DEFAULT_HTTP_TRANSFER_DIMENSION);
        this.host = host;
        this.http_file = http_file;
    }

    // il tipo di ritorno è per matchare i test delle altre classi
    public HashMap<Double, Integer> downlink_test(Socket receive_socket, HashMap<Double, Integer> intervals) throws IOException{
        receive_socket.setSoTimeout(5000);

        String request = "GET /" + http_file + " HTTP/1.1\r\nHost: " + host + "\r\n\r\n";
        super.send_on_socket(receive_socket, request.getBytes());

        double start = System.currentTimeMillis() / 1000;
        intervals.put(start, 0);
        byte[] msg = super.receive_from_socket(receive_socket, transfer_dimension, intervals);
        double stop = System.currentTimeMillis() / 1000;
        double interval = stop - start;

        int total_rec = msg.length;
        logger.info("Received: " + total_rec + ", Interval: " + interval + ", Throughput: " + (total_rec / interval));

        return null;
    }


    public void uplink_test(Socket send_socket, int duration){ //DEFAULT_TEST_DURATION
        return;
    }
    public void uplink_test(Socket send_socket){ //DEFAULT_TEST_DURATION
        return;
    }




    public void downlink_traceroute(){
        return;
    }
    public void uplink_traceroute(){
        return;
    }

}
