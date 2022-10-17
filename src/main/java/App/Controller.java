package App;

import App.App;
import org.apache.jena.rdf.model.Model;

import java.io.*;

import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;



public class Controller {

    //Only active client will execute this method. This client will perform requests to the passive client
    public static void initProcess() {

        //Obtain own ontology and proceed to step 0
        int step0Code = -1;
        try {step0Code = step0();}
        catch (IOException e){e.printStackTrace();}

        String ownOntology = Service.getOwnOntology();
        String foreignOntology;
        String ownAlignments = null;
        String foreignAlignments;
        Model newAlignments = null;

        //If client responds with 200, go to step 1
        if(step0Code == 200) {
            System.out.println("Parliament code 200, proceeding to step 1");
            foreignOntology = step1(ownOntology);
            ownAlignments = Service.step2(ownOntology, foreignOntology);
        }
        //if client responds with 220, go to step 3. Client does not need our ontology
        else if(step0Code == 220) {
            System.out.println("Parliament code 220, proceeding to step 3");
        }
        foreignAlignments=step3(ownAlignments);
        newAlignments = Service.step4(ownAlignments, foreignAlignments);

        newAlignments.write(System.out, "TTL");

    }


    //Step 0 of the communication. The active server asks to the passive if it wishes to start the process, and in which step
    private static int step0() throws IOException{
        //Ask user to start process
        System.out.println("Press enter when ready to start the process");
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        bufferRead.readLine();

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
    private static String step3(String links){
        String alignments = null;
        try{alignments = sendAlignments(links);}
        catch (ConnectException e){ e.printStackTrace();}
        return alignments;
    }


    //Communication method of Step 0, the active client asks the passive if it wishes to communicate
    private static int sendParliamentRequest() throws ConnectException {
        HttpURLConnection conn = null;
        int code = 300;
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();
        URL url = null;
        try{
            switch (App.CLIENT_TARGET_NAME){
                case "client-1": url = new URL("http://localhost:4567/api/parliament");break;
                case "client-2": url = new URL("http://localhost:4568/api/parliament");break;
                case "client-3": url = new URL("http://localhost:4569/api/parliament");break;
            }
            conn = (HttpURLConnection) url.openConnection();

            // Request setup
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            code = conn.getResponseCode();

            //Get response code and return it to evaluate
            if (code >= 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
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
        return code;
    }


    //Communication method of Step 1, gets own ontology and sends it as a POST, expecting the other server's ontology in the response
    private static String sendOntology(String ontology) throws ConnectException{
        HttpURLConnection conn = null;
        String resp = "";
        URL url = null;
        try{
            switch (App.CLIENT_TARGET_NAME){
                case "client-1": url = new URL("http://localhost:4567/api/exchange/ontology");break;
                case "client-2": url = new URL("http://localhost:4568/api/exchange/ontology");break;
                case "client-3": url = new URL("http://localhost:4569/api/exchange/ontology");break;
            }
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
                byte[] input = ontology.toString().getBytes("utf-8");
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

    //Communication method of Step 3, active client sends its alignments to passive client, and wait for it to return its before proceeding to Step 4
    private static String sendAlignments(String alignments) throws ConnectException{
        HttpURLConnection conn = null;
        String resp = "";
        URL url = null;
        try{
            switch (App.CLIENT_TARGET_NAME){
                case "client-1": url = new URL("http://localhost:4567/api/exchange/alignments");break;
                case "client-2": url = new URL("http://localhost:4568/api/exchange/alignments");break;
                case "client-3": url = new URL("http://localhost:4569/api/exchange/alignments");break;
            }
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
                byte[] input = alignments.toString().getBytes("utf-8");
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