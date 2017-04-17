package br.net.brjdevs.steven.bran.core.translator;

import net.dv8tion.jda.core.Region;

import java.util.stream.Stream;

public enum Language {
    pt_BR(0), en_US(1);
    int code;

    Language(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Language of(int code) {
        return Stream.of(values()).filter(language -> language.code == code).findFirst().orElse(null);
    }

    public static Language of(Region region) {
        switch (region) {
            case BRAZIL:
                return pt_BR;
            default:
                return en_US;
        }
    }
}
