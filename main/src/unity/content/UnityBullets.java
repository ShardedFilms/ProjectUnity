package unity.content;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import unity.*;
import unity.content.effects.*;
import unity.entities.bullet.*;
import unity.entities.bullet.exp.*;
import unity.entities.units.*;
import unity.gen.Expc.*;
import unity.gen.*;
import unity.graphics.*;
import unity.mod.*;

import static mindustry.Vars.*;
import static unity.content.UnityStatusEffects.*;

public class UnityBullets implements ContentList{
    public static BulletType
        laser, shardLaserFrag, shardLaser, frostLaser, branchLaserFrag, branchLaser, distField, fractalLaser, kelvinWaterLaser,
        kelvinSlagLaser, kelvinOilLaser, kelvinCryofluidLaser, kelvinLiquidLaser, celsiusSmoke, kelvinSmoke,
        breakthroughLaser,

        basicMissile, citadelFlame,

        sapLaser, sapArtilleryFrag, continuousSapLaser,

        coalBlaze, pyraBlaze,

        falloutLaser, catastropheLaser, calamityLaser, extinctionLaser,

        plagueMissile,

        gluonWhirl, gluonEnergyBall, singularityBlackHole, singularityEnergyBall,

        orb, shockBeam, currentStroke, shielderBullet, plasmaFragTriangle, plasmaTriangle, surgeBomb,

        pylonLightning, pylonLaser, pylonLaserSmall, monumentRailBullet,

        scarShrapnel, scarMissile,

        kamiBullet1, kamiLaser, kamiVariableLaser, kamiSmallLaser,

        ricochetSmall, ricochetMedium, ricochetBig,

        stopLead, stopMonolite, stopSilicon,

        supernovaLaser,

        ravagerLaser, ravagerArtillery, missileAntiCheat, endLaserSmall, endTentacleSmall,

        laserZap,

        plasmaBullet, phantasmalBullet,

        teleportLightning;

    //only enhanced
    public static BasicBulletType standardDenseLarge, standardHomingLarge, standardIncendiaryLarge, standardThoriumLarge, standardDenseHeavy, standardHomingHeavy, standardIncendiaryHeavy, standardThoriumHeavy, standardDenseMassive, standardHomingMassive,
        standardIncendiaryMassive, standardThoriumMassive, reignBulletWeakened;
    public static ArtilleryBulletType artilleryExplosiveT2;

    @SuppressWarnings("unchecked")
    private <T extends BulletType> T copy(BulletType from, Cons<T> setter){
        T bullet = (T)from.copy();
        setter.get(bullet);
        return bullet;
    }

    @SuppressWarnings("unchecked")
    private <T extends BulletType> T deepCopy(BulletType from, Cons<T> setter){
        T bullet = (T)from.copy();
        if(from.fragBullet != null){
            bullet.fragBullet = deepCopy(bullet.fragBullet, b -> {});
        }
        setter.get(bullet);
        return bullet;
    }

    @Override
    public void load(){
        laser = new ExpLaserBulletType(150f, 30f){{
                damageInc = 7f;
                status = StatusEffects.shocked;
                statusDuration = 3 * 60f;
                hitUnitExpGain = hitBuildingExpGain = 2f;
        }};

        shardLaserFrag = new LaserBoltBulletType(2f, 10f){
            {
                lifetime = 20f;
                pierceCap = 10;
                pierceBuilding = true;
                backColor = Color.white.cpy().lerp(Pal.lancerLaser, 0.1f);
                frontColor = Color.white;
                hitEffect = Fx.hitLancer;
                despawnEffect = Fx.hitLancer;
            }

            @Override
            public void draw(Bullet b){
                Draw.color((Color)b.data);
                Lines.stroke(2f);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), 7f);
                Lines.stroke(1.3f);
                Draw.color(frontColor);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), 4f);
                Draw.reset();
            }
        };

        shardLaser = new ExpLaserBulletType(150f, 30f){
            {
                damageInc = 5f;
                status = StatusEffects.shocked;
                statusDuration = 3 * 60f;
                hitUnitExpGain = hitBuildingExpGain = 2f;
                fragBullet = shardLaserFrag;
                toColor = Pal.sapBullet;
            }

            @Override
            public void hit(Bullet b, float x, float y){
                hitEffect.at(x, y, b.rotation(), hitColor);
                hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
        
                Effect.shake(hitShake, hitShake, b);

                for(var i = 0; i < fragBullets; i++){
                    var len = Mathf.random(1f, 7f);
                    var a = b.rotation() + Mathf.randomSeed(b.id, 360f) + i * 360f/fragBullets;
                    var target = Damage.linecast(b, x, y, b.rotation(), length);
        
                    Object data = getColor(b);
                    b.data = target;
        
                    if(target instanceof Hitboxc hit){
                        fragBullet.create(b.owner, b.team, hit.x() + Angles.trnsx(a, len), hit.y() + Angles.trnsy(a, len), a, -1f, 1f, 1f, data);
        
                    }else if(target instanceof Building tile){
                        if(tile.collide(b)){
                            fragBullet.create(b.owner, b.team, tile.x() + Angles.trnsx(a, len), tile.y() + Angles.trnsy(a, len), a, -1f, 1f, 1f, data);
                        }
                    }else{
                        b.data = new Vec2().trns(b.rotation(), length).add(b.x, b.y);
                    }
                }
            }
        };

        frostLaser = new ExpLaserBulletType(170f, 130f){
            {
                damageInc = 2.5f;
                status = StatusEffects.freezing;
                statusDuration = 3 * 60f;
                hitUnitExpGain = 1.5f;
                hitBuildingExpGain = 2f;
                shootEffect = UnityFx.shootFlake;
                fromColor = Liquids.cryofluid.color;
                toColor = Color.cyan;
            }

            public void freezePos(Bullet b, float x, float y){
                var lvl = getLevel(b);
                float rad = 3.5f;
                if(!Vars.headless) UnityFx.freezeEffect.at(x, y, lvl / rad + 10f, getColor(b));
                if(!Vars.headless) UnitySounds.laserFreeze.at(x, y, 1f, 0.6f);
        
                Damage.status(b.team, x, y, 10f + lvl / rad, status, 60f + lvl * 6f, true, true);
                Damage.status(b.team, x, y, 10f + lvl / rad, UnityStatusEffects.disabled, 2f * lvl, true, true);
            }

            @Override
            public void init(Bullet b){
                super.init(b);

                setDamage(b);

                Healthc target = Damage.linecast(b, b.x, b.y, b.rotation(), getLength(b));
                b.data = target;

                if(target instanceof Hitboxc hit){
                    hit.collision(b, hit.x(), hit.y());
                    b.collision(hit, hit.x(), hit.y());
                    freezePos(b, hit.x(), hit.y());
                    if(b.owner instanceof ExpBuildc exp) exp.incExp(hitUnitExpGain);
                }else if(target instanceof Building tile && tile.collide(b)){
                    tile.collision(b);
                    hit(b, tile.x, tile.y);
                    freezePos(b, tile.x, tile.y);
                    if(b.owner instanceof ExpBuildc exp) exp.incExp(hitBuildingExpGain);
                }else{
                    b.data = new Vec2().trns(b.rotation(), length).add(b.x, b.y);
                }
            }
        };

        branchLaserFrag = new BasicBulletType(3.5f, 15f){
            {
                frontColor = Pal.lancerLaser;
                backColor = Pal.lancerLaser.cpy().mul(0.7f);
                width = height = 2f;
                weaveScale = 0.6f;
                weaveMag = 0.5f;
                homingPower = 0.4f;
                lifetime = 30f;
                shootEffect = Fx.hitLancer;
                hitEffect = despawnEffect = HitFx.branchFragHit;
                pierceCap = 10;
                pierceBuilding = true;
                splashDamageRadius = 4f;
                splashDamage = 4f;
                status = UnityStatusEffects.plasmaed;
                statusDuration = 180f;
            }

            @Override
            public void init(Bullet b){
                b.fdata = (float)b.data;
                b.data = new Trail(6);
            }

            public Color getColor(Bullet b){
                return Tmp.c1.set(Pal.lancerLaser.cpy().lerp(Pal.sapBullet, 0.5f)).lerp(Pal.sapBullet, b.fdata).cpy();
            }

            @Override
            public void draw(Bullet b){
                if(b.data instanceof Trail tr) tr.draw(frontColor, width);
        
                Draw.color(getColor(b));
                Fill.square(b.x, b.y, width, b.rotation() + 45);
                Draw.color();
            }
        
            @Override
            public void update(Bullet b){
                super.update(b);
                if(b.data instanceof Trail tr) tr.update(b.x, b.y);
            }
        
            @Override
            public void hit(Bullet b, float x, float y){
                super.hit(b, x, y);
                if(b.data instanceof Trail tr) tr.clear();
            }
        };

        branchLaser = new ExpLaserBulletType(140f, 20f){
            {
                damageInc = 6f;
                lengthInc = 2f;
                status = StatusEffects.shocked;
                statusDuration = 3 * 60f;
                hitUnitExpGain = hitBuildingExpGain = 1f;
                fragBullet = shardLaserFrag;
                fromColor = Pal.lancerLaser.cpy().lerp(Pal.sapBullet, 0.5f);
                toColor = Pal.sapBullet;
                fragBullets = 3;
                fragBullet = branchLaserFrag;
                maxRange = 150f + 2f * 30f; //Account for range increase
            }

            public void makeFrag(Bullet b, float x, float y, Object color){
                for(int i = 0; i < fragBullets; i++){
                    fragBullet.create(b.owner, b.team, x, y, b.rotation() + Mathf.randomSeed(b.id, 360f) + i * 360f/fragBullets, -1f, 1f, 1f, color);
                }
            }

            @Override
            public void init(Bullet b){
                super.init(b);

                if(b.data instanceof Position point) makeFrag(b, point.getX(), point.getY(), getLevelf(b));
            }

            @Override
            public void hit(Bullet b, float x, float y){
                hitEffect.at(x, y, b.rotation(), hitColor);
                hitSound.at(b);
        
                Effect.shake(hitShake, hitShake, b);
            }
        };

        distField = new DistFieldBulletType(0, -1){{
            centerColor = Pal.lancerLaser.cpy().a(0);
            edgeColor = Pal.place;
            distSplashFx = UnityFx.distSplashFx;
            distStart = UnityFx.distStart;
            distStatus = distort;
            fieldRadius = 85;

            lifetime = 10 * 60;
            collidesTiles = false;
            collides = false;
            collidesAir = false;
            keepVelocity = false;

            bulletSlowMultiplier = 0.94f; //nerfed
            expGain = 0.01f; // 0.6 exp per sec for unit. 0.05 exp per sec for one bullet
            damageLimit = 200f; //too strong damage cannot be slow
            distDamage = 0.1f; //6 damage per sec
        }};

        fractalLaser = new ExpLaserFieldBulletType(170f, 130f){{
            damageInc = 6f;
            lengthInc = 2f;
            hitUnitExpGain = hitBuildingExpGain = 1f;
            fromColor = Pal.lancerLaser.cpy().lerp(Pal.place, 0.5f);
            toColor = Pal.place;
            maxRange = 150f + 2f * 30f; //Account for range increase

            distField = UnityBullets.distField;
            basicFieldRadius = 85;
        }};

        kelvinWaterLaser = new ExpLaserBulletType(170f, 130f){{
            damageInc = 7f;
            status = StatusEffects.wet;
            statusDuration = 3 * 60f;
            knockback = 10f;
            hitUnitExpGain = 1.5f;
            hitBuildingExpGain = 2f;
            fromColor = Liquids.water.color;
            toColor = Color.sky;
        }};

        kelvinSlagLaser = new ExpLaserBulletType(170f, 130f){
            {
                damageInc = 7f;
                status = StatusEffects.burning;
                statusDuration = 3 * 60f;
                hitUnitExpGain = 1.5f;
                hitBuildingExpGain = 2f;
                puddles = 10;
                puddleRange = 4f;
                puddleAmount = 15f;
                puddleLiquid = Liquids.slag;
                fromColor = Liquids.slag.color;
                toColor = Color.orange;
            }

            public void makeLava(float x, float y, Float level){
                for(int i = 0; i < puddles; i++){
                    Tile tile = world.tileWorld(x + Mathf.range(puddleRange), y + Mathf.range(puddleRange));
                    Puddles.deposit(tile, puddleLiquid, puddleAmount + level * 2);
                }
            }

            @Override
            public void init(Bullet b){
                super.init(b);

                if(b.data instanceof Position point) makeLava(point.getX(), point.getY(), getLevelf(b));
            }
        };

        kelvinOilLaser = new ExpLaserBulletType(170f, 130f){
            {

                damageInc = 7f;
                status = StatusEffects.burning;
                statusDuration = 3 * 60f;
                hitUnitExpGain = 1.5f;
                hitBuildingExpGain = 2f;
                puddles = 10;
                puddleRange = 4f;
                puddleAmount = 15f;
                puddleLiquid = Liquids.oil;
                fromColor = Liquids.oil.color;
                toColor = Color.darkGray;
            }

            public void makeLava(float x, float y, Float level){
                for(int i = 0; i < puddles; i++){
                    Tile tile = world.tileWorld(x + Mathf.range(puddleRange), y + Mathf.range(puddleRange));
                    Puddles.deposit(tile, puddleLiquid, puddleAmount + level * 2);
                }
            }

            @Override
            public void init(Bullet b){
                super.init(b);

                if(b.data instanceof Position point) makeLava(point.getX(), point.getY(), getLevelf(b));
            }
        };

        kelvinCryofluidLaser = new ExpLaserBulletType(170f, 130f){
            {
                damageInc = 3f;
                status = StatusEffects.freezing;
                statusDuration = 3 * 60f;
                hitUnitExpGain = 1.5f;
                hitBuildingExpGain = 2f;
                shootEffect = UnityFx.shootFlake;
                fromColor = Liquids.cryofluid.color;
                toColor = Color.cyan;
            }

            public void freezePos(Bullet b, float x, float y){
                var lvl = getLevel(b);
                float rad = 4.5f;
                if(!Vars.headless) UnityFx.freezeEffect.at(x, y, lvl / rad + 10f, getColor(b));
                if(!Vars.headless) UnitySounds.laserFreeze.at(x, y, 1f, 0.6f);
        
                Damage.status(b.team, x, y, 10f + lvl / rad, status, 60f + lvl * 7.5f, true, true);
                Damage.status(b.team, x, y, 10f + lvl / rad, UnityStatusEffects.disabled, 4.5f * lvl, true, true);
            }

            @Override
            public void init(Bullet b){
                super.init(b);

                setDamage(b);

                Healthc target = Damage.linecast(b, b.x, b.y, b.rotation(), getLength(b));
                b.data = target;

                if(target instanceof Hitboxc hit){
                    hit.collision(b, hit.x(), hit.y());
                    b.collision(hit, hit.x(), hit.y());
                    freezePos(b, hit.x(), hit.y());
                    if(b.owner instanceof ExpBuildc exp) exp.incExp(hitUnitExpGain);
                }else if(target instanceof Building tile && tile.collide(b)){
                    tile.collision(b);
                    hit(b, tile.x, tile.y);
                    freezePos(b, tile.x, tile.y);
                    if(b.owner instanceof ExpBuildc exp) exp.incExp(hitBuildingExpGain);
                }else{
                    b.data = new Vec2().trns(b.rotation(), length).add(b.x, b.y);
                }
            }
        };

        //TODO Implement SK's any liquid idea.
        kelvinLiquidLaser = new ExpLaserBulletType(170f, 130f){
            final float damageMultiplier = 150f; //Multiply the liquid's heat capacity
            final float damageMultiplierInc = 10f;

            {
                status = StatusEffects.freezing;
                statusDuration = 3 * 60f;
                hitUnitExpGain = 1.5f;
                hitBuildingExpGain = 2f;
                shootEffect = UnityFx.shootFlake;
                fromColor = Liquids.cryofluid.color;
                toColor = Color.cyan;
            }

            @Override
            public void setDamage(Bullet b){
                Liquid liquid = Liquids.cryofluid;
                if(b.owner instanceof Building build && !build.cheating()) liquid = build.liquids.current();
                float mul = damageMultiplier + damageMultiplierInc * getLevel(b);
                b.damage = liquid.heatCapacity * mul * b.damageMultiplier();
            }

            void freezePos(Bullet b, float x, float y){
                var lvl = getLevel(b);
                float rad = 4.5f;
                if(!Vars.headless) UnityFx.freezeEffect.at(x, y, lvl / rad + 10f, getColor(b));
                if(!Vars.headless) UnitySounds.laserFreeze.at(x, y, 1f, 0.6f);
        
                Damage.status(b.team, x, y, 10f + lvl / rad, status, 60f + lvl * 8f, true, true);
                Damage.status(b.team, x, y, 10f + lvl / rad, UnityStatusEffects.disabled, 3f * lvl, true, true);
            }

            @Override
            public void init(Bullet b){
                super.init(b);

                setDamage(b);

                Healthc target = Damage.linecast(b, b.x, b.y, b.rotation(), getLength(b));
                b.data = target;

                if(target instanceof Hitboxc hit){
                    hit.collision(b, hit.x(), hit.y());
                    b.collision(hit, hit.x(), hit.y());
                    freezePos(b, hit.x(), hit.y());
                    if(b.owner instanceof ExpBuildc exp) exp.incExp(hitUnitExpGain);
                }else if(target instanceof Building tile && tile.collide(b)){
                    tile.collision(b);
                    hit(b, tile.x, tile.y);
                    freezePos(b, tile.x, tile.y);
                    if(b.owner instanceof ExpBuildc exp) exp.incExp(hitBuildingExpGain);
                }else{
                    b.data = new Vec2().trns(b.rotation(), length).add(b.x, b.y);
                }
            }
        };

        breakthroughLaser = new ExpLaserBlastBulletType(500f, 1200f){{
            damageInc = 1000f;
            lengthInc = 150f;
            largeHit = true;
            width = 80f;
            widthInc = 10f;
            lifetime = 65f;
            lightningSpacingInc = -5f;
            lightningDamageInc = 30f;
            hitUnitExpGain = 0.005f;
            hitBuildingExpGain = 0.008f;
            sideLength = 0f;
            sideWidth = 0f;
        }};

        coalBlaze = new ExpBulletType(3.35f, 32f){{
            ammoMultiplier = 3;
            hitSize = 7f;
            lifetime = 24f;
            pierce = true;
            statusDuration = 60 * 4f;
            shootEffect = ShootFx.shootSmallBlaze;
            hitEffect = Fx.hitFlameSmall;
            despawnEffect = Fx.none;
            status = StatusEffects.burning;
            keepVelocity = true;
            hittable = false;
            expGain = 0.5f;
        }};

        pyraBlaze = new ExpBulletType(3.35f, 46f){{
            ammoMultiplier = 3;
            hitSize = 7f;
            lifetime = 24f;
            pierce = true;
            statusDuration = 60 * 4f;
            shootEffect = ShootFx.shootPyraBlaze;
            hitEffect = Fx.hitFlameSmall;
            despawnEffect = Fx.none;
            status = StatusEffects.burning;
            keepVelocity = false;
            hittable = false;
            expGain = 0.6f;
        }};

        basicMissile = new MissileBulletType(4.2f, 15){{
            homingPower = 0.12f;
            width = 8f;
            height = 8f;
            shrinkX = shrinkY = 0f;
            drag = -0.003f;
            homingRange = 80f;
            keepVelocity = false;
            splashDamageRadius = 35f;
            splashDamage = 30f;
            lifetime = 62f;
            trailColor = Pal.missileYellowBack;
            hitEffect = Fx.blastExplosion;
            despawnEffect = Fx.blastExplosion;
            weaveScale = 8f;
            weaveMag = 2f;
        }};

        citadelFlame = new FlameBulletType(4.2f, 50f){{
            lifetime = 20f;
            particleAmount = 17;
        }};

        sapArtilleryFrag = new ArtilleryBulletType(2.3f, 30){{
            hitEffect = Fx.sapExplosion;
            knockback = 0.8f;
            lifetime = 70f;
            width = height = 20f;
            collidesTiles = false;
            splashDamageRadius = 70f;
            splashDamage = 60f;
            backColor = Pal.sapBulletBack;
            frontColor = lightningColor = Pal.sapBullet;
            lightning = 2;
            lightningLength = 5;
            smokeEffect = Fx.shootBigSmoke2;
            hitShake = 5f;
            lightRadius = 30f;
            lightColor = Pal.sap;
            lightOpacity = 0.5f;

            status = StatusEffects.sapped;
            statusDuration = 60f * 10;
        }};

        sapLaser = new LaserBulletType(80f){{
            colors = new Color[]{Pal.sapBulletBack.cpy().a(0.4f), Pal.sapBullet, Color.white};
            length = 150f;
            width = 25f;
            sideLength = sideWidth = 0f;
            shootEffect = ShootFx.sapPlasmaShoot;
            hitColor = lightColor = lightningColor = Pal.sapBullet;
            status = StatusEffects.sapped;
            statusDuration = 80f;
            lightningSpacing = 17f;
            lightningDelay = 0.12f;
            lightningDamage = 15f;
            lightningLength = 4;
            lightningLengthRand = 2;
            lightningAngleRand = 15f;
        }};

        continuousSapLaser = new ContinuousLaserBulletType(60f){
            {
                colors = new Color[]{Pal.sapBulletBack.cpy().a(0.3f), Pal.sapBullet.cpy().a(0.6f), Pal.sapBullet, Color.white};
                length = 190f;
                width = 5f;
                shootEffect = ShootFx.sapPlasmaShoot;
                hitColor = lightColor = lightningColor = Pal.sapBullet;
                hitEffect = HitFx.coloredHitSmall;
                status = StatusEffects.sapped;
                statusDuration = 80f;
                lifetime = 180f;
                incendChance = 0f;
                largeHit = false;
            }

            @Override
            public void hitTile(Bullet b, Building build, float initialHealth, boolean direct){
                super.hitTile(b, build, initialHealth, direct);
                if(b.owner instanceof Healthc owner){
                    owner.heal(Math.max(initialHealth - build.health(), 0f) * 0.2f);
                }
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(entity instanceof Healthc h && b.owner instanceof Healthc owner){
                    owner.heal(Math.max(health - h.health(), 0f) * 0.2f);
                }
            }
        };

        falloutLaser = new SparkingContinuousLaserBulletType(95f){{
            length = 230f;
            fromBlockChance = 0.12f;
            fromBlockDamage = 23f;
            fromLaserAmount = 0;
            incendChance = 0f;
            fromBlockLen = 2;
            fromBlockLenRand = 5;
        }};

        catastropheLaser = new SparkingContinuousLaserBulletType(240f){{
            length = 340f;
            strokes = new float[]{2 * 1.4f, 1.5f * 1.4f, 1 * 1.4f, 0.3f * 1.4f};
            incendSpread = 7f;
            incendAmount = 2;
        }};

        calamityLaser = new SparkingContinuousLaserBulletType(580f){{
            length = 450f;
            strokes = new float[]{2 * 1.7f, 1.5f * 1.7f, 1 * 1.7f, 0.3f * 1.7f};
            lightStroke = 70f;
            spaceMag = 70f;
            fromBlockChance = 0.5f;
            fromBlockDamage = 34f;
            fromLaserChance = 0.8f;
            fromLaserDamage = 32f;
            fromLaserAmount = 3;
            fromLaserLen = 5;
            fromLaserLenRand = 7;
            incendChance = 0.6f;
            incendSpread = 9f;
            incendAmount = 2;
        }};

        extinctionLaser = new SparkingContinuousLaserBulletType(770f){{
            length = 560f;
            strokes = new float[]{2f * 2.2f, 1.5f * 2.2f, 2.2f, 0.3f * 2.2f};
            lightStroke = 90f;
            spaceMag = 70f;
            fromBlockChance = 0.5f;
            fromBlockDamage = 76f;
            fromBlockAmount = 4;
            fromLaserChance = 0.8f;
            fromLaserDamage = 46f;
            fromLaserAmount = 4;
            fromLaserLen = 10;
            fromLaserLenRand = 7;
            incendChance = 0.7f;
            incendSpread = 9f;
            incendAmount = 2;
            extinction = true;
        }};

        plagueMissile = new MissileBulletType(3.8f, 12f){{
            width = height = 8f;
            backColor = hitColor = lightColor = trailColor = UnityPal.plagueDark;
            frontColor = UnityPal.plague;
            shrinkY = 0f;
            drag = -0.01f;
            splashDamage = 30f;
            splashDamageRadius = 35f;
            hitEffect = Fx.blastExplosion;
            despawnEffect = Fx.blastExplosion;
        }};

        gluonWhirl = new GluonWhirlBulletType(4f){{
            lifetime = 5f * 60f;
            hitSize = 12f;
        }};

        gluonEnergyBall = new GluonOrbBulletType(8.6f, 10f){
            {
                lifetime = 50f;
                drag = 0.03f;
                hitSize = 9f;
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);

                gluonWhirl.create(b, b.x, b.y, 0f);
            }
        };

        singularityBlackHole = new SingularityBulletType(26f){{
            lifetime = 3.5f * 60f;
            hitSize = 19f;
        }};

        singularityEnergyBall = new BasicBulletType(6.6f, 7f){
            {
                lifetime = 110f;
                drag = 0.018f;
                pierce = pierceBuilding = true;
                hitSize = 9f;
                despawnEffect = hitEffect = Fx.none;
            }

            @Override
            public void update(Bullet b){
                super.update(b);

                if(Units.closestTarget(b.team, b.x, b.y, 20f) != null){
                    b.remove();
                }

                if(b.timer.get(0, 2f + b.fslope() * 1.5f)){
                    UnityFx.lightHexagonTrail.at(b.x, b.y, 1f + b.fslope() * 4f, backColor);
                }
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);

                singularityBlackHole.create(b, b.x, b.y, 0f);
            }

            @Override
            public void draw(Bullet b){
                Draw.color(Pal.lancerLaser);
                Fill.circle(b.x, b.y, 7f + b.fout() * 1.5f);
                Draw.color(Color.white);
                Fill.circle(b.x, b.y, 5.5f + b.fout());
            }
        };

        orb = new BulletType(){
            {
                lifetime = 240;
                speed = 1.24f;
                damage = 23;
                pierce = true;
                hittable = false;
                hitEffect = HitFx.orbHit;
                trailEffect = UnityFx.orbTrail;
                trailChance = 0.4f;
            }

            @Override
            public void draw(Bullet b){
                Drawf.light(b.x, b.y, 16, Pal.surge, 0.6f);

                Draw.color(Pal.surge);
                Fill.circle(b.x, b.y, 4);

                Draw.color();
                Fill.circle(b.x, b.y, 2.5f);
            }

            @Override
            public void update(Bullet b){
                super.update(b);
                if(b.timer.get(1, 7)) Units.nearbyEnemies(b.team, b.x - 5 * tilesize, b.y - 5 * tilesize, 5 * tilesize * 2, 5 * tilesize * 2, unit -> Lightning.create(b.team, Pal.surge, Mathf.random(17, 33), b.x, b.y, b.angleTo(unit), Mathf.random(7, 13)));
            }

            @Override
            public void drawLight(Bullet b){}
        };

        shockBeam = new BeamBulletType(120f, 35f){{
            status = StatusEffects.shocked;
            statusDuration = 3f * 60f;
            beamWidth = 0.62f;
            hitEffect = Fx.hitLiquid;
            castsLightning = true;
            minLightningDamage = damage / 1.8f;
            maxLightningDamage = damage / 1.2f;
            color = Pal.surge;
        }};

        currentStroke = new LaserBulletType(450){{
            lifetime = 65f;
            width = 20f;
            length = 430f;
            lightningSpacing = 35f;
            lightningLength = 5;
            lightningDelay = 1.1f;
            lightningLengthRand = 15;
            lightningDamage = 50f;
            lightningAngleRand = 40f;
            largeHit = true;
            lightColor = lightningColor = Pal.surge;
            sideAngle = 15f;
            sideWidth = 0f;
            sideLength = 0f;
            colors = new Color[]{Pal.surge.cpy(), Pal.surge, Color.white};
        }};

        shielderBullet = new ShieldBulletType(8){{
            drag = 0.03f;
            shootEffect = Fx.none;
            despawnEffect = Fx.none;
            collides = false;
            hitSize = 0;
            hittable = false;
            hitEffect = Fx.hitLiquid;
            breakSound = Sounds.wave;
            maxRadius = 10f;
            shieldHealth = 3000f;
        }};

        plasmaFragTriangle = new TriangleBulletType(11, 10, 4.5f, 90f){{
            lifetime = 160f;
            lifetimeRand = 40f;
            trailWidth = 3f;
            trailLength = 8;
            drag = 0.05f;
            collides = false;
            castsLightning = true;
            shootEffect = UnityFx.plasmaFragAppear;
            hitEffect = despawnEffect = UnityFx.plasmaFragDisappear;
        }};

        plasmaTriangle = new TriangleBulletType(13, 10, 4f, 380f){{
            lifetime = 180f;
            trailWidth = 3.5f;
            trailLength = 14;
            homingPower = 0.06f;
            hitSound = Sounds.plasmaboom;
            hitEffect = HitFx.plasmaTriangleHit;
            despawnEffect = Fx.none;
            fragBullet = plasmaFragTriangle;
            fragBullets = 8;
        }};

        surgeBomb = new SurgeBulletType(7f, 100f){{
            width = height = 30f;
            maxRange = 30f;
            backColor = Pal.surge;
            frontColor = Color.white;
            mixColorTo = Color.white;
            hitSound = Sounds.plasmaboom;
            despawnShake = 4f;
            collidesAir = false;
            lifetime = 70f;
            despawnEffect = UnityFx.surgeSplash;
            hitEffect = Fx.massiveExplosion;
            keepVelocity = false;
            spin = 2f;
            shrinkX = shrinkY = 0.7f;
            collides = false;
            splashDamage = 680f;
            splashDamageRadius = 120f;

            fragBullets = 8;
            fragLifeMin = 0.8f;
            fragLifeMax = 1.1f;
            scaleVelocity = true;
            fragBullet = plasmaFragTriangle;
            
            shocks = 10;
            shockDamage = 680f / 5f;
            shockLength = 20;
        }};

        pylonLightning = new LightningBulletType(){{
            lightningLength = 32;
            lightningLengthRand = 12;
            damage = 56f;
        }};

        pylonLaser = new LaserBulletType(2000f){
            {
                length = 520f;
                width = 60f;
                lifetime = 72f;
                largeHit = true;
                sideLength = sideWidth = 0f;
                shootEffect = UnityFx.pylonLaserCharge;
            }

            @Override
            public void init(Bullet b){
                super.init(b);

                for(int i = 0; i < 24; i++){
                    Time.run(2f * i, () -> {
                        pylonLightning.create(b, b.x, b.y, b.vel().angle());

                        Sounds.spark.at(b.x, b.y, Mathf.random(0.6f, 0.9f));
                    });
                }
            }
        };

        pylonLaserSmall = new LaserBulletType(192f){{
            length = 180f;
            width = 24f;
        }};

        monumentRailBullet = new PointBulletType(){{
            damage = 6000f;
            buildingDamageMultiplier = 0.8f;
            speed = maxRange = 540f;
            lifetime = 1f;
            hitShake = 6f;
            trailSpacing = 35f;
            shootEffect = ShootFx.monumentShoot;
            despawnEffect = UnityFx.monumentDespawn;
            smokeEffect = shootEffect = Fx.blastExplosion;
            trailEffect = UnityFx.monumentTrail;
        }};

        scarShrapnel = new ShrapnelBulletType(){{
            fromColor = UnityPal.endColor;
            toColor = UnityPal.scarColor;
            damage = 1f;
            length = 110f;
        }};

        scarMissile = new MissileBulletType(6f, 12f){{
            lifetime = 70f;
            speed = 5f;
            width = 7f;
            height = 12f;
            shrinkY = 0f;
            backColor = trailColor = UnityPal.scarColor;
            frontColor = UnityPal.endColor;
            splashDamage = 36f;
            splashDamageRadius = 20f;
            weaveMag = 3f;
            weaveScale = 6f;
            pierceBuilding = true;
            pierceCap = 3;
        }};
        
        celsiusSmoke = new SmokeBulletType(4.7f, 32f){{
            drag = 0.034f;
            lifetime = 18f;
            hitSize = 4f;
            shootEffect = Fx.none;
            smokeEffect = Fx.none;
            hitEffect = HitFx.hitAdvanceFlame;
            despawnEffect = Fx.none;
            collides = true;
            collidesTiles = true;
            collidesAir = true;
            pierce = true;
            statusDuration = 770f;
            status = UnityStatusEffects.blueBurn;
        }};
        
        kelvinSmoke = new SmokeBulletType(4.7f, 16f){{
            drag = 0.016f;
            lifetime = 32f;
            hitSize = 4f;
            shootEffect = Fx.none;
            smokeEffect = Fx.none;
            hitEffect = HitFx.hitAdvanceFlame;
            despawnEffect = Fx.none;
            collides = true;
            collidesTiles = true;
            collidesAir = true;
            pierce = true;
            statusDuration = 770f;
            status = UnityStatusEffects.blueBurn;
        }};

        kamiBullet1 = new CircleBulletType(4f, 7f){{
            lifetime = 240f;
            hitSize = 6f;
            despawnEffect = Fx.none;
            pierce = true;
            keepVelocity = false;
            color = b -> Tmp.c1.set(Color.red).shiftHue(b.time * 3f).cpy();
        }};

        kamiLaser = new KamiLaserBulletType(230f){{
            lifetime = 4f * 60f;
            length = 760f;
            width = 140f;
            fadeInTime = 60f;
            drawSize = (length + (width * 2f)) * 2f;
        }};

        kamiVariableLaser = new KamiAltLaserBulletType(60f);

        kamiSmallLaser = new KamiLaserBulletType(230f){{
            lifetime = 2f * 60f;
            length = 760f;
            width = 20f;
            fadeInTime = 15f;
            curveScl = 3f;
            drawSize = (length + (width * 2f)) * 2f;
        }};

        ricochetSmall = new RicochetBulletType(7f, 80f){{
            width = 9f;
            height = 12f;
            ammoMultiplier = 4;
            lifetime = 30f;
            trailEffect = UnityFx.ricochetTrailSmall;
            frontColor = Color.white;
            backColor = trailColor = Pal.lancerLaser;
        }};

        ricochetMedium = new RicochetBulletType(8.5f, 168f){{
            width = 12f;
            height = 16f;
            ammoMultiplier = 4;
            lifetime = 35f;
            pierceCap = 5;
            trailLength = 7;
            trailEffect = UnityFx.ricochetTrailMedium;
            frontColor = Color.white;
            backColor = trailColor = Pal.lancerLaser;
        }};

        ricochetBig = new RicochetBulletType(10f, 528f){{
            width = 14f;
            height = 18f;
            ammoMultiplier = 4;
            lifetime = 40f;
            pierceCap = 8;
            trailLength = 8;
            trailEffect = UnityFx.ricochetTrailBig;
            frontColor = Color.white;
            backColor = trailColor = Pal.lancerLaser;
        }};

        stopLead = new BasicBulletType(3.6f, 72f, "shell"){{
            width = 9f;
            height = 12f;
            ammoMultiplier = 4;
            lifetime = 60f;
            frontColor = Color.white;
            backColor = Pal.lancerLaser;
            status = StatusEffects.unmoving;
            statusDuration = 5f;
        }};

        stopMonolite = new BasicBulletType(4f, 100f, "shell"){{
            width = 9f;
            height = 12f;
            ammoMultiplier = 4;
            lifetime = 60f;
            frontColor = Color.white;
            backColor = Pal.lancerLaser;
            status = StatusEffects.unmoving;
            statusDuration = 8f;
        }};

        stopSilicon = new BasicBulletType(4f, 72f, "shell"){{
            width = 9f;
            height = 12f;
            ammoMultiplier = 4;
            lifetime = 60f;
            frontColor = Color.white;
            backColor = Pal.lancerLaser;
            status = StatusEffects.unmoving;
            statusDuration = 16f;
            homingPower = 0.08f;
        }};

        supernovaLaser = new ContinuousLaserBulletType(3200f){
            final Effect plasmaEffect;

            {
                length = 280f;
                colors = new Color[]{
                    Color.valueOf("4be3ca55"),
                    Color.valueOf("91eedeaa"),
                    Pal.lancerLaser.cpy(),
                    Color.white
                };

                plasmaEffect = new Effect(36f, e -> {
                    Draw.color(Color.white, Pal.lancerLaser, e.fin());
                    Fill.circle(
                        e.x + Angles.trnsx(e.rotation, e.fin() * 24f),
                        e.y + Angles.trnsy(e.rotation, e.fin() * 24f),
                        e.fout() * 5f
                    );
                });
            }

            @Override
            public void update(Bullet b){
                super.update(b);

                if(b.timer(2, 1f)){
                    float start = Mathf.randomSeed((long)(b.id + Time.time), length);
                    Lightning.create(b.team, Pal.lancerLaser, 12f,
                        b.x + Angles.trnsx(b.rotation(), start),
                        b.y + Angles.trnsy(b.rotation(), start),
                        b.rotation() + Mathf.randomSeedRange((long)(b.id + Time.time + 1f), 15f), Mathf.randomSeed((long)(b.id + Time.time + 2f), 10, 19)
                    );
                }

                for(int i = 0; i < 2; i++){
                    float f = Mathf.random(length * b.fout());
                    plasmaEffect.at(
                        b.x + Angles.trnsx(b.rotation(), f) + Mathf.range(6f),
                        b.y + Angles.trnsy(b.rotation(), f) + Mathf.range(6f),
                        b.rotation() + Mathf.range(85f)
                    );
                }
            }
        };

        ravagerLaser = new PointBlastLaserBulletType(1210f){
            {
                length = 460f;
                width = 26.1f;
                lifetime = 25f;
                widthReduction = 6f;
                auraWidthReduction = 4f;
                damageRadius = 110f;
                auraDamage = 9000f;
                overrideDamage = true;

                laserColors = new Color[]{UnityPal.scarColorAlpha, UnityPal.scarColor, UnityPal.endColor, Color.black};
            }

            @Override
            public void handleBuilding(Bullet b, Building build, float initialHealth){
                float damage = auraDamage / (Math.max(3500f - Math.max(initialHealth - 150000f, 0f), 0f) / 3500f);
                if((damage >= Float.MAX_VALUE || Float.isInfinite(damage))){
                    build.health = 0f;
                    AntiCheat.annihilateEntity(build, false);
                }else{
                    build.damage(damage);
                }
            }

            @Override
            public void handleUnit(Bullet b, Unit unit, float initialHealth){
                float damage = auraDamage / (Math.max(4500f - Math.max(initialHealth - 350000f, 0f), 0f) / 4500f);
                if((damage >= Float.MAX_VALUE || Float.isInfinite(damage)) && !(unit instanceof AntiCheatBase)){
                    unit.health = 0f;
                    AntiCheat.annihilateEntity(unit, false);
                }else{
                    unit.damage(damage);
                }
                if(unit instanceof AntiCheatBase) ((AntiCheatBase)unit).overrideAntiCheatDamage(auraDamage, 1);

                if(unit.health >= initialHealth){
                    Unity.antiCheat.samplerAdd(unit);
                }else{
                    Unity.antiCheat.samplerAdd(unit, true);
                }
            }
        };

        ravagerArtillery = new ArtilleryBulletType(4f, 130f){{
            lifetime = 110f;
            splashDamage = 325f;
            splashDamageRadius = 140f;
            width = height = 21f;
            backColor = lightColor = trailColor = UnityPal.scarColor;
            frontColor = lightningColor = UnityPal.endColor;
            lightning = 5;
            lightningLength = 10;
            lightningLengthRand = 5;

            fragBullets = 7;
            fragLifeMin = 0.9f;
            fragBullet = new AntiCheatBasicBulletType(5.6f, 180f){{
                lifetime = 30f;
                pierce = pierceBuilding = true;
                pierceCap = 5;
                backColor = lightColor = UnityPal.scarColor;
                frontColor = UnityPal.endColor;
                width = height = 16f;
                shrinkY = 0f;
                tolerance = 12000f;
                fade = 40f;
            }};
        }};

        missileAntiCheat = new AntiCheatBasicBulletType(4f, 330f, "missile"){{
            lifetime = 60f;
            width = height = 12f;
            shrinkY = 0f;
            drag = -0.013f;
            tolerance = 12000f;
            fade = 45f;
            splashDamageRadius = 45f;
            splashDamage = 220f;
            homingPower = 0.08f;
            trailChance = 0.2f;
            weaveScale = 6f;
            weaveMag = 1f;
            priority = 2;
            hitEffect = Fx.blastExplosion;
            despawnEffect = Fx.blastExplosion;

            backColor = lightColor = trailColor = UnityPal.scarColor;
            frontColor = UnityPal.endColor;
        }};

        endLaserSmall = new ContinuousLaserBulletType(85f){{
            lifetime = 2f * 60;
            length = 230f;
            for(int i = 0; i < strokes.length; i++){
                strokes[i] *= 0.4f;
            }
            colors = new Color[]{UnityPal.scarColorAlpha, UnityPal.scarColor, UnityPal.endColor, Color.white};
        }

            @Override
            public void hitEntity(Bullet b, Hitboxc other, float initialHealth){
                super.hitEntity(b, other, initialHealth);
                if(other instanceof Unit unit){
                    for(Ability ability : unit.abilities){
                        if(ability instanceof ForceFieldAbility force){
                            if(force.max >= 10000){
                                force.max -= force.max / 35f;
                                unit.shield = Math.min(force.max, unit.shield);
                            }
                            if(force.radius > unit.hitSize * 4f){
                                force.radius -= force.radius / 20f;
                            }
                            if(force.regen > 2700f / 5f) force.regen /= 1.2f;
                            continue;
                        }
                        if(ability instanceof RepairFieldAbility repair){
                            if(repair.amount > unit.maxHealth / 7f) repair.amount *= 0.9f;
                            continue;
                        }
                        if(ability instanceof StatusFieldAbility status){
                            if((status.effect.damage < -unit.maxHealth / 20f || status.effect.reloadMultiplier > 8f) && status.duration > 20f){
                                statusDuration -= statusDuration / 15f;
                            }
                        }
                    }
                    unit.shield -= damage * 0.4f;
                    if(unit.armor > unit.hitSize) unit.armor -= Math.max(damage, unit.armor / 20f);
                }
                if(other instanceof AntiCheatBase) ((AntiCheatBase)other).overrideAntiCheatDamage(damage * 6f, 3);
            }
        };

        endTentacleSmall = new TentacleBulletType(120f){{
            fromColor = Color.red;
            toColor = Color.black;
            length = 290f;
            width = 6f;
            segments = 13;
            lifetime = 35f;
            angleVelocity = 6.5f;
            angleDrag = 0.16f;
        }};

        laserZap = new LaserBulletType(90f){{
            sideAngle = 15f;
            sideWidth = 1.5f;
            sideLength = 60f;
            width = 16f;
            length = 215f;
            shootEffect = Fx.shockwave;
            colors = new Color[]{Pal.lancerLaser.cpy().mul(1f, 1f, 1f, 0.7f), Pal.lancerLaser, Color.white};
        }};

        plasmaBullet = new BasicBulletType(3.5f, 15f){
            {
                frontColor = Pal.lancerLaser.cpy().lerp(Color.white, 0.5f);
                backColor = Pal.lancerLaser.cpy().lerp(Pal.sapBullet, 0.5f).mul(0.7f);
                width = height = 2f;
                weaveScale = 0.6f;
                weaveMag = 0.5f;
                homingPower = 0.4f;
                lifetime = 80f;
                shootEffect = Fx.hitLancer;
                hitEffect = despawnEffect = Fx.hitLancer;
                pierceCap = 10;
                pierceBuilding = true;
                splashDamageRadius = 4f;
                splashDamage = 4f;
                status = UnityStatusEffects.plasmaed;
                statusDuration = 180f;
                inaccuracy = 25f;
            }

            @Override
            public void init(Bullet b){
                b.data = new FixedTrail(9);
            }

            @Override
            public void draw(Bullet b){
                if(b.data instanceof FixedTrail trail){
                    trail.draw(frontColor, width);
                }

                Draw.color(frontColor);
                Fill.square(b.x, b.y, width, b.rotation() + 45f);
                Draw.color();
            }

            @Override
            public void update(Bullet b){
                super.update(b);
                if(b.data instanceof FixedTrail trail){
                    trail.update(b.x, b.y, b.rotation());
                }
            }

            @Override
            public void hit(Bullet b, float x, float y){
                super.hit(b, x, y);
                if(b.data instanceof FixedTrail trail){
                    trail.clear();
                }
            }
        };

        phantasmalBullet = new BasicBulletType(6f, 32f){{
            width = 6f;
            height = 12f;
            shrinkY = 0.3f;
            lifetime = 45f;

            frontColor = Color.white;
            backColor = Pal.lancerLaser;
            shootEffect = Fx.shootSmall;
            smokeEffect = Fx.shootSmallSmoke;
            hitEffect = Fx.flakExplosion;

            lightning = 3;
            lightningColor = Pal.lancerLaser;
            lightningLength = 6;
        }};

        teleportLightning = new LightningBulletType(){{
            damage = 12f;
            shootEffect = Fx.hitLancer;
            smokeEffect = Fx.none;
            despawnEffect = Fx.none;
            hitEffect = Fx.hitLancer;
            keepVelocity = false;
        }};

        //only enhanced

        standardDenseLarge = copy(Bullets.standardDenseBig, (BasicBulletType t) -> {
            t.damage *= 1.4f;
            t.speed *= 1.1f;
            t.width *= 1.12f;
            t.height *= 1.12f;
        });

        standardHomingLarge = copy(Bullets.standardDenseBig, (BasicBulletType t) -> {
            t.damage *= 1.23f;
            t.reloadMultiplier = 1.3f;
            t.homingPower = 0.09f;
            t.speed *= 1.1f;
            t.width *= 1.09f;
            t.height *= 1.09f;
        });

        standardIncendiaryLarge = copy(Bullets.standardIncendiaryBig, (BasicBulletType t) -> {
            t.damage *= 1.4f;
            t.speed *= 1.1f;
            t.width *= 1.12f;
            t.height *= 1.12f;
        });

        standardThoriumLarge = copy(Bullets.standardThoriumBig, (BasicBulletType t) -> {
            t.damage *= 1.4f;
            t.speed *= 1.1f;
            t.width *= 1.12f;
            t.height *= 1.12f;
        });

        standardDenseHeavy = copy(Bullets.standardDenseBig, (BasicBulletType t) -> {
            t.damage *= 1.7f;
            t.speed *= 1.3f;
            t.width *= 1.32f;
            t.height *= 1.32f;
        });

        standardHomingHeavy = copy(Bullets.standardDenseBig, (BasicBulletType t) -> {
            t.damage *= 1.4f;
            t.reloadMultiplier = 1.3f;
            t.homingPower = 0.09f;
            t.speed *= 1.3f;
            t.width *= 1.19f;
            t.height *= 1.19f;
        });

        standardIncendiaryHeavy = copy(Bullets.standardIncendiaryBig, (BasicBulletType t) -> {
            t.damage *= 1.7f;
            t.speed *= 1.3f;
            t.width *= 1.32f;
            t.height *= 1.32f;
        });

        standardThoriumHeavy = copy(Bullets.standardThoriumBig, (BasicBulletType t) -> {
            t.damage *= 1.7f;
            t.speed *= 1.3f;
            t.width *= 1.32f;
            t.height *= 1.32f;
        });

        standardDenseMassive = copy(Bullets.standardDenseBig, (BasicBulletType t) -> {
            t.damage *= 1.8f;
            t.speed *= 1.3f;
            t.width *= 1.34f;
            t.height *= 1.34f;
            t.lifetime *= 1.1f;
        });

        standardHomingMassive = copy(Bullets.standardDenseBig, (BasicBulletType t) -> {
            t.damage *= 1.6f;
            t.reloadMultiplier = 1.3f;
            t.homingPower = 0.09f;
            t.speed *= 1.3f;
            t.width *= 1.21f;
            t.height *= 1.21f;
            t.lifetime *= 1.1f;
        });

        standardIncendiaryMassive = copy(Bullets.standardIncendiaryBig, (BasicBulletType t) -> {
            t.damage *= 1.8f;
            t.speed *= 1.3f;
            t.width *= 1.34f;
            t.height *= 1.34f;
            t.lifetime *= 1.1f;
        });

        standardThoriumMassive = copy(Bullets.standardThoriumBig, (BasicBulletType t) -> {
            t.damage *= 1.8f;
            t.speed *= 1.3f;
            t.width *= 1.34f;
            t.height *= 1.34f;
            t.lifetime *= 1.1f;
        });

        reignBulletWeakened = copy(UnitTypes.reign.weapons.get(0).bullet, (BasicBulletType t) -> {
            t.damage = 45f;
        });

        artilleryExplosiveT2 = copy(Bullets.artilleryExplosive, (ArtilleryBulletType t) -> {
            t.speed = 4.5f;
            t.lifetime = 74f;
            t.ammoMultiplier = 2f;
            t.splashDamageRadius *= 1.3f;
            t.splashDamage *= 3f;
        });

        //endregion
    }
}
