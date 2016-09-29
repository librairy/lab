/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.reports;

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
import java.util.stream.Stream;

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
public class Stats {

    private static final Logger LOG = LoggerFactory.getLogger(Stats.class);

    @Autowired
    UDM udm;

    @Autowired
    URIGenerator uriGenerator;


    @Autowired
    DealsDocEdgeTemplate template;

    @Test
    public void docsPerYear() throws IOException {

        LOG.info("Getting the number of documents grouped by year...");

        Map<String, List<Document>> docsPerYear = udm.find(Resource.Type.DOCUMENT)
                .all()
                .parallelStream()
                .map(res -> udm.read(Resource.Type.DOCUMENT).byUri(res.getUri()).get().asDocument())
                .map(doc -> {
                    doc.setPublishedOn(fixYear(doc));
                    return doc;
                })
                .filter(doc -> !Strings.isNullOrEmpty(doc.getPublishedOn()))
                .collect(Collectors.groupingBy(Document::getPublishedOn));


        docsPerYear.entrySet().stream()
                .sorted(Map.Entry.<String, List<Document>>comparingByKey())
                .forEach(entry -> {
                    String year = entry.getKey();
                    int docs = entry.getValue().size();
                    LOG.info("{ year: '"+ year+"', value: " + docs+" },");
                });


        udm.find(Resource.Type.DOCUMENT)
                .all()
                .parallelStream()
                .map(res -> udm.read(Resource.Type.DOCUMENT).byUri(res.getUri()).get().asDocument())
                .filter(doc -> Strings.isNullOrEmpty(doc.getPublishedOn()) || doc.getPublishedOn().equalsIgnoreCase
                        ("none") || (Integer.valueOf(doc.getPublishedOn())<2002) )
                .forEach(doc -> {

                    String proposedYear = StringUtils.remove(StringUtils.substringBefore(StringUtils
                            .substringAfterLast(doc
                                    .getRetrievedFrom(), "/sig"), "/"), "a");

                    LOG.info(doc.getPublishedOn() +"["+proposedYear+"]" + " - " + doc.getRetrievedFrom() + " : " + doc
                            .getTitle());
                });
    }

    @Test
    public void docs() throws IOException {

        LOG.info("Getting documents ...");

        udm.find(Resource.Type.DOCUMENT)
                .all()
                .stream()
                .map(res -> udm.read(Resource.Type.DOCUMENT).byUri(res.getUri()).get().asDocument())
                .map(doc -> {
                    doc.setPublishedOn(fixYear(doc));
                    return doc;
                })
                .sorted(Comparator.comparingInt(p -> Integer.valueOf(p.getPublishedOn())))
                .forEach(doc -> {
                    System.out.println("<tr>");
                    System.out.println("\t<td>"+doc.getPublishedOn()+"</td>");
                    String title = (doc.getTitle().length()>200)? StringUtils.substring(doc.getTitle(),0,200) + ".."
                    : doc
                            .getTitle();
                    System.out.println("\t<td>"+fixUri(doc.getUri(),title)+"</td>");
                    String authors = doc.getAuthoredBy();
                    System.out.println("\t<td>"+StringUtils.removeStart(authors,", ").replace(", ,",",")+"</td>");
                    System.out.println("</tr>");
                });
    }

    @Test
    public void dealsFromDoc() throws IOException {

        LOG.info("Getting documents ...");

        String uri = "http://drinventor.eu/documents/e9b0ef138c4906a34e43fedac887daf2";

        Map<String,String> topics = new HashMap<>();
        topics.put("http://drinventor.eu/topics/acb0972c27d6096c4d6b6f89c15a44af","topic 1");
        topics.put("http://drinventor.eu/topics/8617654279396256e3f97933205e9807","topic 2");
        topics.put("http://drinventor.eu/topics/d6fd22f2f4735923b89cde588972bacf","topic 3");
        topics.put("http://drinventor.eu/topics/6982c58129104ed592ef00365b0c408f","topic 4");
        topics.put("http://drinventor.eu/topics/29c200fae24ffdf1f03c78854a012f2e","topic 5");
        topics.put("http://drinventor.eu/topics/e541b4b026d8542f4c0c995f1911db04","topic 6");
        topics.put("http://drinventor.eu/topics/28ea36b7c2d287cf9a70aa5aaf3a55d","topic 7");


        Comparator<? super Relation> sortRelated = new Comparator<Relation>() {
            @Override
            public int compare(Relation o1, Relation o2) {
                return topics.get(o1.getEndUri()).compareTo(topics.get(o2.getEndUri()));
            }
        };


        DecimalFormat df = new DecimalFormat("#.####");

        udm.find(Relation.Type.DEALS_WITH_FROM_DOCUMENT).from(Resource.Type.DOCUMENT,uri)
                .stream()
                .sorted(sortRelated)
                .map(rel -> udm.read(Relation.Type.DEALS_WITH_FROM_DOCUMENT).byUri(rel.getUri()).get()
                        .asDealsWithFromDocument())
                .forEach(rel -> {
                    //{"name": "topics", "skill": "topic 0 ", "value": 4},
                    System.out.println("{\"name\": \"topics\", \"topic\": \"" + topics.get(rel.getEndUri()) + "\", " +
                            "\"value\": "+df.format(rel.getWeight())+"},");
                });

    }

    @Test
    public void docContent() throws IOException {

        LOG.info("Getting documents ...");

        String uri = "http://drinventor.eu/documents/1b86b4fe41091a4ec6b1d9e01207bc19";

        Comparator<? super Relation> wsorted = new Comparator<Relation>() {
            @Override
            public int compare(Relation o1, Relation o2) {
                return -o1.getWeight().compareTo(o2.getWeight());
            }
        };


        DecimalFormat df = new DecimalFormat("#.##");

        udm.find(Relation.Type.DEALS_WITH_FROM_DOCUMENT).from(Resource.Type.DOCUMENT,uri)
                .stream()
                .sorted(wsorted)
                .map(rel -> udm.read(Relation.Type.DEALS_WITH_FROM_DOCUMENT).byUri(rel.getUri()).get().asDealsWithFromDocument())
                .forEach(rel -> {
                    udm.find(Relation.Type.MENTIONS_FROM_TOPIC).from
                            (Resource.Type.TOPIC, rel.getEndUri())
                            .stream()
                            .sorted(wsorted)
                            .limit(Double.valueOf(rel.getWeight() * 10).intValue())
                            .forEach(mention -> {
                                String word = StringUtils.substringAfterLast(mention.getEndUri(),"/");
                                String value = df.format((rel.getWeight()*10) * (mention.getWeight()*100));
                                //{"usd": 34590873460, "product": "Oil"},
                                System.out.println("{ \"word\": \""+word+"\", \"weight\": " + value + "},");
                    });
                            ;
                });

    }


    @Test
    public void similarDocs() throws IOException {

        LOG.info("Getting documents ...");

        String uri = "http://drinventor.eu/documents/e9b0ef138c4906a34e43fedac887daf2";

        String ref = udm.read(Resource.Type.DOCUMENT).byUri(uri).get().asDocument().getTitle();

        DecimalFormat df = new DecimalFormat("#.##");

        List<SimilarToDocuments> similar = udm.find(Relation.Type
                .SIMILAR_TO_DOCUMENTS).from(Resource.Type.DOCUMENT, uri)
                .parallelStream()
                .map(rel -> udm.read(Relation.Type.SIMILAR_TO_DOCUMENTS).byUri(rel.getUri()).get()
                        .asSimilarToDocuments())
                .filter(rel -> rel.getWeight() > 0.9)
                .collect(Collectors.toList());;

        similar.parallelStream().forEach(rel -> {
                    //{"name": "alpha", "size": 10},
                    double size = ((rel.getWeight() * 100) - 90) * 10;
                    //String id = StringUtils.substringAfterLast(rel.getEndUri(),"/");
                    String id = udm.read(Resource.Type.DOCUMENT).byUri(rel.getEndUri()).get().asDocument().getTitle();
                    String similarity = df.format(rel.getWeight());
                    System.out.println("{\"name\": \""+id+"\", \"similarity\":" + (similarity.equalsIgnoreCase("1") ?"0.99":similarity) + "},");
                });

        similar.parallelStream().forEach(rel -> {
            //{"source": "gamma", "target": "http://drinventor.eu/documents/bd81cb95bfc2b7a11d265a0bb345510e"},
            String id = udm.read(Resource.Type.DOCUMENT).byUri(rel.getEndUri()).get().asDocument().getTitle();
            if (!id.equalsIgnoreCase(ref)){
                System.out.println("{\"source\": \""+ref+"\", \"target\": \"" + id + "\" },");
            }
        });


    }

    @Test
    public void docDetail() throws IOException {

        LOG.info("Getting a document ...");

        Comparator<? super Part> compareParts = new Comparator<Part>() {
            @Override
            public int compare(Part o1, Part o2) {

                if (o1.getSense().equalsIgnoreCase("challenge")) return -1;

                if (o1.getSense().equalsIgnoreCase("background")){

                    if (o2.getSense().equalsIgnoreCase("challenge")) return  +1;
                    return -1;
                }

                if (o1.getSense().equalsIgnoreCase("approach")){
                    if (o2.getSense().equalsIgnoreCase("challenge")) return  +1;
                    if (o2.getSense().equalsIgnoreCase("background")) return  +1;
                    return -1;
                }

                if (o1.getSense().equalsIgnoreCase("outcome")){
                    if (o2.getSense().equalsIgnoreCase("challenge")) return  +1;
                    if (o2.getSense().equalsIgnoreCase("background")) return  +1;
                    if (o2.getSense().equalsIgnoreCase("approach")) return  +1;
                    return -1;
                }

                if (o1.getSense().equalsIgnoreCase("futureWork")){
                    if (o2.getSense().equalsIgnoreCase("challenge")) return  +1;
                    if (o2.getSense().equalsIgnoreCase("background")) return  +1;
                    if (o2.getSense().equalsIgnoreCase("approach")) return  +1;
                    if (o2.getSense().equalsIgnoreCase("outcome")) return  +1;
                    return -1;
                }

                return 0;
            }
        };

        List<String> uris = Arrays.asList(new String[]{
                "http://drinventor.eu/documents/1b86b4fe41091a4ec6b1d9e01207bc19",
                "http://drinventor.eu/documents/e9b0ef138c4906a34e43fedac887daf2",
                "http://drinventor.eu/documents/9e01326b971ea03474617341bc863fcc"
        });

        uris.forEach(uri -> {
            System.out.println("=================================================================");
            Document document = udm.read(Resource.Type.DOCUMENT).byUri(uri).get().asDocument();

            printRow("uri",fixUri(document.getUri(), document.getUri()),true);
            printRow("authoredBy",fixAuthors(document.getAuthoredBy()),true);
            printRow("publishedOn",fixYear(document),true);
            printRow("publishedBy",fixUri(document.getPublishedBy(), document.getPublishedBy()),true);
            printRow("retrievedFrom",document.getRetrievedFrom(),true);
            printRow("retrievedOn",document.getRetrievedOn(),true);
            printRow("language",document.getLanguage(),true);
            printRow("abstract",document.getDescription(),true);

            udm.find(Resource.Type.PART).from(Resource.Type.DOCUMENT,uri).stream().map(res -> udm.read(Resource.Type
                    .PART).byUri(res.getUri()).get().asPart()).sorted(compareParts).forEach(part -> {
                printRow(part.getSense(),part.getContent(),true);
            });
        });
    }

    @Test
    public void topicDetails() throws IOException {

        LOG.info("Getting topics ...");

        AtomicInteger counter = new AtomicInteger();

        udm.find(Resource.Type.TOPIC).all().forEach(res -> {
            System.out.println("=================================================================");
            Topic topic = udm.read(Resource.Type.TOPIC).byUri(res.getUri()).get().asTopic();
            System.out.println(fixUri(topic.getUri(),"Topic " + counter.getAndIncrement()));

            Comparator<? super Relation> sortWords = new Comparator<Relation>() {
                @Override
                public int compare(Relation o1, Relation o2) {
                    return -o1.getWeight().compareTo(o2.getWeight());
                }
            };
            udm.find(Relation.Type.MENTIONS_FROM_TOPIC).from(Resource.Type.TOPIC,topic.getUri()).stream()
                    .sorted(sortWords).forEach(rel -> {
                String word = StringUtils.substringAfterLast(rel.getEndUri(),"words/");
                printRow(fixUri(rel.getEndUri(),word),""+rel.getWeight(),false);
            });


        });
    }

    @Test
    public void topicDocuments() throws IOException {

        String topicUri = "http://drinventor.eu/topics/28ea36b7c2d287cf9a70aa5aaf3a55d";

        LOG.info("Getting documents for topic: " + topicUri);

        Comparator<? super Relation> compareRels = new Comparator<Relation>() {
            @Override
            public int compare(Relation o1, Relation o2) {
                return -o1.getWeight().compareTo(o2.getWeight());
            }
        };

        udm.find(Relation.Type.DEALS_WITH_FROM_DOCUMENT).from(Resource.Type.TOPIC,topicUri).stream().sorted
                (compareRels).limit(25).forEach(rel -> {
            Document document = udm.read(Resource.Type.DOCUMENT).byUri(rel.getStartUri()).get().asDocument();
            System.out.println("<tr>");
            System.out.println("<td>"+fixYear(document)+"</td>");
            System.out.println("<td>"+fixUri(document.getUri(),document.getTitle())+"</td>");
            System.out.println("<td>"+rel.getWeight()+"</td>");
            System.out.println("</tr>");
        });


    }

    @Test
    public void topicDistribution() throws IOException {

        LOG.info("Getting documents by topic.. ");

        Comparator<? super Relation> compareRels = new Comparator<Relation>() {
            @Override
            public int compare(Relation o1, Relation o2) {
                return -o1.getWeight().compareTo(o2.getWeight());
            }
        };

        Map<String, List<Relation>> topicDist = udm.find(Relation.Type.DEALS_WITH_FROM_DOCUMENT).all().stream()
                .collect(Collectors.groupingBy(rel -> rel.getStartUri()));

        Map<String, List<Tuple2<String, String>>> out = topicDist.entrySet().stream().map(entry -> {
            String year = fixYear(udm.read(Resource.Type.DOCUMENT).byUri(entry.getKey()).get().asDocument());
            return new Tuple2<String, String>(year, maxDist(entry.getValue()));
        }).collect(Collectors.groupingBy(x -> x._2));


        // Total
        Map<String, List<Tuple2<String, String>>> total = out.entrySet().stream().flatMap(e -> e.getValue().stream())
                .collect(Collectors.groupingBy(x -> x._1));


        int tcounter = 0;
        Comparator<? super Map.Entry<String, List<Tuple2<String, String>>>> sortYears = new Comparator<Map.Entry<String, List<Tuple2<String, String>>>>() {


            @Override
            public int compare(Map.Entry<String, List<Tuple2<String, String>>> o1, Map.Entry<String,
                    List<Tuple2<String, String>>> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        };
        for (Map.Entry<String,List<Tuple2<String, String>>> entry : total.entrySet().stream().sorted(sortYears).collect(Collectors.toList())){
            tcounter += entry.getValue().size();
            System.out.println(entry.getKey() + ": " + entry.getValue().size());
        };
        System.out.println("Total Docs: " + tcounter);


        // Partial
        out.entrySet().stream().forEach(e -> {
            System.out.println(e.getKey() + ": " + e.getValue().size());

            Map<String, List<Tuple2<String, String>>> byYear = e.getValue().stream().collect(Collectors.groupingBy(x
                    -> x._1));

            // { label: '2002', value: 10 },
            byYear.entrySet().stream().sorted(sortYears).forEach( j -> {
                System.out.println("{ year: '" + j.getKey() + "', total: " + j.getValue().size() + ", rate: " +
                        (j.getValue().size()*100)/total.get(j.getKey()).size() + " },");
            });


        });


    }


    private String maxDist(List<Relation> relations) {
        String uriRef = "";
        Double wRef = 0.0;

        for (Relation relation : relations) {
            if (wRef < relation.getWeight()) {
                uriRef = relation.getEndUri();
                wRef = relation.getWeight();
            }
        }

        return uriRef;
    }

    @Test
    public void docsWithSummary() throws IOException {

        LOG.info("Getting list of documents containing summary ...");



        udm.find(Resource.Type.DOCUMENT).all().parallelStream().forEach(res -> {
            long count = udm.find(Resource.Type.PART).from(Resource.Type.DOCUMENT, res.getUri()).parallelStream().map
                    (pres
                    -> udm.read
                    (Resource
                            .Type
                            .PART).byUri(pres.getUri()).get().asPart()).filter(part -> part.getSense()
                    .toLowerCase()
                    .contains
                    ("summary")).count();
            if (count>0) System.out.println(res.getUri());
        });
    }

    private void printRow(String label,String value, boolean bold){
        System.out.println("<tr>");
        String id = bold? "th" : "td";
        System.out.println("<"+id+">"+label+"</"+id+">");
        String content = (value.length() > 700)? StringUtils.substring(value,0,700)+"..." : value;
        System.out.println("<td>"+content+"</td>");
        System.out.println("</tr>");
    }


    private String fixUri (String uri, String label){
        return
                "<a href='http://drinventor.dia.fi.upm.es/api/0.2"+StringUtils.substringAfterLast(uri,"drinventor.eu")+"'>"+label+"</a>";
    }

    private String fixAuthors(String authors){
        if (Strings.isNullOrEmpty(authors)) return "";

        return StringUtils.removeStart(authors,", ").replace(", ,",",");

    }

    private String fixYear(Document doc){
        if (!Strings.isNullOrEmpty(doc.getPublishedOn()) && !doc.getPublishedOn().equalsIgnoreCase
                ("none") && !(Integer.valueOf(doc.getPublishedOn())<2002)){
            return doc.getPublishedOn();
        }else {
            String proposedYear = StringUtils.remove(StringUtils.substringBefore(StringUtils
                    .substringAfterLast(doc
                            .getRetrievedFrom(), "/siggraph/sig"), "/"), "a");
            return proposedYear;
        }
    }


    @Test
    public void words() throws IOException {

        LOG.info("Getting the number of different words in the corpus...");

        long numWords = udm.find(Resource.Type.ITEM)
                .all()
                .parallelStream()
                .map(res -> udm.read(Resource.Type.ITEM).byUri(res.getUri()).get().asItem())
                .flatMap(item -> Arrays.stream(item.getTokens().split(" ")))
                .distinct()
                .count();

        LOG.info("Vocabulary Size: " + numWords + " words");

    }

    @Test
    public void terms() throws IOException {

        LOG.info("Getting the number of terms in the corpus...");

        long num = udm.find(Resource.Type.TERM).all().size();

        LOG.info("Term Size: " + num + " terms");

    }

    @Test
    public void termsDetail() throws IOException {

        LOG.info("Getting terms in corpus...");
        DecimalFormat df = new DecimalFormat("#.#####");

        Comparator<? super Relation> compareRel = new Comparator<Relation>() {
            @Override
            public int compare(Relation o1, Relation o2) {
                return -Double.valueOf(o1.asAppearedIn().getTermhood()).compareTo(Double.valueOf(o2.asAppearedIn()
                        .getTermhood()));
            }
        };

        AtomicInteger counter = new AtomicInteger();

        udm.find(Relation.Type.APPEARED_IN)
                .all()
                .stream()
                .map(rel -> udm.read(Relation.Type.APPEARED_IN).byUri(rel.getUri()).get().asAppearedIn())
                .sorted(compareRel)
                .limit(125)
                .forEach(rel -> {

                    AppearedIn appearedIn = rel.asAppearedIn();
                    Term term = udm.read(Resource.Type.TERM).byUri(rel.getStartUri()).get().asTerm();
                    if (term.getContent().length()>2){
                        counter.getAndIncrement();
                        System.out.println("<tr>");
                        System.out.println("<td>"+fixUri(term.getUri(),term.getContent())+"</td>");
                        System.out.println("<td>"+df.format(appearedIn.getTermhood())+"</td>");
                        System.out.println("<td>"+df.format(appearedIn.getCvalue())+"</td>");
                        System.out.println("<td>"+df.format(appearedIn.getConsensus())+"</td>");
                        System.out.println("<td>"+df.format(appearedIn.getProbability())+"</td>");
                        System.out.println("<td>"+df.format(appearedIn.getTimes())+"</td>");
                        System.out.println("</tr>");
                    }
                });
        LOG.info("Found " + counter.get() + " terms!");
    }

}
