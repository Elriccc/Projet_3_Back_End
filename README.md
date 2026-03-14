# Installation
## Environnement
Le projet se lance à l'aide d'un fichier docker-compose qui est programmé pour un environnement de dev. Celui-ci comporte un container pour le back-end, la bdd, le vault, le front-end et le reverse-proxy.
## Configuration git
Une fois ce repository git clonée, il faut aussi télécharger celui du front-end puis en indiquer le chemin en remplissant la variable FRONT_END_CONTEXT du fichier .env

# Lancement
## Chargement de la clé JWT
Il faut lancer l'environnement une première fois avec la commande "docker-compose up", une fois cela fait il faudra se rendre sur l'ui du vault et saisir le token "root".
Puis il suffit d'ajouter la clé JWT dans le chemin "kv/datashare-backend" sous le nom de variable "JWT_SECRET".
Finalement en relançant l'environnement à l'aide de "docker-compose up" le backend se mettra en route en ayant la connaissance de la clé JWT.
## Debug back-end
Le port 5005 est ouvert dans le back et configuré pour le debug JVM. Cela permet entre autre le hotswap et l'activation des points d'arrêts quand on communique dessus.

# Utilisation
Les ports 4200, 8200 et 8081 du reverse proxy sont exposés à l'adresse localhost.
Ils permettent respectivement d'accéder au front, au vault et au back-end.
 - Front-end: http://localhost:4200
 - Vault: http://localhost:8200
 - Back-end: http://localhost:8081 (La documentation de l'api se trouve à l'adresse http://localhost:8081/swagger-ui)