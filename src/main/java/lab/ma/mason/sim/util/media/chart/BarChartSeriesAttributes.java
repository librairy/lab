/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.util.media.chart;

// From JFreeChart
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.event.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.general.*;

/** 
    A SeriesAttributes used for user control of bar chart series created with BarChartGenerator.  Similar to PieChartSeriesAttributes.
*/


public class BarChartSeriesAttributes extends PieChartSeriesAttributes
    {
    /** Produces a PieChartSeriesAttributes object */
    public BarChartSeriesAttributes(ChartGenerator generator, String name, int index, SeriesChangeListener stoppable)  // , boolean includeMargin)
        { 
        super(generator, name, index, stoppable);
        }       
    }
