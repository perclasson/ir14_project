package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lexRank.LexRank2;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParser {
	/** Handling of non-standard characters */
	static final char[] special_char = { '\u00E1', '\u00E0', '\u00E2', '\u00E5', '\u00E4', '\u00E9', '\u00E8',
			'\u00EA', '\u00ED', '\u00E1', '\u00F6', '\u00F4', '\u00FC', '\u00FA', '\u00F9', '\u00FB', '\u00C5', '\u00C4', '\u00D6', 165,
			164, 8222, 182, 184, 732, 8211, 195 };
	
	/**
	 * What special characters should be translated into. NB: This array should
	 * have the same size as the one above!
	 */
	static final char[] translation = { 'a', 'a', 'a', '\u00E5', '\u00E4', 'e', 'e', 'e',
			'i', 'n', '\u00F6', 'o', '\u00FC', 'u', 'u', 'u', '\u00E5', '\u00E4', '\u00F6', '\u00E5', '\u00E4',
			'\u00E4', '\u00F6', '\u00F6', '\u00F6', '\u00F6', '#' };
	
	

	// TODO: läs in från URL
	private static final String filePath = "res/test2.json";
	
	// TODO: hur många dokument ska vi ta med?
	private static final int MAX_NO_DOCS = 5;

	public static void main(String[] args) {
		ArrayList<String> titles = new ArrayList<String>();
		HashMap<Integer, String[]> sentences = new HashMap<Integer, String[]>();
		HashMap<Integer, String[]> names = new HashMap<Integer, String[]>();
		
		try {
			// A Solr url
			URL url = new URL("http://localhost:8983/solr/solr/select?q=titleText%3A%22Jonas+Sj%C3%B6stedt%22&wt=json&indent=true");
			
			// read the json file
			Reader reader;
//			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			reader = new FileReader(filePath);

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

			// Get the json object containing the response
			JSONObject response = (JSONObject) jsonObject.get("response");
			// Get the json array containing all documents
			JSONArray docs = (JSONArray) response.get("docs");
			// Get iterator from array of documents
			@SuppressWarnings("rawtypes")
			Iterator it = docs.iterator();
			int no_docs = 0;
			while (it.hasNext() && no_docs < MAX_NO_DOCS) {
				JSONObject doc = (JSONObject) it.next();
				if (((String)doc.get("text")).startsWith("#REDIRECT"))
					continue;
				
				titles.add((String) doc.get("titleText"));
				// Remove all unnecessary tags and blocks of text we don't need
				String stripped = stripper((String) doc.get("text"));
				// Remove all special characters, turn to lower case and split into sentences
				String[] sent = getSentences(stripped);
				String[] n = new String[sent.length];
				for (int a = 0; a < sent.length; a++)
					n[a] = "d" + no_docs + "s" + a;
				
				sentences.put(no_docs, sent);
				names.put(no_docs, n);
				
				// TODO: One lexrank per document or one lexrank in total?
				new LexRank2(n, sent);
				
				no_docs++;
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}

	}

	// TODO: kombinera och kolla fler patterns samtidigt?
	private static String stripper(String document) {
		StringBuilder sb = new StringBuilder(document);
//		System.out.println(sb.toString());

		// Stripp all "{{...}}", starts in the innermost {} and works outwards
		Stack<Integer> s = new Stack<Integer>();
		for (int i = 0; i < sb.length(); i++) {
			if (sb.charAt(i) == '{' && sb.charAt(i + 1) == '{')
				s.push(i);
			else if (sb.charAt(i) == '}' && sb.charAt(i + 1) == '}') {
				// Ta bort all text mellan s.pop() och i+1 (inklusive ändpunkter)
				int j = s.pop();
				sb.delete(j, i + 2);
				i = i - (i + 1 - j);
			}
		}
//		System.out.println(sb.toString());
		
		// Stripp all "[[...|"
		Pattern p = Pattern.compile("\\[\\[[^\\]]*\\|");
		Matcher m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), m.end());
			m.reset();
		}
//		System.out.println(sb.toString());
		
		// Stripp all "[[" och "]]"
		p = Pattern.compile("\\[\\[|\\]\\]");
		m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), m.end());
			m.reset();
		}
//		System.out.println(sb.toString());
		
		// Stripp all "<ref>...</ref>" and "<ref .../>"
		p = Pattern.compile("(\\<ref.*\\</ref\\>)|(\\<ref.*\\/\\>)");
		m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), m.end());
			m.reset();
		}
//		System.out.println(sb.toString());
				
				
		
		// Stripp all "'"
		p = Pattern.compile("'");
		m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), m.end());
			m.reset();
		}
//		System.out.println(sb.toString());
		
		// Stripp all "==...=="
		p = Pattern.compile("[=]+[\\w\\såäöÅÄÖ–\\-]+[=]+");
		m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), m.end());
			m.reset();
		}
//		System.out.println(sb.toString());
		
		// Remove everything after "<references/>" or "<references>"
		// TODO: finns alltid den taggen?
		p = Pattern.compile("<references");
		m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), sb.length());
			m.reset();
		}
//		System.out.println(sb.toString());
		
		// Stripp text of urls
		// TODO: bra eller dåligt?
		p = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
		m = p.matcher(sb);
		while (m.find()) {
			
			sb.delete(m.start(), sb.indexOf("\n", m.end()));
			m.reset();
		}
//		System.out.println(sb.toString());
		
		return sb.toString();
	}

	private static String[] getSentences(String text) {
		String[] sent = text.split("\\.");
		for (int i = 0; i < sent.length; i++) {
			char[] c = sent[i].toCharArray();
			for (int j = 0; j < c.length; j++) {
				if (!normalize(c, j)) {
					c[j] = '\0';
				}
			}
			sent[i] = new String(c).trim();
		}
		return sent;
	}
	
	// TODO: Tar bort lite för mycket specialtecken, fixa.
	public static boolean normalize(char[] buf, int ptr) {
		char c = buf[ptr];
		if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c == ' ')) {
			return true;
		} else if (c >= 'A' && c <= 'Z') {
			buf[ptr] = (char) (c + 32);
			return true;
		} else {
			for (int i = 0; i < special_char.length; i++) {
				if (special_char[i] == c) {
					buf[ptr] = translation[i];
					return true;
				}
			}
			return false;
		}
	}
}
