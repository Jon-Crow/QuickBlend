package com.ipac.quickblend.calculator;

import com.ipac.quickblend.Main;
import static com.ipac.quickblend.Main.logError;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JOptionPane;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import static com.ipac.quickblend.Main.logError;
import static com.ipac.quickblend.Main.logError;
import static com.ipac.quickblend.Main.logError;
import java.util.Collections;
import java.util.Stack;

/**
 * A container for the row data of the database spreadsheet
 * @author Jonathan Crow
 */
@XmlRootElement
public class BlendComponent implements Comparable<BlendComponent>
{
    private String name;
    private double specGrav, kv40, kv100, vm40, vm100, vc;
    
    private BlendComponent()
    {
        this("",0,0,0,0,0,0);
    }
    public BlendComponent(String name, double specGrav, double kv40, double kv100, double vm40, double vm100, double vc)
    {
        this.name = name;
        this.specGrav = specGrav;
        this.kv40 = kv40;
        this.kv100 = kv100;
        this.vm40 = vm40;
        this.vm100 = vm100;
        this.vc = vc;
    }
    public void set(BlendComponent comp)
    {
        name = comp.name;
        specGrav = comp.specGrav;
        kv40 = comp.kv40;
        kv100 = comp.kv100;
        vm40 = comp.vm40;
        vm100 = comp.vm100;
        vc = comp.vc;
    }
    @XmlAttribute
    public String getName() 
    {
        return name;
    }
    public void setName(String name) 
    {
        this.name = name;
    }
    @XmlElement
    public double getSpecificGravity() 
    {
        return specGrav;
    }
    public void setSpecificGravity(double specGrav) 
    {
        this.specGrav = specGrav;
    }
    @XmlElement
    public double getKv40() 
    {
        return kv40;
    }
    public void setKv40(double kv40) 
    {
        this.kv40 = kv40;
    }
    @XmlElement
    public double getKv100() 
    {
        return kv100;
    }
    public void setKv100(double kv100) 
    {
        this.kv100 = kv100;
    }
    @XmlElement
    public double getVm40() 
    {
        return vm40;
    }
    public void setVm40(double vm40) 
    {
        this.vm40 = vm40;
    }
    @XmlElement
    public double getVm100()
    {
        return vm100;
    }
    public void setVm100(double vm100)
    {
        this.vm100 = vm100;
    }
    @XmlElement
    public double getVc() 
    {
        return vc;
    }
    public void setVc(double vc) 
    {
        this.vc = vc;
    }
    public double getDensity()
    {
        return specGrav*8.337;
    }
    public double getCCS(int temp)
    {
        double logKV40 = Math.log10(Math.log10(kv40+vc)), logKV100 = Math.log10(Math.log10(kv100+vc+0.28)),
                slope = (logKV100-logKV40)/(Math.log10(373.0)-Math.log10(313.0)), yInt = logKV100 - slope*Math.log10(373.0);
        if(getName().equals("IPAC 6510"))
        {
            System.out.println("---CCS " + temp + "---");
            System.out.println("logkv40 = " + logKV40);
            System.out.println("logkv100 = " + logKV100);
            System.out.println("slop = " + slope);
            System.out.println("yInt = " + yInt);
        }
        return Math.round((Math.pow(10.0, Math.pow(10.0, slope*Math.log10(273-temp) + yInt)) - this.vc) * this.specGrav);
    }
    public double getCCS15()
    {
        double logkv100 = Math.log10(Math.log10(kv100+vc+.28)), logkv40 = Math.log10(Math.log10(kv40+vc));
        return Math.pow(10, Math.pow(10, (logkv100-logkv40)/(Math.log10(373)-Math.log10(313))));
    }
    @Override
    public String toString()
    {
        return isValid() ? name : ('*' + name + '*');
    }
    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof BlendComponent) return name.equals(((BlendComponent)obj).name);
        return false;
    }
    @Override
    public int compareTo(BlendComponent comp)
    {
        if(!isValid()) return -1;
        return name.toLowerCase().compareTo(comp.name.toLowerCase());
    }
    private boolean isValid(double num)
    {
        return Double.isFinite(num) && !Double.isNaN(num);
    }
    public boolean isValid()
    {
        return isValid(specGrav) && isValid(kv40) && isValid(kv100) && isValid(vm40) && isValid(vm100) && isValid(vc);
    }
    
    public static BlendComponent[] loadDatabase(String path)
    {
        return loadDatabase(path, 0);
    }
    private static double nextDouble(Scanner scan)
    {
        try
        {
            return Double.parseDouble(scan.next());
        }
        catch(Exception err)
        {}
        return Double.NaN;
    }
    public static BlendComponent[] loadDatabase(String path, int tries)
    {
        File db = new File(path);
        if(!db.exists())
        {
            Main.extractResource("res/database.csv", QuickBlendPanel.DATABASE_FILE.getAbsolutePath());
        }
        Scanner scan = null;
        ArrayList<BlendComponent> loaded = new ArrayList<>();
        BlendComponent load = null;
        try
        {
            scan = new Scanner(db);
            scan.useDelimiter(",|\n");
            while(scan.hasNext())
            {
                load = new BlendComponent();
                load.setName(scan.next());
                load.setSpecificGravity(nextDouble(scan));
                load.setKv40(nextDouble(scan));
                load.setKv100(nextDouble(scan));
                load.setVm40(nextDouble(scan));
                load.setVm100(nextDouble(scan));
                load.setVc(nextDouble(scan));
                loaded.add(load);
            }
        }
        catch(Exception err)
        {
            err.printStackTrace();
            if(load != null) logError("Problem loading component: " + load.getName());
            JOptionPane.showMessageDialog(null, "Error loading database file:\n" + err.getClass(), "Could not load database.", JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            if(scan != null) scan.close();
        }
        Collections.sort(loaded);
        return loaded.toArray(new BlendComponent[0]);
    }
    public static void save(BlendComponent[] comps, String path)
    {
        FileWriter write = null;
        try
        {
             write = new FileWriter(new File(path));
             for(BlendComponent comp : comps)
             {
                 write.write(comp.getName()+','+comp.getSpecificGravity()+','+comp.getKv40()+','+comp.getKv100()
                             +','+comp.getVm40()+','+comp.getVm100()+','+comp.getVc()+'\n');
             }
        }
        catch(Exception err)
        {
            err.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving database file:\n" + err.getMessage(), "Could not save database.", JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            if(write != null) 
            {
                try
                {
                    write.close();
                }
                catch(Exception err)
                {}
            }
        }
    }
}