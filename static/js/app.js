/**
 * MYTHOS 5E - APP LOGIC & STATE CONTROLLER
 */

const AppState = {
  campaign: null,
  character: null,
  messages: [],
  presets: { characters: [], scenarios: [] },
  selectedPresetHero: null,
  selectedScenario: null,
  requiredCheck: null,
  isLoading: false,
  wizardStep: 1,
  charCreationMode: 'preset' // 'preset' or 'custom'
};

// Skill to ability score mapping
const SKILL_ABILITIES = {
  "Acrobatics": "dex",
  "Animal Handling": "wis",
  "Arcana": "int",
  "Athletics": "str",
  "Deception": "cha",
  "History": "int",
  "Insight": "wis",
  "Intimidation": "cha",
  "Investigation": "int",
  "Medicine": "wis",
  "Nature": "int",
  "Perception": "wis",
  "Performance": "cha",
  "Persuasion": "cha",
  "Religion": "int",
  "Sleight of Hand": "dex",
  "Stealth": "dex",
  "Survival": "wis"
};

// Standard D&D 5E Class Hit Dice & Starting Kits
const CLASS_TEMPLATES = {
  "Fighter": {
    hitDie: 10,
    savingThrows: ["str", "con"],
    proficientSkills: ["Athletics", "Perception"],
    baseAC: 16, // Chain Mail
    inventory: [
      { name: "Longsword (1d8/1d10 slashing)", quantity: 1, description: "Martial blade", equipped: true },
      { name: "Shield (+2 AC)", quantity: 1, description: "Steel shield", equipped: true },
      { name: "Chain Mail", quantity: 1, description: "Heavy armor", equipped: true },
      { name: "Light Crossbow & 20 Bolts", quantity: 1, description: "1d8 piercing (80/320)", equipped: false },
      { name: "Dungeoneer's Pack", quantity: 1, description: "Torches, rations, rope", equipped: false }
    ],
    features: ["Second Wind (1d10+1 heal)", "Fighting Style: Dueling"]
  },
  "Paladin": {
    hitDie: 10,
    savingThrows: ["wis", "cha"],
    proficientSkills: ["Athletics", "Persuasion", "Religion"],
    baseAC: 18,
    inventory: [
      { name: "Longsword (1d8/1d10)", quantity: 1, description: "Blessed steel blade", equipped: true },
      { name: "Shield (+2 AC)", quantity: 1, description: "Sunburst emblem shield", equipped: true },
      { name: "Chain Mail", quantity: 1, description: "Heavy armor", equipped: true },
      { name: "Holy Symbol Amulet", quantity: 1, description: "Divine focus", equipped: true },
      { name: "Priest's Pack", quantity: 1, description: "Rations, waterskin, blanket", equipped: false }
    ],
    features: ["Divine Sense", "Lay on Hands (5 HP pool)"]
  },
  "Rogue": {
    hitDie: 8,
    savingThrows: ["dex", "int"],
    proficientSkills: ["Stealth", "Acrobatics", "Deception", "Sleight of Hand", "Perception"],
    baseAC: 14,
    inventory: [
      { name: "Rapier (1d8 finesse)", quantity: 1, description: "Nimble dueling blade", equipped: true },
      { name: "Shortbow & 20 Arrows", quantity: 1, description: "1d6 piercing (80/320)", equipped: true },
      { name: "Leather Armor", quantity: 1, description: "Supple light armor", equipped: true },
      { name: "Thieves' Tools", quantity: 1, description: "Lockpicks & probes", equipped: false },
      { name: "Burglar's Pack", quantity: 1, description: "Rope, lantern, oil, crowbar", equipped: false }
    ],
    features: ["Sneak Attack (+1d6)", "Expertise: Stealth, Thieves' Tools"]
  },
  "Wizard": {
    hitDie: 6,
    savingThrows: ["int", "wis"],
    proficientSkills: ["Arcana", "History", "Investigation"],
    baseAC: 12,
    inventory: [
      { name: "Quarterstaff (1d6)", quantity: 1, description: "Runic wooden staff", equipped: true },
      { name: "Arcane Focus (Orb)", quantity: 1, description: "Glowing crystal", equipped: true },
      { name: "Spellbook", quantity: 1, description: "Grimoire of arcane secrets", equipped: true },
      { name: "Scholar's Pack", quantity: 1, description: "Books, ink, quill, parchment", equipped: false }
    ],
    spells: [
      { name: "Fire Bolt", level: 0, school: "Evocation", description: "1d10 fire damage at 120ft" },
      { name: "Mage Hand", level: 0, school: "Conjuration", description: "Spectral hand manipulates objects" },
      { name: "Magic Missile", level: 1, school: "Evocation", description: "Three force darts auto-hit for 1d4+1 each" },
      { name: "Shield", level: 1, school: "Abjuration", description: "+5 AC reaction" }
    ],
    spellSlots: { "1": { total: 2, used: 0 } },
    features: ["Arcane Recovery", "Spellcasting (Intelligence)"]
  },
  "Cleric": {
    hitDie: 8,
    savingThrows: ["wis", "cha"],
    proficientSkills: ["Insight", "Religion", "Medicine"],
    baseAC: 16,
    inventory: [
      { name: "Mace (1d6 bludgeoning)", quantity: 1, description: "Flanged iron mace", equipped: true },
      { name: "Scale Mail", quantity: 1, description: "Medium armor", equipped: true },
      { name: "Shield (+2 AC)", quantity: 1, description: "Holy crest shield", equipped: true },
      { name: "Holy Symbol", quantity: 1, description: "Sacred talisman", equipped: true }
    ],
    spells: [
      { name: "Sacred Flame", level: 0, school: "Evocation", description: "1d8 radiant damage on Dex save" },
      { name: "Thaumaturgy", level: 0, school: "Transmutation", description: "Minor divine wonders" },
      { name: "Cure Wounds", level: 1, school: "Evocation", description: "Heal 1d8 + Wis mod" },
      { name: "Bless", level: 1, school: "Enchantment", description: "+1d4 to attack rolls and saves" }
    ],
    spellSlots: { "1": { total: 2, used: 0 } },
    features: ["Divine Domain: Life", "Spellcasting (Wisdom)"]
  },
  "Barbarian": {
    hitDie: 12,
    savingThrows: ["str", "con"],
    proficientSkills: ["Athletics", "Intimidation", "Survival"],
    baseAC: 14,
    inventory: [
      { name: "Greataxe (1d12 slashing)", quantity: 1, description: "Brutal battle-scarred axe", equipped: true },
      { name: "Handaxes", quantity: 2, description: "1d6 slashing thrown", equipped: false },
      { name: "Explorer's Pack", quantity: 1, description: "Rations, bedroll, torches", equipped: false }
    ],
    features: ["Rage (2/day: +2 melee dmg & physical resistance)", "Unarmored Defense (10 + Dex + Con)"]
  },
  "Ranger": {
    hitDie: 10,
    savingThrows: ["str", "dex"],
    proficientSkills: ["Stealth", "Perception", "Survival"],
    baseAC: 14,
    inventory: [
      { name: "Longbow & 20 Arrows", quantity: 1, description: "1d8 piercing (150/600)", equipped: true },
      { name: "Shortswords", quantity: 2, description: "1d6 piercing finesse", equipped: true },
      { name: "Scale Mail", quantity: 1, description: "Medium armor", equipped: true }
    ],
    features: ["Favored Enemy", "Natural Explorer"]
  },
  "Warlock": {
    hitDie: 8,
    savingThrows: ["wis", "cha"],
    proficientSkills: ["Arcana", "Deception", "Intimidation"],
    baseAC: 12,
    inventory: [
      { name: "Dagger (1d4 piercing)", quantity: 2, description: "Curved sacrificial blades", equipped: true },
      { name: "Leather Armor", quantity: 1, description: "Dyed black leather", equipped: true },
      { name: "Arcane Focus (Skull Talisman)", quantity: 1, description: "Otherworldly link", equipped: true }
    ],
    spells: [
      { name: "Eldritch Blast", level: 0, school: "Evocation", description: "1d10 force beam at 120ft" },
      { name: "Minor Illusion", level: 0, school: "Illusion", description: "Create sound or image" },
      { name: "Hex", level: 1, school: "Enchantment", description: "+1d6 necrotic on hit, disadvantage on stat checks" }
    ],
    spellSlots: { "1": { total: 1, used: 0 } },
    features: ["Pact Magic (Short rest recharge)", "Otherworldly Patron: The Fiend"]
  },
  "Bard": {
    hitDie: 8,
    savingThrows: ["dex", "cha"],
    proficientSkills: ["Performance", "Persuasion", "Insight"],
    baseAC: 13,
    inventory: [
      { name: "Rapier (1d8 finesse)", quantity: 1, description: "Ornate silver rapier", equipped: true },
      { name: "Lute", quantity: 1, description: "Masterwork musical instrument", equipped: true },
      { name: "Leather Armor", quantity: 1, description: "Light armor", equipped: true }
    ],
    spells: [
      { name: "Vicious Mockery", level: 0, school: "Enchantment", description: "1d4 psychic + disadvantage on next attack" },
      { name: "Healing Word", level: 1, school: "Evocation", description: "Bonus action heal 1d4 + Cha" },
      { name: "Dissonant Whispers", level: 1, school: "Enchantment", description: "3d6 psychic + forced reaction retreat" }
    ],
    spellSlots: { "1": { total: 2, used: 0 } },
    features: ["Bardic Inspiration (d6 bonus to allies)", "Spellcasting (Charisma)"]
  }
};

// ---- INITIALIZATION ----
document.addEventListener('DOMContentLoaded', async () => {
  if (window.lucide) {
    window.lucide.createIcons();
  }

  setupEventListeners();
  await checkSystemStatus();
  await loadPresets();
  await autoLoadOrCreateCampaign();
});

function setupEventListeners() {
  // Tab switching inside Left Panel
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
      btn.classList.add('active');
      const targetId = btn.getAttribute('data-tab');
      const pane = document.getElementById(targetId);
      if (pane) pane.classList.add('active');
    });
  });

  // Header Buttons
  document.getElementById('btn-settings').addEventListener('click', openSettingsModal);
  document.getElementById('btn-new-campaign').addEventListener('click', openNewCampaignModal);
  document.getElementById('btn-campaigns').addEventListener('click', openSavedCampaignsModal);
  document.getElementById('btn-save-campaign').addEventListener('click', handleExplicitSave);
  document.getElementById('btn-roll-required').addEventListener('click', handleRollRequiredCheck);

  // Auto-expand textarea & Enter key
  const inputEl = document.getElementById('action-input');
  if (inputEl) {
    inputEl.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        handleSendAction(e);
      }
    });
  }
}

// ---- API & SYSTEM STATUS ----
async function checkSystemStatus() {
  try {
    const res = await fetch('/api/status');
    const data = await res.json();
    if (!data.has_api_key) {
      setTimeout(() => {
        openSettingsModal();
      }, 500);
    }
  } catch (err) {
    console.error("Status check failed:", err);
  }
}

async function loadPresets() {
  try {
    const res = await fetch('/api/presets');
    AppState.presets = await res.json();
  } catch (err) {
    console.error("Failed to load presets:", err);
  }
}

async function autoLoadOrCreateCampaign() {
  try {
    // 1. Check if user was previously playing an active campaign
    const lastActiveId = localStorage.getItem('mythos_active_campaign');
    if (lastActiveId) {
      const success = await loadCampaign(lastActiveId, false);
      if (success) return;
    }

    // 2. Otherwise load most recent saved campaign
    const res = await fetch('/api/campaigns');
    const camps = await res.json();
    if (camps && camps.length > 0) {
      await loadCampaign(camps[0].id, false);
    } else {
      // 3. First time user: open the campaign selection wizard
      openNewCampaignModal();
    }
  } catch (err) {
    console.error("Error loading initial campaign:", err);
    openNewCampaignModal();
  }
}

async function loadCampaign(campaignId, showAlertOnError = true) {
  try {
    const res = await fetch(`/api/campaigns/${campaignId}`);
    if (!res.ok) throw new Error("Campaign load failed");
    const data = await res.json();
    
    AppState.campaign = data.campaign;
    AppState.character = data.character;
    AppState.messages = data.messages || [];

    // Save active campaign ID to localStorage for seamless reopen across terminal reboots
    localStorage.setItem('mythos_active_campaign', campaignId);

    // Find any pending required roll in the last message
    const lastMsg = AppState.messages[AppState.messages.length - 1];
    if (lastMsg && lastMsg.role === 'assistant' && lastMsg.required_check) {
      AppState.requiredCheck = lastMsg.required_check;
    } else {
      AppState.requiredCheck = null;
    }

    renderAll();
    closeModal('modal-campaigns');
    flashSaveIndicator("Loaded");
    return true;
  } catch (err) {
    if (showAlertOnError) {
      alert("Could not load campaign: " + err.message);
    }
    return false;
  }
}

// ---- EXPLICIT SAVE SYSTEM ----
async function handleExplicitSave() {
  if (!AppState.campaign) {
    alert("No active campaign to save.");
    return;
  }

  const saveBtn = document.getElementById('btn-save-campaign');
  saveBtn.disabled = true;

  try {
    const res = await fetch(`/api/campaigns/${AppState.campaign.id}/save`, {
      method: 'POST'
    });
    if (!res.ok) throw new Error("Save request failed");
    const data = await res.json();
    
    // Update local updated_at
    AppState.campaign.updated_at = data.updated_at;
    flashSaveIndicator("Saved: Just now");
  } catch (err) {
    console.error("Save error:", err);
    flashSaveIndicator("Save Failed", true);
  } finally {
    saveBtn.disabled = false;
  }
}

function flashSaveIndicator(text = "Saved", isError = false) {
  const indicator = document.getElementById('save-status-indicator');
  const label = document.getElementById('save-status-text');
  if (!indicator || !label) return;

  label.textContent = text;
  if (isError) {
    indicator.style.color = '#ff6b6b';
    indicator.style.background = 'rgba(255, 107, 107, 0.15)';
    indicator.style.borderColor = 'rgba(255, 107, 107, 0.35)';
  } else {
    indicator.style.color = '#2ea043';
    indicator.style.background = 'rgba(46, 160, 67, 0.15)';
    indicator.style.borderColor = 'rgba(46, 160, 67, 0.35)';
  }

  indicator.style.opacity = '1';
  setTimeout(() => {
    indicator.style.opacity = '0.85';
  }, 2500);
}

// ---- RENDER ENGINE ----
function renderAll() {
  renderHeader();
  renderCharacterSheet();
  renderChronicle();
  renderJournal();
  if (window.lucide) window.lucide.createIcons();
}

function renderHeader() {
  const locEl = document.getElementById('header-location');
  const combatBadge = document.getElementById('combat-indicator');
  const combatRoundText = document.getElementById('combat-round-text');

  if (AppState.campaign) {
    locEl.textContent = AppState.campaign.current_location || "Unknown Realm";
    if (AppState.campaign.combat_active) {
      combatBadge.classList.remove('hidden');
      combatRoundText.textContent = `COMBAT: ROUND ${AppState.campaign.combat_round || 1}`;
    } else {
      combatBadge.classList.add('hidden');
    }
  }
}

function renderCharacterSheet() {
  const c = AppState.character;
  if (!c) return;

  // Header & Identity
  document.getElementById('char-name').textContent = c.name;
  document.getElementById('char-meta').textContent = `Lvl ${c.level} ${c.race} ${c.class_name}`;

  // Vitals
  document.getElementById('char-hp-current').textContent = c.hp_current;
  document.getElementById('char-hp-max').textContent = c.hp_max;
  const hpPct = Math.max(0, Math.min(100, Math.round((c.hp_current / c.hp_max) * 100)));
  const hpBar = document.getElementById('char-hp-bar');
  hpBar.style.width = `${hpPct}%`;
  if (hpPct < 30) {
    hpBar.style.background = 'linear-gradient(90deg, #ff6b6b, #e03131)';
  } else {
    hpBar.style.background = 'linear-gradient(90deg, #f59f00, #2f9e44)';
  }

  document.getElementById('char-ac').textContent = c.armor_class;
  const initSign = c.initiative_bonus >= 0 ? `+${c.initiative_bonus}` : `${c.initiative_bonus}`;
  document.getElementById('char-init').textContent = initSign;
  document.getElementById('char-speed').textContent = c.speed;
  document.getElementById('char-prof').textContent = `+${c.proficiency_bonus}`;

  // Ability Scores
  const scores = c.ability_scores || { str: 10, dex: 10, con: 10, int: 10, wis: 10, cha: 10 };
  ['str', 'dex', 'con', 'int', 'wis', 'cha'].forEach(stat => {
    const val = scores[stat] !== undefined ? scores[stat] : 10;
    const mod = Math.floor((val - 10) / 2);
    const modStr = mod >= 0 ? `+${mod}` : `${mod}`;
    const scoreValEl = document.getElementById(`score-${stat}-val`);
    const scoreModEl = document.getElementById(`score-${stat}-mod`);
    if (scoreValEl) scoreValEl.textContent = val;
    if (scoreModEl) scoreModEl.textContent = modStr;
  });

  // Skills, Inventory, Spells
  renderSkills(c);
  renderInventory(c);
  renderSpells(c);
}

function renderSkills(character) {
  const container = document.getElementById('skills-list');
  container.innerHTML = '';

  const scores = character.ability_scores || {};
  const profs = character.proficient_skills || [];

  Object.entries(SKILL_ABILITIES).forEach(([skillName, abilityKey]) => {
    const abilityVal = scores[abilityKey] !== undefined ? scores[abilityKey] : 10;
    let mod = Math.floor((abilityVal - 10) / 2);
    const isProf = profs.includes(skillName);
    if (isProf) {
      mod += (character.proficiency_bonus || 2);
    }
    const modStr = mod >= 0 ? `+${mod}` : `${mod}`;

    const row = document.createElement('div');
    row.className = 'skill-row';
    row.onclick = () => rollSkillCheck(skillName, mod);
    row.innerHTML = `
      <div class="skill-left">
        <span class="skill-dot ${isProf ? 'proficient' : ''}"></span>
        <span>${skillName} <small style="color: var(--text-muted); font-size: 0.65rem;">(${abilityKey.toUpperCase()})</small></span>
      </div>
      <span class="skill-mod">${modStr}</span>
    `;
    container.appendChild(row);
  });
}

function renderInventory(character) {
  const container = document.getElementById('inventory-list');
  const countBadge = document.getElementById('inv-count');
  container.innerHTML = '';

  const items = character.inventory || [];
  countBadge.textContent = items.length;

  const curr = character.currency || { gp: 0, sp: 0, cp: 0 };
  document.getElementById('coin-gp').textContent = curr.gp;
  document.getElementById('coin-sp').textContent = curr.sp;
  document.getElementById('coin-cp').textContent = curr.cp;

  if (items.length === 0) {
    container.innerHTML = `<p class="empty-hint">Inventory is empty.</p>`;
    return;
  }

  items.forEach((item) => {
    const el = document.createElement('div');
    el.className = 'inv-item';
    el.innerHTML = `
      <div class="inv-item-header">
        <span class="inv-item-name">${item.name} ${item.quantity > 1 ? `(x${item.quantity})` : ''}</span>
        ${item.equipped ? `<span class="badge-mini" style="color: var(--primary);">EQUIPPED</span>` : ''}
      </div>
      ${item.description ? `<span class="inv-item-desc">${item.description}</span>` : ''}
    `;
    container.appendChild(el);
  });
}

function renderSpells(character) {
  const container = document.getElementById('spells-list');
  const slotsContainer = document.getElementById('spell-slots-container');
  container.innerHTML = '';
  slotsContainer.innerHTML = '';

  const spells = character.known_spells || [];
  if (spells.length === 0) {
    container.innerHTML = `<p class="empty-hint">No spells or cantrips prepared.</p>`;
    return;
  }

  spells.forEach(spell => {
    const card = document.createElement('div');
    card.className = 'spell-card';
    card.onclick = () => castSpell(spell.name);
    card.innerHTML = `
      <div class="spell-header">
        <span>${spell.name}</span>
        <span class="spell-school">${spell.level === 0 ? 'Cantrip' : `Level ${spell.level}`} - ${spell.school}</span>
      </div>
      <div style="font-size: 0.72rem; color: var(--text-muted); margin-top: 0.2rem;">${spell.description}</div>
    `;
    container.appendChild(card);
  });
}

function renderChronicle() {
  const feed = document.getElementById('chronicle-feed');
  feed.innerHTML = '';

  AppState.messages.forEach(msg => {
    const msgEl = document.createElement('div');
    msgEl.className = `chat-msg ${msg.role === 'user' ? 'user' : 'assistant'}`;

    const isDM = msg.role === 'assistant';
    const authorName = isDM ? 'Franz (Dungeon Master)' : (AppState.character ? AppState.character.name : 'Adventurer');

    let rollPillHtml = '';
    if (msg.dice_roll) {
      const r = msg.dice_roll;
      let critClass = r.is_crit ? 'crit' : (r.is_fumble ? 'fumble' : '');
      rollPillHtml = `
        <div class="dice-result-pill ${critClass}">
          🎲 Rolled ${r.dice}: <strong>${r.total}</strong> (Natural ${r.natural}) ${r.is_crit ? '⭐ NAT 20!' : (r.is_fumble ? '💀 NAT 1' : '')}
        </div>
      `;
    }

    const parsedHtml = window.marked ? window.marked.parse(msg.content) : msg.content;

    msgEl.innerHTML = `
      <div class="msg-header">
        <span class="msg-author">${authorName}</span>
        <span class="msg-role-tag">${isDM ? 'DM' : 'PLAYER'}</span>
      </div>
      <div class="msg-body">
        ${parsedHtml}
        ${rollPillHtml}
      </div>
    `;
    feed.appendChild(msgEl);
  });

  feed.scrollTop = feed.scrollHeight;
  renderRequiredCheckBanner();
  renderSuggestedActions();
}

function renderRequiredCheckBanner() {
  const banner = document.getElementById('required-check-banner');
  const titleEl = document.getElementById('rc-title');
  const descEl = document.getElementById('rc-desc');
  const btnText = document.getElementById('btn-roll-required-text');

  if (AppState.requiredCheck && AppState.requiredCheck.skill_or_ability) {
    const rc = AppState.requiredCheck;
    banner.classList.remove('hidden');
    titleEl.textContent = `Required ${rc.type ? rc.type.toUpperCase() : 'CHECK'}: ${rc.skill_or_ability}`;
    descEl.textContent = `DC ${rc.dc || 12}: ${rc.reason || 'Attempt the action'}`;
    btnText.textContent = `Roll ${rc.skill_or_ability}`;
  } else {
    banner.classList.add('hidden');
  }
}

function renderSuggestedActions() {
  const tray = document.getElementById('suggested-actions-tray');
  tray.innerHTML = '';

  const lastMsg = AppState.messages[AppState.messages.length - 1];
  if (lastMsg && lastMsg.role === 'assistant' && lastMsg.suggested_actions && lastMsg.suggested_actions.length > 0) {
    lastMsg.suggested_actions.forEach(actionText => {
      const chip = document.createElement('button');
      chip.className = 'action-chip';
      chip.textContent = actionText;
      chip.onclick = () => {
        document.getElementById('action-input').value = actionText;
        handleSendAction();
      };
      tray.appendChild(chip);
    });
  }
}

function renderJournal() {
  const questList = document.getElementById('quest-list');
  const questCount = document.getElementById('quest-count-badge');
  const loreNotes = document.getElementById('lore-notes-container');
  const combatContent = document.getElementById('combat-card-content');

  questList.innerHTML = '';
  const quests = (AppState.campaign && AppState.campaign.quests) || [];
  questCount.textContent = quests.filter(q => q.status === 'active').length;

  if (quests.length === 0) {
    questList.innerHTML = `<p class="empty-hint">No active quests recorded.</p>`;
  } else {
    quests.forEach(q => {
      const item = document.createElement('div');
      item.className = `quest-item ${q.status}`;
      item.innerHTML = `
        <div class="quest-item-title">${q.status === 'completed' ? '✓ ' : ''}${q.title}</div>
        <div class="quest-item-desc">${q.description}</div>
      `;
      questList.appendChild(item);
    });
  }

  // Combat status
  if (AppState.campaign && AppState.campaign.combat_active) {
    combatContent.innerHTML = `
      <div style="background: rgba(224, 49, 49, 0.1); border: 1px solid rgba(224, 49, 49, 0.4); border-radius: 6px; padding: 0.65rem; color: #ff8787;">
        <strong style="color: #ff6b6b; display: block; margin-bottom: 0.2rem;">⚔️ Turn-Based Combat Active</strong>
        <p style="font-size: 0.75rem; color: #f0f3f6;">Round ${AppState.campaign.combat_round}. Take your standard Action, Bonus Action, or Movement.</p>
      </div>
    `;
  } else {
    combatContent.innerHTML = `
      <div class="peace-mode-msg">
        <i data-lucide="shield"></i>
        <p>No active combat. You are free to explore, dialogue, and investigate.</p>
      </div>
    `;
  }

  if (AppState.campaign && AppState.campaign.discovered_npcs && AppState.campaign.discovered_npcs.length > 0) {
    loreNotes.innerHTML = AppState.campaign.discovered_npcs.map(npc => `
      <div style="margin-bottom: 0.5rem; font-size: 0.8rem;">
        <strong style="color: var(--text-gold);">${npc.name}:</strong> 
        <span style="color: var(--text-muted);">${npc.description}</span>
      </div>
    `).join('');
  }
}

// ---- ACTIONS & ROLLING ----
function rollAbilityCheck(abilityName, abilityKey) {
  if (!AppState.character) return;
  const score = AppState.character.ability_scores[abilityKey] !== undefined ? AppState.character.ability_scores[abilityKey] : 10;
  const mod = Math.floor((score - 10) / 2);
  executeRoll(20, mod, `${abilityName} Check`, 'check', abilityName);
}

function rollSkillCheck(skillName, modifier) {
  executeRoll(20, modifier, `${skillName} Check`, 'check', skillName);
}

function handleRollRequiredCheck() {
  if (!AppState.requiredCheck || !AppState.character) return;
  const rc = AppState.requiredCheck;
  const skillOrStat = rc.skill_or_ability;

  let mod = 0;
  if (SKILL_ABILITIES[skillOrStat]) {
    const abilityKey = SKILL_ABILITIES[skillOrStat];
    const score = AppState.character.ability_scores[abilityKey] !== undefined ? AppState.character.ability_scores[abilityKey] : 10;
    mod = Math.floor((score - 10) / 2);
    if ((AppState.character.proficient_skills || []).includes(skillOrStat)) {
      mod += (AppState.character.proficiency_bonus || 2);
    }
  } else {
    const lower = skillOrStat.toLowerCase().substring(0, 3);
    if (AppState.character.ability_scores[lower] !== undefined) {
      mod = Math.floor((AppState.character.ability_scores[lower] - 10) / 2);
    }
  }

  executeRoll(20, mod, `${skillOrStat} ${rc.type || 'Check'}`, rc.type || 'check', skillOrStat);
}

function castSpell(spellName) {
  const inputEl = document.getElementById('action-input');
  inputEl.value = `I cast ${spellName}!`;
  inputEl.focus();
}

window.handleRollCompleted = function(rollResult) {
  const inputEl = document.getElementById('action-input');
  let currentVal = inputEl.value.trim();
  
  if (!currentVal) {
    currentVal = `I make a ${rollResult.skill_or_ability || rollResult.roll_type || 'dice'} check.`;
  }
  
  submitActionWithRoll(currentVal, rollResult);
  inputEl.value = '';
};

async function handleSendAction(e) {
  if (e && e.preventDefault) e.preventDefault();
  if (AppState.isLoading) return;

  const inputEl = document.getElementById('action-input');
  const text = inputEl.value.trim();
  if (!text) return;

  inputEl.value = '';
  await submitActionWithRoll(text, null);
}

async function submitActionWithRoll(actionText, diceRoll) {
  if (!AppState.campaign) {
    alert("Please select or create a campaign first!");
    return;
  }

  AppState.isLoading = true;
  const sendBtn = document.getElementById('btn-send-action');
  sendBtn.disabled = true;

  const tempUserMsg = {
    id: 'temp-' + Date.now(),
    role: 'user',
    content: actionText,
    dice_roll: diceRoll,
    timestamp: new Date().toISOString()
  };
  AppState.messages.push(tempUserMsg);
  renderChronicle();

  AppState.requiredCheck = null;
  renderRequiredCheckBanner();

  try {
    const res = await fetch('/api/action', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        campaign_id: AppState.campaign.id,
        user_input: actionText,
        dice_roll: diceRoll
      })
    });

    if (!res.ok) {
      throw new Error(`Server returned ${res.status}`);
    }

    const data = await res.json();
    AppState.character = data.character;
    AppState.campaign = data.campaign;
    
    AppState.messages[AppState.messages.length - 1] = data.user_message;
    AppState.messages.push(data.dm_message);

    if (data.dm_message.required_check) {
      AppState.requiredCheck = data.dm_message.required_check;
    }

    renderAll();
    flashSaveIndicator("Autosaved");
  } catch (err) {
    console.error("Action error:", err);
    alert("Failed to reach Dungeon Master: " + err.message);
  } finally {
    AppState.isLoading = false;
    sendBtn.disabled = false;
  }
}

async function adjustHP(amount) {
  if (!AppState.character) return;
  const newHP = Math.max(0, Math.min(AppState.character.hp_max, AppState.character.hp_current + amount));
  AppState.character.hp_current = newHP;
  renderCharacterSheet();

  try {
    await fetch(`/api/character/${AppState.character.id}/update`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ hp_current: newHP })
    });
    flashSaveIndicator("Saved");
  } catch (err) {
    console.error("HP update error:", err);
  }
}

async function takeRest(type) {
  if (!AppState.character) return;
  const isLong = type === 'long';
  const actionText = isLong 
    ? "I make camp, set watch, and take a Long Rest (8 hours)." 
    : "I sit down, bind my wounds, and take a Short Rest (1 hour).";

  if (isLong) {
    AppState.character.hp_current = AppState.character.hp_max;
    if (AppState.character.spell_slots) {
      Object.keys(AppState.character.spell_slots).forEach(lvl => {
        AppState.character.spell_slots[lvl].used = 0;
      });
    }
  }

  submitActionWithRoll(actionText, null);
}

// ---- D&D WORKFLOW WIZARD (STEP 1: CAMPAIGN -> STEP 2: CHARACTER -> START) ----
function openNewCampaignModal() {
  AppState.wizardStep = 1;
  updateWizardDisplay();

  const scenGrid = document.getElementById('preset-scenarios-grid');
  const heroGrid = document.getElementById('preset-heroes-grid');
  scenGrid.innerHTML = '';
  heroGrid.innerHTML = '';

  const scenarios = AppState.presets.scenarios || [];
  const heroes = AppState.presets.characters || [];

  if (scenarios.length > 0) {
    AppState.selectedScenario = scenarios[0];
    document.getElementById('custom-campaign-title').value = scenarios[0].title;
  }
  if (heroes.length > 0) {
    AppState.selectedPresetHero = heroes[0];
  }

  // Populate scenarios
  scenarios.forEach((scen, index) => {
    const card = document.createElement('div');
    card.className = `select-card ${index === 0 ? 'selected' : ''}`;
    card.onclick = () => {
      document.querySelectorAll('#preset-scenarios-grid .select-card').forEach(c => c.classList.remove('selected'));
      card.classList.add('selected');
      AppState.selectedScenario = scen;
      document.getElementById('custom-campaign-title').value = scen.title;
    };
    card.innerHTML = `
      <div class="select-card-title">${scen.title}</div>
      <div class="select-card-sub">📍 ${scen.location}</div>
      <div class="select-card-desc">${scen.description.substring(0, 115)}...</div>
    `;
    scenGrid.appendChild(card);
  });

  // Populate pre-made heroes
  heroes.forEach((hero, index) => {
    const card = document.createElement('div');
    card.className = `select-card ${index === 0 ? 'selected' : ''}`;
    card.onclick = () => {
      document.querySelectorAll('#preset-heroes-grid .select-card').forEach(c => c.classList.remove('selected'));
      card.classList.add('selected');
      AppState.selectedPresetHero = hero;
    };
    card.innerHTML = `
      <div class="select-card-title">${hero.name}</div>
      <div class="select-card-sub">${hero.race} ${hero.class_name} (Lvl ${hero.level})</div>
      <div class="select-card-desc">${hero.backstory || 'A courageous hero.'}</div>
    `;
    heroGrid.appendChild(card);
  });

  openModal('modal-new-campaign');
  if (window.lucide) window.lucide.createIcons();
}

function updateWizardDisplay() {
  const step1 = document.getElementById('wizard-step-1');
  const step2 = document.getElementById('wizard-step-2');
  const tab1 = document.getElementById('step-tab-1');
  const tab2 = document.getElementById('step-tab-2');
  const btnBack = document.getElementById('btn-wizard-back');
  const btnNext = document.getElementById('btn-wizard-next');
  const headerTitle = document.getElementById('wizard-header-title');

  if (AppState.wizardStep === 1) {
    step1.classList.add('active');
    step2.classList.remove('active');
    tab1.classList.add('active');
    tab2.classList.remove('active');
    btnBack.classList.add('hidden');
    btnNext.innerHTML = `Next: Character Creation (Session 0) <i data-lucide="arrow-right"></i>`;
    headerTitle.textContent = "Step 1: Choose Your D&D Campaign";
  } else {
    step1.classList.remove('active');
    step2.classList.add('active');
    tab1.classList.remove('active');
    tab2.classList.add('active');
    btnBack.classList.remove('hidden');
    btnNext.innerHTML = `<i data-lucide="play"></i> Begin Journey (Enter the World)`;
    headerTitle.textContent = "Step 2: Create Your Hero (Session 0)";
  }
  if (window.lucide) window.lucide.createIcons();
}

function wizardStepNext() {
  if (AppState.wizardStep === 1) {
    AppState.wizardStep = 2;
    updateWizardDisplay();
  } else {
    // Step 2 complete: Launch campaign!
    startNewCampaign();
  }
}

function wizardStepBack() {
  if (AppState.wizardStep === 2) {
    AppState.wizardStep = 1;
    updateWizardDisplay();
  }
}

function switchCharCreationMode(mode) {
  AppState.charCreationMode = mode;
  const btnPreset = document.getElementById('btn-mode-preset');
  const btnCustom = document.getElementById('btn-mode-custom');
  const viewPreset = document.getElementById('char-mode-preset');
  const viewCustom = document.getElementById('char-mode-custom');

  if (mode === 'preset') {
    btnPreset.classList.add('active');
    btnCustom.classList.remove('active');
    viewPreset.classList.add('active');
    viewCustom.classList.remove('active');
  } else {
    btnPreset.classList.remove('active');
    btnCustom.classList.add('active');
    viewPreset.classList.remove('active');
    viewCustom.classList.add('active');
  }
}

function rollRandomCustomStats() {
  // Standard D&D 4d6 drop lowest roll for 6 stats
  const rollStat = () => {
    const dice = [
      Math.floor(Math.random() * 6) + 1,
      Math.floor(Math.random() * 6) + 1,
      Math.floor(Math.random() * 6) + 1,
      Math.floor(Math.random() * 6) + 1
    ];
    dice.sort((a, b) => a - b);
    return dice[1] + dice[2] + dice[3]; // sum top 3
  };

  document.getElementById('custom-stat-str').value = rollStat();
  document.getElementById('custom-stat-dex').value = rollStat();
  document.getElementById('custom-stat-con').value = rollStat();
  document.getElementById('custom-stat-int').value = rollStat();
  document.getElementById('custom-stat-wis').value = rollStat();
  document.getElementById('custom-stat-cha').value = rollStat();

  DiceAudio.playRollSound();
}

function buildCustomCharacterData() {
  const name = document.getElementById('custom-char-name').value.trim() || "Rowan Wildheart";
  const race = document.getElementById('custom-char-race').value;
  const className = document.getElementById('custom-char-class').value;
  const bg = document.getElementById('custom-char-bg').value;
  const backstory = document.getElementById('custom-char-backstory').value.trim();

  const str = parseInt(document.getElementById('custom-stat-str').value, 10) || 10;
  const dex = parseInt(document.getElementById('custom-stat-dex').value, 10) || 10;
  const con = parseInt(document.getElementById('custom-stat-con').value, 10) || 10;
  const intVal = parseInt(document.getElementById('custom-stat-int').value, 10) || 10;
  const wis = parseInt(document.getElementById('custom-stat-wis').value, 10) || 10;
  const cha = parseInt(document.getElementById('custom-stat-cha').value, 10) || 10;

  const conMod = Math.floor((con - 10) / 2);
  const dexMod = Math.floor((dex - 10) / 2);

  const template = CLASS_TEMPLATES[className] || CLASS_TEMPLATES["Fighter"];
  const hp = Math.max(1, template.hitDie + conMod);
  const ac = (template.baseAC || 10) + (className === "Barbarian" ? conMod : 0);

  return {
    name: name,
    race: race,
    class_name: className,
    level: 1,
    background: bg,
    alignment: "Neutral Good",
    ability_scores: {
      str: str,
      dex: dex,
      con: con,
      int: intVal,
      wis: wis,
      cha: cha
    },
    hp_current: hp,
    hp_max: hp,
    armor_class: ac,
    speed: 30,
    initiative_bonus: dexMod,
    proficiency_bonus: 2,
    proficient_skills: template.proficientSkills || ["Perception", "Athletics"],
    saving_throws: template.savingThrows || ["str", "con"],
    features_and_traits: template.features || [],
    inventory: template.inventory || [],
    currency: { cp: 0, sp: 5, ep: 0, gp: 20, pp: 0 },
    known_spells: template.spells || [],
    spell_slots: template.spellSlots || {},
    backstory: backstory || "An aspiring adventurer looking to make a name in the realm."
  };
}

async function startNewCampaign() {
  let characterData = null;
  if (AppState.charCreationMode === 'custom') {
    characterData = buildCustomCharacterData();
  } else {
    characterData = AppState.selectedPresetHero || AppState.presets.characters[0];
  }

  if (!characterData) {
    alert("Please select or create a character.");
    return;
  }

  const title = document.getElementById('custom-campaign-title').value.trim() || 
                (AppState.selectedScenario ? AppState.selectedScenario.title : "The Journey Begins");
  const premise = document.getElementById('custom-campaign-premise').value.trim();
  const settingId = premise ? premise : (AppState.selectedScenario ? AppState.selectedScenario.id : "ambush-road");

  const btnNext = document.getElementById('btn-wizard-next');
  btnNext.disabled = true;
  btnNext.innerHTML = `<i data-lucide="loader"></i> Franz is setting the scene...`;
  if (window.lucide) window.lucide.createIcons();

  try {
    const res = await fetch('/api/campaigns', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title: title,
        character_preset_or_data: characterData,
        setting_description: settingId
      })
    });

    if (!res.ok) throw new Error("Failed to create campaign");
    const data = await res.json();

    AppState.campaign = data.campaign;
    AppState.character = data.character;
    AppState.messages = data.messages || [];

    // Save ID for persistence
    localStorage.setItem('mythos_active_campaign', data.campaign.id);

    closeModal('modal-new-campaign');
    renderAll();
    flashSaveIndicator("Adventure Begun");
  } catch (err) {
    alert("Error creating campaign: " + err.message);
  } finally {
    btnNext.disabled = false;
    btnNext.innerHTML = `<i data-lucide="play"></i> Begin Journey`;
    if (window.lucide) window.lucide.createIcons();
  }
}

// ---- SAVED CAMPAIGNS (LOAD GAME) MODAL ----
async function openSavedCampaignsModal() {
  const container = document.getElementById('saved-campaigns-list');
  container.innerHTML = '<p class="empty-hint">Loading saved adventures...</p>';
  openModal('modal-campaigns');

  try {
    const res = await fetch('/api/campaigns');
    const camps = await res.json();
    container.innerHTML = '';

    if (!camps || camps.length === 0) {
      container.innerHTML = `
        <div style="text-align: center; padding: 2rem 1rem;">
          <p class="empty-hint" style="margin-bottom: 1rem;">No saved adventures found in your local database.</p>
          <button class="btn btn-primary" onclick="closeModal('modal-campaigns'); openNewCampaignModal();">
            Create Your First Campaign
          </button>
        </div>
      `;
      return;
    }

    camps.forEach(camp => {
      const item = document.createElement('div');
      item.className = 'saved-camp-card';
      const updatedDate = new Date(camp.updated_at).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
      
      item.innerHTML = `
        <div class="saved-camp-left">
          <div class="saved-camp-title">${camp.title}</div>
          <div class="saved-camp-hero">🛡️ ${camp.character_name} &bull; Level ${camp.character_level} ${camp.character_race} ${camp.character_class}</div>
          <div class="saved-camp-meta">
            <span>📍 ${camp.current_location}</span>
            <span>⏱️ Last Saved: ${updatedDate}</span>
          </div>
        </div>
        <div class="saved-camp-actions">
          <button class="btn btn-primary btn-sm" onclick="loadCampaign('${camp.id}')">
            <i data-lucide="play"></i> Resume
          </button>
          <button class="btn btn-outline btn-sm" onclick="deleteCampaignPrompt('${camp.id}', '${camp.title}')" title="Delete Adventure">
            <i data-lucide="trash-2"></i>
          </button>
        </div>
      `;
      container.appendChild(item);
    });

    if (window.lucide) window.lucide.createIcons();
  } catch (err) {
    container.innerHTML = `<p class="empty-hint">Error: ${err.message}</p>`;
  }
}

async function deleteCampaignPrompt(campaignId, title) {
  if (!confirm(`Are you sure you want to permanently delete '${title}'? This cannot be undone.`)) {
    return;
  }

  try {
    await fetch(`/api/campaigns/${campaignId}`, { method: 'DELETE' });
    if (AppState.campaign && AppState.campaign.id === campaignId) {
      localStorage.removeItem('mythos_active_campaign');
      AppState.campaign = null;
      AppState.character = null;
      AppState.messages = [];
    }
    await openSavedCampaignsModal();
  } catch (err) {
    alert("Delete failed: " + err.message);
  }
}

// ---- SETTINGS MODAL ----
function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('hidden');
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('hidden');
}

async function openSettingsModal() {
  try {
    const res = await fetch('/api/status');
    const data = await res.json();
    if (data.masked_key) {
      document.getElementById('setting-gemini-key').placeholder = `Active (${data.masked_key})`;
    }
    if (data.model_name) {
      document.getElementById('setting-model-name').value = data.model_name;
    }
  } catch (e) {}
  openModal('modal-settings');
}

async function saveSettings() {
  const key = document.getElementById('setting-gemini-key').value;
  const model = document.getElementById('setting-model-name').value;
  const custom = document.getElementById('setting-custom-dm').value;

  const payload = { model_name: model || "gemini-3.8-flash" };
  if (key && key.trim()) {
    payload.gemini_api_key = key.trim();
  }
  if (custom && custom.trim()) {
    payload.custom_dm_instructions = custom.trim();
  }

  try {
    await fetch('/api/settings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    closeModal('modal-settings');
    flashSaveIndicator("Settings Saved");
    alert("Settings saved successfully! You can now resume your adventure.");
  } catch (err) {
    alert("Error saving settings: " + err.message);
  }
}
