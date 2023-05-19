package src;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

public class NLPIndexCreator {

    private Set<String> dataSetWords;
    private String embPath;

    public NLPIndexCreator(Set<String> dataSetWords, String embPath) {
        this.dataSetWords = dataSetWords;
        this.embPath = embPath;
    }

    public ByteBuffersDirectory createIndex() throws IOException {
        ByteBuffersDirectory index = new ByteBuffersDirectory();
        WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);

        try (BufferedReader reader = new BufferedReader(new FileReader(embPath))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                addDoc(w, line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        w.close();
        return index;
    }

    private void addDoc(IndexWriter w, String line) throws IOException {
        Document doc = new Document();
        String[] parts = line.split(" ", 2);
        String word = parts[0];
        String vec = parts[1];
        if (dataSetWords.contains(word)) {
            doc.add(new TextField("word", word, Field.Store.YES));
            doc.add(new StoredField("vec", vec));
            w.addDocument(doc);
        }
    }
}
