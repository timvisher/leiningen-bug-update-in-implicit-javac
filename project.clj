(defproject leiningen-bug-update-in-implicit-javac "1.0.0-SNAPSHOT"
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :plugins [[lein-pprint "1.1.1"]]
  :profiles {:non-existent-java {:java-cmd "/non/existent/java"}})
