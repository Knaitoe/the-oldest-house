package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

/** Small once-only repairs; originals and the working encounter never get restaged. */
public final class FinaleRepairs {
    private static final String STATE="finale_repairs_0430";
    private static final String ENTRANCE="staircase_entrance_0443";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private FinaleRepairs(){}
    public static BlockPos tom(BlockPos origin){return FinaleArchitecture.base(origin).offset(-12,FinaleArchitecture.TOP,22);}
    public static BlockPos paper(BlockPos landing,BlockPos b,int turn){
        int sx=Integer.signum(landing.getX()-b.getX()),sz=Integer.signum(landing.getZ()-b.getZ());
        int[][] offsets={{-4,-2},{2,-4},{-2,4},{4,2},{-5,3},{3,-5}};
        var at=offsets[Math.floorMod(turn/3,offsets.length)];return landing.offset(sx*at[0],0,sz*at[1]);
    }
    public static Direction paperFacing(int turn){return Direction.from2DDataValue(Math.floorMod(turn/3,4));}
    public static void campPlan(Map<BlockPos,BlockState> plan,BlockPos b){
        int top=FinaleArchitecture.TOP;var wall=Blocks.DEEPSLATE_TILES.defaultBlockState();
        for(int x=-15;x<=-8;x++)for(int z=19;z<=26;z++){
            plan.put(b.offset(x,top-1,z),Blocks.DARK_OAK_PLANKS.defaultBlockState());
            for(int y=top;y<=top+4;y++)plan.put(b.offset(x,y,z),x==-15||z==19||z==26||y==top+4
                    ||x==-8&&(z<23||z>25)?wall:Blocks.AIR.defaultBlockState());
        }
        // Join the camp to the actual first tread, on the staircase side of the copied vestibule.
        for(int x=-7;x<=-1;x++)for(int z=22;z<=26;z++){
            plan.put(b.offset(x,top-1,z),Blocks.DARK_OAK_PLANKS.defaultBlockState());
            for(int y=top;y<=top+3;y++)plan.put(b.offset(x,y,z),y==top&&(z==22||z==26)
                    ?Blocks.IRON_BARS.defaultBlockState():Blocks.AIR.defaultBlockState());
        }
    }
    /** Only this small entrance waits for visitors; an explorer far down the shaft cannot starve its repair. */
    public static void repairEntrance(ServerLevel level,BlockPos origin){
        var data=LabyrinthData.get(level.getServer());var state=data.state(ENTRANCE);String key=Long.toString(origin.asLong());
        if(state.getBoolean(key))return;
        var b=FinaleArchitecture.base(origin);int top=FinaleArchitecture.TOP;
        var area=new AABB(b.offset(-16,top-2,18),b.offset(8,top+7,35));
        if(level.players().stream().anyMatch(p->area.intersects(p.getBoundingBox())))return;
        for(int x=(b.getX()-16)>>4;x<=(b.getX()+7)>>4;x++)
            for(int z=(b.getZ()+18)>>4;z<=(b.getZ()+34)>>4;z++)
                if(!level.hasChunkAt(new BlockPos(x<<4,top,z<<4)))return;
        var camp=new LinkedHashMap<BlockPos,BlockState>();campPlan(camp,b);
        camp.forEach((at,block)->{var old=level.getBlockState(at);if(level.getBlockEntity(at)==null
                &&(old.isAir()||old.is(Blocks.DEEPSLATE_TILES)||old.is(Blocks.DARK_OAK_PLANKS)))level.setBlock(at,block,F);});
        if(!StaircaseFire.moveShelf(level,origin))return;
        var fireTo=b.offset(-13,top,24);
        for(var from:List.of(b.offset(-6,top,20),b.offset(-7,top,32))){
            var fire=level.getBlockState(from);
            if(fire.is(Blocks.CAMPFIRE)&&level.getBlockState(fireTo).isAir()){
                var entity=level.getBlockEntity(from);var saved=entity==null?null:entity.saveWithFullMetadata(level.registryAccess());
                level.setBlock(fireTo,fire,F);
                if(saved!=null&&level.getBlockEntity(fireTo)!=null){saved.putInt("x",fireTo.getX());saved.putInt("y",fireTo.getY());saved.putInt("z",fireTo.getZ());level.getBlockEntity(fireTo).loadWithComponents(saved,level.registryAccess());}
                level.removeBlockEntity(from);level.setBlock(from,Blocks.AIR.defaultBlockState(),F);
            }
        }
        for(var old:List.of(b.offset(-4,top,18),b.offset(-6,top,30)))
            for(var actor:level.getEntitiesOfClass(NovelActor.class,new AABB(old).inflate(2),a->a.role()==0))
                actor.moveTo(Vec3.atBottomCenterOf(tom(origin)));
        state.putBoolean(key,true);data.setState(ENTRANCE,state);
    }
    public static void tick(ServerLevel level,BlockPos origin){
        if(level.getGameTime()%20!=0)return;var data=LabyrinthData.get(level.getServer());var state=data.state(STATE);String key=Long.toString(origin.asLong());
        if(state.getBoolean(key)||level.players().stream().anyMatch(p->FinaleArchitecture.contains(origin,p.blockPosition())))return;
        apply(level,origin);state.putBoolean(key,true);data.setState(STATE,state);
    }
    public static void apply(ServerLevel level,BlockPos origin){
        var b=FinaleArchitecture.base(origin);var camp=new LinkedHashMap<BlockPos,BlockState>();campPlan(camp,b);
        if(!LabyrinthData.get(level.getServer()).state(ENTRANCE).getBoolean(Long.toString(origin.asLong())))
            camp.forEach((at,block)->{var old=level.getBlockState(at);if(level.getBlockEntity(at)==null&&(old.isAir()||old.is(Blocks.DEEPSLATE_TILES)||old.is(Blocks.DARK_OAK_PLANKS)))level.setBlock(at,block,F);});
        // Move the actual cold/lit campfire and actor; never create another reward or UUID.
        var oldFire=b.offset(-6,FinaleArchitecture.TOP,20);var newFire=b.offset(-13,FinaleArchitecture.TOP,24);var fire=level.getBlockState(oldFire);
        if(fire.is(Blocks.CAMPFIRE)&&level.getBlockState(newFire).isAir()){
            var entity=level.getBlockEntity(oldFire);CompoundTag saved=entity==null?null:entity.saveWithFullMetadata(level.registryAccess());
            level.setBlock(newFire,fire,F);if(saved!=null&&level.getBlockEntity(newFire)!=null){saved.putInt("x",newFire.getX());saved.putInt("y",newFire.getY());saved.putInt("z",newFire.getZ());level.getBlockEntity(newFire).loadWithComponents(saved,level.registryAccess());}
            level.removeBlockEntity(oldFire);level.setBlock(oldFire,Blocks.AIR.defaultBlockState(),F);
        }
        var oldTom=b.offset(-4,FinaleArchitecture.TOP,18);
        for(var actor:level.getEntitiesOfClass(NovelActor.class,new AABB(oldTom).inflate(2),a->a.role()==0))actor.moveTo(Vec3.atBottomCenterOf(tom(origin)));
        var wall=Blocks.DEEPSLATE_TILES.defaultBlockState();var world=FinaleProgress.world(level.getServer());
        boolean broken=world.getBoolean("MinotaurWounded")||world.getBoolean("Ended");
        for(int x=-17;x<=17;x++)for(int z=30;z<=69;z++)for(int y=FinaleArchitecture.ARENA;y<=FinaleArchitecture.ARENA+13;y++){
            boolean boundary=x==-17||x==17||z==30||z==69||y==FinaleArchitecture.ARENA+13;
            if(!boundary||z==30&&Math.abs(x)<=1&&y<FinaleArchitecture.ARENA+4
                    ||Math.abs(x)==17&&(z==36||z==37)&&y<FinaleArchitecture.ARENA+3
                    ||broken&&x==-17&&z>=63&&z<=67&&y<FinaleArchitecture.ARENA+4)continue;
            var at=b.offset(x,y,z);if(level.getBlockState(at).isAir())level.setBlock(at,wall,F);
        }
        // Repair only outer maze caps. Existing paths and deliberate side entrances remain open.
        for(int side:new int[]{-1,1}){int shift=side<0?-30:0;
            for(int x=18;x<=78;x++)for(int z=32;z<=67;z++)for(int y=FinaleArchitecture.ARENA;y<=FinaleArchitecture.ARENA+4;y++){
                if(x!=18&&x!=78&&z!=32&&z!=67&&y!=FinaleArchitecture.ARENA+4)continue;
                if(x==18&&(z+shift==36||z+shift==37)&&y<FinaleArchitecture.ARENA+3)continue;
                var at=b.offset(side*x,y,z+shift);if(level.getBlockState(at).isAir())level.setBlock(at,wall,F);
            }
        }
        int turn=0;for(var at:FinaleArchitecture.fullRoute(origin))if(Math.abs(at.getX()-b.getX())==FinaleArchitecture.STAIR_RADIUS&&Math.abs(at.getZ()-b.getZ())==FinaleArchitecture.STAIR_RADIUS){
            int sx=Integer.signum(at.getX()-b.getX()),sz=Integer.signum(at.getZ()-b.getZ());var from=at.offset(-sx*3,0,-sz*3);var to=paper(at,b,turn);
            var old=level.getBlockState(from);
            if(turn%3==1&&!from.equals(to)&&old.is(HouseBlocks.NOTE_SURFACE.get())&&old.getValue(NoteSurfaceBlock.THREAD)==HouseMarginalia.Thread.POEMS&&level.getBlockState(to).isAir()&&NoteSurfaceBlock.supported(level,to)){
                level.setBlock(to,old.setValue(NoteSurfaceBlock.FACING,paperFacing(turn)),F);level.setBlock(from,Blocks.AIR.defaultBlockState(),F);StaircaseWriting.moved(level.getServer(),from,to);
            }turn++;
        }
    }
}
