package org.nstodc.ui;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.nstodc.database.Database;
import org.nstodc.database.type.*;
import org.nstodc.print.van.VanBean;
import org.nstodc.print.van.VanDetailEntry;
import org.nstodc.print.van.VanPrintable;
import org.nstodc.ui.configuration.BreedsDialog;
import org.nstodc.ui.configuration.PaymentAmountDialog;
import org.nstodc.ui.configuration.SuburbsDialog;
import org.nstodc.ui.membership.MembershipBundle;
import org.nstodc.ui.membership.MembershipDialog;
import org.nstodc.ui.reports.MembershipCountReportDialog;
import org.nstodc.ui.reports.NewMembersByMonthDialog;
import org.nstodc.ui.reports.ObedienceClassDialog;
import org.nstodc.ui.reports.StatsDialog;
import org.nstodc.ui.search.SearchDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.nstodc.ui.Constants.*;

/**
 * Principle graphical interface for the database.
 */
public class UI extends JFrame implements IOwner {

    private final Preferences preferences = Preferences.userNodeForPackage(getClass());
    private Database database = new Database();
    private final AtomicBoolean loadedDatabaseSuccessfully = new AtomicBoolean();
    private final JMenuItem latestMenuItem;
    private int latestMembership;

    public static void main(String[] args) {
        UI ui = new UI(args.length == 1 && args[0].equals("init"));
        ui.setVisible(true);
    }

    public UI(final boolean init) throws HeadlessException {
        setTitle(UI_TITLE + " " + UI_VERSION);
        doIcon();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Don't care.
        }
        latestMenuItem = new JMenuItem("Edit Latest...");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                tryToClose();
            }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        doPreferences();
        final Thread t = new Thread() {
            public void run() {
                try {
                    if (init) {
                        initializeDatabase();
                    } else {
                        loadDatabase();
                    }
                    setUITitle();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            checkLastArchived();
                        }
                    });
                } catch (Exception e) {
                    // Don't care.
                }
            }
        };
        t.start();
    }

    private void checkLastArchived() {
        Date lastArchived = database.getLastArchived();
        if (lastArchived == null) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Date oneMonthAgo = cal.getTime();
        if (lastArchived.compareTo(oneMonthAgo) < 0) {
            JOptionPane.showMessageDialog(this, "The database has not been archived for over a month.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void doIcon() {
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("paw.png")));
        } catch (Exception e) {
            // Don't care.
        }
    }

    private void setUITitle() {
        String fileLocation = preferences.get(Constants.DATABASE_FILE_LOCATION, Constants.DATABASE_FILE_LOCATION_DEFAULT);
        setTitle(UI_TITLE + " " + UI_VERSION + " - [" + new File(fileLocation, Constants.DATABASE_FILE_NAME).getAbsolutePath() + ']');
    }

    private void initializeDatabase() {
        String fileLocation = preferences.get(Constants.DATABASE_FILE_LOCATION, Constants.DATABASE_FILE_LOCATION_DEFAULT);
        File file = new File(fileLocation, Constants.DATABASE_FILE_NAME);
        if (file.exists()) {
            JOptionPane.showMessageDialog(this, "Can't initialize database.\n\nDatabase already exists at " + fileLocation + ".",
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        } else {
            database = new Database();
            JOptionPane.showMessageDialog(this, "New database initialized.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            loadedDatabaseSuccessfully.set(true);
        }
    }

    private void loadDatabase() {
        boolean success = true;
        String fileLocation = preferences.get(Constants.DATABASE_FILE_LOCATION, Constants.DATABASE_FILE_LOCATION_DEFAULT);
        File file = new File(fileLocation, Constants.DATABASE_FILE_NAME);

        // Backup.
        File backup = new File(fileLocation, Constants.DATABASE_BACKUP_NAME);
        FileSystem fileSystem = FileSystems.getDefault();
        Path filePath = fileSystem.getPath(file.getAbsolutePath());
        Path backupPath = fileSystem.getPath(backup.getAbsolutePath());
        try {
            Files.copy(filePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not backup database at " + fileLocation + ".",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            success = false;
        }

        // Load
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not open database at " + fileLocation + ".",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            success = false;
        }
        final StringBuilder sb = new StringBuilder();
        if (success) {
            try {
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Could not read database at " + fileLocation + ".",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    success = false;
                }
            } finally {
                try {
                    br.close();
                } catch (Exception e) {
                    // Don't care
                }
            }

            // Decode
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                XStream xstream = new XStream(new DomDriver());
                database = (Database) xstream.fromXML(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                success = false;
            } finally {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

        loadedDatabaseSuccessfully.set(success);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                doMenu();
            }
        });
    }

    private void doMenu() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // ///////
        // File //
        // ///////
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.setMnemonic(KeyEvent.VK_F);

        // Add
        if (loadedDatabaseSuccessfully.get()) {
            JMenuItem addMenuItem = new JMenuItem("Add Membership...");
            fileMenu.add(addMenuItem);
            addMenuItem.setMnemonic(KeyEvent.VK_A);
            addMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
            addMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addMembership();
                }
            });

            // Search
            JMenuItem searchMenuItem = new JMenuItem("Search...");
            fileMenu.add(searchMenuItem);
            searchMenuItem.setMnemonic(KeyEvent.VK_E);
            searchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
            searchMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SearchDialog d = new SearchDialog(UI.this);
                    d.setVisible(true);
                }
            });

            // Latest
            fileMenu.add(latestMenuItem);
            latestMenuItem.setMnemonic(KeyEvent.VK_L);
            latestMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
            latestMenuItem.setEnabled(false);
            latestMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editMembership(latestMembership, Tabs.Membership);
                }
            });

            fileMenu.addSeparator();

            // Save
            JMenuItem saveMenuItem = new JMenuItem("Save");
            fileMenu.add(saveMenuItem);
            saveMenuItem.setMnemonic(KeyEvent.VK_S);
            saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
            saveMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveDatabase();
                }
            });

            // Archive
            JMenuItem archiveMenuItem = new JMenuItem("Archive");
            fileMenu.add(archiveMenuItem);
            archiveMenuItem.setMnemonic(KeyEvent.VK_A);
            archiveMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    archiveDatabase();
                }
            });

            fileMenu.addSeparator();

            // Exit without saving
            JMenuItem exitWithoutMenuItem = new JMenuItem("Exit Without Saving...");
            fileMenu.add(exitWithoutMenuItem);
            archiveMenuItem.setMnemonic(KeyEvent.VK_W);
            exitWithoutMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exitWithoutSaving();
                }
            });
        }

        // Exit
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        fileMenu.add(exitMenuItem);
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tryToClose();
            }
        });

        /////////////
        // Reports //
        /////////////
        if (loadedDatabaseSuccessfully.get()) {
            JMenu reportsMenu = new JMenu("Reports");
            menuBar.add(reportsMenu);
            reportsMenu.setMnemonic(KeyEvent.VK_R);

            // Obedience class
            JMenuItem obedienceClassMenuItem = new JMenuItem("Obedience Classes...");
            reportsMenu.add(obedienceClassMenuItem);
            obedienceClassMenuItem.setMnemonic(KeyEvent.VK_O);
            obedienceClassMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ObedienceClassDialog d = new ObedienceClassDialog(UI.this);
                    d.setVisible(true);
                }
            });

            // New Members
            JMenuItem newMembersByMonthItem = new JMenuItem("New Members...");
            reportsMenu.add(newMembersByMonthItem);
            newMembersByMonthItem.setMnemonic(KeyEvent.VK_N);
            newMembersByMonthItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    newMembersByMonth();
                }
            });

            // Sponsor Report
            JMenuItem sponsorReportItem = new JMenuItem("Sponsor Report...");
            reportsMenu.add(sponsorReportItem);
            sponsorReportItem.setMnemonic(KeyEvent.VK_S);
            sponsorReportItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    sponsorReport();
                }
            });

            // Van Report
            JMenuItem vanReportItem = new JMenuItem("Van Report...");
            reportsMenu.add(vanReportItem);
            vanReportItem.setMnemonic(KeyEvent.VK_V);
            vanReportItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    vanReport();
                }
            });

            // Count of memberships by type
            JMenuItem membershipCountItem = new JMenuItem("Membership Count...");
            reportsMenu.add(membershipCountItem);
            membershipCountItem.setMnemonic(KeyEvent.VK_C);
            membershipCountItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    membershipCountReport();
                }
            });

            // Stats
            JMenuItem statsItem = new JMenuItem("Statistics...");
            reportsMenu.add(statsItem);
            statsItem.setMnemonic(KeyEvent.VK_S);
            statsItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    statsReport();
                }
            });
        }

        ///////////////////
        // Configuration //
        ///////////////////
        JMenu configurationMenu = new JMenu("Configuration");
        menuBar.add(configurationMenu);
        configurationMenu.setMnemonic(KeyEvent.VK_C);
        if (loadedDatabaseSuccessfully.get()) {
            // Payment Amount
            JMenuItem paymentAmountMenuItem = new JMenuItem("Payment Amount...");
            configurationMenu.add(paymentAmountMenuItem);
            paymentAmountMenuItem.setMnemonic(KeyEvent.VK_P);
            paymentAmountMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PaymentAmountDialog d = new PaymentAmountDialog(UI.this);
                    d.setVisible(true);
                }
            });

            // Breeds
            JMenuItem breedsMenuItem = new JMenuItem("Dog Breeds...");
            configurationMenu.add(breedsMenuItem);
            breedsMenuItem.setMnemonic(KeyEvent.VK_D);
            breedsMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    BreedsDialog d = new BreedsDialog(UI.this);
                    d.setVisible(true);
                }
            });

            // Suburbs
            JMenuItem suburbsMenuItem = new JMenuItem("Suburbs...");
            configurationMenu.add(suburbsMenuItem);
            suburbsMenuItem.setMnemonic(KeyEvent.VK_S);
            suburbsMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SuburbsDialog d = new SuburbsDialog(UI.this);
                    d.setVisible(true);
                }
            });

            configurationMenu.addSeparator();
        }

        // Database
        JMenuItem databaseMenuItem = new JMenuItem("Database Location...");
        configurationMenu.add(databaseMenuItem);
        databaseMenuItem.setMnemonic(KeyEvent.VK_D);
        databaseMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectDatabase();
            }
        });

        if (loadedDatabaseSuccessfully.get()) {
            // Archive
            JMenuItem archiveLocationMenuItem = new JMenuItem("Archive Location...");
            configurationMenu.add(archiveLocationMenuItem);
            archiveLocationMenuItem.setMnemonic(KeyEvent.VK_A);
            archiveLocationMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    archiveLocation();
                }
            });
        }

    }

    private void statsReport() {
        Map<Integer, String> names = createNamesStats();
        Map<Integer, String> breeds = createBreedsStats();
        Map<Integer, String> suburbs = createSuburbsStats();
        showStats(names, breeds, suburbs);
    }

    private void showStats(Map<Integer, String> names, Map<Integer, String> breeds, Map<Integer, String> suburbs) {
        StatsDialog d = new StatsDialog(this, names, breeds, suburbs);
        d.setVisible(true);
    }

    private Map<Integer, String> createSuburbsStats() {
        Map<Integer, Integer> suburbs = new TreeMap<>();
        for (Membership membership : database.getMemberships()) {
            boolean currentDog = false;
            for (Dog dog : database.getDogs()) {
                if (dog.getMembershipId() == membership.getMembershipId() &&
                        dog.getMembershipYear() >= UiUtils.defaultYear()) {
                    currentDog = true;
                    break;
                }
            }
            if (currentDog) {
                if (!suburbs.containsKey(membership.getSuburbId())) {
                    suburbs.put(membership.getSuburbId(), 0);
                }
                suburbs.put(membership.getSuburbId(), suburbs.get(membership.getSuburbId()) + 1);
            }
        }

        Map<Integer, Integer> intermediates = new TreeMap<>();
        int i = 0;
        do {
            int suburb = 0;
            int count = 0;
            for (Map.Entry<Integer, Integer> entry : suburbs.entrySet()) {
                if (!intermediates.values().contains(entry.getKey())) {
                    if (entry.getValue() > count) {
                        count = entry.getValue();
                        suburb = entry.getKey();
                    }
                }
            }
            intermediates.put(i, suburb);
        } while (i++ < 13);

        i = 0;
        Map<Integer, String> results = new TreeMap<>();
        for (Integer integer : intermediates.values()) {
            for (Suburb suburb : database.getSuburbs()) {
                if (suburb.getSuburbId() == integer) {
                    results.put(i++, suburb.getSuburb());
                }
            }
        }
        return results;
    }

    private Map<Integer, String> createBreedsStats() {

        Map<Integer, Integer> breeds = new TreeMap<>();
        for (Membership membership : database.getMemberships()) {
            for (Dog dog : database.getDogs()) {
                if (dog.getMembershipId() == membership.getMembershipId() &&
                        dog.getMembershipYear() >= UiUtils.defaultYear()) {
                    if (!breeds.containsKey(dog.getBreedId())) {
                        breeds.put(dog.getBreedId(), 0);
                    }
                    breeds.put(dog.getBreedId(), breeds.get(dog.getBreedId()) + 1);
                }
            }
        }

        Map<Integer, Integer> intermediates = new TreeMap<>();
        int i = 0;
        do {
            int breed = 0;
            int count = 0;
            for (Map.Entry<Integer, Integer> entry : breeds.entrySet()) {
                if (!intermediates.values().contains(entry.getKey())) {
                    if (entry.getValue() > count) {
                        count = entry.getValue();
                        breed = entry.getKey();
                    }
                }
            }
            intermediates.put(i, breed);
        } while (i++ < 13);

        i = 0;
        Map<Integer, String> results = new TreeMap<>();
        for (Integer integer : intermediates.values()) {
            for (Breed breed : database.getBreeds()) {
                if (breed.getBreedId() == integer) {
                    results.put(i++, breed.getBreed());
                }
            }
        }
        return results;

    }

    private Map<Integer, String> createNamesStats() {

        Map<String, Integer> names = new TreeMap<>();
        for (Membership membership : database.getMemberships()) {
            for (Dog dog : database.getDogs()) {
                if (dog.getMembershipId() == membership.getMembershipId() &&
                        dog.getMembershipYear() >= UiUtils.defaultYear()) {
                    if (!names.containsKey(dog.getName())) {
                        names.put(dog.getName(), 0);
                    }
                    names.put(dog.getName(), names.get(dog.getName()) + 1);
                }
            }
        }
        Map<Integer, String> results = new TreeMap<>();
        int i = 0;
        do {
            String name = "";
            int count = 0;
            for (Map.Entry<String, Integer> entry : names.entrySet()) {
                if (!results.values().contains(entry.getKey())) {
                    if (entry.getValue() > count) {
                        count = entry.getValue();
                        name = entry.getKey();
                    }
                }
            }
            results.put(i, name);
        } while (i++ < 12);
        return results;
    }

    private void membershipCountReport() {
        MembershipCountReportDialog d = new MembershipCountReportDialog(this);
        d.setVisible(true);
    }

    private void sponsorReport() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date to = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date from = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Map<String, SponsorshipReportLine> map = new TreeMap<>();
            for (Membership membership : database.getMemberships()) {

                if (!membership.isAllowSponsorship()) {
                    continue;
                }

                Date membershipDate = sdf.parse(membership.getDateJoined());
                if (membershipDate.before(from) || membershipDate.after(to)) {
                    continue;
                }

                String address = membership.getAddress();
                String phone = membership.getPhone();
                String mobile = membership.getMobile();
                String date = membership.getDateJoined();
                String suburb = "";
                String postcode = "";

                for (Suburb s : database.getSuburbs()) {
                    if (membership.getSuburbId() == s.getSuburbId()) {
                        suburb = s.getSuburb();
                        postcode = s.getPostcode();
                        break;
                    }
                }

                for (Handler handler : database.getHandlers()) {
                    if (handler.getMembershipId() == membership.getMembershipId()) {
                        String firstName = handler.getFirstName();
                        String lastName = handler.getLastName();
                        SponsorshipReportLine line = new SponsorshipReportLine(lastName, firstName, phone, mobile,
                                address, suburb, postcode, date);
                        map.put(line.lastName + line.firstName + String.valueOf(Math.random()), line);
                    }
                }
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(new ExtensionFileFilter("Excel Files", "xls"));
            int result = chooser.showSaveDialog(this);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (result == JFileChooser.APPROVE_OPTION) {

                Workbook wb = new HSSFWorkbook();
                Sheet sheet1 = wb.createSheet("Sheet 1");

                Row headings = sheet1.createRow(0);
                Cell cell = headings.createCell(0);
                cell.setCellValue("Last Name");
                cell = headings.createCell(1);
                cell.setCellValue("First Name");
                cell = headings.createCell(2);
                cell.setCellValue("Phone");
                cell = headings.createCell(3);
                cell.setCellValue("Mobile");
                cell = headings.createCell(4);
                cell.setCellValue("Address");
                cell = headings.createCell(5);
                cell.setCellValue("Suburb");
                cell = headings.createCell(6);
                cell.setCellValue("Postcode");
                cell = headings.createCell(7);
                cell.setCellValue("Date");

                int rowNumber = 1;
                for (SponsorshipReportLine line : map.values()) {
                    Row row = sheet1.createRow(rowNumber++);
                    cell = row.createCell(0);
                    cell.setCellValue(purify(line.lastName));
                    cell = row.createCell(1);
                    cell.setCellValue(purify(line.firstName));
                    cell = row.createCell(2);
                    cell.setCellValue(purify(line.phone));
                    cell = row.createCell(3);
                    cell.setCellValue(purify(line.mobile));
                    cell = row.createCell(4);
                    cell.setCellValue(purify(line.address));
                    cell = row.createCell(5);
                    cell.setCellValue(purify(line.suburb));
                    cell = row.createCell(6);
                    cell.setCellValue(purify(line.postcode));
                    cell = row.createCell(7);
                    cell.setCellValue(purify(line.date));
                }

                File file = chooser.getSelectedFile();
                FileOutputStream fileOut = new FileOutputStream(file);
                wb.write(fileOut);
                fileOut.close();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String purify(String str) {
        return str.replace(",", "").replace("\"", "");
    }

    private void exitWithoutSaving() {
        int result = JOptionPane.showConfirmDialog(this, "Exit without saving any changes?", "Exit",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            System.exit(0);
        }
    }

    private void vanReport() {
        VanBean vb = createVanBean();
        printVanReport(vb);
    }

    private void printVanReport(VanBean vb) {
        VanPrintable vanPrintable = new VanPrintable(vb);
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(vanPrintable);
        job.setJobName("Van Report");
        boolean ok = job.printDialog();
        if (ok) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private VanBean createVanBean() {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        VanBean vb = new VanBean();
        for (Membership membership : database.getMemberships()) {
            boolean financialDog = false;
            for (Dog dog : database.getDogs()) {
                if (dog.getMembershipId() == membership.getMembershipId() &&
                        dog.getMembershipYear() >= currentYear) {
                    financialDog = true;
                    break;
                }
            }
            if (financialDog) {
                String suburb = "";
                String postcode = "";
                for (Suburb s : database.getSuburbs()) {
                    if (membership.getSuburbId() == s.getSuburbId()) {
                        suburb = s.getSuburb();
                        postcode = s.getPostcode();
                        break;
                    }
                }
                Set<String> handlers = createSurnameUniqueSet(membership.getMembershipId());
                for (String entry : handlers) {
                    VanDetailEntry vbe = new VanDetailEntry(entry, membership.getAddress(), suburb, postcode);
                    vb.getEntries().put((entry + String.valueOf(Math.random())).toUpperCase(), vbe);
                }

            }
        }
        return vb;
    }

    // Convert lots of family members into one, like "Smith: Bob, Jim and Sheila".
    private Set<String> createSurnameUniqueSet(int membershipId) {
        Map<String, Set<String>> membersBySurname = new HashMap<>(); // <"Smith", <"Bob, Jim, Sheila">>
        for (Handler handler : database.getHandlers()) {
            if (handler.getMembershipId() == membershipId) {
                Set<String> firstNames;
                if (membersBySurname.containsKey(handler.getLastName())) {
                    firstNames = membersBySurname.get(handler.getLastName());
                } else {
                    firstNames = new TreeSet<>();
                    membersBySurname.put(handler.getLastName(), firstNames);
                }
                firstNames.add(handler.getFirstName());
            }
        }
        Set<String> uniqueNames = new HashSet<>(); // <"Smith: Bob, Jim and Sheila">
        for (Map.Entry<String, Set<String>> entry : membersBySurname.entrySet()) {
            String last = entry.getKey();
            int index = 0;
            StringBuilder first = new StringBuilder();
            for (String firstName : entry.getValue()) {
                if (index == 0) {
                    first.append(firstName);
                } else if (index == entry.getValue().size() - 1) {
                    first.append(" and ").append(firstName);
                } else {
                    first.append(", ").append(firstName);
                }
                index++;
            }
            uniqueNames.add(last + ": " + first.toString());
        }
        return uniqueNames;
    }

    private void newMembersByMonth() {
        NewMembersByMonthDialog d = new NewMembersByMonthDialog(this);
        d.setVisible(true);
    }

    private void addMembership() {
        Cursor c = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int membershipId = database.generateNextMembershipId();
        Membership membership = new Membership(membershipId, UiUtils.today());

        MembershipBundle bundle = new MembershipBundle(membership);
        MembershipDialog d = new MembershipDialog(this, true, bundle, Tabs.Membership);
        setCursor(c);
        d.setVisible(true);
    }

    private void archiveLocation() {
        Cursor c = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String archiveLocation = preferences.get(Constants.ARCHIVE_FILE_LOCATION, "");
        File originalLocation = new File(archiveLocation);
        if (originalLocation.exists() && originalLocation.isDirectory()) {
            chooser.setCurrentDirectory(originalLocation);
        }
        setCursor(c);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File candidate = chooser.getSelectedFile();
            if (candidate.getAbsolutePath().equals(originalLocation.getAbsolutePath())) {
                return;
            }
            preferences.put(Constants.ARCHIVE_FILE_LOCATION, candidate.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Database will now be archived to " + candidate.getAbsolutePath() + ".", "Info", JOptionPane.WARNING_MESSAGE);

        }
    }

    private void selectDatabase() {
        int i = JOptionPane.showConfirmDialog(this, "Are you sure you want to change the location of the database?", "Database", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (i == 0) {
            Cursor c = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            String fileLocation = preferences.get(Constants.DATABASE_FILE_LOCATION, Constants.DATABASE_FILE_LOCATION_DEFAULT);
            File originalLocation = new File(fileLocation);
            chooser.setCurrentDirectory(originalLocation);
            setCursor(c);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File candidate = chooser.getSelectedFile();
                if (candidate.getAbsolutePath().equals(originalLocation.getAbsolutePath())) {
                    return;
                }
                preferences.put(Constants.DATABASE_FILE_LOCATION, candidate.getAbsolutePath());
                setUITitle();
                JOptionPane.showMessageDialog(this, "The database will now be stored at " + candidate.getAbsolutePath() + ".", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void doPreferences() {
        int x = preferences.getInt(UI_X, UI_X_DEFAULT);
        int y = preferences.getInt(UI_Y, UI_Y_DEFAULT);
        int w = preferences.getInt(UI_WIDTH, UI_WIDTH_DEFAULT);
        int h = preferences.getInt(UI_HEIGHT, UI_HEIGHT_DEFAULT);
        int s = preferences.getInt(UI_STATE, UI_STATE_DEFAULT);
        if (x < 0 || x >= Toolkit.getDefaultToolkit().getScreenSize().width) {
            x = 0;
        }
        if (y < 0 || y >= Toolkit.getDefaultToolkit().getScreenSize().getHeight()) {
            y = 0;
        }
        setLocation(x, y);
        setSize(w, h);
        setExtendedState(s);
    }

    public void tryToClose() {
        if (loadedDatabaseSuccessfully.get()) {
            try {
                int s = getExtendedState();
                if (s == Frame.NORMAL) {
                    preferences.putInt(UI_X, getX());
                    preferences.putInt(UI_Y, getY());
                    preferences.putInt(UI_WIDTH, getWidth());
                    preferences.putInt(UI_HEIGHT, getHeight());
                }
                preferences.putInt(UI_STATE, getExtendedState());
                preferences.flush();
            } catch (BackingStoreException e) {
                // Don't care.
            }

            // Save inline cos we are going to sys-ex after this.
            XStream xstream = new XStream(new DomDriver());
            String xml = xstream.toXML(database);
            // Primary
            String fileLocation = preferences.get(Constants.DATABASE_FILE_LOCATION, Constants.DATABASE_FILE_LOCATION_DEFAULT);
            try {
                try (PrintWriter pw = new PrintWriter(new FileWriter(new File(fileLocation, Constants.DATABASE_FILE_NAME)))) {
                    pw.println(xml);
                    pw.flush();
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(UI.this, "Could not save database at " + fileLocation + ".",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                // Protect the archive in case of an error.
                return;
            }
        }

        System.exit(0);
    }

    private void archiveDatabase() {
        if (!loadedDatabaseSuccessfully.get()) {
            return;
        }

        database.setLastArchived(Calendar.getInstance().getTime());

        final String archiveLocation = preferences.get(Constants.ARCHIVE_FILE_LOCATION, "");
        if (archiveLocation.length() == 0) {
            JOptionPane.showMessageDialog(UI.this, "Archive location has not been configured.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            final File archiveLocationDirectory = new File(archiveLocation);
            if (!archiveLocationDirectory.exists() || !archiveLocationDirectory.isDirectory()) {
                JOptionPane.showMessageDialog(UI.this, "Archive location is invalid.",
                        "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                for (int i = 0; i < getJMenuBar().getMenuCount(); i++) {
                    getJMenuBar().getMenu(i).setEnabled(false);
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                SwingUtilities.invokeLater(new Runnable() {
                                               public void run() {
                                                   try {
                                                       WriteArchiveDatabase(archiveLocation);
                                                       WriteArchiveSpreadSheet(archiveLocation);
                                                   } catch (Exception e) {
                                                       e.printStackTrace();
                                                   } finally {
                                                       for (int i = 0; i < getJMenuBar().getMenuCount(); i++) {
                                                           getJMenuBar().getMenu(i).setEnabled(true);
                                                       }
                                                       setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                                                   }
                                               }
                                           }
                );
            }
        }
    }

    private void WriteArchiveSpreadSheet(String archiveLocation) {
        try {
            Workbook wb = new HSSFWorkbook();
            Sheet sheet1 = wb.createSheet("Sheet 1");

            Row headings = sheet1.createRow(0);
            Cell cell = headings.createCell(0);
            cell.setCellValue("Type");
            cell = headings.createCell(1);
            cell.setCellValue("Address");
            cell = headings.createCell(2);
            cell.setCellValue("Suburb");
            cell = headings.createCell(3);
            cell.setCellValue("Postcode");
            cell = headings.createCell(4);
            cell.setCellValue("Phone");
            cell = headings.createCell(5);
            cell.setCellValue("Mobile");
            cell = headings.createCell(6);
            cell.setCellValue("Email");
            cell = headings.createCell(7);
            cell.setCellValue("First");
            cell = headings.createCell(8);
            cell.setCellValue("Last");
            cell = headings.createCell(9);
            cell.setCellValue("CRN");
            cell = headings.createCell(10);
            cell.setCellValue("Primary");
            cell = headings.createCell(11);
            cell.setCellValue("Dog");
            cell = headings.createCell(12);
            cell.setCellValue("Year");
            cell = headings.createCell(13);
            cell.setCellValue("DOB");
            cell = headings.createCell(14);
            cell.setCellValue("Breed");
            cell = headings.createCell(15);
            cell.setCellValue("Cross");
            cell = headings.createCell(16);
            cell.setCellValue("Gender");
            cell = headings.createCell(17);
            cell.setCellValue("Sterile");
            cell = headings.createCell(18);
            cell.setCellValue("Obedience");
            cell = headings.createCell(19);
            cell.setCellValue("Agility");
            cell = headings.createCell(20);
            cell.setCellValue("DWD");
            cell = headings.createCell(21);
            cell.setCellValue("Class");

            int rowNumber = 1;
            for (Membership membership : database.getMemberships()) {
                for (Handler handler : database.getHandlers()) {
                    if (handler.getMembershipId() == membership.getMembershipId()) {
                        for (Dog dog : database.getDogs()) {
                            if (dog.getMembershipYear() >= UiUtils.defaultYear()) {
                                if (dog.getMembershipId() == membership.getMembershipId()) {
                                    writeSpreadSheetRow(sheet1, rowNumber++, membership, handler, dog);
                                }
                            }
                        }
                    }
                }
            }

            File spreadSheetFile = new File(archiveLocation, Constants.MEMBERSHIP_SPREADSHEET_FILE_NAME);
            FileOutputStream fileOut = new FileOutputStream(spreadSheetFile);
            wb.write(fileOut);
            fileOut.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(UI.this, "Could not archive membership spread sheet to " + archiveLocation + ".",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void writeSpreadSheetRow(Sheet sheet, int rowNumber, Membership membership, Handler handler, Dog dog) {
        String membershipType = "";
        for (MembershipType type : database.getMembershipTypes()) {
            if (type.getMembershipTypeId() == membership.getMembershipTypeId()) {
                membershipType = type.getMembershipType();
            }
        }
        String suburbName = "";
        String postcode = "";
        for (Suburb suburb : database.getSuburbs()) {
            if (suburb.getSuburbId() == membership.getSuburbId()) {
                suburbName = suburb.getSuburb();
                postcode = suburb.getPostcode();
            }
        }

        String dogName = "";
        String year = "";
        String dob = "";
        String breed = "";
        String cross = "";
        String gender = "";
        String sterile = "";
        String obedience = "";
        String agility = "";
        String dwd = "";
        String clazz = "";

        if (dog != null) {
            dogName = dog.getName();
            year = String.valueOf(dog.getMembershipYear());
            dob = dog.getDateOfBirth();
            for (Breed breed1 : database.getBreeds()) {
                if (dog.getBreedId() == breed1.getBreedId()) {
                    breed = breed1.getBreed();
                }
            }
            cross = dog.isCrossBreed() ? "Y" : "N";
            gender = dog.isMale() ? "M" : "F";
            sterile = dog.isSterilized() ? "Y" : "N";
            obedience = dog.isDoesObedience() ? "Y" : "N";
            agility = dog.isDoesAgility() ? "Y" : "N";
            dwd = dog.isDoesDwd() ? "Y" : "N";
            if (dog.isDoesObedience()) {
                for (ObedienceClass aClass : database.getObedienceClasses()) {
                    if (aClass.getObedienceClassId() == dog.getObedienceClassId()) {
                        clazz = aClass.getObedienceClass();
                    }
                }
            }
        }

        Row row = sheet.createRow(rowNumber);

        Cell cell = row.createCell(0);
        cell.setCellValue(purify(membershipType));

        cell = row.createCell(1);
        cell.setCellValue(purify(membership.getAddress()));

        cell = row.createCell(2);
        cell.setCellValue(purify(suburbName));

        cell = row.createCell(3);
        cell.setCellValue(purify(postcode));

        cell = row.createCell(4);
        cell.setCellValue(membership.getPhone());

        cell = row.createCell(5);
        cell.setCellValue(membership.getMobile());

        cell = row.createCell(6);
        cell.setCellValue(membership.getEmail());

        cell = row.createCell(7);
        cell.setCellValue(handler.getFirstName());

        cell = row.createCell(8);
        cell.setCellValue(handler.getLastName());

        cell = row.createCell(9);
        cell.setCellValue(handler.getCrn());

        cell = row.createCell(10);
        cell.setCellValue(handler.isPrimary() ? "Y" : "N");

        cell = row.createCell(11);
        cell.setCellValue(dogName);

        cell = row.createCell(12);
        cell.setCellValue(year);

        cell = row.createCell(13);
        cell.setCellValue(dob);

        cell = row.createCell(14);
        cell.setCellValue(breed);

        cell = row.createCell(15);
        cell.setCellValue(cross);

        cell = row.createCell(16);
        cell.setCellValue(gender);

        cell = row.createCell(17);
        cell.setCellValue(sterile);

        cell = row.createCell(18);
        cell.setCellValue(obedience);

        cell = row.createCell(19);
        cell.setCellValue(agility);

        cell = row.createCell(20);
        cell.setCellValue(dwd);

        cell = row.createCell(21);
        cell.setCellValue(clazz);
    }

    private void WriteArchiveDatabase(String archiveLocation) {
        PrintWriter pw;
        try {
            pw = new PrintWriter(new FileWriter(new File(archiveLocation, Constants.DATABASE_FILE_NAME)));
            try {
                XStream xstream = new XStream(new DomDriver());
                String xml = xstream.toXML(database);
                pw.println(xml);
                pw.flush();
            } finally {
                pw.close();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(UI.this, "Could not archive database to " + archiveLocation + ".",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveDatabase() {
        if (!loadedDatabaseSuccessfully.get()) {
            return;
        }

        Thread t = new Thread() {
            @Override
            public void run() {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    XStream xstream = new XStream(new DomDriver());
                    String xml = xstream.toXML(database);
                    PrintWriter pw;
                    String fileLocation = preferences.get(Constants.DATABASE_FILE_LOCATION, Constants.DATABASE_FILE_LOCATION_DEFAULT);
                    try {
                        pw = new PrintWriter(new FileWriter(new File(fileLocation, Constants.DATABASE_FILE_NAME)));
                        try {
                            pw.println(xml);
                            pw.flush();
                        } finally {
                            pw.close();
                        }
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(UI.this, "Could not save database at " + fileLocation + ".",
                                "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } finally {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public Database getDatabase() {
        return database;
    }

    public void editMembership(int membershipId, Tabs tab) {
        for (Membership membership : database.getMemberships()) {
            if (membership.getMembershipId() == membershipId) {
                Set<Handler> handlers = new HashSet<>();
                for (Handler handler : database.getHandlers()) {
                    if (handler.getMembershipId() == membershipId) {
                        handlers.add(handler);
                    }
                }
                Set<Dog> dogs = new HashSet<>();
                for (Dog dog : database.getDogs()) {
                    if (dog.getMembershipId() == membershipId) {
                        dogs.add(dog);
                    }
                }
                Set<Payment> payments = new HashSet<>();
                for (Payment payment : database.getPayments()) {
                    if (payment.getMembershipId() == membershipId) {
                        payments.add(payment);
                    }
                }
                MembershipBundle bundle = new MembershipBundle(membership, handlers, dogs, payments);
                MembershipDialog d = new MembershipDialog(this, false, bundle, tab);
                d.setVisible(true);
                break;
            }
        }
    }

    public void setLatestMembership(int membershipId) {
        latestMembership = membershipId;
        latestMenuItem.setEnabled(true);
    }

    private class SponsorshipReportLine {

        private final String lastName;
        private final String firstName;
        private final String phone;
        private final String mobile;
        private final String address;
        private final String suburb;
        private final String postcode;
        private final String date;

        public SponsorshipReportLine(String lastName, String firstName, String phone, String mobile, String address, String suburb, String postcode, String date) {
            this.lastName = lastName;
            this.firstName = firstName;
            this.phone = phone;
            this.mobile = mobile;
            this.address = address;
            this.suburb = suburb;
            this.postcode = postcode;
            this.date = date;
        }
    }

}
