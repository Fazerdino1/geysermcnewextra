package fr.crossplaymc.geysermcnewextra.core.locale;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every text shown to players or in the console must come from the language files.
 * This scans the main sources of every module for calls that display text and fails on a
 * string literal containing words that is not a language key.
 */
class NoHardcodedTextTest {
    private static final Path ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final List<String> MODULES = List.of("core", "spigot", "bungee", "velocity", "extension");

    // Calls whose arguments end up on screen or in the console
    private static final Pattern DISPLAY_CALL = Pattern.compile(
            "(?:SERVER\\.(?:log|warn)|\\binfo|\\bwarn|sendMessage|sendRawMessage|sendToast|sendSystemToast|setTitle|setHeader|setContent"
                    + "|new (?:Label|Button|SubSectionLabel|SectionLabel|Toggle|Dropdown|MappedDropdown<[^>]*>|Slider|StepSlider|TextInput))\\(");
    // Calls that produce translated text: what is inside them is a key or a value, not a text
    private static final Pattern TRANSLATING_CALL = Pattern.compile(
            "(?:Lang\\.(?:console|get)|\\blang|langOrElse|\\btranslate|getPlayerLocaleString|getLocaleString)\\(");
    private static final Pattern STRING_LITERAL = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern WORDS = Pattern.compile("[A-Za-z]{2,}");
    // Developer-only prefix of debug traces (debug-mode), not a user-facing text
    private static final Set<String> ALLOWED = Set.of("[DEBUG] ");

    @Test
    void displayedTextsComeFromLanguageFiles() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : mainSources()) {
            String code = stripComments(Files.readString(file));
            Matcher call = DISPLAY_CALL.matcher(code);
            while (call.find()) {
                String args = arguments(code, call.end());
                // Code inside a lambda argument is checked through its own calls
                int lambda = args.indexOf("->");
                args = removeTranslatingCalls(lambda < 0 ? args : args.substring(0, lambda));
                Matcher literal = STRING_LITERAL.matcher(args);
                while (literal.find()) {
                    String text = literal.group(1);
                    if (WORDS.matcher(text).find() && !ALLOWED.contains(text) && !isResourcePath(text)) {
                        violations.add(ROOT.relativize(file) + ":" + line(code, call.start()) + " \"" + text + "\"");
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(), "Hardcoded texts (move them to lang/*.yml):\n" + String.join("\n", violations));
    }

    @Test
    void everyKeyUsedInTheCodeExistsInFrenchAndEnglish() throws IOException {
        Set<String> used = new TreeSet<>();
        Pattern key = Pattern.compile("\"(gmne\\.[a-zA-Z0-9_.]+[a-zA-Z0-9_])\"");
        for (Path file : mainSources()) {
            Matcher matcher = key.matcher(stripComments(Files.readString(file)));
            while (matcher.find()) {
                used.add(matcher.group(1));
            }
        }
        for (String locale : List.of("en_US", "fr_FR")) {
            Map<String, String> texts = Lang.bundled(locale);
            List<String> missing = used.stream().filter(k -> !texts.containsKey(k)).toList();
            assertTrue(missing.isEmpty(), locale + ".yml is missing " + missing);
        }
    }

    private static boolean isResourcePath(String text) {
        return text.startsWith("textures/") || text.matches("[a-z0-9_./-]+\\.(png|json|yml|mcpack|zip)");
    }

    private static List<Path> mainSources() throws IOException {
        List<Path> files = new ArrayList<>();
        for (String module : MODULES) {
            Path sources = ROOT.resolve(module).resolve("src/main/java");
            if (Files.isDirectory(sources)) {
                try (Stream<Path> walk = Files.walk(sources)) {
                    walk.filter(p -> p.toString().endsWith(".java")).forEach(files::add);
                }
            }
        }
        assertTrue(files.size() > 50, "sources not found from " + ROOT);
        return files;
    }

    // Text between the opening parenthesis at 'start' and its matching closing one
    static String arguments(String code, int start) {
        int depth = 1;
        boolean inString = false;
        for (int i = start; i < code.length(); i++) {
            char c = code.charAt(i);
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
            } else if (c == '"') {
                inString = true;
            } else if (c == '(') {
                depth++;
            } else if (c == ')' && --depth == 0) {
                return code.substring(start, i);
            }
        }
        return code.substring(start);
    }

    static String removeTranslatingCalls(String args) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = TRANSLATING_CALL.matcher(args);
        int index = 0;
        while (matcher.find(index)) {
            result.append(args, index, matcher.start());
            String inner = arguments(args, matcher.end());
            index = Math.min(args.length(), matcher.end() + inner.length() + 1);
        }
        result.append(args.substring(Math.min(index, args.length())));
        return result.toString();
    }

    static String stripComments(String code) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (inString) {
                result.append(c);
                if (c == '\\' && i + 1 < code.length()) {
                    result.append(code.charAt(++i));
                } else if (c == '"') {
                    inString = false;
                }
            } else if (c == '"') {
                inString = true;
                result.append(c);
            } else if (code.startsWith("//", i)) {
                while (i < code.length() && code.charAt(i) != '\n') {
                    i++;
                }
                result.append('\n');
            } else if (code.startsWith("/*", i)) {
                int end = code.indexOf("*/", i + 2);
                String comment = code.substring(i, end < 0 ? code.length() : end + 2);
                // Keep line numbers
                comment.chars().filter(ch -> ch == '\n').forEach(ch -> result.append('\n'));
                i = end < 0 ? code.length() : end + 1;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private static int line(String code, int index) {
        return (int) code.substring(0, index).chars().filter(c -> c == '\n').count() + 1;
    }
}
