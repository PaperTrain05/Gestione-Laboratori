package it.paper.gestore_lab;

import it.paper.gestore_lab.gui.AdminGUI;
import it.paper.gestore_lab.gui.UserGUI;
import it.paper.gestore_lab.manager.GestioneLaboratori;
import it.paper.gestore_lab.manager.GestioneUtenti;
import it.paper.gestore_lab.manager.GestorePrenotazione;
import it.paper.gestore_lab.object.Utente;
import it.paper.gestore_lab.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;


public class Main {
    public static final String BASE_PATH = getBasePath();

    public static void main(String[] args) {
        FileUtils.createDirs(BASE_PATH);
        verificaLogGiornaliero();

        GestioneUtenti gestioneUtenti = new GestioneUtenti();
        GestioneLaboratori gestioneLaboratori = new GestioneLaboratori();
        GestorePrenotazione gestorePrenotazione = new GestorePrenotazione();

        gestioneUtenti.caricaUtenti(BASE_PATH + File.separator + "utenti");
        gestioneLaboratori.caricaLaboratori(BASE_PATH + File.separator + "laboratori");
        gestorePrenotazione.caricaPrenotazioni(BASE_PATH + File.separator + "prenotazioni");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            gestorePrenotazione.controllaPrenotazioniScadute();
        }, 0, 5, TimeUnit.MINUTES);

        Scanner sc = new Scanner(System.in);
        Utente loggedUser = null;

        if (gestioneUtenti.getUtentiCache().isEmpty() && gestioneLaboratori.getLaboratoriCache().isEmpty()) {
            System.out.println("********************************************************************");
            System.out.println("*-* SISTEMA GESTIONE DI LABORATORI *-*");
            System.out.println("*-* QUESTO E UN PRIMO AVVIO *-*");
            System.out.println("USARE ACCOUNT \"admin\" CON PASSWORD \"admin123\"");
            System.out.println("PER CREARE UN LABORATORIO ED UTENTI");
            System.out.println("********************************************************************");

            do {
                System.out.print("Username: ");
                String u = sc.nextLine();
                System.out.print("Password: ");
                String p = sc.nextLine();
                if (u.equals("admin") && p.equals("admin123")) {
                    loggedUser = new Utente("admin", "admin123", true);
                    gestioneUtenti.getUtentiCache().add(loggedUser);
                    gestioneUtenti.salvaUtente(loggedUser, BASE_PATH + File.separator + "utenti");
                } else {
                    System.out.println("Credenziali non valide. Riprova.");
                }
            } while (loggedUser == null);

        } else {
            System.out.println("Utenti disponibili:");
            int idx = 0;
            for (Utente u : gestioneUtenti.getUtentiCache()) {
                System.out.println(idx + ". " + u.getNome());
                idx++;
            }

            boolean loginOk = false;
            do {
                System.out.print("Inserisci il tuo username: ");
                String u = sc.nextLine();
                System.out.print("Inserisci la password: ");
                String p = sc.nextLine();
                Utente user = gestioneUtenti.getUtenteByName(u);
                if (user != null && user.getPassword().equals(p)) {
                    loggedUser = user;
                    loginOk = true;
                } else {
                    System.out.println("Credenziali errate. Riprova.");
                }
            } while (!loginOk);
        }
        sc.close();

        final Utente finalUser = loggedUser; // variabile final per lambda

        // Avvia la GUI in base al ruolo (finestra 500x500)
        SwingUtilities.invokeLater(() -> {
            if (finalUser.isAdmin()) {
                AdminGUI adminGui = new AdminGUI(gestioneUtenti, gestioneLaboratori, gestorePrenotazione, finalUser);
                adminGui.setSize(500, 500);
                adminGui.setVisible(true);
            } else {
                UserGUI userGui = new UserGUI(gestioneUtenti, gestioneLaboratori, gestorePrenotazione, finalUser);
                userGui.setSize(500, 500);
                userGui.setVisible(true);
            }
        });
    }

    private static String getBasePath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "C:" + File.separator + "PCTO_Mameli";
        } else {
            return System.getProperty("user.home") + File.separator + "PCTO_Mameli";
        }
    }

    private static void verificaLogGiornaliero() {
        String logDirPath = BASE_PATH + File.separator + "logs";
        File logDir = new File(logDirPath);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String logFilename = logDirPath + File.separator + "log_" + currentDate + ".txt";
        File logFile = new File(logFilename);

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
                System.out.println("Creato nuovo file di log: " + logFilename);
            } catch (IOException e) {
                System.err.println("Errore nella creazione del file di log: " + e.getMessage());
            }
        }
    }
}
