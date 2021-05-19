import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.Arrays;

public class Server {
  
    private static HashMap<String, PrintWriter> connectedClients = new HashMap<>();

    public static void main(String [] args) {

    ServerSocket listener = null;
    PrintWriter out = null;
    int port = 6789;

    System.out.println("\nAvviando il server...");

        try {
 
            // server in ascolto sulla porta 6789
            listener = new ServerSocket(port);
            listener.setReuseAddress(true);
            System.out.println("\n- - - Server on - - -");
        
            // creo il thread di comunicazione del server
            // e lo avvio 
            ServerSender serverSender = new ServerSender();
            Thread senderThread = new Thread(serverSender);
            senderThread.start();

            // ciclo per far connettere più client al server
            while (true) {
                for(int i = 0; i <= 8; i++){

                    Socket socket = listener.accept();

                    // creo un thread per ogni client così
                    // da essere gestiti singolarmente
                    ClientHandler clientSock = new ClientHandler(socket, "");
                    new Thread(clientSock).start();

                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // classe per la trasmissione di un messaggio inviato da un client agli altri client
    private static void broadcastMessage(String message, String username) {

		for(Entry<String, PrintWriter> e : connectedClients.entrySet()) {

			if(!e.getKey().equals(username)){

                e.getValue().println(message);
                e.getValue().flush();
            }
		}
	}


    // classe per la trasmissione di un messaggio inviato dal server agli altri client
    private static void broadcastServerMessage(String message) {	

        for(Entry<String, PrintWriter> e : connectedClients.entrySet()) {

                e.getValue().println(message);
                e.getValue().flush();
        }
	}


    // classe per la gestione del thread del server
    public static class ServerSender implements Runnable {

        public ServerSender() {

        }

        public void run() {
            Scanner userInput = new Scanner(System.in);
            String userMessage = "";
            while(!Thread.interrupted()) { //Finché non ricevi un comando "quit" dall'utente...
                userMessage = userInput.nextLine(); //... leggi un messaggio da console (bloccante!)...
                broadcastServerMessage("[SERVER]: " + userMessage);

            }

            userInput.close();
        }
    }


    // classe per la gestione dei thread dei client
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        public String username;
        private BufferedReader fromClient;
  
        // Constructor
        public ClientHandler(Socket socket, String userName)
        {
            PrintWriter out = null;
            try{
                this.clientSocket = socket;
                this.username = userName;
                InputStream socketInput = clientSocket.getInputStream();          
                InputStreamReader socketReader = new InputStreamReader(socketInput);
                fromClient = new BufferedReader(socketReader);

                out = new PrintWriter(clientSocket.getOutputStream(), true);

                username = fromClient.readLine();
                System.out.println("New client connected: " + username + " [" 
                    + clientSocket.getInetAddress().getHostAddress() 
                    + "]" );
            
                connectedClients.put(username, out);
            
            } catch (Exception e) {
                e.printStackTrace();
            }     

        }

        public void run()
        {           
            try {
                //LOGICA APPLICATIVA - RICEZIONE MESSAGGI
                String message = "";
                while(message != null && !message.equals("quit")) { //Finché il client non chiude la connessione o non ricevi un messaggio "quit"...
                    message = fromClient.readLine(); //Leggi un messaggio inviato dal server (bloccante!)
                        if (message != null) {
                            System.out.println("[" + username + "]: " + message);
                            broadcastMessage(String.format("[%s]: %s", username, message), username);

                        }
                }

                clientSocket.close(); //Interrompi la connessione

            } catch (Exception e) {
                e.printStackTrace();
            }          
        }      
    }
}
