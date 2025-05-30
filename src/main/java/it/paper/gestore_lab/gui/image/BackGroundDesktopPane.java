package it.paper.gestore_lab.gui.image;

import javax.swing.*;
import java.awt.*;

public class BackGroundDesktopPane extends JDesktopPane {

    private Image backgroundImage;

    public BackGroundDesktopPane(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // Disegna l'immagine di sfondo, ridimensionandola all'area del desktop
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
