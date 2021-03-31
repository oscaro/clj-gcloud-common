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
  :dependencies [[org.clojure/clojure "1.10.3" :scope "provided"]
                 [com.google.cloud/google-cloud-core "1.94.4"]
                 [com.google.cloud/google-cloud-core-http "1.94.4"]
                 ;; The dependencies below were carefully added and excluded
                 ;; in order to remove all confusing warnings.
                 [com.google.code.gson/gson "2.8.6"]
                 [com.google.guava/guava "29.0-jre"]
                 [io.grpc/grpc-api "1.36.1"]
                 [io.grpc/grpc-core "1.36.1" :exclusions [com.google.errorprone/error_prone_annotations
                                                          io.grpc/grpc-api]]
                 [com.google.errorprone/error_prone_annotations "2.5.1"]]
  :profiles
  {:dev
   {:dependencies   [[com.google.cloud/google-cloud-bigquery "1.127.11"]
                     [com.google.cloud/google-cloud-pubsub "1.112.0" :exclusions [io.grpc/grpc-api
                                                                                  io.grpc/grpc-core
                                                                                  io.grpc/grpc-netty-shaded]]
                     [com.google.cloud/google-cloud-storage "1.113.14"]
                     [com.google.cloud/google-cloud-datastore "1.106.0" :exclusions [io.grpc/grpc-api
                                                                                     io.grpc/grpc-core]]
                     [org.clojure/tools.namespace "1.1.0"]]
    :source-paths   ["dev"]
    :resource-paths ["test-resources"]
    :test-selectors {:ci (complement :integration)}}}
  :global-vars {*warn-on-reflection* true})
