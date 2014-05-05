package wikipedia;

import java.io.StringWriter;
import java.util.Map;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.util.ServiceLocator;
import org.jsoup.Jsoup;

public class WikimediaTransformer {
	public static final String NAME_MEDIAWIKI = "MediaWiki";

	public static String wikiToPlainText(String text) {
		return Jsoup.parse(parseWikiText(text)).text();
	}

	public static String parseWikiText(String text) {
		MarkupLanguage language = ServiceLocator.getInstance()
				.getMarkupLanguage(NAME_MEDIAWIKI);
		StringWriter writer = new StringWriter();
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer);
		MarkupParser parser = new MarkupParser(language, builder);
		parser.parse(text);
		return writer.toString();
	}

	public Object transformRow(Map<String, Object> row) {
		row.put("error", "false");
		String text = (String) row.get("text");
		System.out.println(row.get("id"));
		try {
			if (row.get("$skipDoc") == "true") {
				System.out.println("fast skipping" + row.get("id"));
				row.put("error", "true");
				row.put("exception", "RegexTransformerSucces");

				return row;

			}
			if (text.startsWith("#REDIRECT")) {
				System.out.println("regex transfromer failed on "
						+ row.get("id"));
				row.put("error", "true");
				row.put("execption", "RegexTransformerFail");
				return row;
			}
			if (text != null) {
				String plainText = wikiToPlainText(text);
				row.put("text", plainText);
			}
		} catch (java.lang.Exception e) {
			System.out.println("error with  " + row.get("title"));
			System.out.println(e);
			row.put("exception", e);
			row.put("error", "true");
		}

		return row;
	}
}