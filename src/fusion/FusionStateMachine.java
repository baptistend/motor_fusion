package fusion;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;

import java.util.Timer;

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
        bus.start("127.255.255.255:2010");


        //bind to reco2D
        bus.bindMsg("^Geste:Forme nom=(.*)", (client, args) -> {
            //dessinerCercle(args, "green");
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

        });

        bus.bindMsg("^Geste:Deplacer", (client, args) -> {
            //dessinerCercle(args, "green");
        });

        //Reco vocale
        bus.bindMsg("^Geste:RecoVoc designation=(.*)", (client, args) -> {
            //dessinerCercle(args, "green");
        });

        //Reco palette
        bus.bindMsg("^Palette:ResultatTesterPoint x=(\\d+) y=(\\d+) nom=(.*)", (client, args) -> {
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
}
