import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class WordProcessor {
    private String text;

    public WordProcessor(String text) {
        this.text = text;
    }

/* 
    //Method to count the total number of words in the text
    
    public int countWords() {}

    // Method to find the shortest word in the text
    public String findShortestWord() {}

    // Method to find the longest word in the text
    public String findLongestWord() {}
*/


    // Method to count occurrences of specific words ("is", "he", "she")
  
    /*public Map<String, Integer> countSpecificWords() {
        if (text == null || text.isEmpty()) {
            return new HashMap<>();
        }
        String[] words = text.split("\\s+");
        This line splits the text into an array of words using
         whitespace (\\s+) as the delimiter. The split method separates
         the text into individual words based on spaces, tabs, or line breaks. 
        Map<String, Integer> wordCountMap = new HashMap<>();
        for (String word : words) {
            wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
        }
        return wordCountMap;
    }
    */

    public Map<String, Integer> countSpecificWordsMultiThreaded() throws InterruptedException {
        if (text == null || text.isEmpty()) {
            return new ConcurrentHashMap<>();
        }

        String[] words = text.split("\\s+");
        Map<String, Integer> wordCountMap = new ConcurrentHashMap<>();
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int chunkSize = words.length / numThreads;
        int startIndex = 0;
        int endIndex = chunkSize;

        for (int i = 0; i < numThreads; i++) {
            if (i == numThreads - 1) {
                endIndex = words.length;
            }
            final String[] wordChunk = Arrays.copyOfRange(words, startIndex, endIndex);
            startIndex = endIndex;
            endIndex = Math.min(endIndex + chunkSize, words.length);

            final int threadIndex = i;
            executor.execute(() -> processWordChunk(wordChunk, wordCountMap));
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        return wordCountMap;
    }

    private void processWordChunk(String[] wordChunk, Map<String, Integer> wordCountMap) {
        for (String word : wordChunk) {
            synchronized (wordCountMap) {
                wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
            }
        }
    }



    // Multi-threaded implementation to count words in the text
    public int countWordsMultiThreaded() throws Exception {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        String[] words = text.split("\\s+");
        int numThreads = Runtime.getRuntime().availableProcessors(); // Get number of available processors
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int chunkSize = words.length / numThreads;
        int startIndex = 0;
        int endIndex = chunkSize;

        Future<Integer>[] futures = new Future[numThreads];

        for (int i = 0; i < numThreads; i++) {
            if (i == numThreads - 1) {
                endIndex = words.length; // Last thread takes the remaining words
            }
            final String[] wordChunk = Arrays.copyOfRange(words, startIndex, endIndex);
            startIndex = endIndex;
            endIndex = Math.min(endIndex + chunkSize, words.length);

            final int threadIndex = i;
            Callable<Integer> task = () -> wordChunk.length;
            futures[i] = executor.submit(task);
        }

        int totalWordsCount = 0;
        for (Future<Integer> future : futures) {
            totalWordsCount += future.get(); // Retrieve the result from each thread
        }

        executor.shutdown();
        return totalWordsCount;
    }

    // Example of usage
    public static void main(String[] args) {
        String sampleText = "he is she and she is he. They are counting words.i love egypt and hate bitches";

        WordProcessor processor = new WordProcessor(sampleText);
/* 
        System.out.println("Total words: " + processor.countWords());
        System.out.println("Shortest word: " + processor.findShortestWord());
        System.out.println("Longest word: " + processor.findLongestWord());

        Map<String, Integer> specificWordCount = processor.countSpecificWords();
        System.out.println("Occurrences of 'is': " + specificWordCount.getOrDefault("is", 0));
        System.out.println("Occurrences of 'he': " + specificWordCount.getOrDefault("he", 0));
        System.out.println("Occurrences of 'she': " + specificWordCount.getOrDefault("she", 0));
        */
        try {
            Map<String, Integer> wordCount = wordProcessor.countSpecificWordsMultiThreaded();

            // Display word counts
            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Total words (Multi-threaded): " + processor.countWordsMultiThreaded());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
