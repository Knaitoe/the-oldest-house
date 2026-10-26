package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** A bounded, vacant-room anatomy refit; actual cuts, original bodies and finite keys remain. */
public final class UncannyAnatomyUpgrade {
    public static final String STATE="uncanny_anatomy_0127";
    private UncannyAnatomyUpgrade(){}
    private static boolean old(BlockPos r){
        int x=r.getX(),y=r.getY(),z=r.getZ();if(r.equals(UncannyRooms.KEY_CAVITY))return false;
        if(y==-2)return Math.abs(x)<=1&&z>=-22&&z<=-20;
        if(y==-3)return Math.abs(x)<=1&&z>=-23&&z<=-19;
        return y>=-5&&y<=-4&&Math.abs(x)<=2&&z>=-23&&z<=-18&&(Math.abs(x)<2||z>=-22&&z<=-19);
    }
    public static void tick(MinecraftServer server){
        if(server instanceof net.minecraft.gametest.framework.GameTestServer||server.getTickCount()%20!=0||LabyrinthBuilder.isCarving())return;
        var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;var data=LabyrinthData.get(server);
        if(!origin.equals(data.builtOrigin())||data.door(LabyrinthPlace.LAYERED_MAN.entryDoorId())==null)return;
        var level=server.getLevel(NovelRooms.dimension(LabyrinthPlace.LAYERED_MAN));if(level!=null)step(level,LabyrinthPlaces.base(origin,LabyrinthPlace.LAYERED_MAN));
    }
    public static boolean step(ServerLevel level,BlockPos b){
        var data=LabyrinthData.get(level.getServer());String key=Long.toString(b.asLong());var progress=data.stateEntry(STATE,key);if(progress.getBoolean("Done"))return true;
        var area=new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(-8,-7,-30)),net.minecraft.world.phys.Vec3.atLowerCornerOf(b.offset(9,6,1)));
        for(int x=(b.getX()-8)>>4;x<=(b.getX()+8)>>4;x++)for(int z=(b.getZ()-30)>>4;z<=b.getZ()>>4;z++)if(!level.hasChunk(x,z)||!level.areEntitiesLoaded(ChunkPos.asLong(x,z)))return false;
        if(level.players().stream().anyMatch(p->area.inflate(12).contains(p.getCamera().position())))return false;
        var world=data.stateEntry(UncannyStories.WORLD,LabyrinthPlace.LAYERED_MAN.id()+":"+b.asLong());var cuts=world.getList("Regrow",Tag.TAG_COMPOUND);
        int cursor=progress.getInt("Cursor"),visited=0;long until=System.nanoTime()+3_000_000L;
        while(cursor<252&&visited++<128&&System.nanoTime()<until){
            int i=cursor;var r=new BlockPos(-3+i%7,-5+i/63,-25+i/7%9);var at=b.offset(r);var actual=level.getBlockState(at);
            boolean wanted=UncannyCellar.authored(r.getX(),r.getY(),r.getZ());boolean cut=cuts.stream().anyMatch(t->((net.minecraft.nbt.CompoundTag)t).getLong("At")==r.asLong());
            if(level.getBlockEntity(at)==null&&!cut&&!r.equals(UncannyRooms.KEY_CAVITY)){
                var next=wanted?UncannyFleshBlock.at(r):Blocks.AIR.defaultBlockState();
                boolean recognized=UncannyFleshBlock.is(actual)||actual.isAir()&&wanted&&!old(r);
                if(recognized&&(wanted||old(r))&&!actual.equals(next)){
                    if(!level.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.02),LivingEntity::isAlive).isEmpty())break;
                    level.setBlock(at,next,UncannyRooms.FLAGS);
                }
            }
            cursor++;
        }
        progress.putInt("Cursor",cursor);if(cursor>=252)progress.putBoolean("Done",true);data.setStateEntry(STATE,key,progress);return progress.getBoolean("Done");
    }
}
