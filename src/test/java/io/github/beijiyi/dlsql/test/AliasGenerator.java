package io.github.beijiyi.dlsql.test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class AliasGenerator {
    private static final char[] chars = "ABCDEFGHIJKLMNOPQRSUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    public static String generateRandomAlias() {
        Random random = new Random();
        char[] chars = "ABCDEFGHIJKLMNOPQRSUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder aliasBuilder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            char randomChar = chars[random.nextInt(chars.length)];
            aliasBuilder.append(randomChar);
        }
        return aliasBuilder.toString();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {
            String alias = generateRandomAlias();
            System.out.println("Generated Alias: " + alias+i);
        }
    }
}
