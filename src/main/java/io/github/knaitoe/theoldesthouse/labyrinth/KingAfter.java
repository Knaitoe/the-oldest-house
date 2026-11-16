package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/**
 * 0.4.649, A sort of thing, part 9 (docs/SLEEPING_KING_0_4_649.md; the proposal's "Afterwards"): the country, come back to.
 *
 * <p>A country whose every reader has had their own ending stands as what that ending left, and nothing of the dream goes on in it:
 * <ul>
 * <li><b>if he woke</b> ({@link #WOKE}): all of it ruled and silent. The breathing has stopped, nobody is under the tree, and his
 *     night-cap hangs on a bough of it;</li>
 * <li><b>if he slept</b> ({@link #SLEPT}): all of it filled to its edges, the breathing going on, and him asleep under his tree.</li>
 * </ul>
 * In both, a reader on the country's land is a line from one end of it to the other (KingBreath), and a thing only in the house. None of
 * the dream's people are there, nothing counts or stirs, and the feast stands drawn or filled with nobody at it.
 *
 * <p>It is decided for a dream, not for a reader: by everybody in the country at once, and by the first of them if their endings
 * differ (a waking wins). A reader who has not had an ending coming into it makes it a fresh dream again, for everybody in it. The
 * House deals it back to a reader who has read their account as it deals any aftermath room ({@link Aftermath}); a reader whose ending
 * landed and who left the account unread finds the same country, and the account still on the armchair.
 */
public final class KingAfter {
    private KingAfter(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SLEEPING_KING;
    /** A dream going on; the country after he woke; the country he sleeps on in. */
    public static final int NONE=0,WOKE=1,SLEPT=2;
    /** In each reader's own record: that they have come back to it, the visit their ending landed on, and how long this visit. */
    public static final String SEEN="KingAfterSeen",ENDED_ON="KingEndedVisit",STAYED="KingAfterStayed";
    /** How often the country asks whether it is still what it was; how often it is kept clear; a reader's calls before the line. */
    static final int RECHECK=10,KEEP=20,TOLD_AFTER=8;
    /** Where the night-cap may hang: under a bough of his tree, on the side he lay. Offsets from the tree's foot. */
    static final int[][] BOUGHS={{0,3},{-1,3},{1,3},{0,2},{-1,2},{1,2},{-2,3},{2,3},{-2,2},{2,2}};
    public static final int CAP_Y=4;

    /** Whether a reader's own ending has landed: the account read, or an ending they have not yet read of. */
    public static boolean done(CompoundTag own){return own.getBoolean("Completed")||!own.getString(KingSleep.ENDING).isEmpty();}
    /** How a reader's ending left him: woken if they woke him, were there when he woke, or he woke after they had gone. */
    public static int left(CompoundTag own){
        String how=own.getString(KingSleep.ENDING);if(how.isEmpty())return NONE;
        return how.equals("left_him_his_sleep")&&!own.getBoolean("KingWokeAfter")?SLEPT:WOKE;
    }
    /** For tests and the country's own look: what this country stands as now. */
    public static int of(BlockPos b){return KingSleep.get(b).after;}

    /**
     * What the country is to the readers in it: its aftermath if every one of them has had their ending, and by their endings. With
     * nobody in it, it is what it was.
     */
    static int mode(ServerLevel l,BlockPos b,int was){
        var here=KingSleep.readers(l,b);if(here.isEmpty())return was;
        var d=LabyrinthData.get(l.getServer());boolean woke=false;
        for(var p:here){
            // A waking that has just ended and that this reader's own tick has not yet written down (KingSleep.settle).
            String coming=KingSleep.comingTo(p.getUUID());
            if(coming!=null){if(coming.equals("too_soon"))return NONE;woke=true;continue;}
            var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
            if(!done(own))return NONE;
            if(left(own)==WOKE)woke=true;
        }
        return woke?WOKE:SLEPT;
    }

    /** Once a tick, from the dream's own tick. True if the country stands as its aftermath, and nothing else of the dream goes on. */
    static boolean tick(ServerLevel l,BlockPos b){
        var s=KingSleep.get(b);long now=l.getGameTime();
        if(now-s.afterAt>=RECHECK){
            s.afterAt=now;
            if(mode(l,b,s.after)!=s.after){KingSleep.reset(l,b);s=KingSleep.get(b);s.afterAt=now;}
        }
        if(s.after==NONE)return false;
        if(now%KEEP==0)keep(l,b,s.after);
        return true;
    }

    /** A country become its aftermath (KingSleep.reset, once the dream's own things have been put back): cleared, and dressed. */
    static void dress(ServerLevel l,BlockPos b,KingSleep.Sleep s){
        keep(l,b,s.after);
        if(l.isLoaded(b.offset(KingSite.FEAST_HEAD)))KingSite.feast(l,b,s.after==SLEPT);
    }
    /** A dream again: the night-cap is on his head, not on a bough, and its people are where a fresh dream has them. */
    static void undress(ServerLevel l,BlockPos b){
        cap(l,b,false);
        King.people(l,b);
    }

    /** Nobody of the dream is in it; he lies asleep in the one, and is gone from the other, where his night-cap hangs. */
    static void keep(ServerLevel l,BlockPos b,int after){
        var bounds=PLACE.room();
        var box=new AABB(b.getX()+bounds.minX(),b.getY()+bounds.minY(),b.getZ()+bounds.minZ(),b.getX()+bounds.maxX()+1,b.getY()+bounds.maxY()+1,b.getZ()+bounds.maxZ()+1);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,box,a->a.scene().equals(PLACE.id())))if(!(after==SLEPT&&King.is(a)))a.discard();
        if(after==SLEPT){
            King.ensure(l,b);var king=King.find(l,b);
            if(king!=null&&(king.getPose()!=net.minecraft.world.entity.Pose.SLEEPING))King.lie(king,b.offset(KingSite.KING),180);
        }
        cap(l,b,after==WOKE);
    }

    /** The night-cap, hung under the first bough that is there on the side he lay, or taken down. */
    static void cap(ServerLevel l,BlockPos b,boolean hung){
        var tree=b.offset(KingSite.TREE);if(!l.isLoaded(tree))return;
        BlockPos at=null;
        for(var o:BOUGHS){
            var p=tree.offset(o[0],CAP_Y,o[1]);var st=l.getBlockState(p);
            if(KingBlock.is(st,KingBlock.Kind.NIGHTCAP)){if(hung&&at==null){at=p;continue;}l.setBlock(p,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);continue;}
            if(hung&&at==null&&st.isAir()&&bough(l.getBlockState(p.above())))at=p;
        }
        if(hung&&at!=null&&!KingBlock.is(l.getBlockState(at),KingBlock.Kind.NIGHTCAP))l.setBlock(at,KingBlock.of(KingBlock.Kind.NIGHTCAP,Direction.NORTH),Block.UPDATE_CLIENTS);
    }
    private static boolean bough(net.minecraft.world.level.block.state.BlockState st){return st.is(KingRegistry.material("bough"))||st.is(KingRegistry.material("bough_ruled"));}
    /** For tests: where the night-cap hangs in this country, or null. */
    public static BlockPos capAt(ServerLevel l,BlockPos b){
        var tree=b.offset(KingSite.TREE);
        for(var o:BOUGHS){var p=tree.offset(o[0],CAP_Y,o[1]);if(KingBlock.is(l.getBlockState(p),KingBlock.Kind.NIGHTCAP))return p;}
        return null;
    }

    /**
     * A reader's own part of it, every quarter second (KingSleep.reader). Coming back to it on a later visit than their ending's, with
     * their account read, counts as a return once (Aftermath) and says what is there; nothing else happens to them.
     */
    static void reader(ServerPlayer p,BlockPos b,CompoundTag own,int after){
        own.putInt(STAYED,own.getInt(STAYED)+1);
        if(own.getInt(STAYED)<TOLD_AFTER||!Aftermath.finished(own)||own.getInt("Visit")==own.getInt(ENDED_ON))return;
        if(Aftermath.count(own)){own.putBoolean(SEEN,true);King.cue(p,after==WOKE?KingTexts.AFTER_WOKE:KingTexts.AFTER_SLEPT);}
    }
    /** A new visit: the stay is counted from its start. */
    static void arrive(CompoundTag own){own.putInt(STAYED,0);}
}
