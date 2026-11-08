package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.TruantFittingBlock.Kind.*;

/**
 * 0.4.359, Johnny Truant's copying room (docs/TRUANT_SENTENCE_0_4_359.md), the last deep pair of the owner's "Johnny Truant · Encounter
 * Pairs": "He supplies the line the reader lost from an account and waits while they compare it with the physical evidence." / "He
 * quotes the reader's own words back with one sentence missing, changing who sounds responsible for what happened. The original account
 * is still available. A friend can see which passage he left out while the reader decides whether to engage."
 * <p>
 * A dark room of pigeonholes with a desk at the end, one lantern on it and a drip from the ceiling over the page. On the page is a
 * reader's account of their history with him, written as they would say it, from his record of what they said and what happened.
 * <ul>
 * <li><b>Help.</b> The drip has run one line of it. He gives the reader the line, and the blotter, which took the wet ink off backwards,
 * shows it is the same words. That is his help, and it is kept.</li>
 * <li><b>Turned (SENTENCE).</b> He reads the reader's account aloud to everyone there with one sentence left out: his harm, a promise
 * kept, a favour done or a refusal, so that what happened sounds like the reader's doing. The page on the desk still has every line, and
 * anyone who reads it can say the missing one aloud; he stops. Walking on is always open.</li>
 * </ul>
 * Nothing hostile happens with anyone under twenty passes in the place (Truant.gated), and with nothing to leave out it is a claim.
 */
public final class TruantSentence {
    private TruantSentence(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.TRUANT_SENTENCE;
    public static final int HELP_SENTENCE=1024,READ_EVERY=50;
    public static final BlockPos PAGE_AT=new BlockPos(0,1,-15),BLOTTER_AT=new BlockPos(-1,1,-15),LANTERN_AT=new BlockPos(2,1,-15),DRIP_AT=new BlockPos(0,2,-15);
    public static final Vec3 HOME=new Vec3(.5,0,-16.4);
    /** How far in the one he wants must be before he starts reading: past the first of the pigeonholes. */
    public static final double READ_FROM=-8;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static boolean is(@Nullable LabyrinthPlace p){return p==PLACE;}

    // ------------------------------------------------------------------ the account
    public enum Role{HELP,TOLD,ANSWER,PROMISE,KEPT,FAVOUR,HARM,OUTCOME,AFTER}
    /** One sentence of a reader's account, in their voice, and what it tells. */
    public record Sentence(String text,Role role){}
    static final int[] HELP_BITS={Truant.HELP_DOOR,Truant.HELP_STEADY,Truant.HELP_CATCH,Truant.HELP_LAMP,TruantService.HELP_CHALK,TruantService.HELP_KNOCK,TruantService.HELP_BRAKE,TruantYard.HELP_SHED,TruantYard.HELP_LANTERN,TruantYard.HELP_GATE,HELP_SENTENCE};
    static final String[] HELP_LINES={"He held the heavy door until I was through.","On the ledge he steadied me for one step.","When I slipped on the ledge, he caught me.",
        "He made room for me at his lamp.","He chalked the safe way at the junction.","He knocked on the pipe when the way round the corner was clear.",
        "He held the lift's brake until I was aboard.","He wedged the shed door open in the squall.","He signalled me across when the wind let up.",
        "He chained the gate behind me.","He gave me back a line I'd lost."};
    /** What he did and how it ended, in the reader's words: left without the first, the second sounds like the reader's own doing. */
    static @Nullable String[] harm(String kind){
        return switch(kind){
            case "shove"->new String[]{"He came up behind me on the ledge and pushed.","I went over the edge."};
            case "doorway"->new String[]{"He stood in the doorway and wouldn't let me past.","I didn't get through that door the first time."};
            case "cover"->new String[]{"He closed his hand round the lamp and took my wrist.","I lost my light."};
            case "arrow"->new String[]{"He turned the chalk arrow toward the drop.","I took the branch that drops."};
            case "knock"->new String[]{"He knocked the all-clear just as the pipe began to shake.","I walked into the steam."};
            case "brake"->new String[]{"He let the brake go while I was on the sill.","I fell down the shaft after the car."};
            case "shed"->new String[]{"He pulled the wedge and dropped the hasp on me.","I ended up shut in the shed."};
            case "false_wind_signal"->new String[]{"He signalled me out into the gust.","I went out into the gust."};
            case "stock_gate"->new String[]{"He let the chain go while I was in the chute.","I got caught among the herd."};
            case "coffee"->new String[]{"He held the thermos shut until I gave him an account.","I gave him what he wanted."};
            case "sentence"->new String[]{"He read my words back with a line left out.","Everyone there heard it."};
            default->null;
        };
    }
    /** The first sentence of one of his pages, without its quotation marks. */
    static String first(String line){var s=line.replace("\"","").trim();int dot=s.indexOf(". ");return dot<0?s:s.substring(0,dot+1);}
    /** A reader's account of their history with him, in order, from his record: what he did, what they told him and promised, his harm. */
    public static List<Sentence> account(CompoundTag own){
        var out=new ArrayList<Sentence>();int help=own.getInt("Help"),n=0;
        for(int i=0;i<HELP_BITS.length&&n<2;i++)if((help&HELP_BITS[i])!=0){out.add(new Sentence(HELP_LINES[i],Role.HELP));n++;}
        var page=TruantTexts.page(own);boolean kept=own.getBoolean("Kept");String keptPlace=own.getString("KeptPlace");
        String promise=switch(own.getString("Promise")){case "agreed"->"I said I'd come back.";case "maybe"->"I said maybe.";case "refused"->"I told him no.";default->"";};
        if(kept){
            if(!promise.isEmpty())out.add(new Sentence(promise,Role.PROMISE));
            out.add(new Sentence("I came back and told him about "+StaircaseProse.place(keptPlace)+".",Role.KEPT));
            if(page!=null){if(!page.place().equals(keptPlace))out.add(new Sentence("I told him about "+StaircaseProse.place(page.place())+".",Role.TOLD));out.add(new Sentence(first(page.lines().get(0)),Role.ANSWER));}
        }else{
            if(page!=null){out.add(new Sentence("I told him about "+StaircaseProse.place(page.place())+".",Role.TOLD));out.add(new Sentence(first(page.lines().get(0)),Role.ANSWER));}
            if(!promise.isEmpty())out.add(new Sentence(promise,Role.PROMISE));
        }
        var favours=own.getList("Favours",Tag.TAG_COMPOUND);
        if(!favours.isEmpty()){var f=favours.getCompound(favours.size()-1);
            if(f.getBoolean("Did"))out.add(new Sentence("He asked me a favour, and I did it.",Role.FAVOUR));
            else if(f.getString("Answer").equals("refused"))out.add(new Sentence("He asked me a favour, and I said no.",Role.FAVOUR));}
        var h=harm(own.getString("Harm"));
        if(h!=null&&own.getInt("HarmAt")>0){out.add(new Sentence(h[0],Role.HARM));out.add(new Sentence(h[1],Role.OUTCOME));}
        if(own.getInt("AfterHarm")>0)out.add(new Sentence("Afterwards he said he wasn't asking me for anything.",Role.AFTER));
        return out;
    }
    static int find(List<Sentence> account,Role role,@Nullable String text){for(int i=0;i<account.size();i++)if(account.get(i).role()==role&&(text==null||account.get(i).text().equals(text)))return i;return -1;}
    /** The sentence he leaves out, so the rest sounds like the reader's doing: his harm, a kept promise, a favour done, a refusal; or -1. */
    public static int omitted(List<Sentence> account){
        int i=find(account,Role.HARM,null);if(i>=0)return i;
        i=find(account,Role.KEPT,null);if(i>=0)return i;
        i=find(account,Role.FAVOUR,"He asked me a favour, and I did it.");if(i>=0)return i;
        return find(account,Role.PROMISE,"I told him no.");
    }
    /** The line the wet has taken: the reader's own words if he has them, else their promise, else the first thing he did; or -1. */
    public static int lost(List<Sentence> account){
        int i=find(account,Role.ANSWER,null);if(i>=0)return i;
        i=find(account,Role.PROMISE,null);if(i>=0)return i;
        return account.isEmpty()?-1:0;
    }
    /** Whether he has anything to leave out of this reader's account. */
    public static boolean due(CompoundTag own){return omitted(account(own))>=0;}
    static boolean restored(CompoundTag own){return own.getInt("SentenceRestoredAt")>=Truant.meeting(own)&&own.getInt("SentenceRestoredAt")>0;}
    /** A run line: its words gone, only their length left. */
    static String blot(String line){var b=new StringBuilder();for(char c:line.toCharArray())b.append(c==' '?' ':'~');return b.toString();}
    static String backwards(String line){return new StringBuilder(line).reverse().toString();}

    // ------------------------------------------------------------------ the room
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,F);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,F);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    static BlockState air(){return Blocks.AIR.defaultBlockState();}
    static BlockState fitting(TruantFittingBlock.Kind k,Direction f,int stage){return TruantFittingBlock.of(k,f,stage);}

    /** The copying room: a dark passage, a long room lined with pigeonholes, his desk at the end under a drip, the way on past it. */
    public static void build(ServerLevel l,BlockPos b){
        var brick=Blocks.DEEPSLATE_BRICKS.defaultBlockState();var tile=Blocks.DEEPSLATE_TILES.defaultBlockState();var oak=Blocks.DARK_OAK_PLANKS.defaultBlockState();
        box(l,b,-7,-1,-22,7,5,0,brick);
        box(l,b,-1,-1,-5,1,-1,-1,tile);box(l,b,-1,0,-5,1,2,-1,air());
        box(l,b,-5,-1,-18,5,-1,-6,oak);box(l,b,-5,0,-18,5,2,-6,air());
        for(int z=-7;z>=-17;z--){if(z==-12)continue;for(int y=0;y<=1;y++){set(l,b,-5,y,z,fitting(PIGEONHOLES,Direction.EAST,0));set(l,b,5,y,z,fitting(PIGEONHOLES,Direction.WEST,0));}}
        // The desk, the page under the drip, the blotter beside it, the lantern that is the room's only light.
        for(int x=-2;x<=2;x++)set(l,b,x,0,-15,oak);
        set(l,b,PAGE_AT,fitting(PAGE,Direction.NORTH,0));set(l,b,BLOTTER_AT,fitting(BLOTTER,Direction.NORTH,0));set(l,b,LANTERN_AT,Blocks.LANTERN.defaultBlockState());
        set(l,b,-2,1,-15,Blocks.CANDLE.defaultBlockState());
        set(l,b,DRIP_AT,Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION,Direction.DOWN).setValue(PointedDripstoneBlock.THICKNESS,DripstoneThickness.TIP));
        set(l,b,DRIP_AT.getX(),4,DRIP_AT.getZ(),Blocks.WATER.defaultBlockState());
        set(l,b,-2,0,-17,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.SOUTH));
        // The way on, round the end of the desk and out of the far corner.
        box(l,b,3,-1,-20,4,-1,-19,tile);box(l,b,3,0,-20,4,2,-19,air());
        LabyrinthBuilder.entrance(l,b,brick,tile,brick);
        LabyrinthBuilder.doors(l,b,PLACE);
    }

    // ------------------------------------------------------------------ this meeting (not saved)
    static final Set<UUID> SUPPLIED=new HashSet<>(),READ_HAUNT=new HashSet<>();
    static int stage,index,omit=-1;static long nextAt;static List<Sentence> reading=List.of();static final List<String> SPOKEN=new ArrayList<>();
    public static void clearAll(){newScene();}
    static void newScene(){SUPPLIED.clear();READ_HAUNT.clear();stage=0;index=0;omit=-1;nextAt=0;reading=List.of();SPOKEN.clear();}
    /** For tests: the reading's stage (0 not begun, 1 reading, 2 read through, 3 stopped), what he has said aloud, and what he left out. */
    public static int stage(){return stage;}
    public static List<String> spoken(){return List.copyOf(SPOKEN);}
    public static String left(){return omit>=0&&omit<reading.size()?reading.get(omit).text():"";}
    static void speak(LiteraryActor a,String line){Truant.say(a,line);SPOKEN.add(line);}
    static boolean haunting(){var sc=Truant.scene;return sc!=null&&sc.place==PLACE&&sc.turn==Truant.Turn.SENTENCE&&sc.target!=null;}

    // ------------------------------------------------------------------ hands on
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;var kind=s.getValue(TruantFittingBlock.KIND);if(kind!=PAGE&&kind!=BLOTTER&&kind!=PIGEONHOLES)return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(!is(place))return false;
        var l=p.serverLevel();var body=Truant.body(l);var a=body!=null&&PLACE.id().equals(body.scene())?body:null;
        switch(kind){
            case PIGEONHOLES->PuzzleRooms.say(p,"Pigeonholes, every one stuffed with folded sheets in the same small, even hand.");
            case PAGE->{l.playSound(null,pos,PuzzleRegistry.TRUANT_PAGE.get(),SoundSource.BLOCKS,.7F,1F);readPage(p,a);}
            default->readBlotter(p,a);
        }
        return true;
    }
    /**
     * The page: the reader's own account, one line run by the wet, and (if he is there) the line given back. During his reading it is
     * the account of the one he is reading to, whole, and whoever reads it can say the line he left out.
     */
    static void readPage(ServerPlayer p,@Nullable LiteraryActor a){
        var sc=Truant.scene;boolean haunt=haunting()&&stage>=1;UUID whose=haunt?sc.target:p.getUUID();
        var own=Truant.record(p.server,whose);var account=haunt?reading:account(own);
        Talk.begin(p,"truant_page","");
        if(account.isEmpty()){Talk.line(p,"","A ruled sheet with your name written at the top, and nothing under it yet.");Talk.end(p);return;}
        if(haunt&&!whose.equals(p.getUUID())){var t=p.server.getPlayerList().getPlayer(whose);String name=t==null?"someone else":t.getGameProfile().getName();
            Talk.line(p,"","The page on the desk is "+name+"'s account, in his small, even hand. He is reading from a copy.");}
        else Talk.line(p,"","The page on the desk is an account of you and him, in his small, even hand, written as if you were saying it.");
        int lost=haunt||restored(own)?-1:lost(account);
        for(int i=0;i<account.size();i++)Talk.line(p,"",i==lost?blot(account.get(i).text()):account.get(i).text());
        if(haunt){
            READ_HAUNT.add(p.getUUID());
            if(stage<3&&omit>=0){String missing=reading.get(omit).text();
                Talk.ask(p,Talk.reply("\""+missing+"\"",q->contradict(q,a)),Talk.act("Leave it.",Talk::end));}
            else Talk.end(p);
            return;
        }
        if(lost>=0)Talk.line(p,"","One line has run where the drip landed on it. Only its length is left.");
        if(lost>=0&&a!=null&&!SUPPLIED.contains(p.getUUID())){
            SUPPLIED.add(p.getUUID());String who=Truant.who(own);String line=account.get(lost).text();
            Talk.line(p,who,"The wet got at that one. It said, \""+line+"\"");Talk.line(p,who,"The blotter took the ink when it ran. Look, if you want.");
            speak(a,"The wet got at that one. It said, \""+line+"\"");
            var r=Truant.record(p.server,p.getUUID());r.putString("SentenceLost",line);Truant.record(p.server,p.getUUID(),r);
        }
        Talk.end(p);
    }
    /** The blotter: the run line, taken off backwards. Read after he gave it back, it is his help, and it is kept. */
    static void readBlotter(ServerPlayer p,@Nullable LiteraryActor a){
        var own=Truant.record(p.server,p.getUUID());var account=account(own);int lost=lost(account);
        Talk.begin(p,"truant_page","");
        if(lost<0||haunting()){Talk.line(p,"","A blotter, worn at the corners. Old blots, nothing you can read.");Talk.end(p);return;}
        String line=account.get(lost).text();
        Talk.line(p,"","Where the wet page lay, the blotter took a line of ink off backwards:");Talk.line(p,"",backwards(line));
        if(SUPPLIED.contains(p.getUUID())&&!restored(own)){
            Talk.line(p,"","Read backwards, it is the line he gave you, word for word.");
            String who=Truant.who(own);Talk.line(p,who,"Same words. I don't change anybody's words.");if(a!=null)speak(a,"Same words. I don't change anybody's words.");
            Truant.helped(p,HELP_SENTENCE);own=Truant.record(p.server,p.getUUID());own.putInt("SentenceRestoredAt",Truant.meeting(own));own.putInt("SentenceHelpAt",Truant.meeting(own));Truant.record(p.server,p.getUUID(),own);
        }else Talk.line(p,"","Read backwards, it is a line of yours.");
        Talk.end(p);
    }
    /** Someone who has read the page says the missing line aloud. He stops. */
    static void contradict(ServerPlayer p,@Nullable LiteraryActor a){
        var sc=Truant.scene;if(!haunting()||stage>=3){Talk.end(p);return;}
        boolean own=p.getUUID().equals(sc.target);stage=3;
        if(a!=null){speak(a,own?"That's not the part I was reading.":"I know what it says.");Truant.gesture(a,0);}
        var me=Truant.record(p.server,p.getUUID());me.putInt(own?"Restored":"Interrupted",me.getInt(own?"Restored":"Interrupted")+1);Truant.record(p.server,p.getUUID(),me);
        if(!own){var t=Truant.record(p.server,sc.target);t.putInt("Contradicted",t.getInt("Contradicted")+1);Truant.record(p.server,sc.target,t);}
        Talk.end(p);
    }
    /** Talking to him while he reads. */
    static boolean converse(ServerPlayer p,LiteraryActor a){
        if(!haunting()||stage!=1||!p.getUUID().equals(Truant.scene.target))return false;
        Talk.begin(p,Truant.KEY,Truant.who(Truant.record(p.server,p.getUUID())));Talk.line(p,Truant.who(Truant.record(p.server,p.getUUID())),"Let me finish.");Talk.end(p);
        return true;
    }
    static void struck(){if(stage==1)stage=3;}

    // ------------------------------------------------------------------ from Truant's clock
    static void tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,Truant.Scene sc){
        long now=l.getGameTime();var target=Truant.target(readers);
        if(sc.turn==Truant.Turn.SENTENCE&&target!=null){reading(l,b,a,readers,target,now);if(now%10==0)page(l,b,0);return;}
        var near=Truant.nearest(a,readers);if(near!=null)Truant.face(a,near.getEyePosition());
        Truant.gesture(a,Truant.PAGE);
        if(now%10==0){
            int stage=2;for(var p:readers){var own=Truant.record(p.server,p.getUUID());var account=account(own);if(account.isEmpty())continue;stage=Math.min(stage,lost(account)>=0&&!restored(own)?1:0);}
            page(l,b,stage);
        }
    }
    static void page(ServerLevel l,BlockPos b,int stage){var at=b.offset(PAGE_AT);var s=l.getBlockState(at);if(TruantFittingBlock.is(s,PAGE)&&s.getValue(TruantFittingBlock.STAGE)!=stage)l.setBlock(at,s.setValue(TruantFittingBlock.STAGE,stage),F);
        var bl=b.offset(BLOTTER_AT);var t=l.getBlockState(bl);int inked=stage==1?1:0;if(TruantFittingBlock.is(t,BLOTTER)&&t.getValue(TruantFittingBlock.STAGE)!=inked)l.setBlock(bl,t.setValue(TruantFittingBlock.STAGE,inked),F);}
    /** The reading: once the one he wants is in among the pigeonholes, their account aloud, a line at a time, one line short. */
    static void reading(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,ServerPlayer target,long now){
        Truant.face(a,target.getEyePosition());
        if(stage==0){
            if(Truant.rel(b,target.position()).z>READ_FROM)return;
            reading=account(Truant.record(target.server,target.getUUID()));omit=omitted(reading);
            if(omit<0){stage=3;return;}
            stage=1;index=0;nextAt=now+40;Truant.gesture(a,Truant.PAGE);
            l.playSound(null,b.offset(PAGE_AT),PuzzleRegistry.TRUANT_PAGE.get(),SoundSource.BLOCKS,.8F,.9F);
            speak(a,"Listen. Your own words. I took them down the way you said them.");
            return;
        }
        if(stage!=1||now<nextAt)return;
        if(index==omit){
            index++;
            // The line he leaves out has gone by, aloud, in front of everyone: that is kept against him.
            Truant.harm(target,"sentence","Misquoted");var own=Truant.record(target.server,target.getUUID());own.putString("SentenceOmitted",reading.get(omit).text());
            var heard=own.getList("SentencesOmitted",Tag.TAG_STRING);heard.add(StringTag.valueOf(reading.get(omit).text()));while(heard.size()>8)heard.remove(0);own.put("SentencesOmitted",heard);
            Truant.record(target.server,target.getUUID(),own);
            for(var p:readers)if(p!=target){var w=Truant.record(p.server,p.getUUID());w.putInt("HeardMisquoted",w.getInt("HeardMisquoted")+1);Truant.record(p.server,p.getUUID(),w);}
        }
        if(index<reading.size()){speak(a,"\""+reading.get(index).text()+"\"");index++;nextAt=now+READ_EVERY;return;}
        stage=2;Truant.gesture(a,0);speak(a,"That's how you told it.");
        String who=Truant.who(Truant.record(target.server,target.getUUID()));String missing=reading.get(omit).text();
        Talk.begin(target,Truant.KEY,who);Talk.line(target,who,"That's how you told it.");
        Talk.ask(target,
            Talk.reply("\""+missing+"\"",q->contradict(q,a)),
            Talk.reply("That's not what I said.",q->{var r=Truant.record(q.server,q.getUUID());r.putInt("Disputed",r.getInt("Disputed")+1);Truant.record(q.server,q.getUUID(),r);Talk.line(q,who,"It's what you said. Every word of it's yours.");speak(a,"It's what you said. Every word of it's yours.");Talk.end(q);}),
            Talk.reply("That's all you get.",q->Truant.limited(q,a)));
    }
}
