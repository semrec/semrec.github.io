package openllet;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.formats.*;
import java.io.*;
import java.util.*;

public class TrainValidTestSplitsNotRandom {
    public static String onto_file = "C:\\Users\\gunja\\Desktop\\Task 3 _ Datasets\\OWL2DL\\OWL2DL_5\\OWL2DL_5.owl";
    public static String train_onto_file = "C:\\Users\\gunja\\Desktop\\Task 3 _ Datasets\\OWL2DL\\OWL2DL_5\\train.owl";
    public static String valid_onto_file = "C:\\Users\\gunja\\Desktop\\Task 3 _ Datasets\\OWL2DL\\OWL2DL_5\\valid.owl";
    public static String train_file = "C:\\Users\\gunja\\Desktop\\Task 3 _ Datasets\\OWL2DL\\OWL2DL_5\\train.csv";
    public static String valid_file = "C:\\Users\\gunja\\Desktop\\Task 3 _ Datasets\\OWL2DL\\OWL2DL_5\\valid.csv";
    public static String test_file = "C:\\Users\\gunja\\Desktop\\Task 3 _ Datasets\\OWL2DL\\OWL2DL_5\\test.csv";

    /**
     * Axioms (non-inferred) are split into train (70%), valid (10%) and test (20%)
     * A training ontology is created for learning the embedding, by removing valid and test axioms
     * @param args
     */
    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        if(args.length >= 2){
            onto_file = args[1]; train_onto_file = args[2]; valid_onto_file = args[3];
            train_file = args[4]; valid_file = args[5]; test_file = args[6];
        }
        
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(onto_file)));
        OWLOntologyManager m1 = OWLManager.createOWLOntologyManager();
        OWLOntology o_main_train = m1.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(onto_file)));
        OWLOntologyManager m2 = OWLManager.createOWLOntologyManager();
        OWLOntology o_main_valid = m2.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(onto_file)));
        System.out.println("Input Ontology Format="+ m.getOntologyFormat(o));
        System.out.println("Total Axiom Count="+ o.getAxiomCount());
        System.out.println("Total Logical Axiom Count="+ o.getLogicalAxiomCount());
        int count =0;
        for (OWLNamedIndividual c :o.getIndividualsInSignature()) {
            count=count+1;
          }
          System.out.println("Total Instances Count="+ count);
          count =0;
          for (OWLClass c : o.getClassesInSignature()) {
            count=count+1;
          }
          System.out.println("Total Classes Count="+ count);      
          count =0;
          for (OWLObjectProperty c : o.getObjectPropertiesInSignature()) {
            count=count+1;
          }
          System.out.println("Total Object Properties Count="+ count);   
          count =0;
          for (OWLDataProperty c : o.getDataPropertiesInSignature()) {
            count=count+1;
          }
          System.out.println("Total Data Properties Count="+ count);  
          
        System.out.println("Total Subclass Axiom Count="+ o.getAxiomCount(AxiomType.SUBCLASS_OF));
        System.out.println("Total Class Assertion Axiom Count="+ o.getAxiomCount(AxiomType.CLASS_ASSERTION));
        System.out.println("Total Object Property Assertion Axiom Count="+ o.getAxiomCount(AxiomType.OBJECT_PROPERTY_ASSERTION));
        System.out.println("Total Data Property Assertion Axiom Count="+ o.getAxiomCount(AxiomType.DATA_PROPERTY_ASSERTION));
  
        FunctionalSyntaxDocumentFormat ofn = new FunctionalSyntaxDocumentFormat();

        
        
 //*****************************************************************************************************************
        //Find all the Sublass Axioms from the main ontology
 //*****************************************************************************************************************

    
        ArrayList<OWLSubClassOfAxiom> subClassAxioms = new ArrayList<>();
        for (OWLSubClassOfAxiom a : o.getAxioms(AxiomType.SUBCLASS_OF)) {
        	//System.out.println(a);
        	subClassAxioms.add(a);
            }
        Collections.shuffle(subClassAxioms);
        int num_ = subClassAxioms.size();
        int index1_ = (int) ( num_ * 0.7);
        int index2_ = index1_ + (int) (num_ * 0.1);
        ArrayList<OWLSubClassOfAxiom> subClassAxioms_train = new ArrayList<>(subClassAxioms.subList(0, index1_));
        ArrayList<OWLSubClassOfAxiom> subClassAxioms_valid = new ArrayList<>(subClassAxioms.subList(index1_, index2_));
        ArrayList<OWLSubClassOfAxiom> subClassAxioms_test = new ArrayList<>(subClassAxioms.subList(index2_, num_));
        System.out.println(("train (positive): " + subClassAxioms_train.size() + ", valid: " + subClassAxioms_valid.size() +
                ", test: " + subClassAxioms_test.size()));

        for (OWLSubClassOfAxiom axiom: subClassAxioms){
            RemoveAxiom ra = new RemoveAxiom(o, axiom);
            List<RemoveAxiom> ral = Collections.singletonList(ra);
            m.applyChanges(ral);
        }

        
//*****************************************************************************************************************
      //Find all the Class Assertions Axioms from the main ontology
//*****************************************************************************************************************

        
        ArrayList<OWLClassAssertionAxiom> classAssertionAxioms = new ArrayList<>();
        for (OWLClassAssertionAxiom a : o.getAxioms(AxiomType.CLASS_ASSERTION)) {
        	//System.out.println(a);
        	classAssertionAxioms.add(a);
            }
        Collections.shuffle(classAssertionAxioms);
        num_ = classAssertionAxioms.size();
        index1_ = (int) ( num_ * 0.7);
        index2_ = index1_ + (int) (num_ * 0.1);
        ArrayList<OWLClassAssertionAxiom> classAssertionAxioms_train = new ArrayList<>(classAssertionAxioms.subList(0, index1_));
        ArrayList<OWLClassAssertionAxiom> classAssertionAxioms_valid = new ArrayList<>(classAssertionAxioms.subList(index1_, index2_));
        ArrayList<OWLClassAssertionAxiom> classAssertionAxioms_test = new ArrayList<>(classAssertionAxioms.subList(index2_, num_));
        System.out.println(("train (positive): " + classAssertionAxioms_train.size() + ", valid: " + classAssertionAxioms_valid.size() +
                ", test: " + classAssertionAxioms_test.size()));


        for (OWLClassAssertionAxiom axiom: classAssertionAxioms){
            RemoveAxiom ra = new RemoveAxiom(o, axiom);
            List<RemoveAxiom> ral = Collections.singletonList(ra);
            m.applyChanges(ral);
        }
        
        
//*****************************************************************************************************************
        //Find all the remaining logical axioms from the ontology (after removing subclass and class assertions)
//*****************************************************************************************************************
        

        ArrayList<OWLLogicalAxiom> logicalAxioms = new ArrayList<>();
        for (OWLLogicalAxiom a : o.getLogicalAxioms()) {
        	//System.out.println(a);
        	logicalAxioms.add(a);
        	
            }
        Collections.shuffle(logicalAxioms);
        num_ = logicalAxioms.size();
        index1_ = (int) ( num_ * 0.7);
        index2_ = index1_ + (int) (num_ * 0.1);
        ArrayList<OWLLogicalAxiom> logicalAxioms_train = new ArrayList<>(logicalAxioms.subList(0, index1_));
        ArrayList<OWLLogicalAxiom> logicalAxioms_valid = new ArrayList<>(logicalAxioms.subList(index1_, index2_));
        ArrayList<OWLLogicalAxiom> logicalAxioms_test = new ArrayList<>(logicalAxioms.subList(index2_, num_));
        System.out.println(("train (positive): " + logicalAxioms_train.size() + ", valid: " + logicalAxioms_valid.size() +
                ", test: " + logicalAxioms_test.size()));
 
        
//*****************************************************************************************************************
        //Create Train ontology
//*****************************************************************************************************************
        ArrayList<OWLLogicalAxiom> logicalAxioms_fortraining = new ArrayList<>(logicalAxioms_train);
        logicalAxioms_fortraining.addAll(classAssertionAxioms_train);
        logicalAxioms_fortraining.addAll(subClassAxioms_train);
        
        ArrayList<OWLLogicalAxiom> logicalAxioms_fortesting = new ArrayList<>(logicalAxioms_test);
        logicalAxioms_fortesting.addAll(classAssertionAxioms_test);
        logicalAxioms_fortesting.addAll(subClassAxioms_test);
        
        ArrayList<OWLLogicalAxiom>  logicalAxioms_forvalidating = new ArrayList<>(logicalAxioms_valid);
        logicalAxioms_forvalidating.addAll(classAssertionAxioms_valid);
        logicalAxioms_forvalidating.addAll(subClassAxioms_valid);
        
        save_sample(train_file, logicalAxioms_fortraining);
        save_sample(valid_file, logicalAxioms_forvalidating);
        save_sample(test_file, logicalAxioms_fortesting);
        
        // Remove the test and valid axioms from the main ontology
        ArrayList<OWLLogicalAxiom> logicalAxioms_remove = new ArrayList<>(logicalAxioms_fortesting);
        logicalAxioms_remove.addAll(logicalAxioms_forvalidating);
        for (OWLLogicalAxiom axiom: logicalAxioms_remove){
            RemoveAxiom ra = new RemoveAxiom(o_main_train, axiom);
            List<RemoveAxiom> ral = Collections.singletonList(ra);
            m.applyChanges(ral);
        }
        m.saveOntology(o_main_train, ofn, new FileOutputStream(new File(train_onto_file)));
        System.out.println("Training ontology saved.");

        // Remove the test and train axioms from the main ontology
        ArrayList<OWLLogicalAxiom> logicalAxioms_remove2 = new ArrayList<>(logicalAxioms_fortesting);
        logicalAxioms_remove2.addAll(logicalAxioms_fortraining);
        for (OWLLogicalAxiom axiom: logicalAxioms_remove2){
            RemoveAxiom ra = new RemoveAxiom(o_main_valid, axiom);
            List<RemoveAxiom> ral = Collections.singletonList(ra);
            m.applyChanges(ral);
        }
        m.saveOntology(o_main_valid, ofn, new FileOutputStream(new File(valid_onto_file)));
        System.out.println("Validating ontology saved.");

    }

  
    static void save_sample(String file_name, ArrayList<OWLLogicalAxiom> samples) throws IOException {
        File fout = new File(file_name);
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        for(OWLLogicalAxiom s: samples){
            bw.write(s + "\n");
        }
        bw.close();
    }

  
}
