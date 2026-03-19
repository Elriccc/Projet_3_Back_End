### Test de performance
Pour faire le test de performance j'ai utilisé k6.  
Celui-ci peut-être retrouvé à `/k6` à partir de la racine du projet  

Voilà les résultats:

     scenarios: (100.00%) 1 scenario, 10 max VUs, 1m0s max duration (incl. graceful stop):
              * default: 10 looping VUs for 30s (gracefulStop: 30s)

█ THRESHOLDS

    http_req_duration
    ✓ 'p(95)<500' p(95)=90.06ms


█ TOTAL RESULTS

    checks_total.......: 430     13.802094/s
    checks_succeeded...: 100.00% 430 out of 430
    checks_failed......: 0.00%   0 out of 430

    ✓ status is 201
    ✓ response time is acceptable

    HTTP
    http_req_duration..............: avg=31.49ms min=7.35ms med=21.03ms max=186.76ms p(90)=62.9ms p(95)=90.06ms
      { expected_response:true }...: avg=31.49ms min=7.35ms med=21.03ms max=186.76ms p(90)=62.9ms p(95)=90.06ms
    http_req_failed................: 0.00%  0 out of 215
    http_reqs......................: 215    6.901047/s

    EXECUTION
    iteration_duration.............: avg=1.42s   min=1.19s  med=1.33s   max=2.15s    p(90)=1.72s  p(95)=1.99s  
    iterations.....................: 215    6.901047/s
    vus............................: 5      min=5        max=10
    vus_max........................: 10     min=10       max=10

    NETWORK
    data_received..................: 103 kB 3.3 kB/s
    data_sent......................: 52 MB  1.7 MB/s

En testant 10 appels simultanés du endpoint pendant 30 secondes j'ai pu avoir 215 appels.  
Tous ont réussi, en moyenne la réponse était reçu en 1.42 secondes, en médiane en 1.33 secondes.  
Au maximum la réponse a été reçu en 2.15 secondes et au minimum 1.19 secondes.
En tout le serveur a reçu 62MB de données et a intégré tous les fichiers sans erreurs.

### Logs
Des logs sont envoyés dès qu'il y a une modification en base de données ou qu'un fichier est ajouté/supprimé