package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.BorrascaArcadePayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.436, the owner: "It should have its own arcade games, don't recycle". The drugstore's two cabinets, by the window on its south
 * wall, and their games, both made for this town and nothing else:
 * <ul>
 * <li><b>DEEP SEAM</b>: a mine cart running down a dark seam on three rails. Up and down change rails, the space bar hops the cart
 * over a gap in the rail, and a fallen timber can only be gone round. Lamps along the way keep the cart's light up, and the light
 * shrinks as it goes. Three carts.</li>
 * <li><b>BIG CAT</b>: night fishing off a johnboat for the catfish on the river bottom. Left and right steer the hook, and the
 * space bar (or up) reels in, while letting go lets it sink. Perch and bass come up easily. The big cat pulls back, and reeled too
 * hard the line parts. Snapping turtles take the worm off the hook. Six worms, until dawn.</li>
 * </ul>
 * A play costs a quarter from the child's own money (the friend puts in what is short, as at the counters). The rules are here, shared
 * with the client's screen ({@code client.BorrascaArcadeScreen}). The screen sends the keys it held on every tick, never a score: the
 * server plays the game again from the session's seed and keeps what it scores, as the Polybius cabinets do (but nothing of theirs
 * is used here).
 * <p>
 * Each reader's town has its own table on each cabinet. It starts with the town's children's initials: PAT, KEN and KEL among them,
 * and every one a P or a K, as the babies on the doctor's corkboard are (nothing says why). The table is kept from visit to visit,
 * so Patrick's score is still at the top after he is gone. Grown, the reader finds the cabinets unplugged, with the last table still
 * showing through the dust.
 */
public final class BorrascaArcade {
    private BorrascaArcade(){}
    public static final int W=200,H=150;
    /** The keys a game reads, as bits: what the screen held on a tick. */
    public static final int LEFT=1,RIGHT=2,UP=4,DOWN=8,SPACE=16,KEYS=31;
    public static final int SEAM=0,CAT=1,GAMES=2;
    public static final String[] TITLES={"DEEP SEAM","BIG CAT"};
    /** The longest either game can run, in ticks: a screen stops there, and the server refuses a longer log. */
    public static final int[] LONGEST={6000,3600};
    public static final int PRICE=25,SLACK=40,ROWS=6;
    /** What the client's screen plays for a game's sound: a tick, a pick-up, a crash, a splash, a hum under the track. */
    public static final int TICK=0,GET=1,CRASH=2,SPLASH=3,HUM=4;
    public interface Sound{void play(int kind,float pitch);}
    static final Sound SILENT=(k,p)->{};

    public abstract static class Game{
        final Random random;final Sound sound;public int ticks;int keys,prev;
        Game(long seed,Sound sound){random=new Random(seed);this.sound=sound==null?SILENT:sound;}
        /** One tick with these keys held. */
        public final void tick(int keys){this.keys=keys;step();prev=keys;ticks++;}
        abstract void step();
        public abstract int score();
        public abstract boolean over();
        boolean held(int key){return (keys&key)!=0;}
        /** A key that went down on this tick. */
        boolean pressed(int key){return (keys&key)!=0&&(prev&key)==0;}
    }
    public static Game of(int game,long seed,Sound sound){return game==CAT?new BigCat(seed,sound):new DeepSeam(seed,sound);}

    /** What the server makes of a game: its own score, how many ticks the keys covered, and whether it was over. */
    public record Result(int score,int ticks,boolean over){}
    /** The game played again from its seed with the keys the screen held, as runs ({@code count<<5|keys}). Null for a malformed log. */
    public static @Nullable Result replay(int game,long seed,List<Integer> runs,int longest){
        var g=of(game,seed,SILENT);int ticks=0;
        for(int run:runs){
            int keys=run&KEYS,count=run>>>5;if(count<=0)return null;
            for(int i=0;i<count&&!g.over();i++){if(++ticks>longest)return null;g.tick(keys);}
            if(g.over())break;
        }
        return new Result(Math.max(0,g.score()),ticks,g.over());
    }
    /** The ticks a log covers, or -1 for a malformed one. */
    public static int length(List<Integer> runs){long n=0;for(int run:runs){int count=run>>>5;if(count<=0)return -1;n+=count;if(n>Integer.MAX_VALUE)return -1;}return (int)n;}

    // ------------------------------------------------------------------------------------------------ DEEP SEAM
    public static final class DeepSeam extends Game{
        /** The three rails' heights on the screen, and where the cart runs along them. */
        public static final int[] RAILS={58,88,118};public static final float CART_X=36;
        public static final int GAP=0,TIMBER=1,LAMP=2;
        /** How long a hop keeps the cart off the rail, in ticks, and how long a gap in the rail is. */
        public static final int HOP=20,GAP_LONG=10;
        public int lane=1,air,lives=3,hurt,lamps;public float shown=RAILS[1],dist,light=100,v=1.5F;
        /** What the cart last came to grief on (GAP or TIMBER), or -1. */
        public int struck=-1;
        /** What comes down the seam: x, rail, kind, length. */
        public final List<float[]> things=new ArrayList<>();
        int next=40,hum=1800;
        DeepSeam(long seed,Sound sound){super(seed,sound);}
        void step(){
            if(pressed(UP)&&lane>0){lane--;sound.play(TICK,1.2F);}
            if(pressed(DOWN)&&lane<2){lane++;sound.play(TICK,1F);}
            if(pressed(SPACE)&&air==0){air=HOP;sound.play(TICK,1.6F);}
            if(air>0)air--;if(hurt>0)hurt--;
            shown+=(RAILS[lane]-shown)*.4F;
            v=Math.min(4.2F,1.5F+ticks/700F);dist+=v;light=Math.max(0,light-.06F);
            if(--next<=0){
                int roll=random.nextInt(10);int kind=roll<4?GAP:roll<7?TIMBER:LAMP;
                things.add(new float[]{W+10,random.nextInt(3),kind,kind==GAP?GAP_LONG:kind==TIMBER?6:5});
                next=22+(int)(random.nextInt(30)*2.2F/v);
            }
            for(var it=things.iterator();it.hasNext();){
                var t=it.next();t[0]-=v;
                if(t[0]+t[3]<0){it.remove();continue;}
                if((int)t[1]!=lane||t[0]>=CART_X+5||t[0]+t[3]<=CART_X-5)continue;
                int kind=(int)t[2];
                if(kind==LAMP){lamps++;light=100;sound.play(GET,1.3F);it.remove();}
                else if(hurt==0&&(kind==TIMBER||air==0)){lives--;hurt=40;struck=kind;sound.play(CRASH,.7F);it.remove();}
            }
            // Deep in, now and then, something hums under the track.
            if(--hum<=0){hum=300+random.nextInt(400);sound.play(HUM,.5F);}
        }
        public int score(){return (int)(dist/10)+lamps*25;}
        public boolean over(){return lives<=0;}
    }

    // ------------------------------------------------------------------------------------------------ BIG CAT
    public static final class BigCat extends Game{
        public static final float SURFACE=24,BOTTOM=138,BOAT_X=100;
        public static final int PERCH=0,BASS=1,CATFISH=2,TURTLE=3;
        public static final int[] VALUE={5,15,100,0};
        public float hx=BOAT_X,hy=SURFACE;public int worms=6,hooked=-1,tension,hurt,caught,cats,score;
        /** Who is in the river: x, y, kind, way (-1 or 1), speed. */
        public final List<float[]> swimmers=new ArrayList<>();
        int nextFish=20,nextCat=500,nextTurtle=300;
        BigCat(long seed,Sound sound){super(seed,sound);}
        boolean reeling(){return held(SPACE)||held(UP);}
        void step(){
            float side=(held(RIGHT)?1:0)-(held(LEFT)?1:0);boolean reel=reeling();
            if(hooked<0){hx=clamp(hx+side*1.4F);hy=reel?Math.max(SURFACE,hy-1.6F):Math.min(BOTTOM,hy+.9F);}
            else{
                // A fish on: it comes up only while reeled, and a big cat pulls back while it is let be.
                if(reel){hy=Math.max(SURFACE,hy-(hooked==CATFISH?.7F:1.2F));tension+=hooked==CATFISH?3:hooked==BASS?1:0;}
                else{tension=Math.max(0,tension-4);if(hooked==CATFISH)hy=Math.min(BOTTOM,hy+.3F);}
                hx=clamp(hx+side*.8F+(hooked==CATFISH?(float)Math.sin(ticks*.05F)*.8F:0));
                if(tension>=100){hooked=-1;tension=0;worms--;hurt=30;sound.play(CRASH,.6F);}
                else if(hy<=SURFACE){score+=VALUE[hooked];caught++;if(hooked==CATFISH)cats++;sound.play(SPLASH,hooked==CATFISH?.6F:1.3F);hooked=-1;tension=0;worms--;}
            }
            if(--nextFish<=0){nextFish=25+random.nextInt(40);boolean bass=random.nextInt(10)<4;swim(bass?BASS:PERCH,bass?70+random.nextInt(35):40+random.nextInt(30),bass?.7F+random.nextFloat()*.4F:.9F+random.nextFloat()*.6F);}
            if(--nextCat<=0){nextCat=700+random.nextInt(600);boolean there=false;for(var f:swimmers)there|=(int)f[2]==CATFISH;if(!there)swim(CATFISH,124+random.nextInt(10),.35F);}
            if(--nextTurtle<=0){nextTurtle=400+random.nextInt(400);swim(TURTLE,90+random.nextInt(30),.3F);}
            for(var it=swimmers.iterator();it.hasNext();){
                var f=it.next();f[0]+=f[3]*f[4];
                if(f[0]<-20||f[0]>W+20){it.remove();continue;}
                int kind=(int)f[2];boolean big=kind==CATFISH;
                if(hooked>=0||worms<=0||Math.abs(f[0]-hx)>=(big?9:5)||Math.abs(f[1]-hy)>=(big?6:4))continue;
                // A snapping turtle takes the worm and swims on.
                if(kind==TURTLE){if(hurt==0){worms--;hurt=30;sound.play(CRASH,1.6F);}continue;}
                hooked=kind;hx=f[0];hy=f[1];it.remove();sound.play(GET,big?.5F:1F);
            }
            if(hurt>0)hurt--;
        }
        void swim(int kind,float y,float speed){int way=random.nextBoolean()?1:-1;swimmers.add(new float[]{way>0?-10:W+10,y,kind,way,speed});}
        static float clamp(float x){return Math.max(8,Math.min(W-8,x));}
        public int score(){return score;}
        public boolean over(){return worms<=0&&hooked<0||ticks>=LONGEST[CAT];}
    }

    // ------------------------------------------------------------------------------------------------ hands that are not the reader's
    /**
     * 0.4.446 (docs/BORRASCA_ARCADE_FRIENDS_0_4_446.md; the owner: "Give Patrick and Kendra a turn at the arcade... Kendra knows a
     * trick he keeps getting wrong"). How each of them plays:
     * <ul>
     * <li>HOPS: DEEP SEAM as Patrick plays it. He hops whatever is on his rail, fallen timbers too, which cannot be hopped, and
     * goes after any lamp on the next rail.</li>
     * <li>ROUND: DEEP SEAM as he plays it once he has been told. He changes rails for a timber. As the cart gets faster his nerve
     * goes, and now and then he only watches a thing come.</li>
     * <li>FISHES: BIG CAT as Kendra plays it. She keeps her hook up out of the perch until there is something worth a worm, goes
     * down for a bass or for the big cat, keeps off the turtles, and lets the big one run: reels to a point, rests, reels again.</li>
     * </ul>
     */
    public static final int HOPS=0,ROUND=1,FISHES=2;
    /** How often, at the cart's top speed, the told hand freezes at a thing in its way. */
    static final float NERVE=.3F;
    /**
     * The keys such a hand holds on a tick. They depend only on the game in front of it and on its own numbers, drawn from the
     * game's seed, so a reader's screen and the server play the same game out of the same seed and nothing else need be sent.
     */
    public static final class Hand{
        final int way;final Random nerve;final Set<float[]> judged=Collections.newSetFromMap(new IdentityHashMap<>());float[] frozen;boolean rest;
        public Hand(long seed,int way){this.way=way;nerve=new Random(seed^0x5DEECE66DL);}
        public int keys(Game g){return g instanceof DeepSeam s?seam(s):g instanceof BigCat c?cat(c):0;}
        private static float[] ahead(DeepSeam s,int lane){float[] best=null;for(var t:s.things)if((int)t[1]==lane&&t[0]+t[3]>DeepSeam.CART_X-5&&(best==null||t[0]<best[0]))best=t;return best;}
        /** Ticks until a thing reaches the cart. */
        private static float eta(DeepSeam s,float[] t){return (t[0]-(DeepSeam.CART_X+5))/s.v;}
        private int seam(DeepSeam s){
            var t=ahead(s,s.lane);
            if(t!=null&&(int)t[2]!=DeepSeam.LAMP){
                if(eta(s,t)<=24&&judged.add(t)&&way==ROUND&&nerve.nextFloat()<NERVE*Math.max(0,(s.v-2.4F)/1.8F))frozen=t;
                if(frozen==t)return 0;
                if((int)t[2]==DeepSeam.GAP||way==HOPS)return eta(s,t)<=2&&s.air==0&&(s.prev&SPACE)==0?SPACE:0;
                if(eta(s,t)<=20){
                    // Round it: the next rail over that is clear, or has a lamp, or whose own trouble is furthest off.
                    int to=-1,rank=-1;float far=-1;
                    for(int lane=s.lane-1;lane<=s.lane+1;lane+=2){
                        if(lane<0||lane>2)continue;var o=ahead(s,lane);
                        int r=o==null||(int)o[2]==DeepSeam.LAMP?2:(int)o[2]==DeepSeam.GAP||eta(s,o)>14?1:0;float e=o==null?999:eta(s,o);
                        if(r>rank||r==rank&&e>far){rank=r;far=e;to=lane;}
                    }
                    if(to>=0){int key=to<s.lane?UP:DOWN;if((s.prev&key)==0)return key;}
                }
                return 0;
            }
            // Nothing in the way: a lamp on the next rail.
            if(t==null||eta(s,t)>26)for(int lane=s.lane-1;lane<=s.lane+1;lane+=2){
                if(lane<0||lane>2)continue;var o=ahead(s,lane);
                if(o!=null&&(int)o[2]==DeepSeam.LAMP&&eta(s,o)>3&&eta(s,o)<34){int key=lane<s.lane?UP:DOWN;if((s.prev&key)==0)return key;}
            }
            return 0;
        }
        private int cat(BigCat c){
            if(c.hooked>=0){
                if(c.hooked!=BigCat.CATFISH)return SPACE;
                if(rest&&c.tension<=20)rest=false;
                if(!rest&&c.tension>=70)rest=true;
                return rest?0:SPACE;
            }
            rest=false;float[] want=null;
            for(var f:c.swimmers)if((int)f[2]==BigCat.CATFISH){want=f;break;}
            if(want==null){float near=Float.MAX_VALUE;for(var f:c.swimmers)if((int)f[2]==BigCat.BASS&&Math.abs(f[0]-c.hx)<near){near=Math.abs(f[0]-c.hx);want=f;}}
            for(var f:c.swimmers)if((int)f[2]==BigCat.TURTLE&&Math.abs(f[0]-c.hx)<16&&Math.abs(f[1]-c.hy)<14)return SPACE;
            if(want==null)return c.hy>32?SPACE:0;
            // Where it will be by the time the hook is down to it.
            float dy=want[1]-c.hy,t=Math.abs(dy)/(dy>0?.9F:1.6F),tx=want[0]+want[3]*want[4]*t;int k=0;
            if(tx<c.hx-2)k|=LEFT;else if(tx>c.hx+2)k|=RIGHT;
            if(Math.abs(tx-c.hx)<30){if(dy<-2)k|=SPACE;}else if(c.hy>32)k|=SPACE;
            return k;
        }
    }
    /**
     * A game one of them played to its end: the score, how long it ran, and when it went wrong or right (the tick, and what it
     * was: on DEEP SEAM a cart lost to a GAP or a TIMBER, on BIG CAT the big cat taking the hook).
     */
    public record Played(int score,int ticks,int[] at,int[] what){}
    public static Played play(int game,long seed,int way){
        var g=of(game,seed,SILENT);var hand=new Hand(seed,way);var at=new ArrayList<Integer>();var what=new ArrayList<Integer>();
        while(!g.over()&&g.ticks<LONGEST[game]){
            int lives=g instanceof DeepSeam s?s.lives:0,hooked=g instanceof BigCat c?c.hooked:-1;
            g.tick(hand.keys(g));
            if(g instanceof DeepSeam s&&s.lives<lives){at.add(g.ticks);what.add(s.struck);}
            if(g instanceof BigCat c&&c.hooked==BigCat.CATFISH&&hooked!=BigCat.CATFISH){at.add(g.ticks);what.add(BigCat.CATFISH);}
        }
        return new Played(Math.max(0,g.score()),g.ticks,at.stream().mapToInt(Integer::intValue).toArray(),what.stream().mapToInt(Integer::intValue).toArray());
    }
    /** How many seeds are tried for a game that should come out a certain way. */
    static final int TRIES=24;
    /**
     * A seed for one of their games: of a few seeds drawn from this one, the first whose game fits, and otherwise the one whose
     * game comes nearest (the least by the comparator). Every one of them is a whole game that hand really plays.
     */
    public static long seed(long from,int game,int way,java.util.function.Predicate<Played> fits,Comparator<Played> nearest){
        var draw=new Random(from);long best=0;Played kept=null;
        for(int i=0;i<TRIES;i++){long seed=draw.nextLong();var played=play(game,seed,way);if(fits.test(played))return seed;if(kept==null||nearest.compare(played,kept)<0){kept=played;best=seed;}}
        return best;
    }

    // ------------------------------------------------------------------------------------------------ the cabinets in the drugstore
    /** A session: the game, its token (the game's seed too), and when it opened by the server's clock and by the wall clock. */
    public record Session(int game,long token,long start,long startMillis){}
    static final Map<UUID,Session> SESSIONS=new HashMap<>();
    public static @Nullable Session session(UUID id){return SESSIONS.get(id);}
    /** For tests: as if the session had been open this many ticks longer. */
    public static void backdate(UUID id,int ticks){var s=SESSIONS.get(id);if(s!=null)SESSIONS.put(id,new Session(s.game(),s.token(),s.start()-ticks,s.startMillis()-ticks*50L));}

    /** Which game a clicked cabinet plays, or -1 if the block is not one of the two. */
    public static int game(BlockState s){return BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.CABINET)||BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.CABINET_TOP)?s.getValue(BorrascaShopBlock.STAGE):-1;}

    /** A cabinet clicked: a quarter in and the game's screen, or what a grown reader finds. */
    static void click(ServerPlayer p,BlockPos at,CompoundTag own,int game){
        if(own.getInt("Visit")>=3){BorrascaStory.cue(p,"The screen is dark, and the plug hangs down the back. Through the dust on the glass the last table still shows: "+table(own,game).get(0)[0]+" at the top.");return;}
        if(SESSIONS.containsKey(p.getUUID())){BorrascaStory.cue(p,"You're already playing.");return;}
        if(BorrascaArcadeFriends.taken(p,own,game))return; // 0.4.446: one of the others is at this cabinet, and can be watched
        if(!BorrascaPeople.payQuarter(p,own)){BorrascaStory.cue(p,"It takes a quarter. You don't have one left.");return;}
        long token=p.getRandom().nextLong();
        SESSIONS.put(p.getUUID(),new Session(game,token,p.serverLevel().getGameTime(),System.currentTimeMillis()));
        own.putInt("ArcadePlays"+game,own.getInt("ArcadePlays"+game)+1);
        BorrascaArcadeFriends.started(p,own,game); // 0.4.446: Patrick and Kendra come in to watch
        HousePackets.send(p,new BorrascaArcadePayload(game,token,initials(p),tag(own,game)));
        p.serverLevel().playSound(null,at,SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.BLOCKS,.5F,game==SEAM?.9F:1.3F);
    }

    /**
     * A game over, from the reader's screen: the keys it held on each tick. The log may run no longer than the session has been
     * open (by the server's clock or the wall clock, whichever is further on), and the game is played again from the session's
     * seed with those keys; that score is the one kept.
     */
    public static boolean submit(ServerPlayer p,long token,List<Integer> runs){
        var s=SESSIONS.get(p.getUUID());if(s==null||s.token()!=token)return false;
        SESSIONS.remove(p.getUUID());
        long open=Math.max(p.serverLevel().getGameTime()-s.start(),(System.currentTimeMillis()-s.startMillis())/50);
        int length=length(runs);
        if(length<0||length>open+SLACK||length>LONGEST[s.game()])return false;
        var game=replay(s.game(),s.token(),runs,LONGEST[s.game()]);
        if(game==null||game.ticks()==0)return false;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),BorrascaStory.PLACE);
        int score=game.score();var rows=table(own,s.game());String above=rows.get(0)[0];
        int rank=record(own,s.game(),initials(p),score);
        if(rank>1)above=table(own,s.game()).get(rank-2)[0];
        own.putInt("ArcadeBest"+s.game(),Math.max(own.getInt("ArcadeBest"+s.game()),score));
        if(rank==1&&rows.get(0)[0].equals("PAT"))own.putBoolean("OverPatrick",true);
        BorrascaArcadeFriends.finished(p,own,s.game(),rank==1&&rows.get(0)[0].equals("PAT")); // 0.4.446: what the two of them make of it
        LiteraryVignettes.save(d,p.getUUID(),BorrascaStory.PLACE,own);
        BorrascaStory.cue(p,rank==0?"GAME OVER. "+score+". Not enough for the table.":rank==1?"GAME OVER. "+score+". Your initials go to the top, over "+rows.get(0)[0]+"'s.":"GAME OVER. "+score+". Your initials go up under "+above+"'s.");
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the tables
    /** Who was on each cabinet's table before the reader: the town's children, Patrick, Kendra and Kelly among them, every initial a P or a K. */
    static final String[][][] SEEDED={
        {{"PAT","730"},{"KRT","610"},{"KEN","480"},{"PRY","390"},{"KEL","270"},{"PAM","150"}},
        {{"PRY","455"},{"PAT","380"},{"KEL","300"},{"KRT","240"},{"KEN","185"},{"PAM","95"}}};
    /** A reader's three letters, as a cabinet takes them. */
    public static String initials(ServerPlayer p){var n=p.getGameProfile().getName().replaceAll("[^A-Za-z0-9]","").toUpperCase(Locale.ROOT);return (n+"AAA").substring(0,3);}
    /** This reader's table for a game, best first: initials and score. */
    public static List<String[]> table(CompoundTag own,int game){
        var out=new ArrayList<String[]>();var key="Arcade"+game;
        if(!own.contains(key)){for(var row:SEEDED[game])out.add(row.clone());return out;}
        for(Tag t:own.getList(key,Tag.TAG_COMPOUND)){var c=(CompoundTag)t;out.add(new String[]{c.getString("N"),Integer.toString(c.getInt("S"))});}
        return out;
    }
    /** A score onto the table: its place there from 1, or 0 if it is not good enough for it. Ties go under the score already there. */
    public static int record(CompoundTag own,int game,String who,int score){
        var rows=table(own,game);int rank=0;
        for(int i=0;i<rows.size();i++)if(score>Integer.parseInt(rows.get(i)[1])){rank=i+1;break;}
        if(rank==0&&rows.size()<ROWS&&score>0)rank=rows.size()+1;
        if(rank==0)return 0;
        rows.add(rank-1,new String[]{who,Integer.toString(score)});while(rows.size()>ROWS)rows.remove(rows.size()-1);
        var list=new ListTag();for(var r:rows){var c=new CompoundTag();c.putString("N",r[0]);c.putInt("S",Integer.parseInt(r[1]));list.add(c);}
        own.put("Arcade"+game,list);return rank;
    }
    static CompoundTag tag(CompoundTag own,int game){
        var list=new ListTag();for(var r:table(own,game)){var c=new CompoundTag();c.putString("N",r[0]);c.putInt("S",Integer.parseInt(r[1]));list.add(c);}
        var out=new CompoundTag();out.put("Rows",list);return out;
    }
}
