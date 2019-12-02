# Developers

## Backend

### Framework
* Java 11
* Lombock

### Build
```bash
./gradlew clean build
```


## Frontend

|File|Description|Copy to docroot|Build index.js|
|:--|:--|:-:|:-:|
|index.html|Mount point for Vue|Yes||
|static/favicon.ico|Favorite icon|Yes||
|src|Source files||Yes|
|src/main.js|Entry point||Yes|
|src/App.vue|Root component||Yes|
|src/components|Vue components||Yes|
|src/router|Vue Router routes||Yes|
|src/store|Vuex store||Yes|
|src/views|Views which are switched by Vue Router||Yes|
|src/assets/yanagishima.svg|Logo/Background image||Yes|
|src/assets/scss/bootstrap.scss|CSS based on Bootstrap||Yes|
|build|Build scripts for webpack|-|-|
|config|Build configs for webpack|-|-|

### Framework

- CSS
  - Bootstrap 4.1.3
  - FontAwesome 5.3.1
  - Google Fonts "[Droid+Sans](https://fonts.google.com/specimen/Droid+Sans)"
- JavaScript
  - Vue 2.5.2
  - Vuex 3.0.1
  - Vue Router 3.0.1
  - Ace Editor 1.3.3
  - Sugar 2.0.4
  - jQuery 3.3.1
- Build/Serve tool
  - webpack 3.6.0

### Customization

#### Dependence

- [Xcode](https://developer.apple.com/jp/xcode/)
- [Node.js](https://nodejs.org/ja/)

#### Install dependencies

```bash
$ cd web
$ npm install
```

#### Build

```bash
$ npm run build
```

#### Build/Serve and Livereload

```bash
$ npm start
```