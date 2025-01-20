package fusion;

import java.awt.*;

public class Commande {
    String action, objet;
    int posX, posY;
    Color couleur;
    int mouseX, mouseY;
    Designation designation;
    public Commande(){
        action = null;
        objet = null;
        posX = -1;
        posY = -1;
        couleur = null;
    }
}
