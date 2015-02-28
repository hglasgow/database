package org.nstodc.ui.membership;

import org.nstodc.database.type.Handler;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

    public HandlerDialog(MembershipDialog owner, final boolean nyoo, final Handler handler) {
        super(owner, (nyoo ? "Add" : "Update") + " Handler", true);
        this.owner = owner;
        this.handler = handler;
        UiUtils.locate(this, owner.getPreferences());
        firstNameTF.setText(handler.getFirstName());
        lastNameTF.setText(handler.getLastName());
        crnTF.setText(handler.getCrn());
        primaryCB.setSelected(handler.isPrimary());

        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////

        JPanel centerOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel centerInnerPanel = new JPanel(new GridLayout(0,1));
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

        JButton okButton = UiUtils.addEast(this);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        return true;
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(HandlerDialog.this, owner.getPreferences());
    }

}
