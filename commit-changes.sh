#!/bin/bash

# 機能ごとに変更をコミットするスクリプト

echo "Starting feature-based commits..."

# 1. カテゴリ機能の基盤追加
echo "Committing category model changes..."
git add shared/src/commonMain/kotlin/xyz/moroku0519/shoppinghelper/model/ItemCategory.kt
git add shared/src/commonMain/kotlin/xyz/moroku0519/shoppinghelper/model/ShoppingItem.kt
git add composeApp/src/commonMain/kotlin/xyz/moroku0519/shoppinghelper/presentation/model/ShoppingItemUi.kt

git commit -m "feat: add item category support to models

- Create ItemCategory enum with 10 predefined categories
- Add category field to ShoppingItem model
- Update ShoppingItemUi with category support and color
- Each category has unique color for visual distinction

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"

# 2. カテゴリ表示とフィルタリング機能
echo "Committing category display and filtering..."
git add composeApp/src/commonMain/kotlin/xyz/moroku0519/shoppinghelper/presentation/components/ShoppingItemCart.kt
git add composeApp/src/commonMain/kotlin/xyz/moroku0519/shoppinghelper/presentation/screens/ShoppingListScreen.kt

git commit -m "feat: implement category filtering and display

- Add dual indicator (category + priority) in ShoppingItemCard
- Implement CategoryFilterBar with item count display
- Add category-based filtering in ShoppingListScreen
- Update sample data with realistic categories

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"

# 3. AddItemDialogの全画面化とカテゴリ選択
echo "Committing AddItemDialog full-screen conversion..."
git add composeApp/src/commonMain/kotlin/xyz/moroku0519/shoppinghelper/presentation/components/AddItemDialog.kt

git commit -m "feat: convert AddItemDialog to full-screen with category selection

- Convert from AlertDialog to full-screen Dialog
- Add CategorySelector with grid layout (2 columns)
- Implement scrollable content for better UX
- Add TopAppBar with close and add actions
- Fix dialog visibility issue with proper Dialog wrapper

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"

# 4. EditItemDialogの優先度レイアウト修正
echo "Committing EditItemDialog layout fixes..."
git add composeApp/src/commonMain/kotlin/xyz/moroku0519/shoppinghelper/presentation/components/EditItemDialog.kt

git commit -m "fix: improve priority selector layout in EditItemDialog

- Change from single row to 2x2 grid layout
- Fix text overflow issue for Japanese labels
- Improve spacing between priority chips
- Use appropriate text styles for better readability

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"

echo "All commits completed successfully!"
git log --oneline -4