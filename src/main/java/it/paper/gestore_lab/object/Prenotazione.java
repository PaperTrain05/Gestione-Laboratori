package it.paper.gestore_lab.object;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Prenotazione {
    private final String nomeUtente;
    private final String laboratorio;
    private final String orario;  // Formato "HH:mm-HH:mm"
    private boolean scaduto;

    public Prenotazione(String nomeUtente, String laboratorio, String orario, boolean scaduto) {
        this.nomeUtente = nomeUtente;
        this.laboratorio = laboratorio;
        this.orario = orario;
        this.scaduto = scaduto;
    }

    public String getNomeUtente() {
        return nomeUtente;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public String getOrario() {
        return orario;
    }

    public boolean isScaduto() {
        return scaduto;
    }

    public void setScaduto(boolean scaduto) {
        this.scaduto = scaduto;
    }

    public LocalTime getOrarioInizio() {
        if (orario == null || orario.trim().isEmpty())
            throw new IllegalArgumentException("L'orario non può essere vuoto.");
        String[] parts = orario.split("-");
        if (parts.length < 2)
            throw new IllegalArgumentException("Formato orario non valido. Deve essere HH:mm-HH:mm.");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String inizio = parts[0].trim();
        if (!inizio.contains(":") && inizio.length() <= 2)
            inizio = inizio + ":00";
        try {
            return LocalTime.parse(inizio, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato orario di inizio non valido: " + inizio, e);
        }
    }

    public LocalTime getOrarioFine() {
        if (orario == null || orario.trim().isEmpty()) throw new IllegalArgumentException("L'orario non può essere vuoto.");

        String[] parts = orario.split("-");

        if (parts.length < 2) throw new IllegalArgumentException("Formato orario non valido. Deve essere HH:mm-HH:mm.");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        try {
            return LocalTime.parse(parts[1].trim(), formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato orario di fine non valido: " + parts[1].trim(), e);
        }
    }
}
