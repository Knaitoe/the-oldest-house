package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseSitting;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.StaircaseHouseholdRooms.*;
import static io.github.knaitoe.theoldesthouse.labyrinth.StaircaseLeakProps.Kind.*;

/**
 * 0.4.214: the household rooms' chores. Every object is a virtual prop in the room's own saved template: nothing goes into or out
 * of the reader's inventory, and the room is put back as it was once they leave. The people are never seen; what they do is done
 * where the reader is not looking, and what they say is on the action bar.
 */
public final class StaircaseHouseholdChores {
    private StaircaseHouseholdChores(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    static void set(ServerPlayer p,BlockPos at,StaircaseLeakProps.Kind kind){var old=p.serverLevel().getBlockState(at);var s=StaircaseLeakProps.state(kind);if(old.hasProperty(BlockStateProperties.HORIZONTAL_FACING))s=s.setValue(BlockStateProperties.HORIZONTAL_FACING,old.getValue(BlockStateProperties.HORIZONTAL_FACING));p.serverLevel().setBlock(at,s,F);}
    static boolean is(ServerPlayer p,BlockPos at,StaircaseLeakProps.Kind kind){var s=p.serverLevel().getBlockState(at);return s.is(StaircaseLeakRegistry.PROP.get())&&s.getValue(StaircaseLeakProps.KIND)==kind;}
    static void cue(ServerPlayer p,String text){StaircaseLeaks.caption(p,text);p.playNotifySound(SoundEvents.WOOL_PLACE,SoundSource.BLOCKS,.18F,1);}
    static void blocked(ServerPlayer p,BlockPos at,String text){ExpeditionRhythm.refuse(p,at,"household_chore",text);p.playNotifySound(SoundEvents.WOOL_PLACE,SoundSource.BLOCKS,.18F,.8F);}
    /** The reader is looking toward this point (within about fifty degrees). */
    static boolean looking(ServerPlayer p,Vec3 at){var to=at.subtract(p.getEyePosition());return to.lengthSqr()<.01||p.getViewVector(1).dot(to.normalize())>.64;}

    static void arrive(ServerPlayer p,BlockPos b,StaircaseLeakRooms.Kind kind){
        var own=StaircaseLeaks.progress(p);
        if(kind==StaircaseLeakRooms.Kind.DINING){
            // A reader who got up from the last lunch has no place laid: five places, and only the visitor's chair to sit in.
            var rooms=StaircaseHousehold.own(p).getCompound("Rooms").getCompound(kind.name());
            boolean unlaid=rooms.getString("Last").equals("failure");
            if(unlaid){own.putBoolean("Unlaid",true);var l=p.serverLevel();l.setBlock(b.offset(PLACES[5]),StaircaseHouseholdRooms.prop(TABLE_PLAIN,Direction.NORTH),F);l.setBlock(b.offset(CHAIRS[5]),Blocks.AIR.defaultBlockState(),F);}
            StaircaseLeaks.progress(p,own);StaircaseLeaks.caption(p,StaircaseHouseholdTexts.arrive(kind,unlaid));
        }else StaircaseLeaks.caption(p,StaircaseHouseholdTexts.arrive(kind,false));
    }
    public static boolean interact(ServerPlayer p,BlockPos b,BlockPos at,BlockState state,StaircaseLeakRooms.Kind kind){
        var own=StaircaseLeaks.progress(p);boolean done=switch(kind){
            case DINING->dining(p,b,at.subtract(b),at,state,own);
            case BEDROOM->bedroom(p,b,at,state,own);
            case MENDING->mending(p,b,at,state,own);
            case DAWN->dawn(p,b,at,state,own);
            default->false;
        };
        StaircaseLeaks.progress(p,own);return done;
    }
    static void tick(ServerPlayer p,BlockPos b,StaircaseLeakRooms.Kind kind,long elapsed){
        switch(kind){
            case DINING->diningTick(p,b,elapsed);
            case BEDROOM->bedroomTick(p,b,elapsed);
            case MENDING->mendingTick(p,b,elapsed);
            case DAWN->dawnTick(p,b,elapsed);
            default->{}
        }
    }
    private static StaircaseLeakProps.Kind kindAt(BlockState state){return state.is(StaircaseLeakRegistry.PROP.get())?state.getValue(StaircaseLeakProps.KIND):null;}
    private static void finish(ServerPlayer p,CompoundTag own,boolean ok,String line,int beat){own.putString("Result",ok?"success":"failure");StaircaseLeaks.progress(p,own);StaircaseLeaks.caption(p,line);StaircaseLeaks.returnAfterBeat(p,beat);}

    // ------------------------------------------------------------------------------------------------ Fresh sheets
    private static boolean bedroom(ServerPlayer p,BlockPos b,BlockPos at,BlockState state,CompoundTag own){
        if(own.contains("Result"))return true;var k=kindAt(state);if(k==null)return false;
        switch(k){
            case BASKET_FULL,BASKET_HALF->{
                if(own.getBoolean("Holding")){blocked(p,at,"Your hands are full.");break;}
                own.putBoolean("Holding",true);own.putString("Held","sheet");set(p,at,k==BASKET_FULL?BASKET_HALF:BASKET_EMPTY);cue(p,"A sheet from the basket. Still warm from the line.");
            }
            case BASKET_EMPTY->{
                if("sock".equals(own.getString("Held"))){set(p,at,BASKET_SOCK);own.remove("Held");own.putBoolean("Holding",false);own.putBoolean("SockHome",true);cue(p,"On the basket, for its partner.");}
                else cue(p,"The basket's empty.");
            }
            case BASKET_STRIPED->{
                if(own.getBoolean("Holding")){blocked(p,at,"Your hands are full.");break;}
                set(p,at,own.getBoolean("SockHome")?BASKET_SOCK:BASKET_EMPTY);own.putBoolean("Holding",true);own.putString("Held","striped");cue(p,"The striped pillowcase. It wasn't there a moment ago.");
            }
            case BASKET_SOCK->{
                if("sock".equals(own.getString("Held"))){blocked(p,at,"There's a sock on it already.");break;}
                cue(p,"The odd sock, waiting for its partner.");
            }
            case FOLD_EMPTY->{
                if(!"sheet".equals(own.getString("Held"))){blocked(p,at,"Somewhere to shake a sheet out. Take one from the basket.");break;}
                set(p,at,SHEET_ONE_END);own.putLong("Spread",p.serverLevel().getGameTime());own.remove("Held");own.putBoolean("Holding",false);
                cue(p,"You take one end and shake it out. It takes two to fold a sheet.");
            }
            case SHEET_ONE_END->{
                // Folded alone, without waiting for the other end.
                set(p,at,SHEET_CREASED);own.putBoolean("Creased",true);own.remove("Spread");cue(p,"You fold it alone. A crease runs down the middle.");
            }
            case SHEET_LIFTED->blocked(p,at,"Wait. The other end is coming to meet yours.");
            case SHEET_FOLDED,SHEET_CREASED->{
                if(own.getBoolean("Holding")){blocked(p,at,"Your hands are full.");break;}
                set(p,at,FOLD_EMPTY);own.putBoolean("Holding",true);own.putString("Held",k==SHEET_CREASED?"creased":"folded");cue(p,"Folded in four.");
            }
            case AIRING_HIGH,AIRING_HIGH_STACKED->{
                String held=own.getString("Held");
                if(held.equals("folded")||held.equals("creased")){own.putInt("Folded",own.getInt("Folded")+1);own.remove("Held");own.putBoolean("Holding",false);set(p,at,AIRING_HIGH_STACKED);
                    cue(p,own.getInt("Folded")>=2?"On top of the pile. The older sheets are underneath, where Bell likes them.":"Into the airing cupboard, on top.");break;}
                if(own.getBoolean("Made")&&!own.getBoolean("Plain")){own.putBoolean("Plain",true);cue(p,"Two plain pillowcases. The striped one isn't here.");break;}
                cue(p,"Warm, from the pipe behind it.");
            }
            case AIRING_LOW->{
                if(own.getInt("Folded")<2){blocked(p,at,"The fresh sheets go away first. On top.");break;}
                if(own.getBoolean("Holding")){blocked(p,at,"Your hands are full.");break;}
                set(p,at,AIRING_LOW_EMPTY);own.putBoolean("Holding",true);own.putString("Held","older");cue(p,"The older sheets, from underneath. Thin enough for summer.");
            }
            case AIRING_LOW_EMPTY->cue(p,"Bare slats.");
            case BED_FOOT_BARE,BED_HEAD_BARE->{
                if(!"older".equals(own.getString("Held"))){blocked(p,at,"The bed's stripped. The sheets for it are in the airing cupboard.");break;}
                set(p,b.offset(BED_FOOT),BED_FOOT_MADE);set(p,b.offset(BED_HEAD),BED_HEAD_MADE);own.remove("Held");own.putBoolean("Holding",false);own.putBoolean("Made",true);
                p.serverLevel().setBlock(b.offset(SOCK),StaircaseHouseholdRooms.prop(FLOOR_SOCK,Direction.NORTH),F);
                cue(p,"Tucked in. Something falls out of the fitted sheet onto the floor.");
            }
            case FLOOR_SOCK->{
                if(own.getBoolean("Holding")){blocked(p,at,"Your hands are full.");break;}
                p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),F);own.putBoolean("Holding",true);own.putString("Held","sock");cue(p,"A sock, caught inside the fitted sheet. Its partner must be somewhere.");
            }
            case BED_HEAD_MADE,BED_HEAD_PLAIN->{
                String held=own.getString("Held");
                if(held.equals("striped")){set(p,at,BED_HEAD_STRIPED);own.remove("Held");own.putBoolean("Holding",false);own.putBoolean("Striped",true);cue(p,"The striped one.");}
                else if(own.getBoolean("Plain")&&k==BED_HEAD_MADE){set(p,at,BED_HEAD_PLAIN);cue(p,"Two plain ones. It will do.");}
                else blocked(p,at,"The pillow wants a case. They're in the airing cupboard.");
            }
            case BED_FOOT_MADE,BED_HEAD_STRIPED->cue(p,"Tight as a drum.");
            default->{return false;}
        }
        if(own.getBoolean("Striped")&&own.getBoolean("SockHome")&&!own.contains("Result")){
            boolean ok=!own.getBoolean("Creased");if(ok)own.putBoolean("TallyBed",true);
            finish(p,own,ok,ok?"Bell, from the landing: \"That's the one.\"":"The bed's made. Ruth looks at the crease in the folded sheets for a moment and says nothing about it.",100);
        }
        return true;
    }
    private static void bedroomTick(ServerPlayer p,BlockPos b,long elapsed){
        var own=StaircaseLeaks.progress(p);if(own.contains("Result"))return;boolean changed=false;
        // The other end of the sheet lifts by itself for a reader who waits for it.
        if(own.contains("Spread")){long t=p.serverLevel().getGameTime()-own.getLong("Spread");var fold=b.offset(FOLD);
            if(t>=30&&is(p,fold,SHEET_ONE_END)){set(p,fold,SHEET_LIFTED);StaircaseLeaks.caption(p,"The other end lifts, as if someone had taken it.");p.playNotifySound(SoundEvents.WOOL_HIT,SoundSource.BLOCKS,.4F,.9F);}
            if(t>=60&&is(p,fold,SHEET_LIFTED)){set(p,fold,SHEET_FOLDED);own.remove("Spread");changed=true;StaircaseLeaks.caption(p,"It comes to meet yours, and the sheet folds between you.");p.playNotifySound(SoundEvents.WOOL_PLACE,SoundSource.BLOCKS,.4F,.9F);}
        }
        // The striped pillowcase turns up on the basket the moment nobody is looking at it.
        if(own.getBoolean("Plain")&&!own.getBoolean("StripedShown")&&elapsed%10==0){var basket=b.offset(BASKET);
            if(!looking(p,basket.getCenter())&&(is(p,basket,BASKET_EMPTY)||is(p,basket,BASKET_SOCK))){set(p,basket,BASKET_STRIPED);own.putBoolean("StripedShown",true);changed=true;}
        }
        if(changed)StaircaseLeaks.progress(p,own);
    }

    // ------------------------------------------------------------------------------------------------ Mending day
    private static boolean mending(ServerPlayer p,BlockPos b,BlockPos at,BlockState state,CompoundTag own){
        if(own.contains("Result"))return true;var k=kindAt(state);if(k==null)return false;
        switch(k){
            case LAMP_STAND->{
                if(own.getBoolean("LampAside")){cue(p,"The lamp, out of your light now.");break;}
                p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),F);p.serverLevel().setBlock(b.offset(LAMP_ASIDE),StaircaseHouseholdRooms.prop(LAMP_STAND,Direction.SOUTH),F);own.putBoolean("LampAside",true);
                cue(p,"You move the lamp aside. The shadow goes off the work.");
            }
            case SEWING_BOX->{
                if(!own.getBoolean("LampAside")){blocked(p,at,"Ruth: \"You're making a shadow.\"");break;}
                int tries=own.getInt("Tries")+1;own.putInt("Tries",tries);
                if(tries<3)cue(p,tries==1?"The thread misses the eye.":"Misses again.");
                else{set(p,at,SEWING_THREADED);own.putBoolean("Threaded",true);cue(p,"Third try. Through.");}
            }
            case SEWING_THREADED->cue(p,"Threaded, and a knot in the end.");
            case COAT_SLEEVE->{
                // A crouching hand turns the sleeve even with the needle threaded; otherwise the needle goes in.
                if(own.getBoolean("Threaded")&&!p.isShiftKeyDown()){
                    int shut=own.getInt("Shut")+1;own.putInt("Shut",shut);set(p,at,COAT_SHUT);own.putLong("ShutAt",p.serverLevel().getGameTime());
                    cue(p,shut==1?"You've stitched the sleeve shut.":"Shut again.");
                }else{set(p,at,COAT_TURNED);cue(p,"You turn the sleeve inside out first.");}
            }
            case COAT_TURNED->{
                if(!own.getBoolean("Threaded")){blocked(p,at,"The needle isn't threaded.");break;}
                set(p,at,COAT_SEWN);own.putBoolean("Sewn",true);cue(p,"Stitched, right side in.");
            }
            case COAT_SHUT->blocked(p,at,own.getInt("Shut")>=2?"It's sewn shut.":"Ruth is unpicking it.");
            case COAT_SEWN->{
                if(!own.contains("Button")){blocked(p,at,"There's a button missing. The tin.");break;}
                set(p,at,COAT_BUTTONED);own.putBoolean("TallyCoat",true);finish(p,own,true,"Ruth: \"That'll see the winter out.\"",100);
            }
            case BUTTON_TIN->{set(p,at,BUTTON_TIN_OPEN);cue(p,"A biscuit tin full of buttons. Look in it for one that matches.");}
            case BUTTON_TIN_OPEN->{
                String[] buttons={"a black horn button with four holes","a brass button off a uniform","a blue glass button","a shell button, a little chipped","a covered button in the coat's own blue"};
                int i=(own.getInt("Rummage")+1)%buttons.length;own.putInt("Rummage",i);own.putString("Button",buttons[i]);
                cue(p,"You choose "+buttons[i]+". (Again for another.)");
            }
            default->{return false;}
        }
        return true;
    }
    private static void mendingTick(ServerPlayer p,BlockPos b,long elapsed){
        var own=StaircaseLeaks.progress(p);if(own.contains("Result")||!own.contains("ShutAt"))return;
        long t=p.serverLevel().getGameTime()-own.getLong("ShutAt");
        if(own.getInt("Shut")>=2&&t>=20){own.remove("ShutAt");finish(p,own,false,"Ruth looks at the sleeve, and doesn't unpick it this time. The coat stays as it is.",100);return;}
        // The first time, Ruth unpicks it, slowly, and lets the reader try once more.
        if(t==20)StaircaseLeaks.caption(p,"Ruth takes it from you and unpicks it, slowly, without a word about it.");
        if(t>=80){set(p,b.offset(COAT),COAT_SLEEVE);own.remove("ShutAt");StaircaseLeaks.progress(p,own);
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("Ruth: \"Turn it first.\" ").append(net.minecraft.network.chat.Component.literal("(Crouch to turn the sleeve.)").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC)),true);}
    }

    // ------------------------------------------------------------------------------------------------ First light
    private static final int WAKE=60;
    private static boolean dawn(ServerPlayer p,BlockPos b,BlockPos at,BlockState state,CompoundTag own){
        if(own.contains("Result"))return true;var k=kindAt(state);if(k==null)return false;
        switch(k){
            case KETTLE_COLD->{own.putLong("Kettle",p.serverLevel().getGameTime());set(p,at,KETTLE_ON);p.playNotifySound(SoundEvents.FIRE_AMBIENT,SoundSource.BLOCKS,.3F,1);cue(p,own.getInt("Boils")==0?"The kettle on. Quietly.":"The kettle on again.");}
            case KETTLE_ON->cue(p,"Not yet.");
            case KETTLE_BOILED->{
                set(p,at,KETTLE_COLD);own.remove("Kettle");own.remove("Whistle");int boils=own.getInt("Boils")+1;own.putInt("Boils",boils);own.putBoolean("Hot",true);
                cue(p,"Off before it sings. "+(boils==1?"Pour the first cup.":"The second cup."));
            }
            case TEACUP_EMPTY->{
                if(!own.getBoolean("Hot")){blocked(p,at,"The kettle first.");break;}
                set(p,at,TEACUP_FULL);own.putBoolean("Hot",false);int cups=own.getInt("Cups")+1;own.putInt("Cups",cups);
                cue(p,cups==1?"The first cup. It's for your hands.":"The second cup. That one's for the settee.");
            }
            case TEACUP_FULL->{
                boolean first=at.equals(b.offset(FIRST_CUP));
                if(first&&!own.getBoolean("Warmed")){own.putBoolean("Warmed",true);cue(p,"You hold it until your hands are warm.");break;}
                if(!first){if(own.getBoolean("Holding")){blocked(p,at,"Your hands are full.");break;}set(p,at,TEACUP_EMPTY);own.putBoolean("Holding",true);own.putString("Held","cup");cue(p,"Carefully. It's full to the brim.");}
                else cue(p,"Your hands are warm.");
            }
            case SIDE_EMPTY->{
                if(!"cup".equals(own.getString("Held"))){blocked(p,at,"A little table by the settee, by the sleeper's head.");break;}
                set(p,at,SIDE_CUP);own.remove("Held");own.putBoolean("Holding",false);own.putBoolean("CupDown",true);p.playNotifySound(SoundEvents.DECORATED_POT_PLACE,SoundSource.BLOCKS,.15F,1.5F);cue(p,"Set down beside the sleeper, without a sound.");
            }
            case SIDE_CUP->cue(p,"Steam off it.");
            case POTATOES->{set(p,at,POTATOES_PEELED);own.putBoolean("Peeled",true);cue(p,"One more than usual. Bell will be late.");}
            case POTATOES_PEELED->cue(p,"Peeled, in cold water.");
            case SUPPER_PLATE->{if(!own.getBoolean("Peeled")){blocked(p,at,"Bell's plate. The extra potato goes on it.");break;}set(p,at,SUPPER_WRAPPED);cue(p,"You wrap it in paper.");}
            case SUPPER_WRAPPED->{set(p,at,SUPPER_NAMED);own.putBoolean("Named",true);cue(p,"You write BELL on the paper, though nobody else would take it.");}
            case SUPPER_NAMED->cue(p,"BELL, on the paper.");
            case SLEEPER_IN,SLEEPER_OUT->cue(p,"Somebody asleep under the blanket. You can't see who.");
            case SETTEE_FEET->cue(p,"Feet, under the blanket's end.");
            default->{return false;}
        }
        if(own.getBoolean("CupDown")&&own.getBoolean("Named")&&!own.contains("Result")){own.putBoolean("TallySleeper",true);finish(p,own,true,"The sleeper turns over and doesn't wake. Outside, the first light.",100);}
        return true;
    }
    private static void dawnTick(ServerPlayer p,BlockPos b,long elapsed){
        var own=StaircaseLeaks.progress(p);if(own.contains("Result"))return;boolean changed=false;
        // The blanket rises and falls.
        var sleeper=b.offset(SLEEPER);if(elapsed%40==0)set(p,sleeper,is(p,sleeper,SLEEPER_IN)?SLEEPER_OUT:SLEEPER_IN);
        // The kettle boils after five seconds and sings two seconds after that, if nobody takes it off.
        if(own.contains("Kettle")){long t=p.serverLevel().getGameTime()-own.getLong("Kettle");var kettle=b.offset(KETTLE);
            if(t==100){set(p,kettle,KETTLE_BOILED);StaircaseLeaks.caption(p,"The kettle's nearly boiled.");}
            if(t>=140){own.putBoolean("Whistle",true);changed=true;}}
        float noise=own.getFloat("Noise");
        if(own.getBoolean("Whistle"))noise+=1.5F;
        if(p.isSprinting())noise+=2;
        if(!p.onGround()&&p.getDeltaMovement().y>.1)noise+=4;
        if(elapsed%20==0&&own.getBoolean("Whistle"))p.playNotifySound(SoundEvents.NOTE_BLOCK_FLUTE.value(),SoundSource.BLOCKS,.6F,1.8F);
        noise=Math.max(0,noise-.25F);
        if(noise>=WAKE/2F&&own.getFloat("Noise")<WAKE/2F)StaircaseLeaks.caption(p,"The blanket shifts.");
        if(noise!=own.getFloat("Noise")){own.putFloat("Noise",noise);changed=true;}
        if(noise>=WAKE){set(p,sleeper,SLEEPER_AWAKE);finish(p,own,false,"The blanket's thrown back. \"What time is it?\" You say it's ten to six.",100);return;}
        if(changed)StaircaseLeaks.progress(p,own);
    }

    // ------------------------------------------------------------------------------------------------ Sunday lunch
    private static int places(CompoundTag own){return own.getBoolean("Unlaid")?5:6;}
    private static int placeIndex(BlockPos rel){for(int i=0;i<PLACES.length;i++)if(PLACES[i].equals(rel))return i;return -1;}
    private static int chairIndex(BlockPos rel){for(int i=0;i<CHAIRS.length;i++)if(CHAIRS[i].equals(rel))return i;return -1;}
    private static boolean dining(ServerPlayer p,BlockPos b,BlockPos rel,BlockPos at,BlockState state,CompoundTag own){
        if(own.contains("Result"))return true;
        if(state.getBlock() instanceof HouseholdFurnitureBlock&&state.getValue(HouseholdFurnitureBlock.KIND)==HouseholdFurnitureBlock.Kind.CANE_CHAIR){
            if(rel.equals(SEVENTH)){
                if(own.getInt("Laid")<places(own)){blocked(p,at,"The household's places first. Forks are in the sideboard.");return true;}
                p.serverLevel().setBlock(at,Blocks.AIR.defaultBlockState(),F);p.serverLevel().setBlock(b.offset(SEVENTH_UP),chair(Direction.WEST),F);
                own.putBoolean("Seventh",true);p.playNotifySound(SoundEvents.WOOD_PLACE,SoundSource.BLOCKS,.4F,.8F);cue(p,"The seventh chair, up to the end of the table. For the visitor.");return true;
            }
            boolean visitorChair=rel.equals(SEVENTH_UP);int chair=chairIndex(rel);
            if(own.getLong("Meal")>0){blocked(p,at,"Somebody is sitting there.");return true;}
            if(own.getInt("Bowls")<places(own)){blocked(p,at,own.getBoolean("Tray")?"Every bowl on the table first.":"Not until the table's laid and served.");return true;}
            if(visitorChair&&!own.getBoolean("Unlaid")){blocked(p,at,"That's the visitor's chair.");return true;}
            if(!visitorChair&&own.getBoolean("Unlaid")){blocked(p,at,"Somebody else's place. There's no chair at the table for you. Only the one at the end.");return true;}
            if(!visitorChair&&chair<0)return true;
            if(!HouseSitting.sit(p,at)){blocked(p,at,"Closer to the chair.");return true;}
            if(p.getVehicle()!=null)StaircaseLeaks.virtual(p.getVehicle());
            own.putInt("Seat",visitorChair?-1:chair);own.putLong("Meal",p.serverLevel().getGameTime());own.putInt("Line",0);
            StaircaseLeaks.caption(p,"Chairs scrape out around the table. Nobody is in them.");
            for(int i=0;i<CHAIRS.length;i++)if(i!=chair&&!(own.getBoolean("Unlaid")&&i==5))moveChair(p,b,CHAIRS[i],pushedBack(CHAIRS[i]));
            p.playNotifySound(SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.5F,.7F);return true;
        }
        if(!state.is(StaircaseLeakRegistry.PROP.get()))return false;var k=state.getValue(StaircaseLeakProps.KIND);
        switch(k){
            case SIDEBOARD_SHUT->{set(p,b.offset(SIDEBOARD),SIDEBOARD_OPEN);set(p,b.offset(SIDEBOARD).north(),SIDEBOARD_OPEN);own.putBoolean("Cutlery",true);cue(p,"The cutlery drawer. Forks first.");}
            case SIDEBOARD_OPEN->cue(p,"Forks, knives, the big spoons. One of the forks has a different handle.");
            case PLACE_BARE->{
                if(!own.getBoolean("Cutlery")){blocked(p,at,"The cutlery is in the sideboard.");break;}
                int i=placeIndex(rel);boolean odd=(i==1||i==4)&&!own.getBoolean("Changed");set(p,at,odd?PLACE_ODD:PLACE_LAID);own.putInt("Laid",own.getInt("Laid")+1);
                p.playNotifySound(SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.25F,1.6F);cue(p,odd?"Forks first. This one's handle doesn't match the others.":"Forks first, then the rest.");
            }
            case PLACE_ODD->blocked(p,at,own.getBoolean("Cups")?"Not that place. The fork's handle doesn't match.":"The handle doesn't match. Somebody will mind.");
            case PLACE_LAID->{
                if(!own.getBoolean("Cups")){blocked(p,at,own.getInt("Laid")<places(own)?"Every place laid first.":"The good cups are in the cupboard.");break;}
                if(!own.getBoolean("Cracked")){blocked(p,at,"Not the cracked one. That goes to the flowers.");break;}
                set(p,at,PLACE_CUP);own.putInt("CupsLaid",own.getInt("CupsLaid")+1);p.playNotifySound(SoundEvents.DECORATED_POT_PLACE,SoundSource.BLOCKS,.3F,1.5F);cue(p,"A good cup.");
            }
            case PLACE_CUP->{
                if(!own.getBoolean("Tray")){blocked(p,at,"Laid. The bowls come through the hatch.");break;}
                set(p,at,PLACE_BOWL);own.putInt("Bowls",own.getInt("Bowls")+1);p.playNotifySound(SoundEvents.DECORATED_POT_PLACE,SoundSource.BLOCKS,.3F,1.1F);
                cue(p,own.getInt("Bowls")>=places(own)?(own.getBoolean("Unlaid")?"Every place served. Sit down, then. The visitor's chair is the only one.":"Every place served. Sit down, then."):"Hot through the cloth.");
            }
            case PLACE_BOWL->cue(p,"Steam off it.");
            case CUPBOARD_CUPS->{
                if(own.getInt("Laid")<places(own)){blocked(p,at,"Forks first.");break;}
                set(p,at,CUPBOARD_EMPTY);own.putBoolean("Cups",true);cue(p,"The good cups. One of them has a hairline crack.");
            }
            case CUPBOARD_EMPTY->cue(p,"Only the saucers left on the shelf.");
            case VASE_FLOWERS->{if(!own.getBoolean("Cups")){blocked(p,at,"Flowers from the garden.");break;}set(p,at,VASE_CUP);own.putBoolean("Cracked",true);cue(p,"The cracked one goes to the flowers.");}
            case VASE_CUP->cue(p,"Water in the cracked cup, and the stalks in it.");
            case TABLE_END->{if(!own.getBoolean("Seventh")){blocked(p,at,"There's no chair at this end.");break;}set(p,at,TABLE_END_LAID);cue(p,"A place at the end, for the visitor.");}
            case TABLE_END_LAID->{
                if(!own.getBoolean("Cups")||!own.getBoolean("Cracked")){blocked(p,at,"The good cups are in the cupboard.");break;}
                set(p,at,TABLE_END_CUP);own.putBoolean("Visitor",true);cue(p,"He never comes. A good cup for him anyway.");
            }
            case TABLE_END_CUP->cue(p,own.getBoolean("Tray")?"Nobody serves the visitor's place.":"The visitor's place.");
            case HATCH_BOWLS->{for(var h:HATCH)set(p,b.offset(h),HATCH_EMPTY);own.putBoolean("Tray",true);cue(p,"Bowls, hot through the cloth.");}
            case HATCH_EMPTY->cue(p,own.getBoolean("Hatch")?"Somebody is moving about in the kitchen.":"The hatch to the kitchen. A pan lid rattles somewhere through it.");
            case BREAD_BOARD->{
                if(own.getLong("Meal")==0||!p.isPassenger()){blocked(p,at,"Not before everyone's sat down.");break;}
                set(p,at,BREAD_CUT);p.playNotifySound(SoundEvents.GENERIC_EAT,SoundSource.PLAYERS,.4F,1);cue(p,"A slice of the bread. Still warm.");own.putBoolean("Bread",true);
            }
            case BREAD_CUT->cue(p,"Crumbs on the board.");
            default->{return false;}
        }
        return true;
    }
    private static void moveChair(ServerPlayer p,BlockPos b,BlockPos from,BlockPos to){
        var l=p.serverLevel();var s=l.getBlockState(b.offset(from));if(!(s.getBlock() instanceof HouseholdFurnitureBlock)||!l.getBlockState(b.offset(to)).isAir())return;
        l.setBlock(b.offset(to),s,F);l.setBlock(b.offset(from),Blocks.AIR.defaultBlockState(),F);
    }
    private static void diningTick(ServerPlayer p,BlockPos b,long elapsed){
        var own=StaircaseLeaks.progress(p);if(own.contains("Result"))return;boolean changed=false;
        // Ruth changes the two forks that don't match, while the reader is looking somewhere else.
        if(own.getInt("Laid")>=places(own)&&!own.getBoolean("Changed")&&elapsed%10==0){
            var a=b.offset(PLACES[1]);var c=b.offset(PLACES[4]);
            if(!looking(p,a.getCenter())&&!looking(p,c.getCenter())){
                for(var at:new BlockPos[]{a,c})if(is(p,at,PLACE_ODD))set(p,at,PLACE_LAID);
                own.putBoolean("Changed",true);changed=true;p.playNotifySound(SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.2F,1.8F);
                StaircaseLeaks.caption(p,"Two of the forks have been changed. The handles match now.");
            }
        }
        // The bowls come through the hatch once the table is laid, from somebody in the kitchen.
        if(own.getInt("CupsLaid")>=places(own)&&own.getBoolean("Visitor")&&!own.getBoolean("Hatch")&&elapsed%10==0&&!looking(p,b.offset(HATCH[1]).getCenter())){
            for(var h:HATCH)set(p,b.offset(h),HATCH_BOWLS);own.putBoolean("Hatch",true);changed=true;
            p.playNotifySound(SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.5F,1.2F);StaircaseLeaks.caption(p,"Ruth, from the kitchen: \"Mind, they're hot.\"");
        }
        long meal=own.getLong("Meal");
        if(meal>0){
            long t=p.serverLevel().getGameTime()-meal;int seat=own.getInt("Seat");
            // The household sits down: their chairs, pushed out a moment ago, come back in to the table.
            if(t==20)for(int i=0;i<CHAIRS.length;i++)if(i!=seat)moveChair(p,b,pushedBack(CHAIRS[i]),CHAIRS[i]);
            if(!p.isPassenger()&&t>5){
                own.putString("Result","failure");changed=true;StaircaseLeaks.caption(p,"The talk stops. Nobody looks up. Your bowl stays where it is.");
                StaircaseLeaks.progress(p,own);StaircaseLeaks.returnAfterBeat(p,80);return;
            }
            if(t>=40&&t%60==40){int line=own.getInt("Line");
                if(line<StaircaseHouseholdTexts.LUNCH.size()){StaircaseLeaks.caption(p,StaircaseHouseholdTexts.lunchLine(line,p.getGameProfile().getName()));own.putInt("Line",line+1);changed=true;
                    if(line%3==1)p.playNotifySound(SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.15F,1.7F);}
                else{
                    for(int i=0;i<CHAIRS.length;i++)if(i!=seat)moveChair(p,b,CHAIRS[i],pushedBack(CHAIRS[i]));
                    p.playNotifySound(SoundEvents.WOOD_HIT,SoundSource.BLOCKS,.5F,.7F);
                    own.putString("Result","success");own.putBoolean("TallyPlace",true);changed=true;StaircaseLeaks.caption(p,"The chairs push back. Mara: \"I'll wash.\" Ruth: \"You washed last week.\"");
                    StaircaseLeaks.progress(p,own);StaircaseLeaks.returnAfterBeat(p,100);return;
                }
            }
        }
        if(changed)StaircaseLeaks.progress(p,own);
    }
}
