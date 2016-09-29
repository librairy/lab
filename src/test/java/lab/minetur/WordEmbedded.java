/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.minetur;

import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.feature.Word2Vec;
import org.apache.spark.mllib.feature.Word2VecModel;
import org.apache.spark.mllib.linalg.Vector;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.computing.helper.SparkHelper;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 28/06/16:
 *
 * @author cbadenes
 */
@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BootConfig.class)
@TestPropertySource(properties = {
        "librairy.cassandra.contactpoints = wiig.dia.fi.upm.es",
        "librairy.cassandra.port = 5011",
        "librairy.cassandra.keyspace = research",
        "librairy.elasticsearch.contactpoints = wiig.dia.fi.upm.es",
        "librairy.elasticsearch.port = 5021",
        "librairy.neo4j.contactpoints = wiig.dia.fi.upm.es",
        "librairy.neo4j.port = 5030",
        "librairy.eventbus.host = wiig.dia.fi.upm.es",
        "librairy.eventbus.port = 5041",
})
public class WordEmbedded {

    private static final Logger LOG = LoggerFactory.getLogger(WordEmbedded.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;

    @Autowired
    SparkHelper sparkHelper;

    @Test
    public void create() throws IOException {

        createModel(uriGenerator.from(Resource.Type.DOMAIN, "2015"), sparkHelper);

    }


    private void createModel(String domainURI, SparkHelper sparkHelper) throws IOException {

        List<Item> items = udm.find(Resource.Type.ITEM).
                from(Resource.Type.DOMAIN, domainURI).
                parallelStream().
                map(res -> udm.read(Resource.Type.ITEM).byUri(res.getUri()).get().asItem()).collect(Collectors.toList());


        JavaRDD<List<String>> docs = sparkHelper.getContext().parallelize(items).map(item ->
                Arrays.asList(item.getTokens().split(" ")));

        Word2Vec word2vec = new Word2Vec()
                .setVectorSize(10)
                .setNumPartitions(5000)
                .setSeed(42L);


        Word2VecModel model = word2vec.fit(docs);

        String id = URIGenerator.retrieveId(domainURI);

        Path dir = Paths.get("out","w2vModel",id);

        Files.deleteIfExists(dir);
        Files.createDirectories(dir);

        LOG.info("Saving model to: " + dir.toString());
        model.save(sparkHelper.getContext().sc(),dir.toString());

    }


    private void loadModel(String domainURI, SparkHelper sparkHelper) throws IOException {

        String id = URIGenerator.retrieveId(domainURI);
        Path dir = Paths.get("out","w2vModel",id);

        LOG.info("Loading w2v from: " + dir.toString());
        Word2VecModel model = Word2VecModel.load(sparkHelper.getContext().sc(), dir.toString());

        String word = "image";
        Vector vector = model.transform("image");
        LOG.info("["+word+"] -> " + vector);

    }






}
