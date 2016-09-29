/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.storage;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.LinkableElement;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.system.column.domain.DocumentColumn;
import org.librairy.storage.system.column.repository.DocumentColumnRepository;
import org.librairy.storage.system.column.templates.ColumnTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.repository.support.BasicMapId;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
        "librairy.eventbus.host = localhost",
        "librairy.eventbus.port = 5041",
})
public class DocumentStorage {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentStorage.class);

    @Autowired
    UDM udm;

    @Autowired
    DocumentColumnRepository documentColumnRepository;

    @Autowired
    ColumnTemplate columnTemplate;


    @Test
    public void saveOrUpdate() throws IOException {

        LOG.info("Ready to build a summary");

        Document doc = Resource.newDocument("test-document");
        doc.setUri("my-uri");
        doc.setCreationTime("2028-01-01T00:00+0200");

        udm.save(doc);

        Document doc1 = udm.read(Resource.Type.DOCUMENT).byUri("my-uri").get().asDocument();

        Assert.assertEquals(doc,doc1);

        doc1.setCreationTime("2029-01-01T00:00+0200");
        doc1.setTitle("my-new-title");

        udm.save(doc1);

        Document doc2 = udm.read(Resource.Type.DOCUMENT).byUri("my-uri").get().asDocument();

        Assert.assertNotEquals(doc,doc2);
        Assert.assertEquals(doc1,doc2);

        List<Resource> result = udm.find(Resource.Type.DOCUMENT).by("uri", "my-uri");
        Assert.assertNotNull(result);
        Assert.assertEquals(1,result.size());
    }

    @Test
    public void delete() throws IOException {

        udm.delete(Resource.Type.DOCUMENT).byUri("my-uri");

    }

    @Test
    public void readColumn() throws IOException {

        LOG.info("Ready to build a summary");

        Iterable<DocumentColumn> result = documentColumnRepository
                .findByTitle("test-document");

        Iterator<DocumentColumn> it = result.iterator();

        while(it.hasNext()){
            System.out.println(it.next());
        }


        DocumentColumn result2 = documentColumnRepository.findOne(BasicMapId.id("uri", "my-uri"));

        System.out.println(result2);


        Select select = QueryBuilder.select().from("documents").where(QueryBuilder.eq("uri", "my-uri")).limit(10);
        List<LinkableElement> result3 = columnTemplate.query(select);
        System.out.println(result3);

    }

}
