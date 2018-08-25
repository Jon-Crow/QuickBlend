package com.ipac.quickblend;

import static com.ipac.quickblend.Main.logError;
import com.ipac.quickblend.calculator.DatabaseEditor2;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Used to save the state of the workspace to be loaded later
 * @author Jonathan Crow
 */
public class WorkSpaceSaver 
{
    public static final File WORKSPACE_FILE = new File(Main.RUNTIME_DIR + "/workspace.xml");
    public static final int QUICKBLEND = 1;
    
    /**
     * Save the JFrame's state in an xml file
     * @param win the jframe to save
     */
    public static void save(JFrame win)
    {
        if(win.getContentPane() instanceof JTabbedPane)
        {
            WorkSpaceData data = new WorkSpaceData((JTabbedPane)win.getContentPane());
            try
            {
                JAXBContext jaxb = JAXBContext.newInstance(WorkSpaceData.class, DesktopData.class, FrameData.class, ContainerData.class, String.class);
                Marshaller mar = jaxb.createMarshaller();
                mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                mar.marshal(data, WORKSPACE_FILE);
            }
            catch(Exception error)
            {
                Main.logError("Could not save workspace", error);
            }
        }
    }
    /**
     * Loads the xml file as tabs and puts them into the tabbedpane
     * @param tabs the tabbedpane
     */
    public static boolean load(JTabbedPane tabs)
    {
        if(!WORKSPACE_FILE.exists()) return false;
        WorkSpaceData wsData = null;
        try
        {
            JAXBContext jaxb = JAXBContext.newInstance(WorkSpaceData.class, DesktopData.class, FrameData.class, ContainerData.class, String.class);
            wsData = (WorkSpaceData)jaxb.createUnmarshaller().unmarshal(WORKSPACE_FILE);
        }
        catch(Exception error)
        {
            Main.logError("Could not load workspace.", error);
            return false;
        }
        if(wsData != null)
        {
            for(DesktopData dData : wsData.DESKTOPS)
            {
                tabs.addTab(dData.NAME, new CalcDesktop());
                tabs.setSelectedIndex(tabs.getTabCount()-1);
                for(FrameData fData : dData.FRAMES)
                {
                    try
                    {
                        Container con = (Container)Class.forName(fData.CONTAINER.DATA.get("type")).newInstance();
                        JInternalFrame frame = Main.addPanel(con, fData.TITLE);
                        frame.setBounds(fData.X, fData.Y, fData.WIDTH, fData.HEIGHT);
                        if(con instanceof Saveable) ((Saveable)con).loadData(fData.CONTAINER.DATA);
                    }
                    catch(Exception error)
                    {
                        Main.logError("Could not add calculator during workspace load.", error);
                        return false;
                    }
                }
            }
        }
        return true;
    }
    /**
     * A container for tabbedpane data
     */
    @XmlRootElement
    private static class WorkSpaceData
    {
        @XmlElement
        private final DesktopData[] DESKTOPS;
        
        private WorkSpaceData()
        {
            DESKTOPS = new DesktopData[0];
        }
        private WorkSpaceData(JTabbedPane tabs)
        {
            ArrayList<String> names = new ArrayList<>();
            ArrayList<CalcDesktop> cds = new ArrayList<>();
            Component[] comps = tabs.getComponents();
            for(int i = 0; i < comps.length; i++)
            {
                if(comps[i] instanceof CalcDesktop)
                {
                    cds.add((CalcDesktop)comps[i]);
                    names.add(tabs.getTitleAt(i));
                }
            }
            DESKTOPS = new DesktopData[cds.size()];
            for(int i = 0; i < DESKTOPS.length; i++)
                DESKTOPS[i] = new DesktopData(names.get(i), cds.get(i));
        }
    }
    /**
     * A container for CalcDesktop data
     */
    @XmlRootElement
    private static class DesktopData
    {
        @XmlAttribute
        private final String NAME;
        @XmlElement
        private final FrameData[] FRAMES;
        
        private DesktopData()
        {
            FRAMES = new FrameData[0];
            NAME = "";
        }
        private DesktopData(String name, CalcDesktop cd)
        {
            NAME = name;
            JInternalFrame[] frames = cd.getAllFrames();
            FRAMES = new FrameData[frames.length];
            for(int i = 0; i < frames.length; i++)
                FRAMES[i] = new FrameData(frames[i]);
        }
    }
    /**
     * A container for frame data
     */
    @XmlRootElement
    private static class FrameData
    {
        @XmlAttribute
        private final String TITLE;
        @XmlAttribute
        private final int X, Y, WIDTH, HEIGHT;
        @XmlElement
        private final ContainerData CONTAINER;
        
        private FrameData()
        {
            TITLE = "";
            X = (Y = (WIDTH = (HEIGHT = 0)));
            CONTAINER = null;
        }
        private FrameData(JInternalFrame frame)
        {
            TITLE = frame.getTitle();
            X = frame.getX();
            Y = frame.getY();
            WIDTH = frame.getWidth();
            HEIGHT = frame.getHeight();
            CONTAINER = new ContainerData(frame.getContentPane());
        }
    }
    /**
     * A container for frame contentPane data
     */
    @XmlRootElement
    private static class ContainerData
    {
        @XmlElement
        private final HashMap<String, String> DATA = new HashMap<>();
        
        private ContainerData()
        {}
        private ContainerData(Container con)
        {
            DATA.put("type", con.getClass().getName());
            try
            {
                if(con instanceof Saveable) ((Saveable)con).putData(DATA);
            }
            catch(Exception err)
            {
                logError("Could not save workspace data for " + con, err);
            }
        }
    }
    /**
     * A window listener that prompts the user to save before closing
     */
    public static class WindowListener extends WindowAdapter
    {
        @Override
        public void windowClosing(WindowEvent event)
        {
            Main.logWarning("Window closing. Was this done by the user?");
            
            /*
            int choice = JOptionPane.showOptionDialog(event.getWindow(),"Would you like to save your workspace and continue your work next time the application is opened?", "Save?", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Yes", "No", "Cancel"}, 2);
            if(choice == 0 && event.getWindow() instanceof JFrame) save((JFrame)event.getWindow());
            Main.logInfo("Closing choice index " + choice);
            if(choice != 2 && choice != -1) event.getWindow().dispose();
            */
            save((JFrame)event.getWindow());
            JTabbedPane tabs = (JTabbedPane)((JFrame)event.getWindow()).getContentPane();
            for(Component comp : tabs.getComponents())
            {
                if(comp instanceof CalcDesktop)
                {
                    CalcDesktop cd = (CalcDesktop)comp;
                    for(JInternalFrame frame : cd.getAllFrames())
                    {
                        if(frame.getContentPane() instanceof DatabaseEditor2) frame.dispose();
                    }
                }
            }
            event.getWindow().dispose();
        }
    }
}