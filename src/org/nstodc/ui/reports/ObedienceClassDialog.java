package org.nstodc.ui.reports;

import org.nstodc.database.type.Dog;
import org.nstodc.database.type.Handler;
import org.nstodc.database.type.ObedienceClass;
import org.nstodc.print.obedience.ObedienceBean;
import org.nstodc.print.obedience.ObedienceClassBean;
import org.nstodc.print.obedience.ObedienceClassEntryBean;
import org.nstodc.print.obedience.ObediencePrintable;
import org.nstodc.ui.Constants;
import org.nstodc.ui.UI;
import org.nstodc.ui.UiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static org.nstodc.ui.Constants.*;

/**
 * Display list of classes to print.
 */
public class ObedienceClassDialog extends JDialog {

    private UI owner;
    private final Map<Integer, ClassTuple> tuples = new TreeMap<>();

    public ObedienceClassDialog(UI owner) {

        super(owner, "Obedience Class", true);
        this.owner = owner;
        UiUtils.locateAndCrippleClose(this, owner.getPreferences());

        for (ObedienceClass obedienceClass : owner.getDatabase().getObedienceClasses()) {
            SpinnerNumberModel model = new SpinnerNumberModel(1, 0, 5, 1);
            JSpinner spinner = new JSpinner(model);
            ClassTuple tuple = new ClassTuple(spinner, obedienceClass);
            int count = owner.getPreferences().getInt(UI_OBEDIENCE_CLASS + obedienceClass.getObedienceClass(), 1);
            model.setValue(count);
            tuples.put(obedienceClass.getListSequenceId(), tuple);
        }

        getContentPane().setLayout(new BorderLayout());

        ////////////
        // Center //
        ////////////

        JPanel center = new JPanel(new GridLayout(0, 1));
        getContentPane().add(UiUtils.enFlow(center));
        java.util.List<JComponent> labels = new ArrayList<>();
        for (ClassTuple tuple : tuples.values()) {
            JLabel label = new JLabel(tuple.getObedienceClass().getObedienceClass());
            center.add(UiUtils.enFlow(label, tuple.getSpinner()));
            labels.add(label);
        }

        UiUtils.sameWidth(labels);

        //////////
        // East //
        //////////
        JButton okButton = UiUtils.addEast(this);
        okButton.addActionListener(e -> {
            printObedienceClass();
            dispose();
        });

        pack();
        setResizable(false);

    }

    private void printObedienceClass() {
        ObedienceBean ob = createObedienceBean();
        printObedienceClasses(ob);
    }

    private void printObedienceClasses(ObedienceBean obedienceBean) {
        ObediencePrintable obediencePrintable = new ObediencePrintable(obedienceBean);
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(obediencePrintable);
        job.setJobName("Obedience Classes");
        boolean ok = job.printDialog();
        if (ok) {
            Cursor c = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                setCursor(c);
            }
        }
    }

    private ObedienceBean createObedienceBean() {
        int year = UiUtils.defaultYear();
        Map<Integer, ObedienceClass> map = new TreeMap<>();
        for (ClassTuple tuple : tuples.values()) {
            SpinnerNumberModel m = (SpinnerNumberModel) (tuple.getSpinner().getModel());
            int v = m.getNumber().intValue();
            for (int i = 0; i < v; i++) {
                map.put(10 * tuple.getObedienceClass().getListSequenceId() + i, tuple.getObedienceClass());
            }
        }
        Map<String, Dog> dogsMap = new TreeMap<>();
        for (Dog dog : owner.getDatabase().getDogs()) {
            dogsMap.put(dog.getName() + Math.random(), dog);
        }
        ObedienceBean ob = new ObedienceBean(UiUtils.nextSunday());
        for (Map.Entry<Integer, ObedienceClass> entry : map.entrySet()) {
            ObedienceClassBean ocb = new ObedienceClassBean(
                    entry.getKey(),
                    entry.getValue().getObedienceClass()
            );
            int entryIndex = 0;
            ob.getClasses().add(ocb);
            for (Dog dog : dogsMap.values()) {
                if (dog.getObedienceClassId() == entry.getValue().getObedienceClassId() &&
                        dog.getMembershipYear() >= year &&
                        dog.isDoesObedience()) {
                    Handler handler = findPrimaryOrRandomHandler(dog.getMembershipId());
                    if (handler != null) {
                        ObedienceClassEntryBean oceb = new ObedienceClassEntryBean(
                                dog.getMembershipId(),
                                handler.getFirstName(),
                                handler.getLastName(),
                                dog.getName()
                        );
                        ocb.getEntries().put(entryIndex++, oceb);
                    }
                }
            }
        }
        return ob;
    }

    private Handler findPrimaryOrRandomHandler(int membershipId) {
        Handler current = null;
        for (Handler handler : owner.getDatabase().getHandlers()) {
            if (handler.getMembershipId() == membershipId && handler.isPrimary()) {
                return handler;
            }
            current = handler;
        }
        return current;
    }

    public void dispose() {
        super.dispose();
        for (ClassTuple tuple : tuples.values()) {
            SpinnerNumberModel m = (SpinnerNumberModel) (tuple.getSpinner().getModel());
            int v = m.getNumber().intValue();
            owner.getPreferences().putInt(UI_OBEDIENCE_CLASS + tuple.getObedienceClass().getObedienceClass(), v);
        }
        UiUtils.updateLocation(ObedienceClassDialog.this, owner.getPreferences());
    }

    private class ClassTuple {
        private JSpinner spinner;
        private ObedienceClass obedienceClass;

        public ClassTuple(JSpinner spinner, ObedienceClass obedienceClass) {
            this.spinner = spinner;
            this.obedienceClass = obedienceClass;
        }

        public JSpinner getSpinner() {
            return spinner;
        }

        public ObedienceClass getObedienceClass() {
            return obedienceClass;
        }
    }

}
