package src;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;


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

        var scene = new Scene(root, 600, 500);
        var lbl = new Label("Lucene Search Engine\n");
        lbl.setAlignment(Pos.CENTER);


        MainReader reader = new MainReader();

        lbl.setFont(Font.font("Serif", FontWeight.BOLD, 30));


        TextField b = new TextField();
        Button button = new Button();
        button.setText("Next");


        final int[] count = {10};

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e)
            {
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
                        variableText[0] = variableText[0] + reader.getFoundDocuments().get(i).get("artist") + " - " + reader.getFoundDocuments().get(i).get("song") + "\n\n";

                    }
                    count[0] = 10;

                    text.setText(variableText[0]);

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
                    System.out.printf("%3d. %30s - %s\n", (i + 1), reader.getFoundDocuments().get(i).get("artist"),
                            reader.getFoundDocuments().get(i).get("song"));
                }
                count[0] = count[0] + 10;

                if (count[0] > documentsNum) {
                    System.out.println("END");
                }
            }
        };

        button.setOnAction(buttonNext);
        b.setOnAction(event);

        root.getChildren().addAll(lbl, b, button, scrollPane);
        stage.setTitle("Lucene Search Engine");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}