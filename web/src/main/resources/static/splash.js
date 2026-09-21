/* Bankomat — (c) 2026 Robert Andersson Kopler. Alla rattigheter forbehallna. */
// Uppstartsskärm för Bankomat 2.0 — "maskinen bootar" innan menyn syns.
//
// Samma mönster som systerprojektens splash (glaskort, statusrader som tickar in och tänds
// gröna, progress-stapel), men i den här maskinens språk: svart botten, fosforgrönt,
// monospace, och SAMMA sifferregn som redan faller bakom bankomaten. Regnet är inte
// dekoration som lagts dit två gånger — tecknen, färgen och takten är hämtade ur
// matrixRain() i fragments.html, så de två lagren ser ut att komma från samma maskin.
//
// TRE REGLER STYR NÄR DEN VISAS, och alla tre kom ur hur appen faktiskt används:
//   · bara på startsidan — appen navigerar mellan meny, kontohantering och översikt, och en
//     boot-sekvens vid varje sidbyte hade varit en spärr i stället för en inledning,
//   · en gång per FLIK (sessionStorage, inte localStorage) — bankomaten är en demo man går
//     tillbaka till; nästa besök samma dag ska se den igen, men inte vid varje knapptryck,
//   · ?splash=1 tvingar fram den, och window.bkReplaySplash() gör det från konsolen.
//
// Siffrorna på kontoraden kommer ur body:s data-attribut, som menysidan fyller ur registret.
// Saknas de visas raden UTAN tal i stället för med påhittade — samma regel som elbilsappens
// splash: hellre tyst än fel.
(function () {
  'use strict';

  var FORCE   = /[?&]splash=1/.test(location.search);
  var NYCKEL  = 'bk_splash_v1';
  var reduce  = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // Raderna. `ic` är ikonen, `an` dess rörelse (se CSS längre ned), `tag` pillret till höger.
  // ORDNINGEN AR MASKINENS EGEN UPPSTART, och den slutar dar besokaren tar over.
  // Splashen kor INNAN ett kort ar isatt, sa ingen rad far lata som om nagon redan
  // dragit ett: sakerhetsmodulen VANTAR pa en PIN, den har inte kontrollerat nagon,
  // och kortlasaren ligger sist for att lamna over till "satt i kort" pa maskinen.
  var ROWS = [
    { ic: '⚙️', t: 'Spring Boot',        s: '',                           tag: 'STARTAD', an: 'kugge',    kind: 'boot' },
    { ic: '🗄️', t: 'Databas',            s: '',                           tag: 'ONLINE',  an: 'arkiv',    kind: 'db' },
    { ic: '🏦', t: 'Weras Betalservice', s: 'betaltjänsten ansluten',     tag: 'ONLINE',  an: 'bank' },
    { ic: '📊', t: 'Kontoregister',      s: '',                           tag: 'LIVE',    an: 'stapel',   kind: 'konton' },
    { ic: '💵', t: 'Sedelkassett',       s: 'uttagsfacket laddat',                        an: 'sedel' },
    { ic: '🖨️', t: 'Kvittoskrivare',     s: 'remsa laddad',                               an: 'skrivare' },
    { ic: '🔐', t: 'Säkerhetsmodul',     s: 'väntar på PIN-kod',          tag: 'REDO',    an: 'las' },
    { ic: '💳', t: 'Kortläsare',         s: 'chip &amp; magnetremsa · sätt i kort',       an: 'kort' }
  ];

  var BOOT = [
    'WERAS BETALSERVICE AB — TERMINAL 02',
    'startar Spring Boot…',
    'ansluter till registret…'
  ];

  /**
   * Tomt register och OLASBART register ar tva olika nej, och de far inte bli samma rad.
   * Saknas attributen kom controllern aldrig fram till talen (undantag, loggat pa servern)
   * - da sags bara att anslutningen ar uppe. Star det 0 ar registret last och tomt, vilket
   * ar sant och nagot helt annat. Hade bada gett samma text vore raden vardelos som vakt.
   */
  function konton() {
    var b = document.body;
    var k = b && b.getAttribute('data-konton');
    var p = b && b.getAttribute('data-kunder');
    if (!k || !p) return 'registret anslutet';
    if (k === '0' && p === '0') return 'registret läst · inga konton ännu';
    return '<b>' + k + '</b> konton · <b>' + p + '</b> kunder';
  }

  /**
   * Databasens namn kommer fran serverns anslutning (Systeminfo), inte fran en strang har.
   * Appen kor H2 lokalt och PostgreSQL i drift - en hardkodad rad hade varit fel i halva
   * livet, och kvar som sanning den dagen databasen byts.
   */
  function dbText() {
    var d = document.body && document.body.getAttribute('data-db');
    return d ? '<b>' + d + '</b> · ansluten' : 'anslutningen uppe';
  }

  function bootText() {
    var b = document.body && document.body.getAttribute('data-boot');
    var j = document.body && document.body.getAttribute('data-java');
    if (!b) return 'ramverket igang';
    return '<b>Spring Boot ' + b + '</b>' + (j ? ' · Java ' + j : '');
  }

  function subFor(r) {
    if (r.kind === 'konton') return konton();
    if (r.kind === 'db')     return dbText();
    if (r.kind === 'boot')   return bootText();
    return r.s;
  }

  function css() {
    if (document.getElementById('bk-splash-css')) return;
    var el = document.createElement('style');
    el.id = 'bk-splash-css';
    el.textContent = [
      '.bk-splash{position:fixed;inset:0;z-index:9999;background:#010301;',
        'display:flex;align-items:center;justify-content:center;padding:16px;',
        'font-family:"Share Tech Mono","Courier New",monospace;',
        'opacity:1;transition:opacity .45s ease;}',
      '.bk-splash.bk-ut{opacity:0;}',
      '.bk-rain{position:absolute;inset:0;width:100%;height:100%;}',
      // Kortet: samma glas som maskinens paneler, men utan ram runt ramen.
      '.bk-kort{position:relative;z-index:2;width:min(460px,100%);',
        'background:linear-gradient(170deg,rgba(10,26,12,.94),rgba(4,8,4,.97));',
        'border:1px solid rgba(0,255,127,.22);border-radius:14px;padding:22px 20px 18px;',
        'box-shadow:0 0 0 1px rgba(0,0,0,.6),0 24px 70px rgba(0,0,0,.9),0 0 60px rgba(0,255,100,.07);}',
      '.bk-rubrik{text-align:center;color:#00ff7f;font-size:1.15rem;letter-spacing:3px;',
        'text-shadow:0 0 12px rgba(0,255,127,.55);}',
      '.bk-under{text-align:center;color:#3a6a4a;font-size:.62rem;letter-spacing:2px;',
        'margin-top:3px;text-transform:uppercase;}',
      '.bk-boot{margin:13px 0 11px;color:#8ee8a8;font-size:.7rem;letter-spacing:1px;min-height:1em;}',
      '.bk-boot .pr{color:#00cc5a;margin-right:6px;}',
      '.bk-cur{display:inline-block;width:7px;height:.85em;background:#00ff7f;',
        'vertical-align:-1px;animation:bk-blink .85s step-end infinite;}',
      '@keyframes bk-blink{0%,100%{opacity:1;}50%{opacity:0;}}',
      '.bk-rader{display:flex;flex-direction:column;gap:5px;}',
      '.bk-rad{display:flex;align-items:center;gap:9px;padding:6px 9px;border-radius:7px;',
        'background:rgba(0,255,127,.04);border:1px solid rgba(0,255,127,.12);',
        'opacity:0;transform:translateY(7px);transition:opacity .3s ease,transform .3s ease,',
        'border-color .3s ease,background .3s ease;}',
      '.bk-rad.syns{opacity:1;transform:none;}',
      '.bk-rad.klar{border-color:rgba(0,255,127,.34);background:rgba(0,255,127,.09);}',
      // Ikonerna rör sig allihop — dämpad andning medan raden laddar, egen rörelse när den
      // tänds. Glöden ligger i drop-shadow och inte i text-shadow: emoji kan bytas mot en
      // <img> (WordPress gör det), och text-shadow biter inte på en bild.
      '.bk-ic{flex-shrink:0;width:19px;text-align:center;font-size:.95rem;display:inline-block;',
        'filter:grayscale(.8) brightness(.85);opacity:.75;',
        'transition:filter .45s ease,opacity .45s ease;',
        'animation:bk-vilar 3s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-vilar{0%,100%{transform:translateY(0) scale(.96);}',
        '50%{transform:translateY(-1.5px) scale(1);}}',
      '.bk-rad.klar .bk-ic{opacity:1;}',
      '.bk-rad.klar .bk-i-kugge{filter:drop-shadow(0 0 6px rgba(0,255,127,.6));',
        'animation:bk-kugge 3.4s linear var(--ikd,0s) infinite;}',
      '@keyframes bk-kugge{to{transform:rotate(360deg);}}',
      '.bk-rad.klar .bk-i-arkiv{filter:drop-shadow(0 0 6px rgba(125,211,252,.6));',
        'animation:bk-arkiv 2.6s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-arkiv{0%,100%{transform:translateX(0) scaleY(1);}',
        '40%{transform:translateX(2.5px) scaleY(.92);}70%{transform:translateX(-1px) scaleY(1.03);}}',
      '.bk-rad.klar .bk-i-bank{filter:drop-shadow(0 0 6px rgba(0,255,127,.6));',
        'animation:bk-bank 3s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-bank{0%,100%{transform:translateY(0) scale(1);}',
        '45%{transform:translateY(-2.5px) scale(1.07);}75%{transform:translateY(0) scale(.99);}}',
      '.bk-rad.klar .bk-i-kort{filter:drop-shadow(0 0 6px rgba(167,139,250,.6));',
        'animation:bk-kort 2.4s ease-in-out var(--ikd,0s) infinite;}',
      // Kortet dras igenom läsaren: in, paus, ut.
      '@keyframes bk-kort{0%{transform:translateX(-3px) rotate(-4deg);}',
        '35%{transform:translateX(3px) rotate(4deg);}60%{transform:translateX(1px) rotate(0);}',
        '100%{transform:translateX(-3px) rotate(-4deg);}}',
      '.bk-rad.klar .bk-i-las{filter:drop-shadow(0 0 6px rgba(250,204,21,.65));',
        'animation:bk-las 2.8s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-las{0%,100%{transform:rotate(-5deg) scale(1);}',
        '30%{transform:rotate(5deg) scale(1.08);}55%{transform:rotate(-2deg) scale(1.02);}}',
      '.bk-rad.klar .bk-i-sedel{filter:drop-shadow(0 0 6px rgba(134,239,172,.65));',
        'animation:bk-sedel 2.8s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-sedel{0%,100%{transform:translateY(1px) rotate(-5deg);}',
        '50%{transform:translateY(-3px) rotate(6deg);}}',
      '.bk-rad.klar .bk-i-stapel{filter:drop-shadow(0 0 6px rgba(56,189,248,.65));',
        'animation:bk-stapel 2.2s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-stapel{0%,100%{transform:scale(.95) translateY(1px);}',
        '50%{transform:scale(1.14) translateY(-1.5px);}}',
      '.bk-rad.klar .bk-i-skrivare{filter:drop-shadow(0 0 6px rgba(226,232,240,.6));',
        'animation:bk-skrivare 2.6s ease-in-out var(--ikd,0s) infinite;}',
      // Kvittot matas ut ett ryck i taget, som en riktig remsa.
      '@keyframes bk-skrivare{0%,100%{transform:translateY(0);}20%{transform:translateY(-2px);}',
        '30%{transform:translateY(0);}50%{transform:translateY(-1.5px);}60%{transform:translateY(0);}}',
      '.bk-tx{flex:1;min-width:0;display:flex;flex-direction:column;line-height:1.25;}',
      '.bk-tx b{font-size:.72rem;color:#ccffdd;font-weight:700;letter-spacing:.5px;',
        'display:flex;align-items:center;gap:6px;}',
      '.bk-tx i{font-size:.62rem;font-style:normal;color:#3a6a4a;white-space:nowrap;',
        'overflow:hidden;text-overflow:ellipsis;}',
      '.bk-tx i b{color:#00ff7f;font-weight:700;display:inline;font-size:1em;}',
      '.bk-pill{font-size:.5rem;letter-spacing:1.5px;padding:1px 5px;border-radius:3px;',
        'background:rgba(0,255,127,.13);color:#00ff7f;border:1px solid rgba(0,255,127,.3);}',
      '.bk-st{flex-shrink:0;width:17px;height:17px;display:flex;align-items:center;justify-content:center;}',
      '.bk-snurra{width:12px;height:12px;border-radius:50%;border:2px solid rgba(0,255,127,.18);',
        'border-top-color:#00ff7f;animation:bk-snurr .55s linear infinite;}',
      '@keyframes bk-snurr{to{transform:rotate(360deg);}}',
      '.bk-check{color:#00ff7f;font-size:.8rem;text-shadow:0 0 8px rgba(0,255,127,.8);',
        'animation:bk-pop .32s cubic-bezier(.22,1,.36,1);}',
      '@keyframes bk-pop{0%{transform:scale(.4);opacity:0;}60%{transform:scale(1.18);}100%{transform:scale(1);opacity:1;}}',
      '.bk-stapel-yttre{margin-top:13px;height:4px;border-radius:3px;background:rgba(0,255,127,.1);overflow:hidden;}',
      '.bk-fyll{height:100%;width:0;border-radius:3px;background:linear-gradient(90deg,#00cc5a,#00ff7f);',
        'box-shadow:0 0 10px rgba(0,255,127,.6);transition:width .32s ease;}',
      '.bk-hoppa{position:absolute;top:14px;right:16px;z-index:3;background:rgba(0,255,127,.08);',
        'border:1px solid rgba(0,255,127,.28);color:#8ee8a8;border-radius:6px;cursor:pointer;',
        'font-family:inherit;font-size:.62rem;letter-spacing:1px;padding:5px 10px;}',
      '.bk-hoppa:hover{background:rgba(0,255,127,.16);color:#ccffdd;}',
      '@media (max-width:580px){.bk-kort{padding:18px 14px 14px;}.bk-rubrik{font-size:1rem;}}',
      '@media (prefers-reduced-motion:reduce){.bk-splash *{animation:none!important;transition:none!important;}}'
    ].join('');
    document.head.appendChild(el);
  }

  function radHtml(r, i) {
    return '<div class="bk-rad" data-i="' + i + '" style="--ikd:' + (i * 0.11).toFixed(2) + 's">' +
      '<span class="bk-ic bk-i-' + r.an + '">' + r.ic + '</span>' +
      '<span class="bk-tx"><b>' + r.t + (r.tag ? '<span class="bk-pill">' + r.tag + '</span>' : '') + '</b>' +
      '<i>' + subFor(r) + '</i></span>' +
      '<span class="bk-st"><span class="bk-snurra"></span></span>' +
    '</div>';
  }

  /**
   * Sifferregnet — samma tecken, färg och takt som matrixRain() bakom maskinen.
   *
   * Ritas på en egen canvas i splashen i stället för att låna den bakomliggande: den ligger
   * under overlayen och hade inte synts. Intervallet stoppas när splashen tas bort, annars
   * fortsätter en osynlig canvas att ritas om var 66:e millisekund i resten av besöket.
   */
  function regn(host) {
    if (reduce) return function () {};
    var cv = document.createElement('canvas');
    cv.className = 'bk-rain';
    host.appendChild(cv);
    var ctx = cv.getContext('2d');
    var tecken = '01kr$€#*+=-<>01';
    var fs = 14, drops = [];
    function resize() {
      cv.width = host.clientWidth;
      cv.height = host.clientHeight;
      drops = [];
      for (var i = 0; i < Math.floor(cv.width / fs); i++) drops.push(Math.random() * -60);
    }
    resize();
    window.addEventListener('resize', resize);
    var id = setInterval(function () {
      if (document.hidden) return;
      ctx.fillStyle = 'rgba(1,3,1,0.11)';
      ctx.fillRect(0, 0, cv.width, cv.height);
      ctx.fillStyle = 'rgba(0,255,127,0.42)';
      ctx.font = fs + 'px monospace';
      for (var i = 0; i < drops.length; i++) {
        ctx.fillText(tecken[Math.floor(Math.random() * tecken.length)], i * fs, drops[i] * fs);
        if (drops[i] * fs > cv.height && Math.random() > 0.972) drops[i] = 0;
        drops[i] += 0.62;   // en aning snabbare än bakgrundens 0.5 — splashen ska kännas som uppstart
      }
    }, 66);
    return function () { clearInterval(id); window.removeEventListener('resize', resize); };
  }

  function skrivBoot(el, klart) {
    var rad = 0, tecken = 0;
    (function steg() {
      if (rad >= BOOT.length) { klart && klart(); return; }
      var txt = BOOT[rad];
      el.textContent = txt.slice(0, ++tecken);
      if (tecken >= txt.length) {
        rad++; tecken = 0;
        setTimeout(steg, rad >= BOOT.length ? 0 : 260);
      } else {
        setTimeout(steg, 18);
      }
    })();
  }

  function kor() {
    css();
    var lager = document.createElement('div');
    lager.className = 'bk-splash';
    lager.innerHTML =
      '<button class="bk-hoppa" type="button">HOPPA ÖVER ✕</button>' +
      '<div class="bk-kort">' +
        '<div class="bk-rubrik">BANKOMAT 2.0</div>' +
        '<div class="bk-under">Weras Betalservice AB</div>' +
        '<p class="bk-boot"><span class="pr">▸</span><span class="bk-boot-tx"></span><span class="bk-cur"></span></p>' +
        '<div class="bk-rader">' + ROWS.map(radHtml).join('') + '</div>' +
        '<div class="bk-stapel-yttre"><div class="bk-fyll"></div></div>' +
      '</div>';
    document.body.appendChild(lager);

    var stoppaRegn = regn(lager);
    var fyll   = lager.querySelector('.bk-fyll');
    var bootEl = lager.querySelector('.bk-boot-tx');
    var rader  = lager.querySelectorAll('.bk-rad');
    var timers = [];
    var klar   = false;

    // Rullningen låses medan lagret ligger på, annars scrollar sidan bakom under boot-en.
    var forraOverflow = document.documentElement.style.overflow;
    document.documentElement.style.overflow = 'hidden';

    function slutfor() {
      if (klar) return;
      klar = true;
      timers.forEach(clearTimeout);
      if (fyll) fyll.style.width = '100%';
      if (bootEl) bootEl.textContent = 'klar — sätt i kort';
      var cur = lager.querySelector('.bk-cur');
      if (cur) cur.style.display = 'none';
      document.documentElement.style.overflow = forraOverflow;
      try { sessionStorage.setItem(NYCKEL, '1'); } catch (e) {}
      // Overlamningen: maskinen satter i kortet nar lagret lyfter. Kallet ar vaktat med
      // flit - splashen ska fungera aven om fragments-skriptet inte hunnit definiera det.
      timers.push(setTimeout(function () {
        try { if (window.atmSattInKort) window.atmSattInKort(); } catch (e) {}
      }, 900));
      timers.push(setTimeout(function () {
        lager.classList.add('bk-ut');
        setTimeout(function () {
          stoppaRegn();
          if (lager.parentNode) lager.parentNode.removeChild(lager);
        }, 480);
      }, 900));   // vilan pa "klar — satt i kort" innan lagret lyfter
    }

    lager.querySelector('.bk-hoppa').addEventListener('click', slutfor);

    if (reduce) {
      rader.forEach(function (rad) {
        rad.classList.add('syns', 'klar');
        rad.querySelector('.bk-st').innerHTML = '<span class="bk-check">✓</span>';
      });
      if (bootEl) bootEl.textContent = 'redo — sätt i kort';
      timers.push(setTimeout(slutfor, 900));
      return;
    }

    skrivBoot(bootEl);

    // ~5,5 s totalt. Forsta versionen lag pa 2,3 s och var for snabb for att hinna lasas:
    // raderna bar riktiga uppgifter - databasens namn, Spring Boot-versionen, antalet konton -
    // och de ska hinna sjunka in. Tiden ligger i STEG och i vilan fore uttoningen, alltsa i
    // rorelsen, inte i en tom paus dar ingenting hander.
    var START = 240, STEG = 440, VAND = 230;
    rader.forEach(function (rad, i) {
      var nar = START + i * STEG;
      timers.push(setTimeout(function () { rad.classList.add('syns'); }, nar));
      timers.push(setTimeout(function () {
        rad.classList.add('klar');
        rad.querySelector('.bk-st').innerHTML = '<span class="bk-check">✓</span>';
        if (fyll) fyll.style.width = Math.round((i + 1) / rader.length * 100) + '%';
        if (i === rader.length - 1) timers.push(setTimeout(slutfor, 320));
      }, nar + VAND));
    });
  }

  function borVisas() {
    if (FORCE) return true;
    // Bara startsidan: sökvägen slutar på "/" (context path + /) — inte undersidorna.
    if (!/\/$/.test(location.pathname)) return false;
    try { return sessionStorage.getItem(NYCKEL) !== '1'; } catch (e) { return true; }
  }

  function start() {
    if (!borVisas()) return;
    // Maskinen har en egen BIOS-sekvens i skarmen (intro() i fragments.html, samma
    // sessionStorage-modell). Kors de samtidigt spelar BIOS-en klart BAKOM det har lagret
    // och ses aldrig - tva uppstarter dar den ena ar osynlig. Splashen ar uppstarten nu,
    // sa flaggan satts har och BIOS-en later bli. Nasta flik far den igen.
    try { sessionStorage.setItem('atmIntro', '1'); } catch (e) {}
    kor();
  }

  window.bkReplaySplash = function () {
    try { sessionStorage.removeItem(NYCKEL); } catch (e) {}
    kor();
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', start);
  } else {
    start();
  }
})();
