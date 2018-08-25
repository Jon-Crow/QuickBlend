package com.ipac.quickblend.calculator;

import com.ipac.quickblend.Initializable;
import com.ipac.quickblend.Main;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

public class DatabaseEditor2 extends JPanel implements Initializable
{
    private JTable table;
    private JTextField name, specGrav, kv40, kv100, vm40, vm100, vc;
    private JButton newBtn, delBtn, saveBtn, defBtn;
    private BlendComponent select;
    private boolean change;
    
    public DatabaseEditor2()
    {
        setLayout(new MigLayout("fill"));
        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(tableSelector);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().getColumnModel().getColumn(0).setPreferredWidth(200);
        table.addKeyListener(search);
        add(new JScrollPane(table), "span 2, growx, wrap");
        name = addField("Name:");
        specGrav = addField("Specific Gravity:");
        kv40 = addField("KV40:");
        kv100 = addField("KV100:");
        vm40 = addField("KV40 Multiplier:");
        vm100 = addField("KV100 Multiplier:");
        vc = addField("VC:");
        add(newBtn = initButton("New"), "skip 1, split 4");
        add(delBtn = initButton("Delete"));
        add(saveBtn = initButton("Save"));
        add(defBtn = initButton("Load Default Database"));
    }
    @Override
    public void init(JInternalFrame frame) 
    {
        table.setRowSelectionInterval(0, 0);
        frame.addInternalFrameListener(frameListener);
    }
    private JButton initButton(String txt)
    {
        JButton btn = new JButton(txt);
        btn.addActionListener(buttons);
        return btn;
    }
    private JTextField addField(String txt)
    {
        JTextField field = new JTextField(20);
        field.addKeyListener(fields);
        field.addFocusListener(fieldFocus);
        add(new JLabel(txt));
        add(field, "wrap");
        return field;
    }
    private void fillFields()
    {
        select = (BlendComponent)table.getModel().getValueAt(table.getSelectedRow(), 0);
        name.setText(select.getName());
        specGrav.setText(select.getSpecificGravity()+"");
        kv40.setText(select.getKv40()+"");
        kv100.setText(select.getKv100()+"");
        vm40.setText(select.getVm40()+"");
        vm100.setText(select.getVm100()+"");
        vc.setText(select.getVc()+"");
        for(Component comp : getComponents())
        {
            if(comp instanceof JTextField)
            {
                JTextField field = (JTextField)comp;
                if(field.getText().equalsIgnoreCase("nan")) field.setText("Invalid Value Found.");
            }
        }
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
    private void selectComponent(BlendComponent select)
    {
        this.select = select;
        BlendComponent[] comps = QuickBlendPanel.COMPONENTS;
        for(int i = 0; i < comps.length; i++)
        {
            if(comps[i] == select)
            {
                table.setRowSelectionInterval(i, i);	
                table.scrollRectToVisible(table.getCellRect(i,0,true));
                return;
            }
        }
    }
    private void updateSelected()
    {
        if(select == null) return;
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
            BlendComponent[] comps = QuickBlendPanel.COMPONENTS;
            Arrays.sort(comps);
            QuickBlendPanel.updateComponents(comps);
            updateUI();
            selectComponent(select);
        }
        change = true;
    }
    private void overwriteDB()
    {
        BlendComponent.save(QuickBlendPanel.COMPONENTS, QuickBlendPanel.DATABASE_FILE.getAbsolutePath());
    }
    
    private final InternalFrameAdapter frameListener = new InternalFrameAdapter()
    {
        @Override
        public void internalFrameDeactivated(InternalFrameEvent event)
        {
            if(change) overwriteDB();
        }
    };
    private final AbstractTableModel model = new AbstractTableModel()
    {
        private String[] cols = {"Name","Specific Gravity","KV40","KV100","KV40 Multiplier","KV100 Multiplier","Vc"};
        
        @Override
        public String getColumnName(int col)
        {
            return cols[col];
        }
        @Override
        public int getRowCount() 
        {
            return QuickBlendPanel.COMPONENTS.length;
        }
        @Override
        public int getColumnCount() 
        {
            return 7;
        }
        @Override
        public Object getValueAt(int row, int col) 
        {
            if(row >= QuickBlendPanel.COMPONENTS.length) return null;
            BlendComponent comp = QuickBlendPanel.COMPONENTS[row];
            switch(col)
            {
                case 0:
                    return comp;
                case 1:
                    return comp.getSpecificGravity();
                case 2:
                    return comp.getKv40();
                case 3:
                    return comp.getKv100();
                case 4:
                    return comp.getVm40();
                case 5:
                    return comp.getVm100();
                case 6:
                    return comp.getVc();
            }
            return null;
        }
    };
    
    private final ListSelectionListener tableSelector = new ListSelectionListener()
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
    private final KeyAdapter search = new KeyAdapter()
    {
        private String query = "";
        private long lastKey = 0;
        
        @Override
        public void keyTyped(KeyEvent event)
        {
            long time = System.currentTimeMillis()-lastKey;
            if(time > 3000) query = "";
            lastKey = System.currentTimeMillis();
            char key = event.getKeyChar();
            if(key == '\b')
            {
                if(query.length() > 0) query = query.substring(0,query.length()-1);
            }
            else if(Character.isLetterOrDigit(key) || key == ' ') query += Character.toLowerCase(key);
            for(BlendComponent check : QuickBlendPanel.COMPONENTS)
            {
                if(check.getName().toLowerCase().startsWith(query))
                {
                    selectComponent(check);
                    return;
                }
            }
        }
    };
    private final ActionListener buttons = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent event) 
        {
            if(event.getSource() == delBtn)
            {
                BlendComponent[] oldArr = QuickBlendPanel.COMPONENTS,
                                 newArr = new BlendComponent[oldArr.length-1];
                int oldI = 0;
                for(int i = 0; i < newArr.length; i++)
                {
                    if(oldArr[oldI] == select) oldI++;
                    newArr[i] = oldArr[oldI];
                    oldI++;
                }
                QuickBlendPanel.updateComponents(newArr);
                updateUI();
                fillFields();
                change = true;
            }
            else if(event.getSource() == newBtn)
            {
                BlendComponent[] oldArr = QuickBlendPanel.COMPONENTS,
                                 newArr = new BlendComponent[oldArr.length+1];
                System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
                BlendComponent newComp = new BlendComponent("New",0,0,0,0,0,0);
                newArr[newArr.length-1] = newComp;
                Arrays.sort(newArr);
                QuickBlendPanel.updateComponents(newArr);
                updateUI();
                selectComponent(newComp);
                change = true;
            }
            else if(event.getSource() == saveBtn)
            {
                overwriteDB();
            }
            else if(event.getSource() == defBtn)
            {
                int choice = JOptionPane.showConfirmDialog(DatabaseEditor2.this, "This will erase any changes you may have made to the database.\nAre you sure you want to restore the default database?", 
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
                        JOptionPane.showMessageDialog(DatabaseEditor2.this, "Could not delete existing database.\nPlease delete it manually and try again.", 
                                "Could not delete.", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Main.extractResource("res/database.csv", file.getAbsolutePath());
                    if(file.exists()) 
                    {
                        JOptionPane.showMessageDialog(DatabaseEditor2.this, "Database successfully reset.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        BlendComponent[] def = BlendComponent.loadDatabase(file.getAbsolutePath());
                        QuickBlendPanel.updateComponents(def);
                        updateUI();
                        fillFields();
                    }
                    else JOptionPane.showMessageDialog(DatabaseEditor2.this, "Could not extract database.\nTry restarting the program.", "Failes", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    };
    private final FocusListener fieldFocus = new FocusListener()
    {
        @Override
        public void focusGained(FocusEvent event) 
        {
            if(event.getSource() instanceof JTextField) ((JTextField)event.getSource()).selectAll();
        }
        @Override
        public void focusLost(FocusEvent fe) {}
    };
}