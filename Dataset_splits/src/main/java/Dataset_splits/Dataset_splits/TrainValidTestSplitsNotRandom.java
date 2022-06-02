package Dataset_splits.Dataset_splits;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.formats.*;
import java.io.*;
import java.util.*;
public class TrainValidTestSplitsNotRandom {
    public static String onto_file; //input ontology
    public static String train_onto_file; //.owl
    public static String valid_onto_file; //.owl 
    public static String train_file; //.csv 
    public static String valid_file; //.csv 
    public static String test_file; //.csv 
   

    /**
     * Axioms (non-inferred) are split into train (70%), validation (10%) and test (20%)
     * A training ontology is created for learning the embedding, by removing validation and test axioms
     * @param args
     */
    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        if(args.length >= 2){
            onto_file = args[1]; train_onto_file = args[2]; valid_onto_file = args[3];
            train_file = args[4]; valid_file = args[5]; test_file = args[6];
        }
        ArrayList<OWLLogicalAxiom> logicalAxioms_fortraining = new ArrayList<>();
        ArrayList<OWLLogicalAxiom> logicalAxioms_fortesting = new ArrayList<>();
        ArrayList<OWLLogicalAxiom> logicalAxioms_forvalidating = new ArrayList<>();
        Properties prop = new Properties();
        InputStream input = null;
        FunctionalSyntaxDocumentFormat ofn = new FunctionalSyntaxDocumentFormat();
        try {
            input = new FileInputStream("config.properties");
            // load range of different parameters from config.properties file
            prop.load(input);
            onto_file = prop.getProperty("onto_file");
            train_onto_file = prop.getProperty("train_onto_file");
            valid_onto_file = prop.getProperty("valid_onto_file");
            train_file = prop.getProperty("train_file");
            valid_file = prop.getProperty("valid_file");
            test_file = prop.getProperty("test_file");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Onto File="+ onto_file);
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(onto_file)));
        OWLOntologyManager m1 = OWLManager.createOWLOntologyManager();
        OWLOntology o_main_train = m1.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(onto_file)));
        OWLOntologyManager m2 = OWLManager.createOWLOntologyManager();
        OWLOntology o_main_valid = m2.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(onto_file)));

        //Find all the Subclass Axioms from the main ontology
        ArrayList<OWLSubClassOfAxiom> subClassAxioms= subclass_axioms(o, logicalAxioms_fortraining, logicalAxioms_fortesting, logicalAxioms_forvalidating );
       
        for (OWLSubClassOfAxiom axiom: subClassAxioms){
            RemoveAxiom ra = new RemoveAxiom(o, axiom);
            List<RemoveAxiom> ral = Collections.singletonList(ra);
            m.applyChanges(ral);
        }

       //Find all the Class Assertions Axioms from the main ontology
        ArrayList<OWLClassAssertionAxiom> classAssertionAxioms= classAssertion_axioms(o, logicalAxioms_fortraining, logicalAxioms_fortesting, logicalAxioms_forvalidating);
       
        for (OWLClassAssertionAxiom axiom: classAssertionAxioms){
            RemoveAxiom ra = new RemoveAxiom(o, axiom);
            List<RemoveAxiom> ral = Collections.singletonList(ra);
            m.applyChanges(ral);
        }
        
        //Find all the remaining logical axioms from the ontology (after removing subclass and class assertions)
        
        ArrayList<OWLLogicalAxiom> logicalAxioms= remainingLogical_axioms(o, logicalAxioms_fortraining, logicalAxioms_fortesting, logicalAxioms_forvalidating);
       
        //Create Train ontology
       
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
        //System.out.println("Training ontology saved.");

        // Remove the test and train axioms from the main ontology
        ArrayList<OWLLogicalAxiom> logicalAxioms_remove2 = new ArrayList<>(logicalAxioms_fortesting);
        logicalAxioms_remove2.addAll(logicalAxioms_fortraining);
        for (OWLLogicalAxiom axiom: logicalAxioms_remove2){
            RemoveAxiom ra = new RemoveAxiom(o_main_valid, axiom);
            List<RemoveAxiom> ral = Collections.singletonList(ra);
            m.applyChanges(ral);
        }
        m.saveOntology(o_main_valid, ofn, new FileOutputStream(new File(valid_onto_file)));
        System.out.println("Datasets saved.");

    }

    public static ArrayList<OWLSubClassOfAxiom> subclass_axioms(OWLOntology o, ArrayList<OWLLogicalAxiom> logicalAxioms_fortraining, ArrayList<OWLLogicalAxiom> logicalAxioms_fortesting, ArrayList<OWLLogicalAxiom> logicalAxioms_forvalidating) {
    	//Find all the Sublass Axioms from the main ontology and split them into train, valid and test axioms
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
        logicalAxioms_fortraining.addAll(subClassAxioms_train);
        logicalAxioms_forvalidating.addAll(subClassAxioms_valid);
        logicalAxioms_fortesting.addAll(subClassAxioms_test);
       return subClassAxioms;

    }
    
    public static ArrayList<OWLClassAssertionAxiom> classAssertion_axioms(OWLOntology o, ArrayList<OWLLogicalAxiom> logicalAxioms_fortraining, ArrayList<OWLLogicalAxiom> logicalAxioms_fortesting, ArrayList<OWLLogicalAxiom> logicalAxioms_forvalidating) {
    	//Find all the classassertion Axioms from the main ontology and split them into train, valid and test axioms
    	ArrayList<OWLClassAssertionAxiom> classAssertionAxioms = new ArrayList<>();
        for (OWLClassAssertionAxiom a : o.getAxioms(AxiomType.CLASS_ASSERTION)) {
        	//System.out.println(a);
        	classAssertionAxioms.add(a);
            }
        Collections.shuffle(classAssertionAxioms);
        int num_ = classAssertionAxioms.size();
        int index1_ = (int) ( num_ * 0.7);
        int index2_ = index1_ + (int) (num_ * 0.1);
        ArrayList<OWLClassAssertionAxiom> classAssertionAxioms_train = new ArrayList<>(classAssertionAxioms.subList(0, index1_));
        ArrayList<OWLClassAssertionAxiom> classAssertionAxioms_valid = new ArrayList<>(classAssertionAxioms.subList(index1_, index2_));
        ArrayList<OWLClassAssertionAxiom> classAssertionAxioms_test = new ArrayList<>(classAssertionAxioms.subList(index2_, num_));
        System.out.println(("train (positive): " + classAssertionAxioms_train.size() + ", valid: " + classAssertionAxioms_valid.size() +
                ", test: " + classAssertionAxioms_test.size()));
        logicalAxioms_fortraining.addAll(classAssertionAxioms_train);
        logicalAxioms_forvalidating.addAll(classAssertionAxioms_valid);
        logicalAxioms_fortesting.addAll(classAssertionAxioms_test);
        return classAssertionAxioms;
        
    }
    
    public static ArrayList<OWLLogicalAxiom> remainingLogical_axioms(OWLOntology o, ArrayList<OWLLogicalAxiom> logicalAxioms_fortraining, ArrayList<OWLLogicalAxiom> logicalAxioms_fortesting, ArrayList<OWLLogicalAxiom> logicalAxioms_forvalidating) {
    	//Find all the remaining logical axioms from the ontology (after removing subclass and class assertions)
    	 ArrayList<OWLLogicalAxiom> logicalAxioms = new ArrayList<>();
         for (OWLLogicalAxiom a : o.getLogicalAxioms()) {
         	//System.out.println(a);
         	logicalAxioms.add(a);
         	
             }
         Collections.shuffle(logicalAxioms);
         int num_ = logicalAxioms.size();
         int index1_ = (int) ( num_ * 0.7);
         int index2_ = index1_ + (int) (num_ * 0.1);
         ArrayList<OWLLogicalAxiom> logicalAxioms_train = new ArrayList<>(logicalAxioms.subList(0, index1_));
         ArrayList<OWLLogicalAxiom> logicalAxioms_valid = new ArrayList<>(logicalAxioms.subList(index1_, index2_));
         ArrayList<OWLLogicalAxiom> logicalAxioms_test = new ArrayList<>(logicalAxioms.subList(index2_, num_));
         System.out.println(("train (positive): " + logicalAxioms_train.size() + ", valid: " + logicalAxioms_valid.size() +
                 ", test: " + logicalAxioms_test.size()));
         logicalAxioms_fortraining.addAll(logicalAxioms_train);
         logicalAxioms_forvalidating.addAll(logicalAxioms_valid);
         logicalAxioms_fortesting.addAll(logicalAxioms_test);
         return logicalAxioms;
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
