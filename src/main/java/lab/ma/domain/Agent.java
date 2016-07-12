/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lab.ma.domain;

import lab.ma.Environment;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lab.ma.mason.sim.util.Double2D;
import lab.ma.mason.sim.util.MutableDouble2D;

import java.awt.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by cbadenes on 12/12/14.
 */
public abstract class Agent {


    public enum Type{
        TOPIC,DOCUMENT,ITEM,ABSTRACT, APPROACH, BACKGROUND, CHALLENGE, OUTCOME, FUTURE_WORK, SUMMARY;
    }


    private static final Logger LOG = LoggerFactory.getLogger(Agent.class);

    private final Type type;
    protected CircularFifoQueue lastMovements;

    protected static final boolean TOROIDAL = false;

    protected MutableDouble2D position = new MutableDouble2D();
    protected MutableDouble2D velocity = new MutableDouble2D();

    protected Environment sim;
    protected String id;
    protected double responseRate;

    protected Agent(double x, double y, double vx, double vy, Environment sim, String id, double responseRate, Agent.Type type) {
        this.id = id;
        this.sim = sim;
        this.position.setTo(x, y);
        this.velocity.setTo(vx, vy);
        this.lastMovements = new CircularFifoQueue(sim.getMovementHistory());
        this.responseRate = responseRate;
        this.type = type;
        sim.space.setObjectLocation(this,new Double2D(position));
    }

    public abstract void stepUpdatePosition();

    public abstract void stepUpdateVelocity();

    public Color getColor() {
        return ColorFactory.colorOf(type);
    }


    protected MutableDouble2D limitToMaxVelocity(MutableDouble2D displacement, double max){
        if ((Math.abs(displacement.getY()) < max)
                && (Math.abs(displacement.getX()) < max)) return displacement;

        double absX = Math.abs(displacement.x);
        double absY = Math.abs(displacement.y);

        double valueX = (displacement.x < 0)? -1 : 1;
        double valueY = (displacement.y < 0)? -1 : 1;

        if (absY >= absX){
            valueX *= (absX*max)/absY;
            valueY *= max;

        }else{
            valueY *= (absY*max)/absX;
            valueX *= max;
        }
        return new MutableDouble2D(valueX, valueY);
    }

    protected double distance (MutableDouble2D p1, MutableDouble2D p2){
        // Handle toroidal space
        return TOROIDAL? toroidalDistance(p1,p2) : euclideanDistance(p1,p2);
    }


    private double euclideanDistance(MutableDouble2D p1, MutableDouble2D p2){
        return p1.distance(p2);
    }

    private double toroidalDistance(MutableDouble2D p1, MutableDouble2D p2){
        // Handle toroidal space
        return Math.sqrt(Math.pow( Math.min( Math.abs(p1.x - p2.x),  sim.space.width - Math.abs(p1.x - p2.x)), 2) +
                Math.pow( Math.min( Math.abs(p1.y - p2.y),  sim.space.height - Math.abs(p1.y - p2.y)), 2));
    }

    protected MutableDouble2D randomMovement(){
        return limitToMaxVelocity(
                new MutableDouble2D(
                        (sim.width * 0.5 + sim.random.nextDouble()- 0.5) - (sim.width * 0.5 ),
                        (sim.height * 0.5 + sim.random.nextDouble() - 0.5) - ((sim.height * 0.5 ))
                ), sim.getMaxVelocity()
        );
//        return limitToMaxVelocity(
//                new MutableDouble2D(
//                        (sim.random.nextDouble() * sim.width) - (sim.width * 0.5),
//                        (sim.random.nextDouble() * sim.height) - (sim.height * 0.5)
//                ), sim.getMaxVelocity()
//        );
    }

    protected Double2D calculateDisplacementBy(MutableDouble2D position, double multiplier){
        return TOROIDAL? calculateToroidalDisplacementBy(position,multiplier) : calculateEuclideanDisplacementBy(position,multiplier);
//        return calculateToroidalDisplacementBy(position,multiplier);
    }

    protected Double2D calculateEuclideanDisplacementBy(MutableDouble2D position, double force){
        double x1 = this.position.x;
        double y1 = this.position.y;
        double x2 = position.x;
        double y2 = position.y;
        return new Double2D(force * (x2-x1), force * (y2-y1));
    }

    protected Double2D calculateToroidalDisplacementBy(MutableDouble2D position, double force){
        double x1 = this.position.x;
        double y1 = this.position.y;
        double x2 = position.x;
        double y2 = position.y;
        // Toroidal space
        double toroidalX = Math.abs(x2 - x1) < sim.space.width - Math.abs(x2 - x1)? force * (x2 - x1):force * (x1 -x2);
        double toroidalY = Math.abs(y2 - y1) < sim.space.height - Math.abs(y2 - y1)? force * (y2 - y1):force * (y1 -y2);
        return new Double2D(toroidalX,toroidalY);
    }

    protected boolean moveFrom(MutableDouble2D current, double distance){
        if (!this.lastMovements.isEmpty()){
            Iterator iterator = this.lastMovements.iterator();
            while(iterator.hasNext()){
                Double2D point = (Double2D) iterator.next();
                if (distance(new MutableDouble2D(point), current) < distance) return false;
            }
        }
        return true;
    }

    public static double commonBtw(List<String> l1, List<String> l2){

        double result =Math.abs(Double.valueOf(CollectionUtils.union(l1, l2).stream().distinct().count() - (l1.size() + l2.size())))/Double.valueOf(l1.size());
        return result;
    }

    public static void main(String[] args){
        List<String> l1 = Arrays.asList(new String[]{"a", "b", "c"});
        List<String> l2 = Arrays.asList(new String[]{"b", "a", "c"});
        System.out.println(commonBtw(l1,l2));
    }

}
