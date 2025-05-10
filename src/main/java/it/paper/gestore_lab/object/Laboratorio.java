package it.paper.gestore_lab.object;

public class Laboratorio {
    private final String nome;
    private final int qntPosti;
    private final String gestoreLab;
    private final int quantitaPC;
    private final int switches;
    private final int routers;
    private final String indirizzoIP;
    private final String subnetMask;

    public Laboratorio(String nome, int qntPosti, String gestoreLab, int quantitaPC, int switches, int routers, String indirizzoIP, String subnetMask) {
        this.nome = nome;
        this.qntPosti = qntPosti;
        this.gestoreLab = gestoreLab;
        this.quantitaPC = quantitaPC;
        this.switches = switches;
        this.routers = routers;
        this.indirizzoIP = indirizzoIP;
        this.subnetMask = subnetMask;
    }

    public String getNome() {
        return nome;
    }

    public int getQntPosti() {
        return qntPosti;
    }

    public String getGestoreLab() {
        return gestoreLab;
    }

    public int getQuantitaPC() {
        return quantitaPC;
    }

    public int getSwitches() {
        return switches;
    }

    public int getRouters() {
        return routers;
    }

    public String getIndirizzoIP() {
        return indirizzoIP;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    @Override
    public String toString() {
        return nome + ", posti: " + qntPosti + ", IP: " + indirizzoIP;
    }
}
