package org.nstodc.print.van;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Printable page for the van report.
 */
public class VanPrintable implements Printable {

    private final VanBean vanBean;
    private final Map<Integer, Block> blocks = new TreeMap<Integer, Block>();
    private final Font plain = new Font("Serif", Font.PLAIN, 10);
    private final Font bold = new Font("Serif", Font.BOLD, 10);
    public VanPrintable(VanBean vanBean) {
        this.vanBean = vanBean;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

        FontMetrics metrics = graphics.getFontMetrics(plain);
        int lineHeight = metrics.getHeight();
        int linesPerPage = (int) (pageFormat.getImageableHeight() / lineHeight);
        int pageWidth = (int) pageFormat.getImageableWidth();
        if (blocks.size() == 0) {
            initializeBlocks(linesPerPage - 2); // Keep 2 spare for title and blank line.
        }

        if (pageIndex >= blocks.size()) {
            return NO_SUCH_PAGE;
        }

        // User (0,0) is typically outside the imageable area, so we must
        // translate by the X and Y values in the PageFormat to avoid clipping
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        printBlock(pageIndex, lineHeight, graphics, pageWidth, metrics);

        // Tell the caller that this page is part of the printed document.
        return PAGE_EXISTS;
    }

    private void printBlock(int blockIndex, int lineHeight, Graphics graphics, int pageWidth, FontMetrics metrics) {
        graphics.setFont(bold);
        Block block = blocks.get(blockIndex);
        int y = 0;

        // Title line
        y += lineHeight;
        graphics.drawString(" V A N   R E P O R T", 0, y);

        // Blank line
        y += lineHeight;

        graphics.setFont(plain);

        for (VanDetailEntry printLine : block.getPrintLines().values()) {
            y += lineHeight;
            graphics.drawString(" " +
                    printLine.getLastName() + ", " +
                    printLine.getFirstName() + " " +
                    printLine.getAddress() + " " +
                    printLine.getSuburb() + " " +
                    printLine.getPostcode(), 0, y);
        }
    }

    private void initializeBlocks(int linesPerPage) {
        int blockNumber = 0;
        Block block = new Block();
        blocks.put(blockNumber, block);
        int entryId = 0;
        for (VanDetailEntry entry : vanBean.getEntries().values()) {
            block.getPrintLines().put(entryId, entry);
            if (++entryId >= linesPerPage) {
                block = new Block();
                blocks.put(++blockNumber, block);
                entryId = 0;
            }
        }
    }

    private class Block {
        private final Map<Integer, VanDetailEntry> printLines = new TreeMap<Integer, VanDetailEntry>();
        public Map<Integer, VanDetailEntry> getPrintLines() {
            return printLines;
        }
    }

}
