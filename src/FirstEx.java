package src;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FirstEx extends Application {
    int pageNumber = 0;
    List<String> suggestions = new ArrayList<>();
    private String searchField = "song";
    private TextFlow out = new TextFlow();
    private TextField searchInput = new TextField();

    @Override
    public void start(Stage stage) {

        initUI(stage);
    }

    private void initUI(Stage stage) {
        Text outputList = new Text();
        outputList.setFont(new Font(15));
        outputList.setTextAlignment(TextAlignment.LEFT);

//        WebView webView = new WebView();
//        String htmlContent = "<html><body><h1>Hello, World!</h1></body></html>";
//        webView.getEngine().loadContent(htmlContent);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(out);

        var root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        var scene = new Scene(root, 800, 600);
        var lbl = new Label("Lucene Search Engine\n");
        lbl.setAlignment(Pos.CENTER);
        lbl.setFont(Font.font("Serif", FontWeight.BOLD, 30));

        HBox radioButtons = getRadioButtonOptions();
        MainReader reader = new MainReader();


        ListView<String> suggestionsListView = getSuggestionsListView(outputList, reader, searchInput);
        suggestionsListView.setPrefHeight(100);

        Button nextButton = new Button();
        nextButton.setText("Next");

        Button prevButton = new Button();
        prevButton.setText("Previous");

        var buttons = new HBox(prevButton, nextButton);
        buttons.setAlignment(Pos.CENTER);

        EventHandler<ActionEvent> search = e -> this.search_query(outputList, reader, searchInput);
        EventHandler<ActionEvent> buttonNext = e -> this.show_next(outputList, reader);
        EventHandler<ActionEvent> buttonPrev = e -> this.show_prev(outputList, reader);

        nextButton.setOnAction(buttonNext);
        prevButton.setOnAction(buttonPrev);
        searchInput.setOnAction(search);

        root.getChildren().addAll(lbl, searchInput, suggestionsListView, radioButtons, buttons, scrollPane);
        stage.setTitle("Lucene Search Engine");
        stage.setScene(scene);
        stage.show();
    }

    private ListView<String> getSuggestionsListView(Text text, MainReader reader, TextField searchField) {
        ListView<String> suggestionsListView = new ListView<>();

        // Update suggestions when the text changes
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            List<String> filteredSuggestions = new ArrayList<>();
            for (String suggestion : suggestions) {
                if (suggestion.toLowerCase().startsWith(newValue.toLowerCase())) {
                    filteredSuggestions.add(suggestion);
                }
            }
            suggestionsListView.setItems(FXCollections.observableArrayList(filteredSuggestions));
        });

        // Select a suggestion when clicked
        suggestionsListView.setOnMouseClicked(event -> {
            String selectedSuggestion = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selectedSuggestion != null) {
                searchField.setText(selectedSuggestion);
                suggestionsListView.getItems().clear();
                this.search_query(text, reader, searchField);

            }
        });
        return suggestionsListView;
    }

    private HBox getRadioButtonOptions() {
        var chooseFieldText = new Label("Search Field:");
        chooseFieldText.setFont(new Font(14));
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton radioButton1 = new RadioButton("Artist");
        RadioButton radioButton2 = new RadioButton("Song");
        RadioButton radioButton3 = new RadioButton("Lyrics");

        radioButton1.setToggleGroup(toggleGroup);
        radioButton2.setToggleGroup(toggleGroup);
        radioButton3.setToggleGroup(toggleGroup);

        EventHandler<ActionEvent> setToArtist = e -> this.setSearchField("artist");
        EventHandler<ActionEvent> setToSong = e -> this.setSearchField("song");
        EventHandler<ActionEvent> setToLyrics = e -> this.setSearchField("text");
        radioButton1.setOnAction(setToArtist);
        radioButton2.setOnAction(setToSong);
        radioButton3.setOnAction(setToLyrics);

        var radioButtons = new HBox(chooseFieldText, radioButton1, radioButton2, radioButton3);
        radioButtons.setAlignment(Pos.CENTER);
        radioButtons.setSpacing(10);
        return radioButtons;
    }

    private void search_query(Text text, MainReader reader, TextField b) {
        String input = b.getText();
        System.out.println("Search Field: " + this.searchField);

        try {
            reader.runQuery(input, this.searchField);
            this.showPage(reader.getDocumentPages().get(pageNumber), text);

            if (suggestions.contains(input)) {
                suggestions.remove(input);
            }
            this.suggestions.add(0, input);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
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


        // Split the text into words
        String[] words = text.getText().split(" ");

        for (String word : words) {
            Text wordText = new Text(word + " ");

            if (word.equalsIgnoreCase(searchInput.getText())) {
                wordText.setFill(Color.RED); // Set the color to red
            }

            out.getChildren().add(wordText);
        }
    }

    private void setSearchField(String field) {
        this.searchField = field;
    }

    public static void main(String[] args) {
        launch(args);
    }
}