<div align="center">

# 📒 Hitrosa
### Carnet Numérique de Crédit & Registre Financier (Offline-First)

[![Android](https://img.shields.io/badge/Plateforme-Android%2014%2B%20(SDK%2024%E2%80%9336)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Offline--First%20%7C%20MVVM-0ea5e9?style=for-the-badge)](https://developer.android.com/topic/architecture)
[![Database](https://img.shields.io/badge/Base%20de%20donn%C3%A9es-Room%20SQLite%20(WAL)-47A248?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Security](https://img.shields.io/badge/S%C3%A9curit%C3%A9-SHA--256%20Blockchain%20%7C%20Triggers-e11d48?style=for-the-badge)](https://en.wikipedia.org/wiki/SHA-2)
[![Auteur](https://img.shields.io/badge/Auteur-N--dev--Mada-10b981?style=for-the-badge&logo=github&logoColor=white)](https://github.com/N-dev-Mada)
[![Licence](https://img.shields.io/badge/Licence-MIT-amber?style=for-the-badge)](LICENSE)

<p align="center">
  <b>La solution numérique, simple et moderne pour la tenue des registres de dettes.</b><br>
  Numérisez votre registre de crédit (<i>trosa</i>), éliminez définitivement les contestations de dettes et protégez la trésorerie de votre commerce grâce à la cryptographie SHA-256, à la photo obligatoire du mandataire et à la signature manuscrite tactile.
</p>

</div>

---

## 📌 Genèse & Vision du Projet

Dans l'économie de proximité et les commerces de quartier (*épiceries de détail, grossistes, quincailleries, boutiques de quartier*), le crédit informel (*trosa*) repose depuis des décennies sur un cahier manuscrit. Si ce support traditionnel a rendu d'immenses services, il expose le commerçant à des faiblesses critiques :
- ❌ **Pertes matérielles et usure** : pages déchirées, cahier égaré, écritures effacées par l'humidité ou manipulations répétées.
- ❌ **Contestations récurrentes** : litiges fréquents sur les montants, dénégations d'achats lors du règlement de fin de mois.
- ❌ **Le dilemme du mandataire sans preuve** : un client titulaire envoie un proche (*enfant, frère, employé, voisin*) retirer des marchandises à son nom, sans preuve visuelle ni décharge formelle.
- ❌ **Erreurs de calcul manuelles** : erreurs d'addition sur les soldes cumulés, acomptes oubliés, échéances dépassées non suivies.

**Hitrosa** a été conçu pour répondre concrètement à cette réalité de terrain : une application Android native, **100 % autonome hors-ligne**, immédiate à la caisse, avec une **valeur probante incontestable** scellée cryptographiquement.

---

## ✨ Fonctionnalités Majeures

| Fonctionnalité | Description & Valeur Ajoutée |
| :--- | :--- |
| 🇲🇬 **Devise Native en Ariary (Ar)** | Pensé pour Madagascar : calculs financiers, plafonds autorisés, acomptes et reliquats gérés nativement en Ariary (ex: `15 000 Ar`, `50 000 Ar`). |
| 📸 **Photo Obligatoire du Mandataire** | Prise de vue par appareil photo ou sélection galerie obligatoire lorsqu'un tiers retire à crédit, avec miniature intégrée au reçu et à la fiche d'opération. |
| 🔒 **Registre Append-Only Infalsifiable** | Déclencheurs SQLite physiques interdisant tout `UPDATE` ou `DELETE` sur les transactions et leurs lignes de facture. |
| ⛓️ **Chaînage Cryptographique SHA-256** | Chaque transaction intègre mathématiquement l'empreinte du bloc précédent, formant une chaîne de blocs locale auditable. |
| ✍️ **Signature Tactile Manuscrite** | Pad tactile haute fidélité permettant au client (ou à son mandataire) d'apposer sa signature au doigt lors du prêt. |
| ⚙️ **Paramètres & Personnalisation Complète** | Personnalisation du nom du commerce, de la devise, du numéro de téléphone et du logo d'établissement affiché sur l'écran principal. |
| 💾 **Sauvegarde & Export Certifié** | Export complet du registre au format standard JSON, partage sécurisé via la feuille de partage Android (Drive, WhatsApp, SMS). |
| 🛡️ **Verrouillage Double Sécurité** | Protection par code secret à 4 chiffres avec clavier virtuel brouillable et authentification biométrique (empreinte digitale / reconnaissance faciale). |
| ⚡ **Opérations de Caisse Instantanées** | Raccourci **« Tout solder »**, puces d'acomptes rapides (`+2 000 Ar`, `+5 000 Ar`, `+10 000 Ar`), calcul automatique du reste à payer. |
| 💬 **Reçus WhatsApp en 1 Clic** | Génération automatique d'un récapitulatif textuel élégant formaté pour WhatsApp / SMS avec empreinte cryptographique. |
| 📴 **100 % Hors-Ligne & Souverain** | Zéro connexion internet requise, zéro tracker, zéro abonnement. Vos données comptables restent sous votre contrôle exclusif. |

---

## 🏗️ Architecture Technique & Normes

Hitrosa applique les principes d'ingénierie logicielle recommandés par Google (**Android Modern Architecture**) : **Clean Architecture MVVM**, **Unidirectional Data Flow (UDF)** et persistance **Offline-First**.

```
                           ┌────────────────────────────┐
                           │   Jetpack Compose UI (M3)  │
                           │(Home, Detail, Credit, Sets)│
                           └──────────────┬─────────────┘
                                          │ Intentions utilisateur
                                          ▼
                           ┌────────────────────────────┐
                           │      CarnetViewModel       │
                           │  (Gestion d'état réactive) │
                           └──────┬──────────────┬──────┘
                                  │              │
                   Flow / State   ▼              ▼   Préférences asynchrones
             ┌─────────────────────────┐   ┌───────────────────────────┐
             │    LedgerRepository     │   │ UserPreferencesRepository │
             │   (Validation Métier)   │   │  (DataStore Preferences)  │
             └────────────┬────────────┘   └───────────────────────────┘
                          │
            ┌─────────────┴─────────────┐
            ▼                           ▼
 ┌──────────────────────┐   ┌──────────────────────┐
 │ AppDatabase (Room)   │   │    CryptoSecurity    │
 │ - Mode SQLite WAL    │   │ - Hachage SHA-256    │
 │ - Triggers physiques │   │ - Vérificateur bloc  │
 │ - Tables Append-Only │   │ - Formateur de reçu  │
 └──────────────────────┘   └──────────────────────┘
```

### Technologies & Bibliothèques

- **Langage de développement** : Kotlin `2.0.21` (Coroutines & Kotlin Flow)
- **Interface graphique** : Jetpack Compose avec Material Design 3 (`androidx.compose.material3`)
- **Stockage de données** : AndroidX Room `2.6.1` (KSP, WAL, déclencheurs d'immuabilité)
- **Préférences système** : Jetpack DataStore Preferences (`androidx.datastore:datastore-preferences`)
- **Sécurité & Authentification** : `androidx.biometric:biometric` & `java.security.MessageDigest` (SHA-256)
- **Moteur graphique** : Coil Compose `2.7.0` (gestion optimisée des photos et miniatures)
- **Tests de non-régression** : JUnit 4, Robolectric `4.14.1`

---

## 🔐 Sécurité & Immuabilité du Registre

L'intégrité financière de Hitrosa est garantie par deux couches de sécurité complémentaires :

### 1. Triggers SQL d'Interdiction (Immuabilité SQLite)
À l'initialisation de la base de données locale, des déclencheurs stricts sont enregistrés au niveau du moteur SQLite :
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
Toute tentative d'altération directe ou malveillante du fichier de base de données est interceptée et rejetée par le moteur SQLite.

### 2. Chaînage Cryptographique SHA-256
Chaque opération de crédit ou de règlement est liée mathématiquement à l'historique complet :
$$\text{Current Hash} = \text{SHA256}(\text{previous\_hash} + \text{client\_id} + \text{date} + \text{total} + \text{signature} + \text{mandataire})$$

Le bloc initial (Genesis) prend pour racine :
```
0000000000000000000000000000000000000000000000000000000000000000
```
La section **Paramètres** propose un outil d'audit en un clic qui recalcule l'intégralité des blocs depuis l'origine pour certifier la conformité de l'historique comptable.

---

## 🚀 Installation & Compilation

### Prérequis
- **Android Studio** : Ladybug (2024.2+) ou version ultérieure
- **JDK** : Java 17 ou Java 21 recommandé
- **Android SDK** : Compile SDK 36 (SDK minimum 24 — compatible avec l'ensemble des smartphones du marché)

### Commandes utiles

```bash
# 1. Cloner le projet
git clone https://github.com/N-dev-Mada/Hitrosa.git
cd Hitrosa

# 2. Exécuter la suite de tests unitaires et cryptographiques
./gradlew testDebugUnitTest

# 3. Assembler l'APK de test / débogage
./gradlew assembleDebug

# 4. Assembler l'application optimisée pour la distribution (Release)
./gradlew assembleRelease
```

---

## 👨‍💻 Auteur & Conception

<div align="center">
  <img src="app/src/main/res/drawable/img_avatar_ndev_1790677090108.jpg" width="100" height="100" style="border-radius: 50%;" alt="N-dev-Mada Avatar"/><br>
  <b>N-dev-Mada</b><br>
  <sub>Développeur Fullstack & Mobile • Concepteur de Hitrosa</sub><br><br>
  <a href="https://github.com/N-dev-Mada">
    <img src="https://img.shields.io/badge/GitHub-N--dev--Mada-181717?style=for-the-badge&logo=github&logoColor=white" alt="Profil GitHub de N-dev-Mada"/>
  </a>
</div>

---

## 📄 Licence

Ce projet est distribué sous licence open-source **MIT** — consultez le fichier [LICENSE](LICENSE) pour plus d'informations.

---

<div align="center">
  Conçu avec passion et rigueur pour l'autonomie et la sécurité financière des commerçants de proximité 🇲🇬
</div>
