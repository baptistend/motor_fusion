package fusion;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;

import java.util.Date;
import java.util.Timer;
import java.util.function.BiConsumer;

public class FusionStateMachine implements IvyMessageListener {
    private Ivy bus;
    enum State {
        Idle,
        Create,
        Color,
        Position,
        Move,
        Selected
    }
    private State state;
    private Timer T1;

    public FusionStateMachine() throws IvyException {
        this.state = State.Idle;
        bus = new Ivy("Fusion", "Fusion Ready", null);
        bus.start("127.0.0.1:2010");
        bus.sendToSelf(true);


        //bind to reco2D
        bus.bindMsg("^Geste:Forme nom=(.*)", (client, args) -> {
            switch (state) {
                case Idle:
                    switch(args[0]){
                        case "circle":
                            dessinerCercle(10,10, "black");
                            break;
                        case "rectangle":
                            dessinerRectangle(10,10, "black");
                            break;
                        default:
                            break;
                    }
                    updateState(State.Create);
                    break;
                case Move:
                    System.out.println("Impossible de créer une forme quand State=Move.");
                    break;
                case Create:
                    System.out.println("Impossible de créer une forme quand State=Create.");
                    break;
                case Color:
                    System.out.println("Impossible de créer une forme quand State=Color.");
                    break;
                case Position:
                    System.out.println("Impossible de créer une forme quand State=Position.");
                    break;
                case Selected:
                    System.out.println("Impossible de créer une forme quand State=Selected.");
                    break;
                default:
                    System.out.println("Geste ignoré dans l'état actuel.");
                    break;
            }
        });

        bus.bindMsg("^Geste:Deplacer", (client, args) -> {
            switch (state) {
                case Idle:
                    updateState(State.Move);
                    break;
                case Move:
                    System.out.println("Déjà dans State=Move.");
                    break;
                case Create:
                    System.out.println("Impossible de bouger quand State=Create.");
                    break;
                case Color:
                    System.out.println("Impossible de bouger quand State=Color.");
                    break;
                case Position:
                    System.out.println("Impossible de bouger quand State=Position.");
                    break;
                case Selected:
                    System.out.println("Impossible de bouger quand State=Selected.");
                    break;
                default:
                    System.out.println("Geste ignoré dans l'état actuel.");
                    break;
            }
        });

        //Reco vocale
        bus.bindMsg("^Geste:RecoVoc designation=(.*)", (client, args) -> {
            switch (state) {
                case Idle:
                    System.out.println("Rien a faire si reco-vocal quand State=Idle.");
                    break;
                case Move:
                    //Faire un test sur la designation pour savoir si c'est "OBJET"
                    break;
                case Create:
                    /* DEUX POSSIBILITES : Vers couleur ou vers position
                    *   1. Ecouter la designation pour savoir si c'est "COULEUR" ou "POSITION"
                    *   2. Si c'est "COULEUR", changer l'état en "Color"
                    *   3. Si c'est "POSITION", changer l'état en "Position"
                    * */
                    break;
                case Color:
                    /* Regarder la position du curseur (sur objet)
                    *  Écouter la designation
                    *  Si c'est COULEUR changer la couleur de l'objet
                    * */
                    break;
                case Position:
                    /* Regarder la position du curseur (sur canva)
                     *  Écouter la designation
                     *  Si c'est POSITION, changer la position de l'objet
                     * */
                    break;
                case Selected:
                    /* Regarder la position du curseur (sur canva)
                     *  Écouter la designation
                     *  Si c'est POSITION, changer la position de l'objet
                     *  Changer vers l'état Position
                     * */
                    break;
                default:
                    System.out.println("Geste ignoré dans l'état actuel.");
                    break;
            }
        });

        //Reco palette
        //S'envoie dans tous les cas après un test de point
        bus.bindMsg("Palette:FinTesterPoint x=(\\d+) y=(\\d+)",  (client, args) -> {
            System.out.println("FinTesterPoint x=" + args[0] + " y=" + args[1]);
            //dessinerCercle(Integer.parseInt(args[0]), Integer.parseInt(args[1]), "green");
        });

        //S'envoie que si un objet est bien sous le curseur
        bus.bindMsg("Palette:ResultatTesterPoint x=(\\d+) y=(\\d+) nom=(.*)", (client, args) -> {
            System.out.println("ResultatTesterPoint x=" + args[0] + " y=" + args[1] + " nom=" + args[2]);
            //dessinerCercle(args, "green");
        });

    }

    private void updateState(State newState){
        switch (newState){
            case Idle:
                break;
            case Create:
                break;
            case Color:
                break;
            case Position:
                break;
            case Move:
                break;
            case Selected:
                break;
        }
    }
    private void dessinerRectangle(int x, int y, String color) {
        String circleMsg = String.format("Palette:CreerRectangle x=%d y=%d longueur=10 hauteur=10 couleurFond=%s", x - 5, y - 5, color);
        try {
            bus.sendMsg(circleMsg);
            System.out.println("Cercle " + color + " créé autour du pointeur (" + x + ", " + y + ")");
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    private void dessinerCercle(int x, int y , String couleur) {

        String circleMsg = String.format("Palette:CreerEllipse x=%d y=%d longueur=10 hauteur=10 couleurFond=%s", x - 5, y - 5, couleur);
        try {
            bus.sendMsg(circleMsg);
            System.out.println("Cercle " + couleur + " créé autour du pointeur (" + x + ", " + y + ")");
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void receive(IvyClient client, String[] args) {

    }

    public static void main(String[] args) {
        try {
            new FusionStateMachine();
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }
}
