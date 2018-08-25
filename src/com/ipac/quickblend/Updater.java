package com.ipac.quickblend;

import com.ipac.quickblend.calculator.BlendComponent;
import com.ipac.quickblend.calculator.QuickBlendPanel;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Updater
{
    public static void updateDatabase()
    {
        File newDB = new File(Main.RUNTIME_DIR + "/update.csv");
        if(Main.downloadGoogleFile("14wVQ3j8IGjgW-g_ei-ZSGCS5-WMKxZ7e", newDB.getAbsolutePath()))
        {
            newDB.deleteOnExit();
            BlendComponent[] comps = BlendComponent.loadDatabase(newDB.getAbsolutePath());
            ArrayList<BlendComponent> old = new ArrayList<>(Arrays.asList(QuickBlendPanel.COMPONENTS));
            for(BlendComponent comp : comps)
            {
                BlendComponent found = null;
                for(BlendComponent check : QuickBlendPanel.COMPONENTS)
                {
                    if(check.getName().equalsIgnoreCase(comp.getName())) found = check;
                }
                if(found != null) found.set(comp);
                else old.add(comp);
            }
            Collections.sort(old);
            QuickBlendPanel.updateComponents(old.toArray(new BlendComponent[0]));
        }
    }
}
