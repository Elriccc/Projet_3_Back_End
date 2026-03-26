# Installation
## Environnement
Le projet se lance à l'aide d'un fichier `docker-compose`.  
Il comporte le back-end, le front-end, la bdd, la bdd e2e, le vault, le reverse-proxy (traefik) et le script d'initialisation du vault.
## Configuration git
Une fois ce repository git clonée, il faut aussi télécharger celui du front-end.
Pour que celui-ci soit reconnu il faut l'indiquer dans la variable `FRONT_END_CONTEXT` du fichier `.env`

# Lancement
Le projet (back-end + front-end) se lance avec `docker compose --env-file .env up -d`
## Changement de la clé JWT
La clé JWT se trouve dans `secret/kv/datashare-backend` avec le nom `JWT_SECRET` dans le vault.
## Debug back-end
Port 5005 est ouvert dans le back-end pour debug JVM.  
Il permet le hotswap et l'activation des points d'arrêts.
## Rebuild du back-end
Pour mettre à jour le jar, lancer `mvn package` puis relancer le container.

# Utilisation
 - Front-end: http://localhost:4200
 - Vault: http://localhost:8200
 - Back-end: http://localhost:8081 (Documentation API: http://localhost:8081/swagger-ui)

## Commandes utiles
- `docker compose --env-file .env up -d` : Lance l'environnement
- `docker compose down` : Ferme l'environnement
- `docker inspect --format "{{json .State.Health }}" ${my_container}` : Health check du ${my_container}
- `docker container restart ${my_container}` : Relance ${my_container}