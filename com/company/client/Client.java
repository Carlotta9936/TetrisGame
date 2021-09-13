package com.company.client;

import com.company.Gioco.RiceviStato;
import com.company.Gioco.*;
import com.company.MainSchermata;
import com.company.server.ClientHandler;
import com.company.server.Server;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.ClientInfoStatus;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.googlecode.lanterna.TextColor.ANSI.BLACK;

public class Client implements Runnable {
    private String name;
    private String IP;
    private int PORT;
    private Panel panel;
    private TextColor coloreLabel;
    public static Socket socket;
    private String serverName;
    private String playersData;
    public static Boolean winner = false;
    public static Thread gameThread;
    private String message = "";
    private Boolean pause = false;
    private List<String> connectedClients;
    private Boolean terminate = true;


    // Reperisco dal form di "find game" i vari dati che mi interessano
    public Client(String name, String IP, String PORT, Panel panel, TextColor coloreLabel, List<String> connectedClients) {
        this.name = name;
        this.IP = IP;
        this.PORT = Integer.parseInt(PORT);
        this.panel = panel;
        this.coloreLabel = coloreLabel;
        this.connectedClients=connectedClients;
    }

    public void run() {

        // Inizializzo la chat pre-partita del client
        panel.removeAllComponents();
        panel.setFillColorOverride(BLACK);
        panel.setPosition(new TerminalPosition(0, 0));
        panel.setPreferredSize(new TerminalSize(100, 10));
        panel.removeAllComponents();
        panel.setFillColorOverride(BLACK);
        Label lab = new Label("\nYour nickname: " + name).setBackgroundColor(BLACK)
                .setForegroundColor(coloreLabel);
        Label connessione = new Label("\nConnecting to the server...").setBackgroundColor(BLACK)
                .setForegroundColor(coloreLabel);
        panel.addComponent(lab);
        panel.addComponent(connessione);
        socket = null; //Creazione socket, connessione a localhost:1555
        InputStream socketInput = null;
        OutputStream socketOutput = null;
        try {
            System.out.println("Connessione alla socket: " + IP + PORT);
            socket = new Socket(IP, PORT);
            System.out.println("Connessione eseguita!");
            Label connesso = new Label("\n- - - - Connected - - - -\n").setBackgroundColor(BLACK)
                    .setForegroundColor(coloreLabel);
            panel.addComponent(connesso);

            socketInput = socket.getInputStream();

            socketOutput = socket.getOutputStream();

        } catch (IOException ex) {
            ex.printStackTrace();
            Label nonconnesso = new Label("\n- - - - Connection failed try again - - - -\n").setBackgroundColor(BLACK)
                    .setForegroundColor(coloreLabel);
            panel.addComponent(nonconnesso);
            panel.removeAllComponents();
            panel.setFillColorOverride(BLACK);

            MainSchermata.Schermata(panel);

        }

        InputStreamReader socketReader = new InputStreamReader(socketInput);
        OutputStreamWriter socketWriter = new OutputStreamWriter(socketOutput);

        BufferedReader fromServer = new BufferedReader(socketReader); //Legge stringhe dal socket
        PrintWriter toServer = new PrintWriter(socketWriter); //Scrive stringhe sul socket

        //Creazione del thread di invio messaggi
        Sender clientSender = new Sender(toServer,panel,coloreLabel,name);
        Thread senderThread = new Thread(clientSender);
        senderThread.start();

        message = name;
        toServer.println(message);
        toServer.flush();

        try {
            serverName = fromServer.readLine();
            if(serverName.equals("_terminate")){
                terminate = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        RiceviStato rs = new RiceviStato();
        while(clientSender.shown){

        }
        if(terminate) {
            // Finché il server non chiude la connessione o non ricevi un messaggio "/quit"...
            while (message != null || message.equals("/quit")) {

                try {

                    // Leggi un messaggio inviato dal server
                    message = fromServer.readLine();
                    System.out.println(message);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                // Se il server invia un comando /quit mi disconnetto dal server
                if (message.equals("/quit")) {
                    /*
                    Label successo = new Label("\n- - - Server left - - -").setBackgroundColor(BLACK)
                            .setForegroundColor(coloreLabel);
                    panel.addComponent(successo);
                  */
                    try {
                        connectedClients.remove(name);
                        Client.socket.close();

                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    panel.removeAllComponents();
                    MainSchermata.Schermata(panel);
                    break;

                } else if (message.equals("/start")) {
                    try {

                        panel.removeAllComponents();
                        panel.setFillColorOverride(BLACK);
                        // Leggo i nick di tutti i giocatori
                        MainSchermata.screen.close();
                        connectedClients.clear();

                        if (YouWin.nextGame) {
                            YouWin.screen.close();
                        }
                        if (GameOver.nextGame) {
                            GameOver.screen.close();
                        }
                        if (Restart.nextGame) {
                            Restart.screen.close();
                        }
                        String playersStringMessage = fromServer.readLine();
                        for (String playerUser : playersStringMessage.split("-")) {
                            connectedClients.add(playerUser);
                        }
                        Schermo schermo = new Schermo(toServer, name, IP, PORT, panel, coloreLabel, connectedClients);
                        gameThread = new Thread(schermo);
                        gameThread.start();

                        Schermo.gameOver = false;

                        while (!Schermo.gameOver) {
                            try {

                                playersData = fromServer.readLine();
                                //System.out.println(playersData);

                                if (playersData.contains(":0")) {
                                    //Schermo.campoAvv=playersData;
                                    rs.run(playersData);

                                    //Schermo.traduciStringToInt(playersData);
                                }

                                //se il messaggio contiene la parola "spazzatura" so che dovrò aggiungere righe spazzatura
                                // in base al numero finale del messaggio
                                if (playersData.contains("spazzatura")) {

                                    //quindi divido il messaggio e aggiungo righe spazzatura in base a quanto dice il
                                    //messaggio ricevuto
                                    String arr[] = playersData.split("-");
                                    //controllo che le righe spazzatura siano indirizzate a me
                                    if(arr[1].equals(name)){
                                        if (arr[2].equals("2")) {
                                            Schermo.aggiungiSpazzatura = 1;
                                            System.out.println("Spazzatura aggiunta: " + Schermo.aggiungiSpazzatura);
                                        } else if (arr[2].equals("3")) {
                                            Schermo.aggiungiSpazzatura = 2;
                                            System.out.println("Spazzatura aggiunta: " + Schermo.aggiungiSpazzatura);

                                            //se non è ne 1 ne 2 ne 3 allora invierò 4 righe perché tanto meno non possono essere
                                            //se no sarebbe ricaduto in uno dei casi precedenti
                                        } else {
                                            Schermo.aggiungiSpazzatura = 4;
                                            System.out.println("Spazzatura aggiunta: " + Schermo.aggiungiSpazzatura);
                                        }
                                    }
                                }
                                if (playersData.contains("/pause") && !pause) {
                                    pause = true;
                                    gameThread.suspend();
                                }
                                if (playersData.contains("/resume") && pause) {
                                    pause = false;
                                    gameThread.resume();
                                }
                                if (playersData.contains("/restart") && !pause) {
                                    Schermo.gameOver = true;
                                    Client.winner = false;
                                    System.out.println("Partita ricominciata");
                                    Restart ricomincia = new Restart(name, IP, PORT, panel, coloreLabel, connectedClients);
                                    Thread ricominciaThread = new Thread(ricomincia);
                                    ricominciaThread.start();
                                } else if (playersData.equals("[" + name + "]-winner")) {

                                    winner = true;
                                    break;

                                } else if (Schermo.gameOver) {
                                    break;
                                }
                            } catch (IOException e) {

                                e.printStackTrace();

                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    //Se il messaggio non è nullo lo stampo
                    Label lab_clientMsg = new Label(message).setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                    panel.addComponent(lab_clientMsg);
                }
            }
        }
        if(connectedClients.size()!=0) {
            try {
                panel.removeAllComponents();
                panel.setFillColorOverride(BLACK);
                Label invalidNick = new Label("\nNickname già in uso da un altro utente.").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                panel.addComponent(invalidNick);
                new Button("Indietro", new Runnable() {
                    @Override
                    public void run() {
                        //svuoto la schermo
                        panel.removeAllComponents();
                        panel.setFillColorOverride(BLACK);
                        MainSchermata.Schermata(panel);
                        //richiamo schermata inziale
                    }
                }).addTo(panel);
                socket.close(); //Chiudi la connessione
                senderThread.interrupt();

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    socket.close(); //Chiudi la connessione
                } catch (IOException e) {
                    e.printStackTrace();
                }
                senderThread.interrupt();
            }

        }
    }
}
