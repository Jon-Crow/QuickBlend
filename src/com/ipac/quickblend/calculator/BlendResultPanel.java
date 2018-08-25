package com.ipac.quickblend.calculator;

import com.ipac.quickblend.Main;
import com.ipac.quickblend.Saveable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.miginfocom.swing.MigLayout;

public class BlendResultPanel extends JPanel implements ActionListener, Saveable
{
    private Blend.Results res;
    private File lastSaveDir;
    private JButton saveBtn, copyBtn, imgBtn;
    private JScrollPane scroll;
    private JLabel discLbl;
    
    public BlendResultPanel()
    {}
    public BlendResultPanel(Blend.Results res)
    {
        setResults(res);
    }
    public final void setResults(Blend.Results res)
    {
        removeAll();
        this.res = res;
        setLayout(new MigLayout("fillx"));
        /*
        for(int i = 0; i < res.getComponents().length; i++)
        {
            addRow(res.getComponents()[i].getName(),"  Weight Percent: " + Main.roundToString(res.getWeightPercents()[i]*100, 4),"  Volume Percent: " + Main.roundToString(res.getVolumePercents()[i]*100, 4));
            //addRow("     Weight Percent: " + Main.roundToString(res.getWeightPercents()[i]*100, 2));
            //addRow("     Volume Percent: " + Main.roundToString(res.getVolumePercents()[i]*100, 2));
        }
        addRow("Density(lb/gal)", Main.roundToString(res.getPoundsPerGallon(), 2));
        addRow("Density(g/mL)", Main.roundToString(res.getGramsPerMilliliter(), 4));
        addRow("kv40",Main.roundToString(res.getKV40(), 1));
        addRow("kv100",Main.roundToString(res.getKV100(), 1));
        addRow("CCS-15",Main.roundToString(res.getCCS15(), 0));
        addRow("CCS-20",Main.roundToString(res.getCCS20(), 0));
        addRow("CCS-25",Main.roundToString(res.getCCS25(), 0));
        addRow("CCS-30",Main.roundToString(res.getCCS30(), 0));
        */
        
        Object[][] data = new Object[res.getComponents().length+9][3];
        int row = 0;
        for(; row < res.getComponents().length; row++)
        {
            data[row][0] = res.getComponents()[row].getName();
            data[row][1] = Main.round(res.getWeightPercents()[row]*100, 2);
            data[row][2] = Main.round(res.getVolumePercents()[row]*100, 2);
        }
        row++;
        data[row++] = new Object[]{"Density(lb/gal)", String.format("%.2f", res.getPoundsPerGallon()), null};
        data[row++] = new Object[]{"Density(g/mL)", String.format("%.2f", res.getGramsPerMilliliter()), null};
        data[row++] = new Object[]{"KV40", String.format("%.1f", res.getKV40()), null};
        data[row++] = new Object[]{"KV100", String.format("%.1f", res.getKV100()), null};
        data[row++] = new Object[]{"CCS-15", (int)Math.round(res.getCCS15()), null};
        data[row++] = new Object[]{"CCS-20", (int)Math.round(res.getCCS20()), null};
        data[row++] = new Object[]{"CCS-25", (int)Math.round(res.getCCS25()), null};
        data[row++] = new Object[]{"CCS-30", (int)Math.round(res.getCCS30()), null};
        
        JTable table = new JTable(data, new String[]{"Component","Weight %","Volume %"});
        table.setEnabled(false);
        scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(table.getPreferredSize().width*2,table.getRowHeight()*(data.length+2)+5));
        add(scroll, "span 3, grow, wrap");
        add(discLbl = new JLabel("DISCLAIMER: These results are estimates only. Actual results must be verified in a lab."),"span 3, center, wrap");
        lastSaveDir = new File(System.getProperty("user.home"));
        add((saveBtn = new JButton("Save to Spreadsheet")), "span 3, center, wrap");
        saveBtn.setIcon(UIManager.getIcon("FileChooser.floppyDriveIcon"));
        saveBtn.addActionListener(this);
        add((copyBtn = new JButton("Copy Excel Data to Clipboard")), "span 3, center, wrap");
        copyBtn.setIcon(Main.loadImageResource("res/img/icons/copy.png"));
        copyBtn.addActionListener(this);
        add((imgBtn = new JButton("Copy Screenshot to Clipboard")), "span 3, center");
        imgBtn.addActionListener(this);
    }
    private void addRow(String... lbls)
    {
        for(int i = 0; i < lbls.length-1; i++)
            add(new JLabel(lbls[i]));
        add(new JLabel(lbls[lbls.length-1]),"wrap");
    }
    @Override
    public void actionPerformed(ActionEvent event) 
    {
        if(event.getSource() == saveBtn)
        {
            JFileChooser fileChoose = new JFileChooser();
            fileChoose.setCurrentDirectory(lastSaveDir);
            fileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChoose.setMultiSelectionEnabled(false);
            fileChoose.setFileFilter(new FileNameExtensionFilter("excel files","xls","xls"));
            fileChoose.showSaveDialog(BlendResultPanel.this);
            File f = fileChoose.getSelectedFile();
            if(f != null)
            {
                int choice = 0;
                if(f.exists())
                {
                    choice = JOptionPane.showOptionDialog(BlendResultPanel.this, "That file already exists.\nWould you like to overwrite it?", "File Exists", JOptionPane.YES_NO_OPTION, 
                            JOptionPane.QUESTION_MESSAGE, null, new String[]{"Yes","No"}, 1);
                }
                if(choice == 0) 
                {
                    lastSaveDir = f.getParentFile();
                    String path = f.getAbsolutePath();
                    if(!path.endsWith(".xls")) path = path + ".xls";
                    res.save(path);
                }
            }
        }
        else if(event.getSource() == copyBtn)
        {
            String info = "";
            /*
            for(int i = 0; i < res.getComponents().length; i++)
                info += res.getComponents()[i].getName() + ":\tWeight Percent:\t" + Main.roundToString(res.getWeightPercents()[i]*100, 2) + "\tVolume Percent:\t" + Main.roundToString(res.getVolumePercents()[i]*100, 2) + "\n";
            */
            info += "Component\tWeight %\tVolume %\n";
            for(int i = 0; i < res.getComponents().length; i++)
                info += res.getComponents()[i].getName() + '\t' + Main.roundToString(res.getWeightPercents()[i]*100, 2) + '\t' + Main.roundToString(res.getVolumePercents()[i]*100, 2) + '\n';
            info += "\nDensity(lb/gal):\t" + Main.roundToString(res.getPoundsPerGallon(), 3) + "\n";
            info += "Density(g/mL):\t" + Main.roundToString(res.getGramsPerMilliliter(), 3) + "\n";
            info += "kv40:\t" + Main.roundToString(res.getKV40(), 3) + "\n";
            info += "kv100:\t" + Main.roundToString(res.getKV100(), 3) + "\n";
            info += "CCS-15:\t" + Main.roundToString(res.getCCS15(), 1) + "\n";
            info += "CCS-20:\t" + Main.roundToString(res.getCCS20(), 1) + "\n";
            info += "CCS-25:\t" + Main.roundToString(res.getCCS25(), 1) + "\n";
            info += "CCS-30:\t" + Main.roundToString(res.getCCS30(), 1) + "\n";
            info += "DISCLAIMER: These results are estimates only. Actual results must be verified in a lab.";
            try
            {
                StringSelection clip = new StringSelection(info);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clip, clip);
            }
            catch(Exception error)
            {
                Main.logError("Could not copy blend results to clipboard", error);
                JOptionPane.showMessageDialog(this, "Could not copy content to clipboard.", error.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        }
        else if(event.getSource() == imgBtn)
        {
            try
            {
                //Robot bot = new Robot();
                /*
                Point loc = scroll.getLocationOnScreen();
                BufferedImage screenCap = bot.createScreenCapture(new Rectangle(loc.x, loc.y, scroll.getWidth(), scroll.getHeight()));
                */
                BufferedImage img = new BufferedImage(getWidth(), discLbl.getY()+discLbl.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics g = img.createGraphics();
                Color bg = getBackground();
                setBackground(Color.WHITE);
                paint(g);
                setBackground(bg);
                g.dispose();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(img), null);
            }
            catch(Exception err)
            {
                err.printStackTrace();
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
    
    private class TransferableImage implements Transferable
    {
        private BufferedImage img;
        
        public TransferableImage(BufferedImage img)
        {
            this.img = img;
        }
        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }
        @Override
        public boolean isDataFlavorSupported(DataFlavor df)
        {
            return df == DataFlavor.imageFlavor;
        }
        @Override
        public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException
        {
            if(df == DataFlavor.imageFlavor) return img;
            throw new UnsupportedFlavorException(df);
        }
    }
}