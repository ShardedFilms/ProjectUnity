package unity.world.blocks.defense;

import arc.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.ui.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.meta.*;

public class PowerWall extends Wall{
    public ObjectFloatMap<Class<?>> energyMultiplier = new ObjectFloatMap<>();
    public float powerProduction = 2f;
    public float damageThreshold = 150f;
    public float overloadDamage = 0.8f;

    public PowerWall(String name){
        super(name);
        update = true;
        sync = true;
        flashHit = true;
        solid = true;
        consumesPower = false;
        outputsPower = true;
        hasPower = true;
        group = BlockGroup.walls;
    }

    @Override
    public void setBars(){
        super.setBars();
        if(hasPower && outputsPower && !consumes.hasPower()){
            bars.add("power", (PowerWallBuild entity) -> new Bar(
                () -> Core.bundle.format("bar.poweroutput", Strings.fixed(entity.getPowerProduction() * 60f * entity.timeScale(), 1)),
                () -> Pal.powerBar,
                () -> entity.productionEfficiency)
            );
        }
    }

    public class PowerWallBuild extends WallBuild{
        public float productionEfficiency = 0.0f;
        protected boolean overloaded;

        @Override
        public void updateTile(){
            super.updateTile();
            productionEfficiency = Mathf.lerpDelta(productionEfficiency, 0f, 0.05f);

            overloaded = productionEfficiency > 1f;
            if(overloaded){
                health -= overloadDamage * Time.delta;
            }
        }

        @Override
        public boolean collision(Bullet bullet){
            productionEfficiency +=
                (bullet.damage() / damageThreshold)
                * (bullet.vel().len() / bullet.type.speed)
                * energyMultiplier.get(
                    bullet.type.getClass().isAnonymousClass()
                    ?   bullet.type.getClass().getSuperclass()
                    :   bullet.type.getClass()
                , 1f);

            //reduce the production efficiency to 0 if healed by a friendly bullet
            if(!bullet.team.isEnemy(team) && bullet.type.healPercent > 0f){
                productionEfficiency = 0f;
            }

            return super.collision(bullet);
        }

        @Override
        public float getPowerProduction(){
            return powerProduction * productionEfficiency;
        }

        @Override
        public void heal(){
            super.heal();
            productionEfficiency = 0f;
        }

        @Override
        public void heal(float amount){
            super.heal(amount);
            productionEfficiency = 0f;
        }

        @Override
        public void health(float health){
            if(this.health < health) productionEfficiency = 0f;
            super.health(health);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(productionEfficiency);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            productionEfficiency = read.f();
        }
    }
}
