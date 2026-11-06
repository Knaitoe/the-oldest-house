package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** An alignment to a real field path, and a flood that carries people to an accessible downstream turnout. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class YardFields {
    private YardFields(){}
    public static final String STATE="yard_fields_0314";
    public static final BlockPos PAD=new BlockPos(0,-1,-9),GRATE=new BlockPos(-2,1,-7);
    public static final int[] GATES={-8,0,8};
    public static final int CYCLE=360,WARNING=180,RISING=220,SURGE=260,DRAIN=320;
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){YardRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){YardRooms.set(l,b,x,y,z,s);}
    static void put(ServerLevel l,BlockPos b,BlockPos at,BlockState s){YardRooms.set(l,b,at,s);}
    public static CompoundTag state(ServerLevel l,LabyrinthPlace p){return LabyrinthData.get(l.getServer()).stateEntry(STATE,p.id());}
    private static void save(ServerLevel l,LabyrinthPlace p,CompoundTag s){LabyrinthData.get(l.getServer()).setStateEntry(STATE,p.id(),s);}
    public static int live(ServerLevel l){return state(l,LabyrinthPlace.YARD_SURVEY).getInt("Live");}
    public static int phase(long clock){int t=(int)Math.floorMod(clock,CYCLE);return t<WARNING?0:t<RISING?1:t<SURGE?2:t<DRAIN?3:4;}
    public static boolean is(LabyrinthPlace p){return p==LabyrinthPlace.YARD_SURVEY||p==LabyrinthPlace.YARD_FLOOD;}
    private static void ground(ServerLevel l,BlockPos b,int half,int end){
        box(l,b,-half,-3,end,half,-1,0,Blocks.DIRT.defaultBlockState());box(l,b,-half,-1,end,half,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());
        box(l,b,-half,0,end,half,7,0,YardRooms.leaves());
    }
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace p){if(p==LabyrinthPlace.YARD_SURVEY)survey(l,b);else flood(l,b);}
    private static void survey(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();ground(l,b,16,-43);
        box(l,b,-13,0,-33,13,10,-5,air);box(l,b,-1,0,-5,1,10,-1,air);
        put(l,b,PAD,Blocks.SMOOTH_STONE.defaultBlockState());
        var s=state(l,LabyrinthPlace.YARD_SURVEY);if(!s.contains("Live")){s.putInt("Live",Math.floorMod(Long.hashCode(b.asLong()),3));save(l,LabyrinthPlace.YARD_SURVEY,s);}
        int x=GATES[s.getInt("Live")];
        for(int z:new int[]{-13,-17}){
            int column=z==-13?x/4:x/2;
            put(l,b,column,0,z,Blocks.STRIPPED_OAK_LOG.defaultBlockState());
            put(l,b,column,1,z,YardBlock.of(YardBlock.Kind.SURVEY_SLOT,Direction.NORTH,0));
        }
        for(int col=-14;col<=14;col++)put(l,b,col,0,-25,Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST,true).setValue(FenceBlock.WEST,true));
        for(int gate:GATES){
            put(l,b,gate,0,-25,Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.NORTH));
            put(l,b,gate+1,1,-24,YardBlock.of(YardBlock.Kind.GATE_LATCH,Direction.SOUTH,0));
        }
        // All gates enter the pasture. Only the aligned line continues into the dip and under the hill beyond.
        box(l,b,-13,0,-38,13,3,-34,Blocks.GRASS_BLOCK.defaultBlockState());
        for(int z=-26;z>=-36;z--){
            int top=z>=-26?-1:z>=-28?-2:-3;
            box(l,b,x-1,top+1,z,x+1,3,z,air);
            box(l,b,x-1,top,z,x+1,top,z,Blocks.COARSE_DIRT.defaultBlockState());
            if(z==-27||z==-29)box(l,b,x-1,top+1,z,x+1,top+1,z,Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        }
        // A sheltered path bends behind the barn and climbs in half-steps to its back gate.
        box(l,b,Math.min(x,8)-1,-2,-37,Math.max(x,8)+1,2,-36,air);
        box(l,b,Math.min(x,8)-1,-3,-37,Math.max(x,8)+1,-3,-36,Blocks.COARSE_DIRT.defaultBlockState());
        for(int z=-38;z>=-41;z--){int floor=z==-38?-3:z>=-40?-2:-1;
            box(l,b,7,floor+1,z,9,4,z,air);box(l,b,7,floor,z,9,floor,z,Blocks.COARSE_DIRT.defaultBlockState());
            if(z==-38||z==-40)box(l,b,7,floor+1,z,9,floor+1,z,Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        }
        box(l,b,-12,0,-41,4,4,-39,Blocks.DARK_OAK_PLANKS.defaultBlockState());
        box(l,b,-13,5,-42,5,5,-38,Blocks.DARK_OAK_SLAB.defaultBlockState());
        LabyrinthBuilder.entrance(l,b,Blocks.DARK_OAK_PLANKS.defaultBlockState(),Blocks.GRASS_BLOCK.defaultBlockState(),Blocks.SPRUCE_SLAB.defaultBlockState());
        LabyrinthBuilder.doors(l,b,LabyrinthPlace.YARD_SURVEY);
    }
    private static void flood(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();ground(l,b,8,-39);box(l,b,-1,0,-37,1,10,-1,air);
        // Low contained channel, with enough air above even at the height of the surge.
        for(int z=-7;z>=-30;z--){
            box(l,b,-1,-1,z,1,3,z,air);box(l,b,-1,-2,z,1,-2,z,Blocks.STONE.defaultBlockState());
            for(int x:new int[]{-2,2})box(l,b,x,-1,z,x,1,z,Blocks.STONE.defaultBlockState());
            box(l,b,-1,-1,z,1,-1,z,Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM).setValue(SlabBlock.WATERLOGGED,true));
        }
        put(l,b,GRATE,YardBlock.of(YardBlock.Kind.FLOOD_GRATE,Direction.EAST,0));
        // The recess is above the current, with a stair out of the channel.
        box(l,b,2,1,-20,5,4,-17,air);box(l,b,2,0,-20,5,0,-17,Blocks.STONE.defaultBlockState());
        put(l,b,1,-1,-18,Blocks.STONE.defaultBlockState());
        put(l,b,2,0,-18,Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        put(l,b,4,3,-19,YardBlock.of(YardBlock.Kind.BULB,Direction.WEST,0));
        // Downstream turnout, broad enough to gather in; a return path rejoins the high bank.
        box(l,b,-5,0,-36,5,5,-31,air);box(l,b,-5,-1,-36,5,-1,-31,Blocks.GRAVEL.defaultBlockState());
        // Longer embankment from the entrance, always available, with ordinary half-step approaches.
        for(int z=-4;z>=-34;z--){int y=z>-8?(int)Math.floor((-z-4)/2.0):z<-30?Math.max(0,(34+z)/2):2;
            box(l,b,-5,y,z,-3,y+3,z,air);box(l,b,-5,y-1,z,-3,y-1,z,Blocks.COARSE_DIRT.defaultBlockState());
            if(z%2!=0&&z>-8)box(l,b,-5,y,z,-3,y,z,Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
            if(z==-31||z==-33)box(l,b,-5,y,z,-3,y,z,Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        }
        box(l,b,-4,0,-4,-1,3,-4,air);box(l,b,-4,-1,-4,-1,-1,-4,Blocks.COARSE_DIRT.defaultBlockState());
        put(l,b,2,2,-35,YardBlock.of(YardBlock.Kind.BULB,Direction.NORTH,0));
        LabyrinthBuilder.entrance(l,b,Blocks.STONE.defaultBlockState(),Blocks.GRASS_BLOCK.defaultBlockState(),Blocks.SPRUCE_SLAB.defaultBlockState());
        LabyrinthBuilder.doors(l,b,LabyrinthPlace.YARD_FLOOD);
        var s=state(l,LabyrinthPlace.YARD_FLOOD);s.putInt("DrawnPhase",-1);save(l,LabyrinthPlace.YARD_FLOOD,s);
    }
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState block){
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null||!p.level().dimension().equals(HouseDimensions.OUTSIDE))return false;
        var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());if(place!=LabyrinthPlace.YARD_SURVEY)return false;
        var b=LabyrinthPlaces.base(origin,place);var r=pos.subtract(b);
        if(YardBlock.is(block,YardBlock.Kind.SURVEY_SLOT)){PuzzleRooms.say(p,"A narrow slot cut through the stake. The old concrete pad is behind you.");return true;}
        if(!YardBlock.is(block,YardBlock.Kind.GATE_LATCH))return false;
        for(int x:GATES)if(r.equals(new BlockPos(x+1,1,-24))){
            var at=b.offset(x,0,-25);var gate=p.serverLevel().getBlockState(at);
            if(gate.is(Blocks.OAK_FENCE_GATE)){p.serverLevel().setBlock(at,gate.cycle(FenceGateBlock.OPEN),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);p.serverLevel().playSound(null,at,SoundEvents.FENCE_GATE_OPEN,SoundSource.BLOCKS,.6F,1F);}
            return true;
        }
        return false;
    }
    private static void water(ServerLevel l,BlockPos b,int phase){
        for(int z=-8;z>=-29;z--)for(int x=-1;x<=1;x++){
            var at=b.offset(x,0,z);var old=l.getBlockState(at);
            boolean wet=phase==2||phase==3;
            if(wet&&(old.isAir()||old.is(Blocks.WATER)))l.setBlock(at,Blocks.WATER.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
            else if(!wet&&old.is(Blocks.WATER))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        }
    }
    private static void updateFlood(ServerLevel l,BlockPos b,CompoundTag s){
        long clock=s.getLong("Clock")+1;s.putLong("Clock",clock);int stage=phase(clock);
        if(stage!=s.getInt("DrawnPhase")){
            water(l,b,stage);var grate=l.getBlockState(b.offset(GRATE));
            if(YardBlock.is(grate,YardBlock.Kind.FLOOD_GRATE))put(l,b,GRATE,grate.setValue(YardBlock.STAGE,Math.min(3,stage)));
            s.putInt("DrawnPhase",stage);
        }
        if(stage>0&&stage<4&&clock%20==0){
            l.playSound(null,b.offset(GRATE),stage==1?SoundEvents.GRAVEL_HIT:PuzzleRegistry.YARD_CREEK.get(),SoundSource.AMBIENT,stage==1?.55F:1.2F,.8F);
            l.sendParticles(ParticleTypes.SPLASH,b.getX()-.5,b.getY()+.3,b.getZ()-7.5,8,.3,.2,.6,.03);
        }
        if(stage==3){
            var current=new AABB(b.getX()-1,b.getY()-1,b.getZ()-30,b.getX()+2,b.getY()+2,b.getZ()-7);
            for(var e:l.getEntitiesOfClass(LivingEntity.class,current,e->e.isAlive()&&!e.isSpectator())){
                var velocity=e.getDeltaMovement();e.setDeltaMovement(Math.max(-.2,Math.min(.2,velocity.x)),Math.max(velocity.y,.04),Math.max(-.32,velocity.z-.07));e.hurtMarked=true;e.fallDistance=0;e.setAirSupply(e.getMaxAirSupply());
            }
        }
        save(l,LabyrinthPlace.YARD_FLOOD,s);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.OUTSIDE);if(origin==null||l==null)return;
        boolean flooding=false,reunion=false;var survey=LabyrinthPlaces.base(origin,LabyrinthPlace.YARD_SURVEY);
        for(var p:l.players()){
            var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());
            if(place==LabyrinthPlace.YARD_FLOOD)flooding=true;if(place==LabyrinthPlace.YARD_REUNION)reunion=true;
            if(place==LabyrinthPlace.YARD_SURVEY&&p.getZ()<survey.getZ()-25.5&&p.getZ()>survey.getZ()-29&&Math.abs(p.getX()-survey.getX()-GATES[live(l)]-.5)<1.5)
                PuzzleRooms.solve(l,place,"Beyond the lined-up stakes, the path drops out of sight. The shelter behind the barn is there.");
        }
        if(flooding){var b=LabyrinthPlaces.base(origin,LabyrinthPlace.YARD_FLOOD);if(l.hasChunkAt(b))updateFlood(l,b,state(l,LabyrinthPlace.YARD_FLOOD));}
        if(reunion){var b=LabyrinthPlaces.base(origin,LabyrinthPlace.YARD_REUNION);long now=l.getGameTime();
            if(now%40==0)l.playSound(null,b.offset(YardRooms.NOSE_AT),PuzzleRegistry.YARD_IDLE.get(),SoundSource.AMBIENT,1,1);
            if(now%320==0)l.playSound(null,b.offset(6,8,-29),PuzzleRegistry.YARD_TRAFFIC.get(),SoundSource.AMBIENT,.8F,1);
            if(now%500==0)l.playSound(null,b.offset(YardRooms.DEAD_TREE).above(5),PuzzleRegistry.YARD_CROW.get(),SoundSource.AMBIENT,.7F,1);
        }
    }
}
