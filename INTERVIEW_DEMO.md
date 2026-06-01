# DemoApp — гайд для собеседования

Демо-приложение со списком фильмов. Часть кода **намеренно** содержит типичные ошибки: приложение в целом работает, но поведение можно «сломать» скроллом, быстрыми действиями или фоном.

---

## Структура проекта

```
app/src/main/java/com/kochetkov/demoapp/
├── MainActivity.kt                    # Точка входа, контейнер фрагментов
├── domain/
│   ├── model/Movie.kt                 # Модель фильма
│   └── repository/MovieRepository.kt  # Контракт репозитория
├── data/
│   └── repository/FakeMovieRepository.kt  # Фейковые данные (200 фильмов)
└── presentation/
    ├── movies/
    │   ├── MoviesFragment.kt          # Экран списка
    │   ├── MoviesViewModel.kt         # Загрузка и лайки
    │   ├── MoviesContract.kt          # Intent / State
    │   ├── MoviesAdapter.kt           # RecyclerView + «ловушки» в bind
    │   └── banner/
    │       └── PromoBannerDialogFragment.kt  # Промо-диалог
    └── details/
        └── MovieDetailsFragment.kt    # Карточка фильма + рекомендации

app/src/main/java/coroutines/Demo.kt   # Отдельные сниппеты по корутинам (не UI)
```

### Разметка

| Файл | Назначение |
|------|------------|
| `activity_main.xml` | `FragmentContainerView` (`@id/main`) |
| `fragment_movies.xml` | Список, прогресс, ошибка |
| `item_movie_card.xml` | Карточка в списке (постер, мета, лайк, **Открыть**) |
| `fragment_movie_details.xml` | Детали + блок рекомендаций |

### Навигация

1. `MainActivity` → `MoviesFragment` (старт).
2. Кнопка **Открыть** на карточке → `MovieDetailsFragment` (`replace` + `addToBackStack`).
3. Системная кнопка «Назад» возвращает к списку.

---

## Обычный сценарий (без ловушек)

| Действие | Что происходит |
|----------|----------------|
| Запуск | Загрузка ~700 ms, показ списка |
| Скролл | Список из 200 фильмов |
| Лайк | Должен переключать иконку (см. баг #4) |
| **Открыть** | Экран деталей, рекомендации из `Bundle` |
| Тап по тексту ошибки | `Retry` — повторная загрузка |

---

## Намеренные ошибки и демо-кейсы

### 1. Тяжёлые вычисления в `bind` + кеш по позиции

**Где:** `MoviesAdapter.kt` → `MovieViewHolder.bind()`, `calculatePopularity()`, `popularityByPosition`

**Суть:**
- В `bind` вызывается тяжёлый цикл (`repeat(7_000)`) для расчёта `Popularity`.
- Результат кешируется в `popularityByPosition` по **`bindingAdapterPosition`**, а не по `movie.id`.

**Как проявляется:**
- Лаги при скролле.
- После обновления списка / diff на позиции может оказаться другой фильм, но показывается старое значение popularity.

**О чём спрашивать:**
- Почему нельзя считать метрики в `bind`.
- Кеш по position vs по stable id.
- `onBindViewHolder` vs payload / `getItemId`.

**Как чинить (идея):** считать вне UI-потока, кеш по `movie.id`, отмена при recycle.

---

### 2. Анимация без отмены при recycle

**Где:** `MoviesAdapter.kt` → `startPosterPulseAnimation()` в `bind`

**Суть:**
- На каждый `bind` стартует бесконечный `ObjectAnimator` на `posterImage`.
- Нет `cancel()` в `onViewRecycled` / при detach.

**Как проявляется:**
- При скролле накапливаются аниматоры, дёрганье, лишняя нагрузка на UI.

**О чём спрашивать:**
- Жизненный цикл `ViewHolder`.
- Утечки / лишняя работа в RecyclerView.

**Как чинить:** хранить `Animator` в holder, отменять перед новым `bind` и в `onViewRecycled`.

---

### 3. Мутабельный state (`var` + `MutableList`)

**Где:** `MoviesContract.kt` → `MoviesState.Content`

**Суть:**
- `Content` с `var movies: MutableList<Movie>` — состояние можно менять «сбоку», минуя иммутабельные обновления.

**О чём спрашивать:**
- Однонаправленный поток данных.
- Почему UI-state лучше делать immutable (`data class` + `copy`).

---

### 4. `toggleLike` без новой эмиссии StateFlow

**Где:** `MoviesViewModel.kt` → `toggleLike()`

**Суть:**
```kotlin
currentState.movies[movieIndex] = movie.copy(isLiked = !movie.isLiked)
_state.value = currentState  // тот же объект Content
```
`StateFlow` сравнивает новое значение со старым. Если это **тот же экземпляр** `Content`, эмиссии может не быть → UI не обновится (или обновится нестабильно).

**Как проявить:** нажать лайк — иконка может не смениться до перезагрузки списка.

**О чём спрашивать:**
- Как работает `StateFlow` / `MutableStateFlow`.
- Иммутабельные обновления: `_state.value = currentState.copy(movies = newList)`.

---

### 5. Промо-баннер после `onSaveInstanceState` → `commitAllowingStateLoss`

**Где:** `MoviesFragment.kt` → `schedulePromoBanner()`, `showPromoBanner()`

**Суть:**
- Через **3.5 сек** после открытия списка показывается `PromoBannerDialogFragment`.
- Имитируется «поздний callback» (сеть, push, аналитика).
- Если `parentFragmentManager.isStateSaved == true` → `commitAllowingStateLoss()`, иначе `commit()`.

**Как проявить:**
1. Открыть приложение (экран списка).
2. До истечения ~3.5 сек свернуть приложение или повернуть экран.
3. Баннер попытается показаться — сработает ветка state loss (диалог может не восстановиться после process death).

**О чём спрашивать:**
- `onSaveInstanceState`, `isStateSaved`.
- `commit()` vs `commitAllowingStateLoss()` vs отложенная навигация.
- Lifecycle-aware показ UI (`repeatOnLifecycle`, `LifecycleObserver`).

---

### 6. Ловушка `addToBackStack` + `commitNow()` (ручной триггер)

**Где:** `MoviesFragment.kt` → `triggerCommitNowBackStackTrap()`

**Как включить:** **долгий тап** по заголовку **Movie list** на экране списка.

**Суть:**
```kotlin
.addToBackStack("commit_now_trap")
.commitNow()  // IllegalStateException: нельзя с back stack
```

**О чём спрашивать:**
- Разница `commit()` / `commitNow()`.
- Почему `commitNow()` несовместим с `addToBackStack`.

---

## Функциональность без «ловушек» (для контекста)

### Экран деталей

**Где:** `MovieDetailsFragment.kt`, `MoviesFragment.openMovieDetails()`

- Метаданные фильма в `Bundle` (поля по отдельности).
- Рекомендации: `ArrayList<Movie>` через `putSerializable` / `getSerializable`.
- `Movie` implements `Serializable` (на собесе можно обсудить `Parcelable` vs `Serializable`).
- Возможная ошибка - передаем потенциально большое количество данных через Bundle

*** О чем стоит обсудить **
- Что такое Bundle и как он передается.
- Что такое IPC и какие есть механизмы
- Про Binder и AIDL

## Шпаргалка: где что искать

| Тема собеса | Файл |
|-------------|------|
| RecyclerView / bind | `MoviesAdapter.kt` |
| StateFlow / MVI-lite | `MoviesViewModel.kt`, `MoviesContract.kt` |
| Fragment transactions | `MoviesFragment.kt` |
| Bundle / Serializable | `MovieDetailsFragment.kt`, `Movie.kt` |
| DialogFragment | `PromoBannerDialogFragment.kt` |
| Корутины | `coroutines/Demo.kt` |
