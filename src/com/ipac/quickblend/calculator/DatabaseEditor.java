package com.ipac.quickblend.calculator;

import com.ipac.quickblend.Initializable;
import com.ipac.quickblend.Main;
import com.ipac.quickblend.Saveable;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.miginfocom.swing.MigLayout;

public class DatabaseEditor extends JSplitPane implements Initializable, Saveable
{
    private ArrayList<BlendComponent> comps;
    private BlendComponent select;
    private JList dbList;
    private JPanel rightPanel;
    private JTextField name, specGrav, kv40, kv100, vm40, vm100, vc;
    private JButton delBtn;
    private boolean change;
    
    public DatabaseEditor()
    {
        comps = new ArrayList<>(Arrays.asList(QuickBlendPanel.COMPONENTS));
        dbList = new JList(dbListModel);
        dbList.addListSelectionListener(dbListListener);
        dbList.setCellRenderer(cellRender);
        setLeftComponent(new JScrollPane(dbList));
        rightPanel = new JPanel();
        rightPanel.setLayout(new MigLayout());
        name = addField("Name:");
        specGrav = addField("Specific Gravity:");
        kv40 = addField("KV-40:");
        kv100 = addField("KV-100:");
        vm40 = addField("KV-40 Multiplier:");
        vm100 = addField("KV-100 Multiplier:");
        vc = addField("VC:");
        delBtn = new JButton("Delete");
        delBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                if(select == null) return;
                change = true;
                comps.remove(select);
                dbList.updateUI();
            }
        });
        rightPanel.add(delBtn);
        setRightComponent(rightPanel);
        change = false;
    }
    @Override
    public void init(JInternalFrame frame)
    {
        frame.setJMenuBar(initMenu());
        frame.addInternalFrameListener(frameListener);
    }
    private JMenuBar initMenu()
    {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        
        final JMenuItem saveBtn = new JMenuItem("Overwrite Database"),
                        newCompBtn = new JMenuItem("Add Component"),
                        defBtn = new JMenuItem("Restore Default Database");
        
        ActionListener menuBtns = new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if(event.getSource() == saveBtn)
                {
                    overwriteDB();
                }
                else if(event.getSource() == newCompBtn)
                {
                    BlendComponent newComp = new BlendComponent("New",0,0,0,0,0,0);
                    comps.add(newComp);
                    Collections.sort(comps);
                    dbList.updateUI();
                    dbList.setSelectedValue(newComp, true);
                }
                else if(event.getSource() == defBtn)
                {
                    int choice = JOptionPane.showConfirmDialog(DatabaseEditor.this, "This will erase any changes you may have made to the database.\nAre you sure you want to restore the default database?", 
                        "Reset Database", JOptionPane.YES_NO_OPTION);
                    if(choice == 0)
                    {
                        File file = QuickBlendPanel.DATABASE_FILE;
                        boolean del = !file.exists();
                        if(!del)
                        {
                            try
                            {
                                del = QuickBlendPanel.DATABASE_FILE.delete();
                            }
                            catch(Exception err)
                            {
                                err.printStackTrace();
                            }
                        }
                        if(!del)
                        {
                            JOptionPane.showMessageDialog(DatabaseEditor.this, "Could not delete existing database.\nPlease delete it manually and try again.", 
                                    "Could not delete.", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        Main.extractResource("res/database.csv", file.getAbsolutePath());
                        if(file.exists()) 
                        {
                            JOptionPane.showMessageDialog(DatabaseEditor.this, "Database successfully reset.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            BlendComponent[] def = BlendComponent.loadDatabase(file.getAbsolutePath());
                            QuickBlendPanel.updateComponents(def);
                            comps = new ArrayList<>(Arrays.asList(def));
                            dbList.updateUI();
                            fillFields();
                        }
                        else JOptionPane.showMessageDialog(DatabaseEditor.this, "Could not extract database.\nTry restarting the program.", "Failes", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        
        saveBtn.addActionListener(menuBtns);
        newCompBtn.addActionListener(menuBtns);
        defBtn.addActionListener(menuBtns);
        
        menu.add(saveBtn);
        menu.add(newCompBtn);
        menu.add(defBtn);
        bar.add(menu);
        return bar;
    }
    private void overwriteDB()
    {
        BlendComponent[] compArr = comps.toArray(new BlendComponent[0]);
        BlendComponent.save(compArr, QuickBlendPanel.DATABASE_FILE.getAbsolutePath());
        QuickBlendPanel.updateComponents(compArr);
        change = false;
    }
    private JTextField addField(String name)
    {
        rightPanel.add(new JLabel(name));
        JTextField field = new JTextField(20);
        field.addKeyListener(fields);
        field.addFocusListener(fieldFocus);
        rightPanel.add(field, "wrap");
        return field;
    }
    private double parseInput(String input, double def)
    {
        try
        {
            double parse = Double.parseDouble(input);
            if(!Double.isInfinite(parse) && !Double.isNaN(parse)) return parse;
        }
        catch(Exception err)
        {}
        return def;
    }
    private void updateSelected()
    {
        if(select == null) return;
        change = true;
        boolean befValid = select.isValid();
        String before = select.getName();
        select.setName(name.getText());
        String after = select.getName();
        select.setSpecificGravity(parseInput(specGrav.getText(),select.getSpecificGravity()));
        select.setKv40(parseInput(kv40.getText(),select.getKv40()));
        select.setKv100(parseInput(kv100.getText(),select.getKv100()));
        select.setVm40(parseInput(vm40.getText(),select.getVm40()));
        select.setVm100(parseInput(vm100.getText(),select.getVm100()));
        select.setVc(parseInput(vc.getText(),select.getVc()));
        if(!before.equals(after) || befValid != select.isValid())
        {
            Collections.sort(comps);
            dbList.updateUI();
            dbList.setSelectedValue(select, true);
        }
    }
    private void fillFields()
    {
        select = (BlendComponent)dbList.getSelectedValue();
        name.setText(select.getName());
        specGrav.setText(select.getSpecificGravity()+"");
        kv40.setText(select.getKv40()+"");
        kv100.setText(select.getKv100()+"");
        vm40.setText(select.getVm40()+"");
        vm100.setText(select.getVm100()+"");
        vc.setText(select.getVc()+"");
        for(Component comp : rightPanel.getComponents())
        {
            if(comp instanceof JTextField)
            {
                JTextField field = (JTextField)comp;
                if(field.getText().equalsIgnoreCase("nan")) field.setText("Invalid Value Found.");
            }
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
    
    private final DefaultListModel dbListModel = new DefaultListModel()
    {
        @Override
        public Object getElementAt(int i)
        {
            return comps.get(i);
        }
        @Override
        public int getSize()
        {
            return comps.size();
        }
    };
    private final ListSelectionListener dbListListener = new ListSelectionListener()
    {
        @Override
        public void valueChanged(ListSelectionEvent event)
        {
            fillFields();
        }
    };
    private final KeyAdapter fields = new KeyAdapter()
    {
        @Override
        public void keyReleased(KeyEvent event)
        {
            updateSelected();
        }
    };
    private final FocusListener fieldFocus = new FocusListener()
    {
        @Override
        public void focusGained(FocusEvent event)
        {
            ((JTextField)event.getSource()).selectAll();
        }
        @Override
        public void focusLost(FocusEvent event)
        {
            ((JTextField)event.getSource()).select(0, 0);
        }
    };
    private final InternalFrameAdapter frameListener = new InternalFrameAdapter()
    {
        @Override
        public void internalFrameDeactivated(InternalFrameEvent event)
        {
            if(change) overwriteDB();
        }
    };
    private final DefaultListCellRenderer cellRender = new DefaultListCellRenderer()
    {
        @Override
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
         {
             Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
             if(!((BlendComponent)value).isValid()) setBackground(Color.RED);
             return c;
         }
    };
}
