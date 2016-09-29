/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.distance;

import com.google.common.collect.ImmutableMap;
import lab.ma.domain.TopicAgent;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created on 01/07/16:
 *
 * @author cbadenes
 */
public class KendallsTau {

    private static final Logger LOG = LoggerFactory.getLogger(KendallsTau.class);

    public static double correlation(List<String> x1, List<String> x2) {

        double[] a1 = x1.stream().mapToDouble(w -> w.hashCode()).toArray();
        double[] a2 = x2.stream().mapToDouble(w -> w.hashCode()).toArray();


        System.out.println(Arrays.toString(a1));
        System.out.println(Arrays.toString(a2));

        double correlation  = new KendallsCorrelation().correlation(a1, a2);
        double normalized   = (correlation - (-1)) / 2;
        return normalized;
    }


    public static double correlation(List<String> vocab, Map<String,Double> x1, Map<String,Double> x2) {

        double[] a1 = vocab.stream().mapToDouble(word -> !x1.containsKey(word)? 0.0 : x1.get(word)).toArray();
        double[] a2 = vocab.stream().mapToDouble(word -> !x2.containsKey(word)? 0.0 : x2.get(word)).toArray();

//        System.out.println(Arrays.toString(a1));
//        System.out.println(Arrays.toString(a2));

        double correlation  = new KendallsCorrelation().correlation(a1, a2);
        return normalize(correlation,true);
    }

    private static double normalize(Double value, Boolean truncate){
        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.CEILING);

        double minValue = -1;
        double normalizedValue = Double.valueOf(df.format((value - minValue) / 2));

        if (!truncate) return normalizedValue;

        return (value < 0 )? 0 : normalizedValue;
    }

    public static double distance(Map<String,Double> x1, Map<String,Double> x2, WordDistance wordDistance){

        double distance = 0.0;

        Comparator<Map.Entry<String,Double>> desc = new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        };

        List<String> l1 = x1.entrySet().stream().sorted(desc).map(entry -> entry.getKey()).collect(Collectors.toList());
        List<String> l2 = x2.entrySet().stream().sorted(desc).map(entry -> entry.getKey()).collect(Collectors.toList());


        List<Tuple2<String, String>> pairs = l1.stream().
                flatMap(i -> l2.stream().filter(j -> !i.equalsIgnoreCase(j)).map(j -> new Tuple2<String, String>
                        (i, j))).
                filter(e -> e._1.hashCode() < e._2.hashCode()).
                collect(Collectors.toList());

        LOG.info("Pairs: " + pairs);


        for (Tuple2<String,String> pair: pairs){

            String i = pair._1;
            String j = pair._2;

            Integer ri1 = Integer.valueOf(l1.indexOf(i));
            Integer ri2 = Integer.valueOf(l2.indexOf(i));

            Integer rj1 = Integer.valueOf(l1.indexOf(j));
            Integer rj2 = Integer.valueOf(l2.indexOf(j));

            boolean inversion = ri1 == -1 || ri2 == -1 || rj1 == -1 || rj2 == -1 || ri1.compareTo(rj1)+ri2.compareTo(rj2)==0;

            if (inversion){

                // Element weights
                Double wi = (x1.getOrDefault(i,0.0) + x2.getOrDefault(i,0.0)) / 2.0;
                Double wj = (x1.getOrDefault(j,0.0) + x2.getOrDefault(j,0.0)) / 2.0;

                // Position weights
                Double pi   = (ri1 != -1)? (l1.size()-ri1)*(1.0/l1.size()) : (1.0/l1.size());
                Double ppi  = (ri2 != -1)? (l2.size()-ri2)*(1.0/l2.size()) : (1.0/l2.size());
                Double Pi   = (ri1 == ri2)? 1.0 : (pi-ppi)/(ri1-ri2);

                Double pj   = (rj1 != -1)? (l1.size()-rj1)*(1.0/l1.size()) : (1.0/l1.size());
                Double ppj  = (rj2 != -1)? (l2.size()-rj2)*(1.0/l2.size()) : (1.0/l2.size());
                Double Pj   = (rj1 == rj2)? 1.0 : (pj-ppj)/(rj1-rj2);


                // Element Similarities
                Double Dij = wordDistance.between(i,j);

                distance += wi*wj*Pi*Pj;
            }
        }


        return distance;
    }

    public static void main(String[] args){

        final List<String> vocabulary = Arrays.asList(new String[]{"a","b","c","d","e","f","g","h","i","j",
                "k","l", "m","n"});

        ImmutableMap<String, Double> t1 = ImmutableMap.of("a", 0.7, "b", 0.5, "c", 0.3);

        ImmutableMap<String, Double> t2 = ImmutableMap.of("a", 0.8, "b", 0.4, "e", 0.1);

        ImmutableMap<String, Double> t3 = ImmutableMap.of("f", 0.6, "g", 0.5, "h", 0.4);

        ImmutableMap<String, Double> t4 = ImmutableMap.of("i", 0.9, "c", 0.3, "k", 0.2);

        System.out.println("t1-t2:" + correlation(vocabulary,t1,t2));
        System.out.println("t1-t3:" + correlation(vocabulary,t1,t3));
        System.out.println("t1-t4:" + correlation(vocabulary,t1,t4));

        System.out.println("t2-t3:" + correlation(vocabulary,t2,t3));
        System.out.println("t2-t4:" + correlation(vocabulary,t2,t4));

        System.out.println("t3-t4:" + correlation(vocabulary,t3,t4));

    }
}
