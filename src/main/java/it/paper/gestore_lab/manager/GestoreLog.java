package it.paper.gestore_lab.manager;

import it.paper.gestore_lab.Main;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GestoreLog {

    private static final String LOG_DIR = Main.BASE_PATH + File.separator + "logs";

    public static void logAction(String username, String action) {
        File logDirectory = new File(LOG_DIR);
        if (!logDirectory.exists()) {
            logDirectory.mkdirs(); // Crea la cartella se non esiste
        }

        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logFilename = LOG_DIR + File.separator + "log_" + currentDate + ".txt";

        String logEntry = currentDate + " - " + currentTime + " - " + username + " - ha fatto -> " + action;

        try (FileWriter fw = new FileWriter(logFilename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {
            pw.println(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
