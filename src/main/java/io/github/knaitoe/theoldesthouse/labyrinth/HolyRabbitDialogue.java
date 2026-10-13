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
        if(night==1&&(own.getInt("RabbitMornings")&2)!=0&&once(own,2))voice(p,father,"The little one from your dream? We'll call him "+name(own)+". Now he has a name to find his way back to.");
        if(night==1&&own.getInt("AteNight")==1&&once(own,4))voice(p,father,"Keep the skin folded. There are things in these woods that know how to come back.");
        if(night==2&&(own.getInt("RabbitMornings")&4)!=0&&own.getInt("Meals")>=1&&once(own,8)){
            own.putBoolean("RabbitBelief0490",true);own.putBoolean("RabbitBelieved0490",true);
            p.displayClientMessage(Component.literal(name(own)+" is alive beside the tree. You ate, and he came back. Perhaps the dream sent him to keep you both alive."),false);
            voice(p,father,"There he is. I told you he could find us. We only have to get through another night.");
        }
        if(night>=2&&own.getInt("MealNight")>=2&&!own.getBoolean("WoundSeen0488")&&once(own,16))voice(p,father,"I went by the creek. That's why my trousers are wet. Don't let the meat get cold while you worry about me.");
        if(own.getBoolean("CarvingRead")&&!own.getBoolean("WoundSeen0488")&&once(own,32))p.displayClientMessage(Component.literal("Your initials are cut beside your mother's. Below them is a little rabbit. Father has pressed the knife hard enough to cut through the bark."),false);
        if(own.getBoolean("WoundSeen0488")&&once(own,64)){
            own.putBoolean("RabbitTruth0490",true);own.putBoolean("RabbitBelief0490",false);
            p.displayClientMessage(Component.literal("The torn trousers show where he has cut. The same skin, filled again. The rabbit never had to come back from the dead. You look at the food, then at his legs."),false);
            voice(p,father,"You needed to eat. That is all. You needed to stay awake.");
        }
    }
    public static void meal(ServerPlayer p,LiteraryActor father,CompoundTag own,int night){voice(p,father,night==1?"I found something for us. Small bites. There will be a morning.":night==2?name(own)+" found us again. Here. Eat yours first.":"There's a little left. I already ate on the way back.");}
    public static void talk(ServerPlayer p,LiteraryActor father,CompoundTag own){
        own.putInt("Drowse",Math.max(0,own.getInt("Drowse")-100));boolean truth=own.getBoolean("WoundSeen0488");var icons=new HashMap<Integer,ItemStack>();
        icons.put(0,LiteraryChoiceMenu.icon(Items.RABBIT_HIDE,truth?"Ask where the meat came from":"Ask whether "+name(own)+" can come back again"));
        icons.put(4,LiteraryChoiceMenu.icon(Items.CAMPFIRE,truth?"Ask him to stay with you":"Ask whether you will get home"));icons.put(8,LiteraryChoiceMenu.icon(Items.PAPER,"Sit with him quietly"));
        LiteraryChoiceMenu.open(p,"Father",icons,()->p.isAlive()&&!p.isSpectator()&&LiteraryVignettes.inside(p,LabyrinthPlace.HOLY_RABBIT)&&p.distanceToSqr(father)<=36,slot->{
            var d=LabyrinthData.get(p.server);var now=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.HOLY_RABBIT);String words;
            if(now.getBoolean("LastHunt0490"))words=slot==4?"I have to get you one more morning.":"Hunting.";
            else if(now.getBoolean("WoundSeen0488"))words=slot==0?"Look at me, not at that. You can walk now. That is what matters.":"I will be right behind you. Keep going when you see the light.";
            else if(slot==0)words=now.getInt("Night")>=2?"You've seen him. Same little mark, same tree. Some things keep finding their way back to us.":"If he can find us under all this snow, he can find us tomorrow. Hold on to that.";
            else words=now.getInt("Night")>=2?"You keep eating. I'll keep looking. We don't need to solve the whole forest tonight.":"First we wait for the snow to ease. Your mother will be waiting. Stay close to me.";
            if(slot!=8)voice(p,father,words);LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.HOLY_RABBIT,now);return true;
        });
    }
}
