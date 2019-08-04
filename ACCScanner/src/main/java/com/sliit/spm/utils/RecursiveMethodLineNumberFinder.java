package com.sliit.spm.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecursiveMethodLineNumberFinder {

    private static Pattern method = Pattern.compile("(public|protected|private|static|\\s) +[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])");
    private static Pattern recursiveMethodCall = Pattern.compile("(return) +[\\w\\<\\>\\[\\]]+\\s+(\\w+) *\\([^\\)]*\\) *(\\{?|[^;])");

    private static String tempMethodNameHolder = "";
    private static Integer methodNameLineNumber = 0;
    private static Integer recursiveMethodCallLineNumber = 0;

    private static Map<Integer, Integer> recursionLineNumbers = new HashMap<>();

    public static Map<Integer, Integer> getRecursiveMethodLineNumbers(File file) {

        //Add code to ignore comments
        try (LineNumberReader lnr = new LineNumberReader(new FileReader(file))) {

            boolean singleLineCommented = false;
            boolean multiLineCommented = false;

            for (String line; (line = lnr.readLine()) != null; ) {

                // ignore comment lines
                if (line.trim().startsWith("//") || line.trim().startsWith("import") || line.trim().startsWith("include")) {
                    singleLineCommented = true;
                } else {
                    singleLineCommented = false;
                }
                if (line.trim().startsWith("/*")) {
                    multiLineCommented = true;
                }
                if (line.trim().startsWith("*/")) {
                    line = line.replaceFirst("\\*/", "");
                    multiLineCommented = false;
                }
                if (line.contains("//")) {
                    line = line.replace(line.substring(line.indexOf("//")), "");
                }


                if (!singleLineCommented && !multiLineCommented) {
                    // method finder
                    Matcher m = method.matcher(line);
                    while (m.find()) {
                        if (!tempMethodNameHolder.equals(m.group(2))) {
                            tempMethodNameHolder = m.group(2);
                            methodNameLineNumber = lnr.getLineNumber();
                        }
                    }

                    if (line.trim().startsWith("return")) {
                        List<String> lineDataList = Arrays.asList(line.trim().replaceAll("[\\(]", " ").split(" "));
                        if (lineDataList.size() >= 2 && lineDataList.get(1).equals(tempMethodNameHolder)) {
                            recursiveMethodCallLineNumber = lnr.getLineNumber();
                            if (methodNameLineNumber > 0 && recursiveMethodCallLineNumber > methodNameLineNumber) {
                                recursionLineNumbers.put(methodNameLineNumber, recursiveMethodCallLineNumber);
                                methodNameLineNumber = 0;
                                recursiveMethodCallLineNumber = 0;
                            }
                        }
                    }
                }

                if (line.trim().endsWith("*/")) {
                    multiLineCommented = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(recursionLineNumbers);
        return recursionLineNumbers;
    }
}
