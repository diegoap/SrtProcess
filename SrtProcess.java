
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.util.IllegalFormatException;

public class SrtProcess {

    public static void main(String[] args) throws IOException {

        File folder = new File(args[0]);
        File[] listOfFiles = folder.listFiles();

        int mkvCount=0;
        int srtCount=0;
        String mkvFullPath= null;
        String srtFullPath = null;

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {

                String fullPath = listOfFiles[i].getAbsolutePath();

                if (fullPath.endsWith("mkv")) {
                    mkvCount++;
                    mkvFullPath = fullPath;
                }

                if (fullPath.endsWith("srt")) {
                    srtCount++;
                    srtFullPath = fullPath;
                }

            }

        }

        if (!(srtCount==1 && mkvCount==1)) {
            throw new IllegalArgumentException("MKV and SRT size != 1");
        }

        String mkvFullPathNoExtension = mkvFullPath.substring(0, mkvFullPath.length()-4);
        String[] mkvFullPathNoExtensionParts = mkvFullPathNoExtension.split("/");
        String mkvFileNameNoExtension = mkvFullPathNoExtensionParts[mkvFullPathNoExtensionParts.length - 1];

        String srtFullPathNoExtension = srtFullPath.substring(0, srtFullPath.length()-4);
        String[] srtFullPathNoExtensionParts = srtFullPathNoExtension.split("/");
        String srtFileNameNoExtension = srtFullPathNoExtensionParts[srtFullPathNoExtensionParts.length - 1];

        if (!mkvFileNameNoExtension.equals(srtFileNameNoExtension)) {

            StringBuilder sb = new StringBuilder();
            for (int i=0; i<srtFullPathNoExtensionParts.length-1;i++) {
                sb.append(srtFullPathNoExtensionParts[i]);
                sb.append("/");
            }
            sb.append(mkvFileNameNoExtension);
            sb.append(".srt");

            String srtOldPath = srtFullPath;
            srtFullPath = sb.toString();

            rename(srtOldPath, srtFullPath);

        }

        String encodingFrom = detectCharset(srtFullPath);
        if (encodingFrom==null) {
            throw new IllegalStateException("cannot determine encoding of srt file");
        } else if (!encodingFrom.equals("UTF-8")){
            reencodeUTF8(srtFullPath, encodingFrom);
        }

    }


    private static void rename(String fileFrom, String fileTo) throws IOException {

        File file = new File(fileFrom);

        File file2 = new File(fileTo);

        if (file2.exists()) {
            throw new java.io.IOException("file exists");
        }

        boolean success = file.renameTo(file2);

        if (!success) {
            throw new IllegalStateException("could not rename");
        }

    }

    private static void reencodeUTF8(String srtFullPath, String srcEncoding) throws IOException {
        File file = new File(srtFullPath);
        String content = FileUtils.readFileToString(file, srcEncoding);
        FileUtils.write(file, content, "UTF-8");
    }

    private static String detectCharset(String fullPath) throws java.io.IOException {
        byte[] buf = new byte[4096];
         java.io.FileInputStream fis = new java.io.FileInputStream(fullPath);

        // (1)
        UniversalDetector detector = new UniversalDetector(null);

        // (2)
        int nread;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        // (3)
        detector.dataEnd();

        // (4)
        String encoding = detector.getDetectedCharset();

        // (5)
        detector.reset();

        return encoding;
    }

}
