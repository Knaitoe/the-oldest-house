package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.LeftRightFogPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

/**
 * 0.4.205, the Left-Right Game (docs/LEFT_RIGHT_0_4_205.md): the owner's first proposal of 31 October. The road past the
 * Bristol Ferry toll forks and keeps forking; left, then right, then left, never the same way twice, and the CB says which side
 * a night starts on. The right branch carries the reader on, out of the fog and back onto the approach to the next fork; the
 * wrong one leaves them on the salt, where Marjorie stands by the crushed cars and moves only while nobody is looking at her.
 * Backing out across the salt's edge with her held in sight makes it a fork again; turning away, she is there.
 *
 * <p>The road is one, shared by everyone on it: the readers on it keep one sequence, a wrong turn by any of them takes all of
 * them to the salt, and one can hold her in sight while another backs the party out. Each reader's own record keeps their
 * nights, turns, cul-de-sacs and the account.
 *
 * <p>Four nights: the toll (four forks, the first time on the salt a lesson and not a failure); the Bluejay cut (three, the
 * second's left chained and its key in the flooding phone box down the wrong branch, which a flare on the divider lets the reader
 * walk); Jubilation (four, among doorless pastel houses whose lit dormer windows hold a gaze until the reader looks down at the
 * yellow line); the sinkhole (two, then Alice's idling wagon, whose tailgate is the end of the road). Survival.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LeftRightGame {
    private LeftRightGame(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LEFT_RIGHT;
    /** Forks each night. */
    public static final int[] JUNCTIONS={4,3,4,2};
    /** What the road is doing: a fork ahead, the salt, closed for the night, the wagon at the end of it. */
    public static final int ROAD=0,SAC=1,CLOSED=2,END=3;
    /** Her cone: held within about 27 degrees of the reader's look; turned away past about 75 degrees and she comes. */
    public static final double HELD=.887,AWAY=.25;
    /** A lit window held in the look this long at its strongest takes the reader. */
    public static final int GAZE_MAX=9,GAZE_TAKE=100;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final ResourceLocation GAZE=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"left_right_gaze");
    private static final DustParticleOptions LINE=new DustParticleOptions(new Vector3f(1F,.85F,.2F),.8F),CRIMSON=new DustParticleOptions(new Vector3f(1F,.13F,0F),1F);

    // ------------------------------------------------------------------------------------------------ records
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static CompoundTag road(MinecraftServer s){return LiteraryVignettes.shared(LabyrinthData.get(s),PLACE);}
    static void road(MinecraftServer s,CompoundTag t){LiteraryVignettes.shared(LabyrinthData.get(s),PLACE,t);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static boolean inside(ServerPlayer p){return LiteraryVignettes.inside(p,PLACE);}
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void cb(ServerPlayer p,String line){p.displayClientMessage(Component.literal(LeftRightTexts.cb(line)).withStyle(ChatFormatting.GREEN),true);p.serverLevel().playSound(null,p.blockPosition(),LeftRightRegistry.CB.get(),SoundSource.PLAYERS,.5F,1F);}
    static void sound(ServerLevel l,Vec3 at,SoundEvent s,float volume,float pitch){l.playSound(null,at.x,at.y,at.z,s,SoundSource.AMBIENT,volume,pitch);}
    /** Everyone on the road with the reader. */
    static List<ServerPlayer> group(MinecraftServer s,BlockPos b){var out=new ArrayList<ServerPlayer>();for(var q:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(q)&&inside(q)&&b.equals(base(q)))out.add(q);return out;}
    static void teleport(ServerPlayer p,BlockPos b,double x,double y,double z,float yaw,float pitch){p.teleportTo(p.serverLevel(),b.getX()+x,b.getY()+y,b.getZ()+z,yaw,pitch);p.connection.resetPosition();}

    // ------------------------------------------------------------------------------------------------ the night
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var t=road(p.server);var others=group(p.server,b);others.remove(p);
        own.putInt("Gaze205",0);own.putInt("GazeHeld205",0);
        if(!t.getBoolean("Run")||others.isEmpty()||t.getLong("Base")!=b.asLong())t=start(p.serverLevel(),b,own.getInt("Visit"));
        else cue(p,"The others are already out on the road. It is their night you are walking.");
        road(p.server,t);
        HousePackets.send(p,new LeftRightFogPayload(fogFor(t)));FOG.put(p.getUUID(),fogFor(t));
        cb(p,LeftRightTexts.radio(t.getInt("RunVisit"),t.getInt("J"),t.getInt("Expect"),dressMode(t.getInt("Mode"))));
    }
    /** A night begins at the toll: the first fork, the side it starts on, the road dressed for it. */
    static CompoundTag start(ServerLevel l,BlockPos b,int visit){
        visit=Math.max(1,Math.min(4,visit));var t=new CompoundTag();
        t.putBoolean("Run",true);t.putLong("Base",b.asLong());t.putInt("RunVisit",visit);t.putInt("J",1);t.putInt("Expect",LeftRightTexts.firstTurn(visit));t.putInt("Mode",ROAD);
        clearMarjorie(l,b,road(l.getServer()));LeftRightRoad.dress(l,b,visit,1,LeftRightRoad.JUNCTION);
        return t;
    }
    static int dressMode(int mode){return mode==CLOSED?LeftRightRoad.CLOSED:mode==END?LeftRightRoad.WAGON_END:LeftRightRoad.JUNCTION;}
    static int fogFor(CompoundTag t){return t.getInt("Mode")==SAC?2:t.getInt("RunVisit")==4?3:1;}
    /** The road empty: set back for whoever comes next. */
    static void reset(ServerLevel l,BlockPos b,CompoundTag t){
        clearMarjorie(l,b,t);var fresh=new CompoundTag();road(l.getServer(),fresh);LeftRightRoad.dress(l,b,1,1,LeftRightRoad.JUNCTION);
    }

    // ------------------------------------------------------------------------------------------------ hands
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var s=l.getBlockState(b.offset(rel));var kind=LeftRightBlock.kind(s);if(kind==null)return false;var t=road(p.server);
        int visit=t.getInt("RunVisit"),j=t.getInt("J");
        switch(kind){
            case RADIO->{cb(p,LeftRightTexts.radio(visit,j,t.getInt("Expect"),dressMode(t.getInt("Mode"))));return true;}
            case LOGBOOK->{int v=Math.max(1,own.getInt("Visit"));LiteraryVignettes.open(p,PLACE,own,"Log"+v,LeftRightTexts.log(v),b.offset(rel));return true;}
            case CRATE->{
                if(visit<2){cue(p,"A crate of road flares, empty. SECOND NIGHT is chalked on the lid.");return true;}
                if(own.getInt("FlareVisit205")!=visit){own.putInt("FlareVisit205",visit);own.putInt("FlaresTaken205",0);}
                if(own.getInt("FlaresTaken205")>=3){cue(p,"You have taken all the crate will give you tonight.");return true;}
                own.putInt("FlaresTaken205",own.getInt("FlaresTaken205")+1);LiteraryVignettes.give(p,new ItemStack(LeftRightRegistry.FLARE.get()));
                cue(p,"A road flare. Strike it on the ground.");return true;}
            case PAYPHONE->{
                if(!(visit==2&&j==2&&t.getInt("Mode")==ROAD)){cue(p,"The line is dead. Water on the floor of the box.");return true;}
                int n=Math.min(2,own.getInt("Bonnie205"));own.putInt("Bonnie205",n+1);t.putBoolean("Answered",true);road(p.server,t);
                p.displayClientMessage(Component.literal(LeftRightTexts.backwards(LeftRightTexts.bonnie(n))).withStyle(ChatFormatting.AQUA,ChatFormatting.ITALIC),true);
                rise(l,b);return true;}
            case KEY_HOOK->{
                if(s.getValue(LeftRightBlock.STAGE)!=0){cue(p,"The hook is empty.");return true;}
                l.setBlock(b.offset(rel),s.setValue(LeftRightBlock.STAGE,1),F);own.putBoolean("Key205",true);LiteraryVignettes.give(p,new ItemStack(LeftRightRegistry.KEY.get()));
                cue(p,"A brass key on a ring stamped CONDUIT.");return true;}
            case GATE->{
                if(s.getValue(LeftRightBlock.STAGE)!=0)return true;
                if(!p.getMainHandItem().is(LeftRightRegistry.KEY.get())){cue(p,"Chain-link, chained shut. A brass padlock.");return true;}
                p.getMainHandItem().shrink(1);own.putBoolean("Gate205",true);
                for(int x=-11;x<=-3;x++)for(int y=0;y<=1;y++){var at=b.offset(x,y,LeftRightRoad.GATE_Z);var g=l.getBlockState(at);if(LeftRightBlock.is(g,LeftRightBlock.Kind.GATE))l.setBlock(at,g.setValue(LeftRightBlock.STAGE,1),F);}
                sound(l,Vec3.atCenterOf(b.offset(rel)),LeftRightRegistry.GATE_SOUND.get(),1F,1F);cue(p,"The padlock turns. The gate swings back.");return true;}
            case TAILGATE->{
                if(t.getInt("Mode")!=END||s.getValue(LeftRightBlock.STAGE)!=0){cue(p,"The tailgate is down.");return true;}
                for(int x=-1;x<=1;x++){var at=b.offset(x,0,LeftRightRoad.TAILGATE.getZ());var g=l.getBlockState(at);if(LeftRightBlock.is(g,LeftRightBlock.Kind.TAILGATE))l.setBlock(at,g.setValue(LeftRightBlock.STAGE,1),F);}
                sound(l,Vec3.atCenterOf(b.offset(rel)),LeftRightRegistry.GATE_SOUND.get(),.8F,.6F);
                for(var m:group(p.server,b)){var o=m==p?own:own(m);if(o.getInt("Visit")!=4)continue;
                    o.putBoolean("Tailgate205",true);o.putString("Turns205_4",o.getString("Turns205_4"));LiteraryVignettes.ready(m,PLACE,o,"opened_the_tailgate");
                    var flare=new ItemStack(LeftRightRegistry.FLARE.get());flare.set(DataComponents.CUSTOM_NAME,Component.literal("Apollo's last flare"));
                    if(m!=p)save(m,o);LiteraryVignettes.reward(m,PLACE,o,"Flare",flare);if(m!=p)save(m,o);
                    cb(m,"Apollo: She waited for me too. Go on. The log's back at the booth. Over and out.");}
                return true;}
            case WAGON->{cue(p,"The engine is running. Exhaust in the cold, the hazards going. Nobody at the wheel.");return true;}
            case SEDAN,BARRICADE->{cue(p,"Rust through the doors. Nobody in it. The keys are in it.");return true;}
            case MARJORIE->{cue(p,"Her coat is wet through. It has not rained.");return true;}
            case SIGN_L,SIGN_R->{cue(p,"The sign gives a number for each way and no names.");return true;}
            default->{return false;}
        }
    }
    /** A flare struck in the House: on the divider at the fork, the salt is held off while it burns. */
    public static void flareLit(ServerPlayer p,BlockPos at){
        if(!inside(p))return;var b=base(p);if(b==null)return;var r=at.subtract(b);
        if(Math.abs(r.getX())<=1&&Math.abs(r.getZ()-LeftRightRoad.FORK_Z)<=1&&r.getY()>=0&&r.getY()<=1){var own=own(p);own.putInt("Burned205",own.getInt("Burned205")+1);save(p,own);cue(p,"The flare spits red on the divider. While it burns, the wrong way is only a road.");}
    }
    /** The water in the phone box comes up a step. */
    static void rise(ServerLevel l,BlockPos b){var at=b.offset(LeftRightRoad.PAYPHONE);var s=l.getBlockState(at);if(LeftRightBlock.is(s,LeftRightBlock.Kind.PAYPHONE)&&s.getValue(LeftRightBlock.STAGE)<3)l.setBlock(at,s.setValue(LeftRightBlock.STAGE,s.getValue(LeftRightBlock.STAGE)+1),F);}
    public static boolean flare(ServerLevel l,BlockPos b){
        for(int x=-1;x<=1;x++)for(int y=0;y<=1;y++)for(int z=LeftRightRoad.FORK_Z-1;z<=LeftRightRoad.FORK_Z+1;z++)if(LeftRightBlock.is(l.getBlockState(b.offset(x,y,z)),LeftRightBlock.Kind.FLARE))return true;
        return false;
    }

    // ------------------------------------------------------------------------------------------------ every other tick
    static final Map<UUID,Integer> FOG=new HashMap<>();
    /** Dormers the tests keep lit through every phase. */
    public static final Set<BlockPos> HOLD_LIT=new HashSet<>();
    private static final Set<UUID> GAZED=new HashSet<>();
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();long tick=s.getTickCount();if(tick%2!=0)return;
        BlockPos b=null;var group=new ArrayList<ServerPlayer>();
        for(var p:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(p)&&inside(p)){var pb=base(p);if(pb==null)continue;if(b==null)b=pb;if(pb.equals(b))group.add(p);}
        // Fog for those on the road, lifted for those who have left it; the gaze's drag taken off anyone not on it.
        var t=b==null?null:road(s);
        for(var it=FOG.entrySet().iterator();it.hasNext();){var en=it.next();var p=s.getPlayerList().getPlayer(en.getKey());if(p!=null&&group.contains(p))continue;if(p!=null)HousePackets.send(p,new LeftRightFogPayload(0));it.remove();}
        for(var it=GAZED.iterator();it.hasNext();){var p=s.getPlayerList().getPlayer(it.next());if(p!=null&&group.contains(p)&&t!=null&&t.getInt("RunVisit")==3)continue;if(p!=null)ungaze(p);it.remove();}
        if(b==null){emptyTick(s);return;}
        var l=group.get(0).serverLevel();
        if(!t.getBoolean("Run")||t.getLong("Base")!=b.asLong()){t=start(l,b,own(group.get(0)).getInt("Visit"));}
        t.putInt("Empty",0);
        if(tick%40==0)for(var p:group){int f=fogFor(t);HousePackets.send(p,new LeftRightFogPayload(f));FOG.put(p.getUUID(),f);}
        switch(t.getInt("Mode")){
            case ROAD->t=roadTick(s,l,b,t,group,tick);
            case SAC->t=saltTick(s,l,b,t,group);
            case END->endTick(l,b,t,tick);
            default->{}
        }
        if(t!=null)road(s,t);
    }
    /** Nobody on the road: after five seconds it is set back for the next night. */
    static void emptyTick(MinecraftServer s){
        var t=road(s);if(!t.getBoolean("Run"))return;int empty=t.getInt("Empty")+2;t.putInt("Empty",empty);
        if(empty<100){road(s,t);return;}
        var l=s.getLevel(NovelRooms.dimension(PLACE));var b=BlockPos.of(t.getLong("Base"));if(l==null||!l.isLoaded(b)){road(s,new CompoundTag());return;}
        reset(l,b,t);
    }
    static CompoundTag roadTick(MinecraftServer s,ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,long tick){
        int visit=t.getInt("RunVisit");
        for(var p:group){var r=rel(p,b);if(r.z>LeftRightRoad.COMMIT_Z)continue;int side=r.x<-2.5?0:r.x>2.5?1:-1;if(side<0||r.x>12.5)continue;
            boolean wrong=side!=t.getInt("Expect");
            if(wrong&&(!flare(l,b)||r.z<=LeftRightRoad.WRAP_Z)){toSalt(s,l,b,t,group);return t;}
            if(!wrong&&r.z<=LeftRightRoad.WRAP_Z){advance(s,l,b,t,group,side);return t;}
        }
        if(visit==2&&t.getInt("J")==2){
            if(!t.getBoolean("Answered")&&tick%40==0)sound(l,Vec3.atCenterOf(b.offset(LeftRightRoad.PAYPHONE)),LeftRightRegistry.RING.get(),1F,1F);
            if(tick%100==0)for(var p:group){var r=rel(p,b);if(r.x>2.5&&r.z<=LeftRightRoad.COMMIT_Z){rise(l,b);break;}}
        }
        if(visit==3)jubilation(l,b,t,group,tick);
        if(visit==4)for(var p:group)l.sendParticles(p,ParticleTypes.SNOWFLAKE,true,p.getX(),p.getY()+4,p.getZ(),6,6,1,6,.02);
        if(flare(l,b)&&tick%4==0)l.sendParticles(CRIMSON,b.getX()+.5,b.getY()+1.2,b.getZ()+LeftRightRoad.FORK_Z+.5,4,.6,.3,.6,0);
        return t;
    }
    /** The right way taken: the turn written down, the road carrying everyone on the branches back onto the approach, the next fork dressed. */
    static void advance(MinecraftServer s,ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,int side){
        int visit=t.getInt("RunVisit");
        for(var p:group){var o=own(p);o.putString("Turns205_"+visit,o.getString("Turns205_"+visit)+(side==0?"L":"R"));save(p,o);}
        for(var p:group){var r=rel(p,b);if(r.z>LeftRightRoad.DIVIDER_END+.5)continue;
            double x=Math.max(-4.3,Math.min(4.3,r.x+(r.x<0?7:-7))),z=Math.max(-26.5,Math.min(-14.5,r.z+36));teleport(p,b,x,Math.max(0,r.y),z,p.getYRot(),p.getXRot());}
        int j=t.getInt("J")+1;t.putInt("J",j);t.putInt("Expect",1-t.getInt("Expect"));
        int mode=j>JUNCTIONS[visit-1]?(visit==4?END:CLOSED):ROAD;t.putInt("Mode",mode);t.putBoolean("Answered",false);
        LeftRightRoad.dress(l,b,visit,j,dressMode(mode));
        if(mode==CLOSED)for(var p:group){var o=own(p);if(o.getInt("Visit")==visit&&o.getInt("Beat")<visit){o.putInt("Beat",visit);save(p,o);}}
        for(var p:group)cb(p,LeftRightTexts.radio(visit,j,t.getInt("Expect"),dressMode(mode)));
    }
    /** The wrong way taken: the road ends in salt, and she is standing by the cars. */
    static void toSalt(MinecraftServer s,ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        t.putInt("Mode",SAC);t.putInt("Step",0);placeMarjorie(l,b,t,LeftRightRoad.MARJORIE.getX(),LeftRightRoad.MARJORIE.getZ(),Direction.SOUTH);
        int i=0;for(var p:group){var o=own(p);o.putInt("Sacs205",o.getInt("Sacs205")+1);save(p,o);
            double dx=(i%2==0?1:-1)*((i+1)/2)*1.5;teleport(p,b,LeftRightRoad.SAC_ENTRY.x+dx,0,LeftRightRoad.SAC_ENTRY.z,180,0);i++;
            HousePackets.send(p,new LeftRightFogPayload(2));FOG.put(p.getUUID(),2);
            cue(p,"The road ends in salt, white as a plate, cars stacked at the far end. Someone is standing by one of them.");}
    }
    static Vec3 head(BlockPos b,CompoundTag t){return LeftRightRoad.at(b,t.getInt("MarjX")+.5,1.6,t.getInt("MarjZ")+.5);}
    static double look(ServerPlayer p,Vec3 point){var d=point.subtract(p.getEyePosition());double len=d.length();return len<.1?1:p.getLookAngle().dot(d.scale(1/len));}
    /** On the salt: still while anyone holds her in sight; nearer, a step at a time, while nobody quite does; there, the moment someone turns away. */
    static CompoundTag saltTick(MinecraftServer s,ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        var head=head(b,t);boolean held=false;ServerPlayer away=null;
        for(var p:group){double dot=look(p,head);if(dot>=HELD)held=true;else if(dot<AWAY)away=p;}
        for(var p:group){var r=rel(p,b);if(r.x>=LeftRightRoad.SAC_X0-1&&r.z>LeftRightRoad.SAC_LINE){if(held){backOut(s,l,b,t,group);return t;}return lunge(s,l,b,t,group,p);}}
        if(held){t.putInt("Step",0);return t;}
        if(away!=null)return lunge(s,l,b,t,group,away);
        int step=t.getInt("Step")+2;t.putInt("Step",step);if(step<10)return t;t.putInt("Step",0);
        ServerPlayer near=null;double best=1e9;for(var p:group){double dd=p.position().distanceToSqr(head.x,p.getY(),head.z);if(dd<best){best=dd;near=p;}}
        if(near==null)return t;
        int mx=t.getInt("MarjX"),mz=t.getInt("MarjZ");var r=rel(near,b);double dx=r.x-(mx+.5),dz=r.z-(mz+.5);
        if(Math.abs(dz)>=Math.abs(dx))mz+=dz>0?1:-1;else mx+=dx>0?1:-1;
        l.sendParticles(ParticleTypes.CLOUD,b.getX()+t.getInt("MarjX")+.5,b.getY()+.05,b.getZ()+t.getInt("MarjZ")+.5,3,.2,0,.2,0);
        placeMarjorie(l,b,t,mx,mz,Direction.getNearest(dx,0,dz));
        if(Math.hypot(r.x-(mx+.5),r.z-(mz+.5))<=1.6)return lunge(s,l,b,t,group,near);
        return t;
    }
    /** Backed out across the salt's edge with her held in sight: it is the road again, at the same fork. */
    static void backOut(MinecraftServer s,ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        clearMarjorie(l,b,t);t.putInt("Mode",ROAD);
        int i=0;for(var p:group){var o=own(p);o.putInt("Backed205",o.getInt("Backed205")+1);save(p,o);
            teleport(p,b,.5+(i%2==0?1:-1)*((i+1)/2)*1.5,0,-16.5,180,0);i++;HousePackets.send(p,new LeftRightFogPayload(1));FOG.put(p.getUUID(),1);
            cue(p,"She stays by the cars. The salt goes grey under your feet, and it is the road again, and the fork ahead.");}
    }
    /** She is there before anyone has turned their head. The first time on the first night, a lesson; after that, the night is over. */
    static CompoundTag lunge(MinecraftServer s,ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,ServerPlayer who){
        sound(l,who.getEyePosition(),LeftRightRegistry.LUNGE.get(),1.4F,1F);
        for(var p:group)HousePackets.send(p,new HouseFadePayload(2,16,24));
        if(t.getInt("RunVisit")==1&&!t.getBoolean("Taught")){
            t.putBoolean("Taught",true);clearMarjorie(l,b,t);t.putInt("Mode",ROAD);
            int i=0;for(var p:group){var o=own(p);o.putBoolean("Taught205",true);save(p,o);teleport(p,b,.5+(i%2==0?1:-1)*((i+1)/2)*1.5,0,-16.5,180,0);i++;
                HousePackets.send(p,new LeftRightFogPayload(1));FOG.put(p.getUUID(),1);
                cb(p,"Apollo: That's her. That's what happens. Next time keep your eyes on her and walk backward till the salt lets you go. Over.");}
            return t;
        }
        clearMarjorie(l,b,t);
        for(var p:group){var o=own(p);o.putInt("Lunged205",o.getInt("Lunged205")+1);save(p,o);
            HousePackets.send(p,new LeftRightFogPayload(0));FOG.remove(p.getUUID());
            cue(p,"She was there before you had turned your head.");
            if(!LabyrinthDoors.sendBack(p,4,30,30,q->{}))teleport(p,b,.5,0,-2.5,180,0);}
        var fresh=new CompoundTag();road(s,fresh);LeftRightRoad.dress(l,b,1,1,LeftRightRoad.JUNCTION);
        return null;
    }
    static void placeMarjorie(ServerLevel l,BlockPos b,CompoundTag t,int x,int z,Direction facing){
        clearMarjorie(l,b,t);
        var low=b.offset(x,0,z);if(!l.getBlockState(low).isAir()||!l.getBlockState(low.above()).isAir()){x=t.getInt("MarjX");z=t.getInt("MarjZ");low=b.offset(x,0,z);}
        if(facing.getAxis()==Direction.Axis.Y)facing=Direction.SOUTH;
        l.setBlock(low,LeftRightBlock.of(LeftRightBlock.Kind.MARJORIE,facing,0),F);l.setBlock(low.above(),LeftRightBlock.of(LeftRightBlock.Kind.MARJORIE,facing,1),F);
        t.putInt("MarjX",x);t.putInt("MarjZ",z);t.putBoolean("Marj",true);
    }
    static void clearMarjorie(ServerLevel l,BlockPos b,CompoundTag t){
        if(!t.getBoolean("Marj"))return;var low=b.offset(t.getInt("MarjX"),0,t.getInt("MarjZ"));
        for(var at:List.of(low,low.above()))if(LeftRightBlock.is(l.getBlockState(at),LeftRightBlock.Kind.MARJORIE))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        t.putBoolean("Marj",false);
    }
    /** Jubilation: the dormer windows light and darken, and a lit one in the reader's look slows them until they look down at the line. */
    static void jubilation(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group,long tick){
        var dormers=LeftRightRoad.dormers();
        if(tick%20==0){long phase=tick/100;
            for(var d:dormers){var at=b.offset(d);var st=l.getBlockState(at);if(!LeftRightBlock.is(st,LeftRightBlock.Kind.DORMER))continue;
                int h=Math.floorMod(Objects.hash(d.getX(),d.getZ(),phase),9);int stage=HOLD_LIT.contains(d)?1:h<=1?1:h==2?2:0;
                if(st.getValue(LeftRightBlock.STAGE)!=stage){l.setBlock(at,st.setValue(LeftRightBlock.STAGE,stage),F);if(stage==1)sound(l,Vec3.atCenterOf(at),LeftRightRegistry.HUM.get(),.5F,1F);}}
        }
        for(var p:group){var o=own(p);int lock=o.getInt("Gaze205");boolean seen=false;var eye=p.getEyePosition();var look=p.getLookAngle();double lh=Math.hypot(look.x,look.z);
            if(p.getXRot()<15&&lh>1e-3)for(var d:dormers){var at=b.offset(d);if(!LeftRightBlock.is(l.getBlockState(at),LeftRightBlock.Kind.DORMER)||l.getBlockState(at).getValue(LeftRightBlock.STAGE)!=1)continue;
                var c=Vec3.atCenterOf(at).subtract(eye);double ch=Math.hypot(c.x,c.z);if(ch<1||ch>28)continue;
                if((look.x*c.x+look.z*c.z)/(lh*ch)>=.94){seen=true;break;}}
            int was=lock;
            if(p.getXRot()>35)lock=Math.max(0,lock-3);else if(seen)lock=Math.min(GAZE_MAX,lock+1);else lock=Math.max(0,lock-1);
            if(lock>0&&was==0)o.putBoolean("GazeEp205",false);
            if(lock>=5&&!o.getBoolean("GazeEp205")){o.putBoolean("GazeEp205",true);o.putInt("Locks205",o.getInt("Locks205")+1);cue(p,"A window up in a roof is lit, and your legs go slow. Look down. The yellow line.");}
            int held=lock==GAZE_MAX?o.getInt("GazeHeld205")+2:0;o.putInt("GazeHeld205",held);o.putInt("Gaze205",lock);
            gaze(p,lock);
            if(lock>0){var ahead=p.position().add(look.x*3,0,look.z*3);l.sendParticles(p,LINE,true,b.getX()+(Math.abs(rel(p,b).z)>Math.abs(LeftRightRoad.DIVIDER_END)?(rel(p,b).x<0?-7:7):0)+.5,b.getY()+.1,ahead.z,3,.05,0,1.5,0);}
            if(held>=GAZE_TAKE){o.putInt("Taken205",o.getInt("Taken205")+1);o.putInt("Gaze205",0);o.putInt("GazeHeld205",0);save(p,o);ungaze(p);
                cue(p,"Somebody parted the curtain and looked back at you.");HousePackets.send(p,new HouseFadePayload(2,20,30));
                if(!LabyrinthDoors.sendBack(p,4,30,30,q->{}))teleport(p,b,.5,0,-2.5,180,0);continue;}
            save(p,o);
        }
    }
    static void gaze(ServerPlayer p,int lock){
        var attr=p.getAttribute(Attributes.MOVEMENT_SPEED);if(attr==null)return;
        if(lock<=0){attr.removeModifier(GAZE);return;}
        attr.addOrUpdateTransientModifier(new AttributeModifier(GAZE,-.1*lock,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));GAZED.add(p.getUUID());
    }
    static void ungaze(ServerPlayer p){var attr=p.getAttribute(Attributes.MOVEMENT_SPEED);if(attr!=null)attr.removeModifier(GAZE);}
    /** The end of the road: Alice's hazards blinking, the engine idling, exhaust in the cold. */
    static void endTick(ServerLevel l,BlockPos b,CompoundTag t,long tick){
        if(tick%10==0){int stage=(int)((tick/10)%2);for(var at:LeftRightRoad.wagonCells()){var p=b.offset(at);var s=l.getBlockState(p);if(LeftRightBlock.is(s,LeftRightBlock.Kind.WAGON)&&s.getValue(LeftRightBlock.STAGE)!=stage)l.setBlock(p,s.setValue(LeftRightBlock.STAGE,stage),F);}}
        if(tick%40==0)sound(l,LeftRightRoad.at(b,.5,.5,LeftRightRoad.WAGON_Z0),LeftRightRegistry.WAGON_SOUND.get(),.8F,1F);
        l.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,b.getX()+1.3,b.getY()+.3,b.getZ()+LeftRightRoad.TAILGATE.getZ()+.3,1,.05,.02,.05,.005);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){ungaze(p);GAZED.remove(p.getUUID());FOG.remove(p.getUUID());}}
    public static void clearAll(){FOG.clear();GAZED.clear();HOLD_LIT.clear();}
}
