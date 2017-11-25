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

(defproject dragon "0.4.2"
  :description "Customised, Stasis-based Static Site Generator"
  :url "https://github.com/clojusc/dragon"
  :scm {
    :name "git"
    :url "https://github.com/clojusc/dragon"}
  :license {
    :name "Apache License, Version 2.0"
    :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [
    [cheshire "5.8.0"]
    [clj-http "3.7.0"]
    [clojusc/env-ini "0.4.1"]
    [clojusc/rfc5322 "0.4.0"]
    [clojusc/trifl "0.2.0"]
    [clojusc/twig "0.3.2"]
    [com.datomic/clj-client "0.8.606"]
    [com.stuartsierra/component "0.3.2"]
    [com.taoensso/carmine "2.16.0" :exclusions [
      com.taoensso/encore
      com.taoensso/truss]]
    [http-kit "2.2.0"]
    [leiningen-core "2.7.1" :exclusions [
      commons-logging
      org.apache.httpcomponents/httpclient
      org.apache.httpcomponents/httpcore
      org.codehaus.plexus/plexus-utils]]
    [markdown-clj "1.0.1"]
    [org.clojure/clojure "1.8.0"]
    [org.clojure/core.async "0.3.465"]
    [pandect "0.6.1"]
    [potemkin "0.4.4"]
    [ring/ring-core "1.6.3"]
    [selmer "1.11.3" :exclusions [joda-time]]
    [stasis "2.3.0"]]
  :profiles {
    :dragon {
      :cli {
        :log-level :trace}}
    :ubercompile {
      :aot :all}
    :custom-repl {
      :repl-options {
        :init-ns dragon.dev
        :prompt ~get-prompt
        :init ~(println (get-banner))}}
    :cli {}
    :dev {
      :source-paths ["dev-resources/src"]
      :main dragon.main
      :dependencies [
        [org.clojure/tools.namespace "0.2.11"]]
      :plugins [
        [lein-simpleton "1.3.0"]]}
    :test {
      :exclusions [org.clojure/clojure]
      :dependencies [
        [clojusc/ltest "0.3.0-SNAPSHOT"]]
      :plugins [
        [jonase/eastwood "0.2.5"]
        [lein-ancient "0.6.14"]
        [lein-bikeshed "0.5.0"]
        [lein-kibit "0.1.6"]
        [lein-ltest "0.3.0-SNAPSHOT"]
        [venantius/yagni "0.1.4"]]
        :test-selectors {
          :select :select}}
    :docs {
      :dependencies [
        [codox-theme-rdash "0.1.2"]]
      :plugins [
        [lein-codox "0.10.3"]
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
    "check-deps" ["with-profile" "+test" "ancient" "check" ":all"]
    "lint" ["with-profile" "+test" "kibit"]
    "ltest" ["with-profile" "+test" "ltest"]
    "docs" ["with-profile" "+docs,+test" "do"
      ["codox"]
      ["marg" "--dir" "docs/current"
              "--file" "marginalia.html"
              "--name" "sockets"]]
    "build" ["with-profile" "+test" "do"
      ;["check-deps"]
      ["lint"]
      ["docs"]
      ["ubercompile"]
      ["clean"]
      ["uberjar"]
      ["clean"]
      ["test"]]})
