package it.paper.gestore_lab.gui.image.mac;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class MacInternalFrameUI extends BasicInternalFrameUI {

    public MacInternalFrameUI(JInternalFrame b) {
        super(b);
    }

    @Override
    protected JComponent createNorthPane(JInternalFrame w) {
        return new MacInternalFrameTitlePane(w);
    }
}
