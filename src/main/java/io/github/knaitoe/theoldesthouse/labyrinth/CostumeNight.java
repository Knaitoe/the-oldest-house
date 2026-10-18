package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Costume Night (0.4.103). Halloween on Proofrock's town beach, and the town's Lake Witch costume contest: twelve
 * numbered entrants, every one of them dressed as her, and old Hank judging from the stage, as he has every year since he
 * was one of the boys at the shallows. The thirteenth witch has no number. She moves only while no living reader can see
 * her. Hank gives the ribbon to "the one down by the water"; nothing comes up for it; the bulbs pop and go dark, the
 * contestants drop to all fours for the crawl, and now she hunts. The way out is the town park's grass, reached with her
 * in sight.
 *
 * One shared clock counts while anyone is here; each reader's sightings and crossing are their own. The scene is built
 * whole here; the polish, composition and review passes leave it alone. New builds only.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CostumeNight {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.COSTUME_NIGHT;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** Occupied ticks: the first call, the time between calls and the ribbon; then the ribbon's beats up to the lights going out. */
    public static final int FIRST_CALL=120,CALL_EVERY=240,RIBBON=900,LIGHTS_OUT=260,EMPTY_RESET=100,RADIO_LOOP=240;
    /** The actors' all-fours gesture. */
    public static final int CRAWL=5;
    /** The park's grass begins north of this line. */
    public static final int PARK_EDGE=-49;
    public static final BlockPos WITCH_START=new BlockPos(15,0,-24);
    /** Where she waits at the water's edge, each just off the sand. */
    public static final List<BlockPos> LURKS=List.of(new BlockPos(14,0,-16),new BlockPos(15,0,-24),new BlockPos(14,0,-32),new BlockPos(15,0,-40));
    /** Where a called entrant stands to be seen, in front of the stage steps. */
    public static final Vec3 STAGE_FRONT=new Vec3(-2.5,0,-23.5);
    public static final BlockPos RADIO=new BlockPos(-13,1,-21),APPLES=new BlockPos(-15,0,-26),CANDY=new BlockPos(5,1,-22),BONFIRE=new BlockPos(-11,0,-19);
    private CostumeNight(){}

    // ------------------------------------------------------------------------------------------------ the people
    /** Hank or an entrant: their number (0 for Hank), skin (a stranger phase), place, facing, and whether they do the crawl. */
    public record Person(String key,String name,int number,int phase,double x,double y,double z,float yaw,boolean crawls,String call,String[] lines){
        public Vec3 home(BlockPos b){return new Vec3(b.getX()+x,b.getY()+y,b.getZ()+z);}
    }
    public static final Person HANK=new Person("Hank","Hank",0,21,-2.5,1,-28.5,0,false,"",new String[]{"Twelve this year. Best turnout in a while.","Don't crowd the fire, kids.","Candy's for the contestants. Judges too, I suppose."});
    public static final List<Person> ENTRANTS=List.of(
        new Person("Tina","Tina",1,22,-7.5,0,-16.5,180,false,"Garbage bags! Very authentic. Very wet-looking.",new String[]{"It's garbage bags. My mom said I'd suffocate.","Number one. I got here first."}),
        new Person("Danny","Danny",2,23,-12.5,0,-24.5,270,true,"On all fours, look at him go. Mind the fire, Danny.",new String[]{"I'm doing the crawl. She goes like this.","Don't step on me, I'm the witch."}),
        new Person("Ron","Mr. Askew",3,24,3.5,0,-19.5,180,false,"Ron's mask came from the hardware store. We can tell, Ron.",new String[]{"The kids made me do it. I'm number three.","This mask is hot. I can't feel my face."}),
        new Person("Kaylee","Kaylee",4,25,-1.5,0,-21.5,0,true,"Green hair! She was never green, sweetheart. Doesn't matter.",new String[]{"I'm green because she lives in the lake.","Watch me crawl! Watch!"}),
        new Person("Marcus","Marcus",5,26,9.5,0,-23.5,90,true,"That's real weed off the dock. Smell it from here.",new String[]{"This is real weed. I pulled it off the dock.","I'm going to win. I've been practicing."}),
        new Person("Shelly","Shelly",6,27,11.5,0,-17.5,90,false,"Shelly does this every year. Every year she holds so still.",new String[]{"I've done her every year since I was nine.","You hold still and they get scared. That's all it is."}),
        new Person("Owen","Owen",7,28,-6.5,0,-12.5,180,false,"A cardboard mask. Drew the teeth himself. Good teeth.",new String[]{"I drew the teeth.","Is the real one here? My dad says she isn't real."}),
        new Person("Brooke","Brooke",8,29,7.5,0,-21.5,90,true,"A mop on her head and she still crawls better than Marcus.",new String[]{"It's a mop. Don't tell my mom.","Mr. Hank always picks somebody weird."}),
        new Person("Wes","Wes",9,30,-10.5,0,-26.5,270,false,"Hood up, hair stapled in. Lazy, Wes. Points for the staples.",new String[]{"Hood up, done. I'm here for the candy.","Staples. It's the hair that sells it."}),
        new Person("Rosa","Rosa",10,31,12.5,0,-21.5,90,true,"Now that. That's close. That's very close, Rosa.",new String[]{"I've been in the lake. That's not water from the hose.","Shh. Nobody's supposed to know which one is me."}),
        new Person("Pip","Pip",11,32,.5,0,-14.5,180,false,"A pillowcase. Is that a ghost, Pip? It's a witch. All right.",new String[]{"I'm a witch. I'm not a ghost. I'm a WITCH.","Can I have more candy?"}),
        new Person("Lyle","Lyle",12,33,-14.5,0,-30.5,270,false,"Lyle. Lyle, you didn't shave. Nobody's scared of a beard.",new String[]{"My wife bet me twenty dollars.","I'm the prettiest witch on this beach."}));
    public static final List<Person> EVERYONE;
    static{var all=new ArrayList<Person>();all.add(HANK);all.addAll(ENTRANTS);EVERYONE=List.copyOf(all);}
    /** The four smallest entrants are children. */
    public static boolean child(int phase){return phase==23||phase==25||phase==28||phase==32;}
    public static @Nullable Person person(LiteraryActor a){if(a.role()!=LiteraryActor.STRANGER)return null;for(var p:EVERYONE)if(p.phase()==a.phase())return p;return null;}
    private static final String[] DARK={"Can't see me!","I'm the witch! I'm the witch!","Mr. Hank? Is this part of it?","Who's touching my foot?","Mom? Mom, where are you?","Somebody's all wet. Who's all wet?","It's not funny anymore."};
    private static final String[] HANK_TALK={"You must be our guest judge. Your card's on the table where you came in. Score 'em out of ten.",
        "Started this with three friends, the summer after the shallows. Figured if every kid in town dressed up as her, nobody'd be scared of the real thing.",
        "I give the ribbon to whoever's most like her. Every year. Somebody has to."};
    private static final String[] HANK_DARK={"Keep your eyes on it. Whatever you do, don't you look away from it.","Get to the park. She never would cross the grass."};

    // ------------------------------------------------------------------------------------------------ helpers
    private static @Nullable LiteraryActor cast(ServerLevel l,CompoundTag w,Person who){return w.hasUUID("Cast"+who.key())&&l.getEntity(w.getUUID("Cast"+who.key())) instanceof LiteraryActor a?a:null;}
    public static @Nullable LiteraryActor cast(ServerLevel l,Person who){return cast(l,LiteraryVignettes.shared(LabyrinthData.get(l.getServer()),PLACE),who);}
    public static @Nullable LakeWitchEntity witch(ServerLevel l){var w=LiteraryVignettes.shared(LabyrinthData.get(l.getServer()),PLACE);return w.hasUUID("Witch")&&l.getEntity(w.getUUID("Witch")) instanceof LakeWitchEntity e&&e.isAlive()?e:null;}
    private static void turn(LivingEntity a,float yaw){a.setYRot(yaw);a.setYHeadRot(yaw);a.yBodyRot=yaw;a.yRotO=yaw;}
    private static void say(LiteraryActor a,String line){a.say(line);a.getPersistentData().putLong("Said0503",a.level().getGameTime());}
    /** Said to the readers near enough to hear it; Hank's megaphone carries across the beach. */
    private static void speak(LiteraryActor a,Person who,String line,List<ServerPlayer> readers,double reach){say(a,line);for(var p:readers)if(p.distanceToSqr(a)<reach*reach)p.displayClientMessage(Component.literal(who.name()+": “"+line+"”"),false);}
    private static void announce(ServerLevel l,LiteraryActor hank,String line,List<ServerPlayer> readers){
        l.playSound(null,hank.getX(),hank.getY()+1.5,hank.getZ(),CostumeNightRegistry.MEGAPHONE.get(),SoundSource.NEUTRAL,1.4F,1);speak(hank,HANK,line,readers,48);
    }
    /** Whether the lights are out and she is hunting, for the scene at this base. */
    public static boolean hunting(ServerLevel l,BlockPos base){return LiteraryVignettes.shared(LabyrinthData.get(l.getServer()),PLACE).getBoolean("LightsOut");}
    /** Where she waits while she is not after anyone: one place at the water's edge at a time, moving on every fifteen seconds. */
    public static BlockPos lurk(ServerLevel l,BlockPos base,int seed){return LURKS.get((int)Math.floorMod(l.getGameTime()/300+seed,(long)LURKS.size()));}
    public static List<BlockPos> bulbs(){
        var out=new ArrayList<BlockPos>();
        for(int x=-16;x<=10;x++){out.add(new BlockPos(x,4,-13));out.add(new BlockPos(x,4,-33));}
        for(int z=-14;z>=-25;z--)out.add(new BlockPos(-3,4,z));
        return out;
    }

    // ------------------------------------------------------------------------------------------------ the shared night
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;
        var b=LabyrinthPlaces.base(origin,PLACE);var l=server.getLevel(NovelRooms.dimension(PLACE));if(b==null||l==null)return;
        var readers=server.getPlayerList().getPlayers().stream().filter(p->LiteraryVignettes.inside(p,PLACE)).toList();
        var d=LabyrinthData.get(server);var w=LiteraryVignettes.shared(d,PLACE);
        if(readers.isEmpty()){
            if(w.contains("Party")){w.putInt("Empty",w.getInt("Empty")+1);
                if(w.getInt("Empty")>=EMPTY_RESET&&l.players().stream().noneMatch(p->p.getCamera().distanceToSqr(Vec3.atCenterOf(b.offset(0,0,-35)))<70*70))reset(w);
                LiteraryVignettes.shared(d,PLACE,w);}
            else if(w.getBoolean("Settle")){if(settle(l,b,w))w.remove("Settle");LiteraryVignettes.shared(d,PLACE,w);}
            return;
        }
        w.putInt("Empty",0);int party=w.getInt("Party")+1;w.putInt("Party",party);
        settle(l,b,w);calls(l,b,w,party,readers);
        if(!w.contains("RibbonAt")&&party>=RIBBON)w.putInt("RibbonAt",party);
        if(w.contains("RibbonAt"))ribbon(l,b,w,party-w.getInt("RibbonAt"),readers);
        boolean out=w.getBoolean("LightsOut");
        if(out)dark(l,b,w,party,readers);
        if(party%20==0)lights(l,b,!out);
        if(!out&&party%RADIO_LOOP==1)l.playSound(null,b.getX()+RADIO.getX()+.5,b.getY()+RADIO.getY()+.5,b.getZ()+RADIO.getZ()+.5,CostumeNightRegistry.RADIO.get(),SoundSource.RECORDS,1.1F,1);
        if(!out&&party%200==100)idle(l,w,readers);
        // The thirteenth one drips; only someone close by hears it, or sees the water run off her.
        var witch=witch(l);
        if(witch!=null&&party%20==0){
            l.sendParticles(ParticleTypes.DRIPPING_WATER,witch.getX(),witch.getY()+.7,witch.getZ(),2,.25,.1,.25,0);
            if(readers.stream().anyMatch(p->p.distanceToSqr(witch)<64))l.playSound(null,witch.getX(),witch.getY()+.5,witch.getZ(),CostumeNightRegistry.DRIP.get(),SoundSource.NEUTRAL,.6F,.9F+l.random.nextFloat()*.2F);
        }
        // Lines fade from over their heads after a few seconds.
        if(party%10==0)for(var who:EVERYONE){var a=cast(l,w,who);if(a!=null&&a.isCustomNameVisible()&&l.getGameTime()-a.getPersistentData().getLong("Said0503")>100)a.setCustomNameVisible(false);}
        LiteraryVignettes.shared(d,PLACE,w);
    }
    /** The night starts over for whoever comes next: the lights back on, everyone in their places, her at the water. */
    private static void reset(CompoundTag w){
        for(var k:List.of("Party","Empty","RibbonAt","LightsOut","Calls","Called","CallLeg","CallTicks","Lines"))w.remove(k);w.putInt("Gen",w.getInt("Gen")+1);w.putBoolean("Settle",true);
    }
    /** Puts each loaded piece back as the current night needs it; true once everything is in place. */
    private static boolean settle(ServerLevel l,BlockPos b,CompoundTag w){
        int gen=w.getInt("Gen");boolean all=true,out=w.getBoolean("LightsOut");
        for(var who:EVERYONE){var a=cast(l,w,who);if(a==null){all&=!w.hasUUID("Cast"+who.key());continue;}if(a.getPersistentData().getInt("Gen0503")==gen)continue;
            home(a,who,b,out);a.getPersistentData().putInt("Gen0503",gen);}
        var witch=witch(l);if(witch==null&&w.hasUUID("Witch"))all=false;
        if(witch!=null&&witch.getPersistentData().getInt("Gen0503")!=gen&&!out){var at=Vec3.atBottomCenterOf(b.offset(WITCH_START));witch.moveTo(at.x,witch.getY(),at.z,90,0);witch.getPersistentData().putInt("Gen0503",gen);}
        if(!out)all&=lights(l,b,true);
        return all;
    }
    private static void home(LiteraryActor a,Person who,BlockPos b,boolean dark){
        a.setInvisible(false);a.setCustomNameVisible(false);a.gesture(who!=HANK&&(dark||who.crawls())?CRAWL:0);
        var at=who.home(b);a.moveTo(at.x,at.y,at.z,who.yaw(),0);turn(a,who.yaw());a.setDeltaMovement(Vec3.ZERO);
    }
    /** Turns every loaded bulb (and the radio) on or off; true once all are as asked. */
    private static boolean lights(ServerLevel l,BlockPos b,boolean on){
        boolean all=true;var points=new ArrayList<>(bulbs());points.add(RADIO);
        for(var rel:points){var at=b.offset(rel);if(!l.hasChunkAt(at)){all=false;continue;}var s=l.getBlockState(at);
            if(s.is(CostumeNightRegistry.PROP.get())&&s.getValue(CostumePropBlock.LIT)!=on)l.setBlock(at,s.setValue(CostumePropBlock.LIT,on),Block.UPDATE_CLIENTS);}
        return all;
    }
    /** Hank calls the numbers up one at a time; each walks (or crawls) to the front of the stage, shows off and goes back. */
    private static void calls(ServerLevel l,BlockPos b,CompoundTag w,int party,List<ServerPlayer> readers){
        var hank=cast(l,w,HANK);
        if(!w.contains("Called")){
            if(w.contains("RibbonAt")||party<FIRST_CALL||(party-FIRST_CALL)%CALL_EVERY!=0)return;
            int n=w.getInt("Calls");var who=ENTRANTS.get(n%ENTRANTS.size());w.putInt("Calls",n+1);w.putInt("Called",n%ENTRANTS.size());w.putInt("CallLeg",1);w.putInt("CallTicks",0);
            if(hank!=null)announce(l,hank,"Number "+who.number()+"! "+who.name()+"! Come on up and show us!",readers);return;
        }
        var who=ENTRANTS.get(w.getInt("Called"));var a=cast(l,w,who);if(a==null||w.getBoolean("LightsOut")){w.remove("Called");return;}
        int leg=w.getInt("CallLeg"),t=w.getInt("CallTicks")+1;w.putInt("CallTicks",t);double speed=who.crawls()?.06:.1;
        if(leg==1){if(ElkLotEnding.step(a,Vec3.atLowerCornerOf(b).add(STAGE_FRONT),speed)||t>400){w.putInt("CallLeg",2);w.putInt("CallTicks",0);turn(a,0);if(!who.crawls())a.gesture(4);if(hank!=null)announce(l,hank,who.call(),readers);}}
        else if(leg==2){if(t>=60){w.putInt("CallLeg",3);w.putInt("CallTicks",0);a.gesture(who.crawls()?CRAWL:0);if(!w.contains("RibbonAt"))l.playSound(null,a.getX(),a.getY()+1,a.getZ(),CostumeNightRegistry.CHEER.get(),SoundSource.NEUTRAL,.7F,1.1F);}}
        else if(ElkLotEnding.step(a,who.home(b),speed)||t>400){w.remove("Called");turn(a,who.yaw());}
    }
    /** The ribbon goes to the one by the water. Nothing comes up for it, and the lights go. */
    private static void ribbon(ServerLevel l,BlockPos b,CompoundTag w,int t,List<ServerPlayer> readers){
        var hank=cast(l,w,HANK);
        if(hank!=null)switch(t){
            case 0->announce(l,hank,"All right, folks. All right. The judges have decided.",readers);
            case 60->announce(l,hank,"This year's ribbon for Best Lake Witch goes to...",readers);
            case 110->{announce(l,hank,"...the one down by the water! Come on up, honey!",readers);l.playSound(null,b.getX()-2.5,b.getY()+1,b.getZ()-20,CostumeNightRegistry.CHEER.get(),SoundSource.NEUTRAL,1.2F,1);}
            case 170->announce(l,hank,"Come on, now. Don't be shy.",readers);
            case 230->speak(hank,HANK,"Who... who is that?",readers,24);
            case LIGHTS_OUT+70->announce(l,hank,HANK_DARK[0],readers);
            case LIGHTS_OUT+160->announce(l,hank,HANK_DARK[1],readers);
            default->{}
        }
        if(t==LIGHTS_OUT&&!w.getBoolean("LightsOut")){
            w.putBoolean("LightsOut",true);lights(l,b,false);
            l.playSound(null,b.getX()-3,b.getY()+4,b.getZ()-23,CostumeNightRegistry.POP.get(),SoundSource.BLOCKS,2.5F,1);
            l.playSound(null,b.getX()-3,b.getY()+1,b.getZ()-22,CostumeNightRegistry.GIGGLE.get(),SoundSource.NEUTRAL,1.2F,1);
            for(var who:ENTRANTS){var a=cast(l,w,who);if(a!=null)a.gesture(CRAWL);}
            if(hank!=null)announce(l,hank,"Everybody stay where you are! Lights'll be right back!",readers);
        }
    }
    /** In the dark every entrant crawls about their place, giggling; the thirteenth is among them. */
    private static void dark(ServerLevel l,BlockPos b,CompoundTag w,int party,List<ServerPlayer> readers){
        for(int i=0;i<ENTRANTS.size();i++){var who=ENTRANTS.get(i);var a=cast(l,w,who);if(a==null)continue;
            if(a.gesture()!=CRAWL)a.gesture(CRAWL);
            double k=Math.floorDiv(party+i*37,90)*2.1+i;var to=who.home(b).add(Math.cos(k)*2.2,0,Math.sin(k)*2.2);
            ElkLotEnding.step(a,to,.04);
        }
        if(party%160==80){int n=w.getInt("Lines");w.putInt("Lines",n+1);var who=ENTRANTS.get(n*5%ENTRANTS.size());var a=cast(l,w,who);
            if(a!=null){speak(a,who,DARK[n%DARK.length],readers,16);if(n%2==0)l.playSound(null,a.getX(),a.getY()+.5,a.getZ(),CostumeNightRegistry.GIGGLE.get(),SoundSource.NEUTRAL,.8F,.9F+n%3*.1F);}}
    }
    private static void idle(ServerLevel l,CompoundTag w,List<ServerPlayer> readers){
        int n=w.getInt("Lines");w.putInt("Lines",n+1);var who=EVERYONE.get(n%EVERYONE.size());var a=cast(l,w,who);if(a==null)return;
        if(who!=HANK&&w.contains("Called")&&ENTRANTS.get(w.getInt("Called"))==who)return;
        if(readers.stream().noneMatch(p->p.distanceToSqr(a)<16*16))return;
        say(a,who.lines()[(n/EVERYONE.size())%who.lines().length]);
    }

    // ------------------------------------------------------------------------------------------------ each reader
    public static void arrive(ServerPlayer p,CompoundTag own){
        for(var k:List.of("Noticed0503","SeenHer0503","Crossing0503","ParkHint0503","CrossHint0503","LookHint0503"))own.remove(k);
        own.putBoolean("Shallows0503",IndianLakeProgress.hasThrown(LabyrinthData.get(p.server),p.getUUID()));
    }
    /** Every five ticks for each reader here: everyone exists, once; what this reader saw of her; the park. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LakeWitchEntity witch){
        var d=LabyrinthData.get(p.server);var w=LiteraryVignettes.shared(d,PLACE);boolean out=w.getBoolean("LightsOut");
        for(var who:EVERYONE){
            var a=LiteraryVignettes.actor(p,PLACE,"Cast"+who.key(),LiteraryActor.STRANGER,BlockPos.containing(who.x(),who.y(),who.z()),false);
            if(a==null||a.phase()==who.phase())continue;
            a.appearance(LiteraryActor.STRANGER,who.phase());a.getPersistentData().putInt("Gen0503",LiteraryVignettes.shared(d,PLACE).getInt("Gen"));home(a,who,b,out);
        }
        if(p.isSpectator())return;
        if(witch!=null&&HouseWatchers.sees(p,witch.getEyePosition())){if(out)own.putBoolean("SeenHer0503",true);else if(p.distanceTo(witch)<14)own.putBoolean("Noticed0503",true);}
        boolean grass=p.serverLevel().getBlockState(p.blockPosition().below()).is(Blocks.GRASS_BLOCK);double z=p.getZ()-b.getZ();
        if(out&&!grass&&z>PARK_EDGE&&z<-10)own.putBoolean("Crossing0503",true);
        if(!grass||z>=PARK_EDGE||own.getBoolean("Ready"))return;
        if(out&&own.getBoolean("SeenHer0503")&&own.getBoolean("Crossing0503")){
            LiteraryVignettes.ready(p,PLACE,own,"reached_living_grass_beyond_the_costumes");
            var hank=cast(p.serverLevel(),w,HANK);if(hank!=null)speak(hank,HANK,"You made it. Good. You stay right there.",List.of(p),80);
        }
        else if(!out)hint(p,own,"ParkHint0503","The contest isn't over. Hank hasn't given the ribbon yet.");
        else if(!own.getBoolean("Crossing0503"))hint(p,own,"CrossHint0503","You were already on the grass when the lights went. Go back onto the sand and cross with her in sight.");
        else hint(p,own,"LookHint0503","Which one was she? Look back across the sand.");
    }
    private static void hint(ServerPlayer p,CompoundTag own,String key,String text){if(own.getBoolean(key))return;own.putBoolean(key,true);p.displayClientMessage(Component.literal(text),true);}
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var who=person(a);if(who==null)return;var w=LiteraryVignettes.shared(LabyrinthData.get(p.server),PLACE);boolean out=w.getBoolean("LightsOut");
        int n=own.getInt("Talk_"+who.key());own.putInt("Talk_"+who.key(),n+1);
        String line=who==HANK?(out?HANK_DARK[n%HANK_DARK.length]:HANK_TALK[Math.min(n,HANK_TALK.length-1)])
                :out?DARK[(n+who.number())%DARK.length]:n==0?"Number "+who.number()+". "+who.lines()[0]:who.lines()[n%who.lines().length];
        if(who==HANK&&!out&&n>=1)own.putBoolean("HankStory0503",true);
        // 0.4.104: a reader who did the dare that winter is someone he remembers.
        if(who==HANK&&!out&&n==0&&WitnessAccount.has(LabyrinthData.get(p.server),p.getUUID(),WitnessAccount.Story.WINTER_LAKE)){
            line="You. You took it back out to her, that winter. I never could. It was back on my table by June, dry. Your card's on the table where you came in.";own.putBoolean("HankDare0503",true);}
        speak(a,who,line,List.of(p),24);
    }
    /** The apple tub, the candy bowl and the radio. One apple and one sweet to each reader, ever. */
    public static void use(ServerPlayer p,CostumePropBlock.Kind kind,BlockPos at){
        if(!LiteraryVignettes.inside(p,PLACE)||p.isSpectator())return;var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
        boolean out=LiteraryVignettes.shared(d,PLACE).getBoolean("LightsOut");
        switch(kind){
            case APPLE_TUB->{if(own.getBoolean("Apple0503")){p.displayClientMessage(Component.literal("The apples left bob just out of reach."),true);return;}
                own.putBoolean("Apple0503",true);give(p,new ItemStack(Items.APPLE));p.displayClientMessage(Component.literal("Third try. Your hair is soaked."),true);}
            case CANDY_BOWL->{if(own.getBoolean("Candy0503")){p.displayClientMessage(Component.literal("The sign says take one."),true);return;}
                own.putBoolean("Candy0503",true);var sweet=new ItemStack(Items.COOKIE);sweet.set(DataComponents.CUSTOM_NAME,Component.literal("Candy from the contest bowl"));give(p,sweet);
                p.displayClientMessage(Component.literal("The sign says take one. You take one."),true);}
            case RADIO->p.displayClientMessage(Component.literal(out?"Dead. The power went with the lights.":"A surf song from the station in town, so old it sounds like it's under water."),true);
            default->{}
        }
        LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
    }
    private static void give(ServerPlayer p,ItemStack stack){if(!p.getInventory().add(stack))p.drop(stack,false);}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void struck(LivingIncomingDamageEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getSource().getEntity() instanceof LakeWitchEntity)||!LiteraryVignettes.inside(p,PLACE))return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("Struck0503",true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
    }

    // ------------------------------------------------------------------------------------------------ the account
    public static String account(CompoundTag own){
        var text=new StringBuilder("Proofrock came down to the beach as the Lake Witch: garbage bags, mops dyed green, a boy in a bedsheet with weeds sewn on. Twelve of them had pinned on numbers.\n\n");
        if(own.getBoolean("Noticed0503"))text.append("I counted thirteen on the sand. The thirteenth had no number, and it was wet.\n\n");
        if(own.getBoolean("HankStory0503"))text.append("Hank told me he started the contest with three friends, the summer after the shallows, so that if every child in town was her, nobody would be afraid of the real one. He gives the ribbon to whoever is most like her.\n\n");
        if(own.getBoolean("Shallows0503"))text.append("He looked at me for a long time, as if he had seen me somewhere before, at the edge of the water.\n\n");
        if(own.getBoolean("HankDare0503"))text.append("He remembered the winter I walked her ribbon out across the ice. He said he never could. It was back on his table by June, dry. It always is.\n\n");
        text.append("He gave the ribbon to the one down by the water. Nobody came up for it. He keeps every year's card, and on every one of them the winner is the same, in his own hand: the one by the water.\n\n");
        text.append("When the lights went, everyone got down on all fours, the way she goes. One of them held still whenever I looked at it. Every time I looked back, it was nearer.\n\n");
        if(own.getBoolean("Struck0503"))text.append("It reached me once, while I was looking at someone else.\n\n");
        return text.append("Then the grass was under my feet. Behind me thirteen witches were crawling in the dark, and one of them was not playing.").toString();
    }
    public static void clearAll(){}

    // ------------------------------------------------------------------------------------------------ the build
    private record Site(ServerLevel l,BlockPos b){
        void set(int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
        void set(int x,int y,int z,Block block){set(x,y,z,block.defaultBlockState());}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(x,y,z,s);}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block block){fill(x0,y0,z0,x1,y1,z1,block.defaultBlockState());}
        BlockState at(int x,int y,int z){return BuildBlocks.state(l,b.offset(x,y,z));}
    }
    private static boolean lake(int x,int z){return x>=14&&x<=30&&z<=-6&&z>=-74;}
    private static boolean park(int x,int z){return z<=-50&&!lake(x,z);}
    private static boolean woods(int x,int z){return !lake(x,z)&&(x<=-28||x>=31||z<=-76);}
    /** The beach, the lake along its east side, the town park beyond the sand, and the party between the fire and the stage. */
    public static void build(ServerLevel l,BlockPos b){
        var s=new Site(l,b);var r=PLACE.room();
        s.fill(r.minX(),-6,r.minZ(),r.maxX(),-6,r.maxZ(),Blocks.STONE);
        for(int x=r.minX();x<=r.maxX();x++)for(int z=r.minZ();z<=r.maxZ();z++){
            if(lake(x,z)){int depth=x<=15||z>=-8||z<=-72?2:4;s.fill(x,-5,z,x,-2-depth,z,Blocks.SANDSTONE);s.set(x,-1-depth,z,Blocks.SAND);s.fill(x,-depth,z,x,-1,z,Blocks.WATER);continue;}
            if(woods(x,z)){s.fill(x,-5,z,x,-2,z,Blocks.DIRT);s.set(x,-1,z,Math.floorMod(x*31+z*17,3)==0?Blocks.COARSE_DIRT:Blocks.PODZOL);continue;}
            if(park(x,z)){s.fill(x,-5,z,x,-2,z,Blocks.DIRT);s.set(x,-1,z,Blocks.GRASS_BLOCK);continue;}
            s.fill(x,-5,z,x,-2,z,Blocks.SANDSTONE);s.set(x,-1,z,Blocks.SAND);
        }
        trees(s);park(s);party(s);
    }
    private static BlockState leaves(Block b){return b.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    private static void spruce(Site s,int x,int z,int height){
        for(int h=2;h<=height+1;h++){int w=h>=height?0:Math.max(0,(height-h)/2+(h%2));for(int dx=-w;dx<=w;dx++)for(int dz=-w;dz<=w;dz++)if(Math.abs(dx)+Math.abs(dz)<=w+1&&s.at(x+dx,h,z+dz).isAir())s.set(x+dx,h,z+dz,leaves(Blocks.SPRUCE_LEAVES));}
        s.fill(x,0,z,x,height-1,z,Blocks.SPRUCE_LOG);s.set(x,height+1,z,leaves(Blocks.SPRUCE_LEAVES));
    }
    private static void oak(Site s,int x,int z,int height){
        for(int h=height-2;h<=height+1;h++){int w=h>=height+1?1:2;for(int dx=-w;dx<=w;dx++)for(int dz=-w;dz<=w;dz++){if(Math.abs(dx)==w&&Math.abs(dz)==w&&(h==height+1||(x+z+h)%2==0))continue;if(s.at(x+dx,h,z+dz).isAir())s.set(x+dx,h,z+dz,leaves(Blocks.OAK_LEAVES));}}
        s.fill(x,0,z,x,height-1,z,Blocks.OAK_LOG);
    }
    private static void trees(Site s){
        // Dark spruce along the west dunes and the far bank; nothing grows in the lake or on the sand.
        for(int z=-4;z>=-74;z-=5){spruce(s,-31,z+Math.floorMod(z,3)-1,6+Math.floorMod(z,4));if(z<=-8)spruce(s,32,z-2,5+Math.floorMod(z,3));}
        for(int x=-26;x<=26;x+=7)spruce(s,x,-76,6+Math.floorMod(x,3));
    }
    private static void park(Site s){
        // A row of low posts where the sand ends, with gaps to walk through; oaks, a bench and a lamp on the grass.
        for(int x=-27;x<=12;x+=3)s.set(x,0,PARK_EDGE,Blocks.SPRUCE_FENCE);
        for(int[] t:new int[][]{{-20,-60},{-12,-71},{8,-62},{-24,-70},{3,-72},{-6,-58}})oak(s,t[0],t[1],5+Math.floorMod(t[0]+t[1],2));
        for(int x=-4;x<=-2;x++)s.set(x,0,-63,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        s.set(-6,0,-66,Blocks.SPRUCE_FENCE);s.set(-6,1,-66,Blocks.SPRUCE_FENCE);s.set(-6,2,-66,Blocks.SPRUCE_FENCE);s.set(-6,3,-66,Blocks.LANTERN);
    }
    private static void party(Site s){
        // The judges' table where readers come in, with the card on it (placed by LiteraryRooms) and a chair.
        NovelRooms.furniture(s.l(),s.b().offset(4,0,-6),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);NovelRooms.furniture(s.l(),s.b().offset(4,0,-4),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        // The stage: a plank platform with steps at the front and a backdrop of orange and black.
        s.fill(-6,0,-30,2,0,-27,Blocks.SPRUCE_PLANKS);
        for(int x=-3;x<=-1;x++)s.set(x,0,-26,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH).setValue(StairBlock.HALF,Half.BOTTOM));
        for(int x:new int[]{-6,2})s.fill(x,1,-31,x,4,-31,Blocks.SPRUCE_FENCE);
        for(int x=-5;x<=1;x++){s.set(x,2,-31,x%2==0?Blocks.ORANGE_WOOL:Blocks.BLACK_WOOL);s.set(x,3,-31,x%2==0?Blocks.BLACK_WOOL:Blocks.ORANGE_WOOL);s.set(x,4,-31,Blocks.SPRUCE_SLAB);}
        for(int x:new int[]{-6,2})s.set(x,5,-31,Blocks.SPRUCE_SLAB);
        for(int x:new int[]{-6,-5,1,2})s.set(x,0,-26,Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING,Direction.SOUTH));
        s.set(4,0,-28,CostumePropBlock.of(CostumePropBlock.Kind.BOARD,Direction.SOUTH));s.set(4,1,-28,CostumePropBlock.of(CostumePropBlock.Kind.BOARD_TOP,Direction.SOUTH));
        // The bonfire with hay bales round it, the radio on one of them.
        s.set(BONFIRE.getX(),0,BONFIRE.getZ(),Blocks.CAMPFIRE);
        for(int[] h:new int[][]{{-14,-19},{-8,-19},{-11,-16},{-11,-22},{-13,-21},{-9,-17}})s.set(h[0],0,h[1],Blocks.HAY_BLOCK);
        s.set(RADIO.getX(),RADIO.getY(),RADIO.getZ(),CostumePropBlock.of(CostumePropBlock.Kind.RADIO,Direction.EAST));
        // Bobbing for apples, and the candy bowl on a bale by the stage.
        s.set(APPLES.getX(),APPLES.getY(),APPLES.getZ(),CostumePropBlock.of(CostumePropBlock.Kind.APPLE_TUB,Direction.SOUTH));
        s.set(CANDY.getX(),0,CANDY.getZ(),Blocks.HAY_BLOCK);s.set(CANDY.getX(),CANDY.getY(),CANDY.getZ(),CostumePropBlock.of(CostumePropBlock.Kind.CANDY_BOWL,Direction.SOUTH));
        for(int[] p:new int[][]{{-15,-24},{6,-26},{-8,-12},{9,-15}})s.set(p[0],0,p[1],Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING,Direction.SOUTH));
        // Light poles at the four corners with strings of bulbs between them, and one string across to the stage.
        for(int[] p:new int[][]{{-17,-13},{11,-13},{-17,-33},{11,-33}}){s.fill(p[0],0,p[1],p[0],4,p[1],Blocks.SPRUCE_FENCE);s.set(p[0],5,p[1],Blocks.SPRUCE_SLAB);}
        for(var bulb:bulbs())s.set(bulb.getX(),bulb.getY(),bulb.getZ(),CostumePropBlock.of(CostumePropBlock.Kind.BULBS,bulb.getX()==-3&&bulb.getZ()>-33&&bulb.getZ()<-13?Direction.EAST:Direction.NORTH));
    }
}
