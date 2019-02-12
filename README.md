# dragon

[![Build Status][travis-badge]][travis]
[![Clojars Project][clojars-badge]][clojars]
[![Tag][tag-badge]][tag]
[![Clojure version][clojure-v]](project.clj)

[![][logo]][logo-large]

*Customised, Stasis-based Static Site Generator*


#### Contents

* [About](#about-)
* [Prerequisites](#prerequisites-)
* [Configuration](#configuration-)
* [Documentation](#documentation-)
* [CLI](#cli-)
* [Examples](#examples-)
* [License](#license-)


## About [&#x219F;](#contents)

This is really just a set of functions used for generating a couple of static
sites on different projects. The goals of this set of functions are loosely
aligned with a nascent [LFE project](http://dragon.lfe.io/) with a cool logo,
so I borrowed the name and images for a "Clojure version" :-)


## Prerequisites [&#x219F;](#contents)

* Clone the repo :-)
* `cd dragon`
* `export PATH=$PATH:$(pwd)/bin`
* `source dev-resources/shell/dragon-bash-autocompletion`


## Configuration [&#x219F;](#contents)

Every project that uses Dragon to generate static content needs to add an
EDN configuration file in a resource directory (e.g.,
`resource/my-blog/config.edn`). Here's some example content for the file:

```clj
  {:name "Chuffed: A Blog"
   :description "Thoughts on the good things in life ..."
   :domain "chuffed.github.io/blog"
   :port 5097
   :paths {
     :output "blog"}
   :logging {
     :level :debug
     :nss [clojusc dragon chuffed rfc5322]}}

```

These config entries override the defaults, which are available
[here](https://github.com/clojusc/dragon/blob/master/resources/config/dragon/config.edn):

```clj
  {:name "Dragon Blog Generator"
   :description "A fire-breathing blog generator"
   :domain "dragon.github.io"
   :port 5097
   :paths {
     :input "./posts"
     :output "docs"
     :base "/blog"
     :posts "/blog/archives"}
   :files {
     :output-template "%s.html"}
   :links {
     :template "<a href=\"%s\">%s</a>"}
   :feed {
     :count 20}
   :logging {
     :level :trace
     :nss [clojusc dragon]
     :color true}
   :parsing {
     :skip-marker "%%%"
     :word-separator "\\s"
     :word-joiner " "
     :paragraph-separator "\n\n"
     :tag-separator ",\\s?"
     :sentence-end "."
     :ellipsis " ..."
     :period-ellipsis ".."}
   :blocks {
     :enabled #{}}
   :robots {
     :allow #{"/blog"}
     :disallow #{}}
   :processor {
     :constructor :default}
   :db {
     :type :redis
     :redis {
       :pool {}
       :spec {
         :host "127.0.0.1" :port 6379}}}
   :watcher {
     :type :hawk
     :content {
       :dirs ["./posts"]
       :action clojure.core/constantly
       :restart? false}
     :docs {
       :dirs []
       :restart? false}
     :sass {
       :dirs []
       :restart? false}}
   :apis {
     :flickr {
       :access "~/.flickr/access.key"}
     :twitter {
       :app-consumer {
         :key "~/.twitter/app-consumer.key"
         :secret "~/.twitter/app-consumer.secret"}
       :user-access {
         :token "~/.twitter/user-access.token"
         :secret "~/.twitter/user-access.secret"}}}}
```


## Documentation [&#x219F;](#contents)

The latest docs are available here:

 * http://clojusc.github.io/dragon
 * http://clojusc.github.io/dragon/marginalia.html


## CLI [&#x219F;](#contents)

The project comes with a CLI. The supported commands (and any subcommands) are
documented [here](), but to give you a sense of things, here's the output of
`dragon help`:

```bash
$ dragon help
```
```
  Usage:

    dragon COMMAND [help | arg...]
    dragon [-h | --help | -v | --version]


  Commands:

    new      Create files of a given type; see 'dragon new help'
    show     Display various blog data in the terminal
    gen      Generate updated static content for a site
    run      Run the dragon site locally as a Ring app
    help     Display this usage message
    version  Display the current dragon version


  More information:

    Each command takes an optional 'help' subcommand that will provide
    usage information about the particular command in question, e.g.:


    $ dragon new help
```


## Examples [&#x219F;](#contents)

The following blogs were generated with Dragon:

* [Forgotten Roads](http://forgotten.roads.mx/blog/)
* [Starship Tools](http://starship.tools/)
* [Clojang](http://clojang.lfe.io/)
* [Clozhang](https://clozhang.github.io/blog/)


## License [&#x219F;](#contents)

Copyright © 2017, Clojure-Aided Enrichment Center

Apache License, Version 2.0.


<!-- Named page links below: /-->

[travis]: https://travis-ci.org/clojusc/dragon
[travis-badge]: https://travis-ci.org/clojusc/dragon.png?branch=master
[deps]: http://jarkeeper.com/clojusc/dragon
[deps-badge]: http://jarkeeper.com/clojusc/dragon/status.svg
[logo]: resources/images/dragon-logo-2-x250.png
[logo-large]: resources/images/dragon-logo-2-x2400-square.png
[tag-badge]: https://img.shields.io/github/tag/clojusc/dragon.svg
[tag]: https://github.com/clojusc/dragon/tags
[clojure-v]: https://img.shields.io/badge/clojure-1.8.0-blue.svg
[jdk-v]: https://img.shields.io/badge/jdk-1.7+-blue.svg
[clojars]: https://clojars.org/dragon
[clojars-badge]: https://img.shields.io/clojars/v/dragon.svg
