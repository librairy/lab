/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.portrayal.inspector;
import java.awt.*;

import lab.ma.mason.sim.display.ChartUtilities;
import lab.ma.mason.sim.display.GUIState;
import lab.ma.mason.sim.engine.Stoppable;
import lab.ma.mason.sim.util.Properties;
import lab.ma.mason.sim.util.media.chart.BarChartGenerator;
import lab.ma.mason.sim.util.media.chart.ChartGenerator;

import org.jfree.data.xy.*;
import org.jfree.data.general.*;

/** A property inspector which generates various bar charts of data.  The bar charts update in real-time as
    requested by the user.  Data properties for which
    the BarChartChartingPropertyInspector will operate include:
        
    <ul>
    <li>Any array of Objects
    </ul>
        
    <p>BarChartChartingPropertyInspector registers itself with the property menu option "Make Bar Chart".
*/

public class BarChartChartingPropertyInspector extends PieChartChartingPropertyInspector
    {
    protected boolean validChartGenerator(ChartGenerator generator) { return generator instanceof BarChartGenerator; }
        
    public static String name() { return "Make Bar Chart"; }
    public static Class[] types() 
        {
        return new Class[]
            {
            new Object[0].getClass(), java.util.Collection.class,
            ChartUtilities.ProvidesDoublesAndLabels.class,
            ChartUtilities.ProvidesObjects.class,
            ChartUtilities.ProvidesCollection.class,
            };
        }

    public BarChartChartingPropertyInspector(Properties properties, int index, Frame parent, final GUIState simulation)
        {
        super(properties,index,parent,simulation);
        }
    
    public BarChartChartingPropertyInspector(Properties properties, int index, final GUIState simulation, ChartGenerator generator)
        {
        super(properties, index, simulation, generator);
        }
    
    protected ChartGenerator createNewGenerator()
        {
        return new BarChartGenerator()
            {
            public void quit()
                {
                super.quit();
                Stoppable stopper = getStopper();
                if (stopper!=null) stopper.stop();

                // remove the chart from the GUIState's charts
                getCharts(simulation).remove(this);
                }
            };
        }
    }
