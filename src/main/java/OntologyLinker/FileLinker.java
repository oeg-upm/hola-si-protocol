package OntologyLinker;

import App.App;
import LinkReducer.Alignment;
import OntologyLinker.IOntologyLinker;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class FileLinker implements IOntologyLinker {

    //Calculates alignments directly reading from a file containing them
    public Set<Alignment> calculateAlignments(String ownOntology, String foreignOntology) {
        String alignments = null;
        Set<Alignment> result;
        try {
            alignments = Files.readString(Path.of(App.CLIENT_NAME + "/data/links/" + App.LINKS_TYPE + ".rdf"));
        }catch (Exception e) {
            e.printStackTrace();
        }

        result = getUsefulAlignments(alignments);
        return result;
    }

    //This method iterates through the whole model and builds an arraylist of alignments as they are found
    private Set<Alignment> getUsefulAlignments(String model){
        Set<Alignment> alignments = new HashSet<Alignment>();

        Scanner scanner = new Scanner(model);

        //Two loops are needed, the outer while for adding alignments to the array, and the inner while for constructing each Alignment
        while (scanner.hasNextLine()){
            Alignment alignment = new Alignment();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                //Iterating through the model until all two entities and its measure are found.
                //They have to be in the same cell, so regardless of the order or quantity of information in each cell, it is made sure that an alignment is built correctly
                if (line.contains("entity1")) {
                    String[] parts = line.split("\"");
                    for(String part : parts) if(part.contains("http:")) alignment.setSubject(part);
                }
                else if(line.contains("entity2")){
                    String[] parts = line.split("\"");
                    for(String part : parts) if(part.contains("http:")) alignment.setObject(part);
                }
                else if(line.contains("measure")){
                    String[] parts = line.split(">");
                    String[] parts2 = parts[1].split("<");
                    alignment.setMeasure(Float.parseFloat(parts2[0]));
                }

                //Once all three fields have been filled with data from model, an alignment has been built, so it can be added to the final array and break to continue with the next in the model
                if(!alignment.getSubject().equals("") && !alignment.getObject().equals("") && alignment.getMeasure()!=-1){
                    alignments.add(alignment);
                    break;
                }

            }
        }
        return alignments;
    }

}
