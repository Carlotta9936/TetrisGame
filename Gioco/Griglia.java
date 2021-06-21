import com.googlecode.lanterna.graphics.TextGraphics;

public class Griglia {
    Blocco[][] griglia;
    TextGraphics screen;

    public Griglia(TextGraphics screen) {
        this.screen = screen;
        griglia = new Blocco[12][8];
    }

    public void creaCampo() {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 8; j++) {
                griglia[i][j] = new BloccoVuoto(screen, i, j);
            }
        }
    }
}
