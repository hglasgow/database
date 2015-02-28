package org.nstodc.ui.search;

import org.nstodc.database.type.Dog;
import org.nstodc.database.type.Handler;
import org.nstodc.database.type.Membership;
import org.nstodc.database.type.ObedienceClass;
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

    private JTextField membershipIdTF = new JTextField(10);
    private JTextField firstNameTF = new JTextField(10);
    private JTextField lastNameTF = new JTextField(10);
    private JTextField dogsNameTF = new JTextField(10);
    private JButton advanceButton = new JButton("Advance");
    private JButton editButton = new JButton("Edit");

    private final DefaultListModel<Result> resultsListModel = new DefaultListModel<Result>();
    JList<Result> resultsList = new JList<Result>(resultsListModel);

    public SearchDialog(final UI owner) {
        super(owner, "Search", true);
        this.owner = owner;
        UiUtils.locate(this, owner.getPreferences());
        getContentPane().setLayout(new BorderLayout());

        JPanel centerOuterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel centerInnerPanel = new JPanel(new GridLayout(0,1));
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

        JLabel membershipLabel =  new JLabel("Membership ID");
        JLabel dogLabel =  new JLabel("Dog's name");
        JLabel firstLabel =  new JLabel("First name");
        JLabel lastLabel =  new JLabel("Last name");

        UiUtils.sameWidth(membershipLabel, dogLabel, firstLabel, lastLabel);

        centerInnerPanel.add(UiUtils.enFlow(membershipLabel, membershipIdTF));
        centerInnerPanel.add(UiUtils.enFlow(dogLabel, dogsNameTF));
        centerInnerPanel.add(UiUtils.enFlow(firstLabel, firstNameTF));
        centerInnerPanel.add(UiUtils.enFlow(lastLabel, lastNameTF));

        membershipIdTF.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                firstNameTF.setText("");
                lastNameTF.setText("");
                dogsNameTF.setText("");
            }
        });

        firstNameTF.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                membershipIdTF.setText("");
                lastNameTF.setText("");
                dogsNameTF.setText("");
            }
        });

        lastNameTF.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                membershipIdTF.setText("");
                firstNameTF.setText("");
                dogsNameTF.setText("");
            }
        });

        dogsNameTF.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                membershipIdTF.setText("");
                firstNameTF.setText("");
                lastNameTF.setText("");
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
            @Override
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });

        JButton lastThousandButton = new JButton("Latest 1000");
        lastThousandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lastThousand();
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
        grid.add(lastThousandButton);
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

        pack();
        setResizable(false);

    }

    private void advance() {
        int index =resultsList.getSelectedIndex();
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
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }
    }

    private void ableEditAdvanceButton() {
        int selectedIndex = resultsList.getSelectedIndex();
        editButton.setEnabled(selectedIndex >= 0);
        if (selectedIndex >= 0) {
            Result result = resultsList.getSelectedValue();
            advanceButton.setEnabled(result.dog != null);
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

    private void lastThousand() {
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
                        if (handler.getMembershipId() == membership.getMembershipId()) {
                            Result r = new Result(membership.getMembershipId(), handler, dog);
                            results.add(r);
                            handlerCount++;
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
                    if (handler.getMembershipId() == membership.getMembershipId()) {
                        Result r = new Result(membership.getMembershipId(), handler, null);
                        results.add(r);
                        handlerCount++;
                    }
                }
                if (handlerCount == 0) {
                    // Membership with no handler and no dog?
                    Result r = new Result(membership.getMembershipId(), null, null);
                    results.add(r);
                }

            }
            if (count++ > 1000) {
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
        if (results.size() == 1) {
            resultsList.setSelectedIndex(0);
        }
    }

    private void search() {
        int membershipId = -1;
        String firstName = firstNameTF.getText().trim();
        String lastName = lastNameTF.getText().trim();
        String dogsName = dogsNameTF.getText().trim();

        try {
            membershipId = Integer.parseInt(membershipIdTF.getText().trim());
        } catch (NumberFormatException e) {}

        Set<Result> results = new TreeSet<Result>();

        if (membershipId >= 0) {
            sortMembershipId(results, membershipId);
        } else if (firstName.length() > 0) {
            sortFirstName(results, firstName);
        } else if (lastName.length() > 0) {
            sortLastName(results, lastName);
        } else if (dogsName.length() > 0) {
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
                                Result r = new Result(membership.getMembershipId(), handler, dog);
                                results.add(r);
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

    private void sortFirstName(Set<Result> results, String firstName) {
        for (Handler handler : owner.getDatabase().getHandlers()) {
            if (handler.getFirstName().equalsIgnoreCase(firstName)) {
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

    private void sortMembershipId(Set<Result> results, int membershipId) {
        for (Membership membership : owner.getDatabase().getMemberships()) {
            if (membership.getMembershipId() == membershipId) {
                for (Handler handler : owner.getDatabase().getHandlers()) {
                    if (handler.getMembershipId() == membershipId) {
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
