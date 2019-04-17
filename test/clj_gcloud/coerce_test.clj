(ns clj-gcloud.coerce-test
  (:require [clojure.test :refer :all])
  (:require [clj-gcloud.coerce :refer :all])
  (:import (com.google.pubsub.v1 Topic)))

(create-clj-coerce Topic [:name])

(deftest coercion-test
  (testing "It should map getters to keywords"
    (let [^Topic topic (-> (Topic/newBuilder) (.setName "test") .build)]
      (is (= (.getName topic) (:name (->clj topic)))))))
