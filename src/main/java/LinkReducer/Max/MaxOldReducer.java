package LinkReducer.Max;

import App.Service;
import LinkReducer.Alignment;
import LinkReducer.ILinkReducer;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;


public class MaxOldReducer implements ILinkReducer {

    //Main method. Takes both alignments from clients and returns a reduced combination of them
    public Set<Alignment> reduceIncompatibilities(Set<ArrayList<Alignment>> incompatibilities) {
        Set<Alignment> result = new HashSet<>();

        for(ArrayList<Alignment> inc : incompatibilities){

            //Check for tautologies
            HashMap<String, String> config = Service.obtainConfig();
            if(inc.get(0).getMeasure().equals(Float.parseFloat(config.get("tautologyValue")))){
                result.add(inc.get(0));
                break;
            }
            else if (inc.get(1).getMeasure().equals(Float.parseFloat(config.get("tautologyValue")))){
                result.add(inc.get(0));
                break;
            }
            //Check for special alignment vs non-special
            if(inc.get(0).getMeasure() >= Float.parseFloat(config.get("specialValue")) && inc.get(1).getMeasure() < Float.parseFloat(config.get("specialValue"))){
                result.add(inc.get(0));
                break;
            }
            else if (inc.get(0).getMeasure() < Float.parseFloat(config.get("specialValue")) && inc.get(1).getMeasure() >= Float.parseFloat(config.get("specialValue"))){
                result.add(inc.get(0));
                break;
            }

            else {
                //In this step, all incompatibilities are made of non-equal alignments, but they might have the same measure. If they do, select always the own
                if (inc.get(0).getMeasure().equals(inc.get(1).getMeasure())) {
                    Alignment a = new Alignment(inc.get(0).getSubject(), inc.get(0).getObject(), inc.get(0).getMeasure());
                    result.add(a);
                }
                //If they have different measure, select the higher one and assign it its old value
                else if (inc.get(0).getMeasure() > inc.get(1).getMeasure()) {
                    Alignment a = new Alignment(inc.get(0).getSubject(), inc.get(0).getObject(), inc.get(0).getMeasure());
                    result.add(a);
                } else {
                    Alignment a = new Alignment(inc.get(1).getSubject(), inc.get(1).getObject(), inc.get(1).getMeasure());
                    result.add(a);
                }
            }
        }
        return result;
    }
}