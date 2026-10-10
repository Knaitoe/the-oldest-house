package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Once-only repairs of recognized authored fixtures; never a scene rebuild, actor replacement or restock. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PlaytestFixes {
    public static final String STATE="playtest_fixes_0468";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private static final List<LabyrinthPlace> SITES=List.of(LabyrinthPlace.GOATMAN,LabyrinthPlace.WHALE,LabyrinthPlace.ZAMPANO_COURTYARD,LabyrinthPlace.DROWNED_TOWN);
    private PlaytestFixes(){}
    private static String key(BlockPos b,LabyrinthPlace p){return b.asLong()+":"+p.id();}
    private static boolean loaded(ServerLevel l,BlockPos at){return l.hasChunkAt(at)&&l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4));}
    private static boolean safe(ServerLevel l,BlockPos at,BlockState next,boolean source){
        if(!loaded(l,at)||l.players().stream().anyMatch(p->new AABB(at).inflate(48).intersects(p.getCamera().getBoundingBox())))return false;
        if(!l.getEntitiesOfClass(Entity.class,new AABB(at).inflate(.02)).isEmpty())return false;
        if(l.getBlockEntity(at)!=null&&!(source&&(l.getBlockEntity(at) instanceof SignBlockEntity||l.getBlockEntity(at) instanceof LecternBlockEntity)))return false;
        for(Direction d:Direction.values()){var near=at.relative(d);var s=l.getBlockState(near);if(s.is(HouseBlocks.SCENE_DETAIL.get())){var k=s.getValue(SceneDetailBlock.KIND);if((k.wall()?near.relative(s.getValue(SceneDetailBlock.FACING).getOpposite()):near.below()).equals(at))return false;}}
        return true;
    }
    private static boolean notice(SignBlockEntity s,String[] expected){for(int i=0;i<4;i++)if(!s.getFrontText().getMessage(i,false).getString().equals(expected[i])||!s.getBackText().getMessage(i,false).getString().isEmpty())return false;return true;}
    private static void sign(ServerLevel l,BlockPos at,Direction facing,InstituteSignBlock.Kind kind,String[] lines,Map<BlockPos,BlockState> edits){if(l.getBlockEntity(at) instanceof SignBlockEntity s&&notice(s,lines))edits.put(at,InstituteSignBlock.of(kind,facing));}
    /** Returns false while native entity sections, a camera or a body still occupies a changed fixture. */
    public static boolean apply(ServerLevel l,BlockPos b,LabyrinthPlace p){
        var data=LabyrinthData.get(l.getServer());var done=data.state(STATE);String key=key(b,p);if(done.getBoolean(key))return true;
        var area=IndianLakeRooms.bounds(b,p);for(int x=(int)Math.floor(area.minX)>>4;x<=(int)Math.floor(area.maxX)>>4;x++)for(int z=(int)Math.floor(area.minZ)>>4;z<=(int)Math.floor(area.maxZ)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(l.players().stream().anyMatch(player->area.inflate(48).intersects(player.getCamera().getBoundingBox())))return false;
        var edits=new LinkedHashMap<BlockPos,BlockState>();BlockPos note=null;
        if(p==LabyrinthPlace.GOATMAN){
            var cooler=b.offset(GoatmanWoods.COOLER);if(l.getBlockState(cooler).isAir())edits.put(cooler,GoatmanRegistry.COOLER.get().defaultBlockState().setValue(TrailerFixtureBlock.FACING,Direction.SOUTH));
            for(int x:new int[]{-9,9})for(int z:new int[]{-59,-73}){var at=b.offset(x,0,z);if(l.getBlockState(at).is(Blocks.BLACK_CONCRETE))edits.put(at,GoatmanRegistry.WHEEL.get().defaultBlockState().setValue(TrailerFixtureBlock.FACING,x<0?Direction.WEST:Direction.EAST));}
        }else if(p==LabyrinthPlace.WHALE){
            for(int i=0;i<=5;i++)for(int z:new int[]{-31,-30})for(int y=i+1;y<=i+3;y++){var at=b.offset(-6-i,y,z);var old=l.getBlockState(at);if(old.is(Blocks.SMOOTH_STONE)||old.is(Blocks.SPRUCE_FENCE)||old.is(Blocks.SPRUCE_PLANKS))edits.put(at,Blocks.AIR.defaultBlockState());}
            // Complete the existing attic roof if an older carve left its ceiling open to the stack.
            for(int x=-13;x<=13;x++)for(int z=-32;z<=-25;z++){var at=b.offset(x,10,z);if(l.getBlockState(at).isAir())edits.put(at,Blocks.DARK_OAK_PLANKS.defaultBlockState());}
            sign(l,b.offset(12,3,-8),Direction.WEST,InstituteSignBlock.Kind.POST,new String[]{"","POST","",""},edits);
            sign(l,b.offset(-12,2,-4),Direction.EAST,InstituteSignBlock.Kind.HOURS,new String[]{"VISITING HOURS","","none at present",""},edits);
            sign(l,b.offset(-12,2,-7),Direction.EAST,InstituteSignBlock.Kind.WRITING,new String[]{"Patients may","write as often","as they like.",""},edits);
            var outgoing=b.offset(4,1,-8);if(l.getBlockState(outgoing).is(HouseBlocks.MAIL_PLAQUE.get()))edits.put(outgoing,InstituteSignBlock.of(InstituteSignBlock.Kind.OUTGOING,Direction.SOUTH));
            for(int i=0;i<4;i++)for(int side:new int[]{-1,1}){int z=-11-i*4,n=2*i+(side>0?1:2);var old=b.offset(side,2,z+1);var at=b.offset(side,2,z);String number=n==7?"·      ·":Integer.toString(n);
                if(l.getBlockEntity(old) instanceof SignBlockEntity s&&notice(s,new String[]{"",number,"",""})&&l.getBlockState(at).isAir()){edits.put(old,Blocks.AIR.defaultBlockState());edits.put(at,InstituteSignBlock.of(n==7?InstituteSignBlock.Kind.MISSING:InstituteSignBlock.Kind.valueOf("ROOM_"+n),side>0?Direction.WEST:Direction.EAST));}}
        }else if(p==LabyrinthPlace.ZAMPANO_COURTYARD){
            for(int y=0;y<=6;y++)for(int z=-38;z<=-19;z++)for(int x=-12;x<=12;x++)if(z==-19||x==-12||x==12){var at=b.offset(x,y,z);var old=l.getBlockState(at);if(old.is(NovelRegistry.PAPER.get())||old.is(NovelRegistry.PLASTER.get()))edits.put(at,(y==0||y==6?Blocks.STONE_BRICKS:y==2?Blocks.POLISHED_ANDESITE:Blocks.LIGHT_GRAY_TERRACOTTA).defaultBlockState());}
        }else if(p==LabyrinthPlace.DROWNED_TOWN){
            var accepted=Set.of(Blocks.POLISHED_BLACKSTONE,Blocks.RED_CONCRETE,Blocks.BLUE_CONCRETE,Blocks.GREEN_TERRACOTTA,Blocks.WHITE_CONCRETE,Blocks.LIGHT_BLUE_TERRACOTTA,Blocks.BROWN_TERRACOTTA,Blocks.CYAN_TERRACOTTA,Blocks.ORANGE_TERRACOTTA,Blocks.BLACK_STAINED_GLASS,Blocks.SMOOTH_STONE_SLAB,Blocks.RED_NETHER_BRICK_SLAB,Blocks.WARPED_SLAB,Blocks.SMOOTH_QUARTZ_SLAB,Blocks.CUT_RED_SANDSTONE_SLAB);
            for(var e:ProofrockTown.cars(b).entrySet())if(accepted.contains(l.getBlockState(e.getKey()).getBlock()))edits.put(e.getKey(),e.getValue());
            note=b.offset(ProofrockTown.LECTERN);if(l.getBlockState(note).is(Blocks.LECTERN)&&l.getBlockEntity(note) instanceof LecternBlockEntity)edits.put(note,DrownedTownRegistry.NOTICE.get().defaultBlockState().setValue(NoticePostBlock.FACING,Direction.SOUTH));
        }
        for(var e:edits.entrySet())if(!safe(l,e.getKey(),e.getValue(),true))return false;
        // Put the exact original in saved custody before clearing a lectern that would otherwise drop it.
        ItemStack original=ItemStack.EMPTY;
        if(note!=null&&edits.containsKey(note)&&l.getBlockEntity(note) instanceof LecternBlockEntity lectern){original=lectern.getBook().copy();if(!original.isEmpty()){done.put(key+":Note",original.save(l.registryAccess()));data.setState(STATE,done);}SceneHuntReview.emptyLectern(l,note);}
        for(var e:edits.entrySet())l.setBlock(e.getKey(),e.getValue(),F);
        if(note!=null&&l.getBlockEntity(note) instanceof NoticePostBlockEntity post&&done.contains(key+":Note")){post.book(ItemStack.parseOptional(l.registryAccess(),done.getCompound(key+":Note")));done.remove(key+":Note");}
        done.putBoolean(key,true);data.setState(STATE,done);return true;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=13||LabyrinthBuilder.isCarving())return;var o=HouseSavedData.get(s).houseOrigin();if(o==null)return;
        var data=LabyrinthData.get(s);for(var p:SITES){var l=s.getLevel(NovelRooms.dimension(p));var b=LabyrinthPlaces.base(o,p);if(l!=null&&b!=null&&LabyrinthBuilder.isPlaceReady(data,p)&&!l.getBlockState(b.offset(0,-1,-3)).isAir())apply(l,b,p);}}
}
