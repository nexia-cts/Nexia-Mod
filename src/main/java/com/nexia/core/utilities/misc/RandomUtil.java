package com.nexia.core.utilities.misc;

import java.util.Random;

public class RandomUtil {
    public static int randomInt(int minimum, int maximum){
        return new Random().nextInt(maximum - minimum + 1) + minimum;
    }

    public static int randomInt(int number) {
        return new Random().nextInt(number);
    }
}
