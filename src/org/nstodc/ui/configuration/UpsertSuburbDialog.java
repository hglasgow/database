package org.nstodc.ui.configuration;

import org.nstodc.database.type.Suburb;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Add / edit a handler.
 */
public class UpsertSuburbDialog extends JDialog {

    private final SuburbsDialog owner;
    private final Suburb suburb;

    private final JTextField suburbNameTF = new JTextField(20);
    private final JTextField postcodeTF = new JTextField(20);

    public UpsertSuburbDialog(SuburbsDialog owner, final boolean nyoo, final Suburb suburb) {
        super(owner, (nyoo ? "Add" : "Update") + " Suburb", true);
        this.owner = owner;
        this.suburb = suburb;
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());

        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////

        JPanel centerOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel centerInnerPanel = new JPanel(new GridLayout(0,1));
        centerOuterPanel.add(centerInnerPanel);
        getContentPane().add(centerOuterPanel, BorderLayout.CENTER);

        centerInnerPanel.add(UiUtils.enFlow(new JLabel("Suburb"), suburbNameTF));
        centerInnerPanel.add(UiUtils.enFlow(new JLabel("Postcode"), postcodeTF));

        suburbNameTF.setText(suburb.getSuburb());
        postcodeTF.setText(suburb.getPostcode());

        //////////
        // East //
        //////////

        JButton okButton = UiUtils.addEast(this);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateIt()) {
                    suburb.setSuburb(suburbNameTF.getText().trim().toUpperCase());
                    suburb.setPostcode(postcodeTF.getText().trim());

                    if (nyoo) {
                        saveNew();
                    } else {
                        saveExisting();
                    }
                    dispose();
                }
            }
        });

        pack();
        setResizable(false);
    }

    private void saveNew() {
        owner.addSuburb(suburb);
    }

    private void saveExisting() {
        owner.updateSuburb(suburb);
    }

    private boolean validateIt() {
        // Suburb must have a name
        if (suburbNameTF.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Name required.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Suburb must have a 4-digit postcode
        if (postcodeTF.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Postcode required.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int postcode = Integer.parseInt(postcodeTF.getText().trim());
            if (postcode < 100 || postcode > 9999) {
                JOptionPane.showMessageDialog(this, "Postcode out of range.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Postcode invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Make sure we don't already have this.
        for (Suburb suburb1 : owner.getDatabase().getSuburbs()) {
            if (suburb1.getSuburb().equalsIgnoreCase(suburbNameTF.getText().trim()) &&
                    suburb1.getSuburbId() != suburb.getSuburbId()) {
                JOptionPane.showMessageDialog(this, "Duplicate suburb.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(UpsertSuburbDialog.this, owner.getPreferences());
    }

}
