package com.ipac.quickblend.calculator;

import com.ipac.quickblend.Initializable;
import com.ipac.quickblend.Main;
import com.ipac.quickblend.Saveable;
import com.ipac.quickblend.Updateable;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import net.miginfocom.swing.MigLayout;

public class QuickBlendPanel extends JPanel implements ActionListener, Updateable, Initializable, Saveable
{
    public static final File DATABASE_FILE = new File(Main.RUNTIME_DIR + "/database.csv"), BLENDS_FILE = new File(Main.RUNTIME_DIR + "/blends.xml");
    public static final Blends BLENDS = Blends.load(BLENDS_FILE.getAbsolutePath());
    private static final ImageIcon BEAKER_ICON = Main.loadImageResource("res/img/beaker.gif"), ADD_ICON = Main.loadImageResource("res/img/icons/add.png"), DELETE_ICON = Main.loadImageResource("res/img/icons/delete.png");
    public static BlendComponent[] COMPONENTS = BlendComponent.loadDatabase(DATABASE_FILE.getAbsolutePath());
    private static ArrayList<QuickBlendPanel> panels = new ArrayList<>();
    private JInternalFrame blendsFrame;
    private JRadioButton weightP, volumeP;
    private JButton blendBtn, blendListBtn, dbBtn, editDbBtn, addBtn;
    private JTextField nameField;
    private ArrayList<ComponentRow> rows = new ArrayList<>();
    private long drawBeaker;
    private JInternalFrame resFrame;
    private String resTitle;
    
    public QuickBlendPanel()
    {
        setLayout(new MigLayout("fillx"));
        System.out.println("Background color: " + getBackground());
        ButtonGroup btnGroup = new ButtonGroup();
        btnGroup.add((weightP = new JRadioButton("Weight Percent")));
        btnGroup.add((volumeP = new JRadioButton("Volume Percent")));
        add(weightP, "span 2,left");
        weightP.setSelected(true);
        add(volumeP, "span 2,center");
        add((blendBtn = new JButton("Blend")), "span 2,right,wrap");
        blendBtn.addActionListener(this);
        blendBtn.setIcon(BEAKER_ICON);
        add((blendListBtn = new JButton("My Blends")));
        blendListBtn.addActionListener(this);
        add(new JLabel("Blend Name:"),"span 1");
        add((nameField = new JTextField(25)), "span 3,wrap");
        add((dbBtn = new JButton("Open Database Spreadsheet")),"span 3,left");
        dbBtn.setIcon(UIManager.getIcon("FileChooser.fileIcon"));
        dbBtn.addActionListener(this);
        add((editDbBtn = new JButton("Edit Database")),"span 3, right, wrap");
        editDbBtn.setIcon(UIManager.getIcon("FileChooser.listViewIcon"));
        editDbBtn.addActionListener(this);
        add((addBtn = new JButton(ADD_ICON)), "span 6,center,wrap");
        addBtn.addActionListener(this);
        addRow();
        addRow();
        panels.add(this);
    }
    @Override
    public void init(JInternalFrame frame)
    {
        frame.addInternalFrameListener(new InternalFrameAdapter()
        {
            public void internalFrameClosed(InternalFrameEvent event)
            {
                if(blendsFrame != null && blendsFrame.isVisible()) blendsFrame.dispose();
            }
        });
    }
    private void updateComboBoxes()
    {
        for(ComponentRow row : rows)
        {
            BlendComponent comp = (BlendComponent)row.compBox.getSelectedItem();
            row.compBox.setModel(new DefaultComboBoxModel(COMPONENTS));
            row.compBox.setSelectedItem(comp);
        }
    }
    private ComponentRow addRow()
    {
        ComponentRow row = new ComponentRow();
        rows.add(row);
        pack();
        return row;
    }
    private void pack()
    {
        Dimension prefSize = getPreferredSize();
        if(getWidth() < prefSize.width || getHeight() < prefSize.height)
        {
            JInternalFrame frame = Main.getEnclosingFrame(this);
            if(frame != null) frame.pack();
        }
    }
    public Blend createBlend()
    {
        Blend blend = new Blend(rows.size(),weightP.isSelected() ? Blend.PERCENT_TYPE_WEIGHT : Blend.PERCENT_TYPE_VOLUME);
        double perc, total = 0;
        for(int i = 0; i < rows.size()-1; i++)
        {
            perc = rows.get(i).getPercentValue();
            if(Double.isNaN(perc)) 
            {
                JOptionPane.showMessageDialog(this, "\"" + rows.get(i).percField.getText() + "\" cannot be translated to a number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            blend.setEntry(i, rows.get(i).getBlendComponent(), perc);
            total += perc;
        }
        if(total <= 1) 
        {
            blend.setEntry(rows.size()-1, rows.get(rows.size()-1).getBlendComponent(), 1-total);
            rows.get(rows.size()-1).setPercentValue((1-total)*100);
            return blend;
        }
        else
        {
            JOptionPane.showMessageDialog(this, "Provided percentages muct add up to equal 100.\n(Your total=" + total*100 + ")", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    public void setBlend(Blend blend, String name)
    {
        nameField.setText(name);
        for(int i = rows.size()-1; i >= 0; i--)
            rows.get(i).delete();
        weightP.setSelected(blend.getPercentType() == Blend.PERCENT_TYPE_WEIGHT);
        volumeP.setSelected(blend.getPercentType() == Blend.PERCENT_TYPE_VOLUME);
        for(int i = 0; i < blend.getSize(); i++)
        {
            ComponentRow row = addRow();
            row.setBlendComponent(blend.getComponent(i));
            row.setPercentValue((blend.getPercentType() == Blend.PERCENT_TYPE_WEIGHT ? blend.getWeightPercent(i) : blend.getVolumePercent(i))*100);
        }
    }
    /*
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        if(System.currentTimeMillis() <= drawBeaker)
        {
            int w = BEAKER_ICON.getIconWidth()*4, h = BEAKER_ICON.getIconHeight()*4;
            g.drawImage(BEAKER_ICON.getImage(), getWidth()/2-w/2, getHeight()/2-h/2, w, h, this);
        }
    }
    */
    @Override
    public void update(int delta) 
    {
        /*
        if(nextRes != null && System.currentTimeMillis() > drawBeaker)
        {
            Main.addPanel(nextRes, resTitle, 475, 0);
            nextRes = null;
        }
        */
    }
    @Override
    public void actionPerformed(ActionEvent event) 
    {
        if(event.getSource() == addBtn && rows.size() < 15) addRow();
        else if(event.getSource() == blendListBtn) 
        {
            if(blendsFrame == null || !blendsFrame.isVisible()) blendsFrame = Main.addPanel(new BlendsList(this), "My Blends");
            else Main.focusFrame(blendsFrame);
        }
        else if(event.getSource() == dbBtn)
        {
            if(DATABASE_FILE.exists()) 
            {
                try
                {
                    Main.logInfo("Opening " + DATABASE_FILE.getAbsolutePath() + " with default software");
                    Desktop.getDesktop().open(DATABASE_FILE);
                }
                catch(Exception error)
                {
                    Main.logError("Could not open database file externally.", error);
                    JOptionPane.showMessageDialog(this, "Could not open database file.\nIt may be corrupt.", "Could not open", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        else if(event.getSource() == editDbBtn)
        {
            Main.addPanel(new NewDatabaseTable(), "Database");
        }
        else if(event.getSource() == blendBtn)
        {
            Main.logInfo("Creating blend");
            Blend blend = createBlend();
            if(blend != null) 
            {
                String name = nameField.getText().isEmpty() ? "unnamed" : nameField.getText();
                /*
                boolean cancel = false;
                if(BLENDS.blendExists(name))
                {
                    int choice = JOptionPane.showOptionDialog(this, "A blend with that name already exists.\nDo you want to overwrite it?", 
                            "Blend Already Exists", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Yes","No"}, 1);
                    cancel = choice == 1;
                }
                if(!cancel)
                {
                */
                    BLENDS.setBlend(name, blend);
                    BLENDS.save(BLENDS_FILE.getAbsolutePath());
                    //JInternalFrame frame = Main.getEnclosingFrame(this);
                    if(resFrame == null || !resFrame.isVisible())
                    {
                        resFrame = Main.addPanel(new BlendResultPanel(blend.blend()), resTitle, 475, 0);
                    }
                    else 
                    {
                        ((BlendResultPanel)resFrame.getContentPane()).setResults(blend.blend());
                    }
                    resFrame.setTitle(resTitle = name + " Results");
                    //drawBeaker = System.currentTimeMillis()+3000;
                //}
            }
            else Main.logWarning("Blend was not created. Probably due to invalid input.");
        }
        if(rows.size() > 2)
        {
            for(int i = rows.size()-1; i >= 0; i--)
            {
                if(event.getSource() == rows.get(i).delBtn) rows.get(i).delete();
            }
        }
    }
    @Override
    public void putData(HashMap<String, String> data)
    {
        data.put("volumePSelected", volumeP.isSelected()+"");
        data.put("weightPSelected", weightP.isSelected()+"");
        data.put("blendName", nameField.getText());
        data.put("rowNum", rows.size()+"");
        for(int i = 0; i < rows.size(); i++)
        {
            data.put("row" + i + "comp", rows.get(i).getBlendComponent().getName());
            data.put("row" + i + "perc", rows.get(i).percField.getText());
        }
        if(blendsFrame != null && blendsFrame.isVisible()) data.put("blendsFrame", blendsFrame.getX() + ":" + blendsFrame.getY() + ":" + blendsFrame.getWidth() + ":" + blendsFrame.getHeight());
    }
    @Override
    public void loadData(HashMap<String, String> data) 
    {
        volumeP.setSelected(data.containsKey("volumePSelected") && data.get("volumePSelected").equalsIgnoreCase("true"));
        weightP.setSelected(data.containsKey("weightPSelected") && data.get("weightPSelected").equalsIgnoreCase("true"));
        nameField.setText(data.containsKey("blendName") ? data.get("blendName") : "");
        int rowNum = 2;
        if(data.containsKey("rowNum"))
        {
            try
            {
                rowNum = Integer.parseInt(data.get("rowNum"));
            }
            catch(Exception error)
            {}
        }
        for(int i = rows.size()-1; i >= 0; i--)
            rows.get(i).delete();
        for(int i = 0; i < rowNum; i++)
        {
            addRow();
            if(data.containsKey("row" + i + "comp")) rows.get(i).setBlendComponent(data.get("row" + i + "comp"));
            if(data.containsKey("row" + i + "perc")) rows.get(i).percField.setText(data.get("row" + i + "perc"));
        }
        if(data.containsKey("blendsFrame"))
        {
            String[] bounds = data.get("blendsFrame").split(":");
            if(bounds.length == 4)
            {
                try
                {
                    int x = Integer.parseInt(bounds[0]), y = Integer.parseInt(bounds[1]),
                            w = Integer.parseInt(bounds[2]), h = Integer.parseInt(bounds[3]);
                    blendsFrame = Main.addPanel(new BlendsList(this), "My Blends");
                    blendsFrame.setBounds(x, y, w, h);
                }
                catch(Exception error)
                {}
            }
        }
    }
    
    public static void updateComponents(BlendComponent[] comps)
    {
        COMPONENTS = comps;
        for(int i = panels.size()-1; i >= 0; i--)
        {
            if(Main.getEnclosingFrame(panels.get(i)) == null) panels.remove(i);
            else panels.get(i).updateComboBoxes();
        }
    }
    
    private class ComponentRow
    {
        private JButton delBtn;
        private JComboBox<BlendComponent> compBox;
        private JTextField percField;
        
        private ComponentRow()
        {
            add((delBtn = new JButton(DELETE_ICON)),"left");
            delBtn.addActionListener(QuickBlendPanel.this);
            add((compBox = new JComboBox(COMPONENTS)),"span 3, center");
            //compBox.setModel(new ComponentBoxModel());
            add((percField = new JTextField(10)),"span 2,right,wrap");
            Main.updateUI();
        }
        private BlendComponent getBlendComponent()
        {
            return (BlendComponent)compBox.getSelectedItem();
        }
        private void setBlendComponent(BlendComponent comp)
        {
            compBox.setSelectedItem(comp);
        }
        private void setBlendComponent(String name)
        {
            ComboBoxModel<BlendComponent> model = compBox.getModel();
            for(int i = 0; i < model.getSize(); i++)
            {
                if(model.getElementAt(i).getName().equalsIgnoreCase(name)) setBlendComponent(model.getElementAt(i));
            }
        }
        private double getPercentValue()
        {
            try
            {
                return Math.abs(Double.parseDouble(percField.getText())/100);
            }
            catch(Exception error)
            {}
            return Double.NaN;
        }
        public void setPercentValue(double perc)
        {
            percField.setText(Main.roundToString(perc, 4));
        }
        private void delete()
        {
            remove(delBtn);
            remove(compBox);
            remove(percField);
            rows.remove(this);
            pack();
        }
    }
    /*
    private static class ComponentBoxModel implements ComboBoxModel<BlendComponent>
    {
        private Object selected = COMPONENTS[0];
        
        @Override
        public void setSelectedItem(Object obj)
        {
            selected = obj;
        }
        @Override
        public Object getSelectedItem()
        {
            return selected;
        }
        @Override
        public int getSize()
        {
            return COMPONENTS.length;
        }
        @Override
        public BlendComponent getElementAt(int i)
        {
            return COMPONENTS[i];
        }
        @Override
        public void addListDataListener(ListDataListener ldl)
        {}
        @Override
        public void removeListDataListener(ListDataListener ldl)
        {}
    }
    */
}