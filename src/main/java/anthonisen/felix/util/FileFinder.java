package anthonisen.felix.util;

import java.io.File;

public class FileFinder {
    public static String findRelativePath(File rootDir, String fileName) {
        for (File file : rootDir.listFiles()) {
            if (file.isDirectory()) {
                String path = findRelativePath(file, fileName);
                if (path != null) {
                    return rootDir.getName() + "/" + path;
                }
            } else if (fileName.equals(file.getName()))
                return rootDir.getName() + "/" + fileName;
        }
        return null;
    }
}
