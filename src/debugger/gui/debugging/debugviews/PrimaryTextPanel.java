package debugger.gui.debugging.debugviews;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishenko, Vladimir Karasev
 */

public class PrimaryTextPanel extends JPanel {
    public static final int OPEN_NONE = 0;
    public static final int OPEN_LEFT = 1;
    public static final int OPEN_RIGHT = 2;
    public static final int OPEN_BOTH = 3;

    private boolean isHighlighted = false;
    private boolean red = false;
    private String text = "";
    private Font font = new Font("Arial", Font.PLAIN, 11);
    private int openMode = OPEN_NONE;
    private ArrayList annotations = null;
    private InfoWindow infoWindow = null;

    public boolean isRed() {
        return red;
    }

    public void setRed(boolean red) {
        this.red = red;
    }

    public boolean isTextVisible() {
        return textVisible;
    }

    public void setTextVisible(boolean textVisible) {
        this.textVisible = textVisible;
    }

    private boolean textVisible = true;

    public PrimaryTextPanel(String text, boolean isHighlighted, int openMode) {
        this.text = text;
        this.isHighlighted = isHighlighted;
        this.openMode = openMode;
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (infoWindow == null) {
                    infoWindow = new InfoWindow(PrimaryTextPanel.this, null);
                    int x = PrimaryTextPanel.this.getLocationOnScreen().x;
                    int y = PrimaryTextPanel.this.getLocationOnScreen().y;
                    Dimension size = PrimaryTextPanel.this.getSize();
                    if (x + infoWindow.getSize().width < Toolkit.getDefaultToolkit().getScreenSize().width)
                        infoWindow.setLocation(x, y + (int) size.getHeight());
                    else
                        infoWindow.setLocation(x + size.width - infoWindow.getSize().width, y + (int) size.getHeight());
                    infoWindow.setVisible(true);
                } else if (!infoWindow.isVisible()) {
                    int x = PrimaryTextPanel.this.getLocationOnScreen().x;
                    int y = PrimaryTextPanel.this.getLocationOnScreen().y;
                    Dimension size = PrimaryTextPanel.this.getSize();
                    if (x + infoWindow.getSize().width < Toolkit.getDefaultToolkit().getScreenSize().width)
                        infoWindow.setLocation(x, y + (int) size.getHeight());
                    else
                        infoWindow.setLocation(x + size.width - infoWindow.getSize().width, y + (int) size.getHeight());
                    infoWindow.setVisible(true);
                }
            }
        });
    }

    public ArrayList getAnnotations() {
        return annotations;
    }

    public void setAnnotations(ArrayList annotations) {
        this.annotations = annotations;
    }

    public void setOpenMode(int openMode) {
        this.openMode = openMode;
    }

    public int getOpenMode() {
        return openMode;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void paint(Graphics g) {
        int width = this.getSize().width;
        int height = this.getSize().height;
        if (!isHighlighted) {
            if (textVisible)
                g.setColor(Color.gray);
            else
                g.setColor(new Color(200, 200, 200));
            g.fillRect(0, 0, width - 1, height - 1);
            g.setColor(new Color(200, 200, 200));
            if (openMode == OPEN_NONE)
                g.fillRect(1, 1, width - 3, height - 3);
            else if (openMode == OPEN_RIGHT)
                g.fillRect(1, 1, width - 1, height - 3);
            else if (openMode == OPEN_LEFT)
                g.fillRect(0, 1, width - 2, height - 3);
            else if (openMode == OPEN_BOTH) g.fillRect(0, 1, width, height - 3);
        } else if (!red) {
            g.setColor(new Color(51, 204, 51));
            g.fillRect(0, 0, width - 1, height - 1);
//            g.setColor(new Color(220, 220, 220));
            if (openMode == OPEN_NONE)
                g.fillRect(1, 1, width - 3, height - 3);
            else if (openMode == OPEN_RIGHT)
                g.fillRect(1, 1, width - 1, height - 3);
            else if (openMode == OPEN_LEFT)
                g.fillRect(0, 1, width - 2, height - 3);
            else if (openMode == OPEN_BOTH) g.fillRect(0, 1, width, height - 3);
        }
        if (!isHighlighted && red) {
            g.setColor(new Color(204, 51, 51));
            g.fillRect(0, 0, width - 1, height - 1);
            g.setColor(new Color(220, 220, 220));
            if (openMode == OPEN_NONE)
                g.fillRect(1, 1, width - 3, height - 3);
            else if (openMode == OPEN_RIGHT)
                g.fillRect(1, 1, width - 1, height - 3);
            else if (openMode == OPEN_LEFT)
                g.fillRect(0, 1, width - 2, height - 3);
            else if (openMode == OPEN_BOTH) g.fillRect(0, 1, width, height - 3);
        }
        if (textVisible)
            g.setColor(Color.black);
        g.setFont(font);
        g.drawString(text, 3, height - 6);
    }

    public Dimension getSize() {
        FontMetrics fontMetrics = getFontMetrics(font);
        int width = fontMetrics.stringWidth(text);
        int height = fontMetrics.getAscent();
        return new Dimension(width + 6, height + 6);
    }

    public Dimension getPreferredSize() {
        return getSize();
    }

    public Dimension getMinimumSize() {
        return getSize();
    }

    public Dimension getMaximumSize() {
        return getSize();
    }
}