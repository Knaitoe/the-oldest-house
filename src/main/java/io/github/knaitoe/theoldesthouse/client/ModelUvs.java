package io.github.knaitoe.theoldesthouse.client;

/** 0.4.240: automatic face coordinates kept on their own texture (see ModelUvRangeMixin). */
public final class ModelUvs {
    private ModelUvs(){}
    /** {u0,v0,u1,v1} moved back inside 0..16 without changing their size; a span wider than the texture is fitted to it. */
    public static float[] fit(float[] uv){
        if(uv==null||uv.length<4)return uv;
        float[] o=uv.clone();axis(o,0,2);axis(o,1,3);return o;
    }
    private static void axis(float[] o,int a,int b){
        float lo=Math.min(o[a],o[b]),hi=Math.max(o[a],o[b]);
        if(lo>=-1e-4F&&hi<=16+1e-4F)return;
        float span=hi-lo;
        if(span>16){float s=16/span;o[a]=(o[a]-lo)*s;o[b]=(o[b]-lo)*s;return;}
        float d=lo<0?-lo:16-hi;o[a]+=d;o[b]+=d;
    }
}
