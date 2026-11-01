package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.NovelScenePayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.211, the stillness (docs/STILLNESS_0_4_211.md), the third of the dark halls' horrors: the growl, made flesh.
 *
 * <p>A wide, low dark hall with a door at each end. When a reader is past halfway, a growl comes from the far door and something
 * enormous walks the hall: the Minotaur's voice and weight, never seen, only heard, felt in the floor and pressed into the dust.
 * It hunts by sound. Standing still is silent; crouching is heard close by; walking across a good part of the hall; running,
 * jumping, a door, food or a dropped thing all of it. It goes to what it hears, fast; when it hears nothing it finds the still
 * reader by smell, slowly, and stands over them for twenty to forty seconds, breathing on them (each breath counted on their
 * screen, a fog of it over their sight), shoving them now and then with its head to make them move, while their heart climbs
 * until the screen shakes with it. Moving while it stands over them is a goring: a real House death unless a raised shield takes
 * it, once, and cracks; a survivor is thrown back out through the door they came in by. Once it has stood over everyone in the
 * hall it goes back through the far door, and the hoofs fade; a reader it stood over who then reaches the far door has their
 * account ({@code let_it_breathe}).
 *
 * <p>The thing is shared and so is its hearing: one sound draws it to everyone, and a reader can draw it off a friend by making
 * noise, and then has to stand still themselves while it comes. Counts and outcomes are each reader's own.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Stillness {
    private Stillness(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.STILLNESS;
    /** Its modes. */
    public static final int WAITING=0,ENTERING=1,HUNTING=2,OVER=3,LEAVING=4,GONE=5;
    /** How far each noise carries, in blocks. */
    public static final double CROUCH=3,WALK=10,JUMP=14,RUN=24,LOUD=48;
    /** How fast it goes to a sound, how slowly it follows a smell, how it walks off; how long it stands in the far door first. */
    public static final double TO_SOUND=.30,TO_SMELL=.10,AWAY=.14;
    public static final int ENTER=60,REACH_GRACE=10,BREATH=60,NUDGE_QUIET=12;
    /** Within this, it has reached a reader; past this from where they stood, they have moved. */
    public static final double REACH=1.6,MOVED=.35;
    /** A goring: through armour, and enough to kill; what a raised shield lets through. */
    public static final float GORE=30F,BLOCKED=4F;
    /** Test hook: stands over a reader this long instead of twenty to forty seconds, and comes in at once. */
    public static int TEST_OVER=0;
    public static final ResourceKey<DamageType> GORED=ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"stillness"));
    static DamageSource goring(ServerLevel l){return l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolder(GORED).map(DamageSource::new).orElse(l.damageSources().generic());}
    private static final Map<UUID,Vec3> LAST=new HashMap<>();
    private static final Map<UUID,Long> PULSE=new HashMap<>();

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static CompoundTag set(MinecraftServer s){return LiteraryVignettes.shared(LabyrinthData.get(s),PLACE);}
    static void set(MinecraftServer s,CompoundTag t){LiteraryVignettes.shared(LabyrinthData.get(s),PLACE,t);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static boolean inside(ServerPlayer p){return LiteraryVignettes.inside(p,PLACE);}
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void heard(ServerPlayer p,Vec3 at,SoundEvent s,float volume,float pitch){p.connection.send(new ClientboundSoundPacket(Holder.direct(s),SoundSource.HOSTILE,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}
    public static int mode(CompoundTag t){return t.getInt("Mode");}
    public static Vec3 at(CompoundTag t){return new Vec3(t.getDouble("X"),0,t.getDouble("Z"));}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static double flat(Vec3 a,Vec3 c){return Math.hypot(a.x-c.x,a.z-c.z);}
    static boolean present(int mode){return mode==ENTERING||mode==HUNTING||mode==OVER||mode==LEAVING;}
    static Set<String> done(CompoundTag t){var out=new HashSet<String>();for(var e:t.getList("Done",Tag.TAG_STRING))out.add(e.getAsString());return out;}
    static void done(CompoundTag t,ServerPlayer q){var list=t.getList("Done",Tag.TAG_STRING);for(var e:list)if(e.getAsString().equals(q.getStringUUID()))return;list.add(StringTag.valueOf(q.getStringUUID()));t.put("Done",list);}
    public static String words(int n){String[] w={"none","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen","twenty"};
        if(n<w.length)return w[n];String[] tens={"","","twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety"};return n<100?tens[n/10]+(n%10==0?"":"-"+w[n%10]):Integer.toString(n);}

    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("Ready")&&!own.getBoolean("Completed")){cue(p,"At the far end, scratches in the dust before the door. You know whose they are.");return;}
        own.remove("Stood211");own.remove("Drawn211");own.remove("Drew211");
        cue(p,"A wide hall, and low. Your hand could touch the ceiling. Far off at the end of it, a door.");
    }

    // ------------------------------------------------------------------------------------------------ the noise a reader makes
    /** How far this reader's noise carries this tick: nothing standing still, a little crouching, more walking, all of it running. */
    public static double noise(ServerPlayer q,long tick){
        var now=q.position();var last=LAST.put(q.getUUID(),now);
        Long pulse=PULSE.get(q.getUUID());if(pulse!=null&&tick-pulse<=2)return LOUD;
        if(last==null)return 0;
        double d=flat(now,last),up=now.y-last.y;
        if(up>.12&&!q.isPassenger())return JUMP;
        if(d<.03)return 0;
        if(q.isSprinting()||d>.26)return RUN;
        if(q.isShiftKeyDown()&&d<.16)return CROUCH;
        return WALK;
    }
    /** Food, a dropped thing or a door, anywhere in the hall: all of it hears. */
    static void loud(ServerPlayer p){if(inside(p))PULSE.put(p.getUUID(),(long)p.server.getTickCount());}
    @SubscribeEvent public static void eat(LivingEntityUseItemEvent.Start e){if(e.getEntity() instanceof ServerPlayer p&&e.getItem().has(net.minecraft.core.component.DataComponents.FOOD))loud(p);}
    @SubscribeEvent public static void toss(ItemTossEvent e){if(e.getPlayer() instanceof ServerPlayer p)loud(p);}
    @SubscribeEvent public static void door(PlayerInteractEvent.RightClickBlock e){if(e.getEntity() instanceof ServerPlayer p&&p.level().getBlockState(e.getPos()).getBlock() instanceof DoorBlock)loud(p);}

    // ------------------------------------------------------------------------------------------------ the hall
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        if(rel.equals(StillnessSite.TALLY)){if(!own.getBoolean("Ready")){cue(p,"Scratches in the dust in rows of five, more rows than you can count from here. Someone counted something, a long time.");return true;}return false;}
        return false;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();long tick=s.getTickCount();
        BlockPos b=null;var group=new ArrayList<ServerPlayer>();
        for(var p:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(p)&&inside(p)&&p.isAlive()){var pb=base(p);if(pb==null)continue;if(b==null)b=pb;if(pb.equals(b))group.add(p);}
        var t=set(s);
        if(b==null){
            if(!t.contains("Base"))return;
            int empty=t.getInt("Empty")+1;t.putInt("Empty",empty);if(empty<100){set(s,t);return;}
            var l=s.getLevel(NovelRooms.dimension(PLACE));var rb=BlockPos.of(t.getLong("Base"));
            if(l!=null&&l.isLoaded(rb)&&t.getBoolean("Printed"))StillnessSite.build(l,rb);
            set(s,new CompoundTag());return;
        }
        var l=group.get(0).serverLevel();t.putInt("Empty",0);t.putLong("Base",b.asLong());
        var noise=new HashMap<ServerPlayer,Double>();for(var q:group)noise.put(q,noise(q,tick));
        var done=done(t);
        switch(mode(t)){
            case WAITING,GONE->{
                for(var q:group)if(!done.contains(q.getStringUUID())&&StillnessSite.pastHalfway(rel(q,b))){enter(l,b,t,group);break;}
                if(mode(t)==GONE)for(var q:group){var own=own(q);if(own.getBoolean("Stood211")&&!own.getBoolean("Ready")&&StillnessSite.atFarDoor(rel(q,b))){
                    LiteraryVignettes.ready(q,PLACE,own,"let_it_breathe");save(q,own);cue(q,"At the far door, scratched into the dust in rows of five, a count. The hand is yours.");}}
            }
            case ENTERING->{int w=t.getInt("Wait")-1;t.putInt("Wait",w);if(w<=0){t.putInt("Mode",HUNTING);step(l,b,t,group,0,-1,TO_SOUND);}}
            case HUNTING->hunt(l,b,t,group,noise,done,tick);
            case OVER->over(l,b,t,group,noise,tick);
            case LEAVING->{
                if(step(l,b,t,group,StillnessSite.LAIR.x,StillnessSite.LAIR.z,AWAY)<.4){t.putInt("Mode",GONE);
                    for(var q:group)cue(q,"The hoofs go up to the far door, and through it, and the door never opens. Then nothing at all.");}
            }
            default->{}
        }
        set(s,t);
    }
    static void enter(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        t.putInt("Mode",ENTERING);t.putInt("Wait",TEST_OVER>0?2:ENTER);t.putDouble("X",StillnessSite.LAIR.x);t.putDouble("Z",StillnessSite.LAIR.z);t.putDouble("Stride",0);t.remove("HT");t.remove("Target");
        var door=world(b,StillnessSite.LAIR.add(0,1.5,0));
        l.playSound(null,door.x,door.y,door.z,SoundEvent.createVariableRangeEvent(Growl.Kind.NEAR.sound()),SoundSource.HOSTILE,2.6F,.8F);
        for(var q:group){cue(q,"From the far door, a growl. Then hoofs, on this side of it.");HousePackets.send(q,new NovelScenePayload(21,99,"",0,.45F));}
    }
    /**
     * One step toward a point, at a pace; every stride of a block and a half a hoofstep that shakes the floor, throws up the dust
     * and leaves a print in it. Returns how far it still has to go.
     */
    static double step(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,double tx,double tz,double pace){
        var from=at(t);double dx=tx-from.x,dz=tz-from.z,d=Math.hypot(dx,dz);
        if(d>1e-3){double m=Math.min(pace,d);double x=StillnessSite.clampX(from.x+dx/d*m),z=StillnessSite.clampZ(from.z+dz/d*m);t.putDouble("X",x);t.putDouble("Z",z);t.putFloat("Yaw",(float)(Math.atan2(-dx,dz)*180/Math.PI));
            double stride=t.getDouble("Stride")+m;
            if(stride>=1.5){stride=0;hoof(l,b,t,group,pace);}
            t.putDouble("Stride",stride);d-=m;}
        return d;
    }
    static void hoof(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,double pace){
        var r=at(t);var w=world(b,r);boolean left=t.getBoolean("Left");t.putBoolean("Left",!left);float yaw=t.getFloat("Yaw");
        double side=(left?-.45:.45),sx=Math.cos(Math.toRadians(yaw))*side,sz=Math.sin(Math.toRadians(yaw))*side;
        float volume=pace>=TO_SOUND?2.2F:1.4F;
        // walking off, the hoofs fade with the distance from the nearest reader
        if(mode(t)==LEAVING){double far=0;for(var q:group)far=Math.max(far,flat(rel(q,b),r));volume=(float)Math.max(.2,1.6-far/30);}
        l.playSound(null,w.x,w.y,w.z,FinaleRegistry.HOOF.get(),SoundSource.HOSTILE,volume,pace>=TO_SOUND?.62F:.5F);
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,StillnessSite.floor()),w.x+sx,w.y+.05,w.z+sz,14,.35,.02,.35,.05);
        if(StillnessSite.print(l,b,r.x+sx,r.z+sz,yaw))t.putBoolean("Printed",true);
        for(var q:group){double d=flat(rel(q,b),r);if(d<16)HousePackets.send(q,new NovelScenePayload(21,99,"",0,(float)Math.min(.7,(1-d/16)*(pace>=TO_SOUND?.8:.5))));}
    }
    static void hunt(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,Map<ServerPlayer,Double> noise,Set<String> done,long tick){
        var r=at(t);ServerPlayer heardBy=null;double best=0;
        for(var q:group){double n=noise.get(q),d=flat(rel(q,b),r);if(n>0&&d<=n&&n-d>best){best=n-d;heardBy=q;}}
        if(heardBy!=null){var hr=rel(heardBy,b);t.putDouble("HX",hr.x);t.putDouble("HZ",hr.z);t.putLong("HT",tick);t.putString("HBy",heardBy.getStringUUID());
            if(tick-t.getLong("Snort")>80){t.putLong("Snort",tick);var w=world(b,r.add(0,1.8,0));l.playSound(null,w.x,w.y,w.z,FinaleRegistry.BELLOW.get(),SoundSource.HOSTILE,.9F,.75F);}}
        boolean chasing=t.contains("HT")&&tick-t.getLong("HT")<100;
        if(chasing){
            if(step(l,b,t,group,t.getDouble("HX"),t.getDouble("HZ"),TO_SOUND)<.6){t.remove("HT");var w=world(b,at(t).add(0,1.4,0));l.playSound(null,w.x,w.y,w.z,HorrorRegistry.STILL_SNIFF.get(),SoundSource.HOSTILE,1.6F,1F);}
        }else{
            ServerPlayer near=null;double nd=1e9;for(var q:group){if(done.contains(q.getStringUUID()))continue;double d=flat(rel(q,b),r);if(d<nd){nd=d;near=q;}}
            if(near==null){t.putInt("Mode",LEAVING);for(var q:group)cue(q,"It stands a while. Then the hoofs turn back toward the far door.");return;}
            var nr=rel(near,b);step(l,b,t,group,nr.x,nr.z,TO_SMELL);
            if(tick%50==0){var w=world(b,at(t).add(0,1.4,0));l.playSound(null,w.x,w.y,w.z,HorrorRegistry.STILL_SNIFF.get(),SoundSource.HOSTILE,1.2F,.9F);}
        }
        r=at(t);String by=t.getString("HBy");ServerPlayer reach=null;double rd=REACH;
        for(var q:group){double d=flat(rel(q,b),r);if(d<rd&&(!done.contains(q.getStringUUID())||chasing&&by.equals(q.getStringUUID()))){rd=d;reach=q;}}
        if(reach!=null)begin(l,b,t,group,reach,tick);
    }
    /** It reaches a reader and stands over them. */
    static void begin(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,ServerPlayer q,long tick){
        var rng=q.getRandom();t.putInt("Mode",OVER);t.putString("Target",q.getStringUUID());t.putInt("OT",0);
        t.putInt("Len",TEST_OVER>0?TEST_OVER:400+rng.nextInt(401));t.putInt("Breaths",0);t.putInt("Pushes",0);t.putInt("NextNudge",70+rng.nextInt(40));t.putLong("Quiet",0);t.remove("HT");
        var qr=rel(q,b);float face=q.getYRot();double fx=-Math.sin(Math.toRadians(face))*.8,fz=Math.cos(Math.toRadians(face))*.8;
        t.putDouble("X",StillnessSite.clampX(qr.x+fx));t.putDouble("Z",StillnessSite.clampZ(qr.z+fz));t.putFloat("Yaw",face+180);
        t.putDouble("AX",qr.x);t.putDouble("AZ",qr.z);
        StillnessSite.print(l,b,qr.x+fx-.4,qr.z+fz,face+180);StillnessSite.print(l,b,qr.x+fx+.4,qr.z+fz,face+180);t.putBoolean("Printed",true);
        var w=world(b,at(t).add(0,1.6,0));l.playSound(null,w.x,w.y,w.z,FinaleRegistry.HOOF.get(),SoundSource.HOSTILE,2.4F,.45F);
        cue(q,"It is right in front of you. Don't move.");
        for(var o:group)if(o!=q)cue(o,"The hoofs stop, by "+q.getGameProfile().getName()+".");
    }
    static void over(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,Map<ServerPlayer,Double> noise,long tick){
        ServerPlayer q=null;for(var o:group)if(o.getStringUUID().equals(t.getString("Target")))q=o;
        if(q==null){t.putInt("Mode",HUNTING);t.remove("Target");return;}
        int ot=t.getInt("OT")+1;t.putInt("OT",ot);var qr=rel(q,b);var r=at(t);
        // anyone else's noise draws it off them
        for(var o:group){if(o==q)continue;double n=noise.get(o),d=flat(rel(o,b),r);
            if(n>0&&d<=n){var or=rel(o,b);t.putDouble("HX",or.x);t.putDouble("HZ",or.z);t.putLong("HT",tick);t.putString("HBy",o.getStringUUID());t.putInt("Mode",HUNTING);t.remove("Target");
                var own=own(q);own.putBoolean("Stood211",true);own.putInt("Breaths211",t.getInt("Breaths"));own.putInt("Pushes211",t.getInt("Pushes"));own.putString("Drawn211",o.getGameProfile().getName());save(q,own);done(t,q);
                var oo=own(o);oo.putString("Drew211",q.getGameProfile().getName());save(o,oo);
                cue(q,"It lifts its head toward a sound down the hall, and goes. "+o.getGameProfile().getName()+" drew it off you.");
                cue(o,"It heard you. It is coming. Stand still.");
                HousePackets.send(q,new NovelScenePayload(21,99,"",0,0F));return;}}
        // the reader moving, or making any loud noise, under it
        boolean quiet=tick<t.getLong("Quiet");
        if(ot==REACH_GRACE||tick==t.getLong("Quiet")){t.putDouble("AX",qr.x);t.putDouble("AZ",qr.z);}
        if(ot>REACH_GRACE&&!quiet){boolean moved=Math.hypot(qr.x-t.getDouble("AX"),qr.z-t.getDouble("AZ"))>MOVED;
            if(moved||noise.get(q)>=JUMP){gore(l,b,t,group,q);return;}}
        // its breath
        if(ot>=20&&(ot-20)%BREATH==0){int n=t.getInt("Breaths")+1;t.putInt("Breaths",n);t.putLong("Breath",tick);
            var head=world(b,new Vec3((qr.x+r.x)/2,1.9,(qr.z+r.z)/2));l.playSound(null,head.x,head.y,head.z,HorrorRegistry.STILL_BREATH.get(),SoundSource.HOSTILE,1.4F,.85F+q.getRandom().nextFloat()*.1F);
            cue(q,n==1?"It breathes out, right over you. Hot, wet, and it smells of iron.":StillnessTexts.capital(words(n))+".");}
        // its head against them
        if(ot>=t.getInt("NextNudge")){int k=t.getInt("Pushes")+1;t.putInt("Pushes",k);t.putInt("NextNudge",ot+90+q.getRandom().nextInt(50));t.putLong("Quiet",tick+NUDGE_QUIET);
            double ang=q.getRandom().nextDouble()*Math.PI*2;q.setDeltaMovement(q.getDeltaMovement().add(Math.cos(ang)*.32,.05,Math.sin(ang)*.32));q.hurtMarked=true;q.connection.send(new ClientboundSetEntityMotionPacket(q));
            var w=world(b,r.add(0,1.2,0));l.playSound(null,w.x,w.y,w.z,HorrorRegistry.STILL_NUDGE.get(),SoundSource.HOSTILE,1.5F,1F);
            cue(q,k==1?"It pushes at you with its head, to see if you will move.":"It pushes at you again.");}
        // their heart, and the fog of its breath
        int len=t.getInt("Len");float fear=Math.min(1,(float)ot/len);
        int beat=Math.max(7,(int)(24-16*fear));if(ot%beat==0)heard(q,q.getEyePosition(),HorrorRegistry.STORE_HEART.get(),.6F+.5F*fear,1F);
        if(ot%5==0)HousePackets.send(q,new NovelScenePayload(21,(int)Math.min(99,tick-t.getLong("Breath")),"",0,.12F+.62F*fear));
        if(ot>=len){var own=own(q);own.putBoolean("Stood211",true);own.putInt("Breaths211",t.getInt("Breaths"));own.putInt("Pushes211",t.getInt("Pushes"));save(q,own);done(t,q);
            t.putInt("Mode",HUNTING);t.remove("Target");HousePackets.send(q,new NovelScenePayload(21,99,"",0,0F));
            cue(q,"It turns away. The hoofs go off down the hall.");}
    }
    /** Moving under it: the horns. A raised shield takes it once, and cracks. A survivor is thrown back out the way they came. */
    static void gore(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,ServerPlayer q){
        t.putInt("Mode",HUNTING);t.remove("Target");t.remove("HT");
        var w=world(b,at(t).add(0,1.4,0));l.playSound(null,w.x,w.y,w.z,HorrorRegistry.STILL_GORE.get(),SoundSource.HOSTILE,2F,1F);l.playSound(null,w.x,w.y,w.z,FinaleRegistry.BELLOW.get(),SoundSource.HOSTILE,2.2F,.85F);
        var own=own(q);own.putInt("Gored211",own.getInt("Gored211")+1);own.remove("Stood211");save(q,own);
        HousePackets.send(q,new NovelScenePayload(21,99,"",0,1F));
        boolean shielded=q.isUsingItem()&&q.getUseItem().getItem() instanceof ShieldItem;
        q.invulnerableTime=0;
        if(shielded){var hand=q.getUsedItemHand();var shield=q.getUseItem();
            boolean broke=MinotaurEntity.wearShield(l,q,shield,hand==net.minecraft.world.InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
            q.stopUsingItem();if(!broke&&!shield.isEmpty())q.getCooldowns().addCooldown(shield.getItem(),40);
            q.hurt(goring(l),BLOCKED);cue(q,"You moved. The horns hit the shield; the shield takes it, and cracks.");}
        else q.hurt(goring(l),GORE);
        if(q.isAlive()){var away=rel(q,b).subtract(at(t));double d=Math.max(.1,Math.hypot(away.x,away.z));q.setDeltaMovement(away.x/d*1.1,.35,away.z/d*1.1);q.hurtMarked=true;q.connection.send(new ClientboundSetEntityMotionPacket(q));
            LabyrinthDoors.sendBack(q,2,20,20);}
        for(var o:group)if(o!=q)cue(o,q.getGameProfile().getName()+" moved.");
    }
    public static boolean locked(ServerPlayer p){if(!inside(p))return false;var b=base(p);if(b==null)return false;return present(mode(set(p.server)));}
    /** Test hook: changes the shared state directly. */
    public static void edit(MinecraftServer s,java.util.function.Consumer<CompoundTag> change){var t=set(s);change.accept(t);set(s,t);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){LAST.remove(e.getEntity().getUUID());PULSE.remove(e.getEntity().getUUID());}
    public static void clearAll(){LAST.clear();PULSE.clear();TEST_OVER=0;}
}
