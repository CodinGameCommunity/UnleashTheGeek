<!-- LEAGUES level1 level2 -->
<div id="statement_back" class="statement_back" style="display: none"></div>
<div class="statement-body">
  <!-- LEAGUE ALERT -->
  <div style="color: #7cc576; 
    background-color: rgba(124, 197, 118,.1);
    padding: 20px;
    margin-right: 15px;
    margin-left: 15px;
    margin-bottom: 10px;
    text-align: left;">
    <div style="text-align: center; margin-bottom: 6px">
      <img src="//cdn.codingame.com/smash-the-code/statement/league_wood_04.png" />
    </div>
    <p style="text-align: center; font-weight: 700; margin-bottom: 6px;">
      Ce challenge se déroule en <b>ligues</b>.
    </p>
    <span class="statement-league-alert-content">
      Pour ce challenge, plusieurs ligues pour le même jeu seront disponibles. Quand vous aurez prouvé votre valeur
      contre le premier Boss, vous accéderez à la ligue supérieure et débloquerez de nouveaux adversaires.
    </span>
  </div>
  <!-- GOAL -->
  <div class="statement-section statement-goal">
    <h2>
      <span class="icon icon-goal">&nbsp;</span>
      <span>Objectif</span>
    </h2>
    <div class="statement-goal-content">
      <p>L'<strong>Amadeusium</strong> est un cristal rare et précieux, que l'on trouve sous la surface de certaines
        planètes inhospitalières. Vous devez contrôler les robots de votre société minière, présents à la surface d'une
        de ces planètes pour extraire le plus possible de cristal. Cependant, vous n'êtes pas les seuls sur le coup !
      </p>
      <span>Livrez plus de cristaux d'Amadeusium que la société minière adverse !</span>
    </div>
  </div>
  <!-- RULES -->
  <div class="statement-section statement-rules">
    <h2>
      <span class="icon icon-rules">&nbsp;</span>
      <span>Règles</span>
    </h2>

    <div class="statement-rules-content">
      <p>Chaque joueur contrôle une équipe de <strong>robots</strong>.
        Les deux équipes démarrent au même endroit sur la zone de jeu, au <strong>quartier général</strong>. Les robots
        peuvent faire chacune des actions suivantes : se déplacer, forer la surface, miner un filon d'Amadeusium, mettre
        en place un <strong>radar</strong> pour détecter des filons, mettre en place un <strong>piège électromagnétique
          (EMP)</strong> pour saboter les robots adverses.
      </p>

      <br>
      <p><b>La grille de jeu</b></p>
      <br>

      <p>Le jeu est joué sur une grille de <const>30</const> cases en largeur par <const>15</const> en hauteur. Les
        coordonnées
        <const>x=0, y=0</const>
        correspondent à la case en haut à gauche. </p>

      <p>La première colonne à gauche de la grille de jeu correspond au <strong>quartier général</strong>. C'est là que
        le cristal d'<strong>Amadeusium</strong> doit être amené une fois extrait et que les objets sont demandés.</p>

      <p>Certaines cases cachent des filons d'<strong>Amadeusium</strong>. Par défaut, les filons ne sont pas visibles
        des joueurs. On ne peut pas trouver de filon au <strong>quartier général</strong>.</p>

      <p> Les <strong>robots</strong> peuvent forer la surface et creuser un <strong>trou</strong> sur n'importe quelle
        case de la grille de jeu (hors <strong>quartier général</strong>). Les <strong>trous</strong> sont visibles des
        2 joueurs et n'empêchent pas le mouvement des <strong>robots</strong>.</p>

      <br>
      <p><b>Les robots</b></p>
      <br>

      <p>Chaque robot ne peut transporter qu' <const>1</const> seul <strong>objet</strong> dans son inventaire.</p>

      <p>Un robot peut faire les actions suivantes :</p>

      <ul>
        <li>
          demander un objet au quartier général avec la commande <action>REQUEST</action>.
        </li>

        <li>
          se déplacer avec la commande <action>MOVE</action>.
        </li>
        <li>
          utiliser la commande <action>DIG</action> pour creuser un <strong>trou</strong> et interagir avec. Voici, dans
          l'ordre, ses effets :

          <ol style="padding-top: 0; padding-bottom: 0;">
            <li>Si la case ne contient pas déjà un trou, un nouveau trou est creusé.</li>

            <li>Si le robot contient un objet, l'objet est enterré dans le trou (et retiré de l'inventaire du robot).
            </li>

            <li>Si la case contient un filon d'Amadeusium (et qu'un cristal n'a pas été enterré à l'étape 2), un cristal
              est extrait du filon et ajouté à l'inventaire du robot.</li>
          </ol>

        </li>
        <li>
          ne rien faire avec la commande <action>WAIT</action>.
        </li>
      </ul>

      <p>Précisions :</p>
      <ul>
        <li>Chaque robot ne peut creuser que sur la case qu'il occupe ou les cases adjacentes à sa position. Chaque case
          a <const>4</const>
          cases adjacentes : en haut, à droite, en bas et à gauche.</li>

        <li>Si un robot possède un cristal dans son inventaire en arrivant au quartier général, le cristal est
          automatiquement livré et le joueur marque <const>1</const> point.</li>

        <li>Plusieurs robots peuvent occuper une même case.</li>

        <li>Les robots ne peuvent pas quitter la grille de jeu.</li>

        <li>Les inventaires des robots sont invisibles des joueurs adverses. </li>

      </ul>

      <p><b>Les objets</b></p>
      <br>

      <p>Un <strong>cristal d'Amadeusium</strong> est considéré comme un objet et doit être livré au quartier général
        pour marquer <const>1</const> point.</p>

      <p>Au quartier général, un robot peut demander l'un des 2 <strong>objets</strong> suivants : un
        <strong>radar</strong> avec la commande <action>
          RADAR</action> ou un <strong>piège EMP</strong> avec la commande
        <action>TRAP</action>.</p>

      <p>Si un objet est livré suite à une demande d'un robot, cet objet sera de nouveau disponible pour la même équipe
        de robot après <const>5</const> tours.</p>

      <p>Un <strong>piège EMP</strong> enterrée dans un <strong>trou</strong> ne se déclenche que si un robot utilise la
        commande <action>DIG</action> sur ce même trou. L'impulsion électromagnétique qui s'en suit détruit tous les
        robots sur la case correspondante et les <const>4</const> cases adjacentes. N'importe quel autre
        <strong>piège EMP</strong> se déclenche à son tour si il se trouve sur une des cases touchées par l'impulsion,
        entraînant une réaction en chaîne.</p>
      <!-- TODO: Add an illustration? -->

      <p>Un <strong>radar</strong> enterré dans un <strong>trou</strong> permet de détecter les filons d'Amadeusium et
        de connaître la quantité de cristaux disponibles dans chaque, dans un rayon de <const>4</const> cases, pour
        l'équipe qui l'a enterré.
        <!-- TODO: Add an illustration? -->
        Si un robot adverse utilise la commande <action>DIG</action> sur le trou contenant l'un de vos radars, le radar
        est détruit.
      </p>

      <br>
      <p><b id="actionsorderforoneturn">Ordre des actions pour un tour de jeu</b></p>

      <ol>
        <li>Si des commandes <action>DIG</action> déclenchent des <strong>piège EMP</strong>, ils sont détonés.</li>

        <li>
          Les autres commandes <action>DIG</action> sont résolues.
        </li>

        <li>
          Les commandes <action>REQUEST</action> sont résolues.
        </li>

        <li>
          Les temps de recharge des objets sont incrémentés.</li>

        <li>
          Les commandes <action>MOVE</action> et <action>WAIT</action> sont résolues.
        </li>

        <li>Les <strong>cristaux d'Amadeusium</strong> sont livrés au quartier général.</li>

        <li>
          Il n'est pas nécessaire de détecter un filon d'Amadeusium pour pouvoir extraire un cristal.</li>

      </ol>
    </div>
    <!-- Victory conditions -->
    <div class="statement-victory-conditions">
      <div class="icon victory"></div>
      <div class="blk">
        <div class="title">Conditions de victoire</div>
        <div class="text">
          <ul style="padding-top:0; padding-bottom: 0;">
            <li>Après <code>200</code> tours, votre équipe de robot a marqué le plus de points.</li>
            <li>Vous avez marqué plus de points que votre adversaire, et tous ses robots ont été détruits.</li>
          </ul>
        </div>
      </div>
    </div>
    <!-- Lose conditions -->
    <div class="statement-lose-conditions">
      <div class="icon lose"></div>
      <div class="blk">
        <div class="title">Conditions de défaite</div>
        <div class="text">
          <ul style="padding-top:0; padding-bottom: 0;">
            <li>Votre programme ne retourne pas de commande valide dans le temps imparti pour chaque robot de votre
              équipe, y compris ceux qui sont détruits.</li>
          </ul>
        </div>
      </div>
    </div>
  </div>


  <!-- EXPERT RULES -->
  <div class="statement-section statement-expertrules">
    <h2>
      <span class="icon icon-expertrules">&nbsp;</span>
      <span>Détails techniques</span>
    </h2>
    <div class="statement-expert-rules-content">
      <ul style="padding-left: 20px;padding-bottom: 0">
        <li>Un robot peut enterrer un cristal dans un trou. Si la case ne contenait pas déjà un filon, alors un nouveau
          filon est créé, détectable par les radars.</li>

        <li>Chaque robot, radar, et piège EMP possède un unique identifiant.</li>

        <li>Recevoir un objet du quartier général détruira n'importe quel objet que le robot a déjà dans son inventaire.
        </li>

        <li>Quand plusieurs robots de la même équipe demandent le même objet, les robots sans objets auront la priorité.
        </li>

        <li>Les pièges EMP n'ont pas d'effet sur les radars et les filons.</li>

        <li>Si un robot transportant un objet est détruit, l'objet est détruit.</li>
      </ul>
      <p>Vous pouvez voir le code source de ce jeu sur <a rel="nofollow" target="_blank"
          href="https://github.com/CodinGameCommunity/UnleashTheGeek">ce repo GitHub</a>.</p>
    </div>
  </div>

  <!-- PROTOCOL -->
  <div class="statement-section statement-protocol">
    <h2>
      <span class="icon icon-protocol">&nbsp;</span>
      <span>Données d'entrée</span>
    </h2>
    <!-- Protocol block -->
    <div class="blk">
      <div class="title">Entrée pour le premier tour</div>
      <div class="text">
        <span class="statement-lineno">Ligne 1:</span> deux entiers
        <var>width</var> et
        <var>height</var> pour la taille de la grille.
        La première colonne à gauche de la grille de jeu correspond au <strong>quartier général</strong>.
        <br>
      </div>
    </div>
    <!-- Protocol block -->
    <div class="blk">
      <div class="title">Entrée pour un tour de jeu</div>
      <div class="text">
        <span class="statement-lineno">Première ligne :</span> Deux entiers
        <ul style="padding-left: 20px;padding-top:0;padding-bottom:0px">
          <li>
            <var>myScore</var> pour le score du joueur.
          </li>
          <li>
            <var>enemyScore</var> pour le score de son adversaire.
          </li>
        </ul>
        <span class="statement-lineno">Prochaines <var>height</var> lignes :</span> <var>width</var> * <const>2
        </const> variables <var>ore</var> et <var>hole</var>.<br>
        <var>ore</var> vaut : <ul style="padding-bottom: 0; padding-top:0">
          <li>
            <const>?</const> si la case n'est pas à portée d'un radar que le joueur contrôle.
          </li>
          <li>Un entier positif, pour la quantité de cristaux dans le filon. <const>0</const>, si la case ne cache pas
            de filon.</li>
        </ul>
        <var>hole</var> vaut : <ul style="padding-bottom: 0; padding-top:0">
          <li>
            <const>1</const> s'il y a un trou sur la case.
          </li>
          <li>
            <const>0</const> sinon.
          </li>
        </ul>
        <br><br>
        <span class="statement-lineno">Prochaine ligne :</span> 4 entiers <br>
        <ul style="padding-left: 20px;padding-top:0;padding-bottom:0px">
          <li><var>entityCount</var> pour la quantité d'entités : robots, radars et pièges EMP visibles au joueur.
          </li>
          <li>
            <var>radarCooldown</var> pour le nombre de tours jusqu'à ce qu'un nouveau radar soit disponible au quartier
            général.
          </li>
          <li>
            <var>trapCooldown</var> pour le nombre de tours jusqu'à ce qu'un nouveau piège EMP soit disponible au
            quartier général.
          </li>
        </ul>
        <br><br>
        <span class="statement-lineno">Prochaines
          <var>entityCount</var> lignes :</span> 5 entiers pour décrire chaque entité
        <ul style="padding-left: 20px;padding-top:0;padding-bottom:0px">
          <li>
            <var>id</var>: son unique identifiant.
          </li>
          <li>
            <var>type</var>: son type.
            <ul style="padding-left: 20px;padding-top:0;padding-bottom:0px">
              <li>
                <const>0</const> pour l'un de vos robots
              </li>
              <li>
                <const>1</const> pour un robot adverse
              </li>
              <li>
                <const>2</const> pour un de vos radars enterrés
              </li>
              <li>
                <const>3</const> pour une de vos pièges enterrées
              </li>
            </ul>
          </li>
          <li>
            <var>x</var> &
            <var>y</var>: les coordonnées de l'entité.<br> Si l'entité est un robot détruit, ses coordonnées <var>x
              y</var>valent
            <const>-1 -1</const>
          </li>
          <li>
            <var>item</var>: si l'entité est un robot, l'objet présent dans son inventaire.
            <ul>
              <li>
                <const>-1</const> si l'inventaire est vide
              </li>
              <li>
                <const>2</const> pour un radar
              </li>
              <li>
                <const>3</const> pour un piège EMP
              </li>
              <li>
                <const>4</const> pour un cristal d'Amadeusium
              </li>
            </ul>
          </li>
        </ul>

      </div>
    </div>
    <!-- Protocol block -->
    <div class="blk">
      <div class="title">Sortie pour un tour de jeu</div>
      <div class="text">
        <span class="statement-lineno">
          <const>5</const> lignes,
        </span> pour chaque robot de l'équipe, suivant l'ordre de leurs identifiants, une des commandes suivantes :
        <ul style="padding-left: 20px;padding-top: 0">
          <li>
            <action>WAIT</action>: le robot ne fait rien.
          </li>
          <li>
            <action>MOVE x y</action>: le robot se déplace de 4 cases vers la case <var>(x, y)</var>.
          </li>
          <li>
            <action>DIG x y</action>: le robot tente d'enterrer un objet s'il en transporte, et d'extraire un cristal
            d'un possible filon. <b> Si la case n'est pas adjacente, le robot effectuera une commande <action>MOVE
              </action> pour se rapprocher de la destination à la place.</b>
          </li>
          <li>
            <action>REQUEST</action> suivi de <action>RADAR</action> ou <action>TRAP</action>: le robot tente de
            récupérer un objet du quartier général. Si le robot n'est pas sur une case du quartier général, il
            effectuera la commande <action>MOVE 0 y</action> où y est l'ordonnée du robot.
          </li>
        </ul>
        Il est possible d'ajouter un message à chaque commande (séparé d'un espace) pour qu'il soit affiché au-dessus du
        robot.
        <br><br> Exemples: <ul style="padding-top:0; padding-bottom: 0;">
          <li>
            <action>MOVE 8 4</action>
          </li>
          <li>
            <action>DIG 7 10 let's go mining</action>
          </li>
        </ul>

        Il est nécessaire d'envoyer une commande pour les robots détruits. Elle sera ignorée.</div>
    </div>
    <div class="blk">
      <div class="title">Contraintes</div>
      <div class="text">
        Temps de réponse pour un tour ≤
        <const>50</const>ms
        <br>Temps de réponse pour le premier tour ≤
        <const>1000</const>ms
      </div>
    </div>
    <!-- STORY -->
    <div class="statement-story-background">
      <div class="statement-story-cover"
        style="background-size: cover; background-image: url(https://static.codingame.com/servlet/fileservlet?id=31275947693156)">
        <div class="statement-story" style="min-height: 300px; position: relative">
          <h2><span style="color: #b3b9ad">Pour Démarrer</span></h2>
          <div class="story-text">
            Pourquoi ne pas se lancer dans la bataille avec l'un ces nos <b>IA Starters</b>, fourni par l'équipe Unleash
            The Geek&nbsp;:
            <ul>
              <li>
                C++
                <a style="color: #f2bb13; border-bottom: 1px dotted #f2bb13;"
                  href="https://gist.github.com/CGjupoulton/a4053faf94bc3fe2b4db3e71d6253ac5">https://gist.github.com/CGjupoulton/a4053faf94bc3fe2b4db3e71d6253ac5</a>
              </li>
              <li>
                C#
                <a style="color: #f2bb13; border-bottom: 1px dotted #f2bb13;"
                  href="https://gist.github.com/CGjupoulton/557d990b31b79538f6e44709efd66968">https://gist.github.com/CGjupoulton/557d990b31b79538f6e44709efd66968</a>
              </li>
              <li>
                Java
                <a style="color: #f2bb13; border-bottom: 1px dotted #f2bb13;"
                  href="https://gist.github.com/CGjupoulton/f273031a12f552c145602582115f2b25">https://gist.github.com/CGjupoulton/f273031a12f552c145602582115f2b25</a>
              </li>
              <li>
                JavaScript
                <a style="color: #f2bb13; border-bottom: 1px dotted #f2bb13;"
                  href="https://gist.github.com/CGjupoulton/d258d155c0dd443d16035c372bb58525">https://gist.github.com/CGjupoulton/d258d155c0dd443d16035c372bb58525</a>
              </li>
              <li>
                Python
                <a style="color: #f2bb13; border-bottom: 1px dotted #f2bb13;"
                  href="https://gist.github.com/CGjupoulton/e93512f74336aeef97a2c2a52b381e20">https://gist.github.com/CGjupoulton/e93512f74336aeef97a2c2a52b381e20</a>
              </li>
            </ul>
            <p>
              Vous pouvez les modifier selon votre style, ou les prendre comme exemple pour tout coder à partir de
              zero.
            </p>

            D'autres starters pourront être rendus disponibles pendant le contest <a target="_blank" rel="nofollow"
              href="https://gist.github.com/CGjupoulton/">ici</a>.

          </div>
        </div>
      </div>
    </div>
  </div>
</div>