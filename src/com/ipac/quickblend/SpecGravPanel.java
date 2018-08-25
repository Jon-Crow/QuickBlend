package com.ipac.quickblend;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Specific Gravity calculator
 * @author Jonathan Crow
 */
public class SpecGravPanel extends JPanel implements Saveable
{
    private JTextField specGravField, apiField;
    
    public SpecGravPanel()
    {
        setLayout(new MigLayout());
        KeyAdapter keys = new KeyAdapter()
        {
            public void keyReleased(KeyEvent event)
            {
                if(event.getSource() == specGravField) updateAPI();
                else if(event.getSource() == apiField) updateSpecGrav();
            }
        };
        add(new JLabel("Specific Gravity:"),"left");
        add((specGravField = new JTextField(15)),"span 2, right, wrap");
        specGravField.addKeyListener(keys);
        add(new JLabel("API Gravity:"),"left");
        add((apiField = new JTextField(15)),"span 2, right");
        apiField.addKeyListener(keys);
    }
    /**
     * calculates specgrav and updates the field
     */
    private void updateSpecGrav()
    {
        try
        {
            double api = Double.parseDouble(apiField.getText());
            specGravField.setText(Main.roundToString(141.5/(api+131.5), 3));
        }
        catch(Exception error)
        {
            specGravField.setText("Invalid Input");
        }
    }
    /**
     * calculates the api and updates the field
     */
    private void updateAPI()
    {
        try
        {
            double specGrav = Double.parseDouble(specGravField.getText());
            apiField.setText(Main.roundToString(141.5/specGrav-131.5, 3));
        }
        catch(Exception error)
        {
            apiField.setText("Invalid Input");
        }
    }
    @Override
    public void putData(HashMap<String, String> data) 
    {
        data.put("specGrav", specGravField.getText());
        data.put("apiGrav", apiField.getText());
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        if(data.containsKey("specGrav")) specGravField.setText(data.get("specGrav"));
        if(data.containsKey("apiGrav")) apiField.setText(data.get("apiGrav"));
    }
}