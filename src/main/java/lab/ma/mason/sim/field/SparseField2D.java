/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.field;
import lab.ma.mason.sim.util.Double2D;

public interface SparseField2D
    {
    /** Returns the width and height of the sparse field as a Double2D */
    public Double2D getDimensions();
        
    /** Returns the location of an object in the sparse field as a Double2D */
    public Double2D getObjectLocationAsDouble2D(Object obect);
    }