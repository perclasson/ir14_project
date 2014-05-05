import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * This class is designed to take a human produced query and produce a
 * corresponding Solr Query.
 * 
 * Some of the main assumptions:
 * 
 * A word beginning with a capital letter is important.
 * 
 * We have a field named title, and a default field with the bread text.
 * 
 * @author Mwli
 * 
 */
public class QueryToSolr {

	public QueryToSolr() {
	}
	
	public String interpret(String[] args){
		final String[] forbidden = {"+", "-", "&", "|", "!",
				  "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "\\" , "/"};
			StringBuilder query = new StringBuilder();
			StringBuilder initialQuery = new StringBuilder();
			
			ArrayList<String> titleWords = new ArrayList<String>();
			if(args.length > 0)
				query.append("text:");
			for (int i = 0; i < args.length; i++) {
				initialQuery.append(args[i] + " ");
				
				String word = args[i].trim();
				StringBuilder sb = new StringBuilder();
				
				/*
				 * FORBIDDEN CHARACTERS
				 * Protects each forbidden solr char with a backslash
				 */
				if(word.contains("\\"))
					word = word.replace("\\", "\\\\");
				for(String old : forbidden){
					word = word.replace(old, ("\\" + old));
				}
				word = word.replace("\\\\", "\\");
				sb.append(word);
				
				/*
				 * WORDS WITH CAPITAL LETTERS ARE IMPORTANT
				 * Appends a ^ char at the end of each word beginning with a capital letter
				 * and also assumes it has a higher chance of appearing in the title of a text.
				 */
				if (word.matches("[A-Z]+.*") || word.matches("[ÅÄÖ]+.*") || args.length == 1) {
					titleWords.add(word);
					sb.append("^");
				}
				query.append(sb).append(" ");
			}
			/* Adds title words to the query using the field name title*/
			if(titleWords.size() != 0)
				query.append("OR title:");
				for(String words : titleWords){
					query.append(words + " ");
				}
			query.setLength(query.length()-1);
			System.out.println("Initial query: " + initialQuery.toString());	
			System.out.println("Final query: " + query.toString());
			return query.toString();
	}

	public static void main(String[] args) {
		new QueryToSolr().interpret(args);
	}
}
