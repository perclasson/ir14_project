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

	public static void main(String[] args) {
		String test = "{{källor|datum=2013-05}}\n[[Fil:Denmark location amager.svg|miniatyr|Amagers position i Danmark.]]\n[[Fil:Amager.jpg|höger|miniatyr|250px|Amager från ovan.]] \n\n'''Amager''' är en [[Danmark|dansk]] [[ö (landområde)|ö]] i [[Öresund]]. Öns norra och västra delar tillhör [[Köpenhamn]], medan övriga delar upptas av [[Tårnby kommun]] och [[Dragørs kommun]]. \nAmager har en yta på 96,29 km² stor och befolkningen uppgår till 174 179 personer (1/1 2011). En stor del av bebyggelsen har förortsprägel, men även åtskilliga innerstadskvarter finns (i Köpenhamn, samt i Dragør). På den östra delen av ön finns [[Kastrups flygplats]].\n\nAmager är delvis en konstgjord ö, delvis en naturlig sådan. Ön är mycket låg och vissa delar ligger under havsytan, framförallt det genom fördämning och utfyllnad skapade området [[Kalvebod Fælled]] (Vestamager). Ön började uppodlas i större skala under 1500-talets första hälft då kung [[Kristian II]] bjöd in nederländska bönder till att odla upp ön för kronans räkning. Flertalet slog sig ned i fiskeläget [[Dragør]] där många än idag har namn med nederländskt ursprung som Neels och Tønnes. \n\nDen stora utvecklingsfasen började på 1800-talet då flera fabriker, bostadsområden och militär verksamhet utvecklade sig på ön. Ön, som är förbunden med övriga Köpenhamn genom flera broar, [[järnväg]] och [[Köpenhamns tunnelbana|Metro]], har alltid varit ett arbetarklassfäste med undantag för Dragör och vissa andra områden. Ön har aldrig ansetts vara riktigt \"fin\" i många{{vem|vilka|datum=2013-05}} köpenhamnsbors ögon, men inställningen håller på att ändras i takt med stigande huspriser. \n\nUnder 1900-talet började militären att utveckla stora övningsområden på ön vilket resulterade i Vestamager som idag innefattar ett stort naturområde endast några kilometer från Köpenhamns centrum. Under senare delen av 1900-talet har flygplatsen [[Kastrups flygplats|Kastrup]] som ligger på ön expanderat kraftigt och i samband med byggandet av [[Öresundsförbindelsen]] började även ett stort område byggas som kallas för [[Ørestad]]. \n\nÖn återfick i samband med bron även en järnvägsförbindelse vilket tidigare inte funnits sedan 1960-talet. En [[motorväg]] byggdes samtidigt över öns mellersta del. [[Köpenhamns metro]] går till Kastrup och Vestamager. Flera nya byggprojekt har startats i olika områden på ön under 00-talet, bland annat i [[Islands Brygge]] och i Ørestad.\n\nPå Amager finns bl.a [[Danmarks Radio]]s nya TV-hus (DR-byen), kongresscentret och mässhallarna [[Bella Center]] (där f.ö. det internationella klimatmötet [[COP15]] avhölls i december 2009). Kring metro- och regionaltågstationen Ørestad har ett stort kontors-, handels- och bostadsområde uppförts under [[00-talet]]. Detta sträcker sig från [[Bella Center]] i norr till metrostationen [[Vestamager (station)|Vestamager]] i söder. Även om området inte ligger vid kusten har Ørestad delvis fått karaktären av en [[sjöstad]] genom anläggandet av konstgjorda kanaler och dammar. Vid [[Islands Brygge]] finns en \"äkta\" sjöstad. \n\nFöre [[Öresundsförbindelsen]]s uppförande hade småstaden [[Dragør]] en färjeförbindelse med [[Limhamn]] i södra [[Malmö]]. På grund av denna fanns i Dragør tidigare en en betydande gränshandel. Denna har under 2000-talet helt upphört liksom den genomgående trafiken. \n\nÖns västra del, som utvanns från Öresund under 1920- och 30-talen, är med undantag för Bella Center, Ørestad samt ett koloniområde, avsatt som natur- och rekreationsområde. Marken är ängsmark (odlad mark finns dock på den naturliga delen av öns södra del). \nLängst i söder finns den flerhundraåriga skogen [[Kongelunden]].\n\nPå öns östra del, vid Öresundskusten, finns [[Amager Strandpark]]<!--stort S på danska--> med en populär sandstrand. Området har omgestaltats, med en konstgjord [[ö]] och en [[lagun]] innanför. Nyinvigningen av parken, som funnits sedan 1934, ägde rum 2005. \n\nAmager är den tätast befolkade ön i Danmark. Den näst mest tätbefolkade är [[Thurø]]. \n\n== Se även ==\n*[[Amagerbro]]\n*[[Amagerport]]\n*[[Amagerbroderi]]\n\n{{coord|55|37|N|12|35|E|type:isle_region:DK|display=title}}\n\n[[Kategori:Amager| ]]";
		System.out.println(wikiToPlainText(test));
	}
	
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