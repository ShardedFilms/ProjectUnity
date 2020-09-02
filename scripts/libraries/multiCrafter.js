var multi = extendContent(GenericCrafter, "multi",
{
    tmpRecs: [
    { //1  you can skip recipe properties
        input:
        {

        },
        output:
        {
            power: 5.25
        },
        craftTime: 12
    },
    { //2
        input:
        {
            items: ["coal/1", "sand/1"],
            liquids: ["water/5"],
            power: 1
        },
        output:
        {
            liquids: ["slag/5"],
        },
        craftTime: 60
    },
    { //3
        input:
        {
            items: ["pyratite/1", "blast-compound/1"],
            liquids: ["water/5"],
            power: 1
        },
        output:
        {
            items: ["scrap/1", "plastanium/2", "spore-pod/2"],
            liquids: ["oil/5"],
        },
        craftTime: 72
    },
    { //4
        input:
        {
            items: ["sand/1"],
        },
        output:
        {
            items: ["silicon/1"],
        },
        craftTime: 30
    },
    { //5
        input:
        {
            items: ["sand/1", "lead/2"],
            liquids: ["water/5"],
        },
        output:
        {
            items: ["unity-contagium/1"],
        },
        craftTime: 12
    },
    { //6
        input:
        {
            items: ["coal/1", "sand/1"],
            liquids: ["water/5"],
            power: 1
        },
        output:
        {
            items: ["thorium/1", "surge-alloy/2"],
            liquids: ["slag/5"],
        },
        craftTime: 60
    },
    { //7
        input:
        {
            items: ["pyratite/1", "blast-compound/1"],
            liquids: ["water/5"],
            power: 1
        },
        output:
        {
            items: ["scrap/1", "plastanium/2", "spore-pod/2"],
            liquids: ["oil/5"],
        },
        craftTime: 72
    },
    { //8
        input:
        {
            items: ["sand/1"],
        },
        output:
        {
            items: ["silicon/1"],
        },
        craftTime: 30
    },
    { //9
        input:
        {
            items: ["sand/1", "lead/2"],
            liquids: ["water/5"],
        },
        output:
        {
            items: ["unity-contagium/1"],
        },
        craftTime: 12
    },
    { //10
        input:
        {
            items: ["coal/1", "sand/1"],
            liquids: ["water/5"],
            power: 1
        },
        output:
        {
            items: ["thorium/1", "surge-alloy/2"],
            liquids: ["slag/5", "oil/5"],
        },
        craftTime: 60
    }],
    recs: [],
    getRecipes()
    {
        return this.recs;
    },
    _liquidSet: new ObjectSet(),
    getLiquidSet()
    {
        return this._liquidSet;
    },
    hasOutputItem: false,
    _inputItemSet: new ObjectSet(),
    getInputItemSet()
    {
        return this._inputItemSet;
    },
    _inputLiquidSet: new ObjectSet(),
    getInputLiquidSet()
    {
        return this._inputLiquidSet;
    },
    _outputItemSet: new ObjectSet(),
    getOutputItemSet()
    {
        return this._outputItemSet;
    },
    _outputLiquidSet: new ObjectSet(),
    getOutputLiquidSet()
    {
        return this._outputLiquidSet;
    },
    doDumpToggle()
    {
        return this.dumpToggle;
    },
    powerBarI: false,
    powerBarO: false,
    init()
    {
        for (var i = 0; i < this.tmpRecs.length; i++)
        {
            var tmp = this.tmpRecs[i];
            var isInputExist = tmp.input != null,
                isOutputExist = tmp.output != null;
            var tmpInput = tmp.input;
            var tmpOutput = tmp.output;
            if (isInputExist && tmpInput.power > 0) this.powerBarI = true;
            if (isOutputExist && tmpOutput.power > 0) this.powerBarO = true;
            this.recs[i] = {
                input:
                {
                    items: [],
                    liquids: [],
                    power: isInputExist ? typeof tmpInput.power == "number" ? tmpInput.power : 0 : 0
                },
                output:
                {
                    items: [],
                    liquids: [],
                    power: isOutputExist ? typeof tmpOutput.power == "number" ? tmpOutput.power : 0 : 0
                },
                craftTime: typeof tmp.craftTime == "number" ? tmp.craftTime : 80
            };
            var vc = Vars.content;
            var ci = ContentType.item;
            var cl = ContentType.liquid;
            var realInput = this.recs[i].input;
            var realOutput = this.recs[i].output;
            if (isInputExist)
            {
                if (tmpInput.items != null)
                {
                    for (var j = 0, len = tmpInput.items.length; j < len; j++)
                    {
                        if (typeof tmpInput.items[j] != "string") throw "It is not string at " + j + "th input item in " + i + "th recipe";
                        var words = tmpInput.items[j].split("/");
                        if (words.length != 2) throw "Malform at " + j + "th input item in " + i + "th recipe";
                        var item = vc.getByName(ci, words[0]);
                        if (item == null) throw "Invalid item: " + words[0] + " at " + j + "th input item in " + i + "th recipe";
                        this._inputItemSet.add(item);
                        if (isNaN(words[1])) throw "Invalid amount: " + words[1] + " at " + j + "th input item in " + i + "th recipe";
                        realInput.items[j] = new ItemStack(item, words[1] * 1);
                    }
                }
                if (tmpInput.liquids != null)
                {
                    for (var j = 0, len = tmpInput.liquids.length; j < len; j++)
                    {
                        if (typeof tmpInput.liquids[j] != "string") throw "It is not string at " + j + "th input liquid in " + i + "th recipe";
                        var words = tmpInput.liquids[j].split("/");
                        if (words.length != 2) throw "Malform at " + j + "th input liquid in " + i + "th recipe";
                        var liquid = vc.getByName(cl, words[0]);
                        if (liquid == null) throw "Invalid liquid: " + words[0] + " at " + j + "th input liquid in " + i + "th recipe";
                        this._inputLiquidSet.add(liquid);
                        this._liquidSet.add(liquid);
                        if (isNaN(words[1])) throw "Invalid amount: " + words[1] + " at " + j + "th input liquid in " + i + "th recipe";
                        realInput.liquids[j] = new LiquidStack(liquid, words[1] * 1);
                    }
                }
            }
            if (isOutputExist)
            {
                if (tmpOutput.items != null)
                {
                    for (var j = 0, len = tmpOutput.items.length; j < len; j++)
                    {
                        if (typeof tmpOutput.items[j] != "string") throw "It is not string at " + j + "th output item in " + i + "th recipe";
                        var words = tmpOutput.items[j].split("/");
                        if (words.length != 2) throw "Malform at " + j + "th output item in " + i + "th recipe"
                        var item = vc.getByName(ci, words[0]);
                        if (item == null) throw "Invalid item: " + words[0] + " at " + j + "th output item in " + i + "th recipe";
                        this.outputItemSet.add(item);
                        if (isNaN(words[1])) throw "Invalid amount: " + words[1] + " at " + j + "th output item in " + i + "th recipe";
                        realOutput.items[j] = new ItemStack(item, words[1] * 1);
                    }
                    if (j != 0) this.hasOutputItem = true;
                }
                if (tmpOutput.liquids != null)
                {
                    for (var j = 0, len = tmpOutput.liquids.length; j < len; j++)
                    {
                        if (typeof tmpOutput.liquids[j] != "string") throw "It is not string at " + j + "th output liquid in " + i + "th recipe";
                        var words = tmpOutput.liquids[j].split("/");
                        if (words.length != 2) throw "Malform at " + j + "th output liquid in " + i + "th recipe";
                        var liquid = vc.getByName(cl, words[0]);
                        if (liquid == null) throw "Invalid liquid: " + words[0] + " at " + j + "th output liquid in " + i + "th recipe";
                        this._outputLiquidSet.add(liquid);
                        this._liquidSet.add(liquid);
                        if (isNaN(words[1])) throw "Invalid amount: " + words[1] + " at " + j + "th output liquid in " + i + "th recipe";
                        realOutput.liquids[j] = new LiquidStack(liquid, words[1] * 1);
                    }
                }
            }
        }
        this.super$init();
        this.consumesPower = this.powerBarI;
        this.outputsPower = this.powerBarO;
    },
    setStats()
    {
        this.super$setStats();
        this.stats.remove(BlockStat.powerUse);
        this.stats.remove(BlockStat.productionTime);
        var recLen = this.recs.length;
        //crafTimes
        for (var i = 0; i < recLen; i++)
        {
            var rec = this.recs[i];
            var outputItems = rec.output.items,
                inputItems = rec.input.items;
            var outputLiquids = rec.output.liquids,
                inputLiquids = rec.input.liquids;
            this.stats.add(BlockStat.productionTime, i + 1, StatUnit.none);
            this.stats.add(BlockStat.productionTime, rec.craftTime / 60, StatUnit.seconds);
            this.stats.add(BlockStat.input, i + 1, StatUnit.none);
            //items
            for (var l = 0, len = inputItems.length; l < len; l++) this.stats.add(BlockStat.input, inputItems[l]);
            //liquids
            for (var l = 0, len = inputLiquids.length; l < len; l++) this.stats.add(BlockStat.input, inputLiquids[l].liquid, inputLiquids[l].amount, false);
            this.stats.add(BlockStat.output, i + 1, StatUnit.none);
            //items
            for (var jj = 0, len = outputItems.length; jj < len; jj++) this.stats.add(BlockStat.output, outputItems[jj]);
            //liquids
            for (var jj = 0, len = outputLiquids.length; jj < len; jj++) this.stats.add(BlockStat.output, outputLiquids[jj].liquid, outputLiquids[jj].amount, false);
            if (this.powerBarI)
            {
                this.stats.add(BlockStat.powerUse, i + 1, StatUnit.none);
                if (this.recs[i].input.power > 0) this.stats.add(BlockStat.powerUse, this.recs[i].input.power * 60, StatUnit.powerSecond);
                else this.stats.add(BlockStat.powerUse, 0, StatUnit.powerSecond);
            }
            if (this.powerBarO)
            {
                this.stats.add(BlockStat.basePowerGeneration, i + 1, StatUnit.none);
                if (this.recs[i].output.power > 0) this.stats.add(BlockStat.basePowerGeneration, this.recs[i].output.power * 60, StatUnit.powerSecond);
                else this.stats.add(BlockStat.basePowerGeneration, 0, StatUnit.powerSecond);
            }
        }
    },
    setBars()
    {
        this.super$setBars();
        //initialize
        this.bars.remove("liquid");
        this.bars.remove("items");
        if (!this.powerBarI)
        {
            this.bars.remove("power");
        }
        if (this.powerBarO)
        {
            this.bars.add("poweroutput", func(entity =>
                new Bar(prov(() => Core.bundle.format("bar.poweroutput", entity.getPowerProduction() * 60 * entity.timeScale)), prov(() => Pal.powerBar), floatp(() => typeof entity["getPowerStat"] === "function" ? entity.getPowerStat() : 0))
            ));
        }
        //display every Liquids that can contain
        var i = 0;
        if (!this._liquidSet.isEmpty())
        {
            this._liquidSet.each(cons(k =>
            {
                this.bars.add("liquid" + i, func(entity =>
                    new Bar(prov(() => k.localizedName), prov(() => k.barColor == null ? k.color : k.barColor), floatp(() => entity.liquids.get(k) / this.liquidCapacity))
                ));
                i++;
            }));
        }
    },
    outputsItems()
    {
        return this.hasOutputItem;
    },
});
multi.configurable = true;
multi.hasItems = true;
multi.hasLiquids = true;
multi.hasPower = true;
multi.dumpToggle = true;
multi.size = 3;
multi.entityType = prov(() => extendContent(GenericCrafter.GenericCrafterBuild, multi,
{
    acceptItem(source, item)
    {
        if (typeof this.block["getInputItemSet"] !== "function") return false;
        if (this.items.get(item) >= this.getMaximumAccepted(item)) return false;
        var ret = this.block.getInputItemSet();
        print(ret);
        print(ret.contains(item));
        return ret.contains(item);
    },
    acceptLiquid(source, liquid, amount)
    {
        if (typeof this.block["getInputLiquidSet"] !== "function") return false;
        if (this.liquids.get(liquid) + amount > this.block.liquidCapacity) return false;
        return this.block.getInputLiquidSet().contains(liquid);
    },
    displayConsumption(table)
    {
        if (typeof this.block["getRecipes"] !== "function") return;
        const recs = this.block.getRecipes();
        var z = 0;
        var y = 0;
        var x = 0;
        var recLen = recs.length;
        table.left();
        //input 아이템, 액체 그림 띄우기
        for (var i = 0; i < recLen; i++)
        {
            var items = recs[i].input.items;
            var liquids = recs[i].input.liquids;
            //아이템
            for (var j = 0, len = items.length; j < len; j++)
            {
                (function(that, stack)
                {
                    table.add(new ReqImage(new ItemImage(stack.item.icon(Cicon.medium), stack.amount), boolp(() => that.items != null && that.items.has(stack.item, stack.amount)))).size(8 * 4);
                })(this, items[j]);
            }
            z += len;
            //액체
            for (var l = 0, len = liquids.length; l < len; l++)
            {
                (function(that, stack)
                {
                    table.add(new ReqImage(new ItemImage(stack.liquid.icon(Cicon.medium), stack.amount), boolp(() => that.liquids != null && that.liquids.get(stack.liquid) > stack.amount))).size(8 * 4);
                })(this, liquids[l]);
            }
            z += len;
            //아이템 유뮤 바에서 레시피 구분및 자동 줄바꿈을 위해 정리된 input item 필요.
            if (z == 0)
            {
                table.image(Icon.cancel).size(8 * 4);
                x += 1;
            }
            if (i < recLen - 1)
            {
                var next = recs[i + 1].input;
                y += next.items.length + next.liquids.length;
                x += z;
                if (x + y <= 7 && y != 0)
                {
                    table.image(Icon.pause).size(8 * 4);
                    x += 1;
                }
                else if (x + y <= 6 && y == 0)
                {
                    table.image(Icon.pause).size(8 * 4);
                    x += 1;
                }
                else
                {
                    table.row();
                    x = 0;
                }
            }
            y = 0;
            z = 0;
        }
    },
    getPowerProduction()
    {
        var i = this.getToggle();
        if (i < 0 || typeof this.block["getRecipes"] !== "function") return 0;
        var oPower = this.block.getRecipes()[i].output.power;
        if (oPower > 0 && this.getCond())
        {
            if (this.block.getRecipes()[i].input.power > 0)
            {
                this.setPowerStat(this.efficiency());
                return oPower * this.efficiency();
            }
            else
            {
                this.setPowerStat(1);
                return oPower;
            }
        }
        this.setPowerStat(0);
        return 0;
    },
    getProgressIncreaseA(i, baseTime)
    {
        //when use power
        if (typeof this.block["getRecipes"] !== "function" || this.block.getRecipes()[i].input.power > 0) return this.getProgressIncrease(baseTime);
        else return 1 / baseTime * this.delta();
    },
    checkinput(i)
    {
        const recs = this.block.getRecipes();
        //items
        var items = recs[i].input.items;
        var liquids = recs[i].input.liquids;
        for (var j = 0, len = items.length; j < len; j++)
        {
            if (this.items.get(items[j].item) < items[j].amount) return true;
        }
        //liquids
        for (var j = 0, len = liquids.length; j < len; j++)
        {
            if (this.liquids.get(liquids[j].liquid) < liquids[j].amount) return true;
        }
        return false;
    },
    checkoutput(i)
    {
        const recs = this.block.getRecipes();
        //items
        var items = recs[i].output.items;
        var liquids = recs[i].output.liquids;
        for (var j = 0, len = items.length; j < len; j++)
        {
            if (this.items.get(items[j].item) + items[j].amount > this.itemCapacity) return true;
        }
        //liquids
        for (var j = 0, len = liquids.length; j < len; j++)
        {
            if (this.liquids.get(liquids[j].liquid) + liquids[j].amount > this.liquidCapacity) return true;
        }
        return false;
    },
    checkCond(i)
    {
        if (this.power.status <= 0 && this.block.getRecipes()[i].input.power > 0) return false;
        //check power
        else if (this.checkinput(i)) return false;
        else if (this.checkoutput(i)) return false;
        else return true;
    },
    customCons(i)
    {
        const recs = this.block.getRecipes();
        var excute = this.checkCond(i);
        this.saveCond(excute);
        if (excute)
        {
            //do produce
            if (this.getProgress(i) != 0 && this.getProgress(i) != null)
            {
                this.progress = this.getProgress(i);
                this.saveProgress(i, 0);
            }
            this.progress += this.getProgressIncreaseA(i, recs[i].craftTime);
            this.totalProgress += this.delta();
            this.warmup = Mathf.lerpDelta(this.warmup, 1, 0.02);
            if (Mathf.chance(Time.delta * this.updateEffectChance)) Effects.effect(this.updateEffect, this.x + Mathf.range(this.size * 4), this.y + Mathf.range(this.size * 4));
        }
        else this.warmup = Mathf.lerp(this.warmup, 0, 0.02);
    },
    customProd(i)
    {
        const recs = this.block.getRecipes();
        //consume items
        var inputItems = recs[i].input.items;
        var inputLiquids = recs[i].input.liquids;
        var outputItems = recs[i].output.items;
        var outputLiquids = recs[i].output.liquids;
        var eItems = this.items;
        var eLiquids = this.liquids;
        for (var k = 0, len = inputItems.length; k < len; k++) eItems.remove(inputItems[k]);
        //consume liquids
        for (var j = 0, len = inputLiquids.length; j < len; j++) eLiquids.remove(inputLiquids[j].liquid, inputLiquids[j].amount);
        //produce items
        for (var a = 0, len = outputItems.length; a < len; a++)
        {
            for (var aa = 0, amount = outputItems[a].amount; aa < amount; aa++)
            {
                this.offload(outputItems[a].item);
            }
        }
        //produce liquids
        for (var j = 0, len = outputLiquids.length; j < len; j++)
        {
            this.handleLiquid(this, outputLiquids[j].liquid, outputLiquids[j].amount);
        }
        this.block.craftEffect.at(this.x, this.y);
        this.progress = 0;
    },
    updateTile()
    {
        if (typeof this.block["getRecipes"] !== "function") return;
        const recs = this.block.getRecipes();
        /*if (!this.invFrag.isShown() && Vars.control.input.frag.config.isShown() && Vars.control.input.frag.config.getSelectedTile() == tile)
        {
            this.invFrag.showFor(tile);
        }*/
        var recLen = recs.length;
        var current = this.getToggle();
        //to not rewrite whole update
        if (typeof this["customUpdate"] === "function") this.customUpdate();
        //calls customCons and customProd
        if (current >= 0)
        {
            this.customCons(current);
            if (this.progress >= 1) this.customProd(current);
        }
        var eItems = this.items;
        var eLiquids = this.liquids;
        //dump
        var itemTimer = this.timer.get(this.block.dumpTime);
        if (this.block.doDumpToggle() && current > -1)
        {
            var items = recs[current].output.items;
            var liquids = recs[current].output.liquids;
            if (itemTimer)
            {
                for (var i = 0, len = items.length; i < len; i++)
                {
                    if (eItems.has(items[i].item))
                    {
                        if (this.put(items[i].item))
                        {
                            this.items.remove(items[i].item, 1);
                            break;
                        }
                    }
                }
            }
            for (var i = 0, len = liquids.length; i < len; i++)
            {
                if (eLiquids.get(liquids[i].liquid) > 0.001)
                {
                    this.dumpLiquid(liquids[i].liquid);
                    break;
                }
            }
        }
        else
        {
            //TODO 반복문 줄이기
            if (itemTimer && eItems.total() > 0)
            {
                var itemIter = this.block.getOutputItemSet().iterator();
                while (itemIter.hasNext())
                {
                    var item = itemIter.next();
                    if (eItems.has(item))
                    {
                        this.dump(item);
                        break;
                    }
                }
            }
            if (eLiquids.total() > 0.001)
            {
                var liquidIter = this.block.getOutputLiquidSet().iterator();
                while (liquidIter.hasNext())
                {
                    var liquid = liquidIter.next();
                    if (eLiquids.get(liquid) > 0.001)
                    {
                        this.dumpLiquid(liquid);
                        break;
                    }
                }
            }
        }
    },
    shouldIdleSound()
    {
        return this.getCond();
    },
    shouldConsume()
    {
        return this.getCond() && this.enabled;
    },
    buildConfiguration(table)
    {
        if (typeof this.block["getRecipes"] !== "function") return;
        const recs = this.block.getRecipes();
        /*if (!this.invFrag.isBuilt()) this.invFrag.build(table.getParent());
        if (this.invFrag.isShown())
        {
            this.invFrag.hide();
            Vars.control.input.frag.config.hideConfig();
            return;
        }*/
        var group = new ButtonGroup();
        group.setMinCheckCount(0);
        group.setMaxCheckCount(1);
        var recLen = recs.length;
        for (var i = 0; i < recLen; i++)
        {
            //representative images
            (function(i, that)
            {
                var output = recs[i].output;
                var button = table.button(Tex.whiteui, Styles.clearToggleTransi, 40, run(() => that.configure(button.isChecked() ? i : -1))).group(group).get();
                button.getStyle().imageUp = new TextureRegionDrawable(output.items.length > 0 ? output.items[0].item.icon(Cicon.small) : output.liquids.length > 0 ? output.liquids[0].liquid.icon(Cicon.small) : output.power > 0 ? Icon.power : Icon.cancel);
                button.update(run(() => button.setChecked(that.getToggle() == i)));
            })(i, this);
        }
        table.row();
        //other images
        var lengths = [];
        var max = 0;
        for (var l = 0; l < recLen; l++)
        {
            var output = recs[l].output;
            var outputItemLen = output.items.length;
            var outputLiquidLen = output.liquids.length;
            if (lengths[l] == null) lengths[l] = [0, 0, 0];
            if (outputItemLen > 0) lengths[l][0] = outputItemLen - 1;
            if (outputLiquidLen > 0)
            {
                if (outputItemLen > 0) lengths[l][1] = outputLiquidLen;
                else lengths[l][1] = outputLiquidLen - 1;
            }
            if (output.power > 0) lengths[l][2] = 1;
        }
        for (var i = 0; i < recLen; i++)
        {
            max = max < lengths[i][0] + lengths[i][1] + lengths[i][2] ? lengths[i][0] + lengths[i][1] + lengths[i][2] : max;
        }
        for (var i = 0; i < max; i++)
        {
            for (var j = 0; j < recLen; j++)
            {
                var output = recs[j].output;
                var outputItemLen = output.items.length;
                var outputLiquidLen = output.liquids.length;
                if (lengths[j][0] > 0)
                {
                    table.image(output.items[outputItemLen - lengths[j][0]].item.icon(Cicon.small));
                    lengths[j][0]--;
                }
                else if (lengths[j][1] > 0)
                {
                    table.image(output.liquids[outputLiquidLen - lengths[j][1]].liquid.icon(Cicon.small));
                    lengths[j][1]--;
                }
                else if (lengths[j][2] > 0)
                {
                    if (output.items[0] != null || output.liquids[0] != null)
                    {
                        table.image(Icon.power);
                    }
                    else table.image(Tex.clear);
                    lengths[j][2]--;
                }
                else
                {
                    table.image(Tex.clear);
                }
            }
            table.row();
        }
    },
    configured(player, value)
    {
        if (this.getToggle() >= 0) this.saveProgress(this.getToggle(), this.progress);
        if (value == -1) this.saveCond(false);
        this.progress = 0;
        this.setToggle(value);
    },
    setToggle(a)
    {
        this._toggle = a;
    },
    getToggle()
    {
        return this._toggle;
    },
    _toggle: 0,
    saveProgress(c, d)
    {
        this._progressArr[c] = d;
    },
    getProgress(e)
    {
        return this._progressArr[e];
    },
    _progressArr: [],
    saveCond(f)
    {
        this._cond = f;
    },
    getCond()
    {
        return this._cond;
    },
    _cond: false,
    setPowerStat(g)
    {
        this._powerStat = g;
    },
    getPowerStat()
    {
        return this._powerStat;
    },
    _powerStat: 0,
    config()
    {
        return this._toggle;
    },
    write(write)
    {
        this.super$write(write);
        write.s(this._toggle);
    },
    read(read, revision)
    {
        this.super$read(read, revision);
        this._toggle = read.s();
    }
}));
multi.consumes.add(extend(ConsumePower,
{
    requestedPower(entity)
    {
        if (typeof entity["getToggle"] !== "function") return 0;
        var i = entity.getToggle();
        if (i < 0) return 0;
        var input = entity.block.getRecipes()[i].input.power;
        if (input > 0 && entity.getCond()) return input;
        return 0;
    }
}))
multi.buildVisibility = BuildVisibility.sandboxOnly;
multi.category = Category.crafting;