package uk.co.binaryoverload.textbookparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class DownloadThread implements Runnable {

    private final int start;
    private final int end;
    private final String url;
    private final String fileExt;
    private final int zeroPad;
    private final int romanNumeral;

    public DownloadThread(int start, int end, String url, String fileExt, int zeroPad, int romanNumeral) {
        this.start = start;
        this.end = end;
        this.url = url;
        this.fileExt = fileExt;
        this.zeroPad = zeroPad;
        this.romanNumeral = romanNumeral;
    }

    @Override
    public void run() {
        System.out.printf("Starting download of %d images (Start: %d End: %d)\n",(end - start + 1), start, end);
        int failed = 0;
        int successful = 0;
        for (int i = start; i <= end; i++) {
            try {
                URL website = new URL(url.replace("{}", Utils.zeroPad(i, zeroPad)));
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                String filename = i > romanNumeral ? String.valueOf(i - romanNumeral) : Utils.toRoman(i);
                File file = new File("photos", filename + "." + fileExt);
                file.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(file);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                successful++;
            } catch (IOException e) {
                e.printStackTrace();
                failed++;
            }
        }
        System.out.printf("Finished download of images in range %d-%d (Successful: %d Failed: %d)\n", start, end, successful, failed);
    }
}
