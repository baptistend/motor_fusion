import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyException;

public class Main {
    public static void main(String[] args) throws IvyException {

        Ivy bus = new Ivy("TestIvy", "TestIvy Ready", null);
        bus.bindMsg("^Bonjour(.*)", (client, arguments) -> {
            System.out.println("Réponse reçue : " + arguments[0]);
        });

        bus.start("localhost:2000"); // Démarre sur le domaine par défaut
        bus.sendMsg("Hello Test");
        System.out.println("Hello world!");
    }
}