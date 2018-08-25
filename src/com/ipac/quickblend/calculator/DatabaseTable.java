package com.ipac.quickblend.calculator;

import com.ipac.quickblend.Main;
import com.ipac.quickblend.Updateable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import net.miginfocom.swing.MigLayout;

/**
 * An editor for the database spreadsheet
 * @author Jonathan Crow
 */
public class DatabaseTable extends JPanel implements ActionListener, Updateable
{
    private JTable table;
    private JScrollPane scroll;
    private JButton saveBtn, addBtn;
    
    public DatabaseTable()
    {
        setLayout(new MigLayout("fill"));
        table = new JTable();
        table.setModel(new DatabaseModel(QuickBlendPanel.COMPONENTS));
        table.getColumn(table.getColumnName(0)).setMinWidth(125);
        table.getInputMap(JTable.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        table.getActionMap().put("delete", new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                if(table.getSelectedRow() != -1) 
                {
                    ((DatabaseModel)table.getModel()).deleteRow(table.getSelectedRow());
                    Main.updateUI();
                }
            }
        });
        //table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        add((scroll = new JScrollPane(table)),"wrap");
        add((addBtn = new JButton("New Component")), "center, wrap");
        addBtn.setIcon(UIManager.getIcon("FileChooser.fileIcon"));
        addBtn.addActionListener(this);
        add((saveBtn = new JButton("Overwrite Database")),"center");
        saveBtn.setIcon(UIManager.getIcon("FileChooser.floppyDriveIcon"));
        saveBtn.addActionListener(this);
    }
    @Override
    public void update(int delta) 
    {
        table.setPreferredScrollableViewportSize(scroll.getPreferredSize());
    }
    @Override
    public void actionPerformed(ActionEvent event) 
    {
        if(event.getSource() == saveBtn)
        {
            BlendComponent[] compsArr = ((DatabaseModel)table.getModel()).comps.toArray(new BlendComponent[0]);
            Arrays.sort(compsArr);
            QuickBlendPanel.updateComponents(compsArr);
            BlendComponent.save(compsArr, QuickBlendPanel.DATABASE_FILE.getAbsolutePath());
            Main.updateUI();
        }
        else if(event.getSource() == addBtn)
        {
            DatabaseModel dbModel = (DatabaseModel)table.getModel();
            BlendComponent comp = new BlendComponent("New Component",0,0,0,0,0,0);
            ArrayList<BlendComponent> comps = dbModel.comps;
            comps.add(0, comp);
            dbModel.sort();
            int index = comps.indexOf(comp);
            table.changeSelection(index, index, false, false);
        }
    }
    
    /**
     * an implementation of TableModel that uses an ArrayList of BlendComponents 
     */
    private static class DatabaseModel extends AbstractTableModel
    {
        private String[] colNames = {"Product", "Specific Gravity", "KV40", "KV100", "KV40 Multiplier", "KV100 Multiplier", "Vc"};
        private ArrayList<BlendComponent> comps = new ArrayList<>();
        
        private DatabaseModel(BlendComponent[] comps)
        {
            this.comps.addAll(Arrays.asList(comps));
        }
        /**
         * Gets the component at the provided row number
         * @param r the row number
         * @return the component at the provided row. null if out of bounds
         */
        private BlendComponent getComponentAt(int r)
        {
            if(r < comps.size() && r >= 0) return comps.get(r);
            return null;
        }
        /**
         * Removes the component at the provided row
         * @param r the row to delete
         */
        public void deleteRow(int r)
        {
            comps.remove(r);
        }
        public void sort()
        {
            Collections.sort(comps);
        }
        @Override
        public String getColumnName(int c)
        {
            return colNames[c];
        }
        @Override
        public int getRowCount()
        {
            return comps.size();
        }
        @Override
        public int getColumnCount()
        {
            return colNames.length;
        }
        @Override
        public Object getValueAt(int r, int c)
        {
            BlendComponent comp = comps.get(r);
            switch(c)
            {
                case 0:
                    return comp.getName();
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
                default:
                    return null;
            }
        }
        @Override
        public void setValueAt(Object val, int r, int c)
        {
            BlendComponent comp = comps.get(r);
            switch(c)
            {
                case 0:
                    comp.setName((String)val);
                    break;
                case 1:
                    comp.setSpecificGravity((double)val);
                    break;
                case 2:
                    comp.setKv40((double)val);
                    break;
                case 3:
                    comp.setKv100((double)val);
                    break;
                case 4:
                    comp.setVm40((double)val);
                    break;
                case 5:
                    comp.setVm100((double)val);
                    break;
                case 6:
                    comp.setVc((double)val);
                    break;
            }
            sort();
        }
        @Override
        public boolean isCellEditable(int r, int c)
        {
            return true;
        }
        @Override
        public Class getColumnClass(int c)
        {
            if(c == 0) return String.class;
            return Double.class;
        }
    }
}