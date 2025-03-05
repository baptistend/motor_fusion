package geste;

import fr.dgac.ivy.*;

import java.io.*;
import java.util.*;

/**
 * dessiner cercle = cercle
 * dessiner rectangle = rectangle
 * action deplacer = deplacer
 */
public class RecoGeste implements IvyMessageListener {
    private static final String FILE_PATH = "gestures.ser";
    private Stroke currentStroke;
    private Map<String, Stroke> gestureDictionary;

    private double treshold = 350.0;
    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    private Mode mode = Mode.RECONNAISSANCE;
    private Ivy bus;
    Scanner scanner = new Scanner(System.in);
    public RecoGeste(String adresse) throws IvyException{
        gestureDictionary = loadGestureDictionary();
        if (gestureDictionary == null) {
            gestureDictionary = new HashMap<>();
        }
        bus = new Ivy("RecoGeste", "RecoGeste Ready", null);

        bus.start(adresse);


        // Gestion des événements de souris pour dessiner des cercles
        bus.bindMsg("^Palette:MousePressed x=(\\d+) y=(\\d+)", (client, args) -> {
            currentStroke = new Stroke();
            if (mode == Mode.APPRENTISSAGE && args.length >= 2) {
                currentStroke.init(); // Réinitialisation du tracé

                currentStroke.addPoint(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            }
        });

        bus.bindMsg("^Palette:MouseDragged x=(\\d+) y=(\\d+)", (client, args) -> {
            if (mode == Mode.APPRENTISSAGE || mode == Mode.RECONNAISSANCE && args.length >= 2) {
                currentStroke.addPoint(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            }
        });

        bus.bindMsg("^Palette:MouseReleased x=(\\d+) y=(\\d+)", (client, args) -> {
            if (mode == Mode.APPRENTISSAGE) {
                apprendreGeste(); // Apprentissage du geste
            } else if (mode == Mode.RECONNAISSANCE) {
                try {
                    reconnaitreGeste(); // Reconnaissance du geste
                } catch (IvyException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    public void saveGestureDictionary() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(gestureDictionary);
            System.out.println("Dictionnaire sauvegardé dans " + FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour désérialiser et charger le dictionnaire
    @SuppressWarnings("unchecked")
    private Map<String, Stroke> loadGestureDictionary() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("Fichier de gestes non trouvé. Un nouveau dictionnaire sera créé.");
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            return (Map<String, Stroke>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }



    @Override
    public void receive(IvyClient client, String[] args) {

    }
    private void apprendreGeste() {
        System.out.println("Entrez le nom de la commande pour ce geste : ");
        try  {
            String commandName = scanner.nextLine();
            currentStroke.normalize();
            gestureDictionary.put(commandName, currentStroke);
            saveGestureDictionary();
            System.out.println("Geste appris et associé à la commande : " + commandName);
        } catch (Exception e ){
            e.printStackTrace();
        }
    }

    // Reconnaissance d'un geste
    private void reconnaitreGeste() throws IvyException {
        String recognizedCommand = null;
        double minDistance = Double.MAX_VALUE;
        if (currentStroke.isEmpty() ){
            return;
        }
        currentStroke.normalize();
        for (Map.Entry<String, Stroke> entry : gestureDictionary.entrySet()) {
            double distance = calculateDistance(currentStroke, entry.getValue());
            if (distance < minDistance) {
                minDistance = distance;
                recognizedCommand = entry.getKey();
            }
        }
        System.out.println("Distance minimale : " + minDistance);
        if (recognizedCommand != null && minDistance < treshold) {
            System.out.println("Geste reconnu : " + recognizedCommand + "distance :" + minDistance);
            envoyerCommandeReconnaissance(recognizedCommand);
        } else {
            System.out.println("Aucun geste reconnu.");
        }
    }

    private double calculateDistance(Stroke s1, Stroke s2) {
        double distance = 0.0;


        var points1 = s1.getPoints();
        var points2 = s2.getPoints();

        int size = Math.min(points1.size(), points2.size());
        for (int i = 0; i < size; i++) {
            distance += points1.get(i).distance(points2.get(i));
        }
        return distance;
    }

    // Envoi d'une commande reconnue sur le bus Ivy
    private void envoyerCommandeReconnaissance(String commandName) {
        try {
            bus.sendMsg("Geste:Forme nom=" + commandName);
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        try {
            new RecoGeste("127.255.255.255:2010");
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

}
