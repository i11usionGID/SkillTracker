package com.i11usion.core.ui.model

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.i11usion.core.ui.R

@Composable
fun Logo(
    boxModifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier
) {
    Box(
        modifier = boxModifier
            .size(72.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(percent = 40)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = stringResource(R.string.cd_application_logo),
            modifier = imageModifier.size(36.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}