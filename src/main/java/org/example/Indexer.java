package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
public class Indexer {

    private Indexer() {}

    // Creates a document with the fields specified to be written to an index
    static Document createDocument(String id, String title, String author, String bib, String content){
        Document document = new Document();
        document.add(new StringField("ID", id, Field.Store.YES));
        document.add(new StringField("Path", id, Field.Store.YES));
        document.add(new TextField("Title", title, Field.Store.YES));
        document.add(new TextField("Author", author, Field.Store.YES));
        document.add(new TextField("Bibliography", bib, Field.Store.YES));
        document.add(new TextField("Content", content, Field.Store.YES));
        return document;
    }

//    This function separates the cranfield dataset into different documents and indexes them.
    static void indexFiles(IndexWriter writer, Path file, String analyzer_selection) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {

            BufferedReader buffer = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

            String id = "", title = "", author = "", bib = "", content = "", state = "";
            Boolean first = true;
            String line;

            System.out.println("Indexing documents using "+analyzer_selection+".");

            // Read in lines from the cranfield collection and create indexes for them
            while ((line = buffer.readLine()) != null){
                switch(line.substring(0,2)){
                    case ".I":
                        if(!first){
                            Document d = createDocument(id,title,author,bib,content);
                            writer.addDocument(d);
                        }
                        else first=false;
                        title = "";
                        author = "";
                        bib = "";
                        content = "";
                        id = line.substring(3,line.length());
                        break;
                    case ".T":
                    case ".A":
                    case ".B":
                    case ".W":
                        state = line; break;
                    default:
                        switch(state){
                            case ".T": title += line + " "; break;
                            case ".A": author += line + " "; break;
                            case ".B": bib += line + " "; break;
                            case ".W": content += line + " "; break;
                        }
                }
            }
            Document d = createDocument(id,title,author,bib,content);
            writer.addDocument(d);
        }
    }
}
