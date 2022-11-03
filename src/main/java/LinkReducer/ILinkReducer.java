package LinkReducer;

import org.apache.jena.rdf.model.Model;

import java.util.ArrayList;
import java.util.Set;

public interface ILinkReducer {
    Set<Alignment> reduceIncompatibilities(Set<ArrayList<Alignment>> incompatibilities);
}
