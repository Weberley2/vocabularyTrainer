package ta.vocable_trainer;

import java.util.*;

public class Vocable {

    // Determines ID for a new vocable.
    private static int nextID = 0;

    // Used to randomly select a word of the vocable.
    private static Random rng = new Random();

    private int id;

    // The currently asked word.
    private String lastWord = "";

    // The currently queried foreign word.
    private String lastForeignWord = "";

    // Contain all words and translations of the vocable
    private List<String> words;
    private List<String> foreignWords;

    // How often this vocable was asked.
    private int learnedCount;

    // How often the answer to a question containing this vocable was correct.
    private int correctCount;

    // Unix timestamp in seconds of when this vocable was created.
    private long creationTime;

    // Was this vocable changed during execution of the program.
    boolean Changed;

    private static int chooseID(){
        return nextID++;
    }

    /**
     * Used for restoring a vocable from the vocable file.
     * @param words Stored words.
     * @param foreignWords Stored translations.
     * @param learnedCount Stored count of how often this vocable was asked.
     * @param correctCount Stored count of how often the answer to a question containing this vocable was correct.
     * @param creationTime Stored unix timestamp in seconds of when this vocable was created.
     */
    Vocable(List<String> words, List<String> foreignWords, int learnedCount, int correctCount, long creationTime){
        id = chooseID();
        this.words = words;
        this.foreignWords = foreignWords;
        this.learnedCount = learnedCount;
        this.correctCount = correctCount;
        this.creationTime = creationTime;
        this.Changed = false;
    }

    /**
     * Used to create a new vocable through user input.
     * @param words List of words.
     * @param foreignWords List of translations.
     */
    Vocable(List<String> words, List<String> foreignWords){
        id = chooseID();
        this.words = words;
        this.foreignWords = foreignWords;
        this.learnedCount = 0;
        this.correctCount = 0;
        this.creationTime = new Date().getTime();
        this.Changed = true;
    }

    /**
     * Generate an functionally equivalent copy of the vocable that is independent from the original.
     * @param toCopy Vocable object to be copied.
     * @return New independent Vocable object with the same attributes as the original.
     */
    static Vocable copy(Vocable toCopy){
        return new Vocable(new ArrayList<>(toCopy.getWords()), new ArrayList<>(toCopy.getForeignWords()), toCopy.getLearnedCount(), toCopy.getCorrectCount(), toCopy.creationTime);
    }

    /**
     * Remove a word from the vocable. Can be native or foreign.
     * @param word Word to be removed
     * @return -1 if word does not exist, 0 if it was a native word, else 1.
     */
    int removeWord(String word){
        if(words.remove(word)){
            return 0;
        }
        if(foreignWords.remove(word)){
            return 1;
        }
        return -1;
    }

    /**
     * Add a native word to the vocable. Fails if the word is already in the vocable.
     * @param word Word to be added.
     * @return True if the process was successful.
     */
    boolean addWord(String word){
        if(!words.contains(word)){
            words.add(word);
            Changed = true;
            return true;
        }
        return false;
    }

    /**
     * Add a list of native words to the vocable. Successful if at least one word is not already in the vocable.
     * @param words List of words to be added.
     * @return True if at least on word is successfully added.
     */
    boolean addWords(List<String> words){
        boolean successful = false;
        for(String word : words){
            if(addWord(word)){
                successful = true;
            }
        }
        return successful;
    }

    /**
     * Get a random native word from the vocable.
     * @return The chosen word.
     */
    String getNewWord(){
        lastWord = words.get(rng.nextInt(words.size()));
        return lastWord;
    }

    /**
     * Get the first native word of the vocable.
     * @return The chosen word.
     */
    String getFirstWord(){
        lastWord = words.get(0);
        return lastWord;
    }

    /**
     * Get the last native word of the vocable.
     * @return The chosen word.
     */
    String getLastWord(){
        return lastWord;
    }

    /**
     * Get a native word from the vocable.
     * @param i Index of the word in the internal list.
     * @return The chosen word.
     */
    String getWord(int i){
        return words.get(i);
    }

    /**
     * Get all native words of the vocable.
     * @return List of native words.
     */
    List<String> getWords(){
        return words;
    }

    /**
     * Add a foreign word to the vocable. Fails if the word is already in the vocable.
     * @param word Foreign word to be added.
     * @return True if the process was successful.
     */
    boolean addForeignWord(String word){
        if(!foreignWords.contains(word)){
            foreignWords.add(word);
            Changed = true;
            return true;
        }
        return false;
    }

    /**
     * Add a list of foreign words to the vocable. Successful if at least one word is not already in the vocable.
     * @param foreignWords List of words to be added.
     * @return True if at least on word is successfully added.
     */
    boolean addForeignWords(List<String> foreignWords){
        boolean successful = false;
        for(String foreignWord : foreignWords){
            if(addForeignWord(foreignWord)){
                successful = true;
            }
        }
        return successful;
    }

    /**
     * Get a random foreign word from the vocable.
     * @return The chosen word.
     */
    String getNewForeignWord() {
        lastForeignWord = foreignWords.get(rng.nextInt(foreignWords.size()));
        return lastForeignWord;
    }

    /**
     * Get a random foreign word from the vocable that fulfills the kanji requirement.
     * @param prefKanji Specifies if the word should contain kanji symbols or not.
     * @return The chosen word.
     */
    String getNewForeignWord(Utils.PreferKanji prefKanji){
        if(prefKanji.equals(Utils.PreferKanji.yes) || prefKanji.equals(Utils.PreferKanji.force)){
            List<String> kanjiWords = new LinkedList<>();
            for(String word : foreignWords)
                if(Utils.containsKanji(word))
                    kanjiWords.add(word);
            if(!kanjiWords.isEmpty()){
                lastForeignWord = kanjiWords.get(rng.nextInt(kanjiWords.size()));
                return lastForeignWord;
            }
        }
        return getNewForeignWord();
    }

    /**
     * Get the last foreign word of the vocable.
     * @return The chosen word.
     */
    String getLastForeignWord(){
        return lastForeignWord;
    }

    /**
     * Get a foreign word from the vocable.
     * @param i Index of the word in the internal list.
     * @return The chosen word.
     */
    String getForeignWord(int i){
        return foreignWords.get(i);
    }

    /**
     * Get all foreign words of the vocable.
     * @return List of foreign words.
     */
    List<String> getForeignWords(){
        return foreignWords;
    }

    int getLearnedCount(){
        return learnedCount;
    }
    int getCorrectCount(){
        return correctCount;
    }
    long getCreationTime() {
        return creationTime;
    }

    /**
     * Calculates the ratio of correctCount/learnedCount and returns it.
     * @return Calculated accuracy ratio.
     */
    double getAccuracy(){
        if(learnedCount == 0){
            return 1;
        }
        return (double)correctCount/(double)learnedCount;
    }

    /**
     * Increments learnedCount and, if applicable, increments correctCount.
     * @param learnedCorrectly Specifies if the answer to the question concerning this vocable was correct.
     */
    void learned(boolean learnedCorrectly){
        if(learnedCorrectly){
            correctCount++;
        }
        learnedCount++;
    }

    /**
     * Change a native word of the vocable.
     * @param oldWord Word to be changed.
     * @param newWord New word.
     * @return True if the process is successful.
     */
    boolean changeWord(String oldWord, String newWord){
        return change(words, oldWord, newWord);
    }

    /**
     * Change a foreign word of the vocable.
     * @param oldForeignWord Word to be changed.
     * @param newForeignWord New word.
     * @return True if the process is successful.
     */
    boolean changeForeignWord(String oldForeignWord, String newForeignWord){
        return change(foreignWords, oldForeignWord, newForeignWord);
    }

    /**
     * Change a word of the vocable. The new word must not already be in the vocable.
     * @param vocabs List that contains the vocable to be changed, e.g. words or foreignWords attribute of the vocable.
     * @param oldVocab Word to be changed.
     * @param newVocab New word.
     * @return True if vocable contains old word and new word is unique.
     */
    private boolean change(List<String> vocabs, String oldVocab, String newVocab){
        if(foreignWords.contains(newVocab) || words.contains(newVocab)){
            return false;
        }
        int index = vocabs.indexOf(oldVocab);
        if(index >= 0){
            vocabs.set(index, newVocab);
            Changed = true;
            return true;
        }
        return false;
    }

    /**
     * Comparator to sort vocables by age.
     * @return  1 if v1 is older then v2,
     *          0 if v1 and v2 are of the same age,
     *          -1 else.
     */
    static Comparator<Vocable> getAgeComparator() {
        return new Comparator<Vocable>() {
            public int compare(Vocable v1, Vocable v2){
                return Long.compare(v2.creationTime,v1.creationTime);
            }
        };
    }

    /**
     * Comparator to sort vocables by learning accuracy.
     * @return  1 if accuracy of v1 is greater then accuracy of v2,
     *          0 if v1 and v2 have the same accuracy,
     *          -1 else.
     */
    static Comparator<Vocable> getCorrectnessComparator() {
        return new Comparator<Vocable>() {
            public int compare(Vocable v1, Vocable v2){
                return Double.compare(v1.getAccuracy(),v2.getAccuracy());
            }
        };
    }

    /**
     * Comparator to sort vocables by increasing order of number of queries.
     * @return  1 if v1 has been queried fewer then v2.
     *          0 if v1 and v2 have been queried the same amount of times,
     *          -1 else.
     */
    static Comparator<Vocable> getLeastLearnedComparator() {
        return new Comparator<Vocable>() {
            public int compare(Vocable v1, Vocable v2){
                return Integer.compare(v2.getLearnedCount(),v1.getLearnedCount());
            }
        };
    }

    /**
     * Comparator to sort vocables by alphabetic order.
     * @return  1 if v1 has a higher alphabetic order then v2.
     *          0 if v1 and v2 have the same alphabetic order.
     *          -1 else.
     */
    static Comparator<Vocable> getAlphabeticComparator() {
        return new Comparator<Vocable>() {
            public int compare(Vocable v1, Vocable v2){
                return v1.getFirstWord().compareTo(v2.getFirstWord());
            }
        };
    }

    /**
     * Creates a string that represents the vocable, in the form "word1, ... -> translation1, ..."
     * @return String of the vocable.
     */
    public String toString(){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i + 1< words.size(); i++){
            builder.append(words.get(i));
            builder.append(", ");
        }
        builder.append(words.get(words.size() - 1));
        builder.append(" -> ");
        for(int i = 0; i + 1< foreignWords.size(); i++){
            builder.append(foreignWords.get(i));
            builder.append(", ");
        }
        builder.append(foreignWords.get(foreignWords.size() - 1));
        return builder.toString();
    }
}
