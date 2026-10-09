package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import javax.annotation.Nullable;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.house.PlaytestLog;
import io.github.knaitoe.theoldesthouse.network.CabinStormPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.LiteraryViewPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

/**
 * The cabin at the end of the world (0.4.51). Four visitors wait outside a reader's door at dusk, private to that reader.
 * They ask three times, in a fixed order: Leonard for one heart of the reader's life, Adriane for another, Sabrina for
 * the arm of the off hand. Each gift is permanent. A storm rises with every ask. Refusing at any point is an answer too:
 * the one who asked walks into the lake, a room of the reader's own goes dark for them, and nothing already given comes
 * back. Giving everything breaks the storm; the four go into the lake together, and the television shows the reader's
 * home still standing. Either answer, read on the screen, resolves the story; the visitors leave a globe, whole or cracked.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class CabinBargain {
    static final LabyrinthPlace PLACE=LabyrinthPlace.END_WORLD_CABIN;
    public static final int LEONARD=0,ADRIANE=1,SABRINA=2,REDMOND=3;
    public static final String[] NAMES={"Leonard","Adriane","Sabrina","Redmond"};
    /** Who asks, in order: a heart, another heart, the arm. */
    public static final int[] ASKER={LEONARD,ADRIANE,SABRINA};
    /** The arm: the cord, the blow, black until waking, then the floor, then up. */
    public static final int BLOW=90,WAKE=170,DONE=230;
    /** Where they wait: on the grass either side of the path to the porch steps, facing the door. */
    static final double[][] WAIT={{1.6,-5.5},{-1.6,-5.5},{4.4,-5.0},{-4.4,-5.0}};
    /** Around the east side of the cabin, down the jetty and off its end. */
    static final double[][] ROUTE={{14.5,-6.5},{14.5,-39.5},{7.0,-41.8},{7.0,-48.6},{7.0,-49.4}};
    /** The jetty's far edge: past it there is only the lake. */
    static final double JETTY_END=-49.0;
    static final double WALK_SPEED=.11;static final int SINK=60,STAGGER=30;
    static final ResourceLocation HELD=ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"cabin_held");
    private record Cut(Vec3 anchor,float yaw,long start){}
    private static final Map<UUID,Cut> CUTS=new HashMap<>();
    private static final Map<UUID,Long> LAST=new HashMap<>();
    private CabinBargain(){}

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    /** A reader who made the earlier offering keeps that answer and its account. */
    public static boolean legacy(CompoundTag own){return own.getBoolean("ChoiceMade")&&!own.getBoolean("Bargain0451");}
    public static boolean answered(CompoundTag own){return own.getBoolean("Refused")||own.getBoolean("Sacrificed");}
    /** During the arm the reader is held where they knelt: no door, no pearl, no mount. */
    public static boolean held(ServerPlayer p){return CUTS.containsKey(p.getUUID());}

    // ------------------------------------------------------------------------------------------------ the words
    record Line(int who,String text){}
    static List<Line> speech(String id,CompoundTag own,String name){
        String home=own.getString("FactHome");int deaths=own.getInt("FactDeaths"),slept=own.getInt("FactSlept");long days=Math.max(1,own.getLong("FactDays"));
        String side=own.getString("ArmSide").isEmpty()?"left":own.getString("ArmSide");
        return switch(id){
            case "meet"->List.of(
                new Line(LEONARD,"Hello. My name is Leonard. I'm sorry. I know how this looks: four strangers at your door, and the light going."),
                new Line(LEONARD,"We didn't know each other before tonight. We were each shown the same thing, and we came."),
                new Line(LEONARD,"We saw your world end, "+name+". The water came up over "+home+". Over everything you built."),
                new Line(REDMOND,"Tell them the rest."),
                new Line(LEONARD,deaths>0?"You've died "+deaths+(deaths==1?" time":" times")+" and got up again. That costs you nothing, so it doesn't count. It wants something that does."
                        :"You've never died. Good. Then you know what a life is worth. It wants some of yours."),
                new Line(LEONARD,"It asks for your life. Not all of it. One heart of it. We can't take it from you. You have to give it."),
                new Line(LEONARD,"The television inside will show you, if you need to see it first. When you're ready, tell me."));
            case "ask2"->List.of(
                new Line(ADRIANE,"Thank you. I'm Adriane."),
                new Line(ADRIANE,"It wasn't enough. I'm so sorry. Listen to it. The storm is still coming."),
                new Line(ADRIANE,days+(days==1?" day":" days")+" you've lived there. "+(slept>0?"You slept in that bed "+slept+(slept==1?" night.":" nights."):"You never once slept through a night.")),
                new Line(ADRIANE,"And it isn't only yours that ends. I have a boy. Where we come from, the sea is already in the streets."),
                new Line(ADRIANE,"Only you can stop it. Another heart. Please."));
            case "ask3"->List.of(
                new Line(SABRINA,"My name is Sabrina. I'm a nurse. If this is going to be done, I'd rather be the one who does it properly."),
                new Line(SABRINA,"It wants something real now. Something... handy."),
                new Line(SABRINA,"Your "+side+" arm. I'll tie it off first. I've done this before, for people who never got to choose."),
                new Line(REDMOND,"It's our world that ends tonight if you don't. Not yours. Ours. You'd go home with both hands."),
                new Line(SABRINA,"Say no, and I'll walk into the lake, and that will be the end of it. Say yes, and everyone gets to go home."));
            case "after"->List.of(
                new Line(REDMOND,"It was ours, you know. The world. We let you think it was yours."),
                new Line(LEONARD,"Thank you, "+name+". Go home. It's still there. Look at the television."),
                new Line(ADRIANE,"The storm's going. Do you hear it?"),
                new Line(SABRINA,"Keep it clean and dry. You'll feel the hand for a long time. That's normal."));
            case "refuse0"->List.of(new Line(LEONARD,"All right. That's all right. It was always your answer to give."),new Line(LEONARD,"It ends for us, then. Not for you. Never for you."));
            case "refuse1"->List.of(new Line(ADRIANE,"You already gave one. I know what it cost."),new Line(ADRIANE,"Look after the heart you have left."));
            case "refuse2"->List.of(new Line(SABRINA,"I understand. I wouldn't either."),new Line(SABRINA,"Keep your hands. Both of them."));
            default->List.of();
        };
    }
    private static void say(ServerPlayer p,@Nullable LiteraryActor[] cast,Line line){
        p.sendSystemMessage(Component.literal(NAMES[line.who()]+": ").withStyle(ChatFormatting.GRAY).append(Component.literal(line.text()).withStyle(ChatFormatting.WHITE)));
        var actor=cast==null?null:cast[line.who()];if(actor!=null){actor.say(NAMES[line.who()]);face(actor,p.position());}
    }
    static void facts(ServerPlayer p,CompoundTag own){
        var stats=p.getStats();own.putInt("FactDeaths",stats.getValue(Stats.CUSTOM.get(Stats.DEATHS)));own.putInt("FactSlept",stats.getValue(Stats.CUSTOM.get(Stats.SLEEP_IN_BED)));
        own.putLong("FactDays",p.server.overworld().getDayTime()/24000+1);own.putString("ArmSide",p.getMainArm()==HumanoidArm.RIGHT?"left":"right");
        var bed=p.getRespawnPosition();boolean slept=bed!=null&&p.getRespawnDimension().equals(Level.OVERWORLD);var at=CabinScreen.home(p);
        String biome=p.server.overworld().getBiome(at).unwrapKey().map(k->k.location().getPath().replace('_',' ')).orElse("open country");
        own.putString("FactHome",slept?"the bed you made in the "+biome:"the "+biome+" where you first woke");
    }

    /** For the native tests: the current speech ends on the next tick, as though every line had been heard. */
    public static void hurry(ServerPlayer p){var own=own(p);var id=own.getString("Speech");if(id.isEmpty())return;own.putInt("Line",speech(id,own,p.getGameProfile().getName()).size());own.putInt("LineAt",0);save(p,own);}

    // ------------------------------------------------------------------------------------------------ the cast
    static @Nullable LiteraryActor[] cast(ServerPlayer p,CompoundTag own){
        var cast=new LiteraryActor[4];var b=LiteraryVignettes.base(p,PLACE);if(b==null)return cast;int gone=own.getInt("Gone");
        for(int i=0;i<4;i++){if((gone&1<<i)!=0)continue;
            var a=LiteraryVignettes.actor(p,PLACE,"Visitor"+i,LiteraryActor.STRANGER,BlockPos.containing(WAIT[i][0],0,WAIT[i][1]),true);
            if(a!=null){if(a.role()!=LiteraryActor.STRANGER||a.phase()!=i){a.appearance(LiteraryActor.STRANGER,i);a.setYRot(180);a.setYHeadRot(180);a.yBodyRot=180;}cast[i]=a;}}
        return cast;
    }
    /** The earlier shared strangers, seen by everyone at once, are retired as soon as they are loaded. */
    static void retireShared(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var s=LiteraryVignettes.shared(d,PLACE);boolean changed=false;
        for(int i=0;i<4;i++){String key="Stranger"+i;if(!s.hasUUID(key))continue;var e=p.serverLevel().getEntity(s.getUUID(key));if(e!=null){e.discard();s.remove(key);changed=true;}}
        if(changed)LiteraryVignettes.shared(d,PLACE,s);
    }
    static void face(LiteraryActor a,Vec3 at){var d=at.subtract(a.position());float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));a.setYRot(yaw);a.setYHeadRot(yaw);a.yBodyRot=yaw;}
    static double ground(ServerLevel l,BlockPos b,double x,double z){
        for(int y=3;y>=-4;y--){var pos=BlockPos.containing(b.getX()+x,b.getY()+y,b.getZ()+z);var s=l.getBlockState(pos);var shape=s.getCollisionShape(l,pos);if(!shape.isEmpty())return pos.getY()+shape.max(net.minecraft.core.Direction.Axis.Y);}
        return b.getY();
    }
    /** Where a walker is, a pure function of how long it has walked: the route, then the step off the jetty and under. */
    static @Nullable Vec3 walk(ServerLevel l,BlockPos b,int i,double ticks,double previousY){
        var points=new ArrayList<double[]>();points.add(WAIT[i]);points.addAll(Arrays.asList(ROUTE));double left=ticks*WALK_SPEED;
        for(int k=1;k<points.size();k++){var a=points.get(k-1);var c=points.get(k);double len=Math.hypot(c[0]-a[0],c[1]-a[1]);
            if(left<=len){double f=left/len,x=a[0]+(c[0]-a[0])*f,z=a[1]+(c[1]-a[1])*f;double y=ground(l,b,x,z);if(!Double.isNaN(previousY))y=previousY+Math.max(-.35,Math.min(.35,y-previousY));return new Vec3(b.getX()+x,y,b.getZ()+z);}
            left-=len;}
        double sink=left/WALK_SPEED;if(sink>SINK)return null;double f=sink/SINK;var end=ROUTE[ROUTE.length-1];
        return new Vec3(b.getX()+end[0],b.getY()-7*f*f,b.getZ()+end[1]-2.5*f);
    }
    static double routeTicks(int i){double len=0;double[] a=WAIT[i];for(var c:ROUTE){len+=Math.hypot(c[0]-a[0],c[1]-a[1]);a=c;}return len/WALK_SPEED;}

    // ------------------------------------------------------------------------------------------------ each five ticks
    public static void tick(ServerPlayer p,BlockPos b,CompoundTag own){
        retireShared(p);
        if(legacy(own)){LiteraryCabinChoices.legacyTick(p,b,own);return;}
        own.putBoolean("Bargain0451",true);var cast=cast(p,own);int present=own.getInt("Present");long now=p.server.overworld().getGameTime();
        var name=p.getGameProfile().getName();
        if(!own.getBoolean("Met")){
            var leonard=cast[LEONARD];boolean inside=Math.abs(p.getZ()-b.getZ()+24)<11&&Math.abs(p.getX()-b.getX())<12;
            if(leonard!=null&&p.distanceToSqr(leonard)<64&&HouseWatchers.sees(p,leonard.getEyePosition())){
                own.putBoolean("Met",true);facts(p,own);startSpeech(own,"meet",present+10);CabinScreen.requestHome(p);PlaytestLog.event(p,"cabin_met");
            }else if(inside&&present-own.getInt("KnockAt")>=300){own.putInt("KnockAt",present);sound(p,LiteraryRegistry.CABIN_KNOCK.get(),b.offset(0,1,-13).getCenter(),1,1);p.displayClientMessage(Component.literal("Someone is knocking at the cabin door."),true);}
        }
        runSpeech(p,own,cast,name,present);
        if(own.getBoolean("Asking")&&present-own.getInt("HintAt")>=160){own.putInt("HintAt",present);int asker=ASKER[Math.min(2,own.getInt("Ask"))];
            p.displayClientMessage(Component.literal(NAMES[asker]+" is waiting for your answer. Speak to "+NAMES[asker]+"."),true);}
        // The one asking, and anyone speaking, looks at the reader.
        if(own.getBoolean("Asking")){var asker=cast[ASKER[Math.min(2,own.getInt("Ask"))]];if(asker!=null&&!own.contains("Walk"+asker.phase()))face(asker,p.position());}
        if(answered(own)){
            boolean pictured=own.getBoolean("Refused")?own.getIntArray(CabinScreen.ROOM).length==CabinScreen.PIXELS:own.getIntArray(CabinScreen.HOME).length==CabinScreen.PIXELS;
            boolean settled=own.getBoolean("Refused")||own.getBoolean("CutDone");
            if(pictured&&settled){own.putInt("ChoiceTicks",own.getInt("ChoiceTicks")+5);
                var tv=b.offset(LiteraryRooms.TV).getCenter();
                if(!own.getBoolean("Ready")&&present-own.getInt("TvHintAt")>=200){own.putInt("TvHintAt",present);p.displayClientMessage(Component.literal("The television inside is showing something. Watch it."),true);}
                if(own.getInt("ChoiceTicks")>=200&&p.distanceToSqr(tv)<49&&HouseWatchers.sees(p,tv))
                    LiteraryVignettes.ready(p,PLACE,own,own.getBoolean("Sacrificed")?"gave_two_hearts_and_an_arm":"refused_the_visitors_after_"+own.getInt("Given")+"_gifts");
            }
            if(own.getBoolean("Sacrificed")&&own.getIntArray(CabinScreen.HOME).length!=CabinScreen.PIXELS&&!CabinScreen.pending(p.getUUID(),CabinScreen.HOME))CabinScreen.requestHome(p);
            if(own.getBoolean("Refused")&&own.getIntArray(CabinScreen.ROOM).length!=CabinScreen.PIXELS&&!CabinScreen.pending(p.getUUID(),CabinScreen.ROOM)){var room=LabyrinthPlace.byId(own.getString("ClosedRoom"));if(room==null||!CabinScreen.requestRoom(p,room,CabinScreen.ROOM))own.putIntArray(CabinScreen.ROOM,CabinScreen.blank());}
        }
        display(p,own,now);
    }
    static int storm(CompoundTag own,long now){
        if(own.getBoolean("Sacrificed"))return own.contains("CutStart")&&now-own.getLong("CutStart")<WAKE?100:0;
        if(own.getBoolean("Refused"))return 76;
        if(!own.getBoolean("Met"))return 0;
        boolean asking=own.getBoolean("Asking");return switch(own.getInt("Given")){case 0->asking?32:18;case 1->asking?60:46;default->asking?88:74;};
    }
    static void display(ServerPlayer p,CompoundTag own,long now){
        int scene=0,ticks=0;
        if(own.contains("CutStart")&&!own.getBoolean("CutDone")){scene=2;ticks=(int)(now-own.getLong("CutStart"));}
        else if(own.contains("HeartAt")&&now-own.getLong("HeartAt")<60){scene=1;ticks=(int)(now-own.getLong("HeartAt"));}
        else if(own.getBoolean("Sacrificed"))scene=3;
        int flash=own.getBoolean("Flash")?1:0;own.remove("Flash");
        HousePackets.send(p,new CabinStormPayload(storm(own,now),scene,ticks,flash));
        var tv=new CompoundTag();tv.putUUID("Reader",p.getUUID());
        int[] screen=own.getBoolean("Refused")?own.getIntArray(CabinScreen.ROOM):own.getIntArray(CabinScreen.HOME);
        if(screen.length==CabinScreen.PIXELS){tv.putIntArray("Screen",screen);tv.putBoolean("ScreenDark",own.getBoolean("Refused")&&own.getInt("ChoiceTicks")>=150);tv.putInt("ScreenStorm",storm(own,now));HousePackets.send(p,new LiteraryViewPayload(-1,30,0,tv));}
    }
    static void startSpeech(CompoundTag own,String id,int at){own.putString("Speech",id);own.putInt("Line",0);own.putInt("LineAt",at);}
    static void runSpeech(ServerPlayer p,CompoundTag own,LiteraryActor[] cast,String name,int present){
        String id=own.getString("Speech");if(id.isEmpty()||present<own.getInt("LineAt"))return;var lines=speech(id,own,name);int line=own.getInt("Line");
        if(line<lines.size()){var next=lines.get(line);say(p,cast,next);own.putInt("Line",line+1);own.putInt("LineAt",present+40+Math.min(60,next.text().length()/2));return;}
        own.remove("Speech");
        switch(id){
            case "meet","ask2","ask3"->{own.putBoolean("Asking",true);own.putInt("HintAt",present);int asker=ASKER[Math.min(2,own.getInt("Ask"))];p.displayClientMessage(Component.literal(NAMES[asker]+" is waiting for your answer. Speak to "+NAMES[asker]+"."),true);}
            case "after"->{long now=p.server.overworld().getGameTime();int k=0;for(int i:new int[]{LEONARD,ADRIANE,SABRINA,REDMOND})if((own.getInt("Gone")&1<<i)==0&&!own.contains("Walk"+i))own.putLong("Walk"+i,now+20+STAGGER*k++);}
            case "refuse0","refuse1","refuse2"->{int asker=ASKER[id.charAt(6)-'0'];if(!own.contains("Walk"+asker))own.putLong("Walk"+asker,p.server.overworld().getGameTime()+10);}
            default->{}
        }
    }

    // ------------------------------------------------------------------------------------------------ answering
    /** Speaking to any of them, or touching the television, during an ask. */
    public static void open(ServerPlayer p){
        if(!LiteraryVignettes.inside(p,PLACE))return;var own=own(p);
        if(legacy(own)||answered(own)){p.displayClientMessage(Component.literal(own.getBoolean("Ready")?"They have heard your answer. Your account is on the table inside.":"They have heard your answer. Watch the television inside."),true);return;}
        if(!own.getBoolean("Met")){p.displayClientMessage(Component.literal("Go out to them. They are waiting in front of the porch."),true);return;}
        if(!own.getBoolean("Asking")){p.displayClientMessage(Component.literal("Let them finish."),true);return;}
        int ask=Math.min(2,own.getInt("Ask"));String asker=NAMES[ASKER[ask]];
        var icons=new HashMap<Integer,ItemStack>();
        if(ask<2)icons.put(0,icon(Items.RED_DYE,ask==0?"Give Leonard one heart":"Give Adriane another heart","Your greatest health falls by one heart, forever.","Dying will not bring it back. No door will."));
        else icons.put(0,icon(Items.IRON_AXE,"Give Sabrina your "+own.getString("ArmSide")+" arm","You will never hold anything in your "+own.getString("ArmSide")+" hand again.","This is permanent. You will be asked once more."));
        icons.put(4,icon(Items.BARRIER,"Refuse",asker+" will walk into the lake.","One unfinished room of yours goes dark, for you alone.",own.getInt("Given")>0?"What you have given stays given.":"You keep your whole life."));
        icons.put(8,icon(Items.OAK_DOOR,"Not yet","They will wait for you."));
        LiteraryChoiceMenu.open(p,asker+" is asking",icons,()->LiteraryVignettes.inside(p,PLACE),slot->answer(p,ask,slot));
    }
    static ItemStack icon(net.minecraft.world.item.Item item,String label,String... lore){
        var s=LiteraryChoiceMenu.icon(item,label);var lines=new ArrayList<Component>();for(var l:lore)lines.add(Component.literal(l).withStyle(ChatFormatting.GRAY));s.set(DataComponents.LORE,new ItemLore(lines));return s;
    }
    static boolean answer(ServerPlayer p,int ask,int slot){
        if(slot==8)return true;var own=own(p);
        if(answered(own)||!own.getBoolean("Asking")||own.getInt("Ask")!=ask){p.displayClientMessage(Component.literal("That answer has already been given."),true);return true;}
        if(slot==4){refuse(p,own,ask);return true;}
        if(slot!=0)return false;
        if(ask<2){giveHeart(p,own,ask);return true;}
        own.putBoolean("Confirm",true);save(p,own);return true;
    }
    /** The second question for the arm opens on the next tick, after the first closes. */
    static void confirm(ServerPlayer p){
        var own=own(p);own.remove("Confirm");save(p,own);if(answered(own)||!own.getBoolean("Asking")||own.getInt("Ask")!=2)return;
        var icons=new HashMap<Integer,ItemStack>();String side=own.getString("ArmSide");
        icons.put(2,icon(Items.IRON_AXE,"Yes. Take it.","Your "+side+" arm, for good.","You will hold nothing in that hand again."));
        icons.put(6,icon(Items.OAK_DOOR,"No. Not yet.","Sabrina will wait."));
        LiteraryChoiceMenu.open(p,"Are you sure?",icons,()->LiteraryVignettes.inside(p,PLACE),slot->{if(slot==6)return true;if(slot!=2)return false;var now=own(p);if(answered(now)||!now.getBoolean("Asking")||now.getInt("Ask")!=2)return true;giveArm(p,now);return true;});
    }
    static void giveHeart(ServerPlayer p,CompoundTag own,int ask){
        BodyLoss.takeHeart(p);long now=p.server.overworld().getGameTime();
        own.putInt("Given",own.getInt("Given")+1);own.putInt("Ask",ask+1);own.putBoolean("Asking",false);own.putLong("HeartAt",now);own.putBoolean("Flash",true);
        startSpeech(own,ask==0?"ask2":"ask3",own.getInt("Present")+90);save(p,own);
        sound(p,LiteraryRegistry.CABIN_HEART.get(),p.getEyePosition(),1,1);
        var cast=cast(p,own);var asker=cast[ASKER[ask]];if(asker!=null){asker.say(NAMES[ASKER[ask]]);face(asker,p.position());}
        p.sendSystemMessage(Component.literal(NAMES[ASKER[ask]]+" lays a hand flat on your chest. Something in it stops, and starts again, smaller.").withStyle(ChatFormatting.ITALIC));
        PlaytestLog.event(p,"cabin_gift","what","heart","given",own.getInt("Given"));
    }
    static void refuse(ServerPlayer p,CompoundTag own,int ask){
        var d=LabyrinthData.get(p.server);var room=LiteraryCabinChoices.personalClosure(p);
        if(room!=null){LiteraryCabinChoices.close(d,p.getUUID(),room);own.putString("ClosedRoom",room.id());}
        own.putBoolean("Refused",true);own.putBoolean("ChoiceMade",true);own.putBoolean("Asking",false);own.putInt("RefusedAt",ask);own.putInt("ChoiceTicks",0);own.putBoolean("Flash",true);
        startSpeech(own,"refuse"+ask,own.getInt("Present")+10);save(p,own);
        if(room!=null&&!CabinScreen.requestRoom(p,room,CabinScreen.ROOM)){own.putIntArray(CabinScreen.ROOM,CabinScreen.blank());save(p,own);}
        p.displayClientMessage(Component.literal(room==null?"You refused. Nothing of yours is left to go dark.":"You refused. Inside, the television has changed: it shows "+StaircaseProse.place(room.id())+"."),false);
        PlaytestLog.event(p,"cabin_refused","given",own.getInt("Given"),"room",room==null?"":room.id());
    }
    static void giveArm(ServerPlayer p,CompoundTag own){
        String side=own.getString("ArmSide");
        if(!BodyLoss.takeArm(p)){p.displayClientMessage(Component.literal("There is nothing more of you to give."),true);return;}
        long now=p.server.overworld().getGameTime();var collection=MotherCollection.get(p.server);
        var arm=new ItemStack(LiteraryRegistry.GIVEN_ARM.get());arm.set(DataComponents.CUSTOM_NAME,Component.literal(p.getGameProfile().getName()+"'s "+side+" arm"));
        CustomData.update(DataComponents.CUSTOM_DATA,arm,t->{t.putUUID("GivenBy",p.getUUID());t.putString("Side",side);t.putLong("GivenAt",now);});
        var kept=collection.sealVignetteItem(arm,p.registryAccess(),p.getUUID(),now);if(kept!=null)own.putUUID("Offering",kept.id);
        own.putInt("Given",3);own.putInt("Ask",3);own.putBoolean("Asking",false);own.putBoolean("Sacrificed",true);own.putBoolean("ChoiceMade",true);own.putLong("CutStart",now);own.putInt("ChoiceTicks",0);
        save(p,own);begin(p,now);PlaytestLog.event(p,"cabin_gift","what","arm","given",3);
    }

    // ------------------------------------------------------------------------------------------------ the arm
    static void begin(ServerPlayer p,long now){
        var look=p.getLookAngle().multiply(1,0,1);if(look.lengthSqr()<1e-4)look=new Vec3(0,0,-1);look=look.normalize();
        CUTS.put(p.getUUID(),new Cut(p.position(),p.getYRot(),now));LAST.put(p.getUUID(),now);hold(p,true);
        var cast=cast(p,own(p));var sabrina=cast[SABRINA];
        if(sabrina!=null){var at=p.position().add(look.scale(1.4));sabrina.moveTo(at.x,p.getY(),at.z,0,0);face(sabrina,p.position());p.lookAt(EntityAnchorArgument.Anchor.EYES,sabrina.getEyePosition().add(0,-.2,0));}
    }
    static void hold(ServerPlayer p,boolean on){
        for(var attribute:List.of(Attributes.MOVEMENT_SPEED,Attributes.JUMP_STRENGTH)){var a=p.getAttribute(attribute);if(a==null)continue;a.removeModifier(HELD);if(on)a.addTransientModifier(new AttributeModifier(HELD,-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
        p.setForcedPose(on?Pose.CROUCHING:null);if(!on)p.setPose(Pose.STANDING);p.refreshDimensions();
    }
    private static void cutTick(ServerPlayer p,Cut cut,long now){
        long last=LAST.getOrDefault(p.getUUID(),cut.start());LAST.put(p.getUUID(),now);
        if(p.position().distanceToSqr(cut.anchor())>.04)p.teleportTo(p.serverLevel(),cut.anchor().x,cut.anchor().y,cut.anchor().z,p.getYRot(),p.getXRot());
        for(long t=last-cut.start()+1;t<=now-cut.start();t++)cutEvent(p,(int)t);
    }
    private static void cutEvent(ServerPlayer p,int t){
        switch(t){
            case 1->{say(p,cast(p,own(p)),new Line(SABRINA,"Kneel down for me. Look at me, not at it."));sync(p);}
            case 22->{sound(p,LiteraryRegistry.CABIN_CORD.get(),p.getEyePosition(),1,1);say(p,null,new Line(SABRINA,"I'm tying it off above the elbow. This part hurts more than the rest."));}
            case 52->{sound(p,LiteraryRegistry.CABIN_HEART.get(),p.getEyePosition(),.8F,1.15F);say(p,null,new Line(LEONARD,"We're here. We're right here with you."));}
            case 78->say(p,null,new Line(SABRINA,"Breathe in."));
            case BLOW->{var l=p.serverLevel();l.playSound(null,p.blockPosition(),LiteraryRegistry.CABIN_CHOP.get(),SoundSource.PLAYERS,1,1);BodyLoss.reveal(p);var own=own(p);own.putBoolean("Flash",true);save(p,own);sync(p);
                var arm=p.position().add(0,1.3,0);l.sendParticles(new DustParticleOptions(new Vector3f(.42F,.03F,.03F),1.2F),arm.x,arm.y,arm.z,18,.25,.2,.25,0);}
            case BLOW+6->p.serverLevel().playSound(null,p.blockPosition(),LiteraryRegistry.CABIN_WET.get(),SoundSource.PLAYERS,.9F,1);
            case BLOW+10->sound(p,LiteraryRegistry.CABIN_RING.get(),p.getEyePosition(),.6F,1);
            case 150->{p.setForcedPose(Pose.SWIMMING);p.refreshDimensions();}
            case WAKE->sync(p);
            case 200->say(p,null,new Line(SABRINA,"Easy. Stay down a moment. You did well."));
            case DONE->finish(p);
            default->{}
        }
    }
    private static void sync(ServerPlayer p){var own=own(p);display(p,own,p.server.overworld().getGameTime());save(p,own);}
    /** The scene ends: up off the floor, the storm gone, the four of them about to speak. Also on returning after a logout. */
    static void finish(ServerPlayer p){
        CUTS.remove(p.getUUID());LAST.remove(p.getUUID());hold(p,false);var own=own(p);if(own.getBoolean("CutDone"))return;
        own.putBoolean("CutDone",true);if(own.getString("Speech").isEmpty()&&!own.contains("Walk0"))startSpeech(own,"after",own.getInt("Present")+30);save(p,own);
        p.addEffect(new MobEffectInstance(MobEffects.CONFUSION,120,0));p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,1200,0));
        p.sendSystemMessage(Component.literal("Your "+own.getString("ArmSide")+" sleeve is pinned up and tied. The rain has stopped.").withStyle(ChatFormatting.ITALIC));
    }

    // ------------------------------------------------------------------------------------------------ each tick
    @SubscribeEvent public static void everyTick(ServerTickEvent.Post e){
        var server=e.getServer();long now=server.overworld().getGameTime();
        for(var p:server.getPlayerList().getPlayers()){
            var cut=CUTS.get(p.getUUID());if(cut!=null){if(!p.isAlive()||!LiteraryVignettes.inside(p,PLACE))finish(p);else cutTick(p,cut,now);}
            if(!LiteraryVignettes.inside(p,PLACE))continue;var own=own(p);if(legacy(own)||!own.getBoolean("Bargain0451"))continue;
            if(own.getBoolean("Confirm")){confirm(p);own=own(p);}
            stage(p,own,now);
        }
    }
    /** Waiting visitors keep their places; walkers follow the route and go under. */
    static void stage(ServerPlayer p,CompoundTag own,long now){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;var l=p.serverLevel();var d=LabyrinthData.get(p.server);var s=LiteraryVignettes.shared(d,PLACE);boolean changed=false;
        for(int i=0;i<4;i++){
            if((own.getInt("Gone")&1<<i)!=0)continue;String key="Visitor"+i+"_"+p.getUUID();if(!s.hasUUID(key))continue;
            if(!(l.getEntity(s.getUUID(key)) instanceof LiteraryActor a))continue;
            if(own.contains("Walk"+i)){long start=own.getLong("Walk"+i);if(now<start)continue;
                var at=walk(l,b,i,now-start,a.getY());
                if(at==null){a.discard();own.putInt("Gone",own.getInt("Gone")|1<<i);changed=true;continue;}
                boolean enters=a.getZ()>b.getZ()+JETTY_END&&at.z<=b.getZ()+JETTY_END;
                var step=at.subtract(a.position());if(step.horizontalDistanceSqr()>1e-5){float yaw=(float)Math.toDegrees(Math.atan2(-step.x,step.z));a.setYRot(yaw);a.setYHeadRot(yaw);a.yBodyRot=yaw;}
                a.moveTo(at.x,at.y,at.z,a.getYRot(),0);a.setCustomNameVisible(false);
                if(enters){sound(p,SoundEvents.GENERIC_SPLASH,at,1,.8F);l.sendParticles(p,ParticleTypes.SPLASH,true,at.x,at.y+.2,at.z,30,.4,.1,.4,.1);own.putBoolean("Flash",true);changed=true;}
            }else if(!held(p)||i!=SABRINA){var w=WAIT[i];double y=ground(l,b,w[0],w[1]);if(Math.abs(a.getX()-b.getX()-w[0])>.05||Math.abs(a.getZ()-b.getZ()-w[1])>.05||Math.abs(a.getY()-y)>.05)a.moveTo(b.getX()+w[0],y,b.getZ()+w[1],a.getYRot(),0);}
        }
        if(changed)save(p,own);
    }
    /** Positional, but only for this reader: the visitors are theirs. */
    static void sound(ServerPlayer p,SoundEvent sound,Vec3 at,float volume,float pitch){
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.NEUTRAL,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var own=own(p);if(own.contains("CutStart")&&!own.getBoolean("CutDone"))finish(p);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p&&CUTS.remove(p.getUUID())!=null){LAST.remove(p.getUUID());hold(p,false);}}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){CUTS.clear();LAST.clear();}
}
