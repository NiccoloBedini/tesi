package test;

import static test.Parametri.*;

import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;

public class TCPRandomTest extends TCPTest{

    private byte[] random_bytes_response;
    private int offset_response;
    private byte[] random_bytes_request;
    private int offset_request;
    
    public TCPRandomTest(int transfer_dimension){
        super(transfer_dimension);
        SecureRandom random = new SecureRandom();

        random_bytes_response = new byte[transfer_dimension * 1000];
        random.nextBytes(random_bytes_response);
        offset_response = 0;
        random_bytes_request = new byte[BITTORRENT_REQUEST_TOTAL_LENGTH * NUMBER_OF_REQUESTS * 1000];
        random.nextBytes(random_bytes_request);
        offset_request = 0;
    }
    // solito discorso di overloading 
    public TCPRandomTest(){    
        this(DEFAULT_BT_TRANSFER_DIMENSION);
    }


    private byte[] build_request(){
        byte[] bytes_array = Arrays.copyOfRange(random_bytes_request, offset_request, offset_request + 
                                                (BITTORRENT_REQUEST_TOTAL_LENGTH * NUMBER_OF_REQUESTS));

        offset_request += (BITTORRENT_REQUEST_TOTAL_LENGTH * NUMBER_OF_REQUESTS);
        if(offset_request == random_bytes_request.length)
            offset_request = 0;

        return bytes_array;
    }

    private byte[] build_response(){
        byte[] bytes_array = new byte[NUMBER_OF_REQUESTS * DEFAULT_BT_TRANSFER_DIMENSION];
        // non capisco il senso di questo for - basterebbe fare la copia in un unica istruzione
        for(int i = 0; i < NUMBER_OF_REQUESTS; i++ ){
            System.arraycopy(random_bytes_response, offset_response, bytes_array, offset_response,DEFAULT_BT_TRANSFER_DIMENSION );
            offset_response += DEFAULT_BT_TRANSFER_DIMENSION;

            if(offset_response == random_bytes_response.length )
                offset_response = 0;
        }
        return bytes_array;
    }

    private void __uplink_preparation(Socket send_socket) throws IOException{
        super.receive_from_socket(send_socket, 68);
        byte[] handshake_send = super.generate_random_bytes(68);
        super.send_on_socket(send_socket, handshake_send);
        byte[] unchoke = super.generate_random_bytes(5);
        super.send_on_socket(send_socket, unchoke);
        super.receive_from_socket(send_socket,5);
    }

    //overloading per parametro opzionale
    public void uplink_test(Socket send_socket) throws IOException{
        uplink_test(send_socket, DEFAULT_TEST_DURATION);
    }

    public void uplink_test(Socket send_socket, int duration) throws IOException{ 
        logger.info("<TCP - TEST UPLINK>");
        __uplink_preparation(send_socket);
        int bytes_sent = 0;
        long stop = System.currentTimeMillis() / 1000;
        long start = System.currentTimeMillis() / 1000;
        
        while(stop - start < duration){
            // 80 pieces request
            super.receive_from_socket(send_socket, BITTORRENT_REQUEST_TOTAL_LENGTH * NUMBER_OF_REQUESTS);
            byte[] response = build_response();
            super.send_on_socket(send_socket, response);
            bytes_sent += response.length;
            stop = System.currentTimeMillis() / 1000;
        }

        // stop test -> invio choke
        byte[] choke = generate_random_bytes(5);
        super.send_on_socket(send_socket, choke);
    }

    /* 
    public String[] uplinkTraceroute(Socket send_socket, DatagramSocket icmp_socket, String[] traceroute) throws IOException {
        int not_responding = 0;
        send_socket.setSoTimeout(5000);
        icmp_socket.setSoTimeout(2000);

        SecureRandom random = new SecureRandom();
        byte[] unchoke = new byte[5];
        random.nextBytes(unchoke);

        super.send_on_socket(send_socket, unchoke);
        super.receive_from_socket(send_socket, 5);
        super.receive_from_socket(send_socket, 1360);
        byte[] response = this.build_response();
        int ttl = 0;
        icmp_socket.getSoTimeout();
        InetAddress peer_address = send_socket.getInetAddress();
        int peer_port = send_socket.getPort();
    }

    */


    private void __downlink_preparation(Socket receive_socket) throws IOException{
        byte[] handshake_send = super.generate_random_bytes(68);
        super.send_on_socket(receive_socket, handshake_send);
        // receive handashake
        super.receive_from_socket(receive_socket, 68);
        // receive unchoke
        receive_from_socket(receive_socket, 5);
        // send interest
        byte[] interest = generate_random_bytes(5);
        send_on_socket(receive_socket, interest);
    }

    public HashMap<Double, Integer> downlink_test(Socket receive_socket, HashMap<Double, Integer> intervals) throws IOException{
        logger.info("<TCP - TEST DOWNLINK>");
        __downlink_preparation(receive_socket);
        receive_socket.setSoTimeout(5000);
        int total_rec = 0;
        double start = System.currentTimeMillis() / 1000;
        intervals.put(start, 0);
        // send request (80 pieces of 0x4000 bytes) and receive response
        // if choke received (5 bytes), stop test

        while(true){
            byte[] request = build_request();
            super.send_on_socket(receive_socket, request);
            byte[] rec = receive_from_socket(receive_socket, super.transfer_dimension * NUMBER_OF_REQUESTS, intervals);
            total_rec += rec.length;
            // controllo se arrivato choke
            if(rec.length == 5)
                break; 
        }

        double stop = (System.currentTimeMillis() / 1000) - 5;
        double interval = stop - start;
        logger.info("Received: " + total_rec + ", Interval: " + interval + ", Throughput: " + (total_rec / interval));

        return intervals;
    }

    public void downlink_traceroute(Socket receive_socket) throws IOException{
        receive_socket.setSoTimeout(15000);
        // receive unchoke
        super.receive_from_socket(receive_socket, 5);
        // send interest
        byte[] interest = super.generate_random_bytes(5);
        super.send_on_socket(receive_socket, interest);
        byte[] request = build_request();
        super.send_on_socket(receive_socket, request);
        super.receive_from_socket(receive_socket, transfer_dimension * NUMBER_OF_REQUESTS);
        // receive choke
        super.receive_from_socket(receive_socket, 5);
    }


    public void downlink_traceroute(){
        return;
    }
    public void uplink_traceroute(){
        return;
    }
}
