# ShowCakeYear

Client-side Fabric mod for Hypixel SkyBlock New Year Cakes.  
Shows the cake year on the item, applies texture variants for special year patterns, and supports per-year texture overrides through an in-game command.

## Features

- Displays the cake year on top of cake items in GUI inventory rendering.
- Text rendering:
  - black text
  - white outline
  - narrower horizontal scale for readability
- Texture behavior:
  - `3 + 6n` uses `black.png`
  - `4 + 6n` uses `blue.png`
  - all other years use `default.png`
- Custom per-year overrides with command:
  - `/cakes <year> <color>`
  - `/cakes <year> reset`
  - `/cakes` for tutorial/help
- Override settings are saved to:
  - `config/showcakeyear.json`

## Commands

- `/cakes`
  - Shows command tutorial and available custom colors.
- `/cakes <year>`
  - Shows whether that year currently has an override.
- `/cakes <year> <color>`
  - Applies `assets/showcakeyear/cakes/<color>.png` to that year.
- `/cakes <year> reset`
  - Removes custom override and returns to default pattern logic.

### Reserved color names

These are reserved and cannot be set manually:

- `black`
- `blue`
- `default`

## Adding custom cake textures

Place your PNG files in:

`src/main/resources/assets/showcakeyear/cakes/`

Example:

- `purple.png`
- `gold.png`

Then use:

- `/cakes 69 purple`
- `/cakes 120 gold`