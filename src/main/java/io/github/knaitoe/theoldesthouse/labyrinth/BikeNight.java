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
 * <p>0.4.465, the swings and the sand. The owner: "Lots of fun potential for unique interaction here - a seesaw, slides, swings,
 * monkey bars, etc", and of the bullies, "players throw balls of sand". The park's four swings are pumped (HouseReadSwing): a reader
 * who lets go of one is in the air, and where they come down on the sand is paced out from under the bar. The furthest anyone has
 * landed tonight is a heel mark in the sand, there for the next reader to beat. The playground's sand is soft: nobody is hurt
 * coming down on it. A hand in it packs a ball of sand, eight at most; a ball thrown bursts on what it meets and hurts nobody, and
 * the failing lamp, hit while it is out, comes back on.
 *
 * <p>0.4.466, the slide: a reader who comes into the top of its chute is taken down it and let go off the end (SlideRide), and
 * their record counts the times.
 *
 * <p>0.4.467, the monkey bars: a hand on their rungs from near enough under them takes hold (BarsHang); a reader's record counts
 * their holds, and the times they went from one end to the other on one.
 *
 * <p>0.4.468, the seesaw: a hand on an end of its board sits on it (SeesawSeat); a reader's record has that they sat, how many
 * times they pushed off the sand, and who was on the other end when they did.
 *
 * <p>0.4.470, the monkey bars and the seesaw as the proposal has them: a jump at the bars catches one (barsJump), a hand let go
 * too early misses and drops its reader (missed), and a hard bump on the seesaw throws whoever is on top (thrownOff).
 *
 * <p>0.4.469, the merry-go-round: a hand on it from the sand beside it shoves it round (RoundaboutBlock.Wheel); a reader's
 * record has how often they shoved it, how far round they went standing on it, and whether they came off it while it was going
 * fast.
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
    /** A child's paces to a block; the fewest a landing is told in; ticks a jump is given to come down. */
    public static final int PACES=2,LEAST=2,FLIGHT=200;
    /** A reader in the air off a swing: where the swing hangs from, the way it faces, and when they let go. */
    record Flight(Vec3 pivot,Direction forward,long tick){}

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
        /** Readers in the air off a swing. */
        final Map<UUID,Flight> flights=new HashMap<>();
        /** For each reader's own record, taken up on their next tick: their furthest landing, balls of sand thrown, and how many met somebody. */
        final Map<UUID,Integer> landed=new HashMap<>(),thrown=new HashMap<>(),hits=new HashMap<>(),slid=new HashMap<>(),hung=new HashMap<>(),crossed=new HashMap<>(),pushed=new HashMap<>(),shoved=new HashMap<>(),missed=new HashMap<>(),bounced=new HashMap<>(),sprung=new HashMap<>();
        /** Readers who have let go of the monkey bars and not touched the ground since: a jump at the bars does not catch them. */
        final Set<UUID> letGo=new HashSet<>();
        /** Readers thrown off the top of the seesaw; and whom a reader threw off it, for their records. */
        final Set<UUID> bumpedOff=new HashSet<>();final Map<UUID,String> bumpedWhom=new HashMap<>();
        /** Whether each reader was standing on the merry-go-round when they were last looked at. */
        final Map<UUID,Boolean> onWheel=new HashMap<>();
        /** 0.4.474: pushes a reader has given somebody on the swings; whom they last pushed; and who last pushed them. */
        final Map<UUID,Integer> swingPushes=new HashMap<>();final Map<UUID,String> pushedWhom=new HashMap<>(),pushedBy=new HashMap<>();
        /** Who was on the other end of the seesaw when a reader last pushed off, for their record. */
        final Map<UUID,String> rodeWith=new HashMap<>();
        /** The furthest anyone has landed off the swings tonight, in paces; whose it is; and where its mark is in the sand. */
        int far;@Nullable UUID farWho;@Nullable BlockPos mark;
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
    /** For tests: the failing lamp's next change put off this long. */
    public static void lampHold(BlockPos b,int ticks){var n=night(b);if(n!=null)n.lampNext=n.tick+ticks;}
    /** For tests: what a reader is waiting to be told, told now. */
    public static void tellNow(BlockPos b){var n=night(b);if(n!=null)n.tellAt.replaceAll((k,v)->n.tick);}
    public static void resetNow(ServerLevel l,BlockPos b){var n=night(b);if(n!=null){reset(l,n);NIGHTS.remove(b.asLong());}}
    /** The furthest landing off the swings tonight, in paces; whose; and where its mark is. */
    public static int far(BlockPos b){var n=night(b);return n==null?0:n.far;}
    @Nullable public static UUID farWho(BlockPos b){var n=night(b);return n==null?null:n.farWho;}
    @Nullable public static BlockPos mark(BlockPos b){var n=night(b);return n==null?null:n.mark;}
    public static boolean inFlight(ServerPlayer p){var n=night(p);return n!=null&&n.flights.containsKey(p.getUUID());}

    // ------------------------------------------------------------------------------------------------ records and speech
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    /** In the night, wherever in it. */
    public static boolean here(ServerPlayer p){return LiteraryVignettes.current(p)==PLACE;}
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
            flights(l,n);
            barsJump(n,here);
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
        // What the night has noted for its readers' records is not the park's to tidy: a night's first reset comes a few ticks
        // after its first reader, and they may have done something by then.
        unmark(l,n);n.far=0;n.farWho=null;
        BikeNightSite.swingsBack(l,n.base);
        remember(l,n);
    }

    // ------------------------------------------------------------------------------------------------ the swings
    /** A reader has let go of a park swing (HouseReadSwing.jump). How far out they come down is paced from under its bar. */
    public static void letGo(ServerPlayer p,Vec3 pivot,Direction forward){var n=night(p);if(n!=null)n.flights.put(p.getUUID(),new Flight(pivot,forward,n.tick));}
    /** 0.4.474: a reader on the sand has pushed another on one of the park's swings (HouseReadSwing.hand). It goes in both their records. */
    static void swingPushed(ServerPlayer p,ServerPlayer rider){
        var n=night(p);if(n==null||night(rider)!=n)return;
        n.swingPushes.merge(p.getUUID(),1,Integer::sum);n.pushedWhom.put(p.getUUID(),rider.getGameProfile().getName());n.pushedBy.put(rider.getUUID(),p.getGameProfile().getName());
    }
    static void flights(ServerLevel l,Night n){
        if(n.flights.isEmpty())return;
        for(var it=n.flights.entrySet().iterator();it.hasNext();){
            var f=it.next();var p=l.getServer().getPlayerList().getPlayer(f.getKey());long up=n.tick-f.getValue().tick();
            if(p==null||night(p)!=n||up>FLIGHT){it.remove();continue;}
            if(up<3||!(p.onGround()||p.isInWater()||p.isPassenger()))continue;
            it.remove();land(l,n,p,f.getValue());
        }
    }
    /** Down. On the sand it counts, and is told in paces; the furthest of the night is marked where it was. */
    static void land(ServerLevel l,Night n,ServerPlayer p,Flight f){
        var at=p.position();double out=(at.x-f.pivot().x)*f.forward().getStepX()+(at.z-f.pivot().z)*f.forward().getStepZ();
        int paces=(int)Math.floor(out*PACES);
        if(!p.onGround()||paces<LEAST)return;
        if(!BikeNightSite.onSand(rel(at,n.base))){cue(p,BikeNightTexts.JUMP_OFF);return;}
        n.landed.merge(p.getUUID(),paces,Math::max);
        boolean best=paces>n.far;
        if(best){n.far=paces;n.farWho=p.getUUID();unmark(l,n);mark(l,n,p.blockPosition(),f.forward());}
        cue(p,best?String.format(BikeNightTexts.JUMP_BEST,Stillness.words(paces)):paces==n.far?String.format(BikeNightTexts.JUMP_LEVEL,Stillness.words(paces))
            :String.format(BikeNightTexts.JUMP,Stillness.words(paces),Stillness.words(n.far)));
    }
    static void unmark(ServerLevel l,Night n){
        if(n.mark!=null&&l.isLoaded(n.mark)&&l.getBlockState(n.mark).is(BikeNightRegistry.MARK.get()))l.setBlock(n.mark,Blocks.AIR.defaultBlockState(),3);
        n.mark=null;
    }
    /** The mark: a line dug across the way the jumper was going, where there is bare sand to dig it in. */
    static void mark(ServerLevel l,Night n,BlockPos at,Direction forward){
        var here=l.getBlockState(at);
        if(!(here.isAir()||here.is(BikeNightRegistry.LITTER.get()))||!l.getBlockState(at.below()).is(Blocks.SAND))return;
        l.setBlock(at,BikeNightRegistry.MARK.get().defaultBlockState().setValue(CalderLitterBlock.FACING,forward),3);n.mark=at.immutable();
    }
    /** The playground's sand is soft. */
    @SubscribeEvent public static void fall(net.neoforged.neoforge.event.entity.living.LivingFallEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!here(p))return;var b=LiteraryVignettes.base(p,PLACE);
        if(b!=null&&BikeNightSite.onSand(rel(p.position(),b)))e.setDamageMultiplier(0);
    }

    // ------------------------------------------------------------------------------------------------ the slide
    /** A reader has been down the slide (SlideRide). */
    static void slid(ServerPlayer p){var n=night(p);if(n!=null)n.slid.merge(p.getUUID(),1,Integer::sum);}

    // ------------------------------------------------------------------------------------------------ the merry-go-round
    /** A reader has shoved the merry-go-round round (RoundaboutBlock.Wheel). */
    static void shoved(ServerPlayer p){var n=night(p);if(n!=null)n.shoved.merge(p.getUUID(),1,Integer::sum);}
    /** How often each reader's own record is brought up to date: what a look at who is on the merry-go-round stands for. */
    static final int LOOK=5;

    /** A reader on the spring rider has rocked it out toward a stop (SpringBlock.Rocker). */
    static void sprung(ServerPlayer p){var n=night(p);if(n!=null)n.sprung.merge(p.getUUID(),1,Integer::sum);}

    // ------------------------------------------------------------------------------------------------ the seesaw
    /** A reader on the seesaw has pushed off the sand (SeesawSeat); with whom on the other end, if anybody; and whether it was a bounce, made as they landed. */
    static void seesawed(ServerPlayer p,@Nullable ServerPlayer other,boolean bounce){
        var n=night(p);if(n==null)return;n.pushed.merge(p.getUUID(),1,Integer::sum);if(bounce)n.bounced.merge(p.getUUID(),1,Integer::sum);
        if(other!=null)n.rodeWith.put(p.getUUID(),other.getGameProfile().getName());
    }
    /** A reader was thrown off the top of the seesaw when the other end came down hard; under whom, if anybody. */
    static void thrownOff(ServerPlayer p,@Nullable ServerPlayer by){
        var n=night(p);if(n==null)return;n.bumpedOff.add(p.getUUID());cue(p,BikeNightTexts.SEESAW_THROWN);
        if(by!=null){n.bumpedWhom.put(by.getUUID(),p.getGameProfile().getName());cue(by,String.format(BikeNightTexts.SEESAW_THREW,p.getGameProfile().getName()));}
    }

    // ------------------------------------------------------------------------------------------------ the monkey bars
    /** A reader has taken hold of the monkey bars (BarsHang). */
    static void hung(ServerPlayer p){var n=night(p);if(n!=null)n.hung.merge(p.getUUID(),1,Integer::sum);}
    /** A reader's hand let go too early and missed the next rung: they are told, and drop. */
    static void missed(ServerPlayer p){var n=night(p);if(n==null)return;n.missed.merge(p.getUUID(),1,Integer::sum);cue(p,BikeNightTexts.BARS_EARLY);}
    /** A reader is off the monkey bars: until they have touched the ground, a jump at the bars does not catch them. */
    static void letGoOfBars(ServerPlayer p){var n=night(p);if(n!=null)n.letGo.add(p.getUUID());}
    /**
     * "A jump at the bars grabs one": a reader in the air under the monkey bars, near enough for their fists to reach, catches the
     * rung over them without a hand on it. Not one on a ladder (theirs is the hand), and not one who has just let go.
     */
    static void barsJump(Night n,List<ServerPlayer> here){
        for(var p:here){
            if(p.onGround()){n.letGo.remove(p.getUUID());continue;}
            if(p.isPassenger()||p.isSpectator()||p.onClimbable()||n.letGo.contains(p.getUUID()))continue;
            var r=rel(p.position(),n.base);
            if(Math.abs(r.z-(BikeNightSite.BARS_Z+.5))>BarsHang.SIDE||r.x<BikeNightSite.BARS_W-BarsHang.PAST||r.x>BikeNightSite.BARS_E+1+BarsHang.PAST||r.y>=BikeNightSite.BARS_Y)continue;
            var at=n.base.offset(BikeNightSite.rungsAt(net.minecraft.util.Mth.clamp(net.minecraft.util.Mth.floor(r.x)-BikeNightSite.BARS_W,0,BikeNightSite.rungs()-1)));
            if(BarsHang.cannot(p,at)==null)BarsHang.take(p,at);
        }
    }
    /** A reader has gone from one end of them to the other on one hold. */
    static void crossed(ServerPlayer p){var n=night(p);if(n==null)return;n.crossed.merge(p.getUUID(),1,Integer::sum);cue(p,BikeNightTexts.BARS_ACROSS);}

    // ------------------------------------------------------------------------------------------------ the sand
    /** A hand in the playground's sand packs a ball of it: one at a time, eight at most. */
    static void scoop(ServerPlayer p){
        var item=BikeNightRegistry.SAND_BALL.get();
        if(p.getCooldowns().isOnCooldown(item))return;
        int has=p.getInventory().countItem(item);
        if(has>=SandBallItem.MOST){cue(p,BikeNightTexts.SAND_FULL);return;}
        if(!p.getInventory().add(new net.minecraft.world.item.ItemStack(item))){cue(p,BikeNightTexts.SAND_NO_ROOM);return;}
        p.getCooldowns().addCooldown(item,SandBallItem.WAIT);
        p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);
        p.serverLevel().playSound(null,p.getX(),p.getY(),p.getZ(),BikeNightRegistry.SAND_SCOOP.get(),net.minecraft.sounds.SoundSource.PLAYERS,.6F,.9F+p.getRandom().nextFloat()*.2F);
        if(has==0)cue(p,BikeNightTexts.SAND);
    }
    /** A reader has thrown a ball of sand. */
    static void threw(ServerPlayer p){var n=night(p);if(n!=null)n.thrown.merge(p.getUUID(),1,Integer::sum);}
    /** A ball of sand has met somebody. Nobody is hurt by one; a reader on their feet is knocked a little the way it was going. */
    static void sanded(ServerLevel l,SandBall ball,net.minecraft.world.entity.Entity met){
        if(!(met instanceof ServerPlayer target))return;var n=night(target);if(n==null)return;
        if(!target.isPassenger()){var v=ball.getDeltaMovement();target.knockback(.15,-v.x,-v.z);target.hurtMarked=true;}
        if(ball.getOwner() instanceof ServerPlayer thrower&&thrower!=target)n.hits.merge(thrower.getUUID(),1,Integer::sum);
    }
    /** A ball of sand has met a block. The failing lamp, hit while it is out, comes back on, as a kicked one does. */
    static void sandLanded(ServerLevel l,SandBall ball,BlockPos at){
        var n=nightAt(l,at);if(n==null)return;var r=at.subtract(n.base);
        if(r.getX()==BikeNightSite.FLICKER.getX()&&r.getZ()==BikeNightSite.FLICKER.getZ()&&r.getY()>=0&&r.getY()<=BikeNightSite.LAMP_TOP&&!n.lampLit)n.lampNext=n.tick;
    }

    // ------------------------------------------------------------------------------------------------ each reader
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;var n=NIGHTS.computeIfAbsent(b.asLong(),k->new Night(b));var id=p.getUUID();
        if(p.getVehicle() instanceof io.github.knaitoe.theoldesthouse.house.SeatEntity seat&&seat.pumped()){
            own.putBoolean("Swung",true);
            if(!own.getBoolean("SwingTold")){own.putBoolean("SwingTold",true);cue(p,BikeNightTexts.SWING_HINT);}
        }
        Integer far=n.landed.remove(id);if(far!=null&&far>own.getInt("SwingBest"))own.putInt("SwingBest",far);
        Integer hands=n.swingPushes.remove(id);if(hands!=null)own.putInt("SwingPushes",own.getInt("SwingPushes")+hands);
        String pushedWhom=n.pushedWhom.remove(id);if(pushedWhom!=null)own.putString("SwingPushed",pushedWhom);
        String pushedBy=n.pushedBy.remove(id);if(pushedBy!=null)own.putString("SwingPushedBy",pushedBy);
        Integer balls=n.thrown.remove(id);if(balls!=null)own.putInt("Sand",own.getInt("Sand")+balls);
        Integer hits=n.hits.remove(id);if(hits!=null)own.putInt("SandHits",own.getInt("SandHits")+hits);
        Integer down=n.slid.remove(id);if(down!=null)own.putInt("Slid",own.getInt("Slid")+down);
        if(p.getVehicle() instanceof BarsHang&&!own.getBoolean("BarsTold")){own.putBoolean("BarsTold",true);cue(p,BikeNightTexts.BARS_HINT);}
        Integer holds=n.hung.remove(id);if(holds!=null)own.putInt("Hung",own.getInt("Hung")+holds);
        Integer across=n.crossed.remove(id);if(across!=null)own.putInt("Crossed",own.getInt("Crossed")+across);
        Integer misses=n.missed.remove(id);if(misses!=null)own.putInt("BarsMissed",own.getInt("BarsMissed")+misses);
        if(p.getVehicle() instanceof SeesawSeat){
            own.putBoolean("SeesawSat",true);
            if(!own.getBoolean("SeesawTold")){own.putBoolean("SeesawTold",true);cue(p,BikeNightTexts.SEESAW_HINT);}
        }
        Integer pushes=n.pushed.remove(id);if(pushes!=null)own.putInt("Seesaw",own.getInt("Seesaw")+pushes);
        Integer shoves=n.shoved.remove(id);if(shoves!=null)own.putInt("RoundPushed",own.getInt("RoundPushed")+shoves);
        // Standing on the top of the climbing dome: told once what is to be seen from there.
        if(!own.getBoolean("DomeTop")&&DomeBlock.onTop(b.offset(BikeNightSite.DOME),p)){own.putBoolean("DomeTop",true);cue(p,BikeNightTexts.DOME_TOP);}
        if(p.getVehicle() instanceof SpringSeat){
            own.putBoolean("SpringSat",true);
            if(!own.getBoolean("SpringTold")){own.putBoolean("SpringTold",true);cue(p,BikeNightTexts.SPRING_HINT);}
        }
        Integer rocks=n.sprung.remove(id);if(rocks!=null)own.putInt("Sprung",own.getInt("Sprung")+rocks);
        if(p.serverLevel().getBlockEntity(b.offset(BikeNightSite.ROUNDABOUT)) instanceof RoundaboutBlock.Wheel wheel){
            boolean on=wheel.on(p),was=Boolean.TRUE.equals(n.onWheel.put(id,on)),fast=Math.abs(wheel.speed())>RoundaboutBlock.Wheel.OUT;
            if(on&&wheel.speed()!=0){
                // How far round they have gone on it, by how far it turns between one look and the next.
                own.putDouble("RoundTurned",own.getDouble("RoundTurned")+Math.abs(wheel.speed())*LOOK);
                if(!own.getBoolean("RoundTold")){own.putBoolean("RoundTold",true);cue(p,BikeNightTexts.ROUNDABOUT_HINT);}
            }
            // Off it while it is going fast, standing up, and out past its rim: it threw them.
            var m=wheel.middle();double dx=p.getX()-m.x,dz=p.getZ()-m.z;
            if(was&&!on&&fast&&!p.isShiftKeyDown()&&!p.isPassenger()&&dx*dx+dz*dz>RoundaboutBlock.RADIUS*RoundaboutBlock.RADIUS)own.putInt("RoundFlung",own.getInt("RoundFlung")+1);
        }
        String with=n.rodeWith.remove(id);if(with!=null)own.putString("SeesawWith",with);
        Integer bounces=n.bounced.remove(id);if(bounces!=null)own.putInt("SeesawBounces",own.getInt("SeesawBounces")+bounces);
        if(n.bumpedOff.remove(id))own.putBoolean("SeesawThrown",true);
        String whom=n.bumpedWhom.remove(id);if(whom!=null)own.putString("SeesawThrew",whom);
    }

    // ------------------------------------------------------------------------------------------------ a hand on the park's things
    /** A hand on something in the park that is only there to be looked at. True if it was one of them. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();BlockState s=l.getBlockState(b.offset(rel));
        // The playground's sand, or the mark lying on it: a ball of it is packed. Anything else, with sand in the hand: the hand
        // is not looking, it is throwing (SandBallItem), so this says nothing and lets it.
        if(s.is(Blocks.SAND)&&BikeNightSite.sandAt(rel)||s.is(BikeNightRegistry.MARK.get())&&p.getMainHandItem().is(BikeNightRegistry.SAND_BALL.get())){scoop(p);return true;}
        if(p.getMainHandItem().is(BikeNightRegistry.SAND_BALL.get()))return false;
        if(s.is(BikeNightRegistry.MARK.get())){
            var n=night(p);var who=n==null?null:n.farWho;var other=who==null?null:p.server.getPlayerList().getPlayer(who);
            cue(p,who==null||other==null?BikeNightTexts.MARK:who.equals(p.getUUID())?BikeNightTexts.MARK_OWN:String.format(BikeNightTexts.MARK_THEIRS,other.getGameProfile().getName()));return true;
        }
        if(s.is(BikeNightRegistry.PLAY.get())){
            // The rungs of the monkey bars are taken hold of, by a reader who can reach them; one who cannot is told why.
            if(s.getValue(CalderPlayBlock.KIND)==CalderPlayBlock.Kind.RUNGS){
                if(p.isPassenger())return true;
                var why=BarsHang.cannot(p,b.offset(rel));if(why!=null)cue(p,why);else BarsHang.take(p,b.offset(rel));return true;
            }
            cue(p,switch(s.getValue(CalderPlayBlock.KIND)){
                case LADDER->s.getValue(CalderPlayBlock.STAGE)>=CalderPlayBlock.BARS?BikeNightTexts.BARS:BikeNightTexts.SLIDE;
                case DECK,CHUTE,RUNOUT->BikeNightTexts.SLIDE;default->BikeNightTexts.SWINGS;});return true;
        }
        // The merry-go-round is shoved round by a reader on the sand beside it; one who cannot is told why.
        if(RoundaboutBlock.is(s)){
            var wheel=RoundaboutBlock.wheel(l,b.offset(rel));
            if(wheel!=null&&!p.isPassenger()){var why=wheel.push(p);if(why!=null)cue(p,why);}
            return true;
        }
        if(DomeBlock.is(s)){cue(p,BikeNightTexts.DOME);return true;}
        // The spring rider is sat on, if nobody is on it.
        if(s.is(BikeNightRegistry.SPRING_RIDER.get())){
            if(p.isPassenger())cue(p,BikeNightTexts.SPRING);
            else if(SpringSeat.sit(p,b.offset(rel))==null)cue(p,BikeNightTexts.SPRING_TAKEN);
            return true;
        }
        // An end of the seesaw's board is sat on, if nobody is on it; the rest of it is looked at.
        if(s.is(BikeNightRegistry.SEESAW.get())){
            if(s.getValue(SeesawBlock.PART)!=SeesawBlock.Part.END||p.isPassenger())cue(p,BikeNightTexts.SEESAW);
            else if(SeesawSeat.sit(p,b.offset(rel))==null)cue(p,BikeNightTexts.SEESAW_TAKEN);
            return true;
        }
        if(CalderSignBlock.is(s)){cue(p,CalderSignBlock.sign(s).look());return true;}
        if(s.is(BikeNightRegistry.RACK.get())){cue(p,BikeNightTexts.RACK);return true;}
        if(!s.is(BikeNightRegistry.FITTING.get()))return false;
        switch(s.getValue(CalderBlock.KIND)){
            case GATE->{cue(p,BikeNightTexts.GATE);l.playSound(null,b.offset(rel),BikeNightRegistry.CHAIN.get(),net.minecraft.sounds.SoundSource.BLOCKS,.8F,.95F+l.getRandom().nextFloat()*.1F);}
            case FENCE->cue(p,BikeNightTexts.RAILINGS);
            case LAMP->{boolean failing=rel.getX()==BikeNightSite.FLICKER.getX()&&rel.getZ()==BikeNightSite.FLICKER.getZ();cue(p,failing?BikeNightTexts.LAMP_FAILING:BikeNightTexts.LAMP);}
            case BENCH->cue(p,s.getValue(CalderBlock.STAGE)==CalderBlock.CARVED?BikeNightTexts.CARVED:BikeNightTexts.BENCH);
            // The fountain runs for a little while when its button is pressed; the payphone's handset is held for a little while.
            case FOUNTAIN->{
                if(s.getValue(CalderBlock.STAGE)==CalderBlock.RUNNING){cue(p,BikeNightTexts.FOUNTAIN_RUNNING);break;}
                var at=b.offset(rel);l.setBlock(at,s.setValue(CalderBlock.STAGE,CalderBlock.RUNNING),net.minecraft.world.level.block.Block.UPDATE_CLIENTS);l.scheduleTick(at,s.getBlock(),CalderBlock.RUNS);
                l.playSound(null,at,BikeNightRegistry.FOUNTAIN.get(),net.minecraft.sounds.SoundSource.BLOCKS,.7F,.95F+l.getRandom().nextFloat()*.1F);
                cue(p,BikeNightTexts.FOUNTAIN);own.putBoolean("Drank",true);
            }
            case PHONE->{
                if(s.getValue(CalderBlock.STAGE)==CalderBlock.LIFTED){cue(p,BikeNightTexts.PHONE_HELD);break;}
                var at=b.offset(rel);l.setBlock(at,s.setValue(CalderBlock.STAGE,CalderBlock.LIFTED),net.minecraft.world.level.block.Block.UPDATE_CLIENTS);l.scheduleTick(at,s.getBlock(),CalderBlock.HELD);
                l.playSound(null,at,BikeNightRegistry.PHONE_TONE.get(),net.minecraft.sounds.SoundSource.BLOCKS,.6F,1F);
                cue(p,BikeNightTexts.PHONE);own.putBoolean("Phoned",true);
            }
            case BIN->cue(p,BikeNightTexts.BIN);
            case BORDER->cue(p,BikeNightTexts.BORDER);
            default->{return false;}
        }
        return true;
    }
}
