# Hookah

An interactive Paper 1.21+ plugin that turns named Brewing Stands into functional hookahs with potion-infused effects, breath-holding mechanics, and animated 3D TextDisplay smoke rings.

**Author:** [Fentez](https://fentez.pp.ua) • **GitHub:** [Fentez/Hookah](https://github.com/Fentez/Hookah)

[English](#english) • [Українська](#українська)

---

## English

**Hookah** adds an immersive roleplay mechanic to Paper servers. Rename any Brewing Stand in an anvil (e.g., to `"Hookah"`), place potions inside its slots, and let players inhale and exhale smoke or blow spinning smoke rings!

### Key Features

* **Brewing Stand Hookahs**: Any Brewing Stand whose custom name contains a configured keyword (`hookah`, `shisha`, `кальян`) acts as an interactive hookah.
* **Potion Infusions**: Place potions inside the Brewing Stand to grant random short bursts of their effects when players take a puff (**Sneak + Right-Click**).
* **Breath Holding & Exhale Mechanics**: Hold **Sneak** to hold your breath, or release **Sneak** to gradually exhale campfire smoke particles with ambient sound effects.
* **Animated 3D Smoke Rings**: While holding smoke in your lungs, **Sneak + Left-Click** to shoot spinning, expanding `TextDisplay` smoke rings that collide with solid blocks.
* **Overpuff & Breath-Hold Poisoning**: Inhaling beyond the threshold or holding your breath too long triggers configurable nausea/poison effects.

### How to Use In-Game

1. Rename a **Brewing Stand** in an Anvil to include `"Hookah"` (or any keyword from `item-keywords`) and place it down.
2. *(Optional)* Put potions inside the Brewing Stand to infuse the smoke with potion effects.
3. **Sneak + Right-Click** the Brewing Stand to inhale smoke.
4. Release **Sneak** to exhale a stream of smoke particles, or **Sneak + Left-Click** to blow spinning smoke rings!

### Commands & Permissions

| Command | Description | Permission | Default |
| --- | --- | --- | --- |
| `/hookah reload` | Reload the plugin configuration | `hookah.reload` / `hookah.admin` | `op` |

### Build & Installation

```bash
mvn clean package
```

1. Requires **Paper 1.21+** and **Java 21+**.
2. Place `target/Hookah-1.0.0.jar` into your server's `plugins/` folder.
3. Restart the server.

---

## Українська

**Hookah** — це інтерактивний ролеплей-плагін для Paper 1.21+, який перетворює перейменовані варильні стійки на повноцінний кальян з ефектами зілля, затримкою дихання та 3D-кільцями диму.

### Основні можливості

* **Варильна стійка як кальян**: Будь-яка варильна стійка, названа на ковадлі словом із конфігу (`hookah`, `shisha`, `кальян`), працює як кальян.
* **Ефекти від зілля**: Покладіть зілля всередину варильної стійки, щоб гравці отримували їхні ефекти під час затяжки (**Shift + ПКМ**).
* **Затримка дихання та видих**: Утримуйте **Shift**, щоб затримати дим у легенях, або відпустіть **Shift** для плавного видиху частинок диму.
* **Анімовані 3D-кільця диму**: Натисніть **Shift + ЛКМ**, поки у вас є запас диму, щоб випустити об'ємні кільця диму (`TextDisplay`), які обертаються в польоті та розсіюються об блоки.
* **Механіка передозування**: Надто сильна затяжка або надто довга затримка дихання накладає ефекти нудоти та отруєння.

### Команди та права

| Команда | Опис | Право | За замовчуванням |
| --- | --- | --- | --- |
| `/hookah reload` | Перезавантажити конфігурацію плагіна | `hookah.reload` / `hookah.admin` | `op` |
