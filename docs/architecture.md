# KetoScanner architecture

How KetoScanner is structured: modules, layers, and the pattern every screen follows.
It is adapted from a general Android architecture guide. The screen pattern (section 5) is kept almost unchanged.
Everything else is reshaped for this project's constraints:

- **Kotlin Multiplatform (KMP) readiness.** The app is Android-only today, but code is written so that every module
  outside the Android-only zones (section 9) can move to `commonMain` without rewrites.
- **Koin** for dependency injection (KMP-capable), not Hilt.
- **Compose-only UI** with Navigation Compose. No fragments in new code.
- **A small app with one developer.** Fewer modules and less ceremony than a large team needs.

`Foo` and `feature/foo` are placeholders. Library versions are not fixed here; the analyst checks the current stable
version (and its KMP support) when planning.

> **Status:** target architecture. Code that hasn't been rewritten yet (fragments, ViewBinding, LiveData) follows the
> old conventions in `CLAUDE.md` until its screen is migrated. Section 10 maps the current modules to the target.

---

## 1. KMP rules (apply to everything)

Code outside the Android-only zones must compile for `commonMain` once the module is converted. In practice:

- No `android.*`, `androidx.*` Android-only APIs, or `java.*` APIs where a Kotlin equivalent exists:
  - time: `kotlin.time.Instant` / `kotlinx-datetime`, never `java.util.Date` or `Calendar`;
  - `kotlin.uuid.Uuid`, never `java.util.UUID`;
  - `String.format` is JVM-only, so number/date formatting goes through a formatter in `core:common` (section 5, Resources).
- Only KMP libraries in shared code. Currently KMP-capable: Koin, kotlinx.coroutines, kotlinx.serialization,
  kotlinx.collections.immutable, androidx lifecycle ViewModel, Room (2.7+), DataStore (1.1+), Compose Multiplatform,
  Navigation Compose, Coil 3, Ktor client.
- A non-KMP library is allowed only inside a data module or an Android-only zone, behind an interface, so that it can
  become an Android `actual` later. Plans must flag every non-KMP dependency they add.
- No `@Parcelize`, no `Serializable` from `java.io`. Use `@Serializable` (kotlinx) where something has to be encoded.

---

## 2. Modules

### Feature modules

A feature lives under `feature/<name>/` and owns only the modules it needs:

| Module | Holds | Visibility |
|---|---|---|
| `api` | Contracts and models: use case and repository interfaces, `data class` models in `api/model/`. No implementations, no IO | public |
| `domain` | Business rules and use case implementations. Pure Kotlin: no IO, no Android, no `core:network`/`core:database`/`core:datastore` | `internal` (only the Koin module is public) |
| `data` | IO-backed implementations of `api` contracts: repositories, API clients, persistence facade, DTOs, mappers (Room entities and DAOs stay in `core:database`, which owns the `@Database`) | `internal` (only the Koin module is public) |
| `ui` | Compose screens, ViewModels, state classes, state factory, screen strings, the feature's navigation entry | `internal` (only the nav entry and Koin module are public) |

- **Start small.** A new feature starts as `ui` (plus `data` if it loads or saves anything). Add `domain` only when
  the feature has business rules beyond loading and saving, and split out `api` only when another feature needs its
  contracts. Splitting later is cheap because packages already follow the same names.
- `domain` keeps the meaning it already has in this project: pure business rules such as `DietAnalysisEngine`. It is
  the code that moves to `commonMain` first. (The original guide used `domain` for all implementations, IO included;
  here that is split into `domain` and `data`.)
- No per-feature shared component module. Composables shared between features go to `core:designsystem`.

### Dependency direction

- `ui` → `api`. `domain` → `api`. `data` → `api`.
- `domain` and `data` don't depend on each other. A use case that needs data (e.g. `AnalyzeProduct`: load the product,
  then run the rules) lives in `domain` and reaches the data through a repository interface in `api`, which `data`
  implements.
- `ui` never depends on `domain` or `data`.
- A feature depends on another feature's `api` only, never its `domain`, `data` or `ui`.
- `app` is the composition root: the only module that sees every `domain`, `data` and `ui`. It starts Koin and builds
  the NavHost.

### Core modules

| Module | Holds | KMP |
|---|---|---|
| `core:model` | Shared vocabulary: `@JvmInline value class` ids (`Barcode`, `ProductId`), enums, constants | common |
| `core:common` | Pure Kotlin utilities: `AppDispatchers`, `Outcome`/error types, formatters | common |
| `core:designsystem` | Theme, tokens, `Ui.*` components, shared composables, `UiText` resolution | common (Compose MP) |
| `core:network` | HTTP client setup. Retrofit/OkHttp/Moshi now, Ktor + kotlinx.serialization later | swap target |
| `core:database` | Room database, entities, DAOs, migrations | common (Room KMP) |
| `core:datastore` | DataStore setup and `FooPersistenceManager`s | common |
| `core:testing` | Fakes, test dispatchers, fixtures | common |

- Value classes instead of the guide's typealiases: a `typealias Barcode = String` accepts any string, a value class
  doesn't, and costs nothing at runtime.
- `core:network`, `core:database` and `core:datastore` are consumed only by `data` modules.

---

## 3. Contracts (`api`)

- A use case is a single-method interface:

  ```kotlin
  interface AnalyzeProduct {

      /**
       * Returns null when the product exists but lacks the nutrients needed for analysis.
       */
      suspend operator fun invoke(barcode: Barcode): ProductAnalysis?
  }
  ```

- A broader capability is one interface with several methods: `suspend fun` for actions,
  `fun observe(): Flow<...>` for streams.
- **An interface is for a boundary, not for every class.** Use one when another module consumes it, when the
  implementation is IO or platform-specific, or when there are real alternative implementations. A pure class
  used only inside its own module (a rules engine, a mapper) stays a concrete class; test it directly.
- Models are plain `data class` declarations in `api/model/`, using `core:model` value classes for ids.
- Models never cross navigation. Routes carry ids; the destination loads the model (section 7).
- KDoc sits on the interface, never repeated on the implementation.

---

## 4. Implementations (`domain` and `data`)

- Interface `Foo` is implemented by `FooImpl`: `internal`, constructor-injected (Koin), no service locator calls inside.
- **Threading is the implementation's job.** `data` runs IO in `withContext(dispatchers.io)`; `domain` runs heavy
  computation, if any, in `withContext(dispatchers.default)`. Both use the injected `AppDispatchers` from
  `core:common`, never `kotlinx.coroutines.Dispatchers` directly.

### `domain`
- Use case implementations and business rules. Rules are plain classes or functions (e.g. `DietAnalysisEngine`), tested
  directly, with no interface unless another module needs one (section 3).
- Gets data only through repository interfaces from `api`. Never imports a DTO, entity, DAO or HTTP type.
- Pure Kotlin under the KMP rules (section 1), so it can become `commonMain` with no code changes.

### `data`
- **Mapping** lives in lowercase files of top-level extension functions (`entityMapper.kt`, `dtoMapper.kt`) exposing
  `toDomain()`, `toEntity()`, `toRequest()`. List variants delegate:

  ```kotlin
  internal fun List<FooEntity>.toDomain(): List<Foo> = map(FooEntity::toDomain)
  ```

- **Storage** goes through the feature's own `FooPersistence` interface (`FooPersistenceImpl`), so the rest of the
  feature never touches a DAO or DataStore directly.
- **Network**: API interfaces are `FooApi`, one suspend method per endpoint. DTOs are named by direction:
  `FooResponseDto`, `FooRequestDto`. **DTOs and entities never leave `data`.** Repositories return `api` models, so
  swapping Retrofit/Moshi for Ktor/kotlinx.serialization changes `data` and `core:network` only.
- Enums decoded from responses get a fallback value for unknown entries, so a new backend value can't fail the
  whole response.
- **Room**: entities and DAOs in `core:database`. Schema changes ship with a `Migration`, never
  `fallbackToDestructiveMigration()`. Schemas are exported and committed.

### Koin module per module

Each `domain`, `data` and `ui` module exposes exactly one Koin module. Use the DSL with constructor references; it
needs no code generation and works the same on every KMP target:

```kotlin
// feature/product/data
val productDataModule = module {
    singleOf(::ProductRepositoryImpl) bind ProductRepository::class
}

// feature/analysis/domain
val analysisDomainModule = module {
    factoryOf(::DietAnalysisEngine)
    factoryOf(::AnalyzeProductImpl) bind AnalyzeProduct::class
}
```

| Guide (Hilt) | Here (Koin) | Use for |
|---|---|---|
| `@Singleton` | `single` / `singleOf` | stores, repositories, clients |
| `@Reusable` | `factory` / `factoryOf` | stateless helpers, use cases |
| `@ViewModelScoped` | `factoryOf`, injected into the ViewModel's constructor | state factories, screen strings |
| `@HiltViewModel` | `viewModelOf(::FooViewModel)` | ViewModels |
| application scope | `single<CoroutineScope>(named("app"))` | work that must outlive a screen |

---

## 5. Screen pattern

Every screen has the same set of files:

```
FooScreen.kt                FooViewModel.kt          FooStrings.kt
event/FooAction.kt          event/FooEvent.kt
state/FooStatePrototype.kt  state/FooState.kt        state/FooStateFactory.kt
```

Data flows in one direction:

```
FooAction ──▶ FooViewModel ──▶ FooStatePrototype ──▶ FooStateFactory ──▶ FooState ──▶ FooScreen
 (view)        (logic)          (logic-shaped)         (pure mapping)     (render-shaped)  (renders)
                   │
                   └──▶ FooEvent (navigation, one-off effects) ──▶ FooScreen
```

### Prototype: logic-shaped state

- Holds domain models and raw flags, plus a `companion object` with a `Default` value.
- `@Immutable data class`.
- May carry small pure helpers, such as building request parameters.
- Never formats anything for display.
- A screen whose logic state *is* its render state (e.g. a static about screen) may skip the prototype; the
  factory then maps from whatever the ViewModel holds. Say so in the plan.

### State: render-shaped state

- A top-level `@Immutable data class` for screen-wide fields.
- An `@Immutable sealed interface` for content variants, such as `InitialLoad`, `Empty`, `ShowItems`, `Error`.
- Lists are `PersistentList` (kotlinx.collections.immutable).
- **Every text is a `UiText`** (see Resources below), already decided by the factory. The view never sees a domain
  type, a raw number to format, or a nullable it has to interpret.
- Item states get an empty `companion object`, so preview `example(...)` extensions can be declared on them.

### StateFactory: the only place that decides what is shown

- `internal class FooStateFactory(private val strings: FooStrings)`, registered with `factoryOf`.
- Exposes `fun createFor(prototype: FooStatePrototype): FooState`. Pure and synchronous: no IO, no coroutines.
- Internals are private extension functions on the prototype. Each branches in a `when { }` from the most specific
  case to the fallback.
- It is the most-tested class of every screen: every branch gets a test (section 8).

### ViewModel

- `internal class FooViewModel(...) : ViewModel()` (androidx lifecycle, KMP), registered with `viewModelOf`.
- Holds the prototype and derives the state from it:

  ```kotlin
  private val statePrototype = MutableStateFlow(Prototype.Default)

  val state = statePrototype.map(stateFactory::createFor).stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = stateFactory.createFor(Prototype.Default)
  )
  ```

- Every user interaction enters through one function, including lifecycle signals:

  ```kotlin
  fun onAction(action: FooAction) {
      when (action) {
          OnStart -> refresh()
          OnBackClicked -> sendEvent(NavigateBack)
          OnRetryClicked -> load()
      }
  }
  ```

- Also standard:
  - a progress flag in the prototype;
  - an event channel for `FooEvent` (below);
  - one error handler that only logs, and one that logs and shows a message.
- Collectors live in private `observeX()` functions, started from an `init` block at the bottom of the class.
- **Per-visit work** runs on `FooAction.OnStart`, sent by the screen's `LifecycleStartEffect`. No
  `DefaultLifecycleObserver` registrations.
- **Process death**: values that must survive it (a query, a selected tab) go through `SavedStateHandle`. Navigation
  arguments are read from it with `savedStateHandle.toRoute<FooRoute>()`.
- Work that must outlive the screen uses the injected application scope.

### Action and Event

- `FooAction`: a `sealed interface` of `data object` and `data class` members, view → ViewModel. Named `OnXClicked`,
  `OnRefresh`, `OnStart`.
- `FooEvent`: ViewModel → view, for navigation and one-off effects (snackbar, open settings). Named imperatively:
  `NavigateBack`, `NavigateToProduct(barcode)`, `ShowMessage(text)`.
- Events go through a `Channel<FooEvent>(Channel.BUFFERED)` exposed as `receiveAsFlow()`. The screen collects it on
  `Dispatchers.Main.immediate` (the `ObserveEvents` helper in `core:designsystem` does this), so an event sent while
  the screen is stopped is delivered on return instead of being lost.

### Screen

- `internal fun FooScreen(onNavigation: (FooNavigation) -> Unit)` does the following:
  1. resolves the ViewModel with `koinViewModel()`;
  2. sends `OnStart` from `LifecycleStartEffect`;
  3. collects state (`collectAsStateWithLifecycle`) and events (`ObserveEvents`);
  4. delegates to `private fun ScreenContent(state, onAction)`.
- `FooNavigation` is the subset of `FooEvent` that leaves the feature (a nested sealed interface). The screen passes
  it up; the app's NavHost maps it to `navController` calls. Features never touch `navController`.
- All composables below `FooScreen` are private and take `state` and `onAction` only.
- They make no decisions: they branch on the sealed content state and render. No formatting, no domain types,
  no business conditionals.

### Resources: `UiText` and `FooStrings`

The guide injected Android `Resources` into a formatter class. That is Android-only, and an injected application
`Resources` doesn't follow an in-app language change. Instead:

- `UiText` (in `core:designsystem`) is a small sealed type: a string resource with arguments, a plural, or a raw
  dynamic value (a product name from the API).
- `FooStrings` (`internal`, `factoryOf`) has one method per piece of dynamic text and returns `UiText`:

  ```kotlin
  internal class ProductStrings(private val numbers: NumberFormatter) {
      fun carbsPer100g(grams: Double): UiText =
          UiText.Res(Res.string.product_carbs_per_100g, numbers.grams(grams))
  }
  ```

- Composables resolve it with `text.resolve()`, which uses Compose Multiplatform resources (`stringResource`).
  Resolution happens at render time, so language changes apply immediately.
- Only the state factory and `FooStrings` build dynamic text. Static labels in composables use `stringResource(...)`
  directly.
- Factory tests assert on `UiText` values, so they need no resources and run on the JVM or in `commonTest`.

### Previews

- Private `@Preview` functions at the bottom of the screen file, one per content variant.
- Each calls `ScreenContent` with hand-built state and `example(...)` helpers.

---

## 6. Design system (`core:designsystem`)

- Everything visual lives here. Feature modules consume it and add nothing of their own.
- One theme root publishes token groups through CompositionLocals: colors, typography, shapes, spacing, sizes, borders.
- Feature code reads semantic tokens only (`AppTheme.colors.text.default`, `AppTheme.spacing.m`). Never raw
  `Color(0x...)` or `.dp` literals.
- Reusable components are extension functions on a marker object, called as `Ui.Toolbar(...)`, `Ui.TextButton(...)`.
- Images load through Coil 3 (KMP), wrapped in a `Ui.ProductImage(...)` component.

---

## 7. Navigation

- Navigation Compose with type-safe routes: each destination is a `@Serializable` `data object` or `data class`
  carrying ids only (`ProductRoute(barcode: String)`).
- Each `ui` module exposes one public `NavGraphBuilder.fooDestination(onNavigation)` extension; routes stay internal
  to the feature except where another feature needs to navigate to it (then the route lives in its `api`).
- The NavHost lives in `app`. The bottom navigation is a `core:designsystem` component driven by the NavHost.

---

## 8. Errors and testing

### Errors
- `data` maps failures (network, HTTP status, parsing, database) to a sealed `AppError` from `core:common` and returns
  `Outcome<T>` (success or `AppError`). `domain` passes these through and adds its own rule outcomes (e.g. "not enough
  nutrient data"). Exceptions don't cross into `ui`, except `CancellationException`, which is always rethrown.
- The state factory turns an `AppError` into an `Error` content variant or a message `UiText`. Nothing else decides
  how an error looks.

### Tests
- One `FooTest` per subject, names like `` `when <condition> then <expectation>` ``, `runTest`, fixtures in a private
  companion object.
- **New tests are KMP-ready:** `kotlin.test` assertions and hand-written fakes from `core:testing`. Mockito and Truth
  are JVM-only; existing tests keep them, but new code that is meant for `commonMain` is tested without them.
- Priority order: state factory (every branch), ViewModel (action → prototype → state, events), `domain` rules (every
  branch) and use cases with fake repositories, then `data` (mappers, repositories with fake persistence and API).
- Dispatchers in tests come from `core:testing` (`TestAppDispatchers` backed by `StandardTestDispatcher`).

---

## 9. Android-only zones

These stay Android-specific and sit behind an interface, so a KMP move only needs a new implementation:

| Zone | Interface | Android implementation |
|---|---|---|
| Barcode scanning (CameraX + ML Kit) | `BarcodeScanner` in `feature/scanner/api` (a `Flow<Barcode>`), plus a `CameraPreview` composable slot | `feature/scanner/data` |
| Camera permission | `PermissionState` in `core:designsystem` | Activity Result API wrapper |
| App entry | none | `MainActivity`, `Application` (start Koin, set content) |
| Debug tooling | none | LeakCanary, debug-only |

---

## 10. Cross-cutting conventions

- `internal` by default in `domain`, `data`, `ui` and `app`. Only `api` types, Koin modules and nav entries are public.
- Constructor injection everywhere. `KoinComponent`/`inject()` only where the framework owns the constructor
  (`Application`, `Activity`).
- Nested sealed members are imported individually, so `when` branches read as bare names.
- The prototype is imported as an alias: `import ...FooStatePrototype as Prototype`.
- Comments explain decisions. They never restate the code.

---

## 11. From today's code to this

| Today | Target | Notes |
|---|---|---|
| `domain` (pure Kotlin, `DietAnalysisEngine`, models, repository interfaces) | split: models/interfaces → `feature/*/api` + `core:model`; engine and analysis rules → `feature/analysis/domain` | `ProductAnalysisHistoryEntry` and `FavouriteProduct` use `java.util.Date` → `kotlin.time.Instant` |
| `openfoodapi` (Retrofit, Moshi codegen, OkHttp) | `core:network` + `feature/product/data` | Already behind interfaces; later swap to Ktor + kotlinx.serialization |
| `barcodescanner` (CameraX controller + `MlKitAnalyzer`) | `feature/scanner/data` (Android-only) | Expose results as `Flow<Barcode>` |
| `app` fragments + ViewBinding + LiveData | `feature/*/ui` Compose screens | Screen by screen; `.claude/work/viewmodel-stateflow/plan.md` is input, its event approach is replaced by section 5 |
| `app` Room database | `core:database` | Add real migrations, drop `fallbackToDestructiveMigration()` |
| `app` DataStore preferences | `core:datastore` + `feature/preferences/data` | Already on DataStore |
| Glide | Coil 3 | KMP |
| Navigation (fragments, safe-args) | Navigation Compose, `@Serializable` routes | |

Suggested order:
1. `core:common`, `core:model`, `core:testing`, `core:designsystem` (theme and first components).
2. Move today's `domain` module into `api` + `domain` shapes; replace `java.util.Date`.
3. One screen end to end (History is the smallest) to prove the pattern, Compose hosted in the existing activity.
4. Remaining screens, then Navigation Compose replaces fragment navigation.
5. Coil, Room migrations, then the network swap.
