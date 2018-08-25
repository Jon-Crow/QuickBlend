package com.ipac.quickblend;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 * A simple panel for displaying info about QuickBlend
 * @author Jonathan Crow
 */
public class InfoPanel extends JPanel implements ActionListener
{
    private JButton siteBtn, vidBtn;
    
    public InfoPanel()
    {
        setLayout(new MigLayout("fill"));
        add(new JLabel("QuickBlend™ was created by IPAC inc. and is a registered trademark."), "center, wrap");
        add(new JLabel("To find out more about IPAC, please visit our website:"), "center, wrap");
        add((siteBtn = new JButton("Visit Our Website")), "center, wrap");
        siteBtn.addActionListener(this);
        add(new JLabel("Need help using QuickBlend™?"), "center, wrap");
        add((vidBtn = new JButton("Watch the Tutorial")), "center, wrap");
        vidBtn.addActionListener(this);
        JLabel credit = new JLabel("QuickBlend™ was created by Jonathan Crow");
        credit.setFont(new Font("Aerial",Font.ITALIC,9));
        add(credit,"center");
    }
    private void openURL(String url)
    {
        try
        {
            Desktop.getDesktop().browse(new URI(url));
        }
        catch(Exception error)
        {
            Main.logError("Could not open " + url, error);
            JOptionPane.showMessageDialog(InfoPanel.this,"An error occured and the program was unable to open your default browser.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    @Override
    public void actionPerformed(ActionEvent event) 
    {
        if(event.getSource() == siteBtn) openURL("http://www.ipac-inc.com");
        else if(event.getSource() == vidBtn) openURL("https://www.youtube.com/watch?v=E8pXrjagm8I");
    }
}