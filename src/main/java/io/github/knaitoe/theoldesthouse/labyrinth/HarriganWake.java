package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.*;

/**
 * Quiet, persistent funeral guests on the third visit (0.4.105: scene actors, not armour stands); talking never confers
 * phone custody or Witness progress.
 */
public final class HarriganWake {
    private static final BlockPos[] SPOTS={new BlockPos(-4,0,-17),new BlockPos(4,0,-17),new BlockPos(4,0,-23)};
    private static final float[] FACING={-35,35,180};
    private static final String[] WORDS={"\"You read to him, didn't you?\"","\"I keep thinking he'll ask us to be quiet.\"","\"I don't know what to do with my hands.\""};
    private HarriganWake(){}
    /** On arrival nobody else is in the room yet, so the guests are set out before anyone can see them appear. */
    public static void ensureCast(ServerLevel level,BlockPos base,boolean arrival){
        var data=LabyrinthData.get(level.getServer());var state=data.state(HarriganVignette.ID);
        if(state.getInt("Visit")<HarriganVignette.FINAL_VISIT&&!data.isCompleted(HarriganVignette.ID))return;
        for(int role=0;role<SPOTS.length;role++){
            String key="Mourner0505_"+role;if(state.hasUUID(key))continue;
            var pos=base.offset(SPOTS[role]);
            if(!level.hasChunkAt(pos)||!level.areEntitiesLoaded(new ChunkPos(pos).toLong())||!arrival&&HouseWatchers.isWatched(level,Vec3.atCenterOf(pos).add(0,1,0)))continue;
            var guest=LiteraryRegistry.ACTOR.get().create(level);if(guest==null)continue;
            guest.bind(LabyrinthPlace.HARRIGAN,null);guest.appearance(LiteraryActor.HARRIGAN_MOURNER,role);guest.setNoGravity(true);guest.setInvulnerable(true);guest.setSilent(true);
            guest.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,FACING[role],10);guest.setYHeadRot(FACING[role]);guest.yBodyRot=FACING[role];
            if(level.addFreshEntity(guest))state.putUUID(key,guest.getUUID());
        }
        data.setState(HarriganVignette.ID,state);
    }
    public static AABB bounds(BlockPos base){return new AABB(base.getX()-8,base.getY()-2,base.getZ()-25,base.getX()+9,base.getY()+7,base.getZ()+3);}
    static void talk(ServerPlayer player,LiteraryActor guest){
        int role=guest.phase();if(role<0||role>=WORDS.length)return;
        long now=player.level().getGameTime();String key="WakeReply0488";
        if(!player.getPersistentData().contains(key)||now-player.getPersistentData().getLong(key)>=60){player.getPersistentData().putLong(key,now);player.displayClientMessage(Component.literal(WORDS[role]),false);}
    }
}
