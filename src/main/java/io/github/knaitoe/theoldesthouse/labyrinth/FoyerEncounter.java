package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;

/** A private standoff, actual admission and native exits; papers never serve as permission switches. */
public final class FoyerEncounter {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.MOTION_LIGHT;
    public static final BlockPos GLASS_DOOR=new BlockPos(0,0,-14),EXIT=new BlockPos(8,0,-5);
    /**
     * 0.4.201 (owner: "altering the light in motion threshold, both in it affecting the outcome and ensuring the player is
     * aware what leaving through the service door does"). Every time the floodlight drops out it stands one step nearer the
     * glass; on the third it is against it, and a stillness held there for GLASS_HOLD ticks lets it in. Light, from the wind or
     * the chain, holds it where it is: a reader who keeps the pendulum swinging keeps it off the glass.
     */
    public static final int GLASS_STILL=3,GLASS_HOLD=200,HAND_WARNING=100;
    private FoyerEncounter(){}
    public static boolean held(CompoundTag own){return own.getBoolean("FoyerAdmitted136")&&!own.getBoolean("FoyerFailureReturned136");}
    public static void tick(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getBoolean("Completed"))return;
        if(own.getBoolean("FoyerFailureReturned136")){
            // A new physical visit can retry; the original books, visitor and previous failure remain.
            own.putBoolean("UncannyFailed",false);own.putBoolean("FoyerFailureReturned136",false);own.putBoolean("FoyerAdmitted136",false);own.remove("FoyerEnding136");own.remove("FoyerThreat136");own.remove("FoyerWindSeen136");own.remove("FoyerDarkAge136");own.remove("FoyerDarkLast136");own.putInt("FoyerClock136",0);own.putInt("FoyerStill131",0);own.remove("FoyerHand201");
            var a=UncannyStories.actor(p,own,"CastVisitor");if(a!=null){a.appearance(LiteraryActor.UNCANNY_VISITOR,0);a.moveTo(b.getX()+.5,b.getY(),b.getZ()-18.5);a.hasImpulse=true;p.serverLevel().getChunkSource().broadcast(a,new net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket(a));}
        }
        // Old unresolved readiness was based on standing beside a door. Preserve completed accounts,
        // but require the actual service return for every unfinished visit.
        own.putBoolean("Ready",false);boolean dark=FoyerMotion.dark(p.serverLevel(),b);int clock=own.getInt("FoyerClock136");if(p.containerMenu==p.inventoryMenu||held(own)){clock+=5;own.putInt("FoyerClock136",clock);}else return;
        own.putInt("UncannyAge",Math.min(72000,own.getInt("UncannyAge")+5));
        if(held(own)){intrusion(p,b,own,clock);return;}
        if(FoyerMotion.wind(p.serverLevel(),b)>.2)own.putBoolean("FoyerWindSeen136",true);
        if(clock>=20&&!own.getBoolean("FoyerStepsHeard136")){own.putBoolean("FoyerStepsHeard136",true);cue(p,UncannyRegistry.FOYER_STEPS.get(),b.offset(1,0,-20),.32F,.9F);}
        if(clock>=65&&!own.getBoolean("FoyerBabbleHeard136")){own.putBoolean("FoyerBabbleHeard136",true);cue(p,UncannyRegistry.FOYER_BABBLE.get(),b.offset(UncannyRooms.INTERCOM),.24F,1F);}
        if(!dark&&own.getBoolean("FoyerDarkLast136")&&own.getBoolean("FoyerThreat136"))say(p,"The light swings back on. The yard is empty again.");
        if(dark&&!own.getBoolean("FoyerDarkLast136")){own.putInt("FoyerDarkAge136",clock);own.putInt("DarkSeen",own.getInt("DarkSeen")+1);own.putInt("FoyerStill131",own.getInt("FoyerStill131")+1);own.putBoolean("FoyerHand201",false);
            if(own.getBoolean("FoyerThreat136"))say(p,own.getInt("FoyerStill131")>=GLASS_STILL?"The light is out. It is right up against the glass now.":"The light is out again. It is standing closer than before.");cue(p,SoundEvents.IRON_TRAPDOOR_CLOSE,b.offset(UncannyRooms.FLOOD),.22F,.75F);cue(p,UncannyRegistry.FOYER_STEPS.get(),b.offset(-1,0,-17),.24F,.72F);}
        own.putBoolean("FoyerDarkLast136",dark);own.putBoolean("LastDark",dark);
        boolean reveal=dark&&clock>=105&&clock-own.getInt("FoyerDarkAge136")>=40;
        FoyerPresence.tick(p,b,own,reveal);
        // Keep the original hidden during the sounds that precede the first actual appearance.
        var visitor=UncannyStories.actor(p,own,"CastVisitor");if(dark&&!reveal&&visitor!=null)visitor.appearance(LiteraryActor.UNCANNY_VISITOR,0);
        if(reveal){
            own.putBoolean("FoyerThreat136",true);
            if(clock>=own.getInt("FoyerNextBuzz136")){own.putInt("FoyerNextBuzz136",clock+145+(int)Math.floorMod(p.getUUID().getLeastSignificantBits(),55));own.putInt("FoyerBuzzUntil136",clock+40);cue(p,UncannyRegistry.FOYER_BUZZER.get(),b.offset(UncannyRooms.INTERCOM),.5F,.92F);}
            if(visitor!=null)visitor.gesture(clock<own.getInt("FoyerBuzzUntil136")?1:0);
            // Against the glass, a stillness held long enough is all it needs.
            int held=clock-own.getInt("FoyerDarkAge136")-40;
            if(own.getInt("FoyerStill131")>=GLASS_STILL){
                if(held>=HAND_WARNING&&!own.getBoolean("FoyerHand201")){own.putBoolean("FoyerHand201",true);say(p,"Its hand is flat on the glass.");cue(p,SoundEvents.GLASS_HIT,b.offset(GLASS_DOOR),.6F,.7F);}
                if(held>=GLASS_HOLD){admit(p,b,own,"left_it_against_the_glass_in_the_dark");return;}
            }
        }else if(visitor!=null)visitor.gesture(0);
        exitHint(p,b,own);
        falseFrames(p,b,own,dark);
        double previousX=own.contains("FoyerLastX136")?own.getDouble("FoyerLastX136"):p.getX(),previousZ=own.contains("FoyerLastZ136")?own.getDouble("FoyerLastZ136"):p.getZ();
        double glass=b.getZ()-13.85;
        if(previousZ>=glass&&p.getZ()<glass){double x=previousX+(p.getX()-previousX)*(glass-previousZ)/(p.getZ()-previousZ);if(Math.abs(x-b.getX()-.5)<.65)admit(p,b,own,"crossed_the_open_courtyard_door");}
        if(own.getBoolean("FoyerThreat136"))for(var rel:UncannyRooms.FALSE_DOORS){
            var at=b.offset(rel);var state=p.serverLevel().getBlockState(at);
            double plane=at.getX()+.5+(rel.getX()<0?-.35:.35);boolean crossed=rel.getX()<0?previousX>=plane&&p.getX()<plane:previousX<=plane&&p.getX()>plane;
            double dz=crossed?previousZ+(p.getZ()-previousZ)*(plane-previousX)/(p.getX()-previousX):p.getZ();
            if(crossed&&Math.abs(dz-at.getZ()-.5)<.65&&Math.abs(p.getY()-at.getY())<1&&state.is(UncannyRegistry.PROP.get())&&state.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.FALSE_DOOR){admit(p,b,own,"entered_the_unhinged_hall");break;}
        }
        own.putDouble("FoyerLastX136",p.getX());own.putDouble("FoyerLastZ136",p.getZ());
    }
    private static void falseFrames(ServerPlayer p,BlockPos b,CompoundTag own,boolean dark){
        int auto=own.getInt("FalseAuto");
        for(int i=0;i<UncannyRooms.FALSE_DOORS.size();i++){
            var rel=UncannyRooms.FALSE_DOORS.get(i);var at=b.offset(rel);var s=p.serverLevel().getBlockState(at);
            if(dark&&(s.isAir()||s.is(Blocks.SMOOTH_SANDSTONE)||s.is(UncannyRegistry.material("lobby_wall")))&&!LiteraryVignettes.watched(p.serverLevel(),at.getCenter())){
                for(int y=0;y<2;y++)p.serverLevel().setBlock(at.above(y),UncannyPropBlock.of(UncannyPropBlock.Kind.FALSE_DOOR,Direction.EAST).setValue(UncannyPropBlock.STAGE,y),UncannyRooms.FLAGS);s=p.serverLevel().getBlockState(at);
            }
            if(!s.is(UncannyRegistry.PROP.get())||s.getValue(UncannyPropBlock.KIND)!=UncannyPropBlock.Kind.FALSE_DOOR)continue;
            int bit=1<<i;boolean behind=UncannySenses.angle(p,at.getCenter())>UncannySenses.halfFov(p),open=dark&&(behind||(auto&bit)!=0);
            if(open!=(s.getValue(UncannyPropBlock.STAGE)>=2)){
                for(int y=0;y<2;y++){var old=p.serverLevel().getBlockState(at.above(y));if(old.is(UncannyRegistry.PROP.get())&&old.getValue(UncannyPropBlock.KIND)==UncannyPropBlock.Kind.FALSE_DOOR)p.serverLevel().setBlock(at.above(y),old.setValue(UncannyPropBlock.STAGE,(open?2:0)+y),UncannyRooms.FLAGS);}
                if(open)cue(p,SoundEvents.WOODEN_TRAPDOOR_OPEN,at,.25F,.6F);
            }
            if(open)auto|=bit;else auto&=~bit;
        }own.putInt("FalseAuto",auto);
    }
    public static void admit(ServerPlayer p,BlockPos b,CompoundTag own,String reason){
        if(!LiteraryVignettes.participant(p)||own.getBoolean("Completed")||held(own))return;
        own.putBoolean("FoyerAdmitted136",true);own.putBoolean("FoyerFailureReturned136",false);own.putInt("FoyerAdmittedAt136",own.getInt("FoyerClock136"));own.putString("FoyerLastFailure136",reason);own.putBoolean("Ready",false);own.putBoolean("UncannyFailed",true);own.putString("Failure",reason);p.closeContainer();
        var at=b.offset(GLASS_DOOR);var s=p.serverLevel().getBlockState(at);if(s.getBlock() instanceof DoorBlock door)door.setOpen(p,p.serverLevel(),s,at,true);
        cue(p,UncannyRegistry.FOYER_BUZZER.get(),b.offset(UncannyRooms.INTERCOM),.7F,.8F);cue(p,UncannyRegistry.FOYER_INSIDE.get(),p.blockPosition(),.65F,.85F);
        var visitor=UncannyStories.actor(p,own,"CastVisitor");if(visitor!=null)visitor.appearance(LiteraryActor.UNCANNY_VISITOR,4);
        if(reason.equals("left_it_against_the_glass_in_the_dark"))say(p,"The glass door swings open on its own. It is inside.");
    }
    /**
     * Whether the warning beside the button has been read (0.4.116 canon: "before that has been read the button admits
     * nothing"): the wall's text itself, or any passage of it.
     */
    public static boolean warned(CompoundTag own){return own.getBoolean("Read_Source")||UncannyReading.readPassage(own);}
    /** Near the service door, once before the visitor has come and once after: what it is and what going through it does. */
    static void exitHint(ServerPlayer p,BlockPos b,CompoundTag own){
        if(p.distanceToSqr(Vec3.atBottomCenterOf(b.offset(EXIT)))>6.25)return;
        int want=own.getBoolean("FoyerThreat136")?2:1;if(own.getInt("FoyerExitHint201")>=want)return;own.putInt("FoyerExitHint201",want);
        say(p,want==1?"The service door has real hinges. It lets out the back, but nothing has come to the glass yet.":"The service door has real hinges. Go out the back this way and whatever is at the glass stays outside.");
    }
    private static void say(ServerPlayer p,String line){p.displayClientMessage(net.minecraft.network.chat.Component.literal(line).withStyle(net.minecraft.ChatFormatting.GRAY),true);}
    private static void intrusion(ServerPlayer p,BlockPos b,CompoundTag own,int clock){
        var visitor=UncannyStories.actor(p,own,"CastVisitor");int age=clock-own.getInt("FoyerAdmittedAt136");
        if(visitor!=null){visitor.appearance(LiteraryActor.UNCANNY_VISITOR,4);SceneFootpaths.walk(visitor,p.position(),.28);}
        if(age<60||age<200&&(visitor==null||visitor.distanceToSqr(p)>2.4))return;
        if(own.getBoolean("FoyerEnding136")){if(!LabyrinthDoors.isBusy(p))own.putBoolean("FoyerEnding136",false);else return;}
        own.putBoolean("FoyerEnding136",true);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);
        if(!LabyrinthDoors.sendBack(p,10,20,30,arrived->{var d=LabyrinthData.get(arrived.server);var t=LiteraryVignettes.personal(d,arrived.getUUID(),PLACE);t.putBoolean("FoyerFailureReturned136",true);t.putBoolean("FoyerEnding136",false);t.putInt("FoyerFailures136",t.getInt("FoyerFailures136")+1);t.putBoolean("Ready",false);LiteraryVignettes.save(d,arrived.getUUID(),PLACE,t);stop(arrived);})){own.putBoolean("FoyerEnding136",false);}
    }
    public static void returned(ServerPlayer p,LabyrinthData.Door door){
        if(!door.id.equals(PLACE.id()+"/service_exit")||!LiteraryVignettes.participant(p))return;
        var d=LabyrinthData.get(p.server);var t=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
        if(!t.getBoolean("Completed")&&!t.getBoolean("FoyerThreat136")&&!t.getBoolean("FoyerAdmitted136")){
            // Leaving early is allowed; it is said why it did not count.
            t.putBoolean("FoyerEarlyService201",true);LiteraryVignettes.save(d,p.getUUID(),PLACE,t);
            say(p,"You left by the service door before anything came to the glass. There was nothing yet to leave outside.");return;
        }
        if(t.getBoolean("Completed")||!t.getBoolean("FoyerThreat136")||t.getBoolean("FoyerAdmitted136")||UncannyStories.failed(t))return;
        t.putBoolean("Ready",true);t.putBoolean("Completed",true);t.putBoolean("FoyerServiceReturned136",true);t.putString("Outcome","left_the_visitor_outside_and_used_the_hinged_exit");
        WitnessAccount.resolve(p,WitnessAccount.Story.MOTION_LIGHT,t.getString("Outcome"));d.setCompleted(PLACE.id(),true);LiteraryVignettes.save(d,p.getUUID(),PLACE,t);stop(p);
        say(p,"You shut the service door behind you. Whatever stood at the glass is still out there, waiting to be let in.");
    }
    public static void stop(ServerPlayer p){for(var cue:List.of(UncannyRegistry.FOYER_BUZZER,UncannyRegistry.FOYER_BABBLE,UncannyRegistry.FOYER_STEPS,UncannyRegistry.FOYER_INSIDE))p.connection.send(new ClientboundStopSoundPacket(cue.getId(),SoundSource.AMBIENT));}
    private static void cue(ServerPlayer p,SoundEvent sound,BlockPos at,float volume,float pitch){UncannyStories.heard(p,sound,at.getCenter(),volume,pitch);}
}
