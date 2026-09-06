package com.seniors.justlevelingfork.common.feat;

public final class FeatChoiceCodec {

    private FeatChoiceCodec() {
    }

    /**
     * Reads one named choice from a compound feat-choice string.
     *
     * Example:
     *
     * ability=strength;weapons=a,b,c,d
     *
     * extract(..., "ability") -> strength
     *
     * Legacy choices such as:
     *
     * strength
     * strength:1,dexterity:1
     *
     * are returned unchanged so existing feats remain
     * backwards compatible.
     */
    public static String extract(
            String choice,
            String key) {

        if (choice == null
                || choice.isBlank()) {

            return "";
        }

        String trimmed =
                choice.trim();

        /*
         * Existing choice format.
         */
        if (!trimmed.contains("=")) {
            return trimmed;
        }

        if (key == null
                || key.isBlank()) {

            return "";
        }

        String[] segments =
                trimmed.split(";");

        for (String segment : segments) {

            if (segment == null
                    || segment.isBlank()) {

                continue;
            }

            int separator =
                    segment.indexOf('=');

            if (separator <= 0) {
                continue;
            }

            String segmentKey =
                    segment.substring(
                                    0,
                                    separator)
                            .trim();

            if (!segmentKey
                    .equalsIgnoreCase(key)) {

                continue;
            }

            return segment.substring(
                            separator + 1)
                    .trim();
        }

        return "";
    }
}
