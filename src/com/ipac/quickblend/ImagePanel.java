package com.ipac.quickblend;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.swing.MigLayout;

/**
 * Displays a slideshow of images
 * @author Jonathan Crow
 * @deprecated Ugly. Use NewImagePanel
 */
@Deprecated
public class ImagePanel extends JPanel implements ActionListener, Updateable, Initializable, Saveable
{
    private static ImageIcon[] images = {getImage("SAE_J300"),getImage("SAE_J306"),
        getImage("ISO_Viscosity_Grades"),getImage("Comparison_Of_Viscosity_Classifications"),
        getImage("Base_Stock_Viscosities"),getImage("Common_Conversions"),getImage("Conversions"),
        getImage("Shear_Stability_Calculations")};
    private JButton prevBtn, nextBtn;
    private JSlider scale;
    private int imgIndex = 0;
    private long lastMove;
    
    public ImagePanel()
    {
        setLayout(new MigLayout());
        MouseAdapter mouse = new MouseAdapter()
        {
            public void mouseMoved(MouseEvent event)
            {
                prevBtn.setVisible(true);
                nextBtn.setVisible(true);
                scale.setVisible(true);
                lastMove = System.currentTimeMillis();
            }
            public void mouseDragged(MouseEvent event)
            {
                mouseMoved(event);
            }
        };
        InputMap input = getInputMap(JPanel.WHEN_FOCUSED);
        ActionMap actions = getActionMap();
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "next");
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "prev");
        actions.put("next", new AbstractAction()
        {
            public void actionPerformed(ActionEvent event)
            {
                nextBtn.doClick();
            }
        });
        actions.put("prev", new AbstractAction()
        {
            public void actionPerformed(ActionEvent event)
            {
                prevBtn.doClick();
            }
        });
        add((prevBtn = new JButton("<")));
        prevBtn.addActionListener(this);
        prevBtn.addMouseMotionListener(mouse);
        add((nextBtn = new JButton(">")), "wrap");
        nextBtn.addActionListener(this);
        nextBtn.addMouseMotionListener(mouse);
        add((scale = new JSlider(5,20,10)),"span 2, center");
        scale.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event) 
            {
                updateImage();
            }
        });
        scale.addMouseMotionListener(mouse);
        addMouseMotionListener(mouse);
        updateImage();
        lastMove = System.currentTimeMillis();
    }
    private void updateImage()
    {
        JInternalFrame frame = Main.getEnclosingFrame(this);
        if(frame != null) frame.setTitle(images[imgIndex].getDescription());
    }
    @Override
    public void init(JInternalFrame frame)
    {
        frame.setTitle(images[imgIndex].getDescription());
        try
        {
            frame.setMaximum(true);
        }
        catch(Exception error)
        {
            Main.logError("could not maximize window", error);
        }
    }
    @Override
    public Dimension getPreferredSize()
    {
        double s = (double)scale.getValue()/10;
        return new Dimension((int)(images[imgIndex].getIconWidth()*s+0.5),(int)(images[imgIndex].getIconHeight()*s+0.5));
    }
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        double s = (double)scale.getValue()/10;
        g.drawImage(images[imgIndex].getImage(), 0, 0, (int)(images[imgIndex].getIconWidth()*s+0.5), (int)(images[imgIndex].getIconHeight()*s+0.5), this);
    }
    @Override
    public void update(int delta) 
    {
        if(System.currentTimeMillis()-lastMove > 250)
        {
            prevBtn.setVisible(false);
            nextBtn.setVisible(false);
            scale.setVisible(false);
        }
    }
    @Override
    public void actionPerformed(ActionEvent event)
    {
        if(event.getSource() == prevBtn)
        {
            imgIndex--;
            if(imgIndex < 0) imgIndex = images.length-1;
        }
        else if(event.getSource() == nextBtn)
        {
            imgIndex++;
            if(imgIndex >= images.length) imgIndex = 0;
        }
        updateImage();
        lastMove = System.currentTimeMillis();
    }
    @Override
    public void putData(HashMap<String, String> data) 
    {
        data.put("imgIndex", imgIndex+"");
        data.put("scale", scale.getValue()+"");
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        if(data.containsKey("imgIndex"))
        {
            try
            {
                imgIndex = Integer.parseInt(data.get("imgIndex"));
            }
            catch(Exception error)
            {}
        }
        if(data.containsKey("scale"))
        {
            try
            {
                scale.setValue(Integer.parseInt(data.get("scale")));
            }
            catch(Exception error)
            {}
        }
    }
    
    private static ImageIcon getImage(String path)
    {
        ImageIcon img = Main.loadImageResource("res/img/tables/" + path + ".png");
        img.setDescription(path.replace("_"," "));
        return img;
    }
}