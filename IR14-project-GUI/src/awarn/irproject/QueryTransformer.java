package awarn.irproject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A class designed to translate a user generated query into a search solr-URL.
 * 
 * Input: user query String Output: URL String
 * 
 * @author Mwli
 *
 */
public class QueryTransformer {
	public static String interpret(String query) {
		// TODO: Om vi m�ste skydda s� ska stoppa in backslash i
		// {"+", "-", "&", "|", "!", "(", ")", "{", "}", "[", "]", "^",
		// "\"", "~", "*", "?", ":", "\\" , "/"};
		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:8983/solr/solr/select?q=");
		StringBuilder qb = new StringBuilder();
		qb.append("text:" + query);
		qb.append(" titleText:" + query);
		qb.append(" text:\"" + query + "\"");
		qb.append(" titleText:\"" + query + "\"");
		try {
			sb.append(URLEncoder.encode(qb.toString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		sb.append("&wt=json&indent=true&rows=50");
		return sb.toString();
	}
}
