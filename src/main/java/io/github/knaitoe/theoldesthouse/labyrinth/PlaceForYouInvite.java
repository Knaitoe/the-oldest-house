package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.688, A Place for You, slice 4 (docs/A_PLACE_FOR_YOU_0_4_688.md): invite the past (the proposal's E3). While somebody is still at
 * the door, with the reader's picture down off the fridge and what letting him in would cost said aloud, the reader can take Theo's
 * photograph off the chest of drawers and put it up where their own picture was, then tell him to come in. He comes along the road, in
 * at the gate and straight in without knocking, an ordinary tired man in his own face. Mara knows him. Alex and Ren, who were never in
 * the old evening, are not in the house after that; Kit, already gone, stays gone. Mara no longer knows why the reader is in the hall,
 * and the things she laid out for the reader in the bathroom are his now. One small thing about him is wrong, and Mara notices it, and
 * lets it go. Nothing here decides whether he is who he was.
 */
final class PlaceForYouInvite {
    private PlaceForYouInvite(){}
    /** Ticks between his lines and hers; how long before Mara turns to the reader; how long after the cupboard before the end. */
    static final int BEAT=50,TURN=120,AFTER=100;
    /** The two ends of the road, where it goes on into the trees; he comes from whichever the reader is not looking at. */
    static final Vec3[] ENDS={new Vec3(-15.5,0,-2.5),new Vec3(16.5,0,-2.5)};
    /** His way in: along the road to the gate, up the path, over the step, into the hall; then to the counter by the cups, and back. */
    static final Vec3 GATE_ROAD=new Vec3(.5,0,-2.5),PATH=new Vec3(.5,0,-6.5),STEP=new Vec3(.5,0,-10.5),HALL=new Vec3(.5,0,-14.5),COUNTER=new Vec3(8.5,0,-20.5);

    static void cue(ServerPlayer p,String line){PlaceForYou.cue(p,line);}

    // ------------------------------------------------------------------------------------------------ the photograph
    /** Theo's photograph on the chest of drawers, once somebody has knocked and the voice has asked. */
    static void photo(ServerPlayer p,PlaceForYou.House h){
        var own=PlaceForYou.own(p);
        Talk.begin(p,"photo","Theo's photograph");Talk.line(p,"","A man half turned from the camera, in a work shirt.");
        if(!own.getBoolean("PictureDown")){Talk.line(p,"","Your picture is still up on the fridge.");Talk.ask(p,Talk.act("Leave it.",Talk::end));return;}
        Talk.ask(p,Talk.act("Take it.",q->{var l=q.serverLevel();PlaceForYouSite.photo(l,h.base,false);PlaceForYou.take(q,PlaceForYouRegistry.PHOTO,"Theo's photograph");
                PlaceForYou.edit(q,o->o.putBoolean("PhotoTaken",true));Talk.end(q);cue(q,"You take Theo's photograph off the chest of drawers.");})
            ,Talk.act("Leave it.",Talk::end));
    }
    /** A click while carrying the photograph: up on the fridge where the reader's picture was, or back on the chest of drawers. */
    static boolean carried(ServerPlayer p,PlaceForYou.House h,BlockPos rel){
        var l=p.serverLevel();var b=h.base;
        if(rel.equals(PlaceForYouSite.FRIDGE)||rel.equals(PlaceForYouSite.FRIDGE.above())){
            PlaceForYou.release(p,null);PlaceForYouSite.theoPhoto(l,b);PlaceForYou.edit(p,o->{o.putBoolean("TheoUp",true);o.putBoolean("TheoWasUp",true);});
            cue(p,"You put Theo's photograph up on the fridge, under the magnet, where your picture was.");return true;}
        if(rel.equals(PlaceForYouSite.PHOTO)||rel.equals(PlaceForYouSite.PHOTO.below())){
            PlaceForYou.release(p,null);PlaceForYouSite.photo(l,b,true);cue(p,"You stand it back on the chest of drawers.");return true;}
        cue(p,"Theo's photograph.");return true;
    }
    /** The fridge with Theo's photograph on it, before he is let in. */
    static void fridge(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"fridge_theo","The fridge");Talk.line(p,"","Theo's photograph, under the magnet, where your picture was.");
        Talk.ask(p,Talk.act("Take it down.",q->{var l=q.serverLevel();PlaceForYouSite.picture(l,h.base,new CompoundTag(),false);PlaceForYou.take(q,PlaceForYouRegistry.PHOTO,"Theo's photograph");
                PlaceForYou.edit(q,o->o.putBoolean("TheoUp",false));Talk.end(q);cue(q,"You take it down. The fridge door is bare.");})
            ,Talk.act("Leave it.",Talk::end));
    }
    /** Whether the reader can tell him to come in: his photograph up where theirs was, and the cost said aloud. */
    static boolean canInvite(CompoundTag own){return own.getBoolean("TheoUp")&&own.getBoolean("CostHeard");}

    // ------------------------------------------------------------------------------------------------ the invitation
    static void invite(ServerPlayer p,PlaceForYou.House h){
        Talk.end(p);var l=p.serverLevel();var b=h.base;
        if(!PlaceForYouSite.doorOpen(l,b)){PlaceForYouSite.frontDoor(l,b,true);l.playSound(null,b.offset(PlaceForYouSite.FRONT_DOOR),SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.8F,1F);}
        PlaceForYouSite.gate(l,b,true);
        PlaceForYou.edit(p,o->{o.putInt("Chapter",PlaceForYou.INVITE);o.putInt("InviteStage",0);o.putBoolean("Invited",true);o.putBoolean("Knocked",true);});
        h.theoLeg=0;h.inviteAt=(int)h.tick;h.theo=null;
        cue(p,"Nobody answers. Then, a long way down the road, footsteps.");
    }

    // ------------------------------------------------------------------------------------------------ the old evening
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;long now=h.tick;int stage=own.getInt("InviteStage");
        var theo=theo(l,h);
        // Made on the road while he is still to come; once he is in (the house emptied and filled again), in his chair.
        if(theo==null){if(!(stage==0?spawn(l,h,p,own):spawnSeated(l,h,p)))return;theo=theo(l,h);if(theo==null)return;}
        oldEvening(l,h,p,own,r);
        var kit=PlaceForYou.actor(l,h,"Kit");
        if(kit!=null&&!own.getBoolean("KitNight")&&theo.distanceTo(kit)<3.5){PlaceForYou.above(h,kit,"Night.");h.kitNightAt=(int)now;PlaceForYou.edit(p,o->o.putBoolean("KitNight",true));}
        if(h.kitNightAt>=0&&now-h.kitNightAt==30){h.kitNightAt=-1;PlaceForYou.above(h,theo,"Night.");}
        switch(stage){
            case 0->{
                // Along the road and up the path; straight over the step and into the hall.
                Vec3[] legs={GATE_ROAD,PATH,STEP,HALL};
                if(h.theoLeg<legs.length&&walk(l,h,p,theo,legs[h.theoLeg],180))h.theoLeg++;
                if(h.theoLeg>=legs.length){
                    l.playSound(null,b.offset(PlaceForYouSite.GLASS),SoundEvents.CHAIN_PLACE,SoundSource.NEUTRAL,.4F,1.6F);
                    cue(p,"He comes straight in without knocking, and drops his keys under the hall glass.");
                    PlaceForYou.above(h,PlaceForYou.actor(l,h,"Mara"),"There you are.");
                    PlaceForYou.edit(p,o->{o.putInt("InviteStage",1);o.putBoolean("Rewritten",true);});h.inviteAt=(int)now;h.theoLeg=0;
                }
            }
            case 1->{
                if(now-h.inviteAt==BEAT)PlaceForYou.above(h,theo,"Here I am. I lost track of the time.");
                seat(l,h,p,theo);
                // Mara turns to the reader, once they are near enough and she can see them.
                var mara=PlaceForYou.actor(l,h,"Mara");
                if(now-h.inviteAt>=TURN&&mara!=null&&p.distanceTo(mara)<9&&mara.hasLineOfSight(p)){
                    PlaceForYou.above(h,mara,"Sorry. Can I help you with something?");PlaceForYou.edit(p,o->{o.putInt("InviteStage",2);o.putBoolean("MaraStranger",true);});h.inviteAt=(int)now;}
            }
            case 2->{
                seat(l,h,p,theo);
                // The things laid out for the reader are his now; said where the reader can hear it.
                if(now-h.inviteAt>=TURN&&PlaceForYouSite.inHouse(r)&&!Talk.waiting(p)){
                    var mara=PlaceForYou.actor(l,h,"Mara");
                    if(mara!=null&&p.distanceTo(mara)<12){
                        PlaceForYou.above(h,mara,"Your things are out in the bathroom. I put them out.");h.careAt=(int)now;
                        PlaceForYou.edit(p,o->{o.putInt("InviteStage",3);o.putBoolean("CareHeard",true);});h.inviteAt=(int)now;}
                }
            }
            case 3->{
                seat(l,h,p,theo);
                if(h.careAt>=0&&now-h.careAt==BEAT){h.careAt=-1;PlaceForYou.above(h,theo,"You didn't have to.");}
                // The one wrong thing, where the reader can see it: in the kitchen.
                if(now-h.inviteAt>=TURN&&PlaceForYouSite.inKitchen(r)){PlaceForYou.edit(p,o->o.putInt("InviteStage",4));h.inviteAt=(int)now;}
            }
            case 4->{
                long since=now-h.inviteAt;
                if(!own.getBoolean("CupAway")){
                    if(walk(l,h,p,theo,COUNTER,180)){
                        theo.setYRot(180);theo.setYHeadRot(180);theo.yBodyRot=180;
                        l.playSound(null,b.offset(PlaceForYouSite.CUPS),SoundEvents.DECORATED_POT_INSERT,SoundSource.NEUTRAL,.4F,1.5F);
                        if(PlaceForYouSite.inKitchen(r))cue(p,"Theo dries a cup and puts it away in the cupboard over the counter.");
                        PlaceForYou.edit(p,o->{o.putBoolean("CupAway",true);o.putBoolean("WrongSeen",PlaceForYouSite.inKitchen(PlaceForYou.rel(p.position(),b)));});h.inviteAt=(int)now;}
                    return;
                }
                var mara=PlaceForYou.actor(l,h,"Mara");
                if(since==BEAT)PlaceForYou.above(h,mara,"That's the right cupboard.");
                if(since==BEAT*2)PlaceForYou.above(h,theo,"Is it?");
                if(since==BEAT*3)PlaceForYou.above(h,mara,"You always put them in the wrong one.");
                if(since==BEAT*4)PlaceForYou.above(h,theo,"Did I?");
                if(since==BEAT*5)PlaceForYou.above(h,mara,"Never mind. Sit down, it's getting cold.");
                if(since>BEAT*5)seat(l,h,p,theo);
                if(since>=BEAT*5+AFTER){PlaceForYou.edit(p,o->o.putInt("InviteStage",5));PlaceForYou.finish(p,"invited_the_past");}
            }
            default->{}
        }
        // Out of the house before the end: the evening goes on without the reader, and is over for them.
        own=PlaceForYou.own(p);
        if(own.getInt("InviteStage")>=1&&own.getInt("InviteStage")<5&&!PlaceForYouSite.inHouse(r)&&r.z>-9){PlaceForYou.edit(p,o->o.putBoolean("LeftEarly",true));PlaceForYou.finish(p,"invited_the_past");}
    }
    /** After it is over: Theo in his place, the old evening as it was. */
    static void after(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var theo=theo(l,h);
        if(theo==null){if(!spawnSeated(l,h,p))return;theo=theo(l,h);if(theo==null)return;}
        seat(l,h,p,theo);oldEvening(l,h,p,own,r);
    }
    /** The old evening, put back out of sight: no place for the reader at the table, their things laid out for him in the bathroom. */
    static void oldEvening(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;if(!own.getBoolean("Rewritten"))return;
        if(PlaceForYouSite.readerChair(l,b)&&!PlaceForYou.looking(p,b,PlaceForYouSite.READER_CHAIR,14))PlaceForYouSite.readerChair(l,b,false);
        if(PlaceForYouSite.cardThere(l,b)&&!PlaceForYou.looking(p,b,PlaceForYouSite.CARD,14))PlaceForYouSite.card(l,b,false);
        if(!PlaceForYouSite.picturesFilled(l,b)&&!PlaceForYou.looking(p,b,PlaceForYouSite.PICTURES,16))PlaceForYouSite.pictures(l,b,true);
        if(!PlaceForYouSite.routineOut(l,b)&&!PlaceForYouSite.inBathroom(r)){int item=own.getCompound("Snap").getInt("Routine");PlaceForYouSite.routine(l,b,item<0?11:item);}
        if(own.getBoolean("CareHeard")&&!own.getBoolean("CareSeen")&&PlaceForYouSite.routineOut(l,b)&&PlaceForYou.looking(p,b,PlaceForYouSite.ROUTINE,8)){
            PlaceForYou.edit(p,o->o.putBoolean("CareSeen",true));cue(p,"Laid out as it was for you. She put it out for him.");}
        // Alex and Ren were never in the old evening: each is gone the moment the reader isn't looking at them.
        for(String who:new String[]{"Alex","Ren"}){var a=PlaceForYou.actor(l,h,who);if(a==null||own.getBoolean(who+"Out"))continue;
            if(!HouseWatchers.sees(p,a.getEyePosition())&&!HouseWatchers.sees(p,a.position().add(0,.3,0))){a.discard();PlaceForYou.edit(p,o->o.putBoolean(who+"Out",true));}}
    }

    // ------------------------------------------------------------------------------------------------ Theo himself
    @Nullable static LiteraryActor theo(ServerLevel l,PlaceForYou.House h){return PlaceForYou.actor(l,h.theo);}
    static boolean watched(ServerPlayer p,Vec3 at){return HouseWatchers.sees(p,at.add(0,1.6,0))||HouseWatchers.sees(p,at.add(0,.3,0));}
    /** Made at whichever end of the road the reader can't see, out among the trees; never in sight. */
    static boolean spawn(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own){
        var b=h.base;
        for(var end:ENDS){var at=new Vec3(b.getX()+end.x,b.getY(),b.getZ()+end.z);
            if(watched(p,at))continue;
            var a=PlaceForYou.spawn(l,b,p.getUUID(),PlaceForYou.THEO,end,end.x<0?-90:90);if(a==null)return false;h.theo=a.getUUID();return true;}
        return false;
    }
    /** After the evening, if he has to be made again (the house emptied and refilled): in his chair, while nobody sees it. */
    static boolean spawnSeated(ServerLevel l,PlaceForYou.House h,ServerPlayer p){
        var b=h.base;var chair=Vec3.atBottomCenterOf(PlaceForYouSite.THEO_CHAIR);
        if(watched(p,new Vec3(b.getX()+chair.x,b.getY()+chair.y,b.getZ()+chair.z)))return false;
        var a=PlaceForYou.spawn(l,b,p.getUUID(),PlaceForYou.THEO,chair,-90);if(a==null)return false;h.theo=a.getUUID();return true;
    }
    /** On foot, a step every other tick, at an ordinary pace. */
    static boolean walk(ServerLevel l,PlaceForYou.House h,ServerPlayer p,LiteraryActor a,Vec3 rel,float yaw){
        var b=h.base;var to=new Vec3(b.getX()+rel.x,b.getY()+rel.y,b.getZ()+rel.z);
        if(a.position().subtract(to).horizontalDistanceSqr()<.05)return true;
        if(h.tick%2!=0)return false;
        return SceneFootpaths.walk(a,to,.16);
    }
    /** Into his own chair at the table, as the others move into theirs. */
    static void seat(ServerLevel l,PlaceForYou.House h,ServerPlayer p,LiteraryActor theo){
        if(h.tick%2!=0)return;PlaceForYou.place(l,h,p,theo,PlaceForYou.seat(PlaceForYouSite.THEO_CHAIR));
    }

    // ------------------------------------------------------------------------------------------------ talking
    static void talk(ServerPlayer p,PlaceForYou.House h,String who,LiteraryActor a){
        var own=PlaceForYou.own(p);
        if(who.equals("Theo")){PlaceForYou.above(h,a,own.getInt("InviteStage")>=1?"Evening.":"...");return;}
        if(who.equals("Mara")&&own.getBoolean("MaraStranger")&&!own.getBoolean("InviteAsked")){mara(p,h);return;}
        if(who.equals("Mara")){PlaceForYou.above(h,a,own.getBoolean("MaraStranger")?"Mind the gate on your way out. It sticks.":"There you are.");return;}
        PlaceForYou.above(h,a,"Night.");
    }
    /** Mara, who doesn't know the reader now. */
    static void mara(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"invite_mara","Mara");PlaceForYou.line(p,h,"Mara","Sorry. Can I help you with something?");
        var replies=new ArrayList<Talk.Reply>();
        replies.add(Talk.reply("It's me.",q->{PlaceForYou.line(q,h,"Mara","I'm sorry. I don't think we've met.");PlaceForYou.edit(q,o->{o.putBoolean("InviteAsked",true);o.putString("InviteSaid","me");});Talk.end(q);}));
        replies.add(Talk.reply("Who's he?",q->{PlaceForYou.line(q,h,"Mara","That's Theo. My brother. He's late. He's always late.");PlaceForYou.edit(q,o->{o.putBoolean("InviteAsked",true);o.putString("InviteSaid","who");});Talk.end(q);}));
        replies.add(Talk.reply("I'll go.",q->{PlaceForYou.line(q,h,"Mara","Mind the gate. It sticks.");PlaceForYou.edit(q,o->{o.putBoolean("InviteAsked",true);o.putString("InviteSaid","go");});Talk.end(q);}));
        Talk.ask(p,replies);
    }
}
