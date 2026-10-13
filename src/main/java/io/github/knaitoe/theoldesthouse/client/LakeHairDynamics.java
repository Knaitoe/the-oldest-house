package io.github.knaitoe.theoldesthouse.client;

/** Nine bounded spring joints, stepped at game-tick rate. No world queries or server simulation. */
public final class LakeHairDynamics {
    public static final int JOINTS=9;
    final float[] x=new float[JOINTS],z=new float[JOINTS],oldX=new float[JOINTS],oldZ=new float[JOINTS];
    private final float[] vx=new float[JOINTS],vz=new float[JOINTS];
    public void step(boolean drifting,double side,double forward,float turn,int tick){
        if(!drifting){still();return;}
        side=clamp((float)side,-.2F,.2F);forward=clamp((float)forward,-.2F,.2F);turn=clamp(turn,-25,25);
        for(int i=0;i<JOINTS;i++){
            oldX[i]=x[i];oldZ[i]=z[i];int chain=i/3,link=i%3;
            float flow=(float)Math.sin(tick*.075+chain*1.7-link*.65);
            float tx=.14F+link*.055F+flow*.055F-(float)forward*.35F;
            float tz=(float)Math.sin(tick*.061+chain*2.1-link*.7)*.035F+(float)side*.3F-turn*.0008F*(link+1);
            if(link>0){tx+=(x[i-1]-x[i])*.12F;tz+=(z[i-1]-z[i])*.12F;}
            vx[i]=vx[i]*.78F+(tx-x[i])*.12F;vz[i]=vz[i]*.8F+(tz-z[i])*.1F;
            x[i]=clamp(x[i]+vx[i],-.06F,.48F);z[i]=clamp(z[i]+vz[i],-.14F,.14F);
        }
    }
    public void still(){for(int i=0;i<JOINTS;i++)x[i]=z[i]=oldX[i]=oldZ[i]=vx[i]=vz[i]=0;}
    private static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}
    public float pitch(int i,float partial){return oldX[i]+(x[i]-oldX[i])*partial;}
    public float roll(int i,float partial){return oldZ[i]+(z[i]-oldZ[i])*partial;}
    /** A finite client-proof check: impulses stay bounded, and the house stops all loose motion immediately. */
    public static void verify(){
        var d=new LakeHairDynamics();for(int t=0;t<400;t++){d.step(true,t%2==0?100:-100,t%3==0?100:-100,t%2==0?100:-100,t);for(int i=0;i<JOINTS;i++)if(!Float.isFinite(d.x[i])||d.x[i]<-.06F||d.x[i]>.48F||Math.abs(d.z[i])>.14F)throw new IllegalStateException("Unbounded Tess hair joint "+i);}
        boolean moved=false;for(float a:d.x)moved|=a!=0;if(!moved)throw new IllegalStateException("Tess hair never drifted in lake water");
        d.step(false,100,100,100,401);for(int i=0;i<JOINTS;i++)if(d.pitch(i,.5F)!=0||d.roll(i,.5F)!=0)throw new IllegalStateException("Tess hair still drifts inside the house");
    }
}
