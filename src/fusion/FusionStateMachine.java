package fusion;

import fr.dgac.ivy.*;

import java.awt.*;
import java.util.*;
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
    private boolean isCursorOnObject = false;
    private String selectedObject = "";
    private Point cursorCoordinates = null;

    private void startTimeout() {
        if (T1 != null) {
            T1.cancel();
        }
        T1 = new Timer();
        T1.schedule(new TimerTask() {
            @Override
            public void run() {
                handleTimeout();
            }
        }, 2000); // 2 secondes
    }

    private void handleTimeout() {
        System.out.println("Timeout ! Retour à l'état Idle.");
        state = State.Idle;
    }
    public FusionStateMachine() throws IvyException {
        this.state = State.Idle;
        bus = new Ivy("Fusion", "Fusion Ready", null);
        bus.start("127.255.255.255:2010");


        //bind to reco2D
        bus.bindMsg("^Geste:Forme nom=(.*)", (client, args) -> {
            String shape = args[0];
            System.out.println("Forme reconnue : " + args[0]);
            switch (state) {
                case Idle:
                    switch(shape){
                        case "cercle":

                            dessinerCercle(100,100, "yellow");
                            this.state = State.Create;
                            startTimeout();
                            break;
                        case "rectangle":
                            dessinerRectangle(90,90, "red");
                            this.state = State.Create;
                            startTimeout();

                            break;
                        case "deplacer":
                            this.state = State.Move;
                            System.out.println("Changement d'état vers Move");
                            break;
                        default:
                            break;
                    }
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
                    this.state = State.Move;
                    break;
                case Move:
                    System.out.println("Retour à l'état Idle");
                    this.state = State.Idle;
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
             String designation = args[0];

            switch (state) {
                case Idle:
                    System.out.println("Rien a faire si reco-vocal quand State=Idle.");
                    break;
                case Move:
                    //Faire un test sur la designation pour savoir si c'est "OBJET"
                    if (designation.equals("objet") && isCursorOnObject ){
                        System.out.println("Changement d'état vers Selected");
                        this.state = State.Selected;

                    }

                    break;
                case Create:
                    /* DEUX POSSIBILITES : Vers couleur ou vers position
                    *   1. Ecouter la designation pour savoir si c'est "COULEUR" ou "POSITION"
                    *   Timeout => Idle
                    *   2. Si c'est "COULEUR", changer l'état en "Color"
                    *   3. Si c'est "POSITION", changer l'état en "Position"
                    *   4
                    * */
                    break;
                case Color:
                    /* Regarder la position du curseur (sur objet)
                    *  Écouter la designation
                    *  Si Timeout => Idle
                    *  Si c'est COULEUR changer la couleur de l'objet
                    *  Reveneir à Idle
                    * */
                    break;
                case Position:
                    /* Regarder la position du curseur (sur canva)
                     *  Écouter la designation
                     *  Timeout => Idle
                     *  Si c'est POSITION, changer la position de l'objet
                     *  Revenir à Idle
                     * */
                    break;
                case Selected:
                    /* Regarder la position du curseur (sur canva)
                     *  Écouter la designation
                     *  Timeout => Idle
                     *  Si c'est POSITION, changer la position de l'objet
                     *  Changer vers l'état Position
                     * */
                    if (designation.equals("position") && !isCursorOnObject ){
                        moveItem(selectedObject, cursorCoordinates);
                        this.state = State.Position;
                        System.out.println("Changement d'état vers Position");
                    }


                    break;
                default:
                    System.out.println("Geste ignoré dans l'état actuel.");
                    break;
            }
        });

        //Reco palette
        //S'envoie dans tous les cas après un test de point
        bus.bindMsg("Palette:FinTesterPoint x=(\\d+) y=(\\d+)",  (client, args) -> {
            //dessinerCercle(Integer.parseInt(args[0]), Integer.parseInt(args[1]), "green");
        });

        //S'envoie que si un objet est bien sous le curseur
        bus.bindMsg("Palette:ResultatTesterPoint x=(\\d+) y=(\\d+) nom=(.*)", (client, args) -> {
            //dessinerCercle(args, "green");
            isCursorOnObject = true;
            selectedObject = args[2];
            //TODO get name and store it
        });

        bus.bindMsg("Palette:MouseMoved x=(\\d+) y=(\\d+)", ((client, args) -> {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);

            try {
                isCursorOnObject = false;
                cursorCoordinates = new Point((int)x,(int)y);
                bus.sendMsg("Palette:TesterPoint x=" + (int)x + " y=" + (int)y);
            } catch (IvyException e) {
                throw new RuntimeException(e);
            }

        }));

    }

    private void moveItem(String selectedObject, Point cursorCoordinate) {
        String moveMsg = String.format("Palette:DeplacerObjetAbsolu nom=%s x=%d y=%d ", selectedObject, (int)cursorCoordinate.getX() , (int)cursorCoordinate.getY());
        try {

            bus.sendMsg(moveMsg);
            System.out.println("deplacer");
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    private void handeTimeout(){

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

        String circleMsg = String.format("Palette:CreerEllipse x=%d y=%d longueur=25 hauteur=25 couleurFond=%s", x - 5, y - 5, couleur);

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
