package unity;

import arc.*;
import arc.func.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import mindustry.ui.dialogs.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import unity.content.*;
import unity.mod.*;
import unity.mod.ContributorList.*;

public class Unity extends Mod{
    public static final String githubURL = "https://github.com/EyeOfDarkness/ProjectUnity";

    private final ContentList[] unityContent = {
        new UnityItems(),
        new OverWriter(),
        new UnityStatusEffects(),
        new UnityLiquids(),
        new UnityBullets(),
        new UnityUnitTypes(),
        new UnityBlocks(),
        new UnityPlanets(),
        new UnitySectorPresets(),
        new UnityTechTree(),
    };

    public Unity(){
        ContributorList.init();

        Events.on(ClientLoadEvent.class, e -> {
            Func<String, String> stringf = value -> Core.bundle.get("mod." + value);

            Time.runTask(10f, () -> {
                //TODO make it also on the about dialog rather than annoyingly pop up everytime it loads
                BaseDialog dialog = new BaseDialog("@credits");
                Table cont = dialog.cont;

                cont.table(t -> {
                    t.add("@mod.credits.text").fillX().pad(3f).wrap().get().setAlignment(Align.center);
                    t.row();

                    t.add("@mod.credits.bottom-text").fillX().pad(3f).wrap().get().setAlignment(Align.center);
                    t.row();
                }).pad(3f);

                cont.row();

                cont.table(b -> {
                    for(ContributionType type : ContributionType.all){
                        Seq<String> list = ContributorList.getBy(type);
                        if(list.size <= 0) continue;

                        b.table(t -> {
                            t.add(stringf.get(type.name())).pad(3f).center();
                            t.row();
                            t.pane(p -> {
                                for(String c : list){
                                    p.add("[lightgray]" + c).left().pad(3f).padLeft(6f).padRight(6f);
                                    p.row();
                                }
                            });
                        }).pad(6f).top();
                    }
                }).fillX();

                dialog.addCloseButton();
                dialog.show();
            });
        });
    }

    @Override
    public void init(){
        Vars.enableConsole = true;

        if(!Vars.headless){
            LoadedMod mod = Vars.mods.locateMod("unity");
            Func<String, String> stringf = value -> Core.bundle.get("mod." + value);

            mod.meta.displayName = stringf.get(mod.meta.name + ".name");
            mod.meta.description = stringf.get(mod.meta.name + ".description");
        }
    }

    @Override
    public void loadContent(){
        for(ContentList list : unityContent){
            try{
                list.load();

                Log.info("@: Loaded content list: @", getClass().getSimpleName(), list.getClass().getSimpleName());
            }catch(Exception e){
                Log.err("Error loading @ content(s): @", getClass().getSimpleName(), e.getMessage());
            }
        }
    }
    //still don't want to import-unimport Log.java everytime i debug.
    //unity.Unity.print(); for copypaste
    public static void print(Object... args){
        Log.info("[unity]: ", args);
    }
}