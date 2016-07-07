package lab.ma.domain;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sim.util.Double2D;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 07/07/16:
 *
 * @author cbadenes
 */
public class DensityClusterer {

    private static final Logger LOG = LoggerFactory.getLogger(DensityClusterer.class);

    public void calculate(List<Clusterable> points){

        int minPts = 0;
        double eps = 1.0;
        LOG.info("MinPts:" + minPts + " | EPS:" + eps);
        DBSCANClusterer clusterer = new DBSCANClusterer(eps,minPts, new EuclideanDistance());

        LOG.info("Points: " + points);

        List<Cluster> clusters = clusterer.cluster(points);

        LOG.info("Number of clusters: " + clusters.size());

        clusters.forEach(cluster -> {
            LOG.info("> Points: "+cluster.getPoints());
        });

    }


    public static void main(String[] args){

        //Points: [[8.165461922787816,5.597321021309145], [13.373402917744274,18.415183192101743], [13.014612622283328,18.326046263379148], [5.460835397481521,16.26490762811762]]



        List<Clusterable> points = Arrays.asList(new Clusterable[]{
                new AgentPosition(new Double2D(8.165461922787816, 5.597321021309145)),
                new AgentPosition(new Double2D(13.373402917744274, 18.415183192101743)),
                new AgentPosition(new Double2D(13.014612622283328, 18.326046263379148)),
                new AgentPosition(new Double2D(5.460835397481521, 16.26490762811762))
        });


        DensityClusterer clusterer = new DensityClusterer();
        clusterer.calculate(points);

    }

}
