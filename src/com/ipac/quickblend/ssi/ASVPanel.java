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
 * After Shear Viscosity calculator
 * @author Jonathan Crow
 */
public class ASVPanel extends JPanel implements Saveable
{
    private JTextField ssiField, befField, oilField;
    private JLabel asvLbl;
    
    public ASVPanel()
    {
        setLayout(new MigLayout());
        add(new JLabel("SSI:"),"left");
        add(ssiField = new JTextField(15),"right, wrap");
        add(new JLabel("KV before:"),"left");
        add(befField = new JTextField(15),"right, wrap");
        add(new JLabel("KV of base oil blend:"),"left");
        add(oilField = new JTextField(15),"right, wrap");
        add(new JLabel("After Shear KV:"), "left");
        add(asvLbl = new JLabel("Invalid Input"),"right");
        KeyAdapter keys = new KeyAdapter()
        {
            public void keyReleased(KeyEvent event)
            {
                updateASV();
            }
        };
        ssiField.addKeyListener(keys);
        befField.addKeyListener(keys);
        oilField.addKeyListener(keys);
    }
    /**
     * calculates the asv and updates the label
     */
    private void updateASV()
    {
        try
        {
            double ssi = Double.parseDouble(ssiField.getText()), bef = Double.parseDouble(befField.getText()), oil = Double.parseDouble(oilField.getText());
            double shearKV = bef-((bef-oil)*(ssi/100));
            asvLbl.setText(String.format("%.3f",shearKV));
        }
        catch(Exception error)
        {
            asvLbl.setText("Invalid Input");
        }
    }
    @Override
    public String toString()
    {
        return "After shear viscosity (based on SSI)";
    }
    @Override
    public void putData(HashMap<String, String> data) 
    {
        data.put("asvSsi", ssiField.getText());
        data.put("asvBef", befField.getText());
        data.put("asvOil", oilField.getText());
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        if(data.containsKey("asvSsi")) ssiField.setText(data.get("asvSsi"));
        if(data.containsKey("asvBef")) befField.setText(data.get("asvBef"));
        if(data.containsKey("asvOil")) oilField.setText(data.get("asvOil"));
        updateASV();
    }
}