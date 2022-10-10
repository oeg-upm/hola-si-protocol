import java.nio.file.Files;
import java.nio.file.Path;

public class FileLinker implements IOntologyLinker{

    //Calculates alignments directly reading from a file containing them
    public String calculateAlignments(String ownOntology, String foreignOntology) {
        String alignments = null;
        try {

            switch(App.CLIENT_NAME){
                case "client-1": alignments = Files.readString(Path.of("src/main/resources/client1-alignments.rdf"));break;
                case "client-2": alignments = Files.readString(Path.of("src/main/resources/client2-alignments.rdf"));break;
                case "client-3": alignments = Files.readString(Path.of("src/main/resources/client3-alignments.rdf"));break;
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return alignments;
    }

}
