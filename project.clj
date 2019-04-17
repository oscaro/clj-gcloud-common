(defproject com.oscaro/clj-gcloud-common "0.87-1.0-SNAPSHOT"
  :description "Common library for all google cloud clojure wrappers"
  :url "https://github.com/oscaro/clj-gcloud-common"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [cheshire "5.8.1"]
                 [com.google.cloud/google-cloud-core "1.69.0"]
                 [com.google.cloud/google-cloud-core-http "1.69.0"]
                 ;; The dependencies below were carefully added and excluded
                 ;; in order to remove all confusing warnings.
                 [com.google.guava/guava "27.1-jre"]
                 [io.grpc/grpc-core "1.19.0"]
                 [com.google.errorprone/error_prone_annotations "2.3.3"]]
  :profiles
  {:dev
   {:dependencies   [[com.google.cloud/google-cloud-bigquery "1.69.0" ]
                     [com.google.cloud/google-cloud-pubsub "1.69.0" :exclusions [io.grpc/grpc-core
                                                                                 io.grpc/grpc-netty-shaded]]
                     [com.google.cloud/google-cloud-storage "1.69.0"]
                     [com.google.cloud/google-cloud-datastore "1.69.0"]
                     [org.clojure/tools.namespace "0.2.11"]
                     [io.grpc/grpc-netty-shaded "1.19.0" :exclusions [io.grpc/grpc-core]]]
    :source-paths   ["dev"]
    :resource-paths ["test-resources"]}}
  :global-vars {*warn-on-reflection* true})
