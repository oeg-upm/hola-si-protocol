package App;

import App.App;
import LinkReducer.Alignment;
import LinkReducer.ILinkReducer;
import OntologyLinker.IOntologyLinker;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class Service{

    private static String foreignOntology;


    //METHODS


    //Access ontology file to retrieve the own complete ontology
    public static String getOwnOntology() {

        String ontology = null;
        try {
            ontology = Files.readString(Path.of(App.CLIENT_NAME + "/data/ontologies/ontology.owl"));
        }catch (Exception e) {
            e.printStackTrace();
        }

        return ontology;
    }


    //Reads local file to get the configuration parameters for the own client
    public static HashMap<String, String> obtainConfig() {

        //Creates hashmap to store config values, fill and return it
        HashMap<String, String> config = new HashMap<>();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(App.CLIENT_NAME + "/data/config/config.json"));

            JSONObject jsonObject = (JSONObject) obj;

            config.put("starts", jsonObject.get("starts").toString());
            config.put("peer", jsonObject.get("peer").toString());
            config.put("linker", jsonObject.get("linker").toString());
            config.put("reducer", jsonObject.get("reducer").toString());
            config.put("targetPort", jsonObject.get("target-port").toString());
            config.put("incrementValue", jsonObject.get("increment").toString());
            config.put("tautologyValue", jsonObject.get("tautology").toString());
            config.put("specialValue", jsonObject.get("special").toString());
            config.put("update", jsonObject.get("update").toString());


        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return config;
    }

    //Calculates the base alignments of the own and foreign ontology based on the config file parameter
    public static Set<Alignment> step2(String ownOntology, String foreignOntology){

        //Factory Method
        AlignmentFactory factory = new AlignmentFactory();
        IOntologyLinker linker = factory.createOntologyLinker(obtainConfig().get("linker"));

        return linker.calculateAlignments(ownOntology, foreignOntology);
    }


    //Step 4 main and auxiliary methods


    public static void step4(Set<Alignment> ownAlignments, Set<Alignment> foreignAlignments){
        //Create duplicates to prevent the originals being modified
        Set<Alignment> ownReducedAlignments = new HashSet<>(ownAlignments);
        Set<Alignment> foreignReducedAlignments = new HashSet<>(foreignAlignments);

        //Obtain incompatibilities
        Set<ArrayList<Alignment>> incompatibilities = getIncompatibilities(ownReducedAlignments, foreignReducedAlignments);

        //Create final set of alignments
        Set<Alignment> reduced = reduceWithoutIncompatibilities(ownReducedAlignments, foreignReducedAlignments, incompatibilities);

        //Factory method to select reducer algorithm
        AlignmentFactory factory = new AlignmentFactory();
        ILinkReducer linker = factory.createLinkReducer(obtainConfig().get("reducer"));

        //reduce incompatibilities and add the result to the final set
        Set<Alignment> incompatibilitiesReduced = linker.reduceIncompatibilities(incompatibilities);
        reduced.addAll(incompatibilitiesReduced);

        if(Boolean.parseBoolean(obtainConfig().get("update"))) {
            //Apply increments and upload to file
            Set<Alignment> incremented = applyIncrements(ownAlignments, reduced);
            updateAlignments(incremented);
        }
    }

    //Method that given both sets of alignments, extracts all pairs which create an incompatibility
    private static Set<ArrayList<Alignment>> getIncompatibilities (Set<Alignment> ownReducedAlignments, Set<Alignment> foreignReducedAlignments) {
        Set<ArrayList<Alignment>> result = new HashSet<>();

        //Get all incompatibilities into only one structure
        //Each incompatibility is a pair of alignments, stored as an arraylist of length 2
        //All incompatibilities are stored in a Set, being that a set of arrays (list of incompatibilities)
        for (Alignment a : ownReducedAlignments) {
            for (Alignment b : foreignReducedAlignments) {
                if(a.getSubject().equals(b.getSubject()) || a.getSubject().equals(b.getObject()) || a.getObject().equals(b.getSubject()) || a.getObject().equals(b.getObject())){
                    ArrayList<Alignment> aux = new ArrayList<>();
                    aux.add(a);
                    aux.add(b);
                    //eliminate as well incompatibilities of two alignments that are completely the same
                    if(!(a.equals(b)))result.add(aux);
                }
            }
        }

        return result;
    }

    //Method that extracts those alignments from both clients which do not create any incompatibility and store them in a shared arraylist
    //also deleting in the process those alignments which do create incompatibilities
    private static Set<Alignment> reduceWithoutIncompatibilities(Set<Alignment> ownReducedAlignments, Set<Alignment> foreignReducedAlignments, Set<ArrayList<Alignment>> incompatibilities) {
        Set<Alignment> reduced = new HashSet<>();
        for(ArrayList<Alignment> arr : incompatibilities){
            ownReducedAlignments.removeIf(a -> arr.get(0).equals(a));
            foreignReducedAlignments.removeIf(a -> arr.get(1).equals(a));
        }
        for(Alignment a : ownReducedAlignments)reduced.add(a);
        for(Alignment a : foreignReducedAlignments)reduced.add(a);
        return reduced;
    }

    //Method that applies increments to the resulting set of step 4, only if those alignments met the requirements for being incremented
    private static Set<Alignment> applyIncrements(Set<Alignment> oldAlignments, Set<Alignment> newAlignments) {
        Set<Alignment> result = new HashSet<>();
        HashMap<String, String> config = obtainConfig();
        Float increment = Float.valueOf(config.get("incrementValue"));
        Float tautology = Float.valueOf(config.get("tautologyValue"));

        boolean found;
        for(Alignment a : newAlignments) {
            found = false;
            for (Alignment b : oldAlignments) {
                //If a new alignment was also in the old set, reward it with an increment of its measure
                if(a.halfEquals(b)){
                    float measure = a.getMeasure() + increment;
                    if(measure > tautology) measure = tautology;
                    Alignment al = new Alignment(a.getSubject(), a.getObject(), measure);
                    result.add(al);
                    found = true;
                    break;
                }
            }
            //If a new alignment was not in the old set, add it as it is
            if(!found) {
                result.add(a);
            }
        }
        return result;
    }

    //Method that overrides the JSON alignment file with the new set after step 4
    private static void updateAlignments(Set<Alignment> alignments) {
        String JSONData = "{\n\"alignments\":[\n";
        Iterator<Alignment> it = alignments.iterator();
        while(it.hasNext()) {
            Alignment next = it.next();
            JSONData = JSONData.concat(next.toJSON() + "\n");
            if(it.hasNext()) JSONData = JSONData.concat(",");
        }
        JSONData = JSONData.concat("]}");

        try {
            FileWriter f = new FileWriter(App.CLIENT_NAME + "/data/links/alignments.json", false);
            f.write(JSONData);
            f.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        if(Service.obtainConfig().get("linker").equals("file")){
            String config = null;
            try{
                config = Files.readString(Path.of(App.CLIENT_NAME + "/data/config/config.json"));
            }catch (Exception e) {
                e.printStackTrace();
            }


            Scanner scanner = new Scanner(config);
            String lines = "";
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                if(line.contains("linker")) {
                    line = line.replace("file", "json");
                }
                lines = lines.concat(line + "\n");
            }
            try {
                FileWriter f = new FileWriter(App.CLIENT_NAME + "/data/config/config.json", false);
                f.write(lines);
                f.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //ROUTES


    //Client decides if it wants to perform communication (Step 0)
    public static Route parliament = (Request request, Response response) -> {
        response.type("text/html");
        if(true) { //evaluate that we need the ontology + links
            response.status(200);
            System.out.println("Accepting parliament with peer");
        }
        else if (false) { //already have your ontology, send links only
            response.status(220);
            System.out.println("Accepting parliament with peer but straight to step 3");
            return getOwnOntology();
        }
        else{ //dont want to communicate at all
            response.status(418);
            System.out.println("Declining parliament with peer");
        }
        return "Parliament done";
    };

    //Client receives other client's ontology and send its own as response
    public static Route exchangeOntology = (Request request, Response response) -> {
        response.type("application/json");
        response.status(200);

        foreignOntology = request.body();

        System.out.println("Sending own ontology to peer and receiving its to calculate first alignments");

        return getOwnOntology();
    };

    //Client receives other client's alignments and performs steps 2 and 4 before returning its alignments via response (step 3) to keep execution single threaded
    public static Route exchangeAlignments = (Request request, Response response) -> {

        response.type("application/json");
        response.status(200);
        request.body();

        Set<Alignment> ownAlignments = step2(getOwnOntology(), foreignOntology);
        Set<Alignment> foreignAlignments = new HashSet<>();

        Set<Alignment> send = new HashSet<>(ownAlignments);

        JSONParser parser = new JSONParser();
        try{
            JSONObject json = (JSONObject) parser.parse(request.body());
            JSONArray getArray = (JSONArray) json.get("alignments");
            for(int i = 0; i < getArray.size(); i++) {
                JSONObject objects = (JSONObject) getArray.get(i);
                foreignAlignments.add(new Alignment(objects.get("entity1").toString(), objects.get("entity2").toString(), Float.valueOf(objects.get("measure").toString())));
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Getting alignments from peer and reducing both sets");
        step4(ownAlignments, foreignAlignments);

        String JSONData = "{\"alignments\":[";
        Iterator<Alignment> it = send.iterator();
        while(it.hasNext()) {
            Alignment next = it.next();
            JSONData = JSONData.concat(next.toJSON());
            if(it.hasNext()) JSONData = JSONData.concat(",");
        }
        JSONData = JSONData.concat("]}");

        System.out.println("Sending own alignments to peer");
        return JSONData;
    };

}
