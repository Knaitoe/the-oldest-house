package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.DraftBlock.Kind.*;

/**
 * 0.4.230, drafts and pressure (docs/DRAFTS_0_4_230.md): the fourth puzzle family, the House's air. Air comes in at one opening and
 * goes out by the easiest other, and it shows: sheets on a line belly toward where it is going, curtains are pulled flat, dust slides
 * under a shut door, a door rattles in its frame, and a door that has nowhere else for the air to go is pushed open by it.
 * <p>
 * <b>The drying room</b> (early): a laundry and the scullery behind it, an inner door between. A vent with a grille in the laundry's
 * wall lets the air in; the scullery's sash window lets it out. With the vent open, the inner door open and the window shut, the air
 * has only the far door, and it opens it. <b>The vent rooms</b> (deep): three rooms in a line, the air coming down through a grate
 * in the first, a louvre in the second bleeding it away. With both doors open and the louvre shut it reaches the third, and lifts the
 * dust off a seam in plain plaster and pulls a curtain flat toward it: there is a door there, and pressed while the air is at it, the
 * plaster gives. Ordinary halls carry faint drafts too: dust sliding toward their doors.
 * <p>
 * Like every puzzle room, nothing counts down, hurts or is used up; a solved room stays solved, for everyone; no Witness.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class DraftRooms {
    private DraftRooms(){}
    public static final LabyrinthPlace DRYING=LabyrinthPlace.DRAFT_ROOM,VENTS=LabyrinthPlace.VENT_ROOMS;
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** Where the air goes, in a draft room, given how its openings stand. */
    public enum Flow {STILL,BLOCKED,ESCAPES,BLOCKED_ON,THROUGH}

    // ------------------------------------------------------------------ the drying room
    /** The laundry (z -1 to -9), the inner wall and its door, the scullery (z -11 to -19); the vent, the window and the line. */
    public static final BlockPos VENT=new BlockPos(-7,1,-5),INNER_DOOR=new BlockPos(-3,0,-10),SASH=new BlockPos(7,1,-15);
    public static final int LINE_Z=-6,LINE_Y=3;
    public static final BlockPos[] SHEETS={new BlockPos(-4,2,LINE_Z),new BlockPos(-1,2,LINE_Z),new BlockPos(2,2,LINE_Z),new BlockPos(5,2,LINE_Z)};
    public static final BlockPos[] WINDOW_CURTAINS={new BlockPos(6,1,-14),new BlockPos(6,1,-16)};

    // ------------------------------------------------------------------ the vent rooms
    /** The first room (z -1 to -8) and its grate; the door between it and the second; the second room (z -10 to -18) and its louvre; the door to the third (x 2 to 9, z -20 to -27); the seam in the third's east wall and the passage behind it to the far door. */
    public static final BlockPos GRATE=new BlockPos(0,5,-4),AB=new BlockPos(0,0,-9),LOUVRE=new BlockPos(-12,1,-14),BC=new BlockPos(6,0,-19),SEAM=new BlockPos(10,0,-24);
    public static final BlockPos[] A_SHEETS={new BlockPos(-2,2,-6),new BlockPos(2,2,-6)};
    public static final BlockPos[] LOUVRE_CURTAINS={new BlockPos(-11,1,-13),new BlockPos(-11,1,-15)};
    public static final BlockPos SEAM_CURTAIN=new BlockPos(9,1,-21);

    static BlockState mat(String name){return PuzzleRegistry.draftMaterial(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void door(ServerLevel l,BlockPos b,BlockPos at,Direction facing,boolean open){
        var s=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,open);
        set(l,b,at,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    static boolean open(ServerLevel l,BlockPos b,BlockPos at){var s=l.getBlockState(b.offset(at));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    static boolean stage(ServerLevel l,BlockPos b,BlockPos at,DraftBlock.Kind kind,int stage){var s=l.getBlockState(b.offset(at));return DraftBlock.is(s,kind)&&s.getValue(DraftBlock.STAGE)==stage;}
    static void shell(ServerLevel l,BlockPos b,LabyrinthPlace place,BlockState wall,BlockState floor,BlockState ceiling){
        var box=place.room();
        for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=box.minY();y<=box.maxY();y++){
            boolean inside=Math.abs(x)<box.maxX()&&z>box.minZ();
            l.setBlock(b.offset(x,y,z),!inside?wall:y==box.minY()?floor:y==box.maxY()?ceiling:Blocks.AIR.defaultBlockState(),FLAGS);
        }
    }

    public static void buildDrying(ServerLevel l,BlockPos b){
        var plaster=mat("plaster");var boards=mat("boards");var tile=mat("tile");
        shell(l,b,DRYING,plaster,boards,plaster);
        boolean solved=PuzzleRooms.solved(l.getServer(),DRYING);
        // The scullery's quarry tiles, and the inner wall with its door, shut.
        PuzzleRooms.fill(l,b,-6,-1,-19,6,-1,-11,tile);
        PuzzleRooms.fill(l,b,-6,0,-10,6,5,-10,plaster);door(l,b,INNER_DOOR,Direction.SOUTH,false);
        // The laundry: the vent in the west wall, its grille shut; a line across the room with four sheets on it; a copper and a basket.
        set(l,b,VENT,DraftBlock.of(DraftBlock.Kind.VENT,Direction.EAST,solved?1:0));
        for(int x=-5;x<=6;x++)set(l,b,new BlockPos(x,LINE_Y,LINE_Z),DraftBlock.of(LINE,Direction.NORTH));
        for(var at:SHEETS)set(l,b,at,DraftBlock.of(SHEET,Direction.NORTH));
        set(l,b,new BlockPos(5,0,-2),Blocks.CAULDRON.defaultBlockState());set(l,b,new BlockPos(5,0,-3),Blocks.BARREL.defaultBlockState());
        set(l,b,new BlockPos(-5,0,-2),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST));
        set(l,b,new BlockPos(-6,0,-8),Blocks.COMPOSTER.defaultBlockState());
        // The scullery: the sash window in the east wall, open, its curtains either side; a stone sink, a shelf, and the far door.
        set(l,b,SASH,DraftBlock.of(DraftBlock.Kind.SASH,Direction.WEST,solved?1:0));
        for(var at:WINDOW_CURTAINS)set(l,b,at,DraftBlock.of(CURTAIN,Direction.WEST));
        set(l,b,new BlockPos(-5,0,-18),Blocks.CAULDRON.defaultBlockState());set(l,b,new BlockPos(-6,0,-18),Blocks.SMOOTH_STONE.defaultBlockState());
        for(int x=-6;x<=-3;x++)set(l,b,new BlockPos(x,2,-19),Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.TOP));
        for(var at:List.of(new BlockPos(0,4,-5),new BlockPos(0,4,-15)))set(l,b,at,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        LabyrinthBuilder.entrance(l,b,plaster,boards,plaster);
        LabyrinthBuilder.doors(l,b,DRYING);
        PuzzleRooms.ironDoor(l,b,DRYING,solved);
    }
    public static void buildVents(ServerLevel l,BlockPos b){
        var plaster=mat("plaster");var boards=mat("boards");var stone=Blocks.STONE_BRICKS.defaultBlockState();
        shell(l,b,VENTS,plaster,boards,plaster);
        boolean solved=PuzzleRooms.solved(l.getServer(),VENTS);
        // The first room: the grate in the ceiling the air comes down through, two sheets hung to dry in it, the door to the second.
        PuzzleRooms.fill(l,b,-11,0,-8,-5,5,-1,plaster);PuzzleRooms.fill(l,b,5,0,-8,11,5,-1,plaster);
        set(l,b,GRATE,DraftBlock.of(DraftBlock.Kind.GRATE,Direction.NORTH));
        for(int x=-3;x<=3;x++)set(l,b,new BlockPos(x,3,-6),DraftBlock.of(LINE,Direction.NORTH));
        for(var at:A_SHEETS)set(l,b,at,DraftBlock.of(SHEET,Direction.NORTH));
        PuzzleRooms.fill(l,b,-11,0,-9,11,5,-9,plaster);door(l,b,AB,Direction.SOUTH,false);
        // The second room, wide: the louvre in its west wall, open, curtains either side of it; benches; the door to the third.
        set(l,b,LOUVRE,DraftBlock.of(DraftBlock.Kind.LOUVRE,Direction.EAST,solved?0:1));
        for(var at:LOUVRE_CURTAINS)set(l,b,at,DraftBlock.of(CURTAIN,Direction.EAST));
        for(int x:new int[]{-6,6})for(int z=-12;z>=-16;z--)set(l,b,new BlockPos(x,0,z),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,x<0?Direction.WEST:Direction.EAST));
        PuzzleRooms.fill(l,b,-11,0,-19,11,5,-19,plaster);door(l,b,BC,Direction.SOUTH,false);
        // The third room: plain plaster all round, and a curtain on a rail by the east wall. Everything west of it is solid.
        PuzzleRooms.fill(l,b,-11,0,-27,1,5,-20,stone);PuzzleRooms.fill(l,b,10,0,-27,11,5,-20,plaster);
        set(l,b,SEAM_CURTAIN,DraftBlock.of(CURTAIN,Direction.WEST));
        set(l,b,new BlockPos(3,0,-26),Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        // Behind the east wall: the seam (a panel that looks like the plaster round it), the passage, and the far door, shut.
        for(int y=0;y<=1;y++)set(l,b,SEAM.above(y),DraftBlock.of(DraftBlock.Kind.SEAM,Direction.WEST,solved?2:0));
        PuzzleRooms.fill(l,b,11,0,-27,11,1,-24,Blocks.AIR.defaultBlockState());
        for(var at:List.of(new BlockPos(0,4,-4),new BlockPos(0,4,-14),new BlockPos(6,4,-24)))set(l,b,at,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        LabyrinthBuilder.entrance(l,b,plaster,boards,plaster);
        LabyrinthBuilder.doors(l,b,VENTS);
        PuzzleRooms.ironDoor(l,b,VENTS,solved);
    }

    // ------------------------------------------------------------------ the air
    /** Where the drying room's air goes. */
    public static Flow drying(ServerLevel l,BlockPos b){
        if(!stage(l,b,VENT,DraftBlock.Kind.VENT,1))return Flow.STILL;
        if(!open(l,b,INNER_DOOR))return Flow.BLOCKED;
        return stage(l,b,SASH,DraftBlock.Kind.SASH,0)?Flow.ESCAPES:Flow.THROUGH;
    }
    /** Where the vent rooms' air goes. */
    public static Flow vents(ServerLevel l,BlockPos b){
        if(!open(l,b,AB))return Flow.BLOCKED;
        if(stage(l,b,LOUVRE,DraftBlock.Kind.LOUVRE,1))return Flow.ESCAPES;
        if(!open(l,b,BC))return Flow.BLOCKED_ON;
        return Flow.THROUGH;
    }
    /** Lays the drying room's sheets and curtains where the air puts them, shows it moving, and lets the far door give when it can. */
    public static Flow airDrying(ServerLevel l,BlockPos b,boolean particles){
        var flow=drying(l,b);
        for(var at:SHEETS)pull(l,b,at,SHEET,flow!=Flow.STILL);
        for(var at:WINDOW_CURTAINS)pull(l,b,at,CURTAIN,flow==Flow.ESCAPES);
        if(particles)switch(flow){
            case BLOCKED->{under(l,b,INNER_DOOR,Direction.NORTH);if(l.random.nextInt(3)==0)l.playSound(null,b.offset(INNER_DOOR),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.25F,1.6F);}
            case ESCAPES->{stream(l,b,VENT,INNER_DOOR);stream(l,b,INNER_DOOR,SASH);}
            case THROUGH->{stream(l,b,VENT,INNER_DOOR);stream(l,b,INNER_DOOR,PuzzleRooms.farDoor(DRYING));}
            default->{}
        }
        if(particles&&flow!=Flow.STILL)stream(l,b,VENT,VENT.east(2));
        if(flow==Flow.THROUGH&&PuzzleRooms.solve(l,DRYING,"The air in the room leans on the far door, and it gives.")){
            PuzzleRooms.ironDoor(l,b,DRYING,true);l.playSound(null,b.offset(PuzzleRooms.farDoor(DRYING)),SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.9F,.7F);
        }
        return flow;
    }
    public static Flow airVents(ServerLevel l,BlockPos b,boolean particles){
        var flow=vents(l,b);
        for(var at:A_SHEETS)pull(l,b,at,SHEET,flow!=Flow.BLOCKED);
        for(var at:LOUVRE_CURTAINS)pull(l,b,at,CURTAIN,flow==Flow.ESCAPES);
        pull(l,b,SEAM_CURTAIN,CURTAIN,flow==Flow.THROUGH);
        // The seam shows dust lifting off its edges while the air is drawn into it; once open it stays open.
        for(int y=0;y<=1;y++){var at=b.offset(SEAM.above(y));var s=l.getBlockState(at);if(DraftBlock.is(s,DraftBlock.Kind.SEAM)&&s.getValue(DraftBlock.STAGE)<2){int want=flow==Flow.THROUGH?1:0;if(s.getValue(DraftBlock.STAGE)!=want)l.setBlock(at,s.setValue(DraftBlock.STAGE,want),FLAGS);}}
        if(particles){
            var down=Vec3.atCenterOf(b.offset(GRATE));for(int i=0;i<2;i++)l.sendParticles(ParticleTypes.WHITE_ASH,down.x+l.random.nextGaussian()*.3,down.y-.4,down.z+l.random.nextGaussian()*.3,0,0,-1,0,.04);
            switch(flow){
                case BLOCKED->under(l,b,AB,Direction.NORTH);
                case ESCAPES->{stream(l,b,GRATE.below(4),AB);stream(l,b,AB,LOUVRE);}
                case BLOCKED_ON->{stream(l,b,GRATE.below(4),AB);stream(l,b,AB,BC);under(l,b,BC,Direction.NORTH);if(l.random.nextInt(3)==0)l.playSound(null,b.offset(BC),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.25F,1.6F);}
                case THROUGH->{stream(l,b,GRATE.below(4),AB);stream(l,b,AB,BC);stream(l,b,BC,SEAM);
                    var seam=Vec3.atCenterOf(b.offset(SEAM));for(int i=0;i<3;i++){double y=seam.y-.5+l.random.nextDouble()*2;l.sendParticles(ParticleTypes.WHITE_ASH,seam.x-.52,y,seam.z+(l.random.nextBoolean()?-.5:.5),0,.5,0,0,.03);}}
                default->{}
            }
        }
        return flow;
    }
    /** A sheet or curtain, hanging still or pulled by the air. */
    static void pull(ServerLevel l,BlockPos b,BlockPos rel,DraftBlock.Kind kind,boolean pulled){
        var at=b.offset(rel);var s=l.getBlockState(at);if(!DraftBlock.is(s,kind))return;int want=pulled?1:0;
        if(s.getValue(DraftBlock.STAGE)!=want)l.setBlock(at,s.setValue(DraftBlock.STAGE,want),FLAGS);
    }
    /** Dust carried from one opening toward another, a few motes at a time. */
    static void stream(ServerLevel l,BlockPos b,BlockPos from,BlockPos to){
        var a=Vec3.atCenterOf(b.offset(from));var z=Vec3.atCenterOf(b.offset(to));var d=z.subtract(a);double len=d.length();if(len<.1)return;var v=d.scale(1/len);
        for(int i=0;i<2;i++){double t=l.random.nextDouble()*len;var at=a.add(v.scale(t));l.sendParticles(ParticleTypes.WHITE_ASH,at.x+l.random.nextGaussian()*.25,at.y+.2+l.random.nextGaussian()*.2,at.z+l.random.nextGaussian()*.25,0,v.x,0,v.z,.06);}
    }
    /** Dust lifting and sliding at the gap under a shut door, toward the way the air wants to go. */
    static void under(ServerLevel l,BlockPos b,BlockPos door,Direction toward){
        var at=Vec3.atBottomCenterOf(b.offset(door)).relative(toward.getOpposite(),.6);
        for(int i=0;i<3;i++)l.sendParticles(ParticleTypes.WHITE_ASH,at.x+l.random.nextGaussian()*.3,at.y+.05,at.z,0,toward.getStepX(),.02,toward.getStepZ(),.05);
    }

    // ------------------------------------------------------------------ hands on
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(place!=DRYING&&place!=VENTS)return false;
        var b=LabyrinthPlaces.base(origin,place);var l=p.serverLevel();int stage=s.getValue(DraftBlock.STAGE);
        switch(s.getValue(DraftBlock.KIND)){
            case VENT->{l.setBlock(pos,s.setValue(DraftBlock.STAGE,stage==0?1:0),FLAGS);l.playSound(null,pos,SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,.6F,stage==0?1.2F:.9F);
                PuzzleRooms.say(p,stage==0?"You slide the grille open. Cold air comes through it, steadily, from somewhere.":"You slide the grille shut. The air stops.");}
            case SASH->{l.setBlock(pos,s.setValue(DraftBlock.STAGE,stage==0?1:0),FLAGS);l.playSound(null,pos,SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.8F,stage==0?.8F:1.1F);
                PuzzleRooms.say(p,stage==0?"You pull the sash down. The window is shut.":"You push the sash up. The window is open; there is only dark on the other side.");}
            case LOUVRE->{l.setBlock(pos,s.setValue(DraftBlock.STAGE,stage==0?1:0),FLAGS);l.playSound(null,pos,SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.6F,stage==0?1.2F:.9F);
                PuzzleRooms.say(p,stage==0?"You turn the louvre's handle. Its slats open.":"You turn the louvre's handle. Its slats close.");}
            case SEAM->{
                if(stage==2){PuzzleRooms.say(p,"A panel in the wall, standing open on a passage.");return true;}
                if(stage==0){PuzzleRooms.say(p,"Plaster.");return true;}
                // The air drawn into it: pressed now, the plaster gives.
                for(int y=0;y<=1;y++){var at=b.offset(SEAM.above(y));var t=l.getBlockState(at);if(DraftBlock.is(t,DraftBlock.Kind.SEAM))l.setBlock(at,t.setValue(DraftBlock.STAGE,2),FLAGS);}
                l.playSound(null,pos,SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.9F,.6F);
                if(PuzzleRooms.solve(l,VENTS,"The plaster gives under your hand. It was never plaster: a panel, and it swings in on cold air.")){
                    PuzzleRooms.ironDoor(l,b,VENTS,true);l.playSound(null,b.offset(PuzzleRooms.farDoor(VENTS)),SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.8F,.7F);}
            }
            case SHEET->PuzzleRooms.say(p,stage==1?"The sheet is bellied out on the line, pulled one way by the air.":"A sheet on the line, hanging still, nearly dry.");
            case CURTAIN->PuzzleRooms.say(p,stage==1?"The curtain is pulled flat against the wall behind it, as if the wall were breathing in.":"A curtain on a rail, hanging still.");
            case GRATE->PuzzleRooms.say(p,"A grate in the ceiling. Air comes down through it, cold, and never stops.");
            case LINE->{return false;}
        }
        if(place==DRYING)airDrying(l,b,false);else airVents(l,b,false);
        return true;
    }

    // ------------------------------------------------------------------ the clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();long now=server.getTickCount();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        if(now%10==0)for(var place:new LabyrinthPlace[]{DRYING,VENTS}){
            if(LabyrinthData.get(server).door(place.entryDoorId())==null)continue;
            for(var p:l.players())if(PuzzleRooms.at(p)==place){var b=LabyrinthPlaces.base(origin,place);if(b!=null&&l.isLoaded(b)){if(place==DRYING)airDrying(l,b,true);else airVents(l,b,true);}break;}
        }
        if(now%20==5)halls(l,origin);
    }
    /** Ordinary halls carry faint drafts toward their doors: a little dust sliding over the floor to the gap under each, more to an open one. */
    static void halls(ServerLevel l,BlockPos origin){
        var data=LabyrinthData.get(l.getServer());
        for(var p:l.players()){
            if(p.isSpectator())continue;var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());if(place==null||place.isVignette()||PuzzleRooms.is(place))continue;
            for(var door:data.doors()){
                if(!door.dimension.equals(l.dimension())||door.lower.distSqr(p.blockPosition())>100)continue;
                var s=l.getBlockState(door.lower);if(!(s.getBlock() instanceof DoorBlock))continue;boolean open=s.getValue(DoorBlock.OPEN);
                if(!open&&l.random.nextInt(3)!=0)continue;
                var front=Vec3.atBottomCenterOf(door.lower.relative(door.facing,3));var gap=Vec3.atBottomCenterOf(door.lower);var v=gap.subtract(front).normalize();
                double y=open?.6+l.random.nextDouble():.08;
                p.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(ParticleTypes.WHITE_ASH,false,front.x+l.random.nextGaussian()*.8,front.y+y,front.z+l.random.nextGaussian()*.8,(float)v.x,0,(float)v.z,.05F,0));
            }
        }
    }
    @Nullable public static Flow flow(ServerLevel l,LabyrinthPlace place){var b=PuzzleRooms.base(l.getServer(),place);return b==null?null:place==DRYING?drying(l,b):vents(l,b);}
}
