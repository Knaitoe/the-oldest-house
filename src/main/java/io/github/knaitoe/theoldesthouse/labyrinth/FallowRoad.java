package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static io.github.knaitoe.theoldesthouse.labyrinth.FallowSite.*;

/**
 * 0.4.683, Fallow 5a (docs/FALLOW_0_4_683.md): the road. A reader the light found out in it is sent back into the House; the station
 * is held for them a while, and when they come back through the House's door they find the store open to the sky and their shadow on
 * its front. Once the five are up, Opal sets her bean down in its cup in the yard by the steps and sits with it, and the ground round her comes up
 * green a ring at a time. Some while after, a man comes walking down the road out of the ash, from the way the pads were: the one who
 * bought a soda that afternoon. Each time he is looked at again he has somebody else's face, and last the face of whoever is looking.
 * When he is first seen, Faye (or Otis, when Faye is not up) takes Opal down the cellar steps out of his sight; her bean stays in the
 * ground. He comes to the yard and stands west of the doors, looking at them. What he asks is the next half's.
 */
public final class FallowRoad {
    private FallowRoad(){}
    /** How long a station is held, counted while nobody is at it, for a reader the light found out in it who has not come back. */
    public static final int RETURN_HOLD=6000;
    /** Where Opal sets her bean, beside where she sits by the steps; the cells she may use if that one is taken. */
    static final BlockPos[] PLANT={new BlockPos(-12,0,-8),new BlockPos(-11,0,-8),new BlockPos(-11,0,-7),new BlockPos(-12,0,-6)};
    /** How long she sits by the steps before she sets it down; how long she kneels over it. */
    public static final int PLANT_AFTER=100,KNEELING=40;
    /** The green: a ring further out every GROW_RING ticks while she sits with it, out to GROW_MAX blocks; the shoots grow on into young
     * wheat GREEN_AFTER rings behind the edge. */
    public static final int GROW_RING=40,GROW_MAX=20,GREEN_AFTER=3;
    /** How long after the bean is in the ground the man comes, and where he is made: out on the pads' track in the ash, where nobody sees. */
    public static final int COMES_AFTER=600;
    static final Vec3 COMES_FROM=new Vec3(20.5,0,-84.5);
    /** Where he stops: in the yard west of the cellar doors, looking at them. */
    static final Vec3 STOPS=new Vec3(-16.5,0,-9.5),LOOKS_AT=new Vec3(-13.5,0,-9.5);
    /** Down the track to the road, straight across it between two of the fallen poles (they lie across it at x 16 and 32), along the
     * apron's edge north of the rig and the canopy's fall, and through the yard's gate. */
    static final List<Vec3> DOWN_THE_ROAD=List.of(new Vec3(20.5,0,-60.5),new Vec3(20.5,0,-37.5),new Vec3(20.5,0,-27.5),new Vec3(-15.5,0,-25.5),
        new Vec3(-15.5,0,-19.5),STOPS);
    /** His walk, slower than anyone's. */
    static final double PACE=.07;
    /** The man on the road: not come, walking down it, stood in the yard. */
    public static final int NOT_YET=0,WALKING=1,STOOD=2;
    /** His faces, in turn (LiteraryActor.marks, drawn by the client): his own, the keeper's, the big man's, the driver's, and last the
     * face of whoever is looking at him. */
    public static final int OWN_FACE=0,KEEPER_FACE=1,BOYD_FACE=2,OTIS_FACE=3,VIEWER=4;
    /** Whose skin each borrowed face is (the client looks it up), the viewer's own aside. */
    public static int faceOf(int face){return switch(face){case KEEPER_FACE->Fallow.KEEPER;case BOYD_FACE->Fallow.MAN;case OTIS_FACE->Fallow.DRIVER;default->Fallow.STRANGER;};}
    static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ held for the ones outside
    /** Whether a station nobody is at is held: after the light, while someone it found out in it has not come back. */
    static boolean waiting(Fallow.Site s){
        var st=s.strike;
        return st.toneAt>=0&&st.since(s.tick)>=FallowStrike.LIGHT&&!st.back.containsAll(st.outside);
    }
    /** One the light found out in it, at the station again once their faint has sent them off: back, under the store's open roof. Counted
     * by presence, however they came, so that nobody is held for who is already here; not while the faint is still taking them out. */
    static void back(Fallow.Site s,List<ServerPlayer> here){
        var st=s.strike;
        for(var p:here){
            if(!st.outside.contains(p.getUUID())||st.faint.containsKey(p.getUUID())||LabyrinthDoors.isBusy(p)||!st.back.add(p.getUUID()))continue;
            var own=Fallow.own(p);own.putBoolean("CameBack",true);Fallow.save(p,own);
            Fallow.cue(p,FallowTexts.ARRIVE_AFTER);
        }
    }

    // ------------------------------------------------------------------------------------------------ after the light, while anyone is there
    static void tick(ServerLevel l,Fallow.Site s,List<ServerPlayer> here,int k){
        back(s,here);
        if(s.tick%10==0)shadows(l,s,here);
        plant(l,s,here);
        green(l,s,here);
        road(l,s,here);
        // 0.4.684: what he asks at the doors, and the steps.
        FallowSteps.tick(l,s,here);
    }

    // ------------------------------------------------------------------------------------------------ the shadows
    /** A reader near the front who looks at a shadow on it, or puts a hand to the wall where one is, finds it: their own, another's. */
    static void shadows(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        for(var p:here){
            var r=FallowStrike.rel(s,p);
            if(r.z<STORE_N-14||r.z>STORE_N+1||FallowSite.inCellar(r)||whitedOut(s,p))continue;
            for(int x:FallowRuin.WALL){
                var at=s.base.offset(x,0,STORE_N-1);
                if(!(l.getBlockEntity(at) instanceof FallowShadowBlock.Shadow sh)||sh.reader==null)continue;
                var mid=Vec3.atCenterOf(at).add(0,.5,0);
                if(p.position().distanceToSqr(mid)>36||!io.github.knaitoe.theoldesthouse.house.HouseWatchers.sees(p,mid))continue;
                var own=Fallow.own(p);found(p,s,sh,x,false,own);Fallow.save(p,own);
            }
        }
    }
    /** A hand on the wall over a shadow (Fallow.click), or a long enough look at it. */
    static boolean touched(ServerPlayer p,Fallow.Site s,BlockPos rel,CompoundTag own){
        // The wall itself, or the shadow lying against it.
        if(rel.getZ()!=STORE_N&&rel.getZ()!=STORE_N-1||rel.getY()<0||rel.getY()>2)return false;
        var l=p.serverLevel();
        if(whitedOut(s,p)||!(l.getBlockEntity(s.base.offset(rel.getX(),0,STORE_N-1)) instanceof FallowShadowBlock.Shadow sh)||sh.reader==null)return false;
        found(p,s,sh,rel.getX(),true,own);
        return true;
    }
    /** One the light found out in it, between the light and being sent off by the faint: they see nothing. */
    static boolean whitedOut(Fallow.Site s,ServerPlayer p){var st=s.strike;return st.outside.contains(p.getUUID())&&!st.back.contains(p.getUUID());}
    /** Written into the reader's own record as given, which the caller saves (a hand on the wall is saved by the click's own handler). */
    static void found(ServerPlayer p,Fallow.Site s,FallowShadowBlock.Shadow sh,int x,boolean hand,CompoundTag own){
        var seen=s.strike.shadowsFound.computeIfAbsent(p.getUUID(),u->new HashSet<>());
        boolean first=seen.add(x);
        if(!first&&!hand)return;
        if(sh.reader.equals(p.getUUID())){
            if(first){own.putBoolean("FoundShadow",true);Fallow.cue(p,FallowTexts.FOUND_OWN);}
            else Fallow.cue(p,FallowTexts.OWN_ON_HAND);
            return;
        }
        if(first&&!sh.person)own.putInt("OthersShadows",own.getInt("OthersShadows")+1);
        Fallow.cue(p,(sh.reach?FallowTexts.SHADOW_REACHING:FallowTexts.SHADOW_OF).formatted(sh.name));
    }

    // ------------------------------------------------------------------------------------------------ the girl's bean
    /** Up and sat by the steps a while, Opal kneels and sets her bean down in its cup on the yard beside her, and sits by it. */
    static void plant(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        var st=s.strike;
        if(st.planted||st.satAt<0||(st.stood&1<<Fallow.GIRL)==0||st.road>=WALKING)return;
        var girl=Fallow.find(l,s.base,Fallow.GIRL);if(girl==null||Fallow.busy(s,Fallow.GIRL))return;
        int since=s.tick-st.satAt;
        if(st.kneelAt<0){
            if(since<PLANT_AFTER)return;
            var at=cell(l,s);if(at==null)return;
            st.bean=at;st.kneelAt=s.tick;
            Fallow.faceTo(girl,Vec3.atCenterOf(s.base.offset(at)));girl.gesture(Fallow.KNEEL);
            FallowBuried.say(girl,"Opal",FallowTexts.OPAL_PLANTS);
            return;
        }
        if(s.tick-st.kneelAt<KNEELING)return;
        var at=s.base.offset(st.bean);
        if(!l.getBlockState(at).isAir()||occupied(l,at)){st.kneelAt=-1;return;}
        BuildBlocks.set(l,at,FallowBlock.of(FallowBlock.Kind.BEAN,Direction.SOUTH,Math.max(0,st.beanStage)),F);
        l.playSound(null,at,SoundEvents.ROOTED_DIRT_PLACE,SoundSource.BLOCKS,.5F,1.1F);
        girl.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        girl.gesture(Fallow.SIT);
        st.planted=true;st.plantedAt=s.tick;st.ring=0;
        FallowBuried.say(girl,"Opal",FallowTexts.OPAL_SITS_WITH_IT);
    }
    /** The first of her cells that is clear yard ground. */
    static BlockPos cell(ServerLevel l,Fallow.Site s){
        for(var c:PLANT){
            var at=s.base.offset(c);
            if(l.isLoaded(at)&&l.getBlockState(at).isAir()&&ground(l.getBlockState(at.below()))&&!occupied(l,at))return c;
        }
        return null;
    }
    static boolean ground(net.minecraft.world.level.block.state.BlockState s){
        return s.is(FallowRegistry.material("yard"))||s.is(FallowRegistry.material("soil"))||s.is(FallowRegistry.material("ash_ground"));
    }
    static boolean occupied(ServerLevel l,BlockPos at){return !l.getEntitiesOfClass(LivingEntity.class,new AABB(at)).isEmpty();}

    /** While she sits with it, the ground round her comes up: a ring at a time, shoots through the yard's dirt and through the ash, the
     * ash going back to soil under them; and behind the edge the shoots grow into young wheat. Nothing comes up where a body stands, on
     * the doors, on the floors or on the road. */
    static void green(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        var st=s.strike;
        if(!st.planted||st.ring>=GROW_MAX+GREEN_AFTER)return;
        // 0.4.684: round her bean; or, once she is out past the fence, round wherever she sits.
        var c=st.greenAt!=null?st.greenAt:st.bean;
        var girl=Fallow.find(l,s.base,Fallow.GIRL);
        if(girl==null||girl.gesture()!=Fallow.SIT||girl.position().distanceToSqr(Vec3.atCenterOf(s.base.offset(c)))>4)return;
        int due=(s.tick-st.plantedAt)/GROW_RING;
        if(due<=st.ring)return;
        int r=++st.ring;
        for(int x=c.getX()-r;x<=c.getX()+r;x++)for(int z=c.getZ()-r;z<=c.getZ()+r;z++){
            double d=Math.hypot(x-c.getX(),z-c.getZ());
            if(r<=GROW_MAX&&d>r-1&&d<=r)shoot(l,s.base,x,z);
            int g=r-GREEN_AFTER;
            if(g>=1&&d>g-1&&d<=g)grow(l,s.base,x,z);
        }
        var mid=Vec3.atCenterOf(s.base.offset(c));
        if(r<=GROW_MAX){
            l.playSound(null,mid.x,mid.y,mid.z,SoundEvents.AZALEA_LEAVES_PLACE,SoundSource.BLOCKS,.25F,.8F+r*.01F);
            for(var p:here){
                if(p.position().distanceToSqr(mid)>24*24)continue;
                var own=Fallow.own(p);
                if(r>=2&&!own.getBoolean("SawGreen")){own.putBoolean("SawGreen",true);Fallow.save(p,own);Fallow.cue(p,FallowTexts.GREENING);}
            }
        }
        if(r==4)FallowBuried.say(girl,"Opal",FallowTexts.OPAL_COMING_UP);
    }
    static void shoot(ServerLevel l,BlockPos b,int x,int z){
        var top=b.offset(x,0,z);if(!l.isLoaded(top)||occupied(l,top))return;
        var under=top.below();var ground=l.getBlockState(under);var st=l.getBlockState(top);
        boolean ashWheat=FallowBlock.is(st,FallowBlock.Kind.WHEAT)&&st.getValue(FallowBlock.STAGE)>=FallowBlock.STUBBLE&&st.getValue(FallowBlock.STAGE)<=FallowBlock.ASH;
        if(!st.isAir()&&!ashWheat)return;
        if(ground.is(FallowRegistry.material("ash_ground")))BuildBlocks.set(l,under,FallowRegistry.material("soil").defaultBlockState(),F);
        else if(!ground.is(FallowRegistry.material("yard"))&&!ground.is(FallowRegistry.material("soil")))return;
        BuildBlocks.set(l,top,FallowSite.wheatAt(x,z).setValue(FallowBlock.STAGE,FallowBlock.SHOOT),F);
    }
    static void grow(ServerLevel l,BlockPos b,int x,int z){
        var top=b.offset(x,0,z);if(!l.isLoaded(top))return;
        var st=l.getBlockState(top);
        if(FallowBlock.is(st,FallowBlock.Kind.WHEAT)&&st.getValue(FallowBlock.STAGE)==FallowBlock.SHOOT)BuildBlocks.set(l,top,st.setValue(FallowBlock.STAGE,FallowBlock.GREEN),F);
    }

    // ------------------------------------------------------------------------------------------------ the man on the road
    /** He comes a while after the bean is in, made out on the pads' track where nobody sees, and walks down to the road and along it to
     * the yard. Whoever first sees him is told; Faye, or Otis, takes Opal down the steps. His face changes each time he has been seen
     * and then is out of everyone's sight, spectators counted; the readers who see each face keep it. */
    static void road(ServerLevel l,Fallow.Site s,List<ServerPlayer> here){
        var st=s.strike;
        if(st.road==NOT_YET){
            if(!st.planted||s.tick-st.plantedAt<COMES_AFTER)return;
            var at=s.world(COMES_FROM);var cell=BlockPos.containing(at);
            if(!l.isLoaded(cell)||!l.areEntitiesLoaded(ChunkPos.asLong(cell))||inView(watchers(l,s),at))return;
            var old=Fallow.find(l,s.base,Fallow.STRANGER);if(old!=null)old.discard();
            var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;
            a.bind(Fallow.PLACE,null);a.appearance(LiteraryActor.STRANGER,Fallow.FIRST+Fallow.STRANGER);a.marks(OWN_FACE);
            a.moveTo(at.x,at.y,at.z,0,0);a.setYBodyRot(0);a.setYHeadRot(0);
            if(!l.addFreshEntity(a))return;
            st.road=WALKING;st.roadAt=s.tick;st.face=OWN_FACE;st.looked=false;
            s.away.add(Fallow.STRANGER);s.speed.put(Fallow.STRANGER,PACE);Fallow.send(s,Fallow.STRANGER,DOWN_THE_ROAD);
            return;
        }
        var a=Fallow.find(l,s.base,Fallow.STRANGER);if(a==null)return;
        // A reader who sees him keeps the face he has; only readers' sight makes him "looked at", but nobody at all, spectators too, may
        // have him in view when his face changes.
        boolean seen=false;
        for(var p:here){
            if(!inView(List.of(p),a.position()))continue;
            seen=true;
            var own=Fallow.own(p);int faces=own.getInt("SawFaces");
            if((faces&1<<st.face)==0){
                own.putInt("SawFaces",faces|1<<st.face);Fallow.save(p,own);
                Fallow.cue(p,st.face==OWN_FACE?FallowTexts.STRANGER_SEEN:st.face==VIEWER?FallowTexts.YOUR_FACE:FallowTexts.HIS_FACE.formatted(FallowTexts.NAMES[faceOf(st.face)]));
            }
        }
        if(seen){st.looked=true;if(!st.hid)hide(l,s);}
        else if(st.looked&&st.face<VIEWER&&!inView(watchers(l,s),a.position())){st.face++;a.marks(st.face);st.looked=false;}
        if(st.road==WALKING&&!Fallow.busy(s,Fallow.STRANGER)){st.road=STOOD;Fallow.faceTo(a,s.world(LOOKS_AT));a.gesture(0);}
    }
    /** Whether any of them has a body at this spot in view, at the head or the feet: within range, inside the cone of their look and
     * with nothing between. As HouseWatchers.sees, but a spectator's look counts: they cannot change anything, but nothing changes in
     * front of them either. */
    static boolean inView(List<ServerPlayer> watching,Vec3 at){
        for(var p:watching)for(double y:new double[]{1.6,.3})if(looksAt(p,at.add(0,y,0)))return true;
        return false;
    }
    static boolean looksAt(ServerPlayer p,Vec3 point){
        if(p.isSleeping())return false;
        var eye=p.getEyePosition();var to=point.subtract(eye);double d=to.length();
        if(d>io.github.knaitoe.theoldesthouse.house.HouseWatchers.RANGE)return false;
        if(d<1.5)return true;
        if(p.getLookAngle().dot(to.scale(1/d))<.26)return false;
        var hit=p.serverLevel().clip(new net.minecraft.world.level.ClipContext(eye,point,net.minecraft.world.level.ClipContext.Block.VISUAL,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
        return hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS||hit.getBlockPos().distManhattan(BlockPos.containing(point))<=1;
    }
    /** Spoken to, he does not answer yet (what he asks is the next half's): he looks past the reader, at the cellar doors. */
    static void talk(ServerPlayer p,Fallow.Site s){
        if(s.strike.road!=NOT_YET)Fallow.cue(p,FallowTexts.LOOKS_PAST);
    }
    // ------------------------------------------------------------------------------------------------ a fresh afternoon
    /** The girl's bean out of the yard. What came up round it is put back with the field (FallowAsh.mend): the field's own wheat where it
     * was sown, bare yard where it was not. */
    static void afresh(ServerLevel l,BlockPos b){afresh(l,b,List.of());}
    /** As above, leaving a bean that someone at the station can see where it is (one left from an afternoon whose yard was not loaded
     * when it ended is taken up during the next, before the light, when nobody is looking). */
    static void afresh(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        for(var c:PLANT){
            var at=b.offset(c);
            if(l.isLoaded(at)&&FallowBlock.is(l.getBlockState(at),FallowBlock.Kind.BEAN)&&!Drawing.seen(here,Vec3.atBottomCenterOf(at)))l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
        }
    }

    // ------------------------------------------------------------------------------------------------ for the tests
    public static boolean held(BlockPos b){var s=Fallow.afternoon(b);return s!=null&&waiting(s);}
    public static @Nullable BlockPos bean(BlockPos b){var s=Fallow.afternoon(b);return s==null||!s.strike.planted?null:s.strike.bean;}
    public static int ring(BlockPos b){var s=Fallow.afternoon(b);return s==null?-1:s.strike.ring;}
    public static int road(BlockPos b){var s=Fallow.afternoon(b);return s==null?-1:s.strike.road;}
    public static int face(BlockPos b){var s=Fallow.afternoon(b);return s==null?-1:s.strike.face;}
    public static boolean hid(BlockPos b){var s=Fallow.afternoon(b);return s!=null&&s.strike.hid;}
    /** He is made on the road now, as if the bean had been in the ground long enough, so that a test need not wait for him. */
    public static void comeNow(BlockPos b){var s=Fallow.afternoon(b);if(s!=null&&s.strike.planted)s.strike.plantedAt=Math.min(s.strike.plantedAt,s.tick-COMES_AFTER);}
    /** His walk made quicker, so that a test need not wait the whole road. */
    public static void hurry(BlockPos b,double pace){var s=Fallow.afternoon(b);if(s!=null)s.speed.put(Fallow.STRANGER,pace);}

    /** Everyone at the station who could see him change, spectators too. */
    static List<ServerPlayer> watchers(ServerLevel l,Fallow.Site s){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(p.isAlive()&&FallowSite.inScene(FallowStrike.rel(s,p)))out.add(p);
        return out;
    }
    /** When he is first seen on the road, Opal is taken down the steps out of his sight: by Faye if she is up, by Otis if not. */
    static void hide(ServerLevel l,Fallow.Site s){
        var st=s.strike;st.hid=true;
        var girl=Fallow.find(l,s.base,Fallow.GIRL);if(girl==null)return;
        int who=(st.stood&1<<Fallow.WOMAN)!=0?Fallow.WOMAN:Fallow.DRIVER;st.taker=who;
        var taker=Fallow.find(l,s.base,who);
        girl.gesture(0);
        Fallow.send(s,Fallow.GIRL,FallowStrike.toCellar(girl.position().subtract(Vec3.atLowerCornerOf(s.base)),FallowStrike.OPAL_BELOW));s.speed.put(Fallow.GIRL,.1);
        if(taker!=null){
            taker.gesture(0);
            Fallow.send(s,who,FallowStrike.toCellar(taker.position().subtract(Vec3.atLowerCornerOf(s.base)),who==Fallow.WOMAN?FallowStrike.FAYE_REST:FallowStrike.OTIS_REST));s.speed.put(who,.1);
            FallowBuried.say(taker,FallowTexts.NAMES[who],who==Fallow.WOMAN?FallowTexts.FAYE_TAKES_HER:FallowTexts.OTIS_TAKES_HER);
        }
    }
}
