package it.paper.gestore_lab.manager;

import it.paper.gestore_lab.Main;
import it.paper.gestore_lab.object.Laboratorio;
import it.paper.gestore_lab.object.Prenotazione;
import it.paper.gestore_lab.object.Utente;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class GestoreAvvio {
    private static Scanner sc = new Scanner(System.in);

    public static void avvioPrimaEsecuzione() {
        System.out.println("********************************************************************");
        System.out.println("*-* SISTEMA GESTIONE DI LABORATORI *-*");
        System.out.println("*-* QUESTO E UN PRIMO AVVIO *-*");
        System.out.println("USARE ACCOUNT \"admin\" CON PASSWORD \"admin123\"");
        System.out.println("PER CREARE UN LABORATORIO ED UTENTI");
        System.out.println("********************************************************************");

        System.out.print("Inserisci username: ");
        String user = sc.nextLine();
        System.out.print("Inserisci password: ");
        String pass = sc.nextLine();

        if (!user.equals("admin") || !pass.equals("admin123")) {
            System.out.println("Credenziali non valide. Riprova.");
            avvioPrimaEsecuzione();
            return;
        }

        if (GestioneUtenti.getUtenteByName("admin") == null) {
            Utente adminUser = new Utente("admin", "admin123", true);
            GestioneUtenti.utentiCache.add(adminUser);
            GestioneUtenti.salvaUtente(adminUser, Main.BASE_PATH + File.separator + "utenti");
        }

        menuPrimaEsecuzione();
    }

    private static void menuPrimaEsecuzione() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\nMenu Admin:");
            System.out.println("1. Crea Laboratorio");
            System.out.println("2. Crea Utente");
            System.out.println("3. Effettua Prenotazione");
            System.out.println("0. Esci");
            System.out.print("Seleziona un'opzione: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    if (checkAdminAccess()) {
                        GestioneLaboratori.creaLaboratorio();
                    }
                    break;
                case "2":
                    if (checkAdminAccess()) {
                        GestioneUtenti.creaUtente();
                    }
                    break;
                case "3":
                    gestisciPrenotazioni();
                    break;
                case "0":
                    exit = true;
                    break;
                default:
                    System.out.println("Scelta non valida");
            }
        }
    }

    public static void avvioNormale() {
        System.out.println("********************************************************************");
        System.out.println("*-* SISTEMA GESTIONE DI LABORATORI *-*");
        System.out.println("********************************************************************");

        System.out.println("\nUtenti:");
        for (Utente u : GestioneUtenti.utentiCache) {
            System.out.println("- " + u.getNome());
        }

        System.out.println("\nLaboratori:");
        for (Laboratorio lab : GestioneLaboratori.laboratoriCache) {
            boolean prenotato = GestorePrenotazione.isPrenotato(lab);
            System.out.print("- " + lab.getNome() + ", posti: " + lab.getQntPosti() + ", IP: " + lab.getIndirizzoIP()
                    + ", prenotato: " + prenotato);
            if (prenotato) {
                Prenotazione pren = GestorePrenotazione.getPrenotazioneAttiva(lab);
                if (pren != null) {
                    System.out.print(" (" + pren.getOrario() + ", utente: " + pren.getNomeUtente() + ")");
                }
            }
            System.out.println();
        }

        System.out.println("\nPrenotazioni:");
        List<Prenotazione> listaPrenotazioni = GestorePrenotazione.getPrenotazioniNonScadute();
        int count = 0;
        for (int i = 0; i < listaPrenotazioni.size(); i++) {
            Prenotazione p = listaPrenotazioni.get(i);
            if (count < 5) {
                System.out.println("- " + p.getLaboratorio() + ", utente: " + p.getNomeUtente()
                        + ", orario: " + p.getOrario());
                count++;
            } else {
                System.out.println("[premere Q per andare avanti nelle prenotazioni]");
                String input = sc.nextLine();
                if (input.equalsIgnoreCase("Q")) {
                    count = 0;
                }
            }
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("\nOpzioni:");
            System.out.println("1. Crea Laboratorio (solo admin)");
            System.out.println("2. Crea Utente (solo admin)");
            System.out.println("3. Effettua Prenotazione");
            System.out.println("0. Esci");
            System.out.print("Seleziona un'opzione: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    if (checkAdminAccess()) {
                        GestioneLaboratori.creaLaboratorio();
                    }
                    break;
                case "2":
                    if (checkAdminAccess()) {
                        GestioneUtenti.creaUtente();
                    }
                    break;
                case "3":
                    gestisciPrenotazioni();
                    break;
                case "0":
                    exit = true;
                    break;
                default:
                    System.out.println("Scelta non valida");
            }
        }
    }

    private static boolean checkAdminAccess() {
        System.out.print("Inserisci username admin: ");
        String username = sc.nextLine();
        System.out.print("Inserisci password: ");
        String password = sc.nextLine();
        Utente user = GestioneUtenti.getUtenteByName(username);
        if (user != null && user.isAdmin() && user.getPassword().equals(password)) {
            return true;
        } else {
            System.out.println("Accesso non consentito.");
            return false;
        }
    }

    private static void gestisciPrenotazioni() {
        System.out.print("Inserisci il tuo nome utente: ");
        String nomeUtente = sc.nextLine();
        Utente user = GestioneUtenti.getUtenteByName(nomeUtente);
        if (user == null) {
            System.out.println("Utente non trovato.");
            return;
        }

        System.out.print("Inserisci nome del laboratorio: ");
        String nomeLab = sc.nextLine();
        Laboratorio lab = GestioneLaboratori.getLaboratorioByName(nomeLab);
        if (lab == null) {
            System.out.println("Laboratorio non trovato.");
            return;
        }

        System.out.print("Inserisci orario (formato HH:mm-HH:mm): ");
        String orario = sc.nextLine().trim();
        System.out.println("Debug: " + orario);
        while (orario.isEmpty() || !orario.matches("^([01]?\\d|2[0-3]):[0-5]\\d-([01]?\\d|2[0-3]):[0-5]\\d$")) {
            System.out.println("Formato non valido. Inserisci l'orario nel formato HH:mm-HH:mm (es. 16:30-18:30).");
            orario = sc.nextLine().trim();
        }

        System.out.println("Debug: " + orario);

        // Verifica conflitti di prenotazione
        if (GestorePrenotazione.puoiPrenotare(user, lab, orario)) {
            Prenotazione pren = new Prenotazione(nomeUtente, nomeLab, orario, false);
            GestorePrenotazione.prenotaLaboratorio(pren);
            System.out.println("Prenotazione effettuata con successo.");
        } else {
            System.out.println("Impossibile effettuare la prenotazione per conflitti di orario o laboratorio già prenotato.");
        }
    }
}