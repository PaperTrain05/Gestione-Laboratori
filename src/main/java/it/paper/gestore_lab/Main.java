package it.paper.gestore_lab;

import it.paper.gestore_lab.manager.GestioneLaboratori;
import it.paper.gestore_lab.manager.GestioneUtenti;
import it.paper.gestore_lab.manager.GestoreAvvio;
import it.paper.gestore_lab.manager.GestorePrenotazione;
import it.paper.gestore_lab.utils.FileUtils;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static final String BASE_PATH = getBasePath();

    public static void main(String[] args) {
        // Crea le cartelle necessarie
        FileUtils.createDirs(BASE_PATH);

        // Carica utenti, laboratori e prenotazioni in cache dai file
        GestioneUtenti.caricaUtenti(BASE_PATH + File.separator + "utenti");
        GestioneLaboratori.caricaLaboratori(BASE_PATH + File.separator + "laboratori");
        GestorePrenotazione.caricaPrenotazioni(BASE_PATH + File.separator + "prenotazioni");

        // Avvia un job schedulato per controllare le prenotazioni scadute ogni 5 minuti
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            GestorePrenotazione.controllaPrenotazioniScadute();
        }, 0, 5, TimeUnit.MINUTES);

        // Se non esistono utenti e laboratori, è il primo avvio
        if (GestioneUtenti.utentiCache.isEmpty() && GestioneLaboratori.laboratoriCache.isEmpty()) {
            GestoreAvvio.avvioPrimaEsecuzione();
        } else {
            GestoreAvvio.avvioNormale();
        }
    }

    private static String getBasePath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "C:" + File.separator + "PCTO_Mameli";
        } else {
            return System.getProperty("user.home") + File.separator + "PCTO_Mameli";
        }
    }
}
