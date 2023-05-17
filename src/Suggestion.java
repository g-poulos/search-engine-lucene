package src;

public class Suggestion {
    private String word;
    private Double similarity;

    public Suggestion(String word, Double similarity) {
        this.word = word;
        this.similarity = similarity;
    }

    @Override
    public String toString() {
        return word + ": " + similarity;
    }

    public Double getSimilarity() {
        return similarity;
    }
}
