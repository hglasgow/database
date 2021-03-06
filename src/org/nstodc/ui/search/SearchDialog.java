package org.nstodc.ui.search;

import org.nstodc.database.type.*;
import org.nstodc.ui.Tabs;
import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;
import org.nstodc.ui.data.BreedWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.nstodc.ui.Constants.*;

/**
 * Search for a membership.
 */
public class SearchDialog extends JDialog {

    private static final int NO_BREED_SELECTED = -1;
    private final AtomicBoolean vitoBreedSelectionChange = new AtomicBoolean();

    private UI owner;

    private JCheckBox primaryOnlyCB = new JCheckBox("Primary only");
    private JTextField membershipIdTF = new JTextField(10);
    private JTextField firstNameTF = new JTextField(10);
    private JTextField lastNameTF = new JTextField(10);
    private JTextField dogsNameTF = new JTextField(10);
    private final DefaultComboBoxModel<BreedWrapper> breedListModel = new DefaultComboBoxModel<>();
    private final JComboBox<BreedWrapper> breedList = new JComboBox<>(breedListModel);
    private JButton advanceButton = new JButton("Advance");
    private JButton editButton = new JButton("Edit");
    private Tabs lastSearchBy;

    private final DefaultListModel<Result> resultsListModel = new DefaultListModel<>();
    private JList<Result> resultsList = new JList<>(resultsListModel);

    public SearchDialog(final UI owner) {
        super(owner, "Search", true);
        this.owner = owner;
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());
        getContentPane().setLayout(new BorderLayout());

        JPanel centerOuterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel centerInnerPanel = new JPanel(new GridLayout(0, 1));
        centerOuterPanel.add(centerInnerPanel);
        getContentPane().add(centerOuterPanel, BorderLayout.WEST);

        JScrollPane resultsScrollPane = new JScrollPane();
        getContentPane().add(UiUtils.enFlow(resultsScrollPane), BorderLayout.SOUTH);
        resultsScrollPane.setPreferredSize(new Dimension(300, 160));
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    edit();
                }
            }
        });
        resultsScrollPane.setViewportView(resultsList);

        JLabel membershipLabel = new JLabel("Membership ID");
        JLabel dogLabel = new JLabel("Dog's name");
        JLabel firstLabel = new JLabel("First name");
        JLabel lastLabel = new JLabel("Last name");
        JLabel breedLabel = new JLabel("Breed");

        UiUtils.sameWidth(membershipLabel, dogLabel, firstLabel, lastLabel, breedLabel);

        centerInnerPanel.add(UiUtils.enFlow(primaryOnlyCB));
        centerInnerPanel.add(UiUtils.enFlow(membershipLabel, membershipIdTF));
        centerInnerPanel.add(UiUtils.enFlow(dogLabel, dogsNameTF));
        centerInnerPanel.add(UiUtils.enFlow(firstLabel, firstNameTF));
        centerInnerPanel.add(UiUtils.enFlow(lastLabel, lastNameTF));
        centerInnerPanel.add(UiUtils.enFlow(breedLabel, breedList));

        membershipIdTF.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    firstNameTF.setText("");
                    lastNameTF.setText("");
                    dogsNameTF.setText("");
                    resultsListModel.clear();
                    vitoBreedSelectionChange.set(true);
                    breedList.setSelectedIndex(0);
                    vitoBreedSelectionChange.set(false);
                }
            }
        });

        firstNameTF.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    membershipIdTF.setText("");
                    lastNameTF.setText("");
                    dogsNameTF.setText("");
                    resultsListModel.clear();
                }
            }
        });

        lastNameTF.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    membershipIdTF.setText("");
                    firstNameTF.setText("");
                    dogsNameTF.setText("");
                    resultsListModel.clear();
                }
            }
        });

        dogsNameTF.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    membershipIdTF.setText("");
                    firstNameTF.setText("");
                    lastNameTF.setText("");
                    resultsListModel.clear();
                }
            }
        });

        breedList.addItemListener(e -> {
            if (!vitoBreedSelectionChange.get()) {
                membershipIdTF.setText("");
            }
        });

        //////////
        // East //
        //////////

        JButton cancelButton = new JButton("Close");
        cancelButton.addActionListener(e -> dispose());

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> search());

        JButton lastHundredButton = new JButton("Latest 100");
        lastHundredButton.addActionListener(e -> lastHundred());

        editButton.addActionListener(e -> edit());
        editButton.setEnabled(false);

        advanceButton.addActionListener(e -> advance());
        advanceButton.setEnabled(false);

        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        getContentPane().add(flow, BorderLayout.EAST);

        initBreeds();

        JPanel grid = new JPanel(new GridLayout(0, 1));
        flow.add(grid);
        grid.add(searchButton);
        grid.add(lastHundredButton);
        grid.add(advanceButton);
        grid.add(editButton);
        grid.add(cancelButton);
        getRootPane().setDefaultButton(searchButton);

        resultsList.addListSelectionListener(e -> ableEditAdvanceButton());
        primaryOnlyCB.setSelected(owner.getPreferences().getBoolean(SEARCH_PRIMARY, SEARCH_PRIMARY_DEFAULT));
        pack();
        setResizable(false);

        SwingUtilities.invokeLater(() -> membershipIdTF.requestFocus());
    }

    private void advance() {
        int index = resultsList.getSelectedIndex();
        if (index >= 0) {
            Result result = resultsListModel.getElementAt(index);
            Dog dog = result.dog;
            if (dog != null) {
                if (dog.getMembershipYear() < UiUtils.defaultYear()) {
                    dog.setMembershipYear(UiUtils.defaultYear());
                }
                Map<Integer, ObedienceClass> m = new TreeMap<>();
                ObedienceClass currentClass = null;
                for (ObedienceClass obedienceClass : owner.getDatabase().getObedienceClasses()) {
                    m.put(obedienceClass.getListSequenceId(), obedienceClass);
                    if (dog.getObedienceClassId() == obedienceClass.getObedienceClassId()) {
                        currentClass = obedienceClass;
                    }
                }
                if (currentClass != null) {
                    for (ObedienceClass obedienceClass : m.values()) {
                        if (obedienceClass.getListSequenceId() > currentClass.getListSequenceId()) {
                            // Okay, there is a higher class.
                            resultsListModel.removeElementAt(index);
                            dog.setObedienceClassId(obedienceClass.getObedienceClassId());
                            result.updateClass();
                            resultsListModel.addElement(result);
                            resultsList.setSelectedIndex(resultsListModel.size() - 1);
                            SwingUtilities.invokeLater(() -> {
                                resultsList.ensureIndexIsVisible(resultsListModel.size() - 1);
                                if (lastSearchBy == Tabs.Dog) {
                                    dogsNameTF.requestFocus();
                                    dogsNameTF.selectAll();
                                } else if (lastSearchBy == Tabs.FirstName) {
                                    firstNameTF.requestFocus();
                                    firstNameTF.selectAll();
                                } else if (lastSearchBy == Tabs.LastName) {
                                    lastNameTF.requestFocus();
                                    lastNameTF.selectAll();
                                } else {
                                    membershipIdTF.requestFocus();
                                    membershipIdTF.selectAll();
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }
        SwingUtilities.invokeLater(this::search);
    }

    private void ableEditAdvanceButton() {
        int selectedIndex = resultsList.getSelectedIndex();
        editButton.setEnabled(selectedIndex >= 0);
        if (selectedIndex >= 0) {
            Result result = resultsList.getSelectedValue();
            advanceButton.setEnabled(result.dog != null);
        } else {
            advanceButton.setEnabled(false);
        }
    }

    private void edit() {
        if (resultsList.getSelectedIndex() >= 0) {
            dispose();
            final Result r = resultsList.getSelectedValue();

            SwingUtilities.invokeLater(() -> {
                Tabs tab = Tabs.Membership;
                if (lastSearchBy == Tabs.Dog) {
                    tab = Tabs.Dog;
                } else if (lastSearchBy == Tabs.FirstName) {
                    tab = Tabs.FirstName;
                } else if (lastSearchBy == Tabs.LastName) {
                    tab = Tabs.LastName;
                }
                owner.editMembership(r.getMembershipId(), tab);
            });
        }
    }

    private void lastHundred() {
        Cursor c = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        membershipIdTF.setText("");
        dogsNameTF.setText("");
        firstNameTF.setText("");
        lastNameTF.setText("");

        Map<Integer, Membership> map = new TreeMap<>();
        for (Membership membership : owner.getDatabase().getMemberships()) {
            map.put(-membership.getMembershipId(), membership);
        }
        Set<Result> results = new TreeSet<>();
        int count = 0;
        for (Membership membership : map.values()) {
            int dogCount = 0;
            for (Dog dog : owner.getDatabase().getDogs()) {
                if (dog.getMembershipId() == membership.getMembershipId()) {
                    dogCount++;
                    int handlerCount = 0;
                    for (Handler handler : owner.getDatabase().getHandlers()) {
                        if (!primaryOnlyCB.isSelected() || handler.isPrimary()) {
                            if (handler.getMembershipId() == membership.getMembershipId()) {
                                Result r = new Result(membership.getMembershipId(), handler, dog);
                                results.add(r);
                                handlerCount++;
                            }
                        }
                    }
                    if (handlerCount == 0) {
                        // Membership and dog with no handler?
                        Result r = new Result(membership.getMembershipId(), null, dog);
                        results.add(r);
                    }
                }
            }
            if (dogCount == 0) {
                // Membership with no dog?
                int handlerCount = 0;
                for (Handler handler : owner.getDatabase().getHandlers()) {
                    if (!primaryOnlyCB.isSelected() || handler.isPrimary()) {
                        if (handler.getMembershipId() == membership.getMembershipId()) {
                            Result r = new Result(membership.getMembershipId(), handler, null);
                            results.add(r);
                            handlerCount++;
                        }
                    }
                }
                if (handlerCount == 0) {
                    // Membership with no handler and no dog?
                    Result r = new Result(membership.getMembershipId(), null, null);
                    results.add(r);
                }

            }
            if (count++ > 100) {
                break;
            }
        }

        showResults(results);

        setCursor(c);
    }

    private void showResults(Set<Result> results) {
        Result selectedValue = resultsList.getSelectedValue();
        resultsListModel.clear();
        for (Result result : results) {
            resultsListModel.addElement(result);
        }
        if (selectedValue == null) {
            selectSinglePrimaryEntry();
        } else {
            selectExistingEntry(selectedValue);
        }
    }

    private void selectExistingEntry(Result selectedValue) {
        for (int i = 0; i < resultsList.getModel().getSize(); i++) {
            Result elementAt = resultsList.getModel().getElementAt(i);
            if (elementAt.compareTo(selectedValue) == 0) {
                resultsList.setSelectedValue(elementAt, true);
                break;
            }
        }
    }

    private void selectSinglePrimaryEntry() {
        int index = -1;
        for (int i = 0; i < resultsList.getModel().getSize(); i++) {
            Result elementAt = resultsList.getModel().getElementAt(i);
            if (elementAt.handler.isPrimary()) {
                if (index >= 0) {
                    return;
                }
                index = i;
            }
        }
        resultsList.setSelectedIndex(index);
    }

    private void search() {
        int membershipId = -1;
        String firstName = firstNameTF.getText().trim();
        String lastName = lastNameTF.getText().trim();
        String dogsName = dogsNameTF.getText().trim();
        int breedId = ((BreedWrapper) breedList.getSelectedItem()).getBreed().getBreedId();

        try {
            membershipId = Integer.parseInt(membershipIdTF.getText().trim());
        } catch (NumberFormatException e) {
            // Don't care
        }

        Set<Result> results = new TreeSet<>();
        lastSearchBy = Tabs.Membership;

        if (membershipId >= 0) {
            sortMembershipId(results, membershipId);
        } else if (firstName.length() > 0) {
            lastSearchBy = Tabs.FirstName;
            sortFirstName(results, firstName, breedId);
        } else if (lastName.length() > 0) {
            lastSearchBy = Tabs.LastName;
            sortLastName(results, lastName, breedId);
        } else if (dogsName.length() > 0) {
            lastSearchBy = Tabs.Dog;
            sortDogsName(results, dogsName, breedId);
        } else if (breedId != NO_BREED_SELECTED) {
            lastSearchBy = Tabs.Dog;
            sortBreed(results, breedId);
        }

        showResults(results);
    }

    private void sortBreed(Set<Result> results, int breedId) {
        for (Handler handler : owner.getDatabase().getHandlers()) {
            if (!primaryOnlyCB.isSelected() || handler.isPrimary()) {
                for (Membership membership : owner.getDatabase().getMemberships()) {
                    if (membership.getMembershipId() == handler.getMembershipId()) {
                        for (Dog dog : owner.getDatabase().getDogs()) {
                            if (dog.getMembershipId() == membership.getMembershipId() &&
                                    (breedId == NO_BREED_SELECTED || dog.getBreedId() == breedId)) {
                                Result r = new Result(membership.getMembershipId(), handler, dog);
                                results.add(r);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sortDogsName(Set<Result> results, String dogsName, int breedId) {
        String searchString = dogsName;
        boolean wildCard = false;
        if (searchString.endsWith("%")) {
            wildCard = true;
            searchString = searchString.substring(0, searchString.length() - 1);
        }
        for (Dog dog : owner.getDatabase().getDogs()) {
            boolean match = wildCard ?
                    dog.getName().toLowerCase().startsWith(searchString) :
                    dog.getName().equalsIgnoreCase(dogsName);
            if (match && (breedId == NO_BREED_SELECTED || dog.getBreedId() == breedId)) {
                for (Membership membership : owner.getDatabase().getMemberships()) {
                    if (membership.getMembershipId() == dog.getMembershipId()) {
                        for (Handler handler : owner.getDatabase().getHandlers()) {
                            if (handler.getMembershipId() == membership.getMembershipId()) {
                                if (!primaryOnlyCB.isSelected() || handler.isPrimary()) {
                                    Result r = new Result(membership.getMembershipId(), handler, dog);
                                    results.add(r);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void sortLastName(Set<Result> results, String lastName, int breedId) {
        for (Handler handler : owner.getDatabase().getHandlers()) {
            if (handler.getLastName().equalsIgnoreCase(lastName)) {
                if (!primaryOnlyCB.isSelected() || handler.isPrimary()) {
                    for (Membership membership : owner.getDatabase().getMemberships()) {
                        if (membership.getMembershipId() == handler.getMembershipId()) {
                            boolean gotDog = false;
                            for (Dog dog : owner.getDatabase().getDogs()) {
                                if (dog.getMembershipId() == membership.getMembershipId() &&
                                        (breedId == NO_BREED_SELECTED || dog.getBreedId() == breedId)) {
                                    Result r = new Result(membership.getMembershipId(), handler, dog);
                                    results.add(r);
                                    gotDog = true;
                                }
                            }
                            if (!gotDog) {
                                Result r = new Result(membership.getMembershipId(), handler, null);
                                results.add(r);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sortFirstName(Set<Result> results, String firstName, int breedId) {
        for (Handler handler : owner.getDatabase().getHandlers()) {
            if (handler.getFirstName().equalsIgnoreCase(firstName)) {
                if (!primaryOnlyCB.isSelected() || handler.isPrimary()) {
                    for (Membership membership : owner.getDatabase().getMemberships()) {
                        if (membership.getMembershipId() == handler.getMembershipId()) {
                            boolean gotDog = false;
                            for (Dog dog : owner.getDatabase().getDogs()) {
                                if (dog.getMembershipId() == membership.getMembershipId() &&
                                        (breedId == NO_BREED_SELECTED || dog.getBreedId() == breedId)) {
                                    Result r = new Result(membership.getMembershipId(), handler, dog);
                                    results.add(r);
                                    gotDog = true;
                                }
                            }
                            if (!gotDog) {
                                Result r = new Result(membership.getMembershipId(), handler, null);
                                results.add(r);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sortMembershipId(Set<Result> results, int membershipId) {
        for (Membership membership : owner.getDatabase().getMemberships()) {
            if (membership.getMembershipId() == membershipId) {
                for (Handler handler : owner.getDatabase().getHandlers()) {
                    if (handler.getMembershipId() == membershipId) {
                        if (!primaryOnlyCB.isSelected() || handler.isPrimary()) {
                            boolean gotDog = false;
                            for (Dog dog : owner.getDatabase().getDogs()) {
                                if (dog.getMembershipId() == membershipId) {
                                    Result r = new Result(membershipId, handler, dog);
                                    results.add(r);
                                    gotDog = true;
                                }
                            }
                            if (!gotDog) {
                                Result r = new Result(membership.getMembershipId(), handler, null);
                                results.add(r);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getClazz(Dog dog) {
        for (ObedienceClass obedienceClass : owner.getDatabase().getObedienceClasses()) {
            if (dog.getObedienceClassId() == obedienceClass.getObedienceClassId()) {
                return obedienceClass.getObedienceClass();
            }
        }
        return "";
    }

    @Override
    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(this, owner.getPreferences());
        owner.getPreferences().putBoolean(SEARCH_PRIMARY, primaryOnlyCB.isSelected());
    }

    private void initBreeds() {
        Map<String, Breed> m = new TreeMap<>();
        for (Breed breed : owner.getDatabase().getBreeds()) {
            m.put(breed.getBreed(), breed);
        }
        Breed b = new Breed(-1);
        b.setBreed("Select Breed");
        BreedWrapper w = new BreedWrapper(b);
        breedListModel.addElement(w);
        for (Breed breed : m.values()) {
            w = new BreedWrapper(breed);
            breedListModel.addElement(w);
        }
    }

    private class Result implements Comparable<Result> {

        private int membershipId;
        private Handler handler;
        private Dog dog;
        private String clazz;

        public Result(int membershipId, Handler handler, Dog dog) {
            this.membershipId = membershipId;
            this.handler = handler;
            this.dog = dog;
            updateClass();
        }

        public int getMembershipId() {
            return membershipId;
        }

        @Override
        public String toString() {
            return membershipId +
                    " - " +
                    (handler == null ? "(no handler)" : (handler.getFirstName() + " " + handler.getLastName() + (handler.isPrimary() ? " (P)" : ""))) +
                    " - " +
                    (dog == null ? "(no dog)" : dog.getName() + " (" + dog.getMembershipYear() + (clazz.length() == 0 ? "" : " " + clazz) + ")");
        }

        @Override
        public int compareTo(Result o) {
            if (membershipId == o.membershipId) {
                if (compareHandler(o) == 0) {
                    return compareDog(o);
                } else {
                    return compareHandler(o);
                }
            } else {
                // Sort membership descending
                return -(membershipId - o.membershipId);
            }
        }

        private int compareHandler(Result o) {
            if (handler == null && o.handler == null) {
                return 0;
            } else if (handler == null) {
                return -1;
            } else if (o.handler == null) {
                return 1;
            }
            return handler.getHandlerId() - o.handler.getHandlerId();
        }

        private int compareDog(Result o) {
            if (dog == null && o.dog == null) {
                return 0;
            } else if (dog == null) {
                return -1;
            } else if (o.dog == null) {
                return 1;
            }
            return dog.getDogId() - o.dog.getDogId();
        }

        public void updateClass() {
            clazz = dog == null || !dog.isDoesObedience() ? "" : getClazz(dog);
        }
    }

}
