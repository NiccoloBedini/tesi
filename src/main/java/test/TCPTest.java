package test;

import static test.Parametri.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;



abstract class Test{
    Logger logger = Logger.getLogger("nutmon.TEST");
    protected int transfer_dimension;

    protected Test(Integer transfer){
        if(transfer == null)
            transfer = DEFAULT_BT_TRANSFER_DIMENSION;
        transfer_dimension = transfer;
    }

    abstract void send_on_socket(Socket send_socket, byte[] data)  throws IOException;

    abstract byte[] receive_from_socket(Socket receive_socket, int length, HashMap<Double, Integer> intervals) throws IOException;

    // abstract void uplink_test(Socket send_socket, int duration); //DEFAULT_TEST_DURATION

    // abstract void downlink_test(Socket receive_socket, HashMap<Double, Integer> intervals) throws IOException;

    // abstract void uplink_traceroute(Socket send_socket, Socket icmp_socket, String traceroute, String stop_interfaces);

    // abstract void downlink_traceroute(Socket receive_socket);
}



public abstract class TCPTest extends Test{

    protected TCPTest(int transfer) {
        super(transfer);
    }

    protected void send_on_socket(Socket send_socket, byte[] data) throws IOException{
        // System.out.println("invio di " + data.length + " Bytes."); non è un vero controllo!
        OutputStream out = send_socket.getOutputStream();
        out.write(data);
        out.flush();
        
    }
    //overloading
    protected byte[] receive_from_socket(Socket receive_socket, int length) throws IOException{
        return receive_from_socket(receive_socket,length,null);
    }
    
    protected byte[] receive_from_socket(Socket receive_socket, int length, HashMap<Double, Integer> intervals) throws IOException{
        byte[] rec = new byte[length];
        int copied_byte = 0;
        int received_bytes = 0;
        while (length > 0) {
            try {
                byte[] msg = new byte[length];
                received_bytes = receive_socket.getInputStream().read(msg);

                if (received_bytes == -1) {
                    logger.warning("Test: Receiving nothing, connection broken");
                    break;
                }

                if (intervals != null) {
                    intervals.put((double)System.currentTimeMillis() / 1000, received_bytes);
                }

                length -= received_bytes;
                System.arraycopy(msg, 0, rec, copied_byte, received_bytes);
                copied_byte += received_bytes;
            } catch (SocketTimeoutException to) {
                if (intervals == null || received_bytes != 5) 
                    throw to;
                logger.info("Timeout occurred, measurement finished: " + to.getMessage());
                break;
            }
        }
        // per gestire il caso del choke = 5 (!= length = 1360)
        if(length != received_bytes) 
            return  Arrays.copyOfRange(rec, 0, copied_byte);
        else
            return rec;
    }



    protected static byte[] generate_random_bytes(int n) {
        SecureRandom random = new SecureRandom();
        int rest = n % 4;
        int number = n / 4;
        
        ByteBuffer buffer = ByteBuffer.allocate(number * 4 + rest);
        buffer.order(ByteOrder.BIG_ENDIAN);

        for (int i = 0; i < number; i++) {
            int r = random.nextInt();
            buffer.putInt(r);
        }
        if (rest != 0) {
            for (int i = 0; i < rest; i++) {
                int r = random.nextInt(256);
                buffer.put((byte)r);
            }
        }
        return buffer.array();
    }

    protected byte[] fromhex(String hex_string) {   // replica funzione fromhex di python
        int length = hex_string.length();
        byte[] byte_array = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            byte_array[i / 2] = (byte) ((Character.digit(hex_string.charAt(i), 16) << 4)
                                  + Character.digit(hex_string.charAt(i+1), 16));
        }
        return byte_array;
    }

    abstract public void uplink_test(Socket send_socket) throws IOException;
    abstract public void uplink_test(Socket send_socket, int duration) throws IOException; //DEFAULT_TEST_DURATION

    abstract public void uplink_traceroute();
    abstract public void downlink_traceroute();

    abstract public HashMap<Double, Integer> downlink_test(Socket receive_socket, HashMap<Double, Integer> intervals) throws IOException;
}