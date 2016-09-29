/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.portrayal;

/**
   An Orientable2D object can have its orientation changed in radians.
    
   <p>Objects which define the setOrientation2D(val) method can have their orientation changed
   by AdjustablePortrayal2D.
*/

public interface Orientable2D extends Oriented2D
    {
    public void setOrientation2D(double val);
    }
