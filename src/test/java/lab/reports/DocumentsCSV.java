/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.reports;

import com.google.common.base.Strings;
import lab.BootConfig;
import es.cbadenes.lab.test.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.resources.Part;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        "librairy.columndb.host = wiener.dia.fi.upm.es",
        "librairy.columndb.port = 5011",
        "librairy.documentdb.host = wiener.dia.fi.upm.es",
        "librairy.documentdb.port = 5021",
        "librairy.graphdb.host= wiener.dia.fi.upm.es",
        "librairy.graphdb.port = 5030",
        "librairy.eventbus.host = wiener.dia.fi.upm.es",
        "librairy.eventbus.port = 5041",
})
public class DocumentsCSV {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentsCSV.class);

    @Autowired
    UDM udm;

    @Test
    public void create() throws IOException {

        LOG.info("Ready to build a summary");

        FileWriter writer = new FileWriter("repository-documents.csv");
        String separator = ";";

        writer.write(
                "title"+separator+
//                "year"+separator+
                "document"+separator+
                "background"+separator+
                "challenge"+separator+
                "approach"+separator+
                "outcome"+separator+
                "future"+
                "\n");

        udm.find(Resource.Type.DOCUMENT)
                .all()
                .stream()
                .map(res -> udm.read(Resource.Type.DOCUMENT).byUri(res.getUri()).get().asDocument())
                .forEach(document -> {
                    try {

                        String docUri   = document.getUri();

                        String year = (Strings.isNullOrEmpty(document.getPublishedOn()))? "NONE" : document
                                .getPublishedOn();

                        if (Strings.isNullOrEmpty(document.getTitle())) return;

                        String title = document.getTitle().replace(";",":").replace("\"","");

                        List<Resource> parts = udm.find(Resource.Type.PART).from(Resource.Type.DOCUMENT, docUri);

                        Map<String,String> partRecord = new HashMap<>();

                        parts.forEach(res -> {
                            Optional<Resource> partRes = udm.read(Resource.Type.PART).byUri(res.getUri());
                            if (partRes.isPresent()){
                                Part part = partRes.get().asPart();
                                String pUri     = part.getUri();
                                String sense    = part.getSense();

                                if (sense.toLowerCase().contains("approach")) partRecord.put("approach",pUri);
                                else if (sense.toLowerCase().contains("background")) partRecord.put("background",pUri);
                                else if (sense.toLowerCase().contains("future")) partRecord.put("future",pUri);
                                else if (sense.toLowerCase().contains("outcome")) partRecord.put("outcome",pUri);
                                else if (sense.toLowerCase().contains("challenge")) partRecord.put("challenge",pUri);
                            }
                        });


                        StringBuilder row = new StringBuilder()
                                .append(title.length()>100? title.substring(0,100) : title).append(separator)
//                                .append(year).append(separator)
                                .append(docUri).append(separator)
                                .append(partRecord.get("background")).append(separator)
                                .append(partRecord.get("challenge")).append(separator)
                                .append(partRecord.get("approach")).append(separator)
                                .append(partRecord.get("outcome")).append(separator)
                                .append(partRecord.get("future"))
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
