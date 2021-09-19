package com.company.Gioco;

import com.company.Gioco.Mini.*;
import com.googlecode.lanterna.TextColor;
import java.util.concurrent.Semaphore;

import static com.company.Gioco.Schermo.*;


public class RiceviStato extends Thread{
    public static Semaphore traduzione = new Semaphore(1);

    public void run(String stringa) {
        traduciStringToInt(stringa);
    }

    public void traduciStringToInt (String stringa){
        try {
            traduzione.acquire();
            String[] s = stringa.split(":");
            String[] blocchi = s[1].split("");
            int index = 0;
            int[][] campoAvv = new int[12][24];

            for (int i = 0; i < 12; i++) {
                for (int e = 0; e < 24; e++) {
                    campoAvv[i][e] = Integer.parseInt(blocchi[index]);
                    index++;
                }
            }
            TextColor colore = getColore(s[2]);
            traduciIntToMiniGriglia(campoAvv, s[0], colore);
            traduzione.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void traduciIntToMiniGriglia(int[][] campo, String nome, TextColor colore) {

            int id = 0;

            for (int i = 0; i < miniCampo.length; i++) {
                if (nome.equals(miniCampo[i].nome)) {
                    id = i;
                    break;
                }
            }

            for (int i = 0; i < campo.length; i++) {
                for (int e = 0; e < campo[i].length; e++) {
                    switch (campo[i][e]) {
                        case 0 -> {
                            miniCampo[id].griglia[i][e] = new Mini_BloccoVuoto(schermo, i, e, id);
                            break;
                        }
                        case 1 -> {
                            miniCampo[id].griglia[i][e] = new Mini_BloccoPieno(schermo, i, e, colore, id);
                            break;
                        }
                        case 2 -> {
                            miniCampo[id].griglia[i][e] = new Mini_BloccoStruttura(schermo, i, e, id);
                            break;
                        }
                        case 3 -> miniCampo[id].griglia[i][e] = new Mini_BloccoSpazzatura(schermo, i, e, id);
                    }
                }
            }


    }

    public TextColor getColore(String colore){
        switch(colore){
            case "BLUE_BRIGHT": return TextColor.ANSI.BLUE_BRIGHT;
            case "MAGENTA_BRIGHT": return TextColor.ANSI.MAGENTA_BRIGHT;
            case "YELLOW_BRIGHT": return TextColor.ANSI.YELLOW_BRIGHT;
            case "RED_BRIGHT": return TextColor.ANSI.RED_BRIGHT;
            case "CYAN_BRIGHT": return TextColor.ANSI.CYAN_BRIGHT;
            case "GREEN_BRIGHT": return TextColor.ANSI.GREEN_BRIGHT;
        }

        return null;
    }
}
