package App;

import App.App;
import LinkReducer.Alignment;
import org.apache.jena.rdf.model.Model;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;

import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class Controller {
    private static String foreignOntology;

    //Only active client will execute this method. This client will perform requests to the passive client
    public static void initProcess() {

        String ownOntology = Service.getOwnOntology();
        foreignOntology = "";
        Set<Alignment> ownAlignments = null;
        Set<Alignment> foreignAlignments;

        //Obtain own ontology and proceed to step 0
        int step0Code = -1;
        try {step0Code = step0();}
        catch (IOException e){e.printStackTrace();}

        //If client responds with 200, go to step 1
        if(step0Code == 200) {
            System.out.println("Parliament code 200, proceeding to step 1");
            foreignOntology = step1(ownOntology);
            ownAlignments = Service.step2(ownOntology, foreignOntology);
        }
        //if client responds with 220, go to step 3. Client does not need our ontology
        else if(step0Code == 220) {
            System.out.println("Parliament code 220, proceeding to step 3");
            System.out.println(foreignOntology);
            ownAlignments = Service.step2(ownOntology, foreignOntology);
        }
        System.out.println("First alignments obtained. Sending to peer");
        foreignAlignments=step3(ownAlignments);
        System.out.println("Alignments received. Proceeding to step 4, reduction");
        Service.step4(ownAlignments, foreignAlignments);
        System.out.println("Alignments reduced to json output\nTest finished.");
    }


    //Step 0 of the communication. The active server asks to the passive if it wishes to start the process, and in which step
    private static int step0() throws IOException{
        //Send parliament petition and continue based on its value
        int code = -1;
        try {code = sendParliamentRequest();}
        catch (ConnectException e) {e.printStackTrace();}

        //if client refuses, immediately throw an exception
        if (code == 418) throw new ConnectException("Target client refused to communicate");

        return code;
    }


    //In Step 1, first active client send its ontology to the passive, who, when received, send its own to the active
    private static String step1(String ontology){

        //First we get own ontology and send it. The same method who sends also returns the other client's ontology
        String receivedOntology = null;
        try{receivedOntology = sendOntology(ontology);}
        catch (ConnectException e){ e.printStackTrace();}

        return receivedOntology;

    }

    //We send our alignments, result of step 2, to the other client, and wait to obtain its alignments to procede with step 4
    private static Set<Alignment> step3(Set<Alignment> links){
        Set<Alignment> alignments = null;
        try{alignments = sendAlignments(links);}
        catch (ConnectException e){ e.printStackTrace();}
        return alignments;
    }



    //Communication method of Step 0, the active client asks the passive if it wishes to communicate
    private static int sendParliamentRequest() throws ConnectException {
        HttpURLConnection conn = null;
        int code = 300;
        URL url;
        try{
            url = new URL("http://localhost:" + Service.obtainConfig().get("targetPort") + "/api/parliament");

            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            //Get response code and return it to evaluate
            code = conn.getResponseCode();

            //If the client responds with 220, it doesn't need our ontology, but will return its in case we don't have it yet.
            if (code == 220) {
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim() + " ");
                    }
                    foreignOntology = response.toString();
                }
            }

            conn.disconnect();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            throw new ConnectException("Cannot connect to target client. Check if it is running");
        }catch (IOException e) {
            e.printStackTrace();
        }

        return code;
    }


    //Communication method of Step 1, gets own ontology and sends it as a POST, expecting the other server's ontology in the response
    private static String sendOntology(String ontology) throws ConnectException{
        HttpURLConnection conn = null;
        String resp = "";
        URL url;
        try{
            url = new URL("http://localhost:" + Service.obtainConfig().get("targetPort") + "/api/exchange/ontology");

            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            //write data to send
            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = ontology.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            //get data recieved
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim() + " ");
                }
                resp = response.toString();
            }

            conn.disconnect();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            throw new ConnectException("Cannot connect to target client. Check if it is running");
        }catch (IOException e) {
            e.printStackTrace();
        }

        finally {
            conn.disconnect();
        }
        return resp;
    }

    //Communication method of Step 3, active client sends its alignments in JSON to passive client, and wait for it to return its before proceeding to Step 4
    //Data here will be sent in JSON format, standardized to the API protocol. Clients must work with this protocol in order to assure communication
    private static Set<Alignment> sendAlignments(Set<Alignment> alignments) throws ConnectException{
        HttpURLConnection conn = null;
        String resp = "";
        URL url;

        String JSONData = "{\"alignments\":[";
        Iterator<Alignment> it = alignments.iterator();
        while(it.hasNext()) {
            Alignment next = it.next();
            JSONData = JSONData.concat(next.toJSON());
            if(it.hasNext()) JSONData = JSONData.concat(",\n");
        }
        JSONData = JSONData.concat("]}");

        try{
            url = new URL("http://localhost:" + Service.obtainConfig().get("targetPort") + "/api/exchange/alignments");

            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod("PUT");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            //write data to send
            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = JSONData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            //get data recieved
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim() + " ");
                }
                resp = response.toString();
            }

            conn.disconnect();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            throw new ConnectException("Cannot connect to target client. Check if it is running");
        }catch (IOException e) {
            e.printStackTrace();
        }

        Set<Alignment> set = new HashSet<>();
        JSONParser parser = new JSONParser();
        try{
            JSONObject json = (JSONObject) parser.parse(resp);
            JSONArray getArray = (JSONArray) json.get("alignments");
            for(int i = 0; i < getArray.size(); i++) {
                JSONObject objects = (JSONObject) getArray.get(i);
                set.add(new Alignment(objects.get("entity1").toString(), objects.get("entity2").toString(), Float.valueOf(objects.get("measure").toString())));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }


    //Server method. Creates the api routes for the passive client to listen to
    public static void manageRequests(){

        path("/api", () -> {

            get("/parliament", Service.parliament);

            path("/exchange", () -> {
                post("/ontology", Service.exchangeOntology);
                put("/alignments", Service.exchangeAlignments);
            });

        });
        get("/hello", (req, res) -> "Hello World");
    }

}