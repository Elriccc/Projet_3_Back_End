# Maintenance

## Images docker
Les images docker sont stockées dans le .env et ont leur numéro de version précisé afin d'éviter un changement de version automatique comprenant des breaking changes.
En cas de montée de version sur ces images il faudra bien vérifier la compatibilité de leur nouvelle version avec le reste de l'environnement docker avant de les modifier.

## Dépendances maven
Les versions des dépendances maven sont tous stockées en temps que variables dans la balise \<properties/>. Il faut toujours vérifier leur compatibilité avec la version actuelle de maven et des autres dépendances avant d'envisager une montée en version. 

## Dépendances node
Utiliser la commande npm dans le dossier du front-end. Pour faire une montée de version de node il faut faire correspondre la version node à la première ligne du Dockerfile du front-end avec celle actuellement installée.