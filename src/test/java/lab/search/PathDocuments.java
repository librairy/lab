/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.search;

import com.google.common.collect.ImmutableMap;
import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Path;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.librairy.storage.system.graph.domain.nodes.DocumentNode;
import org.librairy.storage.system.graph.repository.nodes.DocumentGraphRepository;
import org.librairy.storage.system.graph.template.TemplateExecutor;
import org.librairy.storage.system.graph.template.edges.SimilarDocEdgeTemplate;
import org.librairy.storage.system.graph.template.nodes.DocumentNodeTemplate;
import org.neo4j.ogm.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

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
public class PathDocuments {

    private static final Logger LOG = LoggerFactory.getLogger(PathDocuments.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;

    @Autowired
    TemplateExecutor executor;

    @Autowired
    DocumentNodeTemplate documentNodeTemplate;

    @Autowired
    SimilarDocEdgeTemplate similarDocEdgeTemplate;


    @Test
    public void simple() throws IOException {

        Double minSimilarValue = 0.7;

        Path path = Resource.newPath("http://drinventor.eu/documents/39d9c4c0bb38b5fd740be63ad4cbb82c","http://drinventor.eu/documents/470c8134092ab394ee4590089add40bf");


        String query = "match (s : Document { uri : {0} }), (e : Document { uri : {1} }), path = shortestPath ((s)" +
                "-[r:SIMILAR_TO*1..10]-(e)) WHERE ALL (r in rels(path) WHERE r.weight > "+minSimilarValue+") return path";


        Optional<Result> results = executor.query(query, ImmutableMap.of("0",path.getStart(),"1",path.getEnd()));

        if (!results.isPresent()) LOG.info("Empty");


        Iterator<Map<String, Object>> iterator = results.get().queryResults().iterator();
        while(iterator.hasNext()){
            Map<String, Object> resource = iterator.next();
            Map resultPath = (Map) resource.get("path");

            List<String>  relationships = (List<String>) resultPath.get("relationships");
            LOG.info("Relations: " + relationships);

            List<String>  nodes         = (List<String>) resultPath.get("nodes");
            LOG.info("Nodes: " + nodes);

            List<String>  directions    = (List<String>) resultPath.get("directions");
            LOG.info("directions: " + directions);

            Integer  length             = (Integer) resultPath.get("length");
            LOG.info("Length: " + length);

            for (String uri: nodes){
                Long id = Long.valueOf(StringUtils.substringAfterLast(uri,"/"));
                Resource doc = documentNodeTemplate.fromNodeId(id).get();
                LOG.info("Node: " + doc);
            }

            for (String uri: relationships){
                Long id = Long.valueOf(StringUtils.substringAfterLast(uri,"/"));
                Relation rel = similarDocEdgeTemplate.fromNodeId(id).get();
                LOG.info("Relation: " + rel);
            }

        }
    }




}
