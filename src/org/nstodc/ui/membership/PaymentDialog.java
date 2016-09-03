package org.nstodc.ui.membership;

import org.nstodc.database.type.Payment;
import org.nstodc.database.type.PaymentType;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * Add / edit a payment.
 */
public class PaymentDialog extends JDialog {

    private final MembershipDialog owner;
    private final Payment payment;

    private final DefaultComboBoxModel<PaymentTypeWrapper> paymentTypeModel = new DefaultComboBoxModel<>();
    private final JComboBox<PaymentTypeWrapper> paymentTypeCombo = new JComboBox<>(paymentTypeModel);
    private final JTextField dateTF = new JTextField(10);
    private final JTextField yearTF = new JTextField(10);
    private final JTextField amountTF = new JTextField(10);
    private final JTextField receiptNumberTF = new JTextField(10);

    public PaymentDialog(MembershipDialog owner, final boolean nyoo, final Payment payment) {
        super(owner, (nyoo ? "Add" : "Update") + " Payment", true);
        this.owner = owner;
        this.payment = payment;
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());

        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////

        JPanel centerOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel centerInnerPanel = new JPanel(new GridLayout(0, 1));
        centerOuterPanel.add(centerInnerPanel);
        getContentPane().add(centerOuterPanel, BorderLayout.CENTER);

        Map<Integer, PaymentType> m2 = new TreeMap<>();
        for (PaymentType paymentType : owner.getDatabase().getPaymentTypes()) {
            m2.put(paymentType.getPaymentTypeSequenceId(), paymentType);
        }
        for (PaymentType paymentType : m2.values()) {
            PaymentTypeWrapper w = new PaymentTypeWrapper(paymentType);
            paymentTypeModel.addElement(w);
            if (payment.getPaymentTypeId() == paymentType.getPaymentTypeId()) {
                paymentTypeModel.setSelectedItem(w);
            }
        }

        JLabel paymentLabel = new JLabel("Payment type");
        JLabel dateLabel = new JLabel("Date");
        JLabel yearLabel = new JLabel("Year");
        JLabel amountLabel = new JLabel("Amount ($)");
        JLabel receiptLabel = new JLabel("Receipt number");

        UiUtils.sameWidth(paymentLabel, dateLabel, yearLabel, amountLabel, receiptLabel);

        centerInnerPanel.add(UiUtils.enFlow(paymentLabel, paymentTypeCombo));
        centerInnerPanel.add(UiUtils.enFlow(dateLabel, dateTF, new JLabel("dd/mm/yyyy")));
        centerInnerPanel.add(UiUtils.enFlow(yearLabel, yearTF));
        centerInnerPanel.add(UiUtils.enFlow(amountLabel, amountTF));
        centerInnerPanel.add(UiUtils.enFlow(receiptLabel, receiptNumberTF));

        dateTF.setText(payment.getPaymentDate());
        yearTF.setText(String.valueOf(payment.getYear()));
        amountTF.setText(String.valueOf(payment.getAmount()));
        if (payment.getReceiptNumber() == 0) {
            receiptNumberTF.setText("");
        } else {
            receiptNumberTF.setText(String.valueOf(payment.getReceiptNumber()));
        }

        //////////
        // East //
        //////////

        JButton okButton = UiUtils.addEast(this);
        okButton.addActionListener(e -> {
            if (validateIt()) {
                payment.setPaymentTypeId(((PaymentTypeWrapper) paymentTypeCombo.getSelectedItem()).paymentType.getPaymentTypeId());
                payment.setPaymentDate(dateTF.getText().trim());
                payment.setYear(Integer.parseInt(yearTF.getText().trim()));
                payment.setAmount(Integer.parseInt(amountTF.getText().trim()));
                if (receiptNumberTF.getText().trim().length() == 0) {
                    payment.setReceiptNumber(0);
                } else {
                    payment.setReceiptNumber(Integer.parseInt(receiptNumberTF.getText().trim()));
                }
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

        SwingUtilities.invokeLater(receiptNumberTF::requestFocus);

    }

    private void saveExisting() {
        owner.updatePayment(payment);
    }

    private void saveNew() {
        owner.addPayment(payment);
    }

    private boolean validateIt() {
        // Date must be valid
        if (!UiUtils.isValidDate(dateTF.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Date is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // Year must be 1900 - 2100
        String y = yearTF.getText().trim();
        if (y.length() == 0) {
            JOptionPane.showMessageDialog(this, "Year required.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int iy = Integer.parseInt(y);
            if (iy < 1900 || iy > 2100) {
                JOptionPane.showMessageDialog(this, "Year invalid.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Year invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // Amount must be positive
        String a = amountTF.getText().trim();
        if (a.length() == 0) {
            JOptionPane.showMessageDialog(this, "Amount required.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int ia = Integer.parseInt(a);
            if (ia < 0) {
                JOptionPane.showMessageDialog(this, "Amount invalid.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // Receipt must be blank or positive
        String r = receiptNumberTF.getText().trim();
        if (r.length() > 0) {
            try {
                int ir = Integer.parseInt(r);
                if (ir < 0) {
                    JOptionPane.showMessageDialog(this, "Receipt number invalid.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Receipt number invalid.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(PaymentDialog.this, owner.getPreferences());
    }

    private class PaymentTypeWrapper {
        private PaymentType paymentType;

        private PaymentTypeWrapper(PaymentType paymentType) {
            this.paymentType = paymentType;
        }

        public String toString() {
            return paymentType.getPaymentType();
        }
    }
}
