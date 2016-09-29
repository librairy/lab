/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

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
import lab.ma.distance.CommonWordsDistance;
import lab.ma.distance.KendallsTau;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lab.ma.mason.sim.engine.Stoppable;
import lab.ma.mason.sim.util.Bag;
import lab.ma.mason.sim.util.Double2D;
import lab.ma.mason.sim.util.MutableDouble2D;
import sun.management.resources.agent;

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by cbadenes on 05/12/14.
 */
public class TopicAgent extends Agent {

    private static final Logger LOG = LoggerFactory.getLogger(TopicAgent.class);

    public final List<String> words;
    public Map<String,Double> weightedWords;
    List<Stoppable> steps = new ArrayList<>();

    private Integer counter;
    private Integer iteration;

    public TopicAgent(Environment sim, String id, Map<String,Double> weightedWords) {
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
        this.counter = 0;
        this.iteration = 0;
        sim.area.setObjectLocation(this,new Double2D(position));
        this.weightedWords = weightedWords;
        this.words = weightedWords.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(entry ->
                entry.getKey()).collect(Collectors.toList());
    }

    public void add(Stoppable step){
        this.steps.add(step);
    }

    @Override
    public Color getColor() {
        Integer count = Integer.valueOf(StringUtils.substringAfterLast(id,"topic"));
        int index = count % Type.values().length;
        Type type = Type.values()[index];
        Color color = ColorFactory.colorOf(type);
        return color;
    }


    @Override
    public void stepUpdateVelocity(){
        MutableDouble2D displacement = new MutableDouble2D(0.0,0.0);

        boolean moved = false;

//        Bag neighbors = sim.area.getNeighborsWithinDistance(new Double2D(position), sim.getRange(), Environment.TOROIDAL);
        Bag neighbors = sim.space.getNeighborsWithinDistance(new Double2D(position), sim.getRange(), Environment
                .TOROIDAL, true, null);
//
//        java.util.List<TopicAgent> neighbors = Arrays.asList(sim.topicAgents);

        if (neighbors.size() > 0){
            Iterator iterator = neighbors.iterator();
            while(iterator.hasNext()){
                TopicAgent agent = (TopicAgent) iterator.next();

                Double2D relativeDisplacement = new Double2D(0.0,0.0);

                if ((agent.id.equals(this.id))) continue;

                // Direct Displacement
//                double force = calculateForceFrom((TopicAgent) agent);
//                if (force == 0.0) {
////                    LOG.info("NO REPULSION");
//                    continue;
//                }
//                relativeDisplacement = calculateDisplacementBy(agent.position, force);

                // Smart Displacement
                relativeDisplacement = agent.displacementFor(this);

                // Move
                displacement.addIn(relativeDisplacement);
                moved = true;
            }
        }
        else{
            // random movement
            MutableDouble2D endpoint = new MutableDouble2D();
            MutableDouble2D movement = new MutableDouble2D();
            int tries = 0;
            do{
                endpoint.setTo(this.position);
                movement.setTo(randomMovement());
                endpoint.addIn(movement);
                tries +=1;
                if (tries > 50){
                    LOG.info("No random movement[2]!");
                    movement = new MutableDouble2D(0.0,0.0);
                    break;
                }
            }while(!moveFrom(endpoint, sim.getMaxVelocity()));
            displacement.addIn(movement);
        }

        // Attraction to the center
        double attractionToCenter = sim.minForce / 20; // 10
        double distanceToCenter = this.position.distance(sim.center);
        Double2D displacementToCenter = calculateDisplacementBy(sim.center, attractionToCenter);
        displacement.addIn(displacementToCenter);
//            moved = true;

//        if (distanceToCenter > (sim.getRange()*2)){
//        }

        if (!moved){
            // random movement
            MutableDouble2D endpoint = new MutableDouble2D();
            MutableDouble2D movement = new MutableDouble2D();
            int tries = 0;
            do{
                endpoint.setTo(this.position);
                movement.setTo(randomMovement());
                endpoint.addIn(movement);
                tries +=1;
                if (tries > 50){
                    LOG.info("No random movement[1]!: ");
                    movement = new MutableDouble2D(0.0,0.0);
                    break;
                }
            }while(!moveFrom(endpoint, sim.getMaxVelocity()));
            displacement.addIn(movement);
        }


        MutableDouble2D limitedVelocity = limitToMaxVelocity(displacement, sim.getMaxVelocity()*responseRate);
        this.velocity.setTo(limitedVelocity);
    }

    public Double2D displacementFor(TopicAgent agent){
        MutableDouble2D refPosition = this.position.dup();
        double refForce = calculateForceFrom(agent);

//        Bag neighbors = sim.area.getNeighborsWithinDistance(new Double2D(position), sim.getRange(), Environment.TOROIDAL);
        Bag neighbors = sim.space.getNeighborsWithinDistance(new Double2D(position), sim.getRange(), Environment
                .TOROIDAL, true, null);

        if (neighbors.size()>0){
            Iterator iterator = neighbors.iterator();
            while(iterator.hasNext()){
                TopicAgent neighbor = (TopicAgent) iterator.next();
                if (!neighbor.id.equals(agent.id)){
                    Double2D relativeDisplacement = new Double2D(0.0,0.0);
                    double relativeForce = agent.calculateForceFrom(neighbor);
                    if (relativeForce > refForce){
                        refForce = relativeForce;
                        refPosition = agent.position;
                    }
                }
            }
        }

        return agent.calculateDisplacementBy(refPosition,refForce);
    }


    public double calculateForceFrom(TopicAgent agent){
        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.CEILING);

//        double correlation = KendallsTau.correlation(sim.vocabulary,this.weightedWords, agent.weightedWords);
        double correlation = CommonWordsDistance.correlation(this.words, agent.words);

        double distance     = agent.distance(agent.position,this.position);

        //double attraction   = Math.max(sim.minForce,correlation);
        double attraction   = correlation;
//        double attraction   = (distance > sim.agentRadius)?3*correlation : 0;


        double repulsion    = 1 - correlation;
//        double repulsion    = (distance < (sim.getRange()/4))? 1 - correlation : 0;

        //double attractiveForce  = Math.max(minForce, attraction / ma.distance);
//        double attractiveForce  = attraction * distance;
        double attractiveForce  = attraction / distance;

        double repulsiveForce   = repulsion / (Math.pow(distance,2));
//        double repulsiveForce   = repulsion / distance;

        double force = attractiveForce - repulsiveForce;
        return force;
    }


    public void stepUpdatePosition(){
        MutableDouble2D startingPoint = this.position.dup();

        if (velocity.length() > 0 ){

            // Move
            position.addIn(velocity);

            if (Environment.TOROIDAL){
                // Adjust to toroidal space
                this.position.x = sim.space.stx(position.x);
                this.position.y = sim.space.sty(position.y);
            }else{
                if (this.position.x > sim.width){
                    double w1 = this.position.x - sim.width;
                    this.position.x = this.position.x - (2*w1);
                }
                if (this.position.y > sim.height){
                    double w1 = this.position.y - sim.height;
                    this.position.y = this.position.y - (2*w1);
                }
                if (this.position.y < 0){
                    this.position.y = Math.abs(this.position.y);
                }
                if (this.position.x < 0){
                    this.position.x = Math.abs(this.position.x);
                }
            }


            // Avoid collision moving backward
//            Bag neighbours = sim.space.getNeighborsExactlyWithinDistance(new Double2D(this.position), sim
//                    .agentRadius*2, Environment.TOROIDAL);
//            Double2D backward = new Double2D(-this.velocity.x/10,-this.velocity.y/10);
//            MutableDouble2D originalPoint = this.position.dup();
//            while (neighbours.size() > 0){
//                if (neighbours.contains(this) && neighbours.size()==1) break;
//                originalPoint = this.position.dup();
//                originalPoint.addIn(randomMovement());
//                neighbours = sim.space.getNeighborsExactlyWithinDistance(new Double2D(originalPoint), sim
//                        .agentRadius*2, Environment.TOROIDAL);
//            }
//            this.position = originalPoint.dup();
        }


//        Double2D newLocation = new Double2D(round(position.x,Environment.ROUND_DECIMAL), round(position.y,Environment
//                .ROUND_DECIMAL));
        Double2D newLocation = new Double2D(position);

        //TODO Last 10 movements
        if (startingPoint.distance(newLocation) == 0){
            this.counter +=1;

            if (this.counter >= 10){
                stopAgent();
            }
        }else{
            this.counter = 0;
        }


        // Max Iterations
        this.iteration += 1;
        if (this.iteration >= sim.maxIterations){
            stopAgent();
        }

        sim.space.setObjectLocation(this, newLocation);
        this.lastMovements.add(newLocation);
        this.velocity.setTo(0.0,0.0);
    }

    private void stopAgent(){
        LOG.info("Stopping Agent: " + this);
        steps.forEach(step -> step.stop());
    }

    public AgentPosition getPosition(){
        return new AgentPosition(this.position);
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
