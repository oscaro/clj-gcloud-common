(ns clj-gcloud.common-test
  (:require [clojure.test :refer :all]
            [clojure.edn :as edn]
            [clj-gcloud.common :refer :all]
            [clj-gcloud.coerce :refer [->clj]])
  (:import (com.google.cloud Service RetryOption)
           (com.google.cloud.bigquery BigQueryOptions BigQuery$QueryOption)
           (com.google.cloud.datastore DatastoreOptions)
           (com.google.cloud.storage StorageOptions)
           (java.util.concurrent TimeUnit)
           (com.google.cloud.pubsub.v1 SubscriptionAdminSettings)))


(def test-creds "./test-resources/service-account.json")

(let [config (edn/read-string (slurp "./test-resources/test-config.edn"))]
  (def test-project-id (:project-id config))
  (def test-svc-account (:svc-account config)))

(deftest build-service-test

  (testing "it should build a service of each type using the default settings"
    (are [builder]
      (let [^Service service (build-service builder {:project-id  test-project-id
                                                     :credentials test-creds})
            {:keys [project-id credentials retry-settings]} (->clj (.getOptions service))]
        (is (and (= test-project-id project-id)
                 (= test-svc-account credentials)
                 (= (->clj default-retry-settings) retry-settings))))

      (BigQueryOptions/newBuilder)
      (DatastoreOptions/newBuilder)
      (StorageOptions/newBuilder)))

  (testing "retry settings can be supplied for greater fault tolerance"
    (let [opts    {:retry-settings
                   {:total-timeout          (.toSeconds (TimeUnit/MINUTES) 10)
                    :max-retry-delay        60
                    :retry-delay-multiplier 1.5
                    :max-attempts           1000
                    :jittered               false}}
          service (build-service (StorageOptions/newBuilder) opts)
          {:keys [retry-settings]} (->> service .getOptions ->clj)]
      (is (= (assoc (:retry-settings opts) :initial-retry-delay 1)
             retry-settings)))))

(deftest fixed-credentials-test
  (is (= (mk-credentials test-creds)
         (.getCredentials (fixed-credentials test-creds)))))

(deftest get-project-test

  (testing "Default project"
    (is (= test-project-id (get-project))))

  (testing "Project located in service account"
    (is (= test-project-id
           (-> (SubscriptionAdminSettings/newBuilder)
               (.setCredentialsProvider (fixed-credentials test-creds))
               .build
               get-project)))))

(deftest array-type-test
  (are [type expected]
    (is (= expected (array-type type)))
    Byte/TYPE "[B"
    BigQuery$QueryOption "[Lcom.google.cloud.bigquery.BigQuery$QueryOption;"
    RetryOption "[Lcom.google.cloud.RetryOption;"))
