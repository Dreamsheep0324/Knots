package com.tang.prm.feature.divination.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.tang.prm.feature.divination.DivinationHistoryScreen
import com.tang.prm.feature.divination.DivinationScreen
import com.tang.prm.feature.divination.liuyao.LiuyaoCastScreen
import com.tang.prm.feature.divination.liuyao.LiuyaoResultScreen
import com.tang.prm.feature.divination.meihua.ExternalOmenScreen
import com.tang.prm.feature.divination.meihua.MeihuaMethodScreen
import com.tang.prm.feature.divination.meihua.MeihuaResultScreen
import com.tang.prm.ui.navigation.DivinationHistoryRoute
import com.tang.prm.ui.navigation.DivinationRoute
import com.tang.prm.ui.navigation.ExternalOmenRoute
import com.tang.prm.ui.navigation.LiuyaoCastRoute
import com.tang.prm.ui.navigation.LiuyaoResultRoute
import com.tang.prm.ui.navigation.MeihuaMethodRoute
import com.tang.prm.ui.navigation.MeihuaResultRoute

fun NavGraphBuilder.divinationGraph(navController: NavHostController) {
    composable<DivinationRoute> {
        DivinationScreen(navController = navController)
    }
    composable<LiuyaoCastRoute> {
        LiuyaoCastScreen(navController = navController)
    }
    composable<LiuyaoResultRoute> {
        LiuyaoResultScreen(navController = navController)
    }
    composable<MeihuaMethodRoute> {
        MeihuaMethodScreen(navController = navController)
    }
    composable<MeihuaResultRoute> {
        MeihuaResultScreen(navController = navController)
    }
    composable<ExternalOmenRoute> {
        ExternalOmenScreen(navController = navController)
    }
    composable<DivinationHistoryRoute> {
        DivinationHistoryScreen(navController = navController)
    }
}
