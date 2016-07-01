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
import org.librairy.model.domain.relations.Relationship;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

import java.util.*;

/**
 * Created by cbadenes on 12/12/14.
 */
public class TextAgent extends Agent {

//    protected TopicAgent source;


    private HashMap<String,Double> weights;

    private List<Relationship> relationships;


    public TextAgent(Environment sim, String id, Agent.Type type) {
        super(
                sim.space.stx((sim.random.nextDouble() * sim.width) - (sim.width * 0.5)),
                sim.space.sty((sim.random.nextDouble() * sim.height) - (sim.height * 0.5)),
                (sim.random.nextDouble() % sim.getMaxVelocity()),
                (sim.random.nextDouble() % sim.getMaxVelocity()),
                sim,
                id,
                1.0,   // lambda response rate
                type
        );

        this.weights = new HashMap<String, Double>();
        this.relationships = new ArrayList<Relationship>();
    }

    public void addWeight(String id, Double value){
        this.weights.put(id,value);
        Relationship relationship = new Relationship(id,value);
        relationships.add(relationship);
    }



    @Override
    public void stepUpdateVelocity(){
        this.velocity = new MutableDouble2D(0.0,0.0);
        MutableDouble2D displacement = new MutableDouble2D(0.0,0.0);

        //Bag neighbors = sim.space.getNeighborsExactlyWithinDistance(new Double2D(position), sim.radiationRadius);
        //Bag neighbors = sim.space.getNeighborsWithinDistance(new Double2D(position), sim.getRange());

        List<TopicAgent> neighbors = Arrays.asList(sim.topicAgents);

        if (neighbors.size() > 1){
            Iterator iterator = neighbors.iterator();
            while(iterator.hasNext()){
                Agent agent = (Agent) iterator.next();

                if (agent.id.equals(this.id)) continue;

                Double2D force = new Double2D(0.0,0.0);

                if ((agent.id.equals(this.id))) continue;

                if (agent instanceof TopicAgent){
                    // Far away from other topics

                    force = calculateDisplacementBy(agent.position, weights.get(agent.id)*distance(agent.position, this.position));
                }

                if (agent instanceof TextAgent){
                    // nothing to do

//                    Double similarity = RelationalSimilarity.between(this.relationships, ((DocumentAgent) agent).relationships);
//
//                    double weight = similarity - 0.5;
//
//                    double ref = (weight < 0)? (1/distance(agent.position, this.position))*weight : weight*(distance(agent.position, this.position));
//
//                    force = calculateDisplacementBy(agent.position, ref );
                }

                //if (distance(agent.position, this.position) <= sim.agentRadius) continue;

                displacement.addIn(force);

            }
        }
        if (displacement.length() == 0) {
            // random movement
            MutableDouble2D endpoint = new MutableDouble2D();
            MutableDouble2D movement = new MutableDouble2D();
            do{
                endpoint.setTo(this.position);
                movement.setTo(randomMovement());
                endpoint.addIn(movement);
            }while(!moveFrom(endpoint, sim.getMaxVelocity()));
            displacement.addIn(movement);

        }


        MutableDouble2D partialVelocity = limitToMaxVelocity(displacement, sim.getMaxVelocity()*responseRate);
        this.velocity.addIn(partialVelocity);
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
//    public void stepUpdatePosition(){
//
//        MutableDouble2D startingPoint = this.position.dup();
//
//        if (velocity.length() > 0 ){
//
//            // Move
//            position.addIn(velocity);
//
//            // Adjust to toroidal space
//            this.position.x = sim.space.stx(position.x);
//            this.position.y = sim.space.sty(position.y);
//
//            // Avoid collision moving backward
//            Bag neighbours = sim.space.getNeighborsExactlyWithinDistance(new Double2D(this.position), sim.agentRadius*10, true);
//            Double2D backward = new Double2D(-this.velocity.x/10,-this.velocity.y/10);
//            while (neighbours.size() > 0){
//                this.position.addIn(backward);
//                if (neighbours.contains(this) && neighbours.size()==1) break;
//                neighbours = sim.space.getNeighborsExactlyWithinDistance(new Double2D(this.position), sim.agentRadius*10, true);
//            }
//        }
//
//        // Maintain a maximum distance to radioactive particle:: Gas Particle Model
//        //double distance = (this.source != null)? distance(this.position, this.source.position) : 0.0 ;
////        if ((distance > sim.radiationRadius)
////                && (distance > sim.radiationRadius*this.source.attached)
////                && (distance < (this.source.attached*sim.radiationRadius+sim.getMaxVelocity()))
////                && (source.velocity.length() <= 0.0)
////                ){
////            // undo movement
////            this.position = startingPoint;
////            // random movement
////            this.velocity = randomMovement();
////            // evaluate
////            stepUpdatePosition();
////            return;
////        }
//
//        sim.space.setObjectLocation(this, new Double2D(position));
//        this.lastMovements.add(new Double2D(this.position));
//        this.velocity.setTo(0.0,0.0);
//    }


//    @Override
//    public void stepUpdateRadiation() {
//        // maintain intensity constant
//    }

//    private synchronized void attach(TopicAgent particle){
//        // Attach to radioactive particles within the radiation radius
//        if ((distance(particle.position, this.position) < sim.radiationRadius)){
//            this.source = particle;
//        }
//    }
}
