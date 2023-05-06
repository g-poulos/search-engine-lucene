package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.*;

public class IndexCreator {

    public static ArrayList<String[]> readData(String filename) {
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

    public static void main(String[] args){
        ArrayList<String[]> textList = readData("spotify_millsongdata_cl.csv");
        for (String[] i: textList) {
            System.out.println(i[0]);
            System.out.println(i[1]);
            System.out.println(i[2]);
            System.out.println("--------------------");
        }
    }

}
