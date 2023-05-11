package src;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainReader {
    private ArrayList<Document> foundDocuments;
    private List<List<Document>> documentPages = new ArrayList<>();


    public void runQuery(String queryStr, String field) throws IOException, ParseException {
        foundDocuments = new ArrayList<>();
        documentPages = new ArrayList<>();

        Path path = Paths.get(System.getProperty("user.dir") + "/index");
        FSDirectory index = FSDirectory.open(path);


        StandardAnalyzer analyzer = new StandardAnalyzer();
        Query query = new QueryParser(field, analyzer).parse(queryStr);

        int hitsPerPage = 100;
        org.apache.lucene.index.IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;


        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            foundDocuments.add(d);
        }

        System.out.println("Found " + hits.length + " hits.");

        ArrayList<Document> page = new ArrayList<>();
        int batch = 0;
        for (Document doc: foundDocuments) {
            if (batch == 10) {
                documentPages.add(page);
                page = new ArrayList<>(Arrays.asList(doc));
                batch = 1;
            }else {
                page.add(doc);
                batch = batch + 1;
            }
        }
        documentPages.add(page);
    }

    public ArrayList<Document> getFoundDocuments() {
        return foundDocuments;
    }

    public List<List<Document>> getDocumentPages() {
        return documentPages;
    }
}

