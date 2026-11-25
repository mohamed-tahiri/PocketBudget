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
  - 13 Tests unitaires **JUnit** (`PocketBudgetUnitTest.kt`).
  - 5 Tests UI **Espresso** (`PocketBudgetUiTest.kt`).

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

---

## 🐛 Documentation des Bugs Résolus (Débogage & Solutions)

### Bug 1 : Écrasement des préférences DataStore et ouverture multiple du dialogue Budget
- **Symptômes :**
  1. Après redémarrage de l'application ou ouverture de l'écran `Paramètres`, la devise, le seuil d'alerte et le thème sélectionnés par l'utilisateur étaient réinitialisés aux valeurs par défaut (`€`, `80%`, `Système`). De plus, le thème sombre n'était appliqué qu'après avoir visité `SettingsFragment`.
  2. Dans `BudgetListFragment`, l'appui sur le bouton `+` ouvrait à nouveau le dialogue d'ajout dès qu'un budget était enregistré.
- **Diagnostic (Logcat, Breakpoints & Inspection de variables) :**
  - En posant un breakpoint dans `UserPreferencesManager.updateCurrency()` et en inspectant les logs `Logcat` (`UserPreferencesManager`), `updateCurrency("€")` et `updateThemeMode("SYSTEM")` étaient appelés automatiquement à l'ouverture de `SettingsFragment`.
  - L'inspection de `SettingsViewModel.uiState` et du flag `isInitialSetup` dans `SettingsFragment` a révélé que `stateIn(..., initialValue = SettingsUiState())` émettait immédiatement l'état par défaut avant la lecture DataStore, et que `Spinner.setSelection(...)` planifie `OnItemSelectedListener.onItemSelected` de façon asynchrone sur le message queue Android — *après* la remise à `false` synchrone de `isInitialSetup`.
  - Dans `BudgetListFragment`, un breakpoint sur `fabAddBudget.setOnClickListener` a montré que `viewModel.uiState.collectLatest` restait actif après le clic et ré-invoquait `showAddEditBudgetDialog` à chaque nouvelle émission du `StateFlow`.
- **Cause racine :**
  - Absence d'indicateur `isLoaded` dans `SettingsUiState`, comparaison manquante avant d'appeler `viewModel.update*()`, et application du thème placée dans `SettingsFragment` au lieu de `MainActivity`.
  - Souscription infinie (`collectLatest`) à un `StateFlow` à l'intérieur d'un `OnClickListener`.
- **Solution :**
  - Ajout de `isLoaded: Boolean` dans `SettingsUiState`, vérification de `hasLoadedState` et comparaison avec la valeur courante du ViewModel dans `OnItemSelectedListener`, et déplacement de l'observation `themeModeFlow` dans `MainActivity`.
  - Remplacement de `collectLatest` dans `fabAddBudget.setOnClickListener` par une lecture synchrone `viewModel.uiState.value.categories`.

### Bug 2 : Désynchronisation réactive des budgets dépensés et perte de catégorie lors de l'édition d'une transaction
- **Symptômes :**
  1. L'ajout, la modification ou la suppression d'une transaction de dépense ne mettait pas à jour les montants dépensés (`spentAmount`), les barres de progression et les alertes budgétaires sur `DashboardFragment` et `BudgetListFragment` tant qu'un budget ou une catégorie n'était pas modifié.
  2. Lors de l'édition d'une transaction existante dans `AddEditTransactionFragment`, le spinner de catégorie se repositionnait toujours sur le premier élément (index `0`) et la date originale était écrasée par `System.currentTimeMillis()`.
- **Diagnostic (Logcat, Breakpoints & Inspection de variables) :**
  - En ajoutant une transaction dans `AddEditTransactionViewModel` (tag `AddEditTransactionVM` dans Logcat) et en plaçant un breakpoint dans `BudgetRepository.getBudgetsWithSpentForMonth`, la lambda `combine(budgetsFlow, categoriesFlow)` n'était jamais ré-exécutée après `transactionDao.insertTransaction(...)`.
  - L'inspection de `AddEditTransactionFragment.onViewCreated` a montré que `tx.categoryId` était récupéré mais jamais appliqué sur `binding.spinnerCategory`.
- **Cause racine :**
  - `BudgetRepository.getBudgetsWithSpentForMonth` ne combinait que les `Flow` de `BudgetDao` et `CategoryDao`, sans observer la table `transactions`.
  - `AddEditTransactionFragment` ne conservait pas `pendingCategoryId` pour le sélectionner une fois la liste des catégories chargée, et `AddEditTransactionViewModel.saveTransaction` passait `date = date` (horodatage courant) même lors d'une mise à jour.
- **Solution :**
  - Ajout dans `TransactionDao` d'une requête réactive `getCategoryExpenseTotalsFlow(): Flow<List<CategorySpentTotal>>` combinée dans `BudgetRepository.getBudgetsWithSpentForMonth(...)` pour recalculer les dépenses dès qu'une transaction change.
  - Conservation de `pendingCategoryId` (avec sauvegarde via `onSaveInstanceState` lors des rotations d'écran) et préservation de `existing.date` lors de la mise à jour d'une transaction.
