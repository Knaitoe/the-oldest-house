package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.BorrascaArcadeWatchPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.Comparator;
import java.util.HashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.446, Borrasca (docs/BORRASCA_LIFE_PROPOSAL.md, docs/BORRASCA_ARCADE_FRIENDS_0_4_446.md). The owner, 10 November: "Give Patrick
 * and Kendra a turn at the arcade. Patrick watches your first run of Deep Seam and asks for a rematch if you beat him. Kendra knows
 * a trick he keeps getting wrong. On the next visit, she reacts differently depending on whose initials are at the top. Patrick's
 * score would remind you of an afternoon you actually spent with him."
 *
 * <p>On the first visit, the first quarter the reader puts in DEEP SEAM brings the two of them into the drugstore behind the
 * reader's back while the screen has the reader's eyes: to the sidewalk by its door when nobody can see them go, then in through
 * the door on their feet, Patrick to the reader's shoulder and Kendra behind him. From then until they leave they are this
 * scene's, not the walk's (BorrascaStory.tagAlong leaves them be).
 *
 * <p>Every DEEP SEAM game the reader finishes gets its answer. Under Patrick's score, he says so, and Kendra says what he gets
 * wrong: he hops the fallen timbers, which cannot be hopped. Over it, he wants the cabinet. His game is a real one
 * ({@link BorrascaArcade.Hand}, HOPS): the server has played it to its end from a seed before he puts his hand on the stick, and
 * while he stands there the reader, with a hand on the cabinet, watches the same game come out of the same seed on their own screen
 * (BorrascaArcadeWatchPayload). Kendra calls each timber from the other cabinet, where she is fishing ({@code FISHES}), and the
 * reader can watch hers too. When he has lost, a word to him is the reader's chance to tell him, or not. Told, he plays once more
 * (ROUND), a game chosen to be a good one. He has two quarters.
 *
 * <p>They leave when the reader has been out of the shop a while and neither is at a cabinet, by the door, and are the walk's
 * again, or go home when nobody sees. A reader who goes far off finds their games settled without them. On the second visit Kendra
 * has one thing to say about it, by whose initials are at the top and how they came to be ({@link #later}).
 */
public final class BorrascaArcadeFriends {
    private BorrascaArcadeFriends(){}
    /** In the world, relative to the base: the sidewalk outside the drugstore's door and the floor inside it; where each watches from; where each plays. */
    public static final BlockPos OUT=new BlockPos(5,0,-98),IN=new BlockPos(8,0,-98),PAT_WATCH=new BlockPos(8,0,-94),KEN_WATCH=new BlockPos(9,0,-96),
        PAT_PLAY=new BlockPos(9,0,-94),KEN_PLAY=new BlockPos(11,0,-94),KEL_WATCH=new BlockPos(10,0,-96);
    /** Kendra's way round to BIG CAT, behind whoever is at the comics. */
    private static final BlockPos[] KEN_WAY={new BlockPos(9,0,-97),new BlockPos(11,0,-97),KEN_PLAY};
    /** ArcadeScene: not begun, going on, and over (they are on their way back to what they were doing). */
    public static final int NONE=0,ON=1,OVER=2;
    /** What each of them is doing (PatMode, KenMode). */
    public static final int COMING=0,WATCHING=1,GOING=2,PLAYING=3,LEAVING=4,LEFT=5;
    /** ArcadeTop: Patrick's score has never been beaten; the reader's is over his; his is back over the reader's. */
    public static final int HIS=0,READERS=1,HIS_AGAIN=2;
    /** ArcadeResult: nothing to answer; the reader went over Patrick's score; stayed under it; was already over it. */
    static final int BEAT=1,UNDER=2,STILL=3;
    /** Patrick's quarters; how long a step of a walk may take; how long the reader is out of the shop before they go. */
    public static final int QUARTERS=2,STEP_LIMIT=120,AWAY=100,WAIT_FOR_READER=120;
    public static final String[] GLOATS={
        "Seven thirty. P, A, T. That's me. It's been up there since June.",
        "You can hop them. The stick on this one's sticky, that's all.",
        "Closer. Not close. Closer."};
    public static final String TRICK="He hops the timbers. You can't hop a timber, you go round it. He got seven thirty the one day none came down his rail.";
    public static final String FISH="I'm not watching him do it again. I'll be at the fish.";
    public static final String WANTS="No. No way. Move over, I'm getting it back.";
    public static final String MOVE="Move!";
    public static final String MY_GO="My go.",HER_GO="I'm on this one.";
    public static final String[] TIMBERS={"That was a timber.","Timber. I can hear it from here.","Go round them, Pat."};
    public static final String GAP_HIS="That one was a gap. That one's on you.",FROZE="You looked right at it.";
    public static final String STICKY="The stick's sticky. That one doesn't count.";
    public static final String TOLD="Round them. ...I knew that. I was about to.";
    public static final String NOT_TOLD="Best of three. Saturday.";
    public static final String BACK="P. A. T. Read it.",BACK_KEN="You told him. You did this to us.";
    public static final String NOT_BACK="...It's a dumb game anyway.",NOT_BACK_KEN="It is not.";
    public static final String BROKE="That was my last quarter. You wait till Saturday.";
    public static final String SHOW_OFF="Now you're just showing off.";
    public static final String BUSY="Not now!",SHH="Shh. Fishing.";
    public static final String BIG_ONE="Big one. You let them run. Patrick reels till it snaps.";
    public static final String HOP_IT="That's my game. Hop everything. That's all there is to it.",DONT_LISTEN="Don't listen to him.";
    public static final String COMICS="That's where Patrick gets them. Thirty-one's the one I've got.",ALWAYS="It's always the stick.";
    public static final String FISHED_NOTHING="Nothing. The turtles had it.",FISHED_BETTER="Better than last time. I'll take it.",FISHED_WORSE="Worse than last time. Don't look.";
    /** What Kendra says of her game, by whose score hers went in over on the table (as it stands with hers on it). */
    public static String fished(CompoundTag own,int rank,String reader){
        var rows=BorrascaArcade.table(own,BorrascaArcade.CAT);if(rank==0)return FISHED_NOTHING;
        // Under an older score of her own, it was a worse game than her last.
        for(int i=0;i<rank-1;i++)if(rows.get(i)[0].equals("KEN"))return FISHED_WORSE;
        if(rank>=rows.size())return FISHED_BETTER;
        String under=rows.get(rank)[0];
        return under.equals("KRT")?"Over K, R, T. He'll hate that.":under.equals("KEL")?"Over Kelly. Don't tell her.":under.equals("PAT")?"Over Patrick. Nobody say anything yet.":under.equals("KEN")?FISHED_BETTER:under.equals(reader)?"Over you. Sorry.":"Over "+String.join(", ",under.split(""))+".";
    }

    private static @Nullable LiteraryActor actor(ServerPlayer p,CompoundTag own,String key){return own.hasUUID(key)&&p.serverLevel().getEntity(own.getUUID(key)) instanceof LiteraryActor a&&!a.isInvisible()?a:null;}
    private static Vec3 at(BlockPos b,BlockPos pos){return Vec3.atBottomCenterOf(b.offset(pos));}
    private static boolean near(Entity e,BlockPos b,BlockPos pos,double within){var d=e.position().subtract(at(b,pos));return d.horizontalDistance()<within&&Math.abs(d.y)<1;}
    /** On the drugstore's floor. */
    public static boolean inside(Entity e,BlockPos b){double x=e.getX()-b.getX(),z=e.getZ()-b.getZ();return x>=7.7&&x<21&&z>=-105&&z<-92&&Math.abs(e.getY()-b.getY())<2;}
    private static void face(LiteraryActor a,Vec3 to){float yaw=(float)Math.toDegrees(Math.atan2(-(to.x-a.getX()),to.z-a.getZ()));a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    /** One of them walks to a place; true when there, or when kept from it long enough. */
    private static boolean walked(CompoundTag own,String who,LiteraryActor a,BlockPos b,BlockPos to){
        int t=own.getInt(who+"Ticks")+5;
        if(near(a,b,to,.5)||t>=STEP_LIMIT){own.putInt(who+"Ticks",0);BorrascaCast.stop(a);return true;}
        own.putInt(who+"Ticks",t);a.gesture(0);BorrascaCast.walk(a,at(b,to),.8);return false;
    }
    private static void mode(CompoundTag own,String who,int mode){own.putInt(who+"Mode",mode);own.putInt(who+"Ticks",0);own.putInt(who+"Step",0);}
    public static boolean on(CompoundTag own){return own.getInt("ArcadeScene")==ON;}

    /** The reader's first quarter in DEEP SEAM on the first visit. */
    static void started(ServerPlayer p,CompoundTag own,int game){
        if(own.getInt("Visit")!=1||game!=BorrascaArcade.SEAM||own.getInt("ArcadeScene")!=NONE||BorrascaGames.on(own)||actor(p,own,"TownEli")==null||actor(p,own,"TownWren")==null)return;
        own.putInt("ArcadeScene",ON);
    }
    /** A cabinet one of them is at: the reader watches, and does not pay. */
    static boolean taken(ServerPlayer p,CompoundTag own,int game){
        if(!on(own))return false;
        String who=game==BorrascaArcade.SEAM?"Pat":"Ken";int mode=own.getInt(who+"Mode");var a=actor(p,own,game==BorrascaArcade.SEAM?"TownEli":"TownWren");
        if(mode==PLAYING){
            int elapsed=(int)Math.max(0,p.serverLevel().getGameTime()-own.getLong(who+"Start"));
            HousePackets.send(p,new BorrascaArcadeWatchPayload(game,own.getLong(who+"Seed"),own.getInt(who+"Way"),elapsed,game==BorrascaArcade.SEAM?"PAT":"KEN",BorrascaArcade.tag(own,game)));
            own.putInt("ArcadeWatched"+game,own.getInt("ArcadeWatched"+game)+1);return true;
        }
        if(mode==GOING&&a!=null){BorrascaStory.say(p,a,game==BorrascaArcade.SEAM?MY_GO:HER_GO);return true;}
        return false;
    }
    /** A DEEP SEAM game of the reader's, over: whether it went over Patrick's score. */
    public static void finished(ServerPlayer p,CompoundTag own,int game,boolean beat){
        if(!on(own)||game!=BorrascaArcade.SEAM)return;
        own.putInt("ArcadeResult",beat?BEAT:own.getInt("ArcadeTop")==READERS?STILL:UNDER);
        if(beat)own.putInt("ArcadeTop",READERS);
    }

    /**
     * The reader comes in at the gate again with the afternoon unfinished (they left the hill in the middle of it): whatever game
     * was being played is settled, and the cast is put back in its places by the arrival's own recall.
     */
    static void arrived(ServerPlayer p,CompoundTag own){
        if(own.getInt("ArcadeScene")!=ON)return;
        settle(p,own);own.putInt("ArcadeScene",OVER);own.putBoolean("ArcadeHomed",true);own.putBoolean("ArcadeDoor",false);
        for(String who:new String[]{"Pat","Ken"})mode(own,who,LEFT);
        if(own.getBoolean("KelIn"))mode(own,"Kel",LEFT);
    }
    /** Every reader tick on the first visit while the scene is on, and after it until the two are back where they belong. */
    static void tick(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LiteraryActor pat,@Nullable LiteraryActor ken){
        int scene=own.getInt("ArcadeScene");if(scene==NONE)return;
        if(own.getInt("Visit")!=1){if(scene==ON){settle(p,own);own.putInt("ArcadeScene",OVER);}own.putBoolean("ArcadeHomed",true);return;}
        if(pat==null||ken==null||pat.isInvisible()||ken.isInvisible())return;
        if(scene==OVER){homeward(p,b,own,pat,ken);return;}
        long now=p.serverLevel().getGameTime();
        int away=inside(p,b)?0:own.getInt("ArcadeAway")+5;own.putInt("ArcadeAway",away);
        // A reader gone a long way off finds it finished without them.
        if(p.distanceToSqr(at(b,IN))>48*48){settle(p,own);for(String who:new String[]{"Pat","Ken"})if(own.getInt(who+"Mode")==WATCHING)mode(own,who,LEAVING);}
        patrick(p,b,own,pat,ken,now);
        kendra(p,b,own,ken,now);
        boolean idle=idle(own,"Pat")&&idle(own,"Ken")&&own.getInt("ArcadeResult")==0;
        if(idle&&away>=AWAY&&own.getInt("PatMode")==WATCHING&&own.getInt("KenMode")==WATCHING){mode(own,"Pat",LEAVING);mode(own,"Ken",LEAVING);}
        if(own.getBoolean("KelIn")&&own.getInt("KelMode")==WATCHING&&own.getInt("PatMode")>=LEAVING&&own.getInt("KenMode")>=LEAVING)mode(own,"Kel",LEAVING);
        for(var e:new Object[][]{{"Pat",pat},{"Ken",ken}}){
            String who=(String)e[0];var a=(LiteraryActor)e[1];if(own.getInt(who+"Mode")!=LEAVING)continue;
            a.gesture(0);
            if(inside(a,b)){if(own.getInt(who+"Step")==0){if(walked(own,who,a,b,IN))own.putInt(who+"Step",1);}else BorrascaCast.walk(a,at(b,OUT),.8);}
            else if(near(a,b,OUT,.6)||walked(own,who,a,b,OUT))mode(own,who,LEFT);
        }
        door(p.serverLevel(),b,own,pat,ken,actor(p,own,"CastKimber"));
        if(own.getInt("PatMode")==LEFT&&own.getInt("KenMode")==LEFT){
            own.putInt("ArcadeScene",OVER);
            // They are on the sidewalk, still within reach of the door they came out of: it shuts behind them now.
            if(own.getBoolean("ArcadeDoor")){own.putBoolean("ArcadeDoor",false);var door=b.offset(BorrascaShops.DRUG_DOOR);
                if(p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.player.Player.class,new net.minecraft.world.phys.AABB(door).expandTowards(0,1,0).inflate(.3,0,.3)).isEmpty()){BorrascaStory.openDoor(p.serverLevel(),door,false);p.serverLevel().playSound(null,door,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.7F,1F);}}
        }
    }
    private static boolean idle(CompoundTag own,String who){int m=own.getInt(who+"Mode");return m==WATCHING&&!own.getBoolean(who+"Wants")||m==LEFT;}
    /**
     * The drugstore's door opens for either of them as they come to it, and they shut it behind them: only a door they opened, and
     * never on the reader.
     */
    private static void door(ServerLevel l,BlockPos b,CompoundTag own,LiteraryActor pat,LiteraryActor ken,@Nullable LiteraryActor kel){
        var at=b.offset(BorrascaShops.DRUG_DOOR);var s=l.getBlockState(at);if(!(s.getBlock() instanceof DoorBlock))return;
        var middle=Vec3.atBottomCenterOf(at);boolean wanted=pat.position().distanceToSqr(middle)<6.5||ken.position().distanceToSqr(middle)<6.5||kel!=null&&own.getBoolean("KelIn")&&own.getInt("KelMode")!=LEFT&&kel.position().distanceToSqr(middle)<6.5,open=s.getValue(DoorBlock.OPEN);
        if(wanted&&!open){BorrascaStory.openDoor(l,at,true);own.putBoolean("ArcadeDoor",true);l.playSound(null,at,SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.7F,1F);}
        else if(!wanted&&own.getBoolean("ArcadeDoor")){
            if(!l.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class,new net.minecraft.world.phys.AABB(at).expandTowards(0,1,0).inflate(.3,0,.3)).isEmpty())return;
            own.putBoolean("ArcadeDoor",false);
            if(open){BorrascaStory.openDoor(l,at,false);l.playSound(null,at,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.7F,1F);}
        }
    }
    /** To the sidewalk when nobody can see them go, in through the door, and to a place on the floor. True once there. */
    private static boolean arrive(ServerPlayer p,BlockPos b,CompoundTag own,String who,LiteraryActor a,BlockPos spot){
        if(own.getInt(who+"Step")>=1)return walked(own,who,a,b,spot);
        if(near(a,b,IN,.5)){own.putInt(who+"Step",1);own.putInt(who+"Ticks",0);return false;}
        // Square through the doorway to the floor inside before turning for a place on it.
        if(inside(a,b)||near(a,b,OUT,1.4)||a.getX()-b.getX()>5.2&&Math.abs(a.getZ()-b.getZ()+97.5)<1.2){a.gesture(0);BorrascaCast.walk(a,at(b,IN),.8);return false;}
        if(!BorrascaCast.recall(p,a,b.offset(OUT)))BorrascaCast.stop(a);
        return false;
    }

    private static void patrick(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor pat,LiteraryActor ken,long now){
        var cabinet=Vec3.atCenterOf(b.offset(BorrascaShops.CABINETS[BorrascaArcade.SEAM]).above());
        switch(own.getInt("PatMode")){
            case COMING->{if(arrive(p,b,own,"Pat",pat,PAT_WATCH)){mode(own,"Pat",WATCHING);own.putBoolean("ArcadeSeen",true);own.putLong("PatHere",now+40);}}
            case WATCHING->{
                if(!near(pat,b,PAT_WATCH,.6)&&!own.getBoolean("PatWants")){if(!walked(own,"Pat",pat,b,PAT_WATCH))return;}
                else BorrascaCast.stop(pat);
                pat.gesture(0);face(pat,cabinet);
                // At the reader's shoulder while their game is still going: his advice, and what Kendra thinks of it.
                long here=own.getLong("PatHere");
                if(here!=0&&now>=here){own.putLong("PatHere",0);if(BorrascaArcade.session(p.getUUID())!=null){BorrascaStory.say(p,pat,HOP_IT);own.putLong("KenHere",now+80);}}
                int result=own.getInt("ArcadeResult");
                if(result!=0&&BorrascaArcade.session(p.getUUID())==null){
                    own.putInt("ArcadeResult",0);
                    if(result==BEAT){
                        if(own.getInt("PatGames")<QUARTERS){BorrascaStory.say(p,pat,WANTS);own.putBoolean("PatWants",true);own.putBoolean("ArcadeBeat",true);}
                        else BorrascaStory.say(p,pat,BROKE);
                    }else if(result==STILL)BorrascaStory.say(p,pat,SHOW_OFF);
                    else{int n=own.getInt("PatGloats");BorrascaStory.say(p,pat,GLOATS[Math.min(n,GLOATS.length-1)]);own.putInt("PatGloats",n+1);}
                    if(!own.getBoolean("KenTurn"))own.putLong("KenSpeaks",now+70);
                }
                if(own.getBoolean("PatWants")&&BorrascaArcade.session(p.getUUID())==null){own.putBoolean("PatWants",false);mode(own,"Pat",GOING);}
            }
            case GOING->{
                // The reader is likely standing where he means to stand: he gives them a few seconds, says so, and then comes anyway.
                int wait=own.getInt("PatWait");
                if(near(p,b,PAT_PLAY,.9)&&wait<WAIT_FOR_READER){BorrascaCast.stop(pat);face(pat,cabinet);own.putInt("PatWait",wait+5);if(wait==WAIT_FOR_READER/2)BorrascaStory.say(p,pat,MOVE);return;}
                if(!walked(own,"Pat",pat,b,PAT_PLAY))return;
                own.putInt("PatWait",0);
                boolean told=own.getBoolean("ArcadeTold");int top=Integer.parseInt(BorrascaArcade.table(own,BorrascaArcade.SEAM).get(0)[1]);
                // Hopping timbers, a short game under his old score. Told, the shortest of his games that goes over the top, or his best.
                long seed=told
                    ?BorrascaArcade.seed(p.getRandom().nextLong(),BorrascaArcade.SEAM,BorrascaArcade.ROUND,g->g.score()>top&&g.ticks()<=3600,Comparator.<BorrascaArcade.Played>comparingInt(g->g.ticks()>3600?1:0).thenComparingInt(g->-g.score()))
                    :BorrascaArcade.seed(p.getRandom().nextLong(),BorrascaArcade.SEAM,BorrascaArcade.HOPS,g->g.score()<600&&g.score()>120&&g.ticks()<=1500&&g.at().length==3,Comparator.comparingInt(BorrascaArcade.Played::score));
                begin(own,"Pat",seed,told?BorrascaArcade.ROUND:BorrascaArcade.HOPS,BorrascaArcade.SEAM,now);
                own.putInt("PatGames",own.getInt("PatGames")+1);
                p.serverLevel().playSound(null,b.offset(BorrascaShops.CABINETS[BorrascaArcade.SEAM]),SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.BLOCKS,.5F,.9F);
            }
            case PLAYING->{
                BorrascaCast.stop(pat);pat.gesture(3);face(pat,cabinet);
                int elapsed=(int)(now-own.getLong("PatStart"));int[] at=own.getIntArray("PatAt"),what=own.getIntArray("PatWhat");int said=own.getInt("PatSaid");
                while(said<at.length&&at[said]<=elapsed&&elapsed<own.getInt("PatLength")+20){
                    p.serverLevel().playSound(null,b.offset(BorrascaShops.CABINETS[BorrascaArcade.SEAM]),SoundEvents.NOTE_BLOCK_BASEDRUM.value(),SoundSource.BLOCKS,.6F,.7F);
                    if(inside(ken,b))BorrascaStory.say(p,ken,own.getInt("PatWay")==BorrascaArcade.HOPS?TIMBERS[Math.min(said,TIMBERS.length-1)]:what[said]==BorrascaArcade.DeepSeam.GAP?GAP_HIS:FROZE);
                    said++;
                }
                own.putInt("PatSaid",said);
                if(elapsed<own.getInt("PatLength"))return;
                boolean back=end(own,"Pat","PAT",BorrascaArcade.SEAM)==1;
                if(own.getInt("PatWay")==BorrascaArcade.ROUND){
                    if(back)own.putInt("ArcadeTop",HIS_AGAIN);
                    BorrascaStory.say(p,pat,back?BACK:NOT_BACK);own.putLong("KenSpeaks",now+60);own.putInt("KenLine",back?1:2);
                }else{BorrascaStory.say(p,pat,STICKY);own.putBoolean("PatLost",true);}
                mode(own,"Pat",WATCHING);
            }
            default->{}
        }
    }
    private static void kendra(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor ken,long now){
        Vec3 seam=Vec3.atCenterOf(b.offset(BorrascaShops.CABINETS[BorrascaArcade.SEAM]).above()),cat=Vec3.atCenterOf(b.offset(BorrascaShops.CABINETS[BorrascaArcade.CAT]).above());
        switch(own.getInt("KenMode")){
            case COMING->{
                // After Patrick, not on his heels.
                if(own.getInt("PatMode")==COMING&&own.getInt("PatStep")==0)return;
                if(arrive(p,b,own,"Ken",ken,KEN_WATCH))mode(own,"Ken",WATCHING);
            }
            case WATCHING->{
                if(!near(ken,b,KEN_WATCH,.6)){if(!walked(own,"Ken",ken,b,KEN_WATCH))return;}else BorrascaCast.stop(ken);
                ken.gesture(0);face(ken,seam);
                long here=own.getLong("KenHere");
                if(here!=0&&now>=here){own.putLong("KenHere",0);if(BorrascaArcade.session(p.getUUID())!=null)BorrascaStory.say(p,ken,DONT_LISTEN);}
                long speaks=own.getLong("KenSpeaks");
                if(speaks!=0&&now>=speaks){
                    own.putLong("KenSpeaks",0);int line=own.getInt("KenLine");own.putInt("KenLine",0);
                    if(line==1)BorrascaStory.say(p,ken,BACK_KEN);else if(line==2)BorrascaStory.say(p,ken,NOT_BACK_KEN);
                    else if(!own.getBoolean("ArcadeTrick")&&!own.getBoolean("ArcadeBeat")){own.putBoolean("ArcadeTrick",true);BorrascaStory.say(p,ken,TRICK);own.putLong("KenSpeaks",now+110);}
                    else if(!own.getBoolean("KenTurn")){own.putBoolean("KenTurn",true);BorrascaStory.say(p,ken,FISH);mode(own,"Ken",GOING);}
                }
            }
            case GOING->{
                int step=own.getInt("KenStep");
                if(!walked(own,"Ken",ken,b,KEN_WAY[step]))return;
                if(step<KEN_WAY.length-1){own.putInt("KenStep",step+1);return;}
                // A game with a big cat in it that puts her a place or two up her own table, if she has one in her.
                long seed=BorrascaArcade.seed(p.getRandom().nextLong(),BorrascaArcade.CAT,BorrascaArcade.FISHES,g->g.score()>240&&g.score()<380&&g.at().length>0&&g.ticks()<=2000,Comparator.comparingInt(g->Math.abs(g.score()-270)));
                begin(own,"Ken",seed,BorrascaArcade.FISHES,BorrascaArcade.CAT,now);
                p.serverLevel().playSound(null,b.offset(BorrascaShops.CABINETS[BorrascaArcade.CAT]),SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.BLOCKS,.5F,1.3F);
            }
            case PLAYING->{
                BorrascaCast.stop(ken);ken.gesture(3);face(ken,cat);
                int elapsed=(int)(now-own.getLong("KenStart"));int[] at=own.getIntArray("KenAt");
                if(!own.getBoolean("KenBigOne")&&at.length>0&&at[0]<=elapsed&&elapsed<own.getInt("KenLength")){own.putBoolean("KenBigOne",true);BorrascaStory.say(p,ken,BIG_ONE);}
                long speaks=own.getLong("KenSpeaks");
                if(speaks!=0&&now>=speaks){own.putLong("KenSpeaks",0);int line=own.getInt("KenLine");own.putInt("KenLine",0);if(line==1)BorrascaStory.say(p,ken,BACK_KEN);else if(line==2)BorrascaStory.say(p,ken,NOT_BACK_KEN);}
                if(elapsed<own.getInt("KenLength"))return;
                int rank=end(own,"Ken","KEN",BorrascaArcade.CAT);
                BorrascaStory.say(p,ken,fished(own,rank,BorrascaArcade.initials(p)));own.putBoolean("KenPlayed",true);
                mode(own,"Ken",WATCHING);
            }
            default->{}
        }
    }
    /**
     * Kelly, on her own walk up the street to the Tree (BorrascaStory.pact asks before it walks her on). While the scene is on and
     * she is near enough, she comes in after the other two and looks through the comics on the spinner; she leaves when they do,
     * and goes on up the street from the door. True while she is the scene's.
     */
    static boolean kelly(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor kel){
        if(own.getInt("ArcadeScene")==NONE||own.getInt("Visit")!=1)return false;
        if(!own.getBoolean("KelIn")){
            if(!on(own)||!own.getBoolean("PlayDone")||own.getBoolean("KimberAtFort")||kel.position().distanceToSqr(at(b,IN))>60*60)return false;
            own.putBoolean("KelIn",true);mode(own,"Kel",COMING);
        }
        int mode=own.getInt("KelMode");if(mode==LEFT)return false;
        if(!on(own)&&mode!=LEAVING){
            // The others have gone before she ever got to the door: she was never part of it.
            if(mode==COMING&&own.getInt("KelStep")==0&&!inside(kel,b)){own.putBoolean("KelIn",false);return false;}
            mode(own,"Kel",LEAVING);mode=LEAVING;
        }
        switch(mode){
            case COMING->{
                if(own.getInt("KenMode")==COMING&&own.getInt("KenStep")==0){BorrascaCast.stop(kel);return true;}
                if(arrive(p,b,own,"Kel",kel,KEL_WATCH)){mode(own,"Kel",WATCHING);own.putBoolean("KelSeen",true);own.putLong("KelSpeaks",p.serverLevel().getGameTime()+40);}
            }
            case WATCHING->{
                if(!near(kel,b,KEL_WATCH,.6)){if(!walked(own,"Kel",kel,b,KEL_WATCH))return true;}else BorrascaCast.stop(kel);
                kel.gesture(0);face(kel,Vec3.atCenterOf(b.offset(BorrascaShops.RACK)));
                long now=p.serverLevel().getGameTime(),speaks=own.getLong("KelSpeaks");
                if(speaks!=0&&now>=speaks&&!BorrascaStory.recentSpeech(p)){own.putLong("KelSpeaks",0);BorrascaStory.say(p,kel,COMICS);}
                if(own.getBoolean("PatLost")&&!own.getBoolean("KelAlways")&&speaks==0){
                    int t=own.getInt("KelWait")+5;own.putInt("KelWait",t);
                    if(t>=50){own.putBoolean("KelAlways",true);BorrascaStory.say(p,kel,ALWAYS);}
                }
            }
            case LEAVING->{
                kel.gesture(0);
                if(inside(kel,b)){if(own.getInt("KelStep")==0){if(walked(own,"Kel",kel,b,IN))own.putInt("KelStep",1);}else BorrascaCast.walk(kel,at(b,OUT),.8);}
                else if(near(kel,b,OUT,.6)||walked(own,"Kel",kel,b,OUT)){
                    mode(own,"Kel",LEFT);
                    // On up the street from here, not back to where the walk had got to.
                    own.putInt("ChildStep",Math.max(own.getInt("ChildStep"),2));
                    return false;
                }
            }
            default->{}
        }
        return true;
    }

    /** One of them puts a quarter in: the game is played to its end here and now, and then takes as long to happen as it took. */
    private static void begin(CompoundTag own,String who,long seed,int way,int game,long now){
        var played=BorrascaArcade.play(game,seed,way);
        own.putLong(who+"Seed",seed);own.putInt(who+"Way",way);own.putLong(who+"Start",now);own.putInt(who+"Length",played.ticks());own.putInt(who+"Score",played.score());
        own.putIntArray(who+"At",played.at());own.putIntArray(who+"What",played.what());own.putInt(who+"Said",0);
        mode(own,who,PLAYING);
    }
    /** Their game is over: its score goes on the table. The place it took, from 1, or 0. */
    private static int end(CompoundTag own,String who,String initials,int game){
        int rank=BorrascaArcade.record(own,game,initials,own.getInt(who+"Score"));own.putInt(who+"Rank",rank);own.putInt(who+"Best",Math.max(own.getInt(who+"Best"),own.getInt(who+"Score")));return rank;
    }
    /** Whatever game either of them is in the middle of is finished without the reader, and nothing more is said. */
    private static void settle(ServerPlayer p,CompoundTag own){
        own.putInt("ArcadeResult",0);own.putBoolean("PatWants",false);own.putLong("KenSpeaks",0);
        if(own.getInt("PatMode")==PLAYING){boolean back=end(own,"Pat","PAT",BorrascaArcade.SEAM)==1;if(own.getInt("PatWay")==BorrascaArcade.ROUND&&back)own.putInt("ArcadeTop",HIS_AGAIN);else if(own.getInt("PatWay")==BorrascaArcade.HOPS)own.putBoolean("PatLost",true);mode(own,"Pat",WATCHING);}
        if(own.getInt("KenMode")==PLAYING){end(own,"Ken","KEN",BorrascaArcade.CAT);own.putBoolean("KenPlayed",true);mode(own,"Ken",WATCHING);}
        if(own.getInt("PatMode")==GOING)mode(own,"Pat",WATCHING);
        if(own.getInt("KenMode")==GOING)mode(own,"Ken",WATCHING);
    }
    /**
     * After the scene: on the walk up with Kelly they go on from the drugstore's door, not back to where the walk began; otherwise
     * they are home at their places in town as soon as nobody sees them go.
     */
    private static void homeward(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor pat,LiteraryActor ken){
        if(own.getBoolean("ArcadeHomed"))return;
        // The walk up has begun, or is about to (as BorrascaStory.tagAlong decides it): Kelly is past the last house.
        if(own.getBoolean("AlongStarted")||own.getBoolean("PlayDone")&&(own.getInt("ChildStep")>=1||own.getBoolean("KimberAtFort"))){
            if(!own.getBoolean("AlongDone")&&!own.getBoolean("AlongHome"))for(String step:new String[]{"EliAlong","WrenAlong"})own.putInt(step,Math.max(own.getInt(step),2));
            own.putBoolean("ArcadeHomed",true);return;
        }
        boolean both=true;
        for(var e:new Object[][]{{"TownEli",pat},{"TownWren",ken}}){var a=(LiteraryActor)e[1];BorrascaCast.stop(a);both&=BorrascaCast.recall(p,a,b.offset(BorrascaCast.target((String)e[0],1)));}
        if(both)own.putBoolean("ArcadeHomed",true);
    }

    /** A word to one of them while the scene is on. True when it was the scene's to answer. */
    static boolean talk(ServerPlayer p,LiteraryActor child,CompoundTag own){
        if(!on(own))return false;
        boolean patrick=child.phase()==BorrascaStory.ELI;String who=patrick?"Pat":"Ken";int mode=own.getInt(who+"Mode");
        if(mode==PLAYING){BorrascaStory.say(p,child,patrick?BUSY:SHH);return true;}
        if(mode!=WATCHING)return false;
        if(!patrick){
            if(own.getBoolean("ArcadeTrick"))return false;
            own.putBoolean("ArcadeTrick",true);BorrascaStory.say(p,child,TRICK);return true;
        }
        if(!own.getBoolean("PatLost")||own.getBoolean("ArcadeTold")||own.getBoolean("ArcadeKept"))return false;
        var icons=new HashMap<Integer,ItemStack>();icons.put(0,LiteraryChoiceMenu.icon(Items.OAK_LOG,"Tell him to go round the timbers"));icons.put(8,LiteraryChoiceMenu.icon(Items.PAPER,"Say nothing"));
        LiteraryChoiceMenu.open(p,"Patrick glares at the cabinet",icons,()->BorrascaSites.inside(p)&&child.isAlive()&&p.distanceToSqr(child)<100,slot->{
            if(slot!=0&&slot!=8)return false;
            var mine=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.BORRASCA);
            if(slot==0){
                mine.putBoolean("ArcadeTold",true);BorrascaStory.cue(p,"You tell him the timbers can't be hopped.");BorrascaStory.say(p,child,TOLD);
                if(mine.getInt("PatGames")<QUARTERS&&mine.getInt("PatMode")==WATCHING)mine.putBoolean("PatWants",true);
            }else{mine.putBoolean("ArcadeKept",true);BorrascaStory.say(p,child,NOT_TOLD);}
            BorrascaStory.save(p,mine);return true;
        });
        return true;
    }

    /** Kendra, on the second visit, once: by whose initials are at the top of DEEP SEAM and how they came to be. Null when there is nothing. */
    public static @Nullable String later(CompoundTag own,int visit){
        if(visit!=2||!own.getBoolean("ArcadeSeen")||own.getBoolean("ArcadeLater"))return null;
        own.putBoolean("ArcadeLater",true);
        boolean told=own.getBoolean("ArcadeTold");
        return switch(own.getInt("ArcadeTop")){
            case READERS->told?"You told him how and you still got back over him. He said the stick was sticky.":"Your letters are still over his on the Deep Seam. He was going back Saturday with his paper money.";
            case HIS_AGAIN->"He got the top back the same afternoon. He told everybody he worked the timbers out by himself.";
            default->"Nobody ever got over him on the Deep Seam. He'd want me to say so.";
        };
    }
    /** The account: the afternoon at the cabinets. */
    static String account(CompoundTag own){
        if(!own.getBoolean("ArcadeSeen"))return "";
        var out=new StringBuilder(" Patrick and Kendra came in to watch.");
        if(own.getInt("PatGames")>0)out.append(" When my initials went over his, Patrick wanted the cabinet back. He hopped every timber, and Kendra told him so each time.");
        else if(own.getBoolean("ArcadeTrick"))out.append(" Kendra said he hops the timbers, and that they can't be hopped.");
        if(own.getBoolean("ArcadeTold"))out.append(" I told him to go round them.").append(own.getInt("ArcadeTop")==HIS_AGAIN?" He had the top back before we left.":"");
        if(own.getBoolean("KenPlayed"))out.append(" Kendra fished.");
        if(own.getBoolean("KelSeen"))out.append(" Kelly went through the comics.");
        return out.toString();
    }
}
