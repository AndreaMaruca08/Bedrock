package scan;

public class FileCategory{
    public String extension;
    public int count;
    public long totalSize;
    public FileCategory(String extension, int count, long totalSize) {
        this.extension = extension;
        this.count = count;
        this.totalSize = totalSize;
    }
}
