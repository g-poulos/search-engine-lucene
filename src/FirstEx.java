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
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import java.io.IOException;
import java.util.List;


public class FirstEx extends Application {
    int pageNumber = 0;

    @Override
    public void start(Stage stage) {

        initUI(stage);
    }

    private void initUI(Stage stage) {

        Text text = new Text();
        text.setFont(new Font(13));
        text.setTextAlignment(TextAlignment.LEFT);

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

        EventHandler<ActionEvent> event = e -> this.search_query(text, reader, b);
        EventHandler<ActionEvent> buttonNext = e -> this.show_next(text, reader);
        EventHandler<ActionEvent> buttonPrev = e -> this.show_prev(text, reader);

        nextButton.setOnAction(buttonNext);
        prevButton.setOnAction(buttonPrev);
        b.setOnAction(event);

        root.getChildren().addAll(lbl, b, buttons, scrollPane);
        stage.setTitle("Lucene Search Engine");
        stage.setScene(scene);
        stage.show();
    }

    private void search_query(Text text, MainReader reader, TextField b) {
        String input = b.getText();

        try {
            reader.runQuery(input, "song");
            this.showPage(reader.getDocumentPages().get(pageNumber), text);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }

    private void show_next(Text text, MainReader reader) {
        if (pageNumber < reader.getDocumentPages().size()-1)
            pageNumber = pageNumber + 1;
        this.showPage(reader.getDocumentPages().get(pageNumber), text);

    }

    private void show_prev(Text text, MainReader reader) {
        if (pageNumber > 1)
            pageNumber = pageNumber - 1;
        this.showPage(reader.getDocumentPages().get(pageNumber), text);

    }

    private void showPage(List<Document> page, Text text) {
        String pageString = "";
        for (int i = 0; i < page.size(); ++i) {
            System.out.printf("%3d. %30s - %s\n", (i + 1), page.get(i).get("artist"), page.get(i).get("song"));
            pageString = pageString + page.get(i).get("artist") + " - " + page.get(i).get("song") + "\n";
        }
        text.setText(pageString);
    }

    public static void main(String[] args) {
        launch(args);
    }
}