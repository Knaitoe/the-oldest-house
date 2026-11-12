package io.github.knaitoe.theoldesthouse.labyrinth;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.515, Borrasca (docs/BORRASCA_LIFE_PROPOSAL.md part 5, docs/BORRASCA_GAMES_0_4_515.md). The owner, 10 November: "I'd also add
 * more active play - hide and seek, tag". The proposal: "Tag and hide and seek with all four, in the trees behind the store, started
 * by any of them and dropped whenever the reader walks off."
 *
 * <p>Kelly's own game on the street (BorrascaPlay) is as it was. When the reader has found her there, Patrick says it does not
 * count, with one tree on the whole street, and the three of them run for the trees behind the general store. A reader who comes
 * after them plays three games. Kelly counts at the big oak and everybody hides, the reader too: she walks the trees and finds whom
 * she can see. Then the reader counts, and finds the three. Then Kendra calls tag, the reader is it, and the three run.
 *
 * <p>Nothing waits on any of it. A reader who goes on up the street, never comes, or walks off in the middle is not called back: the
 * three come out by the alley and the walk to the Tree begins as it always has. Nobody finds a way round anything: every step any of
 * them takes is along one of LINES, each a straight clear line over level ground, to its end and then along the next.
 */
public final class BorrascaGames {
    private BorrascaGames(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** Games: not yet; called, and the three on their way; Kelly counts; the reader counts; tag; the three going back out; over. */
    public static final int NONE=0,CALLED=1,KELLY_SEEKS=2,READER_SEEKS=3,TAG=4,LEAVING=5,OVER=6;

    // ---- The ground -------------------------------------------------------------------------------------------------------

    /** The big oak that was always there (BorrascaTown.edge), and the two planted for the games; the cord of firewood. */
    public static final BlockPos HOME=new BlockPos(-36,0,-22),NORTH_OAK=new BlockPos(-35,0,-29),EAST_OAK=new BlockPos(-32,0,-22);
    public static final BlockPos[] WOODPILE={new BlockPos(-30,0,-27),new BlockPos(-30,0,-26)};
    static final int OAK_HIGH=8,PILE_HIGH=2;

    /** Every place any of them stands or turns. */
    public static final int MOUTH=0,BACK=1,NORTH=2,MIDDLE=3,EAST=4,WEST=5,SOUTH_EAST=6,SOUTH_WEST=7,
        BEHIND_NORTH_OAK=8,BY_EAST_OAK=9,BEHIND_EAST_OAK=10,BEHIND_LOW_TREE=11,ROUND_THE_CORNER=12,BEHIND_WOODPILE=13,BY_FAR_TRUNK=14,BEHIND_FAR_TRUNK=15,
        COUNT=16,KELLY_WAITS=17,PATRICK_WAITS=18,KENDRA_WAITS=19,PATRICK_STREET=20,KELLY_STREET=21,STREET=22,KELLY_EAST=23,KELLY_WEST=24;
    public static final BlockPos[] POINTS={
        new BlockPos(-2,0,-32),new BlockPos(-29,0,-32),new BlockPos(-33,0,-32),new BlockPos(-33,0,-24),new BlockPos(-29,0,-24),new BlockPos(-35,0,-24),
        new BlockPos(-29,0,-10),new BlockPos(-35,0,-10),
        new BlockPos(-35,0,-30),new BlockPos(-31,0,-24),new BlockPos(-31,0,-22),new BlockPos(-33,0,-10),new BlockPos(-27,0,-31),new BlockPos(-29,0,-27),
        new BlockPos(-33,0,-39),new BlockPos(-34,0,-39),
        new BlockPos(-35,0,-22),new BlockPos(-34,0,-23),new BlockPos(-32,0,-25),new BlockPos(-34,0,-25),
        new BlockPos(-2,0,-24),new BlockPos(0,0,-32),new BlockPos(0,0,-35),new BlockPos(BorrascaPlay.EAST,0,-35),new BlockPos(BorrascaPlay.WEST,0,-35)};
    /** The straight clear lines between them. Nobody leaves these. */
    public static final int[][] LINES={
        {MOUTH,BACK},{BACK,NORTH},{NORTH,MIDDLE},{MIDDLE,BY_EAST_OAK},{BY_EAST_OAK,EAST},{MIDDLE,WEST},{EAST,SOUTH_EAST},{SOUTH_EAST,BEHIND_LOW_TREE},
        {BEHIND_LOW_TREE,SOUTH_WEST},{SOUTH_WEST,COUNT},{COUNT,WEST},{NORTH,BEHIND_NORTH_OAK},{BY_EAST_OAK,BEHIND_EAST_OAK},{BACK,ROUND_THE_CORNER},
        {EAST,BEHIND_WOODPILE},{BEHIND_WOODPILE,BACK},{NORTH,BY_FAR_TRUNK},{BY_FAR_TRUNK,BEHIND_FAR_TRUNK},{MIDDLE,KELLY_WAITS},{MIDDLE,PATRICK_WAITS},
        {MIDDLE,KENDRA_WAITS},{COUNT,KELLY_WAITS},{MOUTH,PATRICK_STREET},{MOUTH,KELLY_STREET},{KELLY_STREET,STREET},{STREET,KELLY_EAST},{STREET,KELLY_WEST}};
    /** Where tag is run: two rings, one round the woodpile and one round the low tree. */
    private static final int[] RING={NORTH,BACK,BEHIND_WOODPILE,EAST,BY_EAST_OAK,MIDDLE,WEST,COUNT,SOUTH_WEST,BEHIND_LOW_TREE,SOUTH_EAST};
    /** Who hides where: each has two places and takes the other one the second time. */
    public static final int[][] PLACES={{BEHIND_LOW_TREE,BEHIND_NORTH_OAK},{BEHIND_WOODPILE,ROUND_THE_CORNER},{BEHIND_EAST_OAK,BEHIND_FAR_TRUNK}};
    /** Where Kelly looks, in order. From each of these she can see one of the places. */
    private static final int[] ROUNDS={BY_EAST_OAK,EAST,SOUTH_EAST,BACK,BY_FAR_TRUNK,NORTH,MIDDLE};
    private static final int[] WAITS={KELLY_WAITS,PATRICK_WAITS,KENDRA_WAITS},STREETS={KELLY_STREET,PATRICK_STREET,MOUTH},STARTS={EAST,NORTH,WEST};

    public static final int KEL=0,PAT=1,KEN=2;
    private static final String[] WHO={"GamesKel","GamesPat","GamesKen"};
    private static final double[][] FAR=new double[POINTS.length][POINTS.length];
    private static final int[][] NEXT=new int[POINTS.length][POINTS.length];
    static{
        int n=POINTS.length;
        for(int i=0;i<n;i++)for(int j=0;j<n;j++){FAR[i][j]=i==j?0:1e9;NEXT[i][j]=j;}
        for(var e:LINES){double d=Math.sqrt(POINTS[e[0]].distSqr(POINTS[e[1]]));FAR[e[0]][e[1]]=d;FAR[e[1]][e[0]]=d;}
        for(int k=0;k<n;k++)for(int i=0;i<n;i++)for(int j=0;j<n;j++)if(FAR[i][k]+FAR[k][j]<FAR[i][j]-1e-9){FAR[i][j]=FAR[i][k]+FAR[k][j];NEXT[i][j]=NEXT[i][k];}
    }

    /** How fast they go: running to hide and running from the reader; Kelly looking; going back out. */
    public static final double RUN=1.05,LOOK=.8,OUT=.9,TIRED=.94,SLOWEST=.72;
    /** In reader ticks: how long they wait for a reader who has not come; how long a reader may be gone; the rounds' own limits. */
    public static final int CALL_WAIT=800,AWAY=80,OAK_WAIT=600,HIDE_TIME=260,SEEK_TIME=2400,HINT_AFTER=400,TAG_TIME=2000,TAUNT_EVERY=160;
    /** Kelly's count, in reader ticks from when she turns her face to the oak, and when she turns back. */
    private static final int[] COUNT_AT={5,65,105,145};
    private static final String[] COUNTING={"I'm it. Twenty, and I count fast. Go!","Five. Six. Seven.","Twelve. Thirteen. Fourteen.","Eighteen. Nineteen. Twenty!"};
    public static final int COUNTED=185;
    /** What they call at a reader who is not near them, in turn: who, and what. */
    private static final Object[][] TAUNTS={{PAT,"You can't catch all three."},{KEN,"Can't catch me!"},{KEL,"Over here!"},{PAT,"Too slow!"}};
    /** In blocks: how near Kelly sees one of the others, and the reader; how near the reader finds; a tag; when a runner bolts. */
    public static final double SEEN=4.5,SEEN_READER=9,FOUND=2.3,TOUCH=1.7,BOLT=5,KEEP_ON=7.5;

    /** Two oaks and a cord of firewood, in the trees behind the store. New builds only. Leaves go only where there is air. */
    static void plant(ServerLevel l,BlockPos b){
        for(var tree:new BlockPos[]{NORTH_OAK,EAST_OAK}){
            int x=tree.getX(),y=tree.getY(),z=tree.getZ();
            LiteraryRooms.box(l,b,x,y,z,x,y+OAK_HIGH-1,z,Blocks.OAK_LOG);
            double cy=y+OAK_HIGH-.5,r=2.6;
            for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)for(int dy=-2;dy<=2;dy++){
                double d=dx*dx/(r*r)+dz*dz/(r*r)+dy*dy/4.0;if(d>1.1||dx==0&&dz==0&&dy<1||Math.floorMod(x+dx*31+dz*17+dy*7,7)==0)continue;
                var at=b.offset(x+dx,(int)Math.floor(cy)+dy,z+dz);
                if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true),F);
            }
        }
        // Split oak stacked along the back of the store, its cut ends to the trees.
        for(var cell:WOODPILE)for(int y=0;y<PILE_HIGH;y++)
            BuildBlocks.set(l,b.offset(cell.getX(),cell.getY()+y,cell.getZ()),Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X),F);
    }

    // ---- Who and where ----------------------------------------------------------------------------------------------------

    public static boolean on(CompoundTag own){int s=own.getInt("Games");return s>=CALLED&&s<=LEAVING;}
    private static @Nullable LiteraryActor actor(ServerPlayer p,CompoundTag own,String key){return own.hasUUID(key)&&p.serverLevel().getEntity(own.getUUID(key)) instanceof LiteraryActor a&&!a.isInvisible()?a:null;}
    public static Vec3 at(BlockPos b,int point){return Vec3.atBottomCenterOf(BorrascaRooms.at(b,POINTS[point]));}
    private static boolean near(Entity e,BlockPos b,int point,double within){var d=e.position().subtract(at(b,point));return d.horizontalDistance()<within&&Math.abs(d.y)<1.5;}
    private static void face(LiteraryActor a,Vec3 to){if(to.subtract(a.position()).horizontalDistanceSqr()<.01)return;float yaw=(float)Math.toDegrees(Math.atan2(-(to.x-a.getX()),to.z-a.getZ()));a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    /** In the trees behind the store, or behind the houses north of it. */
    public static boolean grove(Vec3 r){return r.x< -28.2&&r.x> -38.5&&r.z> -43&&r.z< -5.5&&Math.abs(r.y)<3;}
    /** In the alley between the store and the fence. */
    public static boolean alley(Vec3 r){return r.x>= -28.2&&r.x< -4.5&&r.z> -33.2&&r.z< -29.8&&Math.abs(r.y)<3;}
    private static boolean upTheStreet(Vec3 r){return r.z< -40&&r.x> -24;}
    private static void state(CompoundTag own,int state){own.putInt("Games",state);beat(own,0);}
    private static void beat(CompoundTag own,int beat){own.putInt("GamesBeat",beat);own.putInt("GamesTicks",0);own.putInt("GamesSaid",0);}

    /** Stood at a point, going nowhere. */
    private static void put(CompoundTag own,int who,int point){String k=WHO[who];own.putInt(k+"From",point);own.putInt(k+"To",point);own.putInt(k+"Goal",point);own.putInt(k+"Leg",0);}
    private static void send(CompoundTag own,int who,int goal){own.putInt(WHO[who]+"Goal",goal);}
    private static int goal(CompoundTag own,int who){return own.getInt(WHO[who]+"Goal");}
    /**
     * One reader tick of somebody's way to where they were sent: to the end of the line they are on (or back along it, when that is
     * the shorter way), then line by line. True once they stand there. A step that takes far longer than it should is finished for
     * them, when nobody is looking, or after a while whoever is.
     */
    private static boolean walk(ServerPlayer p,CompoundTag own,int who,LiteraryActor a,BlockPos b,double speed){
        String k=WHO[who];int from=own.getInt(k+"From"),to=own.getInt(k+"To"),goal=own.getInt(k+"Goal");
        var end=at(b,to);var d=a.position().subtract(end);
        if(d.horizontalDistance()>=.45||Math.abs(d.y)>=1.2){
            if(from!=to&&a.position().subtract(at(b,from)).horizontalDistance()+FAR[from][goal]+.5<d.horizontalDistance()+FAR[to][goal]){
                own.putInt(k+"From",to);own.putInt(k+"To",from);own.putInt(k+"Leg",0);a.gesture(0);BorrascaCast.walk(a,at(b,from),speed);return false;
            }
            int leg=own.getInt(k+"Leg")+5;own.putInt(k+"Leg",leg);int limit=60+(int)(FAR[from][to]*10);
            if(leg<limit||leg<limit+100&&LiteraryVignettes.watched(p.serverLevel(),a.getEyePosition())){a.gesture(0);BorrascaCast.walk(a,end,speed);return false;}
            BorrascaCast.stop(a);a.moveTo(end.x,end.y,end.z,a.getYRot(),0);
        }
        own.putInt(k+"From",to);own.putInt(k+"Leg",0);
        if(to==goal){BorrascaCast.stop(a);return true;}
        own.putInt(k+"To",NEXT[to][goal]);a.gesture(0);BorrascaCast.walk(a,at(b,NEXT[to][goal]),speed);return false;
    }
    /** The next of a beat's lines, once the one before has been read. True when all of them have been said. */
    private static boolean lines(ServerPlayer p,CompoundTag own,LiteraryActor[] cast,Object... script){
        int said=own.getInt("GamesSaid");if(said*2>=script.length)return true;
        if(BorrascaStory.recentSpeech(p))return false;
        BorrascaStory.say(p,cast[(Integer)script[said*2]],(String)script[said*2+1]);own.putInt("GamesSaid",said+1);
        return (said+1)*2>=script.length;
    }

    // ---- The games --------------------------------------------------------------------------------------------------------

    /** Every reader tick of the first visit before Kelly's walk to the Tree. True while the games have her. */
    static boolean tick(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor kel){
        int state=own.getInt("Games");if(state==OVER)return false;
        var pat=actor(p,own,"TownEli");var ken=actor(p,own,"TownWren");var r=BorrascaRooms.rel(p,b);
        if(state==NONE){
            int hid=own.getInt("HideSide")==BorrascaPlay.EAST?KELLY_EAST:KELLY_WEST;
            // Called only for a reader who played her game through and found her, with all three where the street has them.
            if(!own.getBoolean("FoundNell")||own.getInt("ChildStep")>0||own.getBoolean("KimberAtFort")||pat==null||ken==null||BorrascaArcadeFriends.on(own)
                    ||upTheStreet(r)||!near(pat,b,PATRICK_STREET,2.5)||!near(ken,b,MOUTH,2.5)||!near(kel,b,hid,2.5)){own.putInt("Games",OVER);return false;}
            state(own,CALLED);state=CALLED;put(own,KEL,hid);put(own,PAT,PATRICK_STREET);put(own,KEN,MOUTH);
            own.putInt("GamesSeed",p.getRandom().nextInt(8));own.putInt("GamesAway",0);
        }
        if(pat==null||ken==null){own.putInt("Games",OVER);return false;}
        var cast=new LiteraryActor[]{kel,pat,ken};
        int ticks=own.getInt("GamesTicks")+5;own.putInt("GamesTicks",ticks);
        boolean here=grove(r)||alley(r);if(grove(r))own.putBoolean("GamesCame",true);
        if(state!=LEAVING&&own.getBoolean("GamesCame")){
            // Dropped whenever the reader walks off.
            int away=here?0:own.getInt("GamesAway")+5;own.putInt("GamesAway",away);
            if(away>=AWAY){drop(p,own,kel);return true;}
        }
        switch(state){
            case CALLED->called(p,b,own,cast,r,ticks,here);
            case KELLY_SEEKS->kellySeeks(p,b,own,cast,ticks);
            case READER_SEEKS->readerSeeks(p,b,own,cast,r,ticks);
            case TAG->tag(p,b,own,cast,ticks);
            default->{if(leaving(p,b,own,cast,ticks)){own.putInt("Games",OVER);return false;}}
        }
        return true;
    }

    private static void called(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor[] cast,Vec3 r,int ticks,boolean here){
        lines(p,own,cast,PAT,"That doesn't count. There's one tree on this whole street.",KEN,"Behind the store! Kelly got found, so Kelly's it.",KEL,"One game. Then the Tree.");
        int said=own.getInt("GamesSaid");boolean all=said>=3;
        // Kendra is off as she says it, Patrick after her, and Kelly once she has said yes.
        if(said>=2){send(own,KEN,KENDRA_WAITS);send(own,PAT,PATRICK_WAITS);all&=walk(p,own,KEN,cast[KEN],b,RUN);all&=walk(p,own,PAT,cast[PAT],b,RUN);}
        if(said>=3){send(own,KEL,COUNT);all&=walk(p,own,KEL,cast[KEL],b,RUN);}
        if(upTheStreet(r)||ticks>CALL_WAIT&&!here){drop(p,own,cast[KEL]);return;}
        if(!here&&said>=3&&!own.getBoolean("GamesCalledAgain")&&ticks>240&&!BorrascaStory.recentSpeech(p)&&p.distanceToSqr(cast[PAT])<40*40){
            own.putBoolean("GamesCalledAgain",true);BorrascaStory.say(p,cast[PAT],"Come on! It's just down the alley.");
        }
        // The reader has come, and all three are there (a step of theirs that stalls is finished for them, so they always are).
        if(grove(r)&&all)state(own,KELLY_SEEKS);
    }

    /** Where one of them hides: round 0 is Kelly's count, round 1 the reader's; each takes their other place the second time. */
    public static int place(CompoundTag own,int who,int round){return PLACES[who][(own.getInt("GamesSeed")>>who&1)^round];}

    private static void kellySeeks(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor[] cast,int ticks){
        var kel=cast[KEL];int beat=own.getInt("GamesBeat");
        if(beat==0){
            // She counts with her face to the big oak. Patrick and Kendra run; the reader has as long as they have.
            if(ticks==5){for(int who=PAT;who<=KEN;who++){send(own,who,place(own,who,0));own.putBoolean(WHO[who]+"Hid",true);}own.putBoolean("GamesCaught",false);}
            face(kel,Vec3.atBottomCenterOf(BorrascaRooms.at(b,HOME)));
            for(int who=PAT;who<=KEN;who++)walk(p,own,who,cast[who],b,RUN);
            // A count keeps its own time: each of its lines takes the last one's place (how long the last stays up is Dialogue's).
            for(int i=0;i<COUNT_AT.length;i++)if(ticks==COUNT_AT[i])BorrascaStory.say(p,kel,COUNTING[i]);
            if(ticks>=COUNTED){beat(own,1);own.putInt("GamesStop",0);BorrascaStory.say(p,kel,"Ready or not!");}
            return;
        }
        if(beat==1){
            for(int who=PAT;who<=KEN;who++){
                var kid=cast[who];boolean hid=own.getBoolean(WHO[who]+"Hid");
                if(hid&&kel.distanceToSqr(kid)<SEEN*SEEN&&kel.hasLineOfSight(kid)){
                    own.putBoolean(WHO[who]+"Hid",false);send(own,who,WAITS[who]);
                    BorrascaStory.say(p,kel,switch(place(own,who,0)){
                        case BEHIND_WOODPILE->"Patrick. The woodpile. You always pick the woodpile.";
                        case ROUND_THE_CORNER->"Patrick! I can see your elbow.";
                        case BEHIND_EAST_OAK->"Kendra's behind the skinny oak!";
                        default->"Found you, Kendra. That's out of bounds and you know it.";});
                }
                walk(p,own,who,kid,b,hid?RUN:OUT);
            }
            if(!own.getBoolean("GamesCaught")&&sees(kel,p)){
                own.putBoolean("GamesCaught",true);
                BorrascaStory.say(p,kel,ticks<=60?"You have to hide. That's the whole game.":"There you are. Found you!");
            }
            int stop=own.getInt("GamesStop");
            if(stop>=ROUNDS.length){beat(own,2);return;}
            send(own,KEL,ROUNDS[stop]);if(walk(p,own,KEL,kel,b,LOOK))own.putInt("GamesStop",stop+1);
            return;
        }
        // All in: who is it next.
        boolean back=true;for(int who=KEL;who<=KEN;who++){send(own,who,WAITS[who]);back&=walk(p,own,who,cast[who],b,OUT);if(near(cast[who],b,WAITS[who],.6))face(cast[who],p.position());}
        boolean said=own.getBoolean("GamesCaught")
            ?lines(p,own,cast,KEN,"New kid's it! Count at the big oak. No looking.")
            :lines(p,own,cast,KEL,"I give up! Come out! Where were you?",PAT,"Nobody beats Kelly. Nobody.",KEN,"Then the new kid's it. Count at the big oak. No looking.");
        if(said){own.putBoolean("GrovePlayed",true);if(!own.getBoolean("GamesCaught"))own.putBoolean("GroveHid",true);if(back||ticks>200)state(own,READER_SEEKS);}
    }
    /** Whether she can see the reader: near and in the open in front of her, or near enough to touch. */
    private static boolean sees(LiteraryActor kel,ServerPlayer p){
        double d=kel.distanceTo(p);if(d<2)return true;if(d>SEEN_READER||!kel.hasLineOfSight(p))return false;
        var to=p.position().subtract(kel.position()).multiply(1,0,1).normalize();return Vec3.directionFromRotation(0,kel.getYRot()).dot(to)>.1;
    }

    private static void readerSeeks(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor[] cast,Vec3 r,int ticks){
        int beat=own.getInt("GamesBeat");
        if(beat==0){
            boolean back=true;for(int who=KEL;who<=KEN;who++){send(own,who,WAITS[who]);back&=walk(p,own,who,cast[who],b,OUT);if(near(cast[who],b,WAITS[who],.6))face(cast[who],p.position());}
            var oak=Vec3.atBottomCenterOf(HOME);boolean atOak=r.subtract(oak).horizontalDistance()<2.7;
            if(!atOak&&ticks>240&&!own.getBoolean("GamesOakSaid")&&!BorrascaStory.recentSpeech(p)){own.putBoolean("GamesOakSaid",true);BorrascaStory.say(p,cast[KEN],"The big oak. Go on.");}
            if(back&&atOak||ticks>OAK_WAIT){
                beat(own,1);for(int who=KEL;who<=KEN;who++){send(own,who,place(own,who,1));own.putBoolean(WHO[who]+"Hid",true);own.putBoolean(WHO[who]+"Hinted",false);}own.putInt("GroveFound",0);
            }
            return;
        }
        if(beat==1){
            boolean all=true;for(int who=KEL;who<=KEN;who++)all&=walk(p,own,who,cast[who],b,RUN);
            if(all||ticks>HIDE_TIME){beat(own,2);BorrascaStory.say(p,cast[KEL],"Ready!");}
            return;
        }
        if(beat==2){
            boolean any=false;
            for(int who=KEL;who<=KEN;who++){
                var kid=cast[who];boolean hid=own.getBoolean(WHO[who]+"Hid");
                if(hid&&p.distanceToSqr(kid)<FOUND*FOUND&&p.hasLineOfSight(kid)){
                    hid=false;own.putBoolean(WHO[who]+"Hid",false);own.putInt("GroveFound",own.getInt("GroveFound")+1);send(own,who,WAITS[who]);
                    BorrascaStory.say(p,kid,who==KEL?"Okay, you got me. I'm never that easy.":who==PAT?"Aw, come on. I was barely breathing.":"No fair. You heard me laugh.");
                }
                if(hid&&ticks>HINT_AFTER&&!own.getBoolean(WHO[who]+"Hinted")&&p.distanceToSqr(kid)<81&&!BorrascaStory.recentSpeech(p)){
                    own.putBoolean(WHO[who]+"Hinted",true);
                    BorrascaStory.say(p,kid,switch(place(own,who,1)){
                        case BEHIND_WOODPILE->"It smells like sawdust where I am.";
                        case ROUND_THE_CORNER->"I can see the street from here.";
                        case BEHIND_EAST_OAK->"You walked right past me.";
                        case BEHIND_FAR_TRUNK->"You're not even close. Further.";
                        case BEHIND_LOW_TREE->"South. That's all you get.";
                        default->"North. That's all you get.";});
                }
                any|=hid;walk(p,own,who,kid,b,hid?RUN:OUT);
            }
            if(!any)beat(own,3);
            else if(ticks>SEEK_TIME){
                BorrascaStory.say(p,cast[own.getBoolean(WHO[KEN]+"Hid")?KEN:own.getBoolean(WHO[PAT]+"Hid")?PAT:KEL],"Olly olly oxen free!");
                for(int who=KEL;who<=KEN;who++){own.putBoolean(WHO[who]+"Hid",false);send(own,who,WAITS[who]);}beat(own,3);
            }
            return;
        }
        boolean back=true;for(int who=KEL;who<=KEN;who++)back&=walk(p,own,who,cast[who],b,OUT);
        if(back||ticks>200)state(own,TAG);
    }

    private static void tag(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor[] cast,int ticks){
        int beat=own.getInt("GamesBeat");
        if(beat==0){
            // Kendra calls it and all three are off at once, each to a different part of the rings.
            own.putInt("GroveTagged",0);own.putInt("GamesTaunt",0);
            for(int who=KEL;who<=KEN;who++){own.putBoolean(WHO[who]+"Out",false);own.putBoolean(WHO[who]+"Ran",false);own.putInt(WHO[who]+"Runs",0);send(own,who,STARTS[who]);}
            BorrascaStory.say(p,cast[KEN],"Tag! New kid's still it!");beat(own,1);
            return;
        }
        if(beat==1){
            int out=0;
            for(int who=KEL;who<=KEN;who++){
                var kid=cast[who];String k=WHO[who];
                if(own.getBoolean(k+"Out")){out++;if(walk(p,own,who,kid,b,OUT))face(kid,p.position());continue;}
                double d=kid.distanceTo(p);
                if(d<TOUCH&&Math.abs(kid.getY()-p.getY())<1.5){
                    own.putBoolean(k+"Out",true);out++;own.putInt("GroveTagged",own.getInt("GroveTagged")+1);send(own,who,WAITS[who]);
                    BorrascaStory.say(p,kid,who==KEL?"Okay! Okay. You got me.":who==PAT?"My shoe's untied. Doesn't count. Fine, it counts.":"You're faster than Patrick. Everybody is.");
                    walk(p,own,who,kid,b,OUT);continue;
                }
                double speed=Math.max(SLOWEST,RUN*Math.pow(TIRED,own.getInt(k+"Runs")));
                if(!walk(p,own,who,kid,b,speed))continue;
                // Stood at a point of the rings: bolts when the reader is near, and keeps going while they are close behind.
                int here=own.getInt(k+"From");
                if(d<(own.getBoolean(k+"Ran")?KEEP_ON:BOLT)){
                    int to=flee(p,b,own,who,here);
                    if(to!=here){send(own,who,to);own.putInt(k+"Runs",own.getInt(k+"Runs")+1);own.putBoolean(k+"Ran",true);walk(p,own,who,kid,b,speed);continue;}
                }
                own.putBoolean(k+"Ran",false);face(kid,p.position());
            }
            if(out<3&&ticks%TAUNT_EVERY==0&&!BorrascaStory.recentSpeech(p)){
                int n=own.getInt("GamesTaunt");
                for(int i=0;i<TAUNTS.length;i++){var taunt=TAUNTS[(n+i)%TAUNTS.length];int who=(Integer)taunt[0];if(own.getBoolean(WHO[who]+"Out")||cast[who].distanceToSqr(p)<BOLT*BOLT)continue;
                    BorrascaStory.say(p,cast[who],(String)taunt[1]);own.putInt("GamesTaunt",(n+i+1)%TAUNTS.length);break;}
            }
            if(out==3||ticks>TAG_TIME)beat(own,2);
            return;
        }
        boolean all=own.getInt("GroveTagged")==3;
        if(all?lines(p,own,cast,PAT,"No fair. You're fast.",KEL,"Okay. That's everybody. The Tree. I promised.")
              :lines(p,own,cast,KEL,"Time! Nobody's it. I have to show you the Tree before supper."))state(own,LEAVING);
    }
    /** The next point of the rings for a runner stood at one: the one that ends furthest from the reader, not across the reader's path. */
    private static int flee(ServerPlayer p,BlockPos b,CompoundTag own,int who,int here){
        int best=here;double far=-1;var me=at(b,here);var reader=p.position();
        for(int to:RING){
            if(to==here||!line(here,to))continue;
            var end=at(b,to);
            // Not past the reader: the reader is not nearer the far end than the runner is, and not standing on the way.
            double mine=me.distanceTo(end),theirs=reader.distanceTo(end);if(theirs<mine+.5)continue;
            double score=theirs;for(int other=KEL;other<=KEN;other++)if(other!=who&&!own.getBoolean(WHO[other]+"Out")&&goal(own,other)==to)score-=3;
            if(score>far){far=score;best=to;}
        }
        return best;
    }
    private static boolean line(int a,int c){for(var e:LINES)if(e[0]==a&&e[1]==c||e[0]==c&&e[1]==a)return true;return false;}

    /** The reader has gone, or never came. Nobody is called back. */
    private static void drop(ServerPlayer p,CompoundTag own,LiteraryActor kel){
        if(p.distanceToSqr(kel)<48*48)BorrascaStory.say(p,kel,"Okay. The Tree, then. Wait for me!");
        state(own,LEAVING);
    }
    /** Out by the alley to where the street had them, on their feet while anybody can see them. True when all three are there. */
    private static boolean leaving(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor[] cast,int ticks){
        boolean all=true;
        for(int who=KEL;who<=KEN;who++){
            var a=cast[who];send(own,who,STREETS[who]);
            if(near(a,b,STREETS[who],.6)){put(own,who,STREETS[who]);BorrascaCast.stop(a);continue;}
            if(ticks<who*20){all=false;continue;}
            if(ticks>40&&BorrascaCast.recall(p,a,BorrascaRooms.at(b,POINTS[STREETS[who]]))){put(own,who,STREETS[who]);continue;}
            all&=walk(p,own,who,a,b,OUT);
        }
        if(all)for(String key:new String[]{"CastKimber","TownEli","TownWren"})BorrascaCast.remember(own,key,actor(p,own,key));
        return all;
    }
    /** Back in town after being away: games left in the middle are over, and the three are where the street has them. */
    static void arrived(CompoundTag own){
        if(!on(own))return;
        own.putInt("Games",OVER);for(String key:new String[]{"CastKimber","TownEli","TownWren"})own.remove(key+"At");
    }

    /** For the account. */
    static String told(CompoundTag own){
        if(!own.getBoolean("GrovePlayed"))return "";
        return "After that all four of us played in the trees behind the store. "
            +(own.getBoolean("GroveHid")?"Kelly counted, and she never found me. ":"")
            +(own.getInt("GroveTagged")==3?"I tagged all three of them. ":"");
    }
}
