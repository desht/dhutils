package me.desht.dhutils;

import java.util.Arrays;

import org.bukkit.entity.Player;

public class ExperienceManager {
    public static final int MAX_LEVEL_SUPPORTED = 150;

    private static final int xpRequiredForNextLevel[] = new int[MAX_LEVEL_SUPPORTED];
    private static final int xpTotalToReachLevel[] = new int[MAX_LEVEL_SUPPORTED];

    private final Player player;
    
    // Initialise the xp lookup table.  Basing this on observations noted in https://bukkit.atlassian.net/browse/BUKKIT-47
    // 7 xp to get to level 1, 17 to level 2, 31 to level 3...
    // At each level, the increment to get to the next level increases alternately by 3 and 4
    static {
            xpTotalToReachLevel[0] = 0;
            int incr = 7;
            for (int i = 1; i < xpTotalToReachLevel.length; i++) {
                    xpRequiredForNextLevel[i - 1] = incr;
                    xpTotalToReachLevel[i] = xpTotalToReachLevel[i - 1] + incr;
                    incr += (i % 2 == 0) ? 4 : 3;
            }
    }
   
    /**
     * Create a new ExperienceManager for the given player.
     * 
     * @param p
     */
    public ExperienceManager(Player player) {
    	this.player = player;
    }
    
    /**
     * Adjust the player's XP by the given amount in an intelligent fashion.  Works around
     * some of the non-intuitive behaviour of the basic Bukkit player.giveExp() method
     * 
     * @param player	The player to award XP to
     * @param amt		Amount of XP, may be negative
     */
    public void changeExp(int amt) {
            int xp = getCurrentExp() + amt;
            if (xp < 0) xp = 0;
           
            int curLvl = player.getLevel();
            int newLvl = getLevelForExp(xp); 
            if (curLvl != newLvl) {
                    player.setLevel(newLvl);
            }
           
            float pct = ((float)(xp - xpTotalToReachLevel[newLvl]) / (float)xpRequiredForNextLevel[newLvl]);
            player.setExp(pct);
    }
   
    /**
     * Get the player's current XP total.
     * 
     * @return	the player's total XP
     */
    public int getCurrentExp() {
            int lvl = player.getLevel();
            return xpTotalToReachLevel[lvl] + (int) (xpRequiredForNextLevel[lvl] * player.getExp());
    }
   
    /**
     * Checks if the player has the given amount of XP.
     * 
     * @param amt	The amount to check for.
     * @return		true if the player has enough XP, false otherwise
     */
    public boolean hasExp(int amt) {
            return getCurrentExp() >= amt;
    }
   
    /**
     * Get the level that the given amount of XP falls within.
     * 
     * @param exp	The amount to check for.
     * @return		The level that a player with this amount total XP would be.
     */
    public static int getLevelForExp(int exp) {
            if (exp <= 0) return 0;
            int pos = Arrays.binarySearch(xpTotalToReachLevel, exp);
            return pos < 0 ? -pos - 2 : pos;
    }
}

