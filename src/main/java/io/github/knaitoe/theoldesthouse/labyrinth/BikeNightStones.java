package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.510, Bike night (docs/BIKE_NIGHT_PROPOSAL.md, "Failure: caught in the Hollow"). "What stays: ... in that town, in every copy
 * any reader later visits, there is a new small headstone at the end of the row with the old date. It carries this reader's name
 * and the date of their night, and their bike leans against it, rusting."
 *
 * <p>Who the Hollow has had is kept with the world, in the order it had them: their name as it was that night, and the make they
 * were on. Each has a place at the end of the row of small stones: east of it first, at the row's own spacing, then west. In every
 * night, in every copy of the town, a new small stone stands in each of those places (CalderGraveBlock.NEW_STONE: marble not yet
 * gone grey, with the year of the night on it where the row's have theirs), and the bike they were on leans against the back of
 * it, rusted (a PackBike of PackBike.RUSTED's looks, propped).
 *
 * <p>Both are put there as everything else of the Hollow's is: the first moment nobody could be looking at the place. A stone is a
 * block and stays; a bike is not saved, and is stood there again each night. A hand on the stone is told what is cut on it: the
 * name, and the date. Nothing is said to the reader whose name it is that is not said to anybody.
 */
public final class BikeNightStones {
    private BikeNightStones(){}
    /** How far apart the row's stones are; the first place east of the row and the first west of it; and how many places there are. */
    public static final int EVERY=3,EAST=CalderGraves.SMALL_X[CalderGraves.SMALL_X.length-1]+EVERY,WEST=CalderGraves.SMALL_X[0]-EVERY,MOST=32;
    /** Where the bike's wheels stand, north of the middle of its stone's cell, so that it leans on the stone's back; and the way it points. */
    public static final double BIKE_BACK=.31;public static final float BIKE_YAW=-90;
    /** How often the places are looked at, in ticks. */
    public static final int LOOK_EVERY=20;
    private static final String KEY="Stones";

    public record Stone(UUID id,String name,String make){}
    /** Where the stone of that number stands, across the row: east of it, then west of it. */
    public static int x(int i){return i<MOST/2?EAST+EVERY*i:WEST-EVERY*(i-MOST/2);}
    /** Which stone stands at a place in the cemetery, or -1. */
    public static int index(BlockPos rel){
        if(rel.getZ()!=CalderGraves.SMALL_ROW)return -1;
        for(int i=0;i<MOST;i++)if(x(i)==rel.getX())return i;
        return -1;
    }
    public static BlockPos cell(BlockPos base,int i){return base.offset(x(i),CalderGraves.level(CalderGraves.SMALL_ROW),CalderGraves.SMALL_ROW);}

    public static List<Stone> all(MinecraftServer server){
        var out=new ArrayList<Stone>();var list=LiteraryVignettes.shared(LabyrinthData.get(server),BikeNight.PLACE).getList(KEY,Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++){var t=list.getCompound(i);if(t.hasUUID("Id"))out.add(new Stone(t.getUUID("Id"),t.getString("Name"),t.getString("Make")));}
        return out;
    }
    /** The Hollow has had this reader: their name and the make they were on are kept, once, if there is a place left. */
    static void add(ServerPlayer p,String make){
        var d=LabyrinthData.get(p.server);var world=LiteraryVignettes.shared(d,BikeNight.PLACE);var list=world.getList(KEY,Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++)if(list.getCompound(i).hasUUID("Id")&&list.getCompound(i).getUUID("Id").equals(p.getUUID()))return;
        if(list.size()>=MOST)return;
        var t=new CompoundTag();t.putUUID("Id",p.getUUID());t.putString("Name",p.getGameProfile().getName());t.putString("Make",make);
        list.add(t);world.put(KEY,list);LiteraryVignettes.shared(d,BikeNight.PLACE,world);
    }
    /** What is cut on the new stone at a place: the name, and the date of their night. */
    public static String inscription(MinecraftServer server,BlockPos rel){
        int i=index(rel);var all=all(server);
        if(i<0||i>=all.size())return BikeNightTexts.NEW_STONE_BLANK;
        return all.get(i).name().toUpperCase(Locale.ROOT)+". "+BikeNightTexts.NIGHT_DATE+".";
    }

    /**
     * Each night, in each copy: a stone in every place that should have one, and its bike against it, once nobody could be looking;
     * and a new stone that is nobody's (left by a world that no longer has its name) taken away the same way.
     */
    static void tick(ServerLevel l,BikeNight.Night n){
        if(n.tick%LOOK_EVERY!=3)return;
        var all=all(l.getServer());var stone=CalderGraveBlock.of(CalderGraveBlock.Kind.SMALL,Direction.SOUTH,CalderGraveBlock.NEW_STONE);
        for(int i=0;i<MOST;i++){
            var at=cell(n.base,i);if(!l.isLoaded(at))continue;
            var here=l.getBlockState(at);boolean is=CalderGraveBlock.is(here,CalderGraveBlock.Kind.SMALL)&&here.getValue(CalderGraveBlock.STAGE)==CalderGraveBlock.NEW_STONE;
            boolean due=i<all.size();
            if(is==due&&(!due||n.yard.leaning.get(i)||all.get(i).make().isEmpty()))continue;
            boolean watched=false;for(var p:l.players())watched|=BikeNightGraveyard.seen(p,Vec3.atCenterOf(at),BikeNightGraveyard.SEEN);
            if(watched)continue;
            if(!due){l.setBlock(at,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);continue;}
            if(!is){
                // Only on open grass: nothing of the cemetery's is ever put out for it.
                if(!here.isAir()||!l.getBlockState(at.below()).isCollisionShapeFullBlock(l,at.below()))continue;
                l.setBlock(at,stone,3);
            }
            if(n.yard.leaning.get(i)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))continue;
            var make=make(all.get(i).make());if(make==null){n.yard.leaning.set(i);continue;}
            var bike=BikeNightRegistry.PACK_BIKE.get().create(l);if(bike==null)continue;
            bike.look(PackBike.RUSTED+make.ordinal());bike.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5-BIKE_BACK,BIKE_YAW,0);bike.prop(1);
            if(l.addFreshEntity(bike))n.yard.leaning.set(i);
        }
    }
    private static @javax.annotation.Nullable NightBike.Make make(String id){for(var m:NightBike.Make.values())if(m.id().equals(id))return m;return null;}
    /** The bike that leans on the stone of that number, in a park. For tests. */
    public static @javax.annotation.Nullable PackBike leaning(ServerLevel l,BlockPos base,int i){
        var at=cell(base,i);
        for(var b:l.getEntitiesOfClass(PackBike.class,new net.minecraft.world.phys.AABB(at).inflate(1.2),e->e.isAlive()&&e.look()>=PackBike.RUSTED))return b;
        return null;
    }
}
