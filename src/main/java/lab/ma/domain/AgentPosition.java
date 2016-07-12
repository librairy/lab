package lab.ma.domain;

import org.apache.commons.math3.ml.clustering.Clusterable;
import lab.ma.mason.sim.util.Double2D;
import lab.ma.mason.sim.util.MutableDouble2D;

/**
 * Created on 07/07/16:
 *
 * @author cbadenes
 */
public class AgentPosition implements Clusterable {


    private final double[] point;

    public AgentPosition(Double2D location){
        this.point = new double[]{location.getX(),location.getY()};
    }

    public AgentPosition(MutableDouble2D location){
        this.point = new double[]{location.getX(),location.getY()};
    }


    @Override
    public double[] getPoint() {
        return this.point;
    }

    @Override
    public String toString() {
        if (point == null) return "[]";
        return "["+point[0]+","+point[1]+"]";
    }
}
