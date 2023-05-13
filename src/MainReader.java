package src;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainReader {
    private ArrayList<Document> foundDocuments;
    private ArrayList<StringBuilder> htmlDocuments;
    private List<List<StringBuilder>> htmlPages = new ArrayList<>();

    public void runQuery(String queryStr, String field) throws IOException, ParseException, InvalidTokenOffsetsException {
        foundDocuments = new ArrayList<>();
        htmlDocuments = new ArrayList<>();
        htmlPages = new ArrayList<>();

        Path path = Paths.get(System.getProperty("user.dir") + "/index");
        FSDirectory index = FSDirectory.open(path);

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Query query = new QueryParser(field, analyzer).parse(queryStr);

        int hitsPerPage = 100;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        fillFoundDocuments(searcher, hits);
        highlightedHTMLResult(query, field);
        System.out.println("Found " + hits.length + " hits.");

        createPages();
    }

    private void createPages() {
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

    private void fillFoundDocuments(IndexSearcher searcher, ScoreDoc[] hits) throws IOException {
        for(int i = 0; i< hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            foundDocuments.add(d);
        }
    }

    private void highlightedHTMLResult(Query q, String field) throws InvalidTokenOffsetsException, IOException {
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter();
        QueryScorer scorer = new QueryScorer(q, field);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        highlighter.setTextFragmenter(new SimpleFragmenter(100));
        StandardAnalyzer analyzer = new StandardAnalyzer();

        StringBuilder resultBuilder;
        htmlDocuments = new ArrayList<>();

        for(Document doc: foundDocuments) {
            resultBuilder = new StringBuilder();
            String text = doc.get(field);
            TokenStream tokenStream = TokenSources.getTokenStream(field, text, analyzer);
            String highlightedText = highlighter.getBestFragment(tokenStream, text);


            if (field.equals("artist")) {
                resultBuilder.append("<u>" + highlightedText);
                resultBuilder.append(" - " + doc.get("song") + "</u>");
                resultBuilder.append("<br>" + doc.get("text").replace("  ", "<br>").substring(0, 80) + "<br>");
            } else if (field.equals("song")) {
                resultBuilder.append("<u>" + doc.get("artist") + " - ");
                resultBuilder.append(highlightedText + "</u>");
                resultBuilder.append("<br>" + doc.get("text").replace("  ", "<br>").substring(0, 80) + "<br>");
            } else {
                resultBuilder.append("<u>" + doc.get("artist") + " - ");
                resultBuilder.append(doc.get("song") + "</u>");
                resultBuilder.append("<br>" + highlightedText.replace("  ", "<br>") + "<br>");
            }

            htmlDocuments.add(resultBuilder);
            System.out.println(resultBuilder);
        }
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

