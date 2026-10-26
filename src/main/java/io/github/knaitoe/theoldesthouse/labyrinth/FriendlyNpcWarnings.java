package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
/** Per-stage variation without repeating that stage's previous warning on the next visit. */
public final class FriendlyNpcWarnings {
    private static final String[][] POOLS={
        {"Do you really want to do this?","Take a moment.","There is no need for that.","You can still put it away.","Think about what you're doing."},
        {"You were given a chance to stop.","That was noticed.","You are making a choice.","You can leave this alone.","They have done nothing to you."},
        {"One more, then.","You know what comes next.","This is the last time.","Put it down. Now.","There will not be another warning."}};
    private FriendlyNpcWarnings(){}
    public static void enter(CompoundTag own){own.remove("FriendlyStrikes127");own.remove("FriendlyStrikeAt127");}
    public static String pick(ServerPlayer p,CompoundTag own,int stage){int row=Math.max(0,Math.min(2,stage-1));String key="FriendlyWarningLast130_"+row;var pool=POOLS[row];int last=own.contains(key)?own.getInt(key):-1;int next=p.getRandom().nextInt(last<0?pool.length:pool.length-1);if(last>=0&&next>=last)next++;own.putInt(key,next);return pool[next];}
}
