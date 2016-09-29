/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.field;
import lab.ma.mason.sim.util.Double3D;

public interface SparseField3D
    {
    /** Returns the width and height of the sparse field as a Double3D */
    public Double3D getDimensions();
        
    /** Returns the location of an object in the sparse field as a Double3D */
    public Double3D getObjectLocationAsDouble3D(Object obect);
    }

