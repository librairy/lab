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
public class NearRealTopicsMA {

    private static final Logger LOG = LoggerFactory.getLogger(NearRealTopicsMA.class);

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
    public void fromCSV() throws FileNotFoundException {
        InputStream is = new FileInputStream(new File("src/main/resources/topic100.csv"));
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        List<WordsDistribution> topics = br.lines().map(line -> {
            String[] values = line.split(",");
            WordsDistribution t = new WordsDistribution(values[0]);
            t.add(values[1],Double.valueOf(values[2]));
            t.add(values[3],Double.valueOf(values[4]));
            t.add(values[5],Double.valueOf(values[6]));
            t.add(values[7],Double.valueOf(values[8]));
            t.add(values[9],Double.valueOf(values[10]));
            t.add(values[11],Double.valueOf(values[12]));
            t.add(values[13],Double.valueOf(values[14]));
            t.add(values[15],Double.valueOf(values[16]));
            t.add(values[17],Double.valueOf(values[18]));
            t.add(values[19],Double.valueOf(values[20]));
            return t;
        }).limit(25).collect(Collectors.toList());

        List<String> vocabulary = topics.stream().flatMap(topic -> topic.getWords().keySet().stream()).distinct().collect
                (Collectors.toList());

        simulateGUI(vocabulary,topics);



    }

}
