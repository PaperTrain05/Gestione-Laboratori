package it.paper.gestore_lab.gui.image.mac;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;

public class MacInternalFrameTitlePane extends BasicInternalFrameTitlePane {
    private JInternalFrame frameRef; // Riferimento al JInternalFrame
    private JLabel titleLabel;        // Etichetta per il titolo, centrata

    public MacInternalFrameTitlePane(JInternalFrame f) {
        super(f);
        this.frameRef = f;
        removeAll();  // Rimuove tutti i componenti ereditati

        // Imposta il layout su BorderLayout
        setLayout(new BorderLayout());

        // Pannello sinistro: contiene solamente i pulsanti rotondi
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftPanel.setOpaque(false);

        // Crea i pulsanti standard (chiudi, minimizza, espandi)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        controlPanel.setOpaque(false);

        JButton closeB = createRoundButton(Color.RED, "Chiudi", "X");
        closeB.addActionListener(e -> frameRef.dispose());

        JButton minimizeB = createRoundButton(Color.YELLOW, "Minimizza", "–");
        minimizeB.addActionListener(e -> {
            try {
                frameRef.setIcon(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JButton maximizeB = createRoundButton(Color.GREEN, "Espandi", "<>");
        maximizeB.addActionListener(e -> {
            try {
                if (frameRef.isMaximum()) {
                    frameRef.setMaximum(false);
                } else {
                    frameRef.setMaximum(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        controlPanel.add(closeB);
        controlPanel.add(minimizeB);
        controlPanel.add(maximizeB);
        leftPanel.add(controlPanel);
        add(leftPanel, BorderLayout.WEST);

        // Aggiunge un pannello vuoto nella regione EAST
        // (in modo che il componente centrale risulti centrato)
        JPanel eastPanel = new JPanel();
        eastPanel.setOpaque(false);
        eastPanel.setPreferredSize(leftPanel.getPreferredSize());
        add(eastPanel, BorderLayout.EAST);

        // Pannello centrale: JLabel per il titolo, centrato
        titleLabel = new JLabel(frameRef.getTitle(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Helvetica", Font.BOLD, 14));
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, BorderLayout.CENTER);
    }

    // Metodo helper per creare un pulsante rotondo
    private JButton createRoundButton(Color color, String tooltip, String text) {
        RoundButton btn = new RoundButton(text);
        btn.setBackground(color);
        btn.setToolTipText(tooltip);
        return btn;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 30);
    }

    // Sovrascrivi il metodo paintComponent per non ridipingere il testo di default
    @Override
    public void paintComponent(Graphics g) {
        // Dipingi solo lo sfondo (oppure uno sfondo trasparente)
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        // Non chiamare super.paintComponent(g) per evitare che il testo di default venga disegnato
    }

    // Classe interna per un pulsante rotondo
    public static class RoundButton extends JButton {
        public RoundButton(String text) {
            super(text);
            setFont(new Font("Dialog", Font.BOLD, 10));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape circle = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
            g2.setColor(getBackground());
            g2.fill(circle);

            FontMetrics fm = g2.getFontMetrics();
            int stringWidth = fm.stringWidth(getText());
            int stringHeight = fm.getAscent();
            g2.setColor(getForeground());
            g2.drawString(getText(), (getWidth() - stringWidth) / 2, (getHeight() + stringHeight) / 2 - 2);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(20, 20);
        }

        @Override
        public boolean contains(int x, int y) {
            Shape circle = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
            return circle.contains(x, y);
        }
    }
}
