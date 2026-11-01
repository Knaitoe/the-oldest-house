package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.PolybiusBrawlPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.221 (owner: "Add a brawler that supports 4 players. Using Minecraft characters would be cool."): Block Party, the arcade's
 * four-player cabinet. Up to four readers stand at it at once, each playing as their own skin, down one street at night, against
 * what comes at them in waves. One game per arcade runs on the server, twenty steps a second; each player's screen
 * ({@code client/PolybiusBrawlScreen}) sends only which keys are held and draws what the server sends back.
 *
 * <p>A reader joins by using the cabinet. The first to join opens five seconds for the others; anyone may drop in while it runs,
 * into an empty seat. Three lives each; a fighter who loses one comes back after two seconds, tinted (nothing flashes). When every
 * seated fighter is out of lives the game is over: each is paid in prize tickets for what they knocked down, and the screen goes
 * back to its attract mode. Walking away from the cabinet, or closing the screen, gives up the seat.
 *
 * <p>0.4.224 (owner: "I'd love to make it more mechanically complex. let players pick a class, add combos, items"):
 * <ul>
 * <li><b>Classes</b>, picked with left and right while the game waits to start: the Brawler (fists, a dash punch), the Knight
 * (a sword with a longer reach and a shield), the Archer (arrows, a kick up close, a volley) and the Alchemist (thrown potions
 * that splash, and one that heals the fighters near it). Each has its own health and pace.</li>
 * <li><b>Combos</b>: hits landed one after another keep a chain going; every third hit of a chain is the class's finisher
 * (an uppercut, a spin, an arrow that goes through everything, a bigger splash), and a longer chain scores more.</li>
 * <li><b>Specials</b> on their own key, each with its own wait.</li>
 * <li><b>Items</b> dropped by what goes down: a steak and a golden apple heal, potions of swiftness and strength last a while,
 * TNT is thrown with the next attack, and a totem is a life.</li>
 * <li>From the fourth wave spiders come too, and every fifth wave ends with an enderman, which steps from one fighter's side to
 * another's.</li>
 * </ul>
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PolybiusBrawl {
    private PolybiusBrawl(){}
    public static final int SEATS=4,W=200,TOP=84,BOTTOM=138,LOBBY=100,OVER=80,LIVES=3,HP=20,RESPAWN=40,REACH=5;
    public static final int ZOMBIE=0,SKELETON=1,CREEPER=2,SPIDER=3,ENDERMAN=4;
    public static final int UP=1,DOWN=2,LEFT=4,RIGHT=8,PUNCH=16,LEAVE=32,SPECIAL=64;
    /** What each kind of foe takes to knock down, and is worth. */
    static final int[] FOE_HP={10,8,6,6,40},FOE_POINTS={100,150,200,120,1000};
    /** The classes, their health and their pace. */
    public static final int BRAWLER=0,KNIGHT=1,ARCHER=2,ALCHEMIST=3,CLASSES=4;
    public static final String[] CLASS_NAMES={"BRAWLER","KNIGHT","ARCHER","ALCHEMIST"};
    static final int[] CLASS_HP={20,26,16,18};
    static final double[] CLASS_SPEED={1.6,1.3,1.7,1.5};
    static final int[] SPECIAL_WAIT={80,100,90,160};
    /** The items, as dropped and as carried off. */
    public static final int STEAK=0,GOLDEN_APPLE=1,SWIFTNESS=2,STRENGTH=3,TNT=4,TOTEM=5;
    /** Kinds of what the fighters throw and shoot. */
    public static final int ARROW=0,POTION=1,THROWN_TNT=2;
    static final int CHAIN=30,BUFF=200;

    static final class Fighter {
        final UUID id;final String name;double x,y;int cls,hp,lives=LIVES,facing=1,punch,cool,hurt,respawn,score,keys,last;
        int combo,chain,special,shield,dash,swift,strong,tnt,finisher;final Set<Foe> dashed=new HashSet<>();
        Fighter(UUID id,String name,double x,double y,int cls){this.id=id;this.name=name;this.x=x;this.y=y;this.cls=cls;hp=CLASS_HP[cls];}
        boolean out(){return lives<=0&&hp<=0&&respawn==0;}
        boolean up(){return hp>0&&respawn==0;}
        int max(){return CLASS_HP[cls];}
        int damage(int base){return strong>0?(int)Math.round(base*1.6):base;}
    }
    static final class Foe {int type,hp,facing=-1,hurt,fuse,cool,walk,stun,slow;double x,y;Foe(int type,double x,double y){this.type=type;this.x=x;this.y=y;hp=FOE_HP[type];}}
    static final class Shot {double x,y;int dir;Shot(double x,double y,int dir){this.x=x;this.y=y;this.dir=dir;}}
    /** What a fighter throws or shoots: an arrow flies straight; a potion or TNT arcs to where it lands. */
    static final class Bolt {
        final int kind,seat,dmg;double x,y;final int dir;int t;final double tx;final boolean pierce;final Set<Foe> hit=new HashSet<>();
        Bolt(int kind,int seat,double x,double y,int dir,int dmg,boolean pierce,double tx,int t){this.kind=kind;this.seat=seat;this.x=x;this.y=y;this.dir=dir;this.dmg=dmg;this.pierce=pierce;this.tx=tx;this.t=t;}
    }
    static final class Drop {final int type;final double x,y;int age;Drop(int type,double x,double y){this.type=type;this.x=x;this.y=y;}}
    /** A splash or a blast, drawn as a ring that widens and fades. */
    static final class Ring {final double x,y;final int r,kind;int age;Ring(double x,double y,int r,int kind){this.x=x;this.y=y;this.r=r;this.kind=kind;}}
    static final class Game {
        final BlockPos base;final Fighter[] seats=new Fighter[SEATS];final List<Foe> foes=new ArrayList<>();final List<Shot> shots=new ArrayList<>();
        final List<Bolt> bolts=new ArrayList<>();final List<Drop> drops=new ArrayList<>();final List<Ring> rings=new ArrayList<>();
        final Deque<Integer> coming=new ArrayDeque<>();final Random random=new Random();int tick,lobby=LOBBY,wave,banner,over=-1;boolean started,calm;
        Game(BlockPos base){this.base=base;}
        int seated(){int n=0;for(var f:seats)if(f!=null)n++;return n;}
    }
    static final Map<BlockPos,Game> GAMES=new HashMap<>();
    public static void clearAll(){GAMES.clear();}

    @Nullable static Game game(UUID id){for(var g:GAMES.values())for(var f:g.seats)if(f!=null&&f.id.equals(id))return g;return null;}
    /** Which seat a reader holds at Block Party, or -1. */
    public static int seat(UUID id){var g=game(id);if(g==null)return -1;for(int i=0;i<SEATS;i++)if(g.seats[i]!=null&&g.seats[i].id.equals(id))return i;return -1;}
    public static boolean playing(UUID id){return seat(id)>=0;}

    /** A reader uses the cabinet: a seat if there is one, and the screen. */
    public static void join(ServerPlayer p,BlockPos b){
        var g=GAMES.computeIfAbsent(b.immutable(),Game::new);int seat=seat(p.getUUID());
        if(seat>=0){send(p,g,seat);return;}
        if(Polybius.playing(p.getUUID())){Polybius.cue(p,"You're already playing.");return;}
        if(g.over>=0){Polybius.cue(p,"GAME OVER, on all four screens at once. It starts again in a moment.");return;}
        for(int i=0;i<SEATS;i++)if(g.seats[i]==null){seat=i;break;}
        if(seat<0){Polybius.cue(p,"All four sticks are taken.");return;}
        var f=new Fighter(p.getUUID(),p.getGameProfile().getName(),18+seat*8,TOP+8+seat*13,seat%CLASSES);
        if(g.started)f.respawn=RESPAWN;
        g.seats[seat]=f;
        Polybius.cue(p,g.started?"You drop in. Left and right pick who you are before you're on the street.":g.seated()==1?"BLOCK PARTY. PRESS START. Left and right pick who you are. There are three more sticks.":"You take a stick. Left and right pick who you are.");
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.NOTE_BLOCK_BIT.value(),net.minecraft.sounds.SoundSource.BLOCKS,.6F,1.6F);
        broadcast(p.server,g);
    }
    /** The keys a reader is holding, from their screen. */
    public static void keys(ServerPlayer p,int keys){
        var g=game(p.getUUID());if(g==null)return;int seat=seat(p.getUUID());
        if((keys&LEAVE)!=0){leave(p.server,g,seat);return;}
        g.seats[seat].keys=keys;
    }
    static void leave(MinecraftServer s,Game g,int seat){
        var f=g.seats[seat];if(f==null)return;g.seats[seat]=null;
        var p=s.getPlayerList().getPlayer(f.id);if(p!=null){pay(p,f);HousePackets.send(p,PolybiusBrawlPayload.close());}
        if(g.seated()==0)GAMES.remove(g.base);
    }
    /** Prize tickets for what a fighter knocked down. */
    static void pay(ServerPlayer p,Fighter f){
        int tickets=f.score<=0?0:Math.min(40,1+f.score/250);f.score=0;if(tickets<=0)return;
        LiteraryVignettes.give(p,new ItemStack(PolybiusRegistry.TICKETS.get(),tickets));
        Polybius.cue(p,"The cabinet chatters out a strip of "+tickets+" ticket"+(tickets==1?"":"s")+".");
    }

    // ------------------------------------------------------------------------------------------------ the game
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();
        for(var g:List.copyOf(GAMES.values())){
            for(int i=0;i<SEATS;i++){var f=g.seats[i];if(f==null)continue;var p=s.getPlayerList().getPlayer(f.id);
                if(p==null||!LiteraryVignettes.inside(p,Polybius.PLACE)||p.position().distanceTo(Vec3.atCenterOf(g.base.offset(PolybiusSite.BRAWL)))>REACH)leave(s,g,i);}
            if(!GAMES.containsKey(g.base))continue;
            step(g);
            if(g.over>OVER){for(int i=0;i<SEATS;i++)leave(s,g,i);GAMES.remove(g.base);continue;}
            broadcast(s,g);
        }
    }
    static void step(Game g){
        g.tick++;if(g.banner>0)g.banner--;
        g.rings.removeIf(r->++r.age>10);
        if(g.over>=0){g.over++;return;}
        if(!g.started){
            // Left and right, pressed while the game waits, pick a class.
            for(var f:g.seats)if(f!=null){int pressed=f.keys&~f.last;
                if((pressed&LEFT)!=0)f.cls=(f.cls+CLASSES-1)%CLASSES;if((pressed&RIGHT)!=0)f.cls=(f.cls+1)%CLASSES;
                if((pressed&(LEFT|RIGHT))!=0)f.hp=f.max();f.last=f.keys;}
            if(--g.lobby<=0||g.seated()==SEATS&&g.lobby<LOBBY-20)g.started=true;return;
        }
        boolean anyone=false;for(var f:g.seats)if(f!=null&&!f.out())anyone=true;
        if(!anyone){g.over=0;return;}
        for(int i=0;i<SEATS;i++)if(g.seats[i]!=null)fighter(g,g.seats[i],i);
        if(g.foes.isEmpty()&&g.coming.isEmpty()&&!g.calm)wave(g);
        if(!g.coming.isEmpty()&&g.tick%15==0){int type=g.coming.poll();boolean left=g.random.nextInt(4)==0;g.foes.add(new Foe(type,left?-8:W+8,TOP+4+g.random.nextInt(BOTTOM-TOP-4)));}
        for(var foe:List.copyOf(g.foes))foe(g,foe);
        for(var it=g.shots.iterator();it.hasNext();){var sh=it.next();sh.x+=sh.dir*3;boolean gone=sh.x<-10||sh.x>W+10;
            for(var f:g.seats)if(!gone&&f!=null&&f.up()&&Math.abs(f.x-sh.x)<5&&Math.abs(f.y-sh.y)<6){if(f.shield>0&&f.facing==-sh.dir)gone=true;else{hurt(f,2);gone=true;}}
            if(gone)it.remove();}
        bolts(g);
        for(var it=g.drops.iterator();it.hasNext();){var d=it.next();if(++d.age>400){it.remove();continue;}
            for(var f:g.seats)if(f!=null&&f.up()&&Math.abs(f.x-d.x)<8&&Math.abs(f.y-d.y)<6){pick(f,d.type);it.remove();break;}}
    }
    static void wave(Game g){
        g.wave++;g.banner=50;int count=Math.min(12,2+g.wave+g.seated());
        for(int i=0;i<count;i++){int type=ZOMBIE;int r=g.random.nextInt(10);if(g.wave>=2&&r<3)type=SKELETON;if(g.wave>=3&&r>=8)type=CREEPER;if(g.wave>=4&&(r==3||r==4))type=SPIDER;g.coming.add(type);}
        if(g.wave%5==0)g.coming.add(ENDERMAN);
    }
    static void fighter(Game g,Fighter f,int seat){
        // Waiting to come back in (a life lost, or just dropped in), left and right pick again who to come back as.
        if(f.respawn>0){int pressed=f.keys&~f.last;
            if((pressed&LEFT)!=0)f.cls=(f.cls+CLASSES-1)%CLASSES;if((pressed&RIGHT)!=0)f.cls=(f.cls+1)%CLASSES;f.last=f.keys;
            if(--f.respawn==0){f.hp=f.max();f.hurt=RESPAWN;f.x=18+seat*8;}return;}
        if(f.hp<=0)return;
        int k=f.keys,pressed=k&~f.last;double pace=CLASS_SPEED[f.cls]*(f.swift>0?1.5:1);
        double dx=((k&RIGHT)!=0?pace:0)-((k&LEFT)!=0?pace:0),dy=((k&DOWN)!=0?1:0)-((k&UP)!=0?1:0);
        if(f.punch>0||f.shield>0){dx*=.3;dy*=.3;}
        if(f.dash>0){dx=f.facing*5;dy=0;f.dash--;
            for(var foe:List.copyOf(g.foes))if(!f.dashed.contains(foe)&&Math.abs(foe.x-f.x)<10&&Math.abs(foe.y-f.y)<9){f.dashed.add(foe);strike(g,f,foe,f.damage(6),12,10);}}
        f.x=Mth.clamp(f.x+dx,6,W-6);f.y=Mth.clamp(f.y+dy,TOP,BOTTOM);
        if(dx>0)f.facing=1;else if(dx<0)f.facing=-1;
        if((pressed&PUNCH)!=0&&f.cool==0)attack(g,f,seat);
        if((pressed&SPECIAL)!=0&&f.special==0)special(g,f,seat);
        // The Brawler and the Knight land their blow partway through the swing.
        if(f.punch==5&&(f.cls==BRAWLER||f.cls==KNIGHT))blow(g,f);
        if(f.punch>0)f.punch--;if(f.cool>0)f.cool--;if(f.hurt>0)f.hurt--;if(f.special>0)f.special--;if(f.shield>0)f.shield--;
        if(f.swift>0)f.swift--;if(f.strong>0)f.strong--;
        if(f.chain>0&&--f.chain==0)f.combo=0;
        f.last=k;
    }
    /** Whether this attack is a finisher: the third hit of a chain, and every third after. */
    static boolean finishing(Fighter f){return f.combo>=2&&f.combo%3==2;}
    static void attack(Game g,Fighter f,int seat){
        if(f.tnt>0){f.tnt--;g.bolts.add(new Bolt(THROWN_TNT,seat,f.x,f.y,f.facing,f.damage(12),false,f.x+f.facing*34,24));f.cool=14;f.punch=8;return;}
        boolean fin=finishing(f);f.finisher=fin?8:0;
        switch(f.cls){
            case KNIGHT->{f.punch=8;f.cool=15;}
            case ARCHER->{
                Foe close=null;for(var foe:g.foes){double ahead=(foe.x-f.x)*f.facing;if(ahead>=0&&ahead<11&&Math.abs(foe.y-f.y)<8)close=foe;}
                if(close!=null){strike(g,f,close,f.damage(3),12,0);f.punch=6;f.cool=10;}
                else{g.bolts.add(new Bolt(ARROW,seat,f.x+f.facing*6,f.y,f.facing,f.damage(fin?6:4),fin,0,0));f.punch=5;f.cool=10;}
            }
            case ALCHEMIST->{
                double target=f.x+f.facing*40;for(var foe:g.foes){double ahead=(foe.x-f.x)*f.facing;if(ahead>0&&ahead<50&&Math.abs(foe.y-f.y)<10){target=foe.x;break;}}
                g.bolts.add(new Bolt(POTION,seat,f.x,f.y,f.facing,f.damage(fin?7:4),fin,target,12));f.punch=6;f.cool=16;
            }
            default->{f.punch=8;f.cool=11;}
        }
    }
    /** The Brawler's punch and the Knight's sword, landing; a finisher is an uppercut, or a spin that cuts both ways. */
    static void blow(Game g,Fighter f){
        boolean fin=f.finisher>0;int reach=f.cls==KNIGHT?26:20;
        for(var foe:List.copyOf(g.foes)){double ahead=(foe.x-f.x)*f.facing;boolean spin=fin&&f.cls==KNIGHT;
            if((spin?Math.abs(foe.x-f.x)<=reach:ahead>=0&&ahead<=reach)&&Math.abs(foe.y-f.y)<9){
                int dmg=f.cls==KNIGHT?(fin?8:6):(fin?9:5);strike(g,f,foe,f.damage(dmg),fin?18:7,fin?20:0);}}
    }
    static void special(Game g,Fighter f,int seat){
        f.special=SPECIAL_WAIT[f.cls];
        switch(f.cls){
            case KNIGHT->f.shield=40;
            case ARCHER->{for(int dy:new int[]{-6,0,6})g.bolts.add(new Bolt(ARROW,seat,f.x+f.facing*6,f.y+dy,f.facing,f.damage(4),false,0,0));f.punch=6;}
            case ALCHEMIST->{
                g.rings.add(new Ring(f.x,f.y,30,1));
                for(var o:g.seats)if(o!=null&&o.up()&&Math.hypot(o.x-f.x,(o.y-f.y)*1.5)<30)o.hp=Math.min(o.max(),o.hp+6);
            }
            default->{f.dash=6;f.dashed.clear();}
        }
    }
    /** A foe struck by a fighter: hurt, pushed, maybe stunned, and on going down scored with the chain and maybe dropping something. */
    static void strike(Game g,Fighter f,Foe foe,int dmg,double push,int stun){
        if(!g.foes.contains(foe))return;
        foe.hp-=dmg;foe.hurt=8;foe.fuse=0;foe.x+=(foe.x>=f.x?1:-1)*push;if(stun>0)foe.stun=stun;
        f.combo++;f.chain=CHAIN;
        if(foe.hp<=0){g.foes.remove(foe);f.score+=(int)(FOE_POINTS[foe.type]*(1+Math.min(f.combo,10)/5.0));drop(g,foe);}
    }
    static void drop(Game g,Foe foe){
        if(foe.type==ENDERMAN){g.drops.add(new Drop(GOLDEN_APPLE,foe.x,foe.y));return;}
        if(g.random.nextInt(100)>=12)return;
        int r=g.random.nextInt(100);int type=r<34?STEAK:r<48?GOLDEN_APPLE:r<63?SWIFTNESS:r<78?STRENGTH:r<97?TNT:TOTEM;
        g.drops.add(new Drop(type,Mth.clamp(foe.x,8,W-8),foe.y));
    }
    static void pick(Fighter f,int type){
        switch(type){
            case STEAK->f.hp=Math.min(f.max(),f.hp+6);
            case GOLDEN_APPLE->{f.hp=f.max();f.hurt=60;}
            case SWIFTNESS->f.swift=BUFF;
            case STRENGTH->f.strong=BUFF;
            case TNT->f.tnt=Math.min(3,f.tnt+1);
            default->f.lives++;
        }
    }
    static void bolts(Game g){
        for(var it=g.bolts.iterator();it.hasNext();){var b=it.next();var f=g.seats[b.seat];
            if(f==null){it.remove();continue;}
            if(b.kind==ARROW){b.x+=b.dir*4;
                for(var foe:List.copyOf(g.foes))if(!b.hit.contains(foe)&&Math.abs(foe.x-b.x)<5&&Math.abs(foe.y-b.y)<7){b.hit.add(foe);strike(g,f,foe,b.dmg,4,0);if(!b.pierce){b.x=-100;break;}}
                if(b.x<-10||b.x>W+10)it.remove();continue;}
            if(--b.t>0){b.x+=(b.tx-b.x)/Math.max(1,b.t);continue;}
            // Landed: a potion splashes, TNT goes up.
            int radius=b.kind==THROWN_TNT?30:b.pierce?20:14;g.rings.add(new Ring(b.tx,b.y,radius,b.kind==THROWN_TNT?2:0));
            for(var foe:List.copyOf(g.foes))if(Math.hypot(foe.x-b.tx,(foe.y-b.y)*1.5)<radius){if(b.kind==POTION)foe.slow=60;strike(g,f,foe,b.dmg,b.kind==THROWN_TNT?14:3,0);}
            it.remove();
        }
    }
    static void hurt(Fighter f,int n){
        if(f.hurt>0||!f.up())return;f.hp-=n;f.hurt=14;f.combo=0;f.chain=0;
        if(f.hp<=0){f.hp=0;f.lives--;if(f.lives>0)f.respawn=RESPAWN;}
    }
    /** A fighter struck by a foe, unless the Knight's shield is up and facing it. */
    static void hit(Fighter t,Foe foe,int n){if(t.shield>0&&t.facing==(foe.x>=t.x?1:-1))return;hurt(t,n);}
    @Nullable static Fighter nearest(Game g,Foe foe){
        Fighter best=null;double d=1e9;for(var f:g.seats)if(f!=null&&f.up()){double e=Math.abs(f.x-foe.x)+Math.abs(f.y-foe.y)*1.5;if(e<d){d=e;best=f;}}return best;
    }
    /** Which side of its fighter a foe keeps to: the one it is on (right of them if it stands exactly on them). */
    static int side(Foe foe,Fighter t){return foe.x<t.x?-1:1;}
    static void foe(Game g,Foe foe){
        if(foe.hurt>0){foe.hurt--;return;}
        if(foe.stun>0){foe.stun--;return;}
        if(foe.cool>0)foe.cool--;
        double pace=foe.slow>0?.5:1;if(foe.slow>0)foe.slow--;
        var t=nearest(g,foe);if(t==null)return;
        double dx=t.x-foe.x,dy=t.y-foe.y;foe.facing=dx>=0?1:-1;
        switch(foe.type){
            case ZOMBIE->{
                // 0.4.223: a zombie comes up beside its fighter, a fist's length off on its own side, and swings from there.
                double stand=t.x+side(foe,t)*10;
                if(Math.abs(stand-foe.x)<2&&Math.abs(dy)<4){if(foe.cool==0){hit(t,foe,2);foe.cool=26;}}
                else{if(Math.abs(stand-foe.x)>=1)foe.x+=Math.signum(stand-foe.x)*.6*pace;foe.y+=Math.signum(dy)*Math.min(.5,Math.abs(dy))*pace;foe.walk++;}
            }
            case SPIDER->{
                // Quick, low, and comes in a bound when it is close.
                double stand=t.x+side(foe,t)*9;
                if(Math.abs(stand-foe.x)<2&&Math.abs(dy)<4){if(foe.cool==0){hit(t,foe,2);foe.cool=20;}}
                else{double step=Math.abs(stand-foe.x)<24&&foe.cool==0?2.4:1.0;if(Math.abs(stand-foe.x)>=1)foe.x+=Math.signum(stand-foe.x)*step*pace;foe.y+=Math.signum(dy)*Math.min(.8,Math.abs(dy))*pace;foe.walk+=2;}
            }
            case ENDERMAN->{
                // Steps from one fighter's side to another's now and then, and hits hard from there.
                if(g.tick%80==0){var fighters=new ArrayList<Fighter>();for(var f:g.seats)if(f!=null&&f.up())fighters.add(f);
                    if(!fighters.isEmpty()){var to=fighters.get(g.random.nextInt(fighters.size()));int sideOf=g.random.nextBoolean()?1:-1;foe.x=Mth.clamp(to.x+sideOf*11,6,W-6);foe.y=to.y;
                        g.rings.add(new Ring(foe.x,foe.y,10,3));t=to;dx=t.x-foe.x;dy=t.y-foe.y;foe.facing=dx>=0?1:-1;}}
                double stand=t.x+side(foe,t)*11;
                if(Math.abs(stand-foe.x)<2&&Math.abs(dy)<4){if(foe.cool==0){hit(t,foe,4);foe.cool=30;}}
                else{if(Math.abs(stand-foe.x)>=1)foe.x+=Math.signum(stand-foe.x)*.5*pace;foe.y+=Math.signum(dy)*Math.min(.4,Math.abs(dy))*pace;foe.walk++;}
            }
            case SKELETON->{
                double want=t.x-foe.facing*55;
                if(Math.abs(want-foe.x)>3){foe.x+=Math.signum(want-foe.x)*.5*pace;foe.walk++;}
                if(Math.abs(dy)>2){foe.y+=Math.signum(dy)*.5*pace;foe.walk++;}
                if(foe.cool==0&&Math.abs(dy)<6){g.shots.add(new Shot(foe.x+foe.facing*6,foe.y,foe.facing));foe.cool=48;}
            }
            default->{
                double d=Math.hypot(dx,dy*1.5);
                if(d<16||foe.fuse>0&&d<26){if(++foe.fuse>=30){
                    g.rings.add(new Ring(foe.x,foe.y,26,2));
                    for(var f:g.seats)if(f!=null&&f.up()&&Math.hypot(f.x-foe.x,(f.y-foe.y)*1.5)<26)hit(f,foe,7);
                    g.foes.remove(foe);}}
                else{foe.fuse=Math.max(0,foe.fuse-1);double stand=t.x+side(foe,t)*9;if(Math.abs(stand-foe.x)>=1)foe.x+=Math.signum(stand-foe.x)*.5*pace;foe.y+=Math.signum(dy)*Math.min(.4,Math.abs(dy))*pace;foe.walk++;}
            }
        }
        foe.x=Mth.clamp(foe.x,-10,W+10);foe.y=Mth.clamp(foe.y,TOP,BOTTOM);
    }

    // ------------------------------------------------------------------------------------------------ what the screens are sent
    static CompoundTag state(Game g){
        var t=new CompoundTag();t.putInt("Tick",g.tick);t.putInt("Wave",g.wave);t.putInt("Banner",g.banner);t.putInt("Lobby",g.started?0:Math.max(1,g.lobby));t.putInt("Over",g.over);
        var fs=new ListTag();for(int i=0;i<SEATS;i++){var f=g.seats[i];if(f==null)continue;var c=new CompoundTag();
            c.putInt("Seat",i);c.putUUID("Id",f.id);c.putString("Name",f.name);c.putInt("X",(int)(f.x*4));c.putInt("Y",(int)(f.y*4));c.putInt("Hp",f.hp);c.putInt("Max",f.max());c.putInt("Lives",f.lives);
            c.putInt("Facing",f.facing);c.putInt("Punch",f.punch);c.putInt("Hurt",f.hurt);c.putInt("Respawn",f.respawn);c.putInt("Score",f.score);c.putBoolean("Moving",(f.keys&(UP|DOWN|LEFT|RIGHT))!=0);
            c.putInt("Class",f.cls);c.putInt("Combo",f.chain>0?f.combo:0);c.putInt("Shield",f.shield);c.putInt("Dash",f.dash);c.putInt("Swift",f.swift);c.putInt("Strong",f.strong);c.putInt("Tnt",f.tnt);
            c.putInt("Special",f.special);c.putInt("Wait",SPECIAL_WAIT[f.cls]);c.putBoolean("Finish",f.finisher>0&&f.punch>0);fs.add(c);}
        t.put("Fighters",fs);
        var es=new ListTag();for(var e:g.foes){var c=new CompoundTag();c.putInt("Type",e.type);c.putInt("X",(int)(e.x*4));c.putInt("Y",(int)(e.y*4));c.putInt("Facing",e.facing);c.putInt("Hurt",e.hurt);c.putInt("Fuse",e.fuse);c.putInt("Walk",e.walk);c.putInt("Stun",e.stun);c.putInt("Slow",e.slow);es.add(c);}
        t.put("Foes",es);
        var ss=new ListTag();for(var sh:g.shots){var c=new CompoundTag();c.putInt("X",(int)(sh.x*4));c.putInt("Y",(int)(sh.y*4));c.putInt("Dir",sh.dir);ss.add(c);}
        t.put("Shots",ss);
        var bs=new ListTag();for(var b:g.bolts){var c=new CompoundTag();c.putInt("Kind",b.kind);c.putInt("X",(int)(b.x*4));c.putInt("Y",(int)(b.y*4));c.putInt("Dir",b.dir);c.putInt("T",b.t);bs.add(c);}
        t.put("Bolts",bs);
        var ds=new ListTag();for(var d:g.drops){var c=new CompoundTag();c.putInt("Type",d.type);c.putInt("X",(int)(d.x*4));c.putInt("Y",(int)(d.y*4));c.putInt("Age",d.age);ds.add(c);}
        t.put("Drops",ds);
        var rs=new ListTag();for(var r:g.rings){var c=new CompoundTag();c.putInt("X",(int)(r.x*4));c.putInt("Y",(int)(r.y*4));c.putInt("R",r.r);c.putInt("Age",r.age);c.putInt("Kind",r.kind);rs.add(c);}
        t.put("Rings",rs);
        return t;
    }
    static void send(ServerPlayer p,Game g,int seat){HousePackets.send(p,new PolybiusBrawlPayload(seat,state(g)));}
    static void broadcast(MinecraftServer s,Game g){var t=state(g);for(int i=0;i<SEATS;i++){var f=g.seats[i];if(f==null)continue;var p=s.getPlayerList().getPlayer(f.id);if(p!=null)HousePackets.send(p,new PolybiusBrawlPayload(i,t));}}
    // ------------------------------------------------------------------------------------------------ for the tests
    /** A reader's fighter's place and score, or NaN and -1. */
    public static double fighterX(UUID id){var g=game(id);int s=seat(id);return g==null?Double.NaN:g.seats[s].x;}
    public static int score(UUID id){var g=game(id);int s=seat(id);return g==null?-1:g.seats[s].score;}
    /** A reader's fighter's class, health and chain, or -1. */
    public static int fighterClass(UUID id){var g=game(id);int s=seat(id);return g==null?-1:g.seats[s].cls;}
    public static int fighterHp(UUID id){var g=game(id);int s=seat(id);return g==null?-1:g.seats[s].hp;}
    public static int combo(UUID id){var g=game(id);int s=seat(id);return g==null?-1:g.seats[s].combo;}
    /** Skips the five seconds' wait for others. */
    public static void start(UUID id){var g=game(id);if(g!=null){g.started=true;g.lobby=0;}}
    /** A foe of the given kind, so far in front of a reader's fighter. */
    public static void foe(UUID id,int type,double ahead){var g=game(id);int s=seat(id);if(g==null)return;var f=g.seats[s];g.foes.add(new Foe(type,f.x+f.facing*ahead,f.y));}
    /** The same with its own health. */
    public static void foe(UUID id,int type,double ahead,int hp){var g=game(id);int s=seat(id);if(g==null)return;var f=g.seats[s];var e=new Foe(type,f.x+f.facing*ahead,f.y);e.hp=hp;g.foes.add(e);}
    /** The first foe's health, or -1. */
    public static int foeHp(UUID id){var g=game(id);return g==null||g.foes.isEmpty()?-1:g.foes.get(0).hp;}
    public static int lives(UUID id){var g=game(id);int s=seat(id);return g==null?-1:g.seats[s].lives;}
    /** No more waves at this reader's game, and the street cleared. */
    public static void calm(UUID id){var g=game(id);if(g==null)return;g.calm=true;g.foes.clear();g.coming.clear();g.shots.clear();g.bolts.clear();g.drops.clear();}
    /** An item lying just ahead of a reader's fighter, and how many lie there now. */
    public static void drop(UUID id,int type,double ahead){var g=game(id);int s=seat(id);if(g==null)return;var f=g.seats[s];g.drops.add(new Drop(type,f.x+f.facing*ahead,f.y));}
    public static int drops(UUID id){var g=game(id);return g==null?-1:g.drops.size();}
    /** Hurts a reader's fighter, as a foe would. */
    public static void wound(UUID id,int n){var g=game(id);int s=seat(id);if(g!=null){g.seats[s].hurt=0;hurt(g.seats[s],n);g.seats[s].hurt=0;}}
    /** Every fighter at this reader's game out of lives: the game is over. */
    public static void end(UUID id){var g=game(id);if(g==null)return;for(var f:g.seats)if(f!=null){f.lives=0;f.hp=0;f.respawn=0;}}
    @SubscribeEvent public static void left(PlayerEvent.PlayerLoggedOutEvent e){var g=game(e.getEntity().getUUID());if(g!=null){int seat=seat(e.getEntity().getUUID());g.seats[seat]=null;if(g.seated()==0)GAMES.remove(g.base);}}
}
