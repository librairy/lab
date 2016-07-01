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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by cbadenes on 05/12/14.
 */
public class TopicAgent extends Agent {


    private static final Logger LOG = LoggerFactory.getLogger(TopicAgent.class);

    private Map<String,Double> words;

    public TopicAgent(Environment sim, String id, Map<String,Double> words) {
        super(
                sim.space.stx((sim.random.nextDouble() * sim.width) - (sim.width * 0.5)),
                sim.space.sty((sim.random.nextDouble() * sim.height) - (sim.height * 0.5)),
                (sim.random.nextDouble() % sim.getMaxVelocity()),
                (sim.random.nextDouble() % sim.getMaxVelocity()),
                sim,
                id,
                0.50,                            // 0.15 lambda response rate
                Type.TOPIC
        );
        sim.area.setObjectLocation(this,new Double2D(position));
        this.words = words;
    }

    @Override
    public Color getColor() {
//        if (this.mode.equals(Mode.BLOCKED)) return Color.white;
        float[] colors = new float[3];
        Color.RGBtoHSB(255,0,0,colors);
        return Color.getHSBColor(colors[0],colors[1],colors[2]);
    }


    @Override
    public void stepUpdateVelocity(){

        MutableDouble2D displacement = new MutableDouble2D(0.0,0.0);

        Bag neighbors = sim.area.getNeighborsWithinDistance(new Double2D(position), sim.getRange(), TOROIDAL);
//
//        java.util.List<TopicAgent> neighbors = Arrays.asList(sim.topicAgents);

        if (neighbors.size() > 1){
            Iterator iterator = neighbors.iterator();
            while(iterator.hasNext()){
                Agent agent = (Agent) iterator.next();

                Double2D relativeDisplacement = new Double2D(0.0,0.0);

                if ((agent.id.equals(this.id))) continue;

                if (agent instanceof TextAgent) continue;

                //force = calculateDisplacementBy(agent.position, commonBtw(words,((TopicAgent) agent).words)*distance(agent.position, this.position));
                //double force = -1/distance(agent.position, this.position);
                double force = calculateForceFrom((TopicAgent) agent);
//                if (force == 0.0) LOG.info("NO REPULSION");
                relativeDisplacement = calculateDisplacementBy(agent.position, force);
                displacement.addIn(relativeDisplacement);
            }
        }else{
            // random movement
//            LOG.info("Random movement: " + this);
//            MutableDouble2D endpoint = new MutableDouble2D();
//            MutableDouble2D movement = new MutableDouble2D();
//            do{
//                endpoint.setTo(this.position);
//                movement.setTo(randomMovement());
//                endpoint.addIn(movement);
//            }while(!moveFrom(endpoint, sim.getMaxVelocity()));
//            displacement.addIn(movement);
        }


        MutableDouble2D limitedVelocity = limitToMaxVelocity(displacement, sim.getMaxVelocity()*responseRate);
        this.velocity.setTo(limitedVelocity);
    }

    private double calculateForceFrom(TopicAgent agent){
        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.CEILING);
        double distance = agent.distance(agent.position,this.position);

        long common         = agent.words.keySet().stream().filter(word -> this.words.containsKey(word)).count();
        int size            = this.words.size();
        double minForce     = Double.valueOf(size)/sim.width;

        double attraction   = Math.max(minForce,common);
        double repulsion    = this.words.size() - common;

        //double attractiveForce  = Math.max(minForce, attraction / distance);
        double attractiveForce  = attraction / distance;
        double repulsiveForce   = repulsion / (Math.pow(distance,3.0));

        double force = attractiveForce - repulsiveForce;
        return Double.valueOf(df.format(force));
    }


    public void stepUpdatePosition(){

        MutableDouble2D startingPoint = this.position.dup();

        if (velocity.length() > 0 ){

            // Move
            position.addIn(velocity);

            // Adjust to toroidal space
            this.position.x = sim.space.stx(position.x);
            this.position.y = sim.space.sty(position.y);


            // Avoid collision moving backward
            Bag neighbours = sim.space.getNeighborsExactlyWithinDistance(new Double2D(this.position), sim.agentRadius*2, TOROIDAL);
            Double2D backward = new Double2D(-this.velocity.x/10,-this.velocity.y/10);
            while (neighbours.size() > 0){
                this.position.addIn(randomMovement());
                if (neighbours.contains(this) && neighbours.size()==1) break;
                neighbours = sim.space.getNeighborsExactlyWithinDistance(new Double2D(this.position), sim.agentRadius*2, TOROIDAL);
            }
        }



        // Maintain a maximum distance to radioactive particle:: Gas Particle Model
        //double distance = (this.source != null)? distance(this.position, this.source.position) : 0.0 ;
//        if ((distance > sim.radiationRadius)
//                && (distance > sim.radiationRadius*this.source.attached)
//                && (distance < (this.source.attached*sim.radiationRadius+sim.getMaxVelocity()))
//                && (source.velocity.length() <= 0.0)
//                ){
//            // undo movement
//            this.position = startingPoint;
//            // random movement
//            this.velocity = randomMovement();
//            // evaluate
//            stepUpdatePosition();
//            return;
//        }

        sim.space.setObjectLocation(this, new Double2D(position));
        this.lastMovements.add(new Double2D(this.position));
        this.velocity.setTo(0.0,0.0);
    }


//    @Override
//    public void stepUpdateRadiation() {
//        if (this.mode.equals(Mode.RADIATE)){
//            // Update radiation based on neighbours
//            this.intensity = sim.getRadiationIntensity();
//            Bag neighbors = sim.space.getNeighborsExactlyWithinDistance(new Double2D(position), sim.radiationRadius,true);
//            Iterator iterator = neighbors.iterator();
//            while(iterator.hasNext()){
//                Agent agent = (Agent) iterator.next();
//                if (agent.id.equals(this.id)) continue;
////                if (agent instanceof DocumentAgent){
////                    DocumentAgent insulating = (DocumentAgent) agent;
////                    this.intensity -= ((insulating.source != null) && (insulating.source.id.equals(this.id)))?  Math.abs(agent.intensity) : 0 ;
////                }
//            }
//            if (this.intensity <= 0){
//                // Change to BLOCKED Mode
//                this.mode = Mode.BLOCKED;
//            }
//        }
//    }


}
