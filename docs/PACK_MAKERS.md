# Seed to Cellar for pack makers

Every Seed to Cellar recipe is plain JSON in a recipe type of our own, so datapacks and scripting mods (KubeJS,
CraftTweaker, where they're available for your version) can add, change or remove them without any special support. Our
own recipes live in `data/seedtocellar/recipe/<type>/`.

```js
// KubeJS (server_scripts): a stronger cider
ServerEvents.recipes(event => {
  event.remove({ id: 'seedtocellar:fermenting/cider' })
  event.custom({
    type: 'seedtocellar:fermenting', input: 'seedtocellar:apple_juice', result: 'seedtocellar:cider',
    yeast: 'wine', allow_wild: true, temperature: 'cool', time: 24000, priority: 0
  })
})
```

Ingredients use vanilla's format: an item id (`"minecraft:wheat"`), a tag (`"#c:crops/oats"`) or a list of them. Item
results are `{"id": ..., "count": ...}` (count 1 can be left out). Liquids are a `fluid` or a fluid `tag` with an
`amount` in millibuckets (250 is one bottle, 1000 a bucket). Times are in ticks (20 a second; 24000 is one in-game day)
before the server config's time multipliers. A field shown below with its default value can be left out.

## Recipe types

### `seedtocellar:malting` (Malting Tub)
Grain steeps in water, then sprouts.
```json
{"type":"seedtocellar:malting","ingredient":"#c:crops/barley","result":{"id":"seedtocellar:green_barley_malt"},
 "steep_time":2400,"sprout_time":3600}
```

### `seedtocellar:kilning` (Kiln)
`roast` is the Kiln's setting: `light` (the default), `medium` or `dark`. `time` defaults to 600.
```json
{"type":"seedtocellar:kilning","ingredient":"seedtocellar:green_barley_malt","roast":"medium",
 "result":{"id":"seedtocellar:amber_malt"},"time":1200}
```

### `seedtocellar:milling` (Millstone)
`cranks` turns of the stone (default 1); `byproduct` (optional) comes out beside it.
```json
{"type":"seedtocellar:milling","ingredient":"#c:crops/rice","result":{"id":"seedtocellar:polished_rice"},
 "byproduct":{"id":"seedtocellar:rice_bran"},"cranks":1}
```

### `seedtocellar:drying` (Drying Rack)
`time` defaults to 2400.
```json
{"type":"seedtocellar:drying","ingredient":"#c:foods/fruit/red_grape","result":{"id":"seedtocellar:raisins"},"time":18000}
```

### `seedtocellar:crushing` (Crushing Tub)
One item, stomped `stomps` times (default 2), gives `amount` of a liquid.
```json
{"type":"seedtocellar:crushing","ingredient":"#c:foods/fruit/red_grape",
 "result":{"fluid":"seedtocellar:red_grape_must","amount":125},"stomps":2}
```

### `seedtocellar:pressing` (Fruit Press)
`count` of the input (default 4), `cranks` turns (default 4); `byproduct` (optional) is left in the press.
```json
{"type":"seedtocellar:pressing","ingredient":"#c:foods/fruit/apple","count":4,
 "result":{"fluid":"seedtocellar:apple_juice","amount":500},"byproduct":{"id":"seedtocellar:fruit_pomace"},"cranks":4}
```

### `seedtocellar:cooking` (Brew Kettle, over heat)
Up to 4 ingredients, an optional liquid from the tank (`fluid` or `tag`, with an `amount`: 250 if left out), an optional
container (bowl or bottle) per serving. `time` defaults to 200.
```json
{"type":"seedtocellar:cooking","ingredients":["#c:crops/tomato","#c:crops/onion"],
 "fluid":{"tag":"minecraft:water","amount":250},"container":"minecraft:bowl",
 "result":{"id":"seedtocellar:tomato_soup"},"time":200}
```

### `seedtocellar:mixing` (Brew Kettle, over heat)
Stirs `per_bucket` (default 1) of the ingredient into every bucket of the liquid, turning all of it into `result` (a
fluid). With no `ingredient`, the liquid is boiled down alone. `time` defaults to 400.
```json
{"type":"seedtocellar:mixing","liquid":{"tag":"minecraft:water","amount":250},"ingredient":"minecraft:honey_bottle",
 "per_bucket":2,"result":"seedtocellar:honey_water","time":400}
```
Mashing grist into wort and boiling it with hops are built into the kettle (they record the malt bill), not recipes.

### `seedtocellar:fermenting` (Fermenting Vat)
What a liquid becomes. Checked from the highest `priority` (default 0) down; the first match wins, so a low-priority
catch-all means nothing is ever wasted. `yeast`: `ale` (the default), `wine` or `lager`; `allow_wild` (default true):
whether no yeast at all also works (slower, a star short). `temperature`: the one that earns the star (`cold`, `cool`,
`mild` (the default), `warm`), or a list of them. Beers also read the wort's malt bill: `malt_min` / `malt_max` are shares
(0 to 1) of `pale`, `amber`, `black`, `wheat`, `corn`, `potato`; `min_strength` / `max_strength` are `light`, `normal` or
`strong` (defaults `light` and `strong`). `time` defaults to 24000.
```json
{"type":"seedtocellar:fermenting","input":"seedtocellar:hopped_wort","result":"seedtocellar:stout","yeast":"ale",
 "allow_wild":true,"temperature":"mild","min_strength":"normal","max_strength":"strong","malt_min":{"black":0.25},
 "time":24000,"priority":50}
```

### `seedtocellar:distilling` (Pot Still)
What one run turns the pot into (each run halves it and counts a run on the spirit). Checked from the highest
`priority` down. No `result` means the same spirit, distilled once more. `filter` (default false): needs (and uses) one
charcoal in the still's filter slot. `min_runs`: only for a spirit already run this often. `basket`: the Gin Basket's
botanicals, the `required` one plus others, `min` different in all.
```json
{"type":"seedtocellar:distilling","input":{"fluid":"seedtocellar:vodka"},"result":"seedtocellar:gin",
 "basket":{"required":"#c:crops/juniper","min":3},"priority":20}
```

### `seedtocellar:jar` (Preserving Jar, lid closed)
Exactly these ingredients (list one twice for two), and optionally a liquid (`fluid` or `fluid_tag`, at least
`fluid_amount`). Makes an item `result`, or with `result_fluid` steeps the whole liquid into another, keeping its quality
(liqueurs keep their spirit's stars). `temperature` (optional): where it has to stand, as a list. `time` defaults to 24000.
```json
{"type":"seedtocellar:jar","fluid_tag":"seedtocellar:liqueur_bases","fluid_amount":250,
 "ingredients":["seedtocellar:lemon_peel","seedtocellar:lemon_peel","#seedtocellar:sweeteners"],
 "result_fluid":"seedtocellar:limoncello","time":24000}
```

## Drink data (for scripts, commands and trades that give or check drinks)

A drink's liquid and its bottle, mug or bucket carry the same data components:
- One per quality check passed: `seedtocellar:quality_yeast`, `seedtocellar:quality_temperature`,
  `seedtocellar:quality_craft`, `seedtocellar:quality_aged` (each with no value: `{}`). Stars are 1 plus each one present.
  Being separate components, a trade or advancement can ask for "at least these checks" and accept a better drink.
- `seedtocellar:brew`, a compound with the rest:
  - `Brew: {}`: the drink has been judged at all.
  - `Age` (years), `Wood` (the cask wood's id, e.g. `"oak"`) and `Charred` (`1b`): the cask that gave it the most years.
  - `Runs` and `Filtered`: a spirit's trips through the still. `Botanicals`: how many a gin was run through.
  - `Wort`: a wort's malt bill, `{Malts:{pale:0.5f, amber:0.5f}, Strength:"NORMAL"}`.

For example, a three-star red wine that has rested three years in oak:
```
/give @s seedtocellar:red_wine[seedtocellar:quality_yeast={},seedtocellar:quality_temperature={},seedtocellar:brew={Brew:{},Age:3,Wood:"oak"}]
```

Villager trades are data too: `data/seedtocellar/villager_trade/<profession>/<level>/<name>.json`, drawn on by the trade
sets `data/seedtocellar/trade_set/<profession>/level_<n>.json` through the villager trade tags
`seedtocellar:<profession>/level_<n>`. The Wandering Trader's extras join `#minecraft:wandering_trader/common`.

## Tags you can add to

Items: `seedtocellar:sweeteners`, `botanicals`, `filter_charcoal`, `compostables`, `drinks`, `ales`, `wines`,
`spirits`, `liqueurs`, `shelf_drinks`, `wine_rack_bottles`, `dried_fruits`, `jams`, `jerky_meats`, `dryable_berries`,
`straw`, `boil_hops`, `chilies` (fresh or dried, for cooking), `cooked_rice` (ours and Farmer's Delight's, for rice balls),
`malt/<type>` and `grist/<type>` (pale, amber, black, wheat, corn, potato).
Fluids: `seedtocellar:liqueur_bases`, `spirits`, `grain_spirits`, `vodka_sources`, `grape_wines`, `ales`,
`kettle_liquids`, `sours_to_vinegar`, `fertilizers`, `lemon_juice`.
Blocks: `seedtocellar:heat_sources` (under the kettle and still), `cooling_blocks` (make a vat Cold), `always_fertile`
(soil that counts as Fertile Farmland), `succulent_soil` (where agave grows), `straw_crops`.
Biomes: `seedtocellar:has_wild/<crop>`, where each wild crop and fruit tree generates.
Villager trades: `seedtocellar:vintner/level_<n>` and `seedtocellar:brewer/level_<n>`.
We also join the common `c:` tags (`c:crops/*`, `c:seeds/*`, `c:foods/fruit/*`, `c:foods/vegetable/*`, `c:grains/*`,
`c:flours/*`, `c:foods/dough/*`, `c:foods/bread`, `c:storage_blocks/*`, `c:tools/sickle`...), so other mods' crops
work in our recipes. The exceptions are our bales, sacks, crates and thatch, which pack only our own items: other mods
pack theirs into their own blocks, and two recipes for the same crafting grid would fight (a GameTest checks ours against
every other crafting and cooking recipe loaded).

## Other mods

All optional; nothing is required besides NeoForge. On Minecraft 26.3 now:
- **JEI**: a category for every station, every growing plant, and how to get every item. **EMI** shows them through its
  JEI support when JEI is also installed.
- **Jade**: station progress, temperature, expected stars, cask age and wood, crop growth.
- **Serene Seasons**: crops have seasons; winter makes a vat a step cooler, summer a step warmer, except in its tropical
  biomes (tag `sereneseasons:tropical_biomes`), which have wet and dry seasons instead. Config `seasonsChangeTemperature`.

Coming back as each reaches 26.3 (they were in the 1.20.1 version): The One Probe, Patchouli (*The Brewer's Almanac*),
Farmer's Delight (Cooking Pot and Cutting Board recipes, its Stove heats our kettle and still, Rich Soil counts as
fertile), Create (milling, pressing, mixing and a brewery that mashes and hops wort), Mekanism and Immersive Engineering
(crushers, IE's Squeezer and Garden Cloche), Botany Pots, Tough As Nails (our drinks quench thirst, warming ones warm you;
its tags are already in the mod) and Cold Sweat (Warmth gives off body heat).
