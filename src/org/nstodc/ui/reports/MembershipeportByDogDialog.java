package org.nstodc.ui.reports;

import org.nstodc.database.Database;
import org.nstodc.database.type.Dog;
import org.nstodc.database.type.Membership;
import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MembershipeportByDogDialog extends JDialog {

    private UI owner;
    private DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    private JComboBox<String> combo = new JComboBox<>(model);
    private JLabel results = new JLabel("Results:");
    private JCheckBox lastYear = new JCheckBox("Last year");

    public MembershipeportByDogDialog(UI owner) {
        super(owner, "Membership Year Count", true);
        this.owner = owner;
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());
        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////
        model.addElement("Any");
        model.addElement("Obedience");
        model.addElement("Agility");
        model.addElement("DWD");

        JPanel center = new JPanel(new BorderLayout());
        getContentPane().add(UiUtils.enFlow(center));

        center.add(UiUtils.enFlow(lastYear), BorderLayout.NORTH);
        center.add(UiUtils.enFlow(new JLabel("Type"), combo), BorderLayout.CENTER);
        center.add(UiUtils.enFlow(results), BorderLayout.SOUTH);

        combo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateResults();
            }
        });
        lastYear.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateResults();
            }
        });

        //////////
        // East //
        //////////
        JButton okButton = UiUtils.addEast(this, false);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        pack();
        setResizable(false);

        updateResults();
    }

    private void updateResults() {
        combo.setEnabled(false);
        try {
            int count = 0;
            int year = UiUtils.defaultYear();
            if (lastYear.isSelected()) {
                year -= 1;
            }
            String type = (String) combo.getSelectedItem();
            Database database = owner.getDatabase();
            for (Membership membership : database.getMemberships()) {
                for (Dog dog : database.getDogs()) {
                    if (membership.getMembershipId() == dog.getMembershipId()) {
                        if (dog.getMembershipYear() >= year) {
                            if (type.equals("Any")) {
                                count++;
                                break;
                            } else if (type.equals("Obedience") && dog.isDoesObedience()) {
                                count++;
                                break;
                            } else if (type.equals("Agility") && dog.isDoesAgility()) {
                                count++;
                                break;
                            } else if (type.equals("DWD") && dog.isDoesDwd()) {
                                count++;
                                break;
                            }
                        }
                    }
                }
            }
            results.setText("Results: " + count);
        } finally {
            combo.setEnabled(true);
        }
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(MembershipeportByDogDialog.this, owner.getPreferences());
    }

}
