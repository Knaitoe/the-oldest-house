package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** Four whole visits. Knowledge, originals, cast identities and physical exits are private saved facts. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class BorrascaStory {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.BORRASCA;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockPos FRONT_LEVER=new BlockPos(-15,13,-168);
    private BorrascaStory(){}
    public static void arrive(ServerPlayer p,CompoundTag own){
        int visit=own.getInt("Visit");if(visit==0)visit=1;else if(own.getBoolean("BorrascaDeparted")&&own.getInt("Beat")>=visit)visit=Math.min(4,visit+1);
        own.putInt("Visit",visit);own.putBoolean("BorrascaDeparted",false);own.putBoolean("Here",true);own.putBoolean("Interrupted",false);own.remove("ExitEligible");own.putInt("Present",0);
        if(!own.contains("EvidenceToken"))own.putUUID("EvidenceToken",UUID.randomUUID());
        var b=BorrascaSites.base(p.server,p.getUUID());if(b==null)return;
        // Cast are created once, after native entity sections load. Absent roles wait away from the public route.
        own.putBoolean("CastRecalled",false);
        HousePackets.send(p,new BorrascaViewPayload(b.offset(BorrascaRooms.MACHINE),visit,true,!own.getBoolean("PowerOff")));p.displayClientMessage(Component.literal(switch(visit){case 1->"Drisking. Kyle and Kimber are waiting by the diner.";case 2->"The town remembers you. A bedroom window is still open.";case 3->"Kimber's place is empty. The ridge is not quiet.";default->"The same road. This time, you know where it goes. The way back remains open.";}),false);
    }
    public static ItemStack markEvidence(ServerPlayer p,ItemStack stack){var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);if(!own.hasUUID("EvidenceToken"))own.putUUID("EvidenceToken",UUID.randomUUID());CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putUUID("BorrascaEvidence",own.getUUID("EvidenceToken")));LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);return stack;}
    public static boolean carriesEvidence(ServerPlayer p,CompoundTag own){if(!own.hasUUID("EvidenceToken"))return false;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);var c=s.get(DataComponents.CUSTOM_DATA);if(c==null)continue;var t=c.copyTag();if(t.hasUUID("BorrascaEvidence")&&t.getUUID("BorrascaEvidence").equals(own.getUUID("EvidenceToken"))&&t.hasUUID(LiteraryVignettes.OWNER)&&t.getUUID(LiteraryVignettes.OWNER).equals(p.getUUID()))return true;}return false;}
    public static void read(ServerPlayer p,String key,CompoundTag own){if(!BorrascaSites.inside(p)||!own.getBoolean("Here"))return;if(key.equals("Evidence")&&own.getInt("Visit")==4&&own.getBoolean("Consent"))own.putBoolean("EvidenceRead",true);if(key.equals("Testimony")&&own.getInt("Visit")==4&&own.getBoolean("Consent"))own.putBoolean("TestimonyRead",true);}
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        if(!own.getBoolean("Read_Source")){p.displayClientMessage(Component.literal("There is a content note on the porch. Read it before following the town's story."),true);return true;}
        int visit=own.getInt("Visit");
        if(rel.equals(BorrascaRooms.CARVING)){if(visit==1&&own.getBoolean("TreeReached")&&own.getBoolean("MetKyle")&&own.getBoolean("MetKimber")){own.putInt("Beat",1);p.displayClientMessage(Component.literal("Three names in the wood. Your way home is still behind the last house. You can return."),false);}return true;}
        if(rel.equals(BorrascaRooms.NOTICE)){p.displayClientMessage(Component.literal(visit<2?"A sun-faded noticeboard. Nothing fresh has been pinned up.":visit==2?"Whitney's photograph. The family has written 'still missing' beneath the closure stamp.":"Kimber's photograph has been pinned beside Whitney's."),false);return true;}
        String paper=rel.equals(BorrascaRooms.BEDROOM.above())?"Bedroom":rel.equals(BorrascaRooms.REGISTER)?"Register":rel.equals(BorrascaRooms.MAP)?"Map":rel.equals(BorrascaRooms.SERVICE)?"Service":rel.equals(BorrascaRooms.EVIDENCE)?"Evidence":rel.equals(BorrascaRooms.TESTIMONY)?"Testimony":null;
        if(paper!=null){boolean allowed=switch(paper){case "Bedroom","Register","Map"->visit>=2;case "Service"->visit>=3;default->visit==4&&own.getBoolean("Consent");};if(!allowed){p.displayClientMessage(Component.literal("The drawer is closed. There is no paper addressed to this visit."),true);return true;}LiteraryVignettes.open(p,PLACE,own,paper,BorrascaTexts.document(paper),b.offset(rel));return true;}
        if(rel.equals(BorrascaRooms.SIGN)){if(visit>=3){own.putBoolean("ReadSign",true);p.displayClientMessage(Component.literal("Letters missing from an ordinary mine sign. The story about skinned men began here."),false);}return true;}
        if(rel.equals(BorrascaRooms.TRUCK)||rel.distManhattan(new BlockPos(12,13,-160))<=1){if(visit>=3){own.putBoolean("RecognisedTruck",true);p.displayClientMessage(Component.literal("D-417. The same company that signs the county's maintenance payments."),false);}return true;}
        if(rel.equals(FRONT_LEVER)){if(visit<3){p.displayClientMessage(Component.literal("The service office is closed. The county keeps its survey downstairs."),true);return true;}if(visit==4&&!own.getBoolean("Consent")){var icons=new HashMap<Integer,ItemStack>();icons.put(0,LiteraryChoiceMenu.icon(Items.WRITTEN_BOOK,"Continue the non-graphic investigation"));icons.put(8,LiteraryChoiceMenu.icon(Items.OAK_DOOR,"Keep the office closed"));LiteraryChoiceMenu.open(p,"Abduction and reproductive abuse: continue?",icons,()->BorrascaSites.inside(p)&&p.distanceToSqr(b.offset(FRONT_LEVER).getCenter())<=36,slot->{if(slot==8)return true;if(slot!=0)return false;var d=LabyrinthData.get(p.server);var state=LiteraryVignettes.personal(d,p.getUUID(),PLACE);state.putBoolean("Consent",true);state.putBoolean("OfficeOpened",true);openDoor(p.serverLevel(),b.offset(BorrascaRooms.FACTORY_DOOR),true);LiteraryVignettes.save(d,p.getUUID(),PLACE,state);return true;});return true;}own.putBoolean("OfficeOpened",true);openDoor(p.serverLevel(),b.offset(BorrascaRooms.FACTORY_DOOR),true);return true;}
        if(rel.equals(BorrascaRooms.POWER)){if(visit>=3){boolean running=!own.getBoolean("PowerOff");own.putBoolean("PowerOff",running);var s=p.serverLevel().getBlockState(b.offset(BorrascaRooms.MACHINE));if(s.is(BorrascaRegistry.MACHINE.get()))p.serverLevel().setBlock(b.offset(BorrascaRooms.MACHINE),s.setValue(BorrascaMachineBlock.RUNNING,!running),F);var lever=p.serverLevel().getBlockState(b.offset(rel));if(lever.is(Blocks.LEVER))p.serverLevel().setBlock(b.offset(rel),lever.setValue(LeverBlock.POWERED,!running),F);if(running)p.serverLevel().playSound(null,b.offset(BorrascaRooms.MACHINE),BorrascaRegistry.MOTOR_STOP.get(),SoundSource.BLOCKS,1,.9F);}return true;}
        if(rel.equals(BorrascaRooms.RELEASE)){if(visit==4&&own.getBoolean("EvidenceRead")&&own.getBoolean("TestimonyRead")&&carriesEvidence(p,own)){own.putBoolean("ReleaseOpened",true);openDoor(p.serverLevel(),b.offset(-17,12,-206),true);var s=p.serverLevel().getBlockState(b.offset(rel));if(s.is(Blocks.LEVER))p.serverLevel().setBlock(b.offset(rel),s.setValue(LeverBlock.POWERED,true),F);p.displayClientMessage(Component.literal("The service latch releases. The survey marked a descent west of the trees."),false);}else p.displayClientMessage(Component.literal("The fire release is here. First read the statement and take the original register from the office."),true);return true;}
        return false;
    }
    static void openDoor(ServerLevel l,BlockPos at,boolean open){for(int y=0;y<2;y++){var s=l.getBlockState(at.above(y));if(s.getBlock() instanceof DoorBlock)l.setBlock(at.above(y),s.setValue(DoorBlock.OPEN,open),F);}}
    public static void talk(ServerPlayer p,LiteraryActor actor,CompoundTag own){
        int visit=own.getInt("Visit");if(actor.role()==LiteraryActor.BORRASCA_KYLE){own.putBoolean("MetKyle",true);actor.say(switch(visit){case 1->"Kimber says the three trees count as one. We left you room on the platform.";case 2->"Whitney's bag is still upstairs. People who choose to leave take their things.";case 3->"Kimber didn't come home. I'm going to find the road on that survey.";default->"If I don't come back, don't let them close the book.";});}
        else if(actor.role()==LiteraryActor.BORRASCA_KIMBER){own.putBoolean("MetKimber",true);actor.say(visit==1?"Climb up with us. When that grinding starts, you can hear the ridge from there.":"The office closed her case. Ask what they actually looked at before they did.");}
        else if(actor.role()==LiteraryActor.BORRASCA_SHERIFF)actor.say(visit<3?"People leave towns. A closed file isn't an invitation to follow them.":"You should have taken the main road home.");
        else actor.say("The mine has been closed for years. Nothing for a visitor up there.");
    }
    private static @Nullable LiteraryActor actor(ServerPlayer p,BlockPos b,CompoundTag own,String key,int role,BlockPos start){
        var l=p.serverLevel();if(own.hasUUID(key))return l.getEntity(own.getUUID(key)) instanceof LiteraryActor a?a:null;
        var at=b.offset(start);if(!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return null;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;a.bind(PLACE,p.getUUID());a.appearance(role,0);a.moveTo(Vec3.atBottomCenterOf(at),180,0);if(!l.addFreshEntity(a))return null;own.putUUID(key,a.getUUID());return a;
    }
    private static void placeWhenUnwatched(ServerPlayer p,LiteraryActor actor,BlockPos at){if(actor==null||actor.distanceToSqr(Vec3.atBottomCenterOf(at))<1)return;if(LiteraryVignettes.watched(p.serverLevel(),actor.getEyePosition())||LiteraryVignettes.watched(p.serverLevel(),at.getCenter()))return;if(!p.serverLevel().hasChunkAt(at))return;actor.moveTo(Vec3.atBottomCenterOf(at),180,0);actor.setDeltaMovement(Vec3.ZERO);}
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!own.getBoolean("Here")||LabyrinthDoors.isBusy(p))return;int visit=own.getInt("Visit");var kyle=actor(p,b,own,"CastKyle",LiteraryActor.BORRASCA_KYLE,new BlockPos(15,0,-24));var kimber=actor(p,b,own,"CastKimber",LiteraryActor.BORRASCA_KIMBER,new BlockPos(19,0,-24));var sheriff=actor(p,b,own,"CastSheriff",LiteraryActor.BORRASCA_SHERIFF,new BlockPos(-18,0,-26));var prescott=actor(p,b,own,"CastPrescott",LiteraryActor.BORRASCA_PRESCOTT,new BlockPos(24,0,-33));
        if(!own.getBoolean("CastRecalled")){placeWhenUnwatched(p,kyle,b.offset(visit>=4?new BlockPos(36,19,-209):visit==3?new BlockPos(-3,6,-95):new BlockPos(15,0,-24)));placeWhenUnwatched(p,kimber,b.offset(visit>=3?new BlockPos(39,19,-209):new BlockPos(19,0,-24)));if(kyle!=null&&kimber!=null&&sheriff!=null&&prescott!=null&&(visit<3||kimber.blockPosition().getX()>=b.getX()+34)&&(visit<4||kyle.blockPosition().getX()>=b.getX()+34))own.putBoolean("CastRecalled",true);}
        // Missing people are not displayed as victims. The original bodies wait in a sealed, inaccessible
        // records loft, outside native tracking; the next visit never invents a replacement entity.
        if(kyle!=null)kyle.setInvisible(visit>=4&&kyle.getX()>b.getX()+34);if(kimber!=null)kimber.setInvisible(visit>=3&&kimber.getX()>b.getX()+34);
        for(var a:new LiteraryActor[]{kyle,kimber,sheriff,prescott})if(a!=null&&p.distanceToSqr(a)<100&&!a.isInvisible()){a.getLookControl().setLookAt(p,20,15);a.setYHeadRot(net.minecraft.util.Mth.approachDegrees(a.getYHeadRot(),(float)Math.toDegrees(Math.atan2(-(p.getX()-a.getX()),p.getZ()-a.getZ())),8));}
        if(visit==1&&p.getY()>=b.getY()+6&&Math.abs(p.getX()-b.getX())<6&&p.getZ()<b.getZ()-89&&p.getZ()>b.getZ()-100){if(!own.getBoolean("TreeReached")){own.putBoolean("TreeReached",true);p.serverLevel().playSound(null,b.offset(0,6,-94),BorrascaRegistry.TREE_CREAK.get(),SoundSource.BLOCKS,.6F,1);p.displayClientMessage(Component.literal("From the platform, the sound belongs to the ridge. The carved plank is behind you."),false);}}
        if(visit==2&&own.getBoolean("Read_Bedroom")&&own.getBoolean("Read_Register")&&own.getBoolean("Read_Map")){own.putInt("Beat",2);if(!own.getBoolean("MapUnderstood")){own.putBoolean("MapUnderstood",true);p.displayClientMessage(Component.literal("The map puts the service road beyond the three trees. Return when you are ready to follow it."),false);}}
        boolean seesMachine=p.distanceToSqr(b.offset(BorrascaRooms.MACHINE).getCenter())<18*18&&sees(p,b.offset(BorrascaRooms.MACHINE).getCenter().add(-2.01,2,0));if(visit>=3&&seesMachine)own.putBoolean("SawMachine",true);
        if(visit==3&&own.getBoolean("ReadSign")&&own.getBoolean("RecognisedTruck")&&own.getBoolean("Read_Service")&&own.getBoolean("SawMachine")){own.putInt("Beat",3);if(!own.getBoolean("MachineUnderstood")){own.putBoolean("MachineUnderstood",true);p.displayClientMessage(Component.literal("It is an operating refinery. The same sound, the same truck, the same signatures. You know the way back."),false);}}
        if(visit==4&&p.isCrouching()&&p.getX()>b.getX()-22&&p.getX()<b.getX()-14&&p.getZ()<b.getZ()-207&&p.getZ()>b.getZ()-211)own.putBoolean("CrouchedUnderPipe",true);if(visit==4&&own.getBoolean("ReleaseOpened")&&own.getBoolean("EvidenceRead")&&own.getBoolean("TestimonyRead")&&carriesEvidence(p,own)){
            int step=own.getInt("EscapeStep");if(step<BorrascaRooms.RETURN_ROUTE.length&&p.distanceToSqr(Vec3.atBottomCenterOf(b.offset(BorrascaRooms.RETURN_ROUTE[step])))<16){own.putInt("EscapeStep",++step);}
            if(step==BorrascaRooms.RETURN_ROUTE.length&&own.getBoolean("CrouchedUnderPipe")&&p.getZ()>b.getZ()-15){own.putBoolean("EscapedWithOriginal",true);LiteraryVignettes.ready(p,PLACE,own,"carried_the_names_out_of_drisking");}
        }
        // The source remains fixed in the refinery. The client filters distance/enclosure, not story meaning.
        if(own.getInt("Present")%20==0)HousePackets.send(p,new BorrascaViewPayload(b.offset(BorrascaRooms.MACHINE),visit,own.getBoolean("Here"),!own.getBoolean("PowerOff")));
    }
    private static boolean sees(ServerPlayer p,Vec3 at){var hit=p.serverLevel().clip(new ClipContext(p.getEyePosition(),at,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));return hit.getType()==HitResult.Type.MISS||hit.getLocation().distanceToSqr(at)<1;}
    /** Called before travel only to capture eligibility; it never commits progress or grants a Witness. */
    public static void depart(ServerPlayer p){var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("ExitEligible",BorrascaSites.inside(p)&&own.getBoolean("Here")&&own.getBoolean("EscapedWithOriginal")&&own.getBoolean("Read_Ending")&&own.getBoolean("Read_Source")&&carriesEvidence(p,own)&&!own.getBoolean("Interrupted"));own.putBoolean("Here",false);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);HousePackets.send(p,new BorrascaViewPayload(BlockPos.ZERO,0,false,false));}
    /** Only the native door's successful travel callback, after its exact saved return is consumed. */
    public static void returned(ServerPlayer p){var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("BorrascaDeparted",true);if(own.getBoolean("ExitEligible")&&carriesEvidence(p,own)&&!own.getBoolean("Completed")){WitnessAccount.resolve(p,WitnessAccount.Story.BORRASCA,"carried_the_names_out_of_drisking");own.putBoolean("Completed",true);own.putInt("Beat",4);d.setCompleted(PLACE.id(),true);}own.remove("ExitEligible");LiteraryVignettes.save(d,p.getUUID(),PLACE,own);}
    @SubscribeEvent public static void died(LivingDeathEvent event){if(!(event.getEntity() instanceof ServerPlayer p)||!BorrascaSites.inside(p))return;var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("Here",false);own.remove("ExitEligible");LiteraryVignettes.save(d,p.getUUID(),PLACE,own);HousePackets.send(p,new BorrascaViewPayload(BlockPos.ZERO,0,false,false));}
    @SubscribeEvent public static void dropped(EntityJoinLevelEvent event){if(!(event.getEntity() instanceof ItemEntity item))return;var t=item.getItem().get(DataComponents.CUSTOM_DATA);if(t!=null&&t.copyTag().hasUUID("BorrascaEvidence")){item.setInvulnerable(true);item.setUnlimitedLifetime();}}
}
