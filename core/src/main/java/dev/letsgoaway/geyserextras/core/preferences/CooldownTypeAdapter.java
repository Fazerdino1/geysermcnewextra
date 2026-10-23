package dev.letsgoaway.geyserextras.core.preferences;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.geysermc.geyser.util.CooldownUtils.CooldownType;

import java.io.IOException;

/**
 * Reads cooldown preferences saved by older versions.
 * Geyser 2.11 renamed TITLE to CROSSHAIR and ACTIONBAR to HOTBAR; plain Gson would turn
 * those saved values into null, which Geyser's cooldown code does not expect.
 */
public class CooldownTypeAdapter extends TypeAdapter<CooldownType> {
    public static final CooldownType FALLBACK = CooldownType.CROSSHAIR;

    @Override
    public void write(JsonWriter out, CooldownType value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.name());
        }
    }

    @Override
    public CooldownType read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return FALLBACK;
        }
        return fromName(in.nextString());
    }

    public static CooldownType fromName(String name) {
        switch (name) {
            case "TITLE" -> {
                return CooldownType.CROSSHAIR;
            }
            case "ACTIONBAR" -> {
                return CooldownType.HOTBAR;
            }
        }
        for (CooldownType type : CooldownType.VALUES) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return FALLBACK;
    }
}
