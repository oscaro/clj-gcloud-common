(ns clj-gcloud.dsl-test
  (:require
   [clj-gcloud.dsl :as sut]
   [clojure.test :refer [deftest is]]))

(deftest kw->field-name-test
  (is (= "tableId" (sut/kw->field-name :table-id))))

(deftest kw->enum-str-test
  (is (= "CREATE_IF_NEEDED" (sut/kw->enum-str :create-if-needed))))

(deftest dsl->google-json-map-test
  (is (= {"key"
          {"aKey"       "string"
           "bLongerKey" 1
           "type"       "THIS_IS_AN_ENUM"
           "anotherKey" {"nested" "VALUE"}}}
         (sut/dsl->google-json-map {:key
                                    {:a-key        "string"
                                     :b-longer-key 1
                                     :type         :this-is-an-enum
                                     :another-key  {:nested :value}}}))))
