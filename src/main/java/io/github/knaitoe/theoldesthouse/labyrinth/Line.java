package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.212, the line (docs/LINE_0_4_212.md), the fourth of the dark halls' horrors, after Exploration #4 in the book.
 *
 * <p>A space with no walls a reader can find, and a line tied to the doorframe running off into the black at hand height. Taking
 * it (the tag on the knot) the reader holds it: it shows a little within arm's length, and leads. Off it, or running, the dark is
 * total until they find it again by walking back. A third of the way along, it tugs from behind: once, again, then steadily.
 * Something has hold of the line behind them and comes along it toward whoever holds it, faster while they stand still; its weight
 * drags at them, and close, drags them back across the floor toward it, scraping, the head dipping, the hands slipping. Letting go
 * shakes it off and leaves them blind. Holding on is the only way to the end, and holding on is how it reels them in.
 *
 * <p>Reeled all the way: the line goes slack and the floor is not there. The reader comes to several doors shallower, the line
 * wound round their arm until they next sleep, and something of theirs gone, to be found tied to the pack the next time they reach
 * it. Reaching the pack first: the line goes slack behind them, and the tape recorder on the pack has their own voice on it, dated
 * days from now, describing this room ({@code held_on}).
 *
 * <p>The line and the thing are shared; what each reader holds, loses and finds is their own. A reader holding at the door feels
 * the tugs as well, can pull twice on the tag to warn, and is what it comes for if the one ahead lets go.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Line {
    private Line(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LINE;
    /** Off the line past this, the hand loses it; back within this, it finds it again. */
    public static final double LOSE=1.8,FIND=.9;
    /** How far along the line before it tugs; the tugs' spacing; how fast it comes, standing still and otherwise. */
    public static final double TUG_AT=.3;
    public static final int TUG_GAP=60;
    public static final double COMES_STILL=.16,COMES=.09,PULL=.03,CLOSE=4,DRAG=.12,REELED=.9;
    /** Doors of depth lost when it reels someone in. */
    public static final int DEPTH_LOST=3;
    public static final ResourceKey<DamageType> SCRAPED=ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"the_line"));
    static DamageSource scraping(ServerLevel l){return l.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolder(SCRAPED).map(DamageSource::new).orElse(l.damageSources().generic());}
    private static final Map<UUID,Double> LAST=new HashMap<>();
    private static final Map<UUID,Long> PULLED=new HashMap<>();

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static CompoundTag set(MinecraftServer s){return LiteraryVignettes.shared(LabyrinthData.get(s),PLACE);}
    static void set(MinecraftServer s,CompoundTag t){LiteraryVignettes.shared(LabyrinthData.get(s),PLACE,t);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static boolean inside(ServerPlayer p){return LiteraryVignettes.inside(p,PLACE);}
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void heard(ServerPlayer p,Vec3 at,SoundEvent s,float volume,float pitch){p.connection.send(new ClientboundSoundPacket(Holder.direct(s),SoundSource.HOSTILE,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}
    public static boolean holding(CompoundTag own){return own.getBoolean("Holding212");}
    public static int phase(CompoundTag t){return t.getInt("Phase");}
    public static double thing(CompoundTag t){return t.getDouble("T");}

    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        own.putBoolean("Holding212",false);
        if(own.getBoolean("Ready")&&!own.getBoolean("Completed")){cue(p,"Somewhere out there is a pack with a tape recorder on it. You have been there. The line is still tied to the frame.");return;}
        cue(p,"Past the doorframe there is nothing to see at all. A line is knotted round the frame, and runs off into it at the height of your hand.");
    }

    // ------------------------------------------------------------------------------------------------ the tag and the pack
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        if(rel.equals(LineSite.TAG)){
            if(!holding(own)){take(p,own);save(p,own);return true;}
            // already holding at the door: two pulls on it is a warning to whoever is out there
            long now=p.server.getTickCount();Long last=PULLED.put(p.getUUID(),now);
            if(last!=null&&now-last<40){PULLED.remove(p.getUUID());for(var q:group(p.server,b))if(q!=p&&holding(own(q))){var oq=own(q);oq.putBoolean("Warned212",true);save(q,oq);cue(q,"Two pulls on the line, from the door. A warning.");heard(q,q.getEyePosition(),HorrorRegistry.LINE_TWANG.get(),.6F,1.3F);}cue(p,"You pull twice on the line.");}
            else cue(p,"You pull once on the line. Pull again to warn whoever is out there.");
            return true;
        }
        if(rel.equals(LineSite.PACK)){cue(p,own.getBoolean("Ready")?"An explorer's pack, the line tied to its strap. Nobody's name on it. Your handwriting on the tape.":"An explorer's pack, a long way out in the dark. You should not be able to see it.");return true;}
        if(rel.equals(LineSite.RECORDER)){if(!own.getBoolean("Ready")){cue(p,"A tape recorder. The tape has wound to its end.");return true;}return false;}
        return false;
    }
    static void take(ServerPlayer p,CompoundTag own){own.putBoolean("Holding212",true);p.removeEffect(MobEffects.BLINDNESS);cue(p,"You take hold of the line. It is thin, and taut, and it goes a long way.");}
    static void letGo(ServerPlayer p,CompoundTag own,String why){
        own.putBoolean("Holding212",false);own.putInt("LetGo212",own.getInt("LetGo212")+1);cue(p,why);
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,60,0,false,false,false));
    }
    static List<ServerPlayer> group(MinecraftServer s,BlockPos b){var out=new ArrayList<ServerPlayer>();for(var q:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(q)&&inside(q)&&q.isAlive()&&b.equals(base(q)))out.add(q);return out;}

    // ------------------------------------------------------------------------------------------------ the room
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();long tick=s.getTickCount();
        BlockPos b=null;var group=new ArrayList<ServerPlayer>();
        for(var p:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(p)&&inside(p)&&p.isAlive()){var pb=base(p);if(pb==null)continue;if(b==null)b=pb;if(pb.equals(b))group.add(p);}
        var t=set(s);
        if(b==null){if(t.contains("Base")){int empty=t.getInt("Empty")+1;if(empty>=100)set(s,new CompoundTag());else{t.putInt("Empty",empty);set(s,t);}}return;}
        var l=group.get(0).serverLevel();t.putInt("Empty",0);t.putLong("Base",b.asLong());
        double length=LineSite.length();
        // each reader's hold on the line, and where along it they are
        var holders=new ArrayList<ServerPlayer>();var along=new HashMap<ServerPlayer,Double>();
        for(var q:group){var r=rel(q,b);var own=own(q);boolean changed=false;
            if(!LineSite.inside(r)){if(holding(own)){own.putBoolean("Holding212",false);changed=true;}if(changed)save(q,own);continue;}
            var n=LineSite.nearest(r);
            if(holding(own)){
                if(q.isSprinting()){letGo(q,own,"You can't run and keep hold of it. It's gone.");changed=true;}
                else if(n[1]>LOSE){letGo(q,own,"The line slips out of your hand, and the dark is total.");changed=true;}
            }else if(LineSite.inSpace(r)&&own.getBoolean("Read_Source")&&!q.isSprinting()&&n[1]<FIND&&n[0]>1.5){take(q,own);cue(q,"Your hand finds the line again.");changed=true;}
            if(holding(own)){holders.add(q);along.put(q,n[0]);
                if(tick%3==0)glimmer(l,b,q,n[0]);
                if(!own.getBoolean("Ready")&&n[0]>=length-1.2){reach(l,b,t,q,own,group);changed=true;}
            }else if(LineSite.inSpace(r)&&tick%20==0)q.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,60,0,false,false,false));
            if(changed)save(q,own);
        }
        thing(l,b,t,holders,along,length,tick);
        set(s,t);
    }
    /** The line shows a little, within arm's length of the hand on it, and nowhere else. */
    static void glimmer(ServerLevel l,BlockPos b,ServerPlayer q,double s){
        for(double d=-2.4;d<=2.4;d+=.5){var at=world(b,LineSite.at(s+d));l.sendParticles(q,ParticleTypes.ELECTRIC_SPARK,true,at.x,at.y,at.z,1,0,0,0,0);}
    }
    /** What has hold of the line behind them. */
    static void thing(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> holders,Map<ServerPlayer,Double> along,double length,long tick){
        int phase=phase(t);
        if(phase==3)return;
        double front=0,back=1e9;for(var q:holders){double s=along.get(q);front=Math.max(front,s);back=Math.min(back,s);}
        if(phase==0){if(front>=TUG_AT*length){t.putInt("Phase",1);t.putLong("TugAt",tick);tug(l,b,holders,along,0);}return;}
        if(phase==1){long since=tick-t.getLong("TugAt");
            if(since==TUG_GAP)tug(l,b,holders,along,1);
            if(since>=2L*TUG_GAP){t.putInt("Phase",2);t.putDouble("T",Math.max(0,(holders.isEmpty()?front:back)-10));for(var q:holders)cue(q,"Then it pulls, steadily. Something has hold of the line behind you, and it is coming along it.");}
            return;}
        // phase 2: it comes along the line toward the nearest hand on it
        if(holders.isEmpty())return;
        double th=thing(t);ServerPlayer near=null;double gap=1e9;
        for(var q:holders){double g=Math.abs(along.get(q)-th);if(g<gap){gap=g;near=q;}}
        double s=along.get(near);Double was=LAST.put(near.getUUID(),s);boolean still=was!=null&&Math.abs(s-was)<.03;
        double step=Math.min(gap,still?COMES_STILL:COMES);th+=Math.signum(s-th)*step;t.putDouble("T",th);gap=Math.abs(s-th);
        var pos=world(b,LineSite.at(th));
        if(tick%8==0)l.playSound(null,pos.x,b.getY(),pos.z,HorrorRegistry.LINE_STEPS.get(),SoundSource.HOSTILE,gap<CLOSE*3?1.2F:.6F,.9F+l.random.nextFloat()*.1F);
        if(tick%30==0)l.playSound(null,pos.x,pos.y,pos.z,HorrorRegistry.LINE_CREAK.get(),SoundSource.HOSTILE,.8F,1F);
        for(var q:holders){double g=Math.abs(along.get(q)-th);var own=own(q);
            // its weight on the line drags at every hand on it, harder close by
            if(tick%2==0)drag(q,b,along.get(q),th,g<CLOSE?DRAG:PULL);
            if(g<CLOSE*3&&!own.getBoolean("Near212")){own.putBoolean("Near212",true);save(q,own);cue(q,"Something walking along the line, near.");}
            if(g<CLOSE){if(tick%20==0){q.invulnerableTime=0;if(q.getHealth()>4F)q.hurt(scraping(l),1F);cue(q,"It is dragging you back across the floor. Your hands are slipping.");own.putInt("Dragged212",own.getInt("Dragged212")+1);save(q,own);}
                if(tick%4==0)q.teleportTo(l,q.getX(),q.getY(),q.getZ(),q.getYRot(),Math.min(40F,q.getXRot()+2F));}
            if(g<REELED){reeled(l,b,t,q,own);return;}
        }
    }
    static void tug(ServerLevel l,BlockPos b,List<ServerPlayer> holders,Map<ServerPlayer,Double> along,int n){
        for(var q:holders){var back=LineSite.at(Math.max(0,along.get(q)-3)).subtract(rel(q,b));double d=Math.max(.1,Math.hypot(back.x,back.z));
            q.setDeltaMovement(q.getDeltaMovement().add(back.x/d*.5,.05,back.z/d*.5));q.hurtMarked=true;q.connection.send(new ClientboundSetEntityMotionPacket(q));
            heard(q,q.getEyePosition(),HorrorRegistry.LINE_TWANG.get(),1F,n==0?.9F:1F);
            cue(q,n==0?"The line tugs in your hand. Hard, from behind you.":"Again.");}
    }
    /** Its weight on the line, pulling the hand that holds it back toward it. */
    static void drag(ServerPlayer q,BlockPos b,double s,double th,double strength){
        var to=LineSite.at(th).subtract(rel(q,b));double d=Math.hypot(to.x,to.z);if(d<.05)return;
        q.setDeltaMovement(q.getDeltaMovement().add(to.x/d*strength,0,to.z/d*strength));q.hurtMarked=true;q.connection.send(new ClientboundSetEntityMotionPacket(q));
    }
    /** At the pack, first: the line goes slack behind them. */
    static void reach(ServerLevel l,BlockPos b,CompoundTag t,ServerPlayer q,CompoundTag own,List<ServerPlayer> group){
        t.putInt("Phase",3);
        LiteraryVignettes.ready(q,PLACE,own,"held_on");
        own.putLong("Found212",l.getDayTime()/24000);
        cue(q,"The line goes slack in your hand. At the end of it, an explorer's pack, and a tape recorder with its tape run out.");
        if(own.contains("Tied212")){var item=ItemStack.parseOptional(q.registryAccess(),own.getCompound("Tied212"));own.remove("Tied212");LiteraryVignettes.give(q,item);
            cue(q,"Tied to the pack's strap with another length of the same line: your "+item.getHoverName().getString().toLowerCase(Locale.ROOT)+".");}
        for(var o:group)if(o!=q){heard(o,o.getEyePosition(),HorrorRegistry.LINE_SLACK.get(),1F,1F);cue(o,"The line goes slack. Whatever had hold of it has let go.");}
        heard(q,q.getEyePosition(),HorrorRegistry.LINE_SLACK.get(),1F,1F);
    }
    /** Reeled all the way: the floor is not there; several doors shallower; the line round the arm; something gone. */
    static void reeled(ServerLevel l,BlockPos b,CompoundTag t,ServerPlayer q,CompoundTag own){
        t.putInt("Phase",0);t.remove("T");LAST.clear();
        own.putBoolean("Holding212",false);own.putInt("Reeled212",own.getInt("Reeled212")+1);own.remove("Near212");
        heard(q,q.getEyePosition(),HorrorRegistry.LINE_SLACK.get(),1F,.8F);
        HousePackets.send(q,new HouseFadePayload(2,40,30));
        cue(q,"The line goes slack, and the floor isn't there.");
        // something of theirs, gone, to be found tied to the end of the line next time
        var inv=q.getInventory();var candidates=new ArrayList<Integer>();
        for(int i=0;i<inv.getContainerSize();i++)if(Storeroom.plain(inv.getItem(i)))candidates.add(i);
        if(!candidates.isEmpty()&&!own.contains("Tied212")){int i=candidates.get(q.getRandom().nextInt(candidates.size()));var gone=inv.getItem(i).copy();inv.setItem(i,ItemStack.EMPTY);own.put("Tied212",gone.save(q.registryAccess()));own.putString("Gone212",gone.getHoverName().getString());}
        // the line, wound tight round their arm
        var wound=new ItemStack(Items.STRING);wound.set(DataComponents.CUSTOM_NAME,Component.literal("The line, wound round your arm").withStyle(s->s.withItalic(false)));
        wound.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Too tight to get off. Maybe after you have slept.").withStyle(ChatFormatting.GRAY))));
        CustomData.update(DataComponents.CUSTOM_DATA,wound,tag->tag.putBoolean("Wound212",true));
        if(!woundRound(q)){if(q.getOffhandItem().isEmpty())q.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,wound);else LiteraryVignettes.give(q,wound);}
        save(q,own);
        // several doors shallower
        var d=LabyrinthData.get(q.server);for(int i=0;i<DEPTH_LOST;i++){var w=d.peekReturn(q.getUUID());if(w==null)break;d.consumeReturn(q.getUUID(),w);}
        LabyrinthDoors.sendBack(q,2,40,30);
    }
    static boolean woundRound(ServerPlayer q){var inv=q.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(wound(inv.getItem(i)))return true;return false;}
    public static boolean wound(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c!=null&&c.copyTag().getBoolean("Wound212");}
    /** The line round the arm will not come off; sleeping, it does. */
    @SubscribeEvent public static void toss(ItemTossEvent e){
        if(!wound(e.getEntity().getItem())||!(e.getPlayer() instanceof ServerPlayer p))return;
        e.setCanceled(true);LiteraryVignettes.give(p,e.getEntity().getItem().copy());cue(p,"It won't come off. It's wound too tight.");
    }
    @SubscribeEvent public static void woke(PlayerWakeUpEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var inv=p.getInventory();boolean any=false;
        for(int i=0;i<inv.getContainerSize();i++)if(wound(inv.getItem(i))){inv.setItem(i,ItemStack.EMPTY);any=true;}
        if(any)cue(p,"You wake with the line gone from your arm. The marks it left go all the way round.");
    }
    public static boolean locked(ServerPlayer p){return false;}
    /** Test hook: changes the shared state directly. */
    public static void edit(MinecraftServer s,java.util.function.Consumer<CompoundTag> change){var t=set(s);change.accept(t);set(s,t);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){LAST.remove(e.getEntity().getUUID());PULLED.remove(e.getEntity().getUUID());}
    public static void clearAll(){LAST.clear();PULLED.clear();}
}
