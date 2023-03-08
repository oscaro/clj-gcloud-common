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
  [[com.google.cloud/google-cloud-bom "0.192.0"
    :extension "pom"
    :scope "import"]
   [com.google.cloud/google-cloud-shared-dependencies "3.5.0"
    :extension "pom"
    :scope "import"]]
  :dependencies
  [[org.clojure/clojure "1.11.1" :scope "provided"]
   ;; This must correspond to the version pinned in BOM files.
   [com.google.cloud/google-cloud-core "2.13.0"]
   [com.google.cloud/google-cloud-core-http "2.13.0"]
   ;; grpc-api is required to compile ‘clj-gcloud.common’ namespace
   [io.grpc/grpc-api "1.54.0"]
   ;; Handle version mismatches between grpc-api and other deps.
   [com.google.errorprone/error_prone_annotations "2.18.0"]]
  :profiles
  {:dev
   {:dependencies
    [[com.google.cloud/google-cloud-bigquery "2.24.2"]
     [com.google.cloud/google-cloud-pubsub "1.123.7"]
     [com.google.cloud/google-cloud-datastore "2.14.1"]
     [com.google.cloud/google-cloud-storage "2.20.2"]
     [org.clojure/tools.namespace "1.4.4"]]
    :source-paths   ["dev" "test"]
    :resource-paths ["test-resources"]
    :test-selectors {:ci (complement :integration)}
    :global-vars {*warn-on-reflection* true}}})
