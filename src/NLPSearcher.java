package src;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import java.io.IOException;
import java.util.*;


public class NLPSearcher {
    private static double SIMILARITY_THRESHOLD = 0.5;
    private static int SIMILAR_WORD_COUNT = 7;

    private ByteBuffersDirectory nlpIndex;
    private List<String> suggestions;
    private Set<String> uniqueWords;

    public NLPSearcher(ByteBuffersDirectory nlpIndex) {
        this.nlpIndex = nlpIndex;
        createUniqueWords(nlpIndex);
    }

    private void createUniqueWords(ByteBuffersDirectory nlpIndex) {
        try {
            IndexReader reader = DirectoryReader.open(nlpIndex);
            uniqueWords = new HashSet<>();
            int numSegments = reader.leaves().size();
            for (int i = 0; i < numSegments; i++) {
                Terms terms = reader.leaves().get(i).reader().terms("word");

                TermsEnum termsEnum = terms.iterator();
                while (termsEnum.next() != null) {
                    String term = termsEnum.term().utf8ToString();
                    uniqueWords.add(term);
                }
            }
            System.out.println("Unique Words Created with " + uniqueWords.size() + " terms");
        } catch (IOException e) {
            System.out.println("ERROR: Unique Words creation failed. Unable to open index");
        }
    }

    public void searchSuggestions(String input) throws ParseException, IOException {
        Document inputQueryVec = vectorize(input);
        Document wordVec;
        suggestions = new ArrayList<>();

        for (String word: uniqueWords) {
            wordVec = vectorize(word);

            double cs = cosineSimilarity(toDoubleVector(inputQueryVec.get("vec")), toDoubleVector(wordVec.get("vec")));
            if (cs > SIMILARITY_THRESHOLD) {
                suggestions.add(word);
                if (suggestions.size() == SIMILAR_WORD_COUNT)
                    break;
            }
        }
    }

    private Document vectorize(String queryStr) throws ParseException, IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Query query = new QueryParser("word", analyzer).parse(queryStr);

        ScoreDoc[] hits = new ScoreDoc[0];

        IndexReader reader = DirectoryReader.open(nlpIndex);
        IndexSearcher searcher = searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, 1);
        hits = docs.scoreDocs;
        return searcher.doc(hits[0].doc);
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

    public List<String> getSuggestionWords() {
        return this.suggestions;
    }
}
