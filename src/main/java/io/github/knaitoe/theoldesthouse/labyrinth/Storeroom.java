package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.210, the storeroom (docs/STOREROOM_0_4_210.md), the second of the dark halls' horrors, after Johnny's storeroom in the book.
 *
 * <p>A dark hall ends in a door on a spring and a narrow storeroom behind it. Once it has shut behind a reader, the room is
 * running. Beside the door, on its inside, hangs a list in the reader's own hand: the ink, the needles, and a thing of their own,
 * copied out of their own world, with their name on a tag. While nobody faces the door, something gathers behind them: the dread
 * climbs (faster with a back to it), the bulb's chain swings toward them, breathing comes nearer, the edges of the sight close and
 * the heart pounds. Facing the door holds it back and empties the dread; but every look, after the list is read, kills the bulb
 * for a second and brings the side shelves a block nearer, and holding the look costs another every four seconds. From the second
 * look the shelves press the reader's sides (a heart every three seconds, and the breath spent fast); at the third they meet, and
 * anyone between them dies there.
 *
 * <p>The dread full: a hand on the shoulder, the head turned slowly toward the door against the mouse, the bulb out the instant
 * they would see, a blow, and the reader comes to on the hall floor outside, one thing of theirs gone and the thing on the tag gone
 * too, to be found on their bed days later. All three things, and backing into the door without turning: it opens behind them
 * and Johnny's footnote, by the way in, is ready ({@code did_not_turn_round}).
 *
 * <p>The room, its shelves and its dread are shared: two readers back to back hold the clock between them (it climbs at half the
 * rate), the room shortens whenever either looks, and the pinch closes on both. The list, the three things and the outcome are
 * each reader's own.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Storeroom {
    private Storeroom(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.STOREROOM;
    /** The dread: full at this; what a back to the door adds a tick, what anything else short of facing it adds. */
    public static final int DREAD=600,BACK=2,SIDE=1;
    /** A held look costs another this often; the pinch hurts this often; how long the bulb dies for. */
    public static final int HELD_LOOK=80,PINCH_TICKS=60,DARK_TICKS=20;
    /** How long the spring takes to bang the door shut once nobody is in it. */
    public static final int SPRING=30;
    public static final int INK=1,NEEDLES=2,OWN=4,ALL=7;
    /** Test hook: the dread does not climb on its own. */
    public static boolean TEST_HOLD=false;
    public static final ResourceKey<DamageType> CRUSHED_IN=ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"storeroom"));
    static DamageSource crushing(ServerLevel l){return l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolder(CRUSHED_IN).map(DamageSource::new).orElse(l.damageSources().inWall());}
    private static final Map<UUID,Boolean> FACING=new HashMap<>();
    private static final Map<UUID,Integer> HELD=new HashMap<>(),TURN=new HashMap<>();

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static CompoundTag set(MinecraftServer s){return LiteraryVignettes.shared(LabyrinthData.get(s),PLACE);}
    static void set(MinecraftServer s,CompoundTag t){LiteraryVignettes.shared(LabyrinthData.get(s),PLACE,t);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static boolean inside(ServerPlayer p){return LiteraryVignettes.inside(p,PLACE);}
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void heard(ServerPlayer p,Vec3 at,SoundEvent s,float volume,float pitch){p.connection.send(new ClientboundSoundPacket(Holder.direct(s),SoundSource.AMBIENT,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}
    static List<ServerPlayer> group(MinecraftServer s,BlockPos b){var out=new ArrayList<ServerPlayer>();for(var q:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(q)&&inside(q)&&b.equals(base(q)))out.add(q);return out;}
    public static int looks(CompoundTag t){return t.getInt("Looks");}
    public static int dread(CompoundTag t){return t.getInt("Dread");}
    public static boolean running(CompoundTag t){return t.getBoolean("Run");}
    public static int has(CompoundTag own){return own.getInt("Has210");}
    /** How squarely a reader faces the door: 1 straight at it, -1 with their back to it. */
    public static double facing(ServerPlayer p,BlockPos b){
        var look=p.getLookAngle();var to=StoreroomSite.doorCentre(b).subtract(p.getEyePosition());
        double lh=Math.hypot(look.x,look.z),th=Math.hypot(to.x,to.z);if(lh<1e-4||th<1e-4)return 0;
        return (look.x*to.x+look.z*to.z)/(lh*th);
    }

    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("Ready")&&!own.getBoolean("Completed")){cue(p,"A page lies on the hall floor just inside, as if dropped. It has a footnote on it.");return;}
        cue(p,"At the far end of the hall, a door. Behind it a bulb is lit; you can see the line of light under it.");
    }

    // ------------------------------------------------------------------------------------------------ the list and the shelves
    /** The list, in the reader's own hand, naming the thing of theirs it wants. */
    public static ItemStack list(ServerPlayer p,CompoundTag own){
        var thing=ownThing(p,own);return StoreroomTexts.list(p.getGameProfile().getName(),thing.getHoverName().getString());
    }
    /**
     * The reader's own thing: copied from a chest of theirs. Their copied home's containers first, then their ender chest, then
     * whatever they carry; failing all of that, a key to a house. Chosen once and kept.
     */
    static ItemStack ownThing(ServerPlayer p,CompoundTag own){
        if(own.contains("Own210"))return ItemStack.parseOptional(p.registryAccess(),own.getCompound("Own210"));
        var picked=pick(p);var copy=picked.copyWithCount(1);
        copy.set(DataComponents.CUSTOM_NAME,picked.getHoverName().copy().withStyle(s->s.withItalic(false)));
        copy.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("A tag on it, in your own hand: "+p.getGameProfile().getName()).withStyle(ChatFormatting.GRAY))));
        CustomData.update(DataComponents.CUSTOM_DATA,copy,t->{t.putString("Storeroom210","own");t.putUUID("Reader",p.getUUID());});
        own.put("Own210",copy.save(p.registryAccess()));return copy;
    }
    static ItemStack pick(ServerPlayer p){
        var copy=LiteraryCopies.base(p.server,p.getUUID(),LabyrinthPlace.FAMILY_COPY);
        if(copy!=null){var l=p.server.getLevel(NovelRooms.dimension(LabyrinthPlace.FAMILY_COPY));
            if(l!=null&&l.isLoaded(copy))for(var at:BlockPos.betweenClosed(copy.offset(-24,-2,-40),copy.offset(24,10,2)))if(l.getBlockEntity(at) instanceof net.minecraft.world.Container c)for(int i=0;i<c.getContainerSize();i++){var s=c.getItem(i);if(everyday(s))return s;}}
        var ender=p.getEnderChestInventory();for(int i=0;i<ender.getContainerSize();i++)if(everyday(ender.getItem(i)))return ender.getItem(i);
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(everyday(inv.getItem(i)))return inv.getItem(i);
        return new ItemStack(Items.TRIPWIRE_HOOK).copy();
    }
    /** An ordinary thing of the reader's: nothing of the House's, no book, no journal. */
    static boolean plain(ItemStack s){return !s.isEmpty()&&!s.has(DataComponents.CUSTOM_DATA)&&!s.has(DataComponents.WRITTEN_BOOK_CONTENT)&&!s.has(DataComponents.WRITABLE_BOOK_CONTENT)&&!(s.getItem() instanceof io.github.knaitoe.theoldesthouse.journal.JournalItem);}
    /**
     * What the end shelf may copy: a plain, everyday thing. Nothing that holds other things (a copied box would copy its contents),
     * nothing enchanted or rare, no precious metal or gem: the copy is a likeness with a tag on it, never a windfall.
     */
    static boolean everyday(ItemStack s){
        if(!plain(s)||s.has(DataComponents.CONTAINER)||s.has(DataComponents.BUNDLE_CONTENTS)||s.isEnchanted()||s.getRarity()!=net.minecraft.world.item.Rarity.COMMON||s.is(net.minecraft.tags.ItemTags.BEACON_PAYMENT_ITEMS))return false;
        String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();
        for(String rich:new String[]{"diamond","netherite","emerald","gold","shulker","totem","elytra","beacon","nether_star","echo_shard","heart_of","trim","spawn_egg"})if(id.contains(rich))return false;
        return true;
    }

    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var s=l.getBlockState(b.offset(rel));var k=HorrorBlock.kind(s);
        if(rel.equals(StoreroomSite.FOOTNOTE)){if(!own.getBoolean("Ready"))cue(p,"A torn page, mostly footnote. The small print gives out halfway down; you can't make out the rest.");return !own.getBoolean("Ready");}
        if(k==HorrorBlock.Kind.SHELF_INK)return take(p,own,INK,new ItemStack(Items.INK_SAC),"A bottle of black ink, the big one","Black ink. The big bottle.");
        if(k==HorrorBlock.Kind.SHELF_NEEDLES)return take(p,own,NEEDLES,new ItemStack(Items.IRON_NUGGET),"Needles in sterile packs","Needles, in their sterile packs.");
        if(k==HorrorBlock.Kind.SHELF_OWN){
            if((has(own)&OWN)!=0){cue(p,"Nothing else on the end shelf has your name on it.");return true;}
            if(own.contains("Return210")||own.getBoolean("Returned210")){own.putInt("Has210",has(own)|OWN);cue(p,"The place on the end shelf where it stood is a clean square in the dust. The tag is still there, in your hand: "+(own.getBoolean("Returned210")?"RETURNED.":"KEPT FOR YOU."));if(has(own)==ALL)cue(p,"All three. Don't turn round. The door is behind you.");return true;}
            var thing=ownThing(p,own).copy();own.putInt("Has210",has(own)|OWN);LiteraryVignettes.give(p,thing);
            cue(p,thing.getHoverName().getString()+", on the end shelf. The tag is in your handwriting. You never wrote it.");
            if(has(own)==ALL)cue(p,"All three. Don't turn round. The door is behind you.");return true;
        }
        if(k==HorrorBlock.Kind.SHELF){cue(p,"Steel shelving: boxes, rags, bottles nobody has opened. Not what's on the list.");return true;}
        if(k==HorrorBlock.Kind.BULB){cue(p,"The bulb is too high to reach, and you would have to look up at it.");return true;}
        return false;
    }
    static boolean take(ServerPlayer p,CompoundTag own,int bit,ItemStack item,String name,String line){
        if((has(own)&bit)!=0){cue(p,"You have that already.");return true;}
        item.set(DataComponents.CUSTOM_NAME,Component.literal(name).withStyle(s->s.withItalic(false)));
        CustomData.update(DataComponents.CUSTOM_DATA,item,t->{t.putString("Storeroom210",bit==INK?"ink":"needles");t.putUUID("Reader",p.getUUID());});
        own.putInt("Has210",has(own)|bit);LiteraryVignettes.give(p,item);cue(p,line+(has(own)==ALL?" All three. Don't turn round.":""));return true;
    }

    // ------------------------------------------------------------------------------------------------ the room
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();long tick=s.getTickCount();
        if(tick%100==0)returns(s);
        BlockPos b=null;var group=new ArrayList<ServerPlayer>();
        for(var p:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(p)&&inside(p)){var pb=base(p);if(pb==null)continue;if(b==null)b=pb;if(pb.equals(b))group.add(p);}
        var t=set(s);
        if(b==null){
            if(!t.contains("Base"))return;
            int empty=t.getInt("Empty")+1;t.putInt("Empty",empty);if(empty<100){set(s,t);return;}
            var l=s.getLevel(NovelRooms.dimension(PLACE));var rb=BlockPos.of(t.getLong("Base"));
            if(l!=null&&l.isLoaded(rb)&&(looks(t)>0||t.getBoolean("Run")))StoreroomSite.build(l,rb);
            set(s,new CompoundTag());return;
        }
        var l=group.get(0).serverLevel();t.putInt("Empty",0);t.putLong("Base",b.asLong());
        spring(l,b,t,group);
        var inStore=new ArrayList<ServerPlayer>();for(var q:group)if(StoreroomSite.inStore(rel(q,b)))inStore.add(q);
        if(!t.getBoolean("Run")){
            if(!inStore.isEmpty()&&!StoreroomSite.open(l,b)){t.putBoolean("Run",true);t.putInt("Dread",0);
                var present=new net.minecraft.nbt.ListTag();for(var q:inStore){present.add(net.minecraft.nbt.StringTag.valueOf(q.getStringUUID()));cue(q,"The door bangs shut behind you on its spring. Something is pinned beside it. You'd have to turn round to read it.");}
                t.put("Present",present);}
            set(s,t);return;
        }
        if(t.getInt("Dark")>0){int d=t.getInt("Dark")-1;t.putInt("Dark",d);if(d==0)StoreroomSite.bulb(l,b,1);}
        boolean anyFacing=false,anyBack=false;
        for(var q:inStore){var own=own(q);double f=facing(q,b);boolean facing=f>.6;var id=q.getUUID();
            if(TURN.containsKey(id)){turn(l,b,t,q,own);continue;}
            if(facing){anyFacing=true;
                boolean was=FACING.getOrDefault(id,false);int held=was?HELD.getOrDefault(id,0)+1:0;HELD.put(id,held);
                if(own.getBoolean("Read_Source")&&(!was||held>=HELD_LOOK)){HELD.put(id,0);look(l,b,t,inStore,q);}
            }else if(f<0)anyBack=true;
            FACING.put(id,facing);
        }
        if(anyFacing)t.putInt("Dread",0);
        else if(!inStore.isEmpty()&&!TEST_HOLD){int rate=anyBack?BACK:SIDE;int d=dread(t)+(inStore.size()>=2?(tick%2==0?rate:0):rate);t.putInt("Dread",Math.min(DREAD,d));}
        gathering(l,b,t,inStore,tick);
        if(dread(t)>=DREAD&&!inStore.isEmpty()){for(var q:inStore)if(!TURN.containsKey(q.getUUID()))TURN.put(q.getUUID(),0);t.putInt("Dread",0);}
        pinch(l,b,t,inStore,tick);
        for(var q:group)backOut(l,b,t,q);
        set(s,t);
    }
    /** The door swings shut on its spring a moment after nobody is in it, and bangs. */
    static void spring(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        if(!StoreroomSite.open(l,b)){t.putInt("Spring",0);return;}
        var cell=new AABB(b.offset(StoreroomSite.DOOR)).expandTowards(0,1,0);
        for(var q:group)if(q.getBoundingBox().intersects(cell)){t.putInt("Spring",0);return;}
        int sp=t.getInt("Spring")+1;t.putInt("Spring",sp);
        if(sp>=SPRING){StoreroomSite.door(l,b,false);t.putInt("Spring",0);for(var q:group)heard(q,StoreroomSite.doorCentre(b),HorrorRegistry.STORE_SLAM.get(),1F,1F);}
    }
    /** A look at the door: the bulb dies for a moment and comes back with the side shelves a block nearer. */
    static void look(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> inStore,ServerPlayer who){
        int n=looks(t)+1;t.putInt("Looks",n);t.putInt("Dark",DARK_TICKS);StoreroomSite.bulb(l,b,0);
        var filled=StoreroomSite.shelves(l,b,n);
        for(var q:inStore){heard(q,q.getEyePosition(),HorrorRegistry.STORE_FLICKER.get(),.8F,1F);heard(q,q.position().add(0,1,0),HorrorRegistry.STORE_SCRAPE.get(),1F,.9F+.05F*n);
            var own=own(q);own.putInt("Looks210",own.getInt("Looks210")+(q==who?1:0));save(q,own);
            cue(q,n>=3?"The light comes back and there is no room left.":n==2?"The light comes back. The shelves are against your shoulders.":"The light comes back, and the shelves are a block nearer than they were.");}
        if(n>=3)for(var q:inStore)q.hurt(crushing(l),1000F);
        else for(var q:inStore)shove(l,b,q,n);
    }
    /** A reader standing where a shelf now is is pushed into the gap left. */
    static void shove(ServerLevel l,BlockPos b,ServerPlayer q,int looks){
        var r=rel(q,b);int in=StoreroomSite.side(looks)-1;double x=Mth.clamp(r.x,-in+.3,in+.7);if(in<0)x=.5;
        if(Math.abs(x-r.x)>.01)q.teleportTo(b.getX()+x,q.getY(),q.getZ());
    }
    /** What gathers while nobody looks: the chain, the breath, the edges of the sight, the heart. */
    static void gathering(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> inStore,long tick){
        int d=dread(t);if(t.getInt("Dark")>0)return;
        if(d>DREAD/4&&tick%10==0)StoreroomSite.bulb(l,b,d>DREAD/2?((tick/10)%2==0?2:3):1);
        if(d<=DREAD/4&&tick%10==0)StoreroomSite.bulb(l,b,1);
        for(var q:inStore){
            float near=(float)d/DREAD;
            if(d>DREAD/5&&tick%Math.max(20,80-(int)(near*60))==0){var behind=q.getEyePosition().subtract(q.getLookAngle().multiply(1,0,1).normalize().scale(4-3.2*near));heard(q,behind,HorrorRegistry.STORE_BREATH.get(),.4F+.6F*near,1F);
                if(near>.6)cue(q,near>.85?"Breathing, an arm's length behind you.":"Breathing, behind you, nearer.");}
            if(d>DREAD/3&&tick%Math.max(10,40-(int)(near*30))==0)heard(q,q.getEyePosition(),HorrorRegistry.STORE_HEART.get(),.5F+.5F*near,1F);
            if(d>DREAD/2&&tick%40==0)q.addEffect(new MobEffectInstance(MobEffects.DARKNESS,60,0,false,false,false));
        }
    }
    /** The dread full: a hand on the shoulder; the head turned slowly to the door; the bulb out the instant they would see; a blow. */
    static void turn(ServerLevel l,BlockPos b,CompoundTag t,ServerPlayer q,CompoundTag own){
        var id=q.getUUID();int step=TURN.get(id);TURN.put(id,step+1);
        if(step==0){cue(q,"A hand closes on your shoulder.");q.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,6,false,false,false));}
        var to=StoreroomSite.doorCentre(b).subtract(q.getEyePosition());float want=(float)(Mth.atan2(to.z,to.x)*180/Math.PI)-90;
        float yaw=Mth.approachDegrees(q.getYRot(),want,6);q.teleportTo(l,q.getX(),q.getY(),q.getZ(),yaw,q.getXRot());
        if(Math.abs(Mth.wrapDegrees(want-yaw))<45){StoreroomSite.bulb(l,b,0);t.putInt("Dark",30);}
        if(step>=30||Math.abs(Mth.wrapDegrees(want-yaw))<12){TURN.remove(id);knocked(l,b,q,own);}
    }
    /** Comes to on the hall floor, one thing gone and the thing on the tag gone, to be found on their bed days later. */
    static void knocked(ServerLevel l,BlockPos b,ServerPlayer q,CompoundTag own){
        heard(q,q.getEyePosition(),HorrorRegistry.STORE_BLOW.get(),1F,1F);q.hurt(l.damageSources().generic(),6F);
        HousePackets.send(q,new HouseFadePayload(2,10,40));
        var inv=q.getInventory();var candidates=new ArrayList<Integer>();
        for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);var c=s.get(DataComponents.CUSTOM_DATA);
            if(c!=null&&c.copyTag().contains("Storeroom210")){if(c.copyTag().getString("Storeroom210").equals("own")){own.put("Return210",s.save(q.registryAccess()));own.putLong("ReturnDay210",l.getDayTime()/24000+3);}inv.setItem(i,ItemStack.EMPTY);continue;}
            if(plain(s))candidates.add(i);}
        String gone="";if(!candidates.isEmpty()){int i=candidates.get(q.getRandom().nextInt(candidates.size()));gone=inv.getItem(i).getHoverName().getString();inv.setItem(i,ItemStack.EMPTY);}
        own.putInt("Has210",0);own.putInt("Knocked210",own.getInt("Knocked210")+1);if(!gone.isEmpty())own.putString("Gone210",gone);
        q.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()+StoreroomSite.HALL_END+1.5,180,20);q.connection.resetPosition();
        cue(q,gone.isEmpty()?"You come to on the hall floor. The door at the end is shut.":"You come to on the hall floor. The door at the end is shut. Your "+gone.toLowerCase(Locale.ROOT)+" is gone.");
        save(q,own);
    }
    /** From the second look the shelves press on whoever is between them: a heart every three seconds, the breath spent fast. */
    static void pinch(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> inStore,long tick){
        if(looks(t)<2)return;
        for(var q:inStore){q.setAirSupply(Math.max(-19,q.getAirSupply()-8));
            if(tick%PINCH_TICKS==0){q.hurt(crushing(l),2F);cue(q,"The shelves press on both your shoulders. You can't get a breath.");}}
    }
    /** With all three, backing into the door without turning: it opens behind them and lets them through. */
    static void backOut(ServerLevel l,BlockPos b,CompoundTag t,ServerPlayer q){
        var own=own(q);if(has(own)!=ALL||own.getBoolean("Ready"))return;
        var r=rel(q,b);if(r.z>StoreroomSite.NEAR_Z+1.6||r.z<StoreroomSite.NEAR_Z||Math.abs(r.x-.5)>1.1)return;
        if(facing(q,b)>-.3)return;
        StoreroomSite.door(l,b,true);q.teleportTo(l,q.getX(),q.getY(),b.getZ()+StoreroomSite.HALL_END+.5,q.getYRot(),q.getXRot());
        LiteraryVignettes.ready(q,PLACE,own,"did_not_turn_round");save(q,own);
        cue(q,"The door opens behind you as you back into it. You don't turn round. You don't turn round until you are out.");
    }
    /** The thing on the tag, a few days after it was taken: on the reader's bed, or in their pocket if they have no bed. */
    static void returns(MinecraftServer s){
        for(var p:s.getPlayerList().getPlayers()){var own=own(p);if(!own.contains("Return210"))continue;
            long day=p.serverLevel().getDayTime()/24000;if(day<own.getLong("ReturnDay210"))continue;
            var item=ItemStack.parseOptional(p.registryAccess(),own.getCompound("Return210"));var bed=p.getRespawnPosition();
            if(bed!=null&&p.level().dimension().equals(p.getRespawnDimension())&&p.blockPosition().closerThan(bed,32)){
                var e=new ItemEntity(p.serverLevel(),bed.getX()+.5,bed.getY()+.7,bed.getZ()+.5,item);e.setDeltaMovement(Vec3.ZERO);p.serverLevel().addFreshEntity(e);
                cue(p,"Something has been left on your bed.");}
            else if(day>=own.getLong("ReturnDay210")+2){LiteraryVignettes.give(p,item);cue(p,"It was in your pocket. You are sure it wasn't, a moment ago.");}
            else continue;
            own.remove("Return210");own.remove("ReturnDay210");own.putBoolean("Returned210",true);save(p,own);
        }
    }
    public static boolean locked(ServerPlayer p){if(!inside(p))return false;var b=base(p);if(b==null)return false;return running(set(p.server))&&StoreroomSite.inStore(rel(p,b));}
    /** Test hook: changes the shared state directly. */
    public static void edit(MinecraftServer s,java.util.function.Consumer<CompoundTag> change){var t=set(s);change.accept(t);set(s,t);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){var id=e.getEntity().getUUID();FACING.remove(id);HELD.remove(id);TURN.remove(id);}
    public static void clearAll(){FACING.clear();HELD.clear();TURN.clear();TEST_HOLD=false;}
}
