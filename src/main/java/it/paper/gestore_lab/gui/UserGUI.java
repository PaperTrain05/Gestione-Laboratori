package it.paper.gestore_lab.gui;

import it.paper.gestore_lab.Main;
import it.paper.gestore_lab.manager.GestioneLaboratori;
import it.paper.gestore_lab.manager.GestioneUtenti;
import it.paper.gestore_lab.manager.GestoreLog;
import it.paper.gestore_lab.manager.GestorePrenotazione;
import it.paper.gestore_lab.object.Laboratorio;
import it.paper.gestore_lab.object.Prenotazione;
import it.paper.gestore_lab.object.Utente;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserGUI extends JFrame {

    private GestioneUtenti gestioneUtenti;
    private GestioneLaboratori gestioneLaboratori;
    private GestorePrenotazione gestorePrenotazione;
    private Utente loggedUser;

    // Dashboard
    private JTextArea dashboardArea;

    // Prenotazioni
    private JPanel prenPanel;
    private JTextField labField;
    private JTextField orarioField;
    private JButton createPrenBtn;
    private DefaultListModel<String> prenListModel;
    private JList<String> prenList;
    private JButton deletePrenBtn;

    public UserGUI(GestioneUtenti gestioneUtenti, GestioneLaboratori gestioneLaboratori, GestorePrenotazione gestorePrenotazione, Utente loggedUser) {
        this.gestioneUtenti = gestioneUtenti;
        this.gestioneLaboratori = gestioneLaboratori;
        this.gestorePrenotazione = gestorePrenotazione;
        this.loggedUser = loggedUser;
        initialize();
    }

    private void initialize() {
        setTitle("User - Sistema di Prenotazioni");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

        JMenuBar menuBar = new JMenuBar();

        // Menù "Utenti"
        JMenu utentiMenu = new JMenu("Utenti");
        JMenuItem cambiaUtente = new JMenuItem("Cambia Utente");
        cambiaUtente.addActionListener(e -> cambioUtente());
        utentiMenu.add(cambiaUtente);

        // Menù "File"
        JMenu fileMenu = new JMenu("File");
        JMenuItem apriLogDirectory = new JMenuItem("Apri Directory Log");
        apriLogDirectory.addActionListener(e -> apriCartella(Main.BASE_PATH + File.separator + "logs"));

        fileMenu.add(apriLogDirectory);

        menuBar.add(utentiMenu);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        // Dashboard Tab
        JPanel dashPanel = new JPanel(new BorderLayout());
        dashboardArea = new JTextArea();
        dashboardArea.setEditable(false);
        dashPanel.add(new JScrollPane(dashboardArea), BorderLayout.CENTER);
        JButton refreshDashBtn = new JButton("Aggiorna Dashboard");
        refreshDashBtn.addActionListener(e -> refreshDashboard());
        dashPanel.add(refreshDashBtn, BorderLayout.SOUTH);
        tabs.addTab("Dashboard", dashPanel);

        // Prenotazioni Tab
        JPanel prenTab = new JPanel(new BorderLayout());
        // Form di creazione prenotazione
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder("Crea Prenotazione"));
        formPanel.add(labeled("Utente:", new JLabel(loggedUser.getNome())));
        labField = new JTextField(10);
        formPanel.add(labeled("Laboratorio:", labField));
        orarioField = new JTextField(10);
        formPanel.add(labeled("Orario (HH:mm-HH:mm):", orarioField));
        createPrenBtn = new JButton("Crea Prenotazione");
        createPrenBtn.addActionListener(e -> createPrenotazione());
        JPanel btnPanel = new JPanel();
        btnPanel.add(createPrenBtn);
        formPanel.add(btnPanel);
        prenTab.add(formPanel, BorderLayout.SOUTH);

        // Lista delle prenotazioni dell'utente
        prenListModel = new DefaultListModel<>();
        prenList = new JList<>(prenListModel);
        prenTab.add(new JScrollPane(prenList), BorderLayout.CENTER);

        // Bottone per eliminare prenotazioni dell'utente
        deletePrenBtn = new JButton("Elimina Prenotazione");
        deletePrenBtn.addActionListener(e -> deleteSelectedPren());
        prenTab.add(deletePrenBtn, BorderLayout.NORTH);

        tabs.addTab("Prenotazioni", prenTab);
        add(tabs, BorderLayout.CENTER);

        refreshDashboard();
        refreshUserPrenotations();
    }

    private void cambioUtente() {
        dispose();
        SwingUtilities.invokeLater(() -> Main.loginGUI());
    }

    private void apriCartella(String percorso) {
        try {
            Desktop.getDesktop().open(new File(percorso));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Errore nell'apertura della cartella: " + percorso, "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel labeled(String label, Component comp) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(label));
        panel.add(comp);

        return panel;
    }

    private void refreshDashboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("----- Laboratori -----\n");
        for (Laboratorio lab : gestioneLaboratori.getLaboratoriCache()) {
            sb.append("Nome: ").append(lab.getNome())
                    .append(", Posti: ").append(lab.getQntPosti())
                    .append(", Gestore: ").append(lab.getGestoreLab())
                    .append(", IP: ").append(lab.getIndirizzoIP()).append("\n");
        }

        sb.append("\n----- Prenotazioni -----\n");
        for (Prenotazione p : gestorePrenotazione.getPrenotazioniNonScadute()) {
            sb.append("Lab: ").append(p.getLaboratorio())
                    .append(", Utente: ").append(p.getNomeUtente())
                    .append(", Orario: ").append(p.getOrario()).append("\n");
        }

        dashboardArea.setText(sb.toString());
    }

    private void refreshUserPrenotations() {
        prenListModel.clear();
        List<Prenotazione> prenListAll = gestorePrenotazione.getPrenotazioniNonScadute();

        for (Prenotazione p : prenListAll) {
            if (p.getNomeUtente().equalsIgnoreCase(loggedUser.getNome())) {
                prenListModel.addElement("Lab: " + p.getLaboratorio() + ", Orario: " + p.getOrario());
            }
        }
    }

    private void createPrenotazione() {
        String labName = labField.getText().trim();
        String orario = orarioField.getText().trim();

        if (labName.isEmpty() || orario.isEmpty() || !orario.matches("^([01]?\\d|2[0-3]):[0-5]\\d-([01]?\\d|2[0-3]):[0-5]\\d$")) {
            JOptionPane.showMessageDialog(this, "Verifica i campi per la prenotazione.");
            return;
        }

        Laboratorio lab = gestioneLaboratori.getLaboratorioByName(labName);
        if (lab == null) {
            JOptionPane.showMessageDialog(this, "Laboratorio non trovato.");
            return;
        }

        if (!gestorePrenotazione.puoiPrenotare(loggedUser, lab, orario)) {
            JOptionPane.showMessageDialog(this, "Prenotazione non possibile per conflitto.");
            return;
        }

        Prenotazione pren = new Prenotazione(loggedUser.getNome(), labName, orario, false);
        gestorePrenotazione.prenotaLaboratorio(pren, Main.BASE_PATH + File.separator + "prenotazioni");
        JOptionPane.showMessageDialog(this, "Prenotazione effettuata.");
        labField.setText("");
        orarioField.setText("");
        refreshDashboard();
        refreshUserPrenotations();

        GestoreLog.logAction(loggedUser.getNome(), "Ha creato una prenotazione: " + labName + " - " + orario);
    }

    private void deleteSelectedPren() {
        int sel = prenList.getSelectedIndex();

        if (sel >= 0) {
            // Ricava la lista delle prenotazioni dell'utente
            List<Prenotazione> userPren = new ArrayList<>();
            for (Prenotazione p : gestorePrenotazione.getPrenotazioniNonScadute()) {
                if (p.getNomeUtente().equalsIgnoreCase(loggedUser.getNome())) userPren.add(p);
            }

            if (sel < userPren.size()) {
                gestorePrenotazione.getPrenotazioniCache().remove(userPren.get(sel));

                Prenotazione pren = gestorePrenotazione.getPrenotazioniCache().get(sel);

                String safeOrario = pren.getOrario().replace(":", "-");
                String filePath = Main.BASE_PATH + File.separator + "prenotazioni"
                        + File.separator + pren.getNomeUtente() + "-" + pren.getLaboratorio()
                        + "-" + safeOrario + ".txt";

                // Elimina il file se esiste
                File file = new File(filePath);
                if (file.exists()) {
                    if (!file.delete()) {
                        JOptionPane.showMessageDialog(this, "Errore durante la cancellazione del file.");
                        return;
                    }
                }

                refreshUserPrenotations();
                refreshDashboard();

                GestoreLog.logAction(loggedUser.getNome(), "Ha eliminato una prenotazione: " + pren.getLaboratorio() + " - " + safeOrario);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleziona una prenotazione da eliminare.");
        }
    }
}
