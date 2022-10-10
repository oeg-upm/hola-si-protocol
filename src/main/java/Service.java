import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;


public class Service{


    //METHODS


    //Access Triplestore to retrieve the own complete ontology
    public static String getOwnOntology() {

        String ontology = null;
        try {

            switch(App.CLIENT_NAME){
                case "client-1": ontology = Files.readString(Path.of("src/main/resources/client1-ontology.owl"));break;
                case "client-2": ontology = Files.readString(Path.of("src/main/resources/client2-ontology.owl"));break;
                case "client-3": ontology = Files.readString(Path.of("src/main/resources/client3-ontology.owl"));break;
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return ontology;
    }


    //Reads local file to get the configuration parameters for the own client
    public static HashMap<String, String> obtainConfig() {

        //Creates hashmap to store config values, fill and return it
        HashMap<String, String> config = new HashMap<String, String>();
        JSONParser parser = new JSONParser();
        try {
            Object obj = null;
            switch(App.CLIENT_NAME){
                case "client-1": obj = parser.parse(new FileReader("src/main/resources/client1-config.json"));break;
                case "client-2": obj = parser.parse(new FileReader("src/main/resources/client2-config.json"));break;
                case "client-3": obj = parser.parse(new FileReader("src/main/resources/client3-config.json"));break;
            }
            JSONObject jsonObject = (JSONObject) obj;

            config.put("ontologyName", jsonObject.get("ontology-name").toString());
            config.put("queryEndpoint", jsonObject.get("query-endpoint").toString());
            config.put("updateEndpoint", jsonObject.get("update-endpoint").toString());
            config.put("linker", jsonObject.get("linker").toString());

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return config;
    }

    //Calculates the base alignments of the own and foreign ontology based on the config file parameter
    public static String step2(String ownOntology, String foreignOntology){

        //Factory Method
        HashMap<String, String> config = obtainConfig();

        AlignmentFactory factory = new AlignmentFactory();
        IOntologyLinker linker = factory.createOntologyLinker(config.get("linker"));

        return linker.calculateAlignments(ownOntology, foreignOntology);
    }

    public static void step4(String own, String foreign){

    }


    //ROUTES


    //Client decides if it wants to perform communication (Step 0)
    public static Route parliament = (Request request, Response response) -> {
        response.type("text/html");
        if(true) { //evaluate that we need the ontology + links
            response.status(200);
        }
        else if (false) { //already have your ontology, send links only
            response.status(220);
        }
        else{ //dont want to communicate at all
            response.status(418);
        }
        return "Parliament done";
    };

    //Client receives other client's ontology and send its own as response
    public static Route exchangeOntology = (Request request, Response response) -> {
        response.type("application/json");
        response.status(200);
        request.body();

        return getOwnOntology();
    };

    //Client receives other client's alignments and performs steps 2 and 4 before returning its alignments via response (step 3) to keep execution single threaded
    public static Route exchangeAlignments = (Request request, Response response) -> {

        response.type("application/json");
        response.status(200);
        request.body();

        String ownAlignments = step2(getOwnOntology(), request.body());
        step4(ownAlignments, request.body());

        return ownAlignments;
    };

}
