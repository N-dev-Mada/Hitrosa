<div align="center">

  <img src="app/src/main/res/drawable/img_hitrosa_logo_1790674724593.jpg" width="110" height="110" style="border-radius: 24px;" alt="Hitrosa Logo" />

  # Hitrosa
  ### Carnet Numérique de Crédit & Registre Financier Sécurisé

  [![Android Compatibility](https://img.shields.io/badge/Android-5.0%20%C3%A0%2017%20(API%2021--36)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Room Database](https://img.shields.io/badge/Database-Room%20SQLite%20(WAL)-47A248?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
  [![Security](https://img.shields.io/badge/S%C3%A9curit%C3%A9-SHA--256%20%7C%20Biom%C3%A9trie-e11d48?style=for-the-badge)](https://en.wikipedia.org/wiki/SHA-2)
  [![Licence](https://img.shields.io/badge/Licence-MIT-amber?style=for-the-badge)](LICENSE)

  <p align="center">
    <b>La tenue de compte simple, infalsifiable et 100 % hors-ligne pour commerçants et artisans.</b><br>
    Fini les cahiers de dettes égarés et les litiges de fin de mois : scellez vos opérations avec signature tactile, photo du porteur et preuve cryptographique SHA-256.
  </p>

  <p align="center">
    <a href="https://github.com/N-dev-Mada/Hitrosa/releases/latest">
      <img src="https://img.shields.io/badge/%E2%AC%87%EF%B8%8F%20T%C3%A9l%C3%A9charger%20l'APK%20(v1.0.0)-0ea5e9?style=for-the-badge&logo=android&logoColor=white" alt="Télécharger l'APK Hitrosa"/>
    </a>
  </p>

</div>

---

## 🌟 Présentation

Dans les commerces de quartier (*épiceries, quincailleries, grossistes*), le crédit informel (*trosa*) repose traditionnellement sur des carnets manuscrits sujets aux pertes de pages, calculs erronés et contestations lors des règlements.

**Hitrosa** modernise cette pratique tout en conservant son immédiateté : une application Android native **ultra-légère**, **100 % opérationnelle hors connexion** et dotée d'une **valeur probante renforcée**.

---

## ✨ Fonctionnalités Clés

- 📴 **100 % Hors-Ligne & Souverain** : Aucune dépendance au réseau ni compte cloud requis. L'ensemble des données réside exclusivement sur l'appareil du commerçant.
- 🔒 **Registre Append-Only Infalsifiable** : Protection physique au niveau de la base de données interdisant toute modification ou suppression d'opération enregistrée.
- ⛓️ **Chaînage Cryptographique SHA-256** : Chaque écriture de crédit ou de règlement intègre l'empreinte mathématique de la transaction précédente, formant un registre vérifiable en un clic.
- 📸 **Photo & Décharge du Mandataire** : Capture photographique obligatoire et enregistrement de l'identité lorsqu'un tiers retire des marchandises au nom du client.
- ✍️ **Signature Tactile Manuscrite** : Pavé de signature numérique sur écran pour acter l'accord au moment du retrait.
- 💵 **Gestion Précise des Règlements** : Suivi des paiements en espèces ou mobiles (MVola), calcul automatique du solde restant dû et alertes de dépassement.
- 💬 **Reçus & Relevés en 1 Clic** : Génération instantanée de tickets et relevés de compte détaillés prêts à partager (WhatsApp, SMS, etc.).
- 🛡️ **Verrouillage PIN & Biométrie** : Accès protégé par code secret à 4 chiffres et capteur d'empreinte digitale / reconnaissance faciale.
- 💾 **Sauvegarde & Réinitialisation** : Export certifié au format JSON pour archivage et fonction de remise à zéro usine sécurisée.

---

## 🏗️ Architecture & Technologies

Hitrosa applique les standards modernes de développement Android recommandés par Google :

```
┌────────────────────────────────────────────────────────┐
│              Jetpack Compose UI (Material 3)           │
│         (Home, Fiche Client, Nouveau Crédit, Paiement) │
└───────────────────────────┬────────────────────────────┘
                            │ Actions utilisateur
                            ▼
┌────────────────────────────────────────────────────────┐
│                   CarnetViewModel                      │
│             (État réactif & Logique métier)            │
└─────────────┬────────────────────────────┬─────────────┘
              │                            │
              ▼                            ▼
┌──────────────────────────┐  ┌──────────────────────────┐
│     LedgerRepository     │  │ UserPreferencesRepository│
│  (Opérations financières)│  │   (DataStore Preferences) │
└─────────────┬────────────┘  └──────────────────────────┘
              │
      ┌───────┴───────┐
      ▼               ▼
┌───────────┐   ┌────────────┐
│ Room / DB │   │ Cryptos    │
│ (SQLite)  │   │  (SHA-256) │
└───────────┘   └────────────┘
```

| Composant | Technologie | Rôle |
| :--- | :--- | :--- |
| **Langage** | Kotlin 2.2 | Typage fort, Coroutines & Flow |
| **Interface** | Jetpack Compose & M3 | Design réactif, thématique claire/sombre |
| **Persistance** | Room Database 2.7 (SQLite) | Stockage local haute performance |
| **Préférences** | AndroidX DataStore | Configuration de la boutique et de la sécurité |
| **Authentification** | AndroidX Biometric | Biométrie matérielle avec repli PIN |
| **Images** | Coil Compose | Chargement optimisé des pièces justificatives |

---

## 📲 Installation

### Option 1 : Téléchargement direct (Recommandé)
1. Rendez-vous dans la section [Releases](https://github.com/N-dev-Mada/Hitrosa/releases/latest).
2. Téléchargez le fichier **`Hitrosa-v1.0.0.apk`**.
3. Ouvrez le fichier sur votre smartphone Android (Android 5.0 ou supérieur) et suivez les instructions à l'écran.

### Option 2 : Compilation depuis les sources

```bash
# 1. Cloner le projet
git clone https://github.com/N-dev-Mada/Hitrosa.git
cd Hitrosa

# 2. Lancer les tests unitaires et de sécurité
./gradlew testDebugUnitTest

# 3. Générer l'APK de production
./gradlew assembleRelease
```

L'APK généré sera disponible dans `app/build/outputs/apk/release/`.

---

## 📱 Compatibilité Système

Hitrosa a été optimisé pour une couverture maximale du parc mobile :
- **Version minimale :** Android 5.0 (Lollipop - API 21)
- **Version cible :** Android 16 / 17 (Baklava - API 36+)
- **Formats supportés :** Smartphones, tablettes et pliables

---

## 🤝 Crédits & Auteur

- **Auteur & Architecte** : [N-dev-Mada](https://github.com/N-dev-Mada)
- **Écosystème** : Produit officiel de la suite **N-product**
- **Licence** : Ce projet est sous licence [MIT](LICENSE) — libre d'utilisation personnelle et commerciale.
