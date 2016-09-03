package org.nstodc.ui.membership;

import org.nstodc.database.Database;
import org.nstodc.database.ValidationException;
import org.nstodc.database.type.*;
import org.nstodc.ui.IOwner;
import org.nstodc.ui.Tabs;
import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;
import org.nstodc.ui.configuration.SuburbsDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

/**
 * Add / update a membership.
 */
public class MembershipDialog extends JDialog implements IOwner {

    private final boolean nyoo;
    private final UI owner;
    private final JTextField addressTF = new JTextField(40);
    private final JTextField phoneTF;
    private final JTextField mobileTF;
    private final JTextField emailTF;
    private final DefaultComboBoxModel<SuburbWrapper> suburbModel = new DefaultComboBoxModel<>();
    private final JCheckBox sponsorshipCB;
    private final DefaultComboBoxModel<MembershipTypeWrapper> membershipTypeModel = new DefaultComboBoxModel<>();
    private final DefaultListModel<HandlerWrapper> handlersListModel = new DefaultListModel<>();
    private final JList<HandlerWrapper> handlersList;
    private final Membership membership;
    private final DefaultListModel<DogWrapper> dogsListModel = new DefaultListModel<>();
    private final JList<DogWrapper> dogsList;
    private final DefaultListModel<PaymentWrapper> paymentsListModel = new DefaultListModel<>();
    private final JList<PaymentWrapper> paymentsList;
    private final JTabbedPane tabs;

    private boolean doneAutoHandlersPopup;
    private boolean doneAutoDogsPopup;
    private boolean doneAutoPaymentsPopup;

    public MembershipDialog(final UI owner, final boolean nyoo, final MembershipBundle bundle, Tabs tab) {
        super(owner, (nyoo ? "Add" : "Update") + " Membership " + bundle.getMembership().getMembershipId() + " (" + bundle.getMembership().getDateJoined() + ")", true);
        this.nyoo = nyoo;
        this.owner = owner;
        this.membership = bundle.getMembership();
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());
        getContentPane().setLayout(new BorderLayout());

        tabs = new JTabbedPane();
        tabs.addChangeListener(e -> tabChanged());
        getContentPane().add(tabs, BorderLayout.CENTER);

        ////////////////
        // Membership //
        ////////////////

        JPanel membershipOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel membershipInnerPanel = new JPanel(new GridLayout(0, 1));
        membershipOuterPanel.add(membershipInnerPanel);
        tabs.addTab("Membership", membershipOuterPanel);

        JLabel l = new JLabel("Membership type");
        Map<Integer, MembershipType> m1 = new TreeMap<>();
        for (MembershipType mt : owner.getDatabase().getMembershipTypes()) {
            m1.put(mt.getMembershipTypeId(), mt);
        }
        for (MembershipType mt : m1.values()) {
            MembershipTypeWrapper w = new MembershipTypeWrapper(mt);
            membershipTypeModel.addElement(w);
            if (membership.getMembershipTypeId() == mt.getMembershipTypeId()) {
                membershipTypeModel.setSelectedItem(w);
            }
        }
        JComboBox<MembershipTypeWrapper> membershipTypeCB = new JComboBox<>(membershipTypeModel);
        membershipInnerPanel.add(UiUtils.enFlow(l, membershipTypeCB));

        JLabel addressLabel = new JLabel("Address");
        membershipInnerPanel.add(UiUtils.enFlow(addressLabel, addressTF));
        addressTF.setText(membership.getAddress());

        JLabel suburbLabel = new JLabel("Suburb");
        JComboBox<SuburbWrapper> suburbCB = new JComboBox<>(suburbModel);
        JButton addSuburbBtn = new JButton("Add");
        addSuburbBtn.addActionListener(e -> addSuburb());
        membershipInnerPanel.add(UiUtils.enFlow(suburbLabel, suburbCB, addSuburbBtn));

        l = new JLabel("Postcode");
        final JLabel postcodeLabel = new JLabel();
        membershipInnerPanel.add(UiUtils.enFlow(l, postcodeLabel));
        suburbCB.addActionListener(e -> {
            Object item = suburbModel.getSelectedItem();
            if (item != null) {
                postcodeLabel.setText(((SuburbWrapper) item).suburb.getPostcode());
            }
        });

        initSuburbs();

        JLabel phoneLabel = new JLabel("Phone");
        phoneTF = new JTextField(20);
        membershipInnerPanel.add(UiUtils.enFlow(phoneLabel, phoneTF));
        phoneTF.setText(membership.getPhone());

        JLabel mobileLabel = new JLabel("Mobile");
        mobileTF = new JTextField(20);
        membershipInnerPanel.add(UiUtils.enFlow(mobileLabel, mobileTF));
        mobileTF.setText(membership.getMobile());

        JLabel emailLabel = new JLabel("Email");
        emailTF = new JTextField(40);
        membershipInnerPanel.add(UiUtils.enFlow(emailLabel, emailTF));
        emailTF.setText(membership.getEmail());

        sponsorshipCB = new JCheckBox("Allow sponsorship");
        JPanel sponsorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sponsorPanel.add(sponsorshipCB);
        membershipInnerPanel.add(sponsorPanel);
        sponsorshipCB.setSelected(membership.isAllowSponsorship());

        UiUtils.sameWidth(addressLabel, suburbLabel, phoneLabel, mobileLabel, emailLabel);

        SwingUtilities.invokeLater(addressTF::requestFocus);

        //////////////
        // Handlers //
        //////////////

        JPanel handlersOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel handlersInnerPanel = new JPanel(new BorderLayout());
        handlersOuterPanel.add(handlersInnerPanel);
        tabs.addTab("Handlers", handlersOuterPanel);

        JScrollPane handlersScrollPane = new JScrollPane();
        handlersScrollPane.setPreferredSize(new Dimension(250, 80));
        handlersInnerPanel.add(UiUtils.enFlow(handlersScrollPane), BorderLayout.CENTER);
        handlersList = new JList<>(handlersListModel);
        handlersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        handlersList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    editHandler();
                }
            }
        });
        handlersScrollPane.setViewportView(handlersList);

        JPanel handlerButtonsPanel = new JPanel(new GridLayout(0, 1));
        JButton handlerAddBtn = new JButton("Add");
        handlerAddBtn.addActionListener(e -> addHandler());
        final JButton handlerEditBtn = new JButton("Edit");
        handlerEditBtn.setEnabled(false);
        handlerEditBtn.addActionListener(e -> editHandler());
        final JButton handlerDeleteBtn = new JButton("Delete");
        handlerDeleteBtn.setEnabled(false);
        handlerDeleteBtn.addActionListener(e -> {
            HandlerWrapper selectedValue = handlersList.getSelectedValue();
            if (JOptionPane.showConfirmDialog(MembershipDialog.this,
                    "Delete " + selectedValue.handler.getFirstName() + " " + selectedValue.handler.getFirstName() + "?", "Delete",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                handlersListModel.remove(handlersList.getSelectedIndex());
            }
        });
        handlerButtonsPanel.add(handlerAddBtn);
        handlerButtonsPanel.add(handlerEditBtn);
        handlerButtonsPanel.add(handlerDeleteBtn);
        JPanel handlerButtonFlowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        handlerButtonFlowPanel.add(handlerButtonsPanel);
        handlersInnerPanel.add(handlerButtonFlowPanel, BorderLayout.EAST);
        for (Handler handler : bundle.getHandlers()) {
            HandlerWrapper w = new HandlerWrapper(handler);
            handlersListModel.addElement(w);
        }
        handlersList.addListSelectionListener(e -> {
            boolean selected = handlersList.getSelectedIndex() > -1;
            handlerEditBtn.setEnabled(selected);
            handlerDeleteBtn.setEnabled(selected);
        });
        if (handlersListModel.size() == 1) {
            handlersList.setSelectedIndex(0);
        }

        //////////
        // Dogs //
        //////////

        JPanel dogsOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel dogsInnerPanel = new JPanel(new BorderLayout());
        dogsOuterPanel.add(dogsInnerPanel);
        tabs.addTab("Dogs", dogsOuterPanel);

        JScrollPane dogsScrollPane = new JScrollPane();
        dogsScrollPane.setPreferredSize(new Dimension(250, 80));
        dogsInnerPanel.add(UiUtils.enFlow(dogsScrollPane), BorderLayout.CENTER);
        dogsList = new JList<>(dogsListModel);
        dogsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dogsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    editDog();
                }
            }
        });
        dogsScrollPane.setViewportView(dogsList);

        JPanel dogButtonsPanel = new JPanel(new GridLayout(0, 1));
        JButton dogAddBtn = new JButton("Add");
        dogAddBtn.addActionListener(e -> addDog());
        final JButton dogEditBtn = new JButton("Edit");
        dogEditBtn.setEnabled(false);
        dogEditBtn.addActionListener(e -> editDog());
        final JButton dogDeleteBtn = new JButton("Delete");
        dogDeleteBtn.setEnabled(false);
        dogDeleteBtn.addActionListener(e -> {
            DogWrapper selectedValue = dogsList.getSelectedValue();
            if (JOptionPane.showConfirmDialog(MembershipDialog.this,
                    "Delete " + selectedValue.dog.getName() + "?", "Delete",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dogsListModel.remove(dogsList.getSelectedIndex());
            }
        });
        final JButton dogAdvanceBtn = new JButton("Advance");
        dogAdvanceBtn.setEnabled(false);
        dogAdvanceBtn.addActionListener(e -> advanceDog());

        final JButton dogRenewBtn = new JButton("Renew");
        dogRenewBtn.setEnabled(false);
        dogRenewBtn.addActionListener(e -> renewDog());

        dogButtonsPanel.add(dogAddBtn);
        dogButtonsPanel.add(dogEditBtn);
        dogButtonsPanel.add(dogDeleteBtn);
        dogButtonsPanel.add(dogAdvanceBtn);
        dogButtonsPanel.add(dogRenewBtn);
        JPanel dogButtonFlowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dogButtonFlowPanel.add(dogButtonsPanel);
        dogsInnerPanel.add(dogButtonFlowPanel, BorderLayout.EAST);
        for (Dog dog : bundle.getDogs()) {
            DogWrapper w = new DogWrapper(dog);
            dogsListModel.addElement(w);
        }
        dogsList.addListSelectionListener(e -> {
            boolean selected = dogsList.getSelectedIndex() > -1;
            dogEditBtn.setEnabled(selected);
            dogDeleteBtn.setEnabled(selected);
            if (selected) {
                Dog dog = dogsListModel.getElementAt(dogsList.getSelectedIndex()).dog;
                dogAdvanceBtn.setEnabled(dog.isDoesObedience());
                dogRenewBtn.setEnabled(dog.getMembershipYear() < UiUtils.defaultYear());
            } else {
                dogAdvanceBtn.setEnabled(false);
                dogRenewBtn.setEnabled(false);
            }
        });
        if (dogsListModel.size() == 1) {
            dogsList.setSelectedIndex(0);
        }

        //////////////
        // Payments //
        //////////////

        JPanel paymentsOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel paymentsInnerPanel = new JPanel(new BorderLayout());
        paymentsOuterPanel.add(paymentsInnerPanel);
        tabs.addTab("Payments", paymentsOuterPanel);

        JScrollPane paymentsScrollPane = new JScrollPane();
        paymentsScrollPane.setPreferredSize(new Dimension(250, 80));
        paymentsInnerPanel.add(UiUtils.enFlow(paymentsScrollPane), BorderLayout.CENTER);
        paymentsList = new JList<>(paymentsListModel);
        paymentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    editPayment();
                }
            }
        });
        paymentsScrollPane.setViewportView(paymentsList);

        JPanel paymentButtonsPanel = new JPanel(new GridLayout(0, 1));
        JButton paymentAddBtn = new JButton("Add");
        paymentAddBtn.addActionListener(e -> addPayment());
        final JButton paymentEditBtn = new JButton("Edit");
        paymentEditBtn.setEnabled(false);
        paymentEditBtn.addActionListener(e -> editPayment());
        final JButton paymentDeleteBtn = new JButton("Delete");
        paymentDeleteBtn.setEnabled(false);
        paymentDeleteBtn.addActionListener(e -> {
            PaymentWrapper selectedValue = paymentsList.getSelectedValue();
            if (JOptionPane.showConfirmDialog(MembershipDialog.this,
                    "Delete " + selectedValue.toString() + "?", "Delete",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                paymentsListModel.remove(paymentsList.getSelectedIndex());
            }
        });
        paymentButtonsPanel.add(paymentAddBtn);
        paymentButtonsPanel.add(paymentEditBtn);
        paymentButtonsPanel.add(paymentDeleteBtn);
        JPanel paymentButtonFlowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentButtonFlowPanel.add(paymentButtonsPanel);
        paymentsInnerPanel.add(paymentButtonFlowPanel, BorderLayout.EAST);
        for (Payment payment : bundle.getPayments()) {
            PaymentWrapper w = new PaymentWrapper(payment);
            paymentsListModel.addElement(w);
        }
        paymentsList.addListSelectionListener(e -> {
            boolean selected = paymentsList.getSelectedIndex() > -1;
            paymentEditBtn.setEnabled(selected);
            paymentDeleteBtn.setEnabled(selected);
        });
        if (paymentsListModel.size() == 1) {
            paymentsList.setSelectedIndex(0);
        }

        //////////
        // East //
        //////////

        JButton okButton = UiUtils.addEast(this);
        okButton.addActionListener(e -> {
            if (validateIt()) {
                updateMembership();
                updateHandlers();
                updateDogs();
                updatePayments();
                dispose();
            }
        });

        pack();
        setResizable(false);

        if (tab == Tabs.Dog) {
            tabs.setSelectedIndex(2);
        } else if (tab == Tabs.FirstName || tab == Tabs.LastName) {
            tabs.setSelectedIndex(1);
        }
    }

    private void tabChanged() {
        SwingUtilities.invokeLater(() -> {
            int tabIndex = tabs.getSelectedIndex();
            if (nyoo) {
                if (tabIndex == 1 && !doneAutoHandlersPopup) { // Handlers
                    doneAutoHandlersPopup = true;
                    addHandler();
                } else if (tabIndex == 2 && !doneAutoDogsPopup) { // Dogs
                    doneAutoDogsPopup = true;
                    addDog();
                } else if (tabIndex == 3 && !doneAutoPaymentsPopup) { // Payments
                    doneAutoPaymentsPopup = true;
                    addPayment();
                }
            }
        });
    }

    private void initSuburbs() {
        Map<String, Suburb> m2 = new TreeMap<>();
        for (Suburb suburb : owner.getDatabase().getSuburbs()) {
            if (suburb.getPostcode().equals("0000")) {
                // Unknown first.
                m2.put("", suburb);
            } else {
                m2.put(suburb.getSuburb(), suburb);
            }
        }
        suburbModel.removeAllElements();
        for (Suburb suburb : m2.values()) {
            SuburbWrapper w = new SuburbWrapper(suburb);
            suburbModel.addElement(w);
            if (suburb.getSuburbId() == membership.getSuburbId()) {
                suburbModel.setSelectedItem(w);
            }
        }
    }

    private void addSuburb() {
        SuburbsDialog dialog = new SuburbsDialog(this);
        dialog.setVisible(true);
        initSuburbs();
    }

    private void updatePayments() {
        // Remove all payments for this member.
        Payment[] allPayments = owner.getDatabase().getPayments().toArray(new Payment[owner.getDatabase().getPayments().size()]);
        for (Payment payment : allPayments) {
            if (payment.getMembershipId() == membership.getMembershipId()) {
                owner.getDatabase().removePayment(payment);
            }
        }

        // Re-add the payments.
        for (int i = 0; i < paymentsListModel.size(); i++) {
            Payment p = paymentsListModel.getElementAt(i).payment;
            try {
                owner.getDatabase().addPayment(p);
            } catch (ValidationException e) {
                error("Payments", "There was an error saving a payment: " + e.getMessage());
            }
        }
    }

    public Database getDatabase() {
        return owner.getDatabase();
    }

    private void updateMembership() {
        String address = addressTF.getText().trim();
        String phone = phoneTF.getText().trim();
        String mobile = mobileTF.getText().trim();
        String email = emailTF.getText().trim();
        int suburbId = ((SuburbWrapper) suburbModel.getSelectedItem()).suburb.getSuburbId();
        boolean allowSponsorship = sponsorshipCB.isSelected();
        int membershipTypeId = ((MembershipTypeWrapper) membershipTypeModel.getSelectedItem()).membershipType.getMembershipTypeId();

        phone = formatPhone(phone);

        mobile = formatMobile(mobile);

        membership.setAddress(address);
        membership.setPhone(phone);
        membership.setMobile(mobile);
        membership.setEmail(email);
        membership.setSuburbId(suburbId);
        membership.setAllowSponsorship(allowSponsorship);
        membership.setMembershipTypeId(membershipTypeId);
        owner.setLatestMembership(membership.getMembershipId());
        if (nyoo) {
            try {
                owner.getDatabase().addMembership(membership);
            } catch (ValidationException e) {
                error("Membership", "There was an error saving the membership: " + e.getMessage());
            }
        }

    }

    private String formatMobile(String mobile) {
        if (mobile != null && mobile.startsWith("04")) {
            mobile = mobile.replaceAll("\\s", "");
            if (mobile.length() == 10) {
                mobile = mobile.substring(0, 4) + " " + mobile.substring(4, 7) + " " + mobile.substring(7);
            }
        }
        return mobile;
    }

    private String formatPhone(String phone) {
        if (phone != null && phone.startsWith("9")) {
            phone = phone.replaceAll("\\s", "");
            if (phone.length() == 8) {
                phone = phone.substring(0, 4) + " " + phone.substring(4);
            }
        }
        return phone;
    }

    private void updateHandlers() {
        // Remove all handlers for this member.
        Handler[] allHandlers = owner.getDatabase().getHandlers().toArray(new Handler[owner.getDatabase().getHandlers().size()]);
        for (Handler handler : allHandlers) {
            if (handler.getMembershipId() == membership.getMembershipId()) {
                owner.getDatabase().removeHandler(handler);
            }
        }

        // Re-add the handlers.
        for (int i = 0; i < handlersListModel.size(); i++) {
            Handler h = handlersListModel.getElementAt(i).handler;
            try {
                owner.getDatabase().addHandler(h);
            } catch (ValidationException e) {
                error("Handlers", "There was an error saving a handler: " + e.getMessage());
            }
        }
    }

    private void updateDogs() {
        // Remove all dogs for this member.
        Dog[] allDogs = owner.getDatabase().getDogs().toArray(new Dog[owner.getDatabase().getDogs().size()]);
        for (Dog dog : allDogs) {
            if (dog.getMembershipId() == membership.getMembershipId()) {
                owner.getDatabase().removeDog(dog);
            }
        }

        // Re-add the dogs.
        for (int i = 0; i < dogsListModel.size(); i++) {
            Dog d = dogsListModel.getElementAt(i).dog;
            try {
                owner.getDatabase().addDog(d);
            } catch (ValidationException e) {
                error("Dogs", "There was an error saving a dog: " + e.getMessage());
            }
        }
    }

    private void addDog() {
        int nextDogId = 1;
        // Check db, then locally.
        for (Dog dog : owner.getDatabase().getDogs()) {
            if (dog.getDogId() >= nextDogId) {
                nextDogId = dog.getDogId() + 1;
            }
        }
        for (int i = 0; i < dogsListModel.size(); i++) {
            Dog d = dogsListModel.getElementAt(i).dog;
            if (d.getDogId() >= nextDogId) {
                nextDogId = d.getDogId() + 1;
            }
        }

        Dog d = new Dog(nextDogId, membership.getMembershipId());

        d.setMembershipYear(UiUtils.defaultYear());
        d.setDoesObedience(true);
        d.setMale(true);

        DogDialog dialog = new DogDialog(this, true, d);
        dialog.setVisible(true);
    }

    private void editDog() {
        DogWrapper wrapper = dogsList.getSelectedValue();
        DogDialog dialog = new DogDialog(this, false, wrapper.dog);
        dialog.setVisible(true);
    }

    private void renewDog() {
        int index = dogsList.getSelectedIndex();
        if (index >= 0) {
            Dog dog = dogsListModel.getElementAt(index).dog;
            dogsListModel.removeElementAt(index);
            dog.setMembershipYear(UiUtils.defaultYear());
            dogsListModel.addElement(new DogWrapper(dog));
            dogsList.setSelectedIndex(dogsListModel.size() - 1);
        }
    }

    private void advanceDog() {
        int index = dogsList.getSelectedIndex();
        if (index >= 0) {
            Dog dog = dogsListModel.getElementAt(index).dog;

            Map<Integer, ObedienceClass> m = new TreeMap<>();
            ObedienceClass currentClass = null;
            for (ObedienceClass obedienceClass : getDatabase().getObedienceClasses()) {
                m.put(obedienceClass.getListSequenceId(), obedienceClass);
                if (dog.getObedienceClassId() == obedienceClass.getObedienceClassId()) {
                    currentClass = obedienceClass;
                }
            }
            if (currentClass != null) {
                for (ObedienceClass obedienceClass : m.values()) {
                    if (obedienceClass.getListSequenceId() > currentClass.getListSequenceId()) {
                        // Okay, there is a higher class.
                        dogsListModel.removeElementAt(index);
                        dog.setObedienceClassId(obedienceClass.getObedienceClassId());
                        dogsListModel.addElement(new DogWrapper(dog));
                        dogsList.setSelectedIndex(dogsListModel.size() - 1);
                        break;
                    }
                }
            }
        }
    }

    public void addHandler() {
        addHandler("");
    }

    public void addHandler(String lastName) {
        int nextHandlerId = 1;
        // Check db, then locally.
        for (Handler handler : owner.getDatabase().getHandlers()) {
            if (handler.getHandlerId() >= nextHandlerId) {
                nextHandlerId = handler.getHandlerId() + 1;
            }
        }
        for (int i = 0; i < handlersListModel.size(); i++) {
            Handler h = handlersListModel.getElementAt(i).handler;
            if (h.getHandlerId() >= nextHandlerId) {
                nextHandlerId = h.getHandlerId() + 1;
            }
        }

        Handler h = new Handler(nextHandlerId, membership.getMembershipId());
        if (handlersListModel.isEmpty()) {
            h.setPrimary(true);
        }
        if (lastName != null && lastName.length() > 0) {
            h.setLastName(lastName);
        }
        HandlerDialog dialog = new HandlerDialog(this, true, h);
        dialog.setVisible(true);
    }

    private void editHandler() {
        HandlerWrapper wrapper = handlersList.getSelectedValue();
        HandlerDialog dialog = new HandlerDialog(this, false, wrapper.handler);
        dialog.setVisible(true);
    }

    public Preferences getPreferences() {
        return owner.getPreferences();
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(MembershipDialog.this, owner.getPreferences());
    }

    private void addPayment() {
        int nextPaymentId = 1;
        // Check db, then locally.
        for (Payment payment : owner.getDatabase().getPayments()) {
            if (payment.getPaymentId() >= nextPaymentId) {
                nextPaymentId = payment.getPaymentId() + 1;
            }
        }
        for (int i = 0; i < paymentsListModel.size(); i++) {
            Payment p = paymentsListModel.getElementAt(i).payment;
            if (p.getPaymentId() >= nextPaymentId) {
                nextPaymentId = p.getPaymentId() + 1;
            }
        }

        Payment p = new Payment(nextPaymentId, membership.getMembershipId());
        p.setYear(UiUtils.defaultYear());
        p.setPaymentDate(UiUtils.lastSunday());
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        if (month == Calendar.OCTOBER) {
            p.setAmount(getDatabase().getDefaultMembershipAmountFromOctober());
        } else if (month == Calendar.SEPTEMBER) {
            p.setAmount(getDatabase().getDefaultMembershipAmountFromSeptember());
        } else if (month == Calendar.AUGUST) {
            p.setAmount(getDatabase().getDefaultMembershipAmountFromAugust());
        } else if (month == Calendar.JULY) {
            p.setAmount(getDatabase().getDefaultMembershipAmountFromJuly());
        } else {
            p.setAmount(getDatabase().getDefaultMembershipAmount());
        }

        PaymentDialog dialog = new PaymentDialog(this, true, p);
        dialog.setVisible(true);
    }

    private void editPayment() {
        PaymentWrapper wrapper = paymentsList.getSelectedValue();
        PaymentDialog dialog = new PaymentDialog(this, false, wrapper.payment);
        dialog.setVisible(true);
    }

    private boolean validateIt() {

        ////////////////
        // Membership //
        ////////////////

        // Address line must be entered.
        if (addressTF.getText().trim().length() == 0) {
            error("Membership", "Address missing.");
            return false;
        }

        // Phone blank or numeric.
        String phoneText = phoneTF.getText().trim();
        for (int i = 0; i < phoneText.length(); i++) {
            char c = phoneText.charAt(i);
            if (c != ' ' && (c < '0' || c > '9')) {
                error("Membership", "Phone invalid " + phoneText + ".");
                return false;
            }
        }

        if (phoneText.startsWith("9")) {
            int count = 0;
            for (int i = 0; i < phoneText.length(); i++) {
                if (!phoneText.substring(i, i + 1).equalsIgnoreCase(" ")) {
                    count++;
                }
            }
            if (count != 8) {
                int i = JOptionPane.showOptionDialog(this, "Phone appears to be invalid.", "Membership", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Redo", "Accept"}, 0);
                if (i == 0) {
                    return false;
                }
            }
        }

        // Mobile blank or numeric.
        String mobileText = mobileTF.getText().trim();
        for (int i = 0; i < mobileText.length(); i++) {
            char c = mobileText.charAt(i);
            if (c != ' ' && (c < '0' || c > '9')) {
                error("Membership", "Mobile invalid " + mobileText + ".");
                return false;
            }
        }

        if (mobileText.startsWith("04")) {
            int count = 0;
            for (int i = 0; i < mobileText.length(); i++) {
                if (!mobileText.substring(i, i + 1).equalsIgnoreCase(" ")) {
                    count++;
                }
            }
            if (count != 10) {
                int i = JOptionPane.showOptionDialog(this, "Mobile appears to be invalid.", "Membership", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Redo", "Accept"}, 0);
                if (i == 0) {
                    return false;
                }
            }
        }

        // Email blank or has @ character and . character and no spaces.
        String emailText = emailTF.getText().trim();
        if (emailText.length() > 0 && (!emailText.contains("@") || !emailText.contains(".") || emailText.contains(" "))) {
            error("Membership", "Email invalid " + emailText + ".");
            return false;
        }

        //////////////
        // Handlers //
        //////////////

        // Must be at least one handler.
        if (handlersListModel.size() == 0) {
            error("Handlers", "Must have at least one handler.");
            return false;
        }

        // Must be one and one only primary handler.
        int primaryCount = 0;
        for (int i = 0; i < handlersListModel.size(); i++) {
            Handler h = handlersListModel.getElementAt(i).handler;
            if (h.isPrimary()) {
                primaryCount++;
            }
        }

        if (primaryCount != 1) {
            error("Handlers", "Expected 1 primary handler but found " + primaryCount + ".");
            return false;
        }

        //////////
        // Dogs //
        //////////

        // Expect at least one dog, but not actually mandatory.
        if (dogsListModel.size() == 0) {
            int i = JOptionPane.showOptionDialog(this, "No dogs have been entered.", "Dogs", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Add dog", "Close anyway"}, 0);
            if (i == 0) {
                return false;
            }
        }

        //////////////
        // Payments //
        //////////////
        // N/A

        return true;
    }

    private void error(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public void addHandler(Handler handler) {
        if (handler.isPrimary()) {
            for (int i = 0; i < handlersListModel.size(); i++) {
                HandlerWrapper wrapper = handlersListModel.getElementAt(i);
                wrapper.handler.setPrimary(false);
            }
        }
        handlersListModel.addElement(new HandlerWrapper(handler));
    }

    public void updateHandler(Handler handler) {
        for (int i = 0; i < handlersListModel.size(); i++) {
            HandlerWrapper w = handlersListModel.getElementAt(i);
            if (w.handler.getHandlerId() == handler.getHandlerId()) {
                handlersListModel.removeElement(w);
            }
        }
        if (handler.isPrimary()) {
            for (int i = 0; i < handlersListModel.size(); i++) {
                HandlerWrapper wrapper = handlersListModel.getElementAt(i);
                wrapper.handler.setPrimary(false);
            }
        }
        handlersListModel.addElement(new HandlerWrapper(handler));
    }

    public void addDog(Dog dog) {
        dogsListModel.addElement(new DogWrapper(dog));
    }

    public void updateDog(Dog dog) {
        for (int i = 0; i < dogsListModel.size(); i++) {
            DogWrapper w = dogsListModel.getElementAt(i);
            if (w.dog.getDogId() == dog.getDogId()) {
                dogsListModel.removeElement(w);
            }
        }
        dogsListModel.addElement(new DogWrapper(dog));
    }

    public void addPayment(Payment payment) {
        paymentsListModel.addElement(new PaymentWrapper(payment));
    }

    public void updatePayment(Payment payment) {
        for (int i = 0; i < paymentsListModel.size(); i++) {
            PaymentWrapper w = paymentsListModel.getElementAt(i);
            if (w.payment.getPaymentId() == payment.getPaymentId()) {
                paymentsListModel.removeElement(w);
            }
        }
        paymentsListModel.addElement(new PaymentWrapper(payment));
    }

    private class SuburbWrapper {
        private Suburb suburb;

        private SuburbWrapper(Suburb suburb) {
            this.suburb = suburb;
        }

        public String toString() {
            return suburb.getSuburb();
        }
    }

    private class MembershipTypeWrapper {
        private MembershipType membershipType;

        private MembershipTypeWrapper(MembershipType membershipType) {
            this.membershipType = membershipType;
        }

        public String toString() {
            return membershipType.getMembershipType();
        }
    }

    private class HandlerWrapper {
        private Handler handler;

        private HandlerWrapper(Handler handler) {
            this.handler = handler;
        }

        public String toString() {
            return handler.getFirstName() + " " + handler.getLastName() + (handler.isPrimary() ? " (Primary)" : "");
        }
    }

    private class PaymentWrapper {
        private Payment payment;

        private PaymentWrapper(Payment payment) {
            this.payment = payment;
        }

        public String toString() {
            return payment.getPaymentDate() + " $" + payment.getAmount();
        }
    }

    private class DogWrapper {
        private Dog dog;
        private String clazz = "";

        private DogWrapper(Dog dog) {
            this.dog = dog;

            if (dog.isDoesObedience()) {
                for (ObedienceClass obedienceClass : owner.getDatabase().getObedienceClasses()) {
                    if (obedienceClass.getObedienceClassId() == dog.getObedienceClassId()) {
                        clazz = " " + obedienceClass.getObedienceClass();
                    }
                }
            }
        }

        public String toString() {
            return dog.getName() + " (" + dog.getMembershipYear() + clazz + ')';
        }
    }
}
