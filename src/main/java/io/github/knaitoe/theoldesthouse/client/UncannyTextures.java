package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;

/** Original native pixel paintings: sixteen-pixel materials and eight-pixel faces, with nearest sampling. */
public final class UncannyTextures {
    public static final int WOOD=0,PAINT=1,PAPER=2,FORMICA=3,VINYL=4,FROST=5,CONCRETE=6,METAL=7,CRT=8,RED=9,GREEN=10,FLESH=11,OBSIDIAN=12,BONE=13,GLASS=14,BLACK=15;
    private static final Map<String,ResourceLocation> TEXTURES=new HashMap<>();
    private UncannyTextures(){}
    private static int rgba(int rgb){return 0xff000000|(rgb&255)<<16|(rgb&0xff00)|(rgb>>16&255);}
    private static int shade(int c,int n){return Math.max(0,Math.min(255,(c>>16&255)+n))<<16|Math.max(0,Math.min(255,(c>>8&255)+n))<<8|Math.max(0,Math.min(255,(c&255)+n));}
    private static ResourceLocation register(String key,NativeImage image){
        var texture=new DynamicTexture(image);texture.setFilter(false,false);var id=Minecraft.getInstance().getTextureManager().register("uncanny_"+key,texture);TEXTURES.put(key,id);return id;
    }
    public static ResourceLocation atlas(){
        if(TEXTURES.containsKey("materials"))return TEXTURES.get("materials");var image=new NativeImage(64,64,false);
        int[] colors={0x51402d,0xa08048,0xc4b68f,0x547b82,0x5b3440,0xa6c2c8,0x72716b,0x606866,0x182e2e,0xb7483e,0x64a177,0xa87969,0x302936,0xd4c4a4,0x30414a,0x181a1c};
        for(int tile=0;tile<16;tile++)for(int y=0;y<16;y++)for(int x=0;x<16;x++){
            int c=colors[tile],grain=Math.floorMod(x*17+y*31+tile*47,9)-4;c=shade(c,grain);
            if(tile==WOOD&&(y%5==4||x==0))c=0x30271e;
            if(tile==PAPER&&(x==0||y==0||x==15||y==15))c=0x7b7455;
            if(tile==PAPER&&y>3&&y<13&&y%2==0&&x>2&&x<13)c=0x575647;
            if(tile==METAL&&(x==1||y==1))c=0x9caaa2;
            if(tile==CRT&&(y%3==0))c=0x10201d;
            if(tile==FLESH&&(y==(x/3+tile)%5+5||y==(x/4)%6+10))c=0x775247;
            if(tile==OBSIDIAN&&((x+2*y)%11<2))c=0x4b4055;
            if(tile==BONE&&(x%7==0))c=0x9c9076;
            if(tile==FROST&&((x*y+y)%7==0))c=0xd1dcdb;
            if(tile==PAINT){
                c=y<11?0xa88549:0x6f5235;
                if(x>=8&&x<=14&&y>=1&&y<=9)c=0xd4c78a;
                if(x>=10&&x<=12&&y>=2&&y<=4)c=0xe8d793;
                if((x==8||x==14||y==1||y==9)&&x>=8&&y<=9)c=0x614b30;
                if(x>=2&&x<=6&&y>=10&&y<=11)c=0x876340;
                if((x==2||x==6)&&y>=12)c=0x4b3a28;
                if(y%5==4)c=shade(c,-22);
                if(x==1&&(y==1||y==11)||x==14&&y==14)c=0x262521;
                if(x>=4&&x<=5&&y==8)c=0xd6c7a2; // the little china dog
            }
            image.setPixelRGBA(tile%4*16+x,tile/4*16+y,rgba(c));
        }
        return register("materials",image);
    }
    public static ResourceLocation skin(int role,int phase){
        int variant=role==LiteraryActor.UNCANNY_FALSE_WAITRESS&&phase>=2?1:0;String key="skin_"+role+"_"+variant;
        if(TEXTURES.containsKey(key))return TEXTURES.get(key);
        var image=new NativeImage(64,64,true);int flesh=role==LiteraryActor.UNCANNY_PROPPED?0x9b927a:role==LiteraryActor.UNCANNY_GIANT?0xada592:0xb18d73;
        int coat=role==LiteraryActor.UNCANNY_WAITRESS||role==LiteraryActor.UNCANNY_FALSE_WAITRESS?0x547786:role==LiteraryActor.UNCANNY_PEELER?0x70634c:0x3d3936;
        for(int y=0;y<64;y++)for(int x=0;x<64;x++){int base=y<16?flesh:coat;image.setPixelRGBA(x,y,rgba(shade(base,Math.floorMod(x*11+y*23,7)-3)));}
        // All six sides of the native head use the same deliberately small painted grid.
        for(int y=8;y<16;y++)for(int x=8;x<16;x++){
            int c=flesh;if(y<10||x==8||x==15)c=role==LiteraryActor.UNCANNY_GIANT?0x38362f:0x554738;
            if((x==10||x==13)&&y==11)c=0x32352f;
            if((x==10||x==13)&&y==10)c=0x847965;
            if(y==12&&x==12)c=shade(flesh,-18);
            if(y==14&&x>=11&&x<=12)c=0x705b4d;
            if(role==LiteraryActor.UNCANNY_PROPPED&&y>=12&&x>=10&&x<=13)c=y==12||y==15?0xbdb499:0x302d27;
            if(variant==1&&y>=12&&x>=9&&x<=14)c=y==12||y==14?0xc9bd97:0x413127;
            image.setPixelRGBA(x,y,rgba(c));
        }
        if(role==LiteraryActor.UNCANNY_WAITRESS||role==LiteraryActor.UNCANNY_FALSE_WAITRESS)for(int y=21;y<32;y++)for(int x=21;x<27;x++)image.setPixelRGBA(x,y,rgba(0xc6bfa5));
        return register(key,image);
    }
    public static void clear(){var manager=Minecraft.getInstance().getTextureManager();for(var id:TEXTURES.values())manager.release(id);TEXTURES.clear();}
}
