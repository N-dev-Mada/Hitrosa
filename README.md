<div align="center">

# 📒 Hitrosa
### Carnet Numérique de Crédit & Registre Financier Infalsifiable (100% Offline-First)

[![Android](https://img.shields.io/badge/Platform-Android%2014%2B%20(SDK%2024%E2%80%9336)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Offline--First%20%7C%20MVVM-0ea5e9?style=for-the-badge)](https://developer.android.com/topic/architecture)
[![Database](https://img.shields.io/badge/Database-Room%20SQLite%20(WAL)-47A248?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Security](https://img.shields.io/badge/Security-SHA--256%20Blockchain%20%7C%20Triggers-e11d48?style=for-the-badge)](https://en.wikipedia.org/wiki/SHA-2)
[![License](https://img.shields.io/badge/License-MIT-amber?style=for-the-badge)](LICENSE)

<p align="center">
  <b>La solution moderne, souveraine et infalsifiable pour la gestion des crédits de proximité en Ariary (Ar).</b><br>
  Numérisez votre carnet d'épicerie, éliminez les contestations de dette et sécurisez vos créances grâce à la cryptographie et à la signature manuscrite tactile.
</p>

</div>

---

## 📌 Présentation & Problématique Résolue

Dans le commerce de quartier et l'économie de proximité (*épiceries, grossistes, quincailleries, boutiques de quartier*), le crédit informel (« carnet de crédit ») repose traditionnellement sur un cahier manuscrit. Ce support physique présente des risques majeurs :
- ❌ **Pertes et dégradations** : pages déchirées, cahier égaré, écritures effacées par l'eau ou l'usure.
- ❌ **Contestations récurrentes** : litiges sur les montants, dénégations d'achats ou contestations lors du règlement.
- ❌ **Cas des mandataires** : un client habitué envoie un proche (*enfant, frère, employé*) chercher des denrées à son nom, sans preuve matérielle de retrait.
- ❌ **Calculs d'apothicaire manuels** : erreurs d'addition sur les soldes cumulés, retards non suivis.

**Hitrosa** transpose ce carnet traditionnel dans une application Android native, **100 % autonome hors-ligne**, ultra-rapide à la saisie, et dotée d'un **registre immuable cryptographiquement scellé** garantissant une valeur probante incontestable.

---

## ✨ Fonctionnalités Clés

| Fonctionnalité | Description |
| :--- | :--- |
| 🇲🇬 **Devise Native en Ariary (Ar)** | Tous les calculs financiers, plafonds, acomptes et créances sont gérés nativement en Ariary (ex: `15 000 Ar`, `50 000 Ar`). |
| 🔒 **Registre Append-Only Infalsifiable** | Triggers SQLite bloquant physiquement tout `UPDATE` ou `DELETE` sur les transactions et leurs lignes de facture. |
| ⛓️ **Chaînage Cryptographique SHA-256** | Chaque transaction calcule et intègre le hash du bloc précédent, formant une blockchain locale auditable en un clic. |
| ✍️ **Signature Tactile Manuscrite** | Pad tactile interactif haute précision permettant au client (ou à son mandataire) de signer directement au doigt. |
| 🚶 **Gestion des Mandataires & Émissaires** | Enregistrement structuré lorsqu'un proche retire à crédit au nom du client (*nom du mandataire, lien de parenté, téléphone, mode d'accord*). |
| ⚡ **Usage Caisse & Raccourcis Instantanés** | Bouton **« Tout solder »**, puces de montants rapides (`+2 000 Ar`, `+5 000 Ar`, `+10 000 Ar`), calcul instantané du reliquat. |
| 💬 **Reçus & Relevés WhatsApp** | Génération instantanée en 1 clic de reçus textuels détaillés formatés pour WhatsApp ou SMS, avec sceau cryptographique. |
| 🛡️ **Verrouillage Biométrique & Code PIN** | Accès protégé par empreinte digitale / reconnaissance faciale avec repli sur code PIN local à 4 chiffres. |
| 📴 **100 % Hors-Ligne & Souverain** | Aucune dépendance cloud, 0 tracker, 0 abonnement. Vos données financières restent strictement sur votre appareil. |

---

## 🏗️ Architecture Logicielle & Stack Technique

Hitrosa adopte les recommandations officielles **Google Android App Quality Architecture** : **Clean Architecture MVVM**, **Unidirectional Data Flow (UDF)** et persistance **Offline-First**.

```
                           ┌────────────────────────────┐
                           │   Jetpack Compose UI (M3)  │
                           │   (Home, Detail, Credit)   │
                           └──────────────┬─────────────┘
                                          │ Events & User Intention
                                          ▼
                           ┌────────────────────────────┐
                           │      CarnetViewModel       │
                           │  (UI State Flow & Logic)   │
                           └──────┬──────────────┬──────┘
                                  │              │
                   Flow / State   ▼              ▼   Asynchronous Preferences
             ┌─────────────────────────┐   ┌───────────────────────────┐
             │    LedgerRepository     │   │ UserPreferencesRepository │
             │  (Business Validation)  │   │  (DataStore Preferences)  │
             └────────────┬────────────┘   └───────────────────────────┘
                          │
            ┌─────────────┴─────────────┐
            ▼                           ▼
 ┌──────────────────────┐   ┌──────────────────────┐
 │ AppDatabase (Room)   │   │    CryptoSecurity    │
 │ - WAL Journal Mode   │   │ - SHA-256 Hasher     │
 │ - Immutability Triggers  │ - Blockchain Auditor │
 │ - Append-Only Tables │   │ - Receipt Generator  │
 └──────────────────────┘   └──────────────────────┘
```

### Stack Technique

- **Langage** : Kotlin `2.0.21` (100% Kotlin Coroutines & Flow)
- **UI Framework** : Jetpack Compose avec Material Design 3 (`androidx.compose.material3`)
- **Base de Données** : AndroidX Room `2.6.1` avec compilation KSP et mode SQLite WAL (*Write-Ahead Logging*)
- **Préférences** : Jetpack DataStore Preferences (`androidx.datastore:datastore-preferences`)
- **Sécurité** : `androidx.biometric:biometric` & `java.security.MessageDigest` (SHA-256)
- **Chargement d'Images** : Coil Compose `2.7.0` (optimisation mémoire avec redimensionnement automatique à 1024px)
- **Tests** : JUnit 4, Robolectric `4.14.1`, AndroidX Test Runner

---

## 🔐 Sécurité & Immuabilité du Registre

L'intégrité financière de Hitrosa repose sur une double barrière physique et algorithmique :

### 1. Triggers SQL d'Interdiction (Immuabilité SQLite)
À la création de la base de données locale, des déclencheurs stricts sont enregistrés au niveau du moteur SQLite :
```sql
CREATE TRIGGER prevent_transaction_update
BEFORE UPDATE ON transactions
BEGIN
    SELECT RAISE(FAIL, 'SÉCURITÉ : Un crédit enregistré est immuable et ne peut pas être modifié !');
END;

CREATE TRIGGER prevent_transaction_delete
BEFORE DELETE ON transactions
BEGIN
    SELECT RAISE(FAIL, 'SÉCURITÉ : Un crédit enregistré ne peut pas être supprimé !');
END;
```
Toute tentative de manipulation directe du fichier `.db` est interceptée et avortée par SQLite.

### 2. Chaînage SHA-256 (Preuve de non-altération)
Chaque bloc de crédit ou de règlement dépend mathématiquement de l'historique complet :
$$\text{Current Hash} = \text{SHA256}(\text{previous\_hash} + \text{client\_id} + \text{date} + \text{total} + \text{signature} + \text{mandataire})$$

Le bloc initial (Genesis) prend pour racine :
```
0000000000000000000000000000000000000000000000000000000000000000
```
L'écran **Journal d'Audit** permet de recalculer en temps réel l'ensemble de la chaîne depuis le premier bloc pour certifier qu'aucune donnée n'a été corrompue.

---

## 🚀 Installation & Compilation

### Prérequis
- **Android Studio** : Ladybug (2024.2+) ou version ultérieure
- **JDK** : Java 17 ou Java 21 recommandé
- **Android SDK** : Compile SDK 36 (Minimum SDK 24 - compatible 99% des smartphones en circulation)

### Étapes de compilation

1. **Cloner le dépôt :**
   ```bash
   git clone https://github.com/votre-utilisateur/Hitrosa.git
   cd Hitrosa
   ```

2. **Ouvrir le projet dans Android Studio :**
   - Lancez Android Studio.
   - Sélectionnez `Open` et choisissez le dossier racine du projet.
   - Laissez Gradle synchroniser les dépendances (`libs.versions.toml`).

3. **Exécuter les tests unitaires et cryptographiques :**
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. **Générer l'APK de Débogage :**
   ```bash
   ./gradlew assembleDebug
   ```
   *L'APK compilé sera généré dans :* `app/build/outputs/apk/debug/app-debug.apk`

5. **Générer l'APK / AAB optimisé pour la production (Release R8) :**
   ```bash
   ./gradlew assembleRelease
   ```

---

## 🤝 Contribution & Bonnes Pratiques

Les contributions sont les bienvenues ! Pour proposer une amélioration ou corriger un bug :

1. Forkez le projet.
2. Créez votre branche de fonctionnalité (`git checkout -b feature/nouvelle-fonctionnalite`).
3. Vérifiez la conformité du code et le passage des tests (`./gradlew testDebugUnitTest`).
4. Committez vos modifications selon la convention *Conventional Commits* (`git commit -m 'feat: ajout du filtre par date'`).
5. Poussez sur votre branche (`git push origin feature/nouvelle-fonctionnalite`).
6. Ouvrez une **Pull Request**.

---

## 📄 Licence

Ce projet est sous licence libre **MIT** — consultez le fichier [LICENSE](LICENSE) pour plus de détails.

---

<div align="center">
  Conçu avec passion pour l'autonomie et la sécurité financière des commerçants de proximité 🇲🇬
</div>
