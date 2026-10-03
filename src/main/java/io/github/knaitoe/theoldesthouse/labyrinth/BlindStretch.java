package io.github.knaitoe.theoldesthouse.labyrinth;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
/** Physical turns, honest spatial bells and a separate lying subtitle channel. No Witness source. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BlindStretch {
    public static final String STATE="blind_stretch_0435";
    public static final List<BlockPos> TURNS=List.of(new BlockPos(0,0,-10),new BlockPos(10,0,-10),new BlockPos(10,0,-24),new BlockPos(-10,0,-24),new BlockPos(-10,0,-38),new BlockPos(0,0,-38));
    private BlindStretch(){}
    public static void build(ServerLevel l,BlockPos b){
        int f=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
        LabyrinthBuilder.room(l,b,-3,3,3,-4,-1,Blocks.GRAY_CONCRETE.defaultBlockState(),Blocks.SMOOTH_STONE.defaultBlockState(),Blocks.GRAY_CONCRETE.defaultBlockState());
        BlockPos previous=new BlockPos(0,0,-3);for(var turn:TURNS){NovelRooms.box(l,b,Math.min(previous.getX(),turn.getX())-1,0,Math.min(previous.getZ(),turn.getZ())-1,Math.max(previous.getX(),turn.getX())+1,2,Math.max(previous.getZ(),turn.getZ())+1,Blocks.AIR.defaultBlockState());NovelRooms.box(l,b,Math.min(previous.getX(),turn.getX())-1,-1,Math.min(previous.getZ(),turn.getZ())-1,Math.max(previous.getX(),turn.getX())+1,-1,Math.max(previous.getZ(),turn.getZ())+1,Blocks.SMOOTH_STONE.defaultBlockState());previous=turn;}
        // Two blind spurs join the same route again. The continuous wall is a usable return aid.
        NovelRooms.box(l,b,-1,0,-17,1,2,-10,Blocks.AIR.defaultBlockState());NovelRooms.box(l,b,-1,-1,-17,1,-1,-10,Blocks.SMOOTH_STONE.defaultBlockState());
        NovelRooms.box(l,b,-1,0,-41,1,2,-38,Blocks.AIR.defaultBlockState());NovelRooms.box(l,b,-1,-1,-41,1,-1,-38,Blocks.SMOOTH_STONE.defaultBlockState());
        l.setBlock(b.offset(-2,0,-2),Blocks.LECTERN.defaultBlockState(),f);if(l.getBlockEntity(b.offset(-2,0,-2)) instanceof LecternBlockEntity e)e.setBook(HotelTexts.blind());
        LabyrinthBuilder.entrance(l,b,Blocks.GRAY_CONCRETE.defaultBlockState(),Blocks.SMOOTH_STONE.defaultBlockState(),Blocks.GRAY_CONCRETE.defaultBlockState());LabyrinthBuilder.doors(l,b,LabyrinthPlace.BLIND_STRETCH);
    }
    public static void onArrive(ServerPlayer p,LabyrinthPlace place){if(place!=LabyrinthPlace.BLIND_STRETCH||p.gameMode.getGameModeForPlayer()==GameType.SPECTATOR)return;var d=LabyrinthData.get(p.server);var all=d.state(STATE);var own=d.stateEntry(STATE,p.getUUID().toString());own.putInt("Turn",0);all.put(p.getUUID().toString(),own);d.setState(STATE,all);}
    public static int advance(int stage,BlockPos b,net.minecraft.world.phys.Vec3 position){while(stage<TURNS.size()&&position.distanceToSqr(b.offset(TURNS.get(stage)).getCenter())<6.25)stage++;return stage;}
    public static boolean lies(int stage){return stage>=2;}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){if(e.getServer().getTickCount()%20!=0)return;var origin=HouseSavedData.get(e.getServer()).houseOrigin();if(origin==null)return;var b=LabyrinthPlaces.base(origin,LabyrinthPlace.BLIND_STRETCH);var data=LabyrinthData.get(e.getServer());
        for(var p:e.getServer().getPlayerList().getPlayers()){
            if(!p.isAlive()||p.gameMode.getGameModeForPlayer()==GameType.SPECTATOR||!p.level().dimension().equals(HouseDimensions.INTERIOR)||LabyrinthPlaces.placeAt(origin,p.blockPosition())!=LabyrinthPlace.BLIND_STRETCH||p.getZ()>=b.getZ()-4)continue;
            var own=data.stateEntry(STATE,p.getUUID().toString());int stage=advance(own.getInt("Turn"),b,p.position());own.putInt("Turn",stage);var all=data.state(STATE);all.put(p.getUUID().toString(),own);data.setState(STATE,all);
            var target=b.offset(stage<TURNS.size()?TURNS.get(stage):new BlockPos(0,0,-41));
            HousePackets.send(p,new HotelAtmospherePayload(1,target,lies(stage),0));
            p.connection.send(new ClientboundSoundPacket(HotelRegistry.BELL,SoundSource.BLOCKS,target.getX()+.5,target.getY()+1,target.getZ()+.5,.7F,.7F,p.getRandom().nextLong()));
        }
    }
}
