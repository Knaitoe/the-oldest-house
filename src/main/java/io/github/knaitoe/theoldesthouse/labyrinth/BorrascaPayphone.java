package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.BorrascaPayphonePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;

/**
 * 0.4.426: the payphone on the wall of Ridge Service (docs/BORRASCA_TOWN_DAY_0_4_426.md). The reader dials on its own screen
 * (BorrascaPayphoneScreen) and pays from the purse: a quarter on the childhood visits, two quarters years later; 911 is free and a number
 * not in service gives the coin back. The card under the dial lists the Sheriff's office and the doctor's; Prescott's number is on his
 * truck's sign. Whoever answers depends on the visit. The Sheriff's office has no open case for a missing child, whoever asks.
 */
public final class BorrascaPayphone {
    public static final String SHERIFF="5550117",DOCTOR="5550162",PRESCOTT="5550190",EMERGENCY="911";
    private BorrascaPayphone(){}

    /** What a call costs: a quarter in the early nineties, two years later. */
    public static int price(boolean grown){return grown?50:25;}
    /** A placed call: how many rings before it is answered (0 for none), what is said, and whether the coin was kept. */
    public record Call(String number,int rings,List<String> lines,boolean charged){}

    /** Opens the dial for the reader, with what is in the purse. */
    public static void open(ServerPlayer p,CompoundTag own){send(p,own,true,null);}
    private static void send(ServerPlayer p,CompoundTag own,boolean open,@Nullable Call call){
        var t=new CompoundTag();t.putInt("Cents",own.getInt("Cents"));t.putInt("Price",price(own.getInt("Visit")>=3));t.putBoolean("Grown",own.getInt("Visit")>=3);
        if(call!=null){var c=new CompoundTag();c.putString("Number",call.number());c.putInt("Rings",call.rings());var lines=new ListTag();for(var l:call.lines())lines.add(StringTag.valueOf(l));c.put("Lines",lines);t.put("Call",c);}
        HousePackets.send(p,new BorrascaPayphonePayload(open,t));
    }
    /** The client dialled: place the call if the reader is still at the phone, and send back what happened. */
    public static void dialled(ServerPlayer p,String number){
        if(!BorrascaSites.inside(p))return;var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),BorrascaStory.PLACE);
        var call=dial(p,own,number);if(call==null)return;LiteraryVignettes.save(d,p.getUUID(),BorrascaStory.PLACE,own);send(p,own,false,call);
    }
    /** At the payphone: within reach of it on the station's wall. */
    public static boolean atPhone(ServerPlayer p){var b=BorrascaSites.base(p.server,p.getUUID());return b!=null&&p.position().distanceToSqr(b.offset(BorrascaShops.PAYPHONE).getCenter())<25;}

    /** One call from the payphone. Returns null if the reader is not at the phone. */
    public static @Nullable Call dial(ServerPlayer p,CompoundTag own,String dialled){
        if(!atPhone(p))return null;
        String number=dialled.replaceAll("[^0-9]","");int visit=own.getInt("Visit");boolean grown=visit>=3;
        int price=number.equals(EMERGENCY)?0:price(grown),have=own.getInt("Cents");
        if(have<price)return new Call(number,0,List.of(grown?"The phone wants two quarters. The wallet doesn't have them.":"The phone wants a quarter. Your pockets don't have one."),false);
        var call=answer(own,number,visit,grown);
        if(call.charged()&&price>0){own.putInt("Cents",have-price);var held=BorrascaPeople.carried(p,grown);if(held!=null)BorrascaPurseItem.mark(held,p.getUUID(),have-price);}
        own.putBoolean("Called"+number+"_"+visit,true);
        return call;
    }
    private static Call answer(CompoundTag own,String number,int visit,boolean grown){
        boolean missing=own.getBoolean("FriendMissing");String friend=BorrascaStory.FRIEND;
        return switch(number){
            case EMERGENCY->new Call(number,1,visit==4
                ?List.of("County dispatch. What's your emergency?","...","The ridge. All right. I'll let the Sheriff know you called. He's up there tonight.","The line clicks off.")
                :grown?List.of("County dispatch. What's your emergency?","...","Is anybody hurt? Then I'll pass it to the Sheriff's office. You have a good day.")
                :List.of("County dispatch. What's your emergency?","...","Is there a grown-up with you? Put them on, please.","...","Then go home, and don't play on the phones. This line's for emergencies."),false);
            case SHERIFF->new Call(number,visit==4?1:3,switch(visit){
                case 1->List.of("Sheriff's office.","...","This line's for grown-ups, kid. Go on home before it gets dark.");
                case 2->missing?List.of("Sheriff's office.","...",friend+" Garrity? Hold on.","...","There's no open case under that name. Kids wander off up here. She'll be home for supper.","The line clicks off.")
                    :List.of("Sheriff's office.","...","Is this about the Maddox boy? Then hang up, kid. This line's for grown-ups.");
                case 3->List.of("Drisking County Sheriff's office.","...","Garrity. Let me look.","...","There's no open case under that name. There's no file at all. Is there anything else?");
                default->List.of("...","Somebody picks up and doesn't speak. A radio hisses behind him.","Who is this?","The line goes dead.");
            },true);
            case DOCTOR->new Call(number,2,visit==4
                ?List.of("A machine picks up.","You've reached Dr. Hale's office. If you're expecting, the doctor can be reached day or night through the county at—","The tape runs out.")
                :List.of("Dr. Hale's office. He's with a patient.","...",grown?"Are you a patient of ours? We're booked through next month.":"Is your mom there, hon? No? Then I'll let you go."),true);
            case PRESCOTT->new Call(number,visit==4?6:2,switch(visit){
                case 1,2->List.of("Prescott Ore Services.","...","Mr. Prescott isn't in. Is your daddy one of our drivers? Then you shouldn't be calling here.");
                case 3->List.of("Prescott Ore Services.","...","Who gave you this number?","The line goes dead.");
                default->List.of("It rings for a long time. Then somebody picks up.","A machine is running somewhere behind them, grinding and stopping and grinding.","Nobody speaks. The machine stops. The line goes dead.");
            },true);
            default->new Call(number,0,List.of("Three rising tones.","The number you have dialed is not in service. Please check the number and dial again.","The coin drops back into the return."),false);
        };
    }
}
