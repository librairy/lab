/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.util.gui;
import javax.swing.*;
import java.awt.*;

/** AbstractScrollable is a JPanel with basic implementation of the Scrollable interface,
    making possible simple anonymous subclasses. */
    
public abstract class AbstractScrollable extends JPanel implements Scrollable
    {
    public Dimension getPreferredScrollableViewportSize()
        {
        return getPreferredSize();
        }
        
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
        {
        // twentieth a page
        return (visibleRect.height / 20);
        }
        
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
        {
        // half of a page
        return (visibleRect.height/2);
        }
        
    public boolean getScrollableTracksViewportWidth()
        {
        return false;
        }
        
    public boolean getScrollableTracksViewportHeight()
        {
        return false;
        }
    }
