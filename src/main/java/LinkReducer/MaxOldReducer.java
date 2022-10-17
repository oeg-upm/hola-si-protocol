package LinkReducer;

import LinkReducer.ILinkReducer;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;


public class MaxOldReducer implements ILinkReducer {

    public Model reduceAlignments(String ownAlignments, String foreignAlignments){
        Model result = null;
        Model own = null;
        Model foreign = null;
        try{
            own = ModelFactory.createDefaultModel()
                    .read(IOUtils.toInputStream(ownAlignments, "UTF-8"), null, "RDF/XML");
            foreign = ModelFactory.createDefaultModel()
                    .read(IOUtils.toInputStream(foreignAlignments, "UTF-8"), null, "RDF/XML");
        }catch (IOException e){e.printStackTrace();}

        ArrayList<Alignment> ownReducedAlignments = getUsefulAlignments(own);
        ArrayList<Alignment> foreignReducedAlignments = getUsefulAlignments(foreign);

        System.out.println("Tamaño own inicial: " + ownReducedAlignments.size());
        System.out.println("Tamaño foreign inicial: " + foreignReducedAlignments.size());

        ArrayList<ArrayList<Alignment>> incompatibilities = getIncompatibilities(ownReducedAlignments, foreignReducedAlignments);
        System.out.println("Incompatibilidades sin duplicados: " + incompatibilities.size());

        ArrayList<Alignment> reduced = reduceWithoutIncompatibilities(ownReducedAlignments, foreignReducedAlignments, incompatibilities);

        System.out.println("Tamaño own sin incompatibilidades: " + ownReducedAlignments.size());
        System.out.println("Tamaño foreign sin incompatibilidades: " + foreignReducedAlignments.size());

        ArrayList<Alignment> incompatibilitiesReduced = reduceIncompatibilities(incompatibilities);

        reduced.addAll(incompatibilitiesReduced);

        for(Alignment a: reduced) {
            //System.out.println(a);
            a.setPredicate("http://www.w3.org/2002/07/owl#equivalentClass");
        }
        System.out.println("Tamano final: " + reduced.size());

        result = buildModel(reduced);

        return result;
    }

    //This method iterates through the whole model and builds an arraylist of alignments as they are found in the model
    private ArrayList<Alignment> getUsefulAlignments(Model m){
        final StmtIterator iter = m.listStatements();
        ArrayList<Alignment> alignments = new ArrayList<>();

        //Two loops are needed, the outer while for adding alignments to the array, and the inner while for constructing each Alignment
        while (iter.hasNext()){
            Alignment alignment = new Alignment();

            while (iter.hasNext()) {
                Statement stmt = iter.nextStatement();
                final String pred = stmt.getPredicate().toString();

                //Iterating through the model until all two entities and its measure are found.
                //They have to be in the same cell, so regardless of the order or quantity of information in each cell, it is made sure that an alignment is built correctly
                if (pred.contains("entity1")) alignment.setSubject(stmt.getObject().toString());
                else if(pred.contains("entity2"))alignment.setObject(stmt.getObject().toString());
                else if(pred.contains("measure")){
                    String values[] = stmt.getObject().toString().split("\\^");
                    alignment.setMeasure(Float.parseFloat(values[0]));
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

    //Method that takes both own and foreign arrays of alignments and calculates their incompatibilities, to be then reduced
    private ArrayList<ArrayList<Alignment>> getIncompatibilities (ArrayList<Alignment> ownReducedAlignments, ArrayList<Alignment> foreignReducedAlignments) {
        ArrayList<ArrayList<Alignment>> withIncompatibilities = new ArrayList<>();

        //First get all incompatibilities into only one structure
        //Each incompatibility is a pair of alignments, stored as an arraylist of length 2
        //The whole of incompatibilities are stored into another arraylist, being that an array of arrays (list of incompatibilities)
        for (Alignment a : ownReducedAlignments) {
            for (Alignment b : foreignReducedAlignments) {
                if(a.getSubject().equals(b.getSubject()) || a.getSubject().equals(b.getObject()) || a.getObject().equals(b.getSubject()) || a.getObject().equals(b.getObject())){
                    ArrayList<Alignment> aux = new ArrayList<>();
                    aux.add(a);
                    aux.add(b);
                    withIncompatibilities.add(aux);
                }
            }
        }

        //Then remove the duplicates of the result set, since duplicated incompatibilities provide no further information
        ArrayList<ArrayList<Alignment>> result = new ArrayList<>();
        boolean found;
        for(ArrayList<Alignment> a : withIncompatibilities){
            found = false;
            for (ArrayList<Alignment> b : result) {
                if(a.get(0).equals(b.get(0))) {
                    found = true;
                    break;
                }
                else if(a.get(1).equals(b.get(1))) {
                    found = true;
                    break;
                }
            }
            if(!found) result.add(a);
        }

        return result;
    }

    //Method that extracts those alignments from both clients which do not create any incompatibility and store them in a shared arraylist
    //also deleting in the process those alignments which do create incompatibilities
    private ArrayList<Alignment> reduceWithoutIncompatibilities(ArrayList<Alignment> ownReducedAlignments, ArrayList<Alignment> foreignReducedAlignments, ArrayList<ArrayList<Alignment>> incompatibilities) {
        ArrayList<Alignment> reduced = new ArrayList<>();

        for(ArrayList<Alignment> arr : incompatibilities){
            ownReducedAlignments.removeIf(a -> arr.get(0).equals(a));
            foreignReducedAlignments.removeIf(a -> arr.get(1).equals(a));
        }

        for(Alignment a : ownReducedAlignments)reduced.add(a);
        for(Alignment a : foreignReducedAlignments)reduced.add(a);

        return reduced;
    }

    private ArrayList<Alignment> reduceIncompatibilities(ArrayList<ArrayList<Alignment>> incompatibilities) {
        ArrayList<Alignment> result = new ArrayList<>();

        for(int i=incompatibilities.size()-1; i>=0; i--){
            ArrayList<Alignment> inc = incompatibilities.get(i);

            //If both alignments are exactly the same, by default remove the foreign one
            if(inc.get(0).equals(inc.get(1))){
                result.add(inc.get(0));
                incompatibilities.remove(i);
            }

            //If both alignments are different, but have the same confidence measure, randomly select one biased towards own's alignment
            else if(inc.get(0).getMeasure().equals(inc.get(1).getMeasure())){
                Random random = new Random();
                double p = random.nextDouble();
                if(p<=0.4) result.add(inc.get(1));
                else result.add(inc.get(0));
                incompatibilities.remove(i);
            }

            //If alignments are different, with different measure, select the one with higher measure, and give it its own old measure
            else{
                if(inc.get(0).getMeasure() > inc.get(1).getMeasure()){
                    Alignment a = new Alignment(inc.get(0).getSubject(), inc.get(0).getObject(), inc.get(0).getMeasure());
                    result.add(a);
                }
                else{
                    Alignment a = new Alignment(inc.get(1).getSubject(), inc.get(1).getObject(), inc.get(1).getMeasure());
                    result.add(a);
                }
                incompatibilities.remove(i);
            }
        }
        return result;
    }

    //Now that all alignments have been reduced, it's time to get them into a Jena Model to return it to Controller
    private Model buildModel(ArrayList<Alignment> alignments) {
        Model result = null;

        String model = "<?xml version='1.0' encoding='utf-8'?>\n" +
                "<rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment'\n" +
                "\t xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' \n" +
                "\t xmlns:xsd='http://www.w3.org/2001/XMLSchema#'>\n" +
                "<Alignment>\n" +
                "\t<xml>yes</xml>\n" +
                "\t<level>0</level>\n" +
                "\t<type>11</type>\n";

        for (Alignment a : alignments) {
            model = model.concat("<map>\n<Cell>\n<entity1 rdf:resource=\"" + a.getSubject() + "\"/>\n" +
                    "<entity2 rdf:resource=\"" + a.getObject() + "\"/>\n" +
                    "<measure rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">" + a.getMeasure() + "</measure>\n" +
                    "<relation>=</relation>\n" +
                    "</Cell>\n</map>\n");
        }
        model = model.concat("</Alignment>\n" +
                "</rdf:RDF>");


        try{
            result = ModelFactory.createDefaultModel()
                    .read(IOUtils.toInputStream(model, "UTF-8"), null, "RDF/XML");
        }catch (IOException e){e.printStackTrace();}

        return result;
    }

}
