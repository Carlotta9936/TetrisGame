package com.company;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;

import com.googlecode.lanterna.*;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.util.Arrays;

import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static com.googlecode.lanterna.TextColor.ANSI.BLACK;
import static com.googlecode.lanterna.TextColor.ANSI.BLUE;

public class MainSchermata {
    public static void main(String[] args) throws IOException {

        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

        // width will store the width of the screen
        int width = (int)size.getWidth();

        // height will store the height of the screen
        int height = (int)size.getHeight();

        int leftMargin = width/19;
        int topMargin = height/50;

        //codice per avere uno chermo
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        final Screen screen = new TerminalScreen(terminal);
        screen.startScreen();


        // Creo pannello
        final Panel panel = new Panel();
        panel.setLayoutManager(
                new GridLayout(1)
                        .setLeftMarginSize(leftMargin)
                        .setRightMarginSize(0)
                        .setTopMarginSize(topMargin));
        panel.setFillColorOverride(BLACK);
        panel.addComponent(new EmptySpace(new TerminalSize(0,0))); // Empty space underneath labels
        // richiamo metodo per avviare la grafica
        Schermata(panel);


        BasicWindow window = new BasicWindow();
        // importante

        window.setHints(Arrays.asList(Window.Hint.FULL_SCREEN));
        window.setComponent(panel);



        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(BLACK));
        gui.addWindowAndWait(window);
    }
    //codice scritta bella colorata tetris
    public static void Tetris(Panel panel){
        Label tetris1=new Label("\n ______  ____ ______ ____  __  __ \n").setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.RED);
        Label tetris2=new Label(" | || | ||    | || | || \\\\ || (( \\\n" ).setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.RED_BRIGHT);
        Label tetris3=new Label("   ||   ||==    ||   ||_// ||  \\\\ \n").setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.GREEN_BRIGHT);
        Label tetris4=new Label("   ||   ||___   ||   || \\\\ || \\_))\n").setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.BLUE_BRIGHT);

        panel.addComponent(tetris1);
        panel.addComponent(tetris2);
        panel.addComponent(tetris3);
        panel.addComponent(tetris4);
    }

    // separatore di dimensioni pari a size
    public static void Empty(Panel panel, int size){
        panel.addComponent(new EmptySpace(new TerminalSize(0,size)));
    }

    //codice home
    public static void Schermata(final Panel panel){
        final TextColor coloreLabel=TextColor.ANSI.GREEN_BRIGHT;
        // Label testo=new Label("Benvenuto in:\n").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
        // panel.addComponent(testo);

        Tetris(panel);

        Empty(panel, 1);

        //bottone client
        new Button("Find Game", new Runnable() {
            @Override
            public void run() {
                panel.removeAllComponents();
                panel.setFillColorOverride(BLACK);
                //solita scrittina bellina
                Tetris(panel);

                //registrazione utente
                Label user=new Label("\nName: ").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                TextBox textUser=new TextBox();// nome nuovo client
                Empty(panel, 1);
                Label IP=new Label("\nServer IP: ").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                TextBox textIP=new TextBox();//indirizzo IP del server a cui voglio collegarmi

                panel.addComponent(user);
                panel.addComponent(textUser);
                panel.addComponent(IP);
                panel.addComponent(textIP);

                Empty(panel, 1);
                //bottone che mi riporta alla home
                new Button("Indietro",new Runnable(){
                    @Override
                    public void run(){
                        panel.removeAllComponents();
                        panel.setFillColorOverride(BLACK);
                        Schermata(panel);
                    }
                }).addTo(panel);

            }
        }).addTo(panel);

        Empty(panel, 1);

        //bottone per accedere come server
        new Button("Host Game", new Runnable() {

            public void run() {

                //  System.out.println("sto eseguendo");
                panel.removeAllComponents();
                panel.setFillColorOverride(BLACK);
                //metodo per la scritta tetris
                Tetris(panel);

                //registro il nome del server


                Label user = new Label("\nName: ").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                final TextBox textUser = new TextBox();
                panel.addComponent(user);
                panel.addComponent(textUser);

                Empty(panel, 1);

                new Button("Start Server",new Runnable(){
                    @Override
                    public void run(){
                        String name = textUser.getText();
                        panel.removeAllComponents();
                        panel.setFillColorOverride(BLACK);
                        //dovrei chiamare il codice del server

                        Server server = new Server(name,panel,coloreLabel);
                        server.StartServer(server);

                    }
                }).addTo(panel);

                //System.out.println(" ciao");
                //qui ci proviamo a mettere un bel semaforino
                //server

                Empty(panel, 1);

                //bottone per tornare alla home
                new Button("Indietro",new Runnable(){
                    @Override
                    public void run(){
                        panel.removeAllComponents();
                        panel.setFillColorOverride(BLACK);
                        Schermata(panel);
                    }
                }).addTo(panel);

            }
        }).addTo(panel);

        Empty(panel, 2);

        Label separatore =new Label("~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~^~").setBackgroundColor(BLACK).setForegroundColor(TextColor.ANSI.WHITE);
        panel.addComponent(separatore);

        Empty(panel, 2);

        new Button("Rules", new Runnable(){
            @Override
            public void run() {
                panel.removeAllComponents();
                panel.setFillColorOverride(BLACK);
                Label regolamento = new Label("Regolamento:\n\nUna partita è formata da massimo 8 giocatori.\nVince chi resta più tempo in partita. ").setBackgroundColor(BLACK).setForegroundColor(coloreLabel);
                panel.addComponent(regolamento);

                Empty(panel, 1);
                //bottone per tornare alla home
                new Button("Indietro",new Runnable(){
                    @Override
                    public void run(){
                        panel.removeAllComponents();
                        panel.setFillColorOverride(BLACK);
                        Schermata(panel);
                    }
                }).addTo(panel);
            }

        }).addTo(panel);

        Empty(panel, 1);

        //bottone per accedere al regolamento e ai crediti(sarebbe carino che se un client sta guardando il regolamento
        // il server non possa avviare il gioco)
        new Button("Credits", new Runnable(){
            @Override
            public void run() {
                panel.removeAllComponents();
                panel.setFillColorOverride(BLACK);

                Label crediti=new Label("\nQuesto gioco è stato realizzato da:\n\n\nGabriel Riccardo Alsina,\n\nCarlotta Carboni,\n\nLuca Palmieri,\n\nAlssandro Pasi.").setBackgroundColor(BLACK).setForegroundColor
                        (coloreLabel);

                panel.addComponent(crediti);
                Empty(panel, 1);
                //bottone per tornare alla home
                new Button("Indietro",new Runnable(){
                    @Override
                    public void run(){
                        panel.removeAllComponents();
                        panel.setFillColorOverride(BLACK);
                        Schermata(panel);
                    }
                }).addTo(panel);
            }

        }).addTo(panel);

    }

}
