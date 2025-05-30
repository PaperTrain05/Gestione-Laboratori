package it.paper.gestore_lab.manager;

import it.paper.gestore_lab.object.Utente;
import it.paper.gestore_lab.utils.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestioneUtenti {
    private List<Utente> utentiCache = new ArrayList<>();

    public List<Utente> getUtentiCache() {
        return utentiCache;
    }

    public void caricaUtenti(String directoryPath) {
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


    public Utente getUtenteByName(String nome) {
        for (Utente u : utentiCache) {
            if (u.getNome().equalsIgnoreCase(nome)) return u;
        }

        return null;
    }

    public void salvaUtente(Utente utente, String directoryPath) {
        String fileName = directoryPath + File.separator + utente.getNome() + ".txt";

        String content = "nome: " + utente.getNome() + "\n" +
                "password: " + utente.getPassword() + "\n" +
                "admin: " + (utente.isAdmin() ? "True" : "False");

        FileUtils.writeToFile(fileName, content);
    }
}
