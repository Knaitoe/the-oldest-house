package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.464, Bike night (docs/BIKE_NIGHT_PROPOSAL.md): the last night of October in Calder Falls. The owner: "The vignette are dorks at
 * night. Kids on a playground maybe?" The readers are the kids who did not go trick-or-treating, out on bikes in the park after dark.
 *
 * <p>This slice is the park and the bikes in it. One night for everyone in the park, on one clock that runs while anyone is there.
 * Six bikes stand in the rack by the gate, and whichever a reader gets on is theirs for the night: nobody else can take it, and
 * getting on another that nobody has gives the first one back. One of the two lamps over the playground is failing. When the park has
 * stood empty for a while the night is over: the bikes are back in the rack and nobody has one.
 *
 * <p>It cannot be finished yet (the playground's equipment, the bullies, the town, the graveyard and what comes after it are later
 * slices), so the House does not deal it: {@link #dealable} says no. It is reached by an operator's test door.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BikeNight {
    private BikeNight(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.BIKE_NIGHT;
    /** Ticks: how long an empty park waits before its night is over; how long after getting on a bike its rider is told it is theirs. */
    public static final int EMPTY_RESET=200,TELL_AFTER=70;

    static final class Night {
        final BlockPos base;long tick;int empty;boolean fresh=true;
        /** Whose each bike is tonight, by make. */
        final UUID[] owner=new UUID[6];
        /** Where each bike stands or lies while nobody is on it; null while it is ridden. */
        final BlockPos[] at=new BlockPos[6];
        /** The failing lamp: whether it is lit, when it next changes, and how many times it has gone out. */
        boolean lampLit=true;long lampNext=60,lampOut;int outages;
        /** What a reader is to be told, and when. */
        final Map<UUID,Long> tellAt=new HashMap<>();final Map<UUID,String> tell=new HashMap<>();
        Night(BlockPos b){base=b;for(var make:NightBike.Make.values())at[make.ordinal()]=b.offset(BikeNightSite.bikeAt(make));}
    }
    private static final Map<Long,Night> NIGHTS=new HashMap<>();
    public static void clearAll(){NIGHTS.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}

    /** The House does not deal the night until it can be finished. */
    public static boolean dealable(LabyrinthData d,UUID p){return false;}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Night night(BlockPos b){return NIGHTS.get(b.asLong());}
    @Nullable public static UUID owner(BlockPos b,NightBike.Make make){var n=night(b);return n==null?null:n.owner[make.ordinal()];}
    /** Where a bike is while nobody rides it, or null. */
    @Nullable public static BlockPos bikeAt(BlockPos b,NightBike.Make make){var n=night(b);return n==null?null:n.at[make.ordinal()];}
    public static boolean failingLampLit(BlockPos b){var n=night(b);return n==null||n.lampLit;}
    public static int outages(BlockPos b){var n=night(b);return n==null?0:n.outages;}
    /** For tests: the failing lamp's next change brought forward to now. */
    public static void lampNow(BlockPos b){var n=night(b);if(n!=null)n.lampNext=n.tick;}
    /** For tests: what a reader is waiting to be told, told now. */
    public static void tellNow(BlockPos b){var n=night(b);if(n!=null)n.tellAt.replaceAll((k,v)->n.tick);}
    public static void resetNow(ServerLevel l,BlockPos b){var n=night(b);if(n!=null){reset(l,n);NIGHTS.remove(b.asLong());}}

    // ------------------------------------------------------------------------------------------------ records and speech
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    /** The night a reader is in, if they are in one. */
    @Nullable static Night night(ServerPlayer p){
        if(LiteraryVignettes.current(p)!=PLACE)return null;var b=LiteraryVignettes.base(p,PLACE);return b==null?null:NIGHTS.get(b.asLong());
    }
    /** The night a place in the world belongs to: the one whose ground it is on. */
    @Nullable static Night nightAt(ServerLevel l,BlockPos at){
        if(!l.dimension().equals(NovelRooms.dimension(PLACE)))return null;var room=PLACE.room();
        for(var n:NIGHTS.values()){var r=at.subtract(n.base);if(room.isInside(r))return n;}
        return null;
    }

    // ------------------------------------------------------------------------------------------------ arriving
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;NIGHTS.computeIfAbsent(b.asLong(),k->new Night(b));
        own.putInt("Nights",own.getInt("Nights")+1);
        cue(p,BikeNightTexts.ARRIVE);
    }

    // ------------------------------------------------------------------------------------------------ whose bike is whose
    /** What the bikes tell the night, and what it refuses. Outside a park it answers nothing. */
    public static final NightBikes.Night HOOK=new NightBikes.Night(){
        @Override public @Nullable String refuse(ServerPlayer p,NightBike.Make make,BlockPos at){
            var n=night(p);if(n==null)return null;var who=n.owner[make.ordinal()];
            if(who==null||who.equals(p.getUUID()))return null;
            var other=p.server.getPlayerList().getPlayer(who);
            return other==null?BikeNightTexts.SOMEONES:String.format(BikeNightTexts.THEIRS,other.getGameProfile().getName());
        }
        @Override public void rode(ServerPlayer p,NightBike bike){
            var n=night(p);if(n==null)return;int k=bike.make().ordinal();
            if(n.at[k]!=null){n.at[k]=null;remember(p.serverLevel(),n);}
            if(n.owner[k]!=null)return;
            boolean had=false;for(int i=0;i<n.owner.length;i++)if(p.getUUID().equals(n.owner[i])){n.owner[i]=null;had=true;}
            n.owner[k]=p.getUUID();
            var own=own(p);own.putString("Bike",bike.make().getSerializedName());if(had)own.putInt("Swaps",own.getInt("Swaps")+1);save(p,own);
            n.tell.put(p.getUUID(),had?BikeNightTexts.SWAPPED:BikeNightTexts.YOURS);n.tellAt.put(p.getUUID(),n.tick+TELL_AFTER);
        }
        @Override public void parked(ServerLevel l,@Nullable UUID reader,NightBike.Make make,@Nullable BlockPos at,boolean lying){
            Night n=at!=null?nightAt(l,at):null;
            if(n==null&&reader!=null){var p=l.getServer().getPlayerList().getPlayer(reader);if(p!=null)n=night(p);}
            if(n==null)return;
            if(at==null){at=n.base.offset(BikeNightSite.bikeAt(make));stand(l,at,make);}   // nowhere to set it down: it is back in the rack
            n.at[make.ordinal()]=at;remember(l,n);
        }
        @Override public void carried(ServerPlayer onPegs,ServerPlayer pedalling){
            if(night(onPegs)==null)return;
            var own=own(onPegs);own.putBoolean("Carried",true);save(onPegs,own);
            var theirs=own(pedalling);theirs.putBoolean("CarriedSomeone",true);save(pedalling,theirs);
        }
    };
    // Where the bikes are is kept with the world, so that a night cut short by the server stopping leaves none of them behind:
    // the next night finds each where it was left and takes it back to the rack.
    static String key(Night n){return "Bikes_"+n.base.asLong();}
    static void remember(ServerLevel l,Night n){
        var d=LabyrinthData.get(l.getServer());var world=LiteraryVignettes.shared(d,PLACE);var t=new CompoundTag();
        for(var make:NightBike.Make.values()){var at=n.at[make.ordinal()];if(at!=null)t.putLong(make.id(),at.asLong());}
        world.put(key(n),t);LiteraryVignettes.shared(d,PLACE,world);
    }
    static void recall(ServerLevel l,Night n){
        var world=LiteraryVignettes.shared(LabyrinthData.get(l.getServer()),PLACE);if(!world.contains(key(n)))return;
        var t=world.getCompound(key(n));
        for(var make:NightBike.Make.values())n.at[make.ordinal()]=t.contains(make.id())?BlockPos.of(t.getLong(make.id())):null;
    }
    static void stand(ServerLevel l,BlockPos at,NightBike.Make make){
        var here=l.getBlockState(at);
        if(here.isAir()||here.canBeReplaced())l.setBlock(at,NightBikeBlock.of(make,Direction.NORTH,false),3);
    }

    // ------------------------------------------------------------------------------------------------ the park's clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(NIGHTS.isEmpty())return;var l=e.getServer().getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var n:List.copyOf(NIGHTS.values())){
            var here=readers(l,n.base);
            if(here.isEmpty()){if(++n.empty>=EMPTY_RESET&&loaded(l,n)){reset(l,n);NIGHTS.remove(n.base.asLong());}continue;}
            n.empty=0;n.tick++;
            if(n.fresh){if(!loaded(l,n))continue;n.fresh=false;recall(l,n);reset(l,n);}
            lamp(l,n);
            for(var it=n.tellAt.entrySet().iterator();it.hasNext();){var t=it.next();if(n.tick<t.getValue())continue;
                var p=l.getServer().getPlayerList().getPlayer(t.getKey());if(p!=null&&night(p)==n)cue(p,n.tell.get(t.getKey()));
                n.tell.remove(t.getKey());it.remove();}
        }
    }
    static boolean loaded(ServerLevel l,Night n){return l.isLoaded(n.base.offset(BikeNightSite.FLICKER))&&l.isLoaded(n.base.offset(BikeNightSite.bikeAt(NightBike.Make.RATTLER)));}
    /** The failing lamp: lit for a few seconds, out for a moment; now and then out for longer, and each time a little longer. */
    static void lamp(ServerLevel l,Night n){
        if(n.tick<n.lampNext)return;var rnd=l.getRandom();
        n.lampLit=!n.lampLit;
        if(!n.lampLit){n.outages++;n.lampNext=n.tick+(rnd.nextInt(5)==0?30+Math.min(60,n.outages*2)+rnd.nextInt(20):2+rnd.nextInt(5));}
        else n.lampNext=n.tick+(rnd.nextInt(3)==0?3+rnd.nextInt(6):50+rnd.nextInt(140));
        BikeNightSite.lantern(l,n.base,BikeNightSite.FLICKER,n.lampLit);
        // Coming back on after it has been out a while, it is heard: the starter, the tube catching, the hum.
        if(n.lampLit&&n.tick-n.lampOut>=20)l.playSound(null,n.base.offset(BikeNightSite.FLICKER).above(BikeNightSite.LAMP_TOP),BikeNightRegistry.LAMP_BUZZ.get(),net.minecraft.sounds.SoundSource.BLOCKS,.7F,.95F+rnd.nextFloat()*.1F);
        if(!n.lampLit)n.lampOut=n.tick;
    }
    /** The night over: every bike back in the rack on its stand, nobody's; the failing lamp lit. */
    static void reset(ServerLevel l,Night n){
        for(var bike:l.getEntitiesOfClass(NightBike.class,new net.minecraft.world.phys.AABB(n.base).inflate(110,70,310)))if(nightAt(l,bike.blockPosition())==n&&bike.getPassengers().isEmpty())bike.discard();
        for(var make:NightBike.Make.values()){
            int k=make.ordinal();var home=n.base.offset(BikeNightSite.bikeAt(make));
            if(n.at[k]!=null&&!n.at[k].equals(home)&&l.isLoaded(n.at[k])){var s=l.getBlockState(n.at[k]);if(s.is(BikeNightRegistry.BIKE_BLOCK.get())&&s.getValue(NightBikeBlock.MAKE)==make)l.setBlock(n.at[k],Blocks.AIR.defaultBlockState(),3);}
            l.setBlock(home,NightBikeBlock.of(make,Direction.NORTH,false),3);
            n.at[k]=home;n.owner[k]=null;
        }
        n.tell.clear();n.tellAt.clear();n.lampLit=true;n.lampNext=n.tick+60;n.outages=0;
        BikeNightSite.lantern(l,n.base,BikeNightSite.FLICKER,true);
        remember(l,n);
    }

    // ------------------------------------------------------------------------------------------------ each reader
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;NIGHTS.computeIfAbsent(b.asLong(),k->new Night(b));
    }

    // ------------------------------------------------------------------------------------------------ a hand on the park's things
    /** A hand on something in the park that is only there to be looked at. True if it was one of them. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();BlockState s=l.getBlockState(b.offset(rel));
        if(CalderSignBlock.is(s)){cue(p,CalderSignBlock.sign(s).look());return true;}
        if(s.is(BikeNightRegistry.RACK.get())){cue(p,BikeNightTexts.RACK);return true;}
        if(!s.is(BikeNightRegistry.FITTING.get()))return false;
        switch(s.getValue(CalderBlock.KIND)){
            case GATE->{cue(p,BikeNightTexts.GATE);l.playSound(null,b.offset(rel),BikeNightRegistry.CHAIN.get(),net.minecraft.sounds.SoundSource.BLOCKS,.8F,.95F+l.getRandom().nextFloat()*.1F);}
            case FENCE->cue(p,BikeNightTexts.RAILINGS);
            case LAMP->{boolean failing=rel.getX()==BikeNightSite.FLICKER.getX()&&rel.getZ()==BikeNightSite.FLICKER.getZ();cue(p,failing?BikeNightTexts.LAMP_FAILING:BikeNightTexts.LAMP);}
            case BENCH->cue(p,BikeNightTexts.BENCH);
            case BIN->cue(p,BikeNightTexts.BIN);
            case BORDER->cue(p,BikeNightTexts.BORDER);
            default->{return false;}
        }
        return true;
    }
}
