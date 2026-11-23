package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.WaxNight;
import io.github.knaitoe.theoldesthouse.labyrinth.WaxSite;
import io.github.knaitoe.theoldesthouse.labyrinth.Waxworks;
import io.github.knaitoe.theoldesthouse.network.WaxBlinkPayload;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * 0.4.750, the waxworks on this client (docs/WAXWORKS_0_4_750.md): the blink the server sends this reader alone, and the statesman who
 * copies them. The copy is worked out here from this reader's own body, so each reader in front of him sees him copy themselves: the
 * head turned as a reflection turns it, the same tilt, a crouch for a crouch, a few ticks behind.
 */
public final class WaxClient {
    private WaxClient(){}
    private static final Map<Integer,Long> BLINKS=new HashMap<>();
    /** How many ticks behind the reader the copy is. */
    public static final int LAG=6;
    private static final float[][] SEEN=new float[32][];private static final long[] AT=new long[32];

    public static void blink(WaxBlinkPayload p){var l=Minecraft.getInstance().level;if(l==null)return;BLINKS.put(p.entity(),l.getGameTime()+p.ticks());if(BLINKS.size()>64)BLINKS.entrySet().removeIf(e->e.getValue()<l.getGameTime());}
    public static boolean blinking(LiteraryActor a){var until=BLINKS.get(a.getId());return until!=null&&a.level().getGameTime()<=until;}

    /** The copy for this figure: head yaw offset and pitch (degrees) and whether crouched, or null when it is not the mirror or nobody here is in front of it. */
    public static @Nullable float[] mirror(LiteraryActor a){
        if(Waxworks.which(a)!=WaxSite.MIRROR)return null;
        var mc=Minecraft.getInstance();var p=mc.player;if(p==null||p.isSpectator()||p.level()!=a.level())return null;
        long now=a.level().getGameTime();
        float[] here=null;
        if(WaxNight.before(p.position(),p.getLookAngle(),a.position(),a.yBodyRot)){
            var to=a.position().subtract(p.position());
            float toward=(float)(Mth.atan2(to.z,to.x)*Mth.RAD_TO_DEG)-90;
            float theta=Mth.wrapDegrees(p.getYHeadRot()-toward);
            here=new float[]{Mth.clamp(-theta,-70,70),Mth.clamp(p.getXRot(),-60,60),p.isCrouching()?1:0};
        }
        int i=(int)(now&31);if(AT[i]!=now){AT[i]=now;SEEN[i]=here;}
        int j=(int)((now-LAG)&31);
        return AT[j]==now-LAG&&here!=null?SEEN[j]:null;
    }
}
