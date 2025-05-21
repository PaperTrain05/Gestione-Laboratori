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

public class AdminGUI extends JFrame {

    private GestioneUtenti gestioneUtenti;
    private GestioneLaboratori gestioneLaboratori;
    private GestorePrenotazione gestorePrenotazione;
    private Utente loggedUser;

    // Dashboard
    private JTextArea dashboardArea;

    // Gestione Tab – un JTabbedPane con due sotto-tab: "Utenti e Laboratori" e "Prenotazioni"
    private JTabbedPane gestioneTabs;

    // Pannello Utenti e Laboratori
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JButton addUserBtn, deleteUserBtn, modifyPwdBtn;

    private JList<String> labList;
    private DefaultListModel<String> labListModel;
    private JButton addLabBtn, deleteLabBtn;

    // Pannello Prenotazioni (admin può cancellare tutte)
    private JList<String> resList;
    private DefaultListModel<String> resListModel;
    private JButton addResBtn, deleteResBtn;

    public AdminGUI(GestioneUtenti gestioneUtenti, GestioneLaboratori gestioneLaboratori, GestorePrenotazione gestorePrenotazione, Utente loggedUser) {
        this.gestioneUtenti = gestioneUtenti;
        this.gestioneLaboratori = gestioneLaboratori;
        this.gestorePrenotazione = gestorePrenotazione;
        this.loggedUser = loggedUser;

        initialize();
    }

    private void initialize() {
        setTitle("Admin - Sistema di Gestione");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane mainTabs = new JTabbedPane();

        // Dashboard Tab
        JPanel dashPanel = new JPanel(new BorderLayout());
        dashboardArea = new JTextArea();
        dashboardArea.setEditable(false);
        dashPanel.add(new JScrollPane(dashboardArea), BorderLayout.CENTER);
        JButton refreshDashBtn = new JButton("Aggiorna Dashboard");
        refreshDashBtn.addActionListener(e -> refreshDashboard());
        dashPanel.add(refreshDashBtn, BorderLayout.SOUTH);
        mainTabs.addTab("Dashboard", dashPanel);

        // Gestione Tab
        gestioneTabs = new JTabbedPane();

        // Sotto-tab "Utenti e Laboratori"
        JPanel ulPanel = new JPanel(new GridLayout(1, 2));

        // Pannello Utenti
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("Gestione Utenti"));
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        JPanel userBtnPanel = new JPanel();
        addUserBtn = new JButton("Aggiungi");
        deleteUserBtn = new JButton("Elimina");
        modifyPwdBtn = new JButton("Modifica Pwd");
        addUserBtn.addActionListener(e -> addUser());
        deleteUserBtn.addActionListener(e -> deleteSelectedUser());
        modifyPwdBtn.addActionListener(e -> modifySelectedUserPwd());
        userBtnPanel.add(addUserBtn);
        userBtnPanel.add(deleteUserBtn);
        userBtnPanel.add(modifyPwdBtn);
        userPanel.add(userBtnPanel, BorderLayout.SOUTH);
        ulPanel.add(userPanel);

        // Pannello Laboratori
        JPanel labPanel = new JPanel(new BorderLayout());
        labPanel.setBorder(BorderFactory.createTitledBorder("Gestione Laboratori"));
        labListModel = new DefaultListModel<>();
        labList = new JList<>(labListModel);
        labPanel.add(new JScrollPane(labList), BorderLayout.CENTER);
        JPanel labBtnPanel = new JPanel();
        addLabBtn = new JButton("Aggiungi");
        deleteLabBtn = new JButton("Elimina");
        addLabBtn.addActionListener(e -> addLab());
        deleteLabBtn.addActionListener(e -> deleteSelectedLab());
        labBtnPanel.add(addLabBtn);
        labBtnPanel.add(deleteLabBtn);
        labPanel.add(labBtnPanel, BorderLayout.SOUTH);
        ulPanel.add(labPanel);

        gestioneTabs.addTab("Utenti / Laboratori", ulPanel);

        // Sotto-tab "Prenotazioni"
        JPanel resPanel = new JPanel(new BorderLayout());
        resPanel.setBorder(BorderFactory.createTitledBorder("Gestione Prenotazioni"));
        resListModel = new DefaultListModel<>();
        resList = new JList<>(resListModel);
        resPanel.add(new JScrollPane(resList), BorderLayout.CENTER);
        JPanel resBtnPanel = new JPanel();
        addResBtn = new JButton("Crea Prenotazione");
        deleteResBtn = new JButton("Elimina Prenotazione");
        addResBtn.addActionListener(e -> addReservation());
        deleteResBtn.addActionListener(e -> deleteSelectedReservation());
        resBtnPanel.add(addResBtn);
        resBtnPanel.add(deleteResBtn);
        resPanel.add(resBtnPanel, BorderLayout.SOUTH);
        gestioneTabs.addTab("Prenotazioni", resPanel);

        mainTabs.addTab("Gestione", gestioneTabs);
        add(mainTabs, BorderLayout.CENTER);

        refreshAllAdmin();
    }

    private void refreshDashboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("----- Dashboard -----\n\n");
        sb.append("Laboratori:\n");
        for (Laboratorio lab : gestioneLaboratori.getLaboratoriCache()) {
            sb.append("Nome: ").append(lab.getNome())
                    .append(", Posti: ").append(lab.getQntPosti())
                    .append(", Gestore: ").append(lab.getGestoreLab())
                    .append(", IP: ").append(lab.getIndirizzoIP()).append("\n");
        }

        sb.append("\nUtenti:\n");
        for (Utente u : gestioneUtenti.getUtentiCache()) {
            sb.append("Nome: ").append(u.getNome())
                    .append(", Pwd: ").append(u.getPassword())
                    .append(u.isAdmin() ? " (admin)" : "").append("\n");
        }

        sb.append("\nPrenotazioni:\n");
        for (Prenotazione p : gestorePrenotazione.getPrenotazioniNonScadute()) {
            sb.append("Lab: ").append(p.getLaboratorio())
                    .append(", Utente: ").append(p.getNomeUtente())
                    .append(", Orario: ").append(p.getOrario()).append("\n");
        }

        dashboardArea.setText(sb.toString());
    }

    private void refreshUserList() {
        userListModel.clear();

        for (Utente u : gestioneUtenti.getUtentiCache()) {
            userListModel.addElement(u.getNome() + " - " + u.getPassword() + (u.isAdmin() ? " (admin)" : ""));
        }
    }

    private void refreshLabList() {
        labListModel.clear();

        for (Laboratorio lab : gestioneLaboratori.getLaboratoriCache()) {
            labListModel.addElement(lab.getNome() + " - Posti: " + lab.getQntPosti());
        }
    }

    private void refreshResList() {
        resListModel.clear();

        for (Prenotazione p : gestorePrenotazione.getPrenotazioniNonScadute()) {
            resListModel.addElement("Lab: " + p.getLaboratorio() + ", Utente: " + p.getNomeUtente() + ", Orario: " + p.getOrario());
        }
    }

    private void refreshAllAdmin() {
        refreshDashboard();
        refreshUserList();
        refreshLabList();
        refreshResList();
    }

    // Gestione utenti
    private void addUser() {
        String username = JOptionPane.showInputDialog(this, "Inserisci nuovo username:");
        if (username == null || username.trim().isEmpty()) return;

        String pwd = JOptionPane.showInputDialog(this, "Inserisci password:");
        if (pwd == null || pwd.trim().isEmpty()) return;

        int conf = JOptionPane.showConfirmDialog(this, "L'utente è admin?", "Ruolo", JOptionPane.YES_NO_OPTION);
        boolean isAdmin = (conf == JOptionPane.YES_OPTION);
        Utente nuovo = new Utente(username, pwd, isAdmin);
        gestioneUtenti.getUtentiCache().add(nuovo);
        gestioneUtenti.salvaUtente(nuovo, Main.BASE_PATH + File.separator + "utenti");
        JOptionPane.showMessageDialog(this, "Utente creato.\nNuova password: " + pwd);

        GestoreLog.logAction(loggedUser.getNome(), "Ha creato un utente: " + nuovo.getNome() + " con password " + nuovo.getPassword());

        refreshUserList();
        refreshDashboard();
    }

    private void deleteSelectedUser() {
        int index = userList.getSelectedIndex();

        if (gestioneUtenti.getUtentiCache().get(index).getNome().equalsIgnoreCase("admin")) {
            JOptionPane.showMessageDialog(this, "Questo è un utente di sistema, non si può eliminare");
            return;
        }

        if (index >= 0) {
            GestoreLog.logAction(loggedUser.getNome(), "Ha eliminato un utente: " + gestioneUtenti.getUtentiCache().get(index).getNome());

            gestioneUtenti.getUtentiCache().remove(index);
            refreshUserList();
            refreshDashboard();

        } else {
            JOptionPane.showMessageDialog(this, "Seleziona un utente da eliminare.");
        }
    }

    private void modifySelectedUserPwd() {
        int index = userList.getSelectedIndex();

        if (index >= 0) {
            Utente u = gestioneUtenti.getUtentiCache().get(index);
            String newPwd = JOptionPane.showInputDialog(this, "Inserisci nuova password per " + u.getNome() + ":");

            if (newPwd != null && !newPwd.trim().isEmpty()) {
                Utente updated = new Utente(u.getNome(), newPwd, u.isAdmin());
                gestioneUtenti.getUtentiCache().set(index, updated);
                gestioneUtenti.salvaUtente(updated, Main.BASE_PATH + File.separator + "utenti");
                JOptionPane.showMessageDialog(this, "Password modificata.\nNuova password: " + newPwd);

                GestoreLog.logAction(loggedUser.getNome(), "Ha modificato la password per l'utente: " + updated.getNome() + " con password nuovo: " + updated.getPassword());

                refreshUserList();
                refreshDashboard();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleziona un utente per modificare la password.");
        }
    }

    // Gestione laboratori
    private void addLab() {
        String nome = JOptionPane.showInputDialog(this, "Inserisci nome laboratorio:");
        if (nome == null || nome.trim().isEmpty()) return;

        String postiStr = JOptionPane.showInputDialog(this, "Inserisci quantità posti:");
        int posti = Integer.parseInt(postiStr);

        String gestore = JOptionPane.showInputDialog(this, "Inserisci gestore laboratorio:");
        String ip = JOptionPane.showInputDialog(this, "Inserisci indirizzo IP:");
        String subnet = JOptionPane.showInputDialog(this, "Inserisci Subnet Mask:");
        Laboratorio lab = new Laboratorio(nome, posti, gestore, 0, 0, 0, ip, subnet);

        gestioneLaboratori.getLaboratoriCache().add(lab);
        gestioneLaboratori.salvaLaboratorio(lab, Main.BASE_PATH + File.separator + "laboratori");
        JOptionPane.showMessageDialog(this, "Laboratorio creato.");

        GestoreLog.logAction(loggedUser.getNome(), "Ha aggiunto un nuovo laboratorio con nome: " + nome + ", con posti: " + posti + ", indirizzo IP: " + ip + ", con subnetmask: " + subnet);

        refreshLabList();
        refreshDashboard();
    }

    private void deleteSelectedLab() {
        int index = labList.getSelectedIndex();

        if (index >= 0) {
            GestoreLog.logAction(loggedUser.getNome(), "Ha eliminato un laboratorio con nome: " + gestioneLaboratori.getLaboratoriCache().get(index) + ", con posti: " + gestioneLaboratori.getLaboratoriCache().get(index) + ", indirizzo IP: " + gestioneLaboratori.getLaboratoriCache().get(index) + ", con subnetmask: " + gestioneLaboratori.getLaboratoriCache().get(index));

            gestioneLaboratori.getLaboratoriCache().remove(index);
            refreshLabList();
            refreshDashboard();
        } else {
            JOptionPane.showMessageDialog(this, "Seleziona un laboratorio da eliminare.");
        }
    }

    // Gestione prenotazioni
    private void addReservation() {
        String labName = JOptionPane.showInputDialog(this, "Inserisci nome laboratorio per prenotazione:");
        String orario = JOptionPane.showInputDialog(this, "Inserisci orario (HH:mm-HH:mm):");

        if (labName == null || labName.trim().isEmpty() || orario == null || orario.trim().isEmpty() || !orario.matches("^([01]?\\d|2[0-3]):[0-5]\\d-([01]?\\d|2[0-3]):[0-5]\\d$")) {
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

        GestoreLog.logAction(loggedUser.getNome(), "Ha effettuato una prenotazione con: " + pren.getNomeUtente() + ", nel laboratorio: " + labName + ", con orario: " + orario);

        refreshResList();
        refreshDashboard();
    }

    private void deleteSelectedReservation() {
        int index = resList.getSelectedIndex();

        if (index >= 0) {
            // Ottiene l'oggetto Prenotazione corrispondente
            Prenotazione pren = gestorePrenotazione.getPrenotazioniCache().get(index);

            // Costruisce il nome del file associato alla prenotazione
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

            GestoreLog.logAction(loggedUser.getNome(), "Ha eliminato una prenotazione con nome utente: " + gestorePrenotazione.getPrenotazioniCache().get(index).getNomeUtente() + ", al laboratorio: " + gestorePrenotazione.getPrenotazioniCache().get(index).getLaboratorio() + ", con orario: " + gestorePrenotazione.getPrenotazioniCache().get(index).getOrario());

            // Rimuove la prenotazione dalla cache
            gestorePrenotazione.getPrenotazioniCache().remove(index);
            refreshResList();
            refreshDashboard();
        } else {
            JOptionPane.showMessageDialog(this, "Seleziona una prenotazione da eliminare.");
        }
    }
}
