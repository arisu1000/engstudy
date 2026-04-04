package com.wcjung.engstudy.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wcjung.engstudy.domain.model.Domain

@Composable
fun DomainChip(
    domain: Domain,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(domain.displayNameKo) },
        leadingIcon = {
            Icon(
                imageVector = domain.icon(),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = modifier
    )
}

fun Domain.icon(): ImageVector = when (this) {
    Domain.DAILY_LIFE -> Icons.Default.Home
    Domain.BUSINESS -> Icons.Default.Business
    Domain.SCIENCE -> Icons.Default.Science
    Domain.TECHNOLOGY -> Icons.Default.Computer
    Domain.MEDICINE -> Icons.Default.LocalHospital
    Domain.LAW -> Icons.Default.Gavel
    Domain.EDUCATION -> Icons.Default.MenuBook
    Domain.ARTS -> Icons.Default.Brush
    Domain.SPORTS -> Icons.Default.SportsBasketball
    Domain.TRAVEL -> Icons.Default.Flight
    Domain.FOOD -> Icons.Default.Restaurant
    Domain.GENERAL -> Icons.Default.MoreHoriz
}
