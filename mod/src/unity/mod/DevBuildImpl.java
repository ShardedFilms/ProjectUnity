package unity.mod;

import arc.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Log.*;
import mindustry.ctype.*;
import mindustry.world.blocks.environment.*;
import unity.util.*;

import static mindustry.Vars.*;

/**
 * Developer build class to load when compiling with `-Pmod.dev=true`. Sets logging level to debug, enables JS console, imports all
 * Project Unity packages and classes to the JS console namespace, and logs missing content name and description bundle entries.
 * @author GlennFolker
 */
@SuppressWarnings("unchecked")
public class DevBuildImpl implements DevBuild{
    @Override
    public void setup(){
        enableConsole = true;
        Log.level = LogLevel.debug;
    }

    @Override
    public void init(){
        JSBridge.importDefaults(JSBridge.unityScope);
        for(Faction faction : Faction.all){
            Log.debug("Faction @ has @ contents.", faction.localizedName, FactionRegistry.contents(faction, Object.class).size);
        }

        Seq<Class<? extends Content>> ignore = Seq.with(Floor.class, Prop.class);
        for(Seq<Content> seq : content.getContentMap()){
            seq.each(c -> c instanceof MappableContent && c.minfo.mod != null && c.minfo.mod.name.equals("unity"), (MappableContent c) -> {
                String entry = c.getContentType().name() + "." + c.name + ".";
                if(!Core.bundle.has(entry + "name")){
                    Log.debug("Content @ has no bundle entry for name.", c);
                }

                if(!ignore.contains(c.getClass()::isAssignableFrom) && !Core.bundle.has(entry + "description")){
                    Log.debug("Content @ has no bundle entry for description.", c);
                }
            });
        }
    }

    @Override
    public boolean isDev(){
        return true;
    }
}