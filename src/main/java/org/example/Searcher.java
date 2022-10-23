package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.document.Document;
public class Searcher {

    private Searcher() {}

    public static void performSearch(IndexSearcher searcher, PrintWriter writer, Integer queryNumber, Query query) throws IOException {
        TopDocs result = searcher.search(query, 1400);
        ScoreDoc hits[] = result.scoreDocs;

        for(int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            /*
             * Write the results in the format expected by trec_eval:
             * | Query Number | 0 | Document ID | Rank | Score | "EXP" |
             * (https://stackoverflow.com/questions/4275825/how-to-evaluate-a-search-retrieval-engine-using-trec-eval)
             */
            writer.println(queryNumber + " 0 " + doc.get("id") + " " + i + " " + hits[i].score + " EXP");
        }
    }
}
