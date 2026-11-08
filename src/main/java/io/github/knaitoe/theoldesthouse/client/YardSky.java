package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 0.4.358, the yard's night (docs/YARD_FINISHING_0_4_358.md; the owner's "The House Has a Yard": "the night variation of the sky, a fixed
 * sky with a slightly displaced star pattern").
 * <p>
 * Over the yard the House shows the stars from home, the reader's own sky, laid out by the same rule the Overworld uses, as they stand at
 * midnight. They never wheel. The whole field is turned a couple of degrees off true, and one star in nine has drifted a little further on
 * its own: close enough to be recognised, wrong enough to be noticed by someone who has looked up at home.
 */
public final class YardSky {
    private YardSky(){}
    /** The Overworld's star seed; how far the whole field is turned off true, and how far a drifted star goes on its own (degrees). */
    public static final long HOME_SEED=10842L;
    public static final float TURN=2.5F,DRIFT=1.2F,RADIUS=80;
    private static float[] quads,home;
    private static BlockPos cachedAt;private static boolean cachedYard;private static Object cachedLevel;

    /** Whether the camera stands in one of the yard's places, outside. */
    public static boolean here(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!mc.level.dimension().equals(HouseDimensions.OUTSIDE)||!HouseSightlineState.hasOrigin())return false;
        var at=mc.gameRenderer.getMainCamera().getBlockPosition();
        if(cachedLevel!=mc.level||!at.equals(cachedAt)){cachedLevel=mc.level;cachedAt=at.immutable();cachedYard=yard(LabyrinthPlaces.placeAt(HouseSightlineState.origin(),at));}
        return cachedYard;
    }
    public static boolean yard(LabyrinthPlace p){return p!=null&&p.id().startsWith("yard_");}

    /** Home's stars at midnight, as unit-sphere centres (x, y, z per star): the Overworld's own rule, turned the way its sky turns them. */
    public static synchronized float[] home(){if(home==null)build();return home;}
    /** The yard's stars: home's, turned off true and some drifted, as quads (four corners of x, y, z per star). */
    public static synchronized float[] quads(){if(quads==null)build();return quads;}
    /** The angle in degrees between a star at home and the same star over the yard. */
    public static float offset(int star){
        var q=quads();var h=home();float cx=0,cy=0,cz=0;for(int c=0;c<4;c++){cx+=q[star*12+c*3];cy+=q[star*12+c*3+1];cz+=q[star*12+c*3+2];}
        var a=new Vector3f(cx,cy,cz).normalize();var b=new Vector3f(h[star*3],h[star*3+1],h[star*3+2]).normalize();
        return (float)Math.toDegrees(Math.acos(Math.max(-1,Math.min(1,a.dot(b)))));
    }
    public static int count(){return home().length/3;}

    private static void build(){
        var random=RandomSource.create(HOME_SEED);
        var centres=new java.util.ArrayList<float[]>();var corners=new java.util.ArrayList<float[]>();
        // The sky as the Overworld draws it at midnight: turned a quarter about the vertical, then half about east-west.
        var sky=new Quaternionf().rotateY((float)Math.toRadians(-90)).rotateX((float)Math.PI);
        var turn=new Quaternionf().rotateAxis((float)Math.toRadians(TURN),new Vector3f(.3F,.15F,1).normalize());
        var drifts=RandomSource.create(HOME_SEED^0x5EED_0358L);
        for(int i=0;i<1500;i++){
            float x=random.nextFloat()*2-1,y=random.nextFloat()*2-1,z=random.nextFloat()*2-1,size=.15F+random.nextFloat()*.1F,length=x*x+y*y+z*z;
            if(length<=.010000001F||length>=1)continue;
            var centre=new Vector3f(x,y,z).normalize();float spin=(float)(random.nextDouble()*Math.PI*2);
            var homeCentre=new Vector3f(centre).rotate(sky);centres.add(new float[]{homeCentre.x,homeCentre.y,homeCentre.z});
            var shape=new Quaternionf().rotateTo(new Vector3f(0,0,-1),centre).rotateZ(spin);
            var moved=new Quaternionf(turn).mul(sky);
            boolean drifted=centres.size()%9==0;
            if(drifted)moved=new Quaternionf().rotateAxis((float)Math.toRadians(DRIFT),new Vector3f(drifts.nextFloat()-.5F,drifts.nextFloat()-.5F,drifts.nextFloat()-.5F).normalize()).mul(moved);
            float[] quad=new float[12];int k=0;
            for(float[] c:new float[][]{{size,-size},{size,size},{-size,size},{-size,-size}}){
                var v=new Vector3f(centre).mul(100).add(new Vector3f(c[0],c[1],0).rotate(shape)).rotate(moved).mul(RADIUS/100F);
                quad[k++]=v.x;quad[k++]=v.y;quad[k++]=v.z;
            }
            corners.add(quad);
        }
        home=new float[centres.size()*3];quads=new float[corners.size()*12];
        for(int i=0;i<centres.size();i++){System.arraycopy(centres.get(i),0,home,i*3,3);System.arraycopy(corners.get(i),0,quads,i*12,12);}
    }

    /** The yard's stars into a position-colour quad buffer, as bright as the night allows. */
    public static void stars(BufferBuilder b,Matrix4f view,float night){
        var q=quads();float light=.85F*night;
        for(int i=0;i<q.length;i+=3)b.addVertex(view,q[i],q[i+1],q[i+2]).setColor(light,light,light,1);
    }
}
