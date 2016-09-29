/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.portrayal.inspector;

/** 
	A Tabbable object can have its properties automatically broken up under various
	tabs in a TabbedInspector.
*/

public interface Tabbable
    {
    /** Returns a list of names for tabs to appear in the TabbedInspector. */
    public String[] provideTabNames();
    
    /** Returns, for each tab, a list of names of Java Bean Properties of this object
        which should appear under that tab. */
    public String[][] provideTabProperties();
    
    /** Returns a name for an additional tab holding the remaining Java Bean Properties
        not given in provideValues(), or null if no such tab should be displayed. */
    public String provideExtraTab();
    }
