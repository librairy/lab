/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.fixing;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import es.cbadenes.lab.test.IntegrationTest;
import lab.BootConfig;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.librairy.model.domain.relations.Relation;
import org.librairy.model.domain.resources.Document;
import org.librairy.model.domain.resources.Resource;
import org.librairy.storage.UDM;
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
        "librairy.eventbus.host = localhost",
        "librairy.eventbus.port = 5041",
})
public class DocumentsFixer {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentsFixer.class);

    @Autowired
    UDM udm;

    @Autowired
    TemplateExecutor executor;

    @Test
    public void fixDocuments() throws IOException {

        udm.find(Resource.Type.DOCUMENT).all().parallelStream().map(res -> udm.read(Resource.Type.DOCUMENT).byUri(res
                .getUri())
                .get().asDocument()).forEach(doc -> {

            String year = fixYear(doc);
            doc.setPublishedOn(year);

            String authors = fixAuthors(doc.getAuthoredBy());
            doc.setAuthoredBy(authors);

            doc.setAuthoredOn(year);

            LOG.info("Updating " + doc.getUri());
            udm.save(doc);

        });


    }

    @Test
    public void fixTitles() throws IOException {


        Map<String,String> titles = new HashMap<>();
        titles.put("http://drinventor.eu/documents/b2a11639e0aeddb4f964689ef0983c83","Compressing and Companding High Dynamic Range Images with Subband Architectures");
        titles.put("http://drinventor.eu/documents/6c745350c62c810bff849d8e665bc6e","Shell Maps");
        titles.put("http://drinventor.eu/documents/a48e4629c206abaa6064fbb1fcb35432","Shader Algebra");
        titles.put("http://drinventor.eu/documents/4d7f33b9a87bf235f8c3c28cb4fc95be","3D TV: A Scalable System for Real-Time Acquisition, Transmission, and Autostereoscopic Display of Dynamic Scenes");
        titles.put("http://drinventor.eu/documents/9bc9641c62f546dce93cd1ceb0f5255b","All-Frequency Precomputed Radiance Transfer using Spherical Radial Basis Functions and Clustered Tensor Approximation");
        titles.put("http://drinventor.eu/documents/9b97adac934eaf851b849afe2663fde6","Precomputed Acoustic Transfer: Output-sensitive, accurate sound generation for geometrically complex vibration sources");
        titles.put("http://drinventor.eu/documents/15fc2c6f415531ee770baa121a665f6a","Intrinsic Colorization");
        titles.put("http://drinventor.eu/documents/3a82faf59d7811ff24e28d9dfc5bfa16","");
        titles.put("http://drinventor.eu/documents/5101ffee23462bd5787e6c6e7dd8958d","Paint Selection");
        titles.put("http://drinventor.eu/documents/aca88b316574caf2c7f1ccdb723e0218","Hair Meshes");
        titles.put("http://drinventor.eu/documents/d4e923923e148581d5d976547dea164a","");
        titles.put("http://drinventor.eu/documents/3036effab0edc8514f613ce07b5f2329","");
        titles.put("http://drinventor.eu/documents/4b495c08d99ba023b775ba7c6756a6fb","");
        titles.put("http://drinventor.eu/documents/b8e64fa34dbc70ca6ec859ebd37f84c7","");
        titles.put("http://drinventor.eu/documents/ec1c21d37d28dc7d344d68a836cc7bb5","Image completion with structure propagation");
        titles.put("http://drinventor.eu/documents/47a0325558c6450f428ac860923b1bd5","");
        titles.put("http://drinventor.eu/documents/a161c552e5b36973b3799691cd4ad3a9","Mixed-Integer Quadrangulation");
        titles.put("http://drinventor.eu/documents/a547862e08f7d1ca4ffad481592aad56","");
        titles.put("http://drinventor.eu/documents/5168e106f8660fa7bcf471bbfd70104a","");
        titles.put("http://drinventor.eu/documents/b79aad29364e3ab750c51d352c4792bb","Convolution Pyramids");
        titles.put("http://drinventor.eu/documents/48fe01a6f257cc3b5fa0cd336de89277","");
        titles.put("http://drinventor.eu/documents/4a659daf89a9574c25694eb6819263ec","");
        titles.put("http://drinventor.eu/documents/f564d7fa6b7709dbf7932fc1c8b803cd","");
        titles.put("http://drinventor.eu/documents/3bef3ff5e5339dbd3f554066dcb1d2c1","iWIRES: An Analyze-and-Edit Approach to Shape Manipulation");
        titles.put("http://drinventor.eu/documents/fd867ed8b51f4ba9dadd2994e9e73b47","");
        titles.put("http://drinventor.eu/documents/ee41441108c6cd257154e269ec8b5729","Material Matting");
        titles.put("http://drinventor.eu/documents/a90a4f50f0db211b18c9f9ac410f28e2","");
        titles.put("http://drinventor.eu/documents/895a92a1caee87e62717f2e33e78df9","");
        titles.put("http://drinventor.eu/documents/9efe7046c56777055fd5b11081ebd997","Point Morphology");
        titles.put("http://drinventor.eu/documents/56af279c4a2682266e6dbacdab37009d","");
        titles.put("http://drinventor.eu/documents/dc9ce2a6356aa15cc4e944bc4f30cd4","");
        titles.put("http://drinventor.eu/documents/a9da6aad64340b0091a2a3b6391bfa47","");
        titles.put("http://drinventor.eu/documents/96049087bcfe7bce2023d222cc098696","");
        titles.put("http://drinventor.eu/documents/5b67ee8a065e128b522708ed794354f3","");
        titles.put("http://drinventor.eu/documents/260c6c872dc35ac7d0fbd76039453b27","Motion Graphs");
        titles.put("http://drinventor.eu/documents/8aee7d14a5913d8241363f5289b6eb3d","");
        titles.put("http://drinventor.eu/documents/d51fa9086bee37f62ebe56c961d35203","");
        titles.put("http://drinventor.eu/documents/5e1c06940dc5cfa90e1b7df46df93b63","");
        titles.put("http://drinventor.eu/documents/d41d8cd98f00b204e9800998ecf8427e","Field-Guided Registration for Feature-Conforming Shape Composition");
        titles.put("http://drinventor.eu/documents/b1e1368d9f5d6dc97ad6f448177af073","");
        titles.put("http://drinventor.eu/documents/53d73ca8e8aefa939a5d9ad5c038dde7","");
        titles.put("http://drinventor.eu/documents/98e2ed15b9a50b428360720dffe3c3ca","PushPull++");
        titles.put("http://drinventor.eu/documents/dec8952effcc2e69d21f7f6d5ff739ea","Mesh Saliency");
        titles.put("http://drinventor.eu/documents/d73201c3df5ec3ffb077cbc7f03a03bb","");
        titles.put("http://drinventor.eu/documents/12e95ecae50598e02d29d346f0471ec1","");
        titles.put("http://drinventor.eu/documents/1ccc454b6d621880837cf9a935a2d8c2","");
        titles.put("http://drinventor.eu/documents/fc173dddceecc8de4211a3dd8ec5c53f","");
        titles.put("http://drinventor.eu/documents/dce51cb755e4c7c0411add362b2a16b7","Morphable Crowds\n");
        titles.put("http://drinventor.eu/documents/b20bb8e4a468278982d42032de34f471","");
        titles.put("http://drinventor.eu/documents/da60a3db694315484bef0bc8980ab865","");
        titles.put("http://drinventor.eu/documents/1a5fe7b869b912ca5eaeb6bb4b81b73b","");
        titles.put("http://drinventor.eu/documents/c527eed3deb6621d4e6dbd810612ef82","");
        titles.put("http://drinventor.eu/documents/e3548c25dbffc6aa081bfc7e6d8c9698","");
        titles.put("http://drinventor.eu/documents/cc9832b3b5c4de4e52b5e251abb02e1f","3D Self-Portraits");
        titles.put("http://drinventor.eu/documents/b4bf2926f90fab719757a9439403f7cc","");


        udm.find(Resource.Type.DOCUMENT).all().parallelStream().map(res -> udm.read(Resource.Type.DOCUMENT).byUri(res
                .getUri())
                .get().asDocument()).forEach(doc -> {

            String title = doc.getTitle();

            if (Strings.isNullOrEmpty(title) || title.length() > 150){
//                System.out.println("titles.put(\""+doc.getUri()+"\",\"\");");
                LOG.info(doc.getUri() + " - " + fixUri(doc.getUri()));
                String fixedTitle = titles.get(doc.getUri());
                if (!Strings.isNullOrEmpty(fixedTitle)){
                    doc.setTitle(fixedTitle);
                    udm.save(doc);
                }
            }
        });


    }


    private String fixUri (String uri){
        return "http://drinventor.dia.fi.upm.es/api/0.2"+StringUtils.substringAfterLast(uri,"drinventor.eu");
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


    private String fixAuthors(String authors){
        if (Strings.isNullOrEmpty(authors)) return "";

        return StringUtils.removeStart(authors,", ").replace(", ,",",");

    }

}
