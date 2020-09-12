const falloutLaser = extendContent(ContinuousLaserBulletType, 85, {
	update(b){
		this.super$update(b);
		
		if(Mathf.chanceDelta(0.12)){
			Lightning.create(b.team, Color.valueOf("ff9c5a"), 23, b.x, b.y, b.rotation(), Mathf.round((this.length / 8) + Mathf.random(2, 7)));
		}
	}
});
falloutLaser.length = 230;

//TODO: research from umbrium item.
const fallout = extendContent(LaserTurret, "fallout", {
	load(){
		this.super$load();
		
		this.baseRegion = Core.atlas.find("unity-block-" + this.size);
	}
});
fallout.shootType = falloutLaser;
fallout.consumes.add(new ConsumeLiquidFilter(liquid => liquid.temperature <= 0.5 && liquid.flammability < 0.1, 0.58)).update(false);