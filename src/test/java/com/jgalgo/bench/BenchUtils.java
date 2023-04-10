package com.jgalgo.bench;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

public class BenchUtils {

    static Map<String, String> parseArgsStr(String s) {
        String[] strs = s.split(" ");
        Map<String, String> args = new Object2ObjectArrayMap<>(strs.length);
        for (String arg : strs) {
            int idx = arg.indexOf('=');
            String key = arg.substring(0, idx);
            String value = arg.substring(idx + 1);
            args.put(key, value);
        }
        return args;
    }

}
