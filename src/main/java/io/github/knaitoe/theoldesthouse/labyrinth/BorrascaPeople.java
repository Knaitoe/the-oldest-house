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
    /** 0.4.428, the town's routines: Mr. Dorsey carries the mail up one side of the street and down the other, on stranger phase 160. */
    public static final int CARRIER=160;
    private static final String[] SKINS={"lorna","ames","rowe","kurt","odell","fenn","gus","earl","webb","sherry","roy","jolene","dutch","merle","dorsey"};
    private static final String[] NAMES={"Lorna","Mr. Ames","Mr. Rowe","Kurt","Mrs. Odell","Mr. Fenn","Gus","Earl","Mr. Webb","Mrs. Webb","Roy Tate","Jolene Tate","Dutch","Merle","Mr. Dorsey"};
    /**
     * 0.4.428: the town's day. Each reader's own clock runs while they are in Drisking, DAY ticks to a turn. From SMOKE_FROM to SMOKE_TO
     * Mr. Rowe steps out front of his shop for a smoke: the only time a child can get into the historical society behind it. He goes and
     * comes back only while neither the counter nor the stoop is watched. Back at the counter for CATCH ticks, he sends out a child he finds
     * in the society; a child he finds there has been in it SEEN ticks.
     */
    public static final int DAY=2400,SMOKE_FROM=1200,SMOKE_TO=1800,CATCH=40,SEEN=20;
    /** Where Mr. Rowe smokes, on the sidewalk beside his door; where a child he catches in the society is sent, his shop's floor. */
    public static final BlockPos STOOP=new BlockPos(-6,0,-102),SENT_OUT=new BlockPos(-9,0,-100);
    /** Mr. Dorsey's round, in the world relative to the base: up the west side past each door, across, and back down the east side. */
    public static final BlockPos[] ROUND={new BlockPos(-5,0,-79),new BlockPos(-5,0,-84),new BlockPos(-5,0,-100),new BlockPos(-5,0,-114),new BlockPos(6,0,-114),new BlockPos(6,0,-98),new BlockPos(6,0,-84),new BlockPos(6,0,-79)};
    /** The round's stops (at a door, where he puts the mail through), and how long he stands at each. */
    private static final boolean[] STOPS={false,true,true,true,true,true,true,false};
    public static final int STOP_TICKS=60;
    /** What a child has in change on each childhood visit, and a grown reader in the wallet, in cents. */
    public static final int CHANGE=150,WALLET=2000;
    /** 0.4.426: what the friend has in her own pocket on a childhood visit, to put in when the reader is short. */
    public static final int FRIEND_CHANGE=100;
    private BorrascaPeople(){}

    public static boolean cast(int phase){return phase>=LORNA&&phase<=CARRIER;}
    public static String skin(int phase){return "drisking_"+SKINS[phase-LORNA];}
    public static boolean keeper(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&cast(a.phase());}
    public static String name(LiteraryActor a){return NAMES[a.phase()-LORNA];}
    /** Who is at work: everyone in the daytime visits; on the last night, only the station and the bar, and Earl in his cell. */
    public static boolean staffed(int phase,int visit){return switch(phase){
        case WEBB,SHERRY->visit<=3;
        case ROY,JOLENE->visit==1;
        case DUTCH,MERLE->visit>=3;
        case CARRIER->visit<4;
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
        case CARRIER->ROUND[0];
        default->BorrascaRooms.world(new BlockPos(28,0,-28));
    };}
    /**
     * 0.4.444 (owner: "He shouldn't still be in jail upon returning as an adult. Make him a vagrant on the street instead."): years
     * later Earl sits on a flattened box at the mouth of the alley beside the Tipple, in the world relative to the base.
     */
    public static final BlockPos EARL_STREET=new BlockPos(8,0,-107);
    /** The Sheriff's cell, where it was authored: its bars along the west and south, and its door among them. */
    public static boolean cell(BlockPos rel){return rel.getY()>=0&&rel.getY()<=3&&(rel.getX()==26&&rel.getZ()>=-29&&rel.getZ()<=-26||rel.getZ()==-26&&rel.getX()>=27&&rel.getX()<=29);}
    public static BlockPos post(int phase,int visit){return phase==EARL&&visit>=3?EARL_STREET:post(phase);}
    /** The way each faces at rest: across the counter toward the door, Earl toward the bars (and, years later, the street). */
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
        own.putInt("TownClock",(own.getInt("TownClock")+5)%DAY);
        for(int phase=LORNA;phase<=CARRIER;phase++){
            var a=ensure(p,b,own,phase);if(a==null)continue;
            boolean on=staffed(phase,visit);
            if(a.isInvisible()==on){a.setInvisible(!on);if(!on){a.setCustomNameVisible(false);if(phase==CARRIER)BorrascaCast.stop(a);}}
            if(phase==CARRIER){if(on)round(own,a,b);continue;}
            if(phase==ROWE&&on)rowe(p,b,own,a);
            boolean out=phase==ROWE&&own.getBoolean("RoweOut");float yaw=out?-90:yaw(phase);
            var at=Vec3.atBottomCenterOf(b.offset(out?STOOP:post(phase,visit)));
            // 0.4.429: a keeper who has walked a child out walks back to the counter; nobody is snapped there while walking.
            if(a.getPersistentData().contains("BorrascaSpeed")||shoving(own,phase))continue;
            if(a.position().distanceToSqr(at)>1){if(a.getPersistentData().getBoolean("BorrascaAway")){a.getPersistentData().remove("BorrascaAway");BorrascaCast.walk(a,at,.7);}else{a.moveTo(at,yaw,0);a.setYHeadRot(yaw);}}
        }
        bar(p,b,own,visit);
        society(p,b,own,visit);
    }

    // ---------------------------------------------------------------------------------------------------------------- the routines
    /** Whether the town's clock is in Mr. Rowe's smoke break. */
    public static boolean smoking(CompoundTag own){int c=own.getInt("TownClock");return c>=SMOKE_FROM&&c<SMOKE_TO;}
    /** Mr. Rowe goes out front and comes back in by the clock, only while neither his counter nor the stoop is watched. */
    private static void rowe(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor a){
        var l=p.serverLevel();boolean want=smoking(own),out=own.getBoolean("RoweOut");
        if(want!=out){
            var to=Vec3.atBottomCenterOf(b.offset(want?STOOP:post(ROWE))).add(0,1.6,0);
            if(!LiteraryVignettes.watched(l,a.getEyePosition())&&!LiteraryVignettes.watched(l,to)){
                own.putBoolean("RoweOut",want);own.putInt("RoweIn",0);
                // The shop bell over the door as he comes back in; anyone in the society hears it.
                if(!want){LiteraryVignettes.heard(p,net.minecraft.sounds.SoundEvents.BELL_BLOCK,b.offset(BorrascaShops.ANTIQUES_DOOR),.5F,1.6F);closeSociety(l,b);}
            }
        }
        if(own.getBoolean("RoweOut")){if(own.getInt("TownClock")%20==0)l.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,a.getX()+.25,a.getEyeY()-.05,a.getZ(),1,.02,.02,.02,.005);}
        else own.putInt("RoweIn",own.getInt("RoweIn")+5);
    }
    /** Back from his break, Mr. Rowe shuts the society's door again, unless somebody is standing in it. */
    private static void closeSociety(net.minecraft.server.level.ServerLevel l,BlockPos b){
        var at=b.offset(BorrascaShops.SOCIETY_DOOR);var s=l.getBlockState(at);
        if(s.getBlock() instanceof net.minecraft.world.level.block.DoorBlock door&&s.getValue(net.minecraft.world.level.block.DoorBlock.OPEN)&&l.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class,new net.minecraft.world.phys.AABB(at).expandTowards(0,1,0)).isEmpty())door.setOpen(null,l,s,at,false);
    }
    /** The back room of Rowe's Antiques, in the world relative to the base. */
    public static boolean inSociety(BlockPos rel){return rel.getX()>=-20&&rel.getX()<=-16&&rel.getZ()<=-95&&rel.getZ()>=-106&&rel.getY()>=-1&&rel.getY()<=4;}
    /** Whether a child may open the society's door now: Mr. Rowe is out front, or away, or the child is already inside. */
    public static boolean societyOpen(ServerPlayer p,BlockPos b,CompoundTag own){
        if(own.getInt("Visit")>=3||inSociety(p.blockPosition().subtract(b))||own.getBoolean("RoweOut"))return true;
        var rowe=keeper(p,own,ROWE);return rowe==null||rowe.isInvisible();
    }
    /** What Mr. Rowe says to a child trying the society's door while he is at his counter. */
    public static void notForKids(ServerPlayer p,CompoundTag own){
        var rowe=keeper(p,own,ROWE);if(rowe==null)return;int n=own.getInt("SocietyAsked");own.putInt("SocietyAsked",n+1);turn(rowe,p);
        BorrascaStory.say(p,rowe,new String[]{"That's the historical society, hon. It's for grown-ups, and it's only open Saturdays.","Not back there. Those papers are older than your grandmother.","No."}[Math.min(n,2)]);
    }
    /** A child Mr. Rowe finds in the society once he is back at his counter is sent out into the shop. */
    private static void society(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        var rowe=keeper(p,own,ROWE);
        if(own.getBoolean("SocietyShove")){
            // 0.4.429: walked back out through the shop, his hand on the child's back, as far as the front of the shop.
            boolean out=!inSociety(p.blockPosition().subtract(b))&&p.position().distanceToSqr(Vec3.atBottomCenterOf(b.offset(SENT_OUT)))<1.2;
            if(out||rowe==null||visit>=3||tired(own)||!shove(p,b,rowe,SENT_OUT,14)){own.remove("ShoveTicks");own.remove("SocietyShove");if(rowe!=null)rowe.getPersistentData().putBoolean("BorrascaAway",true);closeSociety(p.serverLevel(),b);}
            return;
        }
        if(visit>=3||!inSociety(p.blockPosition().subtract(b))){own.putInt("InSociety",0);return;}
        int in=own.getInt("InSociety")+5;own.putInt("InSociety",in);
        if(rowe==null||rowe.isInvisible()||own.getBoolean("RoweOut")||own.getInt("RoweIn")<CATCH||in<SEEN)return;
        int n=own.getInt("SocietyCaught");own.putInt("SocietyCaught",n+1);own.putBoolean("CaughtInSociety",true);own.putInt("InSociety",0);
        BorrascaStory.say(p,rowe,n==0?"Hey. Out of there. That room's not for kids.":n==1?"I told you once already. Out.":"Out.");
        BorrascaStory.openDoor(p.serverLevel(),b.offset(BorrascaShops.SOCIETY_DOOR),true);own.putBoolean("SocietyShove",true);shove(p,b,rowe,SENT_OUT,14);
    }
    /** A shove that has gone on fifteen seconds without getting them out is given up (a child pinned in a corner is not lifted out). */
    private static boolean tired(CompoundTag own){int t=own.getInt("ShoveTicks")+5;own.putInt("ShoveTicks",t);return t>300;}
    /** Whether this keeper is walking a child out just now. */
    private static boolean shoving(CompoundTag own,int phase){return phase==GUS&&own.getBoolean("BarShove")||phase==ROWE&&own.getBoolean("SocietyShove");}
    /**
     * 0.4.429 (owner: "children should be pushed out of the bar rather than teleported out"): a grown-up walks a child out with a hand on
     * their back. Every few ticks the child is given a shove toward the next block on the shortest way out (through blocks a child fits
     * through, doors open or not) and the grown-up comes on behind. Their own walking can work against it a while, but it keeps coming.
     * Returns false once the child stands where they are being put out to.
     */
    private static boolean shove(ServerPlayer p,BlockPos b,LiteraryActor by,BlockPos out,int reach){
        var l=p.serverLevel();var from=p.blockPosition();var goal=b.offset(out);if(from.equals(goal))return false;
        var next=nextStep(l,from,goal,reach);var aim=Vec3.atBottomCenterOf(next==null?goal:next);
        var dir=new Vec3(aim.x-p.getX(),0,aim.z-p.getZ());double d=dir.length();if(d<1e-3)return false;dir=dir.scale(1/d);
        p.stopRiding();p.setDeltaMovement(dir.x*SHOVE,Math.max(0,p.getDeltaMovement().y),dir.z*SHOVE);p.hurtMarked=true;
        var behind=p.position().subtract(dir.scale(1.1));var step=nextStep(l,by.blockPosition(),BlockPos.containing(behind),reach);
        BorrascaCast.walk(by,step==null?behind:Vec3.atBottomCenterOf(step),.95);by.gesture(2);turn(by,p);
        return true;
    }
    /** How hard a shove is: about a block's push each time, a little under a walk. */
    static final double SHOVE=.42;
    /** The block after from on the shortest way to goal, through blocks a body fits through, within reach of goal; null if there is none. */
    static @javax.annotation.Nullable BlockPos nextStep(net.minecraft.server.level.ServerLevel l,BlockPos from,BlockPos goal,int reach){
        var toward=new HashMap<BlockPos,BlockPos>();var queue=new ArrayDeque<BlockPos>();toward.put(goal,goal);queue.add(goal);
        while(!queue.isEmpty()){
            var c=queue.poll();
            for(var d:net.minecraft.core.Direction.Plane.HORIZONTAL){
                var n=c.relative(d);if(toward.containsKey(n)||Math.abs(n.getX()-goal.getX())>reach||Math.abs(n.getZ()-goal.getZ())>reach)continue;
                if(n.equals(from))return c;
                if(!walkable(l,n))continue;toward.put(n,c);queue.add(n);
            }
        }
        return null;
    }
    private static boolean walkable(net.minecraft.server.level.ServerLevel l,BlockPos c){return low(l,c)&&clear(l,c.above())&&!l.getBlockState(c.below()).getCollisionShape(l,c.below()).isEmpty();}
    private static boolean low(net.minecraft.server.level.ServerLevel l,BlockPos c){var s=l.getBlockState(c);return s.getBlock() instanceof net.minecraft.world.level.block.DoorBlock||s.getCollisionShape(l,c).isEmpty()||s.getCollisionShape(l,c).max(net.minecraft.core.Direction.Axis.Y)<=.5;}
    private static boolean clear(net.minecraft.server.level.ServerLevel l,BlockPos c){var s=l.getBlockState(c);return s.getBlock() instanceof net.minecraft.world.level.block.DoorBlock||s.getCollisionShape(l,c).isEmpty();}
    /** Mr. Dorsey walks his round: to each point in turn, standing a while at each door. */
    private static void round(CompoundTag own,LiteraryActor a,BlockPos b){
        if(a.getPersistentData().contains("BorrascaSpeed"))return;
        int leg=own.getInt("RoundLeg"),wait=own.getInt("RoundWait");
        if(STOPS[leg]&&wait<STOP_TICKS){own.putInt("RoundWait",wait+5);return;}
        leg=(leg+1)%ROUND.length;own.putInt("RoundLeg",leg);own.putInt("RoundWait",0);
        BorrascaCast.walk(a,Vec3.atBottomCenterOf(b.offset(ROUND[leg])),.6);
    }
    /** A keeper of this reader's, or null while the chunk is not loaded. A keeper that has gone is stood at the counter again. */
    public static @Nullable LiteraryActor keeper(ServerPlayer p,CompoundTag own,int phase){String key="Keeper"+phase;return own.hasUUID(key)&&p.serverLevel().getEntity(own.getUUID(key)) instanceof LiteraryActor a?a:null;}
    /** Whether this keeper is at work for this reader now. */
    public static boolean atWork(ServerPlayer p,CompoundTag own,int phase){var a=keeper(p,own,phase);return a!=null&&!a.isInvisible();}
    /** Marks a townsperson made this tick, so that {@link #stray} lets them in before the reader's record names them. */
    static final String FRESH="BorrascaFresh";
    /**
     * 0.4.444: whether an actor joining the world is a townsperson the reader's record no longer names: a leftover second one, loaded
     * back in after another was made in their place. They are not let back in.
     */
    public static boolean stray(LiteraryActor a,CompoundTag own){
        if(!keeper(a))return false;
        if(a.getPersistentData().getBoolean(FRESH)){a.getPersistentData().remove(FRESH);return false;}
        String key="Keeper"+a.phase();return own.hasUUID(key)&&!own.getUUID(key).equals(a.getUUID());
    }
    private static @Nullable LiteraryActor ensure(ServerPlayer p,BlockPos b,CompoundTag own,int phase){
        String key="Keeper"+phase;
        var known=keeper(p,own,phase);if(known!=null){own.putLong(key+"At",known.blockPosition().asLong());return known;}
        var l=p.serverLevel();var at=b.offset(post(phase));if(!l.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return null;
        // 0.4.444 (owner: "Two dorseys"): one who has walked out of the loaded world is still out there, and used to be made again at
        // his post. A new one is made only when everywhere about the place he was last seen is loaded and he is not in it.
        if(own.hasUUID(key)){var last=own.contains(key+"At")?BlockPos.of(own.getLong(key+"At")):at;for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(!l.areEntitiesLoaded(ChunkPos.asLong((last.getX()>>4)+dx,(last.getZ()>>4)+dz)))return null;}
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(BorrascaStory.PLACE,p.getUUID());a.appearance(LiteraryActor.STRANGER,phase);a.moveTo(Vec3.atBottomCenterOf(at),yaw(phase),0);a.setYHeadRot(yaw(phase));a.setNoAi(true);
        a.getPersistentData().putBoolean(FRESH,true);
        if(!l.addFreshEntity(a))return null;own.putUUID(key,a.getUUID());own.putLong(key+"At",a.blockPosition().asLong());return a;
    }

    // ---------------------------------------------------------------------------------------------------------------- the money
    /** The purse for this visit: new change or a new wallet when the visit changes; the one carried shows what is left. */
    private static void purse(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        if(own.getInt("PurseVisit")!=visit){own.putInt("PurseVisit",visit);own.putInt("Cents",visit>=3?WALLET:CHANGE);own.putInt("FriendCents",visit<=2?FRIEND_CHANGE:0);own.remove("Lollipop");own.remove("BarTurnedOut");own.remove("LollipopAfter");own.remove("WrenAfter");own.remove("DorseyMail");for(int k=LORNA;k<=CARRIER;k++)own.remove("SaidAfter"+k);leave(p);jar(p,b,0);}
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
        boolean after=taken(own);
        if(phase==ODELL&&!grown){lollipop(p,own);return true;}
        if(phase==GUS&&!grown){BorrascaStory.say(p,a,after?GUS_AFTER:"Out. No kids in here.");return true;}
        if(phase==ROWE&&own.getBoolean("RoweOut")){BorrascaStory.say(p,a,grown?"Five minutes. Go on back to the society if you like.":after?ROWE_OUT_AFTER:"I'm on my break, hon. Don't touch the clocks.");return true;}
        // 0.4.459: Mr. Dorsey and the Garritys' letter. On the second visit he has it and hands it over; years on he knows it again.
        if(phase==CARRIER&&visit==2&&!after){BorrascaStory.say(p,a,BorrascaGarritys.dorsey(p,own,a));return true;}
        if(phase==CARRIER&&grown){String known=BorrascaGarritys.dorseyGrown(p,own);if(known!=null){BorrascaStory.say(p,a,known);return true;}}
        var gone=after?afterTheTaking(phase):null;
        if(gone!=null&&phase==CARRIER)gone=BorrascaGarritys.dorseyAfter(own,gone);
        if(gone!=null){
            // 0.4.458: said in order, and then the last of them again: they have said what they are going to say.
            String key="SaidAfter"+phase;int n=own.getInt(key);own.putInt(key,n+1);BorrascaStory.say(p,a,gone[Math.min(n,gone.length-1)]);
        }else{
            String[] lines=own.getBoolean("SheriffDead")&&own.getInt("SheriffDiedVisit")<visit&&afterTheSheriff(phase)!=null?afterTheSheriff(phase):greetings(phase,visit);String key="Said"+phase;int n=own.getInt(key);own.putInt(key,n+1);
            BorrascaStory.say(p,a,lines[n%lines.length]);
        }
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
        if(have<price)return speak(p,a,phase==LORNA&&!grown?"That's "+BorrascaPurseItem.money(price)+", hon. You're a little short."+(taken(own)?"":" Come back with your friends."):"That's "+BorrascaPurseItem.money(price)+". You've got "+BorrascaPurseItem.money(have)+".");
        own.putInt("Cents",have-price);var held=carried(p,grown);if(held!=null)BorrascaPurseItem.mark(held,p.getUUID(),have-price);
        var goods=w.goods(grown);if(!p.getInventory().add(goods))p.drop(goods,false);
        return speak(p,a,sold(phase,w,grown,visit));
    }
    /**
     * 0.4.436: a quarter for a drugstore cabinet, from this reader's own money. A child who is short with the friend beside them has her
     * put in the rest, as at the counters. False if there is not a quarter between them.
     */
    public static boolean payQuarter(ServerPlayer p,CompoundTag own){
        int price=BorrascaArcade.PRICE,have=own.getInt("Cents");boolean grown=own.getInt("Visit")>=3;
        var friend=grown?null:BorrascaStory.friend(p,own);int pool=own.getInt("FriendCents");
        if(have<price&&friend!=null&&friend.distanceToSqr(p)<64&&have+pool>=price){int top=price-have;own.putInt("FriendCents",pool-top);have=price;own.putInt("Pooled",own.getInt("Pooled")+1);BorrascaStory.say(p,friend,"Here, I've got a quarter. I get next go.");}
        if(have<price)return false;
        own.putInt("Cents",have-price);var held=carried(p,grown);if(held!=null)BorrascaPurseItem.mark(held,p.getUUID(),have-price);
        return true;
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
        case GUS->visit==4?new String[]{"Last call's at one. What'll it be?","Sheriff's out on the ridge again. What'll it be?"}:grown?new String[]{"What'll it be?","Earl's out by the alley. I don't serve him anymore. I leave a plate out the back."}:new String[]{"What'll it be?","Earl's stool's open. He's in the Sheriff's cell again."};
        case WEBB->grown?new String[]{"Paul's driving now. Works summers out at Prescott's.","Good money at Prescott's. Everybody's kid works there sooner or later."}:new String[]{"First breakfast out since Paul came. His grandma's got him.","Don't tell Lorna, but the pie's better at the church supper."};
        case SHERRY->grown?new String[]{"You look like somebody's kid. Whose are you?","Paul says the pay at Prescott's is better than the plant ever was."}:new String[]{"Paul slept through the night. First time. So we're celebrating.","Dr. Hale delivered him. Dr. Hale delivers everybody."};
        case ROY->new String[]{"Any day now, Doc says.","Don't mind us. We're just waiting on the doctor."};
        case JOLENE->new String[]{"Mrs. Odell says it's a girl. She's never wrong about it.","We picked out her name in March. Before we even knew."};
        case DUTCH->visit==4?new String[]{"Sheriff's up on the ridge tonight. He likes it quiet when he's up there.","We don't talk shop in here. That's Gus's rule."}:new String[]{"Thirty-one years with the county. Never once had to draw.","We don't talk shop in here. That's Gus's rule.","Prescott bought the department its new cruisers. Bought the old ones too."};
        case CARRIER->visit==2?new String[]{BorrascaGarritys.DORSEY_HAS,BorrascaGarritys.DORSEY_MADDOX}
            :grown?new String[]{"Twenty-two years on this route. Same boxes, mostly.","The Garritys' box is still on its post. Nobody's taken it down."}
            :new String[]{"Nothing for you, kid. Unless you're expecting something.","Eleven Sears catalogues in this bag. Everybody wants the toy pages."};
        case MERLE->visit==4?new String[]{"Go home. Whatever you're thinking of doing tonight, go home.","You're not from the paper, are you?"}:new String[]{"Garrity girl? I took that report myself. Runaway, the Sheriff said. So that's what I wrote down.","Lot of kids run off from a town this size. You'd be surprised how many.","You're not from the paper, are you?"};
        // 0.4.426: grown, Earl says too much.
        default->visit==4?new String[]{"You're going up there tonight. I can see it on you.","Don't let them see your light past the second marker. That's all I'll say.","They quit locking me up. Nobody listens to a man who sleeps in an alley. Costs them less."}:grown?new String[]{"You again. No. You were smaller.","I wore the star once, same as Dutch and Merle. Ask them what we did with the reports.","Every kid on the board at the store, the Sheriff signs off as a runaway. Every one. They don't run anywhere.","There's a road behind the depot fence the county doesn't plow. Somebody plows it."}:new String[]{"Hey. Hey, kid. Tell the Sheriff I was asleep. That's all I was doing.","It's quiet up here. You notice that? It's always so quiet.","Go on home. Go on. It's getting dark early."};
    };}
    /**
     * 0.4.458 (owner: "Dialogue should change on second day, after Kelly/friend is taken"): whether this reader's friend has been
     * taken on the trail and it is still that day. From then until the visit ends the town speaks to a child who came down alone.
     */
    public static boolean taken(CompoundTag own){return own.getInt("Visit")==2&&own.getBoolean("FriendMissing");}
    /** What Gus says to that child as he puts them out, and what Mr. Rowe says on his break out front. */
    public static final String GUS_AFTER="Out. And go home by the street. Not by the trail.",ROWE_OUT_AFTER="I'm on my break. I'm not looking up the street. Go on home.";
    /** What Mrs. Odell says with the lollipop her own rule does not allow, with two at once, and after that. */
    public static final String ODELL_ANOTHER="One's the rule. ...Here. Take another. I won't count it.",ODELL_TWO="Here. Take two today. I won't count them.",ODELL_DONE="That's all I have for you. Go on home.";
    /**
     * What each of the town says to that child, in order; the last is said again after it. Nobody says what happened and nobody asks.
     * Each of them does what they do about it: Lorna feeds them and sends them home, Mr. Ames rubs out her account and makes room on
     * his board, Mr. Rowe did not look up the street, Kurt says what he saw and does not know what he is saying, Mr. Fenn has her
     * mother's prescription to make up, the Webbs saw nothing and want their own child, Earl was locked up before noon, and Mr.
     * Dorsey will hold the family's mail. Null for those with nothing of their own to say (Mrs. Odell and Gus answer elsewhere).
     */
    public static @Nullable String[] afterTheTaking(int phase){return switch(phase){
        case LORNA->new String[]{"Just the one of you? Sit at the counter, hon. I'll bring you a milk.","Drink that. Then you go straight home, and you stay in tonight.","Don't go asking in here, hon. Folks are eating."};
        case AMES->new String[]{"Just you, then. She had a dime of candy on the book. I'll rub that out.","I'll put her sheet on the board in the morning. The bake sale's over anyhow.","Store shuts at five. Be indoors before it does."};
        case ROWE->new String[]{"I had my smoke out front, same as every day. I didn't look up the street. I never do.","Hands in your pockets. And go home."};
        case KURT->new String[]{"Two of Prescott's men walked up past the pumps before noon. Didn't buy a thing. They never do.","You should tell the Sheriff. He's right there on the square. That's what he's for."};
        case FENN->new String[]{"No running in here. ...Sit at the fountain a minute.","Dr. Hale will write her mother something to sleep on. He always does. I'll have it made up by five.","Go on home now. The stool will be here tomorrow."};
        case WEBB->new String[]{"We've sat right here since eight. We didn't see anybody go by. Neither of us.","That's for the Sheriff. It isn't ours to talk about, and it isn't yours."};
        case SHERRY->new String[]{"Her poor mother. ...Dr. Hale will see she's looked after.","I want to go and get Paul from his grandma. Right now."};
        case EARL->new String[]{"There were two of you this morning. I can still count.","He locked me in before noon. I hadn't had a drop. Ask him why. No. Don't ask him.","Don't give him her name. He'll write it down his way, and then that's how it is."};
        case CARRIER->new String[]{"Came down on your own, did you. ...I didn't see it. I just carry the mail.","I'll hold the Garritys' mail at the office a while. That's how it's done."};
        default->null;
    };}
    /** 0.4.427: what the bar and the cell say on the visit after the Sheriff was killed on the ridge; null for everyone else. */
    private static @Nullable String[] afterTheSheriff(int phase){return switch(phase){
        case DUTCH->new String[]{"Sheriff's dead. He's still up by the tape. Nobody's gone for him.","Prescott says leave him till the county comes. So he's still lying up there."};
        case MERLE->new String[]{"Somebody's going to have to write it up. It won't be me. Not this time.","They'll send a new one from the county. They always do."};
        case GUS->new String[]{"Drinks are on the house tonight. Don't ask me why.","Nobody's said a word about the Sheriff in here. Keep it that way."};
        case EARL->new String[]{"Somebody finally did it. Don't tell me who. I'd only say it.","Nobody's told me to move along all day. First time in years."};
        default->null;
    };}
    private static void turn(LiteraryActor a,ServerPlayer p){float yaw=(float)(Mth.atan2(p.getZ()-a.getZ(),p.getX()-a.getX())*180/Math.PI)-90;a.setYRot(yaw);a.setYHeadRot(yaw);a.setYBodyRot(yaw);}

    // ---------------------------------------------------------------------------------------------------------------- the doctor's jar
    /** A child gets one lollipop from the receptionist's jar each childhood visit, from her hand or off the desk; returns whether it was given. */
    public static boolean lollipop(ServerPlayer p,CompoundTag own){
        var odell=keeper(p,own,ODELL);var b=BorrascaSites.base(p.server,p.getUUID());
        if(own.getInt("Visit")>=3||odell==null||odell.isInvisible()||b==null)return false;
        // 0.4.458: to a child who came down alone she gives one her rule does not allow, or two at once. Then she has nothing else.
        boolean after=taken(own);
        if(own.getBoolean("Lollipop")){
            if(!after||own.getBoolean("LollipopAfter")){BorrascaStory.say(p,odell,after?ODELL_DONE:"One's the rule.");return false;}
            own.putBoolean("LollipopAfter",true);var more=new ItemStack(BorrascaRegistry.LOLLIPOP.get());if(!p.getInventory().add(more))p.drop(more,false);
            BorrascaStory.say(p,odell,ODELL_ANOTHER);return true;
        }
        own.putBoolean("Lollipop",true);var s=new ItemStack(BorrascaRegistry.LOLLIPOP.get());if(!p.getInventory().add(s))p.drop(s,false);jar(p,b,1);
        if(after){
            own.putBoolean("LollipopAfter",true);var more=new ItemStack(BorrascaRegistry.LOLLIPOP.get());if(!p.getInventory().add(more))p.drop(more,false);
            BorrascaStory.say(p,odell,ODELL_TWO);return true;
        }
        BorrascaStory.say(p,odell,own.getInt("Visit")==2?"The Tates had their girl last night. Dr. Hale was up till all hours. Here, take one. Just one.":"Dr. Hale's with someone. Here, take one. Just one.");return true;
    }
    private static void jar(ServerPlayer p,BlockPos b,int stage){var at=b.offset(BorrascaShops.JAR);var s=p.serverLevel().getBlockState(at);if(BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.JAR)&&s.getValue(BorrascaShopBlock.STAGE)!=stage)p.serverLevel().setBlock(at,s.setValue(BorrascaShopBlock.STAGE,stage),3);}

    // ---------------------------------------------------------------------------------------------------------------- the bar's door
    /** Inside the Tipple's walls (world, relative to the base). */
    public static boolean inBar(BlockPos rel){return rel.getX()>=8&&rel.getX()<=21&&rel.getZ()<=-109&&rel.getZ()>=-120&&rel.getY()>=-1&&rel.getY()<=4;}
    /** Where a child turned out of the bar is put: the sidewalk outside its door. */
    public static final BlockPos TURNED_OUT=new BlockPos(5,0,-114);
    /** Gus turns a child out of the bar on the childhood visits: comes round the bar and walks them out the door (0.4.429). */
    private static void bar(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        var gus=keeper(p,own,GUS);var rel=p.blockPosition().subtract(b);
        if(own.getBoolean("BarShove")){
            if(rel.getX()<=6||gus==null||visit>=3||tired(own)||!shove(p,b,gus,TURNED_OUT,17)){own.remove("ShoveTicks");own.remove("BarShove");if(gus!=null)gus.getPersistentData().putBoolean("BorrascaAway",true);closeBar(p.serverLevel(),b);}
            return;
        }
        if(visit>=3||!inBar(rel)||gus==null||gus.isInvisible())return;
        int n=own.getInt("BarTurnedOut");own.putInt("BarTurnedOut",n+1);
        BorrascaStory.say(p,gus,n==0?(taken(own)?GUS_AFTER:"Out. No kids in here."):n==1?(taken(own)?"I said out. Go home.":"I said out. Go find your folks."):"Out.");
        BorrascaStory.openDoor(p.serverLevel(),b.offset(BorrascaShops.BAR_DOOR),true);own.putBoolean("BarShove",true);shove(p,b,gus,TURNED_OUT,17);
    }
    /** The bar's door swings shut behind a child put out, unless somebody is standing in it. */
    private static void closeBar(net.minecraft.server.level.ServerLevel l,BlockPos b){
        var at=b.offset(BorrascaShops.BAR_DOOR);if(l.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class,new net.minecraft.world.phys.AABB(at).expandTowards(0,1,0)).isEmpty())BorrascaStory.openDoor(l,at,false);
    }
}
