# Заметки: Android-приложение на Jetpack Compose

Приложение для заметок в духе Google Keep: сетка карточек разной высоты, цвета, закрепление, поиск и отмена удаления. Написано на Kotlin и Jetpack Compose по схеме MVVM.

## Возможности
- Сетка заметок в две колонки с карточками разной высоты (`LazyVerticalStaggeredGrid`)
- Поиск по заголовку и тексту без учёта регистра, кнопка очистки поиска
- Закрепление звёздочкой в списке и в редакторе: закреплённые заметки всегда сверху
- Остальные заметки отсортированы по времени изменения
- 6 цветов карточек, редактор окрашивается в цвет заметки
- Удаление из редактора со снекбаром «Вернуть»
- Пустая новая заметка не сохраняется, а если стереть весь текст у существующей, она удаляется
- Время изменения обновляется только при реальных правках
- Дата последнего изменения в редакторе
- Сохранение по кнопке «Назад» в шапке и системному жесту назад
- Текст в редакторе переживает поворот экрана
- При первом запуске показываются три приветственные заметки
- Заглушки для пустого списка и пустого результата поиска

## Стек
Kotlin 2.0, Jetpack Compose (BOM 2024.09), Material 3, AndroidX ViewModel, Kotlin Flow (`StateFlow`), SharedPreferences + `org.json` для хранения. minSdk 26, targetSdk 34, Android Gradle Plugin 8.5, Gradle 8.7, JDK 17.

## Как запустить
Через Android Studio (Koala или новее):
1. Откройте папку `android-notes-app` как проект.
2. Дождитесь синхронизации Gradle: нужная версия Gradle скачается по `gradle/wrapper/gradle-wrapper.properties`.
3. Запустите на эмуляторе или телефоне кнопкой Run.

Из терминала (нужны установленные Gradle и Android SDK). Скрипта `gradlew` в репозитории нет, его нужно один раз сгенерировать:
```bash
gradle wrapper --gradle-version 8.7
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug   # установка на подключённое устройство
```

## Структура проекта
```
app/src/main/java/com/whysafe/notes/
├── MainActivity.kt          # точка входа, edge-to-edge, тема
├── data/
│   ├── Note.kt              # модель заметки и сериализация в JSON
│   └── NoteRepository.kt    # хранение в SharedPreferences, StateFlow со списком
└── ui/
    ├── NotesViewModel.kt    # поиск, сортировка, сохранение, удаление и отмена
    ├── NotesScreen.kt       # список, поиск, карточки, снекбар
    ├── EditNoteScreen.kt    # редактор: заголовок, текст, цвет, закрепление
    └── theme/Theme.kt       # цвета заметок и цветовая схема Material 3
app/src/main/res/            # строки, тема, иконка
```

## Как это устроено
- MVVM: `NoteRepository` хранит список в `MutableStateFlow` и отдаёт его как `StateFlow`, `NotesViewModel` через `combine` объединяет его со строкой поиска, экраны только отображают состояние через `collectAsStateWithLifecycle`.
- Заметки сериализуются в JSON-массив и пишутся в SharedPreferences через `apply()`, то есть асинхронно; репозиторий можно заменить на Room без изменений в UI.
- Навигация без библиотеки: открытая заметка хранится в `editing` во ViewModel, `null` означает список.
- Поля редактора хранятся в `rememberSaveable` с ключом `note.id`, поэтому ввод не теряется при повороте.
- В `save()` три сценария: удалить опустевшую, пропустить пустую новую, сохранить изменённую с новым `updatedAt`; сравнение идёт без учёта времени.
- Для release включены R8 и ProGuard (`isMinifyEnabled = true`).

## Идеи для развития
- Перенос хранения на Room и DataStore
- Списки с чекбоксами и напоминания
- Тёмная тема

## Лицензия
MIT © 2026 whysafe. Telegram: [@yawhysafe](https://t.me/yawhysafe) · Сайт: [whysafe.ru](https://whysafe.ru)
