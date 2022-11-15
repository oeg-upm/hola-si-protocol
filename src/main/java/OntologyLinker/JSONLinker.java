package OntologyLinker;

import App.App;
import LinkReducer.Alignment;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class JSONLinker implements IOntologyLinker {

    public Set<Alignment> calculateAlignments(String ownOntology, String foreignOntology) {
        Set<Alignment> result = new HashSet<>();
        String alignments = null;

        try {
            alignments = Files.readString(Path.of(App.CLIENT_NAME + "/data/links/alignments.json"));
        }catch (Exception e) {
            e.printStackTrace();
        }

        JSONParser parser = new JSONParser();
        try{
            JSONObject json = (JSONObject) parser.parse(alignments);
            JSONArray getArray = (JSONArray) json.get("alignments");
            for(int i = 0; i < getArray.size(); i++) {
                JSONObject objects = (JSONObject) getArray.get(i);
                result.add(new Alignment(objects.get("entity1").toString(), objects.get("entity2").toString(), Float.valueOf(objects.get("measure").toString())));
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

}
