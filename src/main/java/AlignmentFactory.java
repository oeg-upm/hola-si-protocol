import org.apache.jena.rdf.model.Model;

public class AlignmentFactory {

    //Factory method for linking ontologies. Decides which method will be used based on the value of the type parameter
    public IOntologyLinker createOntologyLinker(String type){
        if (type == null || type.isEmpty())
            return null;
        switch (type) {
            case "file":
                return new FileLinker();
            default:
                throw new IllegalArgumentException("Unknown channel "+type);
        }
    }

}
