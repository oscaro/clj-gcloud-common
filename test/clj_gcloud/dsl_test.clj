(ns clj-gcloud.dsl-test
  (:require [clojure.test :refer :all]
            [clj-gcloud.dsl :refer :all]))

(deftest kw->field-name-test
  (is (= "tableId" (kw->field-name :table-id))))

(deftest kw->enum-str-test
  (is (= "CREATE_IF_NEEDED" (kw->enum-str :create-if-needed))))

(deftest dsl->google-json-map-test
  (is (= {"key"
          {"aKey"       "string"
           "bLongerKey" 1
           "type"       "THIS_IS_AN_ENUM"
           "anotherKey" {"nested" "VALUE"}}}
         (dsl->google-json-map {:key
                                {:a-key        "string"
                                 :b-longer-key 1
                                 :type         :this-is-an-enum
                                 :another-key  {:nested :value}}}))))
