/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.graphs.domain;

import lombok.Data;

import java.util.List;

/**
 * Created on 12/07/16:
 *
 * @author cbadenes
 */
@Data
public class Graph {

    private List<Node> nodes;
    private List<Link> links;
}
