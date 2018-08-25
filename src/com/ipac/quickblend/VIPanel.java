package com.ipac.quickblend;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Viscosity Index calculator
 * @author Jonathan Crow
 */
public class VIPanel extends JPanel implements Saveable
{
    /**
     * Was stored in a spreadsheet. Used arrays for ease of use.
     */
    public static final double[] A = {1.14673, 3.38095, 2.5, 0.101, 3.35714, 0.01191, 0.41858, 0.88779, 0.7672, 0.97305, 0.97256, 0.91413, 0.87031, 0.84703, 0.85921, 0.83531},
            B = {1.7576, -15.4952, -7.2143, 16.635, -23.5643, 21.475, 16.1558, 7.5527, 10.7972, 5.3135, 5.25, 7.4759, 9.7157, 12.6752, 11.1009, 14.6731},
            C = {-0.109, 33.196, 13.812, -45.469, 78.466, -72.87, -56.04, -16.6, -38.18, -2.2, -0.98, -21.82, -50.77, -133.31, -83.19, -216.246},
            D = {0.84155, 0.78571, 0.82143, 0.04985, 0.22619, 0.79762, 0.05794, 0.26665, 0.20073, 0.28889, 0.24504, 0.20323, 0.18411, 0.17029, 0.1713, 0.16841},
            E = {1.5521, 1.7929, 1.5679, 9.1613, 7.7369, -0.7321, 10.5156, 6.7015, 8.4658, 5.9741, 7.416, 9.1267, 10.1015, 11.4866, 11.368, 11.8493},
            F = {-0.077, -0.183, 0.119, -18.557, -16.656, 14.61, -28.24, -10.81, -22.49, -4.93, -16.73, -34.23, -46.75, -80.62, -76.94, -96.947},
            Y_MIN = {2.0, 3.8, 4.4, 5.0, 6.4, 7.0, 7.7, 9.0, 12.0, 15.0, 18.0, 22.0, 28.0, 40.0, 55.0, 70.0},
            Y_MAX = {3.8, 4.4, 5.0, 6.4, 7.0, 7.7, 9.0, 12.0, 15.0, 18.0, 22.0, 28.0, 40.0, 55.0, 70.0, 2000.0};
    private JTextField kv40Field, kv100Field;
    private JLabel viLabel;
    
    public VIPanel()
    {
        KeyAdapter fieldListen = new KeyAdapter()
        {
            public void keyReleased(KeyEvent event)
            {
                updateVILabel();
            }
        };
        setLayout(new MigLayout());
        add(new JLabel("KV40:"),"left");
        add((kv40Field = new JTextField(10)),"span 2, right, wrap");
        kv40Field.addKeyListener(fieldListen);
        add(new JLabel("KV100:"),"left");
        add((kv100Field = new JTextField(10)),"span 2, right, wrap");
        kv100Field.addKeyListener(fieldListen);
        add(new JLabel("Only use actual viscosity data."),"span 3, center, wrap");
        add(new JLabel("Do not use predicted values."),"span 3, center, wrap");
        add(new JLabel("Viscosity Index:"),"span 2, left");
        add((viLabel = new JLabel("Invalid Input")),"right");
    }
    /**
     * Checks the validity of the user's input
     * @return true if the input can be used in calculation
     */
    public boolean isValidInput()
    {
        return !Double.isNaN(getKV40()) && !Double.isNaN(getKV100());
    }
    /**
     * parses the kv40
     * @return the entered kv40 or NaN if unparseable
     */
    public double getKV40()
    {
        try
        {
            return Double.parseDouble(kv40Field.getText());
        }
        catch(Exception error)
        {}
        return Double.NaN;
    }
    /**
     * parses the kv100
     * @return the entered kv100 or NaN if unparseable
     */
    public double getKV100()
    {
        try
        {
            return Double.parseDouble(kv100Field.getText());
        }
        catch(Exception error)
        {}
        return Double.NaN;
    }
    /**
     * Calculates the viscosity index based on the provided input
     * @return the calculated VI
     */
    public double getVI()
    {
        double[] abcdef = new double[6];
        double kv40 = getKV40(), kv100 = getKV100();
        for (int i = 0; i < A.length; i++)
        {
            if (kv100 >= Y_MIN[i] && kv100 <= Y_MAX[i])
            {
                abcdef[0] = A[i];
                abcdef[1] = B[i];
                abcdef[2] = C[i];
                abcdef[3] = D[i];
                abcdef[4] = E[i];
                abcdef[5] = F[i];
            }
        }
        double L = abcdef[0] * Math.pow(kv100, 2.0) + abcdef[1] * kv100 + abcdef[2],
                H = abcdef[3] * Math.pow(kv100, 2.0) + abcdef[4] * kv100 + abcdef[5],
                N = (Math.log(H) - Math.log(kv40)) / Math.log(kv100);
        if (kv40 > H) return Math.round((L - kv40) / (L - H) * 100.0);
        return Math.round((Math.pow(10.0D, N) - 1.0) / 0.00715 + 100.0);
    }
    /**
     * Calls getVI() is valid input. updates the label.
     */
    public void updateVILabel()
    {
        if(isValidInput()) viLabel.setText(getVI()+"");
        else viLabel.setText("Invalid Input");
    }
    @Override
    public void putData(HashMap<String, String> data) 
    {
        data.put("kv40", kv40Field.getText());
        data.put("kv100", kv100Field.getText());
        data.put("vi", viLabel.getText());
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        if(data.containsKey("kv40")) kv40Field.setText(data.get("kv40"));
        if(data.containsKey("kv100")) kv100Field.setText(data.get("kv100"));
        if(data.containsKey("vi")) viLabel.setText(data.get("vi"));
    }
}