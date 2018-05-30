package uk.co.binaryoverload.textbookparser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

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
            System.out.print("Number of threads (Default is 5): ");
            int threadCount = scanner.nextInt();
            threadCount = threadCount > 0 && threadCount < 50 ? threadCount : 5;
            System.out.print("Items per thread (Default is 10): ");
            int itemCount = scanner.nextInt();
            itemCount = itemCount > 0 ? itemCount : 10;

            int count = start;

            BlockingQueue<Integer> startQueue = new LinkedBlockingQueue<>();
            List<Long> averageTimePerItem = Collections.synchronizedList(new ArrayList<>());

            while (!(count > end)) {
                startQueue.put(count);
                count += itemCount;
            }

            ExecutorService pool = Executors.newFixedThreadPool(threadCount);

            for (int i = 1; i <= threadCount; i++) {
                int finalItemCount = itemCount;
                Runnable r = () -> {
                    Integer start1;
                    while ((start1 = startQueue.poll()) != null) {
                        int end1 = Math.min(start1 + (finalItemCount - 1), end);
                        System.out.printf("Starting download of %d images (Start: %d End: %d)\n", (end1 - start1 + 1), start1, end1);
                        int failed = 0;
                        int successful = 0;
                        long startTime = System.currentTimeMillis();
                        for (int i1 = start1; i1 <= end1; i1++) {
                            try {
                                URL website = new URL(url.replace("{}", Utils.zeroPad(i1, zeroPadding)));
                                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                                String filename = i1 > romanNumeral ? String.valueOf(i1 - romanNumeral) : Utils.toRoman(i1);
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
                        long endTime = System.currentTimeMillis();
                        averageTimePerItem.add((endTime - startTime) / (end1 - start1 + 1));
                        System.out.printf("Finished download of images in range %d-%d in %ds (Successful: %d Failed: %d)\n", start1, end1, (endTime - startTime) / 1000, successful, failed);
                    }
                };
                pool.execute(r);
            }
            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.out.printf("Average time per item: %fms", averageTimePerItem.stream().mapToLong(n -> n).average().getAsDouble());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
