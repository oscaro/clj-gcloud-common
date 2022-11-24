(ns clj-gcloud.common-test
  (:require
   [clj-gcloud.coerce :refer [->clj]]
   [clj-gcloud.common :as sut]
   [clojure.edn :as edn]
   [clojure.test :refer [are deftest is testing]])
  (:import
   (com.google.cloud Service RetryOption)
   (com.google.cloud.bigquery BigQueryOptions BigQuery$QueryOption)
   (com.google.cloud.datastore DatastoreOptions)
   (com.google.cloud.pubsub.v1 SubscriptionAdminSettings)
   (com.google.cloud.storage StorageOptions)
   (org.threeten.bp Duration)))

(def test-creds "./test-resources/service-account.json")

(defn load-test-config
  []
  (edn/read-string (slurp "./test-resources/test-config.edn")))

(deftest ^:integration fixed-credentials-test
  (is (= (sut/mk-credentials test-creds)
         (.getCredentials (sut/fixed-credentials test-creds)))))

(deftest ^:integration get-project-test
  (let [test-project-id (:project-id (load-test-config))]
    (testing "retrieval of the project-id from a service account file"
      (is (= test-project-id
             (-> (SubscriptionAdminSettings/newBuilder)
                 (.setCredentialsProvider (sut/fixed-credentials test-creds))
                 .build
                 sut/get-project))))))

(deftest ^:integration build-service-test

  (testing "it should build a service of each type using the default settings"
    (are [builder]
      (let [test-config      (load-test-config)
            test-project-id  (:project-id test-config)
            test-svc-account (:svc-account test-config)
            ^Service service (sut/build-service builder {:project-id  test-project-id
                                                         :credentials test-creds})
            {:keys [project-id credentials retry-settings]} (->clj (.getOptions service))]
        (is (and (= test-project-id project-id)
                 (= test-svc-account credentials)
                 (= (->clj sut/default-retry-settings) retry-settings))))

      (BigQueryOptions/newBuilder)
      (DatastoreOptions/newBuilder)
      (StorageOptions/newBuilder)))

  (testing "retry settings can be supplied for greater fault tolerance"
    (let [opts     {:retry-settings
                    {:total-timeout          [10 :minutes]
                     :max-retry-delay        [60 :seconds]
                     :retry-delay-multiplier 1.5
                     :max-attempts           1000
                     :jittered?              false}}
          service  (sut/build-service (StorageOptions/newBuilder) opts)
          settings (bean (:retrySettings (bean (.getOptions service))))
          expected {:initialRetryDelay    (Duration/ofSeconds 1)
                    :jittered             false
                    :maxAttempts          1000
                    :maxRetryDelay        (Duration/ofMinutes 1)
                    :retryDelayMultiplier 1.5
                    :totalTimeout         (Duration/ofMinutes 10)}]
      (is (= expected (select-keys settings (keys expected)))))))

(deftest array-type-test
  (are [type expected]
    (is (= expected (sut/array-type type)))
    Byte/TYPE "[B"
    BigQuery$QueryOption "[Lcom.google.cloud.bigquery.BigQuery$QueryOption;"
    RetryOption "[Lcom.google.cloud.RetryOption;"))
