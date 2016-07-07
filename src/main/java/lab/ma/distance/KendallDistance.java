package lab.ma.distance;

import com.google.common.collect.ImmutableMap;
import lab.ma.domain.TopicAgent;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created on 01/07/16:
 *
 * @author cbadenes
 */
public class KendallDistance  {

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
