const copterAI = () => extend(FlyingAI, {
    attack(circleLength){
        this.moveTo(this.target, this.unit.range() * 0.8);
        this.unit.lookAt(this.target);
    }
});

module.exports = {
    extend(entity, name, obj, obju){
        if(typeof(obj) === "undefined") obj = {};
        if(typeof(obju) === "undefined") obju = {};

        obj = Object.assign({
            rotor: [],

            fallRotateSpeed: 2.5
        }, obj, {
            load(){
                this.super$load();

                obj.rotor.forEach(r => {
                    if(typeof(r.rotOffset) === "undefined") r.rotOffset = 0;

                    if(typeof(r.speed) === "undefined") r.speed = 29;

                    if(typeof(r.x) === "undefined") r.x = 0;
                    if(typeof(r.y) === "undefined") r.y = 0;

                    if(typeof(r.scale) === "undefined") r.scale = 1;

                    if(typeof(r.bladeCount) === "undefined") r.bladeCount = 4;

                    r.bladeRegion = Core.atlas.find(this.name + "-rotor-blade");
                    r.topRegion = Core.atlas.find(this.name + "-rotor-top");
                    
                    r.bladeOutlineRegion = Core.atlas.find(this.name + "-rotor-blade-outline");
                    r.topOutlineRegion = Core.atlas.find(this.name + "-rotor-top-outline");
                });

                if(typeof(obj.customLoad) === "function") this.customLoad();
            },

            draw(unit){
                this.super$draw(unit);

                if(typeof(obj.customDraw) === "function") this.customDraw(unit);

                Draw.mixcol(Color.white, unit.hitTime / unit.hitDuration);

                for(var i = 0; i < obj.rotor.length; i++){
                    var r = obj.rotor[i];

                    var region = r.bladeRegion;
                    var topRegion = r.topRegion;

                    var outline = r.bladeOutlineRegion;
                    var outlineTop = r.topOutlineRegion;

                    var offx = Angles.trnsx(unit.rotation - 90, r.x, r.y);
                    var offy = Angles.trnsy(unit.rotation - 90, r.x, r.y);

                    var w = region.width * r.scale * Draw.scl;
                    var h = region.height * r.scale * Draw.scl;

                    for(var j = 0; j < r.bladeCount; j++){
                        var angle = ((unit.id * 24) + (Time.time() * r.speed) + ((360 / r.bladeCount) * j) + r.rotOffset) % 360;

                        Draw.alpha(Vars.state.isPaused() ? 1 : Time.time() % 2);

                        Draw.rect(outline, unit.x + offx, unit.y + offy, w, h, angle);
                        Draw.rect(region, unit.x + offx, unit.y + offy, w, h, angle);
                    };

                    Draw.alpha(1);

                    Draw.rect(outlineTop, unit.x + offx, unit.y + offy, unit.rotation - 90);
                    Draw.rect(topRegion, unit.x + offx, unit.y + offy, unit.rotation - 90);
                };

                Draw.mixcol();
            }
        });

        var copter = extendContent(UnitType, name, obj);
        copter.defaultController = copterAI;

        obju = Object.assign(obju, {
            update(){
                this.super$update();

                if(this.dead){
                    this.rotation += obj.fallRotateSpeed * Mathf.signs[this.id % 2];
                };

                if(typeof(obju.customUpdate) === "function") this.customUpdate();
            }
        });

        copter.constructor = () => {
            var unit = extend(entity, obju);
            return unit;
        };

        return copter;
    }
};
