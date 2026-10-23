package dev.letsgoaway.geyserextras.core.preferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.geysermc.geyser.util.CooldownUtils.CooldownType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CooldownTypeAdapterTest {
    private static class Prefs {
        CooldownType cooldownType;
    }

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CooldownType.class, new CooldownTypeAdapter())
            .create();

    private CooldownType read(String json) {
        return gson.fromJson(json, Prefs.class).cooldownType;
    }

    @Test
    void migratesPre211Names() {
        assertEquals(CooldownType.CROSSHAIR, read("{\"cooldownType\":\"TITLE\"}"));
        assertEquals(CooldownType.HOTBAR, read("{\"cooldownType\":\"ACTIONBAR\"}"));
    }

    @Test
    void keepsCurrentNames() {
        for (CooldownType type : CooldownType.values()) {
            assertEquals(type, read("{\"cooldownType\":\"" + type.name() + "\"}"));
        }
    }

    @Test
    void unknownOrNullFallsBack() {
        assertEquals(CooldownTypeAdapter.FALLBACK, read("{\"cooldownType\":\"SOMETHING_ELSE\"}"));
        assertEquals(CooldownTypeAdapter.FALLBACK, read("{\"cooldownType\":null}"));
    }

    @Test
    void writesEnumName() {
        Prefs prefs = new Prefs();
        prefs.cooldownType = CooldownType.HOTBAR;
        assertEquals("{\"cooldownType\":\"HOTBAR\"}", gson.toJson(prefs));
    }
}
