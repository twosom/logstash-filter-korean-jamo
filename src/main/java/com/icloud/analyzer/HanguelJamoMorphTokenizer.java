package com.icloud.analyzer;

public class HanguelJamoMorphTokenizer {

    // {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'}
    private static final char[] CHOSUNG =
            {0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145, 0x3146,
                    0x3147, 0x3148, 0x3149, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};
    // {'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'}
    private static final char[] JUNGSUNG =
            {0x314f, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158, 0x3159,
                    0x315a, 0x315b, 0x315c, 0x315d, 0x315e, 0x315f, 0x3160, 0x3161, 0x3162, 0x3163};
    // {' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'}
    private static final char[] JONGSUNG =
            {0x0000, 0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3139, 0x313a, 0x313b,
                    0x313c, 0x313d, 0x313e, 0x313f, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145, 0x3146, 0x3147,
                    0x3148, 0x314a, 0x314b, 0x314c, 0x314d, 0x314e};
    private static final String[] CHOSUNG_EN = {"r", "R", "s", "e", "E", "f", "a", "q", "Q", "t", "T",
            "d", "w", "W", "c", "z", "x", "v", "g"};
    private static final String[] JUNGSUNG_EN = {"k", "o", "i", "O", "j", "p", "u", "P", "h", "hk",
            "ho", "hl", "y", "n", "nj", "np", "nl", "b", "m", "ml", "l"};
    private static final char CHOSUNG_BEGIN_UNICODE = 12593;
    private static final char CHOSUNG_END_UNICODE = 12622;
    private static final char HANGUEL_BEGIN_UNICODE = 44032;
    private static final char HANGUEL_END_UNICODE = 55203;
    private static final char NUMBER_BEGIN_UNICODE = 48;
    private static final char NUMBER_END_UNICODE = 57;
    private static final char ENGLISH_LOWER_BEGIN_UNICODE = 65;
    private static final char ENGLISH_LOWER_END_UNICODE = 90;
    private static final char ENGLISH_UPPER_BEGIN_UNICODE = 97;
    private static final char ENGLISH_UPPER_END_UNICODE = 122;
    private volatile static HanguelJamoMorphTokenizer hanguelJamoMorphTokenizer;
    private static final String[] JONGSUNG_EN = {"", "r", "R", "rt", "s", "sw", "sg", "e", "f", "fr", "fa",
            "fq", "ft", "fx", "fv", "fg", "a", "q", "qt", "t", "T", "d", "w", "c", "z", "x", "v", "g"};

    private HanguelJamoMorphTokenizer() {
    }

    public static HanguelJamoMorphTokenizer getInstance() {
        if (hanguelJamoMorphTokenizer == null) {
            synchronized (HanguelJamoMorphTokenizer.class) {
                if (hanguelJamoMorphTokenizer == null) {
                    hanguelJamoMorphTokenizer = new HanguelJamoMorphTokenizer();
                }
            }
        }
        return hanguelJamoMorphTokenizer;
    }

    private static boolean isPossibleCharacter(char c) {
        return isBetweenNumberUnicode(c)
                || isBetweenEnglishUpperUnicode(c)
                || isBetweenEnglishLowerUnicode(c)
                || isBetweenHanguelUnicode(c)
                || isBetweenChosungUnicode(c);
    }

    private static boolean isBetweenChosungUnicode(char c) {
        return c >= CHOSUNG_BEGIN_UNICODE && c <= CHOSUNG_END_UNICODE;
    }

    private static boolean isBetweenHanguelUnicode(char c) {
        return c >= HANGUEL_BEGIN_UNICODE && c <= HANGUEL_END_UNICODE;
    }

    private static boolean isBetweenEnglishLowerUnicode(char c) {
        return c >= ENGLISH_LOWER_BEGIN_UNICODE && c <= ENGLISH_LOWER_END_UNICODE;
    }

    private static boolean isBetweenEnglishUpperUnicode(char c) {
        return c >= ENGLISH_UPPER_BEGIN_UNICODE && c <= ENGLISH_UPPER_END_UNICODE;
    }

    private static boolean isBetweenNumberUnicode(char c) {
        return c >= NUMBER_BEGIN_UNICODE && c <= NUMBER_END_UNICODE;
    }

    public String tokenizer(String source, String jamoType) {
        String result = "";

    /*
    [분리 기본 공식]
    초성 = ( ( (글자 - 0xAC00) - (글자 - 0xAC00) % 28 ) ) / 28 ) / 21
    중성 = ( ( (글자 - 0xAC00) - (글자 - 0xAC00) % 28 ) ) / 28 ) % 21
    종성 = (글자 - 0xAC00) % 28
    [합치기 기본 공식]
    원문 = 0xAC00 + 28 * 21 * (초성의 index) + 28 * (중성의 index) + (종성의 index)
    각 index 정보는 CHOSUNG, JUNGSUNG, JONGSUNG char[]에 정의한 index 입니다.
    하지만 아래 코드에서는 원문이 필요 없기 때문에 합치기 위한 로직은 포함 되어 있지 않습니다.
    */

        switch (jamoType) {
            case "chosung":
                result = chosungTokenizer(source);
                break;
            case "jamo":
                result = jamoTokenizer(source);
                break;
            case "kortoeng":
                result = convertKoreanToEnglish(source);
                break;
        }

        return result;
    }

    public String jamoTokenizer(String source) {
        StringBuilder jamo = new StringBuilder();
        int criteria;
        char sourceChar;
        char jamoIdx;

        for (int i = 0; i < source.length(); i++) {
            sourceChar = source.charAt(i);

            if (sourceChar >= 0xAC00) {
                criteria = (sourceChar - 0xAC00);
                jamoIdx = (char) (((criteria - (criteria % 28)) / 28) / 21);
                jamo.append(CHOSUNG[jamoIdx]);

                jamoIdx = (char) (((criteria - (criteria % 28)) / 28) % 21);
                jamo.append(JUNGSUNG[jamoIdx]);

                jamoIdx = (char) ((sourceChar - 0xAC00) % 28);
                // NUL 문자에 대한 제거
                if ((int) jamoIdx != 0) {
                    jamo.append(JONGSUNG[jamoIdx]);
                }
            } else {
                if (isPossibleCharacter(sourceChar)) {
                    jamo.append(sourceChar);
                }
            }
        }

        return jamo.toString();
    }

    public String chosungTokenizer(String source) {
        StringBuilder chosung = new StringBuilder();
        int criteria;
        char sourceChar;
        char choIdx;

        for (int i = 0; i < source.length(); i++) {
            sourceChar = source.charAt(i);

            if (sourceChar >= 0xAC00) {
                criteria = (sourceChar - 0xAC00);
                choIdx = (char) (((criteria - (criteria % 28)) / 28) / 21);

                chosung.append(CHOSUNG[choIdx]);


            } else {
                if (isPossibleCharacter(sourceChar)) {
                    chosung.append(sourceChar);
                }
            }
        }

        return chosung.toString();
    }


    public String convertKoreanToEnglish(String source) {
        StringBuilder english = new StringBuilder();
        char sourceChar;
        int choIdx;
        int jungIdx;
        int jongIdx;
        int criteria;

        for (int i = 0; i < source.length(); i++) {
            sourceChar = source.charAt(i);
            criteria = sourceChar - 0xAC00;
            choIdx = criteria / (21 * 28);
            jungIdx = criteria % (21 * 28) / 28;
            jongIdx = criteria % (21 * 28) % 28;

            if (sourceChar >= 0xAC00) {
                english.append(CHOSUNG_EN[choIdx]).append(JUNGSUNG_EN[jungIdx]);

                if (jongIdx != 0x0000) {
                    english.append(JONGSUNG_EN[jongIdx]);
                }
            } else {
                if (isPossibleCharacter(sourceChar)) {
                    english.append(sourceChar);
                }
            }
        }

        return english.toString();
    }
}
