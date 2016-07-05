package lab.ma.distance;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;

import java.util.Arrays;
import java.util.List;
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


    public static double correlation(List<String> vocab, List<String> x1, List<String> x2) {

        double[] a1 = x1.stream().mapToDouble(w -> w.hashCode()).toArray();
        double[] a2 = x2.stream().mapToDouble(w -> w.hashCode()).toArray();



        System.out.println(Arrays.toString(a1));
        System.out.println(Arrays.toString(a2));

        double correlation  = new KendallsCorrelation().correlation(a1, a2);
        double normalized   = (correlation - (-1)) / 2;
        return normalized;
    }
}
