package com.ipac.quickblend.ssi;

import com.ipac.quickblend.Main;
import com.ipac.quickblend.Saveable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Shear Stability Index calculator
 * @author Jonathan Crow
 */
public class SSIPanel extends JPanel implements Saveable
{
    private JTextField befField, aftField, oilField;
    private JLabel ssiLbl;

    public SSIPanel()
    {
        setLayout(new MigLayout());
        add(new JLabel("KV Before:"), "left");
        add(befField = new JTextField(15), "right, wrap");
        add(new JLabel("KV After:"), "left");
        add(aftField = new JTextField(15), "right, wrap");
        add(new JLabel("KV of base oil blend:"), "left");
        add(oilField = new JTextField(15), "right, wrap");
        add(new JLabel("Shear Stability Index:"), "left");
        add(ssiLbl = new JLabel("Invalid Input"), "right");
        KeyAdapter keys = new KeyAdapter()
        {
            public void keyReleased(KeyEvent event)
            {
                updateSSI();
            }
        };
        befField.addKeyListener(keys);
        aftField.addKeyListener(keys);
        oilField.addKeyListener(keys);
    }
    /**
     * Calculates the ssi and updated the label.
     */
    private void updateSSI()
    {
        try
        {
            double bef = Double.parseDouble(befField.getText()), aft = Double.parseDouble(aftField.getText()), oil = Double.parseDouble(oilField.getText());
            ssiLbl.setText(Main.roundToString(((bef-aft)/(bef-oil))*100, 3, true));
        }
        catch(Exception error)
        {
            ssiLbl.setText("Invalid Input");
        }
    }
    @Override
    public String toString()
    {
        return "SSI of polymer";
    }
    @Override
    public void putData(HashMap<String, String> data) 
    {
        data.put("ssiBef", befField.getText());
        data.put("ssiAft", aftField.getText());
        data.put("ssiOil", oilField.getText());
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        if(data.containsKey("ssiBef")) befField.setText(data.get("ssiBef"));
        if(data.containsKey("ssiAft")) aftField.setText(data.get("ssiAft"));
        if(data.containsKey("ssiOil")) oilField.setText(data.get("ssiOil"));
        updateSSI();
    }
}