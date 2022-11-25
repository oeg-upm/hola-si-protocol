import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class Automation {

    public static final double SPECIAL = 1.25;

    public static void main (String[] args) {

        String links1 = args[0];
        int iter = Integer.parseInt(args[1]);

        //Get and randomize clients to communicate with
        ArrayList<String> aux = listFiles("client2/data/links");
        ArrayList<String> files = new ArrayList<>();
        for(String f : aux) {
            String[] parts = f.split("\\.");
            if(parts[1].equals("rdf")) files.add(parts[0]);
        }

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
        System.out.println("Original F-Score: " + FScore1 + ", links: " + links1);

        for (int i=0; i<iter; i++) {

            //Randomize the next client to talk with
            Random rand = new Random();
            String file = files.get(rand.nextInt(files.size()));

            try {
                //For each iteration, start both clients with their configs, being client2 first
                Path path = Path.of("client1/logs/state.log");
                String logs = Files.readString(path);
                Process client2 = Runtime.getRuntime().exec("java -jar client2/hola-si-protocol.jar client2 " + file);
                Thread.sleep(2000);
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

                client1.destroy();
                client2.destroy();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        FScore1 = FScoreJSON(1);
        System.out.println("Final F-Score: " + FScore1);
        percentageTautologies();
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

    //Method that takes the output of the test and calculates the statistics to be printed out
    private static void percentageTautologies() {
        Set<Alignment> alignments = new HashSet<>();
        Set<Alignment> gold = new HashSet<>();
        Set<Alignment> tautologies = new HashSet<>();
        Set<Alignment> special = new HashSet<>();
        String file = null;
        String goldFile = null;

        //read files
        try {
            file = Files.readString(Path.of("client1/data/links/alignments.json"));
            goldFile = Files.readString(Path.of("reference.rdf"));
        }catch (Exception e) {
            e.printStackTrace();
        }

        //parse json alignments to Alignment
        JSONParser parser = new JSONParser();
        try{
            JSONObject json = (JSONObject) parser.parse(file);
            JSONArray getArray = (JSONArray) json.get("alignments");
            for(int i = 0; i < getArray.size(); i++) {
                JSONObject objects = (JSONObject) getArray.get(i);
                alignments.add(new Alignment(objects.get("entity1").toString(), objects.get("entity2").toString(), Float.valueOf(objects.get("measure").toString())));
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        int initialSize = alignments.size();

        //build gold standard from file
        Set<ArrayList<String>> loop = loopFile(goldFile);
        for (ArrayList<String> a : loop) {
            gold.add(new Alignment(a.get(0), a.get(1), Float.valueOf("1.0")));
        }

        //calculate total number of links in gold standard
        int totalMatches = 0;
        for(Alignment a : alignments) {
            for (Alignment b : gold) {
                if(a.halfEquals(b)) {
                    totalMatches++;
                    break;
                }
            }
        }

        //Separate regular alignments from tautologies and special
        for (Alignment a : alignments) {
            if(a.getMeasure() == 2) tautologies.add(a);
            else if(a.getMeasure() >= SPECIAL) special.add(a);
        }
        alignments.removeIf(a -> a.getMeasure() >= SPECIAL);

        //Count matches
        int matchesTaut = 0;
        int matchesSpecial = 0;
        int matchesRegular = 0;
        for(Alignment ref : gold) {
            for (Alignment a : tautologies) {
                if(a.halfEquals(ref)) {
                    matchesTaut++;
                }
            }
            for(Alignment a : special) {
                if(a.halfEquals(ref)) {
                    matchesSpecial++;
                }
            }
            for(Alignment a : alignments) {
                if(a.halfEquals(ref)) {
                    matchesRegular++;
                }
            }
        }

        //Count percentages and print
        double percentageTaut = matchesTaut*100 / (double) tautologies.size();
        double percentageRegular = matchesRegular*100 / (double) alignments.size();
        double percentageSpecial = matchesSpecial*100 / (double) special.size();
        double percentageTotal = totalMatches*100 / (double) initialSize;

        System.out.println("Percentage of links in gold standard: " + percentageTotal);
        System.out.println("Total tautologies created: " + tautologies.size());
        System.out.println("Percentage of tautologies in gold standard: " + percentageTaut);
        System.out.println("Total special alignments created: " + special.size());
        System.out.println("Percentage of special links in gold standard: " + percentageSpecial);
        System.out.println("Percentage of regular links in gold standard: " + percentageRegular);

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


