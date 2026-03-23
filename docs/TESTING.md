## Test
### Tester le back-end
#### Procédure de test
Les tests unitaires sont faits avec JUnit et Mockito et se lancent avec `mvn test` ou à chaque `mvn package`.  
Ils produisent un rapport de test se trouvant dans `/target/coverage`  
Les tests d'intégrations sont faits avec Testcontainers et se lancent avec `mvn verify`

### Tester le front-end
#### Procédure de test
Les tests unitaires et de composant sont faits avec Jest et se lancent avec la commande `npm test`.
Ils produisent un rapport de tests se trouvant dans `/coverage`  
Les tests e2e sont faits avec Cypress qui se lance avec `ngx cypress open`