/**
 * MYTHOS 5E - DICE ROLLING & AUDIO ENGINE
 */

const DiceAudio = {
  ctx: null,
  init() {
    if (!this.ctx) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (AudioContext) {
        this.ctx = new AudioContext();
      }
    }
  },
  playRollSound() {
    try {
      this.init();
      if (!this.ctx) return;
      if (this.ctx.state === 'suspended') {
        this.ctx.resume();
      }
      
      const now = this.ctx.currentTime;
      // Play 3 rapid clatter clicks simulating dice tumbling on felt/wood
      for (let i = 0; i < 4; i++) {
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        const filter = this.ctx.createBiquadFilter();

        osc.type = 'triangle';
        osc.frequency.setValueAtTime(200 + Math.random() * 400, now + i * 0.08);

        filter.type = 'bandpass';
        filter.frequency.setValueAtTime(800 + Math.random() * 600, now + i * 0.08);

        gain.gain.setValueAtTime(0.15, now + i * 0.08);
        gain.gain.exponentialRampToValueAtTime(0.001, now + i * 0.08 + 0.07);

        osc.connect(filter);
        filter.connect(gain);
        gain.connect(this.ctx.destination);

        osc.start(now + i * 0.08);
        osc.stop(now + i * 0.08 + 0.08);
      }
    } catch (e) {
      console.warn("Audio context error:", e);
    }
  },
  playCritSound() {
    try {
      this.init();
      if (!this.ctx) return;
      const now = this.ctx.currentTime;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(523.25, now); // C5
      osc.frequency.exponentialRampToValueAtTime(1046.50, now + 0.3); // C6

      gain.gain.setValueAtTime(0.2, now);
      gain.gain.exponentialRampToValueAtTime(0.001, now + 0.6);

      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start(now);
      osc.stop(now + 0.6);
    } catch (e) {
      console.warn("Audio context error:", e);
    }
  }
};

let currentRollState = {
  advantage: false,
  disadvantage: false,
  lastRoll: null,
  pendingCallback: null
};

function toggleAdvantage(type) {
  const chipAdv = document.getElementById('chip-adv');
  const chipDis = document.getElementById('chip-disadv');

  if (type === 'adv') {
    currentRollState.advantage = !currentRollState.advantage;
    currentRollState.disadvantage = false;
  } else {
    currentRollState.disadvantage = !currentRollState.disadvantage;
    currentRollState.advantage = false;
  }

  if (chipAdv) chipAdv.classList.toggle('active', currentRollState.advantage);
  if (chipDis) chipDis.classList.toggle('active', currentRollState.disadvantage);
}

function getManualModifier() {
  const modInput = document.getElementById('dice-custom-mod');
  return modInput ? parseInt(modInput.value, 10) || 0 : 0;
}

/**
 * Core dice roller function
 */
function executeRoll(sides, modifier = 0, title = `Roll d${sides}`, rollType = "custom", skillOrAbility = null) {
  DiceAudio.playRollSound();

  let r1 = Math.floor(Math.random() * sides) + 1;
  let r2 = Math.floor(Math.random() * sides) + 1;
  let natural = r1;

  if (currentRollState.advantage) {
    natural = Math.max(r1, r2);
  } else if (currentRollState.disadvantage) {
    natural = Math.min(r1, r2);
  }

  const total = natural + modifier;
  const isCrit = (sides === 20 && natural === 20);
  const isFumble = (sides === 20 && natural === 1);

  if (isCrit) {
    DiceAudio.playCritSound();
    if (window.confetti) {
      window.confetti({ particleCount: 60, spread: 70, origin: { y: 0.6 } });
    }
  }

  const rollResult = {
    dice: `1d${sides}${modifier !== 0 ? (modifier > 0 ? `+${modifier}` : `${modifier}`) : ''}`,
    total: total,
    natural: natural,
    modifier: modifier,
    roll_type: rollType,
    skill_or_ability: skillOrAbility,
    advantage: currentRollState.advantage,
    disadvantage: currentRollState.disadvantage,
    is_crit: isCrit,
    is_fumble: isFumble,
    title: title
  };

  currentRollState.lastRoll = rollResult;

  // Show animated overlay
  showDiceOverlay(rollResult);

  // Reset advantage/disadvantage after roll
  currentRollState.advantage = false;
  currentRollState.disadvantage = false;
  const chipAdv = document.getElementById('chip-adv');
  const chipDis = document.getElementById('chip-disadv');
  if (chipAdv) chipAdv.classList.remove('active');
  if (chipDis) chipDis.classList.remove('active');

  return rollResult;
}

function showDiceOverlay(roll) {
  const overlay = document.getElementById('dice-overlay');
  const titleEl = document.getElementById('dice-anim-title');
  const dieEl = document.getElementById('d20-animated-die');
  const breakdownEl = document.getElementById('dice-anim-breakdown');

  if (!overlay || !dieEl) return;

  titleEl.textContent = roll.title.toUpperCase();
  dieEl.classList.remove('rolling');
  void dieEl.offsetWidth; // trigger reflow
  dieEl.classList.add('rolling');

  dieEl.textContent = roll.natural;
  
  if (roll.is_crit) {
    dieEl.style.background = 'radial-gradient(circle at 30% 30%, #fff3bf, #ffd43b, #d9480f)';
  } else if (roll.is_fumble) {
    dieEl.style.background = 'radial-gradient(circle at 30% 30%, #ffc9c9, #e03131, #800000)';
    dieEl.style.color = '#fff';
  } else {
    dieEl.style.background = 'radial-gradient(circle at 30% 30%, #ffd43b, #d4af37, #744210)';
    dieEl.style.color = '#12161f';
  }

  const modSign = roll.modifier >= 0 ? `+${roll.modifier}` : `${roll.modifier}`;
  let advText = '';
  if (roll.advantage) advText = ' (Advantage)';
  if (roll.disadvantage) advText = ' (Disadvantage)';

  breakdownEl.innerHTML = `
    <span>Natural: <strong>${roll.natural}</strong>${advText}</span> &nbsp;|&nbsp; 
    <span>Mod: <strong>${modSign}</strong></span> &nbsp;=&nbsp; 
    <span class="dice-grand-total">${roll.total}</span>
  `;

  overlay.classList.remove('hidden');
}

function closeDiceOverlay() {
  const overlay = document.getElementById('dice-overlay');
  if (overlay) overlay.classList.add('hidden');
}

function applyDiceRollToTurn() {
  closeDiceOverlay();
  if (currentRollState.lastRoll) {
    // If there's an action prompt, automatically dispatch or append
    window.handleRollCompleted(currentRollState.lastRoll);
  }
}

function quickRollDie(sides) {
  const mod = getManualModifier();
  executeRoll(sides, mod, `Roll d${sides}`, "custom");
}
