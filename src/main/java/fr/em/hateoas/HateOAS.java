package fr.em.hateoas;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class HateOAS {

    @Getter
    Map<String, String> links;

    public HateOAS(){
        links = new HashMap<>();
    }

    public void addLink(String key, String link){
        links.put(key, link);
    }
}
