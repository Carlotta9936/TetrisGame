package com.company.Gioco;

import com.company.MainSchermata;
import com.company.client.Client;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static com.googlecode.lanterna.TextColor.ANSI.*;

//classe per la schermata di inizializzazione del gioco
public class Restart implements Runnable{

    private String username;
    private String IP;
    private int PORT;
    private Panel panel;
    private TextColor coloreLabel;
    public static Screen screen;
    public static Boolean nextGame = false;
    private List<String> connectedClients;

    public Restart(String name, String IP, int PORT, Panel panel, TextColor coloreLabel,List<String> connectedClients) {
        this.IP = IP;
        this.PORT = PORT;
        this.panel = panel;
        this.coloreLabel = coloreLabel;
        this.connectedClients=connectedClients;
        username = name;
    }

    @Override
    public void run() {

        try {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            // width will store the width of the screen
            int width = (int)size.getWidth();

            // height will store the height of the screen
            int height = (int)size.getHeight();

            int leftMargin = width/19;
            int topMargin = height/50;

            //codice per avere uno chermo
            final int COLS = width/8;
            final int ROWS = height/16;
            DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
            defaultTerminalFactory.setInitialTerminalSize(new TerminalSize(COLS, ROWS));
            Terminal terminal = defaultTerminalFactory.createTerminal();

            screen = new TerminalScreen(terminal);
            screen.startScreen();

            // Creo pannello
            panel.setLayoutManager(
                    new GridLayout(1)
                            .setLeftMarginSize(leftMargin)
                            .setRightMarginSize(0)
                            .setTopMarginSize(topMargin));
            panel.setFillColorOverride(BLACK);

            panel.addComponent(new EmptySpace(new TerminalSize(0,0)));

            //  panel.addComponent(new EmptySpace(new TerminalSize(0,0))); // Empty space underneath labels
            // richiamo metodo per avviare la grafica
            Schermata(panel);


            BasicWindow window = new BasicWindow();
            // importante
            window.setHints(Arrays.asList(Window.Hint.FULL_SCREEN));
            //window.setFixedSize((new TerminalSize(10,20)));

            window.setComponent(panel);

            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(BLACK));
            gui.addWindowAndWait(window);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //codice scritta colorata you win
    public void provaWin(Panel panel){

        Label provaWin1=new Label("\n  _____           _             _   _                   \n").setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.YELLOW_BRIGHT);
        Label provaWin2=new Label(" |  __ \\         | |           | | (_)                  \n").setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.YELLOW_BRIGHT);
        Label provaWin3=new Label(" | |__) |___  ___| |_ __ _ _ __| |_ _ _ __   __ _       ").setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.YELLOW_BRIGHT);
        Label provaWin4=new Label(" |  _  // _ \\/ __| __/ _` | '__| __| | '_ \\ / _` |      \n" ).setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.YELLOW_BRIGHT);
        Label provaWin5=new Label(" | | \\ \\  __/\\__ \\ || (_| | |  | |_| | | | | (_| |_ _ _ \n").setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.YELLOW_BRIGHT);
        Label provaWin6=new Label(" |_|  \\_\\___||___/\\__\\__,_|_|   \\__|_|_| |_|\\__, (_|_|_)\n").setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.YELLOW_BRIGHT);
        Label provaWin7=new Label("                                             __/ |      \n").setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.YELLOW_BRIGHT);
        Label provaWin8=new Label("                                            |___/       \n").setBackgroundColor(BLACK).setForegroundColor
                (TextColor.ANSI.YELLOW_BRIGHT);

        panel.addComponent(provaWin1);
        panel.addComponent(provaWin2);
        panel.addComponent(provaWin3);
        panel.addComponent(provaWin4);
        panel.addComponent(provaWin5);
        panel.addComponent(provaWin6);
        panel.addComponent(provaWin7);
        panel.addComponent(provaWin8);

        MainSchermata.Empty(panel, 1);

        new Button("Join lobby",new Runnable(){
            @Override
            public void run(){
                //svuoto la schermo
                panel.removeAllComponents();
                panel.setFillColorOverride(BLACK);
                nextGame = true;
                //richiamo schermata inziale
                Client client=new Client(username, IP, String.valueOf(PORT), panel, coloreLabel, connectedClients);
                MainSchermata.clientThread = new Thread(client);
                MainSchermata.clientThread.start();
            }
        }).addTo(panel);
    }

    // separatore di dimensioni pari a size
    public static void Empty(Panel panel, int size){
        panel.addComponent(new EmptySpace(new TerminalSize(0,size)));
    }

    //codice home
    public void Schermata(final Panel panel){
        final TextColor coloreLabel=TextColor.ANSI.GREEN_BRIGHT;

        provaWin(panel);
    }
}