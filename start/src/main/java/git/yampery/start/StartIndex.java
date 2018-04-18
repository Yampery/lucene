package git.yampery.start;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

/**
 * @decription: StartIndex
 * <p>索引创建</p>
 * @date 18/4/17 23:40
 * @author yampery
 */
public class StartIndex {

    public static void main(String[] args) {
        boolean create = true;
        String docsPath = args[0];
        if ("update".equals(docsPath)) {
            create = false;
            docsPath = args[1];
        }
        index(create, docsPath, "index");
    }

    /**
     * 创建索引
     */
    public static void index(boolean create, String docsPath, String indexPath) {

        final Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" +docDir.toAbsolutePath()+
                    "' does not exist or is not readable, please check the path");
            System.exit(1);
        }
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");
            Date start = new Date();
            // 1. 创建Directory
            Directory dir = FSDirectory.open(Paths.get(indexPath));

            // 2. 创建IndexWriter，并指定分词器
            // 创建IndexWriterConfig，默认使用标准分词器StandardAnalyzer
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
            if (create) {
                config.setOpenMode(OpenMode.CREATE);
            } else {
                // 将一个新的文档添加到索引
                config.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }
            // 可以设置RAM缓冲大小
            // 如果这么做的话，配置JVM参数：add -Xmx512m or -Xmx1g
            // config.setRAMBufferSizeMB(256.0);
            IndexWriter writer = new IndexWriter(dir, config);
            // 3. 索引文档
            indexDocs(writer, docDir);
            writer.close();
            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /** 通过给定文件或路径索引文档 **/
    static void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException e) {

                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /** 索引一个文档 **/
    static void indexDoc(final IndexWriter writer, Path file, long lastModified) throws IOException {

        // 获取原文件的输入流
        try (InputStream stream = Files.newInputStream(file)) {
            // 1. 创建一个新的空Document
            Document document = new Document();
            // 将文件file的路径作为一个filed命名为"path"
            // 只索引不分词
            Field pathField = new StringField("path", file.toString(), Field.Store.YES);
            document.add(pathField);
            // 添加最后一次修改时间
            document.add(new LongPoint("modified", lastModified));
            // 添加内容，索引并分词
            document.add(new TextField("contents",
                    new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
            if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                System.out.println("adding " + file);
                // 新建索引
                writer.addDocument(document);
            } else {
                // 已经存在索引，使用更新
                System.out.println("updating " + file);
                writer.updateDocument(new Term("path", file.toString()), document);
            }
        }
    }
}
