/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.portrayal.inspector;
import lab.ma.mason.sim.display.GUIState;
import lab.ma.mason.sim.portrayal.Inspector;

public interface ProvidesInspector
    {
    /** Provides an inspector for this object.  The provided name should appear prominently,
        typically as a TitledBorder around the inspector. */
    public Inspector provideInspector(GUIState state, String name);
    }
