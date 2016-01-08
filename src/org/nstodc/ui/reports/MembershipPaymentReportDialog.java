package org.nstodc.ui.reports;

import org.nstodc.database.Database;
import org.nstodc.database.type.Dog;
import org.nstodc.database.type.Membership;
import org.nstodc.database.type.Payment;
import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MembershipPaymentReportDialog extends JDialog {

    private UI owner;
    private JLabel results = new JLabel("Results:");
    private JCheckBox lastYear = new JCheckBox("Last year");

    public MembershipPaymentReportDialog(UI owner) {
        super(owner, "Membership Payment Count", true);
        this.owner = owner;
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());
        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////
        getContentPane().add(UiUtils.enFlow(lastYear), BorderLayout.NORTH);
        getContentPane().add(UiUtils.enFlow(results), BorderLayout.SOUTH);
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
        int count = 0;
        int year = UiUtils.defaultYear();
        if (lastYear.isSelected()) {
            year -= 1;
        }
        Database database = owner.getDatabase();
        for (Membership membership : database.getMemberships()) {
            for (Payment payment : database.getPayments()) {
                if (membership.getMembershipId() == payment.getMembershipId()) {
                    if (payment.getYear() == year) {
                        count++;
                        break;
                    }
                }
            }
        }
        results.setText("Results: " + count);
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(MembershipPaymentReportDialog.this, owner.getPreferences());
    }

}
