/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.portrayal.inspector;

public interface TabbableAndGroupable
    {
    /** Returns a list of names for tabs to appear in the TabbedInspector. */
    public String[] provideTabNames();
    
    /** Returns, for each tab and each group, a list of names of Java Bean Properties of this object
        which should appear under that tab and group. */
    public String[][][] provideTabGroupProperties();

    /** Returns, for each tab, a list of names of groups which should appear under that tab.
        The number of groups per tab must be equal to, or one less than, the number of tab 
        properties per tab as provided in provideTabProperties().  If there is one less
        group, then the remaining tab properties will be inserted inline.  */
    public String[][] provideTabGroups();    
    
    /** Returns a name for an additional tab holding the remaining Java Bean Properties
        not given in provideValues(), or null if no such tab should be displayed. */
    public String provideExtraTab();
    }
