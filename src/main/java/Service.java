import org.apache.jena.sparql.resultset.ResultsFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;


public class Service {


    //METHODS


    //Access Triplestore to retrieve the own complete ontology
    public static String getOwnOntology() {

        //build query
        HashMap<String, String> config = obtainConfig(App.CLIENT_NAME);
        String query = "SELECT ?subject ?predicate ?object\n" +
                "WHERE {\n" +
                "  ?subject ?predicate ?object\n" +
                "}\n";

        //create triplestore
        Triplestore t = null;
        try{t = new Triplestore(config.get("queryEndpoint"), config.get("updateEndpoint"));}
        catch (URISyntaxException e){e.printStackTrace();}

        //perform query
        ByteArrayOutputStream baos = null;
        try{baos =t.query(query, ResultsFormat.FMT_RS_JSON);}
        catch (Exception e){e.printStackTrace();}

        /*String a = baos.toString();
        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {json = (JSONObject) parser.parse(a);}
        catch (ParseException e){e.printStackTrace();}*/

        //System.out.println(json);
        return baos.toString();
    }


    //Reads local file to get the configuration parameters for the own client
    public static HashMap<String, String> obtainConfig(String clientName) {

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


        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return config;
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
        System.out.println(request.body());

        response.body(getOwnOntology());
        return getOwnOntology();
    };

    public static Route exchangeAlignments = (Request request, Response response) -> {

        return null;
    };



}
