package com.ipac.quickblend.ssi;

import com.ipac.quickblend.Main;
import com.ipac.quickblend.Saveable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Percent Shear Loss calculator
 * @author Jonathan Crow
 */
public class PSLPanel extends JPanel implements Saveable
{
    private JTextField befField, aftField, oilField, ssiField;
    private JCheckBox ssiBox;
    private JLabel pslLbl;
    
    public PSLPanel()
    {
        setLayout(new MigLayout());
        add(ssiBox = new JCheckBox("SSI is known"), "span 2, center, wrap");
        ssiBox.setSelected(true);
        ssiBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                updateButtons();
                updatePSL();
            }
        });
        add(new JLabel("SSI:"),"left");
        add(ssiField = new JTextField(15), "right, wrap");
        add(new JLabel("KV of base oil blend:"), "left");
        add(oilField = new JTextField(15), "right, wrap");
        add(new JLabel("KV after"), "left");
        add(aftField = new JTextField(15), "right, wrap");
        add(new JLabel("KV before"), "left");
        add(befField = new JTextField(15), "right, wrap");
        add(new JLabel("Percent Shear Loss:"), "left");
        add(pslLbl = new JLabel("Invalid Input"));
        updateButtons();
        KeyAdapter keys = new KeyAdapter()
        {
            public void keyReleased(KeyEvent event)
            {
                updatePSL();
            }
        };
        ssiField.addKeyListener(keys);
        oilField.addKeyListener(keys);
        befField.addKeyListener(keys);
        aftField.addKeyListener(keys);
    }
    /**
     * Enables/disables the correct buttons
     */
    private void updateButtons()
    {
        ssiField.setEnabled(ssiBox.isSelected());
        oilField.setEnabled(ssiBox.isSelected());
        aftField.setEnabled(!ssiBox.isSelected());
    }
    /**
     * calculates the psl and updates the label
     */
    private void updatePSL()
    {
        try
        {
            double bef = Double.parseDouble(befField.getText());
            if(ssiBox.isSelected())
            {
                double ssi = Double.parseDouble(ssiField.getText()), oil = Double.parseDouble(oilField.getText());
                pslLbl.setText(Main.roundToString((ssi/100)*(oil/bef), 3, true));
            }
            else
            {
                double aft = Double.parseDouble(aftField.getText());
                pslLbl.setText(Main.roundToString(((bef-aft)/bef)*100, 3, true));
            }
        }
        catch(Exception error)
        {
            pslLbl.setText("Invalid Input");
        }
    }
    @Override
    public String toString()
    {
        return "Percent shear loss";
    }
    @Override
    public void putData(HashMap<String, String> data) 
    {
        data.put("pslBef", befField.getText());
        data.put("pslAft", aftField.getText());
        data.put("pslSsi", ssiField.getText());
        data.put("pslOil", oilField.getText());
        if(ssiBox.isSelected()) data.put("ssiKnown", "");
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        if(data.containsKey("pslBef")) befField.setText(data.get("pslBef"));
        if(data.containsKey("pslAft")) aftField.setText(data.get("pslAft"));
        if(data.containsKey("pslSsi")) ssiField.setText(data.get("pslSsi"));
        if(data.containsKey("pslOil")) oilField.setText(data.get("pslOil"));
        ssiBox.setSelected(data.containsKey("ssiKnown"));
        updateButtons();
        updatePSL();
    }
}