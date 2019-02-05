(defn get-banner
  []
  (try
    (str
      (slurp "resources/text/banner.txt")
      (slurp "resources/text/loading.txt"))
    ;; If another project can't find the banner, just skip it;
    ;; this function is really only meant to be used by Dragon itself.
    (catch Exception _ "")))

(defn get-prompt
  [ns]
  (str "\u001B[35m[\u001B[34m"
       ns
       "\u001B[35m]\u001B[33m λ\u001B[m=> "))

(defproject dragon "0.6.0-SNAPSHOT"
  :description "Customised, Stasis-based Static Site Generator"
  :url "https://github.com/clojusc/dragon"
  :scm {
    :name "git"
    :url "https://github.com/clojusc/dragon"}
  :license {
    :name "Apache License, Version 2.0"
    :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :exclusions [
    [args4j]
    [clojusc/cljs-tools]
    [commons-codec]
    [joda-time]
    [org.apache.httpcomponents/httpclient]
    [org.apache.httpcomponents/httpcore]
    [org.apache.maven.wagon/wagon-http]
    [org.apache.maven.wagon/wagon-provider-api]
    [org.clojure/clojure]
    [org.clojure/tools.reader]
    [org.codehaus.plexus/plexus-utils]
    [org.jsoup/jsoup]]
  :dependencies [
    [args4j "2.33"]
    [ch.qos.logback/logback-classic "1.2.3"]
    [cheshire "5.8.1"]
    [clj-blogger "0.2.0"]
    [clj-http "3.9.1"]
    [clojusc/cljs-tools "0.2.2"]
    [clojusc/env-ini "0.4.1"]
    [clojusc/process-manager "0.2.0-SNAPSHOT"]
    [clojusc/rfc5322 "0.4.0"]
    [clojusc/system-manager "0.3.0"]
    [clojusc/trifl "0.4.2"]
    [clojusc/twig "0.4.1"]
    [clojusc/unified-config "0.4.0"]
    [com.stuartsierra/component "0.4.0"]
    [com.taoensso/carmine "2.19.1"]
    [commons-codec "1.11"]
    [enlive "1.1.6"]
    [hawk "0.2.11"]
    [http-kit "2.3.0"]
    [joda-time "2.10.1"]
    [leiningen-core "2.8.3"]
    [markdown-clj "1.0.7"]
    [org.apache.httpcomponents/httpclient "4.5.7"]
    [org.apache.httpcomponents/httpcore "4.4.11"]
    [org.apache.maven.wagon/wagon-http "3.3.1"]
    [org.apache.maven.wagon/wagon-provider-api "3.3.1"]
    [org.clojure/clojure "1.10.0"]
    [org.clojure/core.async "0.4.490"]
    [org.clojure/tools.reader "1.3.2"]
    [org.codehaus.plexus/plexus-utils "3.1.1"]
    [org.jsoup/jsoup "1.11.3"]
    [pandect "0.6.1"]
    [potemkin "0.4.5"]
    [ring/ring-core "1.7.1"]
    [selmer "1.12.6"]
    [stasis "2.4.0"]]
  :profiles {
    :ubercompile {
      :aot :all}
    :custom-repl {
      :repl-options {
        :init-ns dragon.repl
        :prompt ~get-prompt
        :init ~(println (get-banner))}}
    :cli {}
    :dev {
      :source-paths ["dev-resources/src"]
      :main dragon.main
      :dependencies [
        [org.clojure/tools.namespace "0.2.11"]]
      :plugins [
        [lein-shell "0.5.0"]]}
    :lint {
      :plugins [
        [jonase/eastwood "0.3.5"]
        [lein-kibit "0.1.6"]]}
    :test {
      :dependencies [
        [clojusc/ltest "0.4.0-SNAPSHOT"]]
      :plugins [
        [lein-ancient "0.6.15"]
        [lein-ltest "0.4.0-SNAPSHOT"]]
      :test-selectors {
        :select :select}}
    :docs {
      :dependencies [
        [codox-theme-rdash "0.1.2"]]
      :plugins [
        [lein-codox "0.10.5"]
        [lein-marginalia "0.9.1"]
        [lein-simpleton "1.3.0"]]
      :codox {
        :project {
          :name "Dragon"
          :description "Customised, Stasis-based Static Site Generator"}
        :namespaces [#"^dragon\.(?!dev)"]
        :themes [:rdash]
        :output-path "docs/current"
        :doc-paths ["resources/docs"]
        :metadata {
          :doc/format :markdown
          :doc "Documentation forthcoming"}}}}
  :aliases {
    "dragon"
      ^{:doc (str "The Dragon blog CLI; type `lein dragon help` or `dragon help` "
                  "for commands")}
      ["with-profile" "+cli"
       "run" "-m" "dragon.main" "cli"]
    "repl" ["with-profile" "+custom-repl,+test" "repl"]
    "ubercompile" ["with-profile" "+ubercompile" "compile"]
    "check-vers" ["with-profile" "+test" "ancient" "check" ":all"]
    "check-jars" ["with-profile" "+test" "do"
      ["deps" ":tree"]
      ["deps" ":plugin-tree"]]
    "check-deps" ["do"
      ["check-jars"]
      ["check-vers"]]
    "kibit" ["with-profile" "+lint" "kibit"]
    "eastwood" ["with-profile" "+lint" "eastwood" "{:namespaces [:source-paths]}"]
    "lint" ["do"
      ["kibit"]
      ;["eastwood"]
      ]
    "ltest" ["with-profile" "+test" "ltest"]
    "docs" ["with-profile" "+docs,+test" "do"
      ["codox"]
      ["marg" "--dir" "docs/current"
              "--file" "marginalia.html"
              "--name" "sockets"]]
    "build" ["with-profile" "+test" "do"
      ;["check-deps"]
      ["ubercompile"]
      ["lint"]
      ["ltest"]
      ["docs"]
      ["uberjar"]]
    "publish-docs" ["do"
      ["docs"]
      ["shell" "resources/scripts/publish-docs.sh"]]})
