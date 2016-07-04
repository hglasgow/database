package org.nstodc.ui.configuration;

import org.nstodc.database.Database;
import org.nstodc.database.ValidationException;
import org.nstodc.database.type.Breed;
import org.nstodc.ui.IOwner;
import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;
import org.nstodc.ui.membership.DogDialog;

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
 * Upsert dog breeds.
 */

public class BreedsDialog extends JDialog {

    private IOwner owner;
    private final DefaultListModel<BreedWrapper> breedsListModel = new DefaultListModel<>();
    private final JList<BreedWrapper> breedsList = new JList<>(breedsListModel);
    private final JButton editButton = new JButton("Edit");

    public BreedsDialog(DogDialog owner) {
        super(owner, "Manage Breeds", true);
        this.owner = owner;
        init();
    }

    public BreedsDialog(UI owner) {
        super(owner, "Manage Breeds", true);
        this.owner = owner;
        init();
    }

    private void init() {
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());
        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////

        JScrollPane breedsScrollPane = new JScrollPane();
        breedsScrollPane.setPreferredSize(new Dimension(250, 80));
        breedsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        breedsList.addListSelectionListener(e -> ableEdit());
        breedsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    editBreed();
                }
            }
        });
        breedsScrollPane.setViewportView(breedsList);
        getContentPane().add(UiUtils.enFlow(breedsScrollPane), BorderLayout.CENTER);

        Map<String, Breed> map = new TreeMap<>();
        for (Breed breed : owner.getDatabase().getBreeds()) {
            map.put(breed.getBreed(), breed);
        }
        for (Breed breed : map.values()) {
            BreedWrapper w = new BreedWrapper(breed);
            breedsListModel.addElement(w);
        }

        //////////
        // East //
        //////////

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addBreed());
        editButton.addActionListener(e -> editBreed());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
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
        editButton.setEnabled(breedsList.getSelectedIndex() >= 0);
    }

    private void addBreed() {
        int maxBreedId = 0;
        for (Breed breed : owner.getDatabase().getBreeds()) {
            if (breed.getBreedId() > maxBreedId) {
                maxBreedId = breed.getBreedId();
            }
        }

        Breed b = new Breed(1 + maxBreedId);
        UpsertBreedDialog d = new UpsertBreedDialog(this, true, b);
        d.setVisible(true);
    }

    private void editBreed() {
        if (breedsList.getSelectedIndex() >= 0) {
            BreedWrapper breedWrapper = breedsList.getSelectedValue();
            UpsertBreedDialog d = new UpsertBreedDialog(this, false, breedWrapper.breed);
            d.setVisible(true);
        }
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(BreedsDialog.this, owner.getPreferences());
    }

    public Preferences getPreferences() {
        return owner.getPreferences();
    }

    public void addBreed(Breed breed) {
        try {
            owner.getDatabase().addBreed(breed);
            breedsListModel.addElement(new BreedWrapper(breed));
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateBreed(Breed breed) {
        for (int i = 0; i < breedsListModel.size(); i++) {
            BreedWrapper w = breedsListModel.getElementAt(i);
            if (w.breed.getBreedId() == breed.getBreedId()) {
                breedsListModel.removeElement(w);
            }
        }
        breedsListModel.addElement(new BreedWrapper(breed));
    }

    public Database getDatabase() {
        return owner.getDatabase();
    }

    private class BreedWrapper {

        private Breed breed;

        private BreedWrapper(Breed breed) {
            this.breed = breed;
        }

        @Override
        public String toString() {
            return breed.getBreed() + " (" + (breed.isActive() ? "active" : "inactive") + ")";
        }
    }

}
