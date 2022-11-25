import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.text.DecimalFormat;

public class Automation {

    public static void main (String[] args) {

        String links1 = args[0];

        //Get and randomize clients to communicate with
        ArrayList<String> aux = listFiles("client2/data/links");
        ArrayList<String> files = new ArrayList<>();
        for(String f : aux) {
            String[] parts = f.split("\\.");
            if(parts[1].equals("rdf")) files.add(parts[0]);
        }
        Collections.shuffle(files);

        //Calculate initial F-Score either from rdf or json
        double FScore1 = 0;
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("client1/data/config/config.json"));
            JSONObject jsonObject = (JSONObject) obj;
            if(jsonObject.get("linker").toString().equals("file")) FScore1 = FScore(1, links1);
            else FScore1 = FScoreJSON(1);

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.###", otherSymbols);

        System.out.print(df.format(FScore1) + ", ");

        int i=0;
        for (String file : files) {
            i++;
            try {
                //For each iteration, start both clients with their configs, being client2 first
                Path path = Path.of("client1/logs/state.log");
                String logs = Files.readString(path);
                Process client2 = Runtime.getRuntime().exec("java -jar client2/hola-si-protocol.jar client2 " + file);
                Thread.sleep(3000);
                Process client1 = Runtime.getRuntime().exec("java -jar client1/hola-si-protocol.jar client1 " + links1);

                while (true) {
                    //Each 500ms, check if client1 finished in order to start next iteration
                    Thread.sleep(500);
                    String logsNew = Files.readString(path);
                    if (!logs.equals(logsNew)) {
                        break;
                    }
                    //printClients(client1, client2);
                }

                FScore1 = FScoreJSON(1);

                System.out.print("\n" + df.format(FScore1) + ", " + file);

                client1.destroy();
                client2.destroy();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Method that calculates F-Score from rdf file
    private static double FScore (int client, String linksFile) {

        String file = null;
        String gold = null;

        try {
            if(client == 1) file = Files.readString(Path.of("client1/data/links/" + linksFile + ".rdf"));
            else file = Files.readString(Path.of("client2/data/links/" + linksFile + ".rdf"));
            gold = Files.readString(Path.of("reference.rdf"));
        }catch (Exception e) {
            e.printStackTrace();
        }

        Set<ArrayList<String>> links = loopFile(file);
        Set<ArrayList<String>> reference = loopFile(gold);

        return calculateFScore(links, reference);
    }

    //Method that calculates F-Score from json file
    private static double FScoreJSON (int client) {

        String file = null;
        String gold = null;

        try {
            if(client == 1) file = Files.readString(Path.of("client1/data/links/alignments.json"));
            else file = Files.readString(Path.of("client2/data/links/alignments.json"));
            gold = Files.readString(Path.of("reference.rdf"));
        }catch (Exception e) {
            e.printStackTrace();
        }

        Set<ArrayList<String>> reference = loopFile(gold);
        Set<ArrayList<String>> links = new HashSet<>();

        JSONParser parser = new JSONParser();
        try{
            JSONObject json = (JSONObject) parser.parse(file);
            JSONArray getArray = (JSONArray) json.get("alignments");
            for(int i = 0; i < getArray.size(); i++) {
                ArrayList<String> aux = new ArrayList<>();
                JSONObject objects = (JSONObject) getArray.get(i);
                aux.add(objects.get("entity1").toString());
                aux.add(objects.get("entity2").toString());
                links.add(aux);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return calculateFScore(links, reference);
    }

    //method that reads rdf file and extracts its alignmets to a more useful data type
    private static Set<ArrayList<String>> loopFile (String file){
        Set<ArrayList<String>> result = new HashSet<>();
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            ArrayList<String> link = new ArrayList<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("entity1")) {
                    String[] parts = line.split("\"");
                    for(String part : parts) if(part.contains("http:")) link.add(part);
                }
                else if(line.contains("entity2")){
                    String[] parts = line.split("\"");
                    for(String part : parts) if(part.contains("http:")) link.add(part);
                }
                if(link.size() == 2){
                    result.add(link);
                    break;
                }
            }
        }
        return result;
    }

    //Method that calculates F-Score from our data type
    private static double calculateFScore(Set<ArrayList<String>> links, Set<ArrayList<String>> reference) {
        double tp = 0;
        for (ArrayList<String> a : links) {
            for (ArrayList<String> b : reference) {
                if((a.get(0).equals(b.get(0)) || a.get(0).equals(b.get(1)))
                        && (a.get(1).equals(b.get(0)) || a.get(1).equals(b.get(1)))) {
                    tp++;
                    break;
                }
            }
        }
        double fp = links.size() - tp;
        double fn = reference.size() - tp;
        return 2*tp / (2*tp + fp + fn);
    }

    //Method that reads all files form a directory and returns their names
    private static ArrayList<String> listFiles(String dir) {
        return new ArrayList<String>(Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet()));
    }

    //Auxiliar method used to print clients individual output
    private static void printClients(Process client1, Process client2) throws IOException {
        BufferedReader c1in = new BufferedReader(new
                InputStreamReader(client1.getInputStream()));

        BufferedReader c1ou = new BufferedReader(new
                InputStreamReader(client1.getErrorStream()));

        BufferedReader c2in = new BufferedReader(new
                InputStreamReader(client2.getInputStream()));

        BufferedReader c2ou = new BufferedReader(new
                InputStreamReader(client2.getErrorStream()));

        String s = null;
        while ((s = c1in.readLine()) != null) {
            System.out.println(s);
        }
        while ((s = c1ou.readLine()) != null) {
            System.out.println(s);
        }
        while ((s = c2in.readLine()) != null) {
            System.out.println(s);
        }
        while ((s = c2ou.readLine()) != null) {
            System.out.println(s);
        }
    }

}


