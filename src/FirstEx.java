package src;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.ArrayList;

public class FirstEx extends Application {

    @Override
    public void start(Stage stage) {

        initUI(stage);
    }

    private void initUI(Stage stage) {

        var root = new TilePane();
        var scene = new Scene(root, 600, 500);
        var lbl = new Label("Simple JavaFX application.\n");
        MainReader reader = new MainReader();

        lbl.setFont(Font.font("Serif", FontWeight.BOLD, 20));


        TextField b = new TextField();
        Label l = new Label("no text");
        Button button = new Button();
        button.setText("Next");
        button.setTranslateX(150);
        button.setTranslateY(60);

        final int[] count = {10};

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                l.setText(b.getText());
                String input = b.getText();

                try {
                    reader.runQuery(input, "song");
                    int documentsNum = 10;

                    if (reader.getFoundDocuments().size() < 10) {
                        documentsNum = reader.getFoundDocuments().size();
                    }

                    for(int i=0; i<documentsNum; ++i) {
                        System.out.printf("%3d. %20s - %s\n", (i + 1), reader.getFoundDocuments().get(i).get("artist"),
                                                                        reader.getFoundDocuments().get(i).get("song"));
                    }
                    count[0] = 10;

                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
        };

        EventHandler<ActionEvent> buttonNext = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
                int documentsNum = count[0] + 10;

                if (documentsNum > reader.getFoundDocuments().size()) {
                    documentsNum =  reader.getFoundDocuments().size();
                }

                for (int i = count[0]; i < documentsNum; ++i) {
                    System.out.printf("%3d. %20s - %s\n", (i + 1), reader.getFoundDocuments().get(i).get("artist"),
                            reader.getFoundDocuments().get(i).get("song"));
                }
                count[0] = count[0] + 10;

            }
        };


        button.setOnAction(buttonNext);
        b.setOnAction(event);

        lbl.setStyle("-fx-alignment: center ");

        root.getChildren().add(lbl);
        root.getChildren().add(b);
        root.getChildren().add(l);
        root.getChildren().add(button);


        stage.setTitle("Lucene Search Engine");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}