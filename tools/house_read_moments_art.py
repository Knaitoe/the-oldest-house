"""Native sixteen-pixel domestic materials and fitted cuboids for the remaining quiet rooms."""
from PIL import Image, ImageDraw
import io,json,math
EXTRA_PARTS=['clock_base','clock_face','bell','calendar','reception','crt','cooler','paper','magazines','glasses','phone','armchair','sink','tap','cabinet','routine_a','routine_b','routine_c','routine_d','light_switch','postcard','jukebox','string_light','rowboat','bottle','ashtray']
def tiles(out):
 palettes={'panels':('#947457','#66533f','#b39a72'),'tile':('#9fae9a','#718575','#c9cfb5'),'pavilion':('#746b54','#514f42','#998c6c'),'porcelain':('#dbd8c2','#aaa98f','#f1ead5'),'paper':('#d5ccb2','#a99e82','#ede3c8'),'denim':('#526f80','#354e60','#8198a3'),'rubber':('#414645','#262c2b','#666c65'),'amber':('#ac8046','#725837','#cfaa62'),'sage':('#859683','#546e62','#b0bda2'),'rose':('#a77f7b','#795c61','#d0b1a0'),'label':('#cec8ac','#99957f','#e7dfc4')}
 for name,(mid,dark,light) in palettes.items():
  im=Image.new('RGBA',(16,16),mid);d=ImageDraw.Draw(im)
  if name in ['panels','pavilion']:
   for x in [0,7,15]:d.line((x,0,x,15),fill=dark);d.line((min(15,x+1),0,min(15,x+1),15),fill=light)
   for x,y,length in [(3,2,5),(11,6,4),(4,11,3)]:d.line((x,y,x,y+length),fill=dark)
   if name=='panels':d.line((0,12,15,12),fill=dark);d.line((0,13,15,13),fill=light)
  elif name=='tile':
   d.line((0,0,15,0),fill=dark);d.line((0,0,0,15),fill=dark);d.line((8,0,8,15),fill=dark);d.line((0,8,15,8),fill=dark)
   for x,y in [(2,2),(10,10)]:d.line((x,y,x+3,y),fill=light)
  elif name=='paper':
   for y,length in [(3,11),(5,10),(7,12),(10,8),(12,11)]:d.line((2,y,length,y),fill=dark)
   d.line((2,1,13,1),fill=dark)
  elif name in ['denim','sage','rose']:
   for y in range(2,16,4):
    for x in range(1,16,4):d.line((x,y,x+1,y+1),fill=light)
   d.line((1,0,1,15),fill=dark)
  else:
   d.line((1,1,14,1),fill=light);d.line((1,2,1,13),fill=light);d.line((2,14,14,14),fill=dark)
  out[name]=im
 face=Image.new('RGBA',(16,16),'#c9c0a3');d=ImageDraw.Draw(face);d.rectangle((0,0,15,15),outline='#6e5b42');d.rectangle((2,2,13,13),outline='#91815e')
 for x,y in [(8,3),(12,8),(8,12),(3,8)]:d.point((x,y),fill='#484c40')
 d.line((8,8,8,4),fill='#44483d');d.line((8,8,11,10),fill='#44483d');out['clockface']=face
 for name,written in [('calendar',False),('calendar_written',True),('postcard',False),('postcard_written',True),('songstrips',False),('songstrips_written',True)]:
  im=Image.new('RGBA',(16,16),'#d8cfb5');d=ImageDraw.Draw(im)
  if 'postcard' in name and not written:d.rectangle((1,1,14,8),fill='#87999b');d.rectangle((1,9,14,14),fill='#637d7e');d.polygon([(1,9),(5,5),(9,9),(14,7),(14,10)],fill='#566a58')
  elif 'calendar' in name:
   d.rectangle((0,0,15,3),fill='#7e6953')
   for x in range(2,15,3):
    for y in range(5,12,3):d.point((x,y),fill='#796e58')
  else:
   for y in [2,6,10]:d.rectangle((1,y,14,y+2),fill='#aaa58b');d.line((3,y+1,10,y+1),fill='#615b48')
  if written:
   for y,length in [(5,12),(8,13),(11,11),(14,12)]:d.line((2,y,length,y),fill='#514938')
  out[name]=im
 # A low-resolution animated weather radar, authored directly at native texel scale.
 frames=Image.new('RGBA',(16,128))
 for n in range(8):
  im=Image.new('RGBA',(16,16),'#283f3c');d=ImageDraw.Draw(im)
  for x in [3,8,13]:d.line((x,0,x,15),fill='#36554d')
  for y in [4,9,14]:d.line((0,y,15,y),fill='#36554d')
  d.line((1,13,5,8,9,9,14,3),fill='#84926b')
  for k in range(3):
   x=(n+k*4)%18-2;d.rectangle((x,3+k*3,x+3,5+k*3),fill=['#567e5d','#8d9b61','#baaa6d'][k])
  d.line((8,8,8+int(7*math.cos(n*math.pi/4)),8+int(7*math.sin(n*math.pi/4))),fill='#9ebc86');frames.paste(im,(0,n*16))
 out['radar']=frames
 forecast=Image.new('RGBA',(16,16),'#435964');d=ImageDraw.Draw(forecast);d.rectangle((0,12,15,15),fill='#a9b8ae');d.rectangle((2,3,6,6),fill='#b6c0b5');d.rectangle((5,2,12,6),fill='#cbd1bd')
 for x in [4,7,10]:d.line((x,8,x-1,10),fill='#829fa6')
 for x,h in [(2,2),(5,3),(8,2),(11,1)]:d.line((x,14,x,14-h),fill='#62786e')
 out['forecast']=forecast
 notice=Image.new('RGBA',(16,16),'#797959');d=ImageDraw.Draw(notice);d.rectangle((1,1,14,3),fill='#c3bd9a')
 for y,length in [(6,12),(8,10),(10,13),(13,8)]:d.line((2,y,length,y),fill='#d1c9a8')
 out['notice']=notice
 snow=Image.new('RGBA',(16,64))
 for frame in range(4):
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
  for y in range(16):
   for x in range(16):v=65+((x*17+y*29+frame*31)%67);d.point((x,y),fill=(v,v+3,v-2,255))
  snow.paste(im,(0,frame*16))
 out['snow']=snow

def models(out,model,cube):
 def C(a,b,t):return cube(a,b,t)
 def M(n,e,layer=None):model(n,e,layer)
 M('clock_base',[C([2,0,3],[14,2,13],'oak'),C([3,2,4],[13,16,12],'oak'),C([5,4,12.05],[11,14,12.2],'glass'),C([7,5,12.25],[9,11,12.5],'brass')])
 M('clock_face',[C([3,0,4],[13,13,12],'oak'),C([1,13,2],[15,15,14],'oak'),C([4,2,12.05],[12,11,12.3],'clockface')])
 M('bell',[C([3,0,3],[13,1,13],'rubber'),C([4,1,4],[12,3,12],'brass'),C([6,3,6],[10,4,10],'brass'),C([7,4,7],[9,5,9],'metal')])
 for written in [False,True]:
  M('calendar'+('_written' if written else ''),[C([2,1,0],[14,16,.5],'calendar_written' if written else 'calendar')])
 M('reception',[C([0,0,1],[1,16,3],'oak'),C([15,0,1],[16,16,3],'oak'),C([1,15,1],[15,16,3],'oak'),C([1,0,1],[15,1,3],'oak'),C([1,1,1.2],[15,15,1.5],'frosted')],'translucent')
 cart=[C([x,0,z],[x+2,7,z+2],'metal') for x in [1,13] for z in [2,12]]+[C([0,7,1],[16,8,15],'metal'),C([2,8,3],[14,10,14],'rubber'),C([4,8.5,14.05],[12,9.2,14.2],'metal'),C([1,10.1,2],[15,23,14],'rubber'),C([2,11,14.05],[12,21,14.2],'radar'),C([13,13,14.1],[14.1,15,15.2],'brass')]
 for i in range(4):
  e=[dict(x) for x in cart]
  if i==1:e[-2]=C([2,11,14.05],[12,21,14.2],'forecast')
  if i==2:e[-2]=C([2,11,14.05],[12,21,14.2],'notice')
  if i==3:e[-2]=C([2,11,14.05],[12,21,14.2],'snow')
  M('crt'+('' if i==0 else '_channel_'+str(i)),e)
 M('cooler',[C([3,0,3],[13,15,13],'porcelain'),C([4,15,4],[12,16,12],'metal'),C([5,16.1,5],[11,24,11],'glass'),C([6,9,13.1],[8,10,15],'brass'),C([9,9,13.1],[11,10,15],'sage'),C([4,5,13.1],[12,6,15.5],'metal')])
 M('paper',[C([2,0,3],[14,.4,12],'newsprint')]);M('magazines',[C([2,0,2],[13,.8,13],'sage'),C([3,.85,3],[14,1.3,14],'magazine_cover')])
 glasses=[C([2,0,7],[6,.5,10],'rubber'),C([10,0,7],[14,.5,10],'rubber'),C([6,.1,8],[10,.4,8.7],'brass'),C([2,.55,9.2],[2.5,2,14],'rubber'),C([13.5,.55,9.2],[14,2,14],'rubber')]
 M('glasses',glasses);M('glasses_turned',[])
 phone=[C([4,0,4],[12,.8,15],'rubber'),C([4.5,.85,4.5],[11.5,.95,14.5],'glass'),C([7,.98,13.5],[9,1.1,13.9],'cream')]
 # Taken, the phone leaves its charging cable coiled on the armrest, still plugged in.
 M('phone',phone);M('phone_turned',[C([9,0,11],[15,.3,11.6],'rubber'),C([9,0,11.6],[9.6,.3,14],'rubber'),C([9.6,0,13.4],[13,.3,14],'rubber'),C([13,0,13],[14.5,.8,14.6],'cream')])
 M('armchair',[C([1,0,2],[3,5,4],'oak'),C([13,0,2],[15,5,4],'oak'),C([1,0,12],[3,5,14],'oak'),C([13,0,12],[15,5,14],'oak'),C([1,5,2],[15,8,14],'denim'),C([1,8,2],[15,17,5],'denim'),C([0,8,5.1],[2,11,15],'oak'),C([14,8,5.1],[16,11,15],'oak')])
 M('sink',[C([1,0,1],[15,13,15],'sage'),C([0,13,0],[16,14,16],'porcelain'),C([0,14,0],[16,16,2],'porcelain'),C([0,14,14],[16,16,16],'porcelain'),C([0,14,2],[2,16,14],'porcelain'),C([14,14,2],[16,16,14],'porcelain')])
 tap=[C([7,0,2],[9,7,4],'metal'),C([7,7,2],[9,8,10],'metal'),C([3,0,2],[6,1,5],'metal'),C([10,0,2],[13,1,5],'metal')];M('tap',tap);M('tap_turned',tap+[C([7.6,1,8.5],[8.4,6.95,9.5],'glass')])
 M('cabinet',[C([0,0,0],[16,16,16],'cream'),C([1,2,16.05],[7.5,13,16.2],'oak'),C([8.5,2,16.05],[15,13,16.2],'oak'),C([0,16.05,0],[1,30,15],'cream'),C([15,16.05,0],[16,30,15],'cream'),C([1,16.05,0],[15,30,1],'cream'),C([0,30,0],[16,31,15],'cream')])
 def routine(i,x):
  if i in [0,11]:return [C([x-2,0,5],[x+2,1,12],'rose' if i==0 else 'cream'),C([x-1.7,1.05,5.5],[x+1.7,1.6,11.5],'rose' if i==0 else 'cream')]
  if i in [1,10]:return [C([x-1.8,0,6],[x+1.8,2.8,10],'cream' if i==1 else 'metal'),C([x-1.9,2.85,5.9],[x+1.9,3.5,10.1],'sage' if i==1 else 'rubber')]
  if i in [3,7]:return [C([x-1.5,0,6.5],[x+1.5,4,9.5],'amber' if i==3 else 'porcelain'),C([x-.9,4.05,7],[x+.9,5.3,9],'rubber'),C([x-1.2,1,9.55],[x+1.2,2.8,9.65],'label')]
  if i==5:return [C([x-1.9,0,5.5],[x+1.9,.5,11.5],'porcelain'),C([x-1.5,.55,7],[x-.3,1.2,10],'rubber'),C([x+.3,.55,7],[x+1.5,1.2,10],'rose')]
  if i==6:return [C([x-.6,0,4],[x+.6,.8,7],'oak'),C([x-1.7,0,7],[x+1.7,1.2,11],'oak'),C([x-1.5,1.25,7.2],[x+1.5,1.8,10.8],'rubber')]
  if i==8:return [C([x-1.8,0,6],[x-.7,2.5,11],'amber'),C([x+.7,0,6],[x+1.8,2.5,11],'amber'),C([x-.65,1.5,7],[x+.65,2.4,10],'brass')]
  return [C([x-1.8,0,5],[x+1.8,.65,6],'rubber')]+[C([x-1.8+k*.8,0,6],[x-1.4+k*.8,.65,10 if i!=4 else 12],'rubber') for k in range(5)]
 for group,name in enumerate(['routine_a','routine_b','routine_c','routine_d']):
  pieces=[routine(group*3+i,3+i*5) for i in range(3)]
  for selected in range(4):M(name+('' if selected==0 else '_lifted_'+str(selected)),[part for i,parts in enumerate(pieces) if i+1!=selected for part in parts])
  for i in range(3):M('routine_item_'+str(group*3+i),routine(group*3+i,8))
 M('light_switch',[C([5,4,0],[11,12,.4],'cream'),C([7,7,.4],[9,11,1.2],'rubber')]);M('light_switch_turned',[C([5,4,0],[11,12,.4],'cream'),C([7,5,.4],[9,9,1.2],'rubber')])
 # Tucked at the edge of the mirror frame beside it.
 for written in [False,True]:M('postcard'+('_written' if written else ''),[C([0,4,0],[10,12,.3],'postcard_written' if written else 'postcard')])
 jukebox=[C([1,0,1],[15,2,15],'rubber'),C([2,2,2],[14,20,14],'oak'),C([1,4,14.05],[3,22,15.2],'brass'),C([13,4,14.05],[15,22,15.2],'brass'),C([3,20.1,14.05],[13,22,15.2],'brass'),C([3.1,12,14.05],[12.9,19.9,14.3],'songstrips'),C([4,4,14.1],[12,11,14.4],'rubber')]
 M('jukebox',jukebox);jukebox[-2]=C([3.1,12,14.05],[12.9,19.9,14.3],'songstrips_written');M('jukebox_written',jukebox)
 M('string_light',[C([0,14,7],[16,14.4,8],'rubber'),C([7,11,7],[9,13.9,9],'cream')])
 M('rowboat',[C([-12,3,3],[28,5,13],'oak'),C([-14,5,1],[-12,9,15],'oak'),C([28,5,1],[30,9,15],'oak'),C([-12,5,0],[28,9,2],'oak'),C([-12,5,14],[28,9,16],'oak'),C([-11.9,5.05,2.1],[27.9,8.5,13.9],'sage')])
 M('ashtray',[C([4,0,4],[12,.5,12],'glass'),C([4,.5,4],[12,1.4,5],'glass'),C([4,.5,11],[12,1.4,12],'glass'),C([4,.5,5],[5,1.4,11],'glass'),C([11,.5,5],[12,1.4,11],'glass'),C([6,.5,7],[9.5,.95,7.6],'cream')])
 M('bottle',[C([6,0,6],[10,7,10],'glass'),C([7,7.05,7],[9,10,9],'amber'),C([6.5,2,10.05],[9.5,5,10.2],'label')])
 # Worn glasses and the bottle keep their authored cuboids; everything held in the hand is a flat sprite (0.4.160).
 for name in ['glasses','bottle']:
  data={'parent':'the_oldest_house:block/read_'+name}
  if name=='glasses':data['display']={'head':{'rotation':[90,0,0],'translation':[0,0,1],'scale':[1,1,1]},'gui':{'rotation':[-75,0,0],'translation':[0,-2,0],'scale':[1.25,1.25,1.25]}}
  out['models/item/read_'+name+'.json']=(json.dumps(data,indent=2)+'\n').encode()
