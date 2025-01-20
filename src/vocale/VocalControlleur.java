package vocale;

import fr.dgac.ivy.IvyException;

import javax.swing.*;
import java.awt.*;

public class VocalControlleur {

    public static void main(String[] args) throws IvyException {
        // Crée une nouvelle fenêtre
        RecoPlats recoPlats = new RecoPlats();
        JFrame frame = new JFrame("Liste de plats");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        // Crée un panneau pour la liste
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Liste des plats
        String[] plats = {"Café", "Jus d'orange", "Crème brûlée", "Profiteroles", "Fraises"};

        // Ajoute chaque plat à la liste
        for (String plat : plats) {
            JLabel label = new JLabel(plat);
            label.setFont(new Font("Arial", Font.PLAIN, 16));
            panel.add(label);
        }

        // Ajoute la ScrollView contenant le panneau
        JScrollPane scrollPane = new JScrollPane(panel);
        frame.getContentPane().add(scrollPane);

        // Affiche la fenêtre
        frame.setVisible(true);
    }
}
