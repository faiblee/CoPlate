package com.faible.coplate.util;

import androidx.annotation.Nullable;

/**
 * Сравнивает идентификаторы пользователя из prefs и backend (строка / число).
 */
public final class UserIdEquality {

    private UserIdEquality() {}

    public static boolean same(@Nullable String a, @Nullable String b) {
        if (a == null || b == null) {
            return false;
        }
        String x = normalize(a);
        String y = normalize(b);
        if (x.isEmpty() || y.isEmpty()) {
            return false;
        }
        if (x.equals(y)) {
            return true;
        }
        Long nx = parseLongQuiet(x);
        Long ny = parseLongQuiet(y);
        return nx != null && nx.equals(ny);
    }

    private static String normalize(String s) {
        return s.trim();
    }

    private static Long parseLongQuiet(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
