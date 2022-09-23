import org.apache.jena.sparql.resultset.ResultsFormat;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import static spark.Spark.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class App{

    public static String CLIENT_NAME;
    public static String CLIENT_TARGET_NAME;
    public static final String CLIENT_STARTER="client-1";

    public static void main (String args[]){
        //staticFiles.location("/public");
        CLIENT_NAME = args[0];
        CLIENT_TARGET_NAME = args[1];

        setUp();
        Controller.init();

    }

    public static void setUp() {
        //arrancar jena fuseki HECHO
        //conseguir ontología HECHO
        //conseguir uuid map PENDIENTE
        //sesiones(?) PENDIENTE

        if(CLIENT_NAME.equals("client-2")) port(4568);
        HashMap<String, String> config = Service.obtainConfig(CLIENT_NAME);
    }

}
