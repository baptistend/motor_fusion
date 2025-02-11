package sra5;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
public class sra5 {
    public static void main(String[] args) {
        try {
            // Chemin relatif vers le fichier .bat
            String batchFilePath = new File("sra5_on.bat").getAbsolutePath();
            System.out.println("Batch file path: " + batchFilePath);
            // Lancer le fichier batch
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", batchFilePath);

            // Démarrer le processus
            Process process = processBuilder.start();

            // Lire la sortie du script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Attendre la fin du processus
            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
