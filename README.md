# KetoS (Proof of concept)

Status: In Progress

Application made for people on keto/low carb diet. 
Main purpose is to make shopping faster by reducing time of checking macronutrients.

Idea is simple - open the app, scan barcode and check results of scanned product.

The app is intended for use only on small devices. (Rather, nobody goes shopping with a tablet)

Application main features
- search by barcode and analyze product macronutrients under circumstances of given low carb profile
- history of latest analyzed products
- favourite products list

<!--- TODO screens -->

## Architecture

The architecture is built around [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/).

Logic has been set up in ViewModels, any external components used by them like repositories, apis etc. have been implemented behind interfaces so we received clean and testable logic functions. 

Any IO or time consuming tasks are handled with kotlin coroutines. 

Repository layer is used for handling data operations. Data comes from a few different sources:
- user preferences and settings are stored locally (DataStore)
- product data is stored remotely and is fetched and stored in memory (Retrofit)
- analyze history and favourite products are stored in SQLite database (Room)

Navigation component to simplify into a single Activity app.

Project is quite small so I have decided to use Koin as a light dependency injection library.

<!--- TODO Espresso for basic instrumentation tests and JUnit and Mockito for unit testing. -->
JUnit and Mockito with a little bit of Truth help for unit testing.

### Other tools

- image loading (Glide)
- JSON parsing (Moshi)
- barcode scanning (CameraX + Google MLKit)
 
## Api
 
https://world.openfoodfacts.org/data
 
Product data are fetched from OpenFoodFacts Api under [Open Database License](https://opendatacommons.org/licenses/odbl/1-0/).

Unfortunately that api doesn't provide us complete set of data in most cases, so POC has not been satisfied.