package unity.libraries;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.Time;
import mindustry.type.*;
import mindustry.gen.*;
import mindustry.ai.types.FlyingAI;

import static arc.Core.*;
import static mindustry.Vars.*;

public class CopterUnitType extends UnitType{
	protected final Rotor[] rotors;
	protected float fallRotateSpeed = 2.5f;

	public CopterUnitType(String name, Rotor[] rotors){
		super(name);
		this.rotors = rotors;
		defaultController=()->new CopterAI();
	}

	@Override
	public void load(){
		super.load();
		for (int i = 0; i < rotors.length; i++){
			Rotor temp = rotors[i];
			temp.bladeRegion = atlas.find(name + "-rotor-blade");
			temp.topRegion = atlas.find(name + "-rotor-top");
			temp.bladeOutlineRegion = atlas.find(name + "-rotor-blade-outline");
			temp.topOutlineRegion = atlas.find(name + "-rotor-top-outline");
		}
		customLoad();
	}

	protected void customLoad(){}

	@Override
	public void draw(Unit unit){
		super.draw(unit);
		customDraw(unit);
		Draw.mixcol(Color.white, unit.hitTime);
		for (int i = 0; i < rotors.length; i++){
			Rotor r = rotors[i];
			TextureRegion region = r.bladeRegion;
			float offX = Angles.trnsx(unit.rotation - 90, r.x, r.y);
			float offY = Angles.trnsx(unit.rotation - 90, r.x, r.y);
			float w = region.width * r.scale * Draw.scl;
			float h = region.height * r.scale * Draw.scl;
			for (int j = 0; j < r.bladeCount; j++){
				float angle = (unit.id * 24 + Time.time() * r.speed + (360 / (float) r.bladeCount) * j + r.rotOffset)
					% 360;
				Draw.alpha(state.isPaused() ? 1f : Time.time() % 2);
				Draw.rect(r.bladeOutlineRegion, unit.x + offX, unit.y + offY, w, h, angle);
				Draw.rect(region, unit.x + offX, unit.y + offY, w, h, angle);
			}
			Draw.alpha(1f);
			Draw.rect(r.topOutlineRegion, unit.x + offX, unit.y + offY, w, h, unit.rotation - 90f);
			Draw.rect(r.topRegion, unit.x + offX, unit.y + offY, unit.rotation - 90f);
		}
		Draw.mixcol();
	}

	protected void customDraw(Unit unit){}

	public static class Rotor{
		public TextureRegion bladeRegion, topRegion, bladeOutlineRegion, topOutlineRegion;
		public float x = 0f, y = 0f, scale = 1f;
		public int rotOffset = 0, speed = 29, bladeCount = 4;
	}

	class CopterAI extends FlyingAI{
		@Override
		protected void attack(float attackLength){
			moveTo(target, unit.range() * 0.8f);
			unit.lookAt(target);
		}
	}
}