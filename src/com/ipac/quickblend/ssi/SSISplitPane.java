package com.ipac.quickblend.ssi;

import com.ipac.quickblend.Main;
import com.ipac.quickblend.Saveable;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * an implementation of JSplitPane that has calculator names on the left and
 * the selected calculator on the right
 * @author Jonathan Crow
 */
public class SSISplitPane extends JSplitPane implements Saveable
{
    private JPanel[] panels = {new SSIPanel(), new ASVPanel(), new PSLPanel()};
    private JList list;
    
    public SSISplitPane()
    {
        setLeftComponent(list = new JList(panels));
        setRightComponent(panels[0]);
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent event)
            {
                setRightComponent(panels[list.getSelectedIndex()]);
                Component right = getRightComponent();
                Dimension prefSize = right.getPreferredSize();
                if(right.getWidth() < prefSize.width || right.getHeight() < prefSize.height)
                {
                    JInternalFrame frame = Main.getEnclosingFrame(SSISplitPane.this);
                    if(frame != null) frame.pack();
                }
            }
        });
    }
    @Override
    public void putData(HashMap<String, String> data) 
    {
        Main.logInfo("putData was just called on SSISplitPane");
        data.put("selected", list.getSelectedIndex()+"");
        for(JPanel panel : panels)
            ((Saveable)panel).putData(data);
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        if(data.containsKey("selected"))
        {
            try
            {
                list.setSelectedIndex(Integer.parseInt(data.get("selected")));
            }
            catch(Exception error)
            {}
        }
        for(JPanel panel : panels)
            ((Saveable)panel).loadData(data);
    }
}