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

public class IndexReader {


    public static void main(String[] args) throws IOException, ParseException {
        Path path = Paths.get(System.getProperty("user.dir") + "/index");
        FSDirectory index = FSDirectory.open(path);


        StandardAnalyzer analyzer = new StandardAnalyzer();

        String queryStr = args.length > 0 ? args[0] : "come in";
        Query query = new QueryParser("song", analyzer).parse(queryStr);


        int hitsPerPage = 100;
        org.apache.lucene.index.IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;



        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("artist") + " -- " + d.get("song"));
        }
        System.out.println("Found " + hits.length + " hits.");
    }
}
