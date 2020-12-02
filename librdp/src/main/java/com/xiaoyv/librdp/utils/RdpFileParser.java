package com.xiaoyv.librdp.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

/**
 * 文件解析
 */
public class RdpFileParser {

    private static final int MAX_ERRORS = 20;
    private static final int MAX_LINES = 500;

    private HashMap<String, Object> options;

    public RdpFileParser() {
        init();
    }

    public RdpFileParser(String filename) throws IOException {
        init();
        parse(filename);
    }

    private void init() {
        options = new HashMap<>();
    }

    public void parse(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        int errors = 0;
        int lines = 0;
        boolean ok;

        while ((line = br.readLine()) != null) {
            lines++;
            ok = false;

            if (errors > MAX_ERRORS || lines > MAX_LINES) {
                br.close();
                throw new IOException("Parsing limits exceeded");
            }

            String[] fields = line.split(":", 3);

            if (fields.length == 3) {
                switch (fields[1]) {
                    case "s":
                        options.put(fields[0].toLowerCase(Locale.ENGLISH), fields[2]);
                        ok = true;
                        break;
                    case "i":
                        try {
                            Integer i = Integer.parseInt(fields[2]);
                            options.put(fields[0].toLowerCase(Locale.ENGLISH), i);
                            ok = true;
                        } catch (NumberFormatException ignored) {
                        }
                        break;
                    case "b":
                        ok = true;
                        break;
                }
            }
            if (!ok)
                errors++;
        }
        br.close();
    }

    public String getString(String optionName) {
        if (options.get(optionName) instanceof String)
            return (String) options.get(optionName);
        else
            return null;
    }

    public Integer getInteger(String optionName) {
        if (options.get(optionName) instanceof Integer)
            return (Integer) options.get(optionName);
        else
            return null;
    }
}
