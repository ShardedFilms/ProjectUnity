package unity.gensrc.entities;

import mindustry.gen.*;
import mindustry.type.*;
import unity.annotations.Annotations.*;
import unity.entities.*;
import unity.entities.prop.*;
import unity.entities.type.*;
import unity.gen.entities.*;
import unity.mod.*;

import static mindustry.Vars.*;

@EntityComponent
abstract class MonolithComp implements Unitc, Factionc, SoulHolder{
    @Import UnitType type;
    @Import boolean spawnedByCore;

    private int souls;
    private transient MonolithProps props;

    @Override
    public Faction faction(){
        return FactionRegistry.faction(type);
    }

    @Override
    public void setType(UnitType type){
        // Units spawned by core can't have souls.
        if(!spawnedByCore && type instanceof PUUnitTypeCommon def) props = def.propReq(MonolithProps.class);
    }

    @Override
    public void add(){
        souls = props.startupSouls;
    }

    @Override
    @MethodPriority(-5)
    @Extend(Unitc.class)
    public void update(){
        StatusEffect eff = props.soulLackStatus;
        if(disabled()){
            if(!hasEffect(eff)){
                apply(eff, Float.MAX_VALUE);
            }
        }else{
            unapply(eff);
        }
    }

    @Override
    public void killed(){
        if(net.server() || !net.active()) spreadSouls();
    }

    /*@Override
    public boolean apply(MonolithSoul soul, int index, boolean transferred){
        if(isPlayer() && !transferred && (Mathf.chance(1f / souls) || index == souls - 1)){
            soul.controller(getPlayer());
            transferred = true;
        }

        return transferred;
    }*/

    boolean disabled(){
        return !spawnedByCore && souls <= 0;
    }

    @Override
    public int souls(){
        return souls;
    }

    @Override
    public int maxSouls(){
        return spawnedByCore ? 0 : props.maxSouls;
    }

    @Override
    public int transferSoul(int amount){
        int add = Math.min(maxSouls() - souls, amount);
        souls += add;

        return add;
    }

    @Override
    public int withdrawSoul(int amount){
        int rel = Math.min(souls, amount);
        souls -= rel;

        return rel;
    }
}