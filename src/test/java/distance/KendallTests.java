package distance;

import com.google.common.collect.ImmutableMap;
import lab.ma.distance.KendallDistance;
import lab.ma.domain.TopicAgent;
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



}
