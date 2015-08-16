package org.nstodc.ui.membership;

import org.nstodc.database.Database;
import org.nstodc.database.type.Breed;
import org.nstodc.database.type.Dog;
import org.nstodc.database.type.ObedienceClass;
import org.nstodc.ui.IOwner;
import org.nstodc.ui.UiUtils;
import org.nstodc.ui.configuration.BreedsDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

/**
 * Add / edit a handler.
 */
public class DogDialog extends JDialog implements IOwner {

    private final MembershipDialog owner;
    private final Dog dog;

    private final JTextField dogNameTF = new JTextField(20);
    private final JTextField dogMembershipYearTF = new JTextField(10);
    private final JTextField dogDobTF = new JTextField(10);
    private final DefaultComboBoxModel<BreedWrapper> breedListModel = new DefaultComboBoxModel<>();
    private final JComboBox<BreedWrapper> breedList = new JComboBox<>(breedListModel);
    private final JCheckBox dogCrossBreedCB = new JCheckBox("Cross breed");
    private final JCheckBox dogSterilizedCB = new JCheckBox("Sterilized");
    private final JRadioButton dogGenderMaleRB = new JRadioButton("Male");
    private final JCheckBox dogObedienceCB = new JCheckBox("Obedience");
    private final JCheckBox dogAgilityCB = new JCheckBox("Agility");
    private final JCheckBox dogDwdCB = new JCheckBox("DWD");
    private final DefaultComboBoxModel<ObedienceClassWrapper> obedienceClassModel = new DefaultComboBoxModel<>();
    private final JComboBox<ObedienceClassWrapper> obedienceClassCombo = new JComboBox<>(obedienceClassModel);
    private final JTextField backfillWeeksTF = new JTextField(10);
    private final JTextField backfillMonthsTF = new JTextField(10);
    private final JTextField backfillYearsTF = new JTextField(10);

    public DogDialog(MembershipDialog owner, final boolean nyoo, final Dog dog) {
        super(owner, (nyoo ? "Add" : "Update") + " Dog", true);
        this.owner = owner;
        this.dog = dog;

        UiUtils.locateAndCrippleClose(this, owner.getPreferences());

        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////

        JPanel centerOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel centerInnerPanel = new JPanel(new GridLayout(0,1));
        centerOuterPanel.add(centerInnerPanel);
        getContentPane().add(centerOuterPanel, BorderLayout.CENTER);

        centerInnerPanel.add(UiUtils.enFlow(new JLabel("Name"), dogNameTF));
        centerInnerPanel.add(UiUtils.enFlow(new JLabel("Membership year"), dogMembershipYearTF));
        centerInnerPanel.add(UiUtils.enFlow(new JLabel("Date of birth"), dogDobTF, new JLabel("dd/mm/yyyy")));
        JButton addBreedBtn = new JButton("Add");
        centerInnerPanel.add(UiUtils.enFlow(new JLabel("Breed"), breedList, addBreedBtn));
        addBreedBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addBreed();
            }
        });
        initBreeds();
        centerInnerPanel.add(UiUtils.enFlow(dogCrossBreedCB));
        ButtonGroup bg = new ButtonGroup();
        bg.add(dogGenderMaleRB);
        JRadioButton dogGenderFemaleRB = new JRadioButton("Female");
        bg.add(dogGenderFemaleRB);
        centerInnerPanel.add(UiUtils.enFlow(new JLabel("Gender"), dogGenderMaleRB, dogGenderFemaleRB));
        centerInnerPanel.add(UiUtils.enFlow(dogSterilizedCB));
        centerInnerPanel.add(UiUtils.enFlow(dogObedienceCB, dogAgilityCB, dogDwdCB));
        Map<Integer, ObedienceClass> m2 = new TreeMap<>();
        for (ObedienceClass obedienceClass : owner.getDatabase().getObedienceClasses()) {
            m2.put(obedienceClass.getListSequenceId(), obedienceClass);
        }
        for (ObedienceClass obedienceClass : m2.values()) {
            ObedienceClassWrapper w = new ObedienceClassWrapper(obedienceClass);
            obedienceClassModel.addElement(w);
            if (dog.getObedienceClassId() == obedienceClass.getObedienceClassId()) {
                obedienceClassModel.setSelectedItem(w);
            }
        }
        centerInnerPanel.add(UiUtils.enFlow(new JLabel("Obedience class"), obedienceClassCombo));

        JLabel weeksLabel = new JLabel("Weeks");
        JLabel monthsLabel = new JLabel("Months");
        JLabel yearsLabel = new JLabel("Years");

        UiUtils.sameWidth(weeksLabel, monthsLabel, yearsLabel);

        centerInnerPanel.add(UiUtils.enFlow(weeksLabel, backfillWeeksTF));
        centerInnerPanel.add(UiUtils.enFlow(monthsLabel, backfillMonthsTF));
        centerInnerPanel.add(UiUtils.enFlow(yearsLabel, backfillYearsTF));

        backfillWeeksTF.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                backfillWeek();
            }
        });
        backfillMonthsTF.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                backfillMonth();
            }
        });
        backfillYearsTF.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                backfillYear();
            }
        });

        dogNameTF.setText(dog.getName());
        dogDobTF.setText(dog.getDateOfBirth());
        dogCrossBreedCB.setSelected(dog.isCrossBreed());
        dogGenderMaleRB.setSelected(dog.isMale());
        dogGenderFemaleRB.setSelected(!dog.isMale());
        dogSterilizedCB.setSelected(dog.isSterilized());
        dogObedienceCB.setSelected(dog.isDoesObedience());
        dogAgilityCB.setSelected(dog.isDoesAgility());
        dogDwdCB.setSelected(dog.isDoesDwd());
        dogMembershipYearTF.setText(String.valueOf(dog.getMembershipYear()));

        dogObedienceCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ableDogObedienceClassCombo();
            }
        });
        ableDogObedienceClassCombo();

        //////////
        // East //
        //////////

        JButton okButton = UiUtils.addEast(this);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateIt()) {
                    dog.setName(dogNameTF.getText().trim());
                    dog.setDateOfBirth(dogDobTF.getText().trim());
                    dog.setBreedId(((BreedWrapper) breedList.getSelectedItem()).breed.getBreedId());
                    dog.setCrossBreed(dogCrossBreedCB.isSelected());
                    dog.setMale(dogGenderMaleRB.isSelected());
                    dog.setSterilized(dogSterilizedCB.isSelected());
                    dog.setDoesObedience(dogObedienceCB.isSelected());
                    dog.setDoesAgility(dogAgilityCB.isSelected());
                    dog.setDoesDwd(dogDwdCB.isSelected());
                    dog.setObedienceClassId(((ObedienceClassWrapper) obedienceClassCombo.getSelectedItem()).obedienceClass.getObedienceClassId());
                    dog.setMembershipYear(Integer.parseInt(dogMembershipYearTF.getText()));

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

    private void initBreeds() {
        Map<String, Breed> m = new TreeMap<>();
        for (Breed breed : owner.getDatabase().getBreeds()) {
            m.put(breed.getBreed(), breed);
        }
        breedListModel.removeAllElements();
        for (Breed breed : m.values()) {
            BreedWrapper w = new BreedWrapper(breed);
            if (breed.isActive() || dog.getBreedId() == breed.getBreedId()) {
                breedListModel.addElement(w);
            }
            if (dog.getBreedId() == breed.getBreedId()) {
                breedListModel.setSelectedItem(w);
            }
        }
    }

    private void addBreed() {
        BreedsDialog dialog = new BreedsDialog(this);
        dialog.setVisible(true);
        initBreeds();
    }

    private void backfillYear() {
        try {
            int years = Integer.parseInt(backfillYearsTF.getText());
            backfill(365 * years, backfillYearsTF);
        } catch (NumberFormatException e) {
            // Don't care
        }
    }

    private void backfillMonth() {
        try {
            int months = Integer.parseInt(backfillMonthsTF.getText());
            backfill(31 * months, backfillMonthsTF);
        } catch (NumberFormatException e) {
            // Don't care
        }
    }

    private void backfillWeek() {
        try {
            int weeks = Integer.parseInt(backfillWeeksTF.getText());
            backfill(7 * weeks, backfillWeeksTF);
        } catch (NumberFormatException e) {
            // Don't care
        }
    }

    private void backfill(int period, JTextField tf) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -period);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        dogDobTF.setText(sdf.format(cal.getTime()));
        tf.setText("");

    }

    private void ableDogObedienceClassCombo() {
        obedienceClassCombo.setEnabled(dogObedienceCB.isSelected());
    }

    private void saveNew() {
        owner.addDog(dog);
    }

    private void saveExisting() {
        owner.updateDog(dog);
    }

    private boolean validateIt() {
        // Dog must have a name
        if (dogNameTF.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Name required.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Dob must be valid
        if (!UiUtils.isValidDate(dogDobTF.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Date of birth not valid.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Membership year must be valid (1900 to 2100)
        String y = dogMembershipYearTF.getText().trim();
        if (y.length() == 0) {
            JOptionPane.showMessageDialog(this, "Membership year required.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int iy = Integer.parseInt(y);
            if (iy < 1900 || iy > 2100) {
                JOptionPane.showMessageDialog(this, "Membership year invalid.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Membership year invalid.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Should probably be for obedience, agility or DWD.
        if (!dogObedienceCB.isSelected() && !dogAgilityCB.isSelected() && !dogDwdCB.isSelected()) {
            int result = JOptionPane.showConfirmDialog(this, "No class selected.", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        String dogsName = dogNameTF.getText().trim();
        String letter = dogsName.substring(0, 1);
        if (letter.matches("[a-z]")) {
            String candidate = letter.toUpperCase() + dogsName.substring(1);
            int q = JOptionPane.showConfirmDialog(this, "Convert name from '" + dogsName + "' to '" + candidate + "'?", "Dog's Name", JOptionPane.YES_NO_CANCEL_OPTION);
            if (q == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (q == JOptionPane.YES_OPTION) {
                dogNameTF.setText(candidate);
            }
        }

        return true;
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(DogDialog.this, owner.getPreferences());
    }

    public Preferences getPreferences() {
        return owner.getPreferences();
    }

    public Database getDatabase() {
        return owner.getDatabase();
    }

    private class BreedWrapper {
        private Breed breed;

        private BreedWrapper(Breed breed) {
            this.breed = breed;
        }

        public String toString() {
            return breed.getBreed();
        }
    }

    private class ObedienceClassWrapper {
        private ObedienceClass obedienceClass;

        private ObedienceClassWrapper(ObedienceClass obedienceClass) {
            this.obedienceClass = obedienceClass;
        }

        public String toString() {
            return obedienceClass.getObedienceClass();
        }
    }

}
