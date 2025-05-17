package it.paper.gestore_lab.utils;

import java.io.*;

public class FileUtils {

    public static void writeToFile(String filePath, String content) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean validaIP(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4)
            return false;
        try {
            for (String s : parts) {
                int num = Integer.parseInt(s);
                if (num < 0 || num > 255)
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static void createDirs(String basePath) {
        new File(basePath + File.separator + "utenti").mkdirs();
        new File(basePath + File.separator + "laboratori").mkdirs();
        new File(basePath + File.separator + "prenotazioni").mkdirs();
    }
}
