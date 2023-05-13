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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SearchEngineGUI extends Application {
    private int pageNumber = 0;
    private List<String> suggestions = new ArrayList<>();
    private String searchField = "song";
    private TextField searchInput = new TextField();
    private WebView webView = new WebView();

    @Override
    public void start(Stage stage) {

        initUI(stage);
    }

    private void initUI(Stage stage) {
        Text outputList = new Text();
        outputList.setFont(new Font(15));
        outputList.setTextAlignment(TextAlignment.LEFT);


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


        ListView<String> suggestionsListView = getSuggestionsListView(reader, searchInput);
        suggestionsListView.setPrefHeight(100);
        webView.setPrefHeight(250);

        Button nextButton = new Button();
        nextButton.setText("Next");

        Button prevButton = new Button();
        prevButton.setText("Previous");

        var buttons = new HBox(prevButton, nextButton);
        buttons.setAlignment(Pos.CENTER);

        EventHandler<ActionEvent> search = e -> this.search_query(reader, searchInput);
        EventHandler<ActionEvent> buttonNext = e -> this.show_next(reader);
        EventHandler<ActionEvent> buttonPrev = e -> this.show_prev(reader);

        nextButton.setOnAction(buttonNext);
        prevButton.setOnAction(buttonPrev);
        searchInput.setOnAction(search);

        root.getChildren().addAll(lbl, searchInput, suggestionsListView, radioButtons, buttons, webView);
        stage.setTitle("Lucene Search Engine");
        stage.setScene(scene);
        stage.show();
    }

    private ListView<String> getSuggestionsListView(MainReader reader, TextField searchField) {
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
                this.search_query(reader, searchField);

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

    private void search_query(MainReader reader, TextField b) {
        String input = b.getText();
        System.out.println("Search Field: " + this.searchField);

        try {
            reader.runQuery(input, this.searchField);
            this.showPage(reader.getHtmlPages().get(pageNumber));

            if (suggestions.contains(input)) {
                suggestions.remove(input);
            }
            this.suggestions.add(0, input);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void show_next(MainReader reader) {
        if (pageNumber < reader.getHtmlPages().size()-1)
            pageNumber = pageNumber + 1;
        this.showPage(reader.getHtmlPages().get(pageNumber));

    }

    private void show_prev(MainReader reader) {
        if (pageNumber > 1)
            pageNumber = pageNumber - 1;
        this.showPage(reader.getHtmlPages().get(pageNumber));

    }

    private void showPage(List<StringBuilder> page) {
        String pageString = "";

        for (int i = 0; i < page.size(); ++i) {
            pageString = pageString + page.get(i) + "<br>";
        }
        webView.getEngine().loadContent(pageString);

    }

    private void setSearchField(String field) {
        this.searchField = field;
    }

    public static void main(String[] args) {
        launch(args);
    }
}