package com.mixc.cpms.schedule.mq.service.common;

/**
 * @author Joseph
 * @since 2022/11/24
 */
public class RadixConvertKit {

    private static final char[] CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };


    public static String tenTo62(long num) {
        AssertKit.check(NumberKit.lt0(num), "目前仅支持正整数");

        StringBuilder b = new StringBuilder();
        while (num > 0) {
            b.append(CHARS[(int) (num % 62)]);
            num = num / 62;
        }
        return b.reverse().toString();
    }

}
