package lab.ma.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 07/07/16:
 *
 * @author cbadenes
 */
@Data
public class WordsDistribution {


    private final String id;

    private Map<String,Double> words;

    public WordsDistribution(String id){
        this.id = id;
        this.words = new HashMap<>();
    }

    public WordsDistribution(String id, Map<String,Double> words){
        this.id = id;
        this.words = words;
    }

    public WordsDistribution add(String word, Double weight){
        this.words.put(word,weight);
        return this;
    }

}
