package topics;

import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.mllib.clustering.LDAModel;
import org.apache.spark.mllib.clustering.LocalLDAModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.DealsWithFromItem;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.relations.Relationship;
import org.librairy.model.domain.relations.SimilarTo;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Item;
import org.librairy.model.domain.resources.Resource;
import org.librairy.modeler.lda.builder.OnlineLDABuilder;
import org.librairy.modeler.lda.functions.RowToPair;
import org.librairy.modeler.lda.helper.SparkHelper;
import org.librairy.modeler.lda.models.Corpus;
import org.librairy.modeler.lda.models.similarity.RelationalSimilarity;
import org.librairy.storage.UDM;
import org.librairy.storage.generator.URIGenerator;
import org.librairy.storage.system.column.repository.UnifiedColumnRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import scala.Tuple2;
import scala.reflect.ClassTag$;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created on 11/07/16:
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
public class TopicModelTest {

    private static final Logger LOG = LoggerFactory.getLogger(TopicModelTest.class);

    @Autowired
    UDM udm;

    @Autowired
    UnifiedColumnRepository columnRepository;

    @Test
    public void createModel() throws IOException {

        OnlineLDABuilder topicBuilder = new OnlineLDABuilder();
        topicBuilder.setColumnRepository(columnRepository);
        topicBuilder.setUriGenerator(new URIGenerator());
        topicBuilder.setFileSystemEndpoint("target/");
        topicBuilder.setUdm(udm);

        SparkHelper sparkHelper = new SparkHelper();
        sparkHelper.setMemory("2g");
        sparkHelper.setThreads("4");
        sparkHelper.setup();
        topicBuilder.setSparkHelper(sparkHelper);

        String domainUri = "http://drinventor.eu/domains/4f56ab24bb6d815a48b8968a3b157470";

        Integer vocabSize = 10000;

        Corpus corpus = topicBuilder.createCorpus(domainUri, vocabSize);

        int k           = 100;
        int iterations  = 10;
        double alpha    = -1.0;
        double beta     = -1.0;
        LDAModel ldaModel = topicBuilder.trainModel(k, iterations, alpha, beta, corpus.getDocuments());

        ldaModel.save(sparkHelper.getSc().sc(),"out/ldaModel");
        corpus.getModel().save("out/vocabulary");

    }


    @Test
    public void findSimilarDocs(){

        String domainURI = "http://drinventor.eu/domains/4f56ab24bb6d815a48b8968a3b157470";

        String input = "This paper presents an approach to representing decaying organic life, by\n" +
                "animating shapes and textures. It focuses on a lily, decaying rapidly, in a time-lapse\n" +
                "manner. The process is based on first-hand observation and is entirely\n" +
                "procedural. As for the texture animation, it is created\n" +
                "primarily from three texture maps generated from a node base graph\n" +
                "allowing the animation of\n" +
                "base parameters. Artistic endeavours are also considered to broaden the\n" +
                "reach of the project. new skin deformation method to create dynamic skin deformations in this\n" +
                "paper. The core elements of our approach are a dynamic deformation model,\n" +
                "an efficient data-driven finite difference solution, and a curve-based\n" +
                "representation of 3D models";


        SparkHelper sparkHelper = new SparkHelper();
        sparkHelper.setMemory("2g");
        sparkHelper.setThreads("4");
        sparkHelper.setup();

        OnlineLDABuilder ldaBuilder = new OnlineLDABuilder();
        ldaBuilder.setSparkHelper(sparkHelper);


        LocalLDAModel model = LocalLDAModel.load(sparkHelper.getSc().sc(),"out/ldaModel");
        CountVectorizerModel cvModel = CountVectorizerModel.load("out/vocabulary");
        Tuple2<Object, Vector> tuple = new Tuple2<Object,Vector>(0l, Vectors.dense(new double[]{1.0}));

        List<Row> inputRows = Arrays.asList(new Row[]{RowFactory.create("test-uri", input)});
        DataFrame inputDF = ldaBuilder.preprocess(inputRows);
        RDD<Tuple2<Object, Vector>> inputDocs = cvModel.transform(inputDF)
                .select("uri", "features")
                .map(new RowToPair(), ClassTag$.MODULE$.<Tuple2<Object, Vector>>apply(tuple.getClass()));
        RDD<Tuple2<Object, Vector>> inputTopics = model.topicDistributions(inputDocs);



        // Corpus Data Frame
        LOG.info("Reading item uris..");
        String itemListPath = "out/items.list";
        String itemRegPath = "out/items.reg";
        ConcurrentHashMap<Long,String> itemRegistry = new ConcurrentHashMap<>();
        List<Row> corpusRows = null;
        if (new File(itemListPath).exists()){
            LOG.info("Deserializing variables...");
            try {
                corpusRows = (List<Row>) deserialize(itemListPath);
                itemRegistry = (ConcurrentHashMap<Long, String>) deserialize(itemRegPath);
            } catch (IOException e) {
                LOG.error("Deserialize Error",e);
            } catch (ClassNotFoundException e) {
                LOG.error("Deserialize Error",e);
            }
        }else{
            List<String> itemsUri = udm.find(Resource.Type.ITEM).from(Resource.Type.DOMAIN, domainURI);
            corpusRows = itemsUri.parallelStream().
                    map(uri -> udm.read(Resource.Type.ITEM).byUri(uri)).
                    filter(res -> res.isPresent()).map(res -> (Item) res.get()).
                    map(item -> RowFactory.create(item.getUri(), item.getTokens())).
                    collect(Collectors.toList());
            ConcurrentHashMap<Long, String> finalItemRegistry = itemRegistry;
            corpusRows.parallelStream().forEach(row -> {
                String uri = String.valueOf(row.get(0));
                Long id = RowToPair.from(uri);
                finalItemRegistry.put(id,uri);
            });
            try {
                LOG.info("Serializing variables...");
                serialize(corpusRows,itemListPath);
                serialize(finalItemRegistry, itemRegPath);
            } catch (IOException e) {
                LOG.error("Serialize Error",e);
            }
            itemRegistry = finalItemRegistry;

        }
        DataFrame corpusDF = ldaBuilder.preprocess(corpusRows);
        RDD<Tuple2<Object, Vector>> inputCorpus = cvModel.transform(corpusDF)
                .select("uri", "features")
                .map(new RowToPair(), ClassTag$.MODULE$.<Tuple2<Object, Vector>>apply(tuple.getClass()));
        RDD<Tuple2<Object, Vector>> corpusTopics = model.topicDistributions(inputCorpus);


        // Comparison
        JavaRDD<Tuple2<Object, Vector>> inputRDD = inputTopics.toJavaRDD();
        JavaRDD<Tuple2<Object, Vector>> corpusRDD = corpusTopics.toJavaRDD();


        List<Tuple2<Tuple2<Object, Vector>, Tuple2<Object, Vector>>> itemsPair = inputRDD.cartesian(corpusRDD)
//                .filter(x -> x._1()._1.toString().compareTo(x._2()._1.toString()) > 0)
                .collect();

        LOG.info("Calculating similarities: " + itemsPair.size());
        ConcurrentHashMap<Long, String> finalItemRegistry1 = itemRegistry;
        List<Tuple2<Double, String>> similars = itemsPair.stream().map(pair -> {

            Vector v1 = pair._1._2;
            List<Relationship> v1List = new ArrayList<>();
            for (int i = 0; i < v1.size(); i++) {
                double[] array = v1.toArray();
                v1List.add(new Relationship(String.valueOf(i), array[i]));
            }

            Vector v2 = pair._2._2;
            List<Relationship> v2List = new ArrayList<>();
            for (int i = 0; i < v2.size(); i++) {
                double[] array = v2.toArray();
                v2List.add(new Relationship(String.valueOf(i), array[i]));
            }


            Double similarity = RelationalSimilarity.between(v1List, v2List);

            String itemUri = finalItemRegistry1.get(pair._2._1);
            
            return new Tuple2<Double, String>(similarity, itemUri);
        }).sorted(new Comparator<Tuple2<Double, String>>() {
            @Override
            public int compare(Tuple2<Double, String> o1, Tuple2<Double, String> o2) {
                return -o1._1.compareTo(o2._1);
            }
        }).limit(20).collect(Collectors.toList());


        List<Tuple2<Document, Double>> simDocs = similars.stream().map(doc -> {

            List<String> docUri = udm.find(Resource.Type.DOCUMENT).from(Resource.Type.ITEM, doc._2);

            Document document = udm.read(Resource.Type.DOCUMENT).byUri(docUri.get(0)).get().asDocument();

            return new Tuple2<Document, Double>(document, doc._1);
        }).collect(Collectors.toList());


        simDocs.forEach(t -> {
            LOG.info("[" + t._2 + "]-"+t._1.getTitle() + " [" + t._1.getUri() +"]");
        });





    }


    private void serialize(Object object, String path) throws IOException {
        FileOutputStream fout = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        out.writeObject(object);
        out.close();
        fout.close();
        LOG.info("Object serialized to: " + path);
    }

    private Object deserialize(String path) throws IOException, ClassNotFoundException {
        FileInputStream fin = new FileInputStream(path);
        ObjectInputStream oin = new ObjectInputStream(fin);
        Object value = oin.readObject();
        oin.close();
        fin.close();
        return value;
    }




}
