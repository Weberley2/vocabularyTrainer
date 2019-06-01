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

    /**
     * Saves the vocabulary to disk.
     * @throws IOException if a problem occurs while writing the file (e.g. missing privileges).
     */
    void saveVocabulary() throws IOException{
        Database.setVocabulary(vocabulary);
        Database.setVocabularyReverse(vocabularyReverse);
        Database.saveVocabulary();
    }

    /**
     * Checks if the vocabulary was changed during the runtime of the program. This is also the case if the vocables
     * have been queried by the user, since this changes their stats (times asked, times correct, etc.)
     * @throws IOException if a problem occurs while writing the file (e.g. missing privileges).
     */
    void saveVocabularyIfChanged() throws IOException{
        if(vocabularyChanged){
            saveVocabulary();
        }
    }

    /**
     * Takes a list of words that may make up a future vocable, and checks if they are pairwise distinct.
     * @param wordLists List of words.
     * @return True if the words are pairwise distinct.
     */
    private boolean isUnique(Collection<String> ... wordLists){
        int expectedSize = 0;
        Set<String> unionSet = new HashSet<>();
        for(Collection<String> wordList : wordLists){
            expectedSize += wordList.size();
            unionSet.addAll(wordList);
        }
        return unionSet.size() == expectedSize;
    }

    /**
     * Adds a vocable to the vocabulary. Only allows valid vocables to be added. If multiple ways to add the vocable
     * exist, the user is asked how to add the vocable exactly.
     * @param words List of native words that make up the potential new vocable.
     * @param foreignWords List of foreign words that make up the potential new vocable.
     * @return True if a new vocable is added or an existing vocable is extended.
     */
    boolean addVocable(String words, String foreignWords){
        Set<Vocable> wordVocables = new HashSet<>();
        Set<Vocable> foreignWordVocables = new HashSet<>();
        String[] wordSplit = words.split(",");
        String[] foreignWordSplit = foreignWords.split(",");
        if(!isUnique(Arrays.asList(wordSplit), Arrays.asList(foreignWordSplit))){
            Utils.write("All words need to be unique");
            return false;
        }
        for(String word : wordSplit){
            if(vocabulary.containsKey(word)){
                wordVocables.addAll(vocabulary.get(word));
            }
        }
        for(String foreignWord : foreignWordSplit){
            if(vocabularyReverse.containsKey(foreignWord)){
                foreignWordVocables.addAll(vocabularyReverse.get(foreignWord));
            }
        }

        if(!(wordVocables.isEmpty() && foreignWordVocables.isEmpty())){
            //check if vocable already exists
            Set<Vocable> crossSection = new HashSet<>(wordVocables);
            crossSection.retainAll(foreignWordVocables);
            for(Vocable vocable : crossSection){
                boolean containsAll = true;
                for(String word : wordSplit){
                    if(!vocable.getWords().contains(word)){
                        containsAll = false;
                        break;
                    }
                }
                if(!containsAll){
                    continue;
                }
                for(String foreignWord : foreignWordSplit){
                    if(!vocable.getForeignWords().contains(foreignWord)){
                        containsAll = false;
                        break;
                    }
                }
                if(containsAll){
                    Utils.write("\"" + vocable.toString() + "\" is already in the vocabulary.");
                    return false;
                }
            }
            //vocable does not already exist
            //check if an existing vocable should be changed
            wordVocables.addAll(foreignWordVocables);
            List<Vocable> allVocableList = new LinkedList<>(wordVocables);
            String vocableDescriptor = Utils.listToString(Arrays.asList(wordSplit)) + " -> " +
                    Utils.listToString(Arrays.asList(foreignWordSplit));
            int index = makeDecision(allVocableList, "Would you like to add \"" + vocableDescriptor + "\" to an existing vocable?", "No");
            if(index < -1){
                return false;
            }
            if(index >= 0){
                //change variable

                Vocable changeVocable = allVocableList.get(index);

                Set<String> potentialWords = new HashSet<>(Arrays.asList(wordSplit));
                potentialWords.addAll(changeVocable.getWords());
                Set<String> potentialForeignWords = new HashSet<>(Arrays.asList(foreignWordSplit));
                potentialForeignWords.addAll(changeVocable.getForeignWords());
                if(!isUnique(potentialWords, potentialForeignWords)){
                    Utils.write("All words need to be unique");
                    return false;
                }

                for(String word : wordSplit){
                    List<Vocable> vocableList = getVocabularyList(word, vocabulary);
                    if(!vocableList.contains(changeVocable)){
                        vocableList.add(changeVocable);
                    }
                }
                for(String foreignWord : foreignWordSplit){
                    List<Vocable> foreignVocableList = getVocabularyList(foreignWord, vocabularyReverse);
                    if(!foreignVocableList.contains(changeVocable)){
                        foreignVocableList.add(changeVocable);
                    }
                }
                boolean successful = changeVocable.addForeignWords(new ArrayList<>(Arrays.asList(foreignWordSplit)));
                successful = changeVocable.addWords(new ArrayList<>(Arrays.asList(wordSplit))) || successful;
                if(successful){
                    vocabularyChanged = true;
                    Utils.write("Successfully added to \"" + changeVocable.toString() + "\".");
                }
                else {
                    Utils.write("Could not add to \"" + changeVocable.toString() + "\".");
                }
                return successful;
            }
        }
        Vocable addVocable = new Vocable(new ArrayList<>(Arrays.asList(wordSplit)), new ArrayList<>(Arrays.asList(foreignWordSplit)));
        for(String word : wordSplit){
            List<Vocable> vocableList = getVocabularyList(word, vocabulary);
            vocableList.add(addVocable);
        }
        for(String foreignWord : foreignWordSplit){
            List<Vocable> vocableList = getVocabularyList(foreignWord, vocabularyReverse);
            vocableList.add(addVocable);
        }
        Utils.write("Added \"" + addVocable.toString() + "\" to the vocabulary.");
        vocabularyChanged = true;
        return true;
    }

    /**
     * Outputs a question and several answer options (all but the first one consisting of vocables) to the user, then
     * returns the index of the chosen vocable, '-1' if the default option was chosen or '-2' if the answer could not
     * be parsed.
     * @param vocables Vocables to be chosen from.
     * @param question Question to be asked to the user.
     * @param defaultOptionName First answer option to be displayed.
     * @return Integer representing the users answer.
     */
    private int makeDecision(List<?> vocables, String question, String defaultOptionName){
        StringBuilder builder = new StringBuilder();
        builder.append(question);
        builder.append(System.lineSeparator());
        builder.append(">\t");
        builder.append(defaultOptionName);
        builder.append(" ()");
        builder.append(System.lineSeparator());
        for(int i = 0; i < vocables.size(); i++){
            builder.append(">\t");
            builder.append(vocables.get(i).toString());
            builder.append(" (");
            builder.append(i);
            builder.append(")");
            if(i + 1< vocables.size()){
                builder.append(System.lineSeparator());
            }
        }
        Utils.writeLastOutput(builder.toString());
        int input;
        try {
            String inputString = Utils.read();
            if(inputString.isEmpty()){
                return -1;
            }
            input = Integer.valueOf(inputString);
            if(input < 0 || input >= vocables.size()){
                throw new NumberFormatException();
            }
        }
        catch (Exception e){
            Utils.write("Could not parse input.");
            return -2;
        }
        return input;
    }

    /**
     * Get a list of all vocables that contain a specific word.
     * @param word Word to be searched for.
     * @param source Vocable map to be searched in (e.g. native or foreign vocabulary.)
     * @return List of vocables containing the word.
     */
    private List<Vocable> getVocabularyList(String word, Map<String, List<Vocable>> source){
        List<Vocable> vocableList = source.get(word);
        if(vocableList == null){
            vocableList = new LinkedList<>();
            source.put(word, vocableList);
        }
        return vocableList;
    }

    /**
     * Remove a vocable from the vocabulary identified by a word. If the word does not uniquely identify the vocable,
     * the user is asked to choose from a list of possible matches. The user is then asked if the complete vocable or
     * only the given word should be removed from the vocabulary.
     * @param word Word to be removed.
     * @return True if a vocable has been deleted or reduced.
     */
    boolean removeVocable(String word){
        List<Vocable> toRemove = null;
        if(vocabulary.containsKey(word)){
            toRemove = vocabulary.get(word);
        }
        else if(vocabularyReverse.containsKey(word)){
            toRemove = vocabularyReverse.get(word);
        }
        if(toRemove == null || toRemove.isEmpty()){
            Utils.write("\"" + word + "\" is not in the vocabulary.");
            return false;
        }
        int index = 0;
        if(toRemove.size() > 1){
            index = makeDecision(toRemove, "Which vocable would you like to remove?", "None");
            if(index < 0){
                if(index == -1){
                    Utils.write("\"" + word + "\" was not removed.");
                }
                return false;
            }
        }
        Vocable removeVocable = toRemove.get(index);
        int decision = -1;
        if(!(removeVocable.getForeignWords().contains(word) && removeVocable.getForeignWords().size() == 1) &&
                !(removeVocable.getWords().contains(word) && removeVocable.getWords().size() == 1)){
            List<String> proxyList = new ArrayList<>();
            proxyList.add("Remove \"" + word + "\" from the vocable");
            decision = makeDecision(proxyList, "What would you like to do to \""+ removeVocable.toString() + "\"?",
                    "Remove the complete vocable");
        }
        if(decision == -1){
            for(String removeWord : removeVocable.getWords()){
                vocabulary.get(removeWord).remove(removeVocable);
            }
            for(String removeForeignWord : removeVocable.getForeignWords()){
                vocabularyReverse.get(removeForeignWord).remove(removeVocable);
            }
            Utils.write("Successfully removed \"" + removeVocable.toString()  +"\".");
            vocabularyChanged = true;
            return true;
        }
        if(decision == 0){
            Vocable testVocable = Vocable.copy(removeVocable);
            if(testVocable.removeWord(word) != -1 && containsVocable(testVocable)){
                Utils.write("\"" + testVocable.toString() + "\" is already in the vocabulary.");
                return false;
            }
            int result = removeVocable.removeWord(word);
            if(result == - 1){
                return false;
            }
            if(result == 0){
                vocabulary.get(word).remove(removeVocable);
            }
            else {
                vocabularyReverse.get(word).remove(removeVocable);
            }
            Utils.write("Successfully removed \"" + word  +"\" from \"" + removeVocable.toString() + "\".");
            vocabularyChanged = true;
            return true;
        }
        return false;

    }

    /**
     * Change a word contained in a vocable. If the word does not uniquely identify the vocable, the user is asked
     * to choose from a list of matches. Prevents changes that would lead to an invalid state of the vocabulary (e.g.
     * vocables with non-unique words, multiple identical vocables, etc.).
     * @param word Word to be changed
     * @param newWord Word to be changed into.
     * @return True if a vocable was successfully changed.
     */
    boolean changeVocable(String word, String newWord){
        Set<Vocable> changeableSet = new HashSet<>();
        List<Vocable> vocableList = new LinkedList<>();
        if(vocabulary.containsKey(word)){
            vocableList.addAll(vocabulary.get(word));
            changeableSet.addAll(vocabulary.get(word));
        }
        else if(vocabularyReverse.containsKey(word)){
            changeableSet .addAll(vocabularyReverse.get(word));
        }
        if(changeableSet.isEmpty()){
            Utils.write("\"" + word + "\" is not in the vocabulary.");
            return false;
        }
        int index = 0;
        List<Vocable> changableList = new LinkedList<>(changeableSet);
        if(changeableSet.size() > 1){
            index = makeDecision(changableList, "Which vocable would you like to change?", "None");
            if(index < 0){
                if(index == -1){
                    Utils.write("\"" + word + "\" was not changed.");
                }
                return false;
            }
        }

        Vocable changeVocable = changableList.get(index);
        boolean changeVocabulary = false;
        if(vocableList.contains(changeVocable)){
            changeVocabulary = true;
        }
        HashMap<String, List<Vocable>> vocabularyToChange;
        Vocable proxy = Vocable.copy(changeVocable);
        boolean changeResult;
        if(changeVocabulary){
            vocabularyToChange = vocabulary;
            changeResult = proxy.changeWord(word, newWord);

        }
        else {
            vocabularyToChange = vocabularyReverse;
            changeResult = proxy.changeForeignWord(word, newWord);
        }
        String changedVocableString = proxy.toString();
        if(!changeResult){
            Utils.write("\"" + changedVocableString + "\" could not be changed.");
            return false;
        }
        if(containsVocable(proxy)){
            Utils.write("\"" + changedVocableString + "\" is already in the vocabulary.");
            return false;
        }
        if(changeVocabulary){
            changeVocable.changeWord(word, newWord);
        }
        else {
            changeVocable.changeForeignWord(word, newWord);
        }
        vocabularyToChange.get(word).remove(changeVocable);
        List<Vocable> newList = vocabularyToChange.get(newWord);
        if(newList == null){
            newList = new LinkedList<>();
            vocabularyToChange.put(newWord, newList);
        }
        newList.add(changeVocable);
        Utils.write("Successfully changed \"" + changeVocable.toString() + "\".");
        vocabularyChanged = true;
        return true;
    }
}
