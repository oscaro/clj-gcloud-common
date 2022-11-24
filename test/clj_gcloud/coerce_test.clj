(ns clj-gcloud.coerce-test
  (:require
   [clj-gcloud.coerce :as sut]
   [clojure.test :refer [deftest is testing]])
  (:import
   (com.google.pubsub.v1 Topic)))

(sut/create-clj-coerce Topic [:name])

(deftest coercion-test
  (testing "It should map getters to keywords"
    (let [^Topic topic (-> (Topic/newBuilder) (.setName "test") .build)]
      (is (= (.getName topic) (:name (sut/->clj topic)))))))
