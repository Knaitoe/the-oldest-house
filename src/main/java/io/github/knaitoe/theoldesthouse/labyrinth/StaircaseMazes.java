package io.github.knaitoe.theoldesthouse.labyrinth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
/** Two physical side labyrinths. Darkness is personal and increases only on actual crossings. */
public final class StaircaseMazes {
    private StaircaseMazes(){}
    public static void plan(Map<BlockPos,BlockState> plan,BlockPos b){
        int y=FinaleArchitecture.ARENA;var wall=Blocks.DEEPSLATE_TILES.defaultBlockState();var floor=io.github.knaitoe.theoldesthouse.house.HouseBlocks.STAIRCASE_STONE.get().defaultBlockState();
        for(int side:new int[]{-1,1}){
            int shift=side==1?0:-30;
            for(int x=18;x<=78;x++)for(int z=32;z<=67;z++){
                plan.put(b.offset(side*x,y-1,z+shift),floor);for(int yy=y;yy<=y+4;yy++)plan.put(b.offset(side*x,yy,z+shift),wall);
            }
            boolean[][] visited=new boolean[9][5];var random=new Random(side==1?80427:90317);var stack=new ArrayDeque<int[]>();stack.push(new int[]{0,side==1?0:4});visited[0][side==1?0:4]=true;
            while(!stack.isEmpty()){
                var at=stack.peek();carve(plan,b,side,at[0]*6+23,at[1]*6+36+shift,at[0]*6+23,at[1]*6+36+shift,y);
                var options=new ArrayList<int[]>();for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int x=at[0]+d[0],z=at[1]+d[1];if(x>=0&&x<9&&z>=0&&z<5&&!visited[x][z])options.add(new int[]{x,z});}
                if(options.isEmpty()){stack.pop();continue;}var next=options.get(random.nextInt(options.size()));visited[next[0]][next[1]]=true;
                carve(plan,b,side,at[0]*6+23,at[1]*6+36+shift,next[0]*6+23,next[1]*6+36+shift,y);stack.push(next);
            }
            int entryZ=36;carve(plan,b,side,16,entryZ,23,entryZ,y);if(side==-1)carve(plan,b,side,23,36,23,30,y);
            // A few loops frustrate wall-following without requiring a teleport or an invisible barrier.
            for(int row=1;row<4;row+=2)carve(plan,b,side,47,row*6+36+shift,53,row*6+36+shift,y);
            plan.put(b.offset(side*20,y+2,entryZ),Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        }
    }
    private static void carve(Map<BlockPos,BlockState> p,BlockPos b,int side,int x,int z,int tx,int tz,int y){
        while(true){for(int dz=0;dz<=1;dz++)for(int dx=0;dx<=1;dx++)for(int yy=y;yy<y+3;yy++)p.put(b.offset(side*(x+dx),yy,z+dz),Blocks.AIR.defaultBlockState());if(x==tx&&z==tz)break;x+=Integer.signum(tx-x);z+=Integer.signum(tz-z);}
    }
    public static boolean inArea(BlockPos b,BlockPos p){int x=p.getX()-b.getX(),z=p.getZ()-b.getZ();return Math.abs(x)>=16&&Math.abs(x)<=78&&(x>0?z>=32&&z<=67:z>=2&&z<=37)&&p.getY()>=FinaleArchitecture.ARENA-1&&p.getY()<=FinaleArchitecture.ARENA+4;}
    public static void tick(ServerPlayer p,BlockPos b,net.minecraft.nbt.CompoundTag record){
        double x=Math.abs(p.getX()-b.getX()),z=p.getZ()-b.getZ();boolean inside=x>18.5&&x<79&&(p.getX()>b.getX()?z>32&&z<68:z>2&&z<38)&&Math.abs(p.getY()-FinaleArchitecture.ARENA)<3;
        boolean was=record.getBoolean("InSideMaze");
        if(inside&&!was){record.putInt("MazePasses",record.getInt("MazePasses")+1);record.putBoolean("InSideMaze",true);}
        else if(!inside&&was){record.putBoolean("InSideMaze",false);HousePackets.send(p,new NovelScenePayload(0,0,"",0,0));}
        if(inside&&p.tickCount%20==0)HousePackets.send(p,new NovelScenePayload(12,record.getInt("MazePasses"),record.getInt("MazePasses")==1?"The stairs are behind you.":"The walls take back more of the light.",p.tickCount%160==0?80:0,0));
    }
}
