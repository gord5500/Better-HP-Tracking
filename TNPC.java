package net.runelite.client.plugins.betterhptracking;

import net.runelite.api.NPC;

//Tracked NPC
/*
We are going to keep track of the NPCs HP based on hitsplats. The reasoning
is the server does not send an exact value for the hp bar. We see issues
though for NPCs with high hp due to regen. We could guess after a minute and say
add +1 hp but the main idea is for nibblers and miniblobs. Also won't work for
the mager respawning NPCs. We could get around that but it's not worth the time
 */
public class TNPC implements Comparable<TNPC> {

    // Current hp
    private int hp;
    // Their max hhp
    private int maxHp;
    // The npc
    private NPC npc;
    private boolean dead = false;

    private int spawnTick = 0;
    private int deathTick = 0;

    // Constructor
    public TNPC(int hp, NPC npc, int tick) {

        this.maxHp = hp;
        this.hp = hp;
        this.npc = npc;
        this.spawnTick = tick;
    }

    public int getSpawnTick() {
        return spawnTick;
    }

    public void setSpawnTick(int tick) {
        this.spawnTick = tick;
    }

    // Say the npc should be dead
    public void setDead(boolean val) {
        dead = val;
    }

    // Is it dead?
    public boolean isDead() {
        return dead;
    }

    // Starting hp
    public int getMaxHp() {
        return maxHp;
    }

    // Could be used for mager respawn
    public void setHP(int val) {
        hp = val;
    }

    // Current hhp
    public int getHP() {
        return hp;
    }

    // Get npc
    public NPC getNPC() {
        return npc;
    }

    // Decrease hp
    public void decHP(int val) {
        hp = hp - val;
    }

    public void incHP(int val) {
        hp = hp + val;
    }

    @Override
    public int compareTo(TNPC o) {
        if (this.npc.equals(o.getNPC())) {
            return 0;
        } else return 1;
    }
}
