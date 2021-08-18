(defproject com.oscaro/clj-gcloud-common "0.159-1.0-SNAPSHOT"
  :description "Common library for all goxogogle cloud clojure wrappers"
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
  [[com.google.cloud/google-cloud-bom "0.159.0"
    :extension "pom"
    :scope "import"]
   [com.google.cloud/google-cloud-shared-dependencies "2.0.1"
    :extension "pom"
    :scope "import"]
   ;; Select non-android Guava to work around Guava versioning mess
   ;; <https://github.com/google/guava/issues/2914>.
   [com.google.guava/guava "30.1.1-jre"]]
  :dependencies
  [[org.clojure/clojure "1.10.3" :scope "provided"]
   ;; This must correspond to the version pinned in BOM files.
   [com.google.cloud/google-cloud-core "2.0.5"]
   [com.google.cloud/google-cloud-core-http "2.0.5"]
   ;; grpc-api is required to compile ‘clj-gcloud.common’ namespace
   [io.grpc/grpc-api "1.39.0"]
   ;; Handle version mismatches between grpc-api and other deps.
   [com.google.errorprone/error_prone_annotations "2.8.1"]]
  :profiles
  {:dev
   {:dependencies
    [[com.google.cloud/google-cloud-bigquery "2.1.2"]
     [com.google.cloud/google-cloud-pubsub "1.114.0"]
     [com.google.cloud/google-cloud-storage "2.0.1"]
     [com.google.cloud/google-cloud-datastore "2.0.1"]
     [org.clojure/tools.namespace "1.1.0"]]
    :source-paths   ["dev"]
    :resource-paths ["test-resources"]
    :test-selectors {:ci (complement :integration)}}}
  :global-vars {*warn-on-reflection* true})
