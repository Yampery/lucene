package git.yampery.start;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @decription: StartSearch
 * <p>搜索</p>
 * @date 18/4/18 21:40
 * @author yampery
 */
public class StartSearch {

    /**
     * 1. 创建索引路径 Directory
     * 2. 打开索引 IndexReader
     * 3. 根据IndexReader创建IndexSearch
     * 4. 创建Query 传入分词器QueryParser
     * 5. 执行搜索 返回 TopDocs
     * 6. 根据TopDocs获取ScoreDoc对象
     * 7. 根据Searcher和ScoreDoc对象获取具体的Document对象
     * 8. 根据Document对象获取需要的值
     */

    private StartSearch() { }

    public static void main(String[] args) throws Exception {
        search("", "contents", "python", "index");

    }

    /**
     * 搜索
     * @param queries
     * @param field
     * @param queryString
     * @param indexPath
     * @throws Exception
     */
    public static void search(String queries, String field,
                      String queryString, String indexPath) throws Exception {
        try {
            // <1>
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            // <2>
            IndexReader reader = DirectoryReader.open(dir);
            // <3> 创建IndexSearch
            IndexSearcher searcher = new IndexSearcher(reader);
            // <4> 创建Query
            // BufferedReader in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
            QueryParser queryParser = new QueryParser(field, new StandardAnalyzer());
            Query query = queryParser.parse(queryString);
            System.out.println("Searching for: " + query.toString(field));
            // <5> 执行搜索
            TopDocs topDocs = searcher.search(query, 10);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            Document d;
            for (ScoreDoc doc : scoreDocs) {
                d = searcher.doc(doc.doc);
                System.out.println(d.get("path"));
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pageSearch() {

    }
}
