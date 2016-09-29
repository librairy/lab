/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.engine;

/** Something that can be stepped */

public interface Steppable extends java.io.Serializable
    {
    public void step(SimState state);
    }
