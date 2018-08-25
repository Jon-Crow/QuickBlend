package com.ipac.quickblend;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * A slide show of images that can be moved and resized
 * @author Jonathan Crow
 */
public class NewImagePanel extends JPanel implements Updateable, Initializable, Saveable
{
    private static final Font FONT = new Font("Aerial",0,24);
    private static final Color BUTTON_COLOR = new Color(0f,0f,0f,0.5f);
    private static final ImageIcon RIGHT_ARROW = Main.loadImageResource("res/img/right_key.gif"),
            LEFT_ARROW = Main.loadImageResource("res/img/left_key.gif"),
            SCROLL = Main.loadImageResource("res/img/mouse.gif"),
            MAGNIFY = Main.loadImageResource("res/img/magnify.png");
    private static ImageIcon[] images = {getImage("SAE_J300"),getImage("SAE_J306"),
        getImage("ISO_Viscosity_Grades"),getImage("Comparison_Of_Viscosity_Classifications"),
        getImage("Base_Stock_Viscosities"),getImage("Common_Conversions"),getImage("Conversions"),
        getImage("Shear_Stability_Calculations")};
    //private static boolean info = true;
    private JPopupMenu imgMenu;
    private int imgI, imgX, imgY;
    private double imgScale = 1;
    private boolean drawRight, drawLeft;
    private long activate;
    
    public NewImagePanel()
    {
        drawRight = (drawLeft = true);
        setBackground(Color.WHITE);
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);
        addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent event)
            {
                if(event.getKeyCode() == KeyEvent.VK_RIGHT) nextImage();
                else if(event.getKeyCode() == KeyEvent.VK_LEFT) previousImage();
            }
        });
        imgMenu = new JPopupMenu();
        final JMenuItem[] imgItems = new JMenuItem[images.length];
        final JMenuItem helpBtn = new JMenuItem("Help");
        ActionListener btnAction = new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if(event.getSource() == helpBtn)
                {
                    drawRight = (drawLeft = true);
                    activate = System.currentTimeMillis()+5000;
                }
                else
                {
                    for(int i = 0; i < imgItems.length; i++)
                    {
                        if(event.getSource() == imgItems[i]) imgI = i;
                    }
                    updateFrameTitle();
                }
            }
        };
        for(int i = 0; i < imgItems.length; i++)
        {
            imgItems[i] = new JMenuItem(images[i].getDescription());
            imgItems[i].addActionListener(btnAction);
            imgMenu.add(imgItems[i]);
        }
        helpBtn.addActionListener(btnAction);
        imgMenu.addSeparator();
        imgMenu.add(helpBtn);
    }
    @Override
    public void init(final JInternalFrame frame) 
    {
        activate = System.currentTimeMillis() + 5000;
        frame.setTitle(images[imgI].getDescription());
        /*
        if(info)
        {
            info = false;
            String html = "<html>\n" +
                        "  <body>\n" +
                        "    <p>This window contains several tables to assist you in your calculations.</p>\n" +
                        "    <h1>Controls:</h1>\n" +
                        "    <pre>\n" +
                        "In order to change which image is displayed, you can do any of the following:\n" +
                        "     1.) Use the arrow keys\n" +
                        "     2.) Hover over the sides and click the arrows\n" +
                        "     3.) Right click the center of the window and choose the desired reference material.\n" +
                        "You can also move the image by clicking and dragging it,\n" +
                        "and zoom in and out with the mouse wheel.\n" +
                        "\n" +
                        "(Click to Continue)\n" +
                        "    </pre>\n" +
                        "  </body>\n" +
                        "</html>";
            JLabel lbl = new JLabel(html, JLabel.CENTER);
            lbl.setOpaque(true);
            frame.setGlassPane(lbl);
            frame.getGlassPane().setVisible(true);
        }
                */
    }
    /**
     * Increments the image index. Sets it to zero if it goes above the bounds of the array.
     */
    public void nextImage()
    {
        imgI++;
        if(imgI >= images.length) imgI = 0;
        updateFrameTitle();
    }
    /**
     * Decrements the image index. Sets it to zero if it goes above the bounds of the array.
     */
    public void previousImage()
    {
        imgI--;
        if(imgI < 0) imgI = images.length-1;
        updateFrameTitle();
    }
    /**
     * Sets the title of the containing frame to the title of the displayed image
     */
    public void updateFrameTitle()
    {
        JInternalFrame frame = Main.getEnclosingFrame(this);
        if(frame != null) frame.setTitle(images[imgI].getDescription());
    }
    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension((int)(images[imgI].getIconWidth()*imgScale+0.5), (int)(images[imgI].getIconHeight()*imgScale+0.5));
    }
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.drawImage(images[imgI].getImage(), imgX, imgY, (int)(images[imgI].getIconWidth()*imgScale+0.5), (int)(images[imgI].getIconHeight()*imgScale+0.5), this);
        if(activate > 0)
        {
            g.drawImage(RIGHT_ARROW.getImage(), getWidth()-150-RIGHT_ARROW.getIconWidth()/2, getHeight()/2-RIGHT_ARROW.getIconHeight()/2, this);
            g.drawImage(LEFT_ARROW.getImage(), 150-LEFT_ARROW.getIconWidth()/2, getHeight()/2-LEFT_ARROW.getIconHeight()/2, this);
            g.drawImage(SCROLL.getImage(), getWidth()/2-SCROLL.getIconWidth()/2, getHeight()/2-SCROLL.getIconHeight()/2, this);
            g.drawImage(MAGNIFY.getImage(), getWidth()/2-MAGNIFY.getIconWidth()/2, getHeight()/2-SCROLL.getIconHeight()/2-MAGNIFY.getIconHeight(), this);
        }
        if(drawLeft || drawRight)
        {
            g.setColor(BUTTON_COLOR);
            if(drawRight) g.fillRect(getWidth()-100, 0, 100, getHeight());
            if(drawLeft) g.fillRect(0, 0, 100, getHeight());
            g.setColor(Color.WHITE);
            g.setFont(FONT);
            FontMetrics fm = g.getFontMetrics();
            if(drawRight) g.drawString(">", getWidth()-50-fm.stringWidth(">")/2, getHeight()/2-fm.getHeight()/2);
            if(drawLeft) g.drawString("<", 50-fm.stringWidth("<")/2, getHeight()/2-fm.getHeight()/2);
        }
        g.setColor(BUTTON_COLOR);
        int w = 15*images.length, x = getWidth()/2-w/2;
        for(int i = 0; i < images.length; i++)
        {
            if(i == imgI) g.fillOval(x+15*i, getHeight()-15, 10, 10);
            else g.drawOval(x+15*i, getHeight()-15, 10, 10);
        }
    }
    @Override
    public void update(int delta) 
    {
        if(activate > 0 && System.currentTimeMillis() > activate)
        {
            drawRight = (drawLeft = false);
            activate = 0;
        }
    }
    private int parseInt(HashMap<String, String> data, String name)
    {
        if(data.containsKey(name))
        {
            try
            {
                return Integer.parseInt(data.get(name));
            }
            catch(Exception error)
            {}
        }
        return 0;
    }
    @Override
    public void putData(HashMap<String, String> data) 
    {
        data.put("imgI", imgI+"");
        data.put("imgX", imgX+"");
        data.put("imgY", imgY+"");
        data.put("imgScale", imgScale+"");
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        imgI = parseInt(data, "imgI");
        imgX = parseInt(data, "imgX");
        imgY = parseInt(data, "imgY");
        if(data.containsKey("imgScale"))
        {
            try
            {
                imgScale = Double.parseDouble(data.get("imgScale"));
            }
            catch(Exception error)
            {}
        }
        activate = 0;
    }
    /**
     * Gets the image at the specified path and sets the description to a cleaner title
     * @param path the resource path
     * @return the loaded image with updated description
     */
    private static ImageIcon getImage(String path)
    {
        ImageIcon img = Main.loadImageResource("res/img/tables/" + path + ".png");
        img.setDescription(path.replace("_"," "));
        return img;
    }
    private final MouseAdapter mouse = new MouseAdapter()
    {
        private int dragX = -1, dragY = -1;
        
        private void checkPopupTrigger(MouseEvent event)
        {
            if(event.isPopupTrigger()) imgMenu.show(NewImagePanel.this, event.getX(), event.getY());
        }
        @Override
        public void mousePressed(MouseEvent event)
        {
            if(activate <= 0 && event.getButton() == MouseEvent.BUTTON1)
            {
                if(drawRight) nextImage();
                else if(drawLeft) previousImage();
                else
                {
                    dragX = event.getX();
                    dragY = event.getY();
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                checkPopupTrigger(event);
            }
        }
        @Override
        public void mouseReleased(MouseEvent event)
        {
            if(activate <= 0)
            {
                JInternalFrame frame = Main.getEnclosingFrame(NewImagePanel.this);
                if(frame != null && frame.getGlassPane() != null && frame.getGlassPane().isVisible()) frame.getGlassPane().setVisible(false);
                dragX = -1;
                dragY = -1;
                checkPopupTrigger(event);
                setCursor(Cursor.getDefaultCursor());
            }
            else 
            {
                drawRight = (drawLeft = false);
                activate = 0;
            }
        }
        @Override
        public void mouseDragged(MouseEvent event)
        {
            if(activate <= 0 && dragX != -1 && dragY != -1)
            {
                imgX -= dragX-event.getX();
                imgY -= dragY-event.getY();
                dragX = event.getX();
                dragY = event.getY();
            }
        }
        @Override
        public void mouseMoved(MouseEvent event)
        {
            if(activate <= 0)
            {
                drawRight = event.getX() > getWidth()-100;
                drawLeft = event.getX() < 100;
            }
        }
        @Override
        public void mouseExited(MouseEvent event)
        {
            if(activate <= 0)
            {
                drawRight = false;
                drawLeft = false;
            }
        }
        @Override
        public void mouseWheelMoved(MouseWheelEvent event)
        {
            if(activate <= 0)
            {
                imgScale -= event.getPreciseWheelRotation()/4;
                if(imgScale > 2) imgScale = 2;
                else if(imgScale < 0.5) imgScale = 0.5;
            }
        }
    };
}