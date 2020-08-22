package com.bencullivan.blizzard.util;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * For reading files as fast as possible.
 * Modified version of input class found at https://www.geeksforgeeks.org/fast-io-in-java-in-competitive-programming/
 */
public class FileReader {

    private final StringBuilder sb;
    private final int BUFFER_SIZE;
    private final byte[] buffer;
    private final DataInputStream din;

    /**
     * @param fileName The name of the file that will be read from.
     * @throws IOException If there is a problem opening the input stream.
     */
    public FileReader(String fileName) throws IOException {
        sb = new StringBuilder();
        BUFFER_SIZE = 65536;
        buffer = new byte[BUFFER_SIZE];
        din = new DataInputStream(new FileInputStream(fileName));
    }

    /**
     * Reads a file into a StringBuilder and then closes the input stream.
     * @return This FileReader.
     */
    public FileReader readFile() {
        try {
            int bytesRead = din.read(buffer, 0, BUFFER_SIZE);
            while (bytesRead != -1) {
                sb.append(new String(Arrays.copyOfRange(buffer, 0, bytesRead), StandardCharsets.UTF_8));
                bytesRead = din.read(buffer, 0, BUFFER_SIZE);
            }
            din.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Returns the String of the StringBuilder containing the data from the input file.
     * @return The String.
     */
    public String getFileString() {
        return sb.toString();
    }
}