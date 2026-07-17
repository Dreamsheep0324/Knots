package com.tang.prm.feature.recipes.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.tang.prm.feature.recipes.AddRecipeScreen
import com.tang.prm.feature.recipes.RecipeDetailScreen
import com.tang.prm.feature.recipes.RecipesScreen
import com.tang.prm.ui.navigation.AddRecipeRoute
import com.tang.prm.ui.navigation.EditRecipeRoute
import com.tang.prm.ui.navigation.RecipeDetailRoute
import com.tang.prm.ui.navigation.RecipesRoute

fun NavGraphBuilder.recipesGraph(navController: NavHostController) {
    composable<RecipesRoute> {
        RecipesScreen(navController = navController)
    }
    composable<AddRecipeRoute> {
        AddRecipeScreen(navController = navController)
    }
    composable<EditRecipeRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EditRecipeRoute>()
        AddRecipeScreen(recipeId = route.recipeId, navController = navController)
    }
    composable<RecipeDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<RecipeDetailRoute>()
        RecipeDetailScreen(recipeId = route.recipeId, navController = navController)
    }
}
