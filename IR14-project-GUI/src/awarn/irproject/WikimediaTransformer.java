package awarn.irproject;

import java.io.StringWriter;

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

}