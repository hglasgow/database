package org.nstodc.ui.reports;

import org.nstodc.database.Database;
import org.nstodc.database.type.Dog;
import org.nstodc.database.type.Handler;
import org.nstodc.database.type.Membership;
import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MembershipCountReportDialog extends JDialog {

    private UI owner;
    private DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
    private JComboBox<String> combo = new JComboBox<String>(model);
    private JLabel results = new JLabel("Results:");

    public MembershipCountReportDialog(UI owner) {
        super(owner, "Membership Count", true);
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

        getContentPane().add(UiUtils.enFlow(new JLabel("Type"), combo), BorderLayout.CENTER);
        getContentPane().add(UiUtils.enFlow(results), BorderLayout.SOUTH);
        combo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
            String type = (String) combo.getSelectedItem();
            Database database = owner.getDatabase();
            for (Membership membership : database.getMemberships()) {
                dogs:
                for (Dog dog : database.getDogs()) {
                    if (membership.getMembershipId() == dog.getMembershipId()) {
                        if (dog.getMembershipYear() >= UiUtils.defaultYear()) {
                            if (type.equals("Any")) {
                                count++;
                                break dogs;
                            } else if (type.equals("Obedience") && dog.isDoesObedience()) {
                                count++;
                                break dogs;
                            } else if (type.equals("Agility") && dog.isDoesAgility()) {
                                count++;
                                break dogs;
                            } else if (type.equals("DWD") && dog.isDoesDwd()) {
                                count++;
                                break dogs;
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
        UiUtils.updateLocation(MembershipCountReportDialog.this, owner.getPreferences());
    }

}
