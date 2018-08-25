package com.ipac.quickblend.calculator;

import com.ipac.quickblend.Main;
import java.io.File;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Blends 
{
    @XmlElement
    private HashMap<String, Blend> blends = new HashMap<>();
    
    public String[] getBlendNames()
    {
        return blends.keySet().toArray(new String[0]);
    }
    public int getNumberOfBlends()
    {
        return blends.size();
    }
    public boolean blendExists(String name)
    {
        return blends.containsKey(name);
    }
    public Blend getBlend(String name)
    {
        return blends.get(name);
    }
    public void setBlend(String name, Blend blend)
    {
        blends.put(name, blend);
    }
    public void deleteBlend(String name)
    {
        blends.remove(name);
    }
    public void save(String path)
    {
        try
        {
            Main.logInfo("Saving blends");
            JAXBContext jaxb = JAXBContext.newInstance(Blends.class, Blend.class, Blend.Entry.class, BlendComponent.class);
            Marshaller mar = jaxb.createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            mar.marshal(this, new File(path));
        }
        catch(Exception error)
        {
            Main.logError("Could not save blends to xml.", error);
        }
    }
    public static Blends load(String path)
    {
        File f = new File(path);
        if(f.exists())
        {
            try
            {
                Main.logInfo("Loading blends.");
                JAXBContext jaxb = JAXBContext.newInstance(Blends.class, Blend.class, Blend.Entry.class, BlendComponent.class);
                return (Blends)jaxb.createUnmarshaller().unmarshal(f);
            }
            catch(Exception error)
            {
                Main.logError("Could not load blends from xml.", error);
            }
        }
        else Main.logWarning("No blends file was found. A new one will be created.");
        return new Blends();
    }
}