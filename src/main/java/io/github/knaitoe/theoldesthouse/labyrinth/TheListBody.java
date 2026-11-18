package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.TheListBodyPayload;
import io.github.knaitoe.theoldesthouse.network.TheListMarksPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 0.4.705, The list, part 2 (docs/THE_LIST_0_4_705.md): the slasher's body, tried on its own before any night uses it. It is a reader's
 * own, kept in their record ({@code Body}) and worn only while they are in the town; outside it, nothing of it is left on them.
 * <ul>
 * <li><b>The walk.</b> The reader never sprints, and walks at the pace the game gives a sprint, so nobody still in reach gets ahead.</li>
 * <li><b>Doors give.</b> A locked house door struck three times (a hand or a blow, at least {@link #STRIKE_GAP} ticks apart) splinters and
 * swings open. From then on it is a broken door, which anyone opens and shuts by hand.</li>
 * <li><b>Hearts.</b> Every heart in the town near enough is heard where it beats, by the slasher alone. The five on the list are heard
 * further and louder, the nearest of them louder still; a heart that has seen the body races.</li>
 * <li><b>The get-up.</b> In the town the body never drops below a heart. A blow that would kill puts it down where it is for five seconds,
 * still, and then it gets up there. Not a House death; nothing is lost.</li>
 * <li><b>The mask.</b> A feed sack, worn by default. Taken off, anyone in town who looks at the reader from near enough has seen their face,
 * and the record keeps who.</li>
 * <li><b>The pull.</b> Toward the nearest name, drawn at the edges of the reader's own screen and heard as that name's heart. Light and
 * looking away resist it. It never moves the reader or their camera.</li>
 * <li><b>The tread.</b> Everyone else in town hears the slasher's steps.</li>
 * </ul>
 * Spectators are never given it and hear nothing of it.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TheListBody {
    private TheListBody(){}
    private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,path);}
    public static final ResourceLocation WALK_ID=id("list_walk"),STILL_ID=id("list_still"),STILL_JUMP_ID=id("list_still_jump");
    /** 0.4.709: holding one of the five, the body stands where it is (TheListHunt). */
    public static final ResourceLocation HOLD_ID=id("list_hold"),HOLD_JUMP_ID=id("list_hold_jump");
    /** The game's sprint adds three tenths to a walk. The body walks at that pace all the time, and never sprints. */
    public static final double WALK=.3;
    /** Down for five seconds; in the town the body keeps a heart. */
    public static final int DOWN=100;
    public static final float FLOOR=2F;
    /** A locked door takes three blows, each at least this far apart; blows are forgotten after this long. */
    public static final int STRIKES=3,STRIKE_GAP=8,STRIKE_FORGET=200;
    /** 0.4.708: a bolted steel door takes five. */
    public static final int STEEL_STRIKES=5;
    /** How far a name's heart is heard, and anyone else's; how far a name pulls; how far one in town sees the body, and how near to see its face. */
    public static final double NAME_HEART=64,HEART=40,PULL=96,SEES=24,FACE=12;
    /** A heart's beat, calm, racing and asleep, in ticks; a heart races this long after it has seen the body. */
    public static final int CALM=18,RACING=9,SLEEPING=24,RACE=200;
    /** A step of the tread every this many blocks walked. */
    public static final double STRIDE=1.5;
    /** The five on the list. */
    public static final int[] LISTED={TheListPeople.DEEK,TheListPeople.MEL,TheListPeople.LESLEY,TheListPeople.SHANNON,TheListPeople.WES};
    public static boolean listed(int who){for(int w:LISTED)if(w==who)return true;return false;}

    /** What the body is doing, for each reader wearing it now; never saved. */
    static final class Live{
        int down,tick;@Nullable Vec3 last;double walked;float pullYaw,pull;@Nullable UUID pulled;
        /** 0.4.706: lying where night one's cut left them, not knocked down: the get-up says the body's own line, not the get-up's. */
        boolean cameTo;
        final Map<UUID,Long> beat=new HashMap<>(),raced=new HashMap<>();
        List<LiteraryActor> people=List.of();
    }
    static final Map<UUID,Live> LIVE=new HashMap<>();
    /** Each locked door's blows so far, by where its lower half stands: how many, and when the last was. */
    static final Map<Long,long[]> BLOWS=new HashMap<>();

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),TheList.PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),TheList.PLACE,own);}
    /** Whether the body is this reader's. */
    public static boolean has(ServerPlayer p){return own(p).getBoolean("Body");}
    /** The town the reader stands in, or null. */
    public static @Nullable BlockPos town(ServerPlayer p){
        if(!p.level().dimension().equals(NovelRooms.dimension(TheList.PLACE)))return null;
        var b=LiteraryVignettes.base(p,TheList.PLACE);
        return b!=null&&TheListSite.inScene(p.position().subtract(Vec3.atLowerCornerOf(b)))?b:null;
    }
    /** Whether a body walks the body's walk now. */
    public static boolean walking(Player p){var a=p.getAttribute(Attributes.MOVEMENT_SPEED);return a!=null&&a.hasModifier(WALK_ID);}
    /** Ticks left lying where they fell; 0 when up, or without the body. */
    public static int down(ServerPlayer p){var live=LIVE.get(p.getUUID());return live==null?0:live.down;}
    /** 0.4.709: whether the body is up and about on the reader (it is made on the tick after it is given). */
    public static boolean live(ServerPlayer p){return LIVE.containsKey(p.getUUID());}
    /** Toward which name the pull draws now, and how hard (0 to 1). */
    public static @Nullable UUID pulled(ServerPlayer p){var live=LIVE.get(p.getUUID());return live==null?null:live.pulled;}
    public static float pull(ServerPlayer p){var live=LIVE.get(p.getUUID());return live==null?0:live.pull;}
    /** Whether the heart of one in town races for this slasher now (they have seen the body lately). */
    public static boolean racing(ServerPlayer p,UUID heart){var live=LIVE.get(p.getUUID());return live!=null&&live.raced.getOrDefault(heart,0L)>p.level().getGameTime();}
    /** The blows a locked door has taken so far, by where its lower half stands. */
    public static int blows(BlockPos lower){var b=BLOWS.get(lower.asLong());return b==null?0:(int)b[0];}
    static AABB bounds(BlockPos b){return new AABB(b.getX()+TheListSite.WEST,b.getY()-6,b.getZ()+TheListSite.NORTH,b.getX()+TheListSite.EAST+1,b.getY()+40,b.getZ()+TheListSite.SOUTH+1);}

    /** The body given to a reader in the town, kept in their record for the nights to come; the sack goes on over their head. */
    public static boolean give(ServerPlayer p){
        if(town(p)==null||p.isSpectator()||!p.isAlive())return false;
        var own=own(p);
        if(!own.getBoolean("Body")){own.putBoolean("Body",true);if(!hasSack(p))own.putInt("Sack",3);save(p,own);TheList.cue(p,TheListTexts.BODY);}
        return true;
    }
    /**
     * 0.4.706, night one's cut (TheListNight): the body is the reader's from here, the sack over their head, and they lie face down where
     * they have been set for so many ticks before they get up with the hearts. Nothing is said as they go down; nothing is taken.
     */
    public static void comeTo(ServerPlayer p,int ticks){
        var own=own(p);
        if(!own.getBoolean("Body")){own.putBoolean("Body",true);if(!hasSack(p))own.putInt("Sack",3);save(p,own);}
        var live=LIVE.computeIfAbsent(p.getUUID(),k->new Live());
        live.cameTo=true;live.down=Math.max(1,ticks);still(p);
        PacketDistributor.sendToPlayer(p,new TheListBodyPayload(true,live.down,live.pullYaw,live.pull));
    }
    /** The body taken back: nothing of it, the sack included, is left on them. */
    public static void end(ServerPlayer p){var own=own(p);own.remove("Body");own.remove("Sack");own.remove("Hook");own.remove("Blood");own.remove("SackMarks");save(p,own);strip(p,false);}

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var level=server.getLevel(NovelRooms.dimension(TheList.PLACE));
        var held=new HashSet<UUID>();
        if(level!=null)for(var p:level.players()){
            if(!p.isAlive()||p.isSpectator())continue;
            var b=town(p);if(b==null||!has(p))continue;
            held.add(p.getUUID());hold(p,b,level,LIVE.computeIfAbsent(p.getUUID(),k->new Live()));
        }
        if(!LIVE.isEmpty()||level!=null)for(var p:server.getPlayerList().getPlayers())
            if(!held.contains(p.getUUID())&&(LIVE.containsKey(p.getUUID())||walking(p)))strip(p,has(p));
        LIVE.keySet().removeIf(id->server.getPlayerList().getPlayer(id)==null);
        if(!BLOWS.isEmpty()&&level!=null){long now=level.getGameTime();BLOWS.values().removeIf(b->now-b[1]>STRIKE_FORGET);}
    }

    /** One tick of the body on a reader in the town. */
    static void hold(ServerPlayer p,BlockPos b,ServerLevel l,Live live){
        long now=l.getGameTime();live.tick++;
        var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);
        if(speed!=null&&!speed.hasModifier(WALK_ID))speed.addTransientModifier(new AttributeModifier(WALK_ID,WALK,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if(p.isSprinting())p.setSprinting(false);
        sack(p);
        if(live.down>0){
            p.setForcedPose(Pose.SWIMMING);var v=p.getDeltaMovement();p.setDeltaMovement(0,Math.min(0,v.y),0);
            if(--live.down==0)up(p,live);
        }else tread(p,l,live);
        if(live.tick%20==1)live.people=l.getEntitiesOfClass(LiteraryActor.class,bounds(b),a->TheListPeople.is(a)&&a.isAlive());
        if(live.tick%20==3)hook(p);
        // 0.4.710: the blood on them, to every viewer now and then, so that one who comes into town later sees it too.
        if(live.tick%40==5&&blood(p)>0)shown(p,blood(p));
        if(live.tick%10==2)seen(p,l,live,now);
        if(live.tick%5==0)pull(p,l,live);
        hearts(p,l,live,now);
    }

    /** Nothing of the body left on a reader who has left the town (or whose body is gone): the walk, the stillness, the sack. */
    static void strip(ServerPlayer p,boolean keep){
        var live=LIVE.remove(p.getUUID());
        var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);if(speed!=null){speed.removeModifier(WALK_ID);speed.removeModifier(STILL_ID);}
        var jump=p.getAttribute(Attributes.JUMP_STRENGTH);if(jump!=null)jump.removeModifier(STILL_JUMP_ID);
        holding(p,false);takeHooks(p);
        if(live!=null&&live.down>0&&p.getForcedPose()==Pose.SWIMMING)p.setForcedPose(null);
        // The sack stays in the town: on the back step, and put back on the reader when they come back, as it was (0.4.710).
        int marks=sackMarks(p);
        int was=takeSacks(p);
        if(keep&&was>0){var own=own(p);own.putInt("Sack",was);if(marks!=0)own.putInt("SackMarks",marks);save(p,own);TheList.cue(p,TheListTexts.SACK_LEFT);}
        if(live!=null){PacketDistributor.sendToPlayer(p,new TheListBodyPayload(false,0,0,0));shown(p,0);}
    }
    static boolean hasSack(ServerPlayer p){
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(TheListSack.is(inv.getItem(i)))return true;
        return TheListSack.is(p.containerMenu.getCarried());
    }
    /** Every sack taken from the reader: 1 if one was on their head, 2 if one was only carried, 0 if they had none. */
    static int takeSacks(ServerPlayer p){
        int was=0;
        if(TheListSack.worn(p)){p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);was=1;}
        var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++)if(TheListSack.is(inv.getItem(i))){inv.setItem(i,ItemStack.EMPTY);if(was==0)was=2;}
        if(TheListSack.is(p.containerMenu.getCarried())){p.containerMenu.setCarried(ItemStack.EMPTY);if(was==0)was=2;}
        return was;
    }
    /** The sack the record owes the reader, back on them: on their head if it was worn when they left (or is new), else carried. */
    static void sack(ServerPlayer p){
        var own=own(p);int left=own.getInt("Sack");if(left==0)return;
        own.remove("Sack");save(p,own);
        if(hasSack(p))return;
        var sack=TheListSack.made(p.getUUID());
        // 0.4.710: as it was left: torn, dirt in it, blood on it.
        if(own.contains("SackMarks")){TheListSack.marks(sack,own.getInt("SackMarks"));own.remove("SackMarks");save(p,own);}
        boolean head=left!=2&&p.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        if(head)p.setItemSlot(EquipmentSlot.HEAD,sack);else if(!p.getInventory().add(sack))p.drop(sack,false);
        p.serverLevel().playSound(null,p.getX(),p.getY()+1.5,p.getZ(),TheListRegistry.SACK_SOUND.get(),SoundSource.PLAYERS,.8F,1F);
        if(left!=3)TheList.cue(p,head?TheListTexts.SACK_WORN:TheListTexts.SACK_CARRIED);
    }
    /** A sack let go of in the town lies where it fell until it is picked up; it does not rot away. 0.4.709: the hay hook is not let go
     * of; thrown, it is back in the slasher's hands within the second (hook). */
    @SubscribeEvent public static void tossed(ItemTossEvent e){
        if(TheListSack.is(e.getEntity().getItem()))e.getEntity().setUnlimitedLifetime();
        else if(e.getEntity().getItem().is(TheListRegistry.HAY_HOOK.get())&&e.getPlayer() instanceof ServerPlayer p&&has(p))e.setCanceled(true);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.709: the hay hook and the grip
    /** The hay hook given with the body: in the reader's hands, and kept in their record for the nights to come. */
    public static void giveHook(ServerPlayer p){var own=own(p);if(own.getInt("Hook")==0){own.putInt("Hook",1);save(p,own);}hook(p);}
    /** Whether the reader has the hay hook in their main hand. */
    public static boolean hookInHand(ServerPlayer p){return p.getMainHandItem().is(TheListRegistry.HAY_HOOK.get());}
    /** Whether it has been used: the record's word, 2. */
    public static boolean bloodied(ServerPlayer p){return own(p).getInt("Hook")==2;}
    static boolean hasHook(ServerPlayer p){
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(TheListRegistry.HAY_HOOK.get()))return true;
        return p.containerMenu.getCarried().is(TheListRegistry.HAY_HOOK.get());
    }
    /** The hook the record owes the reader, back in their hands if they have none: clean, or bloodied. */
    static void hook(ServerPlayer p){
        int owed=own(p).getInt("Hook");if(owed==0||hasHook(p))return;
        var stack=new ItemStack(TheListRegistry.HAY_HOOK.get());if(owed==2)stack.set(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA,new net.minecraft.world.item.component.CustomModelData(1));
        p.getInventory().add(stack);
    }
    /** Every hook taken from the reader: none leaves the town. */
    static void takeHooks(ServerPlayer p){
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(TheListRegistry.HAY_HOOK.get()))inv.setItem(i,ItemStack.EMPTY);
        if(p.containerMenu.getCarried().is(TheListRegistry.HAY_HOOK.get()))p.containerMenu.setCarried(ItemStack.EMPTY);
    }
    /** The hook used: the one in hand bloodied, and the record keeps it so. */
    public static void bloody(ServerPlayer p){
        var own=own(p);own.putInt("Hook",2);save(p,own);
        var held=p.getMainHandItem();if(held.is(TheListRegistry.HAY_HOOK.get()))held.set(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA,new net.minecraft.world.item.component.CustomModelData(1));
    }
    /** One of the five killed: the reader's record keeps who (Kills, a bit for each). */
    public static void killed(ServerPlayer p,int who){var own=own(p);own.putInt("Kills",own.getInt("Kills")|1<<who);save(p,own);}
    /** 0.4.710: the same for one who may have logged off since the hook went in. */
    public static void killed(net.minecraft.server.MinecraftServer server,UUID reader,int who){
        var d=LabyrinthData.get(server);var own=LiteraryVignettes.personal(d,reader,TheList.PLACE);
        own.putInt("Kills",own.getInt("Kills")|1<<who);LiteraryVignettes.save(d,reader,TheList.PLACE,own);
    }

    // ------------------------------------------------------------------------------------------------ 0.4.710: what gets on the body
    /** How much blood is down the front of the reader and their right sleeve: 0 to 3, the record's Blood. */
    public static int blood(ServerPlayer p){return own(p).getInt("Blood");}
    /** The hook in, or out: more blood down the front and the sleeve, and on the sack if it is on the head. Every viewer sees it. */
    public static void bleed(ServerPlayer p){
        var own=own(p);int b=Math.min(3,own.getInt("Blood")+1);own.putInt("Blood",b);save(p,own);
        if(TheListSack.worn(p))TheListSack.bleed(p.getItemBySlot(EquipmentSlot.HEAD));
        shown(p,b);
    }
    /** The sack on the head torn (Mel's camcorder), or with dirt in its weave (Lesley's fistful): nothing if the sack is off. */
    public static void mark(ServerPlayer p,int mark){
        if(!TheListSack.worn(p))return;var sack=p.getItemBySlot(EquipmentSlot.HEAD);TheListSack.marks(sack,TheListSack.marks(sack)|mark);
    }
    /** The marks on whichever sack the reader has: the one on their head, else one carried. */
    static int sackMarks(ServerPlayer p){
        if(TheListSack.worn(p))return TheListSack.marks(p.getItemBySlot(EquipmentSlot.HEAD));
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(TheListSack.is(inv.getItem(i)))return TheListSack.marks(inv.getItem(i));
        return 0;
    }
    /** The blood on a reader as every viewer draws it (0 to take it off). */
    static void shown(ServerPlayer p,int blood){var payload=new TheListMarksPayload(p.getUUID(),blood);for(var q:p.server.getPlayerList().getPlayers())HousePackets.send(q,payload);}
    /** 0.4.710: one who is not the slasher came across one of the five dead (Found, a bit for each). */
    public static void found(ServerPlayer p,int who){var own=own(p);own.putInt("Found",own.getInt("Found")|1<<who);save(p,own);}
    public static int found(ServerPlayer p){return own(p).getInt("Found");}
    public static int kills(ServerPlayer p){return own(p).getInt("Kills");}
    /** Holding one of the five, the body stands where it is: no walking, no jumping. Let go, it walks again. */
    public static void holding(ServerPlayer p,boolean on){
        var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);var jump=p.getAttribute(Attributes.JUMP_STRENGTH);
        if(on){
            if(speed!=null&&!speed.hasModifier(HOLD_ID))speed.addTransientModifier(new AttributeModifier(HOLD_ID,-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            if(jump!=null&&!jump.hasModifier(HOLD_JUMP_ID))jump.addTransientModifier(new AttributeModifier(HOLD_JUMP_ID,-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }else{if(speed!=null)speed.removeModifier(HOLD_ID);if(jump!=null)jump.removeModifier(HOLD_JUMP_ID);}
    }

    // ------------------------------------------------------------------------------------------------ hearts
    /** Each heart near enough, beating where it is, to the slasher alone. */
    static void hearts(ServerPlayer p,ServerLevel l,Live live,long now){
        var heard=new HashSet<UUID>();
        for(var a:live.people){
            if(!a.isAlive()||TheListHunt.gone(a))continue;boolean name=listed(TheListPeople.who(a));
            double reach=name?NAME_HEART:HEART;if(a.distanceToSqr(p)>reach*reach)continue;
            beat(p,a,name,TheListPeople.asleep(a.phase()),live,now,heard);
        }
        for(var other:l.players()){
            if(other==p||other.isSpectator()||!other.isAlive()||other.distanceToSqr(p)>HEART*HEART)continue;
            beat(p,other,false,other.isSleeping(),live,now,heard);
        }
        live.beat.keySet().retainAll(heard);
    }
    static void beat(ServerPlayer p,Entity e,boolean name,boolean asleep,Live live,long now,Set<UUID> heard){
        var id=e.getUUID();heard.add(id);
        Long next=live.beat.get(id);
        if(next==null){live.beat.put(id,now+Math.floorMod(id.hashCode(),CALM));return;}
        if(now<next)return;
        boolean racing=!asleep&&(live.raced.getOrDefault(id,0L)>now||TheListHunt.scared(e));
        live.beat.put(id,now+(asleep?SLEEPING:racing?RACING:CALM)+p.getRandom().nextInt(3)-1);
        float volume=name?3F:1.6F;if(id.equals(live.pulled))volume*=1.4F;
        float pitch=(racing?1.22F:asleep?.88F:1F)+(p.getRandom().nextFloat()-.5F)*.04F;
        var at=e.getEyePosition().add(0,-.35,0);
        p.connection.send(new ClientboundSoundPacket(TheListRegistry.HEART,SoundSource.PLAYERS,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));
    }
    /** Whether a body facing this way (its yaw) from one point has the other within so many degrees of straight ahead. */
    static boolean facing(float yaw,Vec3 from,Vec3 to,double within){
        var d=to.subtract(from);return Math.abs(Mth.wrapDegrees(Math.toDegrees(Math.atan2(-d.x,d.z))-yaw))<=within;
    }
    /**
     * Who in town has the body in sight: their heart races. With the sack off, those near enough have seen the reader's face; the record
     * keeps who, and the reader is told once for each.
     */
    static void seen(ServerPlayer p,ServerLevel l,Live live,long now){
        boolean bare=!TheListSack.worn(p);var own=bare?own(p):null;int faced=bare?own.getInt("Faced"):0;boolean changed=false;
        for(var a:live.people){
            if(!a.isAlive()||TheListPeople.asleep(a.phase())||TheListHunt.gone(a))continue;
            double d=a.distanceTo(p);
            if(d>SEES||!facing(a.getYHeadRot(),a.getEyePosition(),p.getEyePosition(),70)||!a.hasLineOfSight(p))continue;
            live.raced.put(a.getUUID(),now+RACE);
            int who=TheListPeople.who(a);var name=TheListTexts.NAMES[who];
            if(bare&&d<=FACE&&name!=null&&(faced&1<<who)==0){faced|=1<<who;changed=true;TheList.cue(p,String.format(TheListTexts.FACE,name));}
        }
        for(var other:l.players()){
            if(other==p||other.isSpectator()||!other.isAlive()||other.distanceTo(p)>SEES)continue;
            if(facing(other.getYHeadRot(),other.getEyePosition(),p.getEyePosition(),35)&&other.hasLineOfSight(p))live.raced.put(other.getUUID(),now+RACE);
        }
        live.raced.values().removeIf(t->t<=now);
        if(changed){own.putInt("Faced",faced);save(p,own);}
    }

    // ------------------------------------------------------------------------------------------------ the pull
    /** The nearest name, and how hard they draw the reader now, sent to the reader's own screen. */
    static void pull(ServerPlayer p,ServerLevel l,Live live){
        LiteraryActor near=null;double best=PULL;
        for(var a:live.people){if(!a.isAlive()||!listed(TheListPeople.who(a))||TheListHunt.gone(a))continue;double d=a.distanceTo(p);if(d<best){best=d;near=a;}}
        float strength=0,yaw=0;
        if(near!=null){
            var to=near.position().subtract(p.position());yaw=(float)Math.toDegrees(Math.atan2(-to.x,to.z));
            strength=(float)(1-best/PULL);
            if(lit(p,l))strength*=.35F;
            if(Math.abs(Mth.wrapDegrees(yaw-p.getYHeadRot()))>110)strength*=.5F;
        }
        live.pulled=near==null?null:near.getUUID();live.pullYaw=yaw;live.pull=strength;
        PacketDistributor.sendToPlayer(p,new TheListBodyPayload(true,live.down,yaw,strength));
    }
    /** Standing in light: a street lamp, a porch light, a lit window. */
    public static boolean lit(ServerPlayer p,ServerLevel l){return l.getBrightness(LightLayer.BLOCK,BlockPos.containing(p.getEyePosition()))>=10;}

    // ------------------------------------------------------------------------------------------------ the tread
    static void tread(ServerPlayer p,ServerLevel l,Live live){
        var here=p.position();
        // 0.4.709: creeping, crouched, the body's tread is not heard (and the five hear it only from a block away: TheListHunt).
        if(live.last!=null&&p.onGround()&&!p.isCrouching()){
            double step=Math.hypot(here.x-live.last.x,here.z-live.last.z);
            if(step<2)live.walked+=step;
            if(live.walked>=STRIDE){live.walked=0;l.playSound(p,here.x,here.y,here.z,TheListRegistry.TREAD.get(),SoundSource.PLAYERS,.9F,.9F+p.getRandom().nextFloat()*.15F);}
        }
        live.last=here;
    }

    // ------------------------------------------------------------------------------------------------ down and up
    static void down(ServerPlayer p,Live live){down(p,live,TheListTexts.DOWN);}
    static void down(ServerPlayer p,Live live,String cue){
        live.down=DOWN;live.cameTo=false;
        still(p);
        p.serverLevel().playSound(null,p.getX(),p.getY(),p.getZ(),TheListRegistry.DOWN.get(),SoundSource.PLAYERS,1F,.95F+p.getRandom().nextFloat()*.1F);
        TheList.cue(p,cue);
        PacketDistributor.sendToPlayer(p,new TheListBodyPayload(true,live.down,live.pullYaw,live.pull));
    }
    /** 0.4.708: put down by one of the five fighting back (TheListHunt): knocked back from where they stood, then the five seconds of a
     * fall, with what they did as its cue. Nothing if the body is down already. */
    public static void felled(ServerPlayer p,String cue,Vec3 from){
        var live=LIVE.get(p.getUUID());if(live==null||live.down>0)return;
        p.knockback(.6,from.x-p.getX(),from.z-p.getZ());p.hurtMarked=true;
        down(p,live,cue);
    }
    /** Face down and still: no walking, no jumping. */
    static void still(ServerPlayer p){
        var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);
        if(speed!=null&&!speed.hasModifier(STILL_ID))speed.addTransientModifier(new AttributeModifier(STILL_ID,-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        var jump=p.getAttribute(Attributes.JUMP_STRENGTH);
        if(jump!=null&&!jump.hasModifier(STILL_JUMP_ID))jump.addTransientModifier(new AttributeModifier(STILL_JUMP_ID,-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        p.setSprinting(false);p.setForcedPose(Pose.SWIMMING);p.setPose(Pose.SWIMMING);
    }
    static void up(ServerPlayer p,Live live){
        live.down=0;
        var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);if(speed!=null)speed.removeModifier(STILL_ID);
        var jump=p.getAttribute(Attributes.JUMP_STRENGTH);if(jump!=null)jump.removeModifier(STILL_JUMP_ID);
        p.setForcedPose(null);
        TheList.cue(p,live.cameTo?TheListTexts.BODY:TheListTexts.UP);live.cameTo=false;
        PacketDistributor.sendToPlayer(p,new TheListBodyPayload(true,0,live.pullYaw,live.pull));
    }
    static boolean spared(DamageSource source){return !source.is(DamageTypes.GENERIC_KILL)&&!source.is(DamageTypes.FELL_OUT_OF_WORLD);}
    /** In the town the body keeps a heart: a blow that would take more takes what is above it, and a blow that would kill puts it down. */
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void hurt(LivingDamageEvent.Pre e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var live=LIVE.get(p.getUUID());
        if(live==null||!spared(e.getSource()))return;
        if(live.down>0){e.setNewDamage(0);return;}
        float health=p.getHealth(),damage=e.getNewDamage();
        if(health-damage>=FLOOR)return;
        e.setNewDamage(Math.max(0,health-FLOOR));
        if(damage>=health)down(p,live);
    }
    /** Whatever got past that is not a death either. */
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void dying(LivingDeathEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var live=LIVE.get(p.getUUID());
        if(live==null||!spared(e.getSource()))return;
        e.setCanceled(true);p.setHealth(FLOOR);if(live.down==0)down(p,live);
    }
    /** Lying there, nothing moves the body. */
    @SubscribeEvent public static void knocked(LivingKnockBackEvent e){if(e.getEntity() instanceof ServerPlayer p&&down(p)>0)e.setCanceled(true);}

    // ------------------------------------------------------------------------------------------------ doors
    /**
     * A hand or a blow on a locked house door of the town, by a reader with the body: the door is struck. True when it was (a blow too soon
     * after the last is taken up and does nothing). The reader's own house, its front door and the House's way on are not the town's to lock.
     */
    public static boolean strike(ServerPlayer p,BlockPos b,BlockPos rel){
        var live=LIVE.get(p.getUUID());if(live==null||live.down>0)return false;
        var l=p.serverLevel();var st=l.getBlockState(b.offset(rel));
        // 0.4.708: a steel door too, once one of the five has bolted it behind them (TheListHunt); it takes five blows.
        boolean steel=st.is(TheListRegistry.STEEL_DOOR.get());
        if(!st.is(TheListRegistry.HOUSE_DOOR.get())&&!steel)return false;
        var lower=st.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?rel.below():rel;
        if(steel&&!TheListHunt.bolted(b,lower))return false;
        if(TheListSite.inHouse(Vec3.atCenterOf(lower))||lower.equals(TheListSite.FRONT_DOOR)||lower.equals(TheListSite.ONWARD))return false;
        var at=b.offset(lower);var door=l.getBlockState(at);
        if(!door.is(st.getBlock())||door.getValue(DoorBlock.OPEN))return false;
        long now=l.getGameTime();var blows=BLOWS.get(at.asLong());
        if(blows!=null&&now-blows[1]>STRIKE_FORGET)blows=null;
        if(blows!=null&&now-blows[1]<STRIKE_GAP)return true;
        int n=blows==null?1:(int)blows[0]+1;BLOWS.put(at.asLong(),new long[]{n,now});
        p.swing(InteractionHand.MAIN_HAND,true);
        if(steel){
            l.playSound(null,at.getX()+.5,at.getY()+1,at.getZ()+.5,net.minecraft.sounds.SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,SoundSource.BLOCKS,1.4F,.8F+l.getRandom().nextFloat()*.1F);
            TheList.cue(p,TheListTexts.STEEL_STRIKES[Math.min(n,STEEL_STRIKES)-1]);
            if(n<STEEL_STRIKES){l.destroyBlockProgress(breaker(at),at,n*2);l.destroyBlockProgress(breaker(at.above()),at.above(),n*2);return true;}
            BLOWS.remove(at.asLong());
            l.destroyBlockProgress(breaker(at),at,-1);l.destroyBlockProgress(breaker(at.above()),at.above(),-1);
            TheListHunt.spring(l,b,lower);
            return true;
        }
        l.playSound(null,at.getX()+.5,at.getY()+1,at.getZ()+.5,TheListRegistry.STRIKE.get(),SoundSource.BLOCKS,1.4F,.9F+l.getRandom().nextFloat()*.15F);
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.STRIPPED_OAK_LOG.defaultBlockState()),at.getX()+.5,at.getY()+1,at.getZ()+.5,6+4*n,.25,.4,.25,.05);
        TheList.cue(p,TheListTexts.STRIKES[Math.min(n,STRIKES)-1]);
        if(n<STRIKES){l.destroyBlockProgress(breaker(at),at,n*3);l.destroyBlockProgress(breaker(at.above()),at.above(),n*3);return true;}
        BLOWS.remove(at.asLong());
        l.destroyBlockProgress(breaker(at),at,-1);l.destroyBlockProgress(breaker(at.above()),at.above(),-1);
        give(l,at);
        return true;
    }
    /** Who the cracks drawn on a struck door are drawn for: nobody's own id, so every client in reach is sent them. */
    static int breaker(BlockPos at){return 0x5EC00000|(at.hashCode()&0xFFFFF);}
    /** The door gives: split at the lock, holed, swung open; from now on anyone opens and shuts it by hand. */
    static void give(ServerLevel l,BlockPos at){
        var lower=l.getBlockState(at);
        var broken=TheListRegistry.BROKEN_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING,lower.getValue(DoorBlock.FACING))
            .setValue(DoorBlock.HINGE,lower.getValue(DoorBlock.HINGE)).setValue(DoorBlock.OPEN,true);
        int flags=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
        l.setBlock(at,broken.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),flags);
        l.setBlock(at.above(),broken.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),flags);
        l.playSound(null,at.getX()+.5,at.getY()+1,at.getZ()+.5,TheListRegistry.SPLINTER.get(),SoundSource.BLOCKS,1.6F,1F);
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.STRIPPED_OAK_LOG.defaultBlockState()),at.getX()+.5,at.getY()+1,at.getZ()+.5,40,.3,.6,.3,.12);
    }
    /** A blow with the hand (the game's attack) on a locked door strikes it as a hand on it does; 0.4.711: on a plank of the home
     * stands, it is the boards' (TheListHunt.boards). */
    @SubscribeEvent public static void punched(PlayerInteractEvent.LeftClickBlock e){
        if(e.getAction()!=PlayerInteractEvent.LeftClickBlock.Action.START||!(e.getEntity() instanceof ServerPlayer p))return;
        var b=town(p);if(b==null)return;
        var st=p.serverLevel().getBlockState(e.getPos());
        if(TheListBlock.is(st,TheListBlock.Kind.STANDS)&&st.getValue(TheListBlock.STAGE)<2&&TheListHunt.boards(p,b,e.getPos().subtract(b))){e.setCanceled(true);return;}
        // 0.4.713: a blow on the line is a hand on it.
        if(TheListBlock.is(st,TheListBlock.Kind.LINE)&&TheListLine.hand(p,b,e.getPos().subtract(b))){e.setCanceled(true);return;}
        // 0.4.714: a blow on the fryer is a hand on it.
        if(TheListBlock.is(st,TheListBlock.Kind.FRYER)&&TheListFryer.hand(p,b,e.getPos().subtract(b))){e.setCanceled(true);return;}
        if(strike(p,b,e.getPos().subtract(b)))e.setCanceled(true);
    }

    @SubscribeEvent public static void stopped(ServerStoppedEvent e){LIVE.clear();BLOWS.clear();}
    public static void clearAll(){LIVE.clear();BLOWS.clear();}
}
