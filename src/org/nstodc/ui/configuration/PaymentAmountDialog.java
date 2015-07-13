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

        centerInnerPanel.add(UiUtils.enFlow(new JLabel("Default payment amount ($)"), paymentAmountTF));
        paymentAmountTF.setText(String.valueOf(owner.getDatabase().getDefaultMembershipAmount()));

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
