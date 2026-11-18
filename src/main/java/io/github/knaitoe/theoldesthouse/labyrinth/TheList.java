package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.699, The list, part 1a (docs/THE_LIST_0_4_699.md), after Stephen Graham Jones's <i>I Was a Teenage Slasher</i>, built to
 * docs/THE_LIST_PLAN.md. The place alone: the middle of Lamesa at night, through the back door of the reader's own house (TheListSite).
 * Nobody is out yet; the town's people come in part 1b. Every thing answers a hand; the neighbors' doors are shut; the town's night is
 * heard. The House does not deal it until night one (part 3).
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TheList {
    private TheList(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.THE_LIST;
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** Not dealt until night one exists. */
    public static boolean dealable(LabyrinthData d,UUID reader){return false;}

    /** A reader in through the back door: counted, and the night begins round them if it has not. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        own.putInt("Visits",own.getInt("Visits")+1);
        ACTIVE.computeIfAbsent(b.asLong(),k->new Site(b));
        p.serverLevel().playSound(null,b.offset(-10,1,2),TheListRegistry.INSECTS.get(),SoundSource.AMBIENT,.5F,1F);
    }

    /** A hand on something of the town's. True if it was the town's to answer. The reader's own house opens as a house does; every
     * other door in town is shut, and the empty house across the street is left to the House's own way on. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(st.getBlock() instanceof DoorBlock){
            if(!st.is(TheListRegistry.HOUSE_DOOR.get()))return false;
            var lower=st.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?rel.below():rel;
            if(TheListSite.inHouse(Vec3.atCenterOf(lower))||lower.equals(TheListSite.FRONT_DOOR))return false;
            cue(p,TheListTexts.SHUT_DOOR);
            return true;
        }
        if(!TheListBlock.is(st))return false;
        String line=TheListTexts.thing(st.getValue(TheListBlock.KIND),st.getValue(TheListBlock.STAGE));
        if(line==null)return false;
        cue(p,line);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the night
    static final Map<Long,Site> ACTIVE=new HashMap<>();
    static final class Site{final BlockPos base;int tick,empty;Site(BlockPos b){base=b;}}
    static final int EMPTY_FORGET=100;
    /** Everyone in the town, inside or out. */
    public static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players()){
            if(p.isSpectator()||!p.isAlive())continue;
            if(TheListSite.inScene(p.position().subtract(Vec3.atLowerCornerOf(b))))out.add(p);
        }
        return out;
    }
    public static boolean active(BlockPos b){return ACTIVE.containsKey(b.asLong());}
    public static int clock(BlockPos b){var s=ACTIVE.get(b.asLong());return s==null?-1:s.tick;}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){ACTIVE.clear();}
    public static void clearAll(){ACTIVE.clear();}
    /**
     * Every few seconds while someone is in town, its night, each heard where it is: crickets near a reader who is outdoors, a dog behind
     * the back fence nearest them, the drive-in's nearest speaker post to anyone on that stretch of Main Street, the loose tin on whichever
     * wall of the gin is nearest anyone close to it, the tower ticking over anyone under it, the wind going by, and now and then a train out
     * past the playa, which every reader in town hears from that way however far they are. A sound is played where the server will send it,
     * within its reach of whoever it is for.
     */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(ACTIVE.isEmpty())return;
        var l=e.getServer().getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var it=ACTIVE.values().iterator();it.hasNext();){
            var s=it.next();var here=readers(l,s.base);
            if(here.isEmpty()){if(++s.empty>=EMPTY_FORGET)it.remove();continue;}
            s.empty=0;s.tick++;
            var rnd=l.getRandom();var one=here.get(rnd.nextInt(here.size()));var r=one.position().subtract(Vec3.atLowerCornerOf(s.base));
            boolean out=!TheListSite.indoors(r);
            if(s.tick%140==20&&out)play(l,one.blockPosition().offset(rnd.nextInt(13)-6,0,rnd.nextInt(13)-6),TheListRegistry.INSECTS.get(),.5F);
            if(s.tick%230==90&&out)play(l,one.blockPosition().offset(rnd.nextInt(2)==0?-12:12,1,rnd.nextInt(17)-8),TheListRegistry.WIND.get(),.6F);
            if(s.tick%410==170&&r.x<TheListSite.AVE_W&&r.z>TheListSite.ALLEY_N)play(l,s.base.offset(dogNear(r)),TheListRegistry.DOG.get(),.8F);
            if(s.tick%120==60&&r.z<TheListSite.WALK_S_S+6&&r.z>TheListSite.MAIN_BACK_N-6&&r.x>TheListSite.AVE_E&&r.x<TheListSite.TRACK_W)
                play(l,s.base.offset(speakerNear(r)),TheListRegistry.SPEAKER.get(),.9F);
            if(s.tick%170==30){var tin=tinNear(r);if(tin.distToCenterSqr(r)<18*18)play(l,s.base.offset(tin),TheListRegistry.TIN.get(),.8F);}
            if(s.tick%260==200&&Math.abs(r.x-TheListSite.TOWER_X)<14&&Math.abs(r.z-TheListSite.TOWER_Z)<14)
                play(l,s.base.offset(TheListSite.TOWER_X,TheListSite.TANK_BOTTOM,TheListSite.TOWER_Z),TheListRegistry.TOWER.get(),2F);
            if(s.tick%1800==900)for(var p:here)train(p,s.base);
        }
    }
    /** Where a train out past the playa is heard from, for one reader: off toward the north-east, near enough for the sound to reach. */
    public static final Vec3 TRAIN_FROM=new Vec3(TheListSite.EAST+60,2,-150);
    static void train(ServerPlayer p,BlockPos b){
        var from=Vec3.atLowerCornerOf(b).add(TRAIN_FROM);var dir=from.subtract(p.position());dir=new Vec3(dir.x,0,dir.z).normalize();
        var at=p.position().add(dir.scale(14)).add(0,2,0);
        p.connection.send(new ClientboundSoundPacket(TheListRegistry.TRAIN,SoundSource.AMBIENT,at.x,at.y,at.z,1.6F,1F,p.getRandom().nextLong()));
    }
    /** A yard a dozen blocks along from the reader, at the back fence nearest them. */
    static BlockPos dogNear(Vec3 r){
        int fence=Math.abs(r.z-(-50))<Math.abs(r.z-(-91))?-50:-91;if(r.z>-25)fence=TheListSite.SOUTH-2;
        int z=(int)Math.floor(r.z+Math.max(-8,Math.min(8,fence-r.z)));
        int x=(int)Math.floor(r.x)+(r.x>-20?-12:12);
        return new BlockPos(x,0,z);
    }
    /** The drive-in's speaker post nearest a reader. */
    static BlockPos speakerNear(Vec3 r){int z=(int)Math.floor(Math.max(-142,Math.min(-131,r.z)));return new BlockPos(53,1,z-Math.floorMod(z-(-142),3)+1);}
    /** The point on the gin's walls nearest a reader, partway up. */
    static BlockPos tinNear(Vec3 r){
        int x=(int)Math.floor(Math.max(TheListSite.GIN_W,Math.min(TheListSite.GIN_E,r.x))),z=(int)Math.floor(Math.max(TheListSite.GIN_N,Math.min(TheListSite.GIN_S,r.z)));
        return new BlockPos(x,4,z);
    }
    static void play(ServerLevel l,BlockPos at,SoundEvent e,float volume){l.playSound(null,at,e,SoundSource.AMBIENT,volume,.95F+l.getRandom().nextFloat()*.1F);}
}
