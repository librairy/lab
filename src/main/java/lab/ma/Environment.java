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
package lab.ma;

import lab.ma.domain.Agent;
import lab.ma.domain.TopicAgent;
import lab.ma.domain.WordsDistribution;
import lab.ma.mason.sim.engine.*;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lab.ma.mason.sim.field.continuous.Continuous2D;
import lab.ma.mason.sim.util.MutableDouble2D;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by cbadenes on 05/12/14.
 */
public class Environment extends SimState {

    private static final Logger LOG = LoggerFactory.getLogger(Environment.class);


    public double width             = 10.0;      // 20.0
    public double height            = width;      // 7.0
    public double agentRadius       = 0.1;     // 0.06
    public double minForce          = 0.01;  // 0.05
//    private double range            = agentRadius*10;  // 0.8, 3.5
    //private double range            = width;  // 0.8, 3.5
    private double range              =   Math.sqrt(1/minForce)*2;
    private double maxVelocity      = agentRadius/2;     // 0.06
    private int movementHistory     = 3;



    public MutableDouble2D center = new MutableDouble2D(width/2,height/2);

    public Continuous2D space;
    public Continuous2D area;

    public TopicAgent[] topicAgents;
    private List<WordsDistribution> topics;

    public static List<String> vocabulary;

    // Properties
    public double getRange() {
        return range;
    }
    public void setRange(double range) {
        this.range = range;
    }
    public double getMaxVelocity() {
        return maxVelocity;
    }
    public void setMaxVelocity(double maxVelocity) {
        this.maxVelocity = maxVelocity;
    }
    public int getMovementHistory() {
        return movementHistory;
    }
    public void setMovementHistory(int movementHistory) {
        this.movementHistory = movementHistory;
    }

    private UDM udm;

    public Environment(List<String> vocabulary, List<WordsDistribution> topics) {
        super(System.currentTimeMillis());
        initialize(topics.size());
        this.topics     = topics;
        this.vocabulary = vocabulary;
    }

    private void initialize(int seed){
        agentRadius = 0.1;
        movementHistory = 10;
        //maxVelocity = agentRadius/2;
        maxVelocity = agentRadius;
        minForce    = maxVelocity;
//        range       = Math.sqrt(1/minForce)*2;
        range       = Math.sqrt(1.0/Double.valueOf(seed))*2.0;

        LOG.info("Range :" + range);

        Double alpha        = 360.0/seed/2.0;
        Double sinAlpha     = Math.sin(Math.toRadians(alpha));

        width       = (range/2.0)/sinAlpha + range*10;
//        width       = (range/2.0)/sinAlpha ;
        height      = width;

        LOG.info("Dimension :" + width + "x"+ height);


    }

    public void setUdm(UDM udm){
        this.udm = udm;
    }

    private Agent initializeParticle(TopicAgent agent){
//        schedule.scheduleRepeating(Schedule.EPOCH, 1, new Steppable() {
//            public void step(SimState state) {
//                agent.stepUpdateRadiation();
//            }
//        });

        Stoppable s2 = schedule.scheduleRepeating(Schedule.EPOCH, 2, new Steppable() {
            public void step(SimState state) {
                agent.stepUpdateVelocity();
            }
        });
        agent.add(s2);


        Stoppable s3 = schedule.scheduleRepeating(Schedule.EPOCH, 3, new Steppable() {
            public void step(SimState state) {
                agent.stepUpdatePosition();
            }
        });
        agent.add(s3);

        return agent;
    }

    public void start() {
        super.start();

        space = new Continuous2D(0.01, width, height);
        area  = new Continuous2D(0.04, width, height); // radioactive particles

        int numTopics = topics.size();
        LOG.info("Initial Number of Topics: " + numTopics);


        int index = 0;
        topicAgents = new TopicAgent[numTopics];
        for (WordsDistribution topic: topics){
            LOG.info("adding topic: " + topic);
            topicAgents[index++] = (TopicAgent) initializeParticle(new TopicAgent(this,topic.getId(),topic.getWords()));
        }

    }


    public void build(){
//        System.setSecurityManager(new MySecurityManager());
        Environment environment = this;
        try{
            doLoop(new MakesSimState() {
                public SimState newInstance(long seed, String[] args) {
                    return environment;
                }

                public Class simulationClass() {
                    return environment.getClass();
                }
            }, new String[]{});
        } catch (SecurityException e){
        }
    }


    public void cluster(){

        List<Clusterable> points = Arrays.stream(topicAgents).map(topic -> topic.getPosition()).collect(Collectors.toList());

        int minPts = 0;
        double eps = range/2;
        LOG.info("MinPts:" + minPts + " | EPS:" + eps);
        DBSCANClusterer clusterer = new DBSCANClusterer(eps,minPts, new EuclideanDistance());

        LOG.info("Points: " + points);

        List<Cluster> clusters = clusterer.cluster(points);

        LOG.info("Number of clusters: " + clusters.size());

        clusters.forEach(cluster -> {
            LOG.info("> Points: "+cluster.getPoints());
        });

    }

}
