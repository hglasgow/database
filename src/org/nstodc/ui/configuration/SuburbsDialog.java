package org.nstodc.ui.configuration;

import org.nstodc.database.Database;
import org.nstodc.database.ValidationException;
import org.nstodc.database.type.Suburb;
import org.nstodc.ui.IOwner;
import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;
import org.nstodc.ui.membership.MembershipDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

/**
 * Upsert suburbs.
 */
public class SuburbsDialog extends JDialog {

    private final IOwner owner;
    private final DefaultListModel<SuburbWrapper> suburbsListModel = new DefaultListModel<SuburbWrapper>();
    private final JList<SuburbWrapper> suburbsList = new JList<SuburbWrapper>(suburbsListModel);
    private final JButton editButton = new JButton("Edit");

    public SuburbsDialog(UI owner) {
        super(owner, "Manage Suburbs", ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        init();
    }

    public SuburbsDialog(MembershipDialog owner) {
        super(owner, "Manage Suburbs", ModalityType.APPLICATION_MODAL);
        this.owner = owner;
        init();
    }

    private void init() {
        UiUtils.locate(this, owner.getPreferences());
        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////

        JScrollPane suburbsScrollPane = new JScrollPane();
        suburbsScrollPane.setPreferredSize(new Dimension(250, 80));
        suburbsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suburbsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                ableEdit();
            }
        });
        suburbsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    editSuburb();
                }
            }
        });
        suburbsScrollPane.setViewportView(suburbsList);
        getContentPane().add(UiUtils.enFlow(suburbsScrollPane), BorderLayout.CENTER);

        Map<String, Suburb> map = new TreeMap<String, Suburb>();
        for (Suburb suburb : owner.getDatabase().getSuburbs()) {
            map.put(suburb.getSuburb(), suburb);
        }
        for (Suburb suburb : map.values()) {
            SuburbWrapper w = new SuburbWrapper(suburb);
            suburbsListModel.addElement(w);
        }

        //////////
        // East //
        //////////

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSuburb();
            }
        });
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSuburb();
            }
        });
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel buttonsPanel = new JPanel(new GridLayout(0, 1));
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(closeButton);
        getContentPane().add(UiUtils.enFlow(buttonsPanel), BorderLayout.EAST);

        pack();
        setResizable(false);
        ableEdit();

    }

    private void ableEdit() {
        editButton.setEnabled(suburbsList.getSelectedIndex() >= 0);
    }

    private void addSuburb() {
        int maxSuburbId = 0;
        for (Suburb suburb : owner.getDatabase().getSuburbs()) {
            if (suburb.getSuburbId() > maxSuburbId) {
                maxSuburbId = suburb.getSuburbId();
            }
        }

        Suburb s = new Suburb(1 + maxSuburbId);
        UpsertSuburbDialog d = new UpsertSuburbDialog(this, true, s);
        d.setVisible(true);
    }

    private void editSuburb() {
        if (suburbsList.getSelectedIndex() >= 0) {
            SuburbWrapper suburbWrapper = suburbsList.getSelectedValue();
            UpsertSuburbDialog d = new UpsertSuburbDialog(this, false, suburbWrapper.suburb);
            d.setVisible(true);
        }
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(SuburbsDialog.this, owner.getPreferences());
    }

    public void addSuburb(Suburb suburb) {
        try {
            owner.getDatabase().addSuburb(suburb);
            suburbsListModel.addElement(new SuburbWrapper(suburb));
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateSuburb(Suburb suburb) {
        for (int i = 0; i < suburbsListModel.size(); i++) {
            SuburbWrapper w = suburbsListModel.getElementAt(i);
            if (w.suburb.getSuburbId() == suburb.getSuburbId()) {
                suburbsListModel.removeElement(w);
            }
        }
        suburbsListModel.addElement(new SuburbWrapper(suburb));
    }

    public Preferences getPreferences() {
        return owner.getPreferences();
    }

    public Database getDatabase() {
        return owner.getDatabase();
    }

    private class SuburbWrapper {

        private Suburb suburb;

        private SuburbWrapper(Suburb suburb) {
            this.suburb = suburb;
        }

        public Suburb getSuburbd() {
            return suburb;
        }

        @Override
        public String toString() {
            return suburb.getSuburb() + " (" + suburb.getPostcode() + ')';
        }
    }

}
