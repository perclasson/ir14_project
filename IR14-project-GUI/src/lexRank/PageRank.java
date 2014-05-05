package lexRank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class PageRank {
	/**
	 * A memory-efficient representation of the transition matrix. The outlinks
	 * are represented as a Hashtable, whose keys are the numbers of the
	 * documents linked from.
	 * <p>
	 * 
	 * The value corresponding to key i is a Hashtable whose keys are all the
	 * numbers of documents j that i links to.
	 * <p>
	 * 
	 * If there are no outlinks from i, then the value corresponding key i is
	 * null.
	 */
	private static Hashtable<Integer, Hashtable<Integer, Boolean>> link = new Hashtable<Integer, Hashtable<Integer, Boolean>>();

	/** The number of outlinks from each node. */
	private static int[] out;

	/** The number of documents with no outlinks. */
	private static int numberOfSinks = 0;

	/**
	 * The probability that the surfer will be bored, stop following links, and
	 * take a random jump somewhere.
	 */
	private static final double BORED = 0.15;

	private static int numberOfDocs;

	public static double[] ComputeRank(int n, ArrayList<Integer>[] adjList) {
		numberOfDocs = n;
		readLexGraph(adjList);
		return noSinkPageRank();
	}

	private static void readLexGraph(ArrayList<Integer>[] adjList) {
		out = new int[adjList.length];
		int fileIndex = 0;
		for (int i = 0; i < adjList.length; i++) {
			ArrayList<Integer> curr = adjList[i];
			for (int n : curr) {
				if (link.get(i) == null)
					link.put(i, new Hashtable<Integer, Boolean>());
				link.get(i).put(n, true);
				out[i]++;
			}
		}

		// Compute the number of sinks.
		for (int i = 0; i < fileIndex; i++)
			if (out[i] == 0)
				numberOfSinks++;
	}

	/**
	 * Calculates the no sink approximation version of pagerank.
	 * 
	 * Algorithm: x and x' = initial guess and state
	 * 
	 * For every i For every link i -> j x'[j] += x[i] * c / out[i] x'[i] +=
	 * (1-c) / N x'[i] += s / N / N (s = antal sink) x = x' x' = 0
	 * 
	 * @return
	 */
	private static double[] noSinkPageRank() {
		final double c = 1 - BORED;
		// the probability of being bored and performing a random jump
		final double bored = 1.0 / numberOfDocs;

		double[] x = new double[numberOfDocs]; // a vector of all zeroes
		double[] xp = new double[numberOfDocs];
		Arrays.fill(xp, bored); // an initial "guess"

		for (int temp = 0; temp < 2000; temp++) {
			for (int i = 0; i < numberOfDocs; i++) {
				if (link.get(i) != null)
					for (Integer j : link.get(i).keySet())
						xp[j] += x[i] * c / out[i];

				xp[i] += (1 - c) / numberOfDocs;
				xp[i] += numberOfSinks / numberOfDocs / numberOfDocs;
			}
			x = xp;
			xp = new double[numberOfDocs];
		}

		return x;
	}
}