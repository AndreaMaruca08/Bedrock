package scan;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;

public class AsyncScanner {

    private final ExecutorService pool;

    public AsyncScanner(int threads) {
        this.pool = Executors.newFixedThreadPool(threads);
    }

    public BedrockNode scan(Path rootPath) throws InterruptedException, ExecutionException {
        BedrockNode root = new BedrockNode(rootPath.getFileName() != null
                ? rootPath.getFileName().toString()
                : rootPath.toString(), true);

        List<Path> topLevelDirs = new ArrayList<>();
        List<Path> topLevelFiles = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    topLevelDirs.add(entry);
                } else {
                    topLevelFiles.add(entry);
                }
            }
        } catch (IOException e) {
            return root; // root illeggibile, restituisci vuoto
        }

        // file diretti nella root
        for (Path file : topLevelFiles) {
            try {
                long size = Files.size(file);
                BedrockNode fileNode = new BedrockNode(file.getFileName().toString(), false);
                fileNode.ownSize = size;
                fileNode.totalSize = size;
                fileNode.parent = root;
                root.children.add(fileNode);
            } catch (IOException ignored) {
                // permission denied o file rimosso durante lo scan
            }
        }

        // una task per ogni sottocartella di primo livello
        List<Future<BedrockNode>> futures = new ArrayList<>();
        for (Path dir : topLevelDirs) {
            futures.add(pool.submit(() -> scanSubtree(dir)));
        }

        long total = 0;
        for (int i = 0; i < futures.size(); i++) {
            BedrockNode child = futures.get(i).get();
            child.parent = root;
            root.children.add(child);
            total += child.totalSize;
        }
        for (BedrockNode f : root.children) {
            if (!f.isDirectory) total += 0; // già sommati sopra, evita doppio conteggio
        }
        root.totalSize = total;

        return root;
    }

    private BedrockNode scanSubtree(Path dirPath) {
        Deque<BedrockNode> stack = new ArrayDeque<>();
        BedrockNode subRoot = new BedrockNode(dirPath.getFileName().toString(), true);
        stack.push(subRoot);

        try {
            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.equals(dirPath)) {
                        return FileVisitResult.CONTINUE; // già creato come subRoot
                    }
                    BedrockNode node = new BedrockNode(dir.getFileName().toString(), true);
                    node.parent = stack.peek();
                    stack.peek().children.add(node);
                    stack.push(node);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    BedrockNode node = new BedrockNode(file.getFileName().toString(), false);
                    node.ownSize = attrs.size();
                    node.totalSize = attrs.size();
                    node.parent = stack.peek();
                    stack.peek().children.add(node);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    if (dir.equals(dirPath) && stack.size() == 1) {
                        return FileVisitResult.CONTINUE; // subRoot si chiude fuori dal visitor
                    }
                    BedrockNode finished = stack.pop();
                    long sum = finished.ownSize;
                    for (BedrockNode child : finished.children) sum += child.totalSize;
                    finished.totalSize = sum;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // errore di I/O non gestito dal visitor, il sottoalbero resta parziale
        }

        // chiudi subRoot manualmente (non passa da postVisitDirectory)
        long sum = subRoot.ownSize;
        for (BedrockNode child : subRoot.children) sum += child.totalSize;
        subRoot.totalSize = sum;

        return subRoot;
    }

    public void shutdown() {
        pool.shutdown();
    }
}