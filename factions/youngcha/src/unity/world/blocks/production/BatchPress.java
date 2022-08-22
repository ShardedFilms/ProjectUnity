package unity.world.blocks.production;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import unity.content.*;
import unity.util.*;

public class BatchPress extends GenericGraphCrafter{
    public Vec2[] itemPlace;
    public TextureRegion[] base = new TextureRegion[2];
    public TextureRegion[] top = new TextureRegion[2];
    TextureRegion topside, spinner;

    public BatchPress(String name){
        super(name);
    }

    @Override
    public void init(){
        super.init();
        itemCapacity = itemPlace.length;
    }

    @Override
    public void load(){
        super.load();
        base[0] = loadTex("base1");
        base[1] = loadTex("base2");
        top[0] = loadTex("top1");
        top[1] = loadTex("top2");
        topside = loadTex("top-side");
        spinner = loadTex("spinner");
    }

    public class BatchPressBuild extends GenericGraphCrafterBuild{
        public float lidOpened = 0;

        @Override
        public boolean acceptItem(Building source, Item item){
            return super.acceptItem(source, item) && !items.has(outputItem.item);
        }

        @Override
        public void updateEfficiencyMultiplier(){
            efficiency *= (items.has(outputItem.item) ? 0 : 1) * Mathf.clamp((torqueNode().getGraph().lastVelocity / torqueNode().maxSpeed));
        }

        //anuken's but return false when this has item even though one.
        @Override
        public boolean shouldConsume(){
            if(outputItems != null){
                for(var output : outputItems){
                    if(items.has(output.item)) return false;
                }
            }
            if(outputLiquids != null && !ignoreLiquidFullness){
                boolean allFull = true;
                for(var output : outputLiquids){
                    if(liquids.get(output.liquid) >= liquidCapacity - 0.001f){
                        if(!dumpExtraLiquid) return false;
                    }else allFull = false;
                }
                if(allFull) return false;
            }
            return enabled;
        }

        @Override
        public void updateTile(){
            super.updateTile();
            lidOpened = Mathf.clamp(Mathf.lerp(0, 1, lidOpened + (potentialEfficiency > 0 && shouldConsume() ? -1 : 1) * 0.1f * delta()));
        }

        @Override
        public void craft(){
            super.craft();
            YoungchaFx.smokePoof.at(x, y);
        }

        @Override
        public void draw(){
            Draw.rect(base[rotation % 2], x, y);
            int[] index = {0};
            items.each((item, am) -> {
                if(index[0] >= itemPlace.length){
                    return;
                }
                for(int i = 0; i < am; i++){
                    Vec2 loc = itemPlace[index[0]];
                    Draw.rect(item.fullIcon, x + loc.x, y + loc.y, Vars.itemSize, Vars.itemSize);
                    index[0]++;
                    if(index[0] >= itemPlace.length){
                        return;
                    }
                }
            });
            float hittime = 0.90f;
            float spinprog = Mathf.map(progress, 0, hittime, 0, 1.02f);
            float shake = 0;
            if(progress > hittime){
                spinprog = Mathf.map(progress, hittime, 1.0f, 1.02f, 1.0f);
                shake = Mathf.clamp(0.05f - (progress - hittime)) * 20.0f;
            }


            float spinnerrot = Mathf.sqr(spinprog) * 360;
            Draw.z(Layer.blockOver);
            DrawUtils.drawRectOrtho(top[rotation % 2], x - 9 + Mathf.range(shake), y + Mathf.range(shake), 9, 0, 2, 18, 18, 90 * lidOpened, 0);
            DrawUtils.drawRectOrtho(topside, x - 9, y, -1, 0, 18, 2, 18, 90 * lidOpened - 90, 0);
            if(lidOpened < 0.01){
                Draw.color(Color.black, Mathf.clamp(progress * 0.6f, 0, 0.22f));
                Draw.rect(spinner, x - (4 - spinprog * 3), y - (4 - spinprog * 3), spinnerrot);
                Draw.color();
            }
            DrawUtils.drawRectOrtho(spinner, x - 9, y, 9, 0, 2, 18, 18, 90 * lidOpened, 0, -spinnerrot);
            drawTeamTop();
        }
    }
}