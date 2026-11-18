package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.690, A Place for You, slice 6 (docs/A_PLACE_FOR_YOU_0_4_690.md): the back passage (the proposal's N6B, somebody's actual way
 * out). While Mara is at the sink, Kit waits by the back door and shows it to the reader: the catch is stiff, and someone has stood the
 * cupboard in front of it. Kit asks the reader to keep it quiet until the bag is outside; the reader can agree, refuse, or say they
 * would rather tell Mara with Kit there. Then three ways out, any one enough:
 * <ul>
 * <li>the cupboard shoved along the wall a third at a time (a wedge was under it, keeping it level), the catch drawn, the door, which
 *     swings back shut and lets the catch drop if nothing holds it, held with the wedge or the step stool, and the back gate tried;</li>
 * <li>Mara's key, in the drawer with the tea towels, with Theo's, which turns the lock so the catch never drops, and the gate tried;</li>
 * <li>Alex's help with the sitting room window, lifted and held.</li>
 * </ul>
 * Once a way is open Kit carries the bag out through it and comes back in for supper. A reader who refused finds Kit has done it
 * alone. What the reader said and did about the quiet is kept for the account and for later slices; Kit answers what was done.
 */
public final class PlaceForYouBack {
    private PlaceForYouBack(){}
    /** Ticks a drawn catch holds the door before it swings back and the catch drops in again, with nothing holding it. */
    public static final int SWING=40;
    /** Unwatched ticks before a refused Kit has done it alone. */
    public static final int ALONE=400;
    /** Where Kit waits, in the hall's back corner, turned to the back door. */
    static final PlaceForYou.Mark WAITS=PlaceForYou.stand(-1.5,-25.5,-150);
    /** Where Kit stands beside Mara at the sink, for a reader who said they would rather tell her with Kit there. */
    static final PlaceForYou.Mark BESIDE=PlaceForYou.stand(7.3,-20.5,180);
    /** Alex at the sitting room window, holding it up. */
    static final PlaceForYou.Mark HOLDS=PlaceForYou.stand(-7.7,-14.5,90);
    /** Kit's way out and back with the bag: through the back door to the lane beyond the gate, or out of the window to the side yard. */
    static final Vec3[] BACK_LEGS={new Vec3(.5,0,-26.5),new Vec3(.5,0,-30.5),new Vec3(.5,0,-26.5),new Vec3(-1.5,0,-25.5)};
    static final Vec3[] SIDE_LEGS={new Vec3(-1,0,-15.5),new Vec3(-4.5,0,-14.5),new Vec3(-6.5,0,-14.5),new Vec3(-4.5,0,-14.5),new Vec3(-1,0,-15.5),new Vec3(-1.5,0,-25.5)};
    /** The leg at which the bag is set down: just through the back gate, or handed to Alex at the window. */
    static final int BACK_DROP=1,SIDE_DROP=2;

    static void cue(ServerPlayer p,String line){PlaceForYou.cue(p,line);}
    static CompoundTag own(ServerPlayer p){return PlaceForYou.own(p);}
    static boolean open(CompoundTag own){return !own.getString("BackWay").isEmpty();}
    static boolean out(CompoundTag own){return own.getInt("BackStage")>=3;}
    /** Whether the passage is live for this reader: shown or waiting, in the sink chapter or after, before the house is opened. */
    static boolean live(CompoundTag own){int ch=own.getInt("Chapter");return (ch==PlaceForYou.SINK||ch==PlaceForYou.DINNER)&&!own.getBoolean("KitGoing");}

    /** Whether Kit's way out is the back: the door and gate opened by the reader, with the key, or by Kit alone. */
    public static boolean backWay(CompoundTag own){var w=own.getString("BackWay");return w.equals("door")||w.equals("key")||w.equals("kit");}
    /** Where Kit goes when Kit goes: out of the back gate and along the path between the trees, or out of the front gate to the road. */
    static PlaceForYou.Mark going(CompoundTag own){return backWay(own)?PlaceForYou.stand(.5,-32.6,180):PlaceForYou.stand(.5,-2.5,90);}
    /** Whatever the reader carried off from the passage goes back where it was: the wedge to the floor, the stool to its corner, the key to the drawer. */
    static void released(ServerPlayer p,BlockPos b,int held){
        var l=p.server.getLevel(NovelRooms.dimension(PlaceForYouSite.PLACE));if(l==null||!l.isLoaded(b.offset(PlaceForYouSite.HOLD)))return;
        if(held==PlaceForYouRegistry.WEDGE)PlaceForYouSite.wedge(l,b,"floor");
        else if(held==PlaceForYouRegistry.STOOL)PlaceForYouSite.stool(l,b,"corner");
        else if(held==PlaceForYouRegistry.KEY)PlaceForYou.edit(p,o->o.putBoolean("KeyTaken",false));
    }

    // ------------------------------------------------------------------------------------------------ where people stand
    /** Kit by the back door, beside Mara, or on the way out with the bag; Alex at the window. */
    static void marks(CompoundTag own,Map<String,PlaceForYou.Mark> m){
        if(!live(own))return;int stage=own.getInt("BackStage");
        if(stage==0){if(own.getInt("Chapter")==PlaceForYou.SINK)m.put("Kit",WAITS);return;}
        if(own.getBoolean("AlexWindow")&&stage<3)m.put("Alex",HOLDS);
        if(stage>=3)return;
        if(own.contains("BagLeg")){boolean window=own.getString("BackWay").equals("window");var legs=window?SIDE_LEGS:BACK_LEGS;int i=Math.min(own.getInt("BagLeg"),legs.length-1);
            float yaw=i<=(window?SIDE_DROP:BACK_DROP)?(window?90:180):(window?-90:0);m.put("Kit",PlaceForYou.stand(legs[i].x,legs[i].z,yaw));return;}
        if(own.getString("Quiet").equals("open")&&!own.getBoolean("ToldOpen")){m.put("Kit",BESIDE);return;}
        m.put("Kit",WAITS);
    }

    // ------------------------------------------------------------------------------------------------ Kit shows it
    /** Kit, at the back door: what it is, and the quiet asked for. */
    static void show(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"back_kit","Kit");
        PlaceForYou.line(p,h,"Kit","This is the back door. Nobody uses it.");
        PlaceForYou.line(p,h,"Kit","The catch is stiff, and somebody stood the cupboard in front of it. I didn't. Mara says she didn't.");
        PlaceForYou.line(p,h,"Kit","There's a gate at the back. If this opens, I can go without walking past the kitchen.");
        PlaceForYou.line(p,h,"Kit","Will you keep it quiet? Only until the bag's outside.");
        Talk.ask(p,Talk.reply("I'll keep it quiet.",q->{PlaceForYou.line(q,h,"Kit","Thanks. Push from the end. It's lighter that side.");shown(q,"agreed");})
            ,Talk.reply("I'd rather tell Mara, with you there.",q->{PlaceForYou.line(q,h,"Kit","...All right. Then I'm standing next to you when you do.");shown(q,"open");})
            ,Talk.reply("I won't help with this.",q->{PlaceForYou.line(q,h,"Kit","That's fine. I'll do it myself.");shown(q,"refused");}));
    }
    static void shown(ServerPlayer p,String quiet){
        Talk.end(p);PlaceForYou.edit(p,o->{o.putInt("BackStage",1);o.putString("Quiet",quiet);});
        if(quiet.equals("open"))cue(p,"Kit goes into the kitchen ahead of you and stands by the sink.");
        else if(!quiet.equals("refused"))cue(p,"The cupboard stands in front of the catch.");
    }

    // ------------------------------------------------------------------------------------------------ the evening's clock
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        if(!live(own))return;var b=h.base;int stage=own.getInt("BackStage");
        // Kit asks the reader over, now and then, until they come.
        if(stage==0){if(own.getInt("Chapter")==PlaceForYou.SINK&&h.tick%300==150&&PlaceForYouSite.inHouse(r)&&!Talk.waiting(p))PlaceForYou.above(h,PlaceForYou.actor(l,h,"Kit"),"Got a minute? Back here.");return;}
        // A drawn catch with nothing holding the door: it swings back on its own, and the catch drops in again.
        if(own.getBoolean("CatchDrawn")&&own.getString("BackHeld").isEmpty()&&!own.getBoolean("KeyUsed")){
            if(h.backAt<0)h.backAt=(int)h.tick;
            if(h.tick-h.backAt>=SWING){PlaceForYouSite.backDoor(l,b,false);l.playSound(null,b.offset(PlaceForYouSite.BACK_DOOR),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.8F,.8F);
                PlaceForYou.edit(p,o->{o.putBoolean("CatchDrawn",false);o.putBoolean("Swung",true);});h.backAt=-1;cue(p,"It swings back shut on its own, and the catch drops in again.");}
            return;
        }
        // Alex, asked to hold the window: it goes up when Alex gets to it.
        if(own.getBoolean("AlexWindow")&&!open(own)){var alex=PlaceForYou.actor(l,h,"Alex");
            if(alex!=null&&alex.position().subtract(new Vec3(b.getX()+HOLDS.at().x,b.getY(),b.getZ()+HOLDS.at().z)).horizontalDistanceSqr()<.36){
                PlaceForYouSite.sideWindow(l,b,true);l.playSound(null,b.offset(PlaceForYouSite.SIDE_WINDOW),SoundEvents.WOODEN_TRAPDOOR_OPEN,SoundSource.BLOCKS,.6F,.8F);
                PlaceForYou.edit(p,o->{o.putString("BackWay","window");o.putInt("BackStage",2);});cue(p,"In the sitting room, Alex lifts the window and holds it.");}
            return;}
        // A reader who refused: Kit does it alone, while nobody is looking at the back of the hall.
        if(stage==1&&own.getString("Quiet").equals("refused")&&!open(own)){
            boolean watched=PlaceForYou.looking(p,b,PlaceForYouSite.BACK_DOOR,24)||PlaceForYouSite.inHouse(r)&&r.z<-24;
            if(watched){h.backSince=-1;return;}
            if(h.backSince<0)h.backSince=(int)h.tick;
            if(h.tick-h.backSince>=ALONE){PlaceForYouSite.press(l,b,3);PlaceForYouSite.wedge(l,b,"held");PlaceForYouSite.backDoor(l,b,true);PlaceForYouSite.backGate(l,b,true);
                PlaceForYouSite.bag(l,b,"back");PlaceForYou.edit(p,o->{o.putString("BackWay","kit");o.putInt("Shoves",3);o.putString("BackHeld","wedge");o.putBoolean("GateTested",true);o.putInt("BackStage",3);o.putString("BagWhere","back");});
                h.backSince=-1;PlaceForYou.above(h,PlaceForYou.actor(l,h,"Kit"),back(own(p)));}
            return;
        }
        // A way open: Kit fetches the bag where nobody sees, and carries it out.
        if(open(own)&&stage<3){
            var kit=PlaceForYou.actor(l,h,"Kit");if(kit==null)return;
            boolean window=own.getString("BackWay").equals("window");var legs=window?SIDE_LEGS:BACK_LEGS;int drop=window?SIDE_DROP:BACK_DROP;
            if(!own.contains("BagLeg")){
                if(Talk.waiting(p)||own.getString("Quiet").equals("open")&&!own.getBoolean("ToldOpen"))return;
                if(io.github.knaitoe.theoldesthouse.house.HouseWatchers.sees(p,kit.getEyePosition()))return;
                PlaceForYouSite.bag(l,b,"none");kit.setItemInHand(InteractionHand.MAIN_HAND,PlaceForYou.carried(p,PlaceForYouRegistry.BAG,"Kit's bag"));
                PlaceForYou.edit(p,o->o.putInt("BagLeg",0));return;
            }
            int leg=own.getInt("BagLeg");var at=legs[Math.min(leg,legs.length-1)];
            var to=new Vec3(b.getX()+at.x,b.getY()+at.y,b.getZ()+at.z);
            if(kit.position().subtract(to).horizontalDistanceSqr()>.36)return;
            if(leg==drop){kit.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);PlaceForYouSite.bag(l,b,window?"side":"back");
                l.playSound(null,kit.blockPosition(),SoundEvents.WOOL_PLACE,SoundSource.NEUTRAL,.6F,.9F);PlaceForYou.edit(p,o->o.putString("BagWhere",window?"side":"back"));}
            if(leg+1<legs.length){PlaceForYou.edit(p,o->o.putInt("BagLeg",leg+1));return;}
            PlaceForYou.edit(p,o->{o.remove("BagLeg");o.putInt("BackStage",3);if(o.getString("Quiet").equals("agreed")&&!o.getBoolean("ToldMara"))o.putBoolean("QuietKept",true);});
            if(window){PlaceForYouSite.sideWindow(l,b,false);PlaceForYou.above(h,PlaceForYou.actor(l,h,"Alex"),"There. I'll shut it before she feels the draft.");}
            PlaceForYou.above(h,kit,back(own(p)));
        }
    }
    /** What Kit says with the bag outside, after what the reader did about the quiet. */
    public static String back(CompoundTag own){
        if(own.getBoolean("BrokeQuiet"))return "It's out. She knew before it was. You said you'd wait.";
        if(own.getBoolean("ToldOpen"))return "It's out. She watched me carry it.";
        if(own.getString("BackWay").equals("kit"))return "It's out. I did it myself.";
        if(own.getBoolean("ToldMara"))return "It's out. You told her. That's fair. I asked a lot.";
        return "It's out. Thanks for keeping it.";
    }

    // ------------------------------------------------------------------------------------------------ hands on things
    /** The cupboard, shoved along the wall a third at a time; under where it stood, the wedge that kept it level. */
    static boolean press(ServerPlayer p,PlaceForYou.House h){
        var own=own(p);var l=p.serverLevel();var b=h.base;int n=own.getInt("Shoves");
        if(own.getInt("BackStage")<1||!live(own)){cue(p,"A cupboard, stood in front of the back door.");return true;}
        if(n>=3){cue(p,"The cupboard, shoved along the wall, clear of the catch.");return true;}
        int now=n+1;PlaceForYou.edit(p,o->o.putInt("Shoves",now));PlaceForYouSite.press(l,b,now);
        l.playSound(null,b.offset(PlaceForYouSite.BACK_PRESS),SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.9F,.6F);
        if(now<3){cue(p,now==1?"It scrapes a little way along the wall.":"Another hand's width. It's heavier at the near end.");return true;}
        PlaceForYouSite.wedge(l,b,"floor");
        cue(p,"It comes clear of the catch. On the floor where it stood, a wooden wedge. It was keeping the cupboard level.");
        PlaceForYou.above(h,PlaceForYou.actor(l,h,"Kit"),"There.");
        return true;
    }
    /** The back door: the catch drawn once the cupboard is clear, the key turned in it, or held open with what the reader carries. */
    static boolean door(ServerPlayer p,PlaceForYou.House h,int held){
        var own=own(p);var l=p.serverLevel();var b=h.base;
        if(!live(own)||own.getInt("BackStage")<1){cue(p,"The back door's catch is stiff, and the cupboard stands in front of it.");return true;}
        if(held==PlaceForYouRegistry.KEY){
            PlaceForYou.release(p,null);PlaceForYouSite.backDoor(l,b,true);l.playSound(null,b.offset(PlaceForYouSite.BACK_DOOR),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.BLOCKS,.6F,1.4F);
            PlaceForYou.edit(p,o->{o.putBoolean("KeyUsed",true);o.putBoolean("CatchDrawn",true);});
            cue(p,"The key turns. The lock gives, and the door stays open on its own. You leave the key in it.");return true;}
        boolean holder=held==PlaceForYouRegistry.WEDGE||held==PlaceForYouRegistry.STOOL;
        if(PlaceForYouSite.backDoorOpen(l,b)){
            boolean loose=own.getString("BackHeld").isEmpty()&&!own.getBoolean("KeyUsed");
            if(holder&&loose){
                PlaceForYou.release(p,null);boolean wedge=held==PlaceForYouRegistry.WEDGE;if(wedge)PlaceForYouSite.wedge(l,b,"held");else PlaceForYouSite.stool(l,b,"held");
                PlaceForYou.edit(p,o->o.putString("BackHeld",wedge?"wedge":"stool"));h.backAt=-1;
                l.playSound(null,b.offset(PlaceForYouSite.HOLD),SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.7F,1F);
                cue(p,wedge?"You push the wedge in under it with your foot. It holds.":"You stand the step stool against it. It holds.");return true;}
            cue(p,loose?"Open, for now. It's already starting to swing back.":"Open, and held.");return true;}
        if(own.getInt("Shoves")<3){cue(p,"The catch is behind the cupboard. You can't get your hand to it.");return true;}
        PlaceForYouSite.backDoor(l,b,true);l.playSound(null,b.offset(PlaceForYouSite.BACK_DOOR),SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.8F,.7F);
        PlaceForYou.edit(p,o->o.putBoolean("CatchDrawn",true));h.backAt=(int)h.tick;
        cue(p,holder?"You work the catch back with your free hand. The door comes open inward, onto the dark.":"You work the catch back. The door comes open inward, onto the dark.");
        return true;
    }
    /** The back gate: tried, once the door is open and stays open. */
    static boolean gate(ServerPlayer p,PlaceForYou.House h){
        var own=own(p);var l=p.serverLevel();var b=h.base;
        if(!live(own)||own.getBoolean("GateTested")){cue(p,"The back gate.");return true;}
        boolean held=!own.getString("BackHeld").isEmpty()||own.getBoolean("KeyUsed");
        if(!held){cue(p,"Behind you, the back door is already swinging shut.");return true;}
        PlaceForYouSite.backGate(l,b,true);l.playSound(null,b.offset(PlaceForYouSite.BACK_GATE),SoundEvents.FENCE_GATE_OPEN,SoundSource.BLOCKS,.8F,1F);
        PlaceForYou.edit(p,o->{o.putBoolean("GateTested",true);o.putString("BackWay",o.getBoolean("KeyUsed")?"key":"door");o.putInt("BackStage",2);});
        cue(p,"The gate opens. A path goes off between the trees.");
        PlaceForYou.above(h,PlaceForYou.actor(l,h,"Kit"),"That's it. That's all it ever was.");
        return true;
    }
    /** The wedge on the floor, or the step stool in the corner: picked up to hold the door with. */
    static boolean wedge(ServerPlayer p,PlaceForYou.House h){
        var own=own(p);var l=p.serverLevel();var b=h.base;
        if(!live(own)||!own.getString("BackHeld").isEmpty()){cue(p,own.getString("BackHeld").equals("wedge")?"A wooden wedge, under the door.":"A wooden wedge.");return true;}
        PlaceForYouSite.wedge(l,b,"none");PlaceForYou.take(p,PlaceForYouRegistry.WEDGE,"A wooden wedge");cue(p,"A wooden wedge, worn smooth along one edge.");return true;
    }
    static boolean stool(ServerPlayer p,PlaceForYou.House h){
        var own=own(p);var l=p.serverLevel();var b=h.base;
        if(!live(own)||own.getInt("BackStage")<1||!own.getString("BackHeld").isEmpty()){cue(p,own.getString("BackHeld").equals("stool")?"The step stool, against the back door.":"A step stool, for the high shelves.");return true;}
        PlaceForYouSite.stool(l,b,"none");PlaceForYou.take(p,PlaceForYouRegistry.STOOL,"The step stool");cue(p,"The step stool. Two treads, paint worn through where feet go.");return true;
    }
    /** The drawer with the tea towels: the back door key, once Mara has said where it is. */
    static boolean drawer(ServerPlayer p,PlaceForYou.House h){
        var own=own(p);
        if(!live(own)||!own.getBoolean("KeyOffered")||own.getBoolean("KeyTaken"))return false;
        PlaceForYou.edit(p,o->o.putBoolean("KeyTaken",true));PlaceForYou.take(p,PlaceForYouRegistry.KEY,"The back door key");
        cue(p,"Tea towels, and under them a ring of keys that aren't hers. The back door key is on its own, with a paper tag.");return true;
    }

    // ------------------------------------------------------------------------------------------------ talking
    /** Spoken to while the passage is going on: Kit to show it, Mara about the key or told with Kit there, Alex about the window. */
    static boolean talk(ServerPlayer p,PlaceForYou.House h,String who,LiteraryActor a){
        var own=own(p);if(!live(own))return false;int stage=own.getInt("BackStage");
        if(who.equals("Kit")){
            if(stage==0&&own.getInt("Chapter")==PlaceForYou.SINK){show(p,h);return true;}
            if(stage>=1&&stage<3){PlaceForYou.above(h,a,own.getString("Quiet").equals("open")&&!own.getBoolean("ToldOpen")?"Go on. I'm here."
                :own.getString("Quiet").equals("refused")?"I said I'd do it myself.":open(own)?"I'll get the bag.":"Push from the end.");return true;}
            return false;
        }
        if(stage<1||stage>=3||open(own)&&!own.getString("Quiet").equals("open"))return false;
        if(who.equals("Alex")&&own.getBoolean("AlexWindow")){PlaceForYou.above(h,a,"I've got it.");return true;}
        if(who.equals("Mara")){
            if(own.getInt("Chapter")!=PlaceForYou.SINK)return false;
            if(own.getString("Quiet").equals("open")&&!own.getBoolean("ToldOpen")){told(p,h);return true;}
            if(!own.getBoolean("BackMaraAsked")&&!open(own)){PlaceForYou.edit(p,o->o.putBoolean("BackMaraAsked",true));key(p,h);return true;}
            return false;
        }
        if(who.equals("Alex")&&!own.getBoolean("WindowAsked")&&!open(own)){PlaceForYou.edit(p,o->o.putBoolean("WindowAsked",true));window(p,h);return true;}
        return false;
    }
    /** Mara, at the sink, asked about the back door. */
    static void key(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"back_mara","Mara");
        PlaceForYou.line(p,h,"Mara","You've been at the back door.");
        Talk.ask(p,Talk.reply("Is there a key for it?",q->{PlaceForYou.line(q,h,"Mara","What for?");
                Talk.ask(q,Talk.reply("Kit wants to go out that way.",z->{tell(z);PlaceForYou.line(z,h,"Mara","...Did Kit ask you to say that?");keyGiven(z,h);})
                    ,Talk.reply("I'd like to see where it goes.",z->{PlaceForYou.line(z,h,"Mara","The path. It only goes to the path.");keyGiven(z,h);}));})
            ,Talk.reply("Kit's going out the back.",q->{tell(q);PlaceForYou.line(q,h,"Mara","Is Kit.");PlaceForYou.line(q,h,"Mara","Thank you for telling me.");Talk.end(q);})
            ,Talk.reply("Never mind.",q->{PlaceForYou.line(q,h,"Mara","Mm.");Talk.end(q);}));
    }
    static void keyGiven(ServerPlayer p,PlaceForYou.House h){
        PlaceForYou.line(p,h,"Mara","It's in the drawer with the tea towels. With his.");
        PlaceForYou.edit(p,o->o.putBoolean("KeyOffered",true));Talk.end(p);
        cue(p,"The drawer with the tea towels, under the counter by the sink.");
    }
    /** What the reader told Mara, in private: a promise of quiet broken if it was given and the bag was still inside. */
    static void tell(ServerPlayer p){
        PlaceForYou.edit(p,o->{o.putBoolean("ToldMara",true);if(o.getString("Quiet").equals("agreed")&&o.getInt("BackStage")<3)o.putBoolean("BrokeQuiet",true);});
    }
    /** Told openly, with Kit standing there. */
    static void told(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"back_told","Mara and Kit");
        PlaceForYou.line(p,h,"Mara","Kit's standing there like they've got something to say.");
        Talk.ask(p,Talk.reply("Kit's found a way out the back. I'm helping.",q->{
            PlaceForYou.line(q,h,"Mara","I know what the back door's for. I put the cupboard there.");
            PlaceForYou.line(q,h,"Kit","You put the cupboard there.");
            PlaceForYou.line(q,h,"Mara","The first winter. So I'd hear it, if anyone went.");
            PlaceForYou.line(q,h,"Mara","The key's in the drawer with the tea towels. With his. Take it, before I think about it.");
            PlaceForYou.edit(q,o->{o.putBoolean("ToldOpen",true);o.putBoolean("KeyOffered",true);});Talk.end(q);
            cue(q,"The drawer with the tea towels, under the counter by the sink.");}));
    }
    /** Alex, offered the window. */
    static void window(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"back_alex","Alex");
        PlaceForYou.line(p,h,"Alex","Kit showed you the back door.");
        PlaceForYou.line(p,h,"Alex","The sitting room window opens if you lift it first. I found that out the first night.");
        PlaceForYou.line(p,h,"Alex","If it's for Kit, I'll hold it.");
        Talk.ask(p,Talk.reply("Hold it for Kit.",q->{PlaceForYou.line(q,h,"Alex","All right. Give me a minute to get there.");
                PlaceForYou.edit(q,o->o.putBoolean("AlexWindow",true));Talk.end(q);})
            ,Talk.reply("Not now.",q->{PlaceForYou.line(q,h,"Alex","Offer stands.");Talk.end(q);}));
    }
}
