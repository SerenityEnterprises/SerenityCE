package host.serenity.serenity.api.friend;

import com.google.common.collect.ImmutableMap;
import host.serenity.serenity.util.ChatColor;

import java.util.*;

public class FriendManager {
    private Map<String, String> friends = new HashMap<>();

    public void addFriend(String friend, String alias) {
        friends.put(friend, alias);
    }

    public void addFriend(String friend) {
        addFriend(friend, friend);
    }

    public String getAliasForFriend(String friend) {
        return friends.get(friend);
    }

    public boolean hasAlias(String friend) {
        return !getAliasForFriend(friend).equals(friend);
    }

    public boolean isFriend(String friend) {
        return friends.containsKey(friend);
    }

    public void delFriend(String friend) {
        ImmutableMap.copyOf(friends).entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(friend) || entry.getValue().equalsIgnoreCase(friend))
                .forEach(entry -> friends.remove(entry.getKey()));
    }

    public Map<String, String> getFriends() {
        return friends;
    }

    public String applyProtection(String orig) {
        String str = orig;
        for (Map.Entry<String, String> protection : this.friends.entrySet()) {
            if (protection.getKey().equals(protection.getValue()))
                continue;

            if (str.toLowerCase().contains(protection.getKey().toLowerCase())) {
                ChatColor lastCol = null;
                try {
                    List<String> codes = new ArrayList<>(Arrays.asList(ChatColor.getLastColors(str.substring(0, str.toLowerCase().indexOf(protection.getKey().toLowerCase()))).split(String.valueOf(ChatColor.COLOR_CHAR))));
                    lastCol = ChatColor.getByChar(codes.get(codes.size() - 1).charAt(0));
                } catch (Exception e) {}
                if (lastCol == null) {
                    lastCol = ChatColor.RESET;
                }

                str = replaceCaseInsensitive(str, protection.getKey(), ChatColor.DARK_AQUA + protection.getValue() + lastCol);
            }
        }
        return str;
    }

    private String replaceCaseInsensitive(String source, String target, String replacement) {
        StringBuilder sbSource = new StringBuilder(source);
        StringBuilder sbSourceLower = new StringBuilder(source.toLowerCase());
        String searchString = target.toLowerCase();

        int idx = 0;
        while ((idx = sbSourceLower.indexOf(searchString, idx)) != -1) {
            sbSource.replace(idx, idx + searchString.length(), replacement);
            sbSourceLower.replace(idx, idx + searchString.length(), replacement);
            idx += replacement.length();
        }
        sbSourceLower.setLength(0);
        sbSourceLower.trimToSize();

        return sbSource.toString();
    }
}
