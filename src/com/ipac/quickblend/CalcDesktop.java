package com.ipac.quickblend;

import com.ipac.quickblend.calculator.BlendComponent;
import com.ipac.quickblend.ssi.SSISplitPane;
import com.ipac.quickblend.calculator.QuickBlendPanel;
import com.ipac.quickblend.eof.EOFPanel;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * an implementation of JDesktopPane that renders a bubble with the calculator names and the IPAC banner
 * @author Jonathan Crow
 */
public class CalcDesktop extends JDesktopPane implements Updateable
{
    private static final Font FONT = new Font("Aerial",0,18);
    private static final Color TEXT_COLOR = Color.WHITE, BUBBLE_COLOR = new Color(1f, 0f, 0f, 0.5f);
    public static final String[] CALC_NAMES = {"QuickBlend™", "Viscosity Index", "Specific Gravity", "Engine Oil Formulator", "Shear Stability", "Thermal Expansion", "Reference Materials", "Ash Calculator"};
    public static final Class[] CALC_CLASSES = {QuickBlendPanel.class, VIPanel.class, SpecGravPanel.class, EOFPanel.class, SSISplitPane.class, CTEPanel.class, NewImagePanel.class, AshPanel.class};
    //private static final boolean[] CALC_SCROLLABLE = {false, false, false, false, true, false};
    private static final ImageIcon[] SLIDE_SHOW = {Main.loadImageResource("res/img/banner/page1.jpg"),Main.loadImageResource("res/img/banner/page2.jpg"),Main.loadImageResource("res/img/banner/page3.jpg"),
    Main.loadImageResource("res/img/banner/page4.jpg"),Main.loadImageResource("res/img/banner/page5.jpg"),Main.loadImageResource("res/img/banner/page6.jpg"),Main.loadImageResource("res/img/banner/page7.jpg")};
    private static final ImageIcon INFO_IMAGE = Main.loadImageResource("res/img/info.png"), LOGO = Main.loadImageResource("res/img/logo_small.png"),
            WARN_IMAGE = Main.loadImageResource("res/img/warn.png");
    private JInternalFrame infoFrame;
    private Rectangle btnBounds = new Rectangle(10,10,25,25), infoBounds = new Rectangle(0, 0, INFO_IMAGE.getIconWidth(), INFO_IMAGE.getIconHeight()),
            warnBounds = new Rectangle(0,0,WARN_IMAGE.getIconWidth(),WARN_IMAGE.getIconHeight());
    private Rectangle[] calcBounds;
    private boolean expand, tutor = true, warn = false;
    private int slide = 0;
    private long endTutor, changeSlide;
    private String tutorStr, warnStr1, warnStr2;
    
    public CalcDesktop()
    {
        calcBounds = new Rectangle[CALC_NAMES.length];
        for(int i = 0; i < calcBounds.length; i++)
            calcBounds[i] = new Rectangle(0,0,0,0);
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        endTutor = System.currentTimeMillis() + 5000;
        changeSlide = System.currentTimeMillis() + 10000;
        tutorStr = "< Hover over this circle to begin.";
        warnStr1 = "There are components that were loaded improperly.";
        warnStr2 = "Click here to fix them.";
    }
    /**
     * Adds a calculator to the current tab and sets ad resets the loading cursor
     * @param i the index of the calculator in CalcDesktop.CALC_CLASSES
     */
    public void addCalculator(int i)
    {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try
        {
            Container con = (Container)CALC_CLASSES[i].newInstance();
            //if(CALC_SCROLLABLE[i]) Main.addScrollablePanel(con, CALC_NAMES[i]);
            //else Main.addPanel(con, CALC_NAMES[i]);
            Main.addPanel(con, CALC_NAMES[i]);
        }
        catch(Exception error)
        {
            Main.logError("Could not add calculator.", error);
        }
        setCursor(Cursor.getDefaultCursor());
    }
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        int slideX = getWidth()/2-SLIDE_SHOW[slide].getIconWidth()/2, slideY = getHeight()-SLIDE_SHOW[slide].getIconHeight()-30;
        g.fillRect(0, slideY-10, getWidth(), SLIDE_SHOW[slide].getIconHeight()+20);
        g.drawImage(SLIDE_SHOW[slide].getImage(), slideX, slideY, this);
        g.drawImage(LOGO.getImage(), slideX-LOGO.getIconWidth(), slideY, this);
        infoBounds.setLocation(getWidth()-INFO_IMAGE.getIconWidth()-10, 10);
        g.drawImage(INFO_IMAGE.getImage(), infoBounds.x, infoBounds.y, this);
        if(QuickBlendPanel.invalidComponents())
        {
            warnBounds.setLocation(getWidth()-WARN_IMAGE.getIconWidth()-10, infoBounds.y+infoBounds.height+10);
            g.drawImage(WARN_IMAGE.getImage(), warnBounds.x, warnBounds.y, this);
        }
        g.setColor(BUBBLE_COLOR);
        g.fillOval(btnBounds.x, btnBounds.y, btnBounds.width, btnBounds.height);
        if(btnBounds.width == 250)
        {
            g.setColor(TEXT_COLOR);
            g.setFont(FONT);
            FontMetrics fm = g.getFontMetrics();
            for(int i = 0; i < CALC_NAMES.length; i++)
            {
                calcBounds[i].setBounds(btnBounds.x+btnBounds.width/2-fm.stringWidth(CALC_NAMES[i])/2, btnBounds.y+25+(fm.getHeight()+5)*i-fm.getHeight()*3/4, fm.stringWidth(CALC_NAMES[i]), fm.getHeight());
                g.drawString(CALC_NAMES[i], calcBounds[i].x, calcBounds[i].y+fm.getHeight()*3/4);
            }
        }
        if(tutor)
        {
            g.setColor(TEXT_COLOR);
            g.setFont(FONT);
            g.drawString(tutorStr, btnBounds.x+btnBounds.width+15, 30);
        }
        if(warn)
        {
            g.setColor(TEXT_COLOR);
            g.setFont(FONT);
            FontMetrics fm = g.getFontMetrics();
            int x = getWidth()-fm.stringWidth(warnStr1)-10, y = warnBounds.y+warnBounds.height+fm.getHeight()+10;
            g.drawString(warnStr1, x, y);
            g.drawString(warnStr2, x, y+fm.getHeight()+10);
        }
    }
    @Override
    public void update(int delta) 
    {
        if(expand && btnBounds.width < 250)
        {
            btnBounds.setSize(btnBounds.width+delta, btnBounds.height+delta);
            if(btnBounds.width > 250) btnBounds.setSize(250, 250);
        }
        else if(!expand && btnBounds.width > 25)
        {
            btnBounds.setSize(btnBounds.width-delta, btnBounds.height-delta);
            if(btnBounds.width < 25) btnBounds.setSize(25, 25);
        }
        if(tutor && (expand || System.currentTimeMillis()>endTutor)) tutor = false;
        if(System.currentTimeMillis() >= changeSlide)
        {
            slide++;
            if(slide >= SLIDE_SHOW.length) slide = 0;
            changeSlide = System.currentTimeMillis() + 10000;
        }
    }
    
    /**
     * handles the bubble and checks popup triggers
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
            if(event.getButton() == MouseEvent.BUTTON1)
            {
                if(infoBounds.contains(event.getX(),event.getY()))
                {
                    if(infoFrame == null || !infoFrame.isVisible()) infoFrame = Main.addPanel(new InfoPanel(), "Info");
                    else Main.focusFrame(infoFrame);
                }
                else if(QuickBlendPanel.invalidComponents() && warnBounds.contains(event.getX(), event.getY()))
                {
                    QuickBlendPanel.openDatabaseEditor();
                }
                else if(btnBounds.width >= 250)
                {
                    for(int i = 0; i < calcBounds.length; i++)
                    {
                        if(calcBounds[i].contains(event.getX(), event.getY())) addCalculator(i);
                    }
                }
            }
            Main.checkPopupTrigger(event);
        }
        @Override
        public void mouseMoved(MouseEvent event)
        {
            expand = btnBounds.contains(event.getX(), event.getY());
            warn = QuickBlendPanel.invalidComponents() && warnBounds.contains(event.getX(), event.getY());
        }
        @Override
        public void mouseExited(MouseEvent event)
        {
            expand = false;
        }
    };
}