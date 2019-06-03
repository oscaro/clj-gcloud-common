(ns clj-gcloud.duration
  (:require [clj-gcloud.dsl :as d])
  (:import (org.threeten.bp Duration)
           (org.threeten.bp.temporal ChronoUnit)
           (java.util.concurrent TimeUnit)))

(def time-units (d/enums-as-map TimeUnit))

(def chrono-units (d/enums-as-map ChronoUnit))
(defn ->duration
  "Convenience method for converting a human friendly duration into a Duration.
  Allowed units are :hours, :minutes, :seconds, :millis and :nanos.

  ex. (->duration [10 :seconds])
   => #object[java.time.Duration 0x4d156026 \"PT10S\"]"
  [[amt unit]]
  (Duration/of amt (get chrono-units unit)))

; Helper methods for logging
(defmethod print-method Duration [^Duration d w]
  (print-method {:seconds (.getSeconds d) :nanos (.getNano d)} w))
