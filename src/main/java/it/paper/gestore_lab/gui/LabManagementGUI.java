package it.paper.gestore_lab.gui;

import it.paper.gestore_lab.Main;
import it.paper.gestore_lab.manager.GestioneLaboratori;
import it.paper.gestore_lab.manager.GestioneUtenti;
import it.paper.gestore_lab.manager.GestorePrenotazione;
import it.paper.gestore_lab.object.Laboratorio;
import it.paper.gestore_lab.object.Prenotazione;
import it.paper.gestore_lab.object.Utente;
import it.paper.gestore_lab.utils.FileUtils;

import javax.swing.*;
import java.awt.*;

public class LabManagementGUI extends JFrame {

    private GestioneUtenti gestioneUtenti;
    private GestioneLaboratori gestioneLaboratori;
    private GestorePrenotazione gestorePrenotazione;

    private JTextArea dashboardArea;
    private JTextArea utentiArea;
    private JTextArea prenotazioniArea;

    // Per il tab Laboratori: area di gestione e modulo di creazione
    private JTextArea labManagementArea;
    private JTextField labNameField;
    private NumericFieldPanel postiPanel;
    private JTextField gestoreField;
    private NumericFieldPanel pcPanel;
    private NumericFieldPanel switchesPanel;
    private NumericFieldPanel routersPanel;
    private JTextField ipField;
    private JTextField subnetField;
    private JButton createLabButton;

    public LabManagementGUI(GestioneUtenti gestioneUtenti, GestioneLaboratori gestioneLaboratori, GestorePrenotazione gestorePrenotazione) {
        this.gestioneUtenti = gestioneUtenti;
        this.gestioneLaboratori = gestioneLaboratori;
        this.gestorePrenotazione = gestorePrenotazione;
        initialize();
    }

    private void initialize() {
        setTitle("Sistema di Gestione Laboratori - Management GUI");
        setExtendedState(JFrame.MAXIMIZED_BOTH);  // Finestra massimizzata
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Dashboard Tab
        JPanel dashboardTab = new JPanel(new BorderLayout());
        dashboardArea = new JTextArea();
        dashboardArea.setEditable(false);
        dashboardTab.add(new JScrollPane(dashboardArea), BorderLayout.CENTER);
        JButton refreshDashBtn = new JButton("Aggiorna Dashboard");
        refreshDashBtn.addActionListener(e -> refreshDashboard());
        dashboardTab.add(refreshDashBtn, BorderLayout.SOUTH);
        tabbedPane.addTab("Dashboard", dashboardTab);

        // Laboratori Tab: gestione e creazione
        JPanel labTab = new JPanel(new BorderLayout());
        JPanel labManagementPanel = new JPanel(new BorderLayout());
        labManagementPanel.setBorder(BorderFactory.createTitledBorder("Gestione Laboratori"));
        labManagementArea = new JTextArea();
        labManagementArea.setEditable(false);
        labManagementPanel.add(new JScrollPane(labManagementArea), BorderLayout.CENTER);
        JButton refreshLabBtn = new JButton("Aggiorna");
        refreshLabBtn.addActionListener(e -> refreshLabManagement());
        labManagementPanel.add(refreshLabBtn, BorderLayout.SOUTH);

        JPanel labCreationPanel = new JPanel();
        labCreationPanel.setLayout(new BoxLayout(labCreationPanel, BoxLayout.Y_AXIS));
        labCreationPanel.setBorder(BorderFactory.createTitledBorder("Crea Nuovo Laboratorio"));

        labNameField = new JTextField(15);
        labCreationPanel.add(labeled("Nome Laboratorio:", labNameField));

        postiPanel = new NumericFieldPanel(0);
        labCreationPanel.add(labeled("Quantità Posti:", postiPanel));

        gestoreField = new JTextField(15);
        labCreationPanel.add(labeled("Gestore Laboratorio:", gestoreField));

        pcPanel = new NumericFieldPanel(0);
        labCreationPanel.add(labeled("Quantità PC:", pcPanel));

        switchesPanel = new NumericFieldPanel(0);
        labCreationPanel.add(labeled("Switches:", switchesPanel));

        routersPanel = new NumericFieldPanel(0);
        labCreationPanel.add(labeled("Routers:", routersPanel));

        ipField = new JTextField(15);
        labCreationPanel.add(labeled("Indirizzo IP:", ipField));

        subnetField = new JTextField(15);
        labCreationPanel.add(labeled("Subnet Mask:", subnetField));

        createLabButton = new JButton("Crea Laboratorio");
        createLabButton.addActionListener(e -> createLaboratory());
        JPanel createBtnPanel = new JPanel();
        createBtnPanel.add(createLabButton);
        labCreationPanel.add(createBtnPanel);

        JSplitPane labSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, labManagementPanel, labCreationPanel);
        labSplitPane.setDividerLocation(700);
        labTab.add(labSplitPane, BorderLayout.CENTER);
        tabbedPane.addTab("Laboratori", labTab);

        // Utenti Tab
        JPanel utentiTab = new JPanel(new BorderLayout());
        utentiArea = new JTextArea();
        utentiArea.setEditable(false);
        utentiTab.add(new JScrollPane(utentiArea), BorderLayout.CENTER);
        JButton refreshUtentiBtn = new JButton("Aggiorna Utenti");
        refreshUtentiBtn.addActionListener(e -> refreshUtenti());
        utentiTab.add(refreshUtentiBtn, BorderLayout.SOUTH);
        tabbedPane.addTab("Utenti", utentiTab);

        // Prenotazioni Tab
        JPanel prenotazioniTab = new JPanel(new BorderLayout());
        prenotazioniArea = new JTextArea();
        prenotazioniArea.setEditable(false);
        prenotazioniTab.add(new JScrollPane(prenotazioniArea), BorderLayout.CENTER);
        JPanel prenBtnPanel = new JPanel();
        JButton refreshPrenBtn = new JButton("Aggiorna Prenotazioni");
        refreshPrenBtn.addActionListener(e -> refreshPrenotazioni());
        prenBtnPanel.add(refreshPrenBtn);
        prenotazioniTab.add(prenBtnPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Prenotazioni", prenotazioniTab);

        add(tabbedPane, BorderLayout.CENTER);
        // Aggiorna le visualizzazioni iniziali
        refreshDashboard();
        refreshLabManagement();
        refreshUtenti();
        refreshPrenotazioni();
    }

    private JPanel labeled(String label, Component comp) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(label));
        panel.add(comp);
        return panel;
    }

    private void refreshDashboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("Laboratori:\n");
        for (Laboratorio lab : gestioneLaboratori.getLaboratoriCache()) {
            sb.append("- ").append(lab.getNome())
                    .append(", Posti: ").append(lab.getQntPosti())
                    .append(", Gestore: ").append(lab.getGestoreLab())
                    .append(", IP: ").append(lab.getIndirizzoIP())
                    .append("\n");
        }
        sb.append("\nUtenti:\n");
        for (Utente u : gestioneUtenti.getUtentiCache()) {
            sb.append("- ").append(u.getNome())
                    .append(", Password: ").append(u.getPassword())
                    .append(u.isAdmin() ? " (admin)" : "")
                    .append("\n");
        }
        sb.append("\nPrenotazioni:\n");
        for (Prenotazione p : gestorePrenotazione.getPrenotazioniNonScadute()) {
            sb.append("- ").append(p.getLaboratorio())
                    .append(", Utente: ").append(p.getNomeUtente())
                    .append(", Orario: ").append(p.getOrario())
                    .append("\n");
        }
        dashboardArea.setText(sb.toString());
    }

    private void refreshLabManagement() {
        StringBuilder sb = new StringBuilder();
        for (Laboratorio lab : gestioneLaboratori.getLaboratoriCache()) {
            sb.append("Nome: ").append(lab.getNome())
                    .append(", Posti: ").append(lab.getQntPosti())
                    .append(", Gestore: ").append(lab.getGestoreLab())
                    .append(", IP: ").append(lab.getIndirizzoIP())
                    .append("\n");
        }
        labManagementArea.setText(sb.toString());
    }

    private void refreshUtenti() {
        StringBuilder sb = new StringBuilder();
        for (Utente u : gestioneUtenti.getUtentiCache()) {
            sb.append("Nome: ").append(u.getNome())
                    .append(", Password: ").append(u.getPassword())
                    .append(u.isAdmin() ? " (admin)" : "")
                    .append("\n");
        }
        utentiArea.setText(sb.toString());
    }

    private void refreshPrenotazioni() {
        StringBuilder sb = new StringBuilder();
        for (Prenotazione p : gestorePrenotazione.getPrenotazioniNonScadute()) {
            sb.append("Laboratorio: ").append(p.getLaboratorio())
                    .append(", Utente: ").append(p.getNomeUtente())
                    .append(", Orario: ").append(p.getOrario())
                    .append("\n");
        }
        prenotazioniArea.setText(sb.toString());
    }

    private void createLaboratory() {
        String nome = labNameField.getText().trim();
        String gestore = gestoreField.getText().trim();
        String ip = ipField.getText().trim();
        String subnet = subnetField.getText().trim();

        if (nome.isEmpty() || gestore.isEmpty() || ip.isEmpty() || subnet.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tutti i campi devono essere compilati.");
            return;
        }
        if (!FileUtils.validaIP(ip) || !FileUtils.validaIP(subnet)) {
            JOptionPane.showMessageDialog(this, "Formato IP o Subnet Mask non valido.");
            return;
        }
        int posti = postiPanel.getValue();
        int pc = pcPanel.getValue();
        int sw = switchesPanel.getValue();
        int routers = routersPanel.getValue();

        Laboratorio lab = new Laboratorio(nome, posti, gestore, pc, sw, routers, ip, subnet);
        gestioneLaboratori.getLaboratoriCache().add(lab);
        gestioneLaboratori.salvaLaboratorio(lab, Main.BASE_PATH + java.io.File.separator + "laboratori");
        JOptionPane.showMessageDialog(this, "Laboratorio creato.");
        refreshLabManagement();
        refreshDashboard();
    }

    // Pannello interno per gestire un valore numerico con bottoni "+" e "-"
    class NumericFieldPanel extends JPanel {
        private int value;
        private JLabel valueLabel;

        public NumericFieldPanel(int initialValue) {
            this.value = initialValue;
            setLayout(new FlowLayout(FlowLayout.LEFT));
            JButton minusBtn = new JButton("-");
            JButton plusBtn = new JButton("+");
            valueLabel = new JLabel(String.valueOf(value));

            minusBtn.addActionListener(e -> {
                if (value > 0) {
                    value--;
                    valueLabel.setText(String.valueOf(value));
                }
            });

            plusBtn.addActionListener(e -> {
                value++;
                valueLabel.setText(String.valueOf(value));
            });

            add(minusBtn);
            add(valueLabel);
            add(plusBtn);
        }

        public int getValue() {
            return value;
        }
    }
}
