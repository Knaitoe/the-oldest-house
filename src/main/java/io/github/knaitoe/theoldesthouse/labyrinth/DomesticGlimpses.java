package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 0.4.199: now and then, a household fragment in an ordinary hall that does not yet lead anywhere has someone in it.
 * The owner: "When the domestic house slices are still inaccessible in hallways, there should be a small chance of
 * players seeing occupants living inside."
 *
 * Once in a while a reader's visit to a hall is chosen. Someone is put, unseen, into one of its fragments whose home
 * has not bled in: at the counter, the washtub, laying the table, or standing by the bed, back to the hall. Only that
 * reader can see them. They never turn round. Once looked at and then looked away from, they are gone; a reader who
 * walks up to them finds the light blinking and the fragment empty. Nothing is said, given or counted.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class DomesticGlimpses {
    private static final String STATE="DomesticGlimpse199",TAG="DomesticGlimpse199";
    /** About one visit to a hall with a free fragment in eight. */
    static final int CHANCE=8;
    private static final double NEAR=4.5,SPAWN_FAR=7;
    private DomesticGlimpses(){}
    /** A visit chosen by a test; the next visit to a hall rolls as usual. */
    public static boolean force;
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.tickCount%5!=0)return;
        var tag=p.getPersistentData();var own=tag.getCompound(STATE);
        var level=p.serverLevel();var origin=HouseSavedData.get(p.server).houseOrigin();
        LabyrinthPlace place=origin==null||!level.dimension().equals(HouseDimensions.INTERIOR)?null:LabyrinthPlaces.placeAt(origin,p.blockPosition());
        String id=place==null?"":place.id();
        if(!id.equals(own.getString("Place"))){
            // A new visit (or none): whoever was here goes, and this visit rolls once.
            leave(level,own);own=new CompoundTag();own.putString("Place",id);
            if(place!=null&&p.isAlive()&&!p.isSpectator()){
                var free=free(level,origin,place);
                if(!free.isEmpty()&&(force||p.getRandom().nextInt(CHANCE)==0)){force=false;own.putBoolean("Due",true);own.putInt("Fragment",free.get(p.getRandom().nextInt(free.size())));}
            }
            tag.put(STATE,own);
        }
        if(place==null||!own.getBoolean("Due")||own.getBoolean("Done"))return;
        var fragments=LabyrinthDomestic.fragments(place);int index=own.getInt("Fragment");if(index<0||index>=fragments.size())return;
        var f=fragments.get(index);var base=LabyrinthPlaces.base(origin,place);var stand=stand(base,f);
        var actor=own.hasUUID("Actor")&&level.getEntity(own.getUUID("Actor")) instanceof LiteraryActor a&&a.isAlive()?a:null;
        Vec3 head=Vec3.atBottomCenterOf(stand).add(0,1.5,0);
        if(actor==null){
            if(own.hasUUID("Actor")){own.putBoolean("Done",true);return;}
            // Put in only where the reader is not looking, from across the hall, and only into an empty fragment.
            if(!p.isAlive()||p.isSpectator()||p.getEyePosition().distanceTo(head)<SPAWN_FAR||HouseWatchers.sees(p,head)
                    ||!level.getEntitiesOfClass(LivingEntity.class,box(base,f),LivingEntity::isAlive).isEmpty())return;
            actor=spawn(level,place,p,f,stand,own.getInt("Fragment"),origin);if(actor==null)return;
            own.putUUID("Actor",actor.getUUID());
            p.connection.send(new ClientboundSoundPacket(net.minecraft.core.Holder.direct(sound(f.room())),SoundSource.AMBIENT,head.x,head.y,head.z,.35F,.9F+p.getRandom().nextFloat()*.2F,p.getRandom().nextLong()));
            return;
        }
        boolean seen=HouseWatchers.sees(p,head)||HouseWatchers.sees(p,head.add(0,-.8,0));
        if(seen)own.putInt("Watched",own.getInt("Watched")+5);
        if(p.getEyePosition().distanceTo(head)<NEAR){
            // Walked right up to: the lamp blinks for this reader, and when it comes back nobody is there.
            var lamp=lamp(base,f);p.connection.send(new ClientboundBlockUpdatePacket(lamp,Blocks.AIR.defaultBlockState()));
            own.putLong("Blink",level.getGameTime()+6);own.putLong("Lamp",lamp.asLong());actor.discard();own.putBoolean("Done",true);
        }else if(!seen&&own.getInt("Watched")>=20||!seen&&own.getInt("Alive")>=1200){actor.discard();own.putBoolean("Done",true);}
        own.putInt("Alive",own.getInt("Alive")+5);
        tag.put(STATE,own);
    }
    /** The blinked lamp comes back for its reader. */
    @SubscribeEvent public static void after(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;var own=p.getPersistentData().getCompound(STATE);
        if(!own.contains("Blink")||p.serverLevel().getGameTime()<own.getLong("Blink"))return;
        var lamp=BlockPos.of(own.getLong("Lamp"));p.connection.send(new ClientboundBlockUpdatePacket(lamp,p.serverLevel().getBlockState(lamp)));
        own.remove("Blink");own.remove("Lamp");p.getPersistentData().put(STATE,own);
    }
    /** An occupant left behind (a restart, a reader gone) leaves too. */
    public static void actorTick(LiteraryActor a){
        if(!a.getPersistentData().getBoolean(TAG)||a.tickCount%20!=0)return;
        var reader=a.owner().map(id->a.getServer().getPlayerList().getPlayer(id)).orElse(null);
        var own=reader==null?null:reader.getPersistentData().getCompound(STATE);
        if(reader==null||reader.level()!=a.level()||reader.distanceToSqr(a)>48*48||!own.hasUUID("Actor")||!own.getUUID("Actor").equals(a.getUUID()))a.discard();
    }
    private static void leave(ServerLevel level,CompoundTag own){
        if(own.hasUUID("Actor")&&level.getEntity(own.getUUID("Actor")) instanceof LiteraryActor a)a.discard();
    }
    /** The fragments of this hall that do not yet lead into a home. */
    public static List<Integer> free(ServerLevel level,BlockPos origin,LabyrinthPlace place){
        var out=new ArrayList<Integer>();var fragments=LabyrinthDomestic.fragments(place);var base=LabyrinthPlaces.base(origin,place);
        var data=LabyrinthData.get(level.getServer()).state(DomesticLives.STATE);
        for(int i=0;i<fragments.size();i++){var home=home(place,fragments.get(i));
            boolean open=home!=null&&DomesticLives.selected(level,base,home)&&data.getBoolean(DomesticLives.key(base,home))&&!DomesticLives.sealed(LabyrinthData.get(level.getServer()),base,home);
            if(!open)out.add(i);}
        return out;
    }
    /** The home behind a fragment, if there is one: its opening is cut through the fragment's own wall. */
    static @Nullable DomesticLives.Home home(LabyrinthPlace place,LabyrinthDomestic.Fragment f){
        for(var h:DomesticLives.HOMES)if(h.hall()==place&&h.opening().getX()>=f.x0()-1&&h.opening().getX()<=f.x1()+1&&h.opening().getZ()>=f.z0()-1&&h.opening().getZ()<=f.z1()+1)return h;
        return null;
    }
    /** One step in front of the fragment's back wall, facing it. */
    public static BlockPos stand(BlockPos base,LabyrinthDomestic.Fragment f){
        int cx=(f.x0()+f.x1())/2,cz=(f.z0()+f.z1())/2;
        var back=switch(f.entrance()){case EAST->new BlockPos(f.x0(),0,cz);case WEST->new BlockPos(f.x1(),0,cz);case NORTH->new BlockPos(cx,0,f.z1());default->new BlockPos(cx,0,f.z0());};
        // The bedroom's bed takes the back's other side; whoever is in there stands beside it.
        return base.offset(back.relative(f.entrance()));
    }
    private static BlockPos lamp(BlockPos base,LabyrinthDomestic.Fragment f){return base.offset((f.x0()+f.x1())/2,3,(f.z0()+f.z1())/2);}
    private static AABB box(BlockPos base,LabyrinthDomestic.Fragment f){return new AABB(base.getX()+f.x0(),base.getY(),base.getZ()+f.z0(),base.getX()+f.x1()+1,base.getY()+3,base.getZ()+f.z1()+1);}
    private static @Nullable LiteraryActor spawn(ServerLevel level,LabyrinthPlace place,ServerPlayer reader,LabyrinthDomestic.Fragment f,BlockPos stand,int index,BlockPos origin){
        if(!level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(stand.getX()>>4,stand.getZ()>>4)))return null;
        var a=LiteraryRegistry.ACTOR.get().create(level);if(a==null)return null;
        var home=home(place,f);
        // The household it would be, where there is one: the family, the students, the father on his own, the grandmother.
        int skin=home==null?Math.floorMod(place.ordinal()*3+index,4):switch(home.id()){case "family"->2;case "students"->1;case "drinker"->0;default->3;};
        a.bind(place,reader.getUUID());a.appearance(LiteraryActor.STRANGER,skin);
        a.gesture(f.room()==LabyrinthDomestic.Room.BEDROOM?0:4);
        float yaw=f.entrance().getOpposite().toYRot();
        a.moveTo(Vec3.atBottomCenterOf(stand),yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setSilent(true);a.setInvulnerable(true);
        a.getPersistentData().putBoolean(TAG,true);
        return level.addFreshEntity(a)?a:null;
    }
    private static SoundEvent sound(LabyrinthDomestic.Room room){
        return switch(room){case KITCHEN->SoundEvents.FURNACE_FIRE_CRACKLE;case LAUNDRY->SoundEvents.BUCKET_EMPTY;case DINING->SoundEvents.WOOD_PLACE;case BEDROOM->SoundEvents.WOOL_STEP;};
    }
    /** For tests: whether this reader has an occupant in a fragment right now. */
    public static @Nullable LiteraryActor occupant(ServerPlayer p){
        var own=p.getPersistentData().getCompound(STATE);
        return own.hasUUID("Actor")&&p.serverLevel().getEntity(own.getUUID("Actor")) instanceof LiteraryActor a&&a.isAlive()?a:null;
    }
}
