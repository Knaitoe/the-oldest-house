package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Winter Lake (0.4.104). The winter after the shallows, Indian Lake froze, and the boys went back out to it: Bud, Hank,
 * Clay and Lonnie, and the reader with them, as at the shallows. Hank has carried her hair ribbon since it came off in his
 * hand; the dare is to walk it back out to the hole where she went down, which never froze.
 *
 * She is under the ice the whole night, one body, hanging from its underside, and she goes to sound. Running is loud,
 * walking is heard, crouching and standing still are silent; clear black ice rings, snow on the ice muffles, and off the
 * ice she hears nothing. A snowball thudding onto the ice draws her to where it lands. Under a reader who keeps making
 * noise she knocks, knocks again, cracks the ice and breaks through: the reader is pulled under, and must swim back up
 * through the hole they fell through. At the hole a hand comes up and takes the ribbon; the boys run, as they ran at the
 * shallows, and the reader walks back to the bank alone.
 *
 * One shared clock counts while anyone is here; each reader's dare, noise and fall are their own. The scene is built whole
 * here; the polish, composition and review passes leave it alone. New builds only.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class WinterLake {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.WINTER_LAKE;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** Occupied ticks until Hank stops the snowball fight; how long her hand stays up; the boys' run; the empty reset. */
    public static final int QUIET_AT=900,HAND_TICKS=60,RUN_TICKS=100,EMPTY_RESET=100,REST_AFTER_BREAK=160;
    /** Suspicion she builds under one noisy reader: a knock, two knocks, a crack, the crack spreading, and through. */
    public static final float KNOCK=20,KNOCK_TWICE=40,CRACK=60,SPLIT=80,THROUGH=100;
    /** The frozen lake, relative to the scene: its ice is at y -1, open water beneath it. */
    public static final int X0=-27,X1=27,NEAR=-13,FAR=-72;
    /** The hole that never froze, where she went down; she hangs from the ice's underside this far below the scene's floor. */
    public static final BlockPos HOLE=new BlockPos(0,-1,-46),WITCH_START=new BlockPos(6,0,-54),BONFIRE=new BlockPos(-7,0,-7);
    public static final double UNDER=-2.04;
    public static final List<BlockPos> SNOWBALLS=List.of(new BlockPos(-13,0,-11),new BlockPos(2,0,-9));
    private WinterLake(){}

    // ------------------------------------------------------------------------------------------------ the boys
    /** One of the boys from the shallows, in winter: skin (a stranger phase), place on the bank, facing and what he says. */
    public record Boy(String key,String name,int phase,double x,double z,float yaw,String[] lines){
        public Vec3 home(BlockPos b){return new Vec3(b.getX()+x,b.getY(),b.getZ()+z);}
    }
    public static final Boy BUD=new Boy("Bud","Bud",34,-8.5,-9.5,180,new String[]{"My dad says sound carries a mile on the ice.","She can't come up through ice. Can she?","Hank let go first. Don't tell him I said that."});
    public static final Boy HANK=new Boy("Hank","Hank",35,-4.5,-8.5,180,new String[]{});
    public static final Boy CLAY=new Boy("Clay","Clay",36,-10.5,-6.5,160,new String[]{"I'm not going out there. I'm not.","It's a foot thick, Hank said. A foot.","If you see her, don't yell. Just come back."});
    public static final Boy LONNIE=new Boy("Lonnie","Lonnie",37,-5.5,-5.5,200,new String[]{"I still feel her ankles. In my hands. At night.","Don't walk on the black parts. You can see down.","Snow's quieter. Walk on the snow."});
    public static final List<Boy> BOYS=List.of(BUD,HANK,CLAY,LONNIE);
    /** The four boys are the same age they were at the shallows, the summer before. */
    public static boolean boy(int phase){return phase>=34&&phase<=37;}
    private static final String[] FIGHT={"Got you!","Clay, quit it!","That one had ice in it!","Bud's out! Bud's out!","Lonnie throws like a girl!"};
    private static final String[] HANK_TALK={"You came. Bud said you wouldn't.",
        "It came off in my hand. Her ribbon. When she went in. I've had it in my pocket since, and I can't sleep.",
        "Somebody has to take it back out to her. Out to the hole, where she went down. Here. Walk soft."};
    /** Where each of them runs when she takes it: west along the bank, into the trees. */
    private static final double[][] RUN={{-33.5,-3.5},{-33.5,-6.5},{-33.5,-9.5},{-33.5,-1.5}};

    // ------------------------------------------------------------------------------------------------ helpers
    private static @Nullable LiteraryActor cast(ServerLevel l,CompoundTag w,Boy who){return w.hasUUID("Cast"+who.key())&&l.getEntity(w.getUUID("Cast"+who.key())) instanceof LiteraryActor a?a:null;}
    public static @Nullable LiteraryActor cast(ServerLevel l,Boy who){return cast(l,LiteraryVignettes.shared(LabyrinthData.get(l.getServer()),PLACE),who);}
    public static @Nullable LakeWitchEntity witch(ServerLevel l){var w=LiteraryVignettes.shared(LabyrinthData.get(l.getServer()),PLACE);return w.hasUUID("Witch")&&l.getEntity(w.getUUID("Witch")) instanceof LakeWitchEntity e&&e.isAlive()?e:null;}
    public static @Nullable Boy boy(LiteraryActor a){if(a.role()!=LiteraryActor.STRANGER)return null;for(var b:BOYS)if(b.phase()==a.phase())return b;return null;}
    private static void turn(LivingEntity a,float yaw){a.setYRot(yaw);a.setYHeadRot(yaw);a.yBodyRot=yaw;a.yRotO=yaw;}
    private static void say(LiteraryActor a,String line){a.say(line);a.getPersistentData().putLong("Said0504",a.level().getGameTime());}
    private static void speak(LiteraryActor a,Boy who,String line,List<ServerPlayer> readers,double reach){say(a,line);for(var p:readers)if(p.distanceToSqr(a)<reach*reach)p.displayClientMessage(Component.literal(who.name()+": “"+line+"”"),false);}
    private static void hint(ServerPlayer p,CompoundTag own,String key,String text){if(own.getBoolean(key))return;own.putBoolean(key,true);p.displayClientMessage(Component.literal(text),true);}
    public static boolean lake(int x,int z){return x>=X0&&x<=X1&&z<=NEAR&&z>=FAR;}
    public static boolean hole(int x,int z){int dx=x-HOLE.getX(),dz=z-HOLE.getZ();return dx*dx+dz*dz<=2;}
    private static boolean shanty(int x,int z){return x>=11&&x<=15&&z>=-37&&z<=-33;}
    /** The ice as the wind left it: clear black ice round the hole and in long patches, snow drifted over the rest and along the shore. */
    public static WinterPropBlock.Kind surface(int x,int z){
        int dx=x-HOLE.getX(),dz=z-HOLE.getZ();if(dx*dx+dz*dz<49)return WinterPropBlock.Kind.BLACK_ICE;
        if(x<=X0+2||x>=X1-2||z>=NEAR-2||z<=FAR+2)return WinterPropBlock.Kind.SNOW_ICE;
        double v=Math.sin(x*.21+z*.13)+.8*Math.sin(z*.27-x*.08)+.35*Math.sin(x*.61+z*.47);
        return v>.35?WinterPropBlock.Kind.SNOW_ICE:WinterPropBlock.Kind.BLACK_ICE;
    }
    /** Whether the scene at this base has been built: some of its own ice beside the hole. */
    public static boolean built(ServerLevel l,BlockPos b){var at=b.offset(HOLE).offset(2,0,0);return l.isLoaded(at)&&WinterPropBlock.ice(l.getBlockState(at));}
    /** The lake ice a reader stands on, or null off it: the bank, the shanty's floor and the water are all silent. */
    private static @Nullable WinterPropBlock.Kind underfoot(ServerLevel l,BlockPos b,ServerPlayer p){
        double y=p.getY()-b.getY();if(y<-.2||y>.7)return null;
        var cell=BlockPos.containing(p.getX(),b.getY()-1,p.getZ());var rel=cell.subtract(b);if(!lake(rel.getX(),rel.getZ()))return null;
        var k=WinterPropBlock.kind(l.getBlockState(cell));return k!=null&&k.ice()?k:null;
    }
    /** A reader sees her only through clear ice, looking her way, not too far off. */
    static boolean seesUnderIce(ServerLevel l,BlockPos b,ServerPlayer p,LakeWitchEntity witch){
        var to=witch.position().add(0,.5,0).subtract(p.getEyePosition());double distance=to.length();if(distance>24||distance<.1)return false;
        if(p.getLookAngle().dot(to.scale(1/distance))<.82)return false;
        var k=WinterPropBlock.kind(l.getBlockState(BlockPos.containing(witch.getX(),b.getY()-1,witch.getZ())));
        return k!=null&&k.clear()||l.getBlockState(BlockPos.containing(witch.getX(),b.getY()-1,witch.getZ())).is(Blocks.WATER);
    }
    /** Noise a reader makes this tick, from how far they moved and how: running 3, walking 1, crouching or standing 0. */
    static int noise(ServerPlayer p,double moved){return moved<.03||p.isShiftKeyDown()||p.isCrouching()?0:p.isSprinting()||moved>.2?3:1;}
    private static final Map<UUID,Vec3> LAST=new HashMap<>();
    private static final Map<UUID,Float> SUSPICION=new HashMap<>();
    /** How close she has come to finding this reader, from beneath. */
    public static float suspicion(ServerPlayer p){return SUSPICION.getOrDefault(p.getUUID(),0F);}

    // ------------------------------------------------------------------------------------------------ the shared night
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;
        var b=LabyrinthPlaces.base(origin,PLACE);var l=server.getLevel(NovelRooms.dimension(PLACE));if(b==null||l==null)return;
        var readers=server.getPlayerList().getPlayers().stream().filter(p->LiteraryVignettes.inside(p,PLACE)).toList();
        var d=LabyrinthData.get(server);var w=LiteraryVignettes.shared(d,PLACE);
        if(readers.isEmpty()){
            if(w.contains("Night")){w.putInt("Empty",w.getInt("Empty")+1);
                if(w.getInt("Empty")>=EMPTY_RESET&&l.players().stream().noneMatch(p->p.getCamera().distanceToSqr(Vec3.atCenterOf(b.offset(0,0,-40)))<70*70))reset(w);
                LiteraryVignettes.shared(d,PLACE,w);}
            else if(w.getBoolean("Settle")){if(settle(l,b,w))w.remove("Settle");LiteraryVignettes.shared(d,PLACE,w);}
            return;
        }
        if(!built(l,b))return;
        w.putInt("Empty",0);int night=w.getInt("Night")+1;w.putInt("Night",night);
        settle(l,b,w);boys(l,b,w,night,readers);
        var witch=witch(l);
        if(witch!=null){if(!witch.driven())adopt(witch,b);listen(l,b,w,readers,witch);move(l,b,w,witch,night);}
        if(night%200==1)l.playSound(null,b.getX(),b.getY()+6,b.getZ()-40,WinterLakeRegistry.WIND.get(),SoundSource.AMBIENT,2.5F,.9F+l.random.nextFloat()*.2F);
        if(night%10==0)for(var who:BOYS){var a=cast(l,w,who);if(a!=null&&a.isCustomNameVisible()&&l.getGameTime()-a.getPersistentData().getLong("Said0504")>100)a.setCustomNameVisible(false);}
        LiteraryVignettes.shared(d,PLACE,w);
    }
    /** The night starts over for whoever comes next: the boys by the fire, the ice whole, her under it. */
    private static void reset(CompoundTag w){
        for(var k:List.of("Night","Empty","Fled","FledAt","Hand","HandBy","Heard","HeardX","HeardZ","Rest","DriftX","DriftZ","Thrown"))w.remove(k);
        w.putInt("Gen",w.getInt("Gen")+1);w.putBoolean("Settle",true);LAST.clear();SUSPICION.clear();
    }
    /** Puts each loaded piece back as the current night needs it; true once everything is in place. */
    private static boolean settle(ServerLevel l,BlockPos b,CompoundTag w){
        int gen=w.getInt("Gen");boolean all=true;
        for(var who:BOYS){var a=cast(l,w,who);if(a==null){all&=!w.hasUUID("Cast"+who.key());continue;}if(a.getPersistentData().getInt("Gen0504")==gen)continue;
            home(a,who,b);a.getPersistentData().putInt("Gen0504",gen);}
        var witch=witch(l);if(witch==null&&w.hasUUID("Witch"))all=false;
        if(witch!=null&&witch.getPersistentData().getInt("Gen0504")!=gen){adopt(witch,b);witch.getPersistentData().putInt("Gen0504",gen);}
        if(!w.getBoolean("Fled")){var fire=b.offset(BONFIRE);if(l.isLoaded(fire)&&l.getBlockState(fire).is(Blocks.CAMPFIRE)&&!l.getBlockState(fire).getValue(CampfireBlock.LIT))l.setBlock(fire,l.getBlockState(fire).setValue(CampfireBlock.LIT,true),3);}
        if(!w.contains("Night")&&w.contains("Changed")){
            var changed=w.getList("Changed",Tag.TAG_COMPOUND);var left=new ListTag();
            for(int i=0;i<changed.size();i++){var c=changed.getCompound(i);var at=BlockPos.of(c.getLong("Pos"));
                if(!l.isLoaded(at)){left.add(c);continue;}l.setBlock(at,WinterPropBlock.of(WinterPropBlock.Kind.valueOf(c.getString("Kind"))),F);}
            if(left.isEmpty())w.remove("Changed");else{w.put("Changed",left);all=false;}
        }
        return all;
    }
    private static void home(LiteraryActor a,Boy who,BlockPos b){
        a.setInvisible(false);a.setCustomNameVisible(false);a.gesture(0);var at=who.home(b);a.moveTo(at.x,at.y,at.z,who.yaw(),0);turn(a,who.yaw());a.setDeltaMovement(Vec3.ZERO);
    }
    /** She is the lake's now: hanging from the underside of the ice, driven by what she hears. */
    private static void adopt(LakeWitchEntity witch,BlockPos b){
        witch.drive(true);witch.underIce(true);witch.reach(false);
        witch.setPos(b.getX()+WITCH_START.getX()+.5,b.getY()+UNDER,b.getZ()+WITCH_START.getZ()+.5);witch.setDeltaMovement(Vec3.ZERO);
    }
    /** Remembers what a cell of ice was before she cracked or broke it, so the next night can have it back. */
    private static void remember(CompoundTag w,BlockPos at,WinterPropBlock.Kind kind){
        var changed=w.getList("Changed",Tag.TAG_COMPOUND);
        for(int i=0;i<changed.size();i++)if(changed.getCompound(i).getLong("Pos")==at.asLong())return;
        var c=new CompoundTag();c.putLong("Pos",at.asLong());c.putString("Kind",kind.name());changed.add(c);w.put("Changed",changed);
    }

    /** Snowballs until Hank stops it, a few words now and then, and the run once she has the ribbon. */
    private static void boys(ServerLevel l,BlockPos b,CompoundTag w,int night,List<ServerPlayer> readers){
        boolean fled=w.getBoolean("Fled");
        if(!fled&&night<QUIET_AT&&night%60==30){
            var who=BOYS.get((night/60)%BOYS.size());var a=cast(l,w,who);
            if(a!=null){var ball=new Snowball(l,a);ball.setItem(new ItemStack(Items.SNOWBALL));
                double tx=b.getX()+l.random.nextInt(25)-12+.5,tz=b.getZ()-18-l.random.nextInt(14)+.5;var from=ball.position();double dx=tx-from.x,dz=tz-from.z,dist=Math.sqrt(dx*dx+dz*dz);
                ball.shoot(dx,b.getY()-from.y+dist*.18,dz,1.1F,2);l.addFreshEntity(ball);a.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                if(night%180==30)speak(a,who,FIGHT[(night/180)%FIGHT.length],readers,20);}
        }
        if(!fled&&night==QUIET_AT){var hank=cast(l,w,HANK);if(hank!=null)speak(hank,HANK,"Quit it. Quit it! You'll wake her up.",readers,40);}
        if(!fled&&night>QUIET_AT&&night%240==120){int n=night/240;var who=BOYS.get(n%BOYS.size());var a=cast(l,w,who);
            if(a!=null&&who!=HANK&&readers.stream().anyMatch(p->p.distanceToSqr(a)<16*16))say(a,who.lines()[(n/BOYS.size())%who.lines().length]);}
        if(!fled)return;
        int t=night-w.getInt("FledAt");
        if(t==0){var bud=cast(l,w,BUD);if(bud!=null)speak(bud,BUD,"She took it. She took it!",readers,64);
            l.playSound(null,b.getX()-7,b.getY()+1,b.getZ()-7,WinterLakeRegistry.RUN.get(),SoundSource.NEUTRAL,1.4F,1);}
        if(t==10){var clay=cast(l,w,CLAY);if(clay!=null)speak(clay,CLAY,"Run!",readers,64);}
        if(t==15){var fire=b.offset(BONFIRE);var s=l.getBlockState(fire);if(s.is(Blocks.CAMPFIRE)&&s.getValue(CampfireBlock.LIT)){l.setBlock(fire,s.setValue(CampfireBlock.LIT,false),3);
            l.sendParticles(ParticleTypes.LARGE_SMOKE,fire.getX()+.5,fire.getY()+.6,fire.getZ()+.5,12,.3,.2,.3,.01);l.playSound(null,fire,net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,1,1);}}
        if(t==40){var hank=cast(l,w,HANK);if(hank!=null)speak(hank,HANK,"I'm sorry. I'm sorry!",readers,80);}
        for(int i=0;i<BOYS.size();i++){var a=cast(l,w,BOYS.get(i));if(a==null||a.isInvisible())continue;
            if(t<RUN_TICKS)ElkLotEnding.step(a,new Vec3(b.getX()+RUN[i][0],b.getY(),b.getZ()+RUN[i][1]),.25);
            else{a.setInvisible(true);a.setCustomNameVisible(false);a.moveTo(b.getX()+RUN[i][0],b.getY(),b.getZ()+RUN[i][1],90,0);}}
    }

    /** What she hears from each reader on the ice, and what she does under one who keeps making noise. */
    private static void listen(ServerLevel l,BlockPos b,CompoundTag w,List<ServerPlayer> readers,LakeWitchEntity witch){
        boolean busy=w.getInt("Hand")>0||w.getInt("Rest")>0;float heard=w.getFloat("Heard");
        for(var p:readers){
            if(p.isSpectator()||p.isCreative()||!p.isAlive())continue;
            var now=p.position();var last=LAST.put(p.getUUID(),now);double moved=last==null?0:Math.hypot(now.x-last.x,now.z-last.z);
            var kind=underfoot(l,b,p);float loud=kind==null?0:noise(p,moved)*(kind.clear()?1.5F:.4F);
            if(loud>0&&loud*10>=heard*.7F){heard=Math.max(heard,loud*10);w.putDouble("HeardX",now.x);w.putDouble("HeardZ",now.z);}
            float before=SUSPICION.getOrDefault(p.getUUID(),0F);
            boolean under=!busy&&kind!=null&&Math.hypot(witch.getX()-now.x,witch.getZ()-now.z)<1.6;
            float sus=under&&loud>0?before+loud:Math.max(0,before-.4F);SUSPICION.put(p.getUUID(),sus);
            if(under&&sus>before)stages(l,b,w,p,before,sus,witch);
            // Once she has let go of someone she has heard nothing; the lake is quiet to her until she has rested.
            if(w.getInt("Rest")>0){busy=true;heard=Math.min(heard,w.getFloat("Heard"));}
        }
        w.putFloat("Heard",heard);
    }
    private static boolean crossed(float before,float after,float mark){return before<mark&&after>=mark;}
    private static void stages(ServerLevel l,BlockPos b,CompoundTag w,ServerPlayer p,float before,float sus,LakeWitchEntity witch){
        var cell=BlockPos.containing(p.getX(),b.getY()-1,p.getZ());var at=Vec3.atCenterOf(cell);
        if(crossed(before,sus,KNOCK)||crossed(before,sus,KNOCK_TWICE)){
            l.playSound(null,at.x,at.y,at.z,WinterLakeRegistry.KNOCK.get(),SoundSource.HOSTILE,sus>=KNOCK_TWICE?1.3F:.9F,sus>=KNOCK_TWICE?.92F:1);
            var d=LabyrinthData.get(l.getServer());var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("Knocked0504",true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        }
        var kind=WinterPropBlock.kind(l.getBlockState(cell));if(kind==null||!kind.ice())return;
        if(crossed(before,sus,CRACK)||crossed(before,sus,SPLIT)){
            remember(w,cell,kind);l.setBlock(cell,WinterPropBlock.of(sus>=SPLIT?WinterPropBlock.Kind.SPLIT:WinterPropBlock.Kind.CRACKED),F);
            l.playSound(null,at.x,at.y,at.z,WinterLakeRegistry.CRACK.get(),SoundSource.BLOCKS,1.2F,sus>=SPLIT?.85F:1);
        }
        if(sus>=THROUGH)through(l,b,w,p,cell,witch);
    }
    /** The ice gives under the reader and she pulls them down; then she lets go, and they must find the hole they made. */
    static void through(ServerLevel l,BlockPos b,CompoundTag w,ServerPlayer p,BlockPos cell,LakeWitchEntity witch){
        for(var at:List.of(cell,cell.north(),cell.south(),cell.east(),cell.west())){var k=WinterPropBlock.kind(l.getBlockState(at));if(k==null||!k.ice())continue;
            remember(w,at,k);l.setBlock(at,at.equals(cell)?Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,12).setValue(BlockStateProperties.WATERLOGGED,true):Blocks.WATER.defaultBlockState(),3);}
        var at=Vec3.atCenterOf(cell);l.playSound(null,at.x,at.y,at.z,WinterLakeRegistry.BREAK.get(),SoundSource.HOSTILE,1.6F,1);
        l.sendParticles(ParticleTypes.SPLASH,at.x,at.y+.6,at.z,30,.6,.1,.6,.2);l.sendParticles(ParticleTypes.ITEM_SNOWBALL,at.x,at.y+.6,at.z,16,.5,.2,.5,.1);
        p.teleportTo(p.serverLevel(),p.getX(),b.getY()-3,p.getZ(),p.getYRot(),p.getXRot());p.setDeltaMovement(0,-.2,0);p.hurtMarked=true;
        p.hurt(witch.damageSources().mobAttack(witch),2);
        p.displayClientMessage(Component.literal("The ice gave. Find the hole you fell through. Swim for the light."),true);
        var d=LabyrinthData.get(l.getServer());var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("FellThrough0504",true);own.remove("CameUp0504");LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        // She lets go and drifts off; nobody is found again for a while.
        w.putInt("Rest",REST_AFTER_BREAK);w.putFloat("Heard",0);SUSPICION.clear();
        double ax=witch.getX()-p.getX(),az=witch.getZ()-p.getZ(),n=Math.max(.1,Math.hypot(ax,az));w.putDouble("DriftX",witch.getX()+ax/n*12);w.putDouble("DriftZ",witch.getZ()+az/n*12);
    }
    /** Her movement under the ice: to the last thing she heard, or drifting; up into the hole for the ribbon. */
    private static void move(ServerLevel l,BlockPos b,CompoundTag w,LakeWitchEntity witch,int night){
        int hand=w.getInt("Hand");
        if(hand>0){
            hand--;w.putInt("Hand",hand);var center=Vec3.atBottomCenterOf(b.offset(HOLE));
            if(hand==HAND_TICKS-1){witch.underIce(false);witch.reach(true);witch.setPos(center.x,b.getY()-1.25,center.z);
                if(w.hasUUID("HandBy")&&l.getPlayerByUUID(w.getUUID("HandBy")) instanceof ServerPlayer giver){var to=giver.position().subtract(center);turn(witch,(float)Math.toDegrees(Math.atan2(-to.x,to.z)));}}
            if(hand%10==0)l.sendParticles(ParticleTypes.SPLASH,center.x,b.getY()+.1,center.z,8,.4,0,.4,.1);
            if(hand==0){witch.reach(false);witch.underIce(true);witch.setPos(center.x+2,b.getY()+UNDER,center.z+2);w.putInt("Rest",60);w.putFloat("Heard",0);w.putDouble("DriftX",center.x+8);w.putDouble("DriftZ",center.z-8);}
            return;
        }
        int rest=w.getInt("Rest");float heard=w.getFloat("Heard");double gx,gz,speed;
        if(rest>0){w.putInt("Rest",rest-1);gx=w.getDouble("DriftX");gz=w.getDouble("DriftZ");speed=.08;}
        else if(heard>4){gx=w.getDouble("HeardX");gz=w.getDouble("HeardZ");speed=.14;w.putFloat("Heard",heard*.985F);}
        else{
            if(!w.contains("DriftX")||night%200==0||Math.hypot(witch.getX()-w.getDouble("DriftX"),witch.getZ()-w.getDouble("DriftZ"))<1){
                w.putDouble("DriftX",b.getX()+X0+3+l.random.nextInt(X1-X0-5)+.5);w.putDouble("DriftZ",b.getZ()+FAR+3+l.random.nextInt(NEAR-FAR-5)+.5);}
            gx=w.getDouble("DriftX");gz=w.getDouble("DriftZ");speed=.035;w.putFloat("Heard",0);
        }
        gx=Math.max(b.getX()+X0+1.5,Math.min(b.getX()+X1-.5,gx));gz=Math.max(b.getZ()+FAR+1.5,Math.min(b.getZ()+NEAR-.5,gz));
        double dx=gx-witch.getX(),dz=gz-witch.getZ(),dist=Math.hypot(dx,dz);
        if(dist>.01){double step=Math.min(speed,dist);witch.setPos(witch.getX()+dx/dist*step,b.getY()+UNDER,witch.getZ()+dz/dist*step);turn(witch,(float)Math.toDegrees(Math.atan2(-dx,dz)));}
        else witch.setPos(witch.getX(),b.getY()+UNDER,witch.getZ());
        witch.setDeltaMovement(Vec3.ZERO);
        // Under someone who has gone quiet, the ice groans now and then.
        if(night%70==0&&SUSPICION.values().stream().anyMatch(v->v>0))l.playSound(null,witch.getX(),b.getY()-1,witch.getZ(),WinterLakeRegistry.GROAN.get(),SoundSource.HOSTILE,.7F,.9F+l.random.nextFloat()*.2F);
    }
    /** A snowball thudding onto the ice is the loudest thing on the lake; she goes to where it landed. */
    @SubscribeEvent public static void landed(ProjectileImpactEvent e){
        if(!(e.getProjectile() instanceof Snowball ball)||!(ball.level() instanceof ServerLevel l))return;
        var origin=HouseSavedData.get(l.getServer()).houseOrigin();if(origin==null)return;var b=LabyrinthPlaces.base(origin,PLACE);
        if(b==null||!l.dimension().equals(NovelRooms.dimension(PLACE))||!built(l,b))return;
        var hit=e.getRayTraceResult().getLocation();int x=(int)Math.floor(hit.x)-b.getX(),z=(int)Math.floor(hit.z)-b.getZ();
        if(!lake(x,z)||hit.y<b.getY()-1.5||hit.y>b.getY()+1.5)return;
        var d=LabyrinthData.get(l.getServer());var w=LiteraryVignettes.shared(d,PLACE);
        if(w.getInt("Hand")>0||w.getInt("Rest")>0)return;
        w.putFloat("Heard",Math.max(w.getFloat("Heard"),60));w.putDouble("HeardX",hit.x);w.putDouble("HeardZ",hit.z);LiteraryVignettes.shared(d,PLACE,w);
        if(ball.getOwner() instanceof ServerPlayer p&&LiteraryVignettes.inside(p,PLACE)){var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("Lured0504",true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);}
    }

    // ------------------------------------------------------------------------------------------------ each reader
    public static void arrive(ServerPlayer p,CompoundTag own){
        for(var k:List.of("GaveRibbon0504","Lured0504","SawHer0504","Knocked0504","FellThrough0504","CameUp0504","HoleHint0504","UnderHint0504"))own.remove(k);
        own.putBoolean("Shallows0504",IndianLakeProgress.hasThrown(LabyrinthData.get(p.server),p.getUUID()));
    }
    /** Every five ticks for each reader here: everyone exists, once; what this reader saw of her; the fall; the bank. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();if(!built(l,b))return;var d=LabyrinthData.get(p.server);
        for(var who:BOYS){
            var a=LiteraryVignettes.actor(p,PLACE,"Cast"+who.key(),LiteraryActor.STRANGER,BlockPos.containing(who.x(),0,who.z()),false);
            if(a==null||a.phase()==who.phase())continue;
            a.appearance(LiteraryActor.STRANGER,who.phase());a.getPersistentData().putInt("Gen0504",LiteraryVignettes.shared(d,PLACE).getInt("Gen"));home(a,who,b);
        }
        var witch=LiteraryVignettes.huntBody(p,PLACE,WITCH_START);if(witch!=null&&!witch.driven())adopt(witch,b);
        if(p.isSpectator())return;
        if(witch!=null&&witch.underIce()&&seesUnderIce(l,b,p,witch))own.putBoolean("SawHer0504",true);
        if(own.getBoolean("FellThrough0504")&&!own.getBoolean("CameUp0504")&&!p.isInWater()&&p.getY()>=b.getY()-.3)own.putBoolean("CameUp0504",true);
        var hole=Vec3.atBottomCenterOf(b.offset(HOLE));double toHole=Math.hypot(p.getX()-hole.x,p.getZ()-hole.z);
        if(toHole<4&&!own.getBoolean("TookDare0504"))hint(p,own,"HoleHint0504","The hole where she went down. Hank still has her ribbon.");
        double z=p.getZ()-b.getZ();boolean bank=z>NEAR+.5&&p.getY()>=b.getY()-.3&&!p.isInWater();
        if(own.getBoolean("GaveRibbon0504")&&bank&&!own.getBoolean("Ready"))LiteraryVignettes.ready(p,PLACE,own,"broke_ice_waited_beneath_it_and_reached_the_bank");
    }
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var who=boy(a);if(who==null||a.isInvisible())return;
        int n=own.getInt("Talk_"+who.key());own.putInt("Talk_"+who.key(),n+1);
        String line;
        if(who==HANK){
            if(own.getBoolean("GaveRibbon0504"))line="Is it gone? Is it really gone?";
            else if(own.getBoolean("TookDare0504")){line="Bud's dad says sound carries a mile on the ice. Go slow.";if(!carries(p))give(p,ribbon(p));}
            else if(n==0&&own.getBoolean("Shallows0504"))line="You came. You were there that day too. You had her by the wrists.";
            else{line=HANK_TALK[Math.min(n,HANK_TALK.length-1)];
                if(n>=HANK_TALK.length-1){own.putBoolean("TookDare0504",true);if(!carries(p))give(p,ribbon(p));}}
        }
        else line=n==0&&!LiteraryVignettes.shared(LabyrinthData.get(p.server),PLACE).getBoolean("Fled")&&LiteraryVignettes.shared(LabyrinthData.get(p.server),PLACE).getInt("Night")<QUIET_AT?FIGHT[who.phase()%FIGHT.length]:who.lines()[n%who.lines().length];
        speak(a,who,line,List.of(p),24);
    }
    /** Her ribbon, given to this reader; only the one it was given to can give it back. */
    public static ItemStack ribbon(ServerPlayer p){
        var stack=new ItemStack(WinterLakeRegistry.RIBBON.get());var tag=new CompoundTag();tag.putUUID("Owner",p.getUUID());stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME,Component.literal("Her ribbon"));stack.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("It came off in Hank's hand."))));
        return stack;
    }
    private static boolean owns(ServerPlayer p,ItemStack stack){var data=stack.get(DataComponents.CUSTOM_DATA);return stack.is(WinterLakeRegistry.RIBBON.get())&&data!=null&&data.copyTag().hasUUID("Owner")&&data.copyTag().getUUID("Owner").equals(p.getUUID());}
    private static boolean carries(ServerPlayer p){for(var stack:p.getInventory().items)if(owns(p,stack))return true;return owns(p,p.getOffhandItem());}
    private static void give(ServerPlayer p,ItemStack stack){if(!p.getInventory().add(stack))p.drop(stack,false);}
    /** Held out over the hole where she went down, the ribbon is taken: a hand comes up out of the water for it, and the boys run. */
    public static boolean giveBack(ServerPlayer p,ItemStack stack){
        if(!LiteraryVignettes.inside(p,PLACE)||p.isSpectator())return false;var b=LiteraryVignettes.base(p,PLACE);var l=p.serverLevel();if(b==null||!built(l,b))return false;
        if(!owns(p,stack)){p.displayClientMessage(Component.literal("It isn't yours to give back."),true);return false;}
        var hole=Vec3.atBottomCenterOf(b.offset(HOLE));double dx=p.getX()-hole.x,dz=p.getZ()-hole.z;
        if(dx*dx+dz*dz>3.5*3.5||p.getY()<b.getY()-2.5){p.displayClientMessage(Component.literal("Out at the hole, Hank said. Where she went down."),true);return false;}
        var d=LabyrinthData.get(p.server);var w=LiteraryVignettes.shared(d,PLACE);if(w.getInt("Hand")>0)return false;
        stack.shrink(1);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("GaveRibbon0504",true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        w.putInt("Hand",HAND_TICKS);w.putUUID("HandBy",p.getUUID());if(!w.getBoolean("Fled")){w.putBoolean("Fled",true);w.putInt("FledAt",w.getInt("Night"));}
        LiteraryVignettes.shared(d,PLACE,w);
        l.playSound(null,hole.x,hole.y,hole.z,WinterLakeRegistry.HAND.get(),SoundSource.HOSTILE,1.2F,1);
        p.displayClientMessage(Component.literal("A hand comes up out of the black water and takes it."),true);
        return true;
    }
    /** A snowball from the boys' heap, eight at most in a reader's pockets. */
    public static void scoop(ServerPlayer p){
        if(!LiteraryVignettes.inside(p,PLACE)||p.isSpectator())return;
        if(p.getInventory().countItem(Items.SNOWBALL)>=8){p.displayClientMessage(Component.literal("Your pockets are full of snow."),true);return;}
        give(p,new ItemStack(Items.SNOWBALL));p.displayClientMessage(Component.literal("You pack a snowball."),true);
    }

    // ------------------------------------------------------------------------------------------------ the account
    public static String account(CompoundTag own){
        var text=new StringBuilder("That winter the lake froze, white where the snow lay on it and black where the wind had cleared it, and the boys went back out to it: Bud, Hank, Clay and Lonnie. I went with them.\n\n");
        text.append(own.getBoolean("Shallows0504")?"I had held her wrists at the shallows that summer. Lonnie had held her feet. Hank had held on to her ribbon ever since.\n\n"
                :"Hank had carried her ribbon since the summer. He said it came off in his hand.\n\n");
        text.append("He gave it to me, because somebody had to take it back out to her.\n\n");
        if(own.getBoolean("Lured0504"))text.append("I threw a snowball out onto the ice, and something under the ice went to where it landed.\n\n");
        if(own.getBoolean("SawHer0504"))text.append("Where the ice was black I could see down. Once she was there, under it, with her hands flat against it, looking up.\n\n");
        if(own.getBoolean("Knocked0504"))text.append("She knocked on the ice under my feet. I stood as still as I have ever stood until she went away.\n\n");
        if(own.getBoolean("FellThrough0504"))text.append("The ice opened and I went down into the black water with her. The only light was the hole I had made. "+(own.getBoolean("CameUp0504")?"I came up through it.":"")+"\n\n");
        text.append("At the hole where she went down, I held the ribbon out over the water. A hand came up and took it.\n\n");
        text.append("Then the boys ran, the way we all ran at the shallows, and I walked back across the ice alone, as quietly as I could.\n\n");
        return text.append("The fire was out when I reached the bank. Behind me the ice ticked and settled, and somewhere under it she had her ribbon back.\n\nIt did not stay down. In June Hank found it on his table, dry, the week of the costume contest, and it has been there every June since.").toString();
    }
    public static void clearAll(){LAST.clear();SUSPICION.clear();}

    // ------------------------------------------------------------------------------------------------ the build
    private record Site(ServerLevel l,BlockPos b){
        void set(int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
        void set(int x,int y,int z,Block block){set(x,y,z,block.defaultBlockState());}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(x,y,z,s);}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block block){fill(x0,y0,z0,x1,y1,z1,block.defaultBlockState());}
        BlockState at(int x,int y,int z){return BuildBlocks.state(l,b.offset(x,y,z));}
    }
    /** The frozen lake, the near bank where the boys keep their fire, the far bank, and the spruce all round. */
    public static void build(ServerLevel l,BlockPos b){
        var s=new Site(l,b);var r=PLACE.room();
        for(int x=r.minX();x<=r.maxX();x++)for(int z=r.minZ();z<=r.maxZ();z++){
            if(lake(x,z)){s.fill(x,r.minY(),z,x,-9,z,Blocks.CLAY);s.fill(x,-8,z,x,-7,z,Blocks.SAND);s.fill(x,-6,z,x,-2,z,Blocks.WATER);
                s.set(x,-1,z,hole(x,z)?Blocks.WATER.defaultBlockState():shanty(x,z)?Blocks.SPRUCE_PLANKS.defaultBlockState():WinterPropBlock.of(surface(x,z)));continue;}
            s.fill(x,r.minY(),z,x,-2,z,Blocks.DIRT);s.set(x,-1,z,Blocks.SNOW_BLOCK);
            if(Math.floorMod(x*7+z*3,5)!=0)s.set(x,0,z,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,1));
        }
        // A faint light down under the hole that never froze, for anyone in the water looking for the way up.
        s.set(HOLE.getX(),-4,HOLE.getZ(),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,7).setValue(BlockStateProperties.WATERLOGGED,true));
        for(int x=-4;x<=4;x++)for(int z=HOLE.getZ()-4;z<=HOLE.getZ()+4;z++){int dx=x-HOLE.getX(),dz=z-HOLE.getZ();int q=dx*dx+dz*dz;
            if(q>=5&&q<=10&&Math.floorMod(x*7+z*13,3)!=0)s.set(x,0,z,WinterPropBlock.of(WinterPropBlock.Kind.CATTAILS));}
        trees(s);bank(s);shanty(s);
    }
    private static BlockState leaves(Block b){return b.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    private static void spruce(Site s,int x,int z,int height){
        for(int h=2;h<=height+1;h++){int w=h>=height?0:Math.max(0,(height-h)/2+(h%2));for(int dx=-w;dx<=w;dx++)for(int dz=-w;dz<=w;dz++)if(Math.abs(dx)+Math.abs(dz)<=w+1&&s.at(x+dx,h,z+dz).canBeReplaced())s.set(x+dx,h,z+dz,leaves(Blocks.SPRUCE_LEAVES));}
        s.fill(x,0,z,x,height-1,z,Blocks.SPRUCE_LOG);s.set(x,height+1,z,leaves(Blocks.SPRUCE_LEAVES));s.set(x,height+2,z,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,2));
    }
    private static void trees(Site s){
        // Dark spruce on both side banks and along the far bank; nothing grows on the ice or the near bank.
        for(int z=-4;z>=-84;z-=5){spruce(s,-31,z+Math.floorMod(z,3)-1,6+Math.floorMod(z,4));if(z<=-16)spruce(s,32,z-2,5+Math.floorMod(z,3));}
        for(int x=-26;x<=26;x+=6)spruce(s,x,-78-Math.floorMod(x,3),6+Math.floorMod(x,3));
        for(int x=-23;x<=23;x+=8)spruce(s,x,-84,7);
    }
    private static void bank(Site s){
        // The boys' fire, with logs to sit on; their sled, with the account beside it; a stump with Bud's note on it; a lantern.
        s.set(BONFIRE.getX(),0,BONFIRE.getZ(),Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.SIGNAL_FIRE,false));
        s.set(-9,0,-8,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z));
        s.set(-6,0,-9,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        for(int z:new int[]{-5,-7})s.set(-3,0,z,Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.NORTH));
        s.set(4,0,-6,Blocks.STRIPPED_SPRUCE_LOG);
        s.set(-1,0,-10,Blocks.LANTERN);
        // The snow fort they had been throwing from, and the heaps of snowballs behind it.
        for(int x=-15;x<=-11;x++)s.set(x,0,-12,Blocks.SNOW_BLOCK);
        for(int x=1;x<=3;x++)s.set(x,0,-10,Blocks.SNOW_BLOCK.defaultBlockState());
        for(var heap:SNOWBALLS)s.set(heap.getX(),heap.getY(),heap.getZ(),WinterPropBlock.of(WinterPropBlock.Kind.SNOWBALLS));
    }
    private static void shanty(Site s){
        // A fishing shanty out on the ice: a plank floor she cannot hear through, walls, two small windows and a doorway to the bank.
        for(int x=11;x<=15;x++)for(int z=-37;z<=-33;z++){
            boolean wall=x==11||x==15||z==-37||z==-33;
            for(int y=0;y<=2;y++)s.set(x,y,z,wall?Blocks.SPRUCE_PLANKS.defaultBlockState():Blocks.AIR.defaultBlockState());
            s.set(x,3,z,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        }
        s.set(13,0,-33,Blocks.AIR);s.set(13,1,-33,Blocks.AIR);
        s.set(11,1,-35,Blocks.GLASS_PANE);s.set(15,1,-35,Blocks.GLASS_PANE);
        s.set(12,0,-36,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
    }
}
