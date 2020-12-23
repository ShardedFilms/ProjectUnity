package unity.type;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import unity.entities.*;
import unity.entities.comp.*;
import unity.entities.units.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/*unit.entities.units might be gradually deleted.
note that as classes are integrated, inner classes are extracted.*/
public class UnityUnitType extends UnitType{
    public final Seq<Weapon> segWeapSeq = new Seq<>();

    // worms
    public TextureRegion segmentRegion, tailRegion, segmentCellRegion, segmentOutline, tailOutline;
    public TextureRegion[] abilityRegions = new TextureRegion[AbilityTextures.values().length];
    public int segmentLength = 9;
    public float segmentOffset = 23f;
        // don't touch, please
    public TextureRegion bodyRegion, bodyOutlineRegion, tailOutlineRegion;
    public boolean splittable = false;
    public float headDamage = 0f;
    public float headTimer = 5f;

    // transforms
    public Prov<UnitType> toTrans;
    public float transformTime;

    // copters
    public final Seq<Rotor> rotors = new Seq<>(4);
    public float fallRotateSpeed = 2.5f;

    public UnityUnitType(String name){
        super(name);
    }

    @Override
    public Unit create(Team team){
        Unit ret = super.create(team);
        //transformer
        if(ret instanceof TransformerBase transformer) transformer.setTimeTrans(transformTime);
        return ret;
    }

    @Override
    public void load(){
        super.load();

        //copter
        rotors.each(Rotor::load);
        //worm

        segmentRegion = atlas.find(name + "-segment");
        segmentCellRegion = atlas.find(name + "-segment-cell");
        tailRegion = atlas.find(name + "-tail");
        segmentOutline = atlas.find(name + "-segment-outline");
        tailOutline = atlas.find(name + "-tail-outline");
        //abilities
        for(AbilityTextures type : AbilityTextures.values()){
            abilityRegions[type.ordinal()] = atlas.find(name + "-" + type.name());
        }

        //please do not the touch
        bodyRegion = atlas.find(name + "-body");
        bodyOutlineRegion = atlas.find(name + "-body-outline");
        tailOutlineRegion = atlas.find(name + "-tail-outline");

        segWeapSeq.each(Weapon::load);
    }

    @Override
    public void init(){
        super.init();
        //copter
        Seq<Rotor> mapped = new Seq<>();

        rotors.each(rotor -> {
            mapped.add(rotor);

            if(rotor.mirror){
                Rotor copy = rotor.copy();
                copy.x *= -1f;
                copy.speed *= -1f;
                copy.rotOffset += 180f; //might change later

                mapped.add(copy);
            }
        });

        rotors.set(mapped);
        //worm
        sortSegWeapons(segWeapSeq);
    }

    public void sortSegWeapons(Seq<Weapon> weaponSeq){
        Seq<Weapon> mapped = new Seq<>();
        for(int i = 0, len = weaponSeq.size; i < len; i++){
            Weapon w = weaponSeq.get(i);
            mapped.add(w);
            if(w.mirror){
                Weapon copy = w.copy();
                copy.x *= -1;
                copy.shootX *= -1;
                copy.flipSprite = !copy.flipSprite;
                mapped.add(copy);
                w.reload *= 2;
                copy.reload *= 2;
                w.otherSide = mapped.size - 1;
                copy.otherSide = mapped.size - 2;
            }
        }
        weaponSeq.set(mapped);
    }

    @Override
    public void draw(Unit unit){
        super.draw(unit);

        // copter
        if(unit instanceof Copterc){
            drawRotors(unit);
        }
    }

    @Override
    public void drawShadow(Unit unit){
        if(unit instanceof Wormc){
            var worm = (Unit & Wormc)unit;

            Draw.color(Pal.shadow);
            float e = Math.max(unit.elevation, visualElevation);

            if(worm.isHead()){
                Draw.rect(region, worm.x + shadowTX * e, worm.y + shadowTY * e, worm.rotation - 90);
            }else if(worm.isTail()){
                Draw.rect(tailRegion, worm.x + shadowTX * e, worm.y + shadowTY * e, worm.rotation - 90);
            }else{
                Draw.rect(bodyRegion, worm.x + shadowTX * e, worm.y + shadowTY * e, worm.rotation - 90);
            }
        }else{
            super.drawShadow(unit);
        }

        //worm
        if(unit instanceof WormDefaultUnit wormunit) wormunit.drawShadow();
    }

    @Override
    public void drawSoftShadow(Unit unit){
        super.drawSoftShadow(unit);
        //worm
        if(!(unit instanceof WormDefaultUnit wormUnit)) return;
        for(int i = 0; i < segmentLength; i++) wormUnit.segmentUnits[i].drawSoftShadow();
    }

    @Override
    public void drawBody(Unit unit){
        float z = Draw.z();

        if(unit instanceof Wormc){
            var worm = (Unit & Wormc)unit;

            applyColor(unit);

            Draw.z(1.01f + z + worm.layer());
            if(worm.isHead()){
                Draw.rect(region, worm.x, worm.y, worm.rotation - 90);
            }else if(worm.isTail()){
                Draw.rect(tailRegion, worm.x, worm.y, worm.rotation - 90);
            }else{
                Draw.rect(bodyRegion, worm.x, worm.y, worm.rotation - 90);
            }

            Draw.reset();
        }else{
            super.drawBody(unit);
        }

        //worm
        if(unit instanceof WormDefaultUnit wormUnit){
            for(int i = 0; i < segmentLength; i++){
                Draw.z(z - (i + 1f) / 500f);
                wormUnit.segmentUnits[i].drawBody();
                drawWeapons(wormUnit.segmentUnits[i]);
            }
        }

        Draw.z(z);
    }

    @Override
    public void drawEngine(Unit unit){
        float z = Draw.z();

        if(unit instanceof Wormc worm){
            if(worm.isHead() && worm.child() == null){
                Draw.z(1.01f + z + worm.layer());

                super.drawEngine(unit);
                Draw.z(z);
            }
        }else{
            super.drawEngine(unit);
        }
    }

    @Override
    public void drawWeapons(Unit unit){
        float z = Draw.z();

        if(unit instanceof Wormc worm){
            Draw.z(1.01f + z + worm.layer());
        }

        super.drawWeapons(unit);
        Draw.z(z);
    }

    @Override
    public void drawCell(Unit unit){
        float z = Draw.z();

        if(unit instanceof Wormc worm){
            Draw.z(1.01f + z + worm.layer());
        }

        super.drawCell(unit);
        Draw.z(z);
    }

    @Override
    public void drawOutline(Unit unit){
        float z = Draw.z();
        Draw.reset();

        if(unit instanceof Wormc){
            var worm = (Unit & Wormc)unit;

            Draw.z(1.01f + z + worm.layer());
            if(worm.isHead()){
                Draw.rect(outlineRegion, worm.x, worm.y, worm.rotation - 90);
            }else if(worm.isTail()){
                Draw.rect(tailOutlineRegion, worm.x, worm.y, worm.rotation - 90);
            }else{
                Draw.rect(bodyOutlineRegion, worm.x, worm.y, worm.rotation - 90);
            }
        }else{
            super.drawOutline(unit);
        }

        Draw.z(z);
    }

    public void drawRotors(Unit unit){
        Draw.mixcol(Color.white, unit.hitTime);
        for(Rotor rotor : rotors){
            float offX = Angles.trnsx(unit.rotation - 90, rotor.x, rotor.y);
            float offY = Angles.trnsy(unit.rotation - 90, rotor.x, rotor.y);

            float w = rotor.bladeRegion.width * rotor.scale * Draw.scl;
            float h = rotor.bladeRegion.height * rotor.scale * Draw.scl;

            for(int i = 0; i < 2; i++){
                for(int j = 0; j < rotor.bladeCount; j++){
                    float angle = (unit.id * 24f + Time.time * rotor.speed + (360f / (float)rotor.bladeCount) * j + rotor.rotOffset) % 360;
                    Draw.alpha(state.isPaused() ? 1f : Time.time % 2);

                    Draw.rect(i == 0 ? rotor.bladeOutlineRegion : rotor.bladeRegion, unit.x + offX, unit.y + offY, w, h, angle);
                }
            }

            Draw.alpha(1f);
            Draw.rect(rotor.topRegion, unit.x + offX, unit.y + offY, unit.rotation - 90f);
        }

        Draw.mixcol();
    }
}