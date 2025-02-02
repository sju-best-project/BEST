package compliancevalidator.matchers.equivalence;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;

import compliancevalidator.misc.StringUtils;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import rita.RiWordNet;

public class WordNetMatcher extends ObjectAlignment implements AlignmentProcess {

	static RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");

	public WordNetMatcher() {
	}

	/**
	 * Should implement a function that considers whether the string contains several tokens (e.g. compounds that could be matched individually)
	 * Do a first check to see if the strings can be decomposed to tokens or not first by checking the size.
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", wordNetMatch(cl1,cl2));  
				}
				
			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	

	
public static Alignment matchAlignment(Alignment inputAlignment) throws AlignmentException, OntowrapException {
		
		System.out.println("Running WordNet alignment!");

		Alignment refinedAlignment = new URIAlignment();
		double score = 0;
		double threshold = 0.9;
		
		//match the objects (need to preprocess to remove URI) in every cell of the alignment
		for (Cell c : inputAlignment) {
			score = computeAlignmentWordNetMatch(StringUtils.getString(c.getObject1().toString()), StringUtils.getString(c.getObject2().toString()));
			System.out.println("Matching " + StringUtils.getString(c.getObject1().toString()) + " and " + StringUtils.getString(c.getObject2().toString()) + " with a score of " + score);
			if (score > c.getStrength() && score > threshold) {
				refinedAlignment.addAlignCell(c.getObject1(), c.getObject2(), "=", increaseCellStrength(score));
			} else {
				refinedAlignment.addAlignCell(c.getObject1(), c.getObject2(), "=", reduceCellStrength(c.getStrength()));
				continue;
			}
		}

		return refinedAlignment;
	}
	
	
	
	private static boolean containedInWordNet(String inputWord) {


		String[] synsets = database.getSynset(inputWord, "n");

		if (synsets.length > 0)
		{
			return true;
		}
		else
		{
			return false;
		}		

	}

	
	/**
	 * The wordNetMatch() method has two objects (ontology entity names) as parameters, checks if both entity names are included in WordNet, if so their distance is computed (I think using Resnik)
	 * @param o1
	 * @param o2
	 * @return
	 * @throws AlignmentException
	 * @throws OntowrapException 
	 */
	public double wordNetMatch(Object o1, Object o2) throws AlignmentException, OntowrapException {

		double distance = 0;
		double finalDistance = 0;
		double score = 0; 
		double finalScore = 0;
		double accScore = 0;
		double correct = 0;
		double incorrect = 0;
		
			//get the objects (entities)
			//need to split the strings
			String s1 = StringUtils.stringTokenize(ontology1().getEntityName(o1),true).toLowerCase();
			String s2 = StringUtils.stringTokenize(ontology2().getEntityName(o2),true).toLowerCase();
			
			//TO-DO: trying to utilize token-by-token matching in wordnet, but not successful
			/*ArrayList<String> tokens_1 = StringUtils.tokenize(s1, true);
			ArrayList<String> tokens_2 = StringUtils.tokenize(s2, true);
			
			if (tokens_1.size() > 1 && tokens_2.size() > 1) {

				
				for (String s : tokens_1) {
					for (String t : tokens_2) {
						score = (1 - database.getDistance(s, t, "n"));
						accScore = accScore + score;
						System.out.println(s + " and " + t + " scores " + score + " and the current score is " + accScore);
						
						if (score > 0.8) {
							correct++;
							break;
						} else {
							incorrect++;
						}
					}
				}
				
				double numTokens = tokens_1.size() + tokens_2.size();
				
				if (incorrect == 0.0) {
					incorrect = 10;		
				}
				finalScore = accScore / numTokens;
				distance = finalScore;
			} else {*/

				//...measure their distance
				 distance = (1 - database.getDistance(s1, s2, "n"));
				 
				 //printing the ontology objects and their measured distance
				 System.out.println(s1 + " - " + s2 + " with measure: " + distance);
//			}

		return distance;
	}
	
	public static double computeAlignmentWordNetMatch(String s1, String s2) throws AlignmentException, OntowrapException {

		double distance = 0;
		//...measure their distance
		 distance = (1 - database.getDistance(s1, s2, "n"));

		return distance;
	}
	
	/*public double computeAlignmentWordNetTokenMatch(Object o1, Object o2) throws AlignmentException, OntowrapException {
		
		//need to split the strings
		String s1 = StringUtils.stringTokenize(ontology1().getEntityName(o1),true).toLowerCase();
		String s2 = StringUtils.stringTokenize(ontology2().getEntityName(o2),true).toLowerCase();
		
		ArrayList<String> tokens_1 = StringUtils.tokenize(s1, true);
		ArrayList<String> tokens_2 = StringUtils.tokenize(s2, true);
		
		double score = 0; 
		double finalScore = 0;
		double accScore = 0;
		double correct = 0;
		double incorrect = 0;
		
		for (String s : tokens_1) {
			for (String t : tokens_2) {
				score = (1 - database.getDistance(s1, s2, "n"));
				accScore = accScore + score;
				System.out.println(s + " and " + t + " scores " + score + " and the current score is " + accScore);
				
				if (score > 0.8) {
					correct++;
					break;
				} else {
					incorrect++;
				}
			}
		}
		
		double avgTokens = (tokens_1.size() + tokens_2.size()) / 2;
		double numTokens = tokens_1.size() + tokens_2.size();
		
		if (incorrect == 0.0) {
			incorrect = 10;		
		}
		finalScore = accScore / numTokens;
	

		return finalScore;
	}*/
	
	public static double increaseCellStrength(double inputStrength) {

		double newStrength = inputStrength + (inputStrength * 0.10);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}
	
	public static double reduceCellStrength(double inputStrength) {

		double newStrength = inputStrength - (inputStrength * 0.10);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}

}
