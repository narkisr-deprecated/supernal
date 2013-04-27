(ns supernal.events)


(def handlers 
  (agent 
    {:error (fn [args] (println args (Thread/currentThread)))
    :process (fn [f] (f))} 
    ))

(defn invoke-event [e dest args]
  (send dest (fn [m] ((e m) args) m)))


(invoke-event :error handlers {:foo 4})

#_(doseq [i (range 10)]
  (invoke-event :process handlers #(println "here"))
   
  ) 


