package it.paper.gestore_lab.manager;

import it.paper.gestore_lab.Main;
import it.paper.gestore_lab.object.Laboratorio;
import it.paper.gestore_lab.object.Prenotazione;
import it.paper.gestore_lab.object.Utente;
import it.paper.gestore_lab.utils.FileUtils;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GestorePrenotazione {
    public static List<Prenotazione> prenotazioniCache = new ArrayList<>();

    public static void caricaPrenotazioni(String directoryPath) {
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
                String[] partsLab = br.readLine().split(":", 2);
                String laboratorio = partsLab[1].trim();
                String[] partsOrario = br.readLine().split(":", 2);
                String orario = partsOrario[1].trim();
                String[] partsScaduto = br.readLine().split(":", 2);
                String scadutoStr = partsScaduto[1].trim();
                boolean scaduto = scadutoStr.equalsIgnoreCase("True");
                prenotazioniCache.add(new Prenotazione(nome, laboratorio, orario, scaduto));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean puoiPrenotare(Utente utente, Laboratorio lab, String orario) {
        LocalTime nuovoInizio = getInizio(orario);
        LocalTime nuovoFine = getFine(orario);

        for (Prenotazione p : prenotazioniCache) {
            if (p.getLaboratorio().equalsIgnoreCase(lab.getNome()) && !p.isScaduto()) {
                if (overlap(nuovoInizio, nuovoFine, p.getOrarioInizio(), p.getOrarioFine()))
                    return false;
            }
            if (p.getNomeUtente().equalsIgnoreCase(utente.getNome()) && !p.isScaduto()) {
                if (overlap(nuovoInizio, nuovoFine, p.getOrarioInizio(), p.getOrarioFine()))
                    return false;
            }
        }

        return true;
    }

    private static boolean overlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private static LocalTime getInizio(String orario) {
        // Usa lo split semplice perché l'orario passato è validato
        String[] parts = orario.split("-");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return LocalTime.parse(parts[0].trim(), formatter);
    }

    private static LocalTime getFine(String orario) {
        String[] parts = orario.split("-");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return LocalTime.parse(parts[1].trim(), formatter);
    }

    public static void prenotaLaboratorio(Prenotazione pren) {
        prenotazioniCache.add(pren);
        salvaPrenotazione(pren, Main.BASE_PATH + File.separator + "prenotazioni");
    }

    public static void salvaPrenotazione(Prenotazione pren, String directoryPath) {
        // Sostituisce i ":" nell'orario per creare un nome file valido
        String safeOrario = pren.getOrario().replace(":", "-");
        String fileName = directoryPath + File.separator + pren.getNomeUtente() + "-" + pren.getLaboratorio() + "-" + safeOrario + ".txt";
        String content = "nome: " + pren.getNomeUtente() + "\n" +
                "laboratorio: " + pren.getLaboratorio() + "\n" +
                "orario: " + pren.getOrario() + "\n" +
                "scaduto: " + (pren.isScaduto() ? "True" : "False");

        FileUtils.writeToFile(fileName, content);
    }

    public static List<Prenotazione> getPrenotazioniNonScadute() {
        List<Prenotazione> ris = new ArrayList<>();

        for (Prenotazione p : prenotazioniCache) {
            if (!p.isScaduto())
                ris.add(p);
        }

        return ris;
    }

    public static boolean isPrenotato(Laboratorio lab) {
        LocalTime now = LocalTime.now();

        for (Prenotazione p : prenotazioniCache) {
            if (p.getLaboratorio().equalsIgnoreCase(lab.getNome()) && !p.isScaduto()) {
                if (now.isAfter(p.getOrarioInizio()) && now.isBefore(p.getOrarioFine())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Prenotazione getPrenotazioneAttiva(Laboratorio lab) {
        LocalTime now = LocalTime.now();

        for (Prenotazione p : prenotazioniCache) {
            if (p.getLaboratorio().equalsIgnoreCase(lab.getNome()) && !p.isScaduto()) {
                if (now.isAfter(p.getOrarioInizio()) && now.isBefore(p.getOrarioFine())) {
                    return p;
                }
            }
        }

        return null;
    }

    public static void controllaPrenotazioniScadute() {
        LocalTime now = LocalTime.now();

        for (Prenotazione p : prenotazioniCache) {
            if (!p.isScaduto() && now.isAfter(p.getOrarioFine())) {
                p.setScaduto(true);
                salvaPrenotazione(p, Main.BASE_PATH + File.separator + "prenotazioni");
            }
        }
    }
}
