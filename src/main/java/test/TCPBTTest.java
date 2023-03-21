package test;

import static test.Parametri.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;


public class TCPBTTest extends TCPTest{

    private byte[] random_bytes;
    private int offset;

    public TCPBTTest(){
        this(DEFAULT_BT_TRANSFER_DIMENSION);
    }

    public TCPBTTest(int transfer_dimension){
        super(transfer_dimension);
        SecureRandom random = new SecureRandom();
        random_bytes = new byte[BITTORRENT_BLOCK_DIMENSION * 1000];
        random.nextBytes(random_bytes);
        offset = 0;
    }

    // genera una concatenazione di tutte le richieste
    private int build_request(int index, byte[] request) {
        //request = new byte[NUMBER_OF_REQUESTS * BITTORRENT_REQUEST_TOTAL_LENGTH];   //1360
        int offset = BITTORRENT_START_OFFSET;
        for (int i = 0; i < NUMBER_OF_REQUESTS; i++) {
            byte[] msg = ByteBuffer.allocate(BITTORRENT_REQUEST_TOTAL_LENGTH).order(ByteOrder.BIG_ENDIAN)    // 4 + 1 + 4 + 4 + 4 = 17
                    .putInt(BITTORRENT_REQUEST_LENGTH)
                    .put((byte) BITTORRENT_REQUEST_TYPE)
                    .putInt(index)
                    .putInt(offset)
                    .putInt(BITTORRENT_BLOCK_DIMENSION)
                    .array();
            System.arraycopy(msg, 0, request, i * msg.length, msg.length);
            offset += BITTORRENT_BLOCK_DIMENSION;
            if (offset == BITTORRENT_PIECE_DIMENSION) {
                offset = BITTORRENT_START_OFFSET;
                index++;
            }
        }
        return index;
    }
    
    private byte[] generate_random_block(){
        byte[] random_block = Arrays.copyOfRange(random_bytes, offset, offset + BITTORRENT_BLOCK_DIMENSION);
        offset += BITTORRENT_BLOCK_DIMENSION;

        if(offset == random_bytes.length)
            offset = 0;

        return random_block;
    }

    // genera una concatenazione di tutte le risposte
    private byte[] build_response(byte[] request){

        int start = 0;
        int msg_len = BITTORRENT_RESPONSE_LENGTH + BITTORRENT_BLOCK_DIMENSION;
        ByteBuffer response = ByteBuffer.allocate(request.length / BITTORRENT_REQUEST_TOTAL_LENGTH * ( 12 + 1 + BITTORRENT_BLOCK_DIMENSION));  /* conto fatto ad occhio 
                                                                                                                    (numero di richieste * lunghezza singola risposta)*/       
        while (start < request.length) {
            // Index and Offset positions in request, relatively to the start of the single request
            int index = ByteBuffer.wrap(Arrays.copyOfRange(request, start + 5, start + 9)).getInt();
            int offset = ByteBuffer.wrap(Arrays.copyOfRange(request, start + 9, start + 13)).getInt();
            response.putInt(msg_len);
            response.put((byte) BITTORRENT_PIECE_TYPE);
            response.putInt(index);
            response.putInt(offset);
            response.put(generate_random_block());
            start += BITTORRENT_REQUEST_TOTAL_LENGTH;
        }  
        return response.array();
    }

    private void __uplink_preparation(Socket send_socket) throws IOException{
        super.receive_from_socket(send_socket, 68);
        byte[] handshake_send = fromhex("13426974546f7272656e742070726f746f636f6c000000000000000031420a403f2ea" +
                            "41c67aca80b46e956389a7f17b62d5452323832302d36333065666467316a677937");
        
        super.send_on_socket(send_socket, handshake_send);
        byte[] unchoke = fromhex("0000000101");
        super.send_on_socket(send_socket, unchoke);
        super.receive_from_socket(send_socket, 5);
    }
    
    //overloading per parametro opzionale
    public void uplink_test(Socket send_socket) throws IOException{
        uplink_test(send_socket, DEFAULT_TEST_DURATION);
    }

    public void uplink_test(Socket send_socket, int duration) throws IOException{ //DEFAULT_TEST_DURATION
        System.out.println("<BitTORRENT - TEST UPLINK>");
        send_socket.setSoTimeout(5000);
        __uplink_preparation(send_socket);
        long bytes_sent = 0;
        long stop = System.currentTimeMillis() / 1000;
        long start = System.currentTimeMillis() / 1000;

        while(stop - start <  duration){
            // 80 pieces request
            byte[] request = super.receive_from_socket(send_socket, 1360);
            byte[] response = build_response(request);
            super.send_on_socket(send_socket, response);
            bytes_sent += response.length;
            stop = System.currentTimeMillis() / 1000;
        }
        logger.info(" BYTES inviati: "+ bytes_sent + " Tempo impiegato: "+ (stop-start));

        //stop test -> invio choke
        byte[] choke = fromhex("0000000100");
        super.send_on_socket(send_socket, choke);
    }

    private void __downlink_preparation(Socket receive_socket) throws IOException{
        byte[] handshake_send = fromhex("13426974546f7272656e742070726f746f636f6c000000000000000031420a403f2ea" +
        "41c67aca80b46e956389a7f17b62d5452323832302d676b36317669687a6d623033");
        super.send_on_socket(receive_socket, handshake_send);
        // receive handshake
        super.receive_from_socket(receive_socket, 68);
        // receive unchoke
        super.receive_from_socket(receive_socket, 5);
        // send interest
        byte[] interest = fromhex("0000000102");
        super.send_on_socket(receive_socket, interest);
    }
    
    public HashMap<Double, Integer> downlink_test(Socket receive_socket, HashMap<Double, Integer> intervals) throws IOException{
        System.out.println("<BitTORRENT - TEST DOWNLINK>");
        receive_socket.setSoTimeout(5000);
        __downlink_preparation(receive_socket);
        int index = 0x0;
        long total_rec = 0;
        double start = System.currentTimeMillis() / 1000;
        intervals.put(start, 0);
        
        // send request (80 pieces of 0x4000 bytes) and receive response
        // if choke received (5 bytes), stop test
        while(true){
            byte[] requests = new byte[NUMBER_OF_REQUESTS * BITTORRENT_REQUEST_TOTAL_LENGTH];   //1360
            index = build_request(index, requests);    
            super.send_on_socket(receive_socket, requests);
            byte[] rec = super.receive_from_socket(receive_socket, transfer_dimension * NUMBER_OF_REQUESTS, intervals);
            total_rec += rec.length;
            if(rec.length == 5)
                break;
            }
            
            double stop = (System.currentTimeMillis() / 1000) - 5; // -5 per timeout
            double interval = stop - start;
            logger.info("Received: " + total_rec + ", Interval: " + interval + ", Throughput: " + (total_rec / interval)); 

            return intervals;
        }
        
        /* 
        public void downlink_traceroute(Socket receive_socket) throws IOException{
            receive_socket.setSoTimeout(15000);
            // receive unchoke
            super.receive_from_socket(receive_socket, 5);
            // send interest
            byte[] interest = fromhex("0000000102");
            super.send_on_socket(receive_socket, interest);
            
            byte[] requests = null;
            int index = 0x0;
            index = build_request(index, requests);
            super.send_on_socket(receive_socket, requests);
            
            super.receive_from_socket(receive_socket, transfer_dimension * NUMBER_OF_REQUESTS);
            // receive choke
            super.receive_from_socket(receive_socket, 5);
        }
    */
    
    // public String[] uplinkTraceroute(Socket sendSocket, DatagramSocket icmpSocket, String[] traceroute, String[] stopInterfaces) throws Exception {}
}
