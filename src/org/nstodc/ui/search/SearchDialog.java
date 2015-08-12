package org.nstodc.ui.search;

import org.nstodc.database.type.Dog;
import org.nstodc.database.type.Handler;
import org.nstodc.database.type.Membership;
import org.nstodc.database.type.ObedienceClass;
import org.nstodc.ui.Constants;
import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Search for a membership.
 */
public class SearchDialog extends JDialog {

    private UI owner;

    private JCheckBox primaryOnlyCB = new JCheckBox("Primary only");
    private JTextField membershipIdTF = new JTextField(10);
    private JTextField firstNameTF = new JTextField(10);
    private JTextField lastNameTF = new JTextField(10);
    private JTextField dogsNameTF = new JTextField(10);
    private JButton advanceButton = new JButton("Advance");
    private JButton editButton = new JButton("Edit");
    private boolean lastSearchByDog;

    private final DefaultListModel<Result> resultsListModel = new DefaultListModel<Result>();
    JList<Result> resultsList = new JList<Result>(resultsListModel);

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

        UiUtils.sameWidth(membershipLabel, dogLabel, firstLabel, lastLabel);

        centerInnerPanel.add(UiUtils.enFlow(primaryOnlyCB));
        centerInnerPanel.add(UiUtils.enFlow(membershipLabel, membershipIdTF));
        centerInnerPanel.add(UiUtils.enFlow(dogLabel, dogsNameTF));
        centerInnerPanel.add(UiUtils.enFlow(firstLabel, firstNameTF));
        centerInnerPanel.add(UiUtils.enFlow(lastLabel, lastNameTF));

        membershipIdTF.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) {
                    firstNameTF.setText("");
                    lastNameTF.setText("");
                    dogsNameTF.setText("");
                    resultsListModel.clear();
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

        //////////
        // East //
        //////////

        JButton cancelButton = new JButton("Close");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });

        JButton lastHundredButton = new JButton("Latest 100");
        lastHundredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lastHundred();
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        editButton.setEnabled(false);

        advanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                advance();
            }
        });
        advanceButton.setEnabled(false);

        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        getContentPane().add(flow, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(0, 1));
        flow.add(grid);
        grid.add(searchButton);
        grid.add(lastHundredButton);
        grid.add(advanceButton);
        grid.add(editButton);
        grid.add(cancelButton);
        getRootPane().setDefaultButton(searchButton);

        resultsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ableEditAdvanceButton();
            }
        });
        primaryOnlyCB.setSelected(owner.getPreferences().getBoolean(Constants.SEARCH_PRIMARY, Constants.SEARCH_PRIMARY_DEFAULT));
        pack();
        setResizable(false);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                membershipIdTF.requestFocus();
            }
        });

    }

    private void advance() {
        int index = resultsList.getSelectedIndex();
        if (index >= 0) {
            Result result = resultsListModel.getElementAt(index);
            Dog dog = result.dog;
            if (dog != null) {
                Map<Integer, ObedienceClass> m = new TreeMap<Integer, ObedienceClass>();
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
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    resultsList.ensureIndexIsVisible(resultsListModel.size() - 1);
                                    if (lastSearchByDog) {
                                        dogsNameTF.requestFocus();
                                        dogsNameTF.selectAll();
                                    } else {
                                        membershipIdTF.requestFocus();
                                        membershipIdTF.selectAll();
                                    }
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }
        resultsListModel.clear();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                search();
            }
        });
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
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    owner.editMembership(r.getMembershipId());
                }
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

        Map<Integer, Membership> map = new TreeMap<Integer, Membership>();
        for (Membership membership : owner.getDatabase().getMemberships()) {
            map.put(-membership.getMembershipId(), membership);
        }
        Set<Result> results = new TreeSet<Result>();
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
        resultsListModel.clear();
        for (Result result : results) {
            resultsListModel.addElement(result);
        }
        selectSinglePrimaryEntry();
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

        try {
            membershipId = Integer.parseInt(membershipIdTF.getText().trim());
        } catch (NumberFormatException e) {
        }

        Set<Result> results = new TreeSet<Result>();
        lastSearchByDog = false;

        if (membershipId >= 0) {
            sortMembershipId(results, membershipId);
        } else if (firstName.length() > 0) {
            sortFirstName(results, firstName);
        } else if (lastName.length() > 0) {
            sortLastName(results, lastName);
        } else if (dogsName.length() > 0) {
            lastSearchByDog = true;
            sortDogsName(results, dogsName);
        }

        showResults(results);
    }

    private void sortDogsName(Set<Result> results, String dogsName) {
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
            if (match) {
                for (Membership membership : owner.getDatabase().getMemberships()) {
                    if (membership.getMembershipId() == dog.getMembershipId()) {
                        for (Handler handler : owner.getDatabase().getHandlers()) {
                            if (handler.getMembershipId() == membership.getMembershipId()) {
                                if (!primaryOnlyCB.isSelected() || handler.isPrimary() ) {
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

    private void sortLastName(Set<Result> results, String lastName) {
        for (Handler handler : owner.getDatabase().getHandlers()) {
            if (handler.getLastName().equalsIgnoreCase(lastName)) {
                if (!primaryOnlyCB.isSelected() || handler.isPrimary() ) {
                    for (Membership membership : owner.getDatabase().getMemberships()) {
                        if (membership.getMembershipId() == handler.getMembershipId()) {
                            boolean gotDog = false;
                            for (Dog dog : owner.getDatabase().getDogs()) {
                                if (dog.getMembershipId() == membership.getMembershipId()) {
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

    private void sortFirstName(Set<Result> results, String firstName) {
        for (Handler handler : owner.getDatabase().getHandlers()) {
            if (handler.getFirstName().equalsIgnoreCase(firstName)) {
                if (!primaryOnlyCB.isSelected() || handler.isPrimary() ) {
                    for (Membership membership : owner.getDatabase().getMemberships()) {
                        if (membership.getMembershipId() == handler.getMembershipId()) {
                            boolean gotDog = false;
                            for (Dog dog : owner.getDatabase().getDogs()) {
                                if (dog.getMembershipId() == membership.getMembershipId()) {
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
                        if (!primaryOnlyCB.isSelected() || handler.isPrimary() ) {
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
        owner.getPreferences().putBoolean(Constants.SEARCH_PRIMARY, primaryOnlyCB.isSelected());
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
