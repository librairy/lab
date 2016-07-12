package ma;

import com.google.common.collect.ImmutableMap;
import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import lab.ma.Environment;
import lab.ma.EnvironmentGUI;
import lab.ma.domain.WordsDistribution;
import org.apache.spark.mllib.clustering.LDAModel;
import org.apache.spark.mllib.clustering.LocalLDAModel;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.modeler.lda.builder.OnlineLDABuilder;
import org.librairy.modeler.lda.helper.SparkHelper;
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

import java.io.*;
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
public class SimulatedTopicsMA {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedTopicsMA.class);

    List<WordsDistribution> topics = Arrays.asList(new WordsDistribution[]{
            new WordsDistribution("t1", ImmutableMap.of("a",0.7,"b",0.5,"c",0.3)),
            new WordsDistribution("t2",ImmutableMap.of("e",0.8,"b",0.4,"c",0.1)),
            new WordsDistribution("t3",ImmutableMap.of("f",0.6,"g",0.5,"h",0.4)),
            new WordsDistribution("t4",ImmutableMap.of("i",0.9,"j",0.3,"k",0.2))
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
