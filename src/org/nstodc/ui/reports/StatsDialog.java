package org.nstodc.ui.reports;

import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class StatsDialog extends JDialog {

    public StatsDialog(UI owner, Map<Integer, String> names, Map<Integer, String> breeds, Map<Integer, String> suburbs) {
        super(owner, "Stats", null);
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());
        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////
        JPanel center = new JPanel(new GridLayout(0, 1));
        getContentPane().add(UiUtils.enFlow(center));
        JTextArea area = new JTextArea(10, 10);
        area.setEditable(false);
        JScrollPane scroller = new JScrollPane(area);
        center.add(scroller);
        scroller.setPreferredSize(new Dimension(200, 200));
        StringBuilder sb = new StringBuilder();
        sb.append("Most Popular Names\n");
        for (String s : names.values()) {
            sb.append(s).append("\n");
        }
        sb.append("\nMost Popular Breeds\n");
        for (String s : breeds.values()) {
            sb.append(s).append("\n");
        }
        sb.append("\nMost Popular Suburbs\n");
        for (String s : suburbs.values()) {
            sb.append(s).append("\n");
        }
        area.setText(sb.toString());

        //////////
        // East //
        //////////
        JButton okButton = UiUtils.addEast(this);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        pack();
        setResizable(false);

    }
}
