package com.tang.prm.feature.graph.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.tang.prm.feature.graph.graph.GraphScreen
import com.tang.prm.ui.navigation.AddContactRoute
import com.tang.prm.ui.navigation.ContactDetailRoute
import com.tang.prm.ui.navigation.EventDetailRoute
import com.tang.prm.ui.navigation.GraphRoute

/**
 * 图谱导航图。
 *
 * 注册 [GraphRoute] → [GraphScreen] 路由，并在节点二次点击时
 * 导航到 [ContactDetailRoute]（联系人详情）或 [EventDetailRoute]（事件详情）。
 *
 * 平板模式：本次仅手机端，平板适配留待 v1.5.4 后续迭代。
 *
 * @param navController 全局导航控制器
 */
fun NavGraphBuilder.graphGraph(
    navController: NavHostController
) {
    composable<GraphRoute> {
        GraphScreen(
            onBack = { navController.popBackStack() },
            onContactClick = { contactId ->
                navController.navigate(ContactDetailRoute(contactId))
            },
            onEventClick = { eventId ->
                navController.navigate(EventDetailRoute(eventId))
            },
            onAddContact = { navController.navigate(AddContactRoute) }
        )
    }
}
