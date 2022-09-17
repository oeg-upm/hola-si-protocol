import org.apache.jena.sparql.resultset.ResultsFormat;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import static spark.Spark.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class App extends Object{

    public static void main (String args[]) throws Exception{
        //staticFiles.location("/public");
        setUp();
        get("/hello", (req, res) -> "Hello World");
        //Controller.manageRequests();
        System.out.println("ey");

    }

    public static void setUp() throws Exception {
        //arrancar jena fuseki
        //conseguir uuid map
        //conseguir ontología

        HashMap<String, String> config = obtainConfig("client-1");
        probarQuery(config);
        mandarRequest();
    }

    private static HashMap<String, String> obtainConfig(String clientName) {
        HashMap<String, String> config = new HashMap<String, String>();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("src/main/resources/config.json"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject client = (JSONObject) jsonObject.get(clientName);
            System.out.println(client);

            config.put("ontologyName", client.get("ontology-name").toString());
            config.put("queryEndpoint", client.get("query-endpoint").toString());
            config.put("updateEndpoint", client.get("update-endpoint").toString());


        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return config;
    }

    private static void probarQuery(HashMap<String, String> config) throws Exception {
        String query = "SELECT ?subject ?predicate ?object\n" +
                "WHERE {\n" +
                "  ?subject ?predicate ?object\n" +
                "}\n" +
                "LIMIT 25";
        //triplestore que se construya mediante fichero de config json, que sea OBLIGATORIO para la configuracion de la app (triplestore, heuristicas del paso 4 específicas para cada cliente, etc)
        Triplestore t = new Triplestore(config.get("queryEndpoint"), config.get("updateEndpoint"));
        ByteArrayOutputStream baos =t.query(query, ResultsFormat.FMT_RS_JSON);
        System.out.println(baos);


        String a = baos.toString();
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(a);
        System.out.println(json);
    }

    private static void mandarRequest(){

    }
}
