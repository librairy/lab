/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.graphs.domain;

import lombok.Data;

/**
 * Created on 12/07/16:
 *
 * @author cbadenes
 */
@Data
public class Node {


    private final Integer group;
    private String id;

    public Node(String id, Integer group){
        this.id = id;
        this.group = group;
    }
}
