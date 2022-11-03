package App;

import LinkReducer.Egoist.EgoGeomReducer;
import LinkReducer.Egoist.EgoMaxReducer;
import LinkReducer.Egoist.EgoMeanReducer;
import LinkReducer.Egoist.EgoOldReducer;
import LinkReducer.ILinkReducer;
import LinkReducer.Max.MaxGeomReducer;
import LinkReducer.Max.MaxMeanReducer;
import LinkReducer.Max.MaxOldReducer;
import LinkReducer.Random.RandGeomReducer;
import LinkReducer.Random.RandMaxReducer;
import LinkReducer.Random.RandMeanReducer;
import LinkReducer.Random.RandOldReducer;
import OntologyLinker.FileLinker;
import OntologyLinker.IOntologyLinker;
import OntologyLinker.JSONLinker;

public class AlignmentFactory {

    //Factory method for linking ontologies. Decides which method will be used based on the value of the type parameter
    public IOntologyLinker createOntologyLinker(String type){
        switch (type) {
            case "file":
                return new FileLinker();
            case "json":
                return new JSONLinker();
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
                    case"mean":
                        return new MaxMeanReducer();
                    case"geom":
                        return new MaxGeomReducer();
                    default:
                        throw new IllegalArgumentException("Unknown channel "+type);
                }
            case "ego":
                switch (parts[1]){
                    case"old":
                        return new EgoOldReducer();
                    case"max":
                        return new EgoMaxReducer();
                    case"mean":
                        return new EgoMeanReducer();
                    case"geom":
                        return new EgoGeomReducer();
                    default:
                        throw new IllegalArgumentException("Unknown channel "+type);
                }
            case "rand":
                switch (parts[1]){
                    case"old":
                        return new RandOldReducer();
                    case"max":
                        return new RandMaxReducer();
                    case"mean":
                        return new RandMeanReducer();
                    case"geom":
                        return new RandGeomReducer();
                    default:
                        throw new IllegalArgumentException("Unknown channel "+type);
                }
            default:
                throw new IllegalArgumentException("Unknown channel "+type);
        }
    }
}
