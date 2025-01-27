import fr.dgac.ivy.*;

import java.util.HashMap;
import java.util.Map;

public class InteractionPalette implements IvyMessageListener {

    private Ivy bus;

    public InteractionPalette() throws IvyException {
        // Initialisation du bus IVY
        bus = new Ivy("InteractionPalette", "InteractionPalette Ready", null);
        bus.start("127.0.0.1:2010");

        // Affiche tous les messages provenant de la Palette
        bus.bindMsg(".*", (client, args) -> {
            System.out.println("Message reçu : " + String.join(" ", args));
        });

        // Réception d'un clic pour dessiner un carré
        bus.bindMsg("^Palette:MouseClicked x=(\\d+) y=(\\d+)", (client, args) -> {
            int x = Integer.parseInt(args[0]);
            int y = Integer.parseInt(args[1]);
            String squareMsg = String.format("Palette:CreerRectangle x=%d y=%d largeur=50 hauteur=50 couleurFond=black", x - 25, y - 25);
            try {
                bus.sendMsg(squareMsg);
                System.out.println("Carré créé au centre du clic (" + x + ", " + y + ")");
            } catch (IvyException e) {
                e.printStackTrace();
            }
        });

        // Gestion des événements de souris pour dessiner des cercles
        bus.bindMsg("^Palette:MousePressed  x=(\\d+) y=(\\d+)", (client, args) -> {
            dessinerCercle(args, "green");
        });

        bus.bindMsg("^Palette:MouseDragged x=(\\d+) y=(\\d+)", (client, args) -> {
            dessinerCercle(args, "blue");
        });

        bus.bindMsg("^Palette:MouseReleased x=(\\d+) y=(\\d+)", (client, args) -> {
            dessinerCercle(args, "red");
            effacerTracesApresDelai();
        });

    }

    // Méthode pour dessiner un cercle
    private void dessinerCercle(String[] args, String couleur) {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        String circleMsg = String.format("Palette:CreerEllipse x=%d y=%d longueur=10 hauteur=10 couleurFond=%s", x - 5, y - 5, couleur);
        try {
            bus.sendMsg(circleMsg);
            System.out.println("Cercle " + couleur + " créé autour du pointeur (" + x + ", " + y + ")");
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour effacer les traces après un délai
    private void effacerTracesApresDelai() {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Délai de 1 seconde
                bus.sendMsg("Palette:SupprimerTout");
                System.out.println("Traces effacées.");
            } catch (InterruptedException | IvyException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void receive(IvyClient client, String[] args) {
        // Implémentation non utilisée directement
    }

    public static void main(String[] args) {
        try {
            new InteractionPalette();
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }
}
