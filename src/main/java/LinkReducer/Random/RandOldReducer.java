package LinkReducer.Random;

import App.Service;
import LinkReducer.Alignment;
import LinkReducer.ILinkReducer;

import java.util.*;

public class RandOldReducer implements ILinkReducer {

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
                //Select a random alignment from the incompatibility, biased towards the better measured, and take its old measure
                if (selectRandom(inc)) {
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

    //Auxiliary method to select the random alignment
    private boolean selectRandom(ArrayList<Alignment> inc) {
        float diff = Math.abs(inc.get(0).getMeasure() - inc.get(1).getMeasure());

        if(diff >= 0.5) return inc.get(0).getMeasure() >= inc.get(1).getMeasure();

        else {
            float p;
            if (inc.get(0).getMeasure() >= inc.get(1).getMeasure()) p = (float) 0.5 + diff;
            else p = (float) 0.5 - diff;

            Random rand = new Random();
            return !(rand.nextFloat() >= p);
        }

    }

}
