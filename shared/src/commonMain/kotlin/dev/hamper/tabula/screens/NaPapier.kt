package dev.hamper.tabula.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import dev.hamper.tabula.math.Rich
import dev.hamper.tabula.res.Res
import dev.hamper.tabula.res.ic_camera
import dev.hamper.tabula.res.ic_close
import dev.hamper.tabula.res.ic_openai
import dev.hamper.tabula.tutor.LocalTutor
import dev.hamper.tabula.tutor.Posudok
import dev.hamper.tabula.tutor.Pripojenie
import dev.hamper.tabula.tutor.PrepisTextu
import dev.hamper.tabula.tutor.TutorChyba
import dev.hamper.tabula.ui.*

/*
 * Case 2 on screen (docs/TUTOR.md, "Na papier"): on every "na papier" checklist one button, "Odfoť riešenie". The tutor
 * reads the paper, says at each item whether it is there, with what the student wrote, and ticks what it found; the
 * student can change every tick, and ticking by hand stays the way without the tutor.
 */

/** The tutor's full-screen frame in the new look: close, a kicker, the content, the plan line at the bottom. */
@Composable
fun TutorRam(kicker: String, onZavri: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = T.c
    Column(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 6.dp, end = 12.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).clickable(role = Role.Button, onClick = onZavri).semantics { contentDescription = "Zavrieť" }, contentAlignment = Alignment.Center) {
                Icon24(Res.drawable.ic_close, tint = c.ink)
            }
            Spacer(Modifier.width(4.dp))
            Text(kicker, style = MaterialTheme.typography.labelMedium, color = c.ink2, modifier = Modifier.weight(1f))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), content = content)
    }
}

@Composable
fun Caka(nadpis: String, popis: String) {
    val c = T.c
    // a waiting or failed state stands in the upper third with Pytagoras over it, the way the sign-in screen does,
    // not as a line of text on top of an empty page (sweep of 18. 11.)
    Spacer(Modifier.height(96.dp))
    Pytagoras(Mood.Thinking, size = 80.dp)
    Spacer(Modifier.height(16.dp))
    Text(nadpis, style = MaterialTheme.typography.headlineSmall, color = c.ink, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(8.dp))
    Text(popis, style = MaterialTheme.typography.bodyMedium, color = c.ink2)
    Spacer(Modifier.height(24.dp))
    androidx.compose.material3.LinearProgressIndicator(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = c.lesson.main, trackColor = c.sand)
}

@Composable
fun ChybaTutora(e: TutorChyba, onZavri: () -> Unit, onZnova: () -> Unit, onVyuzitie: () -> Unit) {
    val c = T.c
    val (title, body) = when (e.druh) {
        TutorChyba.Druh.LIMIT -> "Dosiahol si limit ChatGPT" to "Tvoj ChatGPT plán teraz ďalšie požiadavky nepustí. Fotky ti nechám, skús to neskôr."
        TutorChyba.Druh.OFFLINE -> "Si offline" to "Fotky ti nechám. Keď budeš online, skús to znova."
        TutorChyba.Druh.NEDOSTUPNE -> "ChatGPT teraz neodpovedá" to "Skús to o chvíľu znova, fotky ti nechám."
        TutorChyba.Druh.PRERUSENE -> "Spojenie sa prerušilo" to "Čo už prišlo, ostalo. Skús to znova."
        TutorChyba.Druh.ODPOJENE -> "ChatGPT je odpojený" to "Pripoj ho znova v Nastaveniach, potom to skús znova."
        TutorChyba.Druh.NEPOVOLENE -> "Tvoj ChatGPT plán toto nepovoľuje" to "Pytagoras potrebuje povolenie používať plán. Pozri Nastavenia."
        TutorChyba.Druh.NEPODPOROVANE, TutorChyba.Druh.INE -> "Toto teraz nejde" to "Niečo sa pokazilo na strane appky alebo ChatGPT (${e.kod})."
    }
    Spacer(Modifier.height(96.dp))
    Pytagoras(Mood.Thinking, size = 80.dp, bg = c.art.mascotBad)
    Spacer(Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.headlineSmall, color = c.ink, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(8.dp))
    Text(body, style = MaterialTheme.typography.bodyMedium, color = c.ink2)
    Spacer(Modifier.height(20.dp))
    when (e.druh) {
        TutorChyba.Druh.LIMIT -> ButtonRow { Second("Zavrieť", onZavri); Cta("Spravovať využitie", onVyuzitie) }
        TutorChyba.Druh.ODPOJENE, TutorChyba.Druh.NEPOVOLENE -> Second("Zavrieť", onZavri)
        else -> ButtonRow { Second("Zavrieť", onZavri); Cta("Skúsiť znova", onZnova) }
    }
}

/**
 * The first use, where the tutor is used (Teo, 18. 11.: "neviem, či to chcem cez Nastavenia ... pekné tlačidlo
 * Prihlásiť sa s ChatGPT"): what it gives in one sentence, the sign-in button, and in small type what goes where. The
 * three cards of text it had were too much. Once signed in the caller goes on by itself: the camera opens, or the
 * question's field.
 */
@Composable
internal fun PrvePripojenie(t: dev.hamper.tabula.tutor.TutorSluzba, p: Pripojenie, onZavri: () -> Unit) {
    val c = T.c
    TutorRam("Pytagoras", onZavri) {
        Spacer(Modifier.weight(1f))
        Box(Modifier.align(Alignment.CenterHorizontally)) { Pytagoras(Mood.Pleased, size = 88.dp) }
        Spacer(Modifier.height(18.dp))
        H1("Pytagoras skontroluje tvoj papier a poradí s krokom")
        Spacer(Modifier.height(8.dp))
        Text("Je to AI pomocník: beží cez tvoj účet ChatGPT a môže sa pomýliť. Stačí sa raz prihlásiť.", style = MaterialTheme.typography.bodyLarge, color = c.ink2)
        Spacer(Modifier.height(22.dp))
        when {
            p is Pripojenie.Pripaja -> {
                Text("Čakám na prihlásenie v prehliadači. Potom sa vrátiš sem.", style = MaterialTheme.typography.bodyMedium, color = c.ink2)
                Spacer(Modifier.height(12.dp))
                Second("Otvoriť prihlásenie znova", { t.pripoj() })
                t.CakamNaPrihlasenie()
                Quiet("Zrušiť", { t.zrusPrihlasenie() })
            }
            p is Pripojenie.Pripojeny && !p.planPovoleny -> {
                Text("Si prihlásený, ale appka ešte nemá povolenie používať tvoj plán.", style = MaterialTheme.typography.bodyMedium, color = c.badInk)
                Spacer(Modifier.height(12.dp))
                ChatGptTlacidlo("Povoliť v ChatGPT") { t.pripoj(suhlas = true) }
            }
            else -> ChatGptTlacidlo("Prihlásiť sa cez ChatGPT") { t.pripoj() }
        }
        Spacer(Modifier.height(12.dp))
        Text("Posiela sa len to, na čo sa pýtaš (otázka, fotka papiera, text úlohy), a ide to cez tvoj ChatGPT plán. Prihlásenie ostáva ${t.kdePrihlasenie}, heslo appka nevidí.",
            style = MaterialTheme.typography.bodySmall, color = c.ink3)
        Spacer(Modifier.weight(1f))
    }
}

/**
 * The sign-in button: ink on the page, the brand's own name in it, not the app's accent (a sign-in with someone
 * else's account is not the app's way on); full width, 56 dp. OpenAI's logo before the label (Teo, 19. 11.), in the
 * label's colour, as the app's buttons carry an icon (24 dp, 10 dp gap, stepping aside at a font over 130 %).
 */
@Composable
internal fun ChatGptTlacidlo(label: String, onClick: () -> Unit) {
    val c = T.c
    val ink = if (c.dark) androidx.compose.ui.graphics.Color(0xFFF5F2EA) else androidx.compose.ui.graphics.Color(0xFF111111)
    val on = if (c.dark) androidx.compose.ui.graphics.Color(0xFF111111) else androidx.compose.ui.graphics.Color.White
    Box(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(28.dp)).background(ink).clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick).padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (androidx.compose.ui.platform.LocalDensity.current.fontScale <= 1.3f) { Icon24(Res.drawable.ic_openai, tint = on); Spacer(Modifier.width(10.dp)) }
            Text(label, style = MaterialTheme.typography.labelLarge, color = on, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

/** What the student's paper got, one value per step or part: by the student's own tap or by a sure verdict of the tutor. */
object Hodnotenie {
    const val MAM = 1
    const val NEMAM = 0
    const val TUTOR_UZNAL = 11
    const val TUTOR_NEUZNAL = 10
    /** true: it counts as done; false: a mistake; null: nothing said (the step is put off, never a mistake). */
    fun ok(h: Int?): Boolean? = when (h) { MAM, TUTOR_UZNAL -> true; NEMAM, TUTOR_NEUZNAL -> false; else -> null }
}

/**
 * After the sample, one way (Teo, 18. 11.: "teraz je toho too much … nikdy sa toľko neodklikám"): photograph the paper
 * and get a teacher's verdict in the step; without the tutor one tap, "Mám to" or "Nemám", or nothing at all. The
 * checklist is only background to unfold ("Na čo sa pozerá cvičiaci"), never ticked, no points at its lines.
 * [hodnotenie] is the one value the caller keeps (Hodnotenie); the last word wins: a tap replaces a verdict and a new
 * sure verdict replaces a tap. A verdict the tutor is unsure of changes nothing, the student still says it.
 */
@Composable
fun PorovnanieSPapierom(
    miesto: () -> dev.hamper.tabula.tutor.MiestoAppky, podklad: List<String>, hodnotenie: Int?, onHodnotenie: (Int?, Posudok?) -> Unit,
    navyse: List<String> = emptyList(),
) {
    val c = T.c
    // the newest photo's verdict on screen (null: none yet, or an unsure one)
    var vidno by remember { mutableStateOf<Posudok.Verdikt?>(null) }
    PapierKontrola(miesto) { ps, _ ->
        if (ps.istota != "nizka") { vidno = ps.verdikt; onHodnotenie(if (ps.verdikt == Posudok.Verdikt.NEUZNAL_BY) Hodnotenie.TUTOR_NEUZNAL else Hodnotenie.TUTOR_UZNAL, ps) }
        // an unsure verdict on a new photo: an older photo's verdict no longer speaks for this paper, the student does
        else { vidno = null; if (hodnotenie == Hodnotenie.TUTOR_UZNAL || hodnotenie == Hodnotenie.TUTOR_NEUZNAL) onHodnotenie(null, ps) }
    }
    // the student can always say it themselves, also against a verdict (the last word counts)
    run {
        Spacer(Modifier.height(14.dp))
        when {
            vidno != null -> Text("Nesúhlasíš? Povedz to sám:", style = MaterialTheme.typography.labelLarge, color = c.ink2)
            hodnotenie == Hodnotenie.TUTOR_UZNAL || hodnotenie == Hodnotenie.TUTOR_NEUZNAL -> Text((if (hodnotenie == Hodnotenie.TUTOR_UZNAL) "Pytagoras by to uznal." else "Pytagoras by to neuznal.") + " Môžeš to zmeniť:", style = MaterialTheme.typography.labelLarge, color = c.ink2)
            else -> Text(if (LocalTutor.current != null) "Alebo sám, bez fotky:" else "Máš to na papieri?", style = MaterialTheme.typography.labelLarge, color = c.ink2)
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val mam = hodnotenie == Hodnotenie.MAM; val nemam = hodnotenie == Hodnotenie.NEMAM
            Choice("Mám to", { onHodnotenie(if (mam) null else Hodnotenie.MAM, null) }, Modifier.weight(1f).semantics { selected = mam }, state = if (mam) Pick.Picked else Pick.Idle, center = true, minHeight = 52.dp)
            Choice("Nemám", { onHodnotenie(if (nemam) null else Hodnotenie.NEMAM, null) }, Modifier.weight(1f).semantics { selected = nemam }, state = if (nemam) Pick.Picked else Pick.Idle, center = true, minHeight = 52.dp)
        }
    }
    if (podklad.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        var otvor by remember { mutableStateOf(false) }
        Quiet(if (otvor) "Skryť podklad" else "Na čo sa pozerá cvičiaci", { otvor = !otvor }, color = c.ink2)
        if (otvor) {
            podklad.forEach { p -> Row(Modifier.padding(bottom = 6.dp)) { Text("•  ", style = MaterialTheme.typography.bodyMedium, color = c.ink2); Rich(p, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = c.ink2) } }
            if (navyse.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Navyše zo skrípt, netreba", style = MaterialTheme.typography.labelLarge, color = c.ink3)
                Spacer(Modifier.height(4.dp))
                navyse.forEach { e -> Row(Modifier.padding(bottom = 6.dp)) { Text("•  ", style = MaterialTheme.typography.bodySmall, color = c.ink3); Rich(e, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = c.ink3) } }
            }
        }
    }
}
