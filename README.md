# PocketBudget 💰 - Application de Suivi de Dépenses Personnelles

**PocketBudget** est une application Android native complète développée en **Kotlin**, suivant une architecture **MVVM & Clean Architecture**, conçue pour un projet universitaire de niveau expert. Elle permet de suivre ses revenus et dépenses, d'organiser ses transactions par catégorie, de gérer des budgets mensuels avec alertes de dépassement, et de personnaliser ses préférences et son profil utilisateur en temps réel.

---

## 🚀 Fonctionnalités Métier
1. **Gestion des Transactions :** Enregistrement des revenus (`INCOME`) et dépenses (`EXPENSE`), avec catalogue principal propulsé par **Paging 3** (recherche instantanée, filtres par type/catégorie, tris par date et montant).
2. **Catégories & Budgets :**
   - CRUD complet des catégories avec icônes et codes couleurs personnalisés.
   - Budgets mensuels par catégorie calculant dynamiquement le montant dépensé, le solde restant et le pourcentage consommé.
   - **Logique métier d'alerte :** Avertissement visuel (badges et barres de progression dynamiques) lorsqu'un budget approche (ex: >80%) ou dépasse sa limite (100%).
3. **Tableau de Bord (*Dashboard*) :**
   - Résumé financier global (solde, total revenus, total dépenses).
   - En-tête de profil utilisateur personnalisable (nom, email, avatar).
   - Aperçu direct des alertes budgétaires en cours.
4. **Préférences & Paramètres (*DataStore*) :**
   - Choix de la devise (`€`, `$`, `£`, `CHF`) appliquée dynamiquement à tous les montants.
   - Réglage du seuil d'alerte de budget (`80%`, `90%`, `100%`).
   - Mode sombre / clair (`System`, `Light`, `Dark`).
   - Saisie et persistance du profil utilisateur (Nom & Email) synchronisées avec le tableau de bord.

---

## 🛠️ Architecture Technique & Stack
- **Langage :** 100% Kotlin
- **Architecture :** MVVM + Clean Architecture (`DAO` $\rightarrow$ `Repository` $\rightarrow$ `ViewModel` $\rightarrow$ `Fragment`)
- **UI & Navigation :** Android Views (XML), ViewBinding, Material Components (Material 3), Jetpack Navigation Component, BottomNavigationView.
- **Base de Données :** Room Database (Entités, Clés étrangères, Relations `@Relation`, POJOs, Paging 3).
- **Persistance :** Preferences DataStore.
- **Initialisation :** Fichier JSON (`assets/initial_data.json`) avec 25+ enregistrements injectés une seule fois au premier lancement.
- **Tests Automatisés :** 
  - 11 Tests unitaires **JUnit** (`PocketBudgetUnitTest.kt`).
  - Tests UI **Espresso** (`PocketBudgetUiTest.kt`).

---

## 📂 Arborescence du Projet
```text
app/src/main/
├── assets/initial_data.json
├── java/com/example/pocketbudget/
│   ├── data/
│   │   ├── datastore/UserPreferencesManager.kt
│   │   ├── json/InitialDataInitializer.kt
│   │   └── local/ (Entities, DAOs, Relations, PocketBudgetDatabase)
│   ├── repository/ (Category, Budget, Transaction Repositories)
│   ├── ui/
│   │   ├── MainActivity.kt
│   │   ├── dashboard/
│   │   ├── transactions/
│   │   ├── transaction/ (Add/Edit)
│   │   ├── budgets/
│   │   ├── categories/
│   │   └── settings/
│   └── util/ (CurrencyFormatter, BudgetAlertCalculator)
└── res/ (Layouts, Navigation Graph, Menus, Values, Drawables)
```

---

## 🧪 Exécution des Tests
Pour lancer les tests unitaires JUnit :
```bash
./gradlew testDebugUnitTest
```
Pour lancer les tests d'instrumentation UI Espresso (nécessite un émulateur ou appareil connecté) :
```bash
./gradlew connectedDebugAndroidTest
```
