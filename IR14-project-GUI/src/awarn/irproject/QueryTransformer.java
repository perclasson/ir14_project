package awarn.irproject;
/**
 * A class designed to translate a user generated query into a search solr-URL.
 * 
 * Input: user query String Output: URL String
 * 
 * @author Mwli
 *
 */
public class QueryTransformer {
	public QueryTransformer(String[] args) {
		interpret(args);
	}

	public static String interpret(String[] args) {
		// TODO: Om vi m�ste skydda s� ska stoppa in backslash i
		// {"+", "-", "&", "|", "!", "(", ")", "{", "}", "[", "]", "^",
		// "\"", "~", "*", "?", ":", "\\" , "/"};
		StringBuilder sb = new StringBuilder();
		sb.append("http://localhost:8983/solr/solr/select?q=");
		for (String word : args) {
			char[] characters = word.toCharArray();
			for (char character : characters) {
				switch (character) {
				case '!':
					sb.append("%21");
					break;
				case '"':
					sb.append("%22");
					break;
				case '#':
					sb.append("%23");
					break;
				case '$':
					sb.append("%24");
					break;
				case '%':
					sb.append("%25");
					break;
				case '&':
					sb.append("%26");
					break;
				case '\'':
					sb.append("%27");
					break;
				case '(': // TODO: os�ker
					sb.append("%28");
					break;
				case ')': // TODO: os�ker
					sb.append("%29");
					break;
				case '*':
					sb.append("%2A");
					break;
				case '+':
					sb.append("%2B");
					break;
				case ',':
					sb.append("%2C");
					break;
				case '-':
					sb.append("%2D");
					break;
				case '.':
					sb.append("%2E");
					break;
				case '/':
					sb.append("%2F");
					break;
				case ':':
					sb.append("%3A");
					break;
				case ';':
					sb.append("%3B");
					break;
				case '<':
					sb.append("%3C");
					break;
				case '=':
					sb.append("%3D");
					break;
				case '>':
					sb.append("%3E");
					break;
				case '?':
					sb.append("%3F");
					break;
				case '@':
					sb.append("%40");
					break;
				case '[':
					sb.append("%5B");
					break;
				case '\\':
					sb.append("%5C");
					break;
				case ']':
					sb.append("%5D");
					break;
				case '^':
					sb.append("%5E");
					break;
				case '_':
					sb.append("%5F");
					break;
				case '�':
					sb.append("%60");
					break;
				case '{':
					sb.append("%7B");
					break;
				case '}':
					sb.append("%7C");
					break;
				// TODO:
				// case '�': //dec: 134, 132, 148, 143, 142, 153
				// sb.append("%E5");
				// case '�':
				// sb.append("%E4");
				// case '�':
				// sb.append("%F6");

				// case '�':
				// sb.append("%21");
				// case '�':
				// sb.append("%21");
				// case '�':
				// sb.append("%21");
				default:
					sb.append(character);
				}
			}
			sb.append("+"); // TODO
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("&wt=json&indent=true");
		return sb.toString();
	}
}
