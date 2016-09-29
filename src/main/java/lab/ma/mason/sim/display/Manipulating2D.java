/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.display;
import lab.ma.mason.sim.portrayal.LocationWrapper;

/**
   Manipulating2D is a simple interface for user interface objects which can manipulate
   portrayals in certain ways.  The primary function of the Manipulating2D interface is
   to pass Display2D etc. through to SimplePortrayal2D subclasses which implement the 
   handleMouseEvent(...) method without providing them an explicit class like Display2D.
   Some of these classes (notably MovablePortrayal2D) need a few Display2D functions
   to do their dirty work, which is where the Manipulating interface comes in.  Eventually
   as we determine that more Display2D features need to be made available to certain
   SimplePortrayal2D subclasses, we may extend this interface.
*/

public interface Manipulating2D
    {
    /** Selects the following object, deselecting other selected objects. */
    public void performSelection(LocationWrapper wrapper);
    public void setMovingWrapper(LocationWrapper wrapper);
    }