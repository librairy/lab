/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.reports;

import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.AppearedIn;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Resource;
import org.librairy.model.domain.resources.Term;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

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
public class TermsCSV {

    private static final Logger LOG = LoggerFactory.getLogger(TermsCSV.class);

    @Autowired
    UDM udm;

    @Test
    public void create() throws IOException {

        LOG.info("Ready to build a summary");

        FileWriter writer = new FileWriter("terms.csv");
        String separator = ";";

        writer.write(
                "uri"+separator+
                "value"+separator+
                "termhood"+separator+
                "cvalue"+separator+
                "times"+separator+
                "pertinence"+separator+
                "probability"+separator+
                "consensus"+separator+
                "subtermOf"+separator+
                "supertermOf"+
                "\n");

        udm.find(Relation.Type.APPEARED_IN)
                .all()
                .stream()
                .forEach(relation -> {
                    try {

                        Optional<Relation> appearedInRes = udm.read(Relation.Type.APPEARED_IN).byUri(relation.getUri());

                        if (!appearedInRes.isPresent()){
                            LOG.error("No AppearedIn relation found by uri: " + relation.getUri());
                            return;
                        }

                        AppearedIn appearedIn = appearedInRes.get().asAppearedIn();

                        String termUri   = appearedIn.getStartUri();

                        Optional<Resource> termRes = udm.read(Resource.Type.TERM).byUri(termUri);

                        if (!termRes.isPresent()){
                            LOG.error("No Term found by uri: " + termUri);
                            return;
                        }

                        Term term = termRes.get().asTerm();

                        StringBuilder row = new StringBuilder()
                                .append(termUri).append(separator)
                                .append(term.getContent()).append(separator)
                                .append(appearedIn.getTermhood()).append(separator)
                                .append(appearedIn.getCvalue()).append(separator)
                                .append(appearedIn.getTimes()).append(separator)
                                .append(appearedIn.getPertinence()).append(separator)
                                .append(appearedIn.getProbability()).append(separator)
                                .append(appearedIn.getConsensus()).append(separator)
                                .append(appearedIn.getSubtermOf()).append(separator)
                                .append(appearedIn.getSupertermOf())
                                ;
                        LOG.info(row.toString());
                        writer.write(row.toString());
                        writer.write("\n"); // newline
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        writer.close();

    }

}
