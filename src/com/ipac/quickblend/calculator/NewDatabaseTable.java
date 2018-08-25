package com.ipac.quickblend.calculator;

import com.ipac.quickblend.Main;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import net.miginfocom.swing.MigLayout;

public class NewDatabaseTable extends JTable
{
    private JButton saveBtn, addBtn, defBtn;//, sortBtn;
    private RXTable table;
    
    public NewDatabaseTable()
    {
        setLayout(new MigLayout("fill"));
        setBackground(new Color(214,217,223));
        String[] cols = {"Product", "Specific Gravity", "KV40", "KV100", "KV40 Multiplier", "KV100 Multiplier", "VC"};
        BlendComponent[] comps = QuickBlendPanel.COMPONENTS;
        DefaultTableModel model = new DefaultTableModel(0,0);
        model.setColumnIdentifiers(cols);
        Object[][] data = new Object[comps.length][cols.length];
        for(int row = 0; row < data.length; row++)
        {
            model.addRow(new Object[]
            {
                comps[row].getName(),
                comps[row].getSpecificGravity(),
                comps[row].getKv40(),
                comps[row].getKv100(),
                comps[row].getVm40(),
                comps[row].getVm100(),
                comps[row].getVc()
            });
        }
        table = new RXTable(data, cols);
        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectAllForEdit(true);
        table.setAutoCreateRowSorter(true);
        JScrollPane scroll = new JScrollPane(table);
        
        add(scroll, "newline, grow, wrap");
        add(saveBtn = initButton("Overwrite Database"), "split 2, center");
        add(addBtn = initButton("Add New Component"), "wrap");
        add(defBtn = initButton("Restore Default Database"), "center");
        //add(sortBtn = initButton("Sort Database"), "center, wrap");
        setPreferredSize(new Dimension(400,400));
    }
    private JButton initButton(String txt)
    {
        JButton btn = new JButton(txt);
        btn.addActionListener(btns);
        return btn;
    }
    private double getDoubleCell(TableModel model, int r, int c)
    {
        Object val = model.getValueAt(r, c);
        if(val instanceof Double) return (double)val;
        else if(val instanceof Integer) return (int)val;
        else if(val instanceof String) 
        {
            try
            {
                return Double.parseDouble((String)val);
            }
            catch(Exception err)
            {
                err.printStackTrace();
                return Double.NaN;
            }
        }
        System.err.println("Double cell class was " + val.getClass());
        return Double.NaN;
    }
    private boolean isValid(BlendComponent comp)
    {
        return !Double.isNaN(comp.getSpecificGravity()) && !Double.isNaN(comp.getKv40()) && !Double.isNaN(comp.getKv100()) &&
                !Double.isNaN(comp.getVm40()) && !Double.isNaN(comp.getVm100()) && !Double.isNaN(comp.getVc());
    }
    
    private final ActionListener btns = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent event)
        {
            if(event.getSource() == saveBtn)
            {
                TableModel model = table.getModel();
                ArrayList<BlendComponent> comps = new ArrayList<>(),
                        invalid = new ArrayList<>();
                for(int r = 0; r < table.getRowCount(); r++)
                {
                    try
                    {
                        String name = (String)model.getValueAt(r, 0);
                        double specGrav, kv40, kv100, vm40, vm100, vc;
                        specGrav = getDoubleCell(model, r, 1);
                        kv40 = getDoubleCell(model, r, 2);
                        kv100 = getDoubleCell(model, r, 3);
                        vm40 = getDoubleCell(model, r, 4);
                        vm100 = getDoubleCell(model, r, 5);
                        vc = getDoubleCell(model, r, 6);
                        BlendComponent comp = new BlendComponent(name, specGrav, kv40, kv100, vm40, vm100, vc);
                        if(isValid(comp)) comps.add(comp);
                        else invalid.add(comp);
                    }
                    catch(Exception err)
                    {
                        err.printStackTrace();
                    }
                }
                BlendComponent[] compsArr = comps.toArray(new BlendComponent[0]);
                Arrays.sort(compsArr);
                QuickBlendPanel.updateComponents(compsArr);
                BlendComponent.save(compsArr, QuickBlendPanel.DATABASE_FILE.getAbsolutePath());
                Main.updateUI();
                if(invalid.size() > 0)
                {
                    JPanel errPanel = new JPanel();
                    errPanel.setLayout(new MigLayout());
                    errPanel.add(new JLabel("The following components could no be interpretted:"),"wrap");
                    errPanel.add(new JScrollPane(new JList(invalid.toArray())), "wrap");
                    errPanel.add(new JLabel("These components were ignored."));
                    JOptionPane.showMessageDialog(NewDatabaseTable.this, errPanel, "Errors", JOptionPane.WARNING_MESSAGE);
                }
            }
            else if(event.getSource() == addBtn)
            {
                DefaultTableModel model = (DefaultTableModel)table.getModel();
                model.addRow(new Object[]{"",0,0,0,0,0,0});
                table.changeSelection(model.getRowCount()-1, 0, false, false);
            }
            else if(event.getSource() == defBtn)
            {
                int choice = JOptionPane.showConfirmDialog(NewDatabaseTable.this, "This will erase any changes you may have made to the database.\nAre you sure you want to restore the default database?", 
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
                        JOptionPane.showMessageDialog(NewDatabaseTable.this, "Could not delete existing database.\nPlease delete it manually and try again.", 
                                "Could not delete.", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Main.extractResource("res/database.csv", file.getAbsolutePath());
                    if(file.exists()) 
                    {
                        JOptionPane.showMessageDialog(NewDatabaseTable.this, "Database successfully reset.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        BlendComponent[] def = BlendComponent.loadDatabase(file.getAbsolutePath());
                        QuickBlendPanel.updateComponents(def);
                    }
                    else JOptionPane.showMessageDialog(NewDatabaseTable.this, "Could not extract database.\nTry restarting the program.", "Failes", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    };
}