package org.nstodc.ui.configuration;

import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Update the default payment amount.
 */
public class PaymentAmountDialog extends JDialog {

    private UI owner;
    private JTextField paymentAmountTF = new JTextField(10);
    private JTextField paymentAmountFromJulyTF = new JTextField(10);
    private JTextField paymentAmountFromAugustTF = new JTextField(10);
    private JTextField paymentAmountFromSeptemberTF = new JTextField(10);
    private JTextField paymentAmountFromOctoberTF = new JTextField(10);

    public PaymentAmountDialog(final UI owner) {
        super(owner, "Payment Amount", true);
        this.owner = owner;
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());
        getContentPane().setLayout(new BorderLayout());

        JPanel centerOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JPanel centerInnerPanel = new JPanel(new GridLayout(0, 1));
        centerOuterPanel.add(centerInnerPanel);
        getContentPane().add(centerOuterPanel, BorderLayout.CENTER);

        ////////////
        // Center //
        ////////////

        JLabel label = new JLabel("Default payment amount ($)");
        centerInnerPanel.add(UiUtils.enFlow(label, paymentAmountTF));
        paymentAmountTF.setText(String.valueOf(owner.getDatabase().getDefaultMembershipAmount()));

        JLabel label1 = new JLabel("Default payment amount from July ($)");
        centerInnerPanel.add(UiUtils.enFlow(label1, paymentAmountFromJulyTF));
        paymentAmountFromJulyTF.setText(String.valueOf(owner.getDatabase().getDefaultMembershipAmountFromJuly()));

        JLabel label2 = new JLabel("Default payment amount from August ($)");
        centerInnerPanel.add(UiUtils.enFlow(label2, paymentAmountFromAugustTF));
        paymentAmountFromAugustTF.setText(String.valueOf(owner.getDatabase().getDefaultMembershipAmountFromAugust()));

        JLabel label3 = new JLabel("Default payment amount from September ($)");
        centerInnerPanel.add(UiUtils.enFlow(label3, paymentAmountFromSeptemberTF));
        paymentAmountFromSeptemberTF.setText(String.valueOf(owner.getDatabase().getDefaultMembershipAmountFromSeptember()));

        JLabel label4 = new JLabel("Default payment amount from October ($)");
        centerInnerPanel.add(UiUtils.enFlow(label4, paymentAmountFromOctoberTF));
        paymentAmountFromOctoberTF.setText(String.valueOf(owner.getDatabase().getDefaultMembershipAmountFromOctober()));

        UiUtils.sameWidth(label, label1, label2, label3, label4);

        //////////
        // East //
        //////////

        JButton okButton = UiUtils.addEast(this);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String x = paymentAmountTF.getText().trim();
                try {
                    int i = Integer.parseInt(x);
                    owner.getDatabase().setDefaultMembershipAmount(i);
                } catch (NumberFormatException e1) {
                    // Fool!
                }
                x = paymentAmountFromJulyTF.getText().trim();
                try {
                    int i = Integer.parseInt(x);
                    owner.getDatabase().setDefaultMembershipAmountFromJuly(i);
                } catch (NumberFormatException e1) {
                    // Fool!
                }
                x = paymentAmountFromAugustTF.getText().trim();
                try {
                    int i = Integer.parseInt(x);
                    owner.getDatabase().setDefaultMembershipAmountFromAugust(i);
                } catch (NumberFormatException e1) {
                    // Fool!
                }
                x = paymentAmountFromSeptemberTF.getText().trim();
                try {
                    int i = Integer.parseInt(x);
                    owner.getDatabase().setDefaultMembershipAmountFromSeptember(i);
                } catch (NumberFormatException e1) {
                    // Fool!
                }
                x = paymentAmountFromOctoberTF.getText().trim();
                try {
                    int i = Integer.parseInt(x);
                    owner.getDatabase().setDefaultMembershipAmountFromOctober(i);
                } catch (NumberFormatException e1) {
                    // Fool!
                }
                dispose();
            }
        });

        pack();
        setResizable(false);


    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(PaymentAmountDialog.this, owner.getPreferences());
    }


}
