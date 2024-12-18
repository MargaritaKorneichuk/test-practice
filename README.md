# Тесты для веб-приложения "Еда"

Этот репозиторий содержит набор тестов JUnit 5 для автоматизированного тестирования веб-приложения "Еда".  Тесты проверяют функциональность добавления новых продуктов в базу данных и корректность отображения информации в таблице.

## Функциональность, покрытая тестами:

* Добавление овощей (с примерами Картофель и Melotria).
* Добавление фруктов (с примерами Клубника и Mangosteen).
* Проверка правильности отображения названия, типа и признака "экзотичности" добавленных продуктов в таблице.
* Проверка сброса данных и возврата к исходному состоянию.

## Предварительные условия:

* Установлен JDK 17 или выше.
* Установлен Maven (или Gradle).
* Установлен Selenium WebDriver с драйвером для Chrome.  Путь к драйверу должен быть указан в переменной окружения `webdriver.chrome.driver` или в коде.
* Запущен локальный сервер веб-приложения "Еда" на адресе `http://localhost:8080/food`.

## Запуск тестов:

1. Клонируйте репозиторий.
2. Запустите тесты с помощью Maven: `mvn test`

## Структура проекта:

Проект использует шаблон Page Object Model (POM) для повышения читаемости и поддерживаемости кода.
