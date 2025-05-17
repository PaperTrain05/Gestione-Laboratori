package it.paper.gestore_lab.object;

public class Laboratorio {
    private String nome;
    private int qntPosti;
    private String gestoreLab;
    private int quantitaPC;
    private int switches;
    private int routers;
    private String indirizzoIP;
    private String subnetMask;

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
        return nome + ", posti:" + qntPosti + ", IP:" + indirizzoIP;
    }
}
