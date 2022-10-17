package App;

import LinkReducer.ILinkReducer;
import LinkReducer.MaxOldReducer;
import OntologyLinker.FileLinker;
import OntologyLinker.IOntologyLinker;

public class AlignmentFactory {

    //Factory method for linking ontologies. Decides which method will be used based on the value of the type parameter
    public IOntologyLinker createOntologyLinker(String type){
        switch (type) {
            case "file":
                return new FileLinker();
            default:
                throw new IllegalArgumentException("Unknown channel "+type);
        }
    }

    public ILinkReducer createLinkReducer(String type){
        String[] parts = type.split("-");
        switch (parts[0]) {
            case "max":
                switch (parts[1]){
                    case"old":
                        return new MaxOldReducer();
                }

            default:
                throw new IllegalArgumentException("Unknown channel "+type);
        }
    }

}
