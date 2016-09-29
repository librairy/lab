/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.mason.sim.portrayal.network;
import lab.ma.mason.sim.display.GUIState;
import lab.ma.mason.sim.portrayal.DrawInfo2D;
import lab.ma.mason.sim.portrayal.FieldPortrayal2D;

import java.awt.geom.*;

/**
   An extension of DrawInfo2D for dealing with edges in visualizing network fields.
*/

public class EdgeDrawInfo2D extends DrawInfo2D
    {
    /** A pre-scaled point to draw to. */
    public Point2D.Double secondPoint;
    
    public EdgeDrawInfo2D(GUIState state, FieldPortrayal2D fieldPortrayal, RectangularShape draw, RectangularShape clip, Point2D.Double secondPoint)
        {
        super(state, fieldPortrayal, draw,clip);
        this.secondPoint = secondPoint;
        }
                
    public EdgeDrawInfo2D(DrawInfo2D other, double translateX, double translateY, Point2D.Double secondPoint)
        {
        super(other, translateX, translateY);
        this.secondPoint = secondPoint;
        }

    public EdgeDrawInfo2D(DrawInfo2D other, Point2D.Double secondPoint)
        {
        super(other);
        this.secondPoint = secondPoint;
        }        

    public EdgeDrawInfo2D(EdgeDrawInfo2D other)
        {
        this(other, new Point2D.Double(other.secondPoint.x, other.secondPoint.y));
        }        

    public String toString() 
        {
        return "EdgeDrawInfo2D[ Draw: " + draw + " Clip: " + clip +  " Precise: " + precise + " Location : " + location + " 2nd: " + secondPoint + "]";
        }
    }
