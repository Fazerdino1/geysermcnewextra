package fr.crossplaymc.geysermcnewextra.core.menus.settings.sections;

import fr.crossplaymc.geysermcnewextra.core.form.elements.Label;
import org.geysermc.geyser.text.ChatColor;

public class SubSectionLabel extends Label {
    public SubSectionLabel(String text, String description) {
        super( ChatColor.BOLD + ChatColor.GRAY + text + "\n" + ChatColor.RESET + ChatColor.ITALIC + ChatColor.DARK_GRAY + description);
    }
    public SubSectionLabel(String text) {
        super( ChatColor.BOLD + ChatColor.GRAY + text + "\n" + ChatColor.RESET + ChatColor.ITALIC);
    }

}
