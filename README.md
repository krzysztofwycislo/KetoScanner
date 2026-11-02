# KetoScanner

**Scan a product, see if it fits your diet.**

KetoScanner is a shopping assistant for people on a keto or low-carb diet. Point the camera at a barcode and get a
clear verdict, without reading the nutrition label in the shop aisle.

## What it does

- **Scan or type a barcode.** The product is looked up and rated against your diet profile.
- **A verdict with a reason.** Every product gets a clear verdict and the main reason behind it, for example that most
  of its energy comes from carbs.
- **Your diet, your limits.** Choose Strict keto, Keto or Low carb, or set your own daily carb limit.
- **The macros at a glance.** Carbs, sugars, fibre, protein, fat and energy, per 100 g or per serving.
- **History and favourites.** Recent scans are grouped by day and searchable; favourites are grouped by food type.
- **This trip.** While you shop, the products you scanned in the last few hours stay one tap away.

## How products are rated

The rating looks at more than carbs per 100 g. It also weighs how much of the energy comes from carbs, the balance of
fat and protein against carbs, sugars and a realistic serving, all relative to your daily limit. Keto mode favours
fat; low-carb mode is more relaxed.

KetoScanner is a shopping aid, not medical or dietary advice.

## Product data

Product information comes from [Open Food Facts](https://world.openfoodfacts.org), a free, collaborative database,
available under the [Open Database License](https://opendatacommons.org/licenses/odbl/1-0/). Some products have
incomplete data; the app says so instead of guessing.

## Built with

Kotlin and Jetpack Compose on Android, with CameraX and ML Kit for barcode scanning. The core is kept platform-independent
for a future move to Kotlin Multiplatform. Architecture notes for contributors are in [docs/architecture.md](docs/architecture.md).
