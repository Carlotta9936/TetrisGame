package com.company.server;

import com.company.client.Client;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Map;

import static com.googlecode.lanterna.TextColor.ANSI.BLACK;

public class ServerSender implements Runnable{

    private Panel panel;
    private TextColor coloreLabel;
    public static String name;
    private String players = "";
    public String ip;


    public ServerSender(Panel panel, TextColor coloreLabel, String name) {
        this.name=name;
        this.panel=panel;
        this.coloreLabel=coloreLabel;
    }

    public synchronized void run() {
            TextBox messaggio = new TextBox();
            panel.addComponent(messaggio);
            inviaMessaggio(messaggio);
    }

    public synchronized void inviaMessaggio(final TextBox messaggio){
        new Button("Invia",new Runnable(){
            @Override
            public void run(){
                //flag=false;
                //Finché non ricevi un comando "quit" dall'utente...
                //userMessage = userInput.nextLine(); //... leggi un messaggio da console (bloccante!)...
                //toOther.println(userMessage); //... e invialo al server
                if (!messaggio.getText().equals("")) {

                    String messaggioString = messaggio.getText();

                    // Se il server invia il messaggio "/start" il gioco ccerca di partire

                    if (messaggioString.equals("/start")) {
                        System.out.println("Il Server ha dato il comando START : il gioco cerca di partire...");
                        //controllo il numero dei giocatori
                        if (Server.connectedClients.size() < 2) {
                            System.out.println("Numero di giocatori insufficiente");
                            Label lab_serverMsg = new Label("[SERVER]: Numero di giocatori insufficiente.")
                                    .setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                            panel.addComponent(lab_serverMsg);
                            broadcastServerMessage("[" + name + "]: " + messaggioString);


                        } else {
                            //svuoto il pannello e avverto i client
                            System.out.println("Partita iniziata!");
                            broadcastServerMessage(messaggioString);
                            for (Map.Entry<String, PrintWriter> pair : Server.connectedClients.entrySet()) {
                                players = players + pair.getKey() + "-";
                            }
                            broadcastServerMessage(players);
                            players = "";
                            Server.gameStarted = true;
                            Label lab_serverMsg = new Label("[SERVER]: Partita iniziata").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                            panel.addComponent(lab_serverMsg);
                            broadcastServerMessage("[" + name + "]: " + messaggioString);

                        }
                    } else if (messaggioString.equals("/quit")) {
                        System.out.println("Uscita dal gioco");
                        if(Server.connectedClients.size()==0){
                            Server.serverThread.stop();
                            System.exit(0);
                        }
                        else {
                            broadcastServerMessage(messaggioString);
                            Server.connectedClients.clear();
                            Server.senderThread.stop();
                            Server.serverThread.stop();
                            System.exit(0);

                        }
                    } else if(messaggioString.equals("/restart")) {
                        System.out.println("Restart...");
                        Label lab_serverMsg = new Label("[SERVER]: Resettando la partita...").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                        panel.addComponent(lab_serverMsg);
                        broadcastServerMessage("[" + name + "]: " + messaggioString);

                    }else if(messaggioString.equals("/clear")){
                        System.out.println("Pulisci schermo...");
                        panel.removeAllComponents();
                        Label lab=new Label("\nStarting "+ name + "... ").setBackgroundColor(BLACK).setForegroundColor(
                                coloreLabel);
                        panel.addComponent(lab);
                        Label myIp = new Label("\nShare your ip address: " + Server.ip).setBackgroundColor(BLACK).setForegroundColor(
                                coloreLabel);
                        panel.addComponent(myIp);
                        Label lab_serverOn=new Label("\n- - - Server on - - -").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                        panel.addComponent(lab_serverOn);
                        panel.addComponent(messaggio);
                        inviaMessaggio(messaggio);
                    } else {
                        // In tutti gli altri casi trasmetto il messaggio del server a tutti i client connessi
                        System.out.println("trasmetto il messaggio del server a tutti i client connessi");
                        Label lab_serverMsg = new Label("[" + name + "]: " + messaggioString).setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                        panel.addComponent(lab_serverMsg);
                        broadcastServerMessage("[" + name + "]: " + messaggioString);
                    }

                    // Una volta inviato il messaggio pulisco la textbox
                    messaggio.setText("");
                }
            }
        }).addTo(panel);
    }

    public void broadcastServerMessage(String message) {

        for(Map.Entry<String, PrintWriter> e : Server.connectedClients.entrySet()) {

            e.getValue().println(message);
            e.getValue().flush();
        }
    }
}
