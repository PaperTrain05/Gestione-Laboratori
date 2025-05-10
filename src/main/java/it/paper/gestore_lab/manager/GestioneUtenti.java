package it.paper.gestore_lab.manager;

import it.paper.gestore_lab.Main;
import it.paper.gestore_lab.object.Utente;
import it.paper.gestore_lab.utils.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GestioneUtenti {
    public static List<Utente> utentiCache = new ArrayList<>();

    public static void caricaUtenti(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) return;
        for (File f : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String[] partsNome = br.readLine().split(":", 2);
                String nome = partsNome[1].trim();
                String[] partsPassword = br.readLine().split(":", 2);
                String password = partsPassword[1].trim();
                String[] partsAdmin = br.readLine().split(":", 2);
                String adminStr = partsAdmin[1].trim();
                boolean admin = adminStr.equalsIgnoreCase("True");
                utentiCache.add(new Utente(nome, password, admin));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void creaUtente() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Inserisci nome utente: ");
        String nome = sc.nextLine();
        System.out.print("Inserisci password: ");
        String password = sc.nextLine();
        System.out.print("L'utente è admin? (True/False): ");
        String adminStr = sc.nextLine();
        boolean admin = adminStr.equalsIgnoreCase("True");
        Utente nuovo = new Utente(nome, password, admin);
        utentiCache.add(nuovo);
        salvaUtente(nuovo, Main.BASE_PATH + File.separator + "utenti");
        System.out.println("Utente creato.");
    }

    public static Utente getUtenteByName(String nome) {
        for (Utente u : utentiCache) {
            if (u.getNome().equalsIgnoreCase(nome))
                return u;
        }
        return null;
    }

    public static void salvaUtente(Utente utente, String directoryPath) {
        String fileName = directoryPath + File.separator + utente.getNome() + ".txt";
        String content = "nome: " + utente.getNome() + "\n" +
                "password: " + utente.getPassword() + "\n" +
                "admin: " + (utente.isAdmin() ? "True" : "False");
        FileUtils.writeToFile(fileName, content);
    }
}
