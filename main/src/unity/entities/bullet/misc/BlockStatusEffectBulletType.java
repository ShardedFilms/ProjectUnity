package unity.entities.bullet.misc;

import arc.math.Mathf;
import unity.entities.ExpOrbs;
import unity.world.blocks.defense.turrets.BlockOverdriveTurret;
import unity.world.blocks.defense.turrets.BlockOverdriveTurret.*;
import unity.world.blocks.exp.ExpHolder;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.*;

public class BlockStatusEffectBulletType extends BasicBulletType{
    public float strength = 2f;
    public int amount = 3;
    public boolean upgrade = false;

    public BlockStatusEffectBulletType(float speed, float damage){
        super(speed, damage);
    }

    @Override
    public void draw(Bullet b){
        //no
    }

    @Override
    public void update(Bullet b){
        Building target = null;
        boolean buffing = false;
        float phaseHeat = 0;
        float phaseBoost = 0;
        float phaseExpBoost = 0;

        if (b.owner instanceof BlockOverdriveTurretBuild bb){
            target = bb.target;
            buffing = bb.buffing;
            phaseHeat = bb.phaseHeat;
            phaseBoost = ((BlockOverdriveTurret) bb.block).phaseBoost;
            phaseExpBoost = ((BlockOverdriveTurret) bb.block).phaseExpBoost;
        }


        if (buffing){
            if (b.x == target.x && b.y == target.y){
                strength = Mathf.lerpDelta(strength, 3f + phaseHeat * phaseBoost, 0.02f);
                if (b.timer(0, 179f)) {
                    if (upgrade) addExp(target, 5 + phaseBoost * phaseExpBoost, true);
                    else buff(target, strength + phaseHeat * phaseBoost);
                }
            }
        }else{
            strength = 1f;
        }
    }

    public void buff(Building b, float intensity){
        b.applyBoost(intensity, 180f);
        if (b.health < b.maxHealth) b.heal(intensity);
        else addExp(b, 1, false);
    }

    public void addExp(Building b, float intensity, boolean spread){
        if (b instanceof ExpHolder exp) {
            exp.handleExp(Mathf.round(exp.getExp() * 0.1f) / 10 * Mathf.round(intensity));
            if (spread) ExpOrbs.spreadExp(b.x, b.y, amount);
        }
    }
}