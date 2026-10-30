package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.199, the first puzzle room: Borrowed Light (the owner's design, "Borrowed Light would be my first choice to prototype").
 *
 * A dim dressing room. Before anything is touched, everything the reader needs is in view: a stage lamp on the east wall, lit
 * behind a closed shutter with a brass catch; a dressing mirror on boards scraped along a track, with three deeper scrapes where
 * it has always been stopped; a brass sun over the far wall beside a door that has no handle. Lift the catch and the beam comes
 * out across the room; turn the mirror (its glass) an eighth at a time, or kick its base along the track to the next stop, and the
 * beam follows. Lay it on the sun and the door gives.
 *
 * Every action answers at once, in sound and light. Nothing counts down, hurts or is used up. The room is shared and keeps how it
 * was left; solved, it stays solved, its beam still crossing the room. Which side the sun is on comes from the world.
 * It is offered to a reader only after they have resolved a story and walked a few halls since they last saw it (pacing: story,
 * then wandering, then the puzzle), never in the first crossings and never in the stone depths.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BorrowedLight {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.BORROWED_LIGHT;
    public static final BlockPos LAMP=new BlockPos(6,1,-16),SHUTTER=new BlockPos(5,1,-16);
    /** The track's three stops, along the beam's line, the mirror standing on the floor below it. */
    public static final int TRACK_Z=-16,TRACK_WEST=-5,TRACK_EAST=4;
    public static final int[] STOPS={-3,0,3};
    public static final int BEAM_Y=1,MIRROR_Y=0;
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private BorrowedLight(){}

    /** Which side the sun is on, from the world: east or west of the door. */
    public static int sunX(long seed){return Math.floorMod(seed^0x5bd1e995L,2)==0?3:-3;}
    public static BlockPos sun(long seed){return new BlockPos(sunX(seed),BEAM_Y,-21);}
    public static BlockPos farDoor(){return PLACE.doors().stream().filter(d->d.name().equals("far")).findFirst().orElseThrow().rel();}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var box=PLACE.room();var wall=PuzzleRegistry.material("wall").defaultBlockState();var floor=PuzzleRegistry.material("floor").defaultBlockState();var ceiling=PuzzleRegistry.material("ceiling").defaultBlockState();
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=box.minY();y<=box.maxY();y++){
            boolean inside=Math.abs(x)<box.maxX()&&z>box.minZ();
            l.setBlock(b.offset(x,y,z),!inside?wall:y==box.minY()?floor:y==box.maxY()?ceiling:Blocks.AIR.defaultBlockState(),FLAGS);
        }
        for(int x=TRACK_WEST;x<=TRACK_EAST;x++){boolean stop=false;for(int s:STOPS)stop|=s==x;
            l.setBlock(b.offset(x,-1,TRACK_Z),BorrowedLightBlock.of(BorrowedLightBlock.Kind.TRACK,Direction.EAST).setValue(BorrowedLightBlock.OPEN,stop),FLAGS);}
        boolean solved=solved(l);
        l.setBlock(b.offset(LAMP),BorrowedLightBlock.of(BorrowedLightBlock.Kind.LAMP,Direction.WEST),FLAGS);
        l.setBlock(b.offset(SHUTTER),BorrowedLightBlock.of(BorrowedLightBlock.Kind.SHUTTER,Direction.WEST).setValue(BorrowedLightBlock.OPEN,solved),FLAGS);
        // The mirror starts between the two stops it could be useful at, its glass facing the far wall: no beam goes anywhere yet.
        // A room already solved is built as it was left, the beam on the sun.
        var mirror=BorrowedLightBlock.of(BorrowedLightBlock.Kind.MIRROR,Direction.NORTH);
        if(solved)l.setBlock(b.offset(sunX(l.getSeed()),MIRROR_Y,TRACK_Z),mirror.setValue(BorrowedLightBlock.TURNED,true),FLAGS);
        else l.setBlock(b.offset(0,MIRROR_Y,TRACK_Z),mirror,FLAGS);
        l.setBlock(b.offset(sun(l.getSeed())),BorrowedLightBlock.of(BorrowedLightBlock.Kind.PLATE,Direction.SOUTH),FLAGS);
        for(int[] at:new int[][]{{-6,2,-4},{6,2,-4},{-6,2,-12}}){var facing=at[0]<0?Direction.EAST:Direction.WEST;
            l.setBlock(b.offset(at[0],at[1],at[2]),BorrowedLightBlock.of(BorrowedLightBlock.Kind.SCONCE,facing),FLAGS);}
        LabyrinthBuilder.entrance(l,b,wall,floor,ceiling);
        LabyrinthBuilder.doors(l,b,PLACE);
        PuzzleRooms.ironDoor(l,b,PLACE,solved);
        trace(l,b);
    }

    // ------------------------------------------------------------------ the beam
    public record Trace(List<BlockPos> path,boolean lit){}
    /** The beam from the lamp through the shutter, across the room at head height, turned by the mirror, to what stops it. */
    public static Trace beam(ServerLevel l,BlockPos b){return beam(l,b.offset(LAMP),b.offset(SHUTTER));}
    /**
     * Any lamp's beam, by its absolute lamp and shutter: along the lamp's facing at its height, turned by each mirror standing
     * on the floor below it, through interior windows, to whatever stops it. Lit when it lands square on a brass sun.
     */
    public static Trace beam(ServerLevel l,BlockPos lampAt,BlockPos shutterAt){
        var path=new ArrayList<BlockPos>();var lamp=l.getBlockState(lampAt);var shutter=l.getBlockState(shutterAt);
        if(!BorrowedLightBlock.is(lamp,BorrowedLightBlock.Kind.LAMP)||!BorrowedLightBlock.is(shutter,BorrowedLightBlock.Kind.SHUTTER)||!shutter.getValue(BorrowedLightBlock.OPEN))return new Trace(path,false);
        var facing=lamp.getValue(BorrowedLightBlock.FACING);int dx=facing.getStepX(),dz=facing.getStepZ();
        var at=shutterAt;path.add(lampAt);
        for(int step=0;step<96;step++){
            at=at.offset(dx,0,dz);
            var below=l.getBlockState(at.below(BEAM_Y-MIRROR_Y));
            if(BorrowedLightBlock.is(below,BorrowedLightBlock.Kind.MIRROR)){
                path.add(at);int[] n=BorrowedLightBlock.normal(below);int dot=dx*n[0]+dz*n[1];
                // Its glass turns the beam; its back or its edge stops it.
                if(dot>=0)return new Trace(path,false);
                // Reflect across the glass: d - 2(d.n)n / |n|^2; with the eight facings this always stays on the grid.
                int len=n[0]*n[0]+n[1]*n[1];int rx=dx-2*dot*n[0]/len,rz=dz-2*dot*n[1]/len;dx=Integer.signum(rx);dz=Integer.signum(rz);
                continue;
            }
            var here=l.getBlockState(at);
            // An interior window lets the light through to the next room.
            if(BorrowedLightBlock.is(here,BorrowedLightBlock.Kind.WINDOW))continue;
            if(BorrowedLightBlock.is(here,BorrowedLightBlock.Kind.PLATE)){
                path.add(at);var face=here.getValue(BorrowedLightBlock.FACING);
                return new Trace(path,face.getStepX()==-dx&&face.getStepZ()==-dz);
            }
            if(!here.isAir()&&!here.is(Blocks.LIGHT)&&!here.getCollisionShape(l,at).isEmpty()){path.add(at);return new Trace(path,false);}
        }
        path.add(at);return new Trace(path,false);
    }
    /** Lays the beam down: the lamp draws it, light follows it along the air, the sun lights or goes cold, and the door gives. */
    public static Trace trace(ServerLevel l,BlockPos b){return trace(l,b,PLACE,b.offset(LAMP),b.offset(SHUTTER),b.offset(sun(l.getSeed())));}
    public static Trace trace(ServerLevel l,BlockPos b,LabyrinthPlace place,BlockPos lampAt,BlockPos shutterAt,BlockPos sunAt){
        var t=beam(l,lampAt,shutterAt);
        if(!(l.getBlockEntity(lampAt) instanceof BorrowedLightBlock.Beam beam))return t;
        for(var old:beam.lit())if(l.getBlockState(old).is(Blocks.LIGHT))l.setBlock(old,Blocks.AIR.defaultBlockState(),FLAGS);
        var glow=new ArrayList<BlockPos>();
        for(int i=0;i+1<t.path().size();i++){var from=t.path().get(i);var to=t.path().get(i+1);int sx=Integer.signum(to.getX()-from.getX()),sz=Integer.signum(to.getZ()-from.getZ());
            for(var c=from.offset(sx,0,sz);!c.equals(to);c=c.offset(sx,0,sz))if(l.getBlockState(c).isAir()&&glow.size()<96){l.setBlock(c,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,9),FLAGS);glow.add(c);}}
        beam.set(t.path(),glow);
        var plate=l.getBlockState(sunAt);
        if(BorrowedLightBlock.is(plate,BorrowedLightBlock.Kind.PLATE)&&plate.getValue(BorrowedLightBlock.OPEN)!=t.lit())l.setBlock(sunAt,plate.setValue(BorrowedLightBlock.OPEN,t.lit()),FLAGS);
        if(t.lit()&&PuzzleRooms.solve(l,place,"The sun over the door warms. The door gives.")){
            PuzzleRooms.ironDoor(l,b,place,true);
            l.playSound(null,sunAt,SoundEvents.AMETHYST_BLOCK_RESONATE,SoundSource.BLOCKS,1F,1.2F);l.playSound(null,b.offset(PuzzleRooms.farDoor(place)),SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.8F,.8F);
        }
        return t;
    }
    public static boolean solved(ServerLevel l){return PuzzleRooms.solved(l.getServer(),PLACE);}

    // ------------------------------------------------------------------ hands on
    /** A reader's hand on one of the light rooms' parts. Returns whether it answered. */
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s,double height){
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null||p.isSpectator())return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(place!=PLACE&&place!=LabyrinthPlace.LAMP_ROOMS)return false;
        var b=LabyrinthPlaces.base(origin,place);var l=p.serverLevel();boolean deep=place==LabyrinthPlace.LAMP_ROOMS;
        Runnable retrace=()->{if(deep)LampRooms.trace(l,b);else trace(l,b);};
        switch(s.getValue(BorrowedLightBlock.KIND)){
            case SHUTTER->{
                if(s.getValue(BorrowedLightBlock.OPEN)){say(p,"The shutter is latched open.");return true;}
                l.setBlock(pos,s.setValue(BorrowedLightBlock.OPEN,true),FLAGS);l.playSound(null,pos,SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,.8F,.9F);
                say(p,"You lift the brass catch. The shutter folds back, and the lamp's light goes out across the room.");retrace.run();return true;
            }
            case MIRROR->{
                if(height<.45){
                    // The lamp rooms' mirrors turn on fixed pivots.
                    if(deep){say(p,"It is bolted to the floor. Only the glass turns.");return true;}
                    // A kick at its base: along the track, away from the reader, to the next stop.
                    int x=pos.getX()-b.getX(),dir=p.getX()<pos.getX()+.5?1:-1,to=Integer.MIN_VALUE;
                    for(int stop:STOPS)if(dir>0?stop>x&&(to==Integer.MIN_VALUE||stop<to):stop<x&&(to==Integer.MIN_VALUE||stop>to))to=stop;
                    if(to==Integer.MIN_VALUE){say(p,"It is at the end of its track.");return true;}
                    var dest=b.offset(to,MIRROR_Y,TRACK_Z);if(!l.getBlockState(dest).isAir()||!l.getBlockState(dest.above()).getCollisionShape(l,dest.above()).isEmpty()){say(p,"Something is in its way.");return true;}
                    l.setBlock(pos,Blocks.AIR.defaultBlockState(),FLAGS);l.setBlock(dest,s,FLAGS);l.playSound(null,dest,SoundEvents.GRINDSTONE_USE,SoundSource.BLOCKS,.6F,.6F);
                    retrace.run();return true;
                }
                // A hand on the glass: an eighth of a turn, clockwise.
                var next=s.getValue(BorrowedLightBlock.TURNED)?s.setValue(BorrowedLightBlock.TURNED,false).setValue(BorrowedLightBlock.FACING,s.getValue(BorrowedLightBlock.FACING).getClockWise()):s.setValue(BorrowedLightBlock.TURNED,true);
                l.setBlock(pos,next,FLAGS);l.playSound(null,pos,SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.7F,.7F);retrace.run();return true;
            }
            case LAMP->{say(p,s.getValue(BorrowedLightBlock.OPEN)?"A stage lamp.":"A stage lamp, lit, its light caught behind the shutter.");return true;}
            case PLATE->{say(p,s.getValue(BorrowedLightBlock.OPEN)?"A brass sun, warm to the touch.":"A brass sun set into the wall beside the door. It is cold.");return true;}
            case TRACK->{say(p,"The boards are scraped where something heavy has been dragged, again and again, and stopped in the same three places.");return true;}
            case WINDOW->{say(p,"A window into the next room. There is no way through it but the light's.");return true;}
            default->{return false;}
        }
    }
    private static void say(ServerPlayer p,String line){PuzzleRooms.say(p,line);}

    /** The far door, before the sun is lit: it has no handle on this side (PuzzleRooms.holds). */
    public static boolean holds(ServerPlayer p,LabyrinthData.Door door){return PuzzleRooms.holds(p,door);}

    // ------------------------------------------------------------------ pacing (PuzzleRooms)
    public static int weight(LabyrinthData data,UUID reader,int depth){return PuzzleRooms.weight(data,reader,PLACE,depth);}
    public static void arrived(ServerPlayer p){PuzzleRooms.arrived(p,PLACE);}
    public static boolean inside(ServerPlayer p){return PuzzleRooms.at(p)==PLACE;}
    /** The beams are laid down again now and then while someone is in a light room, so they are right after a reload. */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%40!=0)return;var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        for(var place:new LabyrinthPlace[]{PLACE,LabyrinthPlace.LAMP_ROOMS}){
            if(LabyrinthData.get(server).door(place.entryDoorId())==null)continue;
            for(var p:l.players())if(PuzzleRooms.at(p)==place){var b=LabyrinthPlaces.base(origin,place);if(b!=null&&l.isLoaded(b)){if(place==PLACE)trace(l,b);else LampRooms.trace(l,b);}break;}
        }
    }
}
