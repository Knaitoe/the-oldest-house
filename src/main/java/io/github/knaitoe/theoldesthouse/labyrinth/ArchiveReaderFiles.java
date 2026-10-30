package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** The seven historical reader appointments, filed beside the sealed windows. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ArchiveReaderFiles {
    public static final String STATE="archive_reader_files131",DRAWERS="archive_reader_drawers133";
    public static final String[] NAMES={"Beatrice","Leonie","Pauline","Ruth","Anne","Esther","Helen"};
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private ArchiveReaderFiles(){}
    private static net.minecraft.world.level.block.state.BlockState files(){return ServiceFittingBlock.of(ServiceFittingBlock.Kind.DRAWER,Direction.WEST);}
    private static net.minecraft.world.level.block.state.BlockState label(){return Blocks.DARK_OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING,Direction.WEST);}
    private static void words(SignBlockEntity sign,int reader){var text=sign.getFrontText().setMessage(0,Component.literal("Readers' files")).setMessage(1,Component.literal(NAMES[reader])).setMessage(2,Component.literal("Appointments"));sign.setText(text,true);sign.setWaxed(true);sign.setChanged();}
    public static void build(ServerLevel l,BlockPos b){
        for(int i=0;i<NAMES.length;i++){int z=-22-i*2,index=i;for(int y=0;y<3;y++){var cell=b.offset(11,y,z);int drawer=y;BuildBlocks.set(l,cell,files(),F);BuildBlocks.after(l,()->{if(l.getBlockEntity(cell) instanceof ServiceFittingEntity file)file.file(index,drawer);});}var at=b.offset(10,2,z);BuildBlocks.set(l,at,label(),F);BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof SignBlockEntity sign)words(sign,index);});if(i%3==0)BuildBlocks.set(l,b.offset(11,3,z),Blocks.LANTERN.defaultBlockState(),F);}
        BuildBlocks.after(l,()->{var d=LabyrinthData.get(l.getServer());var saved=d.state(STATE);saved.putInt(Long.toString(b.asLong()),NAMES.length);d.setState(STATE,saved);});
    }
    /** One recognized column per call; edited labels, containers, source books and occupied or watched cells stay intact. */
    public static boolean repair(ServerLevel l,BlockPos b){
        var d=LabyrinthData.get(l.getServer());var saved=d.state(STATE);String key=Long.toString(b.asLong());int next=saved.getInt(key);if(next>=NAMES.length)return true;
        int z=-22-next*2;var area=new AABB(Vec3.atLowerCornerOf(b.offset(10,0,z)),Vec3.atLowerCornerOf(b.offset(12,4,z+1)));
        for(int x=10;x<=11;x++){var at=b.offset(x,0,z);if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;}
        if(l.players().stream().anyMatch(p->area.inflate(24).intersects(p.getCamera().getBoundingBox()))||!l.getEntitiesOfClass(LivingEntity.class,area.inflate(.05),LivingEntity::isAlive).isEmpty())return false;
        var at=b.offset(10,2,z);var state=l.getBlockState(at);var sign=l.getBlockEntity(at) instanceof SignBlockEntity s?s:null;
        boolean original=sign!=null&&state.getBlock() instanceof StandingSignBlock&&sign.getFrontText().getMessage(0,false).getString().equals("reader "+(next+1))&&sign.getFrontText().getMessage(1,false).getString().equals(NAMES[next]);
        for(int y=0;original&&y<3;y++){var cell=b.offset(11,y,z);var old=l.getBlockState(cell);if(l.getBlockEntity(cell)!=null||!old.isAir()&&!old.is(Blocks.BOOKSHELF))original=false;}
        if(original){
            for(int y=0;y<3;y++){var cell=b.offset(11,y,z);l.setBlock(cell,files(),F);if(l.getBlockEntity(cell) instanceof ServiceFittingEntity file)file.file(next,y);}
            // Keep the actual sign BE and its filtered text/colour while removing the standing-sign post renderer.
            var wall=label();l.setBlock(at,wall,F);sign.setBlockState(wall);l.setBlockEntity(sign);words(sign,next);l.sendBlockUpdated(at,wall,wall,F);
            for(int y=0;y<2;y++){var support=b.offset(10,y,z);if(l.getBlockState(support).is(Blocks.SPRUCE_FENCE))l.setBlock(support,Blocks.AIR.defaultBlockState(),F);}
            if(next%3==0&&l.isEmptyBlock(b.offset(11,3,z)))l.setBlock(b.offset(11,3,z),Blocks.LANTERN.defaultBlockState(),F);
        }
        saved.putInt(key,next+1);d.setState(STATE,saved);return next+1>=NAMES.length;
    }
    /** Only recognized old static props change; existing native containers are never restocked. */
    public static boolean drawers(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());var saved=data.state(DRAWERS);String key=Long.toString(b.asLong());int i=saved.getInt(key);if(i>=NAMES.length)return true;
        int z=-22-i*2;var area=new AABB(Vec3.atLowerCornerOf(b.offset(10,0,z)),Vec3.atLowerCornerOf(b.offset(12,4,z+1)));
        for(int x=10;x<=11;x++){var at=b.offset(x,0,z);if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;}
        if(l.players().stream().anyMatch(p->area.inflate(24).intersects(p.getCamera().getBoundingBox()))||!l.getEntitiesOfClass(LivingEntity.class,area.inflate(.05),LivingEntity::isAlive).isEmpty())return false;
        if(l.getBlockEntity(b.offset(10,2,z)) instanceof SignBlockEntity sign&&sign.getFrontText().getMessage(1,false).getString().equals(NAMES[i]))for(int y=0;y<3;y++){
            var at=b.offset(11,y,z);var old=l.getBlockState(at);
            if(l.getBlockEntity(at)==null&&old.is(NovelRegistry.PROP.get())&&old.getValue(NovelPropBlock.KIND)==NovelPropBlock.Kind.ARCHIVE_FILES){l.setBlock(at,files(),F);if(l.getBlockEntity(at) instanceof ServiceFittingEntity drawer)drawer.file(i,y);}
        }
        saved.putInt(key,i+1);data.setState(DRAWERS,saved);return i+1>=NAMES.length;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%5!=3||LabyrinthBuilder.isCarving())return;var o=HouseSavedData.get(s).houseOrigin();var l=s.getLevel(HouseDimensions.OUTSIDE);if(o!=null&&l!=null&&LabyrinthBuilder.isPlaceReady(s,LabyrinthPlace.ZAMPANO_COURTYARD)){var b=LabyrinthPlaces.base(o,LabyrinthPlace.ZAMPANO_COURTYARD);if(repair(l,b)&&drawers(l,b))ZampanoDrawers.place(l,b);}}
}
