/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma;

import org.junit.Test;

/**
 * Created on 08/07/16:
 *
 * @author cbadenes
 */
public class EnvironmentTest {

    @Test
    public void initialization(){
        int seed = 4;
        double agentRadius = 0.1;
        int movementHistory = 10;
        double maxVelocity = agentRadius/2;
        double minForce    = maxVelocity;
        //double range       = Math.sqrt(1/minForce)*2;
        double range       = movementHistory*maxVelocity;

        Double alphaDegrees = 360.0/(seed*2.0);
        Double sinAlpha     = Math.sin(Math.toRadians(alphaDegrees));


        double width = (range / 2.0) / sinAlpha + (range / 2.0);
        double height      = width;
        System.out.println("Dim: " + width + "x" + height);
    }

}
