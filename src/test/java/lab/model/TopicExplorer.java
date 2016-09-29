/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.model;

import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.librairy.storage.system.graph.template.edges.DealsDocEdgeTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
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
public class TopicExplorer {

    private static final Logger LOG = LoggerFactory.getLogger(TopicExplorer.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;


    @Autowired
    DealsDocEdgeTemplate template;

    @Test
    public void exploreTopics() throws IOException {

        LOG.info("Getting the number of documents grouped by year...");

        udm.find(Resource.Type.TOPIC).all()
                .stream().limit(1)
                .map(res -> udm.read(Resource.Type.TOPIC).byUri(res.getUri()).get().asTopic())
        .forEach(topic -> System.out.println(topic));
        ;

    }

}
