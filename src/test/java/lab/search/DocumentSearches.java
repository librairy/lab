/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.search;

import com.google.common.base.Strings;
import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import scala.Tuple2;

import java.io.IOException;
import java.util.*;

/**
 * Created on 28/06/16:
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
public class DocumentSearches {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentSearches.class);

    @Autowired
    UDM udm;

    @Test
    public void similarTo() throws IOException {

        LOG.info("Ready to build a summary");


        Integer top = 20;
        String docUri = "http://drinventor.eu/documents/2f96456d3480f466dca55329a740f6cf";

        Document doc = udm.read(Resource.Type.DOCUMENT).byUri(docUri).get().asDocument();
        LOG.info("Top+"+top+" Similar documents to: '"+ doc.getTitle()+"':");

        udm.find(Relation.Type.SIMILAR_TO_DOCUMENTS).from(Resource.Type.DOCUMENT,docUri)
                .stream()
                .map(relation ->
                        new Tuple2<Document,Double>(udm.read(Resource.Type.DOCUMENT).byUri(relation.getEndUri()).get().asDocument(),relation.getWeight()))
                .filter(tuple -> !Strings.isNullOrEmpty(tuple._1.getPublishedOn()) && !tuple._1.getPublishedOn()
                        .equalsIgnoreCase("none") &&
                        Integer.valueOf
                        (tuple._1.getPublishedOn())<2013)
                .sorted(new Comparator<Tuple2<Document, Double>>() {
                    @Override
                    public int compare(Tuple2<Document, Double> o1, Tuple2<Document, Double> o2) {
                        return -o1._2.compareTo(o2._2);
                    }
                })
                .limit(top)
                .forEach(tuple -> {
                    LOG.info(""+tuple._1.getUri() + "["+tuple._1.getPublishedOn()+"]("+tuple._2+")-"+tuple._1.getTitle());
                });

    }

}
