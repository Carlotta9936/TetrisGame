package com.company.client;

import com.company.Gioco.*;
import com.company.MainSchermata;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

import static com.googlecode.lanterna.TextColor.ANSI.BLACK;
import static com.googlecode.lanterna.TextColor.ANSI.RED;

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
    public static Boolean winCheck = false;
    public static Boolean loseCheck = false;
    public static Thread gameThread;
    private String message = "";
    private Boolean pause = false;
    private List<String> connectedClients;
    private Boolean terminate = false;


    /**
     * Classe principale del client,
     * si occupa di inizializzare tutti i componenti
     * le variabili pubbliche e i Thread necessari
     * al funzionamento, gestisce inoltre la ricezione
     * dei messaggi/dati durante il pre-partita e la partita.
     */
    public Client(String name, String IP, String PORT, Panel panel, TextColor coloreLabel, List<String> connectedClients) {
        // Reperisco dal form di "find game" i vari dati che mi interessano
        this.name = name;
        this.IP = IP;
        this.PORT = Integer.parseInt(PORT);
        this.panel = panel;
        this.coloreLabel = coloreLabel;
        this.connectedClients=connectedClients;
    }

    public void run() {

        // Inizializzo la chat pre-partita del client.
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
        socket = null;
        InputStream socketInput = null;
        OutputStream socketOutput = null;
        try {
            System.out.println("Connessione alla socket: " + IP + PORT);
            socket = new Socket(IP, PORT);
            socketInput = socket.getInputStream();

            socketOutput = socket.getOutputStream();
            InputStreamReader socketReader = new InputStreamReader(socketInput);
            OutputStreamWriter socketWriter = new OutputStreamWriter(socketOutput);

            BufferedReader fromServer = new BufferedReader(socketReader); //Legge stringhe dal socket
            PrintWriter toServer = new PrintWriter(socketWriter); //Scrive stringhe sul socket

            // Creazione del thread di invio messaggi.
            Sender clientSender = new Sender(toServer,panel,coloreLabel,name);
            Thread senderThread = new Thread(clientSender);
            senderThread.start();

            message = name;
            toServer.println(message);
            toServer.flush();

            // Controllo sul Nickname o Lettura del nome del Server.
            try {
                serverName = fromServer.readLine();
                if(serverName.equals("_terminate")){
                    terminate = true;
                }
            } catch (IOException e) {
                System.out.println("Errore di lettura: " + e);
            }

            System.out.println("Connessione eseguita!");
            Label connesso = new Label("\n- - - - Connected - - - -\n").setBackgroundColor(BLACK)
                    .setForegroundColor(coloreLabel);
            panel.addComponent(connesso);

            RiceviStato rs = new RiceviStato();

            // Aspetto che i componenti appaiano a video.
            Thread.currentThread().sleep(500);

            if(!terminate) {
                // Finché il server non chiude la connessione o non ricevi un messaggio "/quit"...
                while (message != null || message.equals("/quit")) {
                    Boolean flag=true;
                    try {
                        // Leggi un messaggio inviato dal server
                        message = fromServer.readLine();

                        // Se si riceve un messaggio null è perché ci si è disconnessi dal server.
                        if(message==null){
                            Label successo = new Label("\n- - - Ti sei disconnesso dal server - - -").setBackgroundColor(BLACK)
                                    .setForegroundColor(RED);
                            try {
                                connectedClients.remove(name);
                                Client.socket.close();

                            } catch (SocketException e) {
                                // e.printStackTrace();
                            } catch (IOException e) {
                                //e.printStackTrace();
                            }
                            panel.removeAllComponents();
                            panel.addComponent(successo);
                            MainSchermata.Schermata(panel);
                            flag=false;
                            break;
                        }

                    } catch (IOException ex) {
                        System.out.println("Errore nella lettura del messaggio:  " + ex);
                    }

                    // Se il server invia un comando /quit mi disconnetto dal server
                    // il flag serve per evitare che venga eseguita anche questa parte se ha eseguito quella sopra.
                    if (flag!=false) {
                        if (message.equals("/quit") || message == null) {

                            Label successo = new Label("\n- - - Server left - - -").setBackgroundColor(BLACK)
                                    .setForegroundColor(RED);
                            try {
                                connectedClients.remove(name);
                                Client.socket.close();

                            } catch (SocketException e) {
                                //e.printStackTrace();
                            } catch (IOException e) {
                                //e.printStackTrace();
                            }
                            panel.removeAllComponents();
                            panel.addComponent(successo);
                            MainSchermata.Schermata(panel);
                            break;

                            // Se arriva il messaggio /start...
                        } else if (message.equals("/start")) {
                            try {

                                panel.removeAllComponents();
                                panel.setFillColorOverride(BLACK);
                                MainSchermata.screen.close();

                                // chiudo eventuali schermi
                                if (YouWin.nextGame) {
                                    try {
                                        YouWin.nextGame = false;
                                        YouWin.screen.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (GameOver.nextGame) {
                                    try {
                                        GameOver.nextGame = false;
                                        GameOver.screen.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (Terminate.termina) {
                                    try {
                                        Terminate.termina = false;
                                        Terminate.screen.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                connectedClients.clear();

                                // Ottengo dal server i nomi di tutti i giocatori.
                                String playersStringMessage = fromServer.readLine();
                                connectedClients.addAll(Arrays.asList(playersStringMessage.split("-")));
                                try {
                                    // Faccio partire il gioco
                                    Thread.currentThread().sleep(500);
                                    Schermo schermo = new Schermo(toServer, name, IP, PORT, panel, coloreLabel, connectedClients);
                                    Gioco(fromServer,toServer,rs,schermo);
                                    break;
                                } catch (IOException e) {
                                     e.printStackTrace();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else {
                            // Se il messaggio non è nullo lo stampo
                            Label lab_clientMsg = new Label(message).setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                            panel.addComponent(lab_clientMsg);
                        }
                    }
                }
            }
            
            // Se il Nickname è un duplicato mi disconnetto
            if(terminate) {
                try {
                    Terminate termina = new Terminate(panel, coloreLabel);
                    Thread terminateThread = new Thread(termina);
                    terminateThread.start();
                    socket.close(); //Chiudi la connessione
                    senderThread.interrupt();
                    try {
                        MainSchermata.screen.close();
                    } catch (IOException e) {
                       // e.printStackTrace();
                    }
                } catch (IOException ex) {
                    //ex.printStackTrace();
                } finally {
                    try {
                        socket.close(); //Chiudi la connessione
                    } catch (IOException e) {
                       // e.printStackTrace();
                    }
                    senderThread.interrupt();
                }

            }

        } catch (IOException | InterruptedException ex) {
            Label nonconnesso = new Label("\n- - - - Connection failed try again - - - -\n").setBackgroundColor(BLACK)
                    .setForegroundColor(RED);
            panel.removeAllComponents();
            panel.addComponent(nonconnesso);
            panel.setFillColorOverride(BLACK);
            MainSchermata.Schermata(panel);
        }
    }

    public void Gioco(BufferedReader fromServer, PrintWriter toServer, RiceviStato rs,Schermo schermo){

        // Faccio partire il Thread del gioco
        gameThread = new Thread(schermo);
        gameThread.start();
        Schermo.gameOver = false;

        // Finchè non perdo o vinco...
        while (!Schermo.gameOver) {
            try {
                playersData = fromServer.readLine();

                // Se il messaggio contiene ":0" significa che sono coordinate provenienti dai client
                if (playersData.contains(":0") || playersData.contains(":2")) {
                    rs.run(playersData);
                }

                // Se il messaggio contiene la parola "spazzatura" so che dovrò aggiungere righe spazzatura
                // in base al numero finale del messaggio
                else if (playersData.contains("spazzatura")) {

                    // Quindi divido il messaggio e aggiungo righe spazzatura in base a quanto dice il
                    // messaggio ricevuto
                    String arr[] = playersData.split("-");

                    // Controllo che le righe spazzatura siano indirizzate a me
                    if (arr[1].equals(name)) {
                        if (arr[2].equals("2")) {
                            Schermo.aggiungiSpazzatura = 1;
                            System.out.println("Spazzatura aggiunta: " + Schermo.aggiungiSpazzatura);
                        } else if (arr[2].equals("3")) {
                            Schermo.aggiungiSpazzatura = 2;
                            System.out.println("Spazzatura aggiunta: " + Schermo.aggiungiSpazzatura);

                            // Se non è ne 1 ne 2 ne 3 allora invierò 4 righe perché tanto meno non possono essere
                            // se no sarebbe ricaduto in uno dei casi precedenti
                        } else {
                            Schermo.aggiungiSpazzatura = 4;
                            System.out.println("Spazzatura aggiunta: " + Schermo.aggiungiSpazzatura);
                        }
                    }
                    // Se arriva il messaggio di sconfitta controllo quale utente abbia perso
                    // e lo scrivo sul suo schermino
                } else if (playersData.contains("-lost")) {
                    String arr[] = playersData.split("-");
                    if (!arr[0].equals(name)) {
                        Schermo.lost(arr[0]);
                    }
                    
                    //Se arriva una messaggio che contiene "/pause" e non si è già in pause, stoppa il gioco
                } else if (playersData.contains("/pause") && !pause) {
                    pause = true;
                    gameThread.suspend();
                    
                    //Se arriva un messaggio che contiene "/resume" e si è in pausa fa ripartire il gioco
                } else if (playersData.contains("/resume") && pause) {
                    pause = false;
                    gameThread.resume();

                    // Faccio ripartire istantaneamente un'altra partita
                } else if (playersData.equals("/startagain") && !pause) {
                    try {
                        gameThread.stop();
                        Schermo.screen.stopScreen();
                        Schermo.screen.close();
                        Schermo schermo1 = new Schermo(toServer, name, IP, PORT, panel, coloreLabel, connectedClients);
                        Gioco(fromServer, toServer, rs, schermo1);

                    } catch (IOException e) {
                        //e.printStackTrace();
                    }

                    // Annullo la partita e disconnetto il client
                }else if(playersData.equals("/restart")){
                    try {
                        Schermo.screen.stopScreen();
                        Schermo.screen.close();
                        gameThread.stop();
                        socket.close();
                        String[] args = new String[0];
                        MainSchermata.main(args);
                        panel.addComponent(new EmptySpace(new TerminalSize(0, 0))); // Empty space underneath labels
                        break;
                    } catch (SocketException e) {
                       // e.printStackTrace();
                    } catch (IOException e) {
                       e.printStackTrace();
                    }

                    // Se il server manda un messaggio /quit interrompo la partita e mi disconnetto
                } else if (playersData.equals("/quit")) {
                    try {
                        Schermo.screen.stopScreen();
                        Schermo.screen.close();
                        socket.close();
                        gameThread.stop();
                        connectedClients.clear();
                        panel.addComponent(new EmptySpace(new TerminalSize(0, 0)));
                        String[] args = new String[0];
                        MainSchermata.main(args);
                        break;
                    } catch (SocketException e) {
                        //e.printStackTrace();
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }

                    // Se arriva il messaggio di vittoria interrompo il Thread e faccio
                    // partire la schermata di vittoria da Schermo
                } else if (playersData.equals("[" + name + "]-winner")) {
                    winner = true;
                    connectedClients.clear();
                    winCheck = true;
                    while (winCheck) {

                    }
                    Thread.currentThread().interrupt();
                    break;

                }

                // Se perdo interrompo il Thread e faccio partire la schermata
                // di sconfitta da Schermo
                if (Schermo.gameOver) {
                    connectedClients.clear();
                    loseCheck = true;
                    while (loseCheck) {

                    }
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (IOException e) {
               // e.printStackTrace();
            }
        }

    }
}
