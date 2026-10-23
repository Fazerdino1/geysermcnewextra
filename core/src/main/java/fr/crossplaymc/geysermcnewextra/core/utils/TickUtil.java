package fr.crossplaymc.geysermcnewextra.core.utils;

import fr.crossplaymc.geysermcnewextra.core.ExtrasPlayer;

public interface TickUtil {
    void runIn(long ticks, Runnable func, ExtrasPlayer player);

    void runSync(Runnable func, ExtrasPlayer player);
}
