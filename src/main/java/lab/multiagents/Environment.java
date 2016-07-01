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
package lab.multiagents;

import com.google.common.collect.ImmutableMap;
import lab.multiagents.domain.Agent;
import lab.multiagents.domain.TextAgent;
import lab.multiagents.domain.TopicAgent;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.MutableDouble2D;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by cbadenes on 05/12/14.
 */
public class Environment extends SimState {

    private static final Logger LOG = LoggerFactory.getLogger(Environment.class);

    public double width             = 20.0;      // 7.0
    public double height            = width;      // 7.0
    //public double agentRadius       = 0.06;     // 0.06
    public double agentRadius       = 0.1;     // 0.06

    public MutableDouble2D center = new MutableDouble2D(width/2,height/2);

    public Continuous2D space;
    public Continuous2D area;

    public TopicAgent[] topicAgents;
    public TextAgent[] textAgents;


//    private double range            = agentRadius*10;  // 0.8, 3.5
    private double range            = width;  // 0.8, 3.5
    private double maxVelocity      = agentRadius/2;     // 0.06

    private int movementHistory     = 3;

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

    public Environment(long seed) {
        super(seed);
    }

    public void setUdm(UDM udm){
        this.udm = udm;
    }

    private Agent initializeParticle(final Agent agent){
//        schedule.scheduleRepeating(Schedule.EPOCH, 1, new Steppable() {
//            public void step(SimState state) {
//                agent.stepUpdateRadiation();
//            }
//        });

        schedule.scheduleRepeating(Schedule.EPOCH, 2, new Steppable() {
            public void step(SimState state) {
                agent.stepUpdateVelocity();
            }
        });

        schedule.scheduleRepeating(Schedule.EPOCH, 3, new Steppable() {
            public void step(SimState state) {
                agent.stepUpdatePosition();
            }
        });


        return agent;
    }

    public void start() {
        super.start();

        space = new Continuous2D(0.01, width, height);
        area  = new Continuous2D(0.04, width, height); // radioactive particles

        // Load topics from DB

        LOG.info("reading relations to topic...");

        HashMap<String,TopicAgent> topics       = new HashMap<String, TopicAgent>();
        HashMap<String,TextAgent> documents = new HashMap<String, TextAgent>();

        topics.put("t1",new TopicAgent(this, "t1", ImmutableMap.of("a",0.1,"b",0.1,"c",0.1)));

        topics.put("t2",new TopicAgent(this, "t2", ImmutableMap.of("e",0.1,"b",0.1,"c",0.1)));

        topics.put("t3",new TopicAgent(this, "t3", ImmutableMap.of("f",0.1,"g",0.1,"h",0.1)));

        topics.put("t4",new TopicAgent(this, "t4", ImmutableMap.of("i",0.1,"j",0.1,"k",0.1)));



//        DocumentAgent documentAgent = new DocumentAgent(this, "d1");
//        documentAgent.addWeight("t1",0.5);
//        documentAgent.addWeight("t2",0.5);
//        documents.put("d1",documentAgent);
//
//        DocumentAgent documentAgent2 = new DocumentAgent(this, "d2");
//        documentAgent2.addWeight("t1",0.8);
//        documentAgent2.addWeight("t2",0.2);
//        documents.put("d2",documentAgent2);
//
//        DocumentAgent documentAgent3 = new DocumentAgent(this, "d3");
//        documentAgent3.addWeight("t1",0.2);
//        documentAgent3.addWeight("t2",0.8);
//        documents.put("d3",documentAgent3);


        int numTopics = topics.keySet().size();
//        int numDocuments = documents.keySet().size();

        LOG.info("NumTopics: " + numTopics);
//        LOG.info("NumDocuments: " + numDocuments);

        int index = 0;
        topicAgents = new TopicAgent[numTopics];
        for (String key: topics.keySet()){
            LOG.info("added topic: " + topics.get(key));
            topicAgents[index++] = (TopicAgent) initializeParticle(topics.get(key));
        }

//        index = 0;
//        textAgents = new TextAgent[numDocuments];
//        for (String key: documents.keySet()){
//            LOG.info("added document: " + documents.get(key));
//            textAgents[index++] = (TextAgent) initializeParticle(documents.get(key));
//        }

    }

    public static void main(String[] args) {
        doLoop(Environment.class, args);
        System.exit(0);
    }

}
