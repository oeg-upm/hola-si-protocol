package LinkReducer;

import org.apache.jena.rdf.model.Model;

public interface ILinkReducer {
    Model reduceAlignments(String ownAlignments, String foreignAlignments);
}
