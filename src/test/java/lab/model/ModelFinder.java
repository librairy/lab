/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.model;

import com.google.common.base.Strings;
import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.AppearedIn;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.relations.SimilarToDocuments;
import org.librairy.model.domain.resources.*;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.librairy.storage.system.graph.template.edges.DealsDocEdgeTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import scala.Tuple2;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
        "librairy.cassandra.contactpoints = wiener.dia.fi.upm.es",
        "librairy.cassandra.port = 5011",
        "librairy.cassandra.keyspace = research",
        "librairy.elasticsearch.contactpoints = wiener.dia.fi.upm.es",
        "librairy.elasticsearch.port = 5021",
        "librairy.neo4j.contactpoints = wiener.dia.fi.upm.es",
        "librairy.neo4j.port = 5030",
        //"librairy.eventbus.host = wiener.dia.fi.upm.es",
        "librairy.eventbus.host = localhost",
        "librairy.eventbus.port = 5041",
})
public class ModelFinder {

    private static final Logger LOG = LoggerFactory.getLogger(ModelFinder.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;


    @Autowired
    DealsDocEdgeTemplate template;

    @Test
    public void docsFromPart() throws IOException {

        LOG.info("Getting the number of documents grouped by year...");

        String partUri = "http://drinventor.eu/parts/3343eb2cb20bec30664b2dc13aac7390";

        List<Document> docs = udm.find(Resource.Type.DOCUMENT).from(Resource.Type.PART, partUri).stream().map(res ->
                udm.read(Resource
                        .Type
                        .DOCUMENT).byUri(res.getUri()).get().asDocument()).collect(Collectors.toList());
        System.out.println(docs.get(0));

    }

    @Test
    public void docsFromItem() throws IOException {

        LOG.info("Getting the number of documents grouped by year...");

        String uri = "http://drinventor.eu/items/9cc43a2454dff5d0a139a4e9cb7dd51f";

        List<Document> docs = udm.find(Resource.Type.DOCUMENT).from(Resource.Type.ITEM, uri).stream().map(res ->
                udm.read(Resource
                        .Type
                        .DOCUMENT).byUri(res.getUri()).get().asDocument()).collect(Collectors.toList());
        System.out.println(docs.get(0));

    }


}
