package com.lovelab.content;

import com.lovelab.common.ApiException;
import java.util.Locale;

public enum ContentKind {
    STORY, CARD, TUTORIAL, QUESTION;
    public static ContentKind parse(String value) {
        try { return valueOf(value.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException error) { throw ApiException.bad("kind须为STORY、CARD、TUTORIAL或QUESTION"); }
    }
}
