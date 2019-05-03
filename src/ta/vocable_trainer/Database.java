package ta.vocable_trainer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * The Database class implements basic persistence functionalities. In order to keep dependencies and complexity at a
 * minimum, no actual databases are used. Instead, data is simply stored in text files.
 */
public class Database {
    // holds vocables indexed by their native words
    private static HashMap<String, List<Vocable>> vocabulary = new HashMap<>();
    // holds vocables indexed by their foreign words
    private static HashMap<String, List<Vocable>> vocabularyReverse = new HashMap<>();
    private static Set<Vocable> removedVocabulary = new HashSet<>();

    static HashMap<String, List<Vocable>> getVocabulary(){
        return vocabulary;
    }
    static HashMap<String, List<Vocable>> getVocabularyReverse(){
        return vocabularyReverse;
    }
    static void setVocabulary(HashMap<String, List<Vocable>> vocabulary){
        Database.vocabulary = vocabulary;
    }

    static void setVocabularyReverse(HashMap<String, List<Vocable>> vocabularyReverse) {
        Database.vocabularyReverse = vocabularyReverse;
    }

    /**
     * Loads the settings data from the specified file path. If the settings file does not exist, it is created with
     * default values.If any values are missing, an exception is thrown that specifies the missing settings.
     * @throws IOException if the settings file is not in a correct format or the setting directory cannot be created.
     */
    static void loadSettings() throws IOException {
        File settingsFile = Utils.settingsFilePath.toFile();
        if(!settingsFile.exists()){
            File parentDirectory = Paths.get(System.getProperty("user.home"), Utils.parentDirectory).toFile();
            if(!parentDirectory.exists() && !parentDirectory.mkdir()){
                throw new IOException("Cannot create settings directory.");
            }
            String settingsContent = "standardNumberOfWords=" + Utils.fallbackStandardNumberOfWords + System.lineSeparator() +
                    "standardLearningMethod=" + Utils.fallbackStandardLearningMethod + System.lineSeparator() +
                    "vocabFileName=" + Utils.fallbackVocabFileName + System.lineSeparator() +
                    "prefKanji=false" + System.lineSeparator() +
                    "prefLanguage=none" + System.lineSeparator() +
                    "addFileDelimiter=-" + System.lineSeparator() +
                    "addFileStandardOrder=true" + System.lineSeparator() +
                    "uploadAddress=" + System.lineSeparator() +
                    "uploadPort=";
            writeToFile(settingsFile.getAbsolutePath(), settingsContent);
        }
        else {
            List<String> settingsLines = Files.readAllLines(settingsFile.toPath());
            List<String> settings = new ArrayList<>(Arrays.asList(Utils.settings));
            for(String line : settingsLines){
                String[] values = line.split("=");
                if(values.length != 2){
                    if(values[0].equals("uploadAddress") || values[0].equals("uploadPort")){
                        continue;
                    }
                    throw new IOException("Cant parse the settings file.");
                }
                switch (values[0]){
                    case "standardNumberOfWords":
                        settings.remove(values[0]);
                        Utils.standardNumberOfWords = Integer.parseInt(values[1]);
                        break;
                    case "uploadAddress":
                        Utils.uploadAddress = values[1];
                        break;
                    case "uploadPort":
                        Utils.uploadPort = values[1];
                        break;
                    case "standardLearningMethod":
                        settings.remove(values[0]);
                        Utils.standardLearningMethod = Utils.parseLearningMethod(values[1]);
                        break;
                    case "vocabFileName":
                        settings.remove(values[0]);
                        Utils.setOldVocabFilePath(values[1]);
                        break;
                    case "prefKanji":
                        settings.remove(values[0]);

                        Utils.prefKanji = Utils.PreferKanji.parse(values[1]);
                        break;
                    case "addFileDelimiter":
                        settings.remove(values[0]);
                        Utils.addFileDelimiter = values[1];
                        break;
                    case "addFileStandardOrder":
                        settings.remove(values[0]);
                        Utils.addFileStandardOrder = Utils.parseBoolean(values[1]);
                        break;
                    case "prefLanguage":
                        settings.remove(values[0]);
                        Utils.prefLanguage = Utils.parsePrefLanguage(values[1]);
                        break;

                }

            }
            if(settings.size() > 0){
                StringBuilder errorBuilder = new StringBuilder();
                errorBuilder.append("Settings file incomplete, following settings missing: ");
                for(String s : settings){
                    errorBuilder.append(s);
                    errorBuilder.append(", ");
                }
                String errorMsg = errorBuilder.toString();
                errorMsg = errorMsg.substring(0, errorMsg.lastIndexOf(","));
                throw new IOException(errorMsg);
            }
        }
    }

    /**
     * Save the settings into the specified settings file. Overrides old settings.
     * @throws IOException if a problem occures while writing the file (e.g. missing privileges).
     */
    static void saveSettings() throws IOException{
        File settingsFile = Utils.settingsFilePath.toFile();
        if(settingsFile.exists() && !settingsFile.delete()){
            throw new IOException("Cannot delete old settings file to save new one.");
        }
        String settingsContent = "standardNumberOfWords=" + Utils.standardNumberOfWords + System.lineSeparator() +
                "standardLearningMethod=" + Utils.standardLearningMethod + System.lineSeparator() +
                "vocabFileName=" + Utils.vocabFileName + System.lineSeparator() +
                "prefKanji=" + Utils.PreferKanji.parse(Utils.prefKanji) + System.lineSeparator() +
                "prefLanguage=" + Utils.parsePrefLanguage(Utils.prefLanguage) + System.lineSeparator() +
                "addFileDelimiter=" + Utils.addFileDelimiter + System.lineSeparator() +
                "addFileStandardOrder=" + Boolean.toString(Utils.addFileStandardOrder) + System.lineSeparator() +
                "uploadAddress=" + Utils.uploadAddress + System.lineSeparator() +
                "uploadPort=" + Utils.uploadPort;
        writeToFile(settingsFile.getAbsolutePath(), settingsContent);
    }

    /**
     * Simple method that writes to a file.
     * @param path absolute path to write to.
     * @param text content to be written. Old content is overwritten.
     * @throws IOException if something wrong happens, e.g. permissions are missing.
     */
    static private void writeToFile(String path, String text) throws IOException{
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.println(text);
        writer.close();
    }
}
