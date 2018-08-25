package com.ipac.quickblend;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Coefficient of Thermal Expansion calculator
 * @author Jonathan Crow
 */
public class CTEPanel extends JPanel implements Saveable
{
    private JTextField lowD, lowT, highD, highT;
    private JRadioButton lowC, lowF, lowGram, lowGal, highC, highF, highGram, highGal;
    private JLabel cteLbl;
    
    public CTEPanel()
    {
        setLayout(new MigLayout());
        String[] lines = {"This calculator computes the coefficient of thermal expansion which is essential in calculating",
        "the required size of a container to accommodate a volume of liquid over a full temperature", 
        "range to which it will be subjected.", " ",
        "The coefficient of thermal expansion is typically 0.00040/°F for petroleum",
        "Temperatures from -17.7°C to 65.5°C", "Specific Gravity of 0.8504 to 0.9659", " ",
        "It can be calculated with a density at a lower temperature (Da) and a density at a higher temperature (Db)",
        "where the temperatures are between 5-90°C (9-194°F) and are no more than 14°C (25°F) apart."};
        for(String line : lines)
            add(new JLabel(line), "span 4, center, wrap");
        addSeparator();
        
        ButtonGroup lowTemp = new ButtonGroup(), lowDens = new ButtonGroup(), highTemp = new ButtonGroup(), highDens = new ButtonGroup();
        lowTemp.add(lowC = new JRadioButton("Celsius"));
        lowTemp.add(lowF = new JRadioButton("Farenheit"));
        lowC.setSelected(true);
        lowDens.add(lowGram = new JRadioButton("g/mL"));
        lowDens.add(lowGal = new JRadioButton("lb/gal"));
        lowGram.setSelected(true);
        highTemp.add(highC = new JRadioButton("Celsius"));
        highTemp.add(highF = new JRadioButton("Farenheit"));
        highC.setSelected(true);
        highDens.add(highGram = new JRadioButton("g/mL"));
        highDens.add(highGal = new JRadioButton("lb/gal"));
        highGram.setSelected(true);
        
        add(new JLabel("Lower Temperature:"), "span 4, left, wrap");
        add(new JLabel("Density:"), "left");
        add(lowD = new JTextField(15));
        add(lowGram);
        add(lowGal, "wrap");
        add(new JLabel("Temperature:"), "left");
        add(lowT = new JTextField(15), "center");
        add(lowC);
        add(lowF, "wrap");
        addSeparator();
        
        add(new JLabel("Higher Temperature:"), "span 4, left, wrap");
        add(new JLabel("Density:"), "left");
        add(highD = new JTextField(15), "center");
        add(highGram);
        add(highGal, "wrap");
        add(new JLabel("Temperature:"), "left");
        add(highT = new JTextField(15), "center");
        add(highC);
        add(highF, "wrap");
        addSeparator();
        
        add(new JLabel("CTE:"),"left");
        add(cteLbl = new JLabel("Invalid Input"), "span 3");
        
        KeyAdapter keys = new KeyAdapter()
        {
            public void keyReleased(KeyEvent event)
            {
                updateCTE();
            }
        };
        lowT.addKeyListener(keys);
        lowD.addKeyListener(keys);
        highT.addKeyListener(keys);
        highD.addKeyListener(keys);
        
        ActionListener update = new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                updateCTE();
            }
        };
        for(Component comp : getComponents())
        {
            if(comp instanceof JRadioButton) ((JRadioButton)comp).addActionListener(update);
        }
    }
    private void addSeparator()
    {
        JSeparator sep = new JSeparator();
        sep.setPreferredSize(new Dimension(getPreferredSize().width, 25));
        add(sep, "span 4, center, wrap");
    }
    /**
     * calculates cte and updates the label
     */
    private void updateCTE()
    {
        try
        {
            double lowTemp = Double.parseDouble(lowT.getText()), lowDens = Double.parseDouble(lowD.getText()),
                    highTemp = Double.parseDouble(highT.getText()), highDens = Double.parseDouble(highD.getText());
            if(lowF.isSelected()) lowTemp = (lowTemp-32)*(5.0/9);
            if(highF.isSelected()) highTemp = (highTemp-32)*(5.0/9);
            if(lowGal.isSelected()) lowDens /= 8.345;
            if(highGal.isSelected()) highDens /= 8.345;
            if(highTemp > lowTemp)
            {
                if(highTemp-lowTemp <= 14) cteLbl.setText((lowDens-highDens)/(lowDens*(highTemp-lowTemp))+"");
                else cteLbl.setText("Your temperatures are too far apart.");
            }
            else cteLbl.setText("Your high temperature is lower than your low temperature.");
        }
        catch(Exception error)
        {
            cteLbl.setText("Invalid Input");
        }
    }
    @Override
    public void putData(HashMap<String, String> data) 
    {
        data.put("lowTemp", lowT.getText());
        data.put("lowDens", lowD.getText());
        data.put("highTemp", highT.getText());
        data.put("highDens", highD.getText());
        if(lowC.isSelected()) data.put("lowC", "");
        if(lowF.isSelected()) data.put("lowF", "");
        if(lowGram.isSelected()) data.put("lowGram", "");
        if(lowGal.isSelected()) data.put("lowGal", "");
        if(highC.isSelected()) data.put("highC", "");
        if(highF.isSelected()) data.put("highF", "");
        if(highGram.isSelected()) data.put("highGram", "");
        if(highGal.isSelected()) data.put("highGal", "");
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        if(data.containsKey("lowTemp")) lowT.setText(data.get("lowTemp"));
        if(data.containsKey("lowDens")) lowD.setText(data.get("lowDens"));
        if(data.containsKey("highTemp")) highT.setText(data.get("highTemp"));
        if(data.containsKey("highDens")) highD.setText(data.get("highDens"));
        lowC.setSelected(data.containsKey("lowC"));
        lowF.setSelected(data.containsKey("lowF"));
        lowGram.setSelected(data.containsKey("lowGram"));
        lowGal.setSelected(data.containsKey("lowGal"));
        highC.setSelected(data.containsKey("highC"));
        highF.setSelected(data.containsKey("highF"));
        highGram.setSelected(data.containsKey("highGram"));
        highGal.setSelected(data.containsKey("highGal"));
        updateCTE();
    }
}