(defproject com.oscaro/clj-gcloud-common "0.185-2.0-SNAPSHOT"
  :description "Common library for all google cloud clojure wrappers"
  :url "https://github.com/oscaro/clj-gcloud-common"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["snapshots" {:url "https://repo.clojars.org"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://repo.clojars.org"
                                      :creds :gpg}]]
  :managed-dependencies
  ;; Google “Bill of Materials” (BOM) defines a combination of
  ;; dependency versions that work well with each other.
  ;;
  ;; Note that, however, instead of declaring
  ;; `com.google.cloud/google-cloud-bom` and
  ;; `com.google.cloud/google-cloud-shared-dependencies` as the source
  ;; of the managed-dependencies as we do now, the recommendation is
  ;; to use `com.google.cloud/libraries-bom` instead. See:
  ;;
  ;; https://github.com/googleapis/java-cloud-bom/tree/main/google-cloud-bom
  ;;
  ;; In theory, this sounds great as Leiningen's
  ;; `:managed-dependencies` allows one to specify versions so that
  ;; this and other dependent packages don't have to worry about
  ;; setting. See:
  ;;
  ;; https://cljdoc.org/d/leiningen/leiningen/2.9.8/doc/managed-dependencies-with-leiningen
  ;;
  ;; In practice, however, it doesn't work because leiningen doesn't
  ;; read a bom to extract package versions from. There's a ticket
  ;; open and marked as `enhancement` but it dates from 2018:
  ;;
  ;; https://github.com/technomancy/leiningen/issues/2414
  ;;
  ;; until that is fixed, we still need to ensure to specify
  ;; dependencies with package versions matching those in the
  ;; bom. Also, this isn't a problem solved by migrating to tools/deps
  ;; as such feature is not yet supported by the tooling:
  ;;
  ;; https://clojure.atlassian.net/jira/software/c/projects/TDEPS/issues/TDEPS-174
  ;;
  ;; Since the version of this package is also based on
  ;; google-cloud-bom, we'll continue to use that. For this release,
  ;; we'll looking at versions specified here:
  ;;
  ;; https://mvnrepository.com/artifact/com.google.cloud/google-cloud-bom/0.199.0
  ;;
  ;; which seem to correspond to
  ;;
  ;; https://mvnrepository.com/artifact/com.google.cloud/libraries-bom/26.18.0
  [[com.google.cloud/google-cloud-bom "0.199.0"
    :extension "pom"
    :scope "import"]
   [com.google.cloud/google-cloud-shared-dependencies "3.13.0"
    :extension "pom"
    :scope "import"]]
  :dependencies
  [[org.clojure/clojure "1.11.1" :scope "provided"]
   ;; This must correspond to the version pinned in BOM files.
   [com.google.cloud/google-cloud-core "2.21.0"]
   [com.google.cloud/google-cloud-core-http "2.21.0"]
   [com.google.http-client/google-http-client-gson "1.43.3"]
   ;; grpc-api is required to compile ‘clj-gcloud.common’ namespace
   [io.grpc/grpc-api "1.56.1"]
   ;; Handle version mismatches between grpc-api and other deps.
   [com.google.errorprone/error_prone_annotations "2.20.0"]]
  :profiles
  {:dev
   {:dependencies
    [[com.google.cloud/google-cloud-bigquery "2.29.0"]
     [com.google.cloud/google-cloud-pubsub "1.123.17"]
     [com.google.cloud/google-cloud-datastore "2.16.1"]
     [com.google.cloud/google-cloud-storage "2.23.0"]
     [org.clojure/tools.namespace "1.4.4"]]
    :source-paths   ["dev" "test"]
    :resource-paths ["test-resources"]
    :test-selectors {:ci (complement :integration)}
    :global-vars {*warn-on-reflection* true}}})
