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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.lucene.luke.models.documents.TermPosting;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class SearchEngineGUI extends Application {
    private String searchField = "all";
    private TextField searchInput = new TextField();
    private int pageNumber = 0;
    private Label pageLabel = new Label();
    private List<String> suggestions = new ArrayList<>();
    private ListView<String> similarWords = new ListView<>();
    private WebView webView = new WebView();
    private static MainReader reader;
    private static NLPSearcher nlpSearcher;


    @Override
    public void start(Stage stage) {
        initUI(stage);
    }

    private void initUI(Stage stage) {
        var root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        var scene = new Scene(root, 1000, 800);

        var lbl = new Label("Lucene Search Engine\n");
        lbl.setAlignment(Pos.CENTER);
        lbl.setFont(Font.font("Serif", FontWeight.BOLD, 30));

        HBox radioButtons = getRadioButtonOptions();
        HBox suggestionSection = getSuggestionSection();


        HBox searchOptions = getSearchOptions();
        HBox searchRow = getSearchRow();

        webView.setPrefHeight(700);
        root.getChildren().addAll(lbl, searchRow, suggestionSection, radioButtons, searchOptions, webView);
        stage.setTitle("Lucene Search Engine");
        stage.setScene(scene);
        stage.show();
    }

    private HBox getSearchRow() {
        EventHandler<ActionEvent> search = e -> this.search_query();
        Button searchButton = new Button();
        searchButton.setText("Search");
        searchButton.setOnAction(search);
        searchInput.setOnAction(search);
        searchInput.setPrefWidth(450);

        HBox searchRow = new HBox(searchInput, searchButton);
        searchRow.setAlignment(Pos.CENTER);
        searchRow.setPrefSize(500, 50);
        return searchRow;
    }

    private HBox getSearchOptions() {
        Label sortBy = new Label("Sort By\n");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("None", "Artist", "Song", "Lyrics");
        comboBox.setValue("None");

        Button prevButton = new Button();
        prevButton.setText("Previous");
        Button nextButton = new Button();
        nextButton.setText("Next");

        var buttons = new HBox(sortBy, comboBox, pageLabel, prevButton, nextButton);
        HBox.setMargin(pageLabel, new Insets(0, 30, 0, 0));
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER_LEFT);

        comboBox.setOnAction(event -> {
            String selectedValue = comboBox.getValue();
            if (selectedValue.equals("Artist")) {
                reader.sortDocumentsBy("artist");
            } else if (selectedValue.equals("Song")) {
                reader.sortDocumentsBy("song");
            } else if (selectedValue.equals("Lyrics")) {
                reader.sortDocumentsBy("text");
            } else {
                reader.toUnsortedDocuments();
            }
            pageNumber = 0;
            showPage(reader.getHtmlPages().get(pageNumber));
        });
        EventHandler<ActionEvent> buttonNext = e -> this.show_next(reader);
        EventHandler<ActionEvent> buttonPrev = e -> this.show_prev(reader);


        nextButton.setOnAction(buttonNext);
        prevButton.setOnAction(buttonPrev);
        return buttons;
    }

    private HBox getSuggestionSection() {
        Button findSimilar = new Button();
        findSimilar.setText("Find\nSimilar");
        findSimilar.setPrefSize(70, 100);
        EventHandler<ActionEvent> findSimilarWords = e -> {
            try {
                nlpSearcher.searchSuggestions(searchInput.getText());
            } catch (ParseException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            ObservableList<String> items = FXCollections.observableArrayList(nlpSearcher.getSuggestionWords());
            similarWords.setItems(items);
        };
        findSimilar.setOnAction(findSimilarWords);

        similarWords.setOnMouseClicked(event -> {
            String selectedSuggestion = similarWords.getSelectionModel().getSelectedItem();
            if (selectedSuggestion != null) {
                searchInput.setText(selectedSuggestion);
                similarWords.getItems().clear();
                this.search_query();
            }
        });
        similarWords.setPrefSize(200, 100);

        ListView<String> suggestionsListView = getSuggestionsListView(suggestions);
        suggestionsListView.setPrefSize(200, 100);

        Button clearSuggestions = new Button();
        clearSuggestions.setText("Clear");
        clearSuggestions.setPrefSize(70, 100);
        EventHandler<ActionEvent> clear = e -> {
            suggestionsListView.getItems().clear();
            suggestions.clear();
        };

        clearSuggestions.setOnAction(clear);
        HBox suggestionSection = new HBox(findSimilar, similarWords, suggestionsListView, clearSuggestions);
        suggestionSection.setAlignment(Pos.CENTER);
        suggestionSection.setSpacing(2);
        suggestionSection.setMinSize(800,100);
        suggestionSection.setMaxSize(800, 100);
        return suggestionSection;
    }

    private ListView<String> getSuggestionsListView(List<String> suggestions) {
        ListView<String> suggestionsListView = new ListView<>();

        searchInput.textProperty().addListener((observable, oldValue, newValue) -> {
            List<String> filteredSuggestions = new ArrayList<>();
            for (String suggestion : suggestions) {
                if (suggestion.toLowerCase().startsWith(newValue.toLowerCase())) {
                    filteredSuggestions.add(suggestion);
                }
            }
            suggestionsListView.setItems(FXCollections.observableArrayList(filteredSuggestions));
        });

        suggestionsListView.setOnMouseClicked(event -> {
            String selectedSuggestion = suggestionsListView.getSelectionModel().getSelectedItem();
            if (selectedSuggestion != null) {
                searchInput.setText(selectedSuggestion);
                suggestionsListView.getItems().clear();
                this.search_query();

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
        RadioButton radioButton4 = new RadioButton("All");

        radioButton1.setToggleGroup(toggleGroup);
        radioButton2.setToggleGroup(toggleGroup);
        radioButton3.setToggleGroup(toggleGroup);
        radioButton4.setToggleGroup(toggleGroup);

        EventHandler<ActionEvent> setToArtist = e -> this.setSearchField("artist");
        EventHandler<ActionEvent> setToSong = e -> this.setSearchField("song");
        EventHandler<ActionEvent> setToLyrics = e -> this.setSearchField("text");
        EventHandler<ActionEvent> setToAll = e -> this.setSearchField("all");

        radioButton1.setOnAction(setToArtist);
        radioButton2.setOnAction(setToSong);
        radioButton3.setOnAction(setToLyrics);
        radioButton4.setOnAction(setToAll);
        radioButton4.setSelected(true);

        var radioButtons = new HBox(chooseFieldText, radioButton1, radioButton2, radioButton3, radioButton4);
        radioButtons.setAlignment(Pos.CENTER);
        radioButtons.setSpacing(10);
        return radioButtons;
    }

    private void search_query() {
        System.out.println("Search Field: " + this.searchField);
        try {
            int hits = reader.runQuery(searchInput.getText(), this.searchField);
            if (hits > 0) {
                this.showPage(reader.getHtmlPages().get(pageNumber));



            } else {
                webView.getEngine().loadContent("No Results");
            }

            if (suggestions.contains(searchInput.getText())) {
                suggestions.remove(searchInput.getText());
            }
            this.suggestions.add(0, searchInput.getText());

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void show_next(MainReader reader) {
        if (pageNumber + 1 > reader.getHtmlPages().size()-1) {
            pageNumber = 0;
        } else {
            pageNumber++;
        }
        this.showPage(reader.getHtmlPages().get(pageNumber));
    }

    private void show_prev(MainReader reader) {
        if (pageNumber -1 < 0) {
            pageNumber = reader.getHtmlPages().size()-1;
        } else {
            pageNumber = pageNumber - 1;
        }

        this.showPage(reader.getHtmlPages().get(pageNumber));
    }

    private void showPage(List<StringBuilder> page) {
        String pageString = "";
        if (page.size() == 0) {
            pageString = "No Results!";
        }

        for (int i = 0; i < page.size(); ++i) {
            pageString = pageString + page.get(i) + "<br>";
        }
        webView.getEngine().loadContent(pageString);
        updatePageNum();
    }

    private void setSearchField(String field) {
        this.searchField = field;
    }

    private void updatePageNum() {
        pageLabel.setText("Page: " + (pageNumber + 1) + " out of " + reader.getHtmlPages().size());
    }

    public static void main(String[] args) throws IOException {
        String indexPath = args[0];
        String embPath = args[1];

        Path songIndexpath = Paths.get(indexPath);
        FSDirectory songIndex = FSDirectory.open(songIndexpath);
        reader = new MainReader(songIndex);

        NLPIndexCreator nlpCreator = new NLPIndexCreator(reader.getUniqueWords(), embPath);
        ByteBuffersDirectory nlpIndex = nlpCreator.createIndex();
        nlpSearcher = new NLPSearcher(nlpIndex);

        launch(args);
    }
}