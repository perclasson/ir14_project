package lexRank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LexRank2 {
	private static boolean DEBUG = false;
	private static final double LIMIT = 0.1;
	
	HashMap<String, HashMap<String, Integer>> index;
	HashMap<String, Integer> df;
	HashMap<String, Double> len;
	
	double[][] sim;
	
	ArrayList<Integer>[] adjList;
	
	String[] names;
	String[] sentences;
	
	public LexRank2(String[] names, String[] sentences) {
		this.names = names;
		this.sentences = sentences;
		
		buildIndex();
		calcSentenceLength();
		similarity();
		// TODO: nu kan vi använda metoden 'degree'

		createAdjacencyList();

		double[] ranks = calcSentRanks();
		
		sortSentRanks(ranks);
	}

	/**
	 * TODO: något slags tokenizer som fixar med specialtecken etc.
	 */
	private void buildIndex() {
		index = new HashMap<String, HashMap<String,Integer>>();
		df = new HashMap<String, Integer>();
		for (int i = 0; i < names.length; i++) {
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			String[] words = sentences[i].split(" ");
			
			for (String word : words) {
				if (map.containsKey(word))
					map.put(word, map.get(word)+1);
				else {
					// första gången ett ord dyker upp i ett dokument, räkna upp df för ordet
					if (df.containsKey(word)) { 
						df.put(word, df.get(word)+1);
					} else {
						df.put(word, 1);
					}
					map.put(word, 1);
				}
			}
			
			index.put(names[i], map);
		}
	}
	
	/**
	 * calculate: sqrt( sum w in s (tf_w,s * idf_w)^2 ) for each sentence
	 * tf_w,s = term freq of word w in sentence s
	 * idf_w = inverted document freq of word w
	 */
	private void calcSentenceLength() {
		len = new HashMap<String, Double>();
		for (String name : names) {
			double length = 0;
			HashMap<String, Integer> map = index.get(name);
			
			for (String word : map.keySet()) {
				double tf_ws = map.get(word);
				double idf_w = Math.log(index.size() / df.get(word) );
				
				length += tf_ws*tf_ws * idf_w*idf_w;
			}
			length = Math.sqrt(length);
			len.put(name, length);
		}
	}
	
	/**
	 * sum w in x,y ( tf_w,x * tf_w,y * (idf_w)^2 ) ; x,y are two sentences
	 */
	private void similarity() {
		sim = new double[names.length][names.length];
		
		for (int i = 0; i < names.length; i++) {
			sim[i][i] = 1;
			String name1 = names[i];
			
			for (int j = i+1; j < names.length; j++) {
				double sum = 0;
				String name2 = names[j];
				
				// Loopa igenom alla ord i ena meningen och kolla om de även finns i andra
				// Om de finns, addera produkten av deras tf och idf till summan.
				HashMap<String, Integer> sen1 = index.get(name1);
				HashMap<String, Integer> sen2 = index.get(name2);
				
				for (String word : sen1.keySet()) {
					if (!sen2.containsKey(word))
						continue;
					
					double tf_wx = sen1.get(word);
					double tf_wy = sen2.get(word);
					double idf_w = Math.log( index.size() / df.get(word) );
					
					sum += tf_wx * tf_wy * idf_w * idf_w;
				}
				sum = sum / len.get(name1) / len.get(name2);
				sim[i][j] = sim[j][i] = sum;
			}
		}
		
		if (DEBUG) {
			for (int i = 0; i < names.length; i++) {
				for (int j = 0; j < names.length; j++) 
					System.out.printf("%.2f ", sim[i][j]);
				System.out.println();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void createAdjacencyList() {
		adjList = new ArrayList[names.length];
		
		for (int i = 0; i < names.length; i++) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (int j = 0; j < names.length; j++) {
				if (sim[i][j] > LIMIT)
					list.add(j);
			}
			adjList[i] = list;
		}
		
		if (DEBUG) {
			for (int i = 0; i < names.length; i++) {
				for (Integer n : adjList[i])
					System.out.printf("%d ", n);
				System.out.println();
			}
		}
	}
	
	private double[] calcSentRanks() {
		double[] ranks = PageRank.ComputeRank(names.length, adjList);
		if (DEBUG)
			for (double d : ranks)
				System.out.println(d);
		return ranks;
	}
	
	private void sortSentRanks(double[] ranks) {
		Sentence[] sentRanks = new Sentence[ranks.length];
		for (int i = 0; i < ranks.length; i++) 
			sentRanks[i] = new Sentence(names[i], sentences[i], ranks[i]);
		Arrays.sort(sentRanks);
		
		for (int i = 0; i < sentRanks.length; i++) {
			System.out.println(sentRanks[i].name + ": " + sentRanks[i].pagerank);
			System.out.println(sentRanks[i].sentence + '\n');
		}
	}
	
	private class Sentence implements Comparable<Sentence> {
		public String name;
		public String sentence;
		public double pagerank;

		public Sentence(String s, String s2, double p) {
			name = s;
			sentence = s2;
			pagerank = p;
		}

		@Override
		public int compareTo(Sentence p) {
			return Double.compare(p.pagerank, pagerank);
		}
	}
}
