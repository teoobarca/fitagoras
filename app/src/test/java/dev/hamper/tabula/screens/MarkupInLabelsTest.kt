package dev.hamper.tabula.screens

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import dev.hamper.tabula.math.MathFonts
import dev.hamper.tabula.ui.Quiet
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A quiet button named after a lesson with math ("Zopakovať: Parameter $a$") shows the formula, not its markup. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [37], qualifiers = "w411dp-h891dp-xxhdpi")
class MarkupInLabelsTest {
    @get:Rule val rule = createComposeRule()

    @Test fun quiet_label_with_math_is_set() {
        dev.hamper.tabula.platform.AndroidContext.install(ApplicationProvider.getApplicationContext())
        rule.setContent { TabulaTheme(ThemeMode.Light) { Quiet("Zopakovať: Parameter \$a\$ a §(\\mathbf{x},\\mathbf{y})§ nie je báza", {}) } }
        val texts = rule.onAllNodes(androidx.compose.ui.test.SemanticsMatcher("any") { true }, useUnmergedTree = true).fetchSemanticsNodes()
            .flatMap { n -> n.config.getOrElseNullable(SemanticsProperties.Text) { null }.orEmpty().map { it.text } }
        assertTrue("no text at all", texts.any { "Zopakovať" in it })
        texts.forEach { t -> assertFalse("markup shown: $t", '$' in t || '§' in t || "\\mathbf" in t) }
    }
}
