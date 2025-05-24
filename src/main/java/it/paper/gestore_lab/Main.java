package it.paper.gestore_lab;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import it.paper.gestore_lab.gui.AdminGUI;
import it.paper.gestore_lab.gui.UserGUI;
import it.paper.gestore_lab.manager.GestioneLaboratori;
import it.paper.gestore_lab.manager.GestioneUtenti;
import it.paper.gestore_lab.manager.GestorePrenotazione;
import it.paper.gestore_lab.object.Utente;
import it.paper.gestore_lab.utils.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.*;


public class Main {
    public static final String BASE_PATH = getBasePath();

    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;

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

        loginGUI();
    }

    private static void loginGUI() {
        JFrame frame = new JFrame("Sistema di Gestione - Login");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3, 2));

        JLabel userLabel = new JLabel("Seleziona utente:");
        JComboBox<String> userComboBox = new JComboBox<>();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        JButton loginButton = new JButton("Accedi");

        // Carica utenti nella lista
        GestioneUtenti gestioneUtenti = new GestioneUtenti();
        gestioneUtenti.caricaUtenti(BASE_PATH + File.separator + "utenti");

        if (gestioneUtenti.getUtentiCache().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "PRIMO AVVIO - Usa account \"admin\" con password \"admin123\" per creare i dati.",
                    "Info Primo Avvio",
                    JOptionPane.INFORMATION_MESSAGE);
            userComboBox.addItem("admin");
        } else {
            for (Utente user : gestioneUtenti.getUtentiCache()) {
                userComboBox.addItem(user.getNome()); // Solo i nomi, senza password
            }
        }

        frame.add(userLabel);
        frame.add(userComboBox);
        frame.add(passLabel);
        frame.add(passField);
        frame.add(new JLabel());
        frame.add(loginButton);

        loginButton.addActionListener(e -> {
            String username = (String) userComboBox.getSelectedItem();
            String password = new String(passField.getPassword()).trim();

            GestioneLaboratori gestioneLaboratori = new GestioneLaboratori();
            GestorePrenotazione gestorePrenotazione = new GestorePrenotazione();

            gestioneLaboratori.caricaLaboratori(BASE_PATH + File.separator + "laboratori");
            gestorePrenotazione.caricaPrenotazioni(BASE_PATH + File.separator + "prenotazioni");

            Utente loggedUser;

            if (username.equals("admin") && password.equals("admin123")) {
                loggedUser = new Utente("admin", "admin123", true);
                gestioneUtenti.getUtentiCache().add(loggedUser);
                gestioneUtenti.salvaUtente(loggedUser, BASE_PATH + File.separator + "utenti");
            } else {
                Utente user = gestioneUtenti.getUtenteByName(username);
                if (user != null && user.getPassword().equals(password)) {
                    loggedUser = user;
                } else {
                    loggedUser = null;
                    JOptionPane.showMessageDialog(frame, "Credenziali errate. Riprova.", "Errore Login", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            frame.dispose(); // Chiude la finestra di login

            SwingUtilities.invokeLater(() -> {
                if (loggedUser.isAdmin()) {
                    AdminGUI adminGui = new AdminGUI(gestioneUtenti, gestioneLaboratori, gestorePrenotazione, loggedUser);
                    adminGui.setSize(500, 500);
                    adminGui.setVisible(true);
                } else {
                    UserGUI userGui = new UserGUI(gestioneUtenti, gestioneLaboratori, gestorePrenotazione, loggedUser);
                    userGui.setSize(500, 500);
                    userGui.setVisible(true);
                }
            });
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
