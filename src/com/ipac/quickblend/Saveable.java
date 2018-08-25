package com.ipac.quickblend;

import java.util.HashMap;

/**
 * To be implemented by containers that will need to write extra data to be reopened properly after being loaded
 * @author Jon
 */
public interface Saveable
{
    /**
     * Place any additional data in the map and it will be saved
     * @param data the map
     */
    public void putData(HashMap<String, String> data);
    /**
     * Read the data that was previously placed in the map
     * @param data the map
     */
    public void loadData(HashMap<String, String> data);
}