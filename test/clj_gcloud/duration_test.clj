(ns clj-gcloud.duration-test
  (:require [clojure.test :refer :all]
            [clj-gcloud.duration :refer :all])
  (:import (org.threeten.bp Duration)))

(deftest ->duration-test
  (are [dsl duration]
    (= duration (->duration dsl))
    [5 :days] (Duration/ofDays 5)
    [10 :hours] (Duration/ofHours 10)
    [30 :minutes] (Duration/ofMinutes 30)
    [10 :seconds] (Duration/ofSeconds 10)
    [45 :millis] (Duration/ofMillis 45)
    [0 :nanos] (Duration/ofNanos 0)))
