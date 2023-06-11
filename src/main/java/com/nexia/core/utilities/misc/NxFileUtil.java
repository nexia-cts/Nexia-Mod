package com.nexia.core.utilities.misc;

import java.io.File;

public class NxFileUtil {

    public static String makeDir(String string) {
        new File(string).mkdirs();
        return string;
    }
}
