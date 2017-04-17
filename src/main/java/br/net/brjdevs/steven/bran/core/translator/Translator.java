package br.net.brjdevs.steven.bran.core.translator;

import br.net.brjdevs.steven.bran.core.data.GuildData;
import br.net.brjdevs.steven.bran.core.data.UserData;
import br.net.brjdevs.steven.bran.core.data.managers.DataFileManager;
import br.net.brjdevs.steven.bran.core.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Translator {

    private static final Logger LOGGER = LoggerFactory.getLogger("Translator");

    private static Map<Language, Translator> translator;

    static {
        translator = new HashMap<>();
    }

    public static Translator of(Language language) {
        return translator.computeIfAbsent(language, lang -> {
            try {
                DataFileManager dataFileManager = new DataFileManager("/translations/" + lang.toString() + ".txt");
                Map<String, String> translations = new HashMap<>();
                for (String line : dataFileManager.get()) {
                    int index = line.indexOf(':');
                    translations.put(line.substring(0, index), line.substring(index + 1));
                }
                return new Translator(translations);
            } catch (Exception e) {
                LOGGER.error("Failed to load translations for " + language + ".", e);
                return null;
            }
        });
    }

    public static Translator of(GuildData guildData) {
        return of(guildData.getLanguage());
    }

    public static Translator of(UserData userData, GuildData guildData) {
        return userData.getLanguage() == null ? of(guildData) : of(userData.getLanguage());
    }

    public void update(Language language) {
        StringBuilder sb = new StringBuilder();
        of(language).translations.forEach((key, translation) -> sb.append(key).append(':').append(translation));

    }

    private Map<String, String> translations;

    public Translator(Map<String, String> translations) {
        this.translations = translations;
    }

    public String getLocalized(String key) {
        return translations.getOrDefault(key, key);
    }

    public String getLocalized(String key, Translator defaultTranslator) {
        return translations.getOrDefault(key, defaultTranslator.getLocalized(key));
    }

    public void localize(String key, String message) {
        translations.put(key, message);
    }
}
