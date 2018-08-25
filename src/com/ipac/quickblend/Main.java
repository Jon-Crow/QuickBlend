package com.ipac.quickblend;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class Main
{
    public static final String VERSION = "5.1.0";
    public static final String RUNTIME_DIR = System.getProperty("user.home") + "/documents/QuickBlend[5.0]";
    private static final Color COMPONENT_BACKGROUND = Color.WHITE, COMPONENT_FOREGROUND = Color.BLUE;
    private static Font COMPONENT_FONT = new Font("Segoe UI",Font.PLAIN,12);
    private static final JPopupMenu popup = new JPopupMenu();
    private static JFrame win;
    private static WorkSpaceTabs tabs;
    private static int errCode = 0;
    
    public static void main(String[] args) 
    {
        File runDir = new File(RUNTIME_DIR);
        if(!runDir.exists() || !runDir.isDirectory()) runDir.mkdir();
        
        /*
        try
        {
            File outFile = new File(RUNTIME_DIR + "/out.txt"), errFile = new File(RUNTIME_DIR + "/err.txt");
            PrintStream out = new PrintStream(new FileOutputStream(outFile));
            System.setOut(out);
            PrintStream err = new PrintStream(new FileOutputStream(errFile));
            System.setErr(err);
        }
        catch(Exception error)
        {
            logError("Could not initialize print streams.", error);
        } 
        */
        
        System.err.println("--------QuickBlend Version " + VERSION + "--------");
        
        logInfo("Loading splash screen");
        SplashScreen splash = null;
        try
        {
            splash = SplashScreen.getSplashScreen();
        }
        catch(Exception error)
        {
            logError("Could not initialize splash screen", error);
        }
        try 
        {
            logInfo("Applying Nimbus look and feel");
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) 
            {
                if(info.getName().equalsIgnoreCase("nimbus")) UIManager.setLookAndFeel(info.getClassName());
            }
        } 
        catch (Exception error) 
        {
            logError("Could not apply Nimbus look and feel", error);
        }
        logInfo("Pre-startup calculator initialization");
        for(Class cls : CalcDesktop.CALC_CLASSES)
        {
            try
            {
                cls.newInstance();
            }
            catch(Exception error)
            {}
        }
        logInfo("Initializing popup menu");
        initPopupMenu();
        if(splash != null) splash.close();
        logInfo("Opening window");
        win = new JFrame("QuickBlend™ 5.1");
        win.addWindowListener(new WorkSpaceSaver.WindowListener());
        win.setContentPane((tabs = new WorkSpaceTabs()));
        win.setIconImage(loadImageResource("res/img/icons/logo_icon.png").getImage());
        win.setSize(750,500);
        win.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        win.setExtendedState(JFrame.MAXIMIZED_BOTH);
        win.setVisible(true);
        if(!loadLastWorkspace() || tabs.getTabCount() == 0 || !tabs.getTitleAt(tabs.getTabCount()-1).equals("+"))
        {
            tabs.addTab("Work Space", new CalcDesktop());
            tabs.addTab("+", new JLabel("Add New Work Space",JLabel.CENTER));
        }
        new Thread(new Runnable()
        {
            public void run()
            {
                logInfo("Repaint thread started");
                long wait, lastFrame = System.currentTimeMillis();
                int delta;
                while(win.isVisible())
                {
                    wait = System.currentTimeMillis()+10;
                    while(System.currentTimeMillis() < wait);
                    delta = (int)(System.currentTimeMillis()-lastFrame);
                    Component comp = tabs.getSelectedComponent();
                    if(comp instanceof CalcDesktop)
                    {
                        CalcDesktop calc = (CalcDesktop)comp;
                        calc.update(delta);
                        Container con;
                        for(JInternalFrame frame : calc.getAllFrames())
                        {
                            con = frame.getContentPane();
                            if(con instanceof Updateable) ((Updateable)con).update(delta);
                            else if(con instanceof JScrollPane)
                            {
                                comp = ((JScrollPane)con).getViewport().getView();
                                if(comp instanceof Updateable) ((Updateable)comp).update(delta);
                            }
                        }
                    }
                    win.repaint();
                    lastFrame = System.currentTimeMillis();
                }
                logWarning("Repaint thread ended. Was the window closed?");
            }
        }).start();
        
        try
        {
            Updater.updateDatabase();
        }
        catch(Exception err)
        {
            err.printStackTrace();
        }
        
        IPACNews.init();
    }
    /**
     * Pulls a text file from google drive that has the names and doc ids of replacement image resources
     * @deprecated a suggested implementation that has not yet been utilized
     */
    @Deprecated
    private static void fetchImages()
    {
        File idList = new File(RUNTIME_DIR + "/images.txt");
        downloadGoogleFile("0ByfHGMBUMCGeanpMT2RaWlI1c1U", idList.getAbsolutePath());
        Scanner idScan = null;
        try
        {
            idScan = new Scanner(idList);
            while(idScan.hasNext())
            {
                String id = idScan.next();
                File f = new File(idScan.next().replace("%runtime_dir%", RUNTIME_DIR)), parent = f.getParentFile();
                if(!parent.exists()) parent.mkdirs();
                downloadGoogleFile(id, f.getAbsolutePath());
                f.deleteOnExit();
            }
        }
        catch(Exception error)
        {
            logError("Could not fetch images", error);
        }
        if(idScan != null) idScan.close();
        idList.delete();
    }
    /**
     * Shows the right click menu
     * @param comp the component on which to show the menu
     * @param x the x coord relative to the component
     * @param y the y coord relative to the component
     */
    public static void showPopup(Component comp, int x, int y)
    {
        popup.show(comp, x, y);
    }
    /**
     * static context of WorkSpaceTabs.deleteCurrentTab()
     */
    public static void deleteCurrentTab()
    {
        tabs.deleteCurrentTab();
    }
    /**
     * static context of WorkSpaceTabs.renameCurrentTab()
     */
    public static void renameCurrentTab()
    {
        tabs.renameCurrentTab();
    }
    /**
     * Calls addPanel but puts the provided container in a jscrollpane
     * @param con the container to be put in the scrollpane
     * @param name the name of the created frame
     */
    public static JInternalFrame addScrollablePanel(Container con, String name)
    {
        return addPanel(new JScrollPane(con), name);
    }
    /**
     * checks if a frame collides with any others
     * @param otherFrame the frame to check
     * @return true if the bounds of the provided frame intersect the bounds of any other frame
     * @deprecated This was used before windows simply appeared in the center
     */
    @Deprecated
    private static boolean collidesWithFrame(JInternalFrame otherFrame)
    {
        for(JInternalFrame frame : ((CalcDesktop)tabs.getSelectedComponent()).getAllFrames())
        {
            if(frame != otherFrame && !frame.isIcon() && (frame.getBounds().intersects(otherFrame.getBounds()) || frame.getBounds().contains(otherFrame.getBounds()))) 
                return true;
        }
        return false;
    }
    /**
     * Applies a style to the provided component and all if its own components
     * @param comp the component to style
     * @deprecated Turned out ugly and should use UIManager instead
     */
    @Deprecated
    public static void styleComponent(Component comp)
    {
        comp.setFont(COMPONENT_FONT);
        comp.setBackground(COMPONENT_BACKGROUND);
        comp.setForeground(COMPONENT_FOREGROUND);
        if(comp instanceof Container)
        {
            for(Component c : ((Container)comp).getComponents())
                styleComponent(c);
        }
    }
    public static JInternalFrame addPanel(Container con, String name)
    {
        return addPanel(con, name, 0, 0);
    }
    /**
     * Adds the provided container to a jinternal frame with the provided title and places it in the current tab
     * @param con the container to put in the frame
     * @param name the title of the frame
     * @return the frame that was created
     */
    public static JInternalFrame addPanel(Container con, String name, int x, int y)
    {
        Component comp = tabs.getSelectedComponent();
        if(comp instanceof CalcDesktop)
        {
            JInternalFrame frame = new JInternalFrame(name);
            //styleComponent(con);
            frame.setContentPane(con);
            frame.setResizable(true);
            frame.setIconifiable(true);
            frame.setMaximizable(true);
            frame.setClosable(true);
            Container north = (Container)((BasicInternalFrameUI)frame.getUI()).getNorthPane();
            for(Component c : north.getComponents())
                if(c.getName().equalsIgnoreCase("InternalFrameTitlePane.menuButton")) north.remove(c);
            frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
            if(con instanceof Initializable) ((Initializable)con).init(frame);
            frame.pack();
            frame.setLocation(comp.getWidth()/2-frame.getWidth()/2+x, Math.max(0, comp.getHeight()/2-frame.getHeight()/2)+y);
            CalcDesktop desktop = (CalcDesktop)comp;
            desktop.add(frame);
            Main.focusFrame(frame);
            logInfo("Added calculator type " + con.getClass().getName());
            return frame;
        }
        logWarning("Attempted to add a calculator to a tab that did not contain a calc desktop.");
        return null;
    }
    /**
     * Brings the provided frame to the front and gives it focus
     * @param frame the frame to focus
     */
    public static void focusFrame(JInternalFrame frame)
    {
        frame.toFront();
        try
        {
            frame.setSelected(true);
        }
        catch(Exception error)
        {}
    }
    /**
     * calls updateUI and repaint on the tabbedpane
     */
    public static void updateUI()
    {
        tabs.updateUI();
        tabs.repaint();
    }
    /**
     * Finds the jinternal frame that contains the provided container. 
     * This checks scrollpanes for the provided container as well.
     * @param con the container to search for
     * @return the containing frame or null if one wasnt found
     */
    public static JInternalFrame getEnclosingFrame(Container con)
    {
        for(Component tab : tabs.getComponents())
        {
            if(tab instanceof CalcDesktop)
            {
                CalcDesktop desktop = (CalcDesktop)tab;
                for(JInternalFrame frame : desktop.getAllFrames())
                {
                    if(frame.getContentPane() == con) return frame;
                    else if(frame.getContentPane() instanceof JScrollPane)
                    {
                        if(((JScrollPane)frame.getContentPane()).getViewport().getView() == con) return frame;
                    }
                }
            }
        }
        logWarning("getEnclosingFrame was called and returned null.");
        return null;
    }
    /**
     * rounds the provided number to the provided number of places
     * @param num the number to round
     * @param places the amount of places to round to
     * @return the rounded number
     */
    public static double round(double num, int places)
    {
        double pow = Math.pow(10, places);
        return (int)Math.round(num*pow)/pow;
    }
    /**
     * rounds the provided number to the provided amount of places in string form
     * calls Main.roundToString with the provided params and true for trim
     * @param num the number to round
     * @param places the amount of places to round to
     * @return the string form of the rounded number
     */
    public static String roundToString(double num, int places)
    {
        return roundToString(num, places, true);
    }
    /**
     * rounds the provided number to the provided number of places in string form
     * @param num the number to round
     * @param places the amount of places to round to
     * @param trim if true, the trailing zeros will be removed
     * @return the string form of the rounded number
     */
    public static String roundToString(double num, int places, boolean trim)
    {
        String str = String.format("%." + places + "f", round(num, places));
        if(trim && str.contains("."))
        {
            while(str.endsWith("0"))
                str = str.substring(0, str.length()-1);
            if(str.endsWith(".")) str += '0';
        }
        return str;
    }
    /**
     * Places the data from a file within the jar in a file outside of the jar
     * @param res the path of the jar resource
     * @param path the path of the file to be created
     * @return the file that was created
     */
    public static File extractResource(String res, String path)
    {
        File f = new File(path);
        URL url = Main.class.getClassLoader().getResource(res);
        InputStream in = null;
        FileOutputStream out = null;
        try
        {
            in = url.openStream();
            out = new FileOutputStream(f);
            while(in.available() > 0)
            {
                byte[] data;
                if(in.available() > 1024) data = new byte[1024];
                else data = new byte[in.available()];
                in.read(data);
                out.write(data);
            }
        }
        catch(Exception error)
        {
            logError("Could not load image resource " + res + " to " + path, error);
        }
        finally
        {
            if(in != null)
            {
                try
                {
                    in.close();
                }
                catch(Exception error)
                {}
            }
            if(out != null)
            {
                try
                {
                    out.close();
                }
                catch(Exception error)
                {}
            }
        }
        return f;
    }
    /**
     * loads an image from within the jar
     * @param path the path of the resource within the jar
     * @return the loaded ImageIcon, null if there was an error
     */
    public static ImageIcon loadImageResource(String path)
    {
        File f = new File(RUNTIME_DIR + "/" + path);
        if(f.exists()) return new ImageIcon(f.getAbsolutePath());
        try
        {
            logInfo("loading image resource at: " + path);
            URL res = Main.class.getClassLoader().getResource(path);
            if(res != null) return new ImageIcon(Main.class.getClassLoader().getResource(path));
            else logError("Resource " + path + " does not exist.");
        }
        catch(Exception error)
        {
            logError("Could not load image resource " + path, error);
        }
        return new ImageIcon();
    }
    /**
     * scales the provided image
     * @param icon the image to scale
     * @param scale the amount to scale
     * @return the scaled ImageIcon
     */
    public static ImageIcon scaleImage(ImageIcon icon, double scale)
    {
        BufferedImage img = new BufferedImage((int)(icon.getIconWidth()*scale+0.5),(int)(icon.getIconHeight()*scale+0.5),BufferedImage.TYPE_INT_RGB);
        Graphics g = img.createGraphics();
        g.drawImage(icon.getImage(), 0, 0, img.getWidth(), img.getHeight(), null);
        g.dispose();
        return new ImageIcon(img);
    }
    /**
     * Sets the cursor for the jframe
     * @param cursor the id of the cursor to be used
     */
    public static void setCursor(int cursor)
    {
        win.setCursor(cursor);
    }
    /**
     * Sets the cursor for the jframe
     * @param cursor the cursor to be used
     */
    public static void setCursor(Cursor cursor)
    {
        win.setCursor(cursor);
    }
    /**
     * calls downloadFile with the google download link with the provided document id
     * @param id the id of the document to be downloaded
     * @param dest the path of the file to download to
     */
    public static boolean downloadGoogleFile(String id, String dest)
    {
        return downloadFile("https://docs.google.com/uc?authuser=0&id=" + id + "&export=download", dest);
    }
    /**
     * downloads a file from the provided url into a file at the provided path
     * @param url the url to pull the data from
     * @param dest the path of the file to download to
     */
    public static boolean downloadFile(String url, String dest)
    {
        ReadableByteChannel in = null;
        FileOutputStream out = null;
        try
        {
            URL site = new URL(url);
            in = Channels.newChannel(site.openStream());
            out = new FileOutputStream(dest);
            out.getChannel().transferFrom(in, 0, Integer.MAX_VALUE);
            return true;
        }
        catch(Exception error)
        {
            logError("Could not download file from " + url + " to " + dest, error);
            return false;
        }
        finally
        {
            if(in != null)
            {
                try
                {
                    in.close();
                }
                catch(Exception error)
                {}
            }
            if(out != null)
            {
                try
                {
                    out.close();
                }
                catch(Exception error)
                {}
            }
        }
    }
    /**
     * Checks if a MouseEvent is a popup trigger and calls Main.showPopup() if it is
     * This was implemented for cross-platform compatibility because mac and linux must check this on
     * MouseListener.mousePressed() while windows checks it on MouseListener.mouseReleased()
     * @param event the MouseEvent to check for a trigger
     */
    public static void checkPopupTrigger(MouseEvent event)
    {
        if(event.isPopupTrigger() && !event.isConsumed()) showPopup(event.getComponent(), event.getX(), event.getY());
    }
    /*
    private static void createShortcut(String path)
    {
        JShellLink link = new JShellLink();
        link.setFolder(JShellLink.getDirectory("desktop"));
        link.setName("QuickBlend5.0");
        link.setPath(RUNTIME_DIR+"/run.vbs");
        link.setIconLocation(RUNTIME_DIR+"/icon.ico");
        link.save();
    }
    */
    private static boolean loadLastWorkspace()
    {
        tabs.removeAll();
        boolean load = WorkSpaceSaver.load(tabs);
        if(load) tabs.addTab("+", new JLabel("Add New WorkSpace", JLabel.CENTER));
        return load;
    }
    private static void initPopupMenu()
    {
        JMenu addMenu = new JMenu("Add...");
        final JMenuItem[] calcItems = new JMenuItem[CalcDesktop.CALC_CLASSES.length];
        final JMenuItem nameBtn = new JMenuItem("Rename Tab"), delBtn = new JMenuItem("Delete Tab"), loadBtn = new JMenuItem("Load Last Workspace"),
                desktopBtn = new JMenuItem("to Desktop"), taskBarBtn = new JMenuItem("to Taskbar");
        ActionListener btnAction = new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if(event.getSource() == nameBtn) renameCurrentTab();
                else if(event.getSource() == delBtn) deleteCurrentTab();
                else if(event.getSource() == loadBtn) loadLastWorkspace();
                /*
                else if(event.getSource() == desktopBtn)
                {
                    createShortcut(RUNTIME_DIR + "/test.lnk");
                }
                */
                else
                {
                    for(int i = 0; i < calcItems.length; i++)
                    {
                        if(event.getSource() == calcItems[i])
                        {
                            Component comp = tabs.getSelectedComponent();
                            if(comp instanceof CalcDesktop) ((CalcDesktop)comp).addCalculator(i);
                        }
                    }
                }
            }
        };
        for(int i = 0; i < calcItems.length; i++)
        {
            calcItems[i] = new JMenuItem(CalcDesktop.CALC_NAMES[i]);
            calcItems[i].addActionListener(btnAction);
            addMenu.add(calcItems[i]);
        }
        nameBtn.addActionListener(btnAction);
        delBtn.addActionListener(btnAction);
        loadBtn.addActionListener(btnAction);
        desktopBtn.addActionListener(btnAction);
        taskBarBtn.addActionListener(btnAction);
        JMenu shortcutMenu = new JMenu("Add QuickBlend™ Shortcut...");
        shortcutMenu.add(desktopBtn);
        shortcutMenu.add(taskBarBtn);
        popup.add(addMenu);
        popup.addSeparator();
        popup.add(nameBtn);
        popup.add(delBtn);
        popup.addSeparator();
        popup.add(loadBtn);
        //popup.addSeparator();
        //popup.add(shortcutMenu);
    }
    /**
     * logs an error with a generic exception to provide a stack trace
     * @param err the error message
     */
    public static void logError(String err)
    {
        logError(err, new Exception(err));
    }
    /**
     * logs and error and prints the stack trace of the provided exception
     * @param err the error message
     * @param exc the exception
     */
    public static void logError(String err, Exception exc)
    {
        errCode++;
        log(System.out, "[ERROR:" + errCode + "] " + err);
        log(System.err, "---ERROR CODE: " + errCode + "---");
        //logStackTrace(exc);
        System.err.print("  ");
        exc.printStackTrace(System.err);
    }
    /**
     * Prints a stack trace with a more desirable format
     * @param err the exception to print
     * @deprecated now, just call Exception.printStackTrace()
     */
    @Deprecated
    private static void logStackTrace(Exception err)
    {
        log(System.err, "ERROR " + err.getMessage());
        for(StackTraceElement elem : err.getStackTrace())
            System.err.println("\t" + elem);
    }
    /**
     * Logs the provided message with an [INFO] tag
     * @param info the message
     */
    public static void logInfo(String info)
    {
        log(System.out, "[INFO] " + info);
    }
    /**
     * Logs the provided message with a [WARNING] tag
     * @param warn the message
     */
    public static void logWarning(String warn)
    {
        log(System.out, "[WARNING] " + warn);
    }
    /**
     * prints a time stamp before the provided message to the provided printstream
     * @param ps the PrintStream
     * @param log the message
     */
    private static void log(PrintStream ps, String log)
    {
        ps.println(new Date(System.currentTimeMillis()) + ": " + log);
    }
    
    public static void uploadError()
    {
        
    }
}