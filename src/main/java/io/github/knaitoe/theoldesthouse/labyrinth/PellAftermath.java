package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.542, the Pell house after (docs/PELL_HOUSE_AFTER_0_4_542.md; the proposal's "Aftermath": "Like the pool and the flat, the house
 * comes back once in a later hall"). It comes back to each reader whose ending has landed (Aftermath), as their ending left it, with
 * nobody in it to ask anything of them:
 * <ul>
 * <li><b>After Tilly came out:</b> empty, with a FOR SALE sign on the front lawn; the line scrubbed off everywhere, its ghost in the
 *     dead grass; the pool drained; in Nessa's room only the height marks, and the N beside the last one rubbed out.</li>
 * <li><b>After Tilly answered:</b> lived in, and nobody home; the line fresh, still wet; on the door frame one more pencil mark, higher,
 *     with a later date and an N.</li>
 * </ul>
 * Each house (one to each pair since 0.4.547, SceneInstances) is shared by everybody at it, so it stands as its aftermath only while everybody at it has had their ending, and it
 * is dressed only where the house would begin its visit again (Household.fresh): never in front of somebody it has been playing for. A
 * reader who has not finished it brings it back as it always was, and the visit begins again for them. Nothing here is credit: a reader
 * whose account still waits under the mat can read it here, as they could have at first light.
 */
public final class PellAftermath {
    private PellAftermath(){}
    public enum Kind{NONE,EMPTY,KEPT}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.PELL_HOUSE;
    /** The agent's sign in the front lawn, and the height marks' upper cell by Nessa's door. */
    public static final BlockPos SIGN=new BlockPos(-6,0,-52),MARKS=new BlockPos(-11,1,-63);
    /** What stays in the empty house: what is built in, plumbed in or fixed down. Everything else has gone with the family, Nessa's room
     *  down to the height marks. */
    static final Set<PellThing.Kind> FIXED=EnumSet.of(PellThing.Kind.COUNTER,PellThing.Kind.SINK,PellThing.Kind.STOVE,PellThing.Kind.CUPBOARD_UPPER,
        PellThing.Kind.MANTEL,PellThing.Kind.WINDOW_SEAT,PellThing.Kind.HOOKS,PellThing.Kind.COAT_HOOK,PellThing.Kind.MARKS,PellThing.Kind.CIRCLE,
        PellThing.Kind.MIRROR,PellThing.Kind.VANITY,PellThing.Kind.BATH,PellThing.Kind.TOILET,PellThing.Kind.POWER,PellThing.Kind.TROUGH,
        PellThing.Kind.BULB,PellThing.Kind.PEGBOARD,PellThing.Kind.WORKBENCH,PellThing.Kind.KENNEL,PellThing.Kind.PUMP,
        PellThing.Kind.FOR_SALE,PellThing.Kind.MARKS_AFTER);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();

    /** How the house stands: as its aftermath after Tilly came out (EMPTY) or answered (KEPT), or as itself (NONE). */
    public static Kind dressed(ServerLevel l,BlockPos b){
        var s=l.getBlockState(b.offset(MARKS));
        return !PellThing.is(s,PellThing.Kind.MARKS_AFTER)?Kind.NONE:s.getValue(PellThing.STAGE)==0?Kind.EMPTY:Kind.KEPT;
    }
    /** A reader's ending at the house, once it has landed: KEPT after Tilly answered, EMPTY after she came out; NONE before. */
    public static Kind ending(CompoundTag own){
        if(!Aftermath.finished(own))return Kind.NONE;
        return own.getBoolean("TillyStayed")?Kind.KEPT:Kind.EMPTY;
    }
    /**
     * The house the readers at it would find: NONE while anybody at it has not had their ending; otherwise the first one's (two readers
     * whose endings differ, at the house at once, find the first one's). Null when nobody counts: spectators only look on.
     */
    static @Nullable Kind wanted(List<ServerPlayer> here){
        Kind want=null;
        for(var p:here){
            if(p.isSpectator())continue;
            var k=ending(PellHouse.own(p));if(k==Kind.NONE)return Kind.NONE;
            if(want==null)want=k;
        }
        return want;
    }
    /** Whether the house stands, or is about to stand, as its aftermath for the readers at it: then nobody is made (PellHouse). */
    public static boolean holds(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var want=wanted(here);var now=dressed(l,b);
        if(want==null)return now!=Kind.NONE;
        return want!=Kind.NONE&&(now!=Kind.NONE||PellPeople.household(b).fresh);
    }

    /**
     * Once a tick for the house, before its visit (PellPeople.tick). True while it stands as its aftermath, so the visit does not run.
     * Dressed only from the start of a visit, with the whole street's ground and bodies loaded, so nobody is left standing in it.
     */
    static boolean tick(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here){
        var want=wanted(here);var now=dressed(l,b);
        if(want==null)return now!=Kind.NONE;
        if(want==Kind.NONE){if(now!=Kind.NONE)undress(l,b,h,now);return false;}
        if(now==Kind.NONE){
            if(!h.fresh||!loaded(l,b))return false;
            dress(l,b,h,want);now=want;
        }
        for(var p:here){
            if(p.isSpectator()||!Aftermath.counted(p,PLACE))continue;
            var own=PellHouse.own(p);
            PellHouse.cue(p,now==Kind.EMPTY?PellTexts.AFTER_EMPTY:PellTexts.AFTER_KEPT);
            // An account read at first light is not read again; one that still waits is waiting under the mat.
            if(own.getBoolean("Ready")&&!own.getBoolean("Completed"))p.displayClientMessage(Component.literal(LiteraryVignettes.readyLine(PLACE,own)),false);
        }
        return true;
    }
    private static boolean loaded(ServerLevel l,BlockPos b){
        for(int cx=(b.getX()+PellSite.WEST)>>4;cx<=(b.getX()+PellSite.EAST)>>4;cx++)
            for(int cz=(b.getZ()+PellSite.NORTH)>>4;cz<=(b.getZ()+2)>>4;cz++)
                if(!l.hasChunk(cx,cz)||!l.areEntitiesLoaded(ChunkPos.asLong(cx,cz)))return false;
        return true;
    }

    /** The house as the ending left it. Nobody is in it: the household, Nessa, the car and the plates are gone, and the visit will
     *  begin again from the start for whoever next brings it back. */
    static void dress(ServerLevel l,BlockPos b,PellPeople.Household h,Kind kind){
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,PellPeople.area(b),PellPeople::is))a.discard();
        for(var c:l.getEntitiesOfClass(TownCar.class,PellPeople.area(b),c->true))c.discard();
        PellVisit.clearPlates(l,b);h.car=null;h.locked.clear();h.fresh=true;
        // Everything the visits moved or set down goes, and everything the house was furnished with is back where it stood.
        clear(l,b,k->k!=PellThing.Kind.CIRCLE);
        PellSite.things(l,b);
        for(var door:PellVisit.ALL_DOORS)if(PellSite.open(l,b,door))PellHouse.toggle(l,b,door);
        if(kind==Kind.EMPTY){
            clear(l,b,k->!FIXED.contains(k));
            for(int x=-13;x<=-8;x++)for(int z=-63;z<=-59;z++)BuildBlocks.set(l,b.offset(x,PellSite.ROOF,z),PellSite.mat("ceiling"),F);
            // The line scrubbed off everywhere: the ground under it as it is, the grass where it lay dead.
            for(var at:PellSite.route()){
                var cell=b.offset(at.getX(),-1,at.getZ());var s=l.getBlockState(cell);if(!PellLine.is(s))continue;
                var surface=s.getValue(PellLine.SURFACE);
                BuildBlocks.set(l,cell,PellSite.mat(surface==PellLine.Surface.LAWN?"lawn_dry":surface.material()),F);
            }
            // The pool drained to its tiles, its rope and light gone; the gate's padlock gone, the gate left open.
            PellWeek.rope(l,b,false);
            for(int x=PellSite.WATER_W;x<=PellSite.WATER_E;x++)for(int z=PellSite.WATER_N;z<=PellSite.WATER_S;z++)
                for(int y=PellSite.floor(z)+1;y<=-1;y++)BuildBlocks.set(l,b.offset(x,y,z),AIR,F);
            gate(l,b,2);
            BuildBlocks.set(l,b.offset(SIGN),PellThing.of(PellThing.Kind.FOR_SALE,Direction.SOUTH,0),F);
            BuildBlocks.set(l,b.offset(MARKS),PellThing.of(PellThing.Kind.MARKS_AFTER,Direction.SOUTH,0),F);
        }else{
            // Lived in: the line gone over again, the pool open and the rope across it as June keeps it, the gate padlocked.
            PellSite.repaint(l,b,PellLine.LAST);
            PellSite.pool(l,b,false);PellWeek.rope(l,b,true);PellLastNight.poolLight(l,b,false);
            gate(l,b,1);
            BuildBlocks.set(l,b.offset(MARKS),PellThing.of(PellThing.Kind.MARKS_AFTER,Direction.SOUTH,1),F);
        }
    }
    /** The house as it always was, for somebody who has not had their ending: the visit, beginning again, dresses it for its day. */
    static void undress(ServerLevel l,BlockPos b,PellPeople.Household h,Kind kind){
        if(PellThing.is(l.getBlockState(b.offset(SIGN)),PellThing.Kind.FOR_SALE))BuildBlocks.set(l,b.offset(SIGN),AIR,F);
        clear(l,b,k->k!=PellThing.Kind.CIRCLE);
        PellSite.things(l,b);
        if(kind==Kind.EMPTY){PellSite.line(l,b);PellSite.pool(l,b,true);}
        gate(l,b,1);
        h.fresh=true;
    }
    /** Every one of the house's things in the street and on the lot that the test takes, gone. */
    private static void clear(ServerLevel l,BlockPos b,Predicate<PellThing.Kind> go){
        var c=new BlockPos.MutableBlockPos();
        for(int x=PellSite.WEST;x<=PellSite.EAST;x++)for(int z=PellSite.NORTH;z<=1;z++)for(int y=-1;y<=PellSite.ROOF-1;y++){
            c.set(b.getX()+x,b.getY()+y,b.getZ()+z);var s=l.getBlockState(c);
            if(PellThing.is(s)&&go.test(s.getValue(PellThing.KIND)))BuildBlocks.set(l,c.immutable(),AIR,F);
        }
    }
    /** The pool's child gate: 0 shut, 1 shut and padlocked, 2 open. */
    private static void gate(ServerLevel l,BlockPos b,int stage){
        var at=b.offset(PellSite.POOL_GATE);var s=l.getBlockState(at);
        if(s.is(PellRegistry.FITTING.get())&&s.getValue(PellBlock.KIND)==PellBlock.Kind.POOL_GATE&&s.getValue(PellBlock.STAGE)!=stage)
            l.setBlock(at,s.setValue(PellBlock.STAGE,stage),3);
    }

    /**
     * A hand on something while the house stands as its aftermath. True if it was answered here; false leaves it to the house's own
     * hand (the letterbox and the mat, which LiteraryVignettes reads first; the windows, fences and roller door; the neighbours').
     */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var kind=dressed(l,b);if(kind==Kind.NONE)return false;
        var at=b.offset(rel);var st=l.getBlockState(at);
        if(PellThing.is(st)){
            var thing=st.getValue(PellThing.KIND);int stage=st.getValue(PellThing.STAGE);
            switch(thing){
                case FOR_SALE->PellHouse.cue(p,PellTexts.SIGN);
                case MARKS,MARKS_AFTER->PellHouse.cue(p,kind==Kind.EMPTY?PellTexts.MARKS_EMPTY:PellTexts.MARKS_KEPT);
                // What only opens and shuts, or goes on and off, does as it always did.
                case WARDROBE,BULB,FLOOR_LAMP->PellHouse.thing(p,l,b,at,st,own);
                case CIRCLE->PellHouse.cue(p,kind==Kind.EMPTY?PellTexts.CIRCLE_EMPTY:PellTexts.thing(thing,stage));
                default->PellHouse.cue(p,kind==Kind.EMPTY?PellTexts.LEFT:PellTexts.thing(thing,stage));
            }
            return true;
        }
        // The line, fresh, after Tilly answered; scrubbed off, after she came out.
        if(PellLine.is(st)){PellHouse.cue(p,PellTexts.line(st.getValue(PellLine.SURFACE),st.getValue(PellLine.SHAPE)==PellLine.Shape.THIN,st.getValue(PellLine.STAGE),false));return true;}
        if(kind==Kind.EMPTY&&rel.getY()==-1&&PellSite.onLine(new BlockPos(rel.getX(),0,rel.getZ()))){
            PellHouse.cue(p,st.is(PellRegistry.material("lawn_dry"))?PellTexts.LINE_GONE:PellTexts.LINE_GONE_HARD);return true;
        }
        // The pool's tiles and its drain, told as always and kept by nobody.
        var detail=PellWeek.detail(st);if(detail!=null){PellHouse.cue(p,detail);return true;}
        if(!st.is(PellRegistry.FITTING.get()))return false;
        var door=PellSite.doorAt(l,b,rel);
        if(door==null||!PellSite.onLot(new Vec3(door.getX()+.5,0,door.getZ()+.5))||door.equals(PellSite.SLIDER.west()))return false;
        if(door.equals(PellSite.POOL_GATE)){
            if(kind==Kind.KEPT){PellHouse.cue(p,PellTexts.POOL_GATE);return true;}
            boolean open=PellBlock.open(st);gate(l,b,open?0:2);
            l.playSound(null,at,open?SoundEvents.FENCE_GATE_CLOSE:SoundEvents.FENCE_GATE_OPEN,SoundSource.BLOCKS,.7F,1F);
            if(!open)PellHouse.cue(p,PellTexts.GATE_OPEN);
            return true;
        }
        // Lived in, June's door and the shed are as she keeps them; empty, every door opens.
        if(kind==Kind.KEPT&&door.equals(PellSite.JUNE_DOOR)){PellHouse.cue(p,PellTexts.JUNES_DOOR);return true;}
        if(kind==Kind.KEPT&&door.equals(PellSite.SHED_DOOR)&&!PellSite.open(l,b,door)){PellHouse.cue(p,PellTexts.PADLOCKED);return true;}
        PellHouse.toggle(l,b,door);
        return true;
    }
}
