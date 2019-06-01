package ta.vocable_trainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    static LearningMethod standardLearningMethod = LearningMethod.randomWords;
    static int standardNumberOfWords = 10;

    // When a new settings file is automatically created, this filename is used for the vocab file.
    static String fallbackVocabFileName = "vocabs.txt";

    // Contains all mandatory settings that need to be in the settings file.
    static String[] settings = {"standardNumberOfWords", "standardLearningMethod", "vocabFileName", "prefKanji",
            "addFileDelimiter", "addFileStandardOrder", "prefLanguage"};

    // When a new settings file is automatically created, the following two values are used as default values.
    static int fallbackStandardNumberOfWords = 10;
    static String fallbackStandardLearningMethod = "r";

    // Filenames for the backup files during saving.
    static String vocabNewBackupName = "vocabsBackupNew.txt";
    static String vocabOldBackupName = "vocabsBackupOld.txt";

    // Directory in which files are saved.
    static String parentDirectory = "Vocabulary Trainer";

    // Standard vocab file name.
    static String vocabFileName = "vocabs.txt";

    static PreferKanji prefKanji;

    // Determines whether vocabs should be asked in native form, foreign form or both.
    // -1 -> present vocables in their native form
    // 0 -> dont care
    // 1 -> present vocables in their foreign form
    static int prefLanguage = 0;

    // Strings to output to the user.
    static final String prefLanguageNative = "native";
    static final String prefLanguageNone = "none";
    static final String prefLanguageForeign = "foreign";

    // When the 'vocabFilePath' is changed during the execution of the program, the old value is stored here, so the
    // backup process still works
    static Path oldVocabFilePath;

    // Values that determine the format of add files (Used to add multiple vocables at once).
    // By default, the format is:
    // foreign, words - native, words
    static String addFileDelimiter = "-";
    static boolean addFileStandardOrder = true;

    // Used to connect to a server to backup the vocable data (TODO)
    static String uploadAddress = "";
    static String uploadPort = "";

    static Path settingsFilePath = Paths.get(System.getProperty("user.home"),parentDirectory, "settings.txt");
    static Path vocabFilePath = Paths.get(System.getProperty("user.home"), parentDirectory, vocabFileName);

    // Files that do not begin with this String will be ignored by the program while searching for suitable
    // vocable files.
    static String vocabFileIdentifier = "[Vocabulary Trainer File]";

    // Separates information in the vocabulary file.
    static String delimiter = ";";

    // Reads user input.
    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    // All symbols that can be entered by the user. Contains the latin alphabet, numbers 0 to 9, kanji, hiragana,
    // katakana and some japanese and western punctuation characters.
    private static final String regex = "[\\u3040-\\u30ff\\u3400-\\u4dbf\\u4e00-\\u9fff\\uf900-\\ufaff\\uff66-\\uff9fa-zA-Z0-9äöüß \\-,;\"０-９()「」\\.]";
    private static final Pattern allowedInputSymbols = Pattern.compile(regex, Pattern.MULTILINE);

    /**
     * Checks if a string kontains kanji symbols.
     * @param text String to be checked.
     * @return True if text contains kanji symbols, else false.
     */
    static boolean containsKanji(String text){
        for(char c : text.toCharArray()) {
            if(Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS){
                return true;
            }
        }
        return false;
    }

    /**
     * check for a variety of japanese commas and whitespace and transform it into standard commas and whitespace.
     * @param input Input to be sanitized.
     * @return Sanitized input.
     */
    private static String correctWhiteSpace(String input){
        return input.replace("　", " ").trim().replaceAll(" +", " ").replace("、",",")
                .replace(" ,", ",").replace(", ",",");
    }

    /**
     * Takes a generic list and returns a sting of the form 'listElem1, listElem2, ...'.
     * @param list List to be turned into a string.
     * @return String representing the list.
     */
    static String listToString(List<?> list){
        if(list == null){
            return "";
        }
        String result = "";
        for(int i = 0; i + 1 < list.size(); i++){
            result += list.get(i).toString() + ", ";
        }
        if(!list.isEmpty()){
            result += list.get(list.size() - 1);
        }
        return result;
    }

    static boolean parseBoolean(String boolString){
        boolString = boolString.toLowerCase();
        if(boolString.equals("true") || boolString.equals("t"))
            return true;
        if(boolString.equals("false") || boolString.equals("f"))
            return false;
        throw new IllegalArgumentException("Cant parse bool.");
    }

    static LearningMethod parseLearningMethod(String string){
        LearningMethod method = LearningMethod.randomWords;
        switch (string){
            case "r":
            case "random":
            case "randomWords":
                method = LearningMethod.randomWords;
                break;
            case "n":
            case "new":
            case "newWords":
                method = LearningMethod.newWords;
                break;
            case "b":
            case "bad":
            case "badWords":
                method = LearningMethod.badWords;
                break;
            case "l":
            case "least":
            case "leastLearnedWords":
                method = LearningMethod.leastLearnedWords;
                break;
        }
        return method;
    }

    static int parsePrefLanguage(String value) throws IllegalArgumentException{
        if(prefLanguageNative.startsWith(value)){
            return -1;
        }
        else if(prefLanguageNone.startsWith(value) || "equal".startsWith(value)){
            return  0;
        }
        else if(prefLanguageForeign.startsWith(value)){
            return  1;
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    static String parsePrefLanguage(int value) throws IllegalArgumentException{
        if(value == -1)
            return prefLanguageNative;
        if(value == 0)
            return prefLanguageNone;
        if(value == 1)
            return prefLanguageForeign;
        throw new IllegalArgumentException();
    }

    /**
     * Read user input, sanitize it and check for illegal characters.
     * @return Sanitized user input.
     * @throws IOException If the user input cannot be read from stdin.
     */
    static String read() throws IOException{
        String correctedWhitepace = correctWhiteSpace(br.readLine());
        Matcher matcher = allowedInputSymbols.matcher(correctedWhitepace);
        String skipped = correctedWhitepace;
        while (matcher.find()) {
            String character = matcher.group(0);
            skipped = skipped.replaceFirst(Pattern.quote(character), "");
        }
        if(skipped.length() > 0){
            String skippedMessage= "Illegal characters found: ";
            for (int i = 0; i < skipped.length(); i++) {
                correctedWhitepace = correctedWhitepace.replaceAll(Pattern.quote(String.valueOf(skipped.charAt(i))), "");
                skippedMessage += "\"" + skipped.charAt(i) + "\", ";
            }
            skippedMessage = skippedMessage.substring(0, skippedMessage.length() - 2);
            skippedMessage += ". Used: " + correctedWhitepace;
            write(skippedMessage);
        }
        return correctedWhitepace;
    }

    /**
     * Sets the path of the file used to store vocables before the file was changed.
     * @param fileName Absolute path to the old vocable file.
     */
    static void setOldVocabFilePath(String fileName){
        oldVocabFilePath = Paths.get(System.getProperty("user.home"), parentDirectory, fileName);
        setVocabFilePath(fileName);
    }

    /**
     * Sets the path of the file used to store vocables.
     * @param fileName Absolute path to the new vocable file.
     */
    static void setVocabFilePath(String fileName){
        vocabFileName = fileName;
        vocabFilePath = Paths.get(System.getProperty("user.home"), parentDirectory, fileName);
    }

    /**
     * Write the specified string to stdout with a leading '>'.
     * @param string String to print.
     */
    static void write(String string){
        System.out.println("> " + string);
    }

    /**
     * Write the specified string to stdout and start a new line, both with a leading '>'.
     * @param string String to print.
     */
    static void writeLastOutput(String string){
        System.out.println("> " + string);
        System.out.print("> ");
    }

    /**
     * Implements a way to prefer/avoid vocables that have (even partial) translations in kanji form.
     */
    enum PreferKanji {
        /**
         * If possible, use vocables that dont have kanji translations.
         */
        no,

        /**
         * If possible, use vocables that have kanji translations.
         */
        yes,

        /**
         * Only use vocables that have kanji translations.
         */
        force;

        static PreferKanji parse(String value){
            value = value.toLowerCase();
            if ("yes".startsWith(value)){
                return PreferKanji.yes;
            }
            if ("force".startsWith(value)){
                return PreferKanji.force;
            }
            return PreferKanji.no;
        }

        static String parse(PreferKanji pf){
            if(pf.equals(PreferKanji.no)){
                return "no";
            }
            if(pf.equals(PreferKanji.yes)){
                return "yes";
            }
            return "force";
        }

    }
}

/**
 * Learning Methods are used to decide which vocables should be presented to the user.
 */
enum LearningMethod{
    /**
     * Recently added or changed vocabs. Determined by unix timestamp of vocable.
     */
    newWords,

    /**
     * Vocables with the worst ratio of: (correctly answered)/(asked)
     */
    badWords,

    /**
     * Vocables that have been been asked the fewest times.
     */
    leastLearnedWords,

    /**
     * Random sampling of vocables.
     */
    randomWords
}
