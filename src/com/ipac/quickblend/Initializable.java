package com.ipac.quickblend;

import javax.swing.JInternalFrame;

/**
 * To be implemented by containers that need post-frame-creation initialization
 * @author Jonathan Crow
 */
public interface Initializable 
{
    /**
     * Called after the JInternalFrame is set up and made visible
     * @param frame the containing frame
     */
    public void init(JInternalFrame frame);
}