/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.minetur;

import com.google.common.collect.ImmutableMap;
import lab.ma.distance.KendallsTau;
import lab.ma.distance.WordDistance;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created on 26/07/16:
 *
 * @author cbadenes
 */
public class TopicDistance {

    private static final Logger LOG = LoggerFactory.getLogger(TopicDistance.class);


    @Test
    public void kendallsdistance(){

        Map<String,Double> t1 = ImmutableMap.of("a",0.9,"b",0.8,"c",0.7);
        Map<String,Double> t2 = ImmutableMap.of("b",0.9,"c",0.8,"a",0.7);

        LOG.info("t1: " + t1);
        LOG.info("t2: " + t2);

        WordDistance wd = new WordDistance() {
            @Override
            public Double between(String w1, String w2) {
                String key = w1+w2;
                switch(key){
                    case "ab":
                    case "ba": return 1.0;
                    case "ac":
                    case "ca": return 1.0;
                    case "bc":
                    case "cb": return 1.0;
                    default: return 1.0;
                }
            }
        };

        double distance = KendallsTau.distance(t1, t2, wd);
        LOG.info("Distance: " + distance);

    }
}
