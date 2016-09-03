package org.nstodc.ui.reports;

import org.nstodc.database.type.Membership;
import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Display new members by month.
 * Can select year from dropdown.
 */
public class NewMembersByMonthDialog extends JDialog {

    private UI owner;

    private JRadioButton thisYearRB = new JRadioButton("This Year");

    private JLabel janL = new JLabel("Jan:");
    private JLabel febL = new JLabel("Feb:");
    private JLabel marL = new JLabel("Mar:");
    private JLabel aprL = new JLabel("Apr:");
    private JLabel mayL = new JLabel("May:");
    private JLabel junL = new JLabel("Jun:");
    private JLabel julL = new JLabel("Jul:");
    private JLabel augL = new JLabel("Aug:");
    private JLabel sepL = new JLabel("Sep:");
    private JLabel octL = new JLabel("Oct:");
    private JLabel novL = new JLabel("Nov:");
    private JLabel decL = new JLabel("Dec:");

    public NewMembersByMonthDialog(UI owner) {

        super(owner, "New Members", true);
        this.owner = owner;
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());

        getContentPane().setLayout(new BorderLayout());

        ButtonGroup bg = new ButtonGroup();
        bg.add(thisYearRB);
        JRadioButton lastYearRB = new JRadioButton("Last Year");
        bg.add(lastYearRB);
        thisYearRB.setSelected(true);
        thisYearRB.addActionListener(e -> updateIt());
        lastYearRB.addActionListener(e -> updateIt());

        ////////////
        // Center //
        ////////////

        JPanel center = new JPanel(new GridLayout(0, 1));
        getContentPane().add(UiUtils.enFlow(center));
        center.add(thisYearRB);
        center.add(lastYearRB);
        center.add(janL);
        center.add(febL);
        center.add(marL);
        center.add(aprL);
        center.add(mayL);
        center.add(junL);
        center.add(julL);
        center.add(augL);
        center.add(sepL);
        center.add(octL);
        center.add(novL);
        center.add(decL);

        //////////
        // East //
        //////////
        JButton okButton = UiUtils.addEast(this, false);
        okButton.addActionListener(e -> dispose());

        pack();
        setResizable(false);

        updateIt();

    }

    private void updateIt() {
        int year;
        if (thisYearRB.isSelected()) {
            year = Calendar.getInstance().get(Calendar.YEAR);
        } else {
            year = Calendar.getInstance().get(Calendar.YEAR) - 1;
        }
        int[] counts = new int[12];
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        for (Membership membership : owner.getDatabase().getMemberships()) {
            String dateJoinedString = membership.getDateJoined();
            try {
                Date dateJoined = sdf.parse(dateJoinedString);
                cal.setTime(dateJoined);
                int membershipYear = cal.get(Calendar.YEAR);
                int membershipMonth = cal.get(Calendar.MONTH);
                if (year == membershipYear) {
                    counts[membershipMonth] += 1;
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Error", "Invalid date joined: " + dateJoinedString, JOptionPane.ERROR_MESSAGE);
            }
        }
        for (int i = 0; i < counts.length; i++) {
            switch (i) {
                case 0:
                    janL.setText("Jan: " + counts[0]);
                    break;
                case 1:
                    febL.setText("Feb: " + counts[1]);
                    break;
                case 2:
                    marL.setText("Mar: " + counts[2]);
                    break;
                case 3:
                    aprL.setText("Apr: " + counts[3]);
                    break;
                case 4:
                    mayL.setText("May: " + counts[4]);
                    break;
                case 5:
                    junL.setText("Jun: " + counts[5]);
                    break;
                case 6:
                    julL.setText("Jul: " + counts[6]);
                    break;
                case 7:
                    augL.setText("Aug: " + counts[7]);
                    break;
                case 8:
                    sepL.setText("Sep: " + counts[8]);
                    break;
                case 9:
                    octL.setText("Oct: " + counts[9]);
                    break;
                case 10:
                    novL.setText("Nov: " + counts[10]);
                    break;
                case 11:
                    decL.setText("Dec: " + counts[11]);
                    break;
            }
        }
    }

    public void dispose() {
        super.dispose();
        UiUtils.updateLocation(NewMembersByMonthDialog.this, owner.getPreferences());
    }


}
