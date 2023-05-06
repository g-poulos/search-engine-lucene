package src;

import javax.lang.model.element.NestingKind;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.*;

public class CreateIndex {

    public static void createData() {

        String csvFile = "spotify_millsongdata_cl.csv"; // replace with the actual path to your CSV file

//        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
////
////            String line;
////
////            String row = br.readLine();
////            System.out.println(row);
////            int character;
////
////            while ((character = br.read()) != -1) {
////                Character ch = (char) character;
////                String t = "";
////                if (ch.equals("\"")) {
////                    t = readText(br);
////                }
////                System.out.println(t);
////            }
////
////        } catch (IOException e) {
////            e.printStackTrace();
////        }

        String line = "";
        String line1 = "";
        String cvsSplitBy = ",";
        String artist = "";
        String song = "";
        String text = "";
        ArrayList<String> textList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            br.readLine();
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] data = line.split(cvsSplitBy);

                artist = data[0];
                song = data[1];

                System.out.println(artist);
                System.out.println(song);

                // find where the text starts and ends
                int startIndex = line.indexOf('"') + 1;
//                int endIndex = line.lastIndexOf('"');

                System.out.println(startIndex);
//                System.out.println(endIndex);

                while ((line1 = br.readLine()).equals("\"")) {
                    textList.add(line1);
                }
                System.out.println(textList.toString());

                System.out.println("Artist: " + artist + " | Song: " + song + " | Text: " + text);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String readText(BufferedReader reader) throws IOException {
        String text = "";
        int character;
        while ((character = reader.read()) != -1) {
            Character ch = (char) character;
            if (ch.equals("\"")) {
                break;
            }
            text = text + ch;
        }
        return text;
    }


//    public static void createData(String csvFileName, Integer N) throws IOException {
//        String row;
//        int attrNames = 0;
//        int count = 0;
//        ArrayList<String> inputs = new ArrayList<String>();
//
//        try(BufferedReader csvReader = new BufferedReader(new FileReader(csvFileName))) {
//            if((row = csvReader.readLine()) != null){
//                attrNames = 3;
//            }
//            while ((row = csvReader.readLine()) != null && count <N) {	// read every line
//                String[] data = row.split(",");
//                for (int i = 0; i < data.length-1; i++) {
//                    System.out.print(data[i]);
//                }
//
//
//
////                for (int i = 0; i < attrNames; i++) {	// read every column
////                    String intputVal = data[i];
////                    inputs.add(intputVal);
////                }
////                count++;
//            }
//        }
//        for (int i = 0; i < 10; i++) {
//            System.out.print(inputs.get(i) + "\n");
//        }
//
//    }

    public static void main(String[] args){
        createData();
    }

}
