package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseCalendar;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * What the funeral's choice does to the phone afterwards (0.4.188). The owner: "You can give Harrigan his phone or not …
 * Maybe you can receive friendly affirming texts if you didn't bury him with it, sort of freeing him. If you gave him the
 * phone, you let him carry the obligation and that's when the rot starts."
 * <p>Kept: he is freed. Once a day, out of the House, a kind line comes into the reader's thread from his number, though his
 * phone is in their pocket. He never takes a name.
 * <p>Buried: he carries the obligation. The line takes names as before, and each name rots it further: his greeting slows and
 * roughens, his replies lose their letters (the existing distorted replies), flies come the morning a name is answered, and at
 * the last he no longer waits to be asked: some nights the reader's own phone rings, and rings, and goes to a missed call.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HarriganPhones {
    private HarriganPhones(){}
    /** After this many names, he rings on his own some nights. */
    public static final int RINGS_AFTER=4;
    /** Rings before a call goes to a missed call, a ring every three seconds. */
    public static final int RINGS=5;
    public static final String[] KIND={
        "Read something today. Anything. A man who reads is never really alone.",
        "You did right by me.",
        "Buy the scratch ticket. You never know.",
        "Don't spend it all in one place.",
        "Turn a light on. You've been sitting in the dark again.",
        "I liked our afternoons. I don't say that to many people.",
        "Go home before it gets late.",
        "Nothing you owe me. Not a thing."};

    private static CompoundTag state(ServerPlayer p){return HarriganVignette.playerState(LabyrinthData.get(p.server),p.getUUID());}
    private static void save(ServerPlayer p,CompoundTag s){HarriganVignette.savePlayerState(LabyrinthData.get(p.server),p.getUUID(),s);}
    /** He has his phone (buried with him), and the line is open. */
    static boolean carries(CompoundTag s){return s.getBoolean("Contact")&&!s.getBoolean("Severed");}
    /** The reader kept his phone, so he is free of it. */
    static boolean freed(CompoundTag s){return s.getBoolean("KeptHarriganPhone")&&!s.getBoolean("Contact");}

    /** His voicemail greeting, as much of it as the names he has carried have left. */
    public static String greeting(CompoundTag s){
        int names=s.getInt("TotalCalls");
        if(names<1)return HarriganVignette.GREETING;
        if(names<3)return "This is Harrigan. I don't… pick up this phone. If you have a name for me… leave it. After the tone.";
        if(names<RINGS_AFTER+2)return "Th s is H rr gan. I d n't p ck up. If y u have a n me f r me… l ave it.";
        return "…a name. Leave a name.";
    }

    /** The morning a name he took is answered: the casket's flies are at the reader's window. */
    public static void flies(ServerPlayer p){
        if(p==null||HarriganVignette.noService(p))return;
        p.serverLevel().sendParticles(p,ParticleTypes.ASH,true,p.getX(),p.getEyeY(),p.getZ(),40,1.2,.6,1.2,0);
        p.displayClientMessage(Component.literal("There are flies at the window this morning. The phone smells of the casket.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);
    }

    /** A kind text from his number, for a reader who kept his phone: once a House day, never in the House. */
    public static boolean kind(ServerPlayer p){
        var s=state(p);if(!freed(s)||HarriganVignette.noService(p))return false;
        long day=HouseCalendar.today(p.server);if(s.getLong("KindDay0188")==day&&s.contains("KindDay0188"))return false;
        s.putLong("KindDay0188",day);int n=s.getInt("KindSent0188");s.putInt("KindSent0188",n+1);save(p,s);
        HarriganVignette.log(p,HarriganVignette.HIM,KIND[n%KIND.length]);
        p.serverLevel().playSound(null,p.getX(),p.getY(),p.getZ(),PhoneSounds.RECEIVED.get(),SoundSource.PLAYERS,.6F,1F);
        p.displayClientMessage(Component.literal("A text from Mr. Harrigan.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
        return true;
    }

    /**
     * At the last of the rot: a night ring he starts himself, on the reader's own phone. It rings, and rings, and goes to a
     * missed call; once a night at most.
     */
    public static boolean ring(ServerPlayer p){
        var s=state(p);if(!carries(s)||s.getInt("TotalCalls")<RINGS_AFTER||HarriganVignette.noService(p)||!HarriganVignette.hasItem(p,LabyrinthRegistry.PHONE.get()))return false;
        long day=HouseCalendar.today(p.server);if(s.contains("RingDay0188")&&s.getLong("RingDay0188")==day)return false;
        s.putLong("RingDay0188",day);s.putInt("RingsLeft0188",RINGS);save(p,s);
        p.displayClientMessage(Component.literal("Your phone is ringing. Mr. Harrigan.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
        return true;
    }
    /** One ring of a night call; the last goes to a missed call. */
    public static void ringing(ServerPlayer p){
        var s=state(p);int left=s.getInt("RingsLeft0188");if(left<=0)return;
        p.serverLevel().playSound(null,p.getX(),p.getY(),p.getZ(),PhoneSounds.HIS_RING.get(),SoundSource.PLAYERS,.9F,.92F);
        s.putInt("RingsLeft0188",left-1);save(p,s);
        if(left==1){HarriganVignette.log(p,HarriganVignette.NOTE,"Missed call · Mr. Harrigan");p.displayClientMessage(Component.literal("Missed call. Mr. Harrigan. You can hear breathing on the voicemail.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();int t=server.getTickCount();if(t%60!=0)return;
        long tod=server.overworld().getDayTime()%24000L;
        for(var p:server.getPlayerList().getPlayers()){
            if(!p.isAlive()||p.isSpectator())continue;
            int offset=Math.floorMod(p.getUUID().hashCode(),6000);
            // A kind line some time in the day, each reader at their own hour.
            if(tod>=1000+offset&&tod<13000)kind(p);
            // A night ring some time after dark.
            if(HarriganVignette.isNight(server)&&tod>=14000+offset/2)ring(p);
            ringing(p);
        }
    }
}
