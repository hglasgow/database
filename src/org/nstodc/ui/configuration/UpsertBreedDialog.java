package org.nstodc.ui.configuration;

import org.nstodc.database.type.Breed;
import org.nstodc.database.type.Suburb;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Add / edit a handler.
 */
public class UpsertBreedDialog extends JDialog {

    private final BreedsDialog owner;
    private final Breed breed;

    private final JTextField breedNameTF = new JTextField(20);

    public UpsertBreedDialog(BreedsDialog owner, final boolean nyoo, final Breed breed) {
        super(owner, (nyoo ? "Add" : "Update") + " Breed", true);
        this.owner = owner;
        this.breed = breed;
        UiUtils.locate(this, owner.getPreferences());

        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////

        JPanel centerOuterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel centerInnerPanel = new JPanel(new GridLayout(0,1));
        centerOuterPanel.add(centerInnerPanel);
        getContentPane().add(centerOuterPanel, BorderLayout.CENTER);

        centerInnerPanel.add(UiUtils.enFlow(new JLabel("Name"), breedNameTF));

        breedNameTF.setText(breed.getBreed());

        //////////
        // East //
        //////////

        JButton okButton = UiUtils.addEast(this);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateIt()) {
                    breed.setBreed(breedNameTF.getText().trim().toUpperCase());

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

    private void saveNew() {
        owner.addBreed(breed);
    }

    private void saveExisting() {
        owner.updateBreed(breed);
    }

    private boolean validateIt() {
        // Dog must have a name
        if (breedNameTF.getText().trim().length() == 0) {
            JOptionPane.showMessageDialog(this, "Name required.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Make sure we don't already have this.
        for (Breed breed1 : owner.getDatabase().getBreeds()) {
            if (breed1.getBreed().equalsIgnoreCase(breedNameTF.getText().trim()) &&
                    breed1.getBreedId() != breed.getBreedId()) {
                JOptionPane.showMessageDialog(this, "Duplicate breed.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(UpsertBreedDialog.this, owner.getPreferences());
    }

}
