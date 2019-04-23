(defproject com.oscaro/clj-gcloud-common "0.71-1.1"
  :description "Common library for all google cloud clojure wrappers"
  :url "https://github.com/oscaro/clj-gcloud-common"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [cheshire "5.8.1"]
                 [com.google.cloud/google-cloud-core "1.53.0"]
                 [com.google.cloud/google-cloud-core-http "1.53.0"]
                 ;; The dependencies below were carefully added and excluded
                 ;; in order to remove all confusing warnings.
                 [io.grpc/grpc-core "1.16.1"]]
  :profiles
  {:dev
   {:dependencies   [[com.google.cloud/google-cloud-bigquery "1.53.0"]
                     [com.google.cloud/google-cloud-pubsub "1.53.0" :exclusions [io.grpc/grpc-core]]
                     [com.google.cloud/google-cloud-storage "1.53.0"]
                     [com.google.cloud/google-cloud-datastore "1.53.0"]
                     [org.clojure/tools.namespace "0.2.11"]]
    :source-paths   ["dev"]
    :resource-paths ["test-resources"]
    :test-selectors {:ci (complement :integration)}}}
  :global-vars {*warn-on-reflection* true})
