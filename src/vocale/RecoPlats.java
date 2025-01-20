package vocale;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;

public class RecoPlats  implements IvyMessageListener {
    private Ivy bus;

    public RecoPlats() throws IvyException {
        bus = new Ivy("InteractionVocale", "InteractionVocale Ready", null);
        bus.start("127.255.255.255:2010");

    }

    @Override
    public void receive(IvyClient client, String[] args) {
        System.out.println("Message reçu : " + String.join(" ", args));
    }
}
