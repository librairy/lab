/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package lab.ma.domain;

/**
 * Created by cbadenes on 14/02/16.
 */
public class AgenFactory {


    public static Agent.Type from(String sense){
        switch(sense.toLowerCase()){
            case "abstract" : return Agent.Type.ABSTRACT;
            case "approach" : return Agent.Type.APPROACH;
            case "background" : return Agent.Type.BACKGROUND;
            case "challenge" : return Agent.Type.CHALLENGE;
            case "futurework" : return Agent.Type.FUTURE_WORK;
            case "outcome" : return Agent.Type.OUTCOME;
            default : return Agent.Type.SUMMARY;
        }
    }
}
