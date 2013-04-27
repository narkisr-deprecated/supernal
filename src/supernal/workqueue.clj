(ns supernal.workqueue
  (:use
    [taoensso.timbre :only  (warn debug info error set-config!)])) 

(def agents (map #(agent %) (range 4)))

(defn err-handler-fn [ag ex]
  (error ex))

(defn register-error-handling []
  (doseq [a agents]
    (set-error-handler! a err-handler-fn)))

(defn queue [dest f]
  (try 
    (send dest (fn [a] (f) a))
    (catch Throwable e
      (debug "restarting agent due to" (.getMessage e)) 
      (when (= (.getMessage e) "Agent is failed, needs restart")
        (restart-agent dest 0) 
        (queue dest f)))))

;; (queue (second agents) (fn [] (println "alive")))
;; (queue (second agents) (fn [] (/ 1 0 )))

(defn execute [fns]
  (doseq [f fns dest (take (count fns) (cycle agents))]
    (queue dest f)))

(register-error-handling)

(set-config! [:shared-appender-config :spit-filename] "supernal.log")
(set-config! [:appenders :spit :enabled?] true)

#_(execute 
  (map  (fn [_] (fn [] (Thread/sleep 2000) (debug (Thread/currentThread)))) (range 100)))

