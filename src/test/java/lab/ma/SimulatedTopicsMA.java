/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma;

import com.google.common.collect.ImmutableMap;
import es.cbadenes.lab.test.IntegrationTest;
import lab.ma.domain.WordsDistribution;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created on 07/07/16:
 *
 * @author cbadenes
 */
@Category(IntegrationTest.class)
public class SimulatedTopicsMA {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedTopicsMA.class);

    List<WordsDistribution> topics = Arrays.asList(new WordsDistribution[]{
            new WordsDistribution("topic1", ImmutableMap.of("a",0.7,"b",0.5,"c",0.3)),
            new WordsDistribution("topic2",ImmutableMap.of("e",0.8,"b",0.4,"c",0.1)),
            new WordsDistribution("topic3",ImmutableMap.of("f",0.6,"g",0.5,"h",0.4)),
            new WordsDistribution("topic4",ImmutableMap.of("i",0.9,"j",0.3,"k",0.2)),
            new WordsDistribution("topic11", ImmutableMap.of("a",0.7,"b",0.5,"c",0.3)),
            new WordsDistribution("topic12",ImmutableMap.of("e",0.8,"b",0.4,"c",0.1)),
            new WordsDistribution("topic13",ImmutableMap.of("f",0.6,"g",0.5,"h",0.4)),
            new WordsDistribution("topic14",ImmutableMap.of("i",0.9,"j",0.3,"k",0.2))
    });


    List<String> vocab = topics.stream().flatMap(topic -> topic.getWords().keySet().stream()).distinct().collect
            (Collectors.toList());


    @Test
    public void simulate(){
        simulate(this.vocab,this.topics);

    }

    private void simulate(List<String> vocab, List<WordsDistribution> topics ){
        Environment environment = new Environment(vocab,topics);
        environment.build();
        environment.cluster();
    }

    @Test
    public void simulateGUI(){

        simulateGUI(this.vocab,this.topics);
    }

    private void simulateGUI(List<String> vocab, List<WordsDistribution> topics ){
        EnvironmentGUI gui = new EnvironmentGUI(vocab, topics);
        gui.createController();

        Environment environment = (Environment) gui.getSimulationInspectedObject();
//        environment.cluster();
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("executed!!!");
    }

}
