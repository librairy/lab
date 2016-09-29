/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.engine;

/** An interface for classes capable of creating SimState subclasses.
    Typically you wouldn't use this interface; but rather it is used
    internaly in the SimState.doLoop methods. */
        
public interface MakesSimState
    {
    /** Creates a SimState subclass with the given random number seed
        and command-line arguments passed into main(...). */
    public SimState newInstance(long seed, String[] args);
        
    /** Returns the class of the SimState subclass that will be generated. */
    public Class simulationClass();
    }
