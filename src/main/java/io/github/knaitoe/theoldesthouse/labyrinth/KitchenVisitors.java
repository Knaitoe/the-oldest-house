package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import io.github.knaitoe.theoldesthouse.labyrinth.KitchenCooking.Cook;
import io.github.knaitoe.theoldesthouse.labyrinth.KitchenCooking.Dish;
import io.github.knaitoe.theoldesthouse.labyrinth.KitchenCooking.Seasoning;
import io.github.knaitoe.theoldesthouse.labyrinth.KitchenCooking.Texture;

/**
 * 0.4.331: people who come into the familiar kitchen (docs/KITCHEN_0_4_331.md). The owner: "Add NPCs that visit the kitchen? Truant?
 * Holloway? Maybe unnamed NPCs – other travelers, or NPCs from the lengthier vignettes? There are some NPCs that claim to leave their
 * homes or houses."
 * <p>
 * While someone is in the kitchen, now and then someone else comes in by one of its far doors, sits at the table a while and goes:
 * Johnny, from his places; Holloway, while he lives; the tenant from Vacant Possession, once a reader in the kitchen has carried the
 * last of their things down; and someone passing through, another person lost in the House. One at a time, the same one for
 * everybody there. They can be talked to, and they take food: each answers to its broad strokes ({@link KitchenCooking}) in their own
 * way, remembers what each reader gave them, and the first time a reader cooks them something they love, tells that reader something
 * about themselves. What they ate, who made it and what they said is written in the kitchen's book on the counter, for anyone.
 * <p>
 * Nothing here frightens anyone, and nothing is a story: no Witness, no account.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class KitchenVisitors {
    private KitchenVisitors(){}
    public static final String STATE="kitchen_visitors_0331",SHARED="Shared",BOOK_TAG="the_oldest_house_kitchen_book";
    public static final int JOHNNY=100,HOLLOWAY=101,TENANT=102,TRAVELLER=103;
    public static boolean visitor(int phase){return phase>=JOHNNY&&phase<=TRAVELLER;}
    /** In seconds of time with someone in the kitchen: the first visit, the gap between visits, how long one stays, and after eating. */
    public static final int FIRST=40,GAP=180,GAP_SPREAD=120,STAY=150,AFTER_MEAL=60,MEALS=2,WALK_LIMIT=15;
    public static final BlockPos BOOK=new BlockPos(-5,0,-2);
    static final int ARRIVING=1,SEATED=2,LEAVING=3;

    /** What someone likes and does not, by stroke. */
    record Palate(Set<Texture> likeT,Set<Seasoning> likeS,Set<Cook> likeC,Set<Texture> hateT,Set<Seasoning> hateS,Set<Cook> hateC){}
    /** Everything a visitor says. lastTime takes the strokes of what the reader gave them before. */
    record Voice(String name,String book,String greet,String lastTime,String hungry,List<String> history,String loved,String good,String polite,
                 Map<Object,String> dislike,String underdone,String burnt,String full,String refuse,String leaving,String story){}
    record Visitor(int phase,Palate palate,Voice voice){}

    static final List<Visitor> CAST=List.of(
        new Visitor(JOHNNY,new Palate(EnumSet.of(Texture.CRISP),EnumSet.of(Seasoning.SHARP,Seasoning.SALTY),EnumSet.of(Cook.WELL_DONE,Cook.BURNT),EnumSet.of(Texture.CREAMY),EnumSet.of(Seasoning.SWEET),EnumSet.noneOf(Cook.class)),
            new Voice("Johnny","Johnny",
                "Is there coffee? No. Fine. I'll sit.",
                "You again. Last time it was %s. I've been thinking about it.",
                "I don't really eat. I graze. Something with an edge to it, if you're making something.",
                List.of("I worked in a shop once where the only food was what the place next door threw out at closing. Pickles, mostly. I got a taste for it.",
                        "There's a counter I keep meaning to go back to. Eight stools. I can't remember the street.",
                        "I keep ending up in other people's kitchens. I don't mind. Nobody asks you anything in a kitchen."),
                "Oh, that's got teeth. Good.","Yeah. That's all right.","Thanks. Really.",
                Map.of(Seasoning.SWEET,"Too sweet. Like being talked down to.",Texture.CREAMY,"It's like eating a pillow. I'll finish it."),
                "Still cold in the middle. I've had worse. Recently.","Burnt on the bottom. Honestly? Fine.",
                "I couldn't. I mean it.","I'll pass.","Somewhere to be. Probably not, but still.",
                "The best thing I ever ate was at four in the morning, out of a paper tray, on a kerb, with people I don't talk to any more. This is close.")),
        new Visitor(HOLLOWAY,new Palate(EnumSet.of(Texture.CRISP),EnumSet.of(Seasoning.SALTY),EnumSet.of(Cook.WELL_DONE,Cook.BURNT),EnumSet.of(Texture.SOFT,Texture.CREAMY),EnumSet.of(Seasoning.SWEET),EnumSet.of(Cook.UNDERDONE)),
            new Voice("Holloway","Holloway",
                "Don't mind me. I'll not be long.",
                "Last time you gave me %s. I remember a good meal.",
                "I could eat. Salt, and cooked hard. Down there you learn not to trust anything soft.",
                List.of("Seven days down we ran out of everything but salt pork. Ate it every way there is. Never got sick of it, which tells you something about me.",
                        "I kept the camp. Somebody has to keep the camp, or there's nothing to come back to.",
                        "The others went on ahead. I stayed with the fire. I'm still not sure which of us was right."),
                "That's a proper camp meal. Thank you.","That'll do. That'll do fine.","Obliged.",
                Map.of(Texture.SOFT,"Slop. I'll eat it. I've eaten worse off a boot.",Texture.CREAMY,"Slop. I'll eat it. I've eaten worse off a boot.",Seasoning.SWEET,"Sweet. Never had the taste for it."),
                "It's not done. You learn to cook it through, down there, or you learn the other way.","Bit of char never hurt anyone.",
                "No more. I'll walk heavy.","Not that.","I'll get back. There's a lot of dark to keep an eye on.",
                "My brother cooked like this. Salt and fire and no talking. I haven't thought about him in a long time.")),
        new Visitor(TENANT,new Palate(EnumSet.of(Texture.SOFT,Texture.CREAMY),EnumSet.of(Seasoning.PLAIN,Seasoning.SWEET),EnumSet.of(Cook.THROUGH),EnumSet.noneOf(Texture.class),EnumSet.of(Seasoning.FIERY,Seasoning.SHARP),EnumSet.of(Cook.BURNT)),
            new Voice("The tenant","The tenant from the flat",
                "I don't have a kitchen at the moment. It's nice to sit in one.",
                "You fed me last time. %s. I've been thinking about it.",
                "A little. Something soft. I lost the knack of chewing, with all that carrying.",
                List.of("Everything went down to the van. I kept thinking I'd left the kettle on.",
                        "The new people will paint over the marks on the doorframe. I suppose that's right.",
                        "I keep the keys in my pocket. I don't know who to give them back to."),
                "That's what my kitchen used to smell like. Thank you.","That's kind. That's very kind.","Thank you. Really.",
                Map.of(Seasoning.FIERY,"Oh, that's got a kick. I'll go slowly.",Seasoning.SHARP,"Sharp. My mouth isn't what it was."),
                "Not quite there, is it. Never mind.","It's caught on the bottom. Never mind.",
                "No more for me. Really.","I'd rather not.","I have the keys to hand back. I keep forgetting where.",
                "We had a table like this. Four chairs, one that wobbled. I always sat on that one, so nobody else had to.")),
        new Visitor(TRAVELLER,new Palate(EnumSet.of(Texture.SOFT,Texture.CREAMY),EnumSet.of(Seasoning.SALTY,Seasoning.EARTHY),EnumSet.of(Cook.THROUGH,Cook.WELL_DONE),EnumSet.noneOf(Texture.class),EnumSet.of(Seasoning.BITTER,Seasoning.MUDDLED),EnumSet.of(Cook.UNDERDONE)),
            new Voice("The traveller","Someone passing through",
                "Is this the kitchen? Everyone says there's a kitchen.",
                "I found it again. And you. Last time you made %s.",
                "I've been eating what I find in drawers. Anything hot would be something.",
                List.of("I came in through a door in my own hallway. I've been looking for it since.",
                        "Some of the rooms have people in them. Some of them talk to you. I stopped answering.",
                        "I left a coat on a chair somewhere. If you see it, it isn't mine any more."),
                "Hot. Salt. I could cry.","That's good. That's really good.","Thank you. I mean it.",
                Map.of(Seasoning.BITTER,"Bitter. That's all right, I'm awake now.",Seasoning.MUDDLED,"There's a lot going on in this."),
                "It's fine. It's warm, mostly.","A bit burnt. Still warm.",
                "I couldn't fit another thing.","No, thank you.","I'm going to try the next door. Maybe it's mine.",
                "Someone made soup like this for me once, when I was ill, and sat on the end of the bed while I ate it. I'd forgotten that.")));
    public static @Nullable Visitor cast(int phase){for(var v:CAST)if(v.phase()==phase)return v;return null;}

    // ------------------------------------------------------------------ state
    static CompoundTag shared(MinecraftServer s){return LabyrinthData.get(s).stateEntry(STATE,SHARED);}
    static void shared(MinecraftServer s,CompoundTag t){LabyrinthData.get(s).setStateEntry(STATE,SHARED,t);}
    static CompoundTag memory(MinecraftServer s,UUID reader){return LabyrinthData.get(s).stateEntry(STATE,reader.toString());}
    static void memory(MinecraftServer s,UUID reader,CompoundTag t){LabyrinthData.get(s).setStateEntry(STATE,reader.toString(),t);}
    /** One walk in or out: the points still to reach. Not saved; a visitor caught mid-walk by a restart sits down or is gone. */
    private static final Map<UUID,List<Vec3>> WALKS=new HashMap<>();
    /** A line to say once a mouthful is swallowed. */
    private record Pending(UUID actor,String text,long at){}
    private static final List<Pending> PENDING=new ArrayList<>();

    @Nullable static BlockPos base(MinecraftServer s){return KitchenFittings.base(s);}
    static List<ServerPlayer> inKitchen(MinecraftServer s){var origin=HouseSavedData.get(s).houseOrigin();var out=new ArrayList<ServerPlayer>();if(origin==null)return out;for(var p:s.getPlayerList().getPlayers())if(p.isAlive()&&FamiliarRoom.inKitchen(p,origin))out.add(p);return out;}
    public static @Nullable LiteraryActor current(ServerLevel l){var t=shared(l.getServer());return t.hasUUID("Actor")&&l.getEntity(t.getUUID("Actor")) instanceof LiteraryActor a&&a.isAlive()?a:null;}
    /** Who could come in now: Holloway only while he lives, the tenant only once someone here has carried their things down. */
    static List<Visitor> available(MinecraftServer s,List<ServerPlayer> here){
        var data=LabyrinthData.get(s);var out=new ArrayList<Visitor>();
        for(var v:CAST){
            if(v.phase()==HOLLOWAY&&data.state(HollowayVignette.ID).getBoolean("ActorDead"))continue;
            if(v.phase()==TENANT&&here.stream().noneMatch(p->movedOut(data,p.getUUID())))continue;
            out.add(v);
        }
        return out;
    }
    /** Who could come in now, by phase: for tests. */
    public static List<Integer> availablePhases(MinecraftServer s){return available(s,inKitchen(s)).stream().map(Visitor::phase).toList();}
    /** How often each comes, before the rest: someone passing through most, Johnny often, Holloway and the tenant less. */
    public static int baseWeight(int phase){return switch(phase){case TRAVELLER->4;case JOHNNY->3;default->2;};}
    /**
     * 0.4.332: who comes in is weighted, not taken in turn. Each has their own weight; someone nobody in the kitchen has fed yet is
     * twice as likely, so a group comes to meet everyone; the one who just left does not come straight back while anyone else could.
     */
    public static int weight(MinecraftServer s,int phase,List<ServerPlayer> here,int last,int available){
        if(phase==last&&available>1)return 0;
        int w=baseWeight(phase);
        boolean met=here.stream().anyMatch(p->memory(s,p.getUUID()).getCompound("V"+phase).getInt("Fed")>0);
        return met?w:w*2;
    }
    static Visitor pick(MinecraftServer s,List<Visitor> pool,List<ServerPlayer> here,int last,net.minecraft.util.RandomSource random){
        int total=0;int[] w=new int[pool.size()];
        for(int i=0;i<pool.size();i++){w[i]=weight(s,pool.get(i).phase(),here,last,pool.size());total+=w[i];}
        if(total<=0)return pool.get(0);int roll=random.nextInt(total);
        for(int i=0;i<pool.size();i++){roll-=w[i];if(roll<0)return pool.get(i);}
        return pool.get(pool.size()-1);
    }
    static boolean movedOut(LabyrinthData data,UUID reader){var own=LiteraryVignettes.personal(data,reader,VacantPossession.PLACE);var outcome=own.getString("Outcome");return !outcome.isEmpty()&&!VacantPossession.kept(outcome);}

    // ------------------------------------------------------------------ the clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();var l=s.getLevel(HouseDimensions.INTERIOR);var b=base(s);if(l==null||b==null)return;
        var a=current(l);
        if(a!=null)walk(l,a);
        for(var it=PENDING.iterator();it.hasNext();){var p=it.next();if(l.getGameTime()<p.at())continue;it.remove();if(l.getEntity(p.actor()) instanceof LiteraryActor who&&who.isAlive()){who.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);speak(who,p.text());}}
        if(s.getTickCount()%20==0)second(s,l,b);
    }
    /** Once a second while anyone is in the kitchen. */
    static void second(MinecraftServer s,ServerLevel l,BlockPos b){
        var here=inKitchen(s);var t=shared(s);var a=current(l);
        if(here.isEmpty()){
            // Nobody to sit with: whoever was here has gone by the time anyone comes back.
            if(a!=null){a.discard();WALKS.remove(a.getUUID());}
            if(t.contains("Who")){t.remove("Who");t.remove("Actor");t.putInt("Next",t.getInt("Occ")+GAP);shared(s,t);}
            return;
        }
        int occ=t.getInt("Occ")+1;t.putInt("Occ",occ);if(occ%5==1)ensureBook(l,b);if(!t.contains("Next"))t.putInt("Next",FIRST);
        if(!t.contains("Who")){
            if(occ>=t.getInt("Next")){var pool=available(s,here);if(!pool.isEmpty()){
                var pick=pick(s,pool,here,t.getInt("LastWho"),l.getRandom());
                shared(s,t);arrive(l,b,pick.phase());return;}}
            shared(s,t);return;
        }
        if(a==null){t.remove("Who");t.remove("Actor");t.putInt("Next",occ+GAP);shared(s,t);return;}
        int stage=t.getInt("Stage"),since=occ-t.getInt("StageAt");
        if(stage==ARRIVING&&(!WALKS.containsKey(a.getUUID())||since>WALK_LIMIT)){sit(l,b,a,t);t.putInt("Stage",SEATED);t.putInt("StageAt",occ);greetAll(l,a,here);}
        else if(stage==SEATED){
            boolean done=t.getBoolean("Fed")?occ-t.getInt("FedAt")>=AFTER_MEAL:since>=STAY;
            if(done)leave(l,b,a,t,occ);
            else if(since%45==20&&!t.getBoolean("Fed")){var v=cast(a.phase());if(v!=null)speak(a,v.voice().hungry());}
        }
        else if(stage==LEAVING&&(!WALKS.containsKey(a.getUUID())||since>WALK_LIMIT)){
            l.playSound(null,a.blockPosition(),SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.6F,1F);
            WALKS.remove(a.getUUID());a.discard();t.remove("Who");t.remove("Actor");t.putInt("LastWho",a.phase());t.putInt("Next",occ+GAP+l.getRandom().nextInt(GAP_SPREAD));
        }
        shared(s,t);
    }

    // ------------------------------------------------------------------ coming and going
    /** The two far doors: the inside of each, and the doorway. */
    static final BlockPos[] DOORS={new BlockPos(6,0,-6),new BlockPos(3,0,-15)},INSIDE={new BlockPos(5,0,-6),new BlockPos(3,0,-14)};
    /** Someone comes in by one of the far doors and makes for a chair. Public for tests. */
    public static @Nullable LiteraryActor arrive(ServerLevel l,BlockPos b,int phase){
        var v=cast(phase);if(v==null)return null;var s=l.getServer();var old=current(l);if(old!=null){WALKS.remove(old.getUUID());old.discard();}
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        int door=l.getRandom().nextInt(DOORS.length);var start=Vec3.atBottomCenterOf(b.offset(INSIDE[door]));
        a.bind(FamiliarRoom.KITCHEN,null);a.appearance(LiteraryActor.STRANGER,phase);a.setNoGravity(true);a.setCustomNameVisible(false);
        a.moveTo(start.x,start.y,start.z,door==0?90:0,0);
        if(!l.addFreshEntity(a))return null;
        l.playSound(null,b.offset(DOORS[door]),SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.7F,1F);
        var t=shared(s);t.putInt("Who",phase);t.putUUID("Actor",a.getUUID());t.putInt("Door",door);t.putInt("Stage",ARRIVING);t.putInt("StageAt",t.getInt("Occ"));
        t.putBoolean("Fed",false);t.putInt("Meals",0);t.putInt("Round",t.getInt("Round")+1);
        var chair=chair(l,b,start);
        if(chair!=null){t.putLong("Chair",chair.subtract(b).asLong());var c=Vec3.atBottomCenterOf(chair);var out=c.add(c.subtract(Vec3.atBottomCenterOf(b.offset(FamiliarRoom.TABLE))).normalize());
            WALKS.put(a.getUUID(),new ArrayList<>(List.of(Vec3.atBottomCenterOf(b.offset(INSIDE[door]).offset(door==0?-1:0,0,door==1?1:0)),out,c)));}
        else{t.remove("Chair");WALKS.put(a.getUUID(),new ArrayList<>(List.of(Vec3.atBottomCenterOf(b.offset(2,0,-5)))));}
        shared(s,t);return a;
    }
    /** A free chair at the table: no one sitting in it or standing in it, the nearest the door. */
    static @Nullable BlockPos chair(ServerLevel l,BlockPos b,Vec3 from){
        BlockPos best=null;double bestD=Double.MAX_VALUE;var table=b.offset(FamiliarRoom.TABLE);
        for(var at:BlockPos.betweenClosed(table.offset(-2,0,-2),table.offset(2,0,2))){
            if(!TraceBlock.is(l.getBlockState(at),TraceBlock.Kind.CHAIR))continue;var c=Vec3.atBottomCenterOf(at);
            if(!l.getEntitiesOfClass(Player.class,new AABB(at).inflate(.2,.5,.2)).isEmpty())continue;
            double d=c.distanceToSqr(from);if(d<bestD){bestD=d;best=at.immutable();}
        }
        return best;
    }
    /** Steps toward the next point, a walking pace. */
    static void walk(ServerLevel l,LiteraryActor a){
        var path=WALKS.get(a.getUUID());if(path==null||path.isEmpty())return;var to=path.get(0);var at=a.position();
        double dx=to.x-at.x,dz=to.z-at.z,dist=Math.sqrt(dx*dx+dz*dz);
        if(dist<.08){path.remove(0);if(path.isEmpty())WALKS.remove(a.getUUID());return;}
        double step=Math.min(.1,dist);float yaw=(float)Math.toDegrees(Math.atan2(-dx,dz));
        a.moveTo(at.x+dx/dist*step,to.y,at.z+dz/dist*step,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
    }
    /** Into the chair, facing the table. */
    static void sit(ServerLevel l,BlockPos b,LiteraryActor a,CompoundTag t){
        WALKS.remove(a.getUUID());if(!t.contains("Chair"))return;var chair=b.offset(BlockPos.of(t.getLong("Chair")));
        if(!TraceBlock.is(l.getBlockState(chair),TraceBlock.Kind.CHAIR))return;
        var c=Vec3.atBottomCenterOf(chair);var d=Vec3.atBottomCenterOf(b.offset(FamiliarRoom.TABLE)).subtract(c);float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));
        a.moveTo(c.x,c.y,c.z,yaw,0);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
    }
    /** Up out of the chair, a word, and away by the door they came in by. */
    static void leave(ServerLevel l,BlockPos b,LiteraryActor a,CompoundTag t,int occ){
        var v=cast(a.phase());if(v!=null)speak(a,v.voice().leaving());
        int door=t.getInt("Door");var path=new ArrayList<Vec3>();
        if(t.contains("Chair")){var c=Vec3.atBottomCenterOf(b.offset(BlockPos.of(t.getLong("Chair"))));path.add(c.add(c.subtract(Vec3.atBottomCenterOf(b.offset(FamiliarRoom.TABLE))).normalize()));}
        path.add(Vec3.atBottomCenterOf(b.offset(INSIDE[door])));
        var start=path.get(0);a.moveTo(start.x,start.y,start.z,a.getYRot(),0);
        WALKS.put(a.getUUID(),path);t.putInt("Stage",LEAVING);t.putInt("StageAt",occ);
        l.playSound(null,b.offset(DOORS[door]),SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.6F,1F);
    }
    /** Said above their head, for whoever is there. */
    static void speak(LiteraryActor a,String text){a.say(text);a.speaks(text);}
    static void greetAll(ServerLevel l,LiteraryActor a,List<ServerPlayer> here){
        var v=cast(a.phase());if(v==null)return;
        if(a.phase()==HOLLOWAY&&here.stream().anyMatch(p->HollowayVignette.personal(LabyrinthData.get(l.getServer()),p.getUUID()).getBoolean("Looted"))){speak(a,"One of you has been in my barrel. I'll sit anyway.");return;}
        speak(a,v.voice().greet());
    }

    // ------------------------------------------------------------------ talking and eating
    /** A hand on a visitor: food is offered, anything else is a word. */
    public static void interact(ServerPlayer p,LiteraryActor a,ItemStack held){
        var v=cast(a.phase());if(v==null)return;
        if(!held.isEmpty()&&held.get(net.minecraft.core.component.DataComponents.FOOD)!=null){offer(p,a,held);return;}
        talk(p,a,v);
    }
    static void talk(ServerPlayer p,LiteraryActor a,Visitor v){
        var key="kitchen_"+v.phase();var mem=memory(p.server,p.getUUID()).getCompound("V"+v.phase());
        Talk.begin(p,key,v.voice().name());
        if(mem.contains("Last"))Talk.line(p,v.voice().name(),String.format(v.voice().lastTime(),Dish.load(mem.getCompound("Last")).strokes()));
        else Talk.line(p,v.voice().name(),v.voice().greet());
        Talk.ask(p,List.of(
            Talk.reply("Are you hungry?",who->{Talk.line(who,v.voice().name(),v.voice().hungry());speak(a,v.voice().hungry());Talk.end(who);}),
            Talk.reply("Where have you come from?",who->{var m=memory(who.server,who.getUUID());var vm=m.getCompound("V"+v.phase());int told=vm.getInt("Told");
                var line=v.voice().history().get(told%v.voice().history().size());vm.putInt("Told",told+1);m.put("V"+v.phase(),vm);memory(who.server,who.getUUID(),m);
                Talk.line(who,v.voice().name(),line);speak(a,line);Talk.end(who);}),
            Talk.reply("Stay as long as you like.",who->{var t=shared(who.server);if(t.getInt("Who")==v.phase()){t.putInt("StageAt",t.getInt("StageAt")+60);shared(who.server,t);}Talk.line(who,v.voice().name(),"Thanks. I might.");speak(a,"Thanks. I might.");Talk.end(who);}),
            Talk.act("Leave them be.",Talk::close)));
    }
    /**
     * Something to eat, held out. They take one, eat it, and say what they make of it once they have; the reader's own memory and the
     * kitchen's book keep it. Returns what they will say, or null if they did not take it. Public for tests.
     */
    public static @Nullable String offer(ServerPlayer p,LiteraryActor a,ItemStack held){
        var v=cast(a.phase());if(v==null)return null;var s=p.server;var l=p.serverLevel();var t=shared(s);
        if(a.phase()==HOLLOWAY&&HollowayVignette.personal(LabyrinthData.get(s),p.getUUID()).getBoolean("Looted")){speak(a,"Not from you.");return null;}
        var dish=KitchenCooking.strokes(held);
        if(dish==null){speak(a,v.voice().refuse());return null;}
        if(t.getInt("Who")==a.phase()&&t.getInt("Meals")>=MEALS){speak(a,v.voice().full());return null;}
        boolean made=KitchenCooking.read(held)!=null;var eaten=held.copyWithCount(1);String foodName=eaten.getHoverName().getString();
        if(!p.getAbilities().instabuild)held.shrink(1);
        // A bowl comes back to the hand that held it out.
        if(eaten.is(Items.MUSHROOM_STEW)||eaten.is(Items.RABBIT_STEW)||eaten.is(Items.BEETROOT_SOUP)||made&&dish.texture()!=Texture.CRISP){var bowl=new ItemStack(Items.BOWL);if(!p.getInventory().add(bowl))p.drop(bowl,false);}
        var mem=memory(s,p.getUUID());var vm=mem.getCompound("V"+v.phase());
        String line=response(v,dish,made&&!vm.getBoolean("Loved"));
        if(made&&score(v.palate(),dish)>=2)vm.putBoolean("Loved",true);
        vm.putInt("Fed",vm.getInt("Fed")+1);vm.put("Last",dish.save());mem.put("V"+v.phase(),vm);memory(s,p.getUUID(),mem);
        if(t.getInt("Who")==a.phase()){t.putBoolean("Fed",true);t.putInt("FedAt",t.getInt("Occ"));t.putInt("Meals",t.getInt("Meals")+1);shared(s,t);}
        a.setItemInHand(InteractionHand.MAIN_HAND,eaten);
        for(int i=0;i<3;i++)l.playSound(null,a.blockPosition(),SoundEvents.GENERIC_EAT,SoundSource.NEUTRAL,.5F,.9F+l.getRandom().nextFloat()*.2F);
        l.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,eaten),a.getX(),a.getEyeY()-.2,a.getZ(),8,.1,.05,.1,.02);
        PENDING.add(new Pending(a.getUUID(),line,l.getGameTime()+40));
        write(s,v.voice().book()+" ate "+(made?dish.strokes():foodName.toLowerCase(java.util.Locale.ROOT))+(made&&!dish.by().isEmpty()?", made by "+dish.by():"")+", from "+p.getGameProfile().getName()+" and said: \""+line+"\"");
        return line;
    }
    static int score(Palate pal,Dish d){
        int n=0;if(pal.likeT().contains(d.texture()))n++;if(pal.likeS().contains(d.seasoning()))n++;if(pal.likeC().contains(d.cook()))n++;
        if(pal.hateT().contains(d.texture()))n--;if(pal.hateS().contains(d.seasoning()))n--;if(pal.hateC().contains(d.cook()))n--;return n;
    }
    /** What they make of it: the cook first if it was wrong, then what they cannot stand, then how much they liked it. */
    static String response(Visitor v,Dish d,boolean firstLove){
        var pal=v.palate();var voice=v.voice();int score=score(pal,d);
        if(d.cook()==Cook.UNDERDONE&&!pal.likeC().contains(Cook.UNDERDONE))return voice.underdone();
        if(d.cook()==Cook.BURNT&&!pal.likeC().contains(Cook.BURNT))return voice.burnt();
        if(score<=0){if(pal.hateS().contains(d.seasoning())&&voice.dislike().containsKey(d.seasoning()))return voice.dislike().get(d.seasoning());if(pal.hateT().contains(d.texture())&&voice.dislike().containsKey(d.texture()))return voice.dislike().get(d.texture());}
        if(score>=2)return firstLove?voice.loved()+" "+voice.story():voice.loved();
        if(d.cook()==Cook.BURNT)return voice.burnt();
        return score==1?voice.good():voice.polite();
    }
    /** Struck: they get up and go. */
    public static void struck(ServerPlayer p,LiteraryActor a){
        var l=p.serverLevel();var b=base(p.server);var t=shared(p.server);if(b==null||t.getInt("Who")!=a.phase()||t.getInt("Stage")==LEAVING)return;
        speak(a,"All right. I'll go.");leave(l,b,a,t,t.getInt("Occ"));shared(p.server,t);
    }

    // ------------------------------------------------------------------ the kitchen's book
    public static final int ENTRIES=40;
    public static List<String> book(MinecraftServer s){var out=new ArrayList<String>();for(var e:shared(s).getList("Book",Tag.TAG_STRING))out.add(e.getAsString());return out;}
    static void write(MinecraftServer s,String entry){var t=shared(s);var list=t.getList("Book",Tag.TAG_STRING);list.add(StringTag.valueOf(entry));while(list.size()>ENTRIES)list.remove(0);t.put("Book",list);shared(s,t);}
    /** The book lying on the counter by the door, for anyone to read. */
    static void ensureBook(ServerLevel l,BlockPos b){
        var top=Vec3.atBottomCenterOf(b.offset(BOOK)).add(0,1.01,0);
        if(!l.getEntitiesOfClass(net.minecraft.world.entity.Display.ItemDisplay.class,new AABB(b.offset(BOOK).above()).inflate(.2),d->d.getTags().contains(BOOK_TAG)).isEmpty())return;
        KitchenFittings.lay(l,top,new ItemStack(Items.WRITABLE_BOOK),90,BOOK_TAG,.45F);
    }
    static ItemStack written(MinecraftServer s){
        var entries=book(s);var pages=new ArrayList<String>();
        pages.add("What was eaten here.\n\nWho came to the table, what they were given, and what they said about it.");
        if(entries.isEmpty())pages.add("Nothing yet.");
        var page=new StringBuilder();
        for(int i=entries.size()-1;i>=0;i--){var e=entries.get(i);if(page.length()>0&&page.length()+e.length()>230){pages.add(page.toString());page.setLength(0);}page.append(page.length()>0?"\n\n":"").append(e);}
        if(page.length()>0)pages.add(page.toString());
        return HouseWriting.book("What was eaten here","the kitchen",HouseWriting.WritingStyle.PLAIN,pages);
    }
    /** A hand on the book: read it. It cannot be taken. */
    public static boolean read(ServerPlayer p){
        var book=written(p.server);var c=new SimpleContainer(1);c.setItem(0,book);
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new LecternMenu(id,c,new SimpleContainerData(1)){@Override public boolean clickMenuButton(Player pl,int button){return button!=3&&super.clickMenuButton(pl,button);}},Component.literal("What was eaten here")));
        return true;
    }

    @SubscribeEvent public static void stopped(ServerStoppedEvent e){WALKS.clear();PENDING.clear();}
}
