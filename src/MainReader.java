package src;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.util.*;


public class MainReader {
    private ArrayList<Document> foundDocuments;
    private ArrayList<StringBuilder> htmlDocuments;
    private List<List<StringBuilder>> htmlPages = new ArrayList<>();
    private Query lastQuery;
    private String lastField;
    private List<List<StringBuilder>> htmlPagesCopy;
    private FSDirectory index;

    public MainReader(FSDirectory index) {
        this.index = index;
    }

    public int runQuery(String queryStr, String field) throws IOException, ParseException, InvalidTokenOffsetsException {
        foundDocuments = new ArrayList<>();

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Query query = new QueryParser(field, analyzer).parse(queryStr);
        lastQuery = query;
        lastField = field;

        int hitsPerPage = 100;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        fillFoundDocuments(searcher, hits);
        highlightedHTMLResult(query, field);
        createPages();
        System.out.println("Found " + hits.length + " hits.");

        return hitsPerPage;
    }

    private void fillFoundDocuments(IndexSearcher searcher, ScoreDoc[] hits) throws IOException {
        for(int i = 0; i< hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            foundDocuments.add(d);
        }
    }

    private void highlightedHTMLResult(Query q, String field) throws InvalidTokenOffsetsException, IOException {
        htmlDocuments = new ArrayList<>();
        String header = "<u style=\"font-size:18px\">";

        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter();
        QueryScorer scorer = new QueryScorer(q, field);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        highlighter.setTextFragmenter(new SimpleFragmenter(10000));
        StandardAnalyzer analyzer = new StandardAnalyzer();

        StringBuilder resultBuilder;
        String text;
        TokenStream tokenStream;
        String highlightedText;
        for(Document doc: foundDocuments) {
            resultBuilder = new StringBuilder();
            text = doc.get(field);
            tokenStream = TokenSources.getTokenStream(field, text, analyzer);
            highlightedText = highlighter.getBestFragment(tokenStream, text);


            if (field.equals("artist")) {
                resultBuilder.append(header + highlightedText);
                resultBuilder.append(" - " + doc.get("song") + "</u>");
                resultBuilder.append("<br>" + doc.get("text").replace("  ", "<br>").substring(0, 80) + "<br>");
            } else if (field.equals("song")) {
                resultBuilder.append(header + doc.get("artist") + " - ");
                resultBuilder.append(highlightedText + "</u>");
                resultBuilder.append("<br>" + doc.get("text").replace("  ", "<br>").substring(0, 80) + "<br>");
            } else if (field.equals("text")){
                resultBuilder.append(header + doc.get("artist") + " - ");
                resultBuilder.append(doc.get("song") + "</u>");
                resultBuilder.append("<br>" + highlightedText.replace("  ", "<br>") + "<br>");
            } else {
                String[] result = highlightedText.split("@", 3);
                resultBuilder.append(header + result[0] + " - " +result[1] + "</u><br>" + result[2].replace("  ", "<br>") + "<br>");
            }

            htmlDocuments.add(resultBuilder);
//            System.out.println(resultBuilder);
        }
    }

    private void createPages() {
        htmlPages = new ArrayList<>();
        ArrayList<StringBuilder> page = new ArrayList<>();
        int batch = 0;
        for (StringBuilder doc: htmlDocuments) {
            if (batch == 10) {
                htmlPages.add(page);
                page = new ArrayList<>(Arrays.asList(doc));
                batch = 1;
            }else {
                page.add(doc);
                batch = batch + 1;
            }
        }
        htmlPages.add(page);
    }

    public void sortDocumentsBy(String field) {
        try{
            htmlPagesCopy = new ArrayList<>(htmlPages);
            Collections.sort(foundDocuments, Comparator.comparing(d -> d.get(field)));
            highlightedHTMLResult(lastQuery, lastField);
            createPages();
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toUnsortedDocuments() {
        htmlPages = htmlPagesCopy;
    }

    public ArrayList<Document> getFoundDocuments() {
        return foundDocuments;
    }

    public ArrayList<StringBuilder> getHtmlDocuments() {
        return htmlDocuments;
    }

    public List<List<StringBuilder>> getHtmlPages() {
        return htmlPages;
    }
}

