/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.minetur;

import com.google.common.base.Strings;
import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Domain;
import org.librairy.model.domain.resources.Part;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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
public class DomainsLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DomainsLoader.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;

    private static final String BASE_DIRECTORY = "/Users/cbadenes/Documents/OEG/Projects/MINETUR/TopicModelling-2016" +
            "/patentes-TIC-norteamericanas/uspto/";

    @Test
    public void create() throws IOException {

        for (int year = 2005; year < 2016; year ++){
            createDomain(String.valueOf(year));
        }
    }


    private void createDomain(String id){

        LOG.info("Creating domain: " + id);
        Path directory = Paths.get(BASE_DIRECTORY, id, "txt");


        // Create domain
        Domain domain = Resource.newDomain(id);
        domain.setUri(uriGenerator.from(Resource.Type.DOMAIN,id));
        domain.setDescription("Patents in " + id);
        udm.save(domain);


        AtomicInteger counter = new AtomicInteger();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry: stream) {

                String docId = StringUtils.substringBeforeLast(entry.toFile().getName(),".txt");

                String docUri = uriGenerator.from(Resource.Type.DOCUMENT,docId);

                counter.incrementAndGet();

                // Associate document to domain
                udm.save(Relation.newContains(domain.getUri(),docUri));
            }
        } catch (IOException e) {
            LOG.error("Error on dir: " + directory, e);
        }

        LOG.info("Domain: " + id + " created and associated to " + counter.get() + " documents");

    }


}
