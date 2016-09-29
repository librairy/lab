/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.portrayal.inspector;

/** 
	A Groupable object can have its properties automatically broken up under various
	disclosure groups in a GroupedInspector.  Properties 
*/
public interface Groupable
    {
    /** Returns a list of names for groups to appear in the TabbedInspector. */
    public String[] provideGroupNames();
    
    /** Returns, for each group, a list of names of Java Bean Properties of this object
        which should appear under that group. */
    public String[][] provideGroupProperties();
    
    /** Returns whether remaining properties (not appearing under provideGroupProperties)
    	should be displayed.  If true, then they will be inserted individually, ungrouped,
    	prior to any groups. */
    public boolean showExtraProperties();
    }
