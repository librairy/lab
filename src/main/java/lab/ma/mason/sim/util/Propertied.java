/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.util;

/**
   A Propertied object is one which provides its own Properties rather than
   letting SimpleProperties scan the object statically.  This is generally
   rare and mostly used for dynamic objects or certain abstract classes. 
*/
    
public interface Propertied
    {
    /** Returns my own Properties. */
    public Properties properties();
    }
