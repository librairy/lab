/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.graphs;

import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import lab.graphs.domain.Graph;
import lab.graphs.domain.Link;
import lab.graphs.domain.Node;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.mllib.clustering.LocalLDAModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.computing.helper.SparkHelper;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.relations.Relationship;
import org.librairy.model.domain.resources.Resource;
import org.librairy.modeler.lda.builder.CorpusBuilder;
import org.librairy.modeler.lda.builder.LDABuilder;
import org.librairy.modeler.lda.builder.SimilarityBuilder;
import org.librairy.modeler.lda.functions.RowToPair;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import scala.Tuple2;
import scala.reflect.ClassTag$;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created on 12/07/16:
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
        "librairy.eventbus.host = local",
        "librairy.eventbus.port = 5041",
})
public class SimilarityGraph {

    private static final Logger LOG = LoggerFactory.getLogger(SimilarityGraph.class);

    @Autowired
    UDM udm;

    @Autowired
    SimilarityBuilder similarityBuilder;

    @Autowired
    SparkHelper sparkHelper;

    @Autowired
    LDABuilder ldaBuilder;

    @Autowired
    CorpusBuilder corpusBuilder;

    @Test
    public void create() throws IOException {


        LOG.info("Finding SIMILAR_TO relations..");
        List<Relation> rels =getRelationsFromNeo4j();

        Graph graph = new Graph();

        LOG.info("creating nodes..");
        List<Node> nodes = rels.parallelStream().flatMap(rel -> Arrays.asList(new String[]{rel.getStartUri(), rel.getEndUri()
        }).stream()).distinct().map(uri -> new Node(uri,1)).collect(Collectors.toList());
        graph.setNodes(nodes);

        LOG.info("creating links..");
        List<Link> links = rels.parallelStream().map(rel -> new Link(rel.getStartUri(), rel.getEndUri(), Double.valueOf(rel
                .getWeight()
                * 10).intValue())).collect(Collectors.toList());
        graph.setLinks(links);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(graph);

        LOG.info("writing as json..");
        FileWriter writer = new FileWriter("target/similarity-graph.json");
        writer.write(json);
        writer.close();

        LOG.info("writing as csv..");
        FileWriter writerCSV = new FileWriter("target/similarity-graph.csv");
        writerCSV.write("Source,Target,Weight\n");
        rels.forEach(rel -> {
            try {
                Resource rel1 = udm.find(Resource.Type.DOCUMENT).from(Resource.Type.ITEM,rel.getStartUri()).get(0);
                Resource rel2 = udm.find(Resource.Type.DOCUMENT).from(Resource.Type.ITEM,rel.getEndUri()).get(0);

                writerCSV.write(URIGenerator.retrieveId(rel1.getUri()) + "," + URIGenerator.retrieveId(rel2.getUri()) +
                        "," + rel
                        .getWeight()+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        writerCSV.close();


        LOG.info("Graph created at: target/similarity-graph.json");
    }

    private List<Relation> getRelationsFromNeo4j(){
        return udm.find(Relation.Type.SIMILAR_TO_DOCUMENTS).all().parallelStream().filter
                (rel -> rel.getWeight() > 0.5)
                .collect(Collectors.toList());
    }
}
