(ns supernal.zmq
 (:import
  [org.zeromq ZMQ ZMQ$Curve]
  [java.nio.charset Charset] 
  )
 )

(def utf8 (Charset/forName "UTF-8"))

(defn gen-key []
  (let [keypair (.. ZMQ$Curve generateKeyPair)]
    {:public (.getBytes (.publicKey keypair) utf8)
     :secret  (.getBytes (.secretKey keypair) utf8) }
    )) 

(def endpoint "tcp://127.0.0.1:5003")

(defn reply-socket [context]
   (let [{:keys [secret public]} (gen-key) rep (.socket context ZMQ/REP) ]
      [public (doto rep (.setCurveServer true) (.setCurveSecretKey secret) (.bind endpoint))]))

(defn request-socket [context server-public]
   (let [{:keys [secret public]} (gen-key) req (.socket context ZMQ/REQ) ]
      (doto req 
        (.setCurvePublicKey public)
        (.setCurveSecretKey secret)
        (.setCurveServerKey server-public)
        (.connect endpoint))))

(defn test-curve-85 []
  
   (let [context (ZMQ/context 1) [public rep] (reply-socket context) req (request-socket context public)]
      (.send req "hello")     
      (println (.recvStr rep utf8))     
      (.close req) 
      (.close rep) 
      (.term context)
     ))

(test-curve-85)
