# Seed to Cellar for pack makers

Every Seed to Cellar recipe is plain JSON in a recipe type of our own, so datapacks, **KubeJS** and **CraftTweaker** can
add, change or remove them without any special support. Our own recipes live in `data/seedtocellar/recipes/<type>/`.

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

```zenscript
// CraftTweaker: any of our types takes a JSON recipe (here, oats roll into two)
<recipetype:seedtocellar:milling>.removeByName("seedtocellar:milling/rolled_oats");
<recipetype:seedtocellar:milling>.addJsonRecipe("rolled_oats", {
  ingredient: {tag: "forge:crops/oats"}, result: {item: "seedtocellar:rolled_oats", count: 2}, cranks: 1
});
```

Ingredients use vanilla's format (`{"item": ...}` or `{"tag": ...}`); liquids are a `fluid` or a fluid `tag` with an
`amount` in millibuckets (250 is one bottle, 1000 a bucket). Times are in ticks (20 a second; 24000 is one in-game day)
before the server config's time multipliers.

## Recipe types

### `seedtocellar:malting` (Malting Tub)
Grain steeps in water, then sprouts.
```json
{"type":"seedtocellar:malting","ingredient":{"tag":"forge:crops/barley"},"result":{"item":"seedtocellar:green_barley_malt"},
 "steep_time":2400,"sprout_time":3600}
```

### `seedtocellar:kilning` (Kiln)
`roast` is the Kiln's setting: `light`, `medium` or `dark`.
```json
{"type":"seedtocellar:kilning","ingredient":{"item":"seedtocellar:green_barley_malt"},"roast":"medium",
 "result":{"item":"seedtocellar:amber_malt"},"time":1200}
```

### `seedtocellar:milling` (Millstone)
`cranks` turns of the stone; `byproduct` (optional) comes out beside it.
```json
{"type":"seedtocellar:milling","ingredient":{"tag":"forge:crops/rice"},"result":{"item":"seedtocellar:polished_rice"},
 "byproduct":{"item":"seedtocellar:rice_bran"},"cranks":1}
```

### `seedtocellar:drying` (Drying Rack)
```json
{"type":"seedtocellar:drying","ingredient":{"tag":"forge:fruits/red_grape"},"result":{"item":"seedtocellar:raisins"},"time":18000}
```

### `seedtocellar:crushing` (Crushing Tub)
One item, stomped `stomps` times, gives `amount` of a liquid.
```json
{"type":"seedtocellar:crushing","ingredient":{"tag":"forge:fruits/red_grape"},
 "result":{"fluid":"seedtocellar:red_grape_must","amount":125},"stomps":2}
```

### `seedtocellar:pressing` (Fruit Press)
`count` of the input, `cranks` turns; `byproduct` (optional) is left in the press.
```json
{"type":"seedtocellar:pressing","ingredient":{"tag":"forge:fruits/apple"},"count":4,
 "result":{"fluid":"seedtocellar:apple_juice","amount":500},"byproduct":{"item":"seedtocellar:fruit_pomace"},"cranks":4}
```

### `seedtocellar:cooking` (Brew Kettle, over heat)
Up to 4 ingredients, an optional liquid from the tank, an optional container (bowl or bottle) per serving.
```json
{"type":"seedtocellar:cooking","ingredients":[{"tag":"forge:crops/tomato"},{"tag":"forge:crops/onion"}],
 "fluid":{"tag":"minecraft:water","amount":250},"container":{"item":"minecraft:bowl"},
 "result":{"item":"seedtocellar:tomato_soup"},"time":200}
```

### `seedtocellar:mixing` (Brew Kettle, over heat)
Stirs `per_bucket` of the ingredient into every bucket of the liquid, turning all of it into `result` (a fluid). With
no `ingredient` (and no `per_bucket`), the liquid is boiled down alone.
```json
{"type":"seedtocellar:mixing","liquid":{"tag":"minecraft:water","amount":250},"ingredient":{"item":"minecraft:honey_bottle"},
 "per_bucket":2,"result":"seedtocellar:honey_water","time":400}
```
Mashing grist into wort and boiling it with hops are built into the kettle (they record the malt bill), not recipes.

### `seedtocellar:fermenting` (Fermenting Vat)
What a liquid becomes. Checked from the highest `priority` down; the first match wins, so a low-priority catch-all means
nothing is ever wasted. `yeast`: `ale`, `wine` or `lager`; `allow_wild`: whether no yeast at all also works (slower, a
star short). `temperature`: the one that earns the star (`cold`, `cool`, `mild`, `warm`), or a list of them. Beers also
read the wort's malt bill: `malt_min` / `malt_max` are shares (0 to 1) of `pale`, `amber`, `black`, `wheat`, `corn`,
`potato`; `min_strength` / `max_strength` are `light`, `normal` or `strong`.
```json
{"type":"seedtocellar:fermenting","input":"seedtocellar:hopped_wort","result":"seedtocellar:stout","yeast":"ale",
 "allow_wild":true,"temperature":"mild","min_strength":"normal","max_strength":"strong","malt_min":{"black":0.25},
 "malt_max":{},"time":24000,"priority":50}
```

### `seedtocellar:distilling` (Pot Still)
What one run turns the pot into (each run halves it and counts a run on the spirit). Checked from the highest
`priority` down. No `result` means the same spirit, distilled once more. `filter`: needs (and uses) one charcoal in the
still's filter slot. `min_runs`: only for a spirit already run this often. `basket`: the Gin Basket's botanicals, the
`required` one plus others, `min` different in all.
```json
{"type":"seedtocellar:distilling","input":{"fluid":"seedtocellar:vodka"},"result":"seedtocellar:gin",
 "basket":{"required":{"tag":"forge:crops/juniper"},"min":3},"priority":20}
```

### `seedtocellar:jar` (Preserving Jar, lid closed)
Exactly these ingredients (list one twice for two), and optionally a liquid (`fluid` or `fluid_tag`, at least
`fluid_amount`). Makes an item `result`, or with `result_fluid` steeps the whole liquid into another, keeping its quality
(liqueurs keep their spirit's stars). `temperature` (optional): where it has to stand, as a list.
```json
{"type":"seedtocellar:jar","fluid_tag":"seedtocellar:liqueur_bases","fluid_amount":250,
 "ingredients":[{"item":"seedtocellar:lemon_peel"},{"item":"seedtocellar:lemon_peel"},{"tag":"seedtocellar:sweeteners"}],
 "result_fluid":"seedtocellar:limoncello","time":24000}
```

## Drink data (for scripts that give or check drinks)

A drink's liquid and its bottle, mug or bucket carry the same NBT:
- `Brew`: the quality checks, `{Yeast:1b, Temperature:1b, Craft:1b, Aged:1b}`. Stars are 1 plus each one passed.
- `Age` (years), `Wood` (the cask wood's id, e.g. `oak`) and `Charred` (1b): the cask that gave it the most years.
- `Runs` and `Filtered`: a spirit's trips through the still. `Botanicals`: how many a gin was run through.
- `Wort`: a wort's malt bill, `{Malts:{pale:0.5f, amber:0.5f}, Strength:"NORMAL"}`.

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
We also join the common `forge:` tags (`forge:crops/*`, `forge:seeds/*`, `forge:fruits/*`, `forge:grain/*`,
`forge:flour/*`, `forge:storage_blocks/*`...), so other mods' crops work in our recipes. The exceptions are our bales,
sacks, crates and thatch, which pack only our own items: other mods pack theirs into their own blocks, and two recipes
for the same crafting grid would fight (a GameTest checks this against Farmer's Delight, Quark and Supplementaries).

## Other mods

All optional; nothing is required besides Forge. With them installed:
- **Create**: our Millstone recipes run on its Millstone and Crushing Wheels; our press and tub recipes on its
  Mechanical Press over a Basin; our kettle mixing on its heated Mixer, which also mashes five malt bills (pale,
  amber, dark, strong amber, wheat) into wort and hops it. Spouts and Drains fill and empty our bottles and mugs.
- **Mekanism** and **Immersive Engineering**: their Crushers grind what our Millstone does; IE's Squeezer presses what
  our Fruit Press and tub do, and its Garden Cloche grows every crop.
- **Botany Pots**: every crop, hops, vanilla and all eight fruit trees.
- **Farmer's Delight**: Cooking Pot and Cutting Board recipes; its Stove heats our kettle and still; Rich Soil counts
  as fertile.
- **Serene Seasons**: crops have seasons; winter makes a vat a step cooler, summer a step warmer, except in its tropical
  biomes (tag `sereneseasons:tropical_biomes`), which have wet and dry seasons instead. Config `seasonsChangeTemperature`.
- **Tough As Nails**: our drinks quench thirst; warming drinks warm you. **Cold Sweat**: Warmth gives off body heat.
- **Jade** and **The One Probe**: station progress, temperature, expected stars, cask age and wood, crop growth.
- **JEI**: a category for every station. **EMI** shows them through its JEI support when JEI is also installed.
- **Patchouli**: *The Brewer's Almanac* (craft a book with barley seeds).

The recipes for other mods' machines are generated with ours, in `data/seedtocellar/recipes/<mod>/`, each wrapped in a
`forge:mod_loaded` condition.
