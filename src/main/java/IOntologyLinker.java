import org.apache.jena.rdf.model.Model;

public interface IOntologyLinker {

    //Method used within factory method to obtain the links between ontologies independently of the algorithm used
    String calculateAlignments(String ownOntology, String foreignOntology);
}
