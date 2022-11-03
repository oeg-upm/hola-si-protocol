package LinkReducer.Egoist;

import App.Service;
import LinkReducer.Alignment;
import LinkReducer.ILinkReducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class EgoMaxReducer implements ILinkReducer {

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
                //Always add own alignment, with the max value of the incompatibility
                Alignment a = new Alignment(inc.get(0).getSubject(), inc.get(0).getObject(), Math.max(inc.get(0).getMeasure(), inc.get(1).getMeasure()));
                result.add(a);
            }
        }
        return result;
    }

}
