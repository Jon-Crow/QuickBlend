package com.ipac.quickblend;

/**
 * To be implemented by anything that needs to be updated each frame
 * @author Jonathan Crow
 */
public interface Updateable 
{
    /**
     * Called before each repaint
     * @param delta the amount of milliseconds since the last update
     */
    public void update(int delta);
}