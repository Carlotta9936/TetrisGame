package com.company.Gioco;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

public class Blocco {

    TextGraphics schermo; // Griglia nel terminale
    // TextGraphics quadrato; // Quadratino nel terminale
    private int coefColonna = 4; // Numero di quadratini che formano la larghezza di un Blocco nel terminale
    private int coefRiga = 2; // Numero di quadratini che formano l'altezza di un Blocco nel terminale

    TextColor colore;
    public int colonnaGriglia;
    public int rigaGriglia;
    int stato = 3; // 0=Vuoto - 1=Pezzo che sta scendendo - 2=Struttura  - 3=Spazzattura

    public Blocco(TextGraphics schermo, int colonnaGriglia, int rigaGriglia, TextColor colore, char simbolo) {
        this.schermo = schermo;
        this.colonnaGriglia = colonnaGriglia;
        this.rigaGriglia = rigaGriglia;
        this.colore = colore;
        try {
            Schermo.semaforoColore.acquire(); //serve per gestire l'accesso a "schermo.setForegroundColor" essendo una risorsa condivsa
            schermo.setForegroundColor(colore);
            schermo.fillRectangle(new TerminalPosition(colonnaGriglia * coefColonna+1, rigaGriglia * coefRiga+1), new TerminalSize(coefColonna, coefRiga), simbolo);
            Schermo.semaforoColore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Ritorna la posizione della colonna sullo schermo, coordinata * coefficente
    public int getColonna() { 
        return colonnaGriglia; 
    }

    // Ritorna la posizione della riga sullo schermo, coordinata * coefficente
    public int getRiga() {
        return rigaGriglia;
    }

    // Ritorna il colore del blocco
    public TextColor getColor() {
        return colore;
    }

    public int getStato() {
        return stato;
    }

    public boolean collisioneSotto(Griglia campo) {
        return this.getRiga() == 23 || campo.griglia[this.getColonna()][this.getRiga() + 1].getStato() > 1;
    }

    public boolean collisioneLaterale(Griglia campo, int spostamentoOrizzontale, int spostamentoVerticale) {
        return this.getColonna() + spostamentoOrizzontale < 0 || //Collisione con muro di sinistra
                this.getColonna() + spostamentoOrizzontale > 11 || //Collisione con muro di destra
                this.getRiga() + spostamentoVerticale < 0 || //Collisione con muro in alto
                this.getRiga() + spostamentoVerticale > 23 || //Collisione con muro in basso
                campo.griglia[this.getColonna() + spostamentoOrizzontale][this.getRiga() + spostamentoVerticale].getStato() >= 2; //Collisione con blocco struttura/spazzatura
    }

    public void muovi(Griglia campo, int colGriglia, int rigGriglia, int orizzontale, int verticale, TextColor colore) {

    }

    public void rimuovi(Griglia campo, int colGriglia, int rigGriglia) {

    }
}
