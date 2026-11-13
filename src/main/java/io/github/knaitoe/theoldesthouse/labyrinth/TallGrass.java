package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.349, In the Tall Grass (docs/TALL_GRASS_0_4_349.md), from the owner's draft "The House Has a Yard": "A separate roadside threshold
 * begins on the shoulder, where a boy sounds close enough to help. Let the player see the road from the first few rows. Once they
 * commit, the road recedes even though the boy's voice stays close. In co-op, one reader can call from the shoulder. The first wrong
 * bearing comes when the other reader hears that call ahead. At the end, another car pulls over and the player has a chance to shout
 * something to the person getting out."
 *
 * <p>The road, the wagon with its hazards ticking, the grass. From the first rows the road shows back through the stems; past the old
 * fence posts a reader has gone in. Then the field moves them: every so often, while nobody outside their own huddle can see them, the
 * huddle is set down somewhere else in the grass, farther from the road, and the boy's voice is always a little ahead. What can be
 * trusted: a reader's own track (trampled stems spring back slowly), and the hazards heard true by a reader standing still. A reader on
 * the shoulder can call into the grass; the first call comes from ahead, and later ones from the true side only while the caller stands
 * still by the car. Readers in the grass hear one another from the wrong side. When someone has followed the voice long enough, the
 * field lets everyone in it out at its edge, another car pulls over, and its driver gets out and asks if somebody shouted. Every reader
 * at the edge may shout something; the driver does what the first shout says. Each reader's account is built from their own record.
 * Nothing here hurts.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TallGrass {
    private TallGrass(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.TALL_GRASS;
    /** The driver of the next car, on a stranger phase. */
    public static final int DRIVER=115;
    public static final String TALK="tall_grass";
    /** 0.4.555: how many readers one road holds, a carload for the wagon on the shoulder; the seventh is given a road of their own (SceneInstances). */
    public static final int PLACES=6;
    public static final String WARNED="warned_them_off_the_grass",CALLED="called_them_into_the_grass",QUIET="kept_quiet_at_the_road";
    /** Ticks: how long a reader must follow the voice before the field lets them out (and the most it will keep anyone); the gaps between
     * moves; how long the driver waits for a shout; an empty road's reset; the trampled grass's three stages back up. */
    public static final int FOLLOW=1600,KEEP_AT_MOST=3600,MOVE_MIN=140,MOVE_MAX=220,SHOUT_WAIT=300,EMPTY_RESET=200;
    public static final int FLAT=400,RISING=300,NEARLY=300;
    /** The end: not begun; letting readers out at the edge; the car coming; the driver getting out; waiting for a shout; the driver gone. */
    public static final int NONE=0,LETTING_OUT=1,CAR_COMING=2,DRIVER_OUT=3,CALLING=4,AFTER=5;
    static final String[] BOY={"Hello? Is someone there? I can't find the road.","I can hear you. You're close.","Over here. Keep coming.","I've been walking a long time.",
        "Don't go back that way. That isn't the way.","Can you see the road? I can hear the cars.","Please. It's getting dark in here."};

    static final class Field {
        final BlockPos base;long tick;int empty,end=NONE,endAt;boolean fresh=true;
        String outcome="",shoutBy="";@Nullable UUID driver;
        final List<UUID> car=new ArrayList<>();double carX;int carAt=-1,carDir;boolean carParked;
        /** Trampled columns and when they went down. */
        final Map<Long,Long> trampled=new HashMap<>();
        final Map<UUID,Long> nextMove=new HashMap<>(),nextVoice=new HashMap<>();
        final Map<UUID,Integer> still=new HashMap<>(),lastLine=new HashMap<>();
        final Map<UUID,Vec3> lastPos=new HashMap<>();
        final Map<UUID,Vec3> letOut=new HashMap<>();
        final Set<UUID> atEdge=new HashSet<>();
        final List<Vec3> driverRoute=new ArrayList<>();int driverLeg,driverLegTicks;boolean driverGone,driverIntoGrass;
        Field(BlockPos b){base=b;}
    }
    private static final Map<Long,Field> FIELDS=new HashMap<>();
    private static final Map<UUID,String> WITH=new HashMap<>();
    public static void clearAll(){FIELDS.clear();WITH.clear();CAPS.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Field field(BlockPos b){return FIELDS.get(b.asLong());}
    public static int end(BlockPos b){var f=field(b);return f==null?-1:f.end;}
    public static String outcome(BlockPos b){var f=field(b);return f==null?"":f.outcome;}
    public static boolean carParked(BlockPos b){var f=field(b);return f!=null&&f.carParked;}
    @Nullable public static LiteraryActor driver(ServerLevel l,BlockPos b){var f=field(b);return f==null?null:actor(l,f.driver);}
    /** For tests: a reader's next move brought forward to now. */
    public static void moveNow(BlockPos b,ServerPlayer p){var f=field(b);if(f!=null)f.nextMove.put(p.getUUID(),0L);}
    /** For tests: the end begun as if someone had followed the voice long enough. */
    public static void beginNow(ServerLevel l,BlockPos b){var f=field(b);if(f!=null&&f.end==NONE)begin(l,f);}
    /** For tests: the shout timer run out. */
    public static void skipShout(BlockPos b){var f=field(b);if(f!=null&&f.end==CALLING)f.endAt=(int)f.tick-SHOUT_WAIT;}
    /** For tests: every trampled column aged by this many ticks. */
    public static void ageTrack(BlockPos b,int ticks){var f=field(b);if(f!=null)f.trampled.replaceAll((k,v)->v-ticks);}
    public static void resetNow(ServerLevel l,BlockPos b){var f=field(b);if(f!=null){reset(l,f);FIELDS.remove(b.asLong());}}

    // ------------------------------------------------------------------------------------------------ records and speech
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void refresh(CompoundTag held,CompoundTag fresh){for(var key:List.copyOf(held.getAllKeys()))held.remove(key);held.merge(fresh);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void line(ServerPlayer p,String who,String text){p.displayClientMessage(Component.literal(who+": “"+text+"”").withStyle(ChatFormatting.GRAY),true);}
    static void heard(ServerPlayer p,Vec3 at,SoundEvent s,float volume,float pitch){p.connection.send(new ClientboundSoundPacket(Holder.direct(s),SoundSource.AMBIENT,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}
    /** Where a heard point lies for a reader, in words: ahead, behind, to the left or the right of where they face. */
    static String side(ServerPlayer p,Vec3 at){
        var to=at.subtract(p.position());double a=Math.toDegrees(Math.atan2(-to.x,to.z))-p.getYRot();a=Mth.wrapDegrees(a);
        return Math.abs(a)<45?"ahead of you":Math.abs(a)>135?"behind you":a>0?"off to your left":"off to your right";
    }
    /** The point mirrored through a reader: the same distance away on the other side. */
    static Vec3 wrong(ServerPlayer p,Vec3 truth,double reach){var d=truth.subtract(p.position());d=new Vec3(d.x,0,d.z);if(d.lengthSqr()<.01)d=new Vec3(1,0,0);return p.position().add(d.normalize().scale(-reach)).add(0,1.2,0);}
    static Vec3 toward(ServerPlayer p,Vec3 truth,double reach){var d=truth.subtract(p.position());double len=Math.sqrt(d.x*d.x+d.z*d.z);if(len<.01)return truth;return p.position().add(d.x/len*Math.min(len,reach),1.2,d.z/len*Math.min(len,reach));}
    static Vec3 ahead(ServerPlayer p,double reach){var look=p.getLookAngle();var flat=new Vec3(look.x,0,look.z);if(flat.lengthSqr()<.01)flat=new Vec3(0,0,-1);return p.position().add(flat.normalize().scale(reach)).add(0,1.2,0);}

    // ------------------------------------------------------------------------------------------------ who is where
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    /** In the grass, gone in past the fence, and not yet let out. */
    static boolean lost(ServerPlayer p,BlockPos b,CompoundTag own){return own.getBoolean("Committed")&&!own.getBoolean("LetOut")&&TallGrassSite.inGrass(rel(p.position(),b));}
    /** Whether a reader could see a point: in reach, before their eyes, and no standing grass, leaves or solid block in the way. */
    static boolean sees(ServerPlayer p,Vec3 point,double reach,double cone){
        var eye=p.getEyePosition();var to=point.subtract(eye);double d=to.length();if(d>reach)return false;if(d<.8)return true;
        if(p.getLookAngle().dot(to.scale(1/d))<cone)return false;
        var l=p.level();int steps=(int)(d/.2);
        for(int i=1;i<steps;i++){var at=eye.add(to.scale(i/(double)steps));var st=l.getBlockState(BlockPos.containing(at));
            if(st.isAir()||YardBlock.is(st,YardBlock.Kind.TRAMPLED)||YardBlock.is(st,YardBlock.Kind.CAP))continue;
            if(YardBlock.is(st,YardBlock.Kind.GRASS)||st.getBlock() instanceof LeavesBlock||st.canOcclude())return false;}
        return true;
    }

    // ------------------------------------------------------------------------------------------------ arriving
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;var f=FIELDS.computeIfAbsent(b.asLong(),k->new Field(b));
        if(f.end>=LETTING_OUT&&!own.getBoolean("Completed"))own.putBoolean("Partway",true);
        var d=LabyrinthData.get(p.server);
        var age=HouseRead.value(d,p.getUUID(),HouseRead.Trait.AGE,false);own.putString("GrassRadio",age.equals("ANALOG")||age.equals("TRANSITIONAL")?"tape":"phone");
        var home=HouseRead.value(d,p.getUUID(),HouseRead.Trait.HOME,true);if(!home.isEmpty())own.putString("GrassHome",home);
        cue(p,"A county road, late in the afternoon. Across it, the wagon on the shoulder with its hazards ticking, and past the ditch, grass higher than your head.");
    }

    // ------------------------------------------------------------------------------------------------ the road's clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(FIELDS.isEmpty())return;var server=e.getServer();var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var f:List.copyOf(FIELDS.values())){
            f.tick++;var here=readers(l,f.base);
            if(here.isEmpty()){if(++f.empty>=EMPTY_RESET&&l.isLoaded(f.base.offset(TallGrassSite.NOTICE))){reset(l,f);FIELDS.remove(f.base.asLong());}continue;}
            f.empty=0;
            if(f.fresh){if(!l.isLoaded(f.base.offset(TallGrassSite.NOTICE)))continue;f.fresh=false;reset(l,f);}
            if(f.tick%10==0)TallGrassSite.hazards(l,f.base,f.tick%20==0);
            springBack(l,f);
            if(f.end==NONE){voices(l,f,here);hazardSounds(l,f,here);moves(l,f,here);if(f.tick%20==0)maybeBegin(l,f,here);}
            else ending(l,f,here);
            car(l,f);
        }
    }
    /** Trampled stems come back up: flat, rising, nearly up, then standing again. */
    static void springBack(ServerLevel l,Field f){
        if(f.tick%10!=0||f.trampled.isEmpty())return;
        for(var it=f.trampled.entrySet().iterator();it.hasNext();){var e=it.next();long age=f.tick-e.getValue();var at=BlockPos.of(e.getKey());
            var rel=at.subtract(f.base);if(!l.isLoaded(at))continue;
            if(age>=FLAT+RISING+NEARLY){boolean body=false;for(var p:l.players())if(p.getBoundingBox().intersects(new AABB(at).expandTowards(0,2,0)))body=true;if(body)continue;
                TallGrassSite.column(l,f.base,rel.getX(),rel.getZ(),Block.UPDATE_CLIENTS);it.remove();}
            else{int stage=age>=FLAT+RISING?2:age>=FLAT?1:0;var st=l.getBlockState(at);if(YardBlock.is(st,YardBlock.Kind.TRAMPLED)&&st.getValue(YardBlock.STAGE)!=stage)l.setBlock(at,st.setValue(YardBlock.STAGE,stage),Block.UPDATE_CLIENTS);}
        }
    }
    /** A reader standing in the grass flattens it, and the two cells above go down with it. */
    public static void trample(ServerLevel l,BlockPos at,ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null||LiteraryVignettes.current(p)!=PLACE)return;var f=field(b);if(f==null)return;
        var rel=at.subtract(b);if(TallGrassSite.heart(rel.getX(),rel.getZ()))return;
        l.setBlock(at,YardBlock.of(YardBlock.Kind.TRAMPLED,p.getDirection(),0),Block.UPDATE_CLIENTS);
        for(int y=1;y<=2;y++)if(YardBlock.is(l.getBlockState(at.above(y)),YardBlock.Kind.GRASS))l.setBlock(at.above(y),Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        f.trampled.put(at.asLong(),f.tick);
        if(f.tick%3==0)l.playSound(null,at,net.minecraft.sounds.SoundEvents.GRASS_BREAK,SoundSource.BLOCKS,.35F,.8F);
    }
    /** The boy's voice: from a little way into the grass for those on the road; always a little ahead for those gone in. */
    static void voices(ServerLevel l,Field f,List<ServerPlayer> here){
        for(var p:here){var own=own(p);if(own.getBoolean("Completed"))continue;
            long next=f.nextVoice.getOrDefault(p.getUUID(),f.tick+40);if(f.tick<next){f.nextVoice.putIfAbsent(p.getUUID(),next);continue;}
            boolean in=lost(p,f.base,own);
            Vec3 at;String text;
            if(in){var r=p.getRandom();at=p.position().add(r.nextInt(7)-3,1.2,-6-r.nextInt(4));int last=f.lastLine.getOrDefault(p.getUUID(),0);int k=1+r.nextInt(BOY.length-1);if(k==last)k=k%(BOY.length-1)+1;f.lastLine.put(p.getUUID(),k);text=BOY[k];
                f.nextVoice.put(p.getUUID(),f.tick+120+r.nextInt(60));}
            else if(!own.getBoolean("Committed")){at=world(f.base,TallGrassSite.FIRST_VOICE);text=BOY[0];f.nextVoice.put(p.getUUID(),f.tick+220);}
            else continue;
            heard(p,at,TallGrassRegistry.BOY.get(),1F,1F);
            p.displayClientMessage(Component.literal((in?"Close by, "+side(p,at)+": “":"From the grass: “")+text+"”").withStyle(ChatFormatting.GRAY),true);
        }
    }
    /** The hazards: heard true from the road; in the grass, true only by a reader standing still, and from the wrong side otherwise. */
    static void hazardSounds(ServerLevel l,Field f,List<ServerPlayer> here){
        if(f.tick%20!=0)return;var truth=world(f.base,TallGrassSite.wagon());
        for(var p:here){var own=own(p);
            if(!lost(p,f.base,own)){heard(p,truth,TallGrassRegistry.HAZARD.get(),.7F,1F);continue;}
            int still=f.still.getOrDefault(p.getUUID(),0);
            if(still>=60){heard(p,toward(p,truth,12),TallGrassRegistry.HAZARD.get(),.9F,1F);
                if(!own.getBoolean("HeardTrue")){own.putBoolean("HeardTrue",true);save(p,own);cue(p,"Standing still, you can hear the wagon's hazards ticking, "+side(p,toward(p,truth,12))+". The road is that way.");}}
            else if(f.tick%40==0){heard(p,wrong(p,truth,14),TallGrassRegistry.HAZARD.get(),.45F,1F);if(!own.getBoolean("HeardWrong")){own.putBoolean("HeardWrong",true);save(p,own);}}
        }
    }

    // ------------------------------------------------------------------------------------------------ the field moves them
    static void moves(ServerLevel l,Field f,List<ServerPlayer> here){
        var lost=new ArrayList<ServerPlayer>();for(var p:here)if(lost(p,f.base,own(p)))lost.add(p);
        var done=new HashSet<UUID>();
        for(var p:lost){
            if(done.contains(p.getUUID()))continue;
            // The huddle: everyone gone in within a few blocks of each other, moved together.
            var group=new ArrayList<ServerPlayer>();var queue=new ArrayDeque<ServerPlayer>();queue.add(p);done.add(p.getUUID());
            while(!queue.isEmpty()){var q=queue.poll();group.add(q);for(var o:lost)if(!done.contains(o.getUUID())&&o.distanceToSqr(q)<3.5*3.5){done.add(o.getUUID());queue.add(o);}}
            long due=Long.MAX_VALUE;boolean nearRoad=false;
            for(var q:group){long n=f.nextMove.computeIfAbsent(q.getUUID(),k->f.tick+40);due=Math.min(due,n);if(rel(q.position(),f.base).z>TallGrassSite.FENCE_Z-3)nearRoad=true;}
            if(nearRoad)due=Math.min(due,f.nextMove.getOrDefault(p.getUUID(),f.tick)-MOVE_MIN+20);
            if(f.tick<due)continue;
            if(watched(l,f,here,group))continue;
            move(l,f,group);
        }
    }
    static boolean watched(ServerLevel l,Field f,List<ServerPlayer> here,List<ServerPlayer> group){
        for(var o:here){if(group.contains(o)||o.isSpectator())continue;for(var q:group)if(sees(o,q.getEyePosition(),48,.35)||sees(o,q.position().add(0,.5,0),48,.35))return true;}
        return false;
    }
    /** Sets the huddle down somewhere else in the grass: farther from the road, the same distance from the voice, out of sight. */
    static void move(ServerLevel l,Field f,List<ServerPlayer> group){
        var lead=group.get(0);var r=rel(lead.position(),f.base);var rnd=lead.getRandom();var leadOwn=own(lead);
        double dx,dz;
        if(leadOwn.getInt("Moved")==2&&!leadOwn.getBoolean("SawHeart")){
            // The third time, the House sets them where they can see the heart of the field.
            var spot=new Vec3(TallGrassSite.HEART.getX()+.5,0,TallGrassSite.HEART.getZ()+TallGrassSite.HEART_R+2.5);dx=spot.x-r.x;dz=spot.z-r.z;
        }else{
            dz=-(5+rnd.nextInt(5));dx=rnd.nextInt(13)-6;
            if(r.z+dz<TallGrassSite.DEEP){dz=0;dx=(rnd.nextBoolean()?1:-1)*(10+rnd.nextInt(10));}
            if(r.x+dx<TallGrassSite.MOVE_W||r.x+dx>TallGrassSite.MOVE_E)dx=-r.x*.5;
            if(r.z+dz>TallGrassSite.FENCE_Z-2)dz=TallGrassSite.FENCE_Z-2-r.z-rnd.nextInt(4);
        }
        boolean together=group.size()>1;
        for(var q:group){
            var qr=rel(q.position(),f.base);double nx=qr.x+dx,nz=qr.z+dz;
            int cx=Mth.floor(nx),cz=Mth.floor(nz);if(TallGrassSite.heart(cx,cz)&&!(leadOwn.getInt("Moved")==2)){nz-=TallGrassSite.HEART_R*2+1;}
            var w=world(f.base,new Vec3(nx,0,nz));
            q.teleportTo(l,w.x,f.base.getY(),w.z,q.getYRot(),q.getXRot());q.setDeltaMovement(Vec3.ZERO);q.fallDistance=0;
            f.lastPos.put(q.getUUID(),q.position());f.nextMove.put(q.getUUID(),f.tick+MOVE_MIN+rnd.nextInt(MOVE_MAX-MOVE_MIN));
            var own=own(q);own.putInt("Moved",own.getInt("Moved")+1);
            if(together){var names=new ArrayList<String>();for(var o:group)if(o!=q)names.add(o.getGameProfile().getName());own.putString("MovedWith",String.join(" and ",names));}
            save(q,own);
        }
    }
    static void maybeBegin(ServerLevel l,Field f,List<ServerPlayer> here){
        for(var p:here){var own=own(p);if(!lost(p,f.base,own))continue;int ticks=own.getInt("FieldTicks");
            if(ticks>=FOLLOW&&own.getInt("Moved")>=3||ticks>=KEEP_AT_MOST){begin(l,f);return;}}
    }

    // ------------------------------------------------------------------------------------------------ the end
    static void begin(ServerLevel l,Field f){
        f.end=LETTING_OUT;f.endAt=(int)f.tick;int i=0;
        for(var p:readers(l,f.base)){var own=own(p);if(own.getBoolean("Completed")||!own.getBoolean("Committed"))continue;
            f.letOut.put(p.getUUID(),new Vec3(TallGrassSite.STOP_X-4+(i++)*2+.5,0,TallGrassSite.LET_OUT_Z+.5));}
    }
    static void ending(ServerLevel l,Field f,List<ServerPlayer> here){
        int t=(int)f.tick-f.endAt;
        switch(f.end){
            case LETTING_OUT->{
                for(var e:List.copyOf(f.letOut.entrySet())){var p=l.getServer().getPlayerList().getPlayer(e.getKey());if(p==null||!here.contains(p)){f.letOut.remove(e.getKey());continue;}
                    var to=world(f.base,e.getValue());boolean seen=false;
                    if(t<200)for(var o:here)if(o!=p&&!f.letOut.containsKey(o.getUUID())&&(sees(o,p.getEyePosition(),48,.35)||sees(o,to.add(0,1.5,0),48,.35)))seen=true;
                    if(seen)continue;
                    p.teleportTo(l,to.x,to.y,to.z,0F,0F);p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;f.letOut.remove(e.getKey());f.atEdge.add(p.getUUID());
                    var own=own(p);own.putBoolean("LetOut",true);save(p,own);cue(p,"The grass thins. Through the stems: the road, close, and the gravel of the shoulder.");}
                if(f.letOut.isEmpty()&&t>=20){f.end=CAR_COMING;f.endAt=(int)f.tick;startCar(l,f,TallGrassSite.ROAD_E+10,-1);}
            }
            case CAR_COMING->{if(f.carParked){f.end=DRIVER_OUT;f.endAt=(int)f.tick;spawnDriver(l,f);}}
            case DRIVER_OUT->{
                var a=actor(l,f.driver);if(a!=null)walkDriver(l,f,a);
                if(f.driverLeg>=f.driverRoute.size()||t>120){f.end=CALLING;f.endAt=(int)f.tick;call(l,f,here);}
            }
            case CALLING->{
                var a=actor(l,f.driver);if(a!=null)for(var p:here)if(f.atEdge.contains(p.getUUID()))SceneFootpaths.face(a,p.getEyePosition(),40);
                if(t>=SHOUT_WAIT)shout(l,f,null,QUIET);
            }
            case AFTER->{var a=actor(l,f.driver);if(a!=null&&!f.driverGone){walkDriver(l,f,a);if(f.driverLeg>=f.driverRoute.size()){driverGoes(l,f,a);}}}
            default->{}
        }
    }
    /** The driver, out of the far side of the cab, round the front, to the edge of the shoulder, looking at the grass. */
    static void spawnDriver(ServerLevel l,Field f){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return;a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,DRIVER);
        int x=TallGrassSite.STOP_X;var start=world(f.base,new Vec3(x+1.5,0,TallGrassSite.ROAD_N+.5));a.moveTo(start.x,start.y,start.z,0,0);
        if(!l.addFreshEntity(a))return;f.driver=a.getUUID();
        f.driverRoute.clear();f.driverLeg=0;f.driverLegTicks=0;f.driverRoute.add(new Vec3(x-.5,0,TallGrassSite.ROAD_N+.5));f.driverRoute.add(new Vec3(x-.5,0,TallGrassSite.SHOULDER_N+.5));
        l.playSound(null,f.base.offset(x+1,1,TallGrassSite.ROAD_N),net.minecraft.sounds.SoundEvents.IRON_DOOR_CLOSE,SoundSource.NEUTRAL,.6F,1.4F);
    }
    static void walkDriver(ServerLevel l,Field f,LiteraryActor a){
        if(f.driverLeg>=f.driverRoute.size())return;var target=world(f.base,f.driverRoute.get(f.driverLeg));
        // A leg the footpaths cannot finish (a body caught on the ditch's lip) is finished by a step, so the driver never stands stuck.
        if(SceneFootpaths.walk(a,target,.12)||a.position().distanceToSqr(target)<.09||++f.driverLegTicks>160){if(f.driverLegTicks>160)a.moveTo(target.x,target.y,target.z,a.getYRot(),0);f.driverLeg++;f.driverLegTicks=0;}
    }
    /** "Hello? Did somebody shout?" Every reader the field let out at the edge may shout something; the first shout decides. */
    static void call(ServerLevel l,Field f,List<ServerPlayer> here){
        var a=actor(l,f.driver);String text="Hello? Did somebody shout?";
        if(a!=null){a.say(text);a.speaks(text);}
        for(var p:here){if(a!=null&&p.distanceToSqr(a)<30*30)line(p,"The driver",text);
            if(!f.atEdge.contains(p.getUUID())||own(p).getBoolean("Completed"))continue;
            begin(p,"The driver");Talk.line(p,"","The driver is looking at the grass, right where you are, and not seeing you. You could shout.");
            Talk.ask(p,Talk.reply("Stay by your car! Don't come in!",q->shout(q,WARNED)),Talk.reply("Help! In here!",q->shout(q,CALLED)),Talk.act("Say nothing.",q->{var o=own(q);o.putBoolean("SaidNothing",true);save(q,o);Talk.close(q);}));}
    }
    static void shout(ServerPlayer p,String outcome){
        var b=LiteraryVignettes.base(p,PLACE);var f=b==null?null:field(b);Talk.close(p);if(f==null||f.end!=CALLING)return;
        shout(p.serverLevel(),f,p,outcome);
    }
    /** The first shout (or none at all): the driver does what it says, and everyone on the road has their ending. */
    static void shout(ServerLevel l,Field f,@Nullable ServerPlayer by,String outcome){
        if(f.end!=CALLING)return;f.end=AFTER;f.endAt=(int)f.tick;f.outcome=outcome;f.shoutBy=by==null?"":by.getGameProfile().getName();
        var a=actor(l,f.driver);var here=readers(l,f.base);
        for(var p:here)if(p!=by&&Talk.waiting(p,TALK))Talk.close(p);
        if(by!=null)heardAll(l,f,here,by.position().add(0,1.5,0),TallGrassRegistry.CALL.get());
        int x=TallGrassSite.STOP_X;f.driverRoute.clear();f.driverLeg=0;f.driverLegTicks=0;
        switch(outcome){
            case WARNED->{if(a!=null){a.say("...All right. All right.");a.speaks("...All right. All right.");}
                f.driverRoute.add(new Vec3(x-.5,0,TallGrassSite.ROAD_N+.5));f.driverRoute.add(new Vec3(x+1.5,0,TallGrassSite.ROAD_N+.5));}
            default->{
                if(outcome.equals(QUIET)){var boy=world(f.base,new Vec3(x-2.5,1.2,-24.5));heardAll(l,f,here,boy,TallGrassRegistry.BOY.get());
                    for(var p:here)line(p,"From the grass",BOY[0]);}
                String text=outcome.equals(QUIET)?"Hang on, kid! I'm coming!":"Hang on! I'm coming in!";if(a!=null){a.say(text);a.speaks(text);}
                f.driverIntoGrass=true;f.driverRoute.add(new Vec3(x-.5,-1,TallGrassSite.DITCH_N+.5));f.driverRoute.add(new Vec3(x-1.5,0,TallGrassSite.LET_OUT_Z-3.5));f.driverRoute.add(new Vec3(x-2.5,0,TallGrassSite.LET_OUT_Z-8.5));}
        }
        for(var p:here){var own=own(p);if(own.getBoolean("Completed"))continue;
            boolean atEdge=f.atEdge.contains(p.getUUID());
            own.putString("Outcome",outcome);own.putString("ShoutBy",f.shoutBy);own.putString("DriverWent",outcome.equals(WARNED)?"drove_on":"into_grass");
            if(p==by)own.putString("Shout",outcome);
            VignetteRole role=own.getBoolean("Partway")?VignetteRole.PARTWAY:p==by||outcome.equals(QUIET)&&atEdge?VignetteRole.ACTOR:VignetteRole.WITNESS;
            resolve(p,own,outcome,role,f.shoutBy);
            cue(p,switch(outcome){case WARNED->(p==by?"You shout.":f.shoutBy+" shouts.")+" The driver stands a moment longer, then gets back in.";
                case CALLED->(p==by?"You shout.":f.shoutBy+" shouts.")+" The driver steps down off the shoulder, toward the grass.";
                default->"Nobody says anything. The driver listens, and hears the boy instead.";});
        }
    }
    static void heardAll(ServerLevel l,Field f,List<ServerPlayer> here,Vec3 at,SoundEvent s){for(var p:here)heard(p,at,s,1F,1F);}
    /** The driver back in the cab and gone, or into the grass and gone. */
    static void driverGoes(ServerLevel l,Field f,LiteraryActor a){
        if(f.driverIntoGrass){boolean seen=false;for(var p:readers(l,f.base))if(sees(p,a.getEyePosition(),48,.35))seen=true;if(seen&&f.tick-f.endAt<200)return;}
        a.discard();f.driverGone=true;
        if(!f.driverIntoGrass){l.playSound(null,a.blockPosition(),net.minecraft.sounds.SoundEvents.IRON_DOOR_CLOSE,SoundSource.NEUTRAL,.6F,1.2F);startCar(l,f,TallGrassSite.STOP_X,1);}
    }
    /** A reader's ending: their record finished, their Witness resolution, their account in hand. */
    static void resolve(ServerPlayer p,CompoundTag own,String outcome,VignetteRole role,String actor){
        if(own.getBoolean("Completed"))return;var d=LabyrinthData.get(p.server);
        own.putBoolean("Ready",true);own.putString("GrassRole",role.id());own.putBoolean("Completed",true);own.putBoolean("Taken_Ending",true);own.putInt("Beat",own.getInt("Visit"));
        save(p,own);
        if(role==VignetteRole.ACTOR)WitnessAccount.resolve(p,WitnessAccount.Story.TALL_GRASS,outcome);else WitnessAccount.resolveAs(p,WitnessAccount.Story.TALL_GRASS,outcome,role,actor);
        d.setCompleted(PLACE.id(),true);
        LiteraryVignettes.reward(p,PLACE,own,"Ending",TallGrassTexts.account(p.getGameProfile().getName(),own));
        var fresh=own(p);refresh(own,fresh);
    }

    // ------------------------------------------------------------------------------------------------ the next car
    /** The pickup on the road as moving pictures of itself: coming from the east to pull onto the shoulder ({@code dir} -1), or pulling away west (1). */
    static void startCar(ServerLevel l,Field f,int fromX,int dir){
        if(dir>0){for(var e:TallGrassSite.pickup(TallGrassSite.STOP_X)){var at=f.base.offset(e.getKey());if(YardBlock.is(l.getBlockState(at),e.getValue().getValue(YardBlock.KIND)))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}f.carParked=false;}
        for(var e:TallGrassSite.pickup(0)){
            var d=EntityType.BLOCK_DISPLAY.create(l);if(d==null)continue;var tag=new CompoundTag();d.saveWithoutId(tag);
            tag.put("block_state",NbtUtils.writeBlockState(e.getValue()));tag.putInt("teleport_duration",2);tag.putFloat("view_range",2F);d.load(tag);d.addTag("tall_grass_car");
            var at=carCell(f,e.getKey(),fromX,dir>0?0:LANE);d.moveTo(at.x,at.y,at.z,0,0);
            if(l.addFreshEntity(d))f.car.add(d.getUUID());
        }
        f.carX=fromX;f.carAt=(int)f.tick;f.carDir=dir;
        heardAll(l,f,readers(l,f.base),world(f.base,new Vec3(fromX,1,-7)),dir<0?TallGrassRegistry.CAR.get():TallGrassRegistry.AWAY.get());
    }
    /** How far south of the shoulder the car runs while it is in the north lane. */
    static final double LANE=2;
    static Vec3 carCell(Field f,BlockPos cell,double noseX,double dz){return new Vec3(f.base.getX()+noseX+cell.getX(),f.base.getY()+cell.getY(),f.base.getZ()+cell.getZ()+dz);}
    static void car(ServerLevel l,Field f){
        if(f.carAt<0||f.car.isEmpty())return;int t=(int)f.tick-f.carAt;
        double x,dz;
        if(f.carDir<0){int run=140;double k=Math.min(1,t/(double)run);double ease=1-(1-k)*(1-k);x=f.carX+(TallGrassSite.STOP_X-f.carX)*ease;dz=k<.7?LANE:LANE*(1-Math.min(1,(k-.7)/.3));
            if(t>=run){park(l,f);return;}}
        else{int run=140;double k=Math.min(1,t/(double)run);x=f.carX+(TallGrassSite.ROAD_W-14-f.carX)*k*k;dz=k<.25?LANE*(k/.25):LANE;if(t>=run){clearCar(l,f);return;}}
        var cells=TallGrassSite.pickup(0);
        for(int i=0;i<f.car.size()&&i<cells.size();i++){var e=l.getEntity(f.car.get(i));if(e instanceof Display.BlockDisplay d){var at=carCell(f,cells.get(i).getKey(),x,dz);d.teleportTo(at.x,at.y,at.z);}}
    }
    static void park(ServerLevel l,Field f){
        clearCar(l,f);
        for(var e:TallGrassSite.pickup(TallGrassSite.STOP_X)){var at=f.base.offset(e.getKey());var st=l.getBlockState(at);if(st.isAir()||st.canBeReplaced())l.setBlock(at,e.getValue(),Block.UPDATE_CLIENTS);}
        f.carParked=true;
    }
    static void clearCar(ServerLevel l,Field f){for(var id:f.car){var e=l.getEntity(id);if(e!=null)e.discard();}f.car.clear();f.carAt=-1;}

    // ------------------------------------------------------------------------------------------------ each reader
    /** Every five ticks, for each reader on the road or in the grass. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag held){
        save(p,held);var own=own(p);var f=field(b);
        if(f==null){refresh(held,own);return;}
        var r=rel(p.position(),b);
        // Standing still, for the hazards.
        var last=f.lastPos.get(p.getUUID());if(last!=null&&last.distanceToSqr(p.position())<.0025)f.still.merge(p.getUUID(),5,Integer::sum);else f.still.put(p.getUUID(),0);f.lastPos.put(p.getUUID(),p.position());
        if(!own.getBoolean("Completed")){
            if(TallGrassSite.nearWagon(r,4)&&!own.getBoolean("HeardRadio")){own.putBoolean("HeardRadio",true);
                cue(p,own.getString("GrassRadio").equals("tape")?"Through the open door the wagon's tape is playing, near the end of its side.":"Through the open door a phone on the dash is playing a station, the signal coming and going.");
                if(own.getString("GrassHome").equals("RUNAWAY")){own.putBoolean("Packed",true);}}
            if(TallGrassSite.inFirstRows(r)&&!own.getBoolean("SawRoad")){own.putBoolean("SawRoad",true);cue(p,"Behind you, through the stems: the road, and the wagon's hazards ticking.");}
            if(f.end==NONE&&!own.getBoolean("Committed")&&TallGrassSite.inGrass(r)&&TallGrassSite.pastFence(r)){own.putBoolean("Committed",true);f.nextMove.put(p.getUUID(),f.tick+40);
                cue(p,"Past the old fence posts the grass closes in behind you.");}
            if(lost(p,b,own)){own.putInt("FieldTicks",own.getInt("FieldTicks")+5);
                if(!own.getBoolean("SawHeart")&&Math.abs(r.x-TallGrassSite.HEART.getX()-.5)<TallGrassSite.HEART_R+4&&Math.abs(r.z-TallGrassSite.HEART.getZ()-.5)<TallGrassSite.HEART_R+4){own.putBoolean("SawHeart",true);
                    cue(p,"The grass opens onto a ring trampled flat, as though something had turned round and round here. In the middle, a small cap.");}
                for(var o:readers(p.serverLevel(),b)){if(o==p||!lost(o,b,own(o)))continue;if(o.distanceToSqr(p)<2.5*2.5&&!own.getString("FoundOther").contains(o.getGameProfile().getName())){
                    own.putString("FoundOther",own.getString("FoundOther").isEmpty()?o.getGameProfile().getName():own.getString("FoundOther")+" and "+o.getGameProfile().getName());cue(p,"There's "+o.getGameProfile().getName()+", a few stems away.");}}
            }
        }
        save(p,own);refresh(held,own);
    }

    // ------------------------------------------------------------------------------------------------ talk and use
    static void begin(ServerPlayer p,String header){WITH.put(p.getUUID(),header);Talk.begin(p,TALK,header);}
    /** Clicks on the yard's fittings here: the wagon, the grass, the cap. */
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(LiteraryVignettes.current(p)!=PLACE)return false;var b=LiteraryVignettes.base(p,PLACE);if(b==null)return false;var f=field(b);var rel=pos.subtract(b);var own=own(p);
        if(TallGrassSite.isWagon(rel)){wagon(p,b,f,own);return true;}
        if(YardBlock.is(s,YardBlock.Kind.CAP)){
            if(own.getBoolean("TookCap")){cue(p,"Where the cap was, the grass is pressed flat.");return true;}
            own.putBoolean("TookCap",true);save(p,own);var cap=new ItemStack(TallGrassRegistry.CAP.get());if(!p.getInventory().add(cap))p.drop(cap,false);
            cue(p,"A child's cap, small, wet through as if it had rained in here and nowhere else.");return true;}
        if(YardBlock.is(s,YardBlock.Kind.GRASS)&&f!=null&&lost(p,b,own)&&f.end==NONE){
            begin(p,"The grass");Talk.line(p,"","Grass on every side. Somewhere in it, the others.");
            Talk.ask(p,Talk.reply("Hello?",q->fromGrass(q,"Hello?")),Talk.reply("Where are you?",q->fromGrass(q,"Where are you?")),Talk.reply("I'm over here!",q->fromGrass(q,"I'm over here!")),Talk.act("Leave it there.",Talk::close));
            return true;}
        if(YardBlock.is(s,YardBlock.Kind.PICKUP_NOSE)||YardBlock.is(s,YardBlock.Kind.PICKUP_BODY)||YardBlock.is(s,YardBlock.Kind.PICKUP_CAB)||YardBlock.is(s,YardBlock.Kind.PICKUP_BED)){
            cue(p,f!=null&&f.driverIntoGrass?"The pickup's engine is running and its door is open. Nobody is coming back to it.":"A pickup, its engine running.");return true;}
        return false;
    }
    /** The wagon: get back in (and back to the House), or call into the grass for whoever is in it. */
    static void wagon(ServerPlayer p,BlockPos b,@Nullable Field f,CompoundTag own){
        boolean someoneIn=false;if(f!=null&&f.end==NONE)for(var o:readers(p.serverLevel(),b))if(o!=p&&lost(o,b,own(o)))someoneIn=true;
        begin(p,"The wagon");
        Talk.line(p,"",own.getBoolean("Packed")?"The wagon, packed to the roof in the back: bags, a lamp, a box of plates, the way you pack when you are not coming back. You had somewhere to be.":"The wagon. The driver's door is open, the keys are in it, the hazards are ticking.");
        var replies=new ArrayList<Talk.Reply>();
        if(someoneIn){replies.add(Talk.reply("Over here!",q->fromRoad(q,"Over here!")));replies.add(Talk.reply("Follow my voice!",q->fromRoad(q,"Follow my voice!")));replies.add(Talk.reply("Can you see the car?",q->fromRoad(q,"Can you see the car?")));}
        replies.add(Talk.act("Get in and go back.",q->{Talk.close(q);LabyrinthDoors.sendBack(q,20,30,30,z->cue(z,"You are back in the House. The hazards are still ticking somewhere, and then they aren't."));}));
        replies.add(Talk.act("Leave it there.",Talk::close));
        Talk.ask(p,replies);
    }
    /** A call from the road into the grass: the first heard ahead, later ones true only from a caller standing still by the wagon. */
    static void fromRoad(ServerPlayer caller,String text){
        var b=LiteraryVignettes.base(caller,PLACE);var f=b==null?null:field(b);Talk.close(caller);if(f==null)return;
        var co=own(caller);co.putInt("Called",co.getInt("Called")+1);co.putString("CallLine",text);save(caller,co);
        boolean steady=f.still.getOrDefault(caller.getUUID(),0)>=40&&TallGrassSite.nearWagon(rel(caller.position(),b),4);
        var truth=caller.position().add(0,1.5,0);
        for(var q:readers(caller.serverLevel(),b)){if(q==caller)continue;var own=own(q);if(!lost(q,b,own))continue;
            Vec3 at;
            if(!own.getBoolean("FirstAhead")){at=ahead(q,10);own.putBoolean("FirstAhead",true);}
            else if(steady){at=toward(q,truth,12);own.putInt("TrueCalls",own.getInt("TrueCalls")+1);}
            else at=wrong(q,truth,12);
            own.putString("HeardCaller",caller.getGameProfile().getName());save(q,own);
            heard(q,at,TallGrassRegistry.CALL.get(),1F,1F);
            q.displayClientMessage(Component.literal(caller.getGameProfile().getName()+", "+side(q,at)+": “"+text+"”").withStyle(ChatFormatting.GRAY),true);
        }
    }
    /** A call from inside the grass: the others in it hear it from the wrong side; those on the road hear where it really is. */
    static void fromGrass(ServerPlayer caller,String text){
        var b=LiteraryVignettes.base(caller,PLACE);var f=b==null?null:field(b);Talk.close(caller);if(f==null)return;
        var truth=caller.position().add(0,1.5,0);
        for(var q:readers(caller.serverLevel(),b)){if(q==caller)continue;var own=own(q);
            var at=lost(q,b,own)?wrong(q,truth,10):truth;
            if(lost(q,b,own)&&own.getString("HeardOther").isEmpty()){own.putString("HeardOther",caller.getGameProfile().getName());save(q,own);}
            heard(q,at,TallGrassRegistry.CALL.get(),1F,1F);
            q.displayClientMessage(Component.literal(caller.getGameProfile().getName()+", "+(lost(q,b,own)?side(q,at):"from the grass")+": “"+text+"”").withStyle(ChatFormatting.GRAY),true);
        }
    }
    /** Right-clicking the driver. */
    public static void talk(ServerPlayer p,LiteraryActor actor){if(actor.phase()==DRIVER)cue(p,"The driver doesn't look away from the grass.");}
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}

    // ------------------------------------------------------------------------------------------------ the cap, carried
    private static final Set<UUID> CAPS=new HashSet<>();
    /** A reader carrying the boy's cap. */
    public static boolean carriesCap(UUID p){return CAPS.contains(p);}
    @SubscribeEvent public static void caps(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%20!=0)return;
        for(var p:server.getPlayerList().getPlayers()){boolean has=false;for(var st:p.getInventory().items)if(st.is(TallGrassRegistry.CAP.get()))has=true;if(has)CAPS.add(p.getUUID());else CAPS.remove(p.getUUID());}
    }
    /** The field places the cap knows its way through: their way on, called once from the right direction. */
    static final Set<LabyrinthPlace> FIELDS_WITH_A_WAY=EnumSet.of(LabyrinthPlace.YARD_STORM_FIELD,LabyrinthPlace.YARD_WINDBREAK,LabyrinthPlace.MARSH_SUPPER,LabyrinthPlace.STATE_ROUTE,LabyrinthPlace.SPHINX_FIELD);
    /** On arriving in a field place with the cap, once each: a boy's voice from the way on, true. */
    public static void arrived(ServerPlayer p,LabyrinthPlace place){
        if(place==null||!FIELDS_WITH_A_WAY.contains(place)||!carriesCap(p.getUUID()))return;
        var origin=io.github.knaitoe.theoldesthouse.house.HouseSavedData.get(p.server).houseOrigin();if(origin==null)return;
        var base=LiteraryRooms.isLiterary(place)?LiteraryVignettes.base(p,place):LabyrinthPlaces.base(origin,place);if(base==null)return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);String key="CapTrue_"+place.id();if(own.getBoolean(key))return;
        BlockPos way=OnwardDoors.lower(base,place);
        if(way==null)for(var spec:place.doors())if(spec.name().equals("far"))way=base.offset(spec.rel());
        if(way==null)return;
        own.putBoolean(key,true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        var at=Vec3.atCenterOf(way).add(0,1,0);heard(p,at,TallGrassRegistry.BOY.get(),1.2F,1.05F);
        p.displayClientMessage(Component.literal("The cap in your pocket. Somewhere "+side(p,at)+", a boy calls once: “This way.”").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
    }

    // ------------------------------------------------------------------------------------------------ dealing and the reset
    /** Dealt once each, and only to a reader who has crossed the House's yard. */
    public static boolean dealable(LabyrinthData d,UUID p){
        if(LiteraryVignettes.personal(d,p,PLACE).getBoolean("Completed"))return false;
        var seen=d.visited(p);for(var y:YardCrossings.ORDINARY)if(seen.contains(y.id()))return true;return false;
    }
    /** Nobody on the road: the grass standing again, the next car gone, the wagon ticking. */
    static void reset(ServerLevel l,Field f){
        var b=f.base;
        for(var key:f.trampled.keySet()){var rel=BlockPos.of(key).subtract(b);TallGrassSite.column(l,b,rel.getX(),rel.getZ(),Block.UPDATE_CLIENTS);}
        f.trampled.clear();clearCar(l,f);
        for(var e:TallGrassSite.pickup(TallGrassSite.STOP_X)){var at=b.offset(e.getKey());if(l.getBlockState(at).is(PuzzleRegistry.YARD.get()))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}
        var area=new AABB(b.offset(TallGrassSite.WEST,-3,TallGrassSite.NORTH)).minmax(new AABB(b.offset(TallGrassSite.EAST,8,2)));
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,area,a->a.role()==LiteraryActor.STRANGER&&a.phase()==DRIVER))a.discard();
        for(var d:l.getEntitiesOfClass(Display.BlockDisplay.class,area,d->d.getTags().contains("tall_grass_car")))d.discard();
        f.end=NONE;f.outcome="";f.shoutBy="";f.driver=null;f.carParked=false;f.atEdge.clear();f.letOut.clear();f.driverGone=false;f.driverIntoGrass=false;
    }
}
