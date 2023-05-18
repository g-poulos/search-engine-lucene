package src;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.uniformsplit.sharedterms.STIntersectBlockReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NLPSearcher {
    private FSDirectory nlpIndex;
    private List<Suggestion> suggestions = new ArrayList<>();

    public NLPSearcher(FSDirectory nlpIndex) {
        this.nlpIndex = nlpIndex;
    }

    public void searchSuggestions(String inputQuery, Set<String> uniqueWords) throws ParseException {
        ArrayList<Document> inputQueryVec = vectorize(inputQuery, 1);
        if (!inputQueryVec.isEmpty()) {
            System.out.print("Found suggestions for: " + inputQuery);
        } else {
            System.out.print("Cannot find suggestions for: " + inputQuery);
            return;
        }

        List<Document> similarWords = vectorize(inputQuery, 100);
        ArrayList<Suggestion> cosineSimilarities = new ArrayList<>();
        double cs;
        for (Document d: similarWords) {
            if (uniqueWords.contains(d.get("word")) && !d.get("word").equals(inputQuery)) {
                cs = cosineSimilarity(toDoubleVector(inputQueryVec.get(0).get("vec")),
                        toDoubleVector(d.get("vec")));
                cosineSimilarities.add(new Suggestion(d.get("word"), cs));

            }
        }

        Set<Suggestion> uniqueSet = new HashSet<>(cosineSimilarities);
        suggestions = new ArrayList<>(uniqueSet);
        Collections.sort(suggestions, Comparator.comparing(s -> s.getSimilarity()));
        Collections.reverse(suggestions);
    }

    private ArrayList<Document> vectorize(String queryStr, int numOfHits) throws ParseException {
        ArrayList<Document> similarWords = new ArrayList<>();
        FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("word", queryStr));

        IndexSearcher searcher = null;
        ScoreDoc[] hits = new ScoreDoc[0];
        String strVec = null;
        try {
            IndexReader reader = DirectoryReader.open(nlpIndex);
            searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(fuzzyQuery, numOfHits);
            hits = docs.scoreDocs;

            for(int i = 0; i< hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                similarWords.add(d);
            }
        } catch (IOException e) {
            System.out.println("Vectorize Query: Cannot open index");
        }
        return similarWords;
    }

    private static double cosineSimilarity(double[] vector1, double[] vector2) {
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

    private static double[] toDoubleVector(String vec) {
        String[] strVec = vec.split(" ");
        double[] doubleVec = new double[strVec.length];

        for (int i = 0; i < strVec.length; i++) {
            doubleVec[i] = Double.parseDouble(strVec[i]);
        }
        return doubleVec;
    }

    public ArrayList<String> getSuggestionWords() {
        ArrayList<String> words = new ArrayList<>();
        for (Suggestion s: suggestions) {
            words.add(s.getWord());
        }
        return words;
    }
}
