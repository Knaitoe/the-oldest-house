package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.463: what a hand on one of Bike night's bikes does, and what the reader is told. The first hand on each bike is a look at it:
 * its name and the one thing it does. Lying on its side, a hand stands it up. Standing, a hand gets on, and the keys are shown once in
 * grey (and the Rattler's own key the first time it is the Rattler).
 *
 * <p>0.4.464: whose bike is whose for the night is the park's rule ({@link BikeNight#HOOK}), which {@link #night} is unless a test
 * puts something else there. Outside a park it answers nothing and refuses nobody.
 */
public final class NightBikes {
    private NightBikes(){}
    /** What the night the bikes are part of hears of them, and may refuse. */
    public interface Night {
        /** Why this reader may not take this bike, or null if they may. */
        default @Nullable String refuse(ServerPlayer p,NightBike.Make make,BlockPos at){return null;}
        default void rode(ServerPlayer p,NightBike bike){}
        default void cameOff(ServerPlayer p,NightBike bike,RiddenBike.Off why){}
        default void parked(ServerLevel l,@Nullable UUID reader,NightBike.Make make,@Nullable BlockPos at,boolean lying){}
        default void carried(ServerPlayer onPegs,ServerPlayer pedalling){}
    }
    public static Night night=BikeNight.HOOK;
    public static void forget(){night=BikeNight.HOOK;}

    public static final String[] LOOK={
        "The Wasp. A low BMX on small wheels with fat tyres, a race plate on its bars. It turns inside anything, and a wall hardly slows it.",
        "The Hare. A light racer with a thumb-shifter. Every push on a pedal counts for two, and a hill is nothing to it.",
        "The Comet. A ten-speed with drop bars and tyres no wider than a thumb. Nothing here is faster flat out, or slower to turn and stop.",
        "The Hopper. A BMX with stunt pegs on both axles. It jumps higher than the rest and comes down easy, and the pegs will carry a friend.",
        "The Lamp. A paper-route bike with a basket and a dynamo lamp. The faster it goes, the brighter it burns.",
        "The Rattler. A long-seated bike with a playing card pegged into its spokes. Let the card take it, and for a moment nothing keeps up."};
    public static String look(NightBike.Make make){return LOOK[make.ordinal()];}
    public static final String STOOD="You pick it up and kick the stand down.";
    public static final String OVER="It goes over, and you go with it.",HEAD_ON="Straight into it. You go over the bars.",LANDING="It comes down too hard. You go over the bars.";
    public static final String PEGS="You step up onto the pegs and hold on.";

    // What a reader has already been shown, kept with the reader: a bit for each bike looked at, one for the keys and one for the Rattler's.
    private static final String SEEN="BikeNightSeen";private static final int KEYS=1<<6,CARD_KEY=1<<7;
    private static CompoundTag kept(ServerPlayer p){
        var all=p.getPersistentData();if(!all.contains(Player.PERSISTED_NBT_TAG))all.put(Player.PERSISTED_NBT_TAG,new CompoundTag());
        return all.getCompound(Player.PERSISTED_NBT_TAG);
    }
    static boolean seen(ServerPlayer p,int bit){return (kept(p).getInt(SEEN)&bit)!=0;}
    static void see(ServerPlayer p,int bit){var k=kept(p);k.putInt(SEEN,k.getInt(SEEN)|bit);}
    /** Forgets what a reader has been shown. For tests. */
    public static void forget(ServerPlayer p){kept(p).remove(SEEN);}
    static void cue(ServerPlayer p,String text){p.displayClientMessage(Component.literal(text),true);}

    /** A hand on a bike nobody is riding. */
    public static void use(ServerPlayer p,ServerLevel l,BlockPos at,BlockState s){
        NightBike.Make make=s.getValue(NightBikeBlock.MAKE);boolean lying=s.getValue(NightBikeBlock.LYING);int bit=1<<make.ordinal();
        String look=look(make);
        if(p.isPassenger()||p.isSpectator()){cue(p,look);return;}
        String no=night.refuse(p,make,at);
        if(no!=null){cue(p,seen(p,bit)?no:look+" "+no);see(p,bit);return;}
        if(lying){l.setBlock(at,s.setValue(NightBikeBlock.LYING,false),3);cue(p,(seen(p,bit)?"":look+" ")+STOOD);see(p,bit);return;}
        if(!seen(p,bit)){see(p,bit);cue(p,look);return;}
        mount(p,l,at,s);
    }

    /** The block becomes the bike, with this reader on it. Null if they could not get on, when the block is back as it was. */
    public static @Nullable NightBike mount(ServerPlayer p,ServerLevel l,BlockPos at,BlockState s){
        var bike=BikeNightRegistry.BIKE.get().create(l);if(bike==null)return null;
        NightBike.Make make=s.getValue(NightBikeBlock.MAKE);float yaw=s.getValue(NightBikeBlock.FACING).toYRot();
        l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
        bike.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);bike.setUp(p,make,yaw);l.addFreshEntity(bike);
        if(!p.startRiding(bike,true)){bike.discard();l.setBlock(at,s,3);return null;}
        keys(p,make);
        return bike;
    }
    /** The keys, shown once in grey to somebody getting on (and the Rattler's own the first time it is the Rattler). */
    static void keys(ServerPlayer p,NightBike.Make make){
        boolean keys=!seen(p,KEYS),card=make==NightBike.Make.RATTLER&&!seen(p,CARD_KEY);
        if(keys||card){
            var line=Component.empty();
            if(keys)line.append(Component.keybind("key.left")).append(" and ").append(Component.keybind("key.right")).append(" push the pedals in turn  ·  ")
                .append(Component.keybind("key.back")).append(" brake  ·  ").append(Component.keybind("key.jump")).append(" hold and let go to hop  ·  ")
                .append(Component.keybind("key.sneak")).append(" get off");
            if(card)line.append(keys?"  ·  ":"").append(Component.keybind("key.sprint")).append(" hold to let the card take it");
            p.displayClientMessage(line.withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
            see(p,KEYS|(card?CARD_KEY:0));
        }
    }

    static void cameOff(ServerPlayer p,NightBike bike,RiddenBike.Off why){
        cue(p,switch(why){case HEAD_ON->HEAD_ON;case LANDING->LANDING;default->OVER;});
        night.cameOff(p,bike,why);
    }
    static void onPegs(ServerPlayer p,NightBike bike){
        cue(p,PEGS);
        if(bike.getFirstPassenger() instanceof ServerPlayer pedalling)night.carried(p,pedalling);
    }
}
