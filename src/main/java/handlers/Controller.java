package handlers;

import static handlers.Parametri.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import com.google.gson.Gson;



public class Controller {

    private Socket control_socket;
//  private int role = ROLE_CLIENT; è considerato di default (non viene presa in considerazione possibilità di role diversi)

    
    public Controller(Socket control_socket){
        this.control_socket = control_socket;
    }

    // dal punto di vista del client extra = port
    public Integer[] recv_control_msg() throws ControllerException {  
        try{    
            Integer[] received = recv_msg_from_tcp_socket(this.control_socket);
            int msg = received[0];
            Integer port = received[1];

            if (msg < CONTROLLER_START_UB_MSG || msg > CONTROLLER_CLIENT_TEST_INIT_ERROR) 
            throw new ControllerException("Received message is not valid");
            
            if (msg >= CONTROLLER_START_UB_MSG && msg <= CONTROLLER_START_DT_MSG) {
                if (port == null) 
                    throw new ControllerException("Received message is " + msg + " but doesn't contain port");
                
                if (port != BT_PORT && port != BT_PORT + 1 && port != ALT_BT_PORT && port != TT_PORT) 
                    throw new ControllerException("The specified port for a start measure message is not valid");

            } else if (msg >= CONTROLLER_OK_MSG && msg <= CONTROLLER_CLIENT_TEST_INIT_ERROR && port != null) 
                throw new ControllerException("The specified port for a start measure message is not valid");

            Integer[] returned = new Integer[2];
            returned[0] = msg;
            returned[1] = port;
            return returned;

        } catch (SocketTimeoutException t) {
            throw new ControllerException("Controller socket timeout on receiving message: " + t.getMessage());
        } catch (IOException e) {
            throw new ControllerException("Controller socket error on receiving message: "+ e.getMessage());
        }
    }

    public Integer[] recv_msg_from_tcp_socket(Socket sock) throws ControllerException, IOException{
        int to_recv_size = 4;
        byte[] length_bytes = new byte[to_recv_size];
        int bytesRead = 0;
        while (to_recv_size != 0) {
            int count = sock.getInputStream().read(length_bytes, bytesRead, to_recv_size);
            if (count == -1) 
                throw new ControllerException("Receiving nothing, connection broken");
            to_recv_size -= count;
            bytesRead += count;
        }
        int length = ByteBuffer.wrap(length_bytes).getInt();
        logger.info("Received message length: " + length);

        int op;
        to_recv_size = 4;
        length_bytes = new byte[to_recv_size];
        bytesRead = 0;
        while (to_recv_size != 0) { 
            int count = sock.getInputStream().read(length_bytes, bytesRead, to_recv_size);
            if (count == -1) 
                throw new ControllerException("Receiving nothing, connection broken");
            to_recv_size -= count;
            bytesRead += count;
        }
        op = ByteBuffer.wrap(length_bytes).getInt();
        logger.info("Received op: " + op);
        length -= 4;

        Integer port;
        if(length != 0){
            // è stata inviata anche la porta
            length_bytes = new byte[length];
            bytesRead = 0;
            while (length != 0) {
                int count = sock.getInputStream().read(length_bytes, bytesRead, length);
                if (count == -1) 
                    throw new ControllerException("Receiving nothing, connection broken");
                length -= count;
                bytesRead += count;
            }
            String port_str = new String(length_bytes, StandardCharsets.UTF_8);
            port = Integer.parseInt(port_str);
        }
        else
            port = null;

        logger.info("Received port: " + port);
        
        Integer[] returned = new Integer[2];
        returned[0] = op;
        returned[1] = port;
        return returned;
    }

    //overloading
    public void send_control_msg(int msg) throws ControllerException{
        send_control_msg(msg,null);
    }

    public <K, V> void send_control_msg(int msg, HashMap<K,V> extra) throws ControllerException{
        if (msg < CONTROLLER_START_UB_MSG || msg > CONTROLLER_CLIENT_TEST_INIT_ERROR) 
            throw new ControllerException("Message is not valid");
        
        if (msg >= CONTROLLER_START_UB_MSG && msg <= CONTROLLER_START_DT_MSG)
            throw new ControllerException("Trying to send a server message without being server");

        if(msg >= CONTROLLER_OK_MSG && msg <= CONTROLLER_CLIENT_TEST_INIT_ERROR){
            // problema libreria esterna e il tipo extra
            try{
                if(extra == null)
                    send_msg_on_tcp_socket(this.control_socket,msg);
                else{
                    Gson gson = new Gson();
                    send_msg_on_tcp_socket(this.control_socket,msg,gson.toJson(extra).getBytes());
                }
            }
            catch(IOException ie){
                logger.info(ie.getMessage());
                throw new ControllerException("Controller socket error on sending message: " + ie.getMessage());
            }
        }
        else
            logger.severe("error in send_control_msg");
    }
    
    //overloading
    public static void send_msg_on_tcp_socket(Socket sock, int op) throws IOException{
        send_msg_on_tcp_socket(sock,op,null);
    }
    
    public static void send_msg_on_tcp_socket(Socket sock, int op, byte[] json) throws IOException{   // problema json -> json (è la conversione json dei risultati)
        ByteBuffer buffer; // contiene il messaggio da inviare
        
        if(json == null){
            buffer = ByteBuffer.allocate(8); //int + int
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(4); //contenente la lunghezza del dato passato (op è un int (4))
            buffer.putInt(op); //specifica l'opzione che si passa al server
        }
        else{  // bisogna passare op e l'oggetto json
            int len = 4 + json.length; //contenente la lunghezza del dato passato (op (4) + json (json.length))
            buffer = ByteBuffer.allocate(4 + len); // 4 per la dimensione del contenuto del messaggio, len messaggio effettivo
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(len); // dimensione del contenuto
            buffer.putInt(op).put(json); // dati effettivi (op è un int + json che è oggetto json)
        }
    
        byte[] msg = buffer.array();
        logger.info("Bytes to send: " + msg.length);
        
        OutputStream out = sock.getOutputStream();
        out.write(msg);
        out.flush();   
    }

    public void abort_measure() throws ControllerException{
        send_control_msg(CONTROLLER_ABORT_MEASURE_MSG);
    }

    public void finish_measure() throws ControllerException{
        send_control_msg(CONTROLLER_FINISH_MEASURE_MSG);
    }
}
