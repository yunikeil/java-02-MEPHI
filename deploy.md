
# какой проект был выбран;

Была выбрана часть проекта - бекенда по сокращению ссылок, отвечающая за отображения покрытия тестами.

# код файла ci-cd.yml;


```yaml
name: Coverage Pages

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: "pages"
  cancel-in-progress: false

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      - name: Make Gradle wrapper executable
        run: chmod +x ./gradlew

      - name: Build with tests and coverage
        run: ./gradlew clean test jacocoTestReport --no-daemon

      - name: Upload coverage report for GitHub Pages
        uses: actions/upload-pages-artifact@v3
        with:
          path: build/reports/jacoco/test/html

  deploy:
    needs: build
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}

    permissions:
      pages: write
      id-token: write

    steps:
      - name: Deploy to GitHub Pages
        id: deployment
        uses: actions/deploy-pages@v4
```

# описание настроенных джобов (что за джобы, какие Github Actions были использованы, какие ивенты тригерят джобы, зависимости между джобами);

[Coverage Pages](.github/workflows/coverage-pages.yaml)

Назначение: Сборка проекта с тестами и генерация отчета покрытия кода с последующим деплоем на GitHub Pages.

Триггеры: workflow запускается при push или pull request в ветки main и master, а также может быть запущен вручную через workflow_dispatch.

Джобы:

1. build:
    * Запускается на Ubuntu.
    * Используются Actions: actions/checkout для клонирования репозитория, actions/setup-java для установки JDK 17 с кэшированием Gradle.
    * Делается Gradle clean, сборка, запуск тестов и генерация отчета jacocoTestReport.
    * Отчет обрабатывается с помощью actions/upload-pages-artifact для последующего деплоя.
2. deploy:
    * Запускается после успешного завершения джобы build (зависимость через needs: build).
    * Используется actions/deploy-pages для деплоя артефактов.
    * Выполняется на Ubuntu и использует environment github-pages.
 

# ссылка на открытый репозиторий, где настроен pipeline.

Репозиторий: https://github.com/yunikeil/java-02-MEPHI

Сайт куда деплоится отчёт автотестов: https://yunikeil.ru/java-02-MEPHI/

Статус деплоя + ссылка: [![Deploy coverage](https://github.com/yunikeil/java-02-MEPHI/actions/workflows/coverage-pages.yaml/badge.svg)](https://github.com/yunikeil/java-02-MEPHI/actions/workflows/coverage-pages.yaml) [![Coverage report link](https://img.shields.io/badge/coverage-report-blue)](https://yunikeil.ru/java-02-MEPHI/) 