package net.runelite.client.plugins.betterhptracking;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.runelite.api.MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET;

@Slf4j
@PluginDescriptor(
        name = "InfernoTrackingPlugin"
)
public class InfernoTrackingPlugin extends Plugin
{

    // No clue what this does I think I copied it from NPC indicators plugin
    private static final Set<MenuAction> NPC_MENU_ACTIONS = ImmutableSet.of(MenuAction.NPC_FIRST_OPTION, MenuAction.NPC_SECOND_OPTION,
            MenuAction.NPC_THIRD_OPTION, MenuAction.NPC_FOURTH_OPTION, MenuAction.NPC_FIFTH_OPTION, MenuAction.SPELL_CAST_ON_NPC,
            MenuAction.ITEM_USE_ON_NPC);

    // Old range xp
    private int lastXp = 0;
    // Current range xp
    private int currXp = 0;

    private ArrayList<Integer> identifiers = new ArrayList<Integer>();
    private final int[] HP_VALUES = {10, 25, 15, 40, 75, 125, 220};
    // Keeps track of alive npcs
    private ArrayList<TNPC> npcs = new ArrayList<TNPC>();

    // Are we on final wave
    private boolean zuk = false;

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private InfernoTrackingConfig config;

    @Inject
    private InfernoTrackingOverlay overlay;

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
    }

    // Get current npcs
    public ArrayList<TNPC> getNpcs() {
        return npcs;
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {

        // Hard coding npc health based on their combat level
        // This makes it useless for npcs outside of inferno or incorrect
        // The client can look the actual value up but no point in changing
        NPC npc = npcSpawned.getNpc();
        npc.setModelHeight(-255);
        int combatLevel = npc.getCombatLevel();
        int hp = 10;

        int tick = client.getTickCount();

        switch (combatLevel) {
            case 32:
                hp = 10;
                break;
            case 85:
                hp = 25;
                break;
            case 70:
                hp = 15;
                break;
            case 165:
                hp = 40;
                break;
            case 240:
                hp = 75;
                break;
            case 370:
                hp = 125;
                break;
            case 490:
                hp = 220;
                break;
            default:
                hp = 0;
                break;
        }

        if (hp != 0) {

            npcs.add(new TNPC(hp, npc, tick));
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
        // Check who took a hit and decrement their hp on overlay
        Actor actor = hitsplatApplied.getActor();
        if (actor != null && actor.getName() != null && actor.getName().contains("-")) {
            NPC splatNpc = (NPC) actor;
            for (int i = 0; i < npcs.size(); i++) {

                if (npcs.get(i).getNPC().equals(splatNpc)) {
                    npcs.get(i).decHP(hitsplatApplied.getHitsplat().getAmount());

                    if (npcs.get(i).getHP() <= 0) {
                        npcs.get(i).setDead(true);
                        // Use to remove but the problem is with hp regen
                        // If we take it out of the array, the attack option
                        // gets removed so it's not worth risking it
                        //npcs.remove(i);
                    }
                }
            }
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        int type = event.getType();

        if (type >= MENU_ACTION_DEPRIORITIZE_OFFSET)
        {
            type -= MENU_ACTION_DEPRIORITIZE_OFFSET;
        }
        final MenuAction menuAction = MenuAction.of(type);
        if (NPC_MENU_ACTIONS.contains(menuAction)) {

            MenuEntry[] menuEntries = client.getMenuEntries();
            ArrayList<MenuEntry> newEntries = new ArrayList<MenuEntry>();

            // We go through all our menu actions and remove the ones we dont want
            for (MenuEntry entry : menuEntries) {

                if (entry.getOption().equals("Walk here")) {
                    entry.setForceLeftClick(false);
                    newEntries.add(entry);
                }

                if (!entry.getOption().equals("Examine") && !entry.getOption().equals("Walk here")) {
                    int i = 0;
                    boolean found = false;

                    while (i < npcs.size() && !found) {

                        TNPC npc = npcs.get(i);
                        int entryID = entry.getIdentifier();
                        NPC menuNPC = client.getCachedNPCs()[entryID];

                        if (menuNPC != null) {
                            String name = menuNPC.getName();

                            /*

                            Need two separate conditions because for nibblers and blob we
                            remove the entry the same tick we see an xp drop that is large enough
                            to kill them. This could be better written to have a failsafe for
                            the chance they do regen hp before you can kill them. Kind of doubt it
                            would happen often since they're so low hp and this is meant for normal runs
                             */
                            if (npc.getNPC().equals(menuNPC) && (name.contains("Nib") || name.contains("Ket"))) {

                                if (!npc.isDead() && !menuNPC.isDead()) {
                                    newEntries.add(entry);
                                }
                                found = true;
                            } else if (npc.getNPC().equals(menuNPC)) {
                                if (!menuNPC.isDead()) {
                                    newEntries.add(entry);
                                }
                                found = true;
                            }
                        }
                        i++;
                    }

                    if (!found) {

                        newEntries.add(entry);
                    }
                }
            }

            /*
            Create a new list of entries to shohw
             */
            MenuEntry[] toAdd = new MenuEntry[newEntries.size()];
            for (int k = 0; k < newEntries.size(); k++) {
                toAdd[k] = newEntries.get(k);
            }
            client.setMenuEntries(toAdd);
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {

        if (event.getGameObject().getId() == 1326) {

            client.getScene().removeGameObject(event.getGameObject());
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) {

        // Update our xp
        currXp = client.getSkillExperience(Skill.RANGED);
        int currentTick = client.getTickCount();

        // Loop through npcs
        for (int i = 0; i < npcs.size(); i++) {

            TNPC tnpc = npcs.get(i);
            int spawnTick = tnpc.getSpawnTick();

            // Here we are guessing the npc might have regained hp since it died
            // We shouldn't count because I'm assuming they don't have a timer
            // ticking down to regain hp when it's full.
            // Have to see what happens now with our overlay and if it's too inaccurate

            if (currentTick - spawnTick >= 100) {

                if (tnpc.getHP() != tnpc.getMaxHp()) {
                    tnpc.incHP(1);
                    tnpc.setSpawnTick(currentTick);
                }
            }
        }


        Actor target = client.getLocalPlayer().getInteracting();
        /*
        Look at who we're attacking this game tick. Estimate our damage value and compare it
        to NPCs health. If it's greater or equal to their hp, we remove their menu option.
        This means it's 1 tick faster than low ram for nibblers and blobs. Also a small
        failsafe that we check to see if our hit will do more than current hp. The problem comes
        up though if we think NPC has 10 hp and we estimate a hit of 10, but NPC regained 1 hp
        so total is now 11. We still think it's died even though 1 hp left. If we hit 11 it's not
        a problem.

        Maybe to fix this we check the next tick going through all our NPCs, if it's actually
        alive we remove the dead indicator.
         */
        if (target instanceof NPC) {

            int weaponId = client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON);
            String weaponName = client.getItemDefinition(weaponId).getName();
            double xpDiff = currXp - lastXp;
            NPC targetNpc = (NPC) target;


            if (weaponName.equals("Toxic blowpipe") || weaponName.equals("Twisted bow")
            || weaponName.contains("agic")) {
                for (int i = 0; i < npcs.size(); i++) {

                    if (npcs.get(i).getNPC().equals(targetNpc)) {

                        double hit = Math.ceil(xpDiff / 4.0);
                        log.debug("We are estimating a hit of " + hit + " and npc has " + npcs.get(i).getHP());
                        if (hit >= npcs.get(i).getHP()) {

                            log.debug("He's dead. Do we see anything happen?");
                            npcs.get(i).setDead(true);
                        }

                    }
                }
            }
        }
        lastXp = currXp;
    }

    // Remove any dead npcs
    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC deadNpc = npcDespawned.getNpc();

        for (int i = 0; i < npcs.size(); i++) {
            if (npcs.get(i).getNPC().equals(deadNpc)) {
                npcs.remove(i);
            }
        }
    }

    // Don't think this does anything. I couldn't get it to work like open
    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged)
    {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
        {
            client.setIsHidingEntities(true);
            lastXp = client.getSkillExperience(Skill.RANGED);
        } else if (gameStateChanged.getGameState() != GameState.LOGGED_IN) {

            npcs.clear();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage message) {

        String text = message.getMessage();
        if (text.contains("Wave 69")) {
            zuk = true;
        } else {
            zuk = false;
        }
    }

    // Dunno some runelite shit
    @Provides
    InfernoTrackingConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(InfernoTrackingConfig.class);
    }
}
