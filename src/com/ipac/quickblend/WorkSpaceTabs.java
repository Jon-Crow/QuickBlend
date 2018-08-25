package com.ipac.quickblend;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * an implementation of DnDTabbedPane(JTabbedPane) that supports the renaming and deleting of tabs
 * @author Jonathan Crow
 */
public class WorkSpaceTabs extends DnDTabbedPane
{
    public WorkSpaceTabs()
    {
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }
    /**
     * prompts the user to enter a new name for the current tab and renames it
     */
    public void renameCurrentTab()
    {
        String name = JOptionPane.showInputDialog(this, "Enter the new name of this tab:", "Tab Name", JOptionPane.PLAIN_MESSAGE);
        if(name != null) renameCurrentTab(name);
    }
    /**
     * simply sets the title of the current tab
     * @param name the title
     */
    public void renameCurrentTab(String name)
    {
        setTitleAt(getSelectedIndex(), name);
    }
    /**
     * prompts the user and deletes the current tab if they confirm
     */
    public void deleteCurrentTab()
    {
        if(getTabCount() > 2 && getSelectedIndex() != getTabCount()-1)
        {
            int choice2 = JOptionPane.showConfirmDialog(this, "Are you sure you wish to delete this tab?\nThe windows open in the tab will not be accesible once it is closed.", 
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if(choice2 == 0)
            {
                removeTabAt(getSelectedIndex());
                if(getSelectedIndex() == getTabCount()-1) setSelectedIndex(getTabCount()-2);
            }
        }
        else JOptionPane.showMessageDialog(this, "You cannot delete this tab because it is the only remaining tab.", "Cannot delete", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * The MouseAdapter that handles the double click and popup trigger
     */
    private final MouseAdapter mouse = new MouseAdapter()
    {
        @Override
        public void mousePressed(MouseEvent event)
        {
            Main.checkPopupTrigger(event);
        }
        @Override
        public void mouseReleased(MouseEvent event) 
        {
            Main.logInfo("Mouse released on JTabbedPane");
            if(event.getButton() == MouseEvent.BUTTON1)
            {
                if(getSelectedIndex() == getTabCount()-1 && getTitleAt(getTabCount()-1).equals("+"))
                {
                    removeTabAt(getTabCount()-1);
                    addTab("Work Space", new CalcDesktop());
                    addTab("+", new JLabel("Add New Work Space",JLabel.CENTER));
                    setSelectedIndex(getTabCount()-2);
                }
                else if(event.getClickCount() == 2)
                {
                    int choice = JOptionPane.showOptionDialog(WorkSpaceTabs.this, "What would you like to do to this tab?", "Tab Options", JOptionPane.DEFAULT_OPTION, 
                            JOptionPane.QUESTION_MESSAGE, null, new String[]{"Rename","Delete","Cancel"}, 2);
                    if(choice == 0) renameCurrentTab();
                    else if(choice == 1) deleteCurrentTab();
                }
            }
            Main.checkPopupTrigger(event);
        }
    };
}