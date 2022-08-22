package unity.world.blocks.power;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import unity.graphics.*;
import unity.world.blocks.*;
import unity.world.graph.GraphConnector.*;
import unity.world.graph.*;

public class SeebeckGenerator extends GenericGraphBlock{
    public float maxPower = 10;
    public float seebeckStrength = 2f;
    TextureRegion[] rotations;
    TextureRegion[] heat;

    public SeebeckGenerator(String name){
        super(name);
        flags = EnumSet.of(BlockFlag.generator);
        rotate = true;
        outputsPower = true;
        consumesPower = false;
    }

    @Override
    public void load(){
        super.load();

        rotations = new TextureRegion[4];
        rotations[0] = Core.atlas.find(name + "-1");
        rotations[1] = Core.atlas.find(name + "-2");
        rotations[2] = Core.atlas.find(name + "-3");
        rotations[3] = Core.atlas.find(name + "-4");
        heat = new TextureRegion[3];
        heat[0] = Core.atlas.find(name + "-heat-left");
        heat[1] = Core.atlas.find(name + "-heat-right");
        heat[2] = Core.atlas.find(name + "-heat-center");
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.basePowerGeneration, Core.bundle.get("stat.unity-seebeckStrength"), seebeckStrength * 60.0f);
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("power", (SeebeckGeneratorBuild entity) -> new Bar(
        () ->
        Core.bundle.format(
        "bar.poweroutput",
        Strings.fixed(entity.getPowerProduction() * 60 * entity.timeScale(), 1)
        ),
        () -> Pal.powerBar,
        () -> entity.getPowerProduction() / maxPower
        ));
    }

    public class SeebeckGeneratorBuild extends GenericGraphBuild{
        float leftHeat, rightHeat;
        float[] heatDiff = {0};
        float powerGen = 0;

        @Override
        public float getPowerProduction(){
            return Mathf.clamp(powerGen, 0, maxPower);
        }


        @Override
        public void updateTile(){
            super.updateTile();

            var node = heatNode();
            leftHeat = node.getTemp();
            rightHeat = node.getTemp();
            heatDiff[0] = 0;
            ((FixedGraphConnector<HeatGraph>)node.connector.get(0)).eachConnected((connector, port) -> {
                if(connector.getNode() instanceof HeatGraphNode heatNode){
                    heatDiff[0] += Math.abs(node.getTemp() - heatNode.getTemp()) * heatNode.conductivity; /// well to stop low conductivity from making huge temperature differentials
                    if(port.getOrdinal() != 0){
                        leftHeat = heatNode.getTemp();
                    }else{
                        rightHeat = heatNode.getTemp();
                    }
                }
            });
            powerGen += (seebeckStrength * heatDiff[0] - powerGen) * Mathf.clamp(0.1f * Time.delta);
        }

        @Override
        public void draw(){
            var node = heatNode();
            Draw.rect(rotations[rotation], x, y);
            YoungchaDrawf.drawHeat(heat[0], x, y, rotdeg(), leftHeat);
            YoungchaDrawf.drawHeat(heat[1], x, y, rotdeg(), rightHeat);
            YoungchaDrawf.drawHeat(heat[2], x, y, rotdeg(), node.getTemp());
            drawTeamTop();
        }
    }
}