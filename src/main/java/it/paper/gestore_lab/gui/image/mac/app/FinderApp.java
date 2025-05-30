package it.paper.gestore_lab.gui.image.mac.app;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FinderApp extends JInternalFrame {

    // Vista principale
    private JTable fileTable;
    private JSplitPane splitPane;

    // Pannelli superiori: Category bar e Breadcrumb
    private JPanel categoryPanel;
    private JPanel breadcrumbPanel;

    // Sidebar a sinistra
    private JPanel sidebarPanel;

    // La directory corrente; se è null, viene mostrata la vista "This PC" (dischi)
    private File currentDirectory;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    public FinderApp() {
        super("Finder", true, true, true, true);
        setSize(800, 600);
        setLayout(new BorderLayout());
        initUI();
        // Al primo avvio currentDirectory è null => vista "This PC"
        currentDirectory = null;
        updateFileTable(currentDirectory);
    }

    private void initUI() {
        // ---------- Top Panel: Category Bar + Breadcrumb ----------
        // Category Bar: come nelle tendine dell'immagine del Finder di Mac
        categoryPanel = createCategoryPanel();
        // Breadcrumb Panel: mostra la path corrente
        breadcrumbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        breadcrumbPanel.setBackground(new Color(245, 245, 245));

        // Il topPanel è un pannello verticale
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(categoryPanel);
        topPanel.add(breadcrumbPanel);
        add(topPanel, BorderLayout.NORTH);

        // ---------- Sidebar a sinistra ----------
        sidebarPanel = createSidebarPanel();
        JScrollPane sidebarScroll = new JScrollPane(sidebarPanel);
        sidebarScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setPreferredSize(new Dimension(200, 0));

        // ---------- Vista dei file/dischi ----------
        fileTable = new JTable();
        fileTable.setFillsViewportHeight(true);
        fileTable.setRowHeight(24);
        fileTable.setDefaultRenderer(Object.class, new FileTableCellRenderer());
        JScrollPane tableScroll = new JScrollPane(fileTable);

        // ---------- SplitPane: Sidebar e Vista dei file ----------
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarScroll, tableScroll);
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);
    }

    // Crea la Category Bar (tendine in alto) con i bottoni per le cartelle standard
    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(240, 240, 240)); // colore chiaro, simile a macOS

        // Elenco di categorie (in ordine: Documents, Music, Movies, Pictures, Desktop, Applications)
        // Per "Applications" su Windows simuliamo con "Program Files"
        String userHome = System.getProperty("user.home");
        Object[][] categories = {
                {"Documents", new File(userHome + File.separator + "Documents")},
                {"Music",     new File(userHome + File.separator + "Music")},
                {"Movies",    new File(userHome + File.separator + "Videos")}, // Videos su Windows
                {"Pictures",  new File(userHome + File.separator + "Pictures")},
                {"Desktop",   new File(userHome + File.separator + "Desktop")},
                {"Applications", new File("C:" + File.separator + "Program Files")}
        };

        for (Object[] cat : categories) {
            String label = (String) cat[0];
            File folder = (File) cat[1];
            JButton btn = new JButton(label, fileSystemView.getSystemIcon(folder));
            btn.setFocusPainted(false);
            btn.setFont(new Font("Helvetica", Font.PLAIN, 12));
            btn.addActionListener(e -> openNewExplorerForCategory(folder));
            panel.add(btn);
        }
        return panel;
    }

    // Quando si clicca su una categoria, apre un nuovo FinderApp per quella cartella
    private void openNewExplorerForCategory(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            FinderApp newFinder = new FinderApp();
            newFinder.currentDirectory = folder;
            newFinder.updateFileTable(folder);
            JDesktopPane desktop = getDesktopPane();
            if (desktop != null) {
                desktop.add(newFinder);
                Dimension ds = desktop.getSize();
                Dimension fs = newFinder.getSize();
                newFinder.setLocation((ds.width - fs.width) / 2, (ds.height - fs.height) / 2);
            }
            newFinder.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Cartella non disponibile: " + folder.getAbsolutePath());
        }
    }

    // Crea la Sidebar in stile Finder: prima i Favourites (Favorites) poi Devices (dischi)
    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));

        // Sezione "Favourites"
        JLabel favouritesLabel = new JLabel("Favourites");
        favouritesLabel.setForeground(Color.GRAY);
        favouritesLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        panel.add(favouritesLabel);

        String userHome = System.getProperty("user.home");
        File desktop = new File(userHome + File.separator + "Desktop");
        File documents = new File(userHome + File.separator + "Documents");
        File downloads = new File(userHome + File.separator + "Downloads");

        panel.add(createSidebarButton(desktop));
        panel.add(createSidebarButton(documents));
        panel.add(createSidebarButton(downloads));

        panel.add(Box.createVerticalStrut(15));

        // Sezione "Devices": elenca i drive del sistema
        JLabel devicesLabel = new JLabel("Devices");
        devicesLabel.setForeground(Color.GRAY);
        devicesLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        panel.add(devicesLabel);
        File[] drives = File.listRoots();
        if (drives != null) {
            for (File drive : drives) {
                panel.add(createSidebarButton(drive));
            }
        }
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    // Crea un bottone della sidebar (con icona e testo da FileSystemView)
    private JButton createSidebarButton(File file) {
        String displayName = fileSystemView.getSystemDisplayName(file);
        if (displayName.isEmpty()) {
            displayName = file.getPath();
        }
        JButton btn = new JButton(displayName, fileSystemView.getSystemIcon(file));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btn.getPreferredSize().height));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(245, 245, 245));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setFont(new Font("Helvetica", Font.PLAIN, 12));
        btn.addActionListener(e -> {
            currentDirectory = file;
            updateFileTable(currentDirectory);
        });
        return btn;
    }

    // Aggiorna la vista della JTable in base alla directory corrente.
    // Se currentDirectory è null, mostra la vista "This PC" (drive view)
    // Altrimenti, mostra la lista dei file della directory.
    private void updateFileTable(File directory) {
        if (directory == null) {
            // Vista "This PC": elenca i drive
            File[] drives = File.listRoots();
            String[] columns = {"Name", "Capacity", "Type"};
            Object[][] data;
            if (drives != null) {
                data = new Object[drives.length][3];
                for (int i = 0; i < drives.length; i++) {
                    File drive = drives[i];
                    data[i][0] = drive;  // Per il renderer: nome e icona
                    data[i][1] = drive;  // Passa il drive al renderer per la barra di utilizzo
                    data[i][2] = drive;  // Per il renderer: tipo
                }
            } else {
                data = new Object[0][3];
            }
            DefaultTableModel model = new DefaultTableModel(data, columns) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
            fileTable.setModel(model);
            fileTable.getColumnModel().getColumn(0).setPreferredWidth(200);
            fileTable.getColumnModel().getColumn(1).setPreferredWidth(300);
            fileTable.getColumnModel().getColumn(0).setCellRenderer(new FileTableCellRenderer());
            fileTable.getColumnModel().getColumn(1).setCellRenderer(new DiskUsageRenderer());
            fileTable.getColumnModel().getColumn(2).setCellRenderer(new DriveTypeRenderer());
            updateBreadcrumbPanel(null);
        } else {
            // Vista directory: elenca file e cartelle
            File[] files = directory.listFiles();
            String[] columns = {"Name", "Date Modified", "Size", "Type"};
            Object[][] data;
            if (files != null) {
                data = new Object[files.length][4];
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    data[i][0] = f;
                    data[i][1] = sdf.format(f.lastModified());
                    data[i][2] = f.isDirectory() ? "" : f.length();
                    data[i][3] = f.isDirectory() ? "Folder" : "File";
                }
            } else {
                data = new Object[0][4];
            }
            DefaultTableModel model = new DefaultTableModel(data, columns) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
            fileTable.setModel(model);
            fileTable.getColumnModel().getColumn(0).setPreferredWidth(250);
            fileTable.getColumnModel().getColumn(0).setCellRenderer(new FileTableCellRenderer());
            updateBreadcrumbPanel(directory);
        }
    }

    // Aggiorna la barra breadcrumb in base alla directory corrente.
    // Se directory è null visualizza "This PC", altrimenti crea bottoni per ogni livello.
    private void updateBreadcrumbPanel(File directory) {
        breadcrumbPanel.removeAll();
        if (directory == null) {
            JLabel label = new JLabel("This PC");
            label.setFont(new Font("Helvetica", Font.BOLD, 14));
            breadcrumbPanel.add(label);
        } else {
            List<File> pathParts = new ArrayList<>();
            File temp = directory;
            while (temp != null) {
                pathParts.add(0, temp);
                temp = temp.getParentFile();
            }
            for (int i = 0; i < pathParts.size(); i++) {
                File part = pathParts.get(i);
                String name = fileSystemView.getSystemDisplayName(part);
                if (name.isEmpty()) {
                    name = part.getPath();
                }
                JButton btn = new JButton(name);
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                btn.setBackground(new Color(230, 230, 230));
                btn.setFont(new Font("Helvetica", Font.PLAIN, 12));
                btn.addActionListener(e -> {
                    currentDirectory = part;
                    updateFileTable(part);
                });
                breadcrumbPanel.add(btn);
                if (i < pathParts.size() - 1) {
                    breadcrumbPanel.add(new JLabel(">"));
                }
            }
        }
        breadcrumbPanel.revalidate();
        breadcrumbPanel.repaint();
    }

    // Renderer per la colonna "Name" (vista directory o drive)
    private class FileTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            if (value instanceof File) {
                File file = (File) value;
                setText(fileSystemView.getSystemDisplayName(file));
                setIcon(fileSystemView.getSystemIcon(file));
            } else {
                setIcon(null);
            }
            return comp;
        }
    }

    // Renderer per la colonna "Capacity" nella vista drive: mostra una barra di progresso
    private class DiskUsageRenderer extends JProgressBar implements javax.swing.table.TableCellRenderer {
        public DiskUsageRenderer() {
            setStringPainted(true);
            setMinimum(0);
            setMaximum(100);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof File) {
                File drive = (File) value;
                long total = drive.getTotalSpace();
                if (total > 0) {
                    long free = drive.getFreeSpace();
                    long used = total - free;
                    int percent = (int) (used * 100 / total);
                    setValue(percent);
                    double freeGB = free / (1024.0 * 1024 * 1024);
                    double totalGB = total / (1024.0 * 1024 * 1024);
                    setString(String.format("%d%% used (%.1fGB free / %.1fGB total)", percent, freeGB, totalGB));
                } else {
                    setValue(0);
                    setString("N/A");
                }
            }
            return this;
        }
    }

    // Renderer per la colonna "Type" nella vista drive: mostra il tipo del disco
    private class DriveTypeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            if (value instanceof File) {
                File drive = (File) value;
                setText(fileSystemView.getSystemTypeDescription(drive));
            }
            return comp;
        }
    }
}