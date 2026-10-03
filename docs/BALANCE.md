# Seed to Cellar: balance sheet (1.0)

Every number a player feels, in one place. Times are in real time (1 in-game day = 20 minutes). Most are settings in
`config/seedtocellar-*.toml` or data that pack makers can change (see [PACK_MAKERS.md](PACK_MAKERS.md)).

## Drinks: effect, alcohol, food

Effect length is shown at three stars; one star halves it, five stars add half again (x0.5 / x1.0 / x1.5; a crowned
whiskey x1.75). Alcohol wears off at 1 unit per 40 seconds.

**The rule (final balance pass):** our own effects (Refreshed, Warmth, Courage) outlast the drink's alcohol, even at one
star. One drink now and then keeps the effect going and never makes you tipsy; drinking faster than that is where
tipsiness comes in. Vanilla effects stay short, because they're strong.

| Drink | Effect | At 3 stars | Alcohol (units) | Hunger |
|---|---|---|---|---|
| Pale, amber, wheat ale; cider, perry | Refreshed | 3 min | 1 | 2 |
| Lager | Refreshed | 3.5 min | 1 | 2 |
| Plain ale | Refreshed | 2 min | 1 | 2 |
| Table beer | Refreshed | 1.5 min | 0.5 | 1 |
| Stout | Warmth | 4 min | 1 | 3 |
| Old ale | Courage | 4 min | 1.5 | 3 |
| White wine | Refreshed | 4 min | 2 | 2 |
| Rosé | Refreshed | 3.5 min | 2 | 2 |
| Fruit wines | Refreshed | 3 min | 2 | 2 |
| Mead | Courage | 4 min | 2 | 3 |
| Red wine, sake | Regeneration I | 8 s | 2 | 2 |
| Glow-berry wine | Night Vision | 3 min | 2 | 2 |
| Mulled wine (no stars) | Warmth | 5 min | 2 | 3 |
| Whiskeys, brandies, Apple Crown | Warmth | 5 min | 3 | 0 |
| Vodka, grappa | Warmth | 4.5 min | 3 | 0 |
| Rum | Courage | 5 min | 3 | 0 |
| Spiced rum | Warmth | 5 min | 3 | 0 |
| Gin | Refreshed | 5 min | 3 | 0 |
| Tequila | Speed I | 40 s | 3 | 0 |
| Fruit liqueurs | Refreshed | 4 min | 2 | 1 |
| Herbal liqueur (cures nausea) | Refreshed | 3 min | 2 | 1 |
| Coffee liqueur | Haste I | 1.5 min | 2 | 1 |
| Aromatic bitters (cures hangover, nausea) | Refreshed | 1.5 min | 1 | 0 |
| Coffee (no stars) | Haste I | 2 min | 0 | 1 |
| Lemonade, elderflower cordial (no stars) | Refreshed | 4 min | 0 | 3 |
| Ginger beer (no stars) | Refreshed | 3 min | 0 | 2 |
| Juices (no stars) | Refreshed | 1.5 min | 0 | 2 |

What the effects do: **Refreshed** gives back a quarter of the hunger you spend; **Warmth** stops freezing (and warms you in
Cold Sweat and Tough As Nails); **Courage** is +20% knockback resistance. A crowned Apple Crown Whiskey adds a golden apple's
Absorption (2 min) and Regeneration II (5 s).

### Tipsiness

| Units | Stage | What happens |
|---|---|---|
| 1-3 | Merry | effects only |
| 4-6 | Tipsy | gentle sway |
| 7-10 | Drunk | stronger sway, stumbles while sprinting, hunger drains a little faster |
| 11+ | Smashed | heavy sway, Slowness I, Mining Fatigue I, hiccups |

Four ales make you tipsy; four spirits smashed. Sobering up after Drunk gives a **Hangover**: mining 30% slower for 2 to 5
minutes (longer after low-star drinks). Cures: water, milk, porridge, sourdough bread, aromatic bitters.

## How long things take

| Step | Time |
|---|---|
| Malting (barley, wheat) | 2 min steeping + 3 min sprouting |
| Kiln | hops 20 s, pale and wheat malt 30 s, coffee 40 s, amber malt and agave 1 min, black malt 2 min |
| Millstone | 1 crank per item |
| Brew Kettle | mash 30 s, boil 30 s, a dish 8-15 s, stirring a liquid 20-30 s |
| Fermenting Vat | beers, ciders, washes: 1 day; wines, mead, sake, lager: 1.5 days; wild yeast x1.5 |
| Preserving Jar | yeast cultures 10 min; pickles, liqueurs, cultures: 1 day; bitters, spiced rum, herbal liqueur, first koji: 2 days |
| Vinegar | 2 days in an open jar (1 day with a Mother of Vinegar) |
| Pot Still | 20 s per bucket in the pot |
| Drying Rack | 15 min (hops 10 min, jerky 1 day); twice as fast in sun, paused in rain |
| Compost Bin | 1 day for 16 leftovers into 4 Compost |
| Cask aging | 1 day = 1 year on the label (only while placed) |

### Aging star (years in an ideal wood; twice as long in any other; nether woods age twice as fast but never give it)

| Old ale, red wine | White wine, cider, perry, mead, fruit wines | Tequila | Bourbon, rum, brandies | Malt whiskey |
|---|---|---|---|---|
| 3 | 2 | 4 | 6 | 8 |

## Yields

| What | Gives |
|---|---|
| Fruit Press | 4 fruit = half a bucket (olives: 4 = a quarter bucket of oil), plus pomace |
| Crushing Tub | per fruit: red grapes 125 mB of must, white grapes 100, everything else 75 |
| Kettle mash | 2 grist a bucket (4 for a strong wort) |
| Pot Still | each run: half the pot comes over as spirit, half stays as stillage |
| A bucket | 4 servings; a vat holds 8 buckets, a cask 16, a keg 4 |
| Fertile Farmland | ~50% faster growth, 50% chance of one extra crop, about 9 harvests per Compost |
| Climate | outside a crop's climate it grows at half speed (glass overhead counts as Warm) |

## Villagers and loot

- **Brewer and Vintner** buy their crops (an emerald for 12 hop cones or apples, 16 grapes, 20 barley or wheat, 10 dried
  hops, 3 honey bottles), and 3-star beer or wine for 2 emeralds, 5-star for 6. They
  sell 3-star drinks for 2-3 emeralds and a 4-star aged one for 8. Any better drink than asked for is accepted.
- **Wandering trader:** warm-climate saplings for 5 emeralds (vanilla's sapling price); 3 sorghum seeds for 1 emerald; 2 agave
  pups, 2 vanilla pods or 3 coffee beans for 2.
- **Chests** (on top of vanilla's loot): village houses 60% for their region's seeds; desert pyramids 60% agave; jungle
  temples 70% vanilla, coffee, ginger; shipwrecks 35-40% and buried treasure 50% rum aged up to 8-12 years; woodland
  mansions 50% old wine and spirits; igloos 50% vodka.

## Changes in the final balance pass (2026-10-02)

Drink effects were 3x longer for beers, wines, liqueurs and soft drinks and about 1.7x for spirits (a pale ale went from
1 minute to 3). Before, most were shorter than it takes the drink's alcohol to wear off, so the only way to keep an effect
going was to get tipsy. Coffee's Haste 45 s to 2 min, tequila's Speed 20 s to 40 s, glow-berry Night Vision 1 to 3 min,
juices 30 s to 90 s. Red wine and sake keep their short Regeneration. Food, timings, trades and loot were checked against
vanilla and left as they are.
