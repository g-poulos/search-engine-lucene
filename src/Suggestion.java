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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Suggestion other = (Suggestion) obj;
        if (this.word.equals(other.word))
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        return word.hashCode() + similarity.hashCode();
    }

    public Double getSimilarity() {
        return similarity;
    }

    public String getWord() {
        return this.word;
    }
}
