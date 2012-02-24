Aide en français pour ExtendCopy
v1.0

SOMMAIRE
------------------------
 * SYNOPSIS
 * DESCRIPTION
 * REMARQUES
 * DEPENDANCES
 * UTILISATION
 * OPTIONS
 * EXEMPLES
 * VOIR AUSSI
 * VALEURS DE RETOUR
 * INTEROPERABILITE
 * RETOURS PENDANT L’EXECUTION
 * DEVELOPPEMENT / CONTACT
 * LICENCE

SYNOPSIS
=======================
	java -jar extendcopy.jar l|v|c|list|copy|verify|lc|cv|lcv -from /source/dir -to /dest/dir [-exclude] [-nohidden] [-keep|-force] [-makehashtag] [-dryrun] [-notesthash] [projet_name]

DESCRIPTION
=======================

Permet de copier de lister le contenu d'un dossier, d'en faire la copie de son contenu, puis de vérifier que tous les éléments ont bien été copiés.
Les trois étapes (lister, copier, vérifier) sont indépendantes et peuvent être enchainés automatiquement ou séparément.

Extendcopy est différent de rsync et des autres utilitaires de copie car la fonction de vérification des fichiers copiés peut être faites même si les
fichiers d'origine ne sont plus accessibles.

Les fichiers de travail sont des fichiers textes dont les champs sont séparés par les tabulations, et les éléments par les sauts de ligne.
Il est facile de trier, filtrer, supprimer, scripter, analyser ces fichiers avec les outils en ligne de commande (grep, awk, cat...) ou avec un tableur
pour les injecter dans l'application.

La fonction de copie calcule de façon transparente et sans surcout de performance la somme de contrôle (hash/digest) du fichier copié,
et la fonction de vérification associe un fichier à sa taille et sa somme de contrôle : si les valeurs ne correspondent pas, alors le fichier est altéré.

REMARQUES
=======================

Les *dates* et les *droits* des fichiers ne sont pas répercutés sur les copies.

Les liens symboliques, les fichiers spéciaux (blocs...), les fichiers Icon? (icônes de dossiers sous OSX) sont systématiquement ignorés.

L'application est plus performante avec des petites listes de grands fichiers, qu'avec des grandes listes de petits fichiers.

Des tests on été faits avec des fichiers vidéos sur les copies de plusieurs To sur les milliers de fichiers.

Par défaut, le hash/digest calculé est du MD5. Vous pouvez changer cette valeur avec l'option `-Dextendcopy.hashname=XXXX` **Attention**, votre VM Java doit le prendre en charge.

L'application ne fait pas de conversion de code de caractères entre les noms de fichiers.

Attention aux "�" et autres "Ã©" si vous montez des systèmes de fichiers depuis plusieurs systèmes d'exploitation. 

Si vous interrompez l'application pendant les copies/vérifications, le traitement reprendra là où il c'est arreté (les fichiers restant à traiter sont dans les listes wait).

Il n'y a pas d'interface graphique, ni de mode interactif en ligne de commande.


DEPENDANCES
=======================

Tous système qui exécute une machine virtuelle Java 1.6

Fonctionnel sous OSX et sous Linux. Non testé mais normalement fonctionnel sous MS Windows.

Pas d'autres dépendances externe, et le code est Java natif.

Utilise le moteur interne de Java pour la manipulation de fichiers ainsi que pour le calcul des sommes de contrôle.

UTILISATION
=======================

La ligne de commande de base est

	java -jar extendcopy.jar ACTION -from SOURCE -to DESTINATION PROJET

Avec `ACTION`

 * `l` ou `list` Liste les fichiers contenu dans le dossier SOURCE vers un fichier texte
 * `c` ou `copy` Copie les fichiers listés dans le fichier texte créé par `list` depuis SOURCE vers DESTINATION
 * `v` ou `verify` Vérifie les fichiers listés dans le fichier texte créé par `copy`

Vous pouvez combiner les actions

 * `lc` Liste et copie
 * `cv` Copie et vérifie
 * `lcv` Liste, copie et vérifie

`SOURCE`

Le dossier d'où proviennent les fichiers.

Pour la copie, il respecte le fonctionnement de la commande `cp` : si présence du `/` (ou du `\\` sous Windows) a la fin du chemin,
alors seul le contenu du dossier sera copié. Sinon le dossier sera copié avec son contenu.

Le listage est toujours récursif (dossier et sous dossiers).

`DESTINATION`

Le dossier où iront vos fichiers.

`PROJET`

Nom de base des fichiers textes (les listes) que produira l'application.


Vous pouvez utiliser un fichier sh sous Linux/OSX pour simplifier le lancement, comme par exemple :
	
	#!/bin/sh
	java -jar extendcopy.jar -XX:MaxPermSize=256m -Xmx=512m $@

Vous pouvez utiliser `-XX:MaxPermSize=256m` et `-Xmx=512m` pour permettre à la VM Java d'utiliser jusqu'a 512 Mo de RAM (si vous avez des très longues listes).


OPTIONS
=======================

Organisation des options
-----------------------

	java -jar extendcopy.jar l|list -from /source/dir [-exclude] [-nohidden] [projet_name]
	java -jar extendcopy.jar c|copy -from /source/dir -to /dest/dir [-keep|-force] [-makehashtag] [-dryrun] [projet_name]
	java -jar extendcopy.jar v|verify [-notesthash] [projet_name]
	
	java -jar extendcopy.jar lc -from /source/dir -to /dest/dir [-exclude] [-nohidden] [-keep|-force] [-makehashtag] [-dryrun] [projet_name]
	java -jar extendcopy.jar cv -from /source/dir -to /dest/dir [-keep|-force] [-makehashtag] [-dryrun] [-notesthash] [projet_name]
	java -jar extendcopy.jar lvc -from /source/dir -to /dest/dir [-exclude] [-nohidden] [-keep|-force] [-makehashtag] [-dryrun] [-notesthash] [projet_name]

Options pour list, copy et verify
-----------------------

`-from /source/dir`
> Le dossier qui sera listé, puis copié.
> Suis la même règle que cp pour la présence de / (ou \ sous Windows) à la fin du chemin entré.

`-to /dest/dir`
> La destination où iront les fichiers.

`projet_name`
> Le nom de base des fichiers textes des listes. Permet la reprise du travail en cas d'arret manuel.

Les autres options sont bien évidement combinable.

Options pour list
-----------------------

`-exclude`
> On utilise les règles d'exclusion de fichiers pour ignorer les .DS_Store, desktop.ini...

`-nohidden`
> On ignore les fichiers cachés (qui commencent par un point, sous Linux/OSX, ou "caché" sous Windows)

Options pour copy
-----------------------

`-keep`
> Si le fichier existe déjà sur la destination, on n'y touche pas, même si la taille est différente

`-force`
> Si le fichier existe déjà sur la destination, on l'écrase de toute façon, même si la taille est identique.
> 
> **Ne pas utiliser -keep et -force en même temps**

`-makehashtag`
> crée un fichier side-car avec chaque fichier copié qui contient le hash calculé 
> 

`-dryrun`
> ne fait pas de copie, mais la simule afin de vérifier les chemins de destination des fichiers


Option pour verify
-----------------------

`-notesthash`
	ne fait pas de tests de calcul de hash lors de la vérification. La vérification ne portera plus que sur la présence et la taille.

Options supplémentaires
-----------------------

Par défaut l'application va automatiquement définir ses fichiers de liste par rapport à projet_name.

Vous pouvez forcer l'application a utiliser d'autres fichiers avec ces options.

*A tester préalablement : je n'ai pas essayé toutes les combinaisons qui pourraient avoir des effets de bords.*

`-set=filelist DIR`
> Liste des fichiers avec toutes les colonnes (en écriture, pour l'option list)

`-set=dirlist DIR`
> Liste des dossiers (en écriture, pour l'option list)

`-set=simplefilelist DIR`
> Liste des fichiers simple (en écriture, pour l'option list)

`-set=copylist DIR`
> Liste des fichiers à copier (en lecture, pour l'option copy)

`-set=copywaitlist DIR`
> Liste des fichiers qu'il reste à copier (en écriture, pour l'option copy)

`-set=copyerrlist DIR`
> Liste des erreurs pendant la copie (en écriture, pour l'option copy)

`-set=copydonelist DIR`
> Liste des fichiers copiés (en écriture, pour l'option copy)

`-set=verifylist DIR`
> Liste des fichiers à vérifier (en lecture, pour l'option verify)

`-set=verifywaitlist DIR`
> Liste des fichiers qu'il reste à vérifier (en écriture, pour l'option verify)

`-set=verifyerrlist DIR`
> Liste des erreurs pendant la vérification  (en écriture, pour l'option verify)

`-set=verifyoklist DIR`
> Liste des fichiers vérifiés et valides (en écriture, pour l'option verify)

`-set=verifynoklist DIR`
> Liste des fichiers vérifiés mais invalides (en écriture, pour l'option verify)

La logique de l'application implique que les valeurs de -set=filelist et de -set=copylist soient identiques, idem pour -set=copydonelist et -set=verifylist lorsque vous enchainez list, copy et verify.

*Vous pouvez bien sur outrepasser ces valeurs.*

EXEMPLES
=======================

	java -jar extendcopy.jar lvc -from /source/dir -to /dest -exclude -nohidden macopie

Copiera le dossier dir dans le dossier dest en ignorant les fichiers cachés et les fichiers spéciaux, et vérifiera après la copie que les fichiers sont bien copiés.

	java -jar extendcopy.jar list -from /source/dir -exclude -nohidden macopie2
	(modification éventuelle de la liste)
	java -jar extendcopy.jar copy -from /source/dir/ -to /dest macopie2
	(modification éventuelle de la liste de la copie)
	java -jar extendcopy.jar verify macopie2

Copiera **le contenu** du dossier dir dans le dossier dest, en ignorant les fichiers cachés et les fichiers spéciaux, et vérifira après la copie que les fichiers sont bien copiés.

VOIR AUSSI
=======================

Pour de la sauvegarde, de la synchronisation de dossier : `rsync`, `rdiffbackup`, unison

Avant cette application je devais utiliser conjointement : `find`, `sh`, `md5sum`, `diff`, `ls`...

VALEURS DE RETOUR
=======================

0. Ok
1. Pas de paramètres
2. Erreur d'execution

INTEROPERABILITE
=======================

Formats des fichiers de liste
-----------------------

Ce sont des fichiers texte avec les champs séparés par des tabulations et des sauts de ligne.
Le codage de caractères est celui de la VM Java.

Ces fichiers sont manipulables dans un tableur et avec les outils unix classiques.

Les lignes de *commentaire* commençant par "#" ou par ";" seront ignorées.

Fichier list
-----------------------

	/dossier/absolu/fichier.ext 9473953 2011/12/24 03:50:52 fichier.ext ext 1366847 2 sem 1 jrs 19:40:47

Avec

`/dossier/absolu/fichier.ext`
> Le chemin absolu du fichier
>
> **Ce champ est obligatoire pour utiliser cette liste en tant que copy**

`9473953`
> Sa taille en octets

`2011/12/24 03:50:52`
> Sa date de modification

`fichier.ext`
> Le nom du fichier seul (à utiliser pour des tris)

`ext`
> L'extension du fichier (à utiliser pour des tris)

`1366847`
> Le temps, en secondes, qu'il y a eu depuis la dernière écriture du fichier et maintenant.
> A utiliser pour trier facilement les fichiers anciens des plus récents.

`2 weeks 1 day 19:40:47`
> La valeur précédente convertie pour être lisible. Ici, cela fait un peu plus de 2 semaines que le fichier a été modifié.

Fichier copy
-----------------------

	/dossiersource/absolu/fichier.ext /dossierdestination/absolu/fichier.ext 9473953 89a52298083b739200cc3adbf1ba4ef1936906d7

**Tous les champs sont obligatoires pour utiliser cette liste en tant que verif.**
**Cependant, le premier champ peut être vide (juste une tabulation avant le champ suivant)**

Avec

`/dossiersource/absolu/fichier.ext`
> Le chemin absolu du fichier d'origine

`/dossierdestination/absolu/fichier.ext`
> Le chemin absolu du fichier copié

`9473953`
> La taille du fichier copié

`89a52298083b739200cc3adbf1ba4ef1936906d7`
> hash du fichier, en hexadécimal et minuscule, qui a été calculé pendant la copie. Ici, c'est du SHA1.

Fichier verif
-----------------------

Le fichier verif à la même structure que le fichier copy.

Le premier champ est vide.

En cas d'erreur, il contient le type d'erreur:

`@_MISSING_FILE_@`
> Le fichier est manquant : il existe dans la liste d'origine, mais il est introuvable dans le système de fichier.

`@_NOT_A_FILE_@`
> Le fichier attendu n'est pas un fichier : la référence existe dans la liste d'origine, mais ce n'est pas un fichier
> (cela peut être un dossier, un fichier bloc...)

`@_CANT_READ_@`
> Le fichier existe mais ne peut pas être lu.

`@_TOO_BIG_SIZE_@`
> Le fichier existe mais il est plus gros que sa référence dans la liste

`@_TOO_SMALL_SIZE_@`
> Le fichier existe mais il est plus petit que sa référence dans la liste

`@_EMPTY_@`
> Le fichier existe et est vide (0 octets). Dans ce cas précis, c'est normal car dans la liste d'origine il était déjà à 0 octets.
> Ce drapeau est là pour rappeler/signaler qu'un fichier vide est présent et été "copié".

`@_BAD_DIGEST_@`
> La vérification de l'intégrité du fichier est incorrect, cela veut dire que le hash initial de la liste et le hash calculé sont différents.

RETOURS PENDANT L'EXECUTION
=======================

L'application retourne le fichier listé, le fichier copié (avec sa taille et son hash) et le fichier vérifié pendant les opérations.

Si la copie ou la vérification prends du temps, un affichage de progression, par fichier, s'affiche.

	[...]
	00:02:50 135.5 Mbytes/s 110.8 Mbytes/s 30% [ETA 00:40:08]
	[...]
	
Avec

`00:02:50`
> Le temps depuis le début de la copie

`135.5 Mbytes/s`
> Débit instantané depuis le dernier pourcent.
>
> Si ces valeurs ne sont stables, c'est que le système de fichier souffre de problème de performance.

`110.8 Mbytes/s`
> Débit moyen depuis le début de la copie.
> Si cette valeur est très différence de la valeur précédente, c'est que le systeme de fichier souffre de problème de performance.

`30%`
> Le pourcentage de copie déjà réalisé.

`[ETA 00:40:08]`
> Le temps restant estimé. Ici 40 minutes.

L'affichage s'actualise d'une ligne maximum par pourcent.

A la fin des opérations, vous trouverez un résumé de ce qui à été fait, et le temps que cela à été pris.

DEVELOPPEMENT / CONTACT
=======================
	hdsdi3g
	http://hd3g.tv
	https://github.com/hdsdi3g

LICENCE
=======================

	This file is part of ExtendCopy.
  
	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU Lesser General Public License as published by
	the Free Software Foundation; either version 3 of the License, or
	any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU Lesser General Public License for more details.
	
	Copyright (C) hdsdi3g for hd3g.tv 2009-2012
