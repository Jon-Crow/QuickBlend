package com.ipac.quickblend.calculator;

import com.ipac.quickblend.Main;
import com.ipac.quickblend.Saveable;
import com.ipac.quickblend.Updateable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;

public class BlendsList extends JPanel implements ActionListener, Updateable, Saveable
{
    private QuickBlendPanel qbPanel;
    private JList list;
    private JButton delBtn, openBtn;
    private String[] names;
    
    public BlendsList()
    {}
    public BlendsList(QuickBlendPanel qb)
    {
        setLayout(new MigLayout("fillx"));
        qbPanel = qb;
        list = new JList();
        updateBlendNames();
        list.addMouseListener(new MouseAdapter()
        {
            public void mouseReleased(MouseEvent event)
            {
                if(event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 2 && list.getSelectedValue() != null) openBlend();
            }
        });
        add(new JLabel("Blends:"),"span 2, center, wrap");
        add(new JScrollPane(list),"span 2, center, wrap");
        add((delBtn = new JButton("Delete")),"left");
        delBtn.setIcon(Main.loadImageResource("res/img/icons/delete.png"));
        delBtn.addActionListener(this);
        add((openBtn = new JButton("Open")),"right");
        openBtn.setIcon(UIManager.getIcon("FileChooser.directoryIcon"));
        openBtn.addActionListener(this);
    }
    private String getSelectedName()
    {
        if(list.getSelectedValue() != null) return ((String)list.getSelectedValue()).trim();
        return null;
    }
    private void openBlend()
    {
        String name = getSelectedName();
        qbPanel.setBlend(QuickBlendPanel.BLENDS.getBlend(name),name);
        Main.focusFrame(Main.getEnclosingFrame(qbPanel));
    }
    public void updateBlendNames()
    {
        String[] bNames = QuickBlendPanel.BLENDS.getBlendNames();
        names = new String[bNames.length];
        for(int i = 0; i < names.length; i++)
            names[i] = "       " + bNames[i] + "       ";
        list.setListData(names);
    }
    @Override
    public void update(int delta) 
    {
        if(QuickBlendPanel.BLENDS.getNumberOfBlends() != names.length) updateBlendNames();
    }
    @Override
    public void actionPerformed(ActionEvent event)
    {
        if(list.getSelectedValue() != null)
        {
            if(event.getSource() == delBtn)
            {
                int choice = JOptionPane.showOptionDialog(this, "Are you sure you want to delete this blend?\nOnce you delete it, it will not be recoverable.", "Are you sure?", JOptionPane.YES_NO_OPTION, 
                                    JOptionPane.WARNING_MESSAGE, null, new String[]{"Yes","No"}, 1);
                if(choice == 0) 
                {
                    QuickBlendPanel.BLENDS.deleteBlend(getSelectedName());
                    QuickBlendPanel.BLENDS.save(QuickBlendPanel.BLENDS_FILE.getAbsolutePath());
                }
            }
            else if(event.getSource() == openBtn) openBlend();
        }
    }
    @Override
    public void putData(HashMap<String, String> data) 
    {}
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        JInternalFrame frame = Main.getEnclosingFrame(this);
        if(frame != null) frame.dispose();
    }
}