/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.engine;

/** Stoppable objects can be prevented from being stepped any further by calling their stop() method. */

public interface Stoppable extends java.io.Serializable
    {
    public void stop();
    }
