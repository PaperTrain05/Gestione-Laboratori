package it.paper.gestore_lab.gui;

import it.paper.gestore_lab.gui.image.BackGroundDesktopPane;
import it.paper.gestore_lab.gui.image.mac.MacInternalFrameUI;
import it.paper.gestore_lab.gui.image.mac.app.FinderApp;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.File;

public class OSFrame extends JFrame {
    private BackGroundDesktopPane desktopPane;
    private JPanel dockPanel;

    // Specifica il percorso dell'immagine di sfondo: modificare come necessario
    public static final String BACKGROUND_IMAGE_PATH = "C:\\PCTO_Mameli\\MacOS_BackGround.jpg";

    public OSFrame(String osName) {
        setTitle("Simulazione " + osName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);  // Dimensione della finestra principale
        setLocationRelativeTo(null);
        initUI(osName);
    }

    private void initUI(String osName) {
        setLayout(new BorderLayout());

        // Barra dei menu in stile MacOS (in alto)
        JMenuBar menuBar = new JMenuBar();
        JMenu appleMenu = new JMenu("");  // Il simbolo Apple
        appleMenu.setFont(new Font("Apple Color Emoji", Font.PLAIN, 16));
        JMenuItem aboutItem = new JMenuItem("About " + osName);
        appleMenu.add(aboutItem);
        menuBar.add(appleMenu);

        // Il menu File, per ora non apre nuove finestre
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        // Carica l'immagine di sfondo; se non si trova l'immagine, si usa un colore di sfondo standard.
        Image bgImage = null;
        try {
            bgImage = new ImageIcon(BACKGROUND_IMAGE_PATH).getImage();
        } catch (Exception e) {
            System.err.println("Immagine di sfondo non trovata, uso uno sfondo di default.");
        }
        desktopPane = new BackGroundDesktopPane(bgImage);
        desktopPane.setBackground(new Color(230, 230, 250)); // colore di fallback
        add(desktopPane, BorderLayout.CENTER);

        // Aggiungi la dock in basso
        dockPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        dockPanel.setBackground(new Color(50, 50, 50, 220)); // dock scura semi-trasparente
        addDockIcons();
        add(dockPanel, BorderLayout.SOUTH);
    }

    // Aggiunge le icone "virtuali" alla dock.
    private void addDockIcons() {
        String[] appNames = {"Finder", "Safari", "Mail", "Notes", "Terminal"};
        for (String appName : appNames) {
            JLabel iconLabel = new JLabel(appName, SwingConstants.CENTER);
            iconLabel.setPreferredSize(new Dimension(50, 50));
            iconLabel.setForeground(Color.WHITE);
            iconLabel.setFont(new Font("Helvetica", Font.PLAIN, 12));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            iconLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    iconLabel.setPreferredSize(new Dimension(70, 70));
                    iconLabel.setFont(new Font("Helvetica", Font.BOLD, 16));
                    dockPanel.revalidate();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    iconLabel.setPreferredSize(new Dimension(50, 50));
                    iconLabel.setFont(new Font("Helvetica", Font.PLAIN, 12));
                    dockPanel.revalidate();
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(appName.equals("Finder")) {
                        FinderApp finder = new FinderApp();
                        finder.setUI(new MacInternalFrameUI(finder));

                        // Centra il FinderApp nel desktop
                        Dimension desktopSize = desktopPane.getSize();
                        Dimension frameSize = finder.getSize();
                        int x = (desktopSize.width - frameSize.width) / 2;
                        int y = (desktopSize.height - frameSize.height) / 2;
                        finder.setLocation(x, y);

                        desktopPane.add(finder);
                        finder.setVisible(true);
                        try {
                            finder.setSelected(true);
                        } catch (PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        openInternalFrame(appName);
                    }
                }
            });

            dockPanel.add(iconLabel);
        }
    }

    // Apre una nuova finestra interna nel JDesktopPane usando la nostra UI personalizzata
    private void openInternalFrame(String appName) {
        JInternalFrame internalFrame = new JInternalFrame("Nuova Finestra - " + appName, true, true, true, true);
        internalFrame.setUI(new MacInternalFrameUI(internalFrame));
        internalFrame.setSize(300, 200);

        // Calcola la posizione centrale nel desktop
        Dimension desktopSize = desktopPane.getSize();
        Dimension frameSize = internalFrame.getSize();
        int x = (desktopSize.width - frameSize.width) / 2;
        int y = (desktopSize.height - frameSize.height) / 2;
        internalFrame.setLocation(x, y);

        internalFrame.getContentPane().setBackground(Color.WHITE);
        internalFrame.getContentPane().add(new JLabel("Contenuto per " + appName, SwingConstants.CENTER), BorderLayout.CENTER);
        desktopPane.add(internalFrame);
        internalFrame.setVisible(true);
        try {
            internalFrame.setSelected(true);
        } catch (PropertyVetoException ex) {
            ex.printStackTrace();
        }
    }

}
