package com.company.server;

import com.company.MainSchermata;
import com.company.client.Client;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import static com.googlecode.lanterna.TextColor.ANSI.BLACK;

// Per ogni client che si connetterà al server creo un Thread handlerThread che si occupa
// di gestire singolarmente i client uno a uno
public class ClientHandler implements Runnable {
    public static Socket clientSocket;
    private String username;
    private BufferedReader fromClient;
    private Panel panel;
    private TextColor coloreLabel;
    private Boolean  usernameCheck = true;

    public ClientHandler(Socket socket, String userName,Panel panel,TextColor coloreLabel) {
        this.clientSocket = socket;
        this.username = userName;
        this.panel=panel;
        this.coloreLabel=coloreLabel;
    }

    public void clientHandler() {
        PrintWriter out;
        try {
            InputStream socketInput = clientSocket.getInputStream();
            InputStreamReader socketReader = new InputStreamReader(socketInput);
            fromClient = new BufferedReader(socketReader);

            out = new PrintWriter(clientSocket.getOutputStream(), true);

            username = fromClient.readLine();

            for(Entry<String, PrintWriter> user : Server.connectedClients.entrySet()){
                if(user.getKey().equals(username)){
                    System.out.println("entrato");
                    out.println("_terminate");
                    usernameCheck = false;
                    clientSocket.close();

                }
            }

            if(usernameCheck) {
                Server.connectedClients.put(username, out);

                for (Entry<String, PrintWriter> e : Server.connectedClients.entrySet()) {

                    if (e.getKey().equals(username)) {

                        e.getValue().println(ServerSender.name);
                        e.getValue().flush();
                    }
                }
                // Appena il seguente client si connette al server lo comunico a tutti nella
                // chat pre-partita
                Label lab_newClient = new Label("[SERVER]: " + "New client connected: " + username + " ["
                        + clientSocket.getInetAddress().getHostAddress()
                        + "]").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                panel.addComponent(lab_newClient);

                broadcastServerMessage("[SERVER]: " + "New client connected: " + username + " ["
                        + clientSocket.getInetAddress().getHostAddress()
                        + "]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized void run() {
        if (usernameCheck) {
            try {
                String message = "";
                while ((message != null || Server.serverThread.isAlive()) && !Server.gameStarted) {

                    // Quando un client invia un messaggio viene ricevuto dal server qui
                    try {
                        message = fromClient.readLine();
                        if (message != null && !Server.gameStarted) {

                            // Se il messaggio ricevuto dal client è /quit il server esce dal ciclo e finisce nel "finally"
                            if (message.toLowerCase().equals("/quit")) break;

                            // Il server si occupa poi di trasmettere il messaggio agli altri client
                            Label lab_clientMsg = new Label("[" + username + "]: " + message).setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                            panel.addComponent(lab_clientMsg);

                            System.out.println("[" + username + "]: " + message);
                            broadcastMessage(String.format("[%s]: %s", username, message), username);
                            System.out.println(Server.gameStarted);
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        Server.serverThread.stop();
                        System.exit(0);
                    }

                }

                while (Server.gameStarted) {
                    message = fromClient.readLine();

                    if (message != null) {

                        if (Server.connectedClients.size() == 1 && Server.gameStarted) {

                            Label lab_clientPerso = new Label("[SERVER]: " + username + " is the winner!").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                            panel.addComponent(lab_clientPerso);
                            broadcastServerMessage(Server.connectedClients.keySet() + "-winner");
                            Server.gameStarted = false;
                        }
                        synchronized (this) {
                            for (String i : Server.connectedClients.keySet()) {

                                if (message.equals(i + "-lost")) {

                                    Label lab_clientPerso = new Label("[SERVER]: " + i + " lost!").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                                    panel.addComponent(lab_clientPerso);

                                    if ((Server.connectedClients.size() - 1) > 1) {

                                        Label lab_clientPerso2 = new Label("[SERVER]: " + (Server.connectedClients.size() - 1) + " players left!").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                                        panel.addComponent(lab_clientPerso2);

                                    } else {

                                        Label lab_clientPerso3 = new Label("[SERVER]: " + (Server.connectedClients.size() - 1) + " player left!").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                                        panel.addComponent(lab_clientPerso3);

                                    }
                                    Server.connectedClients.remove(i);
                                }
                            }
                            broadcastMessage(String.format("%s", message), username);
                        }
                    }
                }

                System.out.println(username + " si è spento!");
                clientSocket.close(); //Interrompi la connessione

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                    // Quando un giocatore invia il comando /quit si disconnette dal server
                    // Viene quindi mandato un messaggio di aggiornamento a tutti i client
                    if (true) System.out.println("[SERVER]: " + username + " is leaving");
                    Server.connectedClients.remove(username);

                    Label lab_clientLeft = new Label("[SERVER]: " + username + " has left").setBackgroundColor(BLACK)
                            .setForegroundColor(coloreLabel);
                    panel.addComponent(lab_clientLeft);
                    Label lab_clientTot = new Label("[SERVER]: " + "Connected clients: " + Server.connectedClients
                            .size() + "/4").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                    panel.addComponent(lab_clientTot);

                    broadcastServerMessage("[SERVER]: " + username + " has left");
                    broadcastServerMessage("[SERVER]: Connected clients: " + Server.connectedClients.size() + "/4");
                    /*try {
                        ClientHandler.clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
            }
        }
    }


    public void broadcastServerMessage(String message) {

        for(Entry<String, PrintWriter> e : Server.connectedClients.entrySet()) {

            e.getValue().println(message);
            e.getValue().flush();
        }
    }

    // Questo metodo serve a trasmettere il messaggio di un client a tutti gli altri tranne a se stesso
    public  void broadcastMessage(String message, String username) {

        for(Entry<String, PrintWriter> e : Server.connectedClients.entrySet()) {

            if(!e.getKey().equals(username)){

                e.getValue().println(message);
                e.getValue().flush();
            }
        }
    }
}
