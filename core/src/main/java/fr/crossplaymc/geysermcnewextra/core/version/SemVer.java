package fr.crossplaymc.geysermcnewextra.core.version;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Semantic Versioning 2.0.0 version, compared by precedence (build metadata ignored).
 */
public record SemVer(int major, int minor, int patch, List<String> preRelease) implements Comparable<SemVer> {
    private static final Pattern PATTERN = Pattern.compile(
            "^v?(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-([0-9A-Za-z.-]+))?(?:\\+[0-9A-Za-z.-]+)?$");

    /**
     * @return the version, or null when {@code text} (optionally prefixed with "v") is not SemVer
     */
    @Nullable
    public static SemVer parse(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(text.trim());
        if (!matcher.matches()) {
            return null;
        }
        List<String> preRelease = matcher.group(4) == null ? List.of() : List.of(matcher.group(4).split("\\."));
        return new SemVer(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)), new ArrayList<>(preRelease));
    }

    public boolean isPreRelease() {
        return !preRelease.isEmpty();
    }

    @Override
    public int compareTo(SemVer other) {
        int result = Integer.compare(major, other.major);
        if (result == 0) {
            result = Integer.compare(minor, other.minor);
        }
        if (result == 0) {
            result = Integer.compare(patch, other.patch);
        }
        if (result != 0) {
            return result;
        }
        // A version without pre-release identifiers is higher than one with them
        if (preRelease.isEmpty() || other.preRelease.isEmpty()) {
            return Boolean.compare(preRelease.isEmpty(), other.preRelease.isEmpty());
        }
        for (int i = 0; i < Math.min(preRelease.size(), other.preRelease.size()); i++) {
            result = compareIdentifier(preRelease.get(i), other.preRelease.get(i));
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(preRelease.size(), other.preRelease.size());
    }

    private static int compareIdentifier(String a, String b) {
        boolean aNumeric = a.chars().allMatch(Character::isDigit);
        boolean bNumeric = b.chars().allMatch(Character::isDigit);
        if (aNumeric && bNumeric) {
            return new java.math.BigInteger(a).compareTo(new java.math.BigInteger(b));
        }
        if (aNumeric != bNumeric) {
            // Numeric identifiers have lower precedence than alphanumeric ones
            return aNumeric ? -1 : 1;
        }
        return a.compareTo(b);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (preRelease.isEmpty() ? "" : "-" + String.join(".", preRelease));
    }
}
