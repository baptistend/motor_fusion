import fr.dgac.ivy.IvyException;
import fusion.FusionStateMachine;
import geste.RecoGeste;
import vocale.vocal_ivy;

public class Fusion {
    public static void main(String[] args) throws IvyException {
        new vocal_ivy("127.255.255.255:2010");
        new FusionStateMachine("127.255.255.255:2010");
        new RecoGeste("127.255.255.255:2010");

    }
}
