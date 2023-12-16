import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WordProcessor {
    private String text;

    public WordProcessor(String filePath) {
        try {
            this.text = readTextFromFile(filePath);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            this.text = ""; // Set text to an empty string in case of an error
        }
    }

    private String readTextFromFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    // Synchronized method to find the longest word using multiple threads
    public synchronized String findLongestWordMultiThreaded() throws InterruptedException {
        return findLongestWord(text);
    }

    // Synchronized method to find the shortest word using multiple threads
    public synchronized String findShortestWordMultiThreaded() throws InterruptedException {
        return findShortestWord(text);
    }

    // Synchronized method to count specific words using multiple threads
    public synchronized Map<String, Integer> countSpecificWordsMultiThreaded() throws InterruptedException {
        return countSpecificWords(text);
    }

    // Synchronized method to count total words using multiple threads
    public synchronized int countWordsMultiThreaded() throws InterruptedException {
        return countWords(text).values().stream().mapToInt(Integer::intValue).sum();
    }  
    private String findLongestWord(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String[] words = text.split("\\s+");
        AtomicReference<String> longestWord = new AtomicReference<>("");
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

            executor.execute(() -> {
                for (String word : wordChunk) {
                    synchronized (longestWord) {
                        String currentLongest = longestWord.get();
                        if (word.length() > currentLongest.length()) {
                            longestWord.set(word);
                        }
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return longestWord.get();
    }

    private String findShortestWord(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String[] words = text.split("\\s+");
        AtomicReference<String> shortestWord = new AtomicReference<>("");
        AtomicInteger shortestLength = new AtomicInteger(Integer.MAX_VALUE);

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

            executor.execute(() -> {
                for (String word : wordChunk) {
                    synchronized (shortestWord) {
                        if (word.length() < shortestLength.get()) {
                            shortestLength.set(word.length());
                            shortestWord.set(word);
                        }
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return shortestWord.get();
    }
    /*private void processLongestWordChunk(String[] wordChunk, String longestWord) {
    
        for (String word : wordChunk) {
            if (word.length() > longestWord.length()) {
                longestWord = word;
            }
        }
    }
    

    private void processShortestWordChunk(String[] wordChunk, String shortestWord, int shortestLength) {
        for (String word : wordChunk) {
            synchronized (shortestWord) {
                if (word.length() < shortestLength) {
                    shortestLength = word.length();
                    shortestWord = word;
                }
            }
        }
    }
    */
    private Map<String, Integer> countWords(String text) throws InterruptedException {
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

    private Map<String, Integer> countSpecificWords(String text) throws InterruptedException {
        if (text == null || text.isEmpty()) {
            return new HashMap<>();
        }

        String[] words = text.split("\\s+");
        Map<String, Integer> specificWordCountMap = new ConcurrentHashMap<>();

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

            executor.execute(() -> processSpecificWordChunk(wordChunk, specificWordCountMap));
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        return specificWordCountMap;
    }

    private void processSpecificWordChunk(String[] wordChunk, Map<String, Integer> specificWordCountMap) {
        for (String word : wordChunk) {
            // Check for specific words "he", "is", and "are"
            if (word.equalsIgnoreCase("he") || word.equalsIgnoreCase("is") || word.equalsIgnoreCase("are")) {
                synchronized (specificWordCountMap) {
                    specificWordCountMap.put(word, specificWordCountMap.getOrDefault(word, 0) + 1);
                }
            }
        }
    }

    public static void main(String[] args) {
        String filePath = "D:\\Third Year\\Second Term\\OS2 Project\\Testing\\First.txt";

    WordProcessor wordProcessor = new WordProcessor(filePath);
    try {
        Map<String, Integer> specificWordCountMap = wordProcessor.countSpecificWordsMultiThreaded();
        int totalWordsCount = wordProcessor.countWordsMultiThreaded();
        String longestWord = wordProcessor.findLongestWordMultiThreaded();
        String shortestWord = wordProcessor.findShortestWordMultiThreaded();


        // Display specific word counts
        System.out.println("Word counts for 'he', 'is', 'are':");
        System.out.println("he: " + specificWordCountMap.getOrDefault("he", 0));
        System.out.println("is: " + specificWordCountMap.getOrDefault("is", 0));
        System.out.println("are: " + specificWordCountMap.getOrDefault("are", 0));

        System.out.println("\nTotal Words Count: " + totalWordsCount);
        System.out.println("Longest Word: " + longestWord);
        System.out.println("Shortest Word: " + shortestWord);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    }
}
