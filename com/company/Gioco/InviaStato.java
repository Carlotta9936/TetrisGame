package com.Gioco;

import com.googlecode.lanterna.TextColor;

import java.io.PrintWriter;

public class InviaStato implements Runnable{
    int[][] miaGriglia = new int[12][24];
    PrintWriter pw;
    String username;
    TextColor colore;

    public InviaStato(String username, PrintWriter pw){
        this.pw=pw;
        this.username=username;
    }

    @Override
    public void run() {

    }

    public synchronized void run(Griglia griglia) {
        traduciGrigliaToInt(griglia);
    }

    public synchronized void traduciGrigliaToInt(Griglia griglia){
        for(int i=0; i<griglia.griglia.length; i++){
            for(int e=0; e<griglia.griglia[i].length; e++){
                switch (griglia.griglia[i][e].getStato()) {
                    case 0 :{miaGriglia[i][e]=0;
                        break;}
                    case 1 :{miaGriglia[i][e]=1;
                            colore=griglia.griglia[i][e].colore;
                        break;}
                    case 2 :{miaGriglia[i][e]=2;
                        break;}
                    case 3 : {
                        miaGriglia[i][e] = 3;
                        break;
                    }
                }
            }
        }
        traduciInttoString(miaGriglia);
    }

    public synchronized void traduciInttoString(int[][] mat){
        String stringa= username + "|";
        for(int i=0; i<12; i++){
            for(int e=0; e<24; e++){
                stringa=stringa+(mat[i][e]);
            }
        }
        stringa=stringa+"|"+colore;
        //System.out.println(stringa);
        Schermo.invia(stringa, pw);

    }
}
