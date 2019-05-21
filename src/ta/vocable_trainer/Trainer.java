package ta.vocable_trainer;

import java.io.IOException;
import java.util.*;

/**
 * This class acts as a layer between the Database and the user interface. It holds the vocabulary loaded from the
 * Database and chooses the vocables to be be trained according to the requested learning method.
 */
public class Trainer {

    // Holds the vocabulary indexed by native words.
    private HashMap<String, List<Vocable>> vocabulary;

    // Holds the vocabulary indexed by foreign words.
    private HashMap<String, List<Vocable>> vocabularyReverse;

    // True if the vocabulary has been changed during the execution of the program.
    boolean vocabularyChanged;

    /**
     * Constructor. Loads the vocabulary.
     * @throws IOException
     */
    Trainer() throws IOException {
        Database.loadVocabulary();
        vocabulary = Database.getVocabulary();
        vocabularyReverse = Database.getVocabularyReverse();
        vocabularyChanged = false;
    }

    public boolean isVocabularyChanged(){
        return vocabularyChanged;
    }

    /**
     * Check if a word (native or foreign) is in the vocabulary.
     * @param word Word to be checked.
     * @return True if the word is in the vocabulary.
     */
    boolean containsWord(String word){
        return vocabulary.containsKey(word) || vocabularyReverse.containsKey(word);
    }

    /**
     * Checks if a vocable is in the vocabulary. Non-word related attributes like time of creation are neglected.
     * Returns true if a vocable with the same exact native and foreign words exists in the vocabulary.
     * @param vocable Vocable to be checked.
     * @return True if an equivalent vocable exists.
     */
    boolean containsVocable(Vocable vocable){
        Set<Vocable> wordSet = new HashSet<>();
        Set<Vocable> foreignWordSet = new HashSet<>();
        for(String word : vocable.getWords()){
            if(vocabulary.get(word) != null){
                wordSet.addAll(vocabulary.get(word));
            }
        }
        for(String foreignWord : vocable.getForeignWords()){
            if(vocabularyReverse.get(foreignWord) != null){
                foreignWordSet.addAll(vocabularyReverse.get(foreignWord));
            }
        }
        wordSet.retainAll(foreignWordSet);
        for(Vocable checkVocable : wordSet){
            if(checkVocable.getWords().containsAll(vocable.getWords())
                    && checkVocable.getForeignWords().containsAll(vocable.getForeignWords())
                    && vocable.getWords().containsAll(checkVocable.getWords())
                    && vocable.getForeignWords().containsAll(checkVocable.getForeignWords())){
                return true;
            }
        }
        return false;
    }

    /**
     * Get all existing vocables.
     * @return List of all existing vocables.
     */
    List<Vocable> getVocables(){
        Set<Vocable> vocabSet = new HashSet<>();
        for(List<Vocable>  vocableList :vocabulary.values()){
            vocabSet.addAll(vocableList);
        }
        return new ArrayList<>(vocabSet);
    }

    /**
     * Takes a vocable and chooses a (native or foreign) word. Then it is checked, if the chosen word uniquely
     * identifies the vocable in the vocabulary. If that is not the case, words in the same language are taken from the
     * vocable until the resulting list of words either uniquely identify the vocable or contains all words of the
     * chosen language contained in the vocable. Returns that list.
     * @param vocable Vocable to choose words from.
     * @param foreignWord True if foreign words should be chosen.
     * @param prefKanji Specifies if words containing kanji should be preferred (if possible).
     * @return List of words that uniquely identify the vocable (if that is possible).
     */
    List<String> getUniqueWordList(Vocable vocable, boolean foreignWord, Utils.PreferKanji prefKanji){
        String word = vocable.getNewWord();
        if(foreignWord){
            word = vocable.getNewForeignWord(prefKanji);
        }
        List<String> result = new LinkedList<>();
        result.add(word);
        List<Vocable> vocableList = vocabulary.get(word);
        if(vocableList == null){
            vocableList = vocabularyReverse.get(word);
        }
        if(vocableList == null || vocableList.size() <= 1){
            return result;
        }

        List<Boolean> checkList = new LinkedList<>();
        for(int i = 0; i + 1< vocableList.size(); i++){
            checkList.add(false);
        }
        int index = 0;
        while (checkList.contains(false)){
            String potentialUniqueWord;
            if(!foreignWord){
                if(index >= vocable.getWords().size()){
                    break;
                }
                potentialUniqueWord = vocable.getWord(index);
            }
            else {
                if(index >= vocable.getForeignWords().size()){
                    break;
                }
                potentialUniqueWord = vocable.getForeignWord(index);
            }
            int identity = 0;
            for(int j = 0; j < vocableList.size(); j++){
                Vocable checkVocable = vocableList.get(j);
                if(checkVocable.equals(vocable)){
                    identity = 1;
                    continue;
                }
                if(!((!foreignWord && checkVocable.getWords().contains(potentialUniqueWord)) ||
                        (foreignWord && checkVocable.getForeignWords().contains(potentialUniqueWord)))){
                    if(!checkList.get(j - identity) && !result.contains(potentialUniqueWord)){
                        result.add(potentialUniqueWord);
                        checkList.set(j - identity, true);
                    }
                }
            }
            index++;
        }
        return result;
    }

    boolean hasVocables(){
        return !vocabulary.isEmpty();
    }

    /**
     * Searches the vocabulary for vocables that contain at least one of the provided strings.
     * @param toSearch List of strings to search for.
     * @return List of vocables that contain at least one of the provided strings.
     */
    List<Vocable> searchVocabulary(String... toSearch){
        Set<Vocable> results = new HashSet<>();
        Set s = vocabulary.keySet();
        Iterator iter = s.iterator();
        while (iter.hasNext()){
            String key = (String)iter.next();
            List<Vocable> vocableList = vocabulary.get(key);
            for(Vocable vocable : vocableList){
                for(String searchTerm : toSearch){
                    for(String word : vocable.getWords()){
                        if(word.contains(searchTerm)){
                            results.add(vocable);
                            break;
                        }
                    }
                    for(String foreignWord : vocable.getForeignWords()){
                        if(foreignWord.contains(searchTerm)){
                            results.add(vocable);
                            break;
                        }
                    }
                }
            }
        }
        List<Vocable> resultList = new LinkedList<>(results);
        resultList.sort(Vocable.getAlphabeticComparator());
        return resultList;
    }
}
