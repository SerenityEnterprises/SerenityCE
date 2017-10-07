package host.serenity.serenity.modules.miscellaneous;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.player.SendChat;
import host.serenity.synapse.Listener;

import java.util.HashMap;
import java.util.Map;

public class UnicodeChat extends Module {
    public UnicodeChat() {
        super("Unicode Chat", 0xBBE0FF, ModuleCategory.MISCELLANEOUS);

        listeners.add(new Listener<SendChat>() {
            @Override
            public void call(SendChat event) {
                if (!event.getMessage().startsWith("/")) {
                    event.setMessage(substitute(event.getMessage().toLowerCase()));
                }
            }
        });
    }

    private static Map<Character, String> characterMap = new HashMap<>(); {
        characterMap.put('a', "а");
        characterMap.put('b', "ら");
        characterMap.put('c', "ㄷ");
        characterMap.put('d', "ᑺ");
        characterMap.put('e', "ㅌ");
        characterMap.put('f', "ｆ");
        characterMap.put('g', "ｇ");
        characterMap.put('h', "һ");
        characterMap.put('i', "ｉ");
        characterMap.put('j', "ｊ");
        characterMap.put('k', "к");
        characterMap.put('l', "ℓ");
        characterMap.put('m', "ｍ");
        characterMap.put('n', "ｎ");
        characterMap.put('o', "ㅇ");
        characterMap.put('p', "р");
        characterMap.put('q', "ｑ");
        characterMap.put('r', "г");
        characterMap.put('s', "ѕ");
        characterMap.put('t', "ヒ");
        characterMap.put('u', "ひ");
        characterMap.put('v', "ⅴ");
        characterMap.put('w', "Ѡ");
        characterMap.put('x', "Х");
        characterMap.put('y', "У");
        characterMap.put('z', "ｚ");
    }

    private static String substitute(String original) {
        StringBuilder builder = new StringBuilder(original.length());
        for (char c : original.toCharArray()) {
            builder.append(characterMap.getOrDefault(c, String.valueOf(c)));
        }
        return builder.toString();
    }
}
