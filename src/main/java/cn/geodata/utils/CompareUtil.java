package cn.geodata.utils;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.Objects;

/**
 * 比对工具（支持中文/汉字比对）
 *
 * @author JZY
 */
public class CompareUtil {

    private static final String REGEX = "[\u4e00-\u9fa5]";

    /**
     * 判断单个字符是否为汉字
     *
     * @param c 字符
     * @return 是否为汉字
     */
    public static boolean isHanZi(char c) {
        return String.valueOf(c).matches(REGEX);
    }

    /**
     * 比对两个字符（可含汉字）大小
     *
     * @param c1 字符1
     * @param c2 字符2
     * @return 两个字符相差的大小（返回结果大于0表示第一个字符大，返回结果小于0表示第二个字符大，返回结果等于0表示两个字符一样大）
     *          （针对两个读音和音调相同的中文/汉字字符，再次进行常规的编码大小比对）
     */
    public static int compareCharWithHanZi(char c1, char c2) {
        boolean b1 = isHanZi(c1);
        boolean b2 = isHanZi(c2);
        if (b1 && b2) {
            int result = Objects.requireNonNull(getFirstStringFromHanyuPinyinStringArray(c1)).compareTo(Objects.requireNonNull(getFirstStringFromHanyuPinyinStringArray(c2)));
            return result != 0 ? result : Character.compare(c1, c2);
        } else {
            return Character.compare(c1, c2);
        }
    }

    /**
     * 判断两个字符串（可含汉字）大小
     *
     * @param string1 字符串1
     * @param string2 字符串2
     * @return 两个字符串大小的比对结果（返回1表示第一个字符串大，返回-1表示第二个字符串大，返回0表示两个字符串一样大）
     */
    public static int compareStringWithHanZi(String string1, String string2) {
        char[] charArray1 = string1.toCharArray();
        char[] charArray2 = string2.toCharArray();
        int length1 = charArray1.length;
        int length2 = charArray2.length;
        int limit = Math.min(length1, length2);
        int result;
        for (int i = 0; i < limit; i++) {
            result = compareCharWithHanZi(charArray1[i], charArray2[i]);
            if (result != 0) {
                return result > 0 ? 1 : -1;
            }
        }
        if (length1 == length2) {
            return 0;
        }
        return length1 > length2 ? 1 : -1;
    }

    /**
     * 获取单个字符的第一个汉语拼音
     *
     * @param c 字符
     * @return 汉字字符的第一个汉语拼音
     */
    public static String getFirstStringFromHanyuPinyinStringArray(char c) {
        String[] strings = PinyinHelper.toHanyuPinyinStringArray(c);
        if (strings != null) {
            return strings[0];
        } else {
            return null;
        }
    }
}
