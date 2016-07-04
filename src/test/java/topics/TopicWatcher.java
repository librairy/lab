package topics;

import com.google.common.collect.ImmutableMap;
import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.system.graph.repository.edges.*;
import org.librairy.storage.system.graph.template.TemplateExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 03/07/16:
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
public class TopicWatcher {

    private static final Logger LOG = LoggerFactory.getLogger(TopicWatcher.class);

    @Autowired
    UDM udm;

    @Autowired
    TemplateExecutor executor;

    @Test
    public void deleteTopicsFromDocs() throws IOException {
        deleteDuplicatedRelations(Resource.Type.DOCUMENT, Relation.Type.DEALS_WITH_FROM_DOCUMENT);
    }

    @Test
    public void deleteTopicsFromItems() throws IOException {
        deleteDuplicatedRelations(Resource.Type.ITEM, Relation.Type.DEALS_WITH_FROM_ITEM);
    }

    @Test
    public void deleteTopicsFromParts() throws IOException {
        deleteDuplicatedRelations(Resource.Type.PART, Relation.Type.DEALS_WITH_FROM_PART);
    }

    @Test
    public void deletePairedWords() throws IOException {
        deleteDuplicatedRelations(Resource.Type.WORD, Relation.Type.PAIRS_WITH);
    }

    @Test
    public void deleteSimilarDocs() throws IOException {
        deleteDuplicatedRelations(Resource.Type.DOCUMENT, Relation.Type.SIMILAR_TO_DOCUMENTS);
    }

    @Test
    public void fixDuplicatedRelations() throws IOException {
        deleteDuplicatedRelations(Resource.Type.SOURCE, Relation.Type.PROVIDES);
        deleteDuplicatedRelations(Resource.Type.SOURCE, Relation.Type.COMPOSES);

        deleteDuplicatedRelations(Resource.Type.DOMAIN, Relation.Type.CONTAINS);

        deleteDuplicatedRelations(Resource.Type.DOCUMENT, Relation.Type.DEALS_WITH_FROM_DOCUMENT);
        deleteDuplicatedRelations(Resource.Type.DOCUMENT, Relation.Type.SIMILAR_TO_DOCUMENTS);
        deleteDuplicatedRelations(Resource.Type.DOCUMENT, Relation.Type.BUNDLES);

        deleteDuplicatedRelations(Resource.Type.ITEM, Relation.Type.DEALS_WITH_FROM_ITEM);
        deleteDuplicatedRelations(Resource.Type.ITEM, Relation.Type.SIMILAR_TO_ITEMS);

        deleteDuplicatedRelations(Resource.Type.PART, Relation.Type.DEALS_WITH_FROM_PART);
        deleteDuplicatedRelations(Resource.Type.PART, Relation.Type.SIMILAR_TO_PARTS);
        deleteDuplicatedRelations(Resource.Type.PART, Relation.Type.DESCRIBES);

        deleteDuplicatedRelations(Resource.Type.TOPIC, Relation.Type.EMERGES_IN);
        deleteDuplicatedRelations(Resource.Type.TOPIC, Relation.Type.MENTIONS_FROM_TOPIC);

        deleteDuplicatedRelations(Resource.Type.WORD, Relation.Type.PAIRS_WITH);
        deleteDuplicatedRelations(Resource.Type.WORD, Relation.Type.EMBEDDED_IN);

        deleteDuplicatedRelations(Resource.Type.TERM, Relation.Type.MENTIONS_FROM_TERM);
        deleteDuplicatedRelations(Resource.Type.TERM, Relation.Type.APPEARED_IN);
        deleteDuplicatedRelations(Resource.Type.TERM, Relation.Type.HYPERNYM_OF);
    }

    private void deleteDuplicatedRelations(Resource.Type resourceRef, Relation.Type relationRef) throws IOException {

        AtomicInteger counter = new AtomicInteger();

        Map<Integer,String> sentences = new HashMap<>();

        StringBuilder queryWriter = new StringBuilder();
        udm.find(resourceRef).all().parallelStream().forEach(doc -> {
            Map<String,List<Relation>> deals = new HashMap<String, List<Relation>>();
            udm.find(relationRef).from(resourceRef, doc).forEach(rel -> {
                List<Relation> rels = deals.get(rel.getEndUri());
                if (rels == null) rels = new ArrayList<Relation>();
                rels.add(rel);
                deals.put(rel.getEndUri(),rels);
            });

            deals.keySet().forEach(key -> {
                List<Relation> rels = deals.get(key);
                if (rels != null && rels.size()>1){
                    rels.stream().sorted(new Comparator<Relation>() {
                        @Override
                        public int compare(Relation o1, Relation o2) {
                            return o1.getCreationTime().compareTo(o2.getCreationTime());
                        }
                    }).skip(1).forEach(rel -> {
                        String query = "start r=rel("+rel.getId()+") delete r";
                        queryWriter.append(query);

                        executor.execute(query, ImmutableMap.of());


//                        int count = counter.getAndIncrement();
//
//                        if ((count % 500 == 0) && (count != 0)){
//                            int index = count/500;
//                            sentences.put(index,queryWriter.toString());
//                            queryWriter.delete(0,queryWriter.length());
//                        }else{
//                            queryWriter.append("\nWITH 1 as dummy\n");
//                        }

//                        repository.delete(rel.getId());
                    });
                }
            });


        });

        sentences.keySet().forEach(key -> {
            FileWriter writer = null;
            try {
                writer = new FileWriter("target/delete"+key+".cql");
                writer.write(sentences.get(key));
                writer.write(";");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }

}
