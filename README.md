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

Every project that uses Dragon to generate static content needs to add some
configuration to its `project.clj` file. Here's an example taken from
the [Clojang Blog](http://clojang.lfe.io/) e.g.:

```clj
  :dragon {
    :domain "clojang.lfe.io"
    :name "The Clojang Blog"
    :description "News, Information, & Tutorials for the Clojang Library Collection"
    :dev-port 5097
    :output-dir "docs"
    :posts-path "/archives"
    :feed-count 20
    :cli {
      :log-level :info
      :log-ns [clojang.blog dragon]}}
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
