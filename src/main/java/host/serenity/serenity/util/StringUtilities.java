package host.serenity.serenity.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilities {
    public static String join(String[] strings, String delimiter) {
        StringBuilder str = new StringBuilder();
        for (String string : strings) {
            str.append(string);
            str.append(delimiter);
        }

        return str.substring(0, str.length() - delimiter.length());
    }

    public static String[] splitExceptingQuotes(String string, boolean stripQuotes) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(string);
        while (m.find())
            list.add(stripQuotes ? m.group(1).replace("\"", "") : m.group(1));

        return list.toArray(new String[list.size()]);
    }
}