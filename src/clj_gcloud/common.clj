(ns clj-gcloud.common
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s])
  (:import (java.lang.reflect Method)
           (com.google.auth.oauth2 ServiceAccountCredentials)
           (com.google.api.gax.core CredentialsProvider)
           (com.google.cloud ServiceOptions Service RetryOption ServiceOptions$Builder)
           (com.google.api.gax.rpc ClientSettings)
           (org.threeten.bp Duration)))

(defn option-mapper [f]
  "Spec conformer helper for options"
  (fn [opts]
    (->> opts
         (reduce #(conj %1 (f %2)) [])
         (filter identity))))

;; RetrySettings
(def default-retry-settings (ServiceOptions/getDefaultRetrySettings))

(defn ^RetryOption ->RetryOption [[k v]]
  (case k
    :total-timeout (RetryOption/totalTimeout (Duration/ofSeconds v))
    :initial-retry-delay (RetryOption/initialRetryDelay (Duration/ofSeconds v))
    :retry-delay-multiplier (RetryOption/retryDelayMultiplier v)
    :max-retry-delay (RetryOption/maxRetryDelay (Duration/ofSeconds v))
    :max-attempts (RetryOption/maxAttempts v)
    :jittered (RetryOption/jittered v)
    nil))

(s/def ::retry-settings
  (s/and (s/keys :opt-un [:retry.option/total-timeout
                          :retry.option/initial-retry-delay
                          :retry.option/retry-delay-multiplier
                          :retry.option/max-retry-delay
                          :retry.option/jittered])
         (s/conformer (option-mapper ->RetryOption))))
(s/def :retry.option/total-timeout pos-int?)
(s/def :retry.option/initial-retry-delay pos-int?)
(s/def :retry.option/retry-delay-multiplier double?)
(s/def :retry.option/max-retry-delay pos-int?)
(s/def :retry.option/jittered boolean?)

(s/def :service.options/project-id string?)
(s/def :service.options/credentials string?)
(s/def :service.options/retry-settings ::retry-settings)
(s/def ::service-options (s/keys :opt-un [:service.options/project-id
                                          :service.options/credentials
                                          :service.options/retry-settings]))

(defn mk-retry-settings [settings]
  (->> settings
       (s/conform ::retry-settings)
       (into-array RetryOption)
       (RetryOption/mergeToSettings default-retry-settings)))

(defn mk-credentials
  [json-path]
  (ServiceAccountCredentials/fromStream (io/input-stream json-path)))

(defn build-service
  ^Service
  [^ServiceOptions$Builder builder {:keys [project-id credentials retry-settings] :as opts}]
  (if (or (nil? opts) (s/valid? ::service-options opts))
    (let [builder (cond-> builder
                    project-id (.setProjectId project-id)
                    credentials (.setCredentials (mk-credentials credentials))
                    retry-settings (.setRetrySettings (mk-retry-settings retry-settings)))]
      (.getService ^ServiceOptions (.build builder)))
    (throw
      (ex-info (with-out-str (s/explain ::service-options opts)) opts))))

(defn fixed-credentials
  "Returns a credentials provider which will always returns the
  service account credentials located at the path"
  ^CredentialsProvider
  [path]
  (let [credentials (mk-credentials path)]
    (reify CredentialsProvider
      (getCredentials [_] credentials))))

(def ^:private default-project (ServiceOptions/getDefaultProjectId))

(defn get-project
  "Returns the project id using the credentials located in the client settings.
  If not a service account, it returns the first available project id among the following sources:
  - The project ID specified by the GOOGLE_CLOUD_PROJECT environment variable;
  - The App Engine project ID;
  - The project ID specified in the JSON credentials file pointed by the
  GOOGLE_APPLICATION_CREDENTIALS environment variable;
  - The Google Cloud SDK project ID;
  - The Compute Engine project ID"
  ^String
  ([]
   default-project)
  ([^ClientSettings settings]
   (let [credentials (-> settings .getCredentialsProvider .getCredentials)]
     (if (instance? ServiceAccountCredentials credentials)
       (let [^ServiceAccountCredentials svc-accnt credentials]
         (.getProjectId svc-accnt))
       default-project))))

(defn array-type
  "Return a string representing the type of an array with dimensions and a type.
  For primitives, use a type like Integer/TYPE.
  Useful for type hints of the form: ^#=(array-type String) my-str-array"
  ([type]
   (array-type type 1))
  ([type dims]
   (let [type (if (symbol? type) (eval type) type)]
     (-> (apply make-array type (repeat dims 0)) class .getName))))
