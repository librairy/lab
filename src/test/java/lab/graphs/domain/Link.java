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
public class Link {

    private String source;
    private String target;
    private Integer value;

    public Link(String source,String target,Integer value){
        this.source = source;
        this.target = target;
        this.value = value;
    }
}
