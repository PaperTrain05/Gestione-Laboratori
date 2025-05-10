package it.paper.gestore_lab.object;

public class Utente {
    private final String nome;
    private final String password;
    private final boolean admin;

    public Utente(String nome, String password, boolean admin) {
        this.nome = nome;
        this.password = password;
        this.admin = admin;
    }

    public String getNome() {
        return nome;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return admin;
    }

    @Override
    public String toString() {
        return nome;
    }
}
