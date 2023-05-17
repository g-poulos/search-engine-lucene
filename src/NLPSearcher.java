package src;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
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

public class NLPSearcher {
    private FSDirectory nlpIndex;

    public NLPSearcher(FSDirectory nlpIndex) {
        this.nlpIndex = nlpIndex;
    }

    public static double cosineSimilarity(double[] vector1, double[] vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += Math.pow(vector1[i], 2);
            norm2 += Math.pow(vector2[i], 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public String vectorize(String queryStr) throws ParseException, IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Query query = new QueryParser("word", analyzer).parse(queryStr);

        int hitsPerPage = 1;
        IndexReader reader = DirectoryReader.open(nlpIndex);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        System.out.println("Found " + hits.length + " hits.");
        return searcher.doc(hits[0].doc).get("vec");
    }

    public static Double[] toDoubleVector(String vec) {
        String[] strVec = vec.split(" ");
        Double[] doubleVec = new Double[strVec.length];

        for (int i = 0; i < strVec.length; i++) {
            doubleVec[i] = Double.parseDouble(strVec[i]);
        }
        return doubleVec;
    }

    public static void main(String[] args) throws IOException, ParseException {
        Path path = Paths.get(System.getProperty("user.dir") + "/emb_index");
        FSDirectory index = FSDirectory.open(path);

        NLPSearcher s = new NLPSearcher(index);
        s.vectorize("lo");

    }
}
