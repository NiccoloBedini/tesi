package handlers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Connector {
    public Socket connector_socket;

    public Connector(){
        connector_socket = new Socket(); 
    }

    // overloading
    public void connect(String server_address, int server_port) throws ConnectorException{
        connect(server_address,server_port,0);
    }

    public void connect(String server_address, int server_port, Integer timeout) throws ConnectorException{
        try {
            if (timeout != 0) {
                connector_socket.setSoTimeout(timeout);
            }
            connector_socket.connect(new InetSocketAddress(server_address, server_port));
        } catch (IOException e) {
            throw new ConnectorException("Couldn't connect to server " + server_address + " on port " + server_port, e);
        }
    }

    public void close_connection() throws ConnectorException{
        try{
            connector_socket.close();
        }
        catch(IOException e){
            throw new ConnectorException("Couldn't correctly close the socket", e);
        }
    }
}

  


