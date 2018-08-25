package com.ipac.quickblend;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Percent Ash calculator
 * @author Jonathan Crow
 */
public class AshPanel extends JPanel implements KeyListener, Saveable
{
    private static final double[] constants = {1.5, 1.7, 3.09, 3.4, 4.95, 1.464, 3.22, 2.33, 7.92, 1.291, 1.5, 1.252};
    private JTextField[] fields = new JTextField[12];
    private JLabel ashLbl;
    
    public AshPanel()
    {
        setLayout(new MigLayout());
        String[] names = {"Zn:","Ba:","Na:","Ca:","Mg:","Pb:","B:","K:","Li:","Mn:","Mo:","Cu:"};
        for(int i = 0; i < fields.length; i++)
        {
            fields[i] = new JTextField(15);
            addRow(names[i],fields[i]);
        }
        add((ashLbl = new JLabel("Percent Ash: 0")), "span 2, center");
    }
    private void addRow(String name, JTextField field)
    {
        add(new JLabel(name));
        add(field, "wrap");
        field.addKeyListener(this);
    }
    /**
     * calculates the percent ash and updates the label
     */
    public void updateAsh()
    {
        double sum = 0;
        try
        {
            for(int i = 0; i < fields.length; i++)
            {
                if(!fields[i].getText().isEmpty()) sum += Double.parseDouble(fields[i].getText())*constants[i];
            }
            if(sum <= 100) ashLbl.setText("Percent Ash: " + Main.round(sum, 2));
            else ashLbl.setText("Percent Ash: Invalid Input");
        }
        catch(Exception error)
        {
            ashLbl.setText("Pecrent Ash: Invalid Input");
        }
    }
    @Override
    public void keyReleased(KeyEvent ke) 
    {
        updateAsh();
    }
    @Override
    public void keyTyped(KeyEvent ke) 
    {}
    @Override
    public void keyPressed(KeyEvent ke) 
    {}
    @Override
    public void putData(HashMap<String, String> data) 
    {
        for(int i = 0; i < fields.length; i++)
            data.put("field" + i, fields[i].getText());
        data.put("ash", ashLbl.getText());
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        for(int i = 0; i < fields.length; i++)
        {
            if(data.containsKey("field" + i)) fields[i].setText(data.get("field" + i));
        }
        if(data.containsKey("ash")) ashLbl.setText(data.get("ash"));
    }
}