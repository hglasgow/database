package org.nstodc.ui.membership;

import org.nstodc.database.type.Handler;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Add / edit a handler.
 */
public class HandlerDialog extends JDialog {

    private final MembershipDialog owner;
    private final Handler handler;

    private JTextField firstNameTF = new JTextField(20);
    private JTextField lastNameTF = new JTextField(20);
    private JTextField crnTF = new JTextField(20);
    private JCheckBox primaryCB = new JCheckBox("Primary handler");

    public HandlerDialog(final MembershipDialog owner, final boolean nyoo, final Handler handler) {
        super(owner, (nyoo ? "Add" : "Update") + " Handler", true);
        this.owner = owner;
        this.handler = handler;
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());
        firstNameTF.setText(handler.getFirstName());
        lastNameTF.setText(handler.getLastName());
        crnTF.setText(handler.getCrn());
        primaryCB.setSelected(handler.isPrimary());

        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////

        JPanel centerOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel centerInnerPanel = new JPanel(new GridLayout(0, 1));
        centerOuterPanel.add(centerInnerPanel);
        getContentPane().add(centerOuterPanel, BorderLayout.CENTER);

        JLabel firstLabel = new JLabel("First name");
        JLabel lastLabel = new JLabel("Last name");
        JLabel pensionerLabel = new JLabel("Pensioner CRN");

        UiUtils.sameWidth(firstLabel, lastLabel, pensionerLabel);

        centerInnerPanel.add(UiUtils.enFlow(firstLabel, firstNameTF));
        centerInnerPanel.add(UiUtils.enFlow(lastLabel, lastNameTF));
        centerInnerPanel.add(UiUtils.enFlow(pensionerLabel, crnTF));
        JPanel primPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        primPanel.add(primaryCB);
        centerInnerPanel.add(primPanel);

        //////////
        // East //
        //////////

        JButton addButtonNext = null;
        JButton addButtonNextCopyLastName = null;
        if (nyoo) {
            addButtonNext = new JButton("Add Next");
            addButtonNext.addActionListener(e -> {
                if (validateIt()) {
                    handler.setFirstName(firstNameTF.getText().trim());
                    handler.setLastName(lastNameTF.getText().trim());
                    handler.setCrn(crnTF.getText().trim());
                    handler.setPrimary(primaryCB.isSelected());
                    saveNew();
                    SwingUtilities.invokeLater(() -> owner.addHandler());
                    dispose();
                }
            });
            addButtonNextCopyLastName = new JButton("ANCL");
            addButtonNextCopyLastName.addActionListener(e -> {
                if (validateIt()) {
                    handler.setFirstName(firstNameTF.getText().trim());
                    handler.setLastName(lastNameTF.getText().trim());
                    handler.setCrn(crnTF.getText().trim());
                    handler.setPrimary(primaryCB.isSelected());
                    saveNew();
                    SwingUtilities.invokeLater(() -> owner.addHandler(lastNameTF.getText().trim()));
                    dispose();
                }
            });
        }

        JButton okButton = UiUtils.addEast(this, addButtonNext, addButtonNextCopyLastName);
        okButton.addActionListener(e -> {
            if (validateIt()) {
                handler.setFirstName(firstNameTF.getText().trim());
                handler.setLastName(lastNameTF.getText().trim());
                handler.setCrn(crnTF.getText().trim());
                handler.setPrimary(primaryCB.isSelected());
                if (nyoo) {
                    saveNew();
                } else {
                    saveExisting();
                }
                dispose();
            }
        });

        pack();
        setResizable(false);
    }

    private void saveExisting() {
        owner.updateHandler(handler);
    }

    private void saveNew() {
        owner.addHandler(handler);
    }

    private boolean validateIt() {
        String first = firstNameTF.getText().trim();
        String last = lastNameTF.getText().trim();

        if (first.length() == 0) {
            JOptionPane.showMessageDialog(this, "First name required.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (last.length() == 0) {
            JOptionPane.showMessageDialog(this, "Last name required.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String firstLetter = first.substring(0, 1);
        if (firstLetter.matches("[a-z]")) {
            String firstCandidate = firstLetter.toUpperCase() + first.substring(1);
            int q = JOptionPane.showConfirmDialog(this, "Convert first name from '" + first + "' to '" + firstCandidate + "'?", "First Name", JOptionPane.YES_NO_CANCEL_OPTION);
            if (q == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (q == JOptionPane.YES_OPTION) {
                firstNameTF.setText(firstCandidate);
            }
        }
        String lastLetter = last.substring(0, 1);
        if (lastLetter.matches("[a-z]")) {
            String lastCandidate = lastLetter.toUpperCase() + last.substring(1);
            int q = JOptionPane.showConfirmDialog(this, "Convert last name from '" + last + "' to '" + lastCandidate + "'?", "Last Name", JOptionPane.YES_NO_CANCEL_OPTION);
            if (q == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (q == JOptionPane.YES_OPTION) {
                lastNameTF.setText(lastCandidate);
            }
        }
        return true;
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(HandlerDialog.this, owner.getPreferences());
    }

}
