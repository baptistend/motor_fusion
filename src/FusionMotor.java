import fr.dgac.ivy.IvyException;
import fusion.Fusion;
import geste.RecoGeste;
import vocale.vocal_ivy;

public class FusionMotor {
    public static void main(String[] args) throws IvyException {
        new vocal_ivy("127.255.255.255:2010");
        new Fusion("127.255.255.255:2010");
        new RecoGeste("127.255.255.255:2010");

    }
}
