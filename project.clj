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

(defproject dragon "0.3.0-SNAPSHOT"
  :description "Customised, Stasis-based Static Site Generator"
  :url "https://github.com/clojusc/dragon"
  :scm {
    :name "git"
    :url "https://github.com/clojusc/dragon"}
  :license {
    :name "Apache License, Version 2.0"
    :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [
    [clojusc/env-ini "0.3.0"]
    [clojusc/rfc5322 "0.3.0"]
    [clojusc/trifl "0.2.0-SNAPSHOT"]
    [clojusc/twig "0.3.2-SNAPSHOT"]
    [com.stuartsierra/component "0.3.2"]
    [http-kit "2.2.0"]
    [leiningen-core "2.7.1"]
    [markdown-clj "1.0.1"]
    [org.clojure/clojure "1.8.0"]
    [org.clojure/core.async "0.3.443"]
    [potemkin "0.4.4"]
    [ring/ring-core "1.6.2"]
    [selmer "1.11.1"]
    [stasis "2.3.0"]
    [tentacles "0.5.1"]]
  :dragon {
    :dev-port 5097
    :output-dir "docs"
    :base-path "/blog"
    :posts-path "/blog/archives"
    :cli {
      :log-level :debug}}
  :profiles {
    :uberjar {:aot :all}
    :custom-repl {
      :repl-options {
        :init-ns dragon.dev
        :prompt ~get-prompt
        :init ~(println (get-banner))}}
    :dev {
      :source-paths ["dev-resources/src"]
      :main dragon.main
      :plugins [
        [lein-simpleton "1.3.0"]]
      :dependencies [
        [org.clojure/tools.namespace "0.2.11"]]}
    :test {
      :exclusions [org.clojure/clojure]
      :plugins [
        [lein-ancient "0.6.12"]
        [jonase/eastwood "0.2.4"]
        [lein-bikeshed "0.4.1"]
        [lein-kibit "0.1.5"]
        [venantius/yagni "0.1.4"]]}
    :docs {
      :dependencies [
        [codox-theme-rdash "0.1.2"]]
      :plugins [
        [lein-codox "0.10.3"]
        [lein-marginalia "0.9.0"]
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
    "blog"
      ^{:doc (str "The Dragon CLI; type `lein dragon help` for commands\n")}
      ["run" "-m" "dragon.main" "cli"]
    "repl" ["with-profile" "+custom-repl" "repl"]
    "check-deps" ["with-profile" "+test" "ancient" "check" ":all"]
    "lint" ["with-profile" "+test" "kibit"]
    "docs" ["with-profile" "+docs" "do"
      ["codox"]
      ["marg" "--dir" "docs/current"
              "--file" "marginalia.html"
              "--name" "sockets"]]
    "build" ["with-profile" "+test" "do"
      ["check-deps"]
      ["lint"]
      ["test"]
      ["compile"]
      ["docs"]
      ["uberjar"]]})

