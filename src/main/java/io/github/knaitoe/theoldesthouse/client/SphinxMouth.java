package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.network.SphinxVoicePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * 0.4.341, the Sphinx's mouth while she speaks (the owner: "Ensure the mouth moves when she speaks too"). Her lines tell this reader's
 * client how long she is speaking; until then her jaw works through three frames of stone, opening and closing on the rhythm of words,
 * and settles back to its slight grimace when she stops. Her face in the first crossing and in the dream both read it.
 */
public final class SphinxMouth {
    private SphinxMouth(){}
    private static long from=-1000,until=-1000;
    public static void accept(SphinxVoicePayload p){var mc=Minecraft.getInstance();if(mc.level==null)return;long now=mc.level.getGameTime();from=now;until=now+Math.max(10,p.ticks());}
    /** Starts her speaking for {@code ticks} ticks from {@code now}, as if a line had come (the client proof, and the dream's question). */
    public static void speak(long now,int ticks){from=now;until=now+ticks;}
    public static boolean speaking(float now){return now>=from&&now<until;}
    /**
     * Which frame of her speaking to draw: 0 for her mouth at rest, 1 to 3 for her jaw lower and lower. Two slow waves make the rhythm of
     * syllables and phrases, and her jaw eases shut over the last few ticks of the line.
     */
    public static int frame(float now){
        if(!speaking(now))return 0;
        float t=now-from,left=until-now;
        float open=Math.abs(Mth.sin(t*.62F))*.75F+Math.abs(Mth.sin(t*.21F+1.3F))*.35F;
        open*=Mth.clamp(left/6F,0,1)*Mth.clamp(t/3F,0,1);
        return Mth.clamp(Math.round(open*3F),0,3);
    }
    public static void clear(){from=until=-1000;}
}
