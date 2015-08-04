package org.nstodc.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Utility stuff for UI.
 */
public class UiUtils {

    public static void locateAndCrippleClose(JDialog dialog, Preferences preferences) {
        String fullClassName = dialog.getClass().getCanonicalName();
        String className = fullClassName.substring(1 + fullClassName.lastIndexOf("."));
        String xName = className + "X";
        String yName = className + "Y";
        int x = preferences.getInt(xName, 10);
        int y = preferences.getInt(yName, 10);
        if (x < 0 || x >= Toolkit.getDefaultToolkit().getScreenSize().getWidth()) {
            x = 0;
        }
        if (y < 0 || y >= Toolkit.getDefaultToolkit().getScreenSize().getHeight()) {
            y = 0;
        }
        dialog.setLocation(x, y);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    }

    public static void updateLocation(JDialog dialog, Preferences preferences) {
        String fullClassName = dialog.getClass().getCanonicalName();
        String className = fullClassName.substring(1 + fullClassName.lastIndexOf("."));
        String xName = className + "X";
        String yName = className + "Y";
        preferences.putInt(xName, dialog.getX());
        preferences.putInt(yName, dialog.getY());
    }

    public static JButton addEast(final JDialog dialog) {
        return addEast(dialog, true);
    }

    public static JButton addEast(final JDialog dialog, boolean withCancel) {

        JButton okButton = new JButton("OK");

        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dialog.getContentPane().add(flow, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(0, 1));
        flow.add(grid);
        grid.add(okButton);
        if (withCancel) {
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            });
            grid.add(cancelButton);
        }
        dialog.getRootPane().setDefaultButton(okButton);
        return okButton;
    }

    public static JButton addEast(final JDialog dialog, JButton... otherButtons) {

        JButton okButton = new JButton("OK");

        JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dialog.getContentPane().add(flow, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(0, 1));
        flow.add(grid);
        grid.add(okButton);
        for (JButton otherButton : otherButtons) {
            if (otherButton != null) {
                grid.add(otherButton);
            }
        }
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        grid.add(cancelButton);
        dialog.getRootPane().setDefaultButton(okButton);
        return okButton;
    }

    public static String today() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    public static int defaultYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        if (cal.get(Calendar.MONTH) == Calendar.NOVEMBER || cal.get(Calendar.MONTH) == Calendar.DECEMBER) {
            // Next calendar year.
            return year + 1;
        }
        return year;
    }

    public static JPanel enFlow(Component... components) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (Component component : components) {
            p.add(component);
        }
        return p;
    }

    public static boolean isValidDate(String dateOfBirth) {
        if (dateOfBirth.trim().length() == 0) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            sdf.parse(dateOfBirth);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public static String lastSunday() {
        return findSunday(false);
    }

    public static String nextSunday() {
        return findSunday(true);
    }

    private static String findSunday(boolean next) {
        Calendar cal = Calendar.getInstance();
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            cal.add(Calendar.DATE, next ? 1 : -1);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(cal.getTime());
    }

    public static void sameWidth(JComponent... components) {
        List<JComponent> list = new ArrayList<>();
        Collections.addAll(list, components);
        sameWidth(list);
    }

    public static void sameWidth(List<JComponent> labels) {
        double preferredWidth = 0;
        for (JComponent component : labels) {
            if (component.getPreferredSize().getWidth() > preferredWidth) {
                preferredWidth = component.getPreferredSize().getWidth();
            }
        }
        for (JComponent component : labels) {
            component.setPreferredSize(new Dimension((int) preferredWidth, (int) component.getPreferredSize().getHeight()));
        }
    }
}

