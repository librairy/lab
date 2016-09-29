/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma;

import com.google.common.collect.ImmutableMap;
import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import lab.ma.domain.WordsDistribution;
import org.apache.spark.mllib.clustering.LDAModel;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.computing.helper.SparkHelper;
import org.librairy.model.domain.resources.Resource;
import org.librairy.modeler.lda.builder.CorpusBuilder;
import org.librairy.modeler.lda.builder.LDABuilder;
import org.librairy.modeler.lda.models.Corpus;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.librairy.storage.system.column.repository.UnifiedColumnRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 07/07/16:
 *
 * @author cbadenes
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BootConfig.class)
@TestPropertySource(properties = {
        "librairy.cassandra.contactpoints = wiener.dia.fi.upm.es",
        "librairy.cassandra.port = 5011",
        "librairy.cassandra.keyspace = research",
        "librairy.elasticsearch.contactpoints = wiener.dia.fi.upm.es",
        "librairy.elasticsearch.port = 5021",
        "librairy.neo4j.contactpoints = wiener.dia.fi.upm.es",
        "librairy.neo4j.port = 5030",
        "librairy.eventbus.host = wiener.dia.fi.upm.es",
        "librairy.eventbus.port = 5041",
})
public class RealTopicsMA {

    private static final Logger LOG = LoggerFactory.getLogger(RealTopicsMA.class);

    List<WordsDistribution> topics = Arrays.asList(new WordsDistribution[]{
            new WordsDistribution("t1", ImmutableMap.of("a",0.7,"b",0.5,"c",0.3)),
            new WordsDistribution("t2",ImmutableMap.of("e",0.8,"b",0.4,"c",0.1)),
            new WordsDistribution("t3",ImmutableMap.of("f",0.6,"g",0.5,"h",0.4)),
            new WordsDistribution("t4",ImmutableMap.of("i",0.9,"j",0.3,"k",0.2))
    });

    List<String> vocab = topics.stream().flatMap(topic -> topic.getWords().keySet().stream()).distinct().collect
            (Collectors.toList());

    @Autowired
    UDM udm;

    @Autowired
    UnifiedColumnRepository columnRepository;

    @Autowired
    SparkHelper sparkHelper;

    @Autowired
    LDABuilder topicBuilder;

    @Autowired
    CorpusBuilder corpusBuilder;



    private void simulate(List<String> vocab, List<WordsDistribution> topics ){
        Environment environment = new Environment(vocab,topics);
        environment.build();
        environment.cluster();
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

    @Test
    public void real(){

        String domainUri = "http://drinventor.eu/domains/4f56ab24bb6d815a48b8968a3b157470";

        Integer vocabSize = 10000;

        Corpus corpus = corpusBuilder.build(domainUri, Resource.Type.ITEM);

        topicBuilder.build(corpus);

    }

    private List<WordsDistribution> retrieveTopics(LDAModel ldaModel, String[] vocabulary, Integer numWords){
        List<WordsDistribution> topics = new ArrayList<>();

        Tuple2<int[], double[]>[] topicIndices = ldaModel.describeTopics(numWords);

        int index = 0;
        for (Tuple2<int[], double[]> topicDistribution : topicIndices){
            WordsDistribution topic = new WordsDistribution("topic"+index++);

            int[] topicWords = topicDistribution._1;
            double[] weights = topicDistribution._2;

            for (int i=0; i< topicWords.length;i++){
                int wid = topicWords[i];
                String word     = vocabulary[wid];
                Double weight   = weights[i];
                topic.add(word,weight);
            }
            topics.add(topic);
        }

        return topics;
    }

}
