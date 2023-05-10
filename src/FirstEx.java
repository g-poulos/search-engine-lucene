package src;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.lucene.queryparser.classic.ParseException;

import javax.swing.text.Document;
import java.io.IOException;
import java.util.ArrayList;


public class FirstEx extends Application {

    @Override
    public void start(Stage stage) {

        initUI(stage);
    }

    private void initUI(Stage stage) {

        Text text = new Text();
        text.setFont(new Font(13));
        text.setTextAlignment(TextAlignment.CENTER);
        final String[] variableText = {""};

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(text);


        var root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        var scene = new Scene(root, 700, 500);
        var lbl = new Label("Lucene Search Engine\n");
        lbl.setAlignment(Pos.CENTER);
        lbl.setFont(Font.font("Serif", FontWeight.BOLD, 30));


        MainReader reader = new MainReader();


        TextField b = new TextField();
        Button nextButton = new Button();
        nextButton.setText("Next");
        Button prevButton = new Button();
        prevButton.setText("Previous");

        var buttons = new HBox(prevButton, nextButton);
        buttons.setAlignment(Pos.CENTER);


        final int[] count = {10};

        int pageNumber = 0;

        EventHandler<ActionEvent> event = e -> {
            search_query(text, variableText, reader, b, count);
        };

        EventHandler<ActionEvent> buttonNext = e -> {
            show_next(text, variableText, reader, count);
        };

        nextButton.setOnAction(buttonNext);
        b.setOnAction(event);

        root.getChildren().addAll(lbl, b, buttons, scrollPane);
        stage.setTitle("Lucene Search Engine");
        stage.setScene(scene);
        stage.show();
    }

    private static void search_query(Text text, String[] variableText, MainReader reader, TextField b, int[] count) {
        String input = b.getText();

        try {
            reader.runQuery(input, "song");
            int documentsNum = 10;

            if (reader.getFoundDocuments().size() < 10) {
                documentsNum = reader.getFoundDocuments().size();
            }

            for(int i=0; i<documentsNum; ++i) {
                System.out.printf("%3d. %30s - %s\n", (i + 1), reader.getFoundDocuments().get(i).get("artist"),
                                                                reader.getFoundDocuments().get(i).get("song"));
                variableText[0] = variableText[0] + reader.getFoundDocuments().get(i).get("artist") + " - " +
                                                    reader.getFoundDocuments().get(i).get("song") + "\n\n";

            }
            count[0] = 10;
            text.setText(variableText[0]);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }

    private static void show_next(Text text, String[] variableText, MainReader reader, int[] count) {
        variableText[0] = "";
        int documentsNum = count[0] + 10;

        if (documentsNum > reader.getFoundDocuments().size()) {
            documentsNum =  reader.getFoundDocuments().size();
        }

        for (int i = count[0]; i < documentsNum; ++i) {
            System.out.printf("%3d. %30s - %s\n", (i + 1), reader.getFoundDocuments().get(i).get("artist"),
                    reader.getFoundDocuments().get(i).get("song"));
            variableText[0] = variableText[0] + reader.getFoundDocuments().get(i).get("artist") + " - " +
                                                reader.getFoundDocuments().get(i).get("song") + "\n\n";
        }
        count[0] = count[0] + 10;
        text.setText(variableText[0]);

        if (count[0] > documentsNum) {
            System.out.println("END");
        }
    }


    private void showPage(int pageNumber, ArrayList<Document> page) {
        String pageString = "";

        for (int i = 0; i < page.size(); ++i) {
            System.out.printf("%3d. %30s - %s\n", (i + 1), page.get(i).get("artist"), page.get(i).get("song"));
            pageString = pageString + page.get(i).get("artist") + " - " + page.get(i).get("song");
        }
        text.setText(variableText[0]);

    }

    public static void main(String[] args) {
        launch(args);
    }
}