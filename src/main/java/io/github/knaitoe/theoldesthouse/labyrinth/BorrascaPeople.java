package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.425, Borrasca's town: the people and the pocket money (docs/BORRASCA_PEOPLE_0_4_425.md). The owner, November 9: "A diner where the
 * player can grab some pie and coffee - the host might respond if the player orders coffee as a child, a sheriff's office with a town drunk,
 * etc." Each shop has its keeper, on stranger phases 146 to 153: Lorna at the diner, Mr. Ames at the store's counter, Mr. Rowe in the
 * antiques shop, Kurt at the gas station, Mrs. Odell at the doctor's desk, Mr. Fenn behind the soda fountain and Gus at the Tipple's bar;
 * Earl sleeps it off in the Sheriff's cell. On the last night only the station and the bar are open.
 *
 * <p>A child has a dollar fifty in change on each visit and a grown reader a wallet with twenty in it. What is left is kept in the reader's
 * own record for the visit ("Cents", "PurseVisit"); the purse item only shows it. Talking to a keeper who sells something opens the counter.
 * The diner's host makes a child's coffee mostly milk, the store will not sell a child a flashlight for the trail, the receptionist gives a
 * child one lollipop from the jar, and Gus turns children out of the bar. The keepers belong to the reader's own site, like the rest of the
 * cast, and stand at their counters.
 */
public final class BorrascaPeople {
    public static final int LORNA=146,AMES=147,ROWE=148,KURT=149,ODELL=150,FENN=151,GUS=152,EARL=153;
    /**
     * 0.4.426, the town's day: the people who are not behind a counter. The Webbs at the diner counter in the daytime (their Paul is on the
     * doctor's corkboard), the Tates waiting at the doctor's on the first visit (their girl's card is on the board by the second), and two
     * retired deputies on the Tipple's stools years later.
     */
    public static final int WEBB=154,SHERRY=155,ROY=156,JOLENE=157,DUTCH=158,MERLE=159;
    private static final String[] SKINS={"lorna","ames","rowe","kurt","odell","fenn","gus","earl","webb","sherry","roy","jolene","dutch","merle"};
    private static final String[] NAMES={"Lorna","Mr. Ames","Mr. Rowe","Kurt","Mrs. Odell","Mr. Fenn","Gus","Earl","Mr. Webb","Mrs. Webb","Roy Tate","Jolene Tate","Dutch","Merle"};
    /** What a child has in change on each childhood visit, and a grown reader in the wallet, in cents. */
    public static final int CHANGE=150,WALLET=2000;
    /** 0.4.426: what the friend has in her own pocket on a childhood visit, to put in when the reader is short. */
    public static final int FRIEND_CHANGE=100;
    private BorrascaPeople(){}

    public static boolean cast(int phase){return phase>=LORNA&&phase<=MERLE;}
    public static String skin(int phase){return "drisking_"+SKINS[phase-LORNA];}
    public static boolean keeper(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    public static String name(LiteraryActor a){return NAMES[a.phase()-LORNA];}
    /** Who is at work: everyone in the daytime visits; on the last night, only the station and the bar, and Earl in his cell. */
    public static boolean staffed(int phase,int visit){return switch(phase){
        case WEBB,SHERRY->visit<=3;
        case ROY,JOLENE->visit==1;
        case DUTCH,MERLE->visit>=3;
        default->visit<4||phase==KURT||phase==GUS||phase==EARL;
    };}

    /** Where each keeper stands, in the world relative to the base: behind each counter, and Earl in the cell. */
    public static BlockPos post(int phase){return switch(phase){
        case LORNA->new BlockPos(-17,0,-85);
        case AMES->BorrascaRooms.world(new BlockPos(-18,0,-26));
        case ROWE->new BlockPos(-14,0,-102);
        case KURT->new BlockPos(-20,0,-117);
        case ODELL->new BlockPos(12,0,-82);
        case FENN->new BlockPos(13,0,-104);
        case GUS->new BlockPos(13,0,-119);
        // The patrons sit: actors placed in a stool or a chair are drawn seated on it.
        case WEBB->new BlockPos(-15,0,-82);
        case SHERRY->new BlockPos(-15,0,-80);
        case ROY->new BlockPos(9,0,-79);
        case JOLENE->new BlockPos(10,0,-79);
        case DUTCH->new BlockPos(14,0,-117);
        case MERLE->new BlockPos(16,0,-117);
        default->BorrascaRooms.world(new BlockPos(28,0,-28));
    };}
    /** The way each faces at rest: across the counter toward the door, Earl toward the bars. */
    private static float yaw(int phase){return switch(phase){case LORNA,ROWE,KURT->-90;case AMES,ODELL,EARL,WEBB,SHERRY->90;case ROY,JOLENE,DUTCH,MERLE->180;default->0;};}

    // ---------------------------------------------------------------------------------------------------------------- what they sell
    public enum Ware {
        COFFEE("Coffee",50,125),PIE("Pie, by the slice",125,350),MALT("Malt",150,375),SODA("Soda",50,125),CANDY("Candy bar",35,100),
        FLASHLIGHT("Flashlight",499,999),BEER("Beer",0,300);
        final String label;final int then,later;
        Ware(String label,int then,int later){this.label=label;this.then=then;this.later=later;}
        /** The early nineties' prices on the childhood visits, and years later the prices the menu and the cards show then. */
        public int price(boolean grown){return grown?later:then;}
        Item icon(){return switch(this){case COFFEE->BorrascaRegistry.COFFEE.get();case PIE->BorrascaRegistry.PIE.get();case MALT->BorrascaRegistry.MALT.get();case SODA->BorrascaRegistry.SODA.get();case CANDY->BorrascaRegistry.CANDY.get();case FLASHLIGHT->BorrascaRegistry.FLASHLIGHT.get();default->BorrascaRegistry.BEER.get();};}
        /** What is handed over: a child's coffee is mostly milk. */
        ItemStack goods(boolean grown){return new ItemStack(this==COFFEE&&!grown?BorrascaRegistry.MILK_COFFEE.get():icon());}
    }
    /** Each counter's goods. Gus serves nothing to a child (a child is not let in). */
    public static List<Ware> wares(int phase,boolean grown){return switch(phase){
        case LORNA->List.of(Ware.COFFEE,Ware.PIE,Ware.MALT);
        case AMES->List.of(Ware.FLASHLIGHT,Ware.SODA,Ware.CANDY);
        case KURT->List.of(Ware.SODA,Ware.CANDY);
        case FENN->List.of(Ware.SODA,Ware.MALT,Ware.CANDY);
        case GUS->grown?List.of(Ware.BEER,Ware.SODA):List.of();
        default->List.of();
    };}

    // ---------------------------------------------------------------------------------------------------------------- each visit
    /** Every reader tick inside the site: the purse for this visit, the keepers at their counters, and the bar's door. */
    public static void tick(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        purse(p,b,own,visit);
        for(int phase=LORNA;phase<=MERLE;phase++){
            var a=ensure(p,b,own,phase);if(a==null)continue;
            boolean on=staffed(phase,visit);
            if(a.isInvisible()==on){a.setInvisible(!on);if(!on)a.setCustomNameVisible(false);}
            var at=Vec3.atBottomCenterOf(b.offset(post(phase)));if(a.position().distanceToSqr(at)>1){a.moveTo(at,yaw(phase),0);a.setYHeadRot(yaw(phase));}
        }
        bar(p,b,own,visit);
    }
    /** A keeper of this reader's, or null while the chunk is not loaded. A keeper that has gone is stood at the counter again. */
    public static @Nullable LiteraryActor keeper(ServerPlayer p,CompoundTag own,int phase){String key="Keeper"+phase;return own.hasUUID(key)&&p.serverLevel().getEntity(own.getUUID(key)) instanceof LiteraryActor a?a:null;}
    /** Whether this keeper is at work for this reader now. */
    public static boolean atWork(ServerPlayer p,CompoundTag own,int phase){var a=keeper(p,own,phase);return a!=null&&!a.isInvisible();}
    private static @Nullable LiteraryActor ensure(ServerPlayer p,BlockPos b,CompoundTag own,int phase){
        var known=keeper(p,own,phase);if(known!=null)return known;
        var l=p.serverLevel();var at=b.offset(post(phase));if(!l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return null;
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(BorrascaStory.PLACE,p.getUUID());a.appearance(LiteraryActor.STRANGER,phase);a.moveTo(Vec3.atBottomCenterOf(at),yaw(phase),0);a.setYHeadRot(yaw(phase));a.setNoAi(true);
        if(!l.addFreshEntity(a))return null;own.putUUID("Keeper"+phase,a.getUUID());return a;
    }

    // ---------------------------------------------------------------------------------------------------------------- the money
    /** The purse for this visit: new change or a new wallet when the visit changes; the one carried shows what is left. */
    private static void purse(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        if(own.getInt("PurseVisit")!=visit){own.putInt("PurseVisit",visit);own.putInt("Cents",visit>=3?WALLET:CHANGE);own.putInt("FriendCents",visit<=2?FRIEND_CHANGE:0);own.remove("Lollipop");own.remove("BarTurnedOut");leave(p);jar(p,b,0);}
        boolean wallet=visit>=3;var held=carried(p,wallet);int cents=own.getInt("Cents");
        if(held==null)p.getInventory().add(BorrascaPurseItem.of(p,cents,wallet));
        else if(BorrascaPurseItem.cents(held)!=cents)BorrascaPurseItem.mark(held,p.getUUID(),cents);
    }
    /** What this reader has left to spend on this visit. */
    public static int cents(CompoundTag own){return own.getInt("Cents");}
    public static @Nullable ItemStack carried(ServerPlayer p,boolean wallet){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(s.getItem() instanceof BorrascaPurseItem purse&&purse.wallet()==wallet&&p.getUUID().equals(BorrascaPurseItem.owner(s)))return s;}return null;}
    /** Leaving the town (or dying in it) leaves the money there: every purse of this reader's comes out of their inventory. */
    public static void leave(ServerPlayer p){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(s.getItem() instanceof BorrascaPurseItem&&p.getUUID().equals(BorrascaPurseItem.owner(s)))inv.setItem(i,ItemStack.EMPTY);}}

    // ---------------------------------------------------------------------------------------------------------------- talking and buying
    /** A word with a keeper: a greeting, then the counter for those who sell something. Returns false for anyone who is not a keeper. */
    public static boolean talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!keeper(a))return false;if(a.isInvisible())return true;
        int visit=own.getInt("Visit");boolean grown=visit>=3;int phase=a.phase();turn(a,p);
        if(phase==ODELL&&!grown){lollipop(p,own);return true;}
        if(phase==GUS&&!grown){BorrascaStory.say(p,a,"Out. No kids in here.");return true;}
        String[] lines=own.getBoolean("SheriffDead")&&own.getInt("SheriffDiedVisit")<visit&&afterTheSheriff(phase)!=null?afterTheSheriff(phase):greetings(phase,visit);String key="Said"+phase;int n=own.getInt(key);own.putInt(key,n+1);
        BorrascaStory.say(p,a,lines[n%lines.length]);
        var wares=wares(phase,grown);if(!wares.isEmpty())counter(p,a,wares,grown);
        return true;
    }
    private static void counter(ServerPlayer p,LiteraryActor a,List<Ware> wares,boolean grown){
        var icons=new HashMap<Integer,ItemStack>();for(int i=0;i<wares.size();i++){var w=wares.get(i);icons.put(i*2,LiteraryChoiceMenu.icon(w.icon(),w.label+"  "+BorrascaPurseItem.money(w.price(grown))));}
        LiteraryChoiceMenu.open(p,name(a),icons,()->BorrascaSites.inside(p)&&a.isAlive()&&!a.isInvisible()&&p.distanceToSqr(a)<36,slot->{
            if(slot%2!=0||slot/2>=wares.size())return false;
            var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),BorrascaStory.PLACE);
            buy(p,own,a,wares.get(slot/2));LiteraryVignettes.save(d,p.getUUID(),BorrascaStory.PLACE,own);return true;
        });
    }
    /** One purchase at a keeper's counter; returns what the keeper said, or null if the keeper does not sell it. */
    public static @Nullable String buy(ServerPlayer p,CompoundTag own,LiteraryActor a,Ware w){
        int visit=own.getInt("Visit");boolean grown=visit>=3;int phase=a.phase();
        if(!keeper(a)||a.isInvisible()||!wares(phase,grown).contains(w))return null;
        turn(a,p);
        if(w==Ware.FLASHLIGHT&&!grown)return speak(p,a,"A flashlight. For the trail, I bet. I don't sell those to kids, not for up there. Bring your folks in.");
        int price=w.price(grown),have=own.getInt("Cents");
        // 0.4.426: friends pool their coins. A child who is short with the friend beside them has her put in the rest, while she has it.
        var friend=grown?null:BorrascaStory.friend(p,own);int pool=own.getInt("FriendCents");
        if(have<price&&friend!=null&&friend.distanceToSqr(p)<64&&have+pool>=price){int top=price-have;own.putInt("FriendCents",pool-top);have=price;own.putInt("Cents",have);own.putInt("Pooled",own.getInt("Pooled")+1);BorrascaStory.say(p,friend,"Here. I've got "+BorrascaPurseItem.money(top)+". We'll split it.");}
        if(have<price)return speak(p,a,phase==LORNA&&!grown?"That's "+BorrascaPurseItem.money(price)+", hon. You're a little short. Come back with your friends.":"That's "+BorrascaPurseItem.money(price)+". You've got "+BorrascaPurseItem.money(have)+".");
        own.putInt("Cents",have-price);var held=carried(p,grown);if(held!=null)BorrascaPurseItem.mark(held,p.getUUID(),have-price);
        var goods=w.goods(grown);if(!p.getInventory().add(goods))p.drop(goods,false);
        return speak(p,a,sold(phase,w,grown,visit));
    }
    private static String speak(ServerPlayer p,LiteraryActor a,String words){BorrascaStory.say(p,a,words);return words;}
    private static String sold(int phase,Ware w,boolean grown,int visit){
        return switch(phase){
            case LORNA->switch(w){
                case COFFEE->grown?"Fresh pot. Cream's on the counter.":"Coffee? You'll be bouncing off the walls. I'll make it mostly milk.";
                case PIE->grown?"Last of the cherry. You're lucky.":"Pecan's what's left. Here you go.";
                default->"One malt, extra thick.";};
            case AMES->w==Ware.FLASHLIGHT?"Batteries are in it. Don't go wandering past the markers with it.":"There you go.";
            case KURT->visit==4?"That it? Okay. We close at ten.":"That it? Okay.";
            case FENN->w==Ware.MALT?"Stool's yours till you finish it.":w==Ware.SODA?"Cherry, lemon-lime, root beer or cola. I'll pick for you.":"There you go.";
            default->w==Ware.BEER?"Cash. Good. Here.":"Soda. Okay.";
        };
    }
    private static String[] greetings(int phase,int visit){boolean grown=visit>=3;return switch(phase){
        case LORNA->grown?new String[]{"Sit anywhere. Coffee's fresh.","Pie's chess today. Nobody orders the chess."}:new String[]{"Sit anywhere, hon. Pie's on the board.","You with your folks? Sit at the counter, then."};
        case AMES->grown?new String[]{"Hardware's on the back wall. Holler if you need something.","Feed's out back. Everything else is in here."}:new String[]{"Look with your eyes, not your hands.","Sodas are in the cooler. Pay up here."};
        case ROWE->grown?new String[]{"The society's through the back. Sign the book if you're from out of town.","Everything in here was somebody's.","The Prescotts lent the society most of its pieces."}:new String[]{"Look all you like. Hands in your pockets.","The society's open Saturdays. Mind the photographs.","That clock's older than me and keeps worse time."};
        case KURT->visit==4?new String[]{"Open till ten. Not much open past that up here.","Pumps are on. Anything from inside?"}:grown?new String[]{"Pump two sticks. Anything from inside?","Ice is in the cooler. Bait's in the back."}:new String[]{"Sodas are in the cooler. Pay me first.","Don't hang on the pumps."};
        case ODELL->new String[]{"He's booked through next month. Half the county's expecting.","Do you have an appointment? No? Then take a seat and wait."};
        case FENN->grown?new String[]{"Prescriptions at the back. The fountain's still open.","A malt, for old times? The stools haven't changed."}:new String[]{"Fountain's open. Stools are for customers.","No running in here."};
        case GUS->visit==4?new String[]{"Last call's at one. What'll it be?","Sheriff's out on the ridge again. What'll it be?"}:new String[]{"What'll it be?","Earl's stool's open. He's in the Sheriff's cell again."};
        case WEBB->grown?new String[]{"Paul's driving now. Works summers out at Prescott's.","Good money at Prescott's. Everybody's kid works there sooner or later."}:new String[]{"First breakfast out since Paul came. His grandma's got him.","Don't tell Lorna, but the pie's better at the church supper."};
        case SHERRY->grown?new String[]{"You look like somebody's kid. Whose are you?","Paul says the pay at Prescott's is better than the plant ever was."}:new String[]{"Paul slept through the night. First time. So we're celebrating.","Dr. Hale delivered him. Dr. Hale delivers everybody."};
        case ROY->new String[]{"Any day now, Doc says.","Don't mind us. We're just waiting on the doctor."};
        case JOLENE->new String[]{"Mrs. Odell says it's a girl. She's never wrong about it.","We picked out her name in March. Before we even knew."};
        case DUTCH->visit==4?new String[]{"Sheriff's up on the ridge tonight. He likes it quiet when he's up there.","We don't talk shop in here. That's Gus's rule."}:new String[]{"Thirty-one years with the county. Never once had to draw.","We don't talk shop in here. That's Gus's rule.","Prescott bought the department its new cruisers. Bought the old ones too."};
        case MERLE->visit==4?new String[]{"Go home. Whatever you're thinking of doing tonight, go home.","You're not from the paper, are you?"}:new String[]{"Garrity girl? I took that report myself. Runaway, the Sheriff said. So that's what I wrote down.","Lot of kids run off from a town this size. You'd be surprised how many.","You're not from the paper, are you?"};
        // 0.4.426: grown, Earl says too much.
        default->visit==4?new String[]{"You're going up there tonight. I can see it on you.","Don't let them see your light past the second marker. That's all I'll say.","They keep me in here so I don't say things. That's a joke. Mostly."}:grown?new String[]{"You again. No. You were smaller.","I wore the star once, same as Dutch and Merle. Ask them what we did with the reports.","Every kid on the board at the store, the Sheriff signs off as a runaway. Every one. They don't run anywhere.","There's a road behind the depot fence the county doesn't plow. Somebody plows it."}:new String[]{"Hey. Hey, kid. Tell the Sheriff I was asleep. That's all I was doing.","It's quiet up here. You notice that? It's always so quiet.","Go on home. Go on. It's getting dark early."};
    };}
    /** 0.4.427: what the bar and the cell say on the visit after the Sheriff was killed on the ridge; null for everyone else. */
    private static @Nullable String[] afterTheSheriff(int phase){return switch(phase){
        case DUTCH->new String[]{"Sheriff's dead. He's still up by the tape. Nobody's gone for him.","Prescott says leave him till the county comes. So he's still lying up there."};
        case MERLE->new String[]{"Somebody's going to have to write it up. It won't be me. Not this time.","They'll send a new one from the county. They always do."};
        case GUS->new String[]{"Drinks are on the house tonight. Don't ask me why.","Nobody's said a word about the Sheriff in here. Keep it that way."};
        case EARL->new String[]{"Somebody finally did it. Don't tell me who. I'd only say it.","Leave the door open on your way out. Nobody's coming to lock it."};
        default->null;
    };}
    private static void turn(LiteraryActor a,ServerPlayer p){float yaw=(float)(Mth.atan2(p.getZ()-a.getZ(),p.getX()-a.getX())*180/Math.PI)-90;a.setYRot(yaw);a.setYHeadRot(yaw);a.setYBodyRot(yaw);}

    // ---------------------------------------------------------------------------------------------------------------- the doctor's jar
    /** A child gets one lollipop from the receptionist's jar each childhood visit, from her hand or off the desk; returns whether it was given. */
    public static boolean lollipop(ServerPlayer p,CompoundTag own){
        var odell=keeper(p,own,ODELL);var b=BorrascaSites.base(p.server,p.getUUID());
        if(own.getInt("Visit")>=3||odell==null||odell.isInvisible()||b==null)return false;
        if(own.getBoolean("Lollipop")){BorrascaStory.say(p,odell,"One's the rule.");return false;}
        own.putBoolean("Lollipop",true);var s=new ItemStack(BorrascaRegistry.LOLLIPOP.get());if(!p.getInventory().add(s))p.drop(s,false);jar(p,b,1);
        BorrascaStory.say(p,odell,own.getInt("Visit")==2?"The Tates had their girl last night. Dr. Hale was up till all hours. Here, take one. Just one.":"Dr. Hale's with someone. Here, take one. Just one.");return true;
    }
    private static void jar(ServerPlayer p,BlockPos b,int stage){var at=b.offset(BorrascaShops.JAR);var s=p.serverLevel().getBlockState(at);if(BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.JAR)&&s.getValue(BorrascaShopBlock.STAGE)!=stage)p.serverLevel().setBlock(at,s.setValue(BorrascaShopBlock.STAGE,stage),3);}

    // ---------------------------------------------------------------------------------------------------------------- the bar's door
    /** Inside the Tipple's walls (world, relative to the base). */
    public static boolean inBar(BlockPos rel){return rel.getX()>=8&&rel.getX()<=21&&rel.getZ()<=-109&&rel.getZ()>=-120&&rel.getY()>=-1&&rel.getY()<=4;}
    /** Where a child turned out of the bar is set: the sidewalk outside its door, facing the street. */
    public static final BlockPos TURNED_OUT=new BlockPos(5,0,-114);
    /** Gus turns a child out of the bar on the childhood visits. */
    private static void bar(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        if(visit>=3||!inBar(p.blockPosition().subtract(b)))return;var gus=keeper(p,own,GUS);if(gus==null||gus.isInvisible())return;
        int n=own.getInt("BarTurnedOut");own.putInt("BarTurnedOut",n+1);
        BorrascaStory.say(p,gus,n==0?"Out. No kids in here.":n==1?"I said out. Go find your folks.":"Out.");
        var to=Vec3.atBottomCenterOf(b.offset(TURNED_OUT));p.stopRiding();p.connection.teleport(to.x,to.y,to.z,90,0);p.setDeltaMovement(Vec3.ZERO);
    }
}
