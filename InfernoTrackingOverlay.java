package net.runelite.client.plugins.betterhptracking;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.runelite.api.Perspective.LOCAL_TILE_SIZE;

public class InfernoTrackingOverlay extends Overlay {

    private final InfernoTrackingPlugin plugin;
    private final InfernoTrackingConfig config;
    private final Client client;

    @Inject
    private InfernoTrackingOverlay(final Client client, final InfernoTrackingPlugin plugin,
                                   final InfernoTrackingConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGHEST);

    }

    @Override
    public Dimension render(Graphics2D graphics) {

        ArrayList<TNPC> npcs = plugin.getNpcs();

        Color c = new Color(000, 000, 000, 5);
        Polygon p = new Polygon(new int[] {-1000, -1000, 1000, 1000}, new int[] {-1000, 1000, 1000, -1000}, 4);

        //OverlayUtil.renderPolygon(graphics, p, c);

        for (TNPC npc : npcs) {

            //drawHP(graphics, npc);
        }

        return null;
    }

    private void drawHP(Graphics2D graphics, TNPC npc) {

        if (config.showHp()) {
            if (npc.isDead() || npc.getNPC().isDead()) {
                OverlayUtil.renderActorOverlay(graphics, npc.getNPC(), "X", config.deadColor());
            } else {
                OverlayUtil.renderActorOverlay(graphics, npc.getNPC(), "" + npc.getHP()/* + npc.getHP() + "/" + npc.getMaxHp()*/, config.aliveColor());
            }
        }
    }
}
