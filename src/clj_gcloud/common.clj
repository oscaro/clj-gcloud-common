(ns clj-gcloud.common
  (:require
   [clj-gcloud.duration :as d]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s])
  (:import
   (com.google.auth.oauth2 GoogleCredentials ImpersonatedCredentials ServiceAccountCredentials)
   (com.google.api.gax.core CredentialsProvider BackgroundResource)
   (com.google.api.gax.retrying RetrySettings)
   (com.google.api.gax.rpc ClientSettings)
   (com.google.cloud ServiceOptions Service RetryOption ServiceOptions$Builder)
   (io.grpc ManagedChannel)))

(defn option-mapper
  "Spec conformer helper for options"
  [f]
  (fn [opts]
    (->> opts
         (reduce #(conj %1 (f %2)) [])
         (filter identity))))

;; RetrySettings
(def default-retry-settings (ServiceOptions/getDefaultRetrySettings))

(defn ->RetryOption ^RetryOption
  [[k v]]
  (case k
    :total-timeout (RetryOption/totalTimeout (d/->duration v))
    :initial-retry-delay (RetryOption/initialRetryDelay (d/->duration v))
    :retry-delay-multiplier (RetryOption/retryDelayMultiplier v)
    :max-retry-delay (RetryOption/maxRetryDelay (d/->duration v))
    :max-attempts (RetryOption/maxAttempts v)
    :jittered? (RetryOption/jittered v)
    nil))

(s/def ::duration (s/tuple pos-int? d/chrono-units))
(s/def :retry.option/total-timeout ::duration)
(s/def :retry.option/initial-retry-delay ::duration)
(s/def :retry.option/retry-delay-multiplier double?)
(s/def :retry.option/max-retry-delay ::duration)
(s/def :retry.option/jittered? boolean?)
(s/def ::retry-settings
  (s/and (s/keys :opt-un [:retry.option/total-timeout
                          :retry.option/initial-retry-delay
                          :retry.option/retry-delay-multiplier
                          :retry.option/max-retry-delay
                          :retry.option/jittered?])
         (s/conformer (option-mapper ->RetryOption))))
(defn mk-retry-settings [settings]
  (if (s/valid? ::retry-settings settings)
    (->> settings
         (s/conform ::retry-settings)
         (into-array RetryOption)
         (RetryOption/mergeToSettings default-retry-settings))
    (throw (ex-info (s/explain-str ::retry-settings settings) settings))))

(declare mk-credentials*)

(defn mk-default-credentials
  ([]
   (mk-default-credentials {}))
  ([opts]
   (GoogleCredentials/getApplicationDefault)))

;; Credentials
(defn mk-service-account-credentials
  [{:keys [credentials]}]
  (ServiceAccountCredentials/fromStream (io/input-stream credentials)))

(defn mk-impersonated-credentials
  [{:keys [credentials target-principal delegates scopes] :as spec}]
  (let [builder (ImpersonatedCredentials/newBuilder)
        credentials (if credentials
                      (mk-service-account-credentials {:type :service-account-file :credentials credentials})
                      (mk-default-credentials {:type :application-default}))
        scopes (or
                 scopes
                 [])
        builder (-> builder
                    (.setSourceCredentials credentials)
                    (.setTargetPrincipal target-principal)
                    (.setScopes scopes))]
    (.build
      (cond-> builder
              delegates (.setDelegates delegates)))))


;; alias for backward compatibility
(defn mk-credentials
  [json-path]
  (mk-service-account-credentials {:credentials json-path}))

(defmulti mk-credentials*
  (fn [{:keys [type credentials target-principal]}]
    (or
      type
      (cond target-principal
              :impersonated
            credentials
              :service-account-file
            :else
              :application-default))))

(defmethod mk-credentials* :default
  [opts]
  (mk-default-credentials opts))


(defmethod mk-credentials* :service-account-file
  [opts]
  (mk-service-account-credentials opts))

(defmethod mk-credentials* :impersonated
  [opts]
  (mk-impersonated-credentials opts))


(defn fixed-credentials
  "Returns a credentials provider which will always returns the
  service account credentials located at the path"
  ^CredentialsProvider
  [path]
  (let [credentials (mk-credentials path)]
    (reify CredentialsProvider
      (getCredentials [_] credentials))))

(defn mk-credentials-provider
  "Creates a new credentials provider"
  [creds]
  (cond
    (instance? CredentialsProvider creds) creds
    (string? creds) (fixed-credentials creds)
    :else nil))

(def default-project (ServiceOptions/getDefaultProjectId))

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

;; Misc
(s/def :service.options/project-id string?)
(s/def :service.options/credentials string?)
(s/def :service.options/retry-settings ::retry-settings)
(s/def ::service-options (s/keys :opt-un [:service.options/project-id
                                          :service.options/credentials
                                          :service.options/retry-settings
                                          :service.options/target-principal
                                          :service.options/delegates
                                          :service.options/scopes]))
(defn build-service
  ^Service
  [^ServiceOptions$Builder builder {:keys [project-id credentials target-principal retry-settings] :as opts}]
  (if (or (nil? opts) (s/valid? ::service-options opts))
    (let [builder (cond-> builder
                    project-id (.setProjectId project-id)
                    (or credentials target-principal) (.setCredentials (mk-credentials* opts))
                    retry-settings (.setRetrySettings (mk-retry-settings retry-settings)))]
      (.getService ^ServiceOptions (.build builder)))
    (throw
      (ex-info (with-out-str (s/explain ::service-options opts)) opts))))

(defn array-type
  "Return a string representing the type of an array with dimensions and a type.
  For primitives, use a type like Integer/TYPE.
  Useful for type hints of the form: ^#=(array-type String) my-str-array"
  ([type]
   (array-type type 1))
  ([type dims]
   (let [type (if (symbol? type) (eval type) type)]
     (-> (apply make-array type (repeat dims 0)) class .getName))))

; Shutdown
(def default-termination-timeout [30 :seconds])
(defprotocol Shutdown
  "Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately cancelled.
  If a timeout/unit is specified, it will wait for the resource to become terminated,
  giving up if the timeout is reached."
  (shutdown! [this] [this timeout unit]))

(extend-type ManagedChannel
  Shutdown
  (shutdown!
    ([this] (apply shutdown! this default-termination-timeout))
    ([this timeout unit]
     (doto this
       (.shutdown)
       (.awaitTermination timeout (get d/time-units unit))))))

(extend-type BackgroundResource
  Shutdown
  (shutdown!
    ([this] (apply shutdown! this default-termination-timeout))
    ([this timeout unit]
     (doto this
       (.shutdown)
       (.awaitTermination timeout (get d/time-units unit))))))

; Helper methods for logging
(defmethod print-method CredentialsProvider [^CredentialsProvider cp w]
  (print-method {:creds (.getCredentials cp)} w))
(defmethod print-method RetrySettings [^RetrySettings rs w]
  (print-method
    {:total-timeout          (.getTotalTimeout rs)
     :initial-retry-delay    (.getInitialRetryDelay rs)
     :retry-delay-multiplier (.getRetryDelayMultiplier rs)
     :max-retry-delay        (.getMaxRetryDelay rs)
     :max-attempts           (.getMaxAttempts rs)
     :jittered?              (.isJittered rs)
     :initial-rpc-timeout    (.getInitialRpcTimeout rs)
     :rpc-timeout-multiplier (.getRpcTimeoutMultiplier rs)
     :max-rpc-timeout        (.getMaxRpcTimeout rs)}
    w))
