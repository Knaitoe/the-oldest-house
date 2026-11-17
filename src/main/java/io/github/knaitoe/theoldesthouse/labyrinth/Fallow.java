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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.672, Fallow, the first part (docs/FALLOW_0_4_672.md), from the owner's approval of docs/THREE_VIGNETTES_PROPOSAL_2026_11_17.md and
 * the plan in docs/FALLOW_PLAN.md. The place alone: a gas station and grocery on a two-lane road in the wheat, on a late summer
 * afternoon (FallowSite). Five people are there: Harlan, who keeps the station, on his stool behind the register; Faye at the coffee
 * machine, whose car is up on the lift; her daughter Opal out front; Faye's man Boyd in the garage, watching the car; and Otis, a long-haul
 * driver, by his rig at the diesel pump (stranger phases 263 to 267; the stranger's skin, on 268, is painted now and he comes in part 2).
 * Each has two lines, counted per reader, and every thing answers a hand. The cellar's doors open and shut for anyone. The House does not
 * deal it until part 2.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Fallow {
    private Fallow(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.FALLOW;
    /** The five at the station on 263 to 267, and the stranger on 268. */
    public static final int FIRST=263,KEEPER=0,WOMAN=1,GIRL=2,MAN=3,DRIVER=4,PEOPLE=5,STRANGER=5,CAST=6;
    private static final String[] SKINS={"fallow_harlan","fallow_faye","fallow_opal","fallow_boyd","fallow_otis","fallow_stranger"};
    private static final BlockPos[] AT={FallowSite.KEEPER_AT,FallowSite.WOMAN_AT,FallowSite.GIRL_AT,FallowSite.MAN_AT,FallowSite.DRIVER_AT};
    /** Which way each faces: the keeper the counter, Faye the coffee machine, Opal the road, Boyd the car, Otis his pump. */
    private static final Direction[] FACING={Direction.NORTH,Direction.EAST,Direction.NORTH,Direction.SOUTH,Direction.EAST};
    public static boolean cast(int phase){return phase>=FIRST&&phase<FIRST+CAST;}
    public static boolean child(int phase){return phase==FIRST+GIRL;}
    public static String skin(int phase){return SKINS[phase-FIRST];}
    public static boolean is(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    /** Not dealt until part 2, when the afternoon begins. */
    public static boolean dealable(LabyrinthData d,UUID reader){return false;}

    /** A reader in: the five are put at their places if they are not there, the car is up on the lift, and the afternoon is heard. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();ensure(l,b);car(l,b);
        own.putInt("Visits",own.getInt("Visits")+1);
        ACTIVE.computeIfAbsent(b.asLong(),k->new Site(b));
        l.playSound(null,b.offset(0,1,FallowSite.STORE_N-2),FallowRegistry.WIND.get(),SoundSource.AMBIENT,.4F,1F);
    }

    /** One of the five, if their body is near their place. */
    public static @Nullable LiteraryActor find(ServerLevel l,BlockPos b,int who){
        var at=b.offset(AT[who]);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,new AABB(at).inflate(4),x->is(x)&&x.phase()==FIRST+who))if(a.isAlive())return a;
        return null;
    }
    /** Each of the five is put at their place if they are not there; nothing is made while their entities are unloaded. */
    public static void ensure(ServerLevel l,BlockPos b){
        for(int who=0;who<PEOPLE;who++){
            var at=b.offset(AT[who]);
            if(!l.isLoaded(at)||!l.areEntitiesLoaded(ChunkPos.asLong(at)))continue;
            var a=find(l,b,who);
            if(a==null){
                a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;
                a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,FIRST+who);
                stand(a,at,FACING[who]);
                if(!l.addFreshEntity(a))continue;
            }
            stand(a,at,FACING[who]);
        }
    }
    /** At the cell, turned the way given; the keeper on his stool (ActorSeat lowers him onto it and keeps him there). */
    static void stand(LiteraryActor a,BlockPos at,Direction facing){
        float yaw=facing.toYRot();
        a.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);a.gesture(0);a.setDeltaMovement(Vec3.ZERO);
    }

    /** The car up on the lift, a little above a reader's head, facing out of the bay; a car that nobody keeps telling where it is goes. */
    static @Nullable TownCar car(ServerLevel l,BlockPos b){
        var at=Vec3.atLowerCornerOf(b).add(FallowSite.CAR_AT);var cell=BlockPos.containing(at);
        if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell)))return null;
        TownCar car=null;
        for(var c:l.getEntitiesOfClass(TownCar.class,new AABB(cell).inflate(3),c->c.isAlive()))car=c;
        if(car==null){
            car=BikeNightRegistry.TOWN_CAR.get().create(l);if(car==null)return null;
            car.kind(TownCar.BLUE);car.place(at,180F);
            if(!l.addFreshEntity(car))return null;
        }
        car.place(at,180F);
        return car;
    }

    /** A word with one of the five: their own first line, then a second, then the second again. */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!is(a)||a.phase()>=FIRST+PEOPLE)return;
        int who=a.phase()-FIRST,heard=own.getInt("Heard"+who);
        String line=FallowTexts.line(who,heard);
        own.putInt("Heard"+who,heard+1);
        a.say(FallowTexts.NAMES[who],line);a.speaks(line);
    }

    /** A hand on something of the station's. True if it was theirs to answer. The note is the first paper: the House's own reading answers it. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        if(st.getBlock() instanceof DoorBlock){
            var lower=st.getValue(DoorBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER?rel.below():rel;
            if(lower.equals(FallowSite.TRAILER_DOOR)){cue(p,FallowTexts.TRAILER_DOOR);return true;}
            return false;
        }
        if(!FallowBlock.is(st))return false;
        int stage=st.getValue(FallowBlock.STAGE);var kind=st.getValue(FallowBlock.KIND);
        // The cellar's doors swing back on their hinges for anyone, and shut again; they stay as they are left.
        if(kind==FallowBlock.Kind.CELLAR||kind==FallowBlock.Kind.CELLAR_OPEN){
            boolean open=kind==FallowBlock.Kind.CELLAR;
            cellarDoors(l,b,open);
            l.playSound(null,b.offset(FallowSite.WELL_W,0,FallowSite.WELL_S-1),open?net.minecraft.sounds.SoundEvents.WOODEN_TRAPDOOR_OPEN:net.minecraft.sounds.SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.8F,.7F);
            cue(p,open?FallowTexts.CELLAR_OPENED:FallowTexts.CELLAR_SHUT);
            return true;
        }
        if(kind==FallowBlock.Kind.PUMP&&(stage==0||stage==2))l.playSound(null,b.offset(rel),FallowRegistry.BELL.get(),SoundSource.BLOCKS,.6F,1F);
        String line=FallowTexts.thing(kind,stage);
        if(line==null)return false;
        cue(p,line);
        return true;
    }
    /** The cellar's six lengths of door, all shut or all thrown back. */
    public static void cellarDoors(ServerLevel l,BlockPos b,boolean open){
        for(int z=FallowSite.WELL_S;z>=FallowSite.WELL_N;z--)for(int x=FallowSite.WELL_W;x<=FallowSite.WELL_E;x++){
            var at=b.offset(x,0,z);var st=l.getBlockState(at);
            if(!FallowBlock.is(st,FallowBlock.Kind.CELLAR)&&!FallowBlock.is(st,FallowBlock.Kind.CELLAR_OPEN))continue;
            l.setBlock(at,FallowBlock.of(open?FallowBlock.Kind.CELLAR_OPEN:FallowBlock.Kind.CELLAR,st.getValue(FallowBlock.FACING),st.getValue(FallowBlock.STAGE)),3);
        }
    }
    public static boolean cellarOpen(ServerLevel l,BlockPos b){return FallowBlock.is(l.getBlockState(b.offset(FallowSite.WELL_W,0,FallowSite.WELL_S)),FallowBlock.Kind.CELLAR_OPEN);}

    // ------------------------------------------------------------------------------------------------ the afternoon
    /** The sites a reader is in or has just left: their clock for the afternoon's sounds and the car on the lift. */
    static final Map<Long,Site> ACTIVE=new HashMap<>();
    static final class Site{final BlockPos base;int tick,empty;Site(BlockPos b){base=b;}}
    static final int EMPTY_FORGET=100;
    /** Everyone in the scene, store, yard, cellar, road and wheat alike. */
    public static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players()){
            if(p.isSpectator()||!p.isAlive())continue;
            if(FallowSite.inScene(p.position().subtract(Vec3.atLowerCornerOf(b))))out.add(p);
        }
        return out;
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){ACTIVE.clear();}
    public static void clearAll(){ACTIVE.clear();}
    /**
     * Every few seconds while someone is there, the afternoon: the wind going over the wheat near a reader, the insects in it, and, while
     * a reader is in the store, the radio on the back shelf. Each is heard where it is. Every second the car on the lift is told where it is.
     */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(ACTIVE.isEmpty())return;
        var l=e.getServer().getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var it=ACTIVE.values().iterator();it.hasNext();){
            var s=it.next();var here=readers(l,s.base);
            if(here.isEmpty()){
                if(++s.empty>=EMPTY_FORGET){var c=car(l,s.base);if(c!=null)c.discard();it.remove();}
                continue;
            }
            s.empty=0;s.tick++;
            if(s.tick%20==0){car(l,s.base);ensure(l,s.base);}
            var rnd=l.getRandom();var one=here.get(rnd.nextInt(here.size()));
            if(s.tick%220==40)play(l,one.blockPosition().offset(rnd.nextInt(17)-8,1,rnd.nextInt(17)-8),FallowRegistry.WIND.get(),.55F);
            if(s.tick%260==130){
                var r=one.position().subtract(Vec3.atLowerCornerOf(s.base));
                if(!FallowSite.inStore(r)&&!FallowSite.inCellar(r))play(l,one.blockPosition().offset(rnd.nextInt(9)-4,0,rnd.nextInt(9)-4),FallowRegistry.INSECTS.get(),.5F);
            }
            if(s.tick%180==60&&here.stream().anyMatch(p->FallowSite.inStore(p.position().subtract(Vec3.atLowerCornerOf(s.base)))))
                play(l,s.base.offset(FallowSite.RADIO),FallowRegistry.RADIO.get(),.4F);
        }
    }
    static void play(ServerLevel l,BlockPos at,SoundEvent e,float volume){l.playSound(null,at,e,SoundSource.AMBIENT,volume,.95F+l.getRandom().nextFloat()*.1F);}
}
