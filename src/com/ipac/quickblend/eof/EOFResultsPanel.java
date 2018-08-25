package com.ipac.quickblend.eof;

import com.ipac.quickblend.Main;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class EOFResultsPanel extends JPanel
{
    public EOFResultsPanel(double ratio, double vii)
    {
        setLayout(new MigLayout());
        add(new JLabel("Percent Heavy Base Oil: " + Main.round(ratio, 2)), "center, wrap");
        add(new JLabel("Percent Light Base Oil: " + Main.round(100-ratio, 2)), "center, wrap");
        add(new JLabel("VI Improver: " + Main.round(vii, 2)), "center");
    }
}