package com.google.code.externalsorting;// filename: com.google.code.externalsorting.ExternalSort.java

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class ExternalSort {

    /**
     * This method calls the garbage collector and then returns the free
     * memory. This avoids problems with applications where the GC hasn't
     * reclaimed memory and reports no available memory.
     *
     * @return available memory
     */
    private static long estimateAvailableMemory() {

        System.gc();
        // http://stackoverflow.com/questions/12807797/java-get-available-memory
        Runtime r = Runtime.getRuntime();
        long allocatedMemory = r.totalMemory() - r.freeMemory();

        return r.maxMemory() - allocatedMemory;
    }

    /**
     * we divide the file into small blocks. If the blocks are too small, we
     * shall create too many temporary files. If they are too big, we shall
     * be using too much memory.
     *
     * @param sizeoffile  how much data (in bytes) can we expect
     * @param maxMemory   Maximum memory to use (in bytes)
     * @return the estimate
     */
    private static long estimateBestSizeOfBlocks(final long sizeoffile,
                                                 final long maxMemory) {

        // we don't want to open up much more than maxtmpfiles temporary
        // files, better run
        // out of memory first.
        long blocksize = sizeoffile / ExternalSort.DEFAULTMAXTEMPFILES +
                (sizeoffile % ExternalSort.DEFAULTMAXTEMPFILES == 0 ? 0 : 1);

        // on the other hand, we don't want to create many temporary
        // files
        // for naught. If blocksize is smaller than half the free
        // memory, grow it.
        if (blocksize < maxMemory / 2) {
            blocksize = maxMemory / 2;
        }

        return blocksize;
    }

    /**
     * This merges several com.google.code.externalsorting.BinaryFileBuffer to an output writer.
     *
     * @param fbw      A buffer where we write the data.
     * @param cmp      A comparator object that tells us how to sort the
     *                 lines.
     * @param buffers  Where the data should be read.
     * @return The number of lines sorted.
     * @throws IOException generic IO exception
     */
    private static long mergeSortedFiles(BufferedWriter fbw,
                                         final Comparator<String> cmp,
                                         List<BinaryFileBuffer> buffers) throws IOException {

        PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<>(
                11,
                (i, j) -> cmp.compare(i.peek(), j.peek())
        );

        for (BinaryFileBuffer bfb : buffers) {
            if (!bfb.empty()) {
                pq.add(bfb);
            }
        }

        long rowcounter = 0;
        try {

            String lastLine = null;
            if (pq.size() > 0) {

                BinaryFileBuffer bfb = pq.poll();
                lastLine = bfb.pop();
                fbw.write(lastLine);

                ++rowcounter;

                if (bfb.empty()) {
                    bfb.fbr.close();
                } else {
                    pq.add(bfb); // add it back
                }
            }

            while (pq.size() > 0) {

                BinaryFileBuffer bfb = pq.poll();
                String r = bfb.pop();

                // Skip duplicate lines
                if (cmp.compare(r, lastLine) != 0) {
                    fbw.newLine();
                    fbw.write(r);
                    lastLine = r;
                } else {
                    String[] list = r.split(":");
                    if (list.length >= 2) fbw.write(String.format("|%s", list[1]));
                }

                ++rowcounter;

                if (bfb.empty()) {
                    bfb.fbr.close();
                } else {
                    pq.add(bfb); // add it back
                }
            }

        } finally {
            fbw.close();
            for (BinaryFileBuffer bfb : pq) {
                bfb.close();
            }
        }

        return rowcounter;
    }

    /**
     * This merges a bunch of temporary flat files
     *
     * @param files      The {@link List} of sorted {@link File}s to be merged.
     * @param outputfile The output {@link File} to merge the results to.
     * @param cmp        The {@link Comparator} to use to compare
     *                   {@link String}s.
     * @return The number of lines sorted.
     * @throws IOException generic IO exception
     * @since v0.1.4
     */
    public static long mergeSortedFiles(List<File> files, File outputfile,
                                        final Comparator<String> cmp) throws IOException {

        ArrayList<BinaryFileBuffer> bfbs = new ArrayList<>();
        for (File f : files) {
            InputStream in = new FileInputStream(f);
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(in));

            BinaryFileBuffer bfb = new BinaryFileBuffer(br);
            bfbs.add(bfb);
        }

        BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputfile)));
        long rowcounter = mergeSortedFiles(fbw, cmp, bfbs);
        for (File f : files) {
            f.delete();
        }

        return rowcounter;
    }

    /**
     * Sort a list and save it to a temporary file
     *
     * @param tmplist      data to be sorted
     * @param cmp          string comparator
     * @param tmpdirectory location of the temporary files (set to null for
     *                     default location)
     * @return the file containing the sorted data
     * @throws IOException generic IO exception
     */
    private static File sortAndSave(List<String> tmplist,
                                    Comparator<String> cmp, File tmpdirectory) throws IOException {

        tmplist = tmplist.parallelStream().sorted(cmp).collect(Collectors.toCollection(ArrayList::new));

        File newTemporaryFile = File.createTempFile("sortInBatch", "flatfile", tmpdirectory);
        newTemporaryFile.deleteOnExit();

        OutputStream out = new FileOutputStream(newTemporaryFile);

        try (BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(out))) {
            String lastLine = null;
            Iterator<String> i = tmplist.iterator();
            if (i.hasNext()) {
                lastLine = i.next();
                fbw.write(lastLine);
            }

            while (i.hasNext()) {
                String r = i.next();
                // Skip duplicate lines
                if (cmp.compare(r, lastLine) != 0) {

                    fbw.newLine();
                    fbw.write(r);
                    lastLine = r;
                } else {
                    String[] list = r.split(":");

                    if (list.length >= 2) fbw.write(String.format("|%s", list[1]));
                }
            }
        }

        return newTemporaryFile;
    }


    /**
     * This will simply load the file by blocks of lines, then sort them
     * in-memory, and write the result to temporary files that have to be
     * merged later.
     *
     * @param file         data source
     * @param cmp          string comparator
     * @param tmpdirectory location of the temporary files (set to null for
     *                     default location)
     * @return a list of temporary flat files
     * @throws IOException generic IO exception
     */
    public static List<File> sortInBatch(final File file,
                                         final Comparator<String> cmp,
                                         final File tmpdirectory)
            throws IOException {

        List<File> files = new ArrayList<>();

        try (BufferedReader fbr = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            long blockSize = estimateBestSizeOfBlocks(
                    file.length(),
                    estimateAvailableMemory()
            ); // in bytes
            List<String> temporaryList = new ArrayList<>();
            String line = "";
            try {
                while (line != null) {
                    long currentblocksize = 0;// in bytes
                    while ((currentblocksize < blockSize)
                            && ((line = fbr.readLine()) != null)) {
                        // as long as you have enough
                        // memory
                        temporaryList.add(line);
                        currentblocksize += StringSizeEstimator
                                .estimatedSizeOf(line);
                    }
                    files.add(sortAndSave(temporaryList, cmp,
                            tmpdirectory));
                    temporaryList.clear();
                }
            } catch (EOFException oef) {
                if (temporaryList.size() > 0) {
                    files.add(sortAndSave(temporaryList, cmp,
                            tmpdirectory));
                    temporaryList.clear();
                }
            }
        }

        return files;
    }

    /**
     * Default maximal number of temporary files allowed.
     */
    private static final int DEFAULTMAXTEMPFILES = 1024;

}

/**
 * This is essentially a thin wrapper on top of a BufferedReader... which keeps
 * the last line in memory.
 */
final class BinaryFileBuffer {

    BinaryFileBuffer(BufferedReader r) throws IOException {
        this.fbr = r;
        reload();
    }

    void close() throws IOException {
        this.fbr.close();
    }

    boolean empty() {
        return this.cache == null;
    }

    String peek() {
        return this.cache;
    }

    String pop() throws IOException {
        String answer = peek();// make a copy
        reload();
        return answer;
    }

    private void reload() throws IOException {
        this.cache = this.fbr.readLine();
    }

    BufferedReader fbr;

    private String cache;
}
