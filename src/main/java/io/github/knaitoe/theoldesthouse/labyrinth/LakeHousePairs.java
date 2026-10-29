package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.LakeHouseViewPayload;
import io.github.knaitoe.theoldesthouse.network.LakePartnerPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * The lake house in pairs (owner, October 28; docs/LAKE_HOUSE_PAIRS_0_4_156.md). A second reader inhabits Tess's role. To
 * the diver they look like Tess, or the young man in her place, by the diver's own partner. The house they swim through is
 * the diver's: its lights, the dresses, the figure and the dawn after staying are shown to both. Tess's lines come to the
 * partner as grey prompts to say in their own words, or not; Tess herself stays away while they are there. Each picks up
 * their own lent gear, and either may mind the compressor. When the diver's ending lands, the partner has an account of
 * their own, from Tess's side, and its Witness credit.
 * <p>Who goes down with whom is chosen by {@link SceneInstances} (the second reader joins the first, a third gets a copy
 * of their own with Tess, a fourth joins them) and saved there.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LakeHousePairs {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAKE_HOUSE;
    /** In the partner's own record: their ending came from Tess's side, the diver's name, and who the diver went down with. */
    public static final String SIDE="PartnerSide0156",DIVER_NAME="DiverName0156",AS="PartnerAs0156";
    private static final String INTRO="PartnerIntro0156",PROMPTED="PartnerPrompt0156",GOODBYE="PartnerGoodbye0156";
    /** A second try at the door within this long lets a waiting partner go down without their diver. */
    public static final int RELEASE_TICKS=200;
    private static final Map<UUID,Long> ASKED=new HashMap<>();
    /** Divers online but not at the lake, refreshed every two seconds: their partners are not dealt the lake alone. */
    private static final Set<UUID> AWAY=new HashSet<>();
    private LakeHousePairs(){}

    // ------------------------------------------------------------------------------------------------ who goes with whom
    public static boolean isPartner(ServerPlayer p){return SceneInstances.diverOf(LabyrinthData.get(p.server),p.getUUID())!=null;}
    /** A diver's story is under way: it can still be dealt and its ending has not landed. */
    public static boolean diving(LabyrinthData d,UUID diver){return LiteraryVignettes.canDeal(d,diver,PLACE)&&!LiteraryVignettes.personal(d,diver,PLACE).getBoolean("Ready");}
    /** The record that holds the story a reader is in: the diver's for a partner, the reader's own otherwise. */
    public static CompoundTag story(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var diver=SceneInstances.diverOf(d,p.getUUID());return LiteraryVignettes.personal(d,diver!=null?diver:p.getUUID(),PLACE);
    }
    public static UUID storyHolder(ServerPlayer p){var diver=SceneInstances.diverOf(LabyrinthData.get(p.server),p.getUUID());return diver!=null?diver:p.getUUID();}
    /** The diver's partner, if they are in the same lake as the diver now. */
    public static @Nullable ServerPlayer partnerHere(ServerPlayer diver){
        var id=SceneInstances.partnerOf(LabyrinthData.get(diver.server),diver.getUUID());if(id==null)return null;
        var q=diver.server.getPlayerList().getPlayer(id);return q!=null&&together(q,diver)?q:null;
    }
    private static boolean together(ServerPlayer a,ServerPlayer b){
        if(!a.isAlive()||a.isSpectator()||!b.isAlive()||b.isSpectator()||!LakeHouse.inside(a)||!LakeHouse.inside(b))return false;
        return Objects.equals(LakeHouse.readerBase(a),LakeHouse.readerBase(b));
    }
    /** Whether a viewer is the partner of an actor's or display's owner, so they see what the diver sees. */
    public static boolean sharesWith(ServerPlayer viewer,UUID owner){return owner.equals(SceneInstances.diverOf(LabyrinthData.get(viewer.server),viewer.getUUID()));}
    /** Not dealt the lake while their diver is online but somewhere else. A diver who is offline leaves the partner the choice at the door. */
    public static boolean waiting(LabyrinthData d,UUID reader){var diver=SceneInstances.diverOf(d,reader);return diver!=null&&AWAY.contains(diver)&&diving(d,diver);}
    public static void refresh(MinecraftServer s){
        AWAY.clear();var d=LabyrinthData.get(s);
        for(var q:s.getPlayerList().getPlayers())if(SceneInstances.partnerOf(d,q.getUUID())!=null&&!LakeHouse.inside(q))AWAY.add(q.getUUID());
    }

    // ------------------------------------------------------------------------------------------------ the door
    /** A new reader has joined a diver alone in their copy. */
    static void paired(ServerPlayer partner,ServerPlayer diver){
        var d=LabyrinthData.get(partner.server);var own=LiteraryVignettes.personal(d,partner.getUUID(),PLACE);
        own.putString(DIVER_NAME,diver.getGameProfile().getName());
        own.putString(AS,LiteraryVignettes.personal(d,diver.getUUID(),PLACE).getString(LakeHousePartner.KEY));
        LiteraryVignettes.save(d,partner.getUUID(),PLACE,own);
    }
    /** The door stays shut for a partner whose diver is not at the lake; trying it again soon after lets them go alone. */
    public static void waits(ServerPlayer p,UUID diver){
        p.displayClientMessage(Component.literal("The lake waits for "+name(p,diver)+". Try the door again to go down without them."),true);
    }
    static boolean release(ServerPlayer p){
        long now=p.level().getGameTime();var last=ASKED.get(p.getUUID());
        if(last!=null&&now-last>=10&&now-last<=RELEASE_TICKS){ASKED.remove(p.getUUID());return true;}
        ASKED.put(p.getUUID(),now);return false;
    }
    static void released(ServerPlayer p,UUID diver){p.displayClientMessage(Component.literal("You go down without "+name(p,diver)+"."),true);}
    private static String name(ServerPlayer p,UUID diver){
        var online=p.server.getPlayerList().getPlayer(diver);if(online!=null)return online.getGameProfile().getName();
        var saved=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE).getString(DIVER_NAME);return saved.isEmpty()?"the one you went down with":saved;
    }

    // ------------------------------------------------------------------------------------------------ in Tess's place
    /** Tess's line, offered to the partner in grey to say or not. */
    public static void prompt(ServerPlayer partner,String text){
        var as=LakeHousePartner.of(story(partner));
        partner.displayClientMessage(Component.literal(as.name+" might say: “"+text+"”").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);
    }
    /** The visit's first words on the dock, as Tess says them to a reader alone. */
    static String greeting(LakeHousePartner.Partner as,int visit){
        return visit>=4?"Something was in my room last night. The floor was wet, and the water was warm, like that room by the pool. We have to go back and say hello."
            :visit==1?LakeHousePartner.firstGreeting(as):visit==2?"I'll keep the compressor going. Pull twice if you need me."
            :"The tanks are filled. Let's see what's upstairs. And there were bubbles coming out round the wardrobe in the study.";
    }
    static String goodbye(String outcome){
        return switch(outcome){
            case LakeHouse.LEFT_THE_LIGHTS_ON->"It's still lit, isn't it. I'm not going back down. I don't think you should either.";
            case LakeHouse.STAYED->"You didn't come up. I sat on that raft until it got light. I can't do that again.";
            default->"I came out here twice on my own. Just sat in the canoe and looked down. There's nothing there. I have to stop coming.";
        };
    }
    /**
     * Every five ticks for a partner in the lake. Returns false for a reader who is not anyone's partner, whose own story
     * then runs as usual.
     */
    static boolean partner(ServerPlayer p,BlockPos b,CompoundTag own){
        var d=LabyrinthData.get(p.server);var diverId=SceneInstances.diverOf(d,p.getUUID());if(diverId==null)return false;
        var story=LiteraryVignettes.personal(d,diverId,PLACE);var as=LakeHousePartner.of(story);
        var diver=p.server.getPlayerList().getPlayer(diverId);boolean together=diver!=null&&together(p,diver);
        // Visits are the diver's: a partner arriving with them keeps step.
        own.putInt("Visit",story.getInt("Visit"));if(!own.getBoolean("Ready"))own.putInt("Beat",story.getInt("Beat"));
        HousePackets.send(p,new LakeHouseViewPayload(1|(story.getBoolean("Lit")?2:0)|(LakeHouse.STAYED.equals(story.getString("Outcome"))?4:0)));
        if(own.getInt("Finish")>0){LakeHouseStory.finishTick(p,b,own);return true;}
        if(together&&own.getInt("T")%20==5){var look=new LakePartnerPayload(p.getUUID(),diverId,as.role);HousePackets.send(p,look);HousePackets.send(diver,look);}
        boolean talking=together&&!p.isEyeInFluid(FluidTags.WATER)&&p.distanceToSqr(diver)<144;
        if(talking&&!own.getBoolean(INTRO)){
            own.putBoolean(INTRO,true);
            String pronoun=as==LakeHousePartner.Partner.THEO?"His":"Her";
            p.displayClientMessage(Component.literal("To "+diver.getGameProfile().getName()+", you are "+as.name+". "+pronoun+" lines come to you in grey: say them in your own words, or don't.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);
        }
        if(talking&&story.getBoolean("Ready")&&story.getInt("Visit")>story.getInt("EndVisit")&&!own.getBoolean(GOODBYE)){own.putBoolean(GOODBYE,true);prompt(p,goodbye(story.getString("Outcome")));}
        if(LakeHouse.empty(p))return true;
        var r=p.position().subtract(Vec3.atLowerCornerOf(b));boolean inside=LakeHouseStory.withinWalls(r);
        var view=story.copy();view.putInt("T",own.getInt("T"));LakeHouseStory.lightView(p,b,view,inside);
        if(own.getBoolean("Ready"))return true;
        int visit=Math.max(1,story.getInt("Visit"));
        if(talking&&!story.getBoolean("Ready")&&own.getInt(PROMPTED)<visit){
            own.putInt(PROMPTED,visit);own.putBoolean(LakeHousePartner.MET,true);prompt(p,greeting(as,visit));
            // The diver has met their partner on the dock: their account and Witness line may name them.
            if(!story.getBoolean(LakeHousePartner.MET)){story.putBoolean(LakeHousePartner.MET,true);LiteraryVignettes.save(d,diverId,PLACE,story);}
        }
        // Their own lent gear, by the diver's visit: the flashlight by the boathouse door, the helmet or the tanks on the raft.
        if(r.z>-10&&LakeHouseGear.give(p,LakeHouseRegistry.FLASHLIGHT.get()))p.displayClientMessage(Component.literal("A flashlight hangs on a nail by the door. You take it."),true);
        if(LakeHouse.onRaft(r)){
            if(visit==2&&LakeHouseGear.give(p,LakeHouseRegistry.HELMET.get()))p.displayClientMessage(Component.literal("A second gold diving helmet sits on the raft, its hose coupled to the compressor."),true);
            else if(visit>=3&&LakeHouseGear.give(p,LakeHouseRegistry.TANKS.get()))p.displayClientMessage(Component.literal("A second pair of air tanks lies on the raft, filled."),true);
        }
        LakeHouseDive.hose(p,b,own);
        if(story.getBoolean("RuleBroken")&&inside&&own.getInt("T")%100==5)ThreeFingers.hear(p,SoundEvents.DEEPSLATE_STEP,b.offset(0,-19,-165-(own.getInt("T")/100%8)),.6F,.75F);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the ending
    /**
     * The diver's ending has landed: the partner's own account is ready, from Tess's side. A partner there with them comes
     * up with them; one elsewhere is told where the account waits.
     */
    static void ready(ServerPlayer diver,CompoundTag diverOwn,String ending){
        var d=LabyrinthData.get(diver.server);var id=SceneInstances.partnerOf(d,diver.getUUID());if(id==null)return;
        var own=LiteraryVignettes.personal(d,id,PLACE);if(own.getBoolean("Ready"))return;
        own.putBoolean("Ready",true);own.putString("Outcome",ending);own.putBoolean(SIDE,true);own.putBoolean(LakeHousePartner.MET,true);
        own.putString(DIVER_NAME,diver.getGameProfile().getName());own.putString(AS,diverOwn.getString(LakeHousePartner.KEY));
        own.putBoolean("RuleBroken",diverOwn.getBoolean("RuleBroken"));own.putInt("EndVisit",diverOwn.getInt("Visit"));own.putInt("Beat",diverOwn.getInt("Visit"));
        var partner=diver.server.getPlayerList().getPlayer(id);
        if(partner!=null&&together(partner,diver)){own.putInt("Finish",100);HousePackets.send(partner,new HouseFadePayload(18,50,32));partner.stopRiding();}
        else if(partner!=null)partner.displayClientMessage(Component.literal("Out at the lake, "+diver.getGameProfile().getName()+" has come up for the last time. Your account waits in the boathouse."),false);
        LiteraryVignettes.save(d,id,PLACE,own);
    }
    /** Where a partner comes to with their diver at the end. */
    static String surfaced(CompoundTag own){
        String diver=own.getString(DIVER_NAME);
        return switch(own.getString("Outcome")){
            case LakeHouse.SAID_HELLO->"You come up beside "+diver+" into an empty lake. There is no roof beneath the water.";
            case LakeHouse.STAYED->"It is getting light. "+diver+" is lying on the raft beside you, wet through.";
            default->"From the raft you can still see it, lit all the way down.";
        };
    }

    @SubscribeEvent public static void stopped(ServerStoppedEvent e){ASKED.clear();AWAY.clear();}
}
