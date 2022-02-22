package io.goodforgod.slf4j.simplelogger;

/**
 * Allow abbreviating class name with length 36
 * from "io.goodforgod.slf4j.simplelogger.ClassNameAbbreviator"
 * into "i.g.slf4j.s.ClassNameAbbreviator"
 *
 * @author Anton Kurako (GoodforGod)
 * @since 21.02.2022
 */
final class ClassNameAbbreviator {

    private ClassNameAbbreviator() {}

    static String abbreviate(String className, int targetLength) {
        if (className == null) {
            throw new IllegalArgumentException("Class name may not be null");
        }

        int inLen = className.length();
        if (inLen < targetLength) {
            return className;
        }

        final StringBuilder builder = new StringBuilder(inLen);
        final int rightMostDotIndex = className.lastIndexOf('.');
        if (rightMostDotIndex == -1) {
            return className;
        }

        // length of last segment including the dot
        int lastSegmentLength = inLen - rightMostDotIndex;

        int leftSegmentsTargetLen = targetLength - lastSegmentLength;
        if (leftSegmentsTargetLen < 0)
            leftSegmentsTargetLen = 0;

        int leftSegmentsLen = inLen - lastSegmentLength;

        // maxPossibleTrim denotes the maximum number of characters we aim to trim
        // the actual number of character trimmed may be higher since segments, when
        // reduced, are reduced to just one character
        int maxPossibleTrim = leftSegmentsLen - leftSegmentsTargetLen;

        int trimmed = 0;
        boolean inDotState = true;

        int i = 0;
        for (; i < rightMostDotIndex; i++) {
            char c = className.charAt(i);
            if (c == '.') {
                // if trimmed too many characters, let us stop
                if (trimmed >= maxPossibleTrim) {
                    break;
                }

                builder.append(c);
                inDotState = true;
            } else {
                if (inDotState) {
                    builder.append(c);
                    inDotState = false;
                } else {
                    trimmed++;
                }
            }
        }

        // append from the position of i which may include the last seen DOT
        builder.append(className.substring(i));
        return builder.toString();
    }
}
