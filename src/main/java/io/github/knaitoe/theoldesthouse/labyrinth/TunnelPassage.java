package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * The tunnel's length (0.4.134). New builds are built long ({@link #fresh}); 0.4.201 removed the refit that lengthened older,
 * shorter tunnels, which the mod no longer carries (new builds only).
 */
public final class TunnelPassage {
    public static final String STATE="tunnel_passage134";
    public static final int WALL=-145,PASS=-138;
    public static final BlockPos ACCOUNT=new BlockPos(3,1,-141),OLD_ACCOUNT=new BlockPos(3,1,-69);
    private TunnelPassage(){}
    public static boolean extended(ServerLevel l,BlockPos b){return LabyrinthData.get(l.getServer()).stateEntry(STATE,Long.toString(b.asLong())).getBoolean("Done");}
    public static void fresh(ServerLevel l,BlockPos b){BuildBlocks.after(l,()->{var d=LabyrinthData.get(l.getServer());var t=d.stateEntry(STATE,Long.toString(b.asLong()));t.putBoolean("Done",true);d.setStateEntry(STATE,Long.toString(b.asLong()),t);});}
    public static int pass(ServerLevel l,BlockPos b){return extended(l,b)?PASS:-66;}
    public static double exit(ServerLevel l,BlockPos b){return extended(l,b)?-142.5:-70.5;}
    public static BlockPos account(ServerLevel l,BlockPos b){
        if(!extended(l,b)||!l.hasChunkAt(b.offset(ACCOUNT)))return OLD_ACCOUNT;
        var s=l.getBlockState(b.offset(ACCOUNT));return s.is(UncannyRegistry.PROP.get())&&s.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.LEDGER?ACCOUNT:OLD_ACCOUNT;
    }
}
