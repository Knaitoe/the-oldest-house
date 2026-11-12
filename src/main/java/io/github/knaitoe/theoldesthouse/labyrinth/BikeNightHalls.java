package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.511, Bike night, "Failure: caught in town": "A reader who is out walks home. The night is over for them, and the vignette is
 * never dealt to them again. The bike turns up later, in a later hall, chained to a school fence with its front wheel bent."
 *
 * <p>For a reader who is out, each crossing into anywhere is counted. From the {@link #WAIT}th on, the first plain hall they come
 * into that has room for it has it: two clear cells along a wall, a few steps in from the door they came by, on the side of the
 * way through and not in it (ChainedBike). It is put there as they arrive, behind the door's own closing, and is theirs alone.
 *
 * <p>It is found once. Until its reader has come near it and looked at it (or put a hand on it) it is owed, and a hall they walk
 * straight through without looking leaves it to turn up in another. Once they have, it is in their record, and it turns up nowhere
 * again; the one they found goes when they have left it.
 */
public final class BikeNightHalls {
    private BikeNightHalls(){}
    /** How many crossings after the night before it may turn up; how far in from the door the wall is looked along, from and to; and how far to each side a wall is looked for. */
    public static final int WAIT=3,FROM=6,TO=16,ASIDE=5;
    /** For tests: it turns up in nothing. */
    public static boolean keptAway;

    /** A reader has come into a place by a door (LabyrinthDoors). */
    public static void arrived(ServerPlayer p,LabyrinthPlace place,@Nullable LabyrinthData.Door entry){
        if(keptAway||entry==null||p.isSpectator())return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),BikeNight.PLACE);
        if(!own.getBoolean("Out")||own.getBoolean("ChainedSeen")||make(own)==null)return;
        int crossed=own.getInt("OutCrossings")+1;own.putInt("OutCrossings",crossed);LiteraryVignettes.save(d,p.getUUID(),BikeNight.PLACE,own);
        if(crossed<WAIT||place.kind()!=LabyrinthPlace.Kind.GRAY)return;
        var l=p.server.getLevel(entry.dimension);if(l==null)return;
        // One at a time: what was put in a hall they left without looking is gone, or goes by itself.
        for(var e:l.getEntitiesOfClass(ChainedBike.class,new AABB(entry.lower).inflate(ChainedBike.FAR),b->b.ownedBy(p.getUUID())))e.discard();
        var spot=spot(l,entry);if(spot==null)return;
        var bike=BikeNightRegistry.CHAINED_BIKE.get().create(l);if(bike==null)return;
        bike.owner(p.getUUID());bike.make(make(own));bike.moveTo(spot.at().x,spot.at().y,spot.at().z,spot.out().toYRot(),0);
        l.addFreshEntity(bike);
    }
    private static @Nullable NightBike.Make make(CompoundTag own){for(var m:NightBike.Make.values())if(m.id().equals(own.getString("Bike")))return m;return null;}

    /** Where it stands: the middle of the foot of two cells of wall, and the way out from the wall. */
    public record Spot(Vec3 at,Direction out){}
    /**
     * Along the way in from a door, the first two cells beside it that are clear, floored, against two cells of plain wall two high,
     * and not on the way itself. The door faces out of its room, so the way in is the other way.
     */
    public static @Nullable Spot spot(ServerLevel l,LabyrinthData.Door entry){
        var in=entry.facing.getOpposite();var floor=entry.lower;
        for(int d=1;d<=TO;d++){
            var on=floor.relative(in,d);
            if(!clear(l,on)||!clear(l,on.above()))return null;
            if(d<FROM)continue;
            for(var side:new Direction[]{in.getClockWise(),in.getCounterClockWise()})for(int s=2;s<=ASIDE;s++){
                // Open floor all the way from the way through to the wall: never round behind something that stands beside it.
                if(s==2&&!clear(l,on.relative(side)))break;
                var wall=on.relative(side,s);
                if(clear(l,wall))continue;
                var a=wall.relative(side.getOpposite());var b=a.relative(in);
                if(stands(l,a,side)&&stands(l,b,side)&&clear(l,b.relative(side.getOpposite()))&&l.getEntities(null,new AABB(a).minmax(new AABB(b)).inflate(.2)).isEmpty()){
                    var mid=Vec3.atBottomCenterOf(a).add(Vec3.atBottomCenterOf(b)).scale(.5);
                    return new Spot(mid.add(side.getStepX()*.5,0,side.getStepZ()*.5),side.getOpposite());
                }
                break;
            }
        }
        return null;
    }
    private static boolean clear(ServerLevel l,BlockPos at){return l.isLoaded(at)&&l.getBlockState(at).isAir();}
    /** A cell it can stand in: clear two high, on a whole floor, with whole plain wall behind it two high. */
    private static boolean stands(ServerLevel l,BlockPos at,Direction side){
        if(!clear(l,at)||!clear(l,at.above())||!whole(l,at.below()))return false;
        var wall=at.relative(side);
        return whole(l,wall)&&whole(l,wall.above());
    }
    private static boolean whole(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return l.isLoaded(at)&&s.isCollisionShapeFullBlock(l,at)&&!s.hasBlockEntity()&&!(s.getBlock() instanceof DoorBlock);}

    /** Its reader has come near it and looked at it: they are told what it is, once, and it is in their record. */
    static void seen(ServerPlayer p,ChainedBike bike){
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),BikeNight.PLACE);
        if(own.getBoolean("ChainedSeen"))return;
        own.putBoolean("ChainedSeen",true);LiteraryVignettes.save(d,p.getUUID(),BikeNight.PLACE,own);
        p.playNotifySound(BikeNightRegistry.CHAIN.get(),SoundSource.BLOCKS,.5F,1.05F);
        BikeNight.cue(p,String.format(BikeNightTexts.CHAINED,BikeNightTexts.bike(bike.make().id())));
    }
    /** A hand on it. */
    static void hand(ServerPlayer p,ChainedBike bike){
        var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),BikeNight.PLACE);
        if(!own.getBoolean("ChainedSeen")){seen(p,bike);return;}
        BikeNight.cue(p,BikeNightTexts.CHAINED_HAND);
    }
}
