/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.distance;

import java.util.List;

/**
 * Created on 01/07/16:
 *
 * @author cbadenes
 */
public class CommonWordsDistance {

    public static double correlation(List<String> x1, List<String> x2) {
        Integer size = x1.size();

        long common = x1.stream().filter(w -> x2.contains(w)).count();

        return common/Double.valueOf(size);
    }
}
