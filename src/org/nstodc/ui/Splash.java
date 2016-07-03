package org.nstodc.ui;

import javax.swing.*;
import java.awt.*;

public class Splash extends JWindow {

    private final JProgressBar bar = new JProgressBar();

    private float f = 10;
    private int lastStartTime;
    public Splash(Frame owner) {
        super(owner);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(UiUtils.enFlow(bar), BorderLayout.CENTER);
        bar.setValue((int)f);
        pack();
    }

    public void setPosition() {
        int x = (getOwner().getWidth() - getWidth()) / 2 + getOwner().getX();
        int y = (getOwner().getHeight() - getHeight()) / 2 + getOwner().getY();
        setLocation(x, y);
    }

    public void uptick() {
        SwingUtilities.invokeLater(() -> {
            f += 50000.0 / lastStartTime;
            if (f > 100) {
                f = 100;
            }
            bar.setValue((int)f);
        });
    }

    public void setLastStartTime(int lastStartTime) {
        this.lastStartTime = lastStartTime;
    }
}
