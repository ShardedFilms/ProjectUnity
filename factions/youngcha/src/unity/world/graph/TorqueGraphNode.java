package unity.world.graph;

import arc.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.ui.*;

public class TorqueGraphNode extends GraphNode<TorqueGraph>{
    public float baseFriction, baseInertia, baseForce = 0;
    public float maxTorque, maxSpeed;
    public boolean torqueProvider = false;

    public boolean torqueConsumer = false;

    public TorqueGraphNode(float friction, float inertia, GraphBuild build){
        super(build);
        baseFriction = friction;
        baseInertia = inertia;
    }

    public TorqueGraphNode(float friction, float inertia, float maxSpeed, GraphBuild build){
        super(build);
        baseFriction = friction;
        baseInertia = inertia;
        this.maxSpeed = maxSpeed;
    }

    public TorqueGraphNode(float friction, float inertia, float maxTorque, float maxSpeed, GraphBuild build){
        super(build);
        torqueProvider = true;
        baseFriction = friction;
        baseInertia = inertia;
        this.maxTorque = maxTorque;
        this.maxSpeed = maxSpeed;
    }

    public TorqueGraphNode(GraphBuild build){
        this(0.1f, 10f, build);
    }

    public float getSpeedRatio(float maxSpeed){
        if(!connector.any()){return 0;}
        return 1f - (connector.first().graph.lastVelocity / maxSpeed);
    }

    @Override
    public void displayBars(Table table){
        var n1 = connector.first();
        table.row();
        table.add(new Bar(() -> Core.bundle.format("bar.unity-torquespeed", Strings.fixed(n1.graph.lastVelocity * 10f, 1)), () -> Pal.ammo, () -> Mathf.clamp(n1.graph.lastVelocity / Math.max(0.01f, maxSpeed))));
    }

    public float getFriction(){
        return baseFriction;
    }

    public float getInertia(){
        return baseInertia;
    }

    public float getForce(){
        if(torqueProvider){
            return baseForce * maxTorque * getSpeedRatio(maxSpeed);
        }else{
            return baseForce;
        }
    }

    @Override
    public void displayStats(Table table){
        addBundleStatLevelLine(table, "stat.unity-friction", baseFriction * (60.0f), new float[]{0.5f, 1, 2, 5, 10});
        addBundleStatLine(table, "stat.unity-inertia", baseInertia);
        if(maxSpeed > 0){
            addBundleStatLine(table, "stat.unity-maxspeed", maxSpeed * 10);
        }
        if(maxTorque > 0){
            addBundleStatLine(table, "stat.unity-maxtorque", maxTorque);
        }

    }
}
