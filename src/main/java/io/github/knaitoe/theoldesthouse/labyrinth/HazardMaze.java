package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** A native six-by-six spanning tree. A panel exchanges edges only through a complete connected cycle. */
public final class HazardMaze {
    public record Edge(int a,int b,BlockPos centre,boolean east){
        public List<BlockPos> cells(){var out=new ArrayList<BlockPos>();for(int across=-1;across<=1;across++)for(int y=0;y<3;y++)out.add(centre.offset(east?0:across,y,east?across:0));return out;}
    }
    public static final List<Edge> EDGES;
    static{var all=new ArrayList<Edge>();for(int r=0;r<6;r++)for(int c=0;c<6;c++){int n=r*6+c;var p=cell(n);if(c<5)all.add(new Edge(n,n+1,p.offset(2,0,0),true));if(r<5)all.add(new Edge(n,n+6,p.offset(0,0,-2),false));}EDGES=List.copyOf(all);}
    private HazardMaze(){}
    public static BlockPos cell(int n){return new BlockPos(-10+(n%6)*4,0,-5-(n/6)*4);}
    public static long initialMask(long seed){var rng=new Random(seed^0x534556454eL);var seen=new HashSet<Integer>();var stack=new ArrayDeque<Integer>();seen.add(3);stack.push(3);long mask=0;
        while(!stack.isEmpty()){int at=stack.peek();var candidates=new ArrayList<Integer>();for(int i=0;i<EDGES.size();i++){var e=EDGES.get(i);int next=e.a==at?e.b:e.b==at?e.a:-1;if(next>=0&&!seen.contains(next))candidates.add(i);}if(candidates.isEmpty()){stack.pop();continue;}int i=candidates.get(rng.nextInt(candidates.size()));var e=EDGES.get(i);int next=e.a==at?e.b:e.a;mask|=1L<<i;seen.add(next);stack.push(next);}return mask;}
    public static List<Integer> path(long mask,int from,int to){int[] parent=new int[36],edge=new int[36];Arrays.fill(parent,-1);var q=new ArrayDeque<Integer>();parent[from]=from;q.add(from);
        while(!q.isEmpty()){int at=q.remove();if(at==to)break;for(int i=0;i<EDGES.size();i++){if((mask&(1L<<i))==0)continue;var e=EDGES.get(i);int next=e.a==at?e.b:e.b==at?e.a:-1;if(next>=0&&parent[next]<0){parent[next]=at;edge[next]=i;q.add(next);}}}
        if(parent[to]<0)return List.of();var out=new ArrayList<Integer>();for(int at=to;at!=from;at=parent[at])out.add(edge[at]);return out;}
    public static boolean connected(long mask){for(int n=1;n<36;n++)if(path(mask,0,n).isEmpty())return false;return true;}
    public static long exchange(ServerLevel l,BlockPos b,long mask,int source,int target){
        if(source<0||target<0||source>=EDGES.size()||target>=EDGES.size()||(mask&(1L<<source))!=0||(mask&(1L<<target))==0)return mask;
        long next=(mask|(1L<<source))&~(1L<<target);if(!connected(next))return mask;
        var a=EDGES.get(source);var z=EDGES.get(target);var sourceCells=a.cells().stream().map(b::offset).toList();var targetCells=z.cells().stream().map(b::offset).toList();
        for(var p:sourceCells)if(!HazardBlock.is(l.getBlockState(p),HazardBlock.Kind.MAZE_PANEL)||HazardVariety.watched(l,p)||!l.getEntities(null,new AABB(p).inflate(.12)).isEmpty())return mask;
        for(var p:targetCells)if(!l.getBlockState(p).isAir()||HazardVariety.watched(l,p)||!l.getEntities(null,new AABB(p).inflate(.12)).isEmpty()||l.getBlockEntity(p)!=null)return mask;
        var marks=new LinkedHashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
        for(var p:sourceCells)for(var side:net.minecraft.core.Direction.values()){var mark=p.relative(side);var s=l.getBlockState(mark);if(s.getBlock() instanceof ChalkMarkBlock&&sourceCells.contains(mark.relative(s.getValue(ChalkMarkBlock.FACE).getOpposite())))marks.put(mark,s);}
        int turns=a.east==z.east?0:1;var moved=new LinkedHashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
        for(var entry:marks.entrySet()){var delta=entry.getKey().subtract(b.offset(a.centre));int x=delta.getX(),zz=delta.getZ();if(turns!=0){int old=x;x=-zz;zz=old;}var dest=b.offset(z.centre).offset(x,delta.getY(),zz);
            if(!l.getBlockState(dest).isAir()||l.getBlockEntity(dest)!=null||HazardVariety.watched(l,dest)||!l.getEntities(null,new AABB(dest).inflate(.12)).isEmpty())return mask;
            var s=entry.getValue();if(turns!=0){var face=s.getValue(ChalkMarkBlock.FACE);if(face.getAxis().isHorizontal())s=s.setValue(ChalkMarkBlock.FACE,face.getClockWise());s=s.setValue(ChalkMarkBlock.ARROW,s.getValue(ChalkMarkBlock.ARROW).getClockWise());}moved.put(dest,s);
        }
        // Open first. Closing the exchanged edge then leaves every cell connected, including every reader's cell.
        marks.keySet().forEach(p->l.setBlock(p,Blocks.AIR.defaultBlockState(),3));sourceCells.forEach(p->l.setBlock(p,Blocks.AIR.defaultBlockState(),3));
        targetCells.forEach(p->l.setBlock(p,HazardBlock.of(HazardBlock.Kind.MAZE_PANEL),3));moved.forEach((p,s)->l.setBlock(p,s,3));return next;
    }
}
