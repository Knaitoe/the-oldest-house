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
 * the zombies, skeletons and creepers that come at them in waves. One game per arcade runs on the server, twenty steps a second;
 * each player's screen ({@code client/PolybiusBrawlScreen}) sends only which keys are held and draws what the server sends back.
 *
 * <p>A reader joins by using the cabinet. The first to join opens five seconds for the others; anyone may drop in while it runs,
 * into an empty seat. Three lives each; a fighter who loses one comes back after two seconds, tinted (nothing flashes). When every seated fighter is
 * out of lives the game is over: each is paid in prize tickets for what they knocked down, and the screen goes back to its attract
 * mode. Walking away from the cabinet, or closing the screen, gives up the seat.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PolybiusBrawl {
    private PolybiusBrawl(){}
    public static final int SEATS=4,W=200,TOP=84,BOTTOM=138,LOBBY=100,OVER=80,LIVES=3,HP=20,RESPAWN=40,REACH=5;
    public static final int ZOMBIE=0,SKELETON=1,CREEPER=2;
    public static final int UP=1,DOWN=2,LEFT=4,RIGHT=8,PUNCH=16,LEAVE=32;
    /** What each kind of foe takes to knock down, and is worth. */
    static final int[] FOE_HP={10,8,6},FOE_POINTS={100,150,200};

    static final class Fighter {
        final UUID id;final String name;double x,y;int hp=HP,lives=LIVES,facing=1,punch,cool,hurt,respawn,score,keys,last;
        Fighter(UUID id,String name,double x,double y){this.id=id;this.name=name;this.x=x;this.y=y;}
        boolean out(){return lives<=0&&hp<=0&&respawn==0;}
        boolean up(){return hp>0&&respawn==0;}
    }
    static final class Foe {int type,hp,facing=-1,hurt,fuse,cool,walk;double x,y;Foe(int type,double x,double y){this.type=type;this.x=x;this.y=y;hp=FOE_HP[type];}}
    static final class Shot {double x,y;int dir;Shot(double x,double y,int dir){this.x=x;this.y=y;this.dir=dir;}}
    static final class Game {
        final BlockPos base;final Fighter[] seats=new Fighter[SEATS];final List<Foe> foes=new ArrayList<>();final List<Shot> shots=new ArrayList<>();
        final Deque<Integer> coming=new ArrayDeque<>();final Random random=new Random();int tick,lobby=LOBBY,wave,banner,over=-1;boolean started;
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
        var f=new Fighter(p.getUUID(),p.getGameProfile().getName(),18+seat*8,TOP+8+seat*13);
        if(g.started)f.hurt=RESPAWN;
        g.seats[seat]=f;
        Polybius.cue(p,g.started?"You drop in. Your own face looks back at you off the screen, in its own colours.":g.seated()==1?"BLOCK PARTY. PRESS START. There are three more sticks.":"You take a stick. The screen puts you in beside the others.");
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
        if(g.over>=0){g.over++;return;}
        if(!g.started){if(--g.lobby<=0||g.seated()==SEATS&&g.lobby<LOBBY-20)g.started=true;return;}
        boolean anyone=false;for(var f:g.seats)if(f!=null&&!f.out())anyone=true;
        if(!anyone){g.over=0;return;}
        for(var f:g.seats)if(f!=null)fighter(g,f);
        if(g.foes.isEmpty()&&g.coming.isEmpty())wave(g);
        if(!g.coming.isEmpty()&&g.tick%15==0){int type=g.coming.poll();boolean left=g.random.nextInt(4)==0;g.foes.add(new Foe(type,left?-8:W+8,TOP+4+g.random.nextInt(BOTTOM-TOP-4)));}
        for(var foe:List.copyOf(g.foes))foe(g,foe);
        for(var it=g.shots.iterator();it.hasNext();){var sh=it.next();sh.x+=sh.dir*3;boolean gone=sh.x<-10||sh.x>W+10;
            for(var f:g.seats)if(!gone&&f!=null&&f.up()&&Math.abs(f.x-sh.x)<5&&Math.abs(f.y-sh.y)<6){hurt(f,2);gone=true;}
            if(gone)it.remove();}
    }
    static void wave(Game g){
        g.wave++;g.banner=50;int count=Math.min(12,2+g.wave+g.seated());
        for(int i=0;i<count;i++){int type=ZOMBIE;int r=g.random.nextInt(10);if(g.wave>=2&&r<3)type=SKELETON;if(g.wave>=3&&r>=8)type=CREEPER;g.coming.add(type);}
    }
    static void fighter(Game g,Fighter f){
        if(f.respawn>0){if(--f.respawn==0){f.hp=HP;f.hurt=RESPAWN;f.x=18;}return;}
        if(f.hp<=0)return;
        int k=f.keys;double dx=((k&RIGHT)!=0?1.6:0)-((k&LEFT)!=0?1.6:0),dy=((k&DOWN)!=0?1:0)-((k&UP)!=0?1:0);
        if(f.punch>0){dx*=.3;dy*=.3;}
        f.x=Mth.clamp(f.x+dx,6,W-6);f.y=Mth.clamp(f.y+dy,TOP,BOTTOM);
        if(dx>0)f.facing=1;else if(dx<0)f.facing=-1;
        if((k&PUNCH)!=0&&(f.last&PUNCH)==0&&f.cool==0){f.punch=8;f.cool=11;}
        if(f.punch==5)for(var foe:List.copyOf(g.foes)){double ahead=(foe.x-f.x)*f.facing;
            if(ahead>=0&&ahead<=20&&Math.abs(foe.y-f.y)<9){foe.hp-=5;foe.hurt=8;foe.fuse=0;foe.x+=f.facing*7;if(foe.hp<=0){g.foes.remove(foe);f.score+=FOE_POINTS[foe.type];}}}
        if(f.punch>0)f.punch--;if(f.cool>0)f.cool--;if(f.hurt>0)f.hurt--;f.last=k;
    }
    static void hurt(Fighter f,int n){
        if(f.hurt>0||!f.up())return;f.hp-=n;f.hurt=14;
        if(f.hp<=0){f.hp=0;f.lives--;if(f.lives>0)f.respawn=RESPAWN;}
    }
    @Nullable static Fighter nearest(Game g,Foe foe){
        Fighter best=null;double d=1e9;for(var f:g.seats)if(f!=null&&f.up()){double e=Math.abs(f.x-foe.x)+Math.abs(f.y-foe.y)*1.5;if(e<d){d=e;best=f;}}return best;
    }
    /** Which side of its fighter a foe keeps to: the one it is on (right of them if it stands exactly on them). */
    static int side(Foe foe,Fighter t){return foe.x<t.x?-1:1;}
    static void foe(Game g,Foe foe){
        if(foe.hurt>0){foe.hurt--;return;}
        if(foe.cool>0)foe.cool--;
        var t=nearest(g,foe);if(t==null)return;
        double dx=t.x-foe.x,dy=t.y-foe.y;foe.facing=dx>=0?1:-1;
        switch(foe.type){
            case ZOMBIE->{
                // 0.4.223 (owner: "rendering is messed up"): a zombie comes up beside its fighter, a fist's length off on its own
                // side, and swings from there; walking on the diagonal it had come to stand inside them.
                double stand=t.x+side(foe,t)*10;
                if(Math.abs(stand-foe.x)<2&&Math.abs(dy)<4){if(foe.cool==0){hurt(t,2);foe.cool=26;}}
                else{if(Math.abs(stand-foe.x)>=1)foe.x+=Math.signum(stand-foe.x)*.6;foe.y+=Math.signum(dy)*Math.min(.5,Math.abs(dy));foe.walk++;}
            }
            case SKELETON->{
                double want=t.x-foe.facing*55;
                if(Math.abs(want-foe.x)>3){foe.x+=Math.signum(want-foe.x)*.5;foe.walk++;}
                if(Math.abs(dy)>2){foe.y+=Math.signum(dy)*.5;foe.walk++;}
                if(foe.cool==0&&Math.abs(dy)<6){g.shots.add(new Shot(foe.x+foe.facing*6,foe.y,foe.facing));foe.cool=48;}
            }
            default->{
                double d=Math.hypot(dx,dy*1.5);
                if(d<16||foe.fuse>0&&d<26){if(++foe.fuse>=30){
                    for(var f:g.seats)if(f!=null&&f.up()&&Math.hypot(f.x-foe.x,(f.y-foe.y)*1.5)<26)hurt(f,7);
                    g.foes.remove(foe);}}
                else{foe.fuse=Math.max(0,foe.fuse-1);double stand=t.x+side(foe,t)*9;if(Math.abs(stand-foe.x)>=1)foe.x+=Math.signum(stand-foe.x)*.5;foe.y+=Math.signum(dy)*Math.min(.4,Math.abs(dy));foe.walk++;}
            }
        }
        foe.x=Mth.clamp(foe.x,-10,W+10);foe.y=Mth.clamp(foe.y,TOP,BOTTOM);
    }

    // ------------------------------------------------------------------------------------------------ what the screens are sent
    static CompoundTag state(Game g){
        var t=new CompoundTag();t.putInt("Tick",g.tick);t.putInt("Wave",g.wave);t.putInt("Banner",g.banner);t.putInt("Lobby",g.started?0:Math.max(1,g.lobby));t.putInt("Over",g.over);
        var fs=new ListTag();for(int i=0;i<SEATS;i++){var f=g.seats[i];if(f==null)continue;var c=new CompoundTag();
            c.putInt("Seat",i);c.putUUID("Id",f.id);c.putString("Name",f.name);c.putInt("X",(int)(f.x*4));c.putInt("Y",(int)(f.y*4));c.putInt("Hp",f.hp);c.putInt("Lives",f.lives);
            c.putInt("Facing",f.facing);c.putInt("Punch",f.punch);c.putInt("Hurt",f.hurt);c.putInt("Respawn",f.respawn);c.putInt("Score",f.score);c.putBoolean("Moving",(f.keys&(UP|DOWN|LEFT|RIGHT))!=0);fs.add(c);}
        t.put("Fighters",fs);
        var es=new ListTag();for(var e:g.foes){var c=new CompoundTag();c.putInt("Type",e.type);c.putInt("X",(int)(e.x*4));c.putInt("Y",(int)(e.y*4));c.putInt("Facing",e.facing);c.putInt("Hurt",e.hurt);c.putInt("Fuse",e.fuse);c.putInt("Walk",e.walk);es.add(c);}
        t.put("Foes",es);
        var ss=new ListTag();for(var sh:g.shots){var c=new CompoundTag();c.putInt("X",(int)(sh.x*4));c.putInt("Y",(int)(sh.y*4));c.putInt("Dir",sh.dir);ss.add(c);}
        t.put("Shots",ss);
        return t;
    }
    static void send(ServerPlayer p,Game g,int seat){HousePackets.send(p,new PolybiusBrawlPayload(seat,state(g)));}
    static void broadcast(MinecraftServer s,Game g){var t=state(g);for(int i=0;i<SEATS;i++){var f=g.seats[i];if(f==null)continue;var p=s.getPlayerList().getPlayer(f.id);if(p!=null)HousePackets.send(p,new PolybiusBrawlPayload(i,t));}}
    // ------------------------------------------------------------------------------------------------ for the tests
    /** A reader's fighter's place and score, or NaN and -1. */
    public static double fighterX(UUID id){var g=game(id);int s=seat(id);return g==null?Double.NaN:g.seats[s].x;}
    public static int score(UUID id){var g=game(id);int s=seat(id);return g==null?-1:g.seats[s].score;}
    /** Skips the five seconds' wait for others. */
    public static void start(UUID id){var g=game(id);if(g!=null){g.started=true;g.lobby=0;}}
    /** A foe of the given kind, so far in front of a reader's fighter. */
    public static void foe(UUID id,int type,double ahead){var g=game(id);int s=seat(id);if(g==null)return;var f=g.seats[s];g.foes.add(new Foe(type,f.x+f.facing*ahead,f.y));}
    /** Every fighter at this reader's game out of lives: the game is over. */
    public static void end(UUID id){var g=game(id);if(g==null)return;for(var f:g.seats)if(f!=null){f.lives=0;f.hp=0;f.respawn=0;}}
    @SubscribeEvent public static void left(PlayerEvent.PlayerLoggedOutEvent e){var g=game(e.getEntity().getUUID());if(g!=null){int seat=seat(e.getEntity().getUUID());g.seats[seat]=null;if(g.seated()==0)GAMES.remove(g.base);}}
}
