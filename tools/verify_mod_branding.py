"""Check the shipped display name and archive without changing saved registry identities."""
import argparse
from pathlib import Path
import tomllib
import zipfile


ROOT = Path(__file__).resolve().parents[1]


def properties():
    return dict(line.split("=", 1) for line in
                (ROOT / "gradle.properties").read_text().splitlines()
                if line and not line.startswith("#") and "=" in line)


def verify(path):
    props = properties()
    assert props["mod_name"] == "This House Has Ghosts in It"
    assert props["mod_id"] == "the_oldest_house", "Existing worlds require the original registry namespace"
    assert props["mod_archive_name"] == "this_house_has_ghosts_in_it"
    expected = f"{props['mod_archive_name']}-{props['mod_version']}.jar"
    assert path.name == expected, (path.name, expected)
    with zipfile.ZipFile(path) as jar:
        assert jar.testzip() is None, "Playable JAR CRC failure"
        info = tomllib.loads(jar.read("META-INF/neoforge.mods.toml").decode())
        mods = info["mods"]
        assert len(mods) == 1 and mods[0]["modId"] == props["mod_id"]
        assert mods[0]["displayName"] == props["mod_name"]
        assert mods[0]["version"] == props["mod_version"]
        manifest = jar.read("META-INF/MANIFEST.MF").decode().replace("\r\n ", "").replace("\r\n", "\n")
        attributes = dict(line.split(": ", 1) for line in manifest.splitlines() if ": " in line)
        assert attributes["Implementation-Title"] == props["mod_name"]
        assert attributes["Implementation-Version"] == props["mod_version"]
        for name in ("the_oldest_house.mixins.json", "the_oldest_house.common.mixins.json",
                     "assets/the_oldest_house/lang/en_us.json",
                     "io/github/knaitoe/theoldesthouse/TheOldestHouse.class",
                     "io/github/knaitoe/theoldesthouse/house/HouseSavedData.class"):
            assert name in jar.namelist(), f"Lost compatibility entry: {name}"
    print(f"PASS: {path.name}: {props['mod_name']}; original mod ID, resources, mixins and saved-state classes retained")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", nargs="?", const="auto", default="auto")
    args = parser.parse_args()
    if args.jar == "auto":
        jars = [p for p in (ROOT / "build/libs").glob("*.jar")
                if not p.name.endswith(("-sources.jar", "-javadoc.jar"))]
        assert len(jars) == 1, f"Expected one exact playable JAR: {jars}"
        path = jars[0]
    else:
        path = Path(args.jar)
    verify(path)


if __name__ == "__main__":
    main()
