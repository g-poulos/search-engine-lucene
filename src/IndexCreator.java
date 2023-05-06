package src;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.lang.*;

public class IndexCreator {

    public ArrayList<String[]> readData(String filename) {
        String line;
        String artist;
        String song;

        ArrayList<String[]> songs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine();
            while ((line = br.readLine()) != null) {
                StringBuilder text;

                String[] data = line.split(",");
                artist = data[0];
                song = data[1];

                text = new StringBuilder(line.split("\"")[1]);

                while (true) {
                    line = br.readLine();
                    if (line.strip().equals("\"")) {
                        break;
                    }
                    text.append(line);

                }
                String[] csv_line = {artist, song, text.toString()};
                songs.add(csv_line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return songs;

    }


    public Directory createIndex(ArrayList<String[]> songs, StandardAnalyzer analyzer) throws IOException {

        Path path = Paths.get(System.getProperty("user.dir") + "/index");
        System.out.println();
        FSDirectory index = FSDirectory.open(path);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);

        for (String[] line: songs) {
            addDoc(w, line);
        }

        w.close();
        return index;
    }

    private static void addDoc(IndexWriter w, String[] line) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("artist", line[0], Field.Store.YES));         // TODO: Check the difference StringField/TextField
        doc.add(new TextField("song", line[1], Field.Store.YES));
        doc.add(new TextField("text", line[2], Field.Store.YES));
        w.addDocument(doc);
    }

    public static void main(String[] args) throws IOException {
        IndexCreator idxCreator = new IndexCreator();
        StandardAnalyzer analyzer = new StandardAnalyzer();

        ArrayList<String[]> textList = idxCreator.readData("spotify_millsongdata_cl.csv");
        idxCreator.createIndex(textList, analyzer);

//        for (String[] i: textList) {
//            System.out.println(i[0]);
//            System.out.println(i[1]);
//            System.out.println(i[2]);
//            System.out.println("--------------------");
//        }
    }

}