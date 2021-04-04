(defproject com.oscaro/clj-gcloud-common "0.112-1.1-SNAPSHOT"
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
  [[com.google.cloud/google-cloud-bom "0.150.0"
    :extension "pom"
    :scope "import"]
   [com.google.cloud/google-cloud-shared-dependencies "0.20.1"
    :extension "pom"
    :scope "import"]
   ;; Handle version mismatches between google-cloud-bom and
   ;; google-cloud-shared-dependencies.
   [com.google.protobuf/protobuf-java "3.15.3"]
   [com.google.protobuf/protobuf-java-util "3.15.3"]
   ;; Select non-android Guava to work around Guava versioning mess
   ;; <https://github.com/google/guava/issues/2914>.
   [com.google.guava/guava "30.1-jre"]]
  :dependencies
  [[org.clojure/clojure "1.10.3" :scope "provided"]
   ;; This must correspond to the version pinned in BOM files.
   [com.google.cloud/google-cloud-core "1.94.3"]
   [com.google.cloud/google-cloud-core-http "1.94.3"]]
  :profiles
  {:dev
   {:dependencies
    [[com.google.cloud/google-cloud-bigquery "1.127.8"]
     [com.google.cloud/google-cloud-pubsub "1.112.0"]
     [com.google.cloud/google-cloud-storage "1.113.14"]
     [com.google.cloud/google-cloud-datastore "1.106.0"]
     [org.clojure/tools.namespace "1.1.0"]]
    :source-paths   ["dev"]
    :resource-paths ["test-resources"]
    :test-selectors {:ci (complement :integration)}}}
  :global-vars {*warn-on-reflection* true})
