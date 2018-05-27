package uk.co.binaryoverload.textbookparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

public class TextbookParser {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the base URL (Use {} for page number): ");
            String url = scanner.next().trim();
            System.out.print("How much zero padding for page numbers? ");
            int zeroPadding = scanner.nextInt();
            System.out.print("How many roman numeral pages? ");
            int romanNumeral = scanner.nextInt();
            System.out.print("Start of range: ");
            int start = scanner.nextInt();
            System.out.print("End of range: ");
            int end = scanner.nextInt();
            System.out.print("File extension: ");
            String fileExt = scanner.next().trim();

            int count = start;

            while (!(count > end)) {
                new Thread(new DownloadThread(
                        count,
                        Math.min(count + 9, end),
                        url,
                        fileExt,
                        zeroPadding,
                        romanNumeral
                )).start();
                count += 10;
            }

        }
    }

}
