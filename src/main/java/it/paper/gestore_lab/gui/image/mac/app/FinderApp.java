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
    private JTextField searchField;

    // Pannelli superiore: toolbar, tendina delle sottocartelle e breadcrumb
    private JPanel toolbarPanel;
    private JPanel dropdownPanel;
    private JComboBox<File> folderDropdown;
    private JPanel breadcrumbPanel;

    // Sidebar a sinistra
    private JPanel sidebarPanel;

    // Se currentDirectory è null, la vista mostra i drive (vista "This PC")
    // altrimenti, mostra il contenuto della directory corrente.
    private File currentDirectory;
    private FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    // Flag per evitare eventi indesiderati durante l'aggiornamento della tendina
    private boolean updatingDropdown = false;

    public FinderApp() {
        super("Finder", true, true, true, true);
        setSize(800, 600);
        setLayout(new BorderLayout());
        initUI();
        // Al primo avvio, currentDirectory è null (vista "This PC")
        currentDirectory = null;
        updateFileTable(currentDirectory);
    }

    private void initUI() {
        // ----------------- Panel Superiore: Toolbar + Dropdown + Breadcrumb -----------------
        // Toolbar: pulsanti Up, Search, Refresh
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JButton upButton = new JButton("Up");
        upButton.addActionListener(e -> {
            if (currentDirectory != null) {
                File parent = currentDirectory.getParentFile();
                if (parent != null) {
                    currentDirectory = parent;
                } else {
                    currentDirectory = null; // Torna alla vista dei dischi
                }
                updateFileTable(currentDirectory);
            }
        });
        toolbar.add(upButton);
        toolbar.addSeparator();
        toolbar.add(new JLabel("Search: "));
        searchField = new JTextField(20);
        toolbar.add(searchField);
        toolbar.addSeparator();
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> updateFileTable(currentDirectory));
        toolbar.add(refreshButton);

        // Tendina delle sottocartelle
        dropdownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        dropdownPanel.setBackground(new Color(245, 245, 245));
        JLabel dropdownLabel = new JLabel("Subfolders: ");
        dropdownPanel.add(dropdownLabel);
        folderDropdown = new JComboBox<>();
        folderDropdown.setPreferredSize(new Dimension(200, 25));
        // Aggiungiamo l'ActionListener solo se non siamo in fase di aggiornamento
        folderDropdown.addActionListener(e -> {
            if (!updatingDropdown) {
                File selected = (File) folderDropdown.getSelectedItem();
                if (selected != null) {
                    // Crea e visualizza una nuova istanza di FinderApp con la cartella selezionata
                    FinderApp newFinder = new FinderApp();
                    newFinder.currentDirectory = selected;
                    newFinder.updateFileTable(selected);
                    // Proviamo a posizionarla centrata nel JDesktopPane, se disponibile
                    JDesktopPane desktop = getDesktopPane();
                    if (desktop != null) {
                        desktop.add(newFinder);
                        Dimension ds = desktop.getSize();
                        Dimension fs = newFinder.getSize();
                        newFinder.setLocation((ds.width - fs.width) / 2, (ds.height - fs.height) / 2);
                    }
                    newFinder.setVisible(true);
                }
            }
        });
        dropdownPanel.add(folderDropdown);

        // Breadcrumb panel: la barra percorso
        breadcrumbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        breadcrumbPanel.setBackground(new Color(245, 245, 245));

        // TopPanel: organizza verticalmente toolbar, dropdown e breadcrumb
        toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
        toolbarPanel.add(toolbar);
        toolbarPanel.add(dropdownPanel);
        toolbarPanel.add(breadcrumbPanel);

        add(toolbarPanel, BorderLayout.NORTH);

        // ----------------- Sidebar a Sinistra -----------------
        sidebarPanel = createSidebarPanel();
        JScrollPane sidebarScroll = new JScrollPane(sidebarPanel);
        sidebarScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setPreferredSize(new Dimension(200, 0));

        // ----------------- Vista dei file/dischi a Destra -----------------
        fileTable = new JTable();
        fileTable.setFillsViewportHeight(true);
        fileTable.setRowHeight(24);
        fileTable.setDefaultRenderer(Object.class, new FileTableCellRenderer());
        JScrollPane tableScroll = new JScrollPane(fileTable);

        // ----------------- SplitPane: Sidebar e Vista -----------------
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarScroll, tableScroll);
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);
    }

    // Crea la sidebar in stile Finder: Favourites in alto e Devices (drives) sotto
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

        // Sezione "Devices"
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

    // Crea un bottone per la sidebar, con icona e nome prelevati da FileSystemView
    private JButton createSidebarButton(final File file) {
        String displayName = fileSystemView.getSystemDisplayName(file);
        if (displayName.isEmpty()) {
            displayName = file.getPath();
        }
        JButton button = new JButton(displayName, fileSystemView.getSystemIcon(file));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        button.setFocusPainted(false);
        button.setBackground(new Color(245, 245, 245));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setFont(new Font("Helvetica", Font.PLAIN, 12));
        button.addActionListener(e -> {
            currentDirectory = file;
            updateFileTable(currentDirectory);
        });
        return button;
    }

    // Aggiorna la JTable e la UI in base alla directory corrente:
    // Se currentDirectory è null => vista "This PC" (drive view),
    // altrimenti => vista standard della directory.
    private void updateFileTable(File directory) {
        if (directory == null) {
            // Vista drive (This PC)
            File[] drives = File.listRoots();
            String[] columns = {"Name", "Capacity", "Type"};
            Object[][] data;
            if (drives != null) {
                data = new Object[drives.length][3];
                for (int i = 0; i < drives.length; i++) {
                    File drive = drives[i];
                    data[i][0] = drive;  // Per il renderer: nome ed icona
                    data[i][1] = drive;  // Passa il drive al renderer per mostrare la barra di utilizzo
                    data[i][2] = drive;  // Per il renderer: tipo del drive
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
            updateFolderDropdown(null);
        } else {
            // Vista standard della directory
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
            updateFolderDropdown(directory);
        }
    }

    // Aggiorna la barra breadcrumb in base alla directory corrente.
    // Se directory è null => visualizza "This PC"
    // Altrimenti, crea dei bottoni per ogni livello della path.
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
                if (name.isEmpty())
                    name = part.getPath();
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

    // Aggiorna la tendina (dropdown) delle sottocartelle in base alla directory corrente.
    // Se directory è null, la tendina viene svuotata.
    private void updateFolderDropdown(File directory) {
        updatingDropdown = true;
        folderDropdown.removeAllItems();
        if (directory != null) {
            File[] subs = directory.listFiles((dir, name) -> new File(dir, name).isDirectory());
            if (subs != null) {
                for (File sub : subs) {
                    folderDropdown.addItem(sub);
                }
            }
        }
        updatingDropdown = false;
    }

    // Renderer personalizzato per la colonna "Name" (vista directory o drive)
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

    // Renderer per la colonna "Capacity" nella vista drive: mostra una barra di progresso con percentuale (JProgressBar)
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

    // Renderer per la colonna "Type" nella vista drive: mostra il tipo descrittivo del disco
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