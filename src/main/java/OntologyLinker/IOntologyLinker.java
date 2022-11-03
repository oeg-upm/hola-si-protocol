package OntologyLinker;

import LinkReducer.Alignment;

import java.util.ArrayList;
import java.util.Set;

public interface IOntologyLinker {

    //Method used within factory method to obtain the links between ontologies independently of the algorithm used
    Set<Alignment> calculateAlignments(String ownOntology, String foreignOntology);
}
