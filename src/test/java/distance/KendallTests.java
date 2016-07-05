package distance;

import com.google.common.collect.ImmutableMap;
import lab.ma.distance.KendallDistance;
import lab.ma.domain.TopicAgent;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 01/07/16:
 *
 * @author cbadenes
 */
public class KendallTests {


    @Test
    public void words(){

        List<String> w1 = Arrays.asList(new String[]{"casa","barco","coche"});
        List<String> w2 = Arrays.asList(new String[]{"elevador","barco","coche"});
        List<String> w3 = Arrays.asList(new String[]{"fuel","gas","humo"});
        List<String> w4 = Arrays.asList(new String[]{"indio","juego","kilo"});

        System.out.println(KendallDistance.correlation(w1,w2));
        System.out.println(KendallDistance.correlation(w1,w3));
        System.out.println(KendallDistance.correlation(w1,w4));

        System.out.println(KendallDistance.correlation(w2,w3));
        System.out.println(KendallDistance.correlation(w2,w4));

        System.out.println(KendallDistance.correlation(w3,w4));

    }

    @Test
    public void wordsAsDouble(){

        List<String> vocab = Arrays.asList(new String[]{"casa","barco","coche","elevador","fuel","gas","humo","indio","juego","kilo"});

        double[] w1 = new double[]{1.0,0.9,0.8,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
        double[] w2 = new double[]{0.0,0.9,0.8,1.0,0.0,0.0,0.0,0.0,0.0,0.0};
        double[] w3 = new double[]{0.0,0.0,0.0,0.0,1.0,0.9,0.8,0.0,0.0,0.0};
        double[] w4 = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,0.9,0.8};

        KendallsCorrelation kendalls = new KendallsCorrelation();

        System.out.println(kendalls.correlation(w1,w2));
        System.out.println(kendalls.correlation(w1,w3));
        System.out.println(kendalls.correlation(w1,w4));

        System.out.println(kendalls.correlation(w2,w3));
        System.out.println(kendalls.correlation(w2,w4));

        System.out.println(kendalls.correlation(w3,w4));

    }



}
