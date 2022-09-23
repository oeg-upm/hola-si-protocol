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

    //First method executed. Since client and server are the same (p2p), we need to detect which client is the one starting the communication
    public static void init(){
        if(App.CLIENT_NAME.equals(App.CLIENT_STARTER)) {

            //If the client is the starter, perform the first steps without going passive (waiting for the other to make requests)
            int step0Code = -1;
            try {step0Code = step0();}
            catch (IOException e){e.printStackTrace();}

            //If client responds with 200, go to step 1
            if(step0Code == 200) {
                System.out.println("Parliament code 200, proceeding to step 1");
                step1();
            }
            //else if(step0Code == 220) step3();
        }

        //If the client is the passive, wait for the other to start communication
        manageRequests();
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
    private static void step1(){

        //First we get own ontology and send it. The same method who sends also returns the other client's ontology
        String ontology = Service.getOwnOntology();
        String receivedOntology = "";
        try{receivedOntology = sendOntology(ontology);}
        catch (ConnectException e){ e.printStackTrace();}

        System.out.println(receivedOntology);

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
                byte[] input = ontology.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            //get data recieved
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
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
