/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.matches;

import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
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
import java.util.List;

import static org.elasticsearch.index.query.FilterBuilders.termFilter;
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
public class MatchDocuments {

    private static final Logger LOG = LoggerFactory.getLogger(MatchDocuments.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;


    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void byTerm() throws IOException {

        LOG.info("Getting the number of documents grouped by year...");

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withIndices("research")
                .withTypes("items")
                //.withQuery(matchAllQuery())
                .withQuery(termQuery("content","animation"))
//                .withFilter(boolFilter().must(termFilter("content", "animation")))
                .withSort(SortBuilders.fieldSort("_score").order(SortOrder.DESC))
                .build();

        List<String> uris = elasticsearchTemplate.queryForIds(searchQuery);

        LOG.info("Result: " + uris);

        ResultsExtractor<List<String>> result = new ResultsExtractor<List<String>>() {
            @Override
            public List<String> extract(SearchResponse searchResponse) {
                LOG.info("Took: " + searchResponse.getTook().toString());

                SearchHits hits = searchResponse.getHits();

                LOG.info("Total Hits: " + hits.totalHits());

                for (SearchHit hit : hits.hits()){
                    LOG.info("Hit Index: " + hit.index());
                    LOG.info("Hit Type: " + hit.getType());
                    LOG.info("Hit Id: " + hit.getId());
                    LOG.info("Hit Score: " + hit.getScore());
                    LOG.info("Hit Source: " + hit.getSourceAsString());
                }

                return null;
            }
        };
        List<String> uris2 = elasticsearchTemplate.query(searchQuery, result);
        LOG.info("Result2: " + uris2);
    }

    @Test
    public void byPhrase() throws IOException {

        LOG.info("Getting the number of documents grouped by year...");

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withIndices("research")
                .withTypes("items")
                //.withQuery(matchAllQuery())
                .withQuery(matchPhraseQuery("content","Vertex"))
//                .withFilter(boolFilter().must(termFilter("content", "animation")))
                .withSort(SortBuilders.fieldSort("_score").order(SortOrder.DESC))
                .build();

        List<String> uris = elasticsearchTemplate.queryForIds(searchQuery);

        LOG.info("Result: " + uris);

        ResultsExtractor<List<String>> result = new ResultsExtractor<List<String>>() {
            @Override
            public List<String> extract(SearchResponse searchResponse) {
                LOG.info("Took: " + searchResponse.getTook().toString());

                SearchHits hits = searchResponse.getHits();

                LOG.info("Total Hits: " + hits.totalHits());

                for (SearchHit hit : hits.hits()){
                    LOG.info("Hit Index: " + hit.index());
                    LOG.info("Hit Type: " + hit.getType());
                    LOG.info("Hit Id: " + hit.getId());
                    LOG.info("Hit Score: " + hit.getScore());
                    LOG.info("Hit Source: " + hit.getSourceAsString());
                }

                return null;
            }
        };
        List<String> uris2 = elasticsearchTemplate.query(searchQuery, result);
        LOG.info("Result2: " + uris2);
    }



}
