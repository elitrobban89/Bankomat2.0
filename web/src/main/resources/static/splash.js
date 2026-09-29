/* Bankomat — (c) 2026 Robert Andersson Kopler. Alla rattigheter forbehallna. */
// Uppstartsskärm för Bankomat 2.0 — "maskinen startar" innan menyn syns.
//
// Samma mönster som systerprojektens splash (statusrader som tickar in och bockas av,
// progress-stapel), men i en BANKS språk: vit yta, marinblått, lugn typografi och gröna
// bockar. Förr var den svart med fosforgrönt sifferregn — det såg ut som ett hackerfilm-
// intro, inte som något man stoppar sitt bankkort i. Maskinen själv behåller sin display;
// det är välkomstskärmen som ska kännas som banken.
//
// TRE REGLER STYR NÄR DEN VISAS, och alla tre kom ur hur appen faktiskt används:
//   · bara på startsidan — appen navigerar mellan meny, kontohantering och översikt, och en
//     boot-sekvens vid varje sidbyte hade varit en spärr i stället för en inledning,
//   · en gång per FLIK (sessionStorage, inte localStorage) — bankomaten är en demo man går
//     tillbaka till; nästa besök samma dag ska se den igen, men inte vid varje knapptryck,
//   · ?splash=1 tvingar fram den, och window.bkReplaySplash() gör det från konsolen.
//
// Siffrorna kommer ur body:s data-attribut, som menysidan fyller ur registret och ur den
// körande JVM:en och databasanslutningen (Systeminfo). Saknas de visas raden UTAN tal i
// stället för med påhittade — samma regel som elbilsappens splash: hellre tyst än fel.
(function () {
  'use strict';

  var FORCE   = /[?&]splash=1/.test(location.search);
  var NYCKEL  = 'bk_splash_v1';
  var reduce  = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  function data(namn) {
    var b = document.body;
    return (b && b.getAttribute('data-' + namn)) || '';
  }
  function tal(n) { return Math.round(n).toLocaleString('sv-SE'); }

  // Raderna. `ic` är ikonen, `an` dess rörelse (se CSS längre ned), `tag` pillret.
  // ORDNINGEN AR MASKINENS EGEN UPPSTART: plattformen först, sedan banken, sist det
  // besokaren tar i. Splashen kor INNAN ett kort ar isatt, sa sakerhetsmodulen VANTAR pa en
  // PIN och kortlasaren ligger sist for att lamna over till "satt i kort" pa maskinen.
  // `tx(e)` bygger undertexten; e (0–1) räknar upp talen så de rullar fram i stället för att hoppa.
  var ROWS = [
    { ic: '☕', t: function () { return 'Java'; }, tag: 'STARTAD', an: 'kugge', tx: function () {
        var j = data('java'), b = data('boot');
        if (!j && !b) return 'plattformen startad';
        return (j ? '<b>Java ' + j + '</b>' : '') + (b ? (j ? ' · ' : '') + 'Spring Boot ' + b : '');
      } },
    { ic: '🐘', t: function () { return data('db').split(' ')[0] || 'Databas'; }, tag: 'ONLINE', an: 'arkiv', tx: function () {
        var d = data('db'), j = data('jdbc');
        if (!d) return 'anslutningen uppe';
        return '<b>' + d + '</b>' + (j ? ' · ' + j : '') + ' · ansluten';
      } },
    { ic: '🧩', t: function () { return 'Teknikstack'; }, an: 'stapel', tx: function () {
        return 'Spring MVC · Thymeleaf · JdbcTemplate · Docker på Render';
      } },
    { ic: '🏦', t: function () { return 'Weras Betalservice'; }, tag: 'ONLINE', an: 'bank', tx: function () {
        return 'betaltjänsten ansluten';
      } },
    { ic: '📊', t: function () { return 'Kontoregister'; }, tag: 'LIVE', an: 'stapel', raknas: true, tx: function (e) {
        // Tomt register och OLASBART register ar tva olika nej och far inte bli samma rad:
        // saknas attributen kom controllern aldrig fram till talen; star det 0 ar registret
        // last och tomt, vilket ar sant och nagot helt annat.
        var k = data('konton'), p = data('kunder');
        if (!k || !p) return 'registret anslutet';
        if (k === '0' && p === '0') return 'registret läst · inga konton ännu';
        return '<b>' + tal(k * e) + '</b> konton · <b>' + tal(p * e) + '</b> kunder';
      } },
    { ic: '💰', t: function () { return 'Förvaltat'; }, tag: 'LIVE', an: 'mynt', raknas: true, tx: function (e) {
        var s = data('saldo');
        if (s === '') return 'saldon summeras';
        if (data('konton') === '0') return 'inga saldon ännu';
        return '<b>' + tal(s * e) + ' kr</b> på kontona i registret';
      } },
    { ic: '💵', t: function () { return 'Sedelkassett'; }, an: 'sedel', tx: function () { return 'uttagsfacket laddat'; } },
    { ic: '🔐', t: function () { return 'Säkerhetsmodul'; }, tag: 'REDO', an: 'las', tx: function () { return 'väntar på PIN-kod'; } },
    { ic: '💳', t: function () { return 'Kortläsare'; }, an: 'kort', tx: function () { return 'chip &amp; magnetremsa · sätt i kort'; } }
  ];

  var BOOT = ['Terminal 02 startar…', 'ansluter till registret…', 'kontrollerar säkerhetsmodulen…'];

  function css() {
    if (document.getElementById('bk-splash-css')) return;
    // Inter för en lugn, bankmässig text. Laddas bara när splashen faktiskt visas;
    // tills den kommit gäller systemfonten, som är nästan lika bra.
    var f = document.createElement('link');
    f.rel = 'stylesheet';
    f.href = 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap';
    document.head.appendChild(f);

    var el = document.createElement('style');
    el.id = 'bk-splash-css';
    el.textContent = [
      '.bk-splash{position:fixed;inset:0;z-index:9999;overflow:auto;',
        'display:flex;align-items:center;justify-content:center;padding:18px 14px;',
        "font-family:Inter,system-ui,-apple-system,'Segoe UI',Roboto,sans-serif;color:#0b2545;",
        // Vit bankhall: ljus yta, en svag blå ton uppifrån och ett diskret rutmönster
        // som i ett värdepapper — syns knappt, men ytan blir inte platt.
        'background:radial-gradient(ellipse 80% 55% at 50% -10%,rgba(29,78,216,.10),transparent 70%),',
          'repeating-linear-gradient(45deg,rgba(11,37,69,.018) 0 1px,transparent 1px 14px),',
          'repeating-linear-gradient(-45deg,rgba(11,37,69,.018) 0 1px,transparent 1px 14px),',
          'linear-gradient(180deg,#ffffff 0%,#f3f6fa 100%);',
        'opacity:1;transition:opacity .45s ease;}',
      '.bk-splash.bk-ut{opacity:0;}',
      '.bk-kort{position:relative;width:min(452px,100%);margin:auto;background:#fff;border-radius:18px;',
        'border:1px solid #e3e8ef;padding:26px 24px 20px;',
        'box-shadow:0 1px 2px rgba(11,37,69,.06),0 18px 50px rgba(11,37,69,.12);',
        'animation:bk-in .5s cubic-bezier(.22,1,.36,1) both;}',
      // Tunn guldlinje överst, som på ett bankkort eller ett kontoutdrag.
      '.bk-kort::before{content:"";position:absolute;left:24px;right:24px;top:0;height:3px;border-radius:0 0 3px 3px;',
        'background:linear-gradient(90deg,#1d4ed8,#0b2545 55%,#c9a24a);}',
      '@keyframes bk-in{from{opacity:0;transform:translateY(10px);}to{opacity:1;transform:none;}}',
      '.bk-huvud{display:flex;align-items:center;gap:13px;}',
      '.bk-logo{flex-shrink:0;width:46px;height:46px;border-radius:12px;display:flex;align-items:center;justify-content:center;',
        'background:linear-gradient(150deg,#1d4ed8,#0b2545);box-shadow:0 6px 16px rgba(29,78,216,.28);}',
      '.bk-logo svg{width:26px;height:26px;}',
      '.bk-rubrik{font-size:1.18rem;font-weight:800;letter-spacing:-.2px;color:#0b2545;line-height:1.2;}',
      '.bk-under{font-size:.72rem;font-weight:500;color:#5b6b82;margin-top:2px;}',
      '.bk-boot{margin:16px 0 12px;padding:8px 11px;border-radius:9px;background:#f5f7fb;border:1px solid #e8edf4;',
        "font-family:ui-monospace,'Cascadia Code',Consolas,monospace;font-size:.7rem;color:#334766;min-height:1em;}",
      '.bk-boot .pr{color:#1d4ed8;margin-right:6px;font-weight:700;}',
      '.bk-cur{display:inline-block;width:6px;height:.9em;background:#1d4ed8;margin-left:2px;',
        'vertical-align:-1px;animation:bk-blink .9s step-end infinite;}',
      '@keyframes bk-blink{0%,100%{opacity:1;}50%{opacity:0;}}',
      '.bk-rader{display:flex;flex-direction:column;gap:6px;}',
      '.bk-rad{display:flex;align-items:center;gap:11px;padding:8px 11px;border-radius:10px;',
        'background:#fafbfd;border:1px solid #edf1f6;',
        'opacity:0;transform:translateY(6px);transition:opacity .3s ease,transform .3s ease,',
        'border-color .3s ease,background .3s ease;}',
      '.bk-rad.syns{opacity:1;transform:none;}',
      '.bk-rad.klar{border-color:#cfe9d9;background:#f4fbf7;}',
      // Ikonerna rör sig allihop — dämpad andning medan raden laddar, egen rörelse när den
      // tänds. Glöden är borta: på vit botten läser en lätt skugga bättre än neon.
      '.bk-ic{flex-shrink:0;width:30px;height:30px;border-radius:8px;background:#eef3fb;',
        'display:flex;align-items:center;justify-content:center;font-size:.95rem;',
        'filter:grayscale(.6);opacity:.75;transition:filter .45s ease,opacity .45s ease,background .45s ease;}',
      '.bk-ic span{display:inline-block;animation:bk-vilar 3s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-vilar{0%,100%{transform:translateY(0) scale(.96);}50%{transform:translateY(-1.5px) scale(1);}}',
      '.bk-rad.klar .bk-ic{opacity:1;filter:none;background:#e6f4ec;}',
      '.bk-rad.klar .bk-i-kugge span{animation:bk-kugge 3.4s linear var(--ikd,0s) infinite;}',
      '@keyframes bk-kugge{0%,100%{transform:rotate(-8deg);}50%{transform:rotate(8deg) translateY(-1px);}}',
      '.bk-rad.klar .bk-i-arkiv span{animation:bk-arkiv 2.6s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-arkiv{0%,100%{transform:translateX(0) scaleY(1);}',
        '40%{transform:translateX(2px) scaleY(.94);}70%{transform:translateX(-1px) scaleY(1.03);}}',
      '.bk-rad.klar .bk-i-bank span{animation:bk-bank 3s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-bank{0%,100%{transform:translateY(0) scale(1);}45%{transform:translateY(-2px) scale(1.06);}}',
      '.bk-rad.klar .bk-i-kort span{animation:bk-kort 2.4s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-kort{0%{transform:translateX(-3px) rotate(-4deg);}35%{transform:translateX(3px) rotate(4deg);}',
        '60%{transform:translateX(1px) rotate(0);}100%{transform:translateX(-3px) rotate(-4deg);}}',
      '.bk-rad.klar .bk-i-las span{animation:bk-las 2.8s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-las{0%,100%{transform:rotate(-5deg) scale(1);}30%{transform:rotate(5deg) scale(1.08);}}',
      '.bk-rad.klar .bk-i-sedel span{animation:bk-sedel 2.8s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-sedel{0%,100%{transform:translateY(1px) rotate(-5deg);}50%{transform:translateY(-2.5px) rotate(6deg);}}',
      '.bk-rad.klar .bk-i-stapel span{animation:bk-stapel 2.2s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-stapel{0%,100%{transform:scale(.95) translateY(1px);}50%{transform:scale(1.12) translateY(-1.5px);}}',
      '.bk-rad.klar .bk-i-mynt span{animation:bk-mynt 3.2s ease-in-out var(--ikd,0s) infinite;}',
      '@keyframes bk-mynt{0%{transform:perspective(60px) rotateY(0);}20%{transform:perspective(60px) rotateY(-16deg);}',
        '40%{transform:perspective(60px) rotateY(16deg);}85%,100%{transform:perspective(60px) rotateY(360deg);}}',
      '.bk-tx{flex:1;min-width:0;display:flex;flex-direction:column;line-height:1.3;}',
      '.bk-tx>b{font-size:.8rem;color:#0b2545;font-weight:700;display:flex;align-items:center;gap:7px;}',
      '.bk-tx i{font-size:.7rem;font-style:normal;color:#5b6b82;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}',
      '.bk-tx i b{color:#1d4ed8;font-weight:700;}',
      '.bk-pill{font-size:.52rem;font-weight:700;letter-spacing:.8px;padding:2px 6px;border-radius:20px;',
        'background:#e8effd;color:#1d4ed8;border:1px solid #d2defa;}',
      '.bk-pill.p-ONLINE,.bk-pill.p-STARTAD{background:#e7f6ee;color:#15803d;border-color:#c9ebd7;}',
      '.bk-st{flex-shrink:0;width:20px;height:20px;display:flex;align-items:center;justify-content:center;}',
      '.bk-snurra{width:14px;height:14px;border-radius:50%;border:2px solid #dbe4f3;',
        'border-top-color:#1d4ed8;animation:bk-snurr .6s linear infinite;}',
      '@keyframes bk-snurr{to{transform:rotate(360deg);}}',
      '.bk-check{width:20px;height:20px;border-radius:50%;background:#16a34a;color:#fff;font-size:.68rem;font-weight:800;',
        'display:flex;align-items:center;justify-content:center;box-shadow:0 2px 6px rgba(22,163,74,.3);',
        'animation:bk-pop .32s cubic-bezier(.22,1,.36,1);}',
      '@keyframes bk-pop{0%{transform:scale(.4);opacity:0;}60%{transform:scale(1.15);}100%{transform:scale(1);opacity:1;}}',
      '.bk-stapel-yttre{margin-top:16px;height:5px;border-radius:5px;background:#e8edf4;overflow:hidden;}',
      '.bk-fyll{height:100%;width:0;border-radius:5px;background:linear-gradient(90deg,#1d4ed8,#0b2545);',
        'transition:width .32s ease;}',
      '.bk-fot{display:flex;justify-content:space-between;margin-top:9px;font-size:.64rem;color:#7a8aa0;font-weight:500;}',
      '.bk-fot{align-items:center;}',
      '.bk-fot .bk-pct{font-variant-numeric:tabular-nums;color:#1d4ed8;font-weight:700;}',
      // Render-brickan: loggans pil lyfter om och om igen, som en deploy.
      '.bk-render{display:inline-flex;align-items:center;gap:7px;min-width:0;padding:3px 10px 3px 4px;border-radius:20px;',
        'background:#f3f0ff;border:1px solid #e0d7fe;color:#4c3d8f;}',
      '.bk-render b{color:#3b2a8a;font-weight:700;}',
      '.bk-render i{font-style:normal;font-family:ui-monospace,Consolas,monospace;color:#6d5bd0;white-space:nowrap;}',
      '.rd-logo{display:block;flex-shrink:0;border-radius:6px;box-shadow:0 2px 8px rgba(99,102,241,.35);}',
      '.rd-pil{animation:rd-lyft 1.6s cubic-bezier(.4,0,.2,1) infinite;}',
      '@keyframes rd-lyft{0%{transform:translateY(2px);opacity:.3;}45%{transform:translateY(-1px);opacity:1;}100%{transform:translateY(-3px);opacity:0;}}',
      '.bk-hoppa{position:fixed;top:14px;right:16px;z-index:3;background:#fff;border:1px solid #dbe2ec;',
        'color:#334766;border-radius:20px;cursor:pointer;font-family:inherit;font-size:.7rem;font-weight:600;',
        'padding:6px 13px;box-shadow:0 2px 8px rgba(11,37,69,.08);}',
      '.bk-hoppa:hover{border-color:#1d4ed8;color:#1d4ed8;}',
      '.bk-splash.bk-redo .bk-boot{color:#15803d;}',
      '.bk-splash.bk-redo .bk-fyll{background:linear-gradient(90deg,#16a34a,#15803d);}',
      '@media (max-width:580px){.bk-kort{padding:22px 15px 16px;border-radius:16px;}',
        '.bk-rad{padding:7px 10px;}.bk-ic{width:27px;height:27px;}.bk-rader{gap:5px;}}',
      '@media (prefers-reduced-motion:reduce){.bk-splash *{animation:none!important;transition:none!important;}}'
    ].join('');
    document.head.appendChild(el);
  }

  var LOGO =
    '<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">' +
      '<path d="M12 3 3 7.5V9h18V7.5L12 3Z" fill="#fff"/>' +
      '<g fill="#fff" opacity=".9"><rect x="5" y="10.5" width="2.2" height="7" rx=".5"/><rect x="9.3" y="10.5" width="2.2" height="7" rx=".5"/>' +
      '<rect x="13.6" y="10.5" width="2.2" height="7" rx=".5"/><rect x="17.8" y="10.5" width="2.2" height="7" rx=".5"/></g>' +
      '<rect x="3" y="18.5" width="18" height="2.5" rx=".6" fill="#c9a24a"/>' +
    '</svg>';

  // Render-loggan: moln med en pil som lyfter — koden som åker från GitHub upp i drift.
  // Samma märke i alla projektens splashar.
  function renderLogo(id) {
    return '<svg class="rd-logo" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">' +
      '<defs><linearGradient id="' + id + '" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#a78bfa"/><stop offset="1" stop-color="#4f46e5"/></linearGradient></defs>' +
      '<rect width="24" height="24" rx="6" fill="url(#' + id + ')"/>' +
      '<path d="M7.6 17h8.8a3.1 3.1 0 0 0 .5-6.15A4.6 4.6 0 0 0 8.1 9.7 3.6 3.6 0 0 0 7.6 17Z" fill="rgba(255,255,255,.22)" stroke="#fff" stroke-width="1.2"/>' +
      '<path class="rd-pil" d="M12 15.4v-4.6m0 0-2 2m2-2 2 2" stroke="#fff" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" fill="none"/>' +
    '</svg>';
  }
  // "master · 9a9e5d0" i drift (Render sätter variablerna); lokalt finns ingen deploy att visa.
  function deployText() {
    var c = data('commit'), b = data('branch');
    return c ? (b ? b + ' · ' : '') + c : 'GitHub → master';
  }

  function radHtml(r, i) {
    return '<div class="bk-rad" data-i="' + i + '" style="--ikd:' + (i * 0.11).toFixed(2) + 's">' +
      '<span class="bk-ic bk-i-' + r.an + '"><span>' + r.ic + '</span></span>' +
      '<span class="bk-tx"><b>' + r.t() + (r.tag ? '<span class="bk-pill p-' + r.tag + '">' + r.tag + '</span>' : '') + '</b>' +
      '<i class="bk-sub">' + r.tx(r.raknas ? 0 : 1) + '</i></span>' +
      '<span class="bk-st"><span class="bk-snurra"></span></span>' +
    '</div>';
  }

  function rakna(rad, r) {
    var el = rad.querySelector('.bk-sub');
    if (!el || !r.raknas) return;
    if (reduce) { el.innerHTML = r.tx(1); return; }
    var start = performance.now();
    (function steg(nu) {
      var p = Math.min(1, (nu - start) / 1000);
      el.innerHTML = r.tx(1 - Math.pow(1 - p, 3));
      if (p < 1) requestAnimationFrame(steg);
    })(start);
  }

  function skrivBoot(el) {
    var rad = 0, tecken = 0;
    (function steg() {
      if (rad >= BOOT.length || !el.isConnected) return;
      var txt = BOOT[rad];
      el.textContent = txt.slice(0, ++tecken);
      if (tecken >= txt.length) {
        rad++; tecken = 0;
        if (rad < BOOT.length) setTimeout(steg, 700);
      } else {
        setTimeout(steg, 22);
      }
    })();
  }

  // Kortet ska rymmas i den SYNLIGA höjden. Raderna + stapel + bricka blir högre än en
  // telefon med adress- och verktygsfält visar 650–700 — uppmätt 2026-09-29: på 390×664 klipptes
  // splashen innan "Kortläsare" (ramen är 88vh). En fast åtstramning räcker inte:
  // raderna blir fler med tiden, och telefonerna är olika höga. Därför skalas kortet ned till
  // exakt det som ryms, mätt på riktigt. Egenskapen scale och inte transform: korten har
  // inflygningsanimationer på transform, och en animation vinner över en inline-transform.
  // Skalning ändrar inte layoutmåttet, så observatören triggas inte av sin egen skalning.
  // Golvet .55 håller texten läsbar; under det hellre klipp.
  // Samma funktion finns i car-advice-splash.js, ev-splash.js och vader-splash.js.
  function passaHojd(lager, kort) {
    if (!lager || !kort) return;
    function passa() {
      if (!lager.isConnected) { window.removeEventListener('resize', passa); return; }
      var cs = getComputedStyle(lager);
      var ledig = lager.clientHeight - parseFloat(cs.paddingTop) - parseFloat(cs.paddingBottom);
      var hojd = kort.offsetHeight;
      if (!ledig || !hojd || hojd <= ledig) { kort.style.scale = ''; lager.style.overflowY = ''; return; }
      // Var kortet LIGGER avgör varifrån det skalas, inte vad CSS:en säger: ett centrerat
      // kort som flödar över sticker ut lika mycket uppåt (offsetTop under paddingen) och
      // skalas runt mitten, medan ett toppförankrat — mobilens flex-start, eller Bankomatens
      // margin:auto som faller till 0 vid överflöd — skalas från överkanten.
      var iToppen = kort.offsetTop >= parseFloat(cs.paddingTop) - 1;
      var s = Math.max(0.55, ledig / hojd);
      kort.style.transformOrigin = iToppen ? '50% 0' : '50% 50%';
      kort.style.scale = s.toFixed(4);
      // Ryms kortet nu släcks rullningslisten, som annars följer det oskalade layoutmåttet.
      lager.style.overflowY = s > 0.55 ? 'hidden' : '';
    }
    passa();
    window.addEventListener('resize', passa);
    if (window.ResizeObserver) new ResizeObserver(passa).observe(kort);
  }

  function kor() {
    css();
    var lager = document.createElement('div');
    lager.className = 'bk-splash';
    lager.innerHTML =
      '<button class="bk-hoppa" type="button">Hoppa över ✕</button>' +
      '<div class="bk-kort">' +
        '<div class="bk-huvud"><span class="bk-logo">' + LOGO + '</span>' +
          '<div><div class="bk-rubrik">Bankomat 2.0</div><div class="bk-under">Weras Betalservice AB · Terminal 02</div></div></div>' +
        '<p class="bk-boot"><span class="pr">›</span><span class="bk-boot-tx"></span><span class="bk-cur"></span></p>' +
        '<div class="bk-rader">' + ROWS.map(radHtml).join('') + '</div>' +
        '<div class="bk-stapel-yttre"><div class="bk-fyll"></div></div>' +
        '<div class="bk-fot"><span class="bk-render">' + renderLogo('bkRd') +
          '<span>Autodeploy via <b>Render</b></span><i>' + deployText() + '</i></span>' +
          '<span class="bk-pct">0 %</span></div>' +
      '</div>';
    document.body.appendChild(lager);
    passaHojd(lager, lager.querySelector('.bk-kort'));

    var fyll   = lager.querySelector('.bk-fyll');
    var pct    = lager.querySelector('.bk-pct');
    var bootEl = lager.querySelector('.bk-boot-tx');
    var rader  = lager.querySelectorAll('.bk-rad');
    var timers = [];
    var klar   = false;

    function satt(p) {
      if (fyll) fyll.style.width = Math.round(p) + '%';
      if (pct) pct.textContent = Math.round(p) + ' %';
    }
    function bocka(rad, i) {
      rad.classList.add('syns', 'klar');
      rad.querySelector('.bk-st').innerHTML = '<span class="bk-check">✓</span>';
    }

    // Rullningen låses medan lagret ligger på, annars scrollar sidan bakom under boot-en.
    var forraOverflow = document.documentElement.style.overflow;
    document.documentElement.style.overflow = 'hidden';

    function slutfor() {
      if (klar) return;
      klar = true;
      timers.forEach(clearTimeout);
      rader.forEach(function (rad, i) {
        if (!rad.classList.contains('klar')) { bocka(rad, i); rakna(rad, ROWS[i]); }
      });
      satt(100);
      lager.classList.add('bk-redo');
      if (bootEl) bootEl.textContent = 'Klar — sätt i kort';
      var cur = lager.querySelector('.bk-cur');
      if (cur) cur.style.display = 'none';
      document.documentElement.style.overflow = forraOverflow;
      try { sessionStorage.setItem(NYCKEL, '1'); } catch (e) {}
      // Overlamningen: maskinen satter i kortet nar lagret lyfter. Kallet ar vaktat med
      // flit - splashen ska fungera aven om fragments-skriptet inte hunnit definiera det.
      // Kortet ska INTE aka in i samma andetag som lagret lyfter. Da har besokaren aldrig
      // sett skarmen den just slappte: "Satt in ditt kort" med kortmarkena stod bakom
      // splashen hela tiden, och forsvann i samma sekund den forsvann. Pausen ar hela
      // poangen - maskinen ber om kortet, man hinner lasa, och sedan kommer det.
      timers.push(setTimeout(function () {
        try { if (window.atmSattInKort) window.atmSattInKort(); } catch (e) {}
      }, 4200));
      timers.push(setTimeout(function () {
        lager.classList.add('bk-ut');
        setTimeout(function () { if (lager.parentNode) lager.parentNode.removeChild(lager); }, 480);
      }, 1000));   // vilan pa "Klar — satt i kort" innan lagret lyfter
    }

    lager.querySelector('.bk-hoppa').addEventListener('click', slutfor);

    if (reduce) {
      rader.forEach(function (rad, i) { bocka(rad, i); rakna(rad, ROWS[i]); });
      if (bootEl) bootEl.textContent = 'Redo — sätt i kort';
      timers.push(setTimeout(slutfor, 1400));
      return;
    }

    skrivBoot(bootEl);

    // ~5 s totalt. Raderna bar riktiga uppgifter - Java, databasens version, antalet konton
    // och saldot - och de ska hinna sjunka in. Tiden ligger i STEGEN, alltsa i rorelsen,
    // inte i en tom paus dar ingenting hander.
    var START = 260, STEG = 400, VAND = 240;
    rader.forEach(function (rad, i) {
      var nar = START + i * STEG;
      timers.push(setTimeout(function () { rad.classList.add('syns'); rakna(rad, ROWS[i]); }, nar));
      timers.push(setTimeout(function () {
        bocka(rad, i);
        satt((i + 1) / rader.length * 100);
        if (i === rader.length - 1) timers.push(setTimeout(slutfor, 360));
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
