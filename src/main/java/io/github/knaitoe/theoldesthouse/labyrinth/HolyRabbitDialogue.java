package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
/** Hope comes from the observed returning rabbit; the explanation changes after the actual wound. */
public final class HolyRabbitDialogue {
    private HolyRabbitDialogue(){}
    private static boolean once(CompoundTag own,int bit){int mask=own.getInt("RabbitDialogue0490");if((mask&bit)!=0)return false;own.putInt("RabbitDialogue0490",mask|bit);return true;}
    private static String name(CompoundTag own){String name=own.getString("RabbitName");return name.isBlank()?"Slaney":name;}
    private static void voice(ServerPlayer p,LiteraryActor father,String words){if(father!=null){father.say(words);father.getPersistentData().putLong("SayUntil0488",p.level().getGameTime()+100);}p.displayClientMessage(Component.literal("Father: "+words),false);}
    public static void tick(ServerPlayer p,LiteraryActor father,CompoundTag own,int night,int ticks){
        if(night==1&&ticks>=15&&once(own,1))p.displayClientMessage(Component.literal("Before you woke, a small rabbit sat beside you in the hollow. One ear had a brown tip. You can still remember its warmth."),false);
        // As in Jones's story, the boy names the rabbit and the boy believes it will come back; Father goes along with it.
        if(night==1&&(own.getInt("RabbitMornings")&2)!=0&&once(own,2)){voice(p,father,"The little one from your dream. Has he got a name?");p.displayClientMessage(Component.literal("You tell him: "+name(own)+". A thing with a name is real."),false);}
        if(night==1&&own.getInt("AteNight")==1&&once(own,4)){p.displayClientMessage(Component.literal("You tell Father the little one from your dream will come back."),false);voice(p,father,"Maybe he will.");}
        if(night==2&&(own.getInt("RabbitMornings")&4)!=0&&own.getInt("Meals")>=1&&once(own,8)){
            own.putBoolean("RabbitBelief0490",true);own.putBoolean("RabbitBelieved0490",true);
            p.displayClientMessage(Component.literal(name(own)+" is beside the tree again, one ear brown at the tip."),false);
            voice(p,father,"There he is. I told you he could find us. We only have to get through another night.");
        }
        if(night>=2&&own.getInt("MealNight")>=2&&!own.getBoolean("WoundSeen0488")&&once(own,16))voice(p,father,"I went by the creek. That's why my trousers are wet. It's a good sign; water runs to people. Don't let the meat get cold while you worry about me.");
        // On the last day he points the reader to what he cut, once, if they have not looked up at it yet.
        if(night>=3&&ticks>=300&&own.getBoolean("CarvingAppeared")&&!own.getBoolean("CarvingRead")&&once(own,128))voice(p,father,"I cut something into the tree for you. Up high, where the snow can't get at it.");
        if(own.getBoolean("CarvingRead")&&once(own,32))p.displayClientMessage(Component.literal("Your initials are cut beside your mother's name. Below them is a little rabbit, with "+name(own)+" cut under its feet. He has gone round her name so many times the bark is coming away."),false);
        if(own.getBoolean("WoundSeen0488")&&once(own,64)){
            own.putBoolean("RabbitTruth0490",true);own.putBoolean("RabbitBelief0490",false);
            p.displayClientMessage(Component.literal("His trouser leg is cut open. The cloth tied round his thigh has bled through. Out by the snow, "+name(own)+" sits where he sat this morning."),false);
            voice(p,father,"You needed to eat. That is all. You needed to stay awake.");
        }
    }
    public static void meal(ServerPlayer p,LiteraryActor father,CompoundTag own,int night){voice(p,father,night==1?"I found something for us. Small bites. There will be a morning.":night==2?name(own)+" found us again. Here. Eat yours first.":"There's a little left. I already ate on the way back.");}
    public static void talk(ServerPlayer p,LiteraryActor father,CompoundTag own){
        own.putInt("Drowse",Math.max(0,own.getInt("Drowse")-100));boolean truth=own.getBoolean("WoundSeen0488");int day=Math.max(1,own.getInt("Night"));var icons=new HashMap<Integer,ItemStack>();
        icons.put(0,LiteraryChoiceMenu.icon(Items.RABBIT_HIDE,truth?"Ask where the meat came from":day==1?"Ask why the rabbit from your dream is here":day==2?"Ask how "+name(own)+" came back":"Ask him to point out "+name(own)));
        icons.put(2,LiteraryChoiceMenu.icon(Items.SNOWBALL,truth?"Ask whether he can walk":day==1?"Ask when the blizzard will stop":day==2?"Ask why his trousers are wet":"Ask why he cannot stand"));
        icons.put(4,LiteraryChoiceMenu.icon(Items.CAMPFIRE,truth?"Ask him to stay with you":day==1?"Ask whether Mother is waiting":day==2?"Ask whether you will get home":"Ask him not to go hunting again"));icons.put(8,LiteraryChoiceMenu.icon(Items.PAPER,"Sit with him quietly"));
        LiteraryChoiceMenu.open(p,"Father",icons,()->p.isAlive()&&!p.isSpectator()&&LiteraryVignettes.inside(p,LabyrinthPlace.HOLY_RABBIT)&&p.distanceToSqr(father)<=36,slot->{
            var d=LabyrinthData.get(p.server);var now=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.HOLY_RABBIT);String words;
            int today=Math.max(1,now.getInt("Night"));
            if(now.getBoolean("LastHunt0490"))words=slot==4?"I have to get you one more morning.":"Hunting.";
            else if(now.getBoolean("WoundSeen0488"))words=slot==0?"You needed to eat. I couldn't let the snow have you.":slot==2?"The poles will hold me. You go ahead. Keep looking for the light.":"I will be right behind you. Keep going when you see the light.";
            else if(today>=3)words=slot==0?name(now)+" is by the tree. My arm won't lift. Look where I looked yesterday.":slot==2?"I'm only tired. You must save your strength. You're the one who has to walk.":"One more try. I can still get you one more morning.";
            else if(today==2)words=slot==0?"You've seen him. Same little mark, same tree. Some things keep finding their way back to us.":slot==2?"I went by the creek. Don't worry about my trousers. Eat while it's warm.":"You keep eating. I'll keep looking. We don't need to solve the whole forest tonight.";
            else words=slot==0?"If he can find us under all this snow, he can find us tomorrow. Hold on to that.":slot==2?"We wait here until it eases. The tree keeps the worst of it off. Stay where I can see you.":"Your mother will be waiting. Tell her about the rabbit when we're home.";
            if(slot!=8)voice(p,father,words);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.HOLY_RABBIT,now);return true;
        });
    }
}
