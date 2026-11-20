package dev.hamper.tabula.screens

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.Env
import dev.hamper.tabula.data.Answer
import dev.hamper.tabula.data.Generators
import dev.hamper.tabula.platform.installJvmAssets
import dev.hamper.tabula.ui.KeyPad
import dev.hamper.tabula.ui.TabulaTheme
import dev.hamper.tabula.ui.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * One keypad for every number field, as on a calculator (Teo, 17. 11.): every key keeps its place in ℝ, with
 * fractions, in ℤ₅ and in ℤ₇; a key the field cannot take is there but disabled.
 */
@OptIn(ExperimentalTestApi::class)
class KeyPadUiTest {
    private val keys = Pad.flatten()

    @Test fun everyKeyKeepsItsPlaceInEveryDomain() = runComposeUiTest {
        Env.install(debug = true, clipsGuestToken = "", versionName = "test")
        installJvmAssets()
        val off = mutableStateOf(padOff(null, true))
        setContent { TabulaTheme(ThemeMode.Light) { KeyPad(Pad, {}, Modifier.width(360.dp), off = off.value) } }
        fun places() = keys.associateWith { onNodeWithTag("key:$it").fetchSemanticsNode().boundsInRoot }
        val fractions = places()
        for ((domain, o) in listOf("ℝ" to padOff(null, false), "ℤ₅" to padOff(5, false), "ℤ₇" to padOff(7, false))) {
            off.value = o; waitForIdle()
            assertEquals("keys moved in $domain", fractions, places())
        }
        // ℤ₇: 0 to 6 typed, 7, 8, 9, the sign and the fraction bar there but off
        for (k in listOf("0", "6")) onNodeWithTag("key:$k").assertIsEnabled()
        for (k in listOf("7", "8", "9", "−", "/")) onNodeWithTag("key:$k").assertIsNotEnabled()
        onNodeWithTag("key:⌫").assertIsEnabled()
        off.value = padOff(null, false); waitForIdle()
        onNodeWithTag("key:/").assertIsNotEnabled(); onNodeWithTag("key:−").assertIsEnabled(); onNodeWithTag("key:9").assertIsEnabled()
    }

    @Test fun everyFieldOverZpHasItsDigitsOnThePad() {
        // a ℤ_p cell is one key: p up to 10 fits 0..9
        for ((name, gen) in Generators) repeat(10) { seed ->
            val ex = gen(Random(seed))
            val answers = ArrayList<Any>()
            for (b in ex.parts) {
                val part = b(answers)
                (part.answer as? Answer.Numbers)?.p?.let { assertTrue("$name: ℤ_$it does not fit the pad", it <= 10) }
                (part.answer as? Answer.Vectors)?.p?.let { assertTrue("$name: ℤ_$it does not fit the pad", it <= 10) }
                answers += when (val a = part.answer) {
                    is Answer.Choice -> a.correct
                    is Answer.Numbers -> a.expected
                    is Answer.Pick -> (0 until (1 shl a.options.size)).map { m -> (0 until a.options.size).filter { m shr it and 1 == 1 }.toSet() }.first { it.size in a.count && a.ok(it) }
                    is Answer.Vectors -> a.example
                    is Answer.Paper -> 1
                }
            }
        }
    }
}
