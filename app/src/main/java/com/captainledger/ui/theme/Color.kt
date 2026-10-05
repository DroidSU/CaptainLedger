package com.captainledger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Theme Palette (Emerald / FinTech Teal)
val PrimaryLight = Color(0xFF0A6847)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF9EEFB2)
val OnPrimaryContainerLight = Color(0xFF00210E)

val SecondaryLight = Color(0xFF4C6356)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCEE8D7)
val OnSecondaryContainerLight = Color(0xFF082015)

val TertiaryLight = Color(0xFF38656A)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFBCEBEF)
val OnTertiaryContainerLight = Color(0xFF002023)

val BackgroundLight = Color(0xFFF9FBF8)
val OnBackgroundLight = Color(0xFF1A1C19)
val SurfaceLight = Color(0xFFF9FBF8)
val OnSurfaceLight = Color(0xFF1A1C19)
val SurfaceVariantLight = Color(0xFFDDE5DC)
val OnSurfaceVariantLight = Color(0xFF424943)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

// Semantic Financial Colors (Light)
val SuccessLight = Color(0xFF1B8246)
val ExpenseLight = Color(0xFFD32F2F)
val WarningLight = Color(0xFFE65100)
val WarningContainerLight = Color(0xFFFFF3E0)

// Dark Theme Palette (Vibrant Mint & Deep Slate)
val PrimaryDark = Color(0xFF6CD38F)
val OnPrimaryDark = Color(0xFF00391A)
val PrimaryContainerDark = Color(0xFF005228)
val OnPrimaryContainerDark = Color(0xFF9EEFB2)

val SecondaryDark = Color(0xFFB3CCBB)
val OnSecondaryDark = Color(0xFF1F352B)
val SecondaryContainerDark = Color(0xFF354B3F)
val OnSecondaryContainerDark = Color(0xFFCEE8D7)

val TertiaryDark = Color(0xFFA0CFD4)
val OnTertiaryDark = Color(0xFF00363A)
val TertiaryContainerDark = Color(0xFF1F4E52)
val OnTertiaryContainerDark = Color(0xFFBCEBEF)

val BackgroundDark = Color(0xFF111412)
val OnBackgroundDark = Color(0xFFE2E3DE)
val SurfaceDark = Color(0xFF111412)
val OnSurfaceDark = Color(0xFFE2E3DE)
val SurfaceVariantDark = Color(0xFF414942)
val OnSurfaceVariantDark = Color(0xFFBFC9C2)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// Semantic Financial Colors (Dark)
val SuccessDark = Color(0xFF4ADE80)
val ExpenseDark = Color(0xFFF87171)
val WarningDark = Color(0xFFFB923C)
val WarningContainerDark = Color(0xFF3D2314)

object FinancialColors {
    val success: Color
        @Composable
        get() = if (isSystemInDarkTheme()) SuccessDark else SuccessLight

    val expense: Color
        @Composable
        get() = if (isSystemInDarkTheme()) ExpenseDark else ExpenseLight

    val warning: Color
        @Composable
        get() = if (isSystemInDarkTheme()) WarningDark else WarningLight

    val warningContainer: Color
        @Composable
        get() = if (isSystemInDarkTheme()) WarningContainerDark else WarningContainerLight
}
