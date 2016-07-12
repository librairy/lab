package fixing;

import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.relations.Relationship;
import org.librairy.model.domain.relations.SimilarTo;
import org.librairy.model.domain.resources.Resource;
import org.librairy.modeler.lda.models.similarity.RelationalSimilarity;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import scala.Tuple2;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 04/07/16:
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
public class SimilarityFixer {


    private static final Logger LOG = LoggerFactory.getLogger(SimilarityFixer.class);

    private JavaSparkContext sc;

    @Autowired
    UDM udm;


    public void setup(){
        int processors = Runtime.getRuntime().availableProcessors();
        int mb = 1024*1024;
        long maxMemory = Runtime.getRuntime().maxMemory();
        String memPerProcess = (maxMemory / mb / processors) + "m";

        // Initialize Spark Context
        SparkConf conf = new SparkConf().
                setMaster("local[4]").
                setAppName("librairy-LDA-Modeler").
                set("spark.executor.memory", memPerProcess).
                set("spark.driver.maxResultSize", "0");

        LOG.info("Spark configured with " +  processors + " processors and " +  memPerProcess+"m per process");

        this.sc = new JavaSparkContext(conf);
    }

    @Test
    public void calculate(){

        LOG.info("Ready to calculate similarities..");
        setup();

        String domainUri = "http://drinventor.eu/domains/4f56ab24bb6d815a48b8968a3b157470";

        LOG.info("Getting parts from domain: " + domainUri);
        List<String> parts = udm.find(Resource.Type.PART).from(Resource.Type.DOMAIN, domainUri);

        LOG.info("Combining pairs of parts..");
        calculateSimilaritiesBetweenParts(parts,domainUri);

    }

    private void calculateSimilaritiesBetweenParts(List<String> parts, String domainUri){


        JavaRDD<String> urisRDD = this.sc.parallelize(parts);

        List<Tuple2<String, String>> pairs = urisRDD.cartesian(urisRDD)
                .filter(x -> x._1().compareTo(x._2()) > 0)
                .collect();

        LOG.info("Calculating similarities...");
        pairs.parallelStream().forEach( pair -> {

            List<Relationship> p1 = udm.find(Relation.Type.DEALS_WITH_FROM_PART).from(Resource.Type
                    .PART, pair._1).stream().map(rel -> new Relationship(rel.getEndUri(), rel.getWeight())).collect
                    (Collectors.toList());
            List<Relationship> p2 = udm.find(Relation.Type.DEALS_WITH_FROM_PART).from(Resource.Type
                    .PART, pair._2).stream().map(rel -> new Relationship(rel.getEndUri(), rel.getWeight())).collect
                    (Collectors.toList());

            Double similarity = RelationalSimilarity.between(p1, p2);

            LOG.info("Attaching SIMILAR_TO (PART) based on " + pair);
            SimilarTo simRel1 = Relation.newSimilarToParts(pair._1, pair._2);
            SimilarTo simRel2 = Relation.newSimilarToParts(pair._2, pair._1);

            simRel1.setWeight(similarity);
            simRel1.setDomain(domainUri);
            udm.save(simRel1);

            simRel2.setWeight(similarity);
            simRel2.setDomain(domainUri);
            udm.save(simRel2);

        });

    }

}
