package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
//    Controller function which calls the indexFiles() and searchFiles()
//    Gives the user options for choosing the analyzer.
    public static void main(String[] args) throws IOException, ParseException {
        //Give user the option to choose the analyzer of their choice.
        Analyzer analyzer;
        BufferedReader buf= new BufferedReader(new InputStreamReader(System.in));
        String analyzer_selection="";
        For:
        for(;true;) {
            System.out.println("Please enter the choice of analyser:\n1) Simple Analyzer\n2) Whitespace Analyzer\n3) English Analyzer\n4) Stop Analyzer\n5) Standard Analyzer\n");
            int choice = Integer.parseInt(buf.readLine());
            switch (choice) {
                case 1:
                    analyzer = new SimpleAnalyzer();
                    analyzer_selection="Simple Analyzer";
                    break For;
                case 2:
                    analyzer = new WhitespaceAnalyzer();
                    analyzer_selection="Whitespace Analyzer";
                    break For;
                case 3:
                    analyzer = new EnglishAnalyzer();
                    analyzer_selection="English Analyzer";
                    break For;
                case 4:
                    analyzer = new StopAnalyzer();
                    analyzer_selection="Stop Analyzer";
                    break For;
                case 5:
                    analyzer = new StandardAnalyzer();
                    analyzer_selection="Standard Analyzer";
                    break For;
                default:
                    System.out.println("Wrong Choice! Try again!\n\n\n\n\n");
                    continue For;
            }
        }
        indexFiles(analyzer,analyzer_selection);
        searchFiles(analyzer);
    }

//    This function calls the Indexer class' indexFiles() to perform the indexing of the documents.
    public static void indexFiles(Analyzer analyzer, String analyzer_selection){
        String index_path = "indexes";
        String docs_path = "src/main/resources/cran/cran.all.1400";
        final Path doc_directory = Paths.get(docs_path);
        if (!Files.isReadable(doc_directory)) {
            System.out.println("Document directory '" +doc_directory.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(0);
        }

        try {
            Directory directory = FSDirectory.open(Paths.get(index_path));

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(directory, config);

            // Indexing the files
            Indexer.indexFiles(writer, doc_directory,analyzer_selection);
            writer.close();
        } catch (IOException e) {
            System.out.println("Error Details: "+e.getStackTrace());
        }
    }
//    This function calls the Searcher class' performSearch() to perform the search operation for a query and saves the result in a text file.
    public static void searchFiles(Analyzer analyzer) throws IOException, ParseException {
        String index = "indexes";
        String result_path = "results/search-results-test-test.txt";
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        PrintWriter writer = new PrintWriter(result_path, "UTF-8");
        IndexSearcher searcher = new IndexSearcher(reader);

        BufferedReader buffer= new BufferedReader(new InputStreamReader(System.in));
        For:
        for(;true;) {
            System.out.println("Please enter the choice for Similarity to be used for searched:\n1) BM25\n2) Vector Space\n");
            int choice = Integer.parseInt(buffer.readLine());
            switch (choice) {
                case 1:
                    searcher.setSimilarity(new BM25Similarity());
                    break For;
                case 2:
                    searcher.setSimilarity(new ClassicSimilarity());
                    break For;
//                case 3:
//                    searcher.setSimilarity(new AxiomaticF1LOG());
//                    break For;
                default:
                    System.out.println("Wrong Choice! Try again!\n\n\n\n\n");
                    continue For;
            }
        }

        String queries_path = "src/main/resources/cran/cran.qry";
        BufferedReader buf = Files.newBufferedReader(Paths.get(queries_path), StandardCharsets.UTF_8);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[] {"title","author","bibliography","content"}, analyzer);

        String query_string = "";
        Integer query_number = 1;
        String line="";
        Boolean first = true;

        System.out.println("Reading in queries and creating search results.");

        while ((line = buf.readLine()) != null){

            if(line.substring(0,2).equals(".I")){
                if(!first){
                    Query query = parser.parse(QueryParser.escape(query_string));
                    Searcher.performSearch(searcher,writer,query_number,query);
                    query_number++;
                }
                else{ first=false; }
                query_string = "";
            } else {
                query_string += " " + line;
            }
        }

        Query query = parser.parse(QueryParser.escape(query_string));
        Searcher.performSearch(searcher,writer,query_number,query);

        writer.close();
        reader.close();
    }
}