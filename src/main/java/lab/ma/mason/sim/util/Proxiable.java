/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.util;

/**
   A proxiable object is one which provides SimpleProperties with a proxy to
   stand in for it; that is, to have the proxy's properties inspected instead
   of the object itself. 
*/
    
public interface Proxiable
    {
    /** Returns the proxy object to query for Properties instead of me. */
    public Object propertiesProxy();
    }
