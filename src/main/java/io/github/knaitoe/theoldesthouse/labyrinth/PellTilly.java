package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.house.SeatEntity;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.PellTillyPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.545, a reader as Tilly (docs/PELL_HOUSE_TILLY_0_4_545.md), from the proposal's "Who plays whom": "Two readers. One reader may play
 * Tilly. The other is the older sibling. A long cane and a small coat hang on the hook inside the front door, and whoever takes the cane is
 * Tilly. If nobody takes the cane within a short while of both arriving, Tilly stays a character. Pairs are saved." And "Co-op lines.
 * Tilly's own lines come to her reader in grey, to say in their own words or not."
 *
 * <ul>
 * <li><b>The cane.</b> On the first visit, from when June opens the front door until the end of her tour, with another reader at the house,
 * a hand on the hook takes the cane, and that reader is Tilly from then on, on every visit, until their ending. Each reader there is told
 * once that the cane is there; each is told who took it.</li>
 * <li><b>Her body.</b> The house's own Tilly stays, hidden, where the reader is, so that everything the house does with Tilly it does with
 * them; and where June leads her by the hand (to the water and in it, and home), the reader goes with her, carried at her feet. The reader
 * is a child's size, as Tilly is, and is drawn as Tilly to everyone at the house, themselves included (client.PellTillyLook).</li>
 * <li><b>Her lines.</b> Whatever Tilly would say comes to the reader in grey, to say in their own words or not, when she would say it.</li>
 * <li><b>Her own verb.</b> In June's hands in the water, each time June calls her Nessa the reader is asked on the panel: "Yes, Mum.", "I'm
 * Tilly." or nothing. Only an answer to the name ends it. A week in, at bedtime and at breakfast, the name is answered on the panel too.</li>
 * <li><b>Nobody else's.</b> The older sibling's things are not hers: June never sends her out to the garage or blames her, Gail never
 * marks her file, the journal is never written in her hand, and she has no turn on the telephone.</li>
 * </ul>
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PellTilly {
    private PellTilly(){}
    static final int T=PellPeople.TILLY,J=PellPeople.JUNE;
    /** In the reader's own record: they are Tilly here, every visit until their ending. */
    public static final String PLAYS="PlaysTilly";
    /** In another reader's record: who played Tilly with them, by id and by name. */
    public static final String TILLY_ID="TillyReader",TILLY_NAME="TillyName";
    /** In the record of a reader as Tilly: who took her hand in the water; that she answered to the name there. */
    public static final String HAND="TillyHand",ANSWERED="AnsweredInWater";
    private static final String HINTED="CaneHint";
    /** The panel's keys: in the water, and a week in, at bedtime and at breakfast. */
    public static final String RITE_TALK="pell_rite",NAME_TALK="pell_name";
    /** A reader as Tilly is a child's size, as Tilly is (PellPeople.size). */
    private static final ResourceLocation SMALL=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"pell_tilly");
    /** How long June stands at Nessa's bed at midnight for a reader as Tilly who is not by it, before the house brings them there. */
    public static final int FETCH=400;
    /** How near Nessa's bed is near enough to be woken and led from it. */
    static final double AT_BED=3.5;
    /** The hook inside the front door, with the cane and the small coat (PellSite: the hall's west wall). */
    public static final BlockPos HOOK=new BlockPos(1,1,-60);
    /** How often the look is sent; how far it is sent. */
    static final int LOOK_EVERY=10;static final double LOOK_FAR=96;
    /** A week in, how long the house waits for an answer on the panel before it goes on without one. */
    static final int ANSWER_WAIT=160;
    /** At bedtime, how long June sits on the edge of Nessa's bed for a reader as Tilly who has not come, before she says goodnight. */
    public static final int BED_WAIT=600;
    /** Readers made small here, to be put right when they go. */
    private static final Map<UUID,ServerPlayer> SMALLED=new HashMap<>();
    /** Readers the house itself is letting go of (whose dismount is not refused). */
    private static final Set<UUID> LETTING=new HashSet<>();
    public static void clearAll(){for(var p:List.copyOf(SMALLED.values()))small(p,false);SMALLED.clear();LETTING.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){SMALLED.clear();LETTING.clear();}

    // ------------------------------------------------------------------------------------------------ who
    static boolean plays(CompoundTag own){return own.getBoolean(PLAYS);}
    /** Whether this reader is Tilly at the Pell house (their own record says so). */
    public static boolean plays(ServerPlayer p){return plays(PellHouse.own(p));}
    /** Whether a reader is playing Tilly at this house now. */
    static boolean played(PellPeople.Household h){return h.tillyReader!=null;}
    static boolean isTilly(PellPeople.Household h,ServerPlayer p){return p.getUUID().equals(h.tillyReader);}
    /** For tests: who is Tilly at this house now, or null. */
    public static @Nullable UUID readerAt(BlockPos b){return PellPeople.household(b).tillyReader;}
    static @Nullable ServerPlayer reader(ServerLevel l,PellPeople.Household h){
        if(h.tillyReader==null)return null;var p=l.getServer().getPlayerList().getPlayer(h.tillyReader);
        return p!=null&&p.level()==l?p:null;
    }
    /** The readers at the house who are not Tilly: those the older sibling's part of the house is for. */
    static List<ServerPlayer> siblings(PellPeople.Household h,List<ServerPlayer> here){
        if(h.tillyReader==null)return here;
        var out=new ArrayList<ServerPlayer>(here.size());for(var p:here)if(!p.getUUID().equals(h.tillyReader))out.add(p);return out;
    }
    /** Where June has Tilly by the hand: woken and led down, held in the water, answered and led home. The reader goes where she goes. */
    static boolean led(PellPeople.Household h){
        return switch(h.rite){
            case PellRite.WAKE->h.waypoint>=2;
            case PellRite.LEAD,PellRite.ANSWERED,PellRite.HOME->true;
            case PellRite.WATER->h.inHold;
            default->false;
        };
    }

    // ------------------------------------------------------------------------------------------------ every tick
    /**
     * Before the visit's tick: who is Tilly here now (a reader whose record says so, at the house, alive), and what Tilly is doing, which is
     * what they are doing: asleep, sat at the table, on the edge of Nessa's bed. A reader who has gone gives Tilly back to the house.
     */
    static void before(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here){
        ServerPlayer found=null;
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&plays(PellHouse.own(p))){found=p;break;}
        if(h.tillyReader!=null&&(found==null||!h.tillyReader.equals(found.getUUID())))leave(l,b,h,h.tillyReader);
        if(found==null)return;
        h.tillyReader=found.getUUID();
        // Tilly where the reader is now, for the visit's tick, unless June has her by the hand (after() carries the reader to her).
        var tilly=PellPeople.find(l,b,T);
        if(tilly!=null&&!led(h)&&found.getVehicle()!=tilly){tilly.moveTo(found.getX(),found.getY(),found.getZ(),found.getYRot(),found.getXRot());tilly.setDeltaMovement(Vec3.ZERO);}
        h.asleep[T]=found.isSleeping();
        int place=-1;var seat=found.getVehicle() instanceof SeatEntity s?s:null;
        if(seat!=null)for(int i=0;i<PellVisit.TABLE.length;i++)if(seat.support().equals(b.offset(PellVisit.TABLE[i].chair())))place=i;
        h.seated[T]=place>=0;h.place[T]=place;
        h.perch[T]=seat!=null&&seat.support().equals(b.offset(PellVisit.TILLY_BED.cell()))?PellVisit.TILLY_BED:null;
        h.way[T].clear();h.sitAt[T]=-1;h.perchTo[T]=null;h.sleepAt[T]=null;
    }
    /** After the visit's tick: Tilly where the reader is (or the reader where June leads Tilly), their size, the cane's hook, and the look. */
    static void after(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        if(!played(h)){hint(l,b,h,here);return;}
        var p=reader(l,h);var tilly=PellPeople.find(l,b,T);if(p==null||tilly==null)return;
        small(p,true);
        if(!tilly.isInvisible())tilly.setInvisible(true);
        if(!tilly.getMainHandItem().isEmpty())tilly.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        if(tilly.saying())tilly.hush();
        tilly.setAirSupply(tilly.getMaxAirSupply());
        h.moving[T]=true;
        if(led(h))carry(l,b,h,p,tilly,now);
        else{
            if(p.getVehicle()==tilly)letGo(p);
            tilly.noPhysics=true;tilly.setNoGravity(true);
            tilly.moveTo(p.getX(),p.getY(),p.getZ(),p.getYRot(),p.getXRot());tilly.setYHeadRot(p.getYHeadRot());tilly.setYBodyRot(p.yBodyRot);tilly.setDeltaMovement(Vec3.ZERO);
            paint(l,b,h,p,now);
        }
        if(now%20==0)hook(l,b,1);
        if(now%LOOK_EVERY==0)look(l,p,gesture(h,p,tilly));
        // 0.4.546: the cane in their hand, and what only Tilly hears and feels (PellSenses).
        PellSenses.tick(l,b,h,p,now);
    }
    /** The pose Tilly's figure is drawn in: asleep in the bed, having her hair brushed, as June holds or leads her, or with her cane. */
    static int gesture(PellPeople.Household h,ServerPlayer p,LiteraryActor tilly){
        if(p.isSleeping())return PellPeople.ASLEEP;
        if(led(h))return tilly.gesture();
        if(h.step==PellVisit.BRUSHING&&h.perch[T]==PellVisit.TILLY_BED)return PellPeople.BRUSHED;
        if(h.visit>=PellWeek.LAST&&h.rite==PellRite.WATER)return PellPeople.LED;
        return PellPeople.CANE;
    }
    private static void look(ServerLevel l,ServerPlayer p,int gesture){
        var payload=new PellTillyPayload(p.getUUID(),gesture);
        for(var q:l.players())if(q.distanceToSqr(p)<LOOK_FAR*LOOK_FAR)HousePackets.send(q,payload);
    }
    /** Led: on June's Tilly, at her feet, wherever she goes. Woken first if asleep. */
    private static void carry(ServerLevel l,BlockPos b,PellPeople.Household h,ServerPlayer p,LiteraryActor tilly,long now){
        tilly.noPhysics=false;
        if(p.getVehicle()==tilly){if(h.tillyMounted>0&&now-h.tillyMounted==2)PellHouse.cue(p,PellTexts.TILLY_R_LED);return;}
        if(p.isSleeping())PellMorning.wake(p,null);
        h.sleptAt.remove(p.getUUID());
        if(p.isPassenger())letGo(p);
        if(p.startRiding(tilly,true))h.tillyMounted=now;
    }
    /** Off June's Tilly, the house's own doing. */
    static void letGo(ServerPlayer p){LETTING.add(p.getUUID());try{p.stopRiding();}finally{LETTING.remove(p.getUUID());}}
    /** While June leads her a reader as Tilly does not get down; nor off the edge of the bed while June brushes. */
    @SubscribeEvent public static void dismount(EntityMountEvent e){
        if(!e.isDismounting()||!(e.getEntityMounting() instanceof ServerPlayer p)||LETTING.contains(p.getUUID()))return;
        if(e.getEntityBeingMounted() instanceof LiteraryActor a&&a.pellStandIn()&&p.isAlive())e.setCanceled(true);
    }
    /** The last night, in the water and on the way out of it: a bare foot that finds the line unbroken is put back off it. */
    private static void paint(ServerLevel l,BlockPos b,PellPeople.Household h,ServerPlayer p,long now){
        if(h.visit<PellWeek.LAST||h.rite!=PellRite.WATER||p.isPassenger()){h.tillySafe=null;return;}
        if(!PellRite.onPaint(l,b,p.position())){h.tillySafe=p.position();return;}
        var back=h.tillySafe;if(back==null)return;
        p.teleportTo(l,back.x,back.y,back.z,p.getYRot(),p.getXRot());p.setDeltaMovement(Vec3.ZERO);
        if(now-h.tillyPaintAt>=200){PellHouse.cue(p,PellTexts.TILLY_R_PAINT);h.tillyPaintAt=now;}
    }
    /** A child's size while they are Tilly at the house, and their own again when they go. */
    static void small(ServerPlayer p,boolean on){
        var a=p.getAttribute(Attributes.SCALE);if(a==null)return;
        if(on){if(!a.hasModifier(SMALL)){a.addTransientModifier(new AttributeModifier(SMALL,PellPeople.size(PellPeople.FIRST+T)-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));p.refreshDimensions();}SMALLED.put(p.getUUID(),p);}
        else{if(a.hasModifier(SMALL)){a.removeModifier(SMALL);p.refreshDimensions();}SMALLED.remove(p.getUUID());}
    }
    /** For tests: whether a reader is a child's size here. */
    public static boolean small(ServerPlayer p){var a=p.getAttribute(Attributes.SCALE);return a!=null&&a.hasModifier(SMALL);}
    /**
     * The reader as Tilly has gone (away from the house, or not alive): their size their own, off June's Tilly, the look stopped, and
     * Tilly the house's again, where the visit has her.
     */
    static void leave(ServerLevel l,BlockPos b,PellPeople.Household h,UUID id){
        h.tillyReader=null;h.tillySafe=null;h.tillyMounted=0;
        var p=l.getServer().getPlayerList().getPlayer(id);
        if(p!=null){small(p,false);if(p.getVehicle() instanceof LiteraryActor a&&a.pellStandIn())letGo(p);}
        PellSenses.leave(p,h);
        var stop=new PellTillyPayload(id,-1);for(var q:l.players())HousePackets.send(q,stop);
        hook(l,b,0);
        var tilly=PellPeople.find(l,b,T);if(tilly==null)return;
        tilly.setInvisible(false);tilly.noPhysics=false;tilly.setNoGravity(false);
        resume(l,b,h,tilly);
    }
    /** Tilly the house's again: where the visit would have her, with her cane, unless June has her by the hand (PellRite has her then). */
    private static void resume(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor tilly){
        if(h.visit>=PellWeek.LAST&&h.rite>=PellRite.WAKE&&h.rite!=PellRite.KEPT){if(h.rite==PellRite.WATER&&!h.inHold)tilly.setNoGravity(PellSite.inWater(tilly.position().subtract(b.getX(),b.getY(),b.getZ())));return;}
        h.asleep[T]=false;h.perch[T]=null;h.seated[T]=false;h.place[T]=-1;
        PellVisit.dress(tilly,T);
        int s=h.step;
        if(s>=PellVisit.TUCKED&&s<PellMorning.CALLED||h.rite==PellRite.KEPT){PellVisit.lie(l,b,h,T,tilly,PellVisit.TILLY_SLEEPS);return;}
        if(s>=PellVisit.BEDTIME&&s<PellVisit.TUCKED){PellVisit.perch(l,b,h,T,tilly,PellVisit.TILLY_BED);return;}
        int t=-1;for(int i=0;i<PellVisit.TABLE.length;i++)if(h.diner[i]==T)t=i;
        boolean meal=s>=PellVisit.CALLED&&s<PellVisit.CLEARING||s>=PellMorning.CALLED&&s<PellMorning.AFTER;
        if(meal&&t>=0){PellVisit.sit(l,b,h,T,tilly,t);return;}
        if(s>=PellVisit.SETTLED)PellVisit.stand(l,b,h,T,PellVisit.TILLY_ROOM);
    }
    /** The cane on the hook inside the front door (stage 0), or not (1): it goes with the reader who is Tilly, and back when they go. */
    private static void hook(ServerLevel l,BlockPos b,int stage){
        var at=b.offset(HOOK);var st=l.getBlockState(at);
        if(PellThing.is(st,PellThing.Kind.COAT_HOOK)&&st.getValue(PellThing.STAGE)!=stage&&st.getValue(PellThing.STAGE)<2)l.setBlock(at,st.setValue(PellThing.STAGE,stage),3);
    }
    /** The visit begun again: the cane on its hook unless a reader is Tilly here, and nothing asked, waited for or brought. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){
        hook(l,b,played(h)?1:0);
        h.tillyAskedAt=0;h.tillyAnswered=false;h.tillySaidYes=false;h.tillyFetchAt=0;h.tillyPaintAt=0;h.tillySafe=null;h.bedAt=0;
    }
    /**
     * Twice a second: a reader made small here who is no longer at the house (gone through its door, dead, elsewhere) has their own size
     * back at once, even when nobody is left at the house to tick it.
     */
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e){
        if(SMALLED.isEmpty()||e.getServer().getTickCount()%10!=0)return;
        for(var p:List.copyOf(SMALLED.values()))if(p.isRemoved()||!p.isAlive()||LiteraryVignettes.current(p)!=PellHouse.PLACE){small(p,false);PellSenses.takeCane(p);}
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        if(p.getVehicle() instanceof LiteraryActor a&&a.pellStandIn())letGo(p);
        small(p,false);PellSenses.takeCane(p);
    }

    // ------------------------------------------------------------------------------------------------ the cane
    /** On the first visit, once June has the front door open and before her tour is done, every reader there is told of the cane once. */
    private static void hint(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here){
        if(h.visit!=1||h.step<PellVisit.TOUR||h.step>=PellVisit.SETTLED||l.getGameTime()%20!=0)return;
        var first=new ArrayList<ServerPlayer>();
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&!PellHouse.own(p).getBoolean("FirstVisit"))first.add(p);
        if(first.size()<2)return;
        for(var p:first){var own=PellHouse.own(p);if(own.getBoolean(HINTED))continue;own.putBoolean(HINTED,true);PellHouse.save(p,own);PellHouse.cue(p,PellTexts.CANE_HINT);}
    }
    /**
     * A hand on the coat hook: on the first visit, from when June opens the front door until her tour is done, with another reader at the
     * house and nobody yet Tilly here, the cane is taken, and the reader who took it is Tilly. True if it was.
     */
    static boolean hook(ServerPlayer p,BlockPos b,BlockPos at,BlockState st,CompoundTag own){
        var h=PellPeople.household(b);var l=p.serverLevel();
        if(st.getValue(PellThing.STAGE)!=0||played(h)||plays(own)||own.contains(TILLY_ID)||own.getBoolean("FirstVisit"))return false;
        if(h.visit!=1||h.step<PellVisit.TOUR||h.step>=PellVisit.SETTLED)return false;
        var others=new ArrayList<ServerPlayer>();
        for(var q:PellHouse.readers(l,b))if(q!=p&&q.isAlive()&&!q.isSpectator()&&!PellHouse.own(q).getBoolean("Ended"))others.add(q);
        if(others.isEmpty())return false;
        own.putBoolean(PLAYS,true);own.putBoolean(HINTED,true);
        l.setBlock(at,st.setValue(PellThing.STAGE,1),3);
        l.playSound(null,at,SoundEvents.BAMBOO_WOOD_HIT,SoundSource.BLOCKS,.5F,1.4F);
        String name=p.getGameProfile().getName();var names=new ArrayList<String>();
        for(var q:others){
            names.add(q.getGameProfile().getName());
            var o=PellHouse.own(q);o.putUUID(TILLY_ID,p.getUUID());o.putString(TILLY_NAME,name);o.putBoolean(HINTED,true);PellHouse.save(q,o);
            q.displayClientMessage(Component.literal(PellTexts.caneTakenBy(name)).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);
        }
        p.displayClientMessage(Component.literal(PellTexts.caneTaken(String.join(" and ",names))).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);
        h.tillyReader=p.getUUID();
        return true;
    }

    // ------------------------------------------------------------------------------------------------ her lines, and the name
    /** Tilly's line, to the reader playing her, in grey: true if a reader is Tilly here (and nobody else hears it). */
    static boolean prompt(PellPeople.Household h,LiteraryActor tilly,String line){
        if(!played(h)||!(tilly.level() instanceof ServerLevel l))return false;
        var p=reader(l,h);if(p==null)return false;
        p.displayClientMessage(Component.literal(PellTexts.mightSay(line)).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);
        return true;
    }
    /** In June's hands in the water, June has said the name: the reader is asked, on the panel. */
    static void ask(ServerPlayer p,BlockPos b,String line){
        if(!Talk.key(p).equals(RITE_TALK))Talk.begin(p,RITE_TALK,PellPeople.name(J));
        Talk.line(p,PellPeople.name(J),line);
        Talk.ask(p,Talk.reply(PellTexts.ANSWER_YES,q->PellRite.answer(q,b)),Talk.reply(PellTexts.ANSWER_OWN,q->PellRite.ownName(q,b)),Talk.act(PellTexts.ANSWER_NOTHING,q->{}));
    }
    /**
     * A week in, June has said the name at bedtime or at breakfast: the reader as Tilly is asked, on the panel, with Tilly's own answer
     * and the one June wants. An answer to the name is kept, and June hears it.
     */
    static void askName(ServerPlayer p,BlockPos b,String june,String own,String yes){
        Talk.begin(p,NAME_TALK,PellPeople.name(J));Talk.line(p,PellPeople.name(J),june);
        Talk.ask(p,Talk.reply(own,q->named(q,b,false)),Talk.reply(yes,q->named(q,b,true)),Talk.act(PellTexts.ANSWER_NOTHING,q->Talk.close(q)));
    }
    private static void named(ServerPlayer p,BlockPos b,boolean yes){
        var h=PellPeople.household(b);if(!isTilly(h,p))return;
        var own=PellHouse.own(p);own.putInt(yes?"AnsweredNessa":"SaidOwnNameBefore",own.getInt(yes?"AnsweredNessa":"SaidOwnNameBefore")+1);PellHouse.save(p,own);
        h.tillySaidYes=yes;h.tillyAnswered=true;Talk.close(p);
    }

    // ------------------------------------------------------------------------------------------------ bed
    /**
     * A hand on Nessa's bed by the reader as Tilly: at bedtime, they sit on its edge for June to brush their hair; at night, they lie down
     * in it (and while every reader at the house is asleep the night hurries on, as it does for the camp bed). True if the bed answered.
     */
    static boolean bed(ServerPlayer p,BlockPos b,BlockPos at,BlockState st,CompoundTag own){
        var h=PellPeople.household(b);if(!isTilly(h,p)||p.isPassenger()||p.isSleeping())return false;
        var l=p.serverLevel();
        if(h.step==PellVisit.BEDTIME&&h.perch[J]==PellVisit.JUNE_BED){sitOnBed(l,b,p);return true;}
        boolean night=h.step>=PellVisit.TUCKED&&h.step<PellMorning.CALLED&&!(h.visit>=PellWeek.LAST&&h.rite>=PellRite.FETCH);
        if(!night)return false;
        lieDown(l,b,h,p,own);return true;
    }
    private static void sitOnBed(ServerLevel l,BlockPos b,ServerPlayer p){
        var seat=HouseSitting.SEAT.get().create(l);if(seat==null)return;
        var c=b.offset(PellVisit.TILLY_BED.cell());seat.support(c);seat.hold();
        var at=PellVisit.world(b,PellVisit.TILLY_BED.at());seat.moveTo(at.x,at.y+9/16.0,at.z);
        if(!l.addFreshEntity(seat)||!p.startRiding(seat)){seat.discard();return;}
        float yaw=PellVisit.TILLY_BED.look();p.setYRot(yaw);p.setYHeadRot(yaw);
        PellHouse.cue(p,PellTexts.TILLY_R_SIT);
    }
    private static void lieDown(ServerLevel l,BlockPos b,PellPeople.Household h,ServerPlayer p,CompoundTag own){
        if(p.isPassenger())letGo(p);
        var head=b.offset(PellVisit.TILLY_BED.cell());var st=l.getBlockState(head);if(!PellThing.is(st,PellThing.Kind.BED))return;
        p.startSleeping(PellThing.pillow(head,st));if(!p.isSleeping())return;
        h.sleptAt.put(p.getUUID(),l.getGameTime());
        HousePackets.send(p,new HouseFadePayload(PellMorning.FADE_IN,PellMorning.FADE_HOLD,PellMorning.FADE_OUT));
        PellHouse.cue(p,PellTexts.TILLY_R_LIE);
    }
    /** Brushed and goodnight said: June tucks the reader in, if they are by the bed. */
    static void tuck(ServerLevel l,BlockPos b,PellPeople.Household h){
        var p=reader(l,h);if(p==null)return;
        var own=PellHouse.own(p);int visits=own.getInt("TillyBrushedVisits");
        if((visits&1<<h.visit)==0){own.putInt("TillyBrushedVisits",visits|1<<h.visit);own.putInt("TillyBrushed",own.getInt("TillyBrushed")+1);PellHouse.save(p,own);}
        var near=PellVisit.world(b,PellVisit.TILLY_BED.at());if(p.position().distanceToSqr(near)>AT_BED*AT_BED)return;
        lieDown(l,b,h,p,own);PellHouse.cue(p,PellTexts.TILLY_R_TUCKED);
    }

    // ------------------------------------------------------------------------------------------------ midnight
    /**
     * Midnight on the last night, June at Nessa's bed: whether the reader as Tilly is by it, to be woken and led. If not, they are told she
     * is there saying the name; after a while the house brings them there (a moment's dark, and they are standing by the bed).
     */
    static boolean atBed(ServerLevel l,BlockPos b,PellPeople.Household h,long now){
        var p=reader(l,h);if(p==null)return true;
        var near=PellVisit.world(b,PellVisit.TILLY_BED.at());
        if(p.position().distanceToSqr(near)<=AT_BED*AT_BED&&Math.abs(p.getY()-near.y)<2){
            if(p.isSleeping())PellMorning.wake(p,null);
            h.sleptAt.remove(p.getUUID());h.tillyFetchAt=0;return true;
        }
        if(h.tillyFetchAt==0){h.tillyFetchAt=now;PellHouse.cue(p,PellTexts.TILLY_R_FETCH);return false;}
        if(now-h.tillyFetchAt<FETCH)return false;
        if(p.isSleeping())PellMorning.wake(p,null);
        if(p.isPassenger())letGo(p);
        h.sleptAt.remove(p.getUUID());
        HousePackets.send(p,new HouseFadePayload(10,30,20));
        var to=PellVisit.world(b,PellVisit.TILLY_BED.beside());
        p.teleportTo(l,to.x,to.y,to.z,PellVisit.TILLY_BED.look(),0);p.setDeltaMovement(Vec3.ZERO);
        PellHouse.cue(p,PellTexts.TILLY_R_FETCHED);h.tillyFetchAt=0;
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the house's hands on them
    /** For tests: a line of Tilly's, said as the visit says it (PellVisit.say). */
    public static void says(ServerLevel l,BlockPos b,String line){var t=PellPeople.find(l,b,T);if(t!=null)PellVisit.say(PellPeople.household(b),t,T,line,l.getGameTime());}
    /** A line in grey to the reader as Tilly here, if there is one. */
    static void cue(ServerLevel l,PellPeople.Household h,String line){var p=reader(l,h);if(p!=null)PellHouse.cue(p,line);}
    /**
     * A week in, the name put to the reader as Tilly on the panel, with June's line, Tilly's own answer and the one June wants. The first
     * call asks; then true once they have answered, or once the house has waited long enough without (the panel is closed). Whether they
     * answered to the name is kept (Household.tillySaidYes) until the next time it is asked.
     */
    static boolean asked(ServerLevel l,BlockPos b,PellPeople.Household h,long now,String june,String own,String yes){
        var p=reader(l,h);if(p==null){h.tillyAskedAt=0;return true;}
        if(h.tillyAskedAt==0){h.tillyAnswered=false;h.tillySaidYes=false;h.tillyAskedAt=now;askName(p,b,june,own,yes);return false;}
        if(!h.tillyAnswered&&now-h.tillyAskedAt<ANSWER_WAIT)return false;
        if(!h.tillyAnswered&&Talk.key(p).equals(NAME_TALK))Talk.close(p);
        h.tillyAskedAt=0;return true;
    }
    /** The last night, a reader as Tilly in the water and nothing answered yet: the night goes on to first light (PellVisit.clock). */
    static boolean holdingOut(PellPeople.Household h){return h.tillyReader!=null&&h.visit>=PellWeek.LAST&&h.rite==PellRite.WATER;}
    /** Answered, and June has them home: off June's Tilly, and put to bed in Nessa's bed. */
    static void kept(ServerLevel l,BlockPos b,PellPeople.Household h){
        var p=reader(l,h);if(p==null)return;
        if(p.isPassenger())letGo(p);
        var to=PellVisit.world(b,PellVisit.TILLY_BED.beside());p.teleportTo(l,to.x,to.y,to.z,PellVisit.TILLY_BED.look(),0);
        lieDown(l,b,h,p,PellHouse.own(p));PellHouse.cue(p,PellTexts.TILLY_R_KEPT);
    }
    /** First light after answering: June stands them at Nessa's window. */
    static void window(ServerLevel l,BlockPos b,PellPeople.Household h,Vec3 at,float look){
        var p=reader(l,h);if(p==null)return;
        if(p.isSleeping())PellMorning.wake(p,null);
        h.sleptAt.remove(p.getUUID());
        if(p.isPassenger())letGo(p);
        HousePackets.send(p,new HouseFadePayload(10,30,20));
        var to=PellVisit.world(b,at);p.teleportTo(l,to.x,to.y,to.z,look,0);p.setDeltaMovement(Vec3.ZERO);
        PellHouse.cue(p,PellTexts.TILLY_R_WINDOW);
    }

    // ------------------------------------------------------------------------------------------------ a hand on the reader as Tilly
    /**
     * In the last night's water and after it (until June has her home), another reader's hand on the reader as Tilly is a hand on Tilly
     * (PellRite.hand). At any other time two readers' hands are their own business.
     */
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract e){
        if(e.getLevel().isClientSide()||e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||!(e.getTarget() instanceof ServerPlayer q))return;
        if(LiteraryVignettes.current(p)!=PellHouse.PLACE)return;
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null)return;
        var h=PellPeople.household(b);if(!isTilly(h,q)||h.visit<PellWeek.LAST)return;
        if(!(h.rite>=PellRite.WAKE&&h.rite<=PellRite.HOME||h.rite==PellRite.OUT))return;
        var tilly=PellPeople.find(p.serverLevel(),b,T);if(tilly==null)return;
        LiteraryVignettes.talk(p,tilly);
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
    }
}
