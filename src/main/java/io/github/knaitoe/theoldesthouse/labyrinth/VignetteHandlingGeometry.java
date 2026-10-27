package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;

/** The same authored cuboids drive production rendering, picking and collision. Coordinates are native sixteenths. */
public final class VignetteHandlingGeometry {
 public static final int BLUE=0,DARK=1,OAK=2,IRON=3,BRASS=4,ENAMEL=5,LINEN=6,INK=7,PAPER=8,BENCH=9,IVORY=10,EBONY=11,GRID=12,SLIDE=13,REEL=17,TAPE=18,PLAY=19,PAUSE=20,REWIND=21,FRAME=22;
 public record Part(double x0,double y0,double z0,double x1,double y1,double z1,int material,boolean picture,boolean lit,float yaw){}
 public record View(int key,int slide,boolean tray,boolean lamp,int cursor,int mode,boolean lid,int[] cells,int[] turns,int held){
  public View{cells=cells.clone();turns=turns.clone();}
  public static View initial(){return new View(-1,0,false,false,0,0,false,new int[]{0,4,8},new int[3],-1);}
 }
 private VignetteHandlingGeometry(){}
 private static void box(List<Part> a,double x0,double y0,double z0,double x1,double y1,double z1,int m){a.add(new Part(x0,y0,z0,x1,y1,z1,m,false,false,0));}
 private static void art(List<Part> a,double x0,double y0,double z0,double x1,double y1,double z1,int m,boolean lit){a.add(new Part(x0,y0,z0,x1,y1,z1,m,true,lit,0));}
 public static List<Part> parts(VignetteHandlingBlock.Kind kind,View v,float partial){var a=new ArrayList<Part>();switch(kind){
  case TOY_PIANO,SPINET->{
   boolean toy=kind==VignetteHandlingBlock.Kind.TOY_PIANO;double y=toy?3:10;
   if(toy){box(a,1,0,.5,15,2,15,BLUE);box(a,1,2,11,15,12,15,BLUE);box(a,2,12,11,14,12.5,15,OAK);}
   else{for(int x:new int[]{0,14})for(int z:new int[]{1,13})box(a,x,0,z,x+2,9,z+2,DARK);box(a,0,9,9,16,14,16,DARK);box(a,0,8,.5,16,9,9,DARK);box(a,0,14,v.lid()?14:9,16,16,16,DARK);}
   for(int i=0;i<7;i++){int n=new int[]{0,2,4,5,7,9,11}[i];double down=v.key()==n?.65:0;box(a,13-2*i,y-1-down,1,14.9-2*i,y-down,10,IVORY);}
   int[] gaps={1,2,4,5,6},notes={1,3,6,8,10};
   for(int i=0;i<gaps.length;i++){double x=15-gaps[i]*2,down=v.key()==notes[i]?.6:0;box(a,x-.55,y-.15-down,5,x+.55,y+1-down,10,EBONY);}
  }
  case SLIDES->{
   box(a,1,0,.5,15,2,15,BLUE);box(a,2,2,5,14,2.5,14,IRON);double dz=v.tray()?-3:0;
   box(a,2,2.5,4+dz,14,3,14+dz,FRAME);box(a,6,2.5,3+dz,10,3.5,4+dz,IRON);
   art(a,4,3,5+dz,12,3.12,13+dz,SLIDE+v.slide(),v.lamp());
   for(int i=0;i<3;i++)art(a,2+i*4,3.15,12.4+dz,5+i*4,3.3,14.4+dz,SLIDE+(v.slide()+i+1)%4,false);
   art(a,11,.5,0,14,1.5,1,REWIND,false);art(a,2,.5,0,5,1.5,1,PLAY,false);
   box(a,13,2,13,15,3,15,v.lamp()?BRASS:INK);
  }
  case MODEL_KIT->{
   box(a,1,0,1,15,1,15,BENCH);art(a,2,1,2,14,1.08,14,GRID,false);box(a,1,1,14,15,2,15,OAK);
   for(int i=0;i<3;i++){
    int cell=v.cells()[i];double cx=4+cell%3*4,cz=4+cell/3*4,dy=v.held()==i?.8:0;var p=new ArrayList<Part>();
    if(i==0){for(double x:new double[]{-1,1})for(double z:new double[]{-1,1})box(p,cx+x-.2,1+dy,cz+z-.2,cx+x+.2,3.5+dy,cz+z+.2,DARK);box(p,cx-1.4,3.5+dy,cz-1.4,cx+1.4,4+dy,cz+1.4,OAK);box(p,cx-1.4,4+dy,cz+1,cx+1.4,6.5+dy,cz+1.4,BLUE);}
    else if(i==1){box(p,cx-.4,1+dy,cz-.4,cx+.4,3.8+dy,cz+.4,OAK);box(p,cx-1.5,3.8+dy,cz-1.5,cx+1.5,4.4+dy,cz+1.5,ENAMEL);}
    else{box(p,cx-.65,1+dy,cz-.6,cx+.65,2+dy,cz+.6,DARK);box(p,cx-.55,2+dy,cz-.4,cx+.55,4+dy,cz+.4,BLUE);box(p,cx-.7,4+dy,cz-.65,cx+.7,5.4+dy,cz+.65,LINEN);box(p,cx-.6,4.7+dy,cz-.71,cx+.6,5.05+dy,cz-.66,INK);}
    for(var b:p){double x0=b.x0(),x1=b.x1(),z0=b.z0(),z1=b.z1();for(int t=0;t<v.turns()[i];t++){double nx0=cx-(z1-cz),nx1=cx-(z0-cz);z0=cz+x0-cx;z1=cz+x1-cx;x0=nx0;x1=nx1;}box(a,x0,b.y0(),z0,x1,b.y1(),z1,b.material());}
   }
  }
  case TAPE->{
   box(a,1,0,2,15,4,15,BLUE);box(a,2,3.5,6,14,4.05,14,IRON);
   double f=Math.max(0,Math.min(1,(v.cursor()+(v.mode()==1?partial:v.mode()==2?-8*partial:0))/320.0));
   for(int i=0;i<2;i++){double cx=i==0?4.5:11.5,r=i==0?2.2-f*1.2:1+f*1.2;
    box(a,cx-2.2,4,7.8,cx+2.2,4.4,12.2,IRON);box(a,cx-r,4.4,10-r,cx+r,4.65,10+r,TAPE);
    a.add(new Part(cx-1.8,4.65,9.7,cx+1.8,4.9,10.3,BRASS,false,false,(float)((v.cursor()+partial*(v.mode()==1?1:v.mode()==2?-8:0))*18)));
    box(a,cx-.3,4.6,9.7,cx+.3,5,10.3,IRON);
   }
   box(a,6.5,4.15,9.8,9.5,4.35,10.2,TAPE);
   if(v.lid()){box(a,1,4,14,15,14,15,BLUE);}
   else{box(a,1,5,6,2,5.5,15,BLUE);box(a,14,5,6,15,5.5,15,BLUE);box(a,1,5,14,15,5.5,15,BLUE);}
   int[] mats={REWIND,PLAY,PAUSE};
   for(int i=0;i<3;i++){boolean pressed=i==0?v.mode()==2:i==1?v.mode()==1:v.mode()==0&&v.cursor()>0;double y=pressed?3.7:4.2;art(a,11-i*4,y,2,14-i*4,y+.4,5,mats[i],false);}
   box(a,13,4,12.5,14,5,13.5,BRASS);
  }
 }return a;}
}
