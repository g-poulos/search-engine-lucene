package src;


import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NLPIndexCreator {

    private static void createIndex() throws IOException {
        Path path = Paths.get(System.getProperty("user.dir") + "/emb_index");
        FSDirectory index = FSDirectory.open(path);
        WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);

        try (BufferedReader reader = new BufferedReader(new FileReader("embmodel/wiki-news-300d-1M.vec"))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                addDoc(w, line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        w.close();
    }

    private static void addDoc(IndexWriter w, String line) throws IOException {
        Document doc = new Document();
        String[] parts = line.split(" ", 2);
        String word = parts[0];
        String vec = parts[1];

        doc.add(new TextField("word", word, Field.Store.YES));
        doc.add(new StoredField("vec", vec));
        w.addDocument(doc);
    }

    public static void main(String[] args) throws IOException {
        createIndex();
    }
}
