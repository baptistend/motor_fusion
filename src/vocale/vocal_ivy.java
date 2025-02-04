package vocale;

import fr.dgac.ivy.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class vocal_ivy extends JFrame {
    private Ivy bus;
    private JLabel target = new JLabel("Passerelle Ivy/Dessert vocal");
    private DefaultListModel<String> listModel; // Modèle de liste pour les plats
    private JList<String> platsList; // Liste graphique des plats

    public vocal_ivy(String adresse) { // Constructeur
        super("Passerelle Ivy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Crée un modèle et une liste pour afficher les plats
        listModel = new DefaultListModel<>();
        platsList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(platsList);
        platsList.setFont(new Font("Arial", Font.PLAIN, 16));

        // Ajoute les éléments graphiques
        add(target, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Initialisation du bus Ivy
        bus = new Ivy("vocal_ivy", "", null);
        try {
            bus.start(adresse); // lancement du bus
        } catch (IvyException ie) {
            System.out.println("Erreur : " + ie);
        }

        try {
            // Gestion des messages de reconnaissance vocale
            bus.bindMsg("^sra5 Text=(.*) Confidence=(.*)", new IvyMessageListener() {
                public void receive(IvyClient client, String[] args) {

                    System.out.println("Message reçu : "+ args[0] + "/" + args[1]);
                    try {
                        bus.sendMsg("Geste:RecoVoc designation="+"COULEUR");
                    } catch (IvyException ie) {
                        System.out.println("Erreur lors de l'envoi du message vocal.");
                    }


                }
            });

            // Gestion des événements de rejet de la parole
            bus.bindMsg("^sra5 Event=SpeechRejected", new IvyMessageListener() {
                public void receive(IvyClient client, String[] args) {
                    try {
                        bus.sendMsg("ppilot5 Say=J'ai été distrait, je ne vous ai pas compris");
                    } catch (IvyException ie) {
                        System.out.println("Erreur lors de l'envoi du message vocal.");
                    }
                }
            });
        } catch (IvyException ie) {
            System.out.println("Erreur lors de la liaison des messages : " + ie);
        }

        // Affichage de la fenêtre
        setSize(400, 300);
        setVisible(true);
    }

    public static void main(String[] arg) {
        new vocal_ivy("127.255.255.255:2010");
    }
}
