package com.ipac.quickblend.eof;

import com.ipac.quickblend.Main;
import com.ipac.quickblend.Saveable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

public class EOFPanel extends JPanel implements ActionListener, Saveable
{
    private static final Font FONT = new Font("Times New Roman",Font.BOLD,16);
    private static final String[] INTRO_LINES = {"This is the Engine Oil Formulator.","To blend an oil, first choose a light","base oil, a heavy base oil, and a VII.","(Click to continue)"};
    private static final String[] INFO = {"Data for 75% light/25% heavy oil blend:", "Data for 25% light/75% heavy oil blend:", "75% light/25% heavy + 10% VII:", "Desired:"};
    private static Point[] points = {new Point(0,0),new Point(0,0),new Point(0,0),new Point(0,0)};
    private JLabel infoLbl;
    private JTextField kvField, ccsField;
    private JButton prevBtn, nextBtn;
    private int page;
    private boolean intro = true;
    
    public EOFPanel()
    {
        setLayout(new MigLayout());
        add((infoLbl = new JLabel("")), "span 3, wrap");
        add(new JLabel("KV100:"));
        add((kvField = new JTextField(15)),"span 2, wrap");
        add(new JLabel("CCS:"));
        add((ccsField = new JTextField(15)), "span 2, wrap");
        add((prevBtn = new JButton("Back")), "left");
        prevBtn.addActionListener(EOFPanel.this);
        add((nextBtn = new JButton("Next")), "skip 1, right");
        nextBtn.addActionListener(EOFPanel.this);
        setPage(0);
        for(Component comp : getComponents())
            comp.setVisible(false);
        addMouseListener(new MouseAdapter()
        {
            public void mouseReleased(MouseEvent event)
            {
                removeIntro();
                removeMouseListener(this);
            }
        });
    }
    private void removeIntro()
    {
        intro = false;
        for(Component comp : getComponents())
            comp.setVisible(true);
    }
    private boolean applyInput()
    {
        try
        {
            points[page].x = Double.parseDouble(ccsField.getText());
            points[page].y = Double.parseDouble(kvField.getText());
            return true;
        }
        catch(Exception error)
        {
            return false;
        }
    }
    private void setPage(int p)
    {
        page = p;
        infoLbl.setText(INFO[p]);
        kvField.setText(Main.roundToString(points[p].y, 4));
        ccsField.setText(Main.roundToString(points[p].x, 4));
        prevBtn.setEnabled(p > 0);
        nextBtn.setText(p == points.length-1 ? "Finish" : "Next");
    }
    private void calculate()
    {
        Point[] logPoints = new Point[points.length];
        for(int i = 0; i < points.length; i++)
            logPoints[i] = new Point(Math.log10(points[i].x), Math.log10(points[i].y));
        Point des = logPoints[logPoints.length-1];
        Line bottom = new Line(points[0], points[1]), left = new Line(points[0], points[2]);
        Point xInt = bottom.getIntersection(left.getParallelLine(des.x - left.getX(des.y), 0)),
                yInt = left.getIntersection(bottom.getParallelLine(0, des.y - xInt.y));
        double ratio = 25 + points[0].getDistance(xInt)/points[0].getDistance(points[1])*50,
                vii = 10*(points[0].getDistance(yInt)/points[0].getDistance(points[2]));
        JInternalFrame frame = Main.getEnclosingFrame(this);
        if(frame != null)
        {
            JInternalFrame resFrame = Main.addPanel(new EOFResultsPanel(ratio, vii), "Oil Formulator Results");
            resFrame.setLocation(frame.getX(), frame.getY());
            frame.dispose();
        }
    }
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if(intro)
        {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.BLACK);
            g.setFont(FONT);
            FontMetrics fm = g.getFontMetrics();
            for(int i = 0; i < INTRO_LINES.length; i++)
                g.drawString(INTRO_LINES[i], getWidth()/2-fm.stringWidth(INTRO_LINES[i])/2, 20+(fm.getHeight()+5)*i);
        }
    }
    @Override
    public void actionPerformed(ActionEvent event) 
    {
        if(applyInput())
        {
            if(event.getSource() == prevBtn) setPage(page - 1);
            else if(event.getSource() == nextBtn) 
            {
                if(nextBtn.getText().equals("Next")) setPage(page + 1);
                else calculate();
            }
        }
        else JOptionPane.showMessageDialog(this, "You must enter valid input before proceeding.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }
    private double parseDouble(HashMap<String, String> data, String name)
    {
        if(data.containsKey(name))
        {
            try
            {
                return Double.parseDouble(data.get(name));
            }
            catch(Exception error)
            {}
        }
        return Double.NaN;
    }
    @Override
    public void putData(HashMap<String, String> data) 
    {
        for(int i = 0; i < points.length; i++)
        {
            data.put("x" + i, points[i].x+"");
            data.put("y" + i, points[i].y+"");
        }
        data.put("page", page+"");
        data.put("info",infoLbl.getText());
        data.put("kv", kvField.getText());
        data.put("ccs", ccsField.getText());
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        removeIntro();
        for(int i = 0; i < points.length; i++)
        {
            double x = parseDouble(data, "x" + i), y = parseDouble(data, "y" + i);
            if(!Double.isNaN(x)) points[i].x = x;
            if(!Double.isNaN(y)) points[i].y = y;
        }
        if(data.containsKey("page"))
        {
            try
            {
                setPage(Integer.parseInt(data.get("page")));
            }
            catch(Exception error)
            {}
        }
        if(data.containsKey("info")) infoLbl.setText(data.get("info"));
        if(data.containsKey("kv")) kvField.setText(data.get("kv"));
        if(data.containsKey("ccs")) ccsField.setText(data.get("ccs"));
    }
    
    private static class Point
    {
        private double x, y;
        
        private Point(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
        private double getDistance(Point p)
        {
            return Math.sqrt(Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2));
        }
    }
    private static class Line
    {
        private Point p1, p2;
        private double m, b;
        
        private Line(Point p1, Point p2)
        {
            this.p1 = p1;
            this.p2 = p2;
            m = (p1.y-p2.y)/(p1.x-p2.x);
            b = p1.y-b*p1.x;
        }
        private double getX(double y)
        {
            return (y-b)/m;
        }
        private double getY(double x)
        {
            return m*x+b;
        }
        private Point getIntersection(Line line)
        {
            double x = (line.b-b)/(m-line.m);
            return new Point(x, line.getY(x));
        }
        private Line getParallelLine(double xOff, double yOff)
        {
            return new Line(new Point(p1.x+xOff, p1.y+yOff), new Point(p2.x+xOff, p2.y+yOff));
        }
    }
}