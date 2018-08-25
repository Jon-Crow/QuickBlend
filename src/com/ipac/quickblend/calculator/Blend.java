package com.ipac.quickblend.calculator;

import com.ipac.quickblend.Main;
import java.io.File;
import java.io.FileOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

@XmlRootElement
public class Blend 
{
    public static final int PERCENT_TYPE_WEIGHT = 1, PERCENT_TYPE_VOLUME = 2;
    @XmlElement(name="entry")
    private Entry[] entries;
    @XmlAttribute
    private int percType;
    
    private Blend()
    {}
    public Blend(int size, int percType)
    {
        entries = new Entry[size];
        this.percType = percType;
    }
    public int getPercentType()
    {
        return percType;
    }
    public int getSize()
    {
        return entries.length;
    }
    public void setEntry(int i, BlendComponent comp, double perc)
    {
        if(i >= 0 && i < entries.length) entries[i] = new Entry(comp, perc, percType);
    }
    public BlendComponent getComponent(int i)
    {
        if(i >= 0 && i < entries.length) return entries[i].comp;
        return null;
    }
    public double getWeightPercent(int i)
    {
        if(i >= 0 && i < entries.length) return entries[i].weightP;
        return Double.NaN;
    }
    public double getVolumePercent(int i)
    {
        if(i >= 0 && i < entries.length) return entries[i].volumeP;
        return Double.NaN;
    }
    public Results blend()
    {
        return new Results(this);
    }
    public void save(String path)
    {
        try
        {
            JAXBContext jaxb = JAXBContext.newInstance(Blend.class,Entry.class,BlendComponent.class);
            Marshaller mar = jaxb.createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            mar.marshal(this, new File(path));
        }
        catch(Exception error)
        {
            Main.logError("Could not save blend to xml.", error);
        }
    }
    
    @XmlRootElement
    public static class Entry
    {
        @XmlElement
        private BlendComponent comp;
        @XmlElement
        private double weightP, volumeP;
        
        private Entry()
        {}
        private Entry(BlendComponent comp, double perc, int percType)
        {
            this.comp = comp;
            if(percType == PERCENT_TYPE_WEIGHT) weightP = perc;
            else if(percType == PERCENT_TYPE_VOLUME) volumeP = perc;
        }
    }
    public static class Results
    {
        private BlendComponent[] comps;
        private double[] weightPs, volumePs;
        private double lbGal, gMl, kv40, kv100, ccs15, ccs20, ccs25, ccs30;
        
        private Results()
        {}
        private Results(Blend blend)
        {
            comps = new BlendComponent[blend.getSize()];
            for(int i = 0; i < blend.getSize(); i++)
                comps[i] = blend.getComponent(i);
            weightPs = new double[blend.getSize()];
            volumePs = new double[blend.getSize()];
            if(blend.percType == PERCENT_TYPE_WEIGHT)
            {
                for(int i = 0; i < blend.getSize(); i++)
                    lbGal += blend.getWeightPercent(i)/comps[i].getDensity();
                lbGal = 1/lbGal;
                double totalVP = 0;
                for(int i = 0; i < volumePs.length; i++)
                {
                    weightPs[i] = blend.getWeightPercent(i);
                    volumePs[i] = weightPs[i] * lbGal/comps[i].getDensity();
                    totalVP += volumePs[i];
                }
            }
            else if(blend.percType == PERCENT_TYPE_VOLUME)
            {
                for(int i = 0; i < blend.getSize(); i++)
                    lbGal += blend.getVolumePercent(i)*comps[i].getDensity();
                for(int i = 0; i < blend.getSize(); i++)
                {
                    volumePs[i] = blend.getVolumePercent(i);
                    weightPs[i] = volumePs[i] * comps[i].getDensity()/lbGal;
                }
            }
            gMl = lbGal/8.33;
            for(int i = 0; i < blend.getSize(); i++)
            {
                kv40 += Math.log(comps[i].getKv40()*comps[i].getVm40()) * weightPs[i];
                kv100 += Math.log(comps[i].getKv100()*comps[i].getVm100()) * weightPs[i];
            }
            kv40 = Math.exp(kv40);
            kv100 = Math.exp(kv100);
            ccs15 = getCCS(blend, 15);
            ccs20 = getCCS(blend, 20);
            ccs25 = getCCS(blend, 25);
            ccs30 = getCCS(blend, 30);
        }
        private double getCCS(Blend blend, int temp)
        {
            double ccs = 0;
            for(int i = 0; i < comps.length; i++)
                ccs += volumePs[i] * Math.log(comps[i].getCCS(temp));
            return Math.round(Math.exp(ccs));
        }
        public BlendComponent[] getComponents()
        {
            return comps;
        }
        public double[] getWeightPercents()
        {
            return weightPs;
        }
        public double[] getVolumePercents()
        {
            return volumePs;
        }
        public double getPoundsPerGallon()
        {
            return lbGal;
        }
        public double getGramsPerMilliliter()
        {
            return gMl;
        }
        public double getKV40()
        {
            return kv40;
        }
        public double getKV100()
        {
            return kv100;
        }
        public double getCCS15()
        {
            return ccs15;
        }
        public double getCCS20()
        {
            return ccs20;
        }
        public double getCCS25()
        {
            return ccs25;
        }
        public double getCCS30()
        {
            return ccs30;
        }
        private void createDataRow(HSSFSheet sheet, int r, String name, double val)
        {
            HSSFRow row = sheet.createRow(r);
            row.createCell(0).setCellValue(name);
            row.createCell(1).setCellValue(val);
        }
        public void save(String path)
        {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Blend");
            sheet.createRow(0).createCell(0).setCellValue("Components:");
            HSSFRow infoRow = sheet.createRow(1);
            infoRow.createCell(0).setCellValue("Name");
            infoRow.createCell(1).setCellValue("Specific-gravity");
            infoRow.createCell(2).setCellValue("kv40");
            infoRow.createCell(3).setCellValue("kv100");
            int r = 2;
            for(BlendComponent comp : comps)
            {
                HSSFRow row = sheet.createRow(r++);
                row.createCell(0).setCellValue(comp.getName());
                row.createCell(1).setCellValue(comp.getSpecificGravity());
                row.createCell(2).setCellValue(comp.getKv40());
                row.createCell(3).setCellValue(comp.getKv100());
            }
            sheet.createRow(r++);
            HSSFRow percInfoRow = sheet.createRow(r++);
            percInfoRow.createCell(0).setCellValue("Component");
            percInfoRow.createCell(1).setCellValue("Weight Percent");
            percInfoRow.createCell(2).setCellValue("Volume Percent");
            for(int i = 0; i < comps.length; i++)
            {
                HSSFRow row = sheet.createRow(r++);
                row.createCell(0).setCellValue(comps[i].getName());
                row.createCell(1).setCellValue(Main.round(weightPs[i]*100, 4));
                row.createCell(2).setCellValue(Main.round(volumePs[i]*100, 4));
            }
            createDataRow(sheet, r++, "Density(lb/gal)", Main.round(lbGal, 2));
            createDataRow(sheet, r++, "Density(g/mL)", Main.round(gMl, 4));
            createDataRow(sheet, r++, "kv40", Main.round(kv40, 1));
            createDataRow(sheet, r++, "kv100", Main.round(kv100, 1));
            createDataRow(sheet, r++, "CCS-15", Main.round(ccs15, 0));
            createDataRow(sheet, r++, "CCS-20", Main.round(ccs20, 0));
            createDataRow(sheet, r++, "CCS-25", Main.round(ccs25, 0));
            createDataRow(sheet, r++, "CCS-30", Main.round(ccs30, 0));
            FileOutputStream out = null;
            try
            {
                workbook.write((out = new FileOutputStream(path)));
                workbook.close();
            }
            catch(Exception error)
            {
                Main.logError("Could not save blend to spreadsheet.", error);
            }
            finally
            {
                if(out != null)
                {
                    try
                    {
                        out.close();
                    }
                    catch(Exception error)
                    {}
                }
            }
        }
    }
}