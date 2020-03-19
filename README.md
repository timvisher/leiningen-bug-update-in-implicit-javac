Leiningen Bug: Implicit `javac` Task Doesn't Receive Project Map as
Produced by `update-in` Reproduction
===================================================================

This is as minimal a project as I could think to make to demonstrate what
I believe is a bug in `leiningen`, admittedly _way_ on the edges of normal
usage.

The bug is around `update-in` and the implicit `javac` task that is
triggered by the existence of uncompiled Java source code.

What appears to happen is that the impliict `javac` task receives the
_original *merged*_ project map rather than the project map produced by
update-in, or something very close to it, such that the `:java-cmd` value
is the original bad value rather than the `update-in`ed good value.

I'm not sure exactly what else might be going on here though. For whatever
reason removing the `:java-source-paths`, _via `update-in`_, fixes the
behavior, despite `javac` still being called. My only thought is that the
removal of the `:java-source-paths` makes the `javac` task a no-op somehow
and so further behavior that is caching the original project map somehow
actually never gets invoked.

Repo Structure
--------------

There are two primary scripts that rely on a basic linux distro, docker,
and curl.

`./run_bug_repro` makes a temp directory unless one is passed in in which
it will download the Zulu OpenJDK distro and create a temp `.m2` space and
then runs `docker … ./bug_repro` close-ish to how things look in GitHub
Actions.

`./bug_repro` runs in the docker container and demonstrates the bug via a
succession of calls. Notably, the docker container _doesn't_ have Java
installed via normal means and expect `lein` to be shared in via the root
of this project (`./run_bug_repro` takes care of that for you). It runs
`lein do clean, pprint` before the commands it runs and `lein clean` after
to make sure that the implicit `javac` should always have work to do.

All that's significant about `project.clj` is that it has:

1. A profile, `:non-existent-java` with a bad value for `:java-cmd`.

1. `:java-source-paths` is defined.

1. A `main` is available that's just a simple println.

1. A real Java file is available in `src/java`. If it's empty `javac`
   never triggers the bug.

Circumstance
------------

I'm trying to use GitHub Actions to create a Delivery Pipeline and am
running into an issue executing an unrelated task where, despite my
`update-in` actions, the original value for `:java-cmd` (which is set
purposefully in `project.clj`) was being used for some reason.

I was able to trace it down to the existence of `:java-source-paths` in
`project.clj`.

Log of Local Run 2020-03-19T10:11:12
------------------------------------

```
mkdir: /tmp/leiningen_bug.6o6xWxBu7/zulu_install: File exists
mkdir: /tmp/leiningen_bug.6o6xWxBu7/temp_m2: File exists
INFO: `project.clj` at run time:
(defproject leiningen-bug-update-in-implicit-javac "1.0.0-SNAPSHOT"
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :plugins [[lein-pprint "1.1.1"]]
  :profiles {:non-existent-java {:java-cmd "/non/existent/java"}})
✓





INFO: java is not `installed` or available in PATH?
./bug_repro: line 12: java: command not found
✓





INFO: But it's available as a standalone installation?
openjdk version "1.8.0_242"
OpenJDK Runtime Environment (Zulu 8.44.0.13-CA-linux64) (build 1.8.0_242-b20)
OpenJDK 64-Bit Server VM (Zulu 8.44.0.13-CA-linux64) (build 25.242-b20, mixed mode)
✓





INFO: I work because JAVA_CMD has been set to the standalane installation
Leiningen's classpath: /mnt/.lein/self-installs/leiningen-2.9.3-standalone.jar
Applying task do to [clean, pprint, run -m main, clean]
Applying task clean to []
Applying task pprint to []
{:compile-path "/mnt/target/classes",
 :deploy-repositories
 [["clojars"
   {:url "https://repo.clojars.org/",
    :password :gpg,
    :username :gpg}]],
 :group "leiningen-bug-update-in-implicit-javac",
 :resource-paths ("/mnt/dev-resources" "/mnt/resources"),
 :uberjar-merge-with
 {"META-INF/plexus/components.xml" leiningen.uberjar/components-merger,
  "data_readers.clj" leiningen.uberjar/clj-map-merger,
  #"META-INF/services/.*"
  [clojure.core/slurp
   (fn*
    [p1__7711__7713__auto__ p2__7712__7714__auto__]
    (clojure.core/str
     p1__7711__7713__auto__
     "\n"
     p2__7712__7714__auto__))
   clojure.core/spit]},
 :name "leiningen-bug-update-in-implicit-javac",
 :checkout-deps-shares
 [:source-paths
  :test-paths
  :resource-paths
  :compile-path
  #'leiningen.core.classpath/checkout-deps-paths],
 :source-paths ("/mnt/src/clj"),
 :eval-in :subprocess,
 :repositories
 [["central"
   {:url "https://repo1.maven.org/maven2/", :snapshots false}]
  ["clojars" {:url "https://repo.clojars.org/"}]],
 :test-paths ("/mnt/test"),
 :target-path "/mnt/target",
 :prep-tasks ["javac" "compile"],
 :java-source-paths ("/mnt/src/java"),
 :native-path "/mnt/target/native",
 :offline? false,
 :root "/mnt",
 :pedantic? ranges,
 :clean-targets [:target-path],
 :plugins ([lein-pprint/lein-pprint "1.1.1"]),
 :profiles {:non-existent-java {:java-cmd "/non/existent/java"}},
 :plugin-repositories
 [["central"
   {:url "https://repo1.maven.org/maven2/", :snapshots false}]
  ["clojars" {:url "https://repo.clojars.org/"}]],
 :aliases {"downgrade" "upgrade"},
 :version "1.0.0-SNAPSHOT",
 :jar-exclusions [#"^\." #"\Q/.\E"],
 :global-vars {},
 :uberjar-exclusions [#"(?i)^META-INF/[^/]*\.(SF|RSA|DSA)$"],
 :jvm-opts
 ["-XX:-OmitStackTraceInFastThrow"
  "-XX:+TieredCompilation"
  "-XX:TieredStopAtLevel=1"],
 :dependencies
 ([org.clojure/clojure "1.10.1"]
  [nrepl/nrepl "0.6.0" :exclusions ([org.clojure/clojure])]
  [clojure-complete/clojure-complete
   "0.2.5"
   :exclusions
   ([org.clojure/clojure])]),
 :release-tasks
 [["vcs" "assert-committed"]
  ["change" "version" "leiningen.release/bump-version" "release"]
  ["vcs" "commit"]
  ["vcs" "tag"]
  ["deploy"]
  ["change" "version" "leiningen.release/bump-version"]
  ["vcs" "commit"]
  ["vcs" "push"]],
 :test-selectors {:default (constantly true)}}
Applying task run to [-m main]
Applying task javac to nil
Running javac with [@/tmp/.leiningen-cmdline1034598991274481379.tmp]
Compiling 1 source files to /mnt/target/classes
Applying task compile to nil
All namespaces already AOT compiled.
Ohai, Charnock!
Applying task clean to []
✓





INFO: I don't work because I activate the `non-existent-java` profile
Leiningen's classpath: /mnt/.lein/self-installs/leiningen-2.9.3-standalone.jar
Applying task with-profile to [non-existent-java do clean, pprint, run -m main, clean]
Applying task do to (clean, pprint, run -m main, clean)
Applying task clean to []
Applying task pprint to []
{:compile-path "/mnt/target/classes",
 :deploy-repositories
 [["clojars"
   {:url "https://repo.clojars.org/",
    :password :gpg,
    :username :gpg}]],
 :group "leiningen-bug-update-in-implicit-javac",
 :java-cmd "/non/existent/java",
 :resource-paths ("/mnt/resources"),
 :uberjar-merge-with
 {"META-INF/plexus/components.xml" leiningen.uberjar/components-merger,
  "data_readers.clj" leiningen.uberjar/clj-map-merger,
  #"META-INF/services/.*"
  [clojure.core/slurp
   (fn*
    [p1__7711__7713__auto__ p2__7712__7714__auto__]
    (clojure.core/str
     p1__7711__7713__auto__
     "\n"
     p2__7712__7714__auto__))
   clojure.core/spit]},
 :name "leiningen-bug-update-in-implicit-javac",
 :source-paths ("/mnt/src/clj"),
 :eval-in :subprocess,
 :repositories
 [["central"
   {:url "https://repo1.maven.org/maven2/", :snapshots false}]
  ["clojars" {:url "https://repo.clojars.org/"}]],
 :test-paths ("/mnt/test"),
 :target-path "/mnt/target",
 :prep-tasks ["javac" "compile"],
 :java-source-paths ("/mnt/src/java"),
 :native-path "/mnt/target/native",
 :offline? false,
 :root "/mnt",
 :pedantic? ranges,
 :clean-targets [:target-path],
 :plugins ([lein-pprint/lein-pprint "1.1.1"]),
 :profiles {:non-existent-java {:java-cmd "/non/existent/java"}},
 :plugin-repositories
 [["central"
   {:url "https://repo1.maven.org/maven2/", :snapshots false}]
  ["clojars" {:url "https://repo.clojars.org/"}]],
 :aliases {},
 :version "1.0.0-SNAPSHOT",
 :jar-exclusions [#"^\." #"\Q/.\E"],
 :global-vars {},
 :uberjar-exclusions [#"(?i)^META-INF/[^/]*\.(SF|RSA|DSA)$"],
 :dependencies ([org.clojure/clojure "1.10.1"]),
 :release-tasks
 [["vcs" "assert-committed"]
  ["change" "version" "leiningen.release/bump-version" "release"]
  ["vcs" "commit"]
  ["vcs" "tag"]
  ["deploy"]
  ["change" "version" "leiningen.release/bump-version"]
  ["vcs" "commit"]
  ["vcs" "push"]]}
Applying task run to [-m main]
Applying task javac to nil
Running javac with [@/tmp/.leiningen-cmdline4621274986213014746.tmp]
Error encountered performing task 'do' with profile(s): 'non-existent-java'
java.io.IOException: Cannot run program "/non/existent/java" (in directory "/mnt"): error=2, No such file or directory
	at java.lang.ProcessBuilder.start(ProcessBuilder.java:1048)
	at java.lang.Runtime.exec(Runtime.java:622)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at clojure.lang.Reflector.invokeMatchingMethod(Reflector.java:167)
	at clojure.lang.Reflector.invokeInstanceMethod(Reflector.java:102)
	at leiningen.core.eval$sh.invokeStatic(eval.clj:179)
	at leiningen.core.eval$sh.doInvoke(eval.clj:173)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invokeStatic(core.clj:665)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.eval$fn__7119.invokeStatic(eval.clj:263)
	at leiningen.core.eval$fn__7119.invoke(eval.clj:261)
	at clojure.lang.MultiFn.invoke(MultiFn.java:234)
	at leiningen.javac$run_javac_subprocess.invokeStatic(javac.clj:128)
	at leiningen.javac$run_javac_subprocess.invoke(javac.clj:115)
	at leiningen.javac$javac.invokeStatic(javac.clj:147)
	at leiningen.javac$javac.doInvoke(javac.clj:136)
	at clojure.lang.RestFn.invoke(RestFn.java:410)
	at clojure.lang.AFn.applyToHelper(AFn.java:154)
	at clojure.lang.RestFn.applyTo(RestFn.java:132)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$partial_task$fn__7356.doInvoke(main.clj:284)
	at clojure.lang.RestFn.invoke(RestFn.java:410)
	at clojure.lang.AFn.applyToHelper(AFn.java:154)
	at clojure.lang.RestFn.applyTo(RestFn.java:132)
	at clojure.lang.AFunction$1.doInvoke(AFunction.java:31)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$apply_task.invokeStatic(main.clj:334)
	at leiningen.core.main$apply_task.invoke(main.clj:320)
	at leiningen.core.eval$run_prep_tasks.invokeStatic(eval.clj:51)
	at leiningen.core.eval$run_prep_tasks.invoke(eval.clj:43)
	at leiningen.core.eval$prep.invokeStatic(eval.clj:86)
	at leiningen.core.eval$prep.invoke(eval.clj:73)
	at leiningen.core.eval$eval_in_project.invokeStatic(eval.clj:364)
	at leiningen.core.eval$eval_in_project.invoke(eval.clj:358)
	at leiningen.core.eval$eval_in_project.invokeStatic(eval.clj:362)
	at leiningen.core.eval$eval_in_project.invoke(eval.clj:358)
	at leiningen.run$run_main.invokeStatic(run.clj:130)
	at leiningen.run$run_main.invoke(run.clj:123)
	at leiningen.run$run.invokeStatic(run.clj:157)
	at leiningen.run$run.doInvoke(run.clj:134)
	at clojure.lang.RestFn.applyTo(RestFn.java:139)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$partial_task$fn__7356.doInvoke(main.clj:284)
	at clojure.lang.RestFn.applyTo(RestFn.java:139)
	at clojure.lang.AFunction$1.doInvoke(AFunction.java:31)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$apply_task.invokeStatic(main.clj:334)
	at leiningen.core.main$apply_task.invoke(main.clj:320)
	at leiningen.core.main$resolve_and_apply.invokeStatic(main.clj:343)
	at leiningen.core.main$resolve_and_apply.invoke(main.clj:336)
	at leiningen.do$do.invokeStatic(do.clj:40)
	at leiningen.do$do.doInvoke(do.clj:32)
	at clojure.lang.RestFn.applyTo(RestFn.java:139)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$partial_task$fn__7356.doInvoke(main.clj:284)
	at clojure.lang.RestFn.applyTo(RestFn.java:139)
	at clojure.lang.AFunction$1.doInvoke(AFunction.java:31)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$apply_task.invokeStatic(main.clj:334)
	at leiningen.core.main$apply_task.invoke(main.clj:320)
	at leiningen.with_profile$with_profiles_STAR_.invokeStatic(with_profile.clj:14)
	at leiningen.with_profile$with_profiles_STAR_.invoke(with_profile.clj:8)
	at leiningen.with_profile$apply_task_with_profiles.invokeStatic(with_profile.clj:53)
	at leiningen.with_profile$apply_task_with_profiles.invoke(with_profile.clj:45)
	at leiningen.with_profile$with_profile$fn__11285.invoke(with_profile.clj:85)
	at clojure.core$mapv$fn__8430.invoke(core.clj:6912)
	at clojure.core.protocols$fn__8144.invokeStatic(protocols.clj:168)
	at clojure.core.protocols$fn__8144.invoke(protocols.clj:124)
	at clojure.core.protocols$fn__8099$G__8094__8108.invoke(protocols.clj:19)
	at clojure.core.protocols$seq_reduce.invokeStatic(protocols.clj:31)
	at clojure.core.protocols$fn__8131.invokeStatic(protocols.clj:75)
	at clojure.core.protocols$fn__8131.invoke(protocols.clj:75)
	at clojure.core.protocols$fn__8073$G__8068__8086.invoke(protocols.clj:13)
	at clojure.core$reduce.invokeStatic(core.clj:6828)
	at clojure.core$mapv.invokeStatic(core.clj:6903)
	at clojure.core$mapv.invoke(core.clj:6903)
	at leiningen.with_profile$with_profile.invokeStatic(with_profile.clj:85)
	at leiningen.with_profile$with_profile.doInvoke(with_profile.clj:63)
	at clojure.lang.RestFn.applyTo(RestFn.java:146)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$partial_task$fn__7356.doInvoke(main.clj:284)
	at clojure.lang.RestFn.applyTo(RestFn.java:139)
	at clojure.lang.AFunction$1.doInvoke(AFunction.java:31)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$apply_task.invokeStatic(main.clj:334)
	at leiningen.core.main$apply_task.invoke(main.clj:320)
	at leiningen.core.main$resolve_and_apply.invokeStatic(main.clj:343)
	at leiningen.core.main$resolve_and_apply.invoke(main.clj:336)
	at leiningen.core.main$_main$fn__7445.invoke(main.clj:453)
	at leiningen.core.main$_main.invokeStatic(main.clj:442)
	at leiningen.core.main$_main.doInvoke(main.clj:439)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.core$apply.invokeStatic(core.clj:665)
	at clojure.main$main_opt.invokeStatic(main.clj:491)
	at clojure.main$main_opt.invoke(main.clj:487)
	at clojure.main$main.invokeStatic(main.clj:598)
	at clojure.main$main.doInvoke(main.clj:561)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.main.main(main.java:37)
Caused by: java.io.IOException: error=2, No such file or directory
	at java.lang.UNIXProcess.forkAndExec(Native Method)
	at java.lang.UNIXProcess.<init>(UNIXProcess.java:247)
	at java.lang.ProcessImpl.start(ProcessImpl.java:134)
	at java.lang.ProcessBuilder.start(ProcessBuilder.java:1029)
	... 120 more
✓





INFO: I work again because I use `update-in` to set the java-cmd to the
INFO: standalone installation _and_ dissoc the `:java-source-paths` from
INFO: the project. The implicit `javac` task is still triggered but
INFO: because `:java-source-paths` was `dissoc`ed it appears to be a
INFO: no-op that doesn't trigger the bug.
Leiningen's classpath: /mnt/.lein/self-installs/leiningen-2.9.3-standalone.jar
Applying task with-profile to [non-existent-java update-in : assoc :java-cmd "/java/bin/java" -- update-in : dissoc :java-source-paths -- do clean, pprint, run -m main, clean]
Applying task update-in to (: assoc :java-cmd "/java/bin/java" -- update-in : dissoc :java-source-paths -- do clean, pprint, run -m main, clean)
Applying task update-in to [: dissoc :java-source-paths -- do clean, pprint, run -m main, clean]
Applying task do to [clean, pprint, run -m main, clean]
Applying task clean to []
Applying task pprint to []
{:compile-path "/mnt/target/classes",
 :deploy-repositories
 [["clojars"
   {:url "https://repo.clojars.org/",
    :password :gpg,
    :username :gpg}]],
 :group "leiningen-bug-update-in-implicit-javac",
 :java-cmd "/java/bin/java",
 :resource-paths ("/mnt/resources"),
 :uberjar-merge-with
 {"META-INF/plexus/components.xml" leiningen.uberjar/components-merger,
  "data_readers.clj" leiningen.uberjar/clj-map-merger,
  #"META-INF/services/.*"
  [clojure.core/slurp
   (fn*
    [p1__7711__7713__auto__ p2__7712__7714__auto__]
    (clojure.core/str
     p1__7711__7713__auto__
     "\n"
     p2__7712__7714__auto__))
   clojure.core/spit]},
 :name "leiningen-bug-update-in-implicit-javac",
 :source-paths ("/mnt/src/clj"),
 :eval-in :subprocess,
 :repositories
 [["central"
   {:url "https://repo1.maven.org/maven2/", :snapshots false}]
  ["clojars" {:url "https://repo.clojars.org/"}]],
 :test-paths ("/mnt/test"),
 :target-path "/mnt/target",
 :prep-tasks ["javac" "compile"],
 :native-path "/mnt/target/native",
 :offline? false,
 :root "/mnt",
 :pedantic? ranges,
 :clean-targets [:target-path],
 :plugins ([lein-pprint/lein-pprint "1.1.1"]),
 :profiles {:non-existent-java {:java-cmd "/non/existent/java"}},
 :plugin-repositories
 [["central"
   {:url "https://repo1.maven.org/maven2/", :snapshots false}]
  ["clojars" {:url "https://repo.clojars.org/"}]],
 :aliases {},
 :version "1.0.0-SNAPSHOT",
 :jar-exclusions [#"^\." #"\Q/.\E"],
 :global-vars {},
 :uberjar-exclusions [#"(?i)^META-INF/[^/]*\.(SF|RSA|DSA)$"],
 :dependencies ([org.clojure/clojure "1.10.1"]),
 :release-tasks
 [["vcs" "assert-committed"]
  ["change" "version" "leiningen.release/bump-version" "release"]
  ["vcs" "commit"]
  ["vcs" "tag"]
  ["deploy"]
  ["change" "version" "leiningen.release/bump-version"]
  ["vcs" "commit"]
  ["vcs" "push"]]}
Applying task run to [-m main]
Applying task javac to nil
Running javac with [@/tmp/.leiningen-cmdline2425239739521238942.tmp]
Applying task compile to nil
All namespaces already AOT compiled.
Ohai, Charnock!
Applying task clean to []
✓





INFO: I don't work because I didn't dissoc the
INFO: `:java-source-paths` key from the project, despite setting the
INFO: :java-cmd in the project root. For whatever reason, the implicit
INFO: `javac` appears to not receive my `update-in`ed project map and
INFO: instead receives the original thing.
Leiningen's classpath: /mnt/.lein/self-installs/leiningen-2.9.3-standalone.jar
Applying task with-profile to [non-existent-java update-in : assoc :java-cmd "/java/bin/java" -- do clean, pprint, run -m main, clean]
Applying task update-in to (: assoc :java-cmd "/java/bin/java" -- do clean, pprint, run -m main, clean)
Applying task do to [clean, pprint, run -m main, clean]
Applying task clean to []
Applying task pprint to []
{:compile-path "/mnt/target/classes",
 :deploy-repositories
 [["clojars"
   {:url "https://repo.clojars.org/",
    :password :gpg,
    :username :gpg}]],
 :group "leiningen-bug-update-in-implicit-javac",
 :java-cmd "/java/bin/java",
 :resource-paths ("/mnt/resources"),
 :uberjar-merge-with
 {"META-INF/plexus/components.xml" leiningen.uberjar/components-merger,
  "data_readers.clj" leiningen.uberjar/clj-map-merger,
  #"META-INF/services/.*"
  [clojure.core/slurp
   (fn*
    [p1__7711__7713__auto__ p2__7712__7714__auto__]
    (clojure.core/str
     p1__7711__7713__auto__
     "\n"
     p2__7712__7714__auto__))
   clojure.core/spit]},
 :name "leiningen-bug-update-in-implicit-javac",
 :source-paths ("/mnt/src/clj"),
 :eval-in :subprocess,
 :repositories
 [["central"
   {:url "https://repo1.maven.org/maven2/", :snapshots false}]
  ["clojars" {:url "https://repo.clojars.org/"}]],
 :test-paths ("/mnt/test"),
 :target-path "/mnt/target",
 :prep-tasks ["javac" "compile"],
 :java-source-paths ("/mnt/src/java"),
 :native-path "/mnt/target/native",
 :offline? false,
 :root "/mnt",
 :pedantic? ranges,
 :clean-targets [:target-path],
 :plugins ([lein-pprint/lein-pprint "1.1.1"]),
 :profiles {:non-existent-java {:java-cmd "/non/existent/java"}},
 :plugin-repositories
 [["central"
   {:url "https://repo1.maven.org/maven2/", :snapshots false}]
  ["clojars" {:url "https://repo.clojars.org/"}]],
 :aliases {},
 :version "1.0.0-SNAPSHOT",
 :jar-exclusions [#"^\." #"\Q/.\E"],
 :global-vars {},
 :uberjar-exclusions [#"(?i)^META-INF/[^/]*\.(SF|RSA|DSA)$"],
 :dependencies ([org.clojure/clojure "1.10.1"]),
 :release-tasks
 [["vcs" "assert-committed"]
  ["change" "version" "leiningen.release/bump-version" "release"]
  ["vcs" "commit"]
  ["vcs" "tag"]
  ["deploy"]
  ["change" "version" "leiningen.release/bump-version"]
  ["vcs" "commit"]
  ["vcs" "push"]]}
Applying task run to [-m main]
Applying task javac to nil
Running javac with [@/tmp/.leiningen-cmdline2972058875922164133.tmp]
Error encountered performing task 'update-in' with profile(s): 'non-existent-java'
java.io.IOException: Cannot run program "/non/existent/java" (in directory "/mnt"): error=2, No such file or directory
	at java.lang.ProcessBuilder.start(ProcessBuilder.java:1048)
	at java.lang.Runtime.exec(Runtime.java:622)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at clojure.lang.Reflector.invokeMatchingMethod(Reflector.java:167)
	at clojure.lang.Reflector.invokeInstanceMethod(Reflector.java:102)
	at leiningen.core.eval$sh.invokeStatic(eval.clj:179)
	at leiningen.core.eval$sh.doInvoke(eval.clj:173)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invokeStatic(core.clj:665)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.eval$fn__7119.invokeStatic(eval.clj:263)
	at leiningen.core.eval$fn__7119.invoke(eval.clj:261)
	at clojure.lang.MultiFn.invoke(MultiFn.java:234)
	at leiningen.javac$run_javac_subprocess.invokeStatic(javac.clj:128)
	at leiningen.javac$run_javac_subprocess.invoke(javac.clj:115)
	at leiningen.javac$javac.invokeStatic(javac.clj:147)
	at leiningen.javac$javac.doInvoke(javac.clj:136)
	at clojure.lang.RestFn.invoke(RestFn.java:410)
	at clojure.lang.AFn.applyToHelper(AFn.java:154)
	at clojure.lang.RestFn.applyTo(RestFn.java:132)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$partial_task$fn__7356.doInvoke(main.clj:284)
	at clojure.lang.RestFn.invoke(RestFn.java:410)
	at clojure.lang.AFn.applyToHelper(AFn.java:154)
	at clojure.lang.RestFn.applyTo(RestFn.java:132)
	at clojure.lang.AFunction$1.doInvoke(AFunction.java:31)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$apply_task.invokeStatic(main.clj:334)
	at leiningen.core.main$apply_task.invoke(main.clj:320)
	at leiningen.core.eval$run_prep_tasks.invokeStatic(eval.clj:51)
	at leiningen.core.eval$run_prep_tasks.invoke(eval.clj:43)
	at leiningen.core.eval$prep.invokeStatic(eval.clj:86)
	at leiningen.core.eval$prep.invoke(eval.clj:73)
	at leiningen.core.eval$eval_in_project.invokeStatic(eval.clj:364)
	at leiningen.core.eval$eval_in_project.invoke(eval.clj:358)
	at leiningen.core.eval$eval_in_project.invokeStatic(eval.clj:362)
	at leiningen.core.eval$eval_in_project.invoke(eval.clj:358)
	at leiningen.run$run_main.invokeStatic(run.clj:130)
	at leiningen.run$run_main.invoke(run.clj:123)
	at leiningen.run$run.invokeStatic(run.clj:157)
	at leiningen.run$run.doInvoke(run.clj:134)
	at clojure.lang.RestFn.applyTo(RestFn.java:139)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$partial_task$fn__7356.doInvoke(main.clj:284)
	at clojure.lang.RestFn.applyTo(RestFn.java:139)
	at clojure.lang.AFunction$1.doInvoke(AFunction.java:31)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$apply_task.invokeStatic(main.clj:334)
	at leiningen.core.main$apply_task.invoke(main.clj:320)
	at leiningen.core.main$resolve_and_apply.invokeStatic(main.clj:343)
	at leiningen.core.main$resolve_and_apply.invoke(main.clj:336)
	at leiningen.do$do.invokeStatic(do.clj:40)
	at leiningen.do$do.doInvoke(do.clj:32)
	at clojure.lang.RestFn.applyTo(RestFn.java:139)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$partial_task$fn__7356.doInvoke(main.clj:284)
	at clojure.lang.RestFn.applyTo(RestFn.java:139)
	at clojure.lang.AFunction$1.doInvoke(AFunction.java:31)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$apply_task.invokeStatic(main.clj:334)
	at leiningen.core.main$apply_task.invoke(main.clj:320)
	at leiningen.core.main$resolve_and_apply.invokeStatic(main.clj:343)
	at leiningen.core.main$resolve_and_apply.invoke(main.clj:336)
	at leiningen.update_in$update_in.invokeStatic(update_in.clj:37)
	at leiningen.update_in$update_in.doInvoke(update_in.clj:24)
	at clojure.lang.RestFn.applyTo(RestFn.java:146)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$partial_task$fn__7356.doInvoke(main.clj:284)
	at clojure.lang.RestFn.applyTo(RestFn.java:139)
	at clojure.lang.AFunction$1.doInvoke(AFunction.java:31)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$apply_task.invokeStatic(main.clj:334)
	at leiningen.core.main$apply_task.invoke(main.clj:320)
	at leiningen.with_profile$with_profiles_STAR_.invokeStatic(with_profile.clj:14)
	at leiningen.with_profile$with_profiles_STAR_.invoke(with_profile.clj:8)
	at leiningen.with_profile$apply_task_with_profiles.invokeStatic(with_profile.clj:53)
	at leiningen.with_profile$apply_task_with_profiles.invoke(with_profile.clj:45)
	at leiningen.with_profile$with_profile$fn__11285.invoke(with_profile.clj:85)
	at clojure.core$mapv$fn__8430.invoke(core.clj:6912)
	at clojure.core.protocols$fn__8144.invokeStatic(protocols.clj:168)
	at clojure.core.protocols$fn__8144.invoke(protocols.clj:124)
	at clojure.core.protocols$fn__8099$G__8094__8108.invoke(protocols.clj:19)
	at clojure.core.protocols$seq_reduce.invokeStatic(protocols.clj:31)
	at clojure.core.protocols$fn__8131.invokeStatic(protocols.clj:75)
	at clojure.core.protocols$fn__8131.invoke(protocols.clj:75)
	at clojure.core.protocols$fn__8073$G__8068__8086.invoke(protocols.clj:13)
	at clojure.core$reduce.invokeStatic(core.clj:6828)
	at clojure.core$mapv.invokeStatic(core.clj:6903)
	at clojure.core$mapv.invoke(core.clj:6903)
	at leiningen.with_profile$with_profile.invokeStatic(with_profile.clj:85)
	at leiningen.with_profile$with_profile.doInvoke(with_profile.clj:63)
	at clojure.lang.RestFn.applyTo(RestFn.java:146)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$partial_task$fn__7356.doInvoke(main.clj:284)
	at clojure.lang.RestFn.applyTo(RestFn.java:139)
	at clojure.lang.AFunction$1.doInvoke(AFunction.java:31)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.core$apply.invokeStatic(core.clj:667)
	at clojure.core$apply.invoke(core.clj:660)
	at leiningen.core.main$apply_task.invokeStatic(main.clj:334)
	at leiningen.core.main$apply_task.invoke(main.clj:320)
	at leiningen.core.main$resolve_and_apply.invokeStatic(main.clj:343)
	at leiningen.core.main$resolve_and_apply.invoke(main.clj:336)
	at leiningen.core.main$_main$fn__7445.invoke(main.clj:453)
	at leiningen.core.main$_main.invokeStatic(main.clj:442)
	at leiningen.core.main$_main.doInvoke(main.clj:439)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.core$apply.invokeStatic(core.clj:665)
	at clojure.main$main_opt.invokeStatic(main.clj:491)
	at clojure.main$main_opt.invoke(main.clj:487)
	at clojure.main$main.invokeStatic(main.clj:598)
	at clojure.main$main.doInvoke(main.clj:561)
	at clojure.lang.RestFn.applyTo(RestFn.java:137)
	at clojure.lang.Var.applyTo(Var.java:705)
	at clojure.main.main(main.java:37)
Caused by: java.io.IOException: error=2, No such file or directory
	at java.lang.UNIXProcess.forkAndExec(Native Method)
	at java.lang.UNIXProcess.<init>(UNIXProcess.java:247)
	at java.lang.ProcessImpl.start(ProcessImpl.java:134)
	at java.lang.ProcessBuilder.start(ProcessBuilder.java:1029)
	... 136 more
✓





# To run again with cached dependencies:
./run_bug_repro '/tmp/leiningen_bug.6o6xWxBu7'
```
