package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
/** A recurring room with no deadline, forced ending or exit lock. The dance continues through visits. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Nightclub {
    public static final int DANCERS=18;
    public static final BlockPos MIXER=new BlockPos(0,1,-42);
    public static final String STATE="nightclub0488";
    private Nightclub(){}
    public static AABB bounds(BlockPos b){return new AABB(b.getX()-16,b.getY(),b.getZ()-49,b.getX()+17,b.getY()+9,b.getZ()+1);}
    public static boolean inside(ServerPlayer p){var o=HouseSavedData.get(p.server).houseOrigin();return o!=null&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&bounds(LabyrinthPlaces.base(o,LabyrinthPlace.NIGHTCLUB)).contains(p.position());}
    private static void prop(ServerLevel l,BlockPos b,int x,int y,int z,NightclubBlock.Kind k,Direction face){BuildBlocks.set(l,b.offset(x,y,z),NightclubBlock.of(k,face),18);}
    public static void build(ServerLevel l,BlockPos b){
        for(int x=-16;x<=16;x++)for(int z=-49;z<=0;z++){
            BuildBlocks.set(l,b.offset(x,-1,z),NightclubBlock.of(NightclubBlock.Kind.FLOOR,Direction.NORTH),18);
            BuildBlocks.set(l,b.offset(x,9,z),NightclubBlock.of(NightclubBlock.Kind.CEILING,Direction.NORTH),18);
            for(int y=0;y<9;y++)BuildBlocks.set(l,b.offset(x,y,z),Math.abs(x)==16||z==-49||z==0?NightclubBlock.of(NightclubBlock.Kind.WALL,Direction.NORTH):Blocks.AIR.defaultBlockState(),18);
        }
        // A low vestibule keeps the first view behind a curtain-like black partition; the exit remains in reach.
        for(int x=-5;x<=5;x++)for(int y=0;y<5;y++)if(Math.abs(x)>1||y>2)prop(l,b,x,y,-8,NightclubBlock.Kind.WALL,Direction.NORTH);
        for(int y=0;y<3;y++)BuildBlocks.set(l,b.offset(0,y,0),Blocks.AIR.defaultBlockState(),18);
        for(int x=-1;x<=1;x++)for(int z=1;z<=4;z++){
            prop(l,b,x,-1,z,NightclubBlock.Kind.FLOOR,Direction.NORTH);
            for(int y=0;y<3;y++)BuildBlocks.set(l,b.offset(x,y,z),Blocks.AIR.defaultBlockState(),18);
        }
        NovelRooms.door(l,b.offset(0,0,1),Direction.SOUTH,Blocks.DARK_OAK_DOOR,true);
        for(int x=-5;x<=5;x++)for(int z=-48;z<=-41;z++)prop(l,b,x,0,z,NightclubBlock.Kind.FLOOR,Direction.NORTH);
        for(int x=-5;x<=5;x++)BuildBlocks.set(l,b.offset(x,0,-40),Blocks.DARK_OAK_SLAB.defaultBlockState(),18);
        prop(l,b,0,1,-42,NightclubBlock.Kind.MIXER,Direction.SOUTH);
        for(int x:new int[]{-13,13})for(int z:new int[]{-14,-25,-36,-46})prop(l,b,x,0,z,NightclubBlock.Kind.SPEAKER,x<0?Direction.EAST:Direction.WEST);
        for(int x:new int[]{-10,10})for(int z:new int[]{-13,-39})for(int y=0;y<9;y++)prop(l,b,x,y,z,NightclubBlock.Kind.TRIM,Direction.NORTH);
        for(int z=-16;z>=-30;z-=7){for(int x=13;x<=15;x++)prop(l,b,x,0,z,NightclubBlock.Kind.BAR,Direction.WEST);prop(l,b,14,1,z,NightclubBlock.Kind.GLASS,Direction.WEST);NovelRooms.furniture(l,b.offset(11,0,z),HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.EAST);}
        for(int z=-21;z>=-35;z-=7)prop(l,b,-14,0,z,NightclubBlock.Kind.BENCH,Direction.EAST);
        lights(l,b,true);
        // A readable, dim exit sign is a native sign rather than approximate generated lettering.
        var at=b.offset(0,3,-1);BuildBlocks.set(l,at,Blocks.DARK_OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING,Direction.NORTH),18);
        BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign){var text=sign.getFrontText().setMessage(1,net.minecraft.network.chat.Component.literal("EXIT")).setColor(net.minecraft.world.item.DyeColor.GREEN).setHasGlowingText(true);sign.setText(text,true);sign.setWaxed(true);}});
    }
    /** Three brief flashes every 130 seconds; the steady coloured lights never go dark. */
    public static boolean strobe(long tick,boolean music){long phase=Math.floorMod(tick,2600L);return music&&phase<22&&phase%10<2;}
    private static void lights(ServerLevel l,BlockPos b,boolean fresh){
        var d=LabyrinthData.get(l.getServer());var state=d.state(STATE);String key="Lights_"+b.asLong();if(!fresh&&state.getBoolean(key))return;
        if(!fresh){
            if(!l.hasChunkAt(b.offset(-14,7,-39))||!l.hasChunkAt(b.offset(14,7,-12))||!l.getBlockState(b.offset(MIXER)).is(NightclubRegistry.BLOCK.get()))return;
            if(l.players().stream().anyMatch(p->p.isAlive()&&!p.isSpectator()&&bounds(b).contains(p.position())))return;
        }
        int colour=0;
        for(int z:new int[]{-12,-26,-39})for(int x:new int[]{-14,14}){
            var at=b.offset(x,7,z);if(fresh||l.getBlockState(at).is(NightclubRegistry.BLOCK.get())&&l.getBlockState(at).getValue(NightclubBlock.KIND)==NightclubBlock.Kind.STRIP)
                l.setBlock(at,NightclubBlock.of(NightclubBlock.Kind.STRIP,Direction.NORTH).setValue(NightclubBlock.STAGE,colour++%3),3);
        }
        for(int z:new int[]{-18,-32})for(int x:new int[]{-7,0,7}){
            var old=b.offset(x,8,z);var lower=b.offset(x,4,z);
            if(!fresh&&(!l.getBlockState(old).is(NightclubRegistry.BLOCK.get())||l.getBlockState(old).getValue(NightclubBlock.KIND)!=NightclubBlock.Kind.SPOT||!l.getBlockState(lower).isAir()))continue;
            if(!fresh)l.setBlock(old,Blocks.AIR.defaultBlockState(),3);
            for(int y=5;y<=8;y++)if(fresh||l.getBlockState(b.offset(x,y,z)).isAir())l.setBlock(b.offset(x,y,z),Blocks.CHAIN.defaultBlockState(),3);
            l.setBlock(lower,NightclubBlock.of(NightclubBlock.Kind.SPOT,Direction.NORTH).setValue(NightclubBlock.STAGE,colour++%3),3);
        }
        for(int z:new int[]{-21,-35}){var at=b.offset(0,8,z);if(fresh||l.getBlockState(at).isAir())l.setBlock(at,NightclubBlock.of(NightclubBlock.Kind.STROBE,Direction.NORTH).setValue(NightclubBlock.STAGE,0),3);}
        if(fresh)prop(l,b,0,4,-2,NightclubBlock.Kind.STRIP,Direction.SOUTH);
        state.putBoolean(key,true);d.setState(STATE,state);
    }
    public static void ensureCast(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());var state=d.state(STATE);String origin=Long.toString(b.asLong());var cast=state.getCompound(origin);
        for(int i=0;i<=DANCERS;i++){
            String key="Actor"+i;if(cast.hasUUID(key))continue;var at=NightclubActor.home(b,i);var chunk=new ChunkPos(BlockPos.containing(at));if(!l.hasChunkAt(BlockPos.containing(at))||!l.areEntitiesLoaded(chunk.toLong()))continue;
            var a=NightclubRegistry.ACTOR.get().create(l);if(a==null)continue;a.bind(b,i);a.moveTo(at);a.setYRot(i==DANCERS?0:i*37);
            if(l.addFreshEntity(a))cast.putUUID(key,a.getUUID());
        }
        state.put(origin,cast);d.setState(STATE,state);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var server=e.getServer();if(server.getTickCount()%2!=0)return;var o=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);if(o==null||l==null)return;var b=LabyrinthPlaces.base(o,LabyrinthPlace.NIGHTCLUB);
        if(server.getTickCount()%10==0)lights(l,b,false);
        if(l.players().stream().noneMatch(p->p.isAlive()&&!p.isSpectator()&&bounds(b).contains(p.position())))return;
        if(server.getTickCount()%10==0){IndianLakeRooms.keepLoaded(l,b,LabyrinthPlace.NIGHTCLUB);ensureCast(l,b);}
        var mixer=l.getBlockState(b.offset(MIXER));boolean flash=strobe(l.getGameTime(),mixer.is(NightclubRegistry.BLOCK.get())&&mixer.getValue(NightclubBlock.STAGE)>0);
        for(int z:new int[]{-21,-35}){var at=b.offset(0,8,z);var s=l.getBlockState(at);if(s.is(NightclubRegistry.BLOCK.get())&&s.getValue(NightclubBlock.KIND)==NightclubBlock.Kind.STROBE&&s.getValue(NightclubBlock.STAGE)!=(flash?1:0))l.setBlock(at,s.setValue(NightclubBlock.STAGE,flash?1:0),3);}
    }
}
