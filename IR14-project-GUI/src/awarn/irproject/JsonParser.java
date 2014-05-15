package awarn.irproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lexRank.LexRank;
import lexRank.LexRank.Sentence;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParser {
	/** Handling of non-standard characters */
	static final char[] special_char = { '\u00E1', '\u00E0', '\u00E2',
			'\u00E5', '\u00E4', '\u00E9', '\u00E8', '\u00EA', '\u00ED',
			'\u00E1', '\u00F6', '\u00F4', '\u00FC', '\u00FA', '\u00F9',
			'\u00FB', '\u00C5', '\u00C4', '\u00D6', 165, 164, 8222, 182, 184,
			732, 8211, 195 };

	/**
	 * What special characters should be translated into. NB: This array should
	 * have the same size as the one above!
	 */
	static final char[] translation = { 'a', 'a', 'a', '\u00E5', '\u00E4', 'e',
			'e', 'e', 'i', 'n', '\u00F6', 'o', '\u00FC', 'u', 'u', 'u',
			'\u00E5', '\u00E4', '\u00F6', '\u00E5', '\u00E4', '\u00E4',
			'\u00F6', '\u00F6', '\u00F6', '\u00F6', '#' };

	// TODO: läs in från URL
	private static final String filePath = "res/test2.json";

	public List<Sentence> search(String urlString) {
		ArrayList<String> titles = new ArrayList<String>();
		HashMap<Integer, String[]> sentences = new HashMap<Integer, String[]>();
		HashMap<Integer, String[]> names = new HashMap<Integer, String[]>();
		List<Sentence> summaries = new ArrayList<>();
		HashMap<String, Integer> sentencePosition = new HashMap<>();

		try {
			// A Solr url
			URL url = new URL(urlString);

			// read the json file
			Reader reader;
			reader = new BufferedReader(new InputStreamReader(url.openStream()));

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

			LinkedList<String> allSentences = new LinkedList<>();
			LinkedList<String> allNames = new LinkedList<>();

			while (it.hasNext() && no_docs < App.MAX_NO_DOCS) {
				JSONObject doc = (JSONObject) it.next();
				String text = (String) doc.get("text");
				String pre = text.substring(0, 15).toLowerCase();
				if (pre.startsWith("#redirect")
						|| pre.startsWith("#omdirigering"))
					continue;

				// Remove all unnecessary tags and blocks of text we don't need
				// String stripped = WikimediaTransformer.wikiToPlainText(text);
				String stripped = stripper(text);

				titles.add((String) doc.get("titleText"));

				// Remove all special characters, turn to lower case and split
				// into sentences
				String[] sent = getSentences(stripped);
				String[] n = new String[sent.length];
				for (int a = 0; a < sent.length; a++) {
					String name = "d" + no_docs + "s" + a;
					n[a] = name;
					sentencePosition.put(name, a);
				}

				if (sent.length < App.MIN_SENTENCES_FOR_DOC) {
					System.out.println("Skip doc");
					continue;
				}

				allSentences.addAll(Arrays.asList(sent));
				allNames.addAll(Arrays.asList(n));

				sentences.put(no_docs, sent);
				names.put(no_docs, n);

				// TODO: One lexrank per document or one lexrank in total?
				LexRank lexRank = new LexRank(n, sent);
				Sentence lexRankSentence = getSentence(lexRank.sentRanks, sentencePosition);
				if (lexRankSentence == null) {
					lexRankSentence = lexRank.getSentRanks();
				}
				lexRankSentence.name = ((String) doc.get("titleText"))
						.replaceAll(" ", "_");
				summaries.add(lexRankSentence);

				no_docs++;
			}

			if (App.ALL_SENTENCES && no_docs > 0) {
				String[] allNameArray = allNames.toArray(new String[0]);
				String[] allSentArray = allSentences.toArray(new String[0]);
				LexRank lexRank = new LexRank(allNameArray, allSentArray);
				Sentence lexRankSentence = getSentence(lexRank.sentRanks, sentencePosition);
				
				if (lexRankSentence == null) {
					lexRankSentence = lexRank.getSentRanks();
				}
				
				lexRankSentence.name = "Listen";
				List<Sentence> oneSommary = new ArrayList<Sentence>();
				oneSommary.add(lexRankSentence);
				return oneSommary;
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}

		return summaries;

	}

	private Sentence getSentence(Sentence[] sentRanks,
			HashMap<String, Integer> sentencePosition) {
		for (Sentence s : sentRanks) {
			Integer position = sentencePosition.get(s.name);
			if (position != null && position < App.MAX_SENTENCE_POSITION) {
				return s;
			}
		}
		return null;
	}

	// TODO: kombinera och kolla fler patterns samtidigt?
	private static String stripper(String document) {
		StringBuilder sb = new StringBuilder(document);
		// System.out.println(sb.toString());

		// Stripp all "{{...}}", starts in the innermost {} and works outwards
		Stack<Integer> s = new Stack<Integer>();
		for (int i = 0; i < sb.length(); i++) {
			if (sb.charAt(i) == '{' && sb.charAt(i + 1) == '{')
				s.push(i);
			else if (sb.charAt(i) == '}' && sb.charAt(i + 1) == '}') {
				// Ta bort all text mellan s.pop() och i+1 (inklusive
				// ändpunkter)
				int j = s.pop();
				sb.delete(j, i + 2);
				i = i - (i + 1 - j);
			}
		}
		// System.out.println(sb.toString());

		// Stripp all "[[...|"
		Pattern p = Pattern.compile("\\[\\[[^\\]]*\\|");
		Matcher m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), m.end());
			m.reset();
		}
		// System.out.println(sb.toString());

		// Stripp all "[[" och "]]"
		p = Pattern.compile("\\[\\[|\\]\\]");
		m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), m.end());
			m.reset();
		}

		// System.out.println(sb.toString());

		// Stripp all "<ref>...</ref>" and "<ref .../>"
		p = Pattern.compile("(\\<ref.*\\</ref\\>)|(\\<ref.*\\/\\>)");
		m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), m.end());
			m.reset();
		}
		// System.out.println(sb.toString());

		// Stripp all "'"
		p = Pattern.compile("'");
		m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), m.end());
			m.reset();
		}
		// System.out.println(sb.toString());

		// Stripp all "==...=="
		p = Pattern.compile("[=]+[\\w\\såäöÅÄÖ–\\-]+[=]+");
		m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), m.end());
			m.reset();
		}
		// System.out.println(sb.toString());

		// Remove everything after "<references/>" or "<references>"
		// TODO: finns alltid den taggen?
		p = Pattern.compile("<references");
		m = p.matcher(sb);
		while (m.find()) {
			sb.delete(m.start(), sb.length());
			m.reset();
		}
		// System.out.println(sb.toString());

		// Stripp text of urls
		// TODO: bra eller dåligt?
		try {
			p = Pattern
					.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
			m = p.matcher(sb);
			while (m.find()) {
				sb.delete(m.start(), sb.indexOf("\n", m.end()));
				m.reset();
			}
		} catch (StringIndexOutOfBoundsException e) {

		}
		// System.out.println(sb.toString());

		// Stripp all "nbsp" (hard spaces)
		p = Pattern.compile("nbsp");
		m = p.matcher(sb);
		while (m.find()) {
			sb.replace(m.start(), m.end(), " ");
			m.reset();
		}

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
