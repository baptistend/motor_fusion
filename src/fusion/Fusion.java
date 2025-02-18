package fusion;

import fr.dgac.ivy.*;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;

public class Fusion implements IvyMessageListener {
    private Ivy bus;
    enum State {
        Idle,
        Create,
        Move,
        Selected
    }
    private State state;
    private Timer T1; // Timer to handle delay before returning to Idle
    private boolean isCursorOnObject = false;
    private String selectedObject = "";
    private Point cursorCoordinates = null;
    private Point rectangleSize = new Point(200,120);
    private Point circleSize = new Point(100,100);
    private int offset = 5;


    public Fusion(String adresse) throws IvyException {

        this.state = State.Idle;
        bus = new Ivy("Fusion", "Fusion Ready", null);
        bus.start(adresse);
        bus.addBindListener(busBinding);
        initBusBinding();
    }

    IvyBindListener busBinding = new IvyBindListener() {
        @Override
        public void bindPerformed(IvyClient client, int id, String regexp) {
            try {
                bus.sendMsg("Palette:SupprimerTout");
            } catch (IvyException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void unbindPerformed(IvyClient client, int id, String regexp) {

        }

    };

    /**
     * Initialize bus binding
     * @throws IvyException
     */
    private void initBusBinding() throws IvyException {
        //bind to reco2D
        bus.sendMsg("Palette:SupprimerTout");

        bus.bindMsg("^Geste:Forme nom=(.*)", (client, args) -> {
            String shape = args[0];
            handleShapeReceived(shape);

        });

        bus.bindMsg("^Geste:Deplacer", (client, args) -> {
            handleMoveReceived();
        });

        //Reco vocale
        bus.bindMsg("^Geste:RecoVoc designation=(.*)", (client, args) -> {
            String designation = args[0];
            handleVocalRecognition(designation);

        });


        //S'envoie que si un objet est bien sous le curseur
        bus.bindMsg("Palette:ResultatTesterPoint x=(\\d+) y=(\\d+) nom=(.*)", (client, args) -> {
            isCursorOnObject = true;
            selectedObject = args[2];
        });

        bus.bindMsg("Palette:MouseMoved x=(\\d+) y=(\\d+)", ((client, args) -> {
            if( args.length < 2){
                return;
            }
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
        }, 5000); // 5 secondes
    }

    private void handleTimeout() {
        System.out.println("Timeout ! Retour à l'état Idle.");
        switch (state) {
            case Idle:
                state = State.Idle;
                break;
            case Move:
                state = State.Idle;
                break;
            case Create:
                state = State.Idle;
                break;

            case Selected:
                state = State.Idle;
                break;
            default:
                break;
        }
    }

    private void moveItem(String selectedObject, Point cursorCoordinate) {
        String moveMsg = String.format("Palette:DeplacerObjetAbsolu nom=%s x=%d y=%d ", selectedObject, (int)cursorCoordinate.getX() -offset , (int)cursorCoordinate.getY() -offset);
        try {

            bus.sendMsg(moveMsg);
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle vocal recognition success
     * @param designation
     */
    private void handleVocalRecognition(String designation){
        switch (state) {
            case Idle:
                System.out.println("Rien a faire si reco-vocal quand State=Idle.");
                break;
            case Move:
                //Faire un test sur la designation pour savoir si c'est "OBJET"
                if (designation.equals("objet") && isCursorOnObject ){
                    System.out.println("Changement d'état vers Selected");
                    this.state = State.Selected;
                    startTimeout();
                }
                break;
            case Create:
                /* DEUX POSSIBILITES : Vers couleur ou vers position
                 *   1. Ecouter la designation pour savoir si c'est "COULEUR" ou "POSITION"
                 *   Timeout => Idle
                 * */
                if (isValidColor(designation) && isCursorOnObject){
                    this.state = State.Create;
                    //change color
                    String msg = String.format("Palette:ModifierCouleur nom=%s couleurFond=%s" , selectedObject,  designation);
                    try {
                        bus.sendMsg(msg);
                    } catch (IvyException e) {
                        throw new RuntimeException(e);
                    }
                    T1.cancel();
                    startTimeout();
                } else if (designation.equals("position") && !isCursorOnObject){
                    //change position
                    moveItem(selectedObject, cursorCoordinates);
                    this.state = State.Create;
                    T1.cancel();
                    startTimeout();
                }
                break;


            case Selected:
                /* Regarder la position du curseur (sur canva)
                 *  Écouter la designation
                 *  Timeout => Idle
                 *  Si c'est POSITION, changer la position de l'objet
                 * */
                if (designation.equals("position") && !isCursorOnObject ){
                    moveItem(selectedObject, cursorCoordinates);
                    this.state = State.Idle;
                    T1.cancel();
                    System.out.println("Changement d'état vers Idle");
                }


                break;
            default:
                System.out.println("Geste ignoré dans l'état actuel.");
                break;
        }
    }

    /**
     * Handle move received
     */
    private void handleMoveReceived(){
        switch (state) {
            case Idle:
                this.state = State.Move;
                startTimeout();
                break;
            case Move:
                System.out.println("Retour à l'état Idle");
                this.state = State.Idle;
                break;
            case Create:
                System.out.println("Impossible de bouger quand State=Create.");
                break;


            case Selected:
                System.out.println("Impossible de bouger quand State=Selected.");
                break;
            default:
                System.out.println("Geste ignoré dans l'état actuel.");
                break;
        }
    }

    /**
     * Handle shape recognized
     * @param shape
     */
    private void handleShapeReceived(String shape){
        switch (state) {
            case Idle:
                switch(shape){
                    case "cercle":
                        dessinerCercle( "yellow");
                        this.state = State.Create;
                        startTimeout();
                        break;
                    case "rectangle":
                        dessinerRectangle( "red");
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


            case Selected:
                System.out.println("Impossible de créer une forme quand State=Selected.");
                break;
            default:
                System.out.println("Geste ignoré dans l'état actuel.");
                break;
        }
    }

    /**
     * draw a rectangle
     * @param color
     */
    private void dessinerRectangle( String color) {
        if (cursorCoordinates == null) {
            System.out.println("Cursor coordinates are null");
            return;
        }
        String circleMsg = String.format("Palette:CreerRectangle x=%d y=%d longueur=%d hauteur=%d couleurFond=%s", (int)cursorCoordinates.getX() , (int)cursorCoordinates.getY() ,
                (int)rectangleSize.getX(), (int)rectangleSize.getY(), color);
        try {

            bus.sendMsg(circleMsg);
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }

    /**
     * draw a circle
     * @param couleur
     */
    private void dessinerCercle( String couleur) {
        if (cursorCoordinates == null) {
            System.out.println("Cursor coordinates are null");
            return;
        }
        String circleMsg = String.format("Palette:CreerEllipse x=%d y=%d longueur=%d hauteur=%d couleurFond=%s", (int)cursorCoordinates.getX() - offset, (int)cursorCoordinates.getY() - offset ,(int)circleSize.getX(), (int)circleSize.getY(), couleur);
        System.out.println(cursorCoordinates.getX() + " " + cursorCoordinates.getY());
        try {
            bus.sendMsg(circleMsg);
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void receive(IvyClient client, String[] args) {

    }

    /**
     * Check if the color is valid
     * @param designation
     * @return
     */
    public static boolean isValidColor(String designation) {
        try {
            Field field = Color.class.getField(designation.toLowerCase());
            return field.getType() == Color.class;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            new Fusion("127.255.255.255:2010");
        } catch (IvyException e) {
            e.printStackTrace();
        }
    }
}
