package com.ipac.quickblend;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

public class SuperSplitPane extends JSplitPane
{
    public static final int LEFT = 1, CENTER = 2, RIGHT = 3, BOTTOM = 1, TOP = 2;
    private JSplitPane infoSplit, mainSplit, extraSplit;
    
    public SuperSplitPane()
    {
        super(JSplitPane.HORIZONTAL_SPLIT);
        setLeftComponent(infoSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT));
        JSplitPane silentSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        setRightComponent(silentSplit);
        silentSplit.setLeftComponent(mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT));
        silentSplit.setRightComponent(extraSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT));
        for(JSplitPane split : new JSplitPane[]{infoSplit, mainSplit, extraSplit})
        {
            split.setLeftComponent(new JTabbedPane());
            split.setRightComponent(new JTabbedPane());
        }
    }
    private JSplitPane getSplitPane(int hor)
    {
        if(hor == LEFT) return infoSplit;
        else if(hor == CENTER) return mainSplit;
        else if(hor == RIGHT) return extraSplit;
        return null;
    }
    private JTabbedPane getTabbedPane(JSplitPane split, int ver)
    {
        if(ver == BOTTOM && split.getRightComponent() instanceof JTabbedPane) return (JTabbedPane)split.getRightComponent();
        else if(ver == TOP && split.getLeftComponent() instanceof JTabbedPane) return (JTabbedPane)split.getLeftComponent();
        return null;
    }
    private JTabbedPane getTabbedPane(int ver, int hor)
    {
        JSplitPane split = getSplitPane(hor);
        if(split != null) return getTabbedPane(split, ver);
        return null;
    }
    public void addComponent(String name, Icon icon, Component comp, int ver, int hor)
    {
        JTabbedPane tabs = getTabbedPane(ver, hor);
        if(tabs != null) tabs.addTab(name, icon, comp);
    }
    public void removeComponent(String name, int ver, int hor)
    {
        JTabbedPane tabs = getTabbedPane(ver, hor);
        if(tabs != null) tabs.removeTabAt(tabs.indexOfTab(name));
    }
}