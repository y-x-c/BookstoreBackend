package YuxinBookstore;

/**
 * Created by Orthocenter on 5/19/15.
 */
public class Formatter {

    private static boolean isChinese(char c) {
        if(c == '•' || c == '—') return false;
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    public static String format(final String str, final Integer maxSize, String manner) {
        if(str == null || str.length() == 0) {
            StringBuffer buffer = new StringBuffer("");
            for(int i = 0; i < maxSize; i++) buffer.append(' ');
            return buffer.toString();
        }

        if(maxSize == 0) return str;

        String _str = str.trim();
        int realSize = 0;
        for(int i = 0; i < _str.length(); i++) {
            char ch = _str.charAt(i);
            realSize += (isChinese(ch) ? 1 : 0) + 1;
        }

        if(realSize == maxSize) {
            return _str;
        } else if(realSize > maxSize) {
            int pos = 0, size = 0;
            for(; pos < _str.length(); pos++) {
                int fillUp = (isChinese(str.charAt(pos)) ? 1 : 0);
                if(size + fillUp + 1 > maxSize - 2) {
                    break;
                }
                size += fillUp + 1;
            }

            StringBuffer buffer = new StringBuffer(_str.substring(0, pos));
            for(int i = 0; i < maxSize - size; i++) {
                buffer.append('.');
            }
            return buffer.toString();
        } else {
            StringBuffer buffer = new StringBuffer(_str);
            if(manner.equals("l")) {
                for(int i = 0; i < maxSize - realSize; i++) {
                    buffer.append(' ');
                }
                return buffer.toString();
            } else if (manner.equals("r")) {
                for(int i = 0; i < maxSize - realSize; i++) {
                    buffer.insert(0, ' ');
                }
                return buffer.toString();
            } else if (manner.equals("c")) {
                int sl = (maxSize - realSize) / 2;
                int sr = (maxSize - realSize) - sl;

                for(int i = 0; i < sl; i++) buffer.insert(0, ' ');
                for(int i = 0; i < sr; i++) buffer.append(' ');
                return buffer.toString();
            }
        }

        return null;
    }
}
