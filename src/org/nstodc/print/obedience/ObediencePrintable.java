package org.nstodc.print.obedience;

import org.nstodc.ui.UiUtils;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Print object for obedience printing.
 */
public class ObediencePrintable implements Printable {
    
    private final ObedienceBean obedienceBean;
    private final Map<Integer, Block> blocks = new TreeMap<Integer, Block>();
    private final Font plain = new Font("Serif", Font.PLAIN, 10);
    private final Font bold = new Font("Serif", Font.BOLD, 10);
    public ObediencePrintable(ObedienceBean obedienceBean) {
        this.obedienceBean = obedienceBean;
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

        FontMetrics metrics = graphics.getFontMetrics(plain);
        int lineHeight = metrics.getHeight();
        int lineAscent = metrics.getAscent();
        int lineDescent = metrics.getDescent();
        int linesPerPage = (int) (pageFormat.getImageableHeight() / lineHeight);
        int pageWidth = (int) pageFormat.getImageableWidth();
        if (blocks.size() == 0) {
            initializeBlocks(linesPerPage - 3); // Keep 3 spare for title and blank line and subtitle.
        }

        if (pageIndex >= blocks.size()) {
            return NO_SUCH_PAGE;
        }

        // User (0,0) is typically outside the imageable area, so we must
        // translate by the X and Y values in the PageFormat to avoid clipping
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        printBlock(pageIndex, lineHeight, graphics, pageWidth, metrics, lineAscent, lineDescent);

        // Tell the caller that this page is part of the printed document.
        return PAGE_EXISTS;
    }

    private void printBlock(int blockIndex, int lineHeight, Graphics graphics, int pageWidth, FontMetrics metrics, int lineAscent, int lineDescent) {
        graphics.setFont(bold);
        Block block = blocks.get(blockIndex);
        int y = 0;

        // Title line
        y += lineHeight;
        graphics.drawString(" Class: " + block.getClassName(), 0, y);
        graphics.drawString("Date: " + UiUtils.nextSunday(), pageWidth / 4, y);
        graphics.drawString(" Trainer:", pageWidth / 2, y);
        graphics.drawString("Promoted:", 3 * pageWidth / 4, y);

        // Blank line
        y += lineHeight;

        // Subtitle line
        y += lineHeight;
        graphics.drawString("Member#", pageWidth / 11, y);
        graphics.drawString("Owner", 2 * pageWidth / 11, y);
        graphics.drawString("Dog", 4 * pageWidth / 11, y);
        graphics.drawString("Promoted", 5 * pageWidth / 11, y);
        graphics.drawString("Member#", 6 * pageWidth / 11 , y);
        graphics.drawString("Owner", 7 * pageWidth / 11, y);
        graphics.drawString("Dog", 9 * pageWidth / 11, y);
        graphics.drawString("Promoted", 10 * pageWidth / 11, y);

        graphics.setFont(plain);

        boolean odd = true;
        for (PrintLine printLine : block.getPrintLines().values()) {
            y += lineHeight;
            if (odd) {
                graphics.setColor(Color.lightGray);
                graphics.fillRect(pageWidth / 11, y + lineDescent, 4 * pageWidth / 11, -lineAscent);
                graphics.setColor(Color.black);
            }
            graphics.drawString(trim(String.valueOf(printLine.getA().getMembershipNumber()), metrics, pageWidth / 11), pageWidth / 11, y);
            graphics.drawString(trim(printLine.getA().getFirstName() + " " + printLine.getA().getLastName(), metrics, 2 * pageWidth / 11), 2 * pageWidth / 11, y);
            graphics.drawString(trim(printLine.getA().getDogsName(), metrics, pageWidth / 11), 4 * pageWidth / 11, y);
            graphics.drawRect(5 * pageWidth / 11, y + lineDescent, lineAscent, -lineAscent);
            if (printLine.getB() != null) {
                if (odd) {
                    graphics.setColor(Color.lightGray);
                    graphics.fillRect(6 * pageWidth / 11, y + lineDescent, 4 * pageWidth / 11, -lineAscent);
                    graphics.setColor(Color.black);
                }
                graphics.drawString(trim(String.valueOf(printLine.getB().getMembershipNumber()), metrics, pageWidth / 11), 6 * pageWidth / 11,  y);
                graphics.drawString(trim(printLine.getB().getFirstName() + " " + printLine.getB().getLastName(), metrics, 2 * pageWidth / 11), 7 * pageWidth / 11, y);
                graphics.drawString(trim(printLine.getB().getDogsName(), metrics, pageWidth / 11), 9 * pageWidth / 11, y);
                graphics.drawRect(10 * pageWidth / 11, y + lineDescent, lineAscent, -lineAscent);
            }
            odd = !odd;
        }
    }

    private String trim(String text, FontMetrics metrics, int size) {
        while (metrics.charsWidth(text.toCharArray(), 0, text.length()) > size) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private void initializeBlocks(int linesPerPage) {
        int blockNumber = 0;
        for (ObedienceClassBean obedienceClassBean : obedienceBean.getClasses()) {
            int entryOffset = 0;
            boolean classDone = false;
            do {
                Block block = new Block(obedienceClassBean.getClassName());
                blocks.put(blockNumber, block);
                int entriesRemaining = obedienceClassBean.getEntries().size() - entryOffset;
                if (entriesRemaining > 2 * linesPerPage) {
                    // Full block
                    for (int i = 0; i < linesPerPage; i++) {
                        ObedienceClassEntryBean entryBeanA = obedienceClassBean.getEntries().get(entryOffset + i);
                        ObedienceClassEntryBean entryBeanB = obedienceClassBean.getEntries().get(entryOffset + linesPerPage + i);
                        PrintLine printLine = new PrintLine(entryBeanA, entryBeanB);
                        block.getPrintLines().put(i, printLine);
                    }
                    entryOffset += 2 * linesPerPage;
                } else {
                    // Last block
                    int numberOfLines = (1 + obedienceClassBean.getEntries().size() - entryOffset) / 2;
                    for (int i = 0; i < numberOfLines; i++) {
                        ObedienceClassEntryBean entryBeanA = obedienceClassBean.getEntries().get(entryOffset + i);
                        ObedienceClassEntryBean entryBeanB = obedienceClassBean.getEntries().get(entryOffset + numberOfLines + i);
                        PrintLine printLine = new PrintLine(entryBeanA, entryBeanB);
                        block.getPrintLines().put(i, printLine);
                    }
                    classDone = true;
                }
                blockNumber++;
            } while (!classDone);
        }
    }

    private class Block {

        private final Map<Integer, PrintLine> printLines = new TreeMap<Integer, PrintLine>();
        private final String className;
        public Map<Integer, PrintLine> getPrintLines() {
            return printLines;
        }

        public Block(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    private class PrintLine {

        private final ObedienceClassEntryBean a;
        private final ObedienceClassEntryBean b;

        public PrintLine(ObedienceClassEntryBean a, ObedienceClassEntryBean b) {
            this.a = a;
            this.b = b;
        }

        public ObedienceClassEntryBean getA() {
            return a;
        }

        public ObedienceClassEntryBean getB() {
            return b;
        }
    }
}
