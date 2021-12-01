package net.runelite.client.plugins.betterhptracking;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("InfernoTracking")
public interface InfernoTrackingConfig extends Config
{
    @ConfigItem(
            keyName = "showHp",
            name = "Draw NPC HP",
            description = "Draw HP over head and a red X on 0 hp"
    )
    default boolean showHp()
    {
        return true;
    }

    @ConfigItem(
            keyName = "aliveColor",
            name = "Alive color",
            description = "Color of box when npc is alive"
    ) default Color aliveColor() {return Color.PINK;}

    @ConfigItem(
            keyName = "deadColor",
            name = "Dead color",
            description = "Color of box when npc is dead"
    ) default Color deadColor() {return Color.RED;}
}