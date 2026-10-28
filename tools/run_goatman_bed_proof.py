"""Two actual socket clients use opposite Goatman beds and capture their production cameras."""
from pathlib import Path
import json,os,signal,subprocess,time

ROOT=Path(__file__).resolve().parents[1];WORLD=ROOT/'run-goatman-bed137';REPORT=ROOT/'build/goatman-bed137-proof'
os.chdir(ROOT);os.environ.setdefault('ALSOFT_DRIVERS','null')
if (WORLD/'server/world/level.dat').exists() or (REPORT/'passed.txt').exists():raise SystemExit('Preserve the previous isolated bed proof/world before running a fresh proof.')
REPORT.mkdir(parents=True,exist_ok=True)
init=REPORT/'launch.init.gradle'
init.write_text('''import groovy.json.JsonOutput
allprojects { afterEvaluate {
    def nativeRuns = ['runProofServer', 'runProofA', 'runProofB'].collectEntries { name -> [(name): tasks.named(name)] }
    def launchFile = layout.buildDirectory.file('goatman-bed137-proof/launch.json').get().asFile
    tasks.register('exportGoatmanBedLaunch') {
        dependsOn 'classes', 'prepareProofServerRun', 'prepareProofARun', 'prepareProofBRun'
        doLast {
            def launches = [:]
            nativeRuns.each { name, provider ->
                def run = provider.get()
                run.classpath(run.classpathProvider)
                def command = [run.javaLauncher.get().executablePath.asFile.absolutePath]
                command.addAll(run.allJvmArgs)
                command.addAll(['-Dthe_oldest_house.goatmanBedSocketSmoke=true', '-Dthe_oldest_house.liveProof=false', '-cp', run.classpath.asPath, run.mainClass.get()])
                command.addAll(run.args)
                launches[name] = [command: command, directory: run.gameDirectory.get().asFile.absolutePath]
            }
            launchFile.text = JsonOutput.toJson(launches)
        }
    }
} }
''')
args=['gradle','exportGoatmanBedLaunch','-PgoatmanBedSocketSmoke','--no-daemon','--no-configuration-cache','--max-workers=2','-Dorg.gradle.jvmargs=-Xmx768m','-I',str(init)]
if os.environ.get('GOATMAN_PROOF_OFFLINE'):args.append('--offline')
try:
    with (REPORT/'prepare.log').open('w') as out:subprocess.run(args,stdout=out,stderr=subprocess.STDOUT,check=True)
except subprocess.CalledProcessError:
    print((REPORT/'prepare.log').read_text(errors='replace')[-16000:],flush=True)
    raise
launches=json.loads((REPORT/'launch.json').read_text())
for role in ['server','a','b']:(WORLD/role).mkdir(parents=True,exist_ok=True)
(WORLD/'server/eula.txt').write_text('eula=true\n')
(WORLD/'server/server.properties').write_text('server-ip=127.0.0.1\nserver-port=25578\nonline-mode=false\nview-distance=4\nsimulation-distance=3\nspawn-protection=0\nspawn-monsters=false\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\nsync-chunk-writes=false\n')
for role in ['a','b']:(WORLD/role/'options.txt').write_text('pauseOnLostFocus:false\nrenderDistance:4\nsimulationDistance:3\nmaxFps:25\nenableVsync:false\nguiScale:2\ntutorialStep:none\n')
processes=[];logs=[]
def launch(task,name):
    entry=launches[task];out=(REPORT/(name+'.log')).open('w');logs.append(out)
    command=entry['command'];assert any('goatmanBedSocketSmoke=true' in x for x in command),task
    p=subprocess.Popen(command,cwd=entry['directory'],stdout=out,stderr=subprocess.STDOUT,start_new_session=True);processes.append(p);return p

def wait(check,seconds,context):
    deadline=time.monotonic()+seconds
    while time.monotonic()<deadline:
        if check():print(context,flush=True);return
        if (REPORT/'failed.txt').exists():raise RuntimeError((REPORT/'failed.txt').read_text())
        for p in processes:
            if p.poll() is not None and p.returncode:raise RuntimeError(context+': native process exited '+str(p.returncode))
        time.sleep(1)
    raise RuntimeError('Timed out: '+context)
def server_log():
    p=WORLD/'server/logs/latest.log';return p.read_text(errors='replace') if p.exists() else ''
def shutdown():
    began=time.monotonic();sampled=False
    while any(p.poll() is None for p in processes):
        if not sampled and time.monotonic()-began>=15:
            sampled=True
            for i,p in enumerate(processes):
                if p.poll() is not None:continue
                try:
                    dump=subprocess.run(['jcmd',str(p.pid),'Thread.print','-l'],capture_output=True,text=True,timeout=5)
                    (REPORT/('shutdown-threads-'+str(i)+'.txt')).write_text(dump.stdout+dump.stderr)
                except (OSError,subprocess.TimeoutExpired) as exc:(REPORT/('shutdown-diagnostics-'+str(i)+'.txt')).write_text(str(exc))
        if time.monotonic()-began>=90:raise RuntimeError('Native bed processes did not shut down cleanly within 90 seconds; see shutdown thread diagnostics')
        time.sleep(.5)
    for p in processes:assert p.returncode==0,p.returncode
try:
    host=launch('runProofServer','server');wait(lambda:'Done (' in server_log(),180,'Native bed host started')
    launch('runProofA','client-a');wait(lambda:'OTHProofA joined the game' in server_log(),180,'First actual client joined')
    launch('runProofB','client-b');wait(lambda:(REPORT/'passed.txt').exists(),240,'Both native sleeping cameras passed')
    shutdown()
    for role in ['A','B']:
        assert (REPORT/(role+'-bed-camera.png')).stat().st_size>500
        assert (REPORT/(role+'-bed-camera.txt')).stat().st_size>100
    print((REPORT/'passed.txt').read_text(),flush=True)
finally:
    for p in processes:
        if p.poll() is None:
            os.killpg(p.pid,signal.SIGTERM)
            try:p.wait(timeout=12)
            except subprocess.TimeoutExpired:os.killpg(p.pid,signal.SIGKILL);p.wait(timeout=5)
    for out in logs:out.close()
