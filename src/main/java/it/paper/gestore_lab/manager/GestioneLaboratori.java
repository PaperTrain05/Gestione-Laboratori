package it.paper.gestore_lab.manager;

import it.paper.gestore_lab.object.Laboratorio;
import it.paper.gestore_lab.utils.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestioneLaboratori {
    private List<Laboratorio> laboratoriCache = new ArrayList<>();

    public List<Laboratorio> getLaboratoriCache() {
        return laboratoriCache;
    }

    public void caricaLaboratori(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) return;

        for (File f : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String[] partsNome = br.readLine().split(":", 2);
                String nome = partsNome[1].trim();
                String[] partsPosti = br.readLine().split(":", 2);
                int qntPosti = Integer.parseInt(partsPosti[1].trim());
                String[] partsGestore = br.readLine().split(":", 2);
                String gestoreLab = partsGestore[1].trim();
                String[] partsPc = br.readLine().split(":", 2);
                int quantitaPC = Integer.parseInt(partsPc[1].trim());
                String[] partsSwitches = br.readLine().split(":", 2);
                int switches = Integer.parseInt(partsSwitches[1].trim());
                String[] partsRouters = br.readLine().split(":", 2);
                int routers = Integer.parseInt(partsRouters[1].trim());
                String[] partsIP = br.readLine().split(":", 2);
                String indIP = partsIP[1].trim();
                String[] partsSubnet = br.readLine().split(":", 2);
                String subnet = partsSubnet[1].trim();

                laboratoriCache.add(new Laboratorio(nome, qntPosti, gestoreLab, quantitaPC, switches, routers, indIP, subnet));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Laboratorio getLaboratorioByName(String nome) {
        for (Laboratorio lab : laboratoriCache) {
            if (lab.getNome().equalsIgnoreCase(nome)) return lab;
        }

        return null;
    }

    public void salvaLaboratorio(Laboratorio lab, String directoryPath) {
        String fileName = directoryPath + File.separator + lab.getNome() + ".txt";

        String content = "nome: " + lab.getNome() + "\n" +
                "qnt_posti: " + lab.getQntPosti() + "\n" +
                "gestore_lab: " + lab.getGestoreLab() + "\n" +
                "quantita_pc: " + lab.getQuantitaPC() + "\n" +
                "switches: " + lab.getSwitches() + "\n" +
                "routers: " + lab.getRouters() + "\n" +
                "ind_ip: " + lab.getIndirizzoIP() + "\n" +
                "subnet_mask: " + lab.getSubnetMask();

        FileUtils.writeToFile(fileName, content);
    }
}
